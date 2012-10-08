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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.ProductBuilder;
import cc.redberry.core.tensor.ProductContent;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.ApplyIndexMapping;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.TensorUtils;
import java.util.Arrays;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class ProductSubstitution implements Transformation {

    static final SubstitutionProvider PRODUCT_SUBSTITUTION_PROVIDER = new SubstitutionProvider() {

        @Override
        public Transformation createSubstitution(Tensor from, Tensor to) {
            return new ProductSubstitution((Product) from, to);
        }
    };
    private final Complex fromFactor;
    private final Tensor to, fromIndexless[], fromData[];
    private final ProductContent fromContent;
    private final boolean symbolic;

    public ProductSubstitution(Product from, Tensor to) {
        this.fromFactor = from.getFactor();
        this.fromIndexless = from.getIndexless();
        this.fromContent = from.getContent();
        this.fromData = fromContent.getDataCopy();
        this.to = to;
        this.symbolic = TensorUtils.isSymbolic(to);
    }

    @Override
    public Tensor transform(Tensor tensor) {
        SubstitutionIterator iterator = new SubstitutionIterator(tensor);
        Tensor current;
        while ((current = iterator.next()) != null) {
            if (!(current instanceof Product))
                continue;

            while (current instanceof Product) {
                Product cp = (Product) current;
                IndexMappingBuffer buffer = null;

                final Tensor[] currentIndexless = cp.getIndexless();
                int[] indexlessBijection;
                IndexlessBijectionsPort indexlessPort = new IndexlessBijectionsPort(fromIndexless, currentIndexless);
                while ((indexlessBijection = indexlessPort.take()) != null) {
                    buffer = IndexMappings.createBijectiveProductPort(fromIndexless, extract(currentIndexless, indexlessBijection)).take();
                    if (buffer != null)
                        break;
                }
                if (buffer == null)
                    break;

                boolean sign = buffer.getSignum();
                buffer = null;
                ProductContent currentContent = cp.getContent();
                final Tensor[] currentData = currentContent.getDataCopy();
                int[] dataBijection;
                ProductsBijectionsPort dataPort = new ProductsBijectionsPort(fromContent, currentContent);
                while ((dataBijection = dataPort.take()) != null) {
                    buffer = IndexMappings.createBijectiveProductPort(fromData, extract(currentData, dataBijection)).take();
                    if (buffer != null)
                        break;
                }
                if (buffer == null)
                    break;

                buffer.addSignum(sign);
                Tensor newTo;
                if (symbolic)
                    newTo = to;
                else {
                    int[] forbidden = new int[iterator.forbiddenIndices().size()];
                    int c = -1;
                    for (Integer f : iterator.forbiddenIndices())
                        forbidden[++c] = f;
                    newTo = ApplyIndexMapping.applyIndexMapping(to, buffer, forbidden);
//                    if (newTo != to)
                    iterator.forbiddenIndices().addAll(TensorUtils.getAllIndicesNames(newTo));
                }

                Arrays.sort(indexlessBijection);
                Arrays.sort(dataBijection);

                ProductBuilder builder = new ProductBuilder();
                builder.put(newTo);

                int i;
                for (i = 0; i < currentIndexless.length; ++i)
                    if (Arrays.binarySearch(indexlessBijection, i) < 0)
                        builder.put(currentIndexless[i]);

                for (i = 0; i < currentData.length; ++i)
                    if (Arrays.binarySearch(dataBijection, i) < 0)
                        builder.put(currentData[i]);

                builder.put(cp.getFactor().divide(fromFactor));
                current = builder.build();
            }
            iterator.set(current);
        }
        return iterator.result();
    }

    private static Tensor[] extract(final Tensor[] source, final int[] positions) {
        Tensor[] r = new Tensor[positions.length];
        for (int i = 0; i < positions.length; ++i)
            r[i] = source[positions[i]];
        return r;
    }
}
