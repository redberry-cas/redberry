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
package cc.redberry.core.transformations;

import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.ScalarsBackedProductBuilder;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.TensorLastIterator;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.utils.TensorUtils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class CollectScalarFactors implements Transformation {
    public static final CollectScalarFactors COLLECT_SCALAR_FACTORS = new CollectScalarFactors();
    private final TraverseGuide traverseGuide;

    private CollectScalarFactors() {
        this.traverseGuide = TraverseGuide.ALL;
    }

    public CollectScalarFactors(TraverseGuide traverseGuide) {
        this.traverseGuide = traverseGuide;
    }

    @Override
    public Tensor transform(Tensor t) {
        return collectScalarFactors(t, traverseGuide);
    }

    public static Tensor collectScalarFactors(Tensor tensor) {
        return collectScalarFactors(tensor, TraverseGuide.ALL);
    }

    public static Tensor collectScalarFactors(Tensor tensor, TraverseGuide traverseGuide) {
        TensorLastIterator iterator = new TensorLastIterator(tensor, traverseGuide);
        Tensor current;
        while ((current = iterator.next()) != null) {
            if (current instanceof Product)
                iterator.set(collectScalarFactorsInProduct((Product) current));
        }
        return iterator.result();
    }

    public static Tensor collectScalarFactorsInProduct(Product product) {
        if (TensorUtils.isSymbolic(product))
            return product;
        ScalarsBackedProductBuilder builder = new ScalarsBackedProductBuilder(product.size(), 1, product.getIndices().getFree().size());
        builder.put(product);
        return builder.build();
    }
}
