/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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

import cc.redberry.core.indexgenerator.IndexGeneratorImpl;
import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.utils.MathUtils;
import cc.redberry.core.parser.*;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.IntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * AST transformer, which inserts additional indices to specified tensors.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 * @deprecated should be replaced with {@link GeneralIndicesInsertion}
 */
@Deprecated
public final class IndicesInsertion implements ParseTokenTransformer {

    private final int[] upper, lower;
    private final Indicator<ParseTokenSimpleTensor> indicator;

    public IndicesInsertion(SimpleIndices upper, SimpleIndices lower, Indicator<ParseTokenSimpleTensor> indicator) {
        checkIndices(upper, lower);
        int[] upperArray = new int[upper.size()];
        for (int i = upper.size() - 1; i >= 0; --i)
            upperArray[i] = IndicesUtils.getNameWithType(upper.get(i));
        this.upper = upperArray;
        this.lower = lower.getAllIndices().copy();
        this.indicator = indicator;
    }

    private static void checkIndices(SimpleIndices upper, SimpleIndices lower) {
        if (upper.size() != lower.size())
            throw new IllegalArgumentException("Upper indices size not equal to lower indices size.");
        int size = upper.size();
        for (int i = 0; i < size; ++i) {
            if (!IndicesUtils.getState(upper.get(i)) || IndicesUtils.getState(lower.get(i)))
                throw new IllegalArgumentException();
            if (IndicesUtils.getType(upper.get(i)) != IndicesUtils.getType(lower.get(i)))
                throw new IllegalArgumentException();
            if (i != 0)
                if (IndicesUtils.getType(upper.get(i - 1)) != IndicesUtils.getType(upper.get(i)))
                    throw new IllegalArgumentException("Many types.");
        }
    }

    @Override
    public ParseToken transform(ParseToken node) {
        final int[] freeIndices = node.getIndices().getFree().getAllIndices().copy();
        int i;
        for (i = 0; i < freeIndices.length; ++i)
            freeIndices[i] = IndicesUtils.getNameWithType(freeIndices[i]);

        Arrays.sort(freeIndices);
        for (i = upper.length - 1; i >= 0; --i)
            if (Arrays.binarySearch(freeIndices, upper[i]) >= 0)
                throw new IllegalArgumentException("Inconsistent indices.");
        for (i = lower.length - 1; i >= 0; --i)
            if (Arrays.binarySearch(freeIndices, lower[i]) >= 0)
                throw new IllegalArgumentException("Inconsistent indices.");

        Set<Integer> dummyIndices = ParseUtils.getAllIndices(node);

        Arrays.sort(upper);
        Arrays.sort(lower);
        int[] upperLower = MathUtils.intSetUnion(upper, lower);

        int[] forbidden = new int[dummyIndices.size() + upperLower.length];
        i = -1;
        for (Integer f : dummyIndices)
            forbidden[++i] = f;
        System.arraycopy(upperLower, 0, forbidden, dummyIndices.size(), upperLower.length);


        IndexGeneratorImpl generator = new IndexGeneratorImpl(forbidden);
        IntArrayList from = new IntArrayList(), to = new IntArrayList();
        int fromIndex;
        for (i = upperLower.length - 1; i >= 0; --i) {
            fromIndex = upperLower[i];
            if (dummyIndices.contains(fromIndex)) {
                from.add(fromIndex);
                to.add(generator.generate(IndicesUtils.getType(fromIndex)));
            }
        }

        int[] _from = from.toArray(), _to = to.toArray();
        ArraysUtils.quickSort(_from, _to);

        IITransformer transformer = createTransformer(node, indicator);
        if (transformer != null)
            transformer.apply(new IndexMapper(_from, _to), new IGWrapper(generator), upper, lower);
        return node;
    }

    private static class IndexMapper implements IndexMapping {

        private final int[] from, to;

        public IndexMapper(int[] from, int[] to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public int map(int index) {
            int position = Arrays.binarySearch(from, IndicesUtils.getNameWithType(index));
            if (position < 0)
                return index;
            return IndicesUtils.getRawStateInt(index) ^ to[position];
        }
    }

    private static IITransformer createTransformer(ParseToken node, Indicator<ParseTokenSimpleTensor> indicator) {
        IITransformer t;
        switch (node.tokenType) {
            case TensorField:
            case SimpleTensor:
                if (indicator.is((ParseTokenSimpleTensor) node))
                    return new SimpleTransformer((ParseTokenSimpleTensor) node);
                else
                    return null;
            case Product:
            case Expression:
            case Sum:
                List<IITransformer> transformers = new ArrayList<>();
                for (ParseToken _node : node.content)
                    if ((t = createTransformer(_node, indicator)) != null)
                        transformers.add(t);
                if (transformers.isEmpty())
                    return null;
                else if (transformers.size() == 1)
                    return transformers.get(0);
                else if (node.tokenType == TokenType.Product)
                    return new ProductTransformer(transformers.toArray(new IITransformer[transformers.size()]));
                else
                    return new SumTransformer(transformers.toArray(new IITransformer[transformers.size()]));
            default:
                return null;
        }
    }

    private static interface IITransformer {

        void apply(IndexMapper indexMapper, IGWrapper generator, int[] upper, int[] lower);
    }

    private static class SimpleTransformer implements IITransformer {

        private final ParseTokenSimpleTensor node;

        public SimpleTransformer(ParseTokenSimpleTensor node) {
            this.node = node;
        }

        @Override
        public void apply(IndexMapper indexMapper, IGWrapper generator, int[] upper, int[] lower) {
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
        public void apply(IndexMapper indexMapper, IGWrapper generator, int[] upper, int[] lower) {
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
        public void apply(IndexMapper indexMapper, IGWrapper generator, int[] upper, int[] lower) {
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

        private IndexGeneratorImpl generator;
        private int generated;

        public IGWrapper(IndexGeneratorImpl generator) {
            this.generator = generator;
        }

        public IGWrapper(IndexGeneratorImpl generator, int generated) {
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
