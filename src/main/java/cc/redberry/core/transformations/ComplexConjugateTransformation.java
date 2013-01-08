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

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ComplexConjugateTransformation implements Transformation {
    public static final ComplexConjugateTransformation COMPLEX_CONJUGATE = new ComplexConjugateTransformation();

    private ComplexConjugateTransformation() {
    }

    @Override
    public Tensor transform(Tensor t) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t);
        Tensor c;
        while ((c = iterator.next()) != null)
            if (c instanceof Complex)
                iterator.set(((Complex) c).conjugate());
        return iterator.result();
    }
}
