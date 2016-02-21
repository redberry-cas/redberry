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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import cc.redberry.core.transformations.TransformationToStringAble;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.TensorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cc.redberry.core.transformations.substitutions.PrimitiveProductSubstitution.algorithm_with_simple_combinations;
import static cc.redberry.core.transformations.substitutions.PrimitiveProductSubstitution.algorithm_with_simple_contractions;
import static cc.redberry.core.utils.TensorUtils.shareSimpleTensors;

/**
 * Substitution.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class SubstitutionTransformation implements TransformationToStringAble {
    private final PrimitiveSubstitution[] primitiveSubstitutions;
    private final PrimitiveSubstitutions container;
    final boolean applyIfModified;

    private SubstitutionTransformation(PrimitiveSubstitution[] primitiveSubstitutions, boolean applyIfModified) {
        this.primitiveSubstitutions = primitiveSubstitutions;
        this.applyIfModified = applyIfModified;
        this.container = create(this.primitiveSubstitutions);
    }

    /**
     * Creates a set of substitutions.
     *
     * @param expressions     an array of the expressions
     * @param applyIfModified if false, then if some substitution was
     *                        applied to some tensor in tree, then others
     *                        will be skipped in current subtree.
     */
    public SubstitutionTransformation(Expression[] expressions, boolean applyIfModified) {
        this.applyIfModified = applyIfModified;
        primitiveSubstitutions = new PrimitiveSubstitution[expressions.length];
        for (int i = expressions.length - 1; i >= 0; --i)
            primitiveSubstitutions[i] = createPrimitiveSubstitution(expressions[i].get(0), expressions[i].get(1));
        this.container = create(this.primitiveSubstitutions);
    }

    /**
     * Creates a substitution.
     *
     * @param expression expression
     */
    public SubstitutionTransformation(Expression expression) {
        this(expression.get(0), expression.get(1));
    }

    /**
     * Creates a set of substitutions.
     *
     * @param expressions an array of the expressions
     */
    public SubstitutionTransformation(Expression... expressions) {
        this(expressions, !shareSimpleTensors0(expressions));
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
        this.container = create(this.primitiveSubstitutions);
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
        this(from, to, !shareSimpleTensors0(from, to));
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
        this(from, to, !shareSimpleTensors(from, to));
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
        this.container = create(this.primitiveSubstitutions);
    }

    private static boolean shareSimpleTensors0(Expression[] exprs) {
        for (Expression a : exprs)
            for (Expression b : exprs)
                if (shareSimpleTensors(a.get(0), b.get(1)))
                    return true;
        return false;
    }

    private static boolean shareSimpleTensors0(Tensor[] from, Tensor[] to) {
        for (Tensor a : from)
            for (Tensor b : to)
                if (shareSimpleTensors(a, b))
                    return true;
        return false;
    }

    /**
     * Adds specified transformations to the list of substitutions
     *
     * @param subs additional substitutions
     * @return a substitution transformation
     */
    public SubstitutionTransformation add(Transformation... subs) {
        return add(Arrays.asList(subs));
    }

    /**
     * Adds specified transformations to the list of substitutions
     *
     * @param subs additional substitutions
     * @return a substitution transformation
     */
    public SubstitutionTransformation add(Iterable<Transformation> subs) {
        List<PrimitiveSubstitution> r = new ArrayList<>();
        r.addAll(Arrays.asList(primitiveSubstitutions));
        add(r, subs);
        return new SubstitutionTransformation(r.toArray(new PrimitiveSubstitution[r.size()]), applyIfModified);
    }

    private static void add(List<PrimitiveSubstitution> r, Iterable<Transformation> subs) {
        for (Transformation tr : subs) {
            if (tr instanceof SubstitutionTransformation)
                r.addAll(Arrays.asList(((SubstitutionTransformation) tr).primitiveSubstitutions));
            else if (tr instanceof Expression)
                r.add(createPrimitiveSubstitution(((Expression) tr).get(0), ((Expression) tr).get(1)));
            else if (tr instanceof TransformationCollection)
                add(r, ((TransformationCollection) tr).getTransformations());
            else throw new IllegalArgumentException("Not a substitution: " + tr);
        }
    }

    /**
     * Returns transposed substitution rule (i.e. from a->b to b->a)
     *
     * @return transposed substitution (lhs swapped with rhs)
     */
    public SubstitutionTransformation transpose() {
        Tensor[] from = new Tensor[primitiveSubstitutions.length],
                to = new Tensor[primitiveSubstitutions.length];
        for (int i = primitiveSubstitutions.length - 1; i >= 0; --i) {
            from[i] = primitiveSubstitutions[i].to;
            to[i] = primitiveSubstitutions[i].from;
        }
        return new SubstitutionTransformation(from, to, applyIfModified);
    }

    /**
     * Creates a new substitution, which treats all of
     * the inner substitutions as simple substitutions.
     *
     * @return new substitution, which treats all of
     * the inner substitutions as simple substitutions.
     */
    public SubstitutionTransformation asSimpleSubstitution() {
        PrimitiveSubstitution[] primitiveSubstitutions = this.primitiveSubstitutions.clone();
        for (int i = primitiveSubstitutions.length - 1; i >= 0; --i)
            primitiveSubstitutions[i] =
                    new PrimitiveSimpleTensorSubstitution(
                            primitiveSubstitutions[i].from,
                            primitiveSubstitutions[i].to);
        return new SubstitutionTransformation(primitiveSubstitutions, applyIfModified);
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
        if (from.getClass() == Product.class) {
            if (from.size() == 2 && from.get(0) instanceof Complex) {
                to = Tensors.divide(to, from.get(0));
                from = from.get(1);
                return createPrimitiveSubstitution(from, to);
            }
            return new PrimitiveProductSubstitution(from, to);
        }
        if (from.getClass() == Sum.class)
            return new PrimitiveSumSubstitution(from, to);
        return new PrimitiveSimpleTensorSubstitution(from, to);
    }

//    @Override
//    public Tensor transform(Tensor t) {
//        SubstitutionIterator iterator = new SubstitutionIterator(t);
//        Tensor current;
//        boolean supposeIndicesAreAdded;
//        while ((current = iterator.next()) != null) {
//            if (!applyIfModified && iterator.isCurrentModified())
//                continue;
//            Tensor old = current;
//            supposeIndicesAreAdded = false;
//            for (PrimitiveSubstitution primitiveSubstitution : primitiveSubstitutions) {
//                current = primitiveSubstitution.newTo(old, iterator);
//                if (current != old) {
//                    supposeIndicesAreAdded |= primitiveSubstitution.possiblyAddsDummies;
//                    if (!applyIfModified)
//                        break;
//                }
//                old = current;
//            }
//            iterator.set(current, supposeIndicesAreAdded);
//        }
//        return iterator.result();
//    }

    @Override
    public Tensor transform(Tensor t) {
        SubstitutionIterator iterator = new SubstitutionIterator(t);
        Tensor current;
        boolean supposeIndicesAreAdded;
        while ((current = iterator.next()) != null) {
            if (!applyIfModified && iterator.isCurrentModified())
                continue;

            supposeIndicesAreAdded = false;
            Tensor old = current;
            if (current instanceof Product && ((Product) current).sizeOfDataPart() >= 2 && container.pContractions.length != 0) {
                PrimitiveProductSubstitution.ResultContained r = algorithm_with_simple_contractions(
                        (Product) current, container.pContractions, iterator);
                current = r.result;
                supposeIndicesAreAdded |= r.possiblyAddsDummies;
            }

            if (current instanceof Product && ((Product) current).sizeOfDataPart() >= 2
                    && (current == old || applyIfModified) && container.pCombinations.length != 0) {
                PrimitiveProductSubstitution.ResultContained r = algorithm_with_simple_combinations(
                        ((Product) current), container.pCombinations, iterator);
                current = r.result;
                supposeIndicesAreAdded |= r.possiblyAddsDummies;
            }


            if (current == old || applyIfModified) {
                old = current;
                for (PrimitiveSubstitution primitiveSubstitution : container.others) {
                    current = primitiveSubstitution.newTo(old, iterator);
                    if (current != old) {
                        supposeIndicesAreAdded |= primitiveSubstitution.possiblyAddsDummies;
                        if (!applyIfModified)
                            break;
                    }
                    old = current;
                }
            }
            iterator.set(current, supposeIndicesAreAdded);
        }
        return iterator.result();
    }

    public Expression[] getExpressions() {
        Expression[] expressions = new Expression[primitiveSubstitutions.length];
        for (int i = 0; i < primitiveSubstitutions.length; i++)
            expressions[i] = Tensors.expression(primitiveSubstitutions[i].from, primitiveSubstitutions[i].to);
        return expressions;
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        for (int i = 0; ; ++i) {
            builder.append(primitiveSubstitutions[i].toString(outputFormat));
            if (i == primitiveSubstitutions.length - 1)
                break;
            builder.append(',');
        }
        return builder.append('}').toString();
    }

    @Override
    public String toString() {
        return toString(CC.getDefaultOutputFormat());
    }

    private static PrimitiveSubstitutions create(final PrimitiveSubstitution... subs) {
        List<PrimitiveProductSubstitution> simpleProductCombinations = new ArrayList<>();
        List<PrimitiveProductSubstitution> simpleProductContractions = new ArrayList<>();
        List<PrimitiveSubstitution> others = new ArrayList<>();

        for (PrimitiveSubstitution ps : subs) {
            if (ps instanceof PrimitiveProductSubstitution) {
                PrimitiveProductSubstitution pps = (PrimitiveProductSubstitution) ps;
                if (pps.simpleContractions)
                    simpleProductContractions.add(pps);
                else if (pps.simpleCombinations)
                    simpleProductCombinations.add(pps);
                else others.add(pps);
            } else
                others.add(ps);
        }

        return new PrimitiveSubstitutions(
                to_array1(simpleProductCombinations),
                to_array1(simpleProductContractions),
                to_array0(others));
    }

    static PrimitiveSubstitution[] to_array0(List<PrimitiveSubstitution> list) {
        return list.toArray(new PrimitiveSubstitution[list.size()]);
    }

    static PrimitiveProductSubstitution[] to_array1(List<PrimitiveProductSubstitution> list) {
        return list.toArray(new PrimitiveProductSubstitution[list.size()]);
    }

    /**
     * Put zero substitutions on first
     */
    private static void sortPrimitiveSubstitutions(final PrimitiveSubstitution[] primitiveSubstitutions) {
        int zeros = 0;
        for (int i = 0; i < primitiveSubstitutions.length; i++)
            if (TensorUtils.isZeroOrIndeterminate(primitiveSubstitutions[i].to))
                ArraysUtils.swap(primitiveSubstitutions, zeros++, i);
    }

    private static class PrimitiveSubstitutions {
        final PrimitiveSubstitution[] others;
        final PrimitiveProductSubstitution[] pCombinations;
        final PrimitiveProductSubstitution[] pContractions;

        PrimitiveSubstitutions(PrimitiveProductSubstitution[] pCombinations,
                               PrimitiveProductSubstitution[] pContractions,
                               PrimitiveSubstitution[] others) {
            this.pCombinations = pCombinations;
            this.pContractions = pContractions;
            this.others = others;
            sortPrimitiveSubstitutions(this.pCombinations);
            sortPrimitiveSubstitutions(this.pContractions);
            sortPrimitiveSubstitutions(this.others);
        }
    }
}
