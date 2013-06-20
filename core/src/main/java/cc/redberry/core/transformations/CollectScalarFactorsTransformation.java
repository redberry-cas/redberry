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
package cc.redberry.core.transformations;

import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.ScalarsBackedProductBuilder;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.utils.TensorUtils;

/**
 * Puts together similar scalar subproducts in each product. For example, tensor A_m*A^m*A_n*A^n
 * will be transformed to tensor (A_m*A^m)**2.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1
 */
public class CollectScalarFactorsTransformation implements Transformation {
    /**
     * Singleton default instance.
     */
    public static final CollectScalarFactorsTransformation COLLECT_SCALAR_FACTORS
            = new CollectScalarFactorsTransformation();
    private final TraverseGuide traverseGuide;

    private CollectScalarFactorsTransformation() {
        this.traverseGuide = TraverseGuide.ALL;
    }

    /**
     * Creates transformation for particular parts of expressions, specified by traverse guide.
     *
     * @param traverseGuide specifies parts of expression to apply the transformation
     */
    public CollectScalarFactorsTransformation(TraverseGuide traverseGuide) {
        this.traverseGuide = traverseGuide;
    }

    @Override
    public Tensor transform(Tensor t) {
        return collectScalarFactors(t, traverseGuide);
    }

    /**
     * Puts together similar scalar subproducts in each product. For example, tensor A_m*A^m*A_n*A^n
     * will be transformed to tensor (A_m*A^m)**2.
     *
     * @param tensor tensor
     * @return the result
     */
    public static Tensor collectScalarFactors(Tensor tensor) {
        return collectScalarFactors(tensor, TraverseGuide.ALL);
    }

    /**
     * Puts together similar scalar subproducts in each product of expression, which can
     * be traversed with specified traverse guide.
     *
     * @param tensor        tensor
     * @param traverseGuide specifies parts of expression to apply the transformation
     * @return the result
     */
    public static Tensor collectScalarFactors(Tensor tensor, TraverseGuide traverseGuide) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(tensor, traverseGuide);
        Tensor current;
        while ((current = iterator.next()) != null) {
            if (current instanceof Product)
                iterator.set(collectScalarFactorsInProduct((Product) current));
        }
        return iterator.result();
    }

    /**
     * Puts together similar scalar subproducts in a given product. For example, tensor A_m*A^m*A_n*A^n
     * will be transformed to tensor (A_m*A^m)**2.
     *
     * @param product product
     * @return the result
     */
    public static Tensor collectScalarFactorsInProduct(Product product) {
        if (TensorUtils.isSymbolic(product))
            return product;
        ScalarsBackedProductBuilder builder = new ScalarsBackedProductBuilder(product.size(), 1, product.getIndices().getFree().size());
        builder.put(product);
        return builder.build();
    }
}
