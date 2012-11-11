package cc.redberry.core.parser.preprocessor;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.IndicesTypeStructureAndName;
import cc.redberry.core.context.NameDescriptor;
import cc.redberry.core.indexgenerator.IndexGenerator;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesTypeStructure;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.parser.ParseNode;
import cc.redberry.core.parser.ParseNodeSimpleTensor;
import cc.redberry.core.parser.ParseNodeTransformer;
import cc.redberry.core.parser.ParseUtils;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.ByteBackedBitArray;

import java.util.*;

import static cc.redberry.core.indices.IndexType.TYPES_COUNT;

public class GeneralIndicesInsertion implements ParseNodeTransformer {
    private final Map<IndicesTypeStructureAndName, InsertionRule> initialRules = new HashMap<>();
    private Map<IndicesTypeStructureAndName, InsertionRule> mappedRules;

    public void addInsertionRule(SimpleTensor tensor, IndexType omittedIndexType) {
        mappedRules = null;
        NameDescriptor nd = CC.getNameDescriptor(tensor.getName());
        IndicesTypeStructureAndName originalStructureAndName = NameDescriptor.extractKey(nd);
        if (tensor.getIndices().size(omittedIndexType) == 0)
            throw new IllegalArgumentException("No indices of specified type in tensor.");
        IndicesTypeStructure structure = tensor.getIndices().getIndicesTypeStructure();
        if (CC.isMetric(omittedIndexType.getType())) {
            if ((structure.getTypeData(omittedIndexType.getType()).length % 2) == 1)
                throw new IllegalArgumentException();
            //TODO ONLY M^i_j not (M_j^i or M^ijk_l)
        }
        InsertionRule rule = initialRules.get(originalStructureAndName);
        if (rule == null)
            initialRules.put(originalStructureAndName, rule = new InsertionRule(originalStructureAndName));
        rule.indicesAllowedToOmit.add(omittedIndexType);
    }

    private void ensureMappedRulesInitialized() {
        if (mappedRules != null)
            return;
        mappedRules = new HashMap<>();
        for (InsertionRule rule : initialRules.values())
            for (IndicesTypeStructureAndName key : rule.getKeys())
                mappedRules.put(key, rule);
    }

    @Override
    public ParseNode transform(ParseNode node) {
        ensureMappedRulesInitialized();
        int[] forbidden = ParseUtils.getAllIndicesT(node).toArray();
        IndexGenerator generator = new IndexGenerator(forbidden);

        IITransformer transformer = createTransformer(node);

        if (transformer == null)
            return node;

        OuterIndices outerIndices = transformer.getOuterIndices();
        int[][] upper = new int[TYPES_COUNT][],
                lower = new int[TYPES_COUNT][];

        int j;
        for (byte i = 0; i < TYPES_COUNT; ++i) {
            upper[i] = new int[outerIndices.upper[i]];
            for (j = 0; j < upper[i].length; ++j)
                upper[i][j] = 0x80000000 | generator.generate(i);

            lower[i] = new int[outerIndices.lower[i]];
            for (j = 0; j < lower[i].length; ++j)
                lower[i][j] = generator.generate(i);
        }
        transformer.apply(generator, upper, lower);
        return node;
    }

    private static class InsertionRule {
        final IndicesTypeStructureAndName originalStructureAndName;
        final Set<IndexType> indicesAllowedToOmit = new HashSet<>();

        private InsertionRule(IndicesTypeStructureAndName originalStructureAndName) {
            this.originalStructureAndName = originalStructureAndName;
        }

        public IndicesTypeStructureAndName[] getKeys() {
            IndexType[] toOmit = indicesAllowedToOmit.toArray(new IndexType[indicesAllowedToOmit.size()]);
            int omitted, i;
            IndicesTypeStructureAndName[] keys = new IndicesTypeStructureAndName[(1 << toOmit.length) - 1];
            int[] allCounts;
            ByteBackedBitArray[] states;
            for (omitted = 1; omitted <= keys.length; ++omitted) {
                allCounts = originalStructureAndName.getStructure()[0].getTypesCounts();
                states = originalStructureAndName.getStructure()[0].getStates();
                for (i = 0; i < toOmit.length; ++i)
                    if ((omitted & (1 << i)) != 0) {
                        allCounts[toOmit[i].getType()] = 0;
                        states[toOmit[i].getType()] = states[toOmit[i].getType()] == null ?
                                null : ByteBackedBitArray.EMPTY;
                    }
                IndicesTypeStructure[] structures = originalStructureAndName.getStructure().clone();
                structures[0] = new IndicesTypeStructure(allCounts, states);
                keys[omitted - 1] = new IndicesTypeStructureAndName(originalStructureAndName.getName(),
                        structures);
            }
            return keys;
        }
    }

    private static class OuterIndices {
        final int[] upper, lower;

        OuterIndices() {
            upper = new int[TYPES_COUNT];
            lower = new int[TYPES_COUNT];
        }

        private OuterIndices(int[] upper, int[] lower) {
            this.upper = upper;
            this.lower = lower;
        }

        public void cumulativeAggregate(OuterIndices other) {
            for (int i = 0; i < TYPES_COUNT; ++i) {
                if (upper[i] == other.upper[i] &&
                        lower[i] == other.lower[i])
                    continue;
                if (other.upper[i] == 0 && other.lower[i] == 0)
                    continue;

                if (upper[i] == 0 && lower[i] == 0) {
                    upper[i] = other.upper[i];
                    lower[i] = other.lower[i];
                    continue;
                }

                throw new IllegalArgumentException("Inconsistent omitted indices exception.");
            }
        }

        public void cumulativeAdd(OuterIndices other) {
            //uuuu        xxxx
            //    xxxxll      yyyyy

            //uuuu     llllxx
            //    llll        yyyyy

            //cV * cV = ll
            //cV * V = 0
            //V * cV = ul
            //V * M = uul
            //M * V = u
            //cV * M = l
            //V * cV * V =
            //V * V * M = uuul

            for (int i = 0; i < TYPES_COUNT; ++i) {
                int dif = other.upper[i] - lower[i];
                lower[i] = other.lower[i];
                if (dif < 0)
                    lower[i] -= dif;
                else
                    upper[i] += dif;
            }
        }

        public OuterIndices clone() {
            return new OuterIndices(upper.clone(), lower.clone());
        }
    }

    //TODO into fields and scalar functions
    private IITransformer createTransformer(ParseNode node) {
        IITransformer t;
        switch (node.tensorType) {
            case TensorField:

            case SimpleTensor:
                InsertionRule rule = mappedRules.get(((ParseNodeSimpleTensor) node).getIndicesTypeStructureAndName());
                if (rule != null)
                    return new SimpleTransformer((ParseNodeSimpleTensor) node,
                            rule);
                else
                    return null;

            case Product:
                List<IITransformer> transformersList = new ArrayList<>();
                for (ParseNode _node : node.content)
                    if ((t = createTransformer(_node)) != null)
                        transformersList.add(t);
                if (transformersList.isEmpty())
                    return null;
                else if (transformersList.size() == 1)
                    return transformersList.get(0);
                return new ProductTransformer(transformersList.toArray(new IITransformer[transformersList.size()]));
            case Expression:

            case Sum:
                IITransformer[] transformersArray = new IITransformer[node.content.length];
                int i;
                OuterIndices outerIndices = null, currentOI;
                for (i = 0; i < transformersArray.length; ++i) {
                    transformersArray[i] = createTransformer(node.content[i]);
                    if (transformersArray[i] != null) {
                        currentOI = transformersArray[i].getOuterIndices();
                        if (outerIndices != null)
                            outerIndices.cumulativeAggregate(currentOI);
                        else
                            outerIndices = currentOI.clone();
                    }
                }
                if (outerIndices == null)
                    return null;
                return new SumTransformer(transformersArray, outerIndices, node);
            default:
                return null;
        }
    }

    private static interface IITransformer {

        OuterIndices getOuterIndices();

        void apply(IndexGenerator generator, int[][] upper, int[][] lower);
    }

    private static class SimpleTransformer implements IITransformer {

        private final ParseNodeSimpleTensor node;
        private final InsertionRule insertionRule;
        private final OuterIndices outerIndices = new OuterIndices();

        public SimpleTransformer(ParseNodeSimpleTensor node, InsertionRule insertionRule) {
            this.node = node;
            this.insertionRule = insertionRule;
            IndicesTypeStructure originalStructure = insertionRule.originalStructureAndName.getStructure()[0];
            IndicesTypeStructure currentStructure = node.getIndicesTypeStructureAndName().getStructure()[0];
            int i;
            for (IndexType type : insertionRule.indicesAllowedToOmit)
                if (currentStructure.getStates(type).size() == 0) {
                    ByteBackedBitArray originalStates = originalStructure.getStates(type);
                    if (originalStates != null) {
                        outerIndices.upper[type.getType()] = originalStates.bitCount();
                        outerIndices.lower[type.getType()] = originalStates.size() - outerIndices.upper[type.getType()];
                    } else {
                        outerIndices.upper[type.getType()] = outerIndices.lower[type.getType()]
                                = originalStructure.typeCount(type.getType()) / 2;
                    }
                } else if (currentStructure.typeCount(type.getType()) !=
                        originalStructure.typeCount(type.getType()))
                    throw new IllegalArgumentException();
        }

        @Override
        public OuterIndices getOuterIndices() {
            return outerIndices;
        }

        @Override
        public void apply(IndexGenerator generator, int[][] upper, int[][] lower) {
            SimpleIndices oldIndices = node.indices;
            int[] result = ArraysUtils.addAll(oldIndices.getAllIndices().copy(), ArraysUtils.addAll(upper),
                    ArraysUtils.addAll(lower));
            node.indices = IndicesFactory.createSimple(null, result);
        }
    }

    private static abstract class MIITransformer implements IITransformer {

        protected final IITransformer[] transformers;

        public MIITransformer(IITransformer[] transformers) {
            this.transformers = transformers;
        }
    }

    private static class SumTransformer extends MIITransformer {
        private final OuterIndices outerIndices;
        private final ParseNode parseNode;

        private SumTransformer(IITransformer[] transformers, OuterIndices outerIndices, ParseNode parseNode) {
            super(transformers);
            this.outerIndices = outerIndices;
            this.parseNode = parseNode;
        }

        @Override
        public OuterIndices getOuterIndices() {
            return outerIndices;
        }

        @Override
        public void apply(IndexGenerator generator, int[][] upper, int[][] lower) {
            IndexGenerator generatorTemp = null;
            IndexGenerator generatorClone;
            for (int i = 0; i < transformers.length - 1; ++i) {
                transformers[i].apply(generatorClone = generator.clone(), upper, lower);
                if (generatorTemp == null)
                    generatorTemp = generatorClone;
                else
                    generatorTemp.mergeFrom(generatorClone);
            }
            transformers[transformers.length - 1].apply(generator, upper, lower);
            if (generatorTemp != null)
                generator.mergeFrom(generatorTemp);
        }
    }

    private final static class TransformersIndicesRange {
        final int from[], count[];

        public TransformersIndicesRange(int[] from, int[] count) {
            this.from = from;
            this.count = count;
        }
    }

    private static class ProductTransformer extends MIITransformer {
        private final OuterIndices outerIndices;

        public ProductTransformer(IITransformer[] transformers) {
            super(transformers);
            OuterIndices oi = null;
            for (IITransformer transformer : transformers) {
                if (oi == null)
                    oi = transformer.getOuterIndices().clone();
                else
                    oi.cumulativeAdd(transformer.getOuterIndices());
            }
            this.outerIndices = oi;
        }

        @Override
        public OuterIndices getOuterIndices() {
            return outerIndices;
        }

        @Override
        public void apply(IndexGenerator generator, int[][] upper, int[][] lower) {
            int i;
            byte j;
            int[] totalCountUpper = new int[TYPES_COUNT],
                    totalCountLower = new int[TYPES_COUNT];
            OuterIndices oi;
            TransformersIndicesRange[] upperRanges = new TransformersIndicesRange[transformers.length],
                    lowerRanges = new TransformersIndicesRange[transformers.length];

            for (i = 0; i < transformers.length; ++i) {
                oi = transformers[i].getOuterIndices();
                upperRanges[i] = new TransformersIndicesRange(totalCountUpper.clone(), oi.upper.clone());
                lowerRanges[i] = new TransformersIndicesRange(totalCountLower.clone(), oi.lower.clone());
                for (j = 0; j < TYPES_COUNT; ++j) {
                    totalCountUpper[j] += oi.upper[j];
                    totalCountLower[j] += oi.lower[j];
                }
            }

            int[][] totalUppers = new int[TYPES_COUNT][], totalLowers = new int[TYPES_COUNT][];
            for (j = 0; j < TYPES_COUNT; ++j) {
                totalUppers[j] = new int[totalCountUpper[j]];
                totalLowers[j] = new int[totalCountLower[j]];
                System.arraycopy(upper[j], 0, totalUppers[j], 0, upper[j].length);
                System.arraycopy(lower[j], 0, totalLowers[j],
                        totalCountLower[j] - lower[j].length, lower[j].length);
                if (totalCountLower[j] - lower[j].length != totalCountUpper[j] - upper[j].length)
                    throw new IllegalArgumentException("PIZDEC!");
                for (i = 0; i < totalCountUpper[j] - upper[j].length; ++i) {
                    totalLowers[j][i] = generator.generate(j);
                    totalUppers[j][i + upper[j].length] = totalLowers[j][i] | 0x80000000;
                }
            }

            for (i = 0; i < transformers.length; ++i) {
                int[][] cUpper = new int[TYPES_COUNT][], cLower = new int[TYPES_COUNT][];
                for (j = 0; j < TYPES_COUNT; ++j) {
                    cUpper[j] = Arrays.copyOfRange(totalUppers[j], upperRanges[i].from[j], upperRanges[i].from[j] + upperRanges[i].count[j]);
                    cLower[j] = Arrays.copyOfRange(totalLowers[j], lowerRanges[i].from[j], lowerRanges[i].from[j] + lowerRanges[i].count[j]);
                }
                transformers[i].apply(generator, cUpper, cLower);
            }
        }
    }
}
