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
package cc.redberry.core.tensor;

import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.substitutions.SubstitutionTransformation;
import cc.redberry.core.utils.TensorUtils;

/**
 * Representation of mathematical expression <i>A = B</i>. {@code Expression} also implements
 * {@link Transformation} and represents substitutions.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see ExpressionBuilder
 * @see ExpressionFactory
 */
public class Expression extends Tensor implements Transformation {

    private final Tensor right, left;
    private final Indices indices;

    Expression(Indices indices, Tensor left, Tensor right) {
        this.indices = indices;
        this.right = right;
        this.left = left;
    }

    @Override
    public Tensor get(int i) {
        switch (i) {
            case 0:
                return left;
            case 1:
                return right;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Indices getIndices() {
        return indices;
    }

    @Override
    protected int hash() {
        return 3 * left.hashCode() - 7 * right.hashCode();
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public String toString(OutputFormat mode) {
        return left.toString(mode) + " = " + right.toString(mode);
    }

    @Override
    public TensorBuilder getBuilder() {
        return new ExpressionBuilder();
    }

    @Override
    public TensorFactory getFactory() {
        return ExpressionFactory.FACTORY;
    }

    @Override
    public Tensor transform(Tensor t) {
        return new SubstitutionTransformation(this).transform(t);
    }

    /**
     * Returns {@code true} if r.h.s. is equal to l.h.s.
     *
     * @return {@code true} if r.h.s. is equal to l.h.s.
     */
    public boolean isIdentity() {
        return TensorUtils.equals(left, right);
    }

    /**
     * Swaps l.h.s. and r.h.s. of expression.
     *
     * @return swapped expression
     */
    public Expression transpose() {
        return new Expression(indices, right, left);
    }
}
