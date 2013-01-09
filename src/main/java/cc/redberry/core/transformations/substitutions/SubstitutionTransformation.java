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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.TensorUtils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SubstitutionTransformation implements Transformation {
    private final PrimitiveSubstitution[] primitiveSubstitutions;
    private final boolean applyIfModified;

    private SubstitutionTransformation(PrimitiveSubstitution[] primitiveSubstitutions, boolean applyIfModified) {
        this.primitiveSubstitutions = primitiveSubstitutions;
        this.applyIfModified = applyIfModified;
    }

    /**
     * Creates a set of substitutions.
     *
     * @param applyIfModified if false, then if some substitution was
     *                        applied to some tensor in tree, then others
     *                        will be skipped in current subtree.
     * @param expressions     an array of the expressions
     */
    public SubstitutionTransformation(boolean applyIfModified, Expression... expressions) {
        this.applyIfModified = applyIfModified;
        primitiveSubstitutions = new PrimitiveSubstitution[expressions.length];
        for (int i = expressions.length - 1; i >= 0; --i)
            primitiveSubstitutions[i] = createPrimitiveSubstitution(expressions[i].get(0), expressions[i].get(1));
    }

    /**
     * Creates a set of substitutions.
     *
     * @param expressions an array of the expressions
     */
    public SubstitutionTransformation(Expression... expressions) {
        this(true, expressions);
    }

    /**
     * Creates a substitution.
     *
     * @param from            from tensor
     * @param to              to tensor
     * @param applyIfModified if false, then if some substitution was
     *                        applied to some tensor in tree, then others
     *                        will be skipped in current subtree.
     * @throws IllegalArgumentException if {@code from} free indices are not equal
     *                                  to {@code to} free indices
     */
    public SubstitutionTransformation(Tensor from, Tensor to, boolean applyIfModified) {
        checkConsistence(from, to);
        primitiveSubstitutions = new PrimitiveSubstitution[1];
        primitiveSubstitutions[0] = createPrimitiveSubstitution(from, to);
        this.applyIfModified = applyIfModified;
    }


    /**
     * Creates a set of substitutions.
     *
     * @param from an array of from tensors
     * @param to   an array of to tensors
     * @throws IllegalArgumentException if {@code from.length != to.length}
     * @throws IllegalArgumentException if for there is a pair for which
     *                                  {@code from[i]} free indices equal
     *                                  to {@code to[i]} free indices
     */
    public SubstitutionTransformation(Tensor[] from, Tensor[] to) {
        this(from, to, true);
    }


    /**
     * Creates a substitution
     *
     * @param from from tensor
     * @param to   to tensor
     * @throws IllegalArgumentException if {@code from} free indices are not equal
     *                                  to {@code to} free indices
     */
    public SubstitutionTransformation(Tensor from, Tensor to) {
        this(from, to, true);
    }

    /**
     * Creates a set of substitutions
     *
     * @param from            an array of from tensors
     * @param to              an array of to tensors
     * @param applyIfModified if false, then if some substitution was
     *                        applied to some tensor in tree, then others
     *                        will be skipped in current subtree.
     * @throws IllegalArgumentException if {@code from.length != to.length}
     * @throws IllegalArgumentException if for there is a pair for which
     *                                  {@code from[i]} free indices are
     *                                  not equal to {@code to[i]} free indices
     */
    public SubstitutionTransformation(Tensor[] from, Tensor[] to, boolean applyIfModified) {
        checkConsistence(from, to);
        primitiveSubstitutions = new PrimitiveSubstitution[from.length];
        for (int i = 0; i < from.length; ++i)
            primitiveSubstitutions[i] = createPrimitiveSubstitution(from[i], to[i]);
        this.applyIfModified = applyIfModified;
    }

    /**
     * Creates a new substitution, which treats all of
     * the inner substitutions as simple substitutions.
     *
     * @return new substitution, which treats all of
     *         the inner substitutions as simple substitutions.
     */
    public SubstitutionTransformation asSimpleSubstitution() {
        SubstitutionTransformation ss = new SubstitutionTransformation(primitiveSubstitutions.clone(), applyIfModified);
        for (int i = primitiveSubstitutions.length - 1; i >= 0; --i)
            ss.primitiveSubstitutions[i] =
                    new PrimitiveSimpleTensorSubstitution(
                            ss.primitiveSubstitutions[i].from,
                            ss.primitiveSubstitutions[i].to);
        return ss;
    }

    private static void checkConsistence(Tensor[] from, Tensor[] to) {
        if (from.length != to.length)
            throw new IllegalArgumentException("from array and to array have different length.");
        for (int i = from.length - 1; i >= 0; --i)
            checkConsistence(from[i], to[i]);
    }

    private static void checkConsistence(Tensor from, Tensor to) {
        if (!TensorUtils.isZeroOrIndeterminate(to))
            if (!from.getIndices().getFree().equalsRegardlessOrder(to.getIndices().getFree()))
                throw new IllegalArgumentException("Tensor from free indices not equal to tensor to free indices: " +
                        from.getIndices().getFree() + "  " + to.getIndices().getFree());
    }

    private static PrimitiveSubstitution createPrimitiveSubstitution(Tensor from, Tensor to) {
        if (from.getClass() == SimpleTensor.class)
            return new PrimitiveSimpleTensorSubstitution(from, to);
        if (from.getClass() == TensorField.class) {
            boolean argumentIsNotSimple = false;
            for (Tensor t : from) {
                if (!(t instanceof SimpleTensor)) {
                    argumentIsNotSimple = true;
                    break;
                }
            }
            if (argumentIsNotSimple)
                return new PrimitiveSimpleTensorSubstitution(from, to);
            return new PrimitiveTensorFieldSubstitution(from, to);
        }
        if (from.getClass() == Product.class)
            return new PrimitiveProductSubstitution(from, to);
        if (from.getClass() == Sum.class)
            return new PrimitiveSumSubstitution(from, to);
        return new PrimitiveSimpleTensorSubstitution(from, to);
    }

    @Override
    public Tensor transform(Tensor t) {
        SubstitutionIterator iterator = new SubstitutionIterator(t);
        Tensor current;
        while ((current = iterator.next()) != null) {
            if (!applyIfModified && iterator.isCurrentModified())
                continue;
            Tensor old = current;
            for (PrimitiveSubstitution nodeSubstitution : primitiveSubstitutions) {
                current = nodeSubstitution.newTo(old, iterator);
                if (current != old && !applyIfModified)
                    break;
                old = current;
            }
            iterator.set(current);
        }
        return iterator.result();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        PrimitiveSubstitution tr;
        for (int i = 0; ; ++i) {
            tr = primitiveSubstitutions[i];
            builder.append(tr.from).append(" -> ").append(tr.to);
            if (i == primitiveSubstitutions.length - 1)
                break;
            builder.append(',');
        }
        return builder.append('}').toString();
    }
}
