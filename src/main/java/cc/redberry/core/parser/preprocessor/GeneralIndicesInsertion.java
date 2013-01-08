/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
 *   Stanislav Poslavsky   <stvlpos@mail.ru>
 *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
 *
 * This file is part of Redberry.
 *
 * Redberry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Redberry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Redberry. If not, see <http://www.gnu.org/licenses/>.
 */
package cc.redberry.core.parser.preprocessor;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.IndicesTypeStructureAndName;
import cc.redberry.core.context.NameDescriptor;
import cc.redberry.core.indexgenerator.IndexGenerator;
import cc.redberry.core.indices.*;
import cc.redberry.core.parser.*;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.BitArray;
import cc.redberry.core.utils.ByteBackedBitArray;
import cc.redberry.core.utils.IntArrayList;

import java.util.*;

import static cc.redberry.core.indices.IndexType.TYPES_COUNT;

/**
 * ParseNodeTransformer facilitating input of matrices and vectors with omitted indices.
 * <p/>
 * <p>It is useful in situations where one is faced with the need to input many huge matrix expressions, and manual
 * indices insertion becomes a complex task.</p>
 */
public class GeneralIndicesInsertion implements ParseTokenTransformer {
    private final Map<IndicesTypeStructureAndName, InsertionRule> initialRules = new HashMap<>();
    private Map<IndicesTypeStructureAndName, InsertionRule> mappedRules;

    /**
     * Creates blank GeneralIndicesInsertion transformer.
     */
    public GeneralIndicesInsertion() {
    }

    /**
     * Adds new insertion rule to this transformer.
     * <p/>
     * <p>After rule is added you can omit indices of specified type in specified simple tensors, when this transformer
     * is passed to {@link cc.redberry.core.tensor.Tensors#parse(String, cc.redberry.core.parser.ParseTokenTransformer...)}
     * method or somehow added to default parse nodes preprocessor.</p>
     *
     * @param tensor           simple tensor
     * @param omittedIndexType type of indices that may be omitted
     */
    public void addInsertionRule(SimpleTensor tensor, IndexType omittedIndexType) {
        addInsertionRule(CC.getNameDescriptor(tensor.getName()), omittedIndexType);
    }

    public void addInsertionRule(NameDescriptor nd, IndexType omittedIndexType) {
        IndicesTypeStructureAndName originalStructureAndName = NameDescriptor.extractKey(nd);
        IndicesTypeStructure structure = nd.getIndicesTypeStructure();

        if (structure.getTypeData(omittedIndexType.getType()).length == 0)
            throw new IllegalArgumentException("No indices of specified type in tensor.");

        if (CC.isMetric(omittedIndexType.getType())) {
            int omittedIndicesCount = structure.getTypeData(omittedIndexType.getType()).length;
            if ((omittedIndicesCount % 2) == 1)
                throw new IllegalArgumentException("The number of omitted indices for metric types should be even.");
            omittedIndicesCount /= 2;

            BitArray omittedIndices = structure.getTypeData(omittedIndexType.getType()).states;

            for (int i = 0, size = omittedIndices.size(); i < size; ++i) {
                if (i < omittedIndicesCount && !omittedIndices.get(i))
                    throw new IllegalArgumentException("Inconsistent states signature for metric type.");
                if (i >= omittedIndicesCount && omittedIndices.get(i))
                    throw new IllegalArgumentException("Inconsistent states signature for metric type.");
            }
        }
        mappedRules = null;
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
                if (mappedRules.put(key, rule) != null)
                    throw new RuntimeException("Conflicting insertion rules.");
    }

    @Override
    public ParseToken transform(ParseToken node) {
        ensureMappedRulesInitialized();
        int[] forbidden = ParseUtils.getAllIndicesT(node).toArray();
        IndexGenerator generator = new IndexGenerator(forbidden);

        transformInsideFieldsAndScalarFunctions(node);

        ParseToken wrapped = new ParseToken(TokenType.Dummy, node);
        IITransformer transformer = createTransformer(wrapped);
        node = wrapped.content[0];
        node.parent = null;

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

    private void transformInsideFieldsAndScalarFunctions(ParseToken pn) {
        if (pn.tokenType == TokenType.TensorField) {
            ParseTokenTensorField pntf = (ParseTokenTensorField) pn;
            if (!pntf.name.equalsIgnoreCase("tr"))
                for (int i = 0; i < pn.content.length; ++i) {
                    ParseToken newArgNode = transform(pntf.content[i]);
                    pntf.content[i] = newArgNode;
                    newArgNode.parent = pntf;

                    SimpleIndices oldArgIndices = pntf.argumentsIndices[i];
                    if (oldArgIndices != null) {
                        IntArrayList newArgIndices = new IntArrayList(oldArgIndices.getAllIndices().copy());
                        Indices newIndices = newArgNode.getIndices();
                        for (byte j = 0; j < TYPES_COUNT; ++j) {
                            if (oldArgIndices.size(IndexType.getType(j)) < newIndices.size(IndexType.getType(j))) {
                                if (oldArgIndices.size(IndexType.getType(j)) != 0)
                                    throw new IllegalArgumentException("Error in field arg indices.");
                                newArgIndices.addAll(newIndices.getOfType(IndexType.getType(j)).getAllIndices());
                            }
                        }
                        pntf.argumentsIndices[i] = IndicesFactory.createSimple(null, newArgIndices.toArray());
                    }
                }
        }
        if (pn.tokenType == TokenType.Power || pn.tokenType == TokenType.ScalarFunction) {
            for (int i = 0; i < pn.content.length; ++i)
                pn.content[i] = transform(pn.content[i]);
        }
        for (int i = 0; i < pn.content.length; ++i)
            transformInsideFieldsAndScalarFunctions(pn.content[i]);
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
        public static final OuterIndices EMPTY = new OuterIndices();
        final int[] upper, lower;
        final boolean[] initialized;

        OuterIndices() {
            upper = new int[TYPES_COUNT];
            lower = new int[TYPES_COUNT];
            initialized = new boolean[TYPES_COUNT];
        }

        private OuterIndices(int[] upper, int[] lower, boolean[] initialized) {
            this.upper = upper;
            this.lower = lower;
            this.initialized = initialized;
        }

        public void init() {
            for (int i = 0; i < TYPES_COUNT; ++i)
                initialized[i] = (upper[i] != 0 || lower[i] != 0);
        }

        public void cumulativeAggregate(OuterIndices other) {
            for (int i = 0; i < TYPES_COUNT; ++i)
                if (other.initialized[i])
                    if (initialized[i]) {
                        if (upper[i] != other.upper[i] ||
                                lower[i] != other.lower[i])
                            throw new IllegalArgumentException("Inconsistent omitted indices exception.");
                    } else {
                        upper[i] = other.upper[i];
                        lower[i] = other.lower[i];
                        initialized[i] = true;
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
                initialized[i] |= other.initialized[i];
                int dif = other.upper[i] - lower[i];
                lower[i] = other.lower[i];
                if (dif < 0)
                    lower[i] -= dif;
                else
                    upper[i] += dif;
            }
        }

        @Override
        public boolean equals(Object o) {
            OuterIndices that = (OuterIndices) o;

            if (!Arrays.equals(initialized, that.initialized)) return false;
            if (!Arrays.equals(lower, that.lower)) return false;
            return Arrays.equals(upper, that.upper);
        }

        public OuterIndices clone() {
            return new OuterIndices(upper.clone(), lower.clone(), initialized.clone());
        }
    }

    //TODO into fields and scalar functions
    private IITransformer createTransformer(ParseToken node) {
        IITransformer t;
        switch (node.tokenType) {
            case TensorField:
                if (((ParseTokenTensorField) node).name.equalsIgnoreCase("tr")) {
                    Set<IndexType> types;
                    int i;
                    if (node.content.length == 1)
                        types = EnumSet.allOf(IndexType.class);
                    else {
                        types = new HashSet<>();
                        ParseToken pn;
                        IndexType type;

                        for (i = 1; i < node.content.length; ++i) {
                            if ((pn = node.content[i]).tokenType != TokenType.SimpleTensor)
                                throw new IllegalArgumentException("Error in trace indices list.");
                            if ((type = IndexType.fromShortString(((ParseTokenSimpleTensor) pn).name)) == null)
                                throw new IllegalArgumentException("Error in trace indices list.");
                            types.add(type);
                        }
                    }

                    ParseToken nested = node.content[0];
                    ParseToken parent = node.parent;
                    for (i = 0; i < parent.content.length; ++i) {
                        if (parent.content[i] == node) {
                            parent.content[i] = nested;
                            nested.parent = parent;
                            break;
                        }
                    }
                    assert i != parent.content.length;

                    IITransformer innerTransformer = createTransformer(nested);
                    if (innerTransformer == null)
                        return null;
                    return new TraceTransformer(innerTransformer, types);
                }
            case SimpleTensor:
                InsertionRule rule = mappedRules.get(((ParseTokenSimpleTensor) node).getIndicesTypeStructureAndName());
                if (rule != null)
                    return new SimpleTransformer((ParseTokenSimpleTensor) node,
                            rule);
                else
                    return null;
            case Product:
                List<IITransformer> transformersList = new ArrayList<>();
                for (ParseToken _node : node.content)
                    if ((t = createTransformer(_node)) != null)
                        transformersList.add(t);
                if (transformersList.isEmpty())
                    return null;
                else if (transformersList.size() == 1)
                    return transformersList.get(0);
                return new ProductTransformer(transformersList.toArray(new IITransformer[transformersList.size()]));
            case Expression:
                IITransformer lhsTransformer = createTransformer(node.content[0]),
                        rhsTransformer = createTransformer(node.content[1]);
                if (lhsTransformer == null && rhsTransformer == null)
                    return null;
                OuterIndices lhsOuterIndices = lhsTransformer == null ? OuterIndices.EMPTY :
                        lhsTransformer.getOuterIndices(),
                        rhsOuterIndices = rhsTransformer == null ? OuterIndices.EMPTY :
                                rhsTransformer.getOuterIndices();
                for (int i = 0; i < TYPES_COUNT; ++i)
                    if ((rhsOuterIndices.upper[i] != 0
                            || rhsOuterIndices.lower[i] != 0) && !lhsOuterIndices.initialized[i])
                        throw new IllegalArgumentException("Inconsistent matrix expression.");
                return new SumTransformer(new IITransformer[]{lhsTransformer, rhsTransformer},
                        lhsOuterIndices, node);
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
            case Dummy:
                return createTransformer(node.content[0]);
            default:
                return null;
        }
    }

    private static interface IITransformer {

        OuterIndices getOuterIndices();

        void apply(IndexGenerator generator, int[][] upper, int[][] lower);
    }

    private static class SimpleTransformer implements IITransformer {

        private final ParseTokenSimpleTensor node;
        //private final InsertionRule insertionRule;
        private final OuterIndices outerIndices = new OuterIndices();

        public SimpleTransformer(ParseTokenSimpleTensor node, InsertionRule insertionRule) {
            this.node = node;
            //this.insertionRule = insertionRule;
            IndicesTypeStructure originalStructure = insertionRule.originalStructureAndName.getStructure()[0];
            IndicesTypeStructure currentStructure = node.getIndicesTypeStructureAndName().getStructure()[0];
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
            outerIndices.init();
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
        private final ParseToken parseToken;

        private SumTransformer(IITransformer[] transformers, OuterIndices outerIndices, ParseToken parseToken) {
            super(transformers);
            this.outerIndices = outerIndices;
            this.parseToken = parseToken;
        }

        @Override
        public OuterIndices getOuterIndices() {
            return outerIndices;
        }

        @Override
        public void apply(IndexGenerator generator, int[][] upper, int[][] lower) {
            IndexGenerator generatorTemp = null;
            IndexGenerator generatorClone;
            int[][] preparedUpper = new int[TYPES_COUNT][], preparedLower = new int[TYPES_COUNT][];
            OuterIndices oi;
            byte j;
            for (int i = 0; i < transformers.length; ++i) {
                if (transformers[i] == null)
                    oi = OuterIndices.EMPTY;
                else {
                    oi = transformers[i].getOuterIndices();

                    if (oi.equals(outerIndices)) {
                        System.arraycopy(upper, 0, preparedUpper, 0, TYPES_COUNT);
                        System.arraycopy(lower, 0, preparedLower, 0, TYPES_COUNT);
                    } else {
                        for (j = 0; j < TYPES_COUNT; ++j)
                            if (oi.initialized[j]) {
                                preparedUpper[j] = upper[j];
                                preparedLower[j] = lower[j];
                            } else {
                                preparedUpper[j] = new int[0];
                                preparedLower[j] = new int[0];
                            }
                    }

                    if (i != transformers.length - 1) {
                        transformers[i].apply(generatorClone = generator.clone(), preparedUpper, preparedLower);
                        if (generatorTemp == null)
                            generatorTemp = generatorClone;
                        else
                            generatorTemp.mergeFrom(generatorClone);
                    } else {
                        if (generatorTemp == null)
                            transformers[i].apply(generator, preparedUpper, preparedLower);
                        else {
                            transformers[i].apply(generatorTemp, preparedUpper, preparedLower);
                            generator.mergeFrom(generatorTemp);
                        }
                    }
                }
                parseToken.content[i] = addDeltas(oi, parseToken.content[i], outerIndices,
                        upper, lower);
            }
            if (generatorTemp != null)
                generator.mergeFrom(generatorTemp);
        }

        private ParseToken addDeltas(OuterIndices inserted, ParseToken node, OuterIndices expected,
                                    int[][] upper, int[][] lower) {
            List<ParseToken> multipliers = new ArrayList<>();
            for (byte i = 0; i < TYPES_COUNT; ++i) {
                if (!inserted.initialized[i] && expected.initialized[i]) {
                    if (expected.lower[i] == 0 && expected.upper[i] == 0)
                        continue;
                    if (expected.lower[i] != 1 || expected.upper[i] != 1)
                        throw new IllegalArgumentException("Deltas insertion is only supported for one upper and " +
                                "one lower omitted indices.");
                    multipliers.add(new ParseTokenSimpleTensor(IndicesFactory.createSimple(null, upper[i][0], lower[i][0]),
                            CC.current().getKroneckerName()));
                }
            }
            if (multipliers.isEmpty())
                return node;
            multipliers.add(node);
            return new ParseToken(TokenType.Product, multipliers.toArray(new ParseToken[multipliers.size()]));
        }
    }

    private final static class TransformersIndicesRange {
        final int from[], count[];

        public TransformersIndicesRange(int[] from, int[] count) {
            this.from = from;
            this.count = count;
        }
    }

    private static class TraceTransformer implements IITransformer {
        private final OuterIndices outerIndices;
        private final IITransformer innerTransformer;
        private final Set<IndexType> typesToContract;

        private TraceTransformer(IITransformer innerTransformer, Set<IndexType> typesToContract) {
            this.innerTransformer = innerTransformer;
            this.typesToContract = new HashSet<>(typesToContract);
            outerIndices = innerTransformer.getOuterIndices().clone();
            for (IndexType type : typesToContract) {
                if (outerIndices.upper[type.getType()] != outerIndices.lower[type.getType()])
                    throw new IllegalArgumentException("Illegal trace usage.");
                if (outerIndices.upper[type.getType()] == 0)
                    this.typesToContract.remove(type);
                outerIndices.upper[type.getType()] = outerIndices.lower[type.getType()] = 0;
            }
        }

        @Override
        public OuterIndices getOuterIndices() {
            return outerIndices;
        }

        @Override
        public void apply(IndexGenerator generator, int[][] upper, int[][] lower) {
            OuterIndices innerIndices = innerTransformer.getOuterIndices();
            int[][] preparedUpper = upper.clone(), preparedLower = lower.clone();
            int i, generated;
            int[] l, u;
            for (IndexType type : typesToContract) {
                l = new int[innerIndices.lower[type.getType()]];
                u = new int[innerIndices.lower[type.getType()]];
                for (i = 0; i < l.length; ++i) {
                    generated = generator.generate(type.getType());
                    l[i] = generated;
                    u[i] = 0x80000000 | generated;
                }
                preparedLower[type.getType()] = l;
                preparedUpper[type.getType()] = u;
            }
            innerTransformer.apply(generator, preparedUpper, preparedLower);
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
                    throw new IllegalArgumentException();
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
