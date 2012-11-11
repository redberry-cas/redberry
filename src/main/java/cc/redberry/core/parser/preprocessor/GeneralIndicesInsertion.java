package cc.redberry.core.parser.preprocessor;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.IndicesTypeStructureAndName;
import cc.redberry.core.context.NameDescriptor;
import cc.redberry.core.indexgenerator.IndexGenerator;
import cc.redberry.core.indices.*;
import cc.redberry.core.parser.ParseNode;
import cc.redberry.core.parser.ParseNodeSimpleTensor;
import cc.redberry.core.parser.ParseNodeTransformer;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.utils.ByteBackedBitArray;

import java.util.*;

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
        if (mappedRules == null)
            return;
        mappedRules = new HashMap<>();
        for (InsertionRule rule : initialRules.values())
            for (IndicesTypeStructureAndName key : rule.getKeys())
                mappedRules.put(key, rule);
    }

    @Override
    public ParseNode transform(ParseNode node) {
        return null;
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
        final int[] upper = new int[IndexType.TYPES_COUNT],
                lower = new int[IndexType.TYPES_COUNT];
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
                    if ((t = createTransformer(_node, indicator)) != null)
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
                boolean notNull = false;
                for (i = 0; i < transformersArray.length; ++i)
                    if ((transformersArray[i] = createTransformer(node.content[i], indicator)) != null)
                        notNull = true;
                if (!notNull)
                    return null;
                else if (transformersArray.length == 1)
                    return transformersArray[0];
                return null;

            default:
                return null;
        }
    }

    private static interface IITransformer {

        OuterIndices getOuterIndices();

        void apply(IGWrapper generator, int[] upper, int[] lower);
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
                        outerIndices.lower[type.getType()] = originalStates.size() - outerIndices.lower[type.getType()];
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
        public void apply(IGWrapper generator, int[] upper, int[] lower) {
            SimpleIndices oldIndices = node.indices;
            int[] _newIndices = new int[oldIndices.size() + 2 * upper.length];
            int i;
            for (i = 0; i < oldIndices.size(); ++i)
                _newIndices[i] = indexMapper.map(oldIndices.get(i));
            System.arraycopy(upper, 0, _newIndices, oldIndices.size(), upper.length);
            System.arraycopy(lower, 0, _newIndices, oldIndices.size() + upper.length, lower.length);
            for (i = 0; i < upper.length; ++i)
                _newIndices[i + oldIndices.size()] |= 0x80000000;
            node.indices = IndicesFactory.createSimple(null, _newIndices);
        }
    }

    private static abstract class MIITransformer implements IITransformer {

        protected final IITransformer[] transformers;

        public MIITransformer(IITransformer[] transformers) {
            this.transformers = transformers;
        }
    }

    private static class SumTransformer extends MIITransformer {

        public SumTransformer(IITransformer[] transformers) {
            super(transformers);
        }

        @Override
        public void apply(IGWrapper generator, int[] upper, int[] lower) {
            IGWrapper generatorTemp = null;
            IGWrapper generatorClone;
            for (int i = 0; i < transformers.length - 1; ++i) {
                transformers[i].apply(indexMapper, generatorClone = generator.clone(), upper, lower);
                if (generatorTemp == null)
                    generatorTemp = generatorClone;
                else
                    generatorTemp.merge(generatorClone);
            }
            transformers[transformers.length - 1].apply(indexMapper, generator, upper, lower);
            if (generatorTemp != null)
                generator.merge(generatorTemp);
        }
    }

    private static class ProductTransformer extends MIITransformer {

        public ProductTransformer(IITransformer[] transformers) {
            super(transformers);
        }

        @Override
        public void apply(IGWrapper generator, int[] upper, int[] lower) {
            int i, j;
            int[] tempUpper = upper.clone(),
                    tempLower = new int[upper.length];
            for (i = 0; i < transformers.length - 1; ++i) {
                for (j = 0; j < upper.length; ++j)
                    tempLower[j] = generator.next(IndicesUtils.getType(lower[j]));
                transformers[i].apply(indexMapper, generator, tempUpper, tempLower);
                System.arraycopy(tempLower, 0, tempUpper, 0, tempUpper.length);
            }
            transformers[i].apply(indexMapper, generator, tempUpper, lower);
        }
    }

    private static class IGWrapper {
        private IndexGenerator generator;
        private int generated;

        public IGWrapper(IndexGenerator generator) {
            this.generator = generator;
        }

        public IGWrapper(IndexGenerator generator, int generated) {
            this.generator = generator;
            this.generated = generated;
        }

        public int next(byte type) {
            ++generated;
            return generator.generate(type);
        }

        public void merge(IGWrapper wrapper) {
            if (wrapper.generated > this.generated) {
                this.generated = wrapper.generated;
                this.generator = wrapper.generator;
            }
        }

        @Override
        public IGWrapper clone() {
            return new IGWrapper(generator.clone(), generated);
        }
    }
}
