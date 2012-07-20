/*
 * org.redberry.concurrent: high-level Java concurrent library.
 * Copyright (c) 2010-2012.
 * Bolotin Dmitriy <bolotin.dmitriy@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 */
package cc.redberry.core.tensor;

import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.substitutions.Substitutions;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
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
    public String toString(ToStringMode mode) {
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
        return Substitutions.getTransformation(left, right).transform(t);
    }
}
