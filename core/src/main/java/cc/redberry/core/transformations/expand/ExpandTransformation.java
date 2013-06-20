/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
package cc.redberry.core.transformations.expand;

import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.transformations.Transformation;

/**
 * Expands out products and positive integer powers in tensor.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class ExpandTransformation extends AbstractExpandTransformation {
    /**
     * The default instance.
     */
    public static final ExpandTransformation EXPAND = new ExpandTransformation();

    private ExpandTransformation() {
        super();
    }

    /**
     * Creates expand transformation with specified additional transformations to
     * be applied after each step of expand.
     *
     * @param transformations transformations to be applied after each step of expand
     */
    public ExpandTransformation(Transformation... transformations) {
        super(transformations);
    }

    /**
     * Creates expand transformation with specified additional transformations to
     * be applied after each step of expand and leaves unexpanded parts of expression specified by
     * {@code traverseGuide}.
     *
     * @param transformations transformations to be applied after each step of expand
     * @param traverseGuide   traverse guide
     */
    public ExpandTransformation(Transformation[] transformations, TraverseGuide traverseGuide) {
        super(transformations, traverseGuide);
    }

    /**
     * Expands out products and positive integer powers in tensor.
     *
     * @param tensor tensor
     * @return result
     */
    public static Tensor expand(Tensor tensor) {
        return EXPAND.transform(tensor);
    }

    /**
     * Expands out products and positive integer powers in tensor and applies specified transformations
     * after each step of expand.
     *
     * @param tensor          tensor
     * @param transformations to be applied after each step of expand
     * @return result
     */
    public static Tensor expand(Tensor tensor, Transformation... transformations) {
        return new ExpandTransformation(transformations).transform(tensor);
    }

    @Override
    protected Tensor expandProduct(Product product, Transformation[] transformations) {
        return ExpandUtils.expandProductOfSums(product, transformations);
    }
}
