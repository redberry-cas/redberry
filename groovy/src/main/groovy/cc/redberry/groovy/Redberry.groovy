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

package cc.redberry.groovy

import cc.redberry.core.combinatorics.IntPermutationsGenerator
import cc.redberry.core.context.OutputFormat
import cc.redberry.core.groups.permutations.Permutation
import cc.redberry.core.groups.permutations.Permutations
import cc.redberry.core.indexmapping.IndexMappings
import cc.redberry.core.indexmapping.Mapping
import cc.redberry.core.indexmapping.MappingsPort
import cc.redberry.core.indices.*
import cc.redberry.core.number.Complex
import cc.redberry.core.number.Numeric
import cc.redberry.core.number.Rational
import cc.redberry.core.number.Real
import cc.redberry.core.parser.ParserIndices
import cc.redberry.core.tensor.*
import cc.redberry.core.tensor.iterator.FromChildToParentIterator
import cc.redberry.core.tensor.iterator.FromParentToChildIterator
import cc.redberry.core.tensor.iterator.TraverseGuide
import cc.redberry.core.transformations.Transformation
import cc.redberry.core.transformations.TransformationCollection
import cc.redberry.core.transformations.substitutions.SubstitutionIterator
import cc.redberry.core.transformations.substitutions.SubstitutionTransformation
import cc.redberry.core.utils.TensorUtils
import org.codehaus.groovy.runtime.DefaultGroovyMethods

import static cc.redberry.core.tensor.Tensors.*

/**
 * Redberry category.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class Redberry {

    static Real number2Real(Number num) {
        if (num instanceof Double || num instanceof BigDecimal)
            return new Numeric(num.doubleValue());
        else
            return new Rational(num.toBigInteger());
    }

    private static Complex number2Complex(Number num) {
        return new Complex(number2Real(num));
    }

    ///////////////////////////////////////////// TENSORS AND BUILDERS ////////////////////////////////////////////////

    /**
     * Returns the result of summation of several tensors.
     *
     * @param a summand
     * @param b summand
     * @return result of summation
     * @throws cc.redberry.core.tensor.TensorException if tensors have different free indices
     * @see Tensors#sum(cc.redberry.core.tensor.Tensor ...)
     */
    static Tensor plus(Tensor a, Tensor b) { Tensors.sum(a, b); }

    /**
     * Adds {@code b} to {@code a}
     *
     * @param a tensor
     * @param b number
     * @return {@code a} - {@code b}
     * @throws cc.redberry.core.tensor.TensorException if a is not scalar
     * @see Tensors#sum(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
     */
    static Tensor plus(Tensor a, Number b) { Tensors.sum(a, number2Complex(b)); }

    /**
     * Adds {@code b} to {@code a}
     *
     * @param a tensor
     * @param b number
     * @return {@code a} - {@code b}
     * @throws cc.redberry.core.tensor.TensorException if a is not scalar
     * @see Tensors#sum(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
     */
    static Tensor plus(Number b, Tensor a) { Tensors.sum(a, number2Complex(b)); }

    /**
     * Subtracts {@code b} from {@code a}
     *
     * @param a tensor
     * @param b tensor
     * @return {@code a} - {@code b}
     * @throws cc.redberry.core.tensor.TensorException if tensors have different free indices
     * @see Tensors#subtract(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
     */
    static Tensor minus(Tensor a, Tensor b) { Tensors.subtract(a, b); }

    /**
     * Subtracts {@code b} from {@code a}
     *
     * @param a tensor
     * @param b number
     * @return {@code a} - {@code b}
     * @throws cc.redberry.core.tensor.TensorException if a is not scalar
     * @see Tensors#subtract(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
     */
    static Tensor minus(Tensor a, Number b) { Tensors.subtract(a, number2Complex(b)); }

    /**
     * Subtracts {@code a} from {@code b}
     *
     * @param a tensor
     * @param b number
     * @return {@code b} - {@code a}
     * @throws cc.redberry.core.tensor.TensorException if a is not scalar
     * @see Tensors#subtract(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
     */
    static Tensor minus(Number b, Tensor a) { Tensors.subtract(number2Complex(b), a); }

    /**
     * Returns {@code a} divided by {@code b}.
     *
     * @param a tensor
     * @param b scalar tensor
     * @return {@code a} divided by {@code b}.
     * @throws IllegalArgumentException if b is not scalar
     * @see Tensors#divide(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
     */
    static Tensor div(Tensor a, Tensor b) { Tensors.divide(a, b); }

    /**
     * Divides tensor on specified number
     * @param a tensor
     * @param b number
     * @return the result
     * @see Tensors#divide(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
     */
    static Tensor div(Tensor a, Number b) { Tensors.divide(a, number2Complex(b)); }

    /**
     * Divides number on specified tensor
     * @param a tensor
     * @param b number
     * @return the result
     * @see Tensors#divide(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
     */
    static Tensor div(Number b, Tensor a) { Tensors.divide(number2Complex(b), a); }

    /**
     * Returns result of multiplication of specified tensors taking care about
     * all conflicting dummy indices in factors. Einstein notation assumed.
     *
     * @param a factor
     * @param b factor
     * @return result of multiplication
     * @throws InconsistentIndicesException if there is indices clash
     * @see Tensors#multiplyAndRenameConflictingDummies(cc.redberry.core.tensor.Tensor ...)
     */
    static Tensor multiply(Tensor a, Tensor b) { multiplyAndRenameConflictingDummies(a, b); }

    /**
     * Multiplies tensor on number
     * @param a tensor
     * @param b number
     * @return the result
     * @see Tensors#multiply(cc.redberry.core.tensor.Tensor ...)
     */
    static Tensor multiply(Tensor a, Number b) { Tensors.multiply(a, number2Complex(b)); }

    /**
     * Multiplies tensor on number
     * @param a tensor
     * @param b number
     * @return the result
     * @see Tensors#multiply(cc.redberry.core.tensor.Tensor ...)
     */
    static Tensor multiply(Number b, Tensor a) { Tensors.multiply(a, number2Complex(b)); }

    /**
     * Power function. Returns tensor raised to specified power.
     *
     * @param a base
     * @param b power
     * @return result of argument exponentiation
     * @throws IllegalArgumentException if argument or power is not scalar
     * @see Tensors#pow(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
     */
    static Tensor power(Tensor a, Tensor b) { Tensors.pow(a, b); }

    /**
     * Power function. Returns tensor raised to specified power.
     *
     * @param a base
     * @param b power
     * @return result of argument exponentiation
     * @throws IllegalArgumentException if argument or power is not scalar
     * @see Tensors#pow(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
     */
    static Tensor power(Tensor a, Number b) { Tensors.pow(a, number2Complex(b)); }

    /**
     * Multiplies specified tensor by minus one.
     *
     * @param a tensor to be negotiated
     * @return tensor negate to specified one
     * @see Tensors#negate(cc.redberry.core.tensor.Tensor)
     */
    static Tensor negative(Tensor a) { negate(a); }

    /**
     * Returns tensor
     * @param a tensor
     * @return tensor
     */
    static Tensor positive(Tensor a) { a; }

    /**
     * Returns element at i-th position.
     *
     * @param a tensor
     * @param i position
     * @return element at i-th position
     * @throws IndexOutOfBoundsException if {@code i < 0} or {@code i >= size()}
     * @see Tensor#get(int)
     */
    static Tensor getAt(Tensor a, int i) { a.get(i); }

    /**
     * Returns element at specified positions in expression-tree
     * @param a tensor
     * @param positions positions in tree
     * @return element at specified positions in expression-tree
     * @see Tensor#get(int)
     */
    static List<Tensor> getAt(Tensor a, Collection<Integer> positions) {
        ArrayList<Tensor> result = new ArrayList<>()
        for (int i : positions)
            result.add(a.get(i))
        return result;
    }

    /**
     * Retrieves several sub-tensors from current tensor. This function is
     * faster than sequential invocations of {@link Tensor#get(int)} method.
     *
     * @param a tensor
     * @param range integer range
     * @return array with retrieved tensors
     * @see Tensor#getRange(int, int)
     */
    static List<Tensor> getAt(Tensor a, IntRange range) {
        int from = range.fromInt, to = range.toInt;
        if (from == 0 && to == a.size())
            return new ArrayList<Tensor>(Arrays.asList(a.toArray()));
        return new ArrayList<Tensor>(Arrays.asList(a.getRange(from, to)));
    }

    /**
     * Selects tensors at the specified positions and puts it together.
     *
     * @param positions positions in tensor
     * @return result subtensor
     * @see MultiTensor#select(int [ ])
     */
    static Tensor select(MultiTensor a, Collection positions) {
        return a.select(positions as int[]);
    }

    /**
     * Removes tensors at the specified positions and returns the result.
     *
     * @param positions position in tensor
     * @return result of removing
     * @throws IndexOutOfBoundsException
     * @see MultiTensor#remove(int [ ])
     */
    static Tensor remove(MultiTensor a, Collection positions) {
        return a.remove(positions as int[]);
    }

    /**
     * Overloads the left shift operator to provide an easy way to put tensors to a TensorBuilder.
     * @param builder tensor builder
     * @param tensor a tensor to be putted to the tensor builder.
     * @return same builder, after the value was putted to it.
     */
    static TensorBuilder leftShift(TensorBuilder builder, Tensor tensor) {
        builder.put(tensor);
        return builder;
    }

    /**
     * Overloads the left shift operator to provide an easy way to put a collections of tensors to a TensorBuilder.
     * @param builder tensor builder
     * @param tensors a collection of tensors to be sequentially putted to the tensor builder.
     * @return same builder, after the value was putted to it.
     */
    static TensorBuilder leftShift(TensorBuilder builder, Collection<Tensor> tensors) {
        for (Tensor t : tensors)
            builder.put(t)
        return builder;
    }

    /**
     * Overloads the left shift operator to provide an easy way to put an array of tensors to a TensorBuilder.
     * @param builder tensor builder
     * @param tensors an array of tensors to be sequentially putted to the tensor builder.
     * @return same builder, after the value was putted to it.
     */
    static TensorBuilder leftShift(TensorBuilder builder, Tensor[] tensors) {
        for (Tensor t : tensors)
            builder.put(t)
        return builder;
    }

    /**
     * Adds ability for {@link TensorFactory} to create tensor from list
     * @param factory tensor factory
     * @param tensors a list of tensors
     * @return result
     */
    static Tensor create(TensorFactory factory, List tensors) {
        return factory.create(tensors.toArray(new Tensor[tensors.size()]))
    }

    /**
     * Returns a list of tensor content
     * @param tensor tensor
     * @return a list of tensor content
     */
    static List<Tensor> toList(Tensor tensor) {
        return tensor.toArray() as List
    }

    /////////////////////////////////////////// PRODUCT CONTENT ///////////////////////////////////////////////////////

    /**
     * Returns i-th element of indexed data in content.
     *
     * @param i position
     * @return i-th element of indexed data in content
     */
    static Tensor getAt(ProductContent content, int i) {
        return content.get(i)
    }

    /**
     * Returns a range of indexed data specified by range.
     *
     * @param from from position (inclusive)
     * @param to to position (exclusive)
     * @return range
     */
    static List<Tensor> getAt(ProductContent content, Range range) {
        return content.getRange(range.from, range.to) as List
    }

    /**
     * Returns a range of indexed data specified by range.
     *
     * @param from from position (inclusive)
     * @param to to position (exclusive)
     * @return range
     */
    static List<Tensor> asType(ProductContent content, Class clazz) {
        if (clazz == List)
            return content.dataCopy as List
        return DefaultGroovyMethods.asType(content, clazz)
    }

    //////////////////////////////////////////////// INDICES //////////////////////////////////////////////////////////

    /**
     * Returns a string representation of single index
     * @param index index
     * @param format output format
     * @return string representation of single index
     */
    static String toStringIndex(Integer index, OutputFormat format) {
        return IndicesUtils.toString(index, format)
    }

    /**
     * Returns a string representation of single index
     * @param index index
     * @return string representation of single index
     */
    static String toStringIndex(Integer index) {
        return IndicesUtils.toString(index)
    }

    /**
     * Inverses state of index
     * @param index index
     * @return index with inverted state
     */
    static Integer invert(Integer index) {
        return IndicesUtils.inverseIndexState(index)
    }

    /**
     * Returns true if index is upper and false otherwise
     * @param index index
     * @return true if index is upper and false otherwise
     */
    static boolean isUpper(Integer index) {
        return IndicesUtils.getState(index)
    }

    /**
     * Returns true if index is lower and false otherwise
     * @param index index
     * @return true if index is lower and false otherwise
     */
    static boolean isLower(Integer index) {
        return !IndicesUtils.getState(index)
    }

    /**
     * Makes index upper
     * @param index index
     * @return contravariant index
     */
    static Integer toUpper(Integer index) {
        return IndicesUtils.setRawState(IndicesUtils.UPPER_RAW_STATE_INT, index)
    }

    /**
     * Makes index lower
     * @param index index
     * @return covariant index
     */
    static Integer toLower(Integer index) {
        return IndicesUtils.setRawState(IndicesUtils.LOWER_RAW_STATE_INT, index)
    }

    /**
     * Returns type of index
     * @param index index
     * @return type of index
     */
    static IndexType getType(Integer index) {
        return IndicesUtils.getTypeEnum(index);
    }

    /**
     * Returns the index at the specified position in indices
     *
     * @param indices indices
     * @param position position of the index
     * @return the index at the specified position in this
     *         <code>Indices</code>
     * @throws IndexOutOfBoundsException - if the index is out of range (index <
     *                                   0 || index >= size())
     * @see Indices#get(int)
     */
    static int getAt(Indices indices, int position) { indices.get(position) }

    /**
     * Returns sub indices of specified range
     *
     * @param indices indices
     * @param range range
     * @return sub indices of specified range
     * @throws IndexOutOfBoundsException
     */
    static Indices getAt(Indices indices, IntRange range) {
        int[] sub = new int[range.size()]
        for (int i = 0; i < range.size(); ++i)
            sub[i] = indices.get(i + range.from);
        return IndicesFactory.create(sub)
    }
    /**
     * Returns sub indices of specified range
     *
     * @param indices indices
     * @param range range
     * @return sub indices of specified range
     * @throws IndexOutOfBoundsException
     */
    static Indices getAt(Indices indices, int[] range) {
        return getAt(indices, range as List)
    }

    /**
     * Returns sub indices of specified range
     *
     * @param indices indices
     * @param range range
     * @return sub indices of specified range
     * @throws IndexOutOfBoundsException
     */
    static SimpleIndices getAt(SimpleIndices indices, int[] range) {
        return getAt(indices, range as List)
    }

    /**
     * Returns sub indices of specified range
     *
     * @param indices indices
     * @param range range
     * @return sub indices of specified range
     * @throws IndexOutOfBoundsException
     */
    static Indices getAt(Indices indices, Collection range) {
        int[] sub = new int[range.size()]
        int c = 0
        for (def i in range)
            sub[c++] = indices.get(i);
        return IndicesFactory.create(sub)
    }

    /**
     * Returns sub indices of specified range
     *
     * @param indices indices
     * @param range range
     * @return sub indices of specified range
     * @throws IndexOutOfBoundsException
     */
    static SimpleIndices getAt(SimpleIndices indices, Collection range) {
        int[] sub = new int[range.size()]
        int c = 0
        for (def i in range)
            sub[c++] = indices.get(i);
        return IndicesFactory.createSimple(null, sub)
    }

    /**
     * Returns sub indices of specified range
     *
     * @param indices indices
     * @param range range
     * @return sub indices of specified range
     * @throws IndexOutOfBoundsException
     */
    static SimpleIndices getAt(SimpleIndices indices, IntRange range) {
        int[] sub = new int[range.size()]
        for (int i = 0; i < range.size(); ++i)
            sub[i] = indices.get(i + range.from);
        return IndicesFactory.createSimple(null, sub)
    }

    /**
     * Returns the index of the specified type at the
     * specified position in indices
     *
     * @param indices indices
     * @param type IndexType
     * @param position position of the index to return
     * @return the index of the specified type at the
     *         specified position in this <code>Indices</code>
     * @throws IndexOutOfBoundsException - if the index is out of range
     * @see Indices#get(cc.redberry.core.indices.IndexType, int)
     */
    static int getAt(Indices indices, IndexType type, int position) { indices.get(type, position) }

    /**
     * Iterator over single indices integers in {@code indices} object
     * @param indices indices
     * @return iterator over integers
     */
    static Iterator<Integer> iterator(final Indices indices) {
        def position = 0;
        return new Iterator<Integer>() {

            @Override
            boolean hasNext() {
                return position < indices.size()
            }

            @Override
            Integer next() {
                return indices.get(position++)
            }

            @Override
            void remove() {
                throw new UnsupportedOperationException()
            }
        }
    }

    /**
     * Returns true if specified index is contained in indices
     * @param indices indices
     * @param index single index
     * @return true if specified index is contained in indices
     */
    static boolean contains(Indices indices, int index) {
        //todo move to Indices
        for (int i = indices.size() - 1; i >= 0; --i)
            if (indices.get(i) == index)
                return true
        return false
    }

    /**
     * Returns true if specified index is contained in indices
     * @param indices indices
     * @param index single index
     * @return true if specified index is contained in indices
     */
    static boolean containsIgnoringState(Indices indices, int index) {
        //todo move to Indices
        index = IndicesUtils.getNameWithType(index)
        for (int i = indices.size() - 1; i >= 0; --i)
            if (IndicesUtils.getNameWithType(indices.get(i)) == index)
                return true
        return false
    }

    /**
     * SimpleIndices concatenation
     * @param indices simple indices
     * @param toAdd to add
     * @return new indices
     * @see SimpleIndicesBuilder
     * @throws InconsistentIndicesException if there was more then one same index (with same names, types and states)
     */
    static SimpleIndices plus(SimpleIndices indices, toAdd) {
        if (toAdd instanceof String)
            toAdd = ParserIndices.parseSimple(toAdd);
        if (toAdd instanceof Indices || toAdd instanceof Integer || toAdd instanceof int[])
            return new SimpleIndicesBuilder().append(indices).append(toAdd).indices;
        if (toAdd instanceof Collection)
            return new SimpleIndicesBuilder().append(indices).append(toAdd as int[]).indices;
        else
            throw new IllegalArgumentException()
    }

    /**
     * Indices concatenation
     * @param indices indices
     * @param toAdd to add
     * @return new indices
     * @see IndicesBuilder
     * @throws InconsistentIndicesException if there was more then one same index (with same names, types and states)
     */
    static Indices plus(Indices indices, toAdd) {
        if (toAdd instanceof String)
            toAdd = ParserIndices.parseSimple(toAdd);
        if (toAdd instanceof Indices || toAdd instanceof Integer || toAdd instanceof int[])
            return new IndicesBuilder().append(indices).append(toAdd).indices;
        if (toAdd instanceof Collection)
            return new IndicesBuilder().append(indices).append(toAdd as int[]).indices;
        else
            throw new IllegalArgumentException()
    }

    ///////////////////////////////////////// TREE TRAVERSAL ///////////////////////////////////////////////////////

    /**
     * Expression-tree traversal parent-after-child
     * @param t expression
     * @param closure do stuff
     * @param guide traverse guide
     * @see FromChildToParentIterator
     */
    static void parentAfterChild(Tensor t, Object guide, Closure<Tensor> closure) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t, guide);
        Tensor c;
        while ((c = iterator.next()) != null)
            closure.call(c);
    }

    /**
     * Expression-tree traversal parent-before-child
     * @param t expression
     * @param closure do stuff
     * @param guide traverse guide
     * @see FromParentToChildIterator
     * @see TraverseGuide
     */
    static void parentBeforeChild(Tensor t, TraverseGuide guide, Closure<Tensor> closure) {
        FromParentToChildIterator iterator = new FromParentToChildIterator(t, guide);
        Tensor c;
        while ((c = iterator.next()) != null)
            closure.call(c);
    }

    /**
     * Expression-tree traversal parent-after-child
     * @param t expression
     * @param closure do stuff
     * @see FromChildToParentIterator
     */
    static void parentAfterChild(Tensor t, Closure<Tensor> closure) {
        parentAfterChild(t, TraverseGuide.ALL, closure)
    }

    /**
     * Expression-tree traversal parent-before-child
     * @param t expression
     * @param closure do stuff
     * @see FromParentToChildIterator
     */
    static void parentBeforeChild(Tensor t, Closure<Tensor> closure) {
        parentBeforeChild(t, TraverseGuide.ALL, closure)
    }

    /*
    * Tree modification
    */

    /**
     * Expression-tree traversal and modification
     * @param t expression
     * @param closure do stuff
     * @param guide traverse guide
     * @return the result
     * @see SubstitutionIterator
     * @see TraverseGuide
     */
    static Tensor transformParentAfterChild(Tensor t, TraverseGuide guide, Closure<Tensor> closure) {
        SubstitutionIterator iterator = new SubstitutionIterator(t, guide);
        Tensor c;
        while ((c = iterator.next()) != null)
            iterator.safeSet(closure.call(c));

        return iterator.result();
    }

    /**
     * Expression-tree traversal and modification
     * @param t expression
     * @param closure do stuff
     * @return the result
     * @see SubstitutionIterator
     */
    static Tensor transformParentAfterChild(Tensor t, Closure<Tensor> closure) {
        return transformParentAfterChild(t, TraverseGuide.ALL, closure);
    }

    ///////////////////////////////////////// TRANSFORMATIONS ///////////////////////////////////////////////////////

    /**
     * Joins two transformations in a single one, which will apply both transformations sequentially
     * @param tr1 transformation
     * @param tr2 transformation
     * @return joined transformation, which will apply both transformations sequentially
     */
    static Transformation and(Transformation tr1, Transformation tr2) {
        def transformations = [];
        if (tr1 instanceof TransformationCollection)
            transformations.addAll(tr1.transformations)
        else
            transformations << tr1

        if (tr2 instanceof TransformationCollection)
            transformations.addAll(tr2.transformations)
        else
            transformations << tr2

        new TransformationCollection(transformations)
    }

    /**
     * Joins two transformations in a single one, which will apply both transformations sequentially
     * @param tr1 transformation
     * @param tr2 transformation
     * @return joined transformation, which will apply both transformations sequentially
     */
    static Transformation and(Transformation tr1, List tr2) {
        def transformations = [];
        if (tr1 instanceof TransformationCollection)
            transformations.addAll(tr1.transformations)
        else
            transformations << tr1

        transformations.addAll(tr2)

        new TransformationCollection(transformations)
    }

    /**
     * Joins two transformations in a single one, which will apply both transformations sequentially
     * @param tr1 transformation
     * @param tr2 transformation
     * @return joined transformation, which will apply both transformations sequentially
     */
    static Transformation and(List tr1, Transformation tr2) {
        def transformations = [];
        transformations.addAll(tr1)
        if (tr2 instanceof TransformationCollection)
            transformations.addAll(tr2.transformations)
        else
            transformations << tr2

        new TransformationCollection(transformations)
    }

    /**
     * Joins two substitutions in a single one, which will apply both substitutions "simultaneously"
     * @param tr1 substitution
     * @param tr2 substitution
     * @return joined substitution, which will apply both substitutions "simultaneously"
     * @see SubstitutionTransformation
     */
    static Transformation or(Transformation tr1, Transformation tr2) {
        def expressions = [];
        if (tr1 instanceof SubstitutionContainer)
            expressions.addAll(tr1.expressions)
        else if (tr1 instanceof Expression)
            expressions << tr1
        else throw new IllegalArgumentException()

        if (tr2 instanceof SubstitutionContainer)
            expressions.addAll(tr2.expressions)
        else if (tr2 instanceof Expression)
            expressions << tr2
        else throw new IllegalArgumentException()

        new SubstitutionContainer(expressions)
    }

    private static final class SubstitutionContainer implements Transformation {
        final List expressions;

        SubstitutionContainer(expressions) {
            this.expressions = expressions
        }

        @Override
        Tensor transform(Tensor t) {
            return new SubstitutionTransformation(expressions.toArray(new Expression[expressions.size()]), false).transform(t)
        }

        @Override
        public String toString() {
            return expressions
        }
    }

    private static boolean isCollectionOfType(collection, Class type) {
        for (t in collection)
            if (!type.isAssignableFrom(t.class))
                return false;
        return true;
    }

    /**
     * Applies transformation to tensor
     * @param tensor tensor
     * @param transformation transformation
     * @return the result
     * @see Transformation#transform(cc.redberry.core.tensor.Tensor)
     */
    static Tensor rightShift(Transformation transformation, Tensor tensor) {
        return transformation.transform(tensor);
    }

    /**
     * Applies transformation to a collection of tensors
     * @param tensors tensors
     * @param transformation transformation
     * @return the result
     * @see Transformation#transform(cc.redberry.core.tensor.Tensor)
     */
    static Collection rightShift(Transformation transformation, Collection tensors) {
        return tensors.collect { rightShift(transformation, it) }
    }

    /**
     * Applies substitution to tensor
     * @param tensor tensor
     * @param transformation string representation of substitution
     * @return the result
     * @see Transformation#transform(cc.redberry.core.tensor.Tensor)
     */
    static Tensor rightShift(String transformation, Tensor tensor) {
        return parseExpression(transformation).transform(tensor);
    }

    /**
     * Applies collection of transformations to tensor
     * @param tensor tensor
     * @param transformations collection of transformations
     * @return the result
     * @see Transformation#transform(cc.redberry.core.tensor.Tensor)
     */
    static Tensor rightShift(Collection transformations, Tensor tensor) {

        transformations = transformations.collect { if (it instanceof String) parse(it); else it; }

        if (isCollectionOfType(transformations, Expression))
            return new SubstitutionTransformation(transformations as Expression[]).transform(tensor);
        def t = tensor
        for (Transformation tr in transformations)
            t = tr.transform(t);
        return t;
    }

    /**
     * Applies collection of transformations to a collection of tensors
     * @param tensors tensors
     * @param transformations collection of transformations
     * @return the result
     * @see Transformation#transform(cc.redberry.core.tensor.Tensor)
     */
    static Collection rightShift(Collection transformations, Collection tensors) {
        return tensors.collect { rightShift(transformations, it) }
    }

    /**
     * Applies collection of transformations to collection of expressions represented by
     * {@link TransformationCollection}
     *
     * @param tensor tensor
     * @param transformations collection of transformations
     * @return the result
     * @see Transformation#transform(cc.redberry.core.tensor.Tensor)
     * @throws RuntimeException if {@code collection} is not a collection of expressions
     */
    static TransformationCollection rightShift(Transformation transformation, TransformationCollection collection) {
        return new TransformationCollection(collection.transformations.collect { transformation.transform(it) })
    }

    /**
     * Applies collection of transformations to collection of expressions represented by
     * {@link TransformationCollection}
     *
     * @param tensor tensor
     * @param transformations collection of transformations
     * @return the result
     * @see Transformation#transform(cc.redberry.core.tensor.Tensor)
     * @throws RuntimeException if {@code collection} is not a collection of expressions
     */
    static TransformationCollection leftShift(TransformationCollection collection, Transformation transformation) {
        return rightShift(transformation, collection)
    }

    /**
     * Applies transformation to tensor
     * @param tensor tensor
     * @param transformation transformation
     * @return the result
     * @see Transformation#transform(cc.redberry.core.tensor.Tensor)
     */
    static Tensor leftShift(Tensor tensor, Transformation transformation) {
        return transformation.transform(tensor);
    }

    /**
     * Applies collection of transformations to tensor
     * @param tensor tensor
     * @param transformations collection of transformations
     * @return the result
     * @see Transformation#transform(cc.redberry.core.tensor.Tensor)
     */
    static Tensor leftShift(Tensor tensor, Collection<Transformation> transformations) {
        if (isCollectionOfType(transformations, Expression))
            return new SubstitutionTransformation(transformations as Expression[]).transform(tensor);
        def t = tensor
        for (Transformation tr in transformations)
            t = tr.transform(t);
        return t;
    }

    /**
     * Applies tensor field substitutions without matching arguments
     * @param substitution
     * @return
     */
    static Transformation getHold(Expression substitution) {
        return new SubstitutionTransformation(substitution).asSimpleSubstitution()
    }

    /**
     * Applies tensor field substitutions without matching arguments
     * @param substitution
     * @return
     */
    static Transformation getHold(SubstitutionTransformation substitution) {
        return substitution.asSimpleSubstitution()
    }

    //////////////////////////////////////////// TYPE CONVERSION ///////////////////////////////////////////////////////

    /**
     * Tensor as array, list etc.
     * @param tensor
     * @param clazz
     * @return
     */
    static Object asType(Tensor tensor, Class clazz) {
        if (clazz == List)
            return tensor.toArray() as List
        else if (clazz == Tensor[])
            return tensor.toArray()
        else if (clazz == String)
            return tensor.toString()
        else
            return DefaultGroovyMethods.asType(tensor, clazz)
    }

    static Object asType(Collection collection, Class clazz) {
        if (clazz == Indices)
            return IndicesFactory.create(*collection)
        else if (clazz == SimpleIndices)
            return IndicesFactory.createSimple(null, *collection)
        else if (clazz == Transformation)
            return new TransformationCollection(collection)
        else if (clazz == Product)
            return Tensors.multiplyAndRenameConflictingDummies(getT(collection))
        else if (clazz == Sum)
            return Tensors.sum(getT(collection))
        return DefaultGroovyMethods.asType(collection, clazz);
    }

    static Tensor asType(Number num, Class clazz) {
        if (clazz == Tensor)
            return number2Complex(num)
        return DefaultGroovyMethods.asType(num, clazz)
    }

    static Object asType(String string, Class clazz) {
        if (clazz == Tensor)
            return parse(string);
        return DefaultGroovyMethods.asType(string, clazz);
    }

    static Object asType(int[] indices, Class clazz) {
        if (clazz == SimpleIndices)
            return IndicesFactory.createSimple(null, indices)
        else if (clazz == Indices)
            return IndicesFactory.create(indices)
        else
            return DefaultGroovyMethods.asType(indices, clazz)
    }

    static Object asType(Indices indices, Class clazz) {
        if (clazz == int[])
            return indices.getAllIndices().copy()
        else if (clazz == List)
            return indices.toArray() as List
        else
            return DefaultGroovyMethods.asType(indices, clazz)
    }

    //////////////////////////////////////////////// MAPPINGS //////////////////////////////////////////////////////////

    /**
     * Returns {@code true} if tensors are mathematically (not programmatically) equal
     * @param a tensor
     * @param b tensor
     * @return {@code true} if tensors are mathematically (not programmatically) equal, {@code false} otherwise
     * @see TensorUtils#equals(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
     */
    static boolean equals(Tensor a, b) {
        if (b.getClass() != a.getClass())
            return false;
        return TensorUtils.equals(a, b);
    }

    /**
     * Returns {@code true} if tensors are mathematically (not programmatically) equal
     * @param a tensor
     * @param b tensor
     * @return {@code true} if tensors are mathematically (not programmatically) equal, {@code false} otherwise
     * @see TensorUtils#equals(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
     */
    static int compareTo(Tensor a, Object b) {
        if (Redberry.equals(a, b))
            return 0;
        return a.compareTo(b)
        //return DefaultGroovyMethods.compareTo(a, b)
    }

    /**
     * Changes sign of mapping
     * @param mapping mapping
     * @return mapping multiplied by minus one
     */
    static Mapping negative(Mapping mapping) {
        return mapping.addSign(true);
    }

    /**
     * Returns a mapping from indices {@code from} to indices {@code to}.
     *
     * @param from {@code from} indices
     * @param to {@code to} indices
     * @return a single mappingç
     */
    static Mapping mod(Indices from, Indices to) {
        return new Mapping(from.toArray(), to.toArray());
    }

    /**
     * Returns the container of mappings from tensor {@code from} onto tensor {@code to}. This structure is iterable
     * and also can be manipulated as single transformation which simply applies first possible mapping.
     *
     * @param from {@code from} tensor
     * @param to {@code to} tensor
     * @return container of mappings
     */
    static MappingsContainer mod(Tensor from, Tensor to) {
        return new MappingsContainer(from, to);
    }

    /**
     * This class describes the container of mappings from one tensor onto another. This structure is iterable
     * and also can be manipulated as single transformation which simply applies first possible mapping.
     */
    public static final class MappingsContainer implements Transformation, Iterable<Mapping> {
        private final Tensor from, to

        private boolean firstCalculated = false
        private Mapping first = null

        MappingsContainer(Tensor from, Tensor to) {
            this.from = from
            this.to = to
        }

        @Override
        Tensor transform(Tensor t) {
            return getFirst().transform(t)
        }

        /**
         * Returns the first possible mapping
         * @return first possible mapping
         */
        public Mapping getFirst() {
            if (!firstCalculated) {
                first = IndexMappings.getFirst(from, to)
                firstCalculated = true
            }
            return first
        }

        /**
         * Returns the output port of possible mappings
         * @return port of possible mappings
         */
        public MappingsPort getPort() {
            return IndexMappings.createPort(from, to)
        }

        @Override
        Iterator<Mapping> iterator() {
            return new MappingsIterator(getPort())
        }

        @Override
        public String toString() {
            return getFirst().toString()
        }

        public boolean isExists(){
            return getFirst() != null
        }

        private static class MappingsIterator implements Iterator<Mapping> {
            private final MappingsPort port
            private Mapping previous, next

            MappingsIterator(MappingsPort port) {
                this.port = port
                next = port.take()
            }

            @Override
            boolean hasNext() {
                return next != null
            }

            @Override
            Mapping next() {
                previous = next
                next = port.take()
                return previous
            }

            @Override
            void remove() {
                throw new UnsupportedOperationException()
            }
        }
    }

    //////////////////////////////////////////////// MATRICES //////////////////////////////////////////////////////////

    /**
     * Covector with respect to specified type
     * @param type index type
     * @return matrix descriptor of covector with specified type
     * @see IndexType
     * @see cc.redberry.groovy.RedberryStatic#defineMatrices(java.lang.Object [])
     */
    static MatrixDescriptor getCovector(IndexType type) {
        return new MatrixDescriptor(type, 0, 1);
    }

    /**
     * Vector with respect to specified type
     * @param type index type
     * @return matrix descriptor of vector with specified type
     * @see IndexType
     * @see cc.redberry.groovy.RedberryStatic#defineMatrices(java.lang.Object [])
     */
    static MatrixDescriptor getVector(IndexType type) {
        return new MatrixDescriptor(type, 1, 0);
    }

    /**
     * Matrix with respect to specified type
     * @param type index type
     * @return matrix descriptor of matrix with specified type
     * @see IndexType
     * @see cc.redberry.groovy.RedberryStatic#defineMatrices(java.lang.Object [])
     */
    static MatrixDescriptor getMatrix(IndexType type) {
        return new MatrixDescriptor(type, 1, 1);
    }

    /**
     * Generalized matrix with respect to specified type with specified number of upper and lower indices
     * @param type index type
     * @param upper number of upper indices
     * @param lower number of lower indices
     * @return matrix descriptor of generalized matrix with specified type
     * @see IndexType
     * @see cc.redberry.groovy.RedberryStatic#defineMatrices(java.lang.Object [])
     */
    static MatrixDescriptor tensor(IndexType type, int upper, int lower) {
        return new MatrixDescriptor(type, upper, lower);
    }

    //////////////////////////////////////////////// PARSE //////////////////////////////////////////////////////////

    /**
     * Parse string to tensor
     * @param string string representation of tensor
     * @return tensor
     * @see Tensor
     * @see Tensors#parse(java.lang.String)
     * @throws cc.redberry.core.parser.ParserException
     *          if expression does not satisfy correct Redberry
     *          input notation for tensors
     *
     */
    static Tensor getT(String string) {
        return parse(string)
    }

    /**
     * Parse string to simple tensor
     * @param string string representation of simple tensor
     * @return simple tensor
     * @see cc.redberry.core.tensor.SimpleTensor
     * @see Tensors#parse(java.lang.String)
     * @throws cc.redberry.core.parser.ParserException
     *          if expression does not satisfy correct Redberry
     *          input notation for tensors
     *
     */
    static SimpleTensor getSt(String string) {
        return parseSimple(string)
    }

    /**
     * Parse collection of strings to colection of tensors
     * @param strings string representations of tensors
     * @return collection of tensors
     * @see Tensor
     * @see Tensors#parse(java.lang.String)
     *
     * @throws cc.redberry.core.parser.ParserException
     *          if expression does not satisfy correct Redberry
     *          input notation for tensors
     *
     */
    static Collection getT(Collection strings) {
        return strings.collect {
            if (it instanceof String || it instanceof GString)
                return parse(it)
            else return it
        }
    }

    /**
     * Parse string to tensor
     * @param string string representation of tensor
     * @return tensor
     * @see Tensor
     * @see Tensors#parse(java.lang.String)
     * @throws cc.redberry.core.parser.ParserException
     *          if expression does not satisfy correct Redberry
     *          input notation for tensors
     */
    static Tensor getT(GString string) {
        return parse(string.toString())
    }

    static Tensor getT(Tensor tensor) {
        return tensor
    }

    /**
     * Gives Redberry representation of number
     * @param number number
     * @return Redberry representation of number
     * @see Complex
     */
    static Tensor getT(Number number) {
        return number2Complex(number)
    }

    /**
     * Parse string to indices
     * @param string string representation of indices
     * @return indices
     * @see Indices
     * @see ParserIndices#parseSimple(java.lang.String)
     * @see IndicesFactory#create(cc.redberry.core.indices.Indices)
     * @throws IllegalArgumentException if string does not represent correct indices object.
     */
    static Indices getI(String string) {
        return IndicesFactory.create(ParserIndices.parseSimple(string))
    }

    /**
     * Parse string to simple indices
     * @param string string representation of simple indices
     * @return simple indices
     * @see SimpleIndices
     * @see ParserIndices#parseSimple(java.lang.String)
     * @throws IllegalArgumentException if string does not represent correct indices object.
     */
    static SimpleIndices getSi(String string) {
        return ParserIndices.parseSimple(string)
    }

    /**
     * Converts list of integers to SimpleIndices
     * @param list list of integers
     * @return simple indices
     * @see SimpleIndices
     * @throws IllegalArgumentException if list does not represent correct indices object.
     */
    static SimpleIndices getSi(List list) {
        return IndicesFactory.createSimple(null, list as int[])
    }

    /**
     * Converts indices to SimpleIndices
     * @param indices indices
     * @return simple indices
     * @see SimpleIndices
     * @throws IllegalArgumentException if list does not represent correct indices object.
     */
    static SimpleIndices getSi(Indices indices) {
        return IndicesFactory.createSimple(null, indices)
    }

    /**
     * Creates the mapping of indices from a given string representation.
     *
     * @param string string representation of a mapping
     * @return mapping of indices
     */
    static Mapping getMapping(String string) {
        return Mapping.valueOf(string);
    }

    //////////////////////////////////////////////// TENSOR CREATE /////////////////////////////////////////////////////

    /**
     * Creates {@link Expression} from given l.h.s. and r.h.s.
     * @param lhs l.h.s. of the expression
     * @param rhs r.h.s. of the expression
     * @return expression
     * @see Tensors#parse(java.lang.String)
     * @see Tensors#expression(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
     * @throws cc.redberry.core.parser.ParserException
     *          if expression does not satisfy correct Redberry
     *          input notation for tensors
     * @throws IllegalArgumentException if l.h.s. free indices are not matches r.h.s. free indices
     */
    static Tensor eq(String lhs, Tensor rhs) {
        return expression(parse(lhs), rhs)
    }

    /**
     * Creates {@link Expression} from given l.h.s. and r.h.s.
     * @param lhs l.h.s. of the expression
     * @param rhs r.h.s. of the expression
     * @return expression
     * @see Tensors#expression(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
     * @throws IllegalArgumentException if l.h.s. free indices are not matches r.h.s. free indices
     */
    static Expression eq(Tensor lhs, Tensor rhs) {
        return expression(lhs, rhs)
    }

    ////////////////////////////////////////////// PERMUTATIONS ///////////////////////////////////////////////////////

    static Permutation getP(Object obj) {
        if (obj instanceof Permutation)
            return obj
        else if (obj instanceof List || obj instanceof int[] || obj instanceof int[][]) {
            //check one-line notation
            boolean oneLine = true;
            boolean negative = false;
            for (def i : obj)
                if (!(i instanceof Integer)) {
                    oneLine = false;
                    break;
                } else if (i < 0)
                    negative = true

            if (oneLine && negative)
                obj = obj.collect { i -> -i }

            //check cycles
            if (!oneLine) {
                for (def c in obj) {
                    if (!(c instanceof List))
                        throw new IllegalArgumentException("Permutation is no either in one-line nor in cycles notation " + obj);

                    for (def i in c) {
                        if (!(i instanceof Integer))
                            throw new IllegalArgumentException("Permutation is no either in one-line nor in cycles notation " + obj);
                        else if (i < 0)
                            negative = true
                    }
                }
                if (negative)
                    obj = obj.collect { c -> c.collect { i -> -i } }
            }

            if (oneLine)
                return Permutations.createPermutation(negative, obj as int[])
            return Permutations.createPermutation(negative, obj as int[][])
        } else
            throw new NoSuchMethodException("No such property .p for class " + obj.getClass())
    }

    static boolean equals(Permutation a, Permutation b) {
        return a.equals(b)
    }

    static Permutation negative(Permutation permutation) {
        return permutation.negate();
    }

    static Permutation positive(Permutation permutation) {
        return permutation;
    }

    static Permutation power(Permutation permutation, int exponent) {
        return permutation.pow(exponent);
    }

    static Permutation multiply(Permutation a, Permutation b) {
        return a.composition(b);
    }

    static Iterator<Integer> iterator(final Permutation p) {
        return new PIterator(p)
    }

    private static final class PIterator implements Iterator<Integer> {
        int state = 0;
        final Permutation p;

        PIterator(Permutation p) {
            this.p = p
        }

        @Override
        void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        boolean hasNext() {
            return state < p.degree()
        }

        @Override
        Integer next() {
            ++state
            return p.newIndexOf(state - 1)
        }
    }

    /**
     * Returns image of specified point under the action of this permutation.
     *
     * @param i point
     * @return image of specified point under the action of this permutation
     */
    static int getAt(Permutation p, int i) {
        return p.newIndexOf(i)
    }

    /**
     * Returns image of specified set of points under the action of this permutation.
     *
     * @param set set
     * @return image of specified set under this permutation
     */
    static List getAt(Permutation p, int[] i) {
        return p.imageOf(i) as List
    }

    /**
     * Returns image of specified set of points under the action of this permutation.
     *
     * @param set set
     * @return image of specified set under this permutation
     */
    static List getAt(Permutation p, Collection i) {
        return p.imageOf(i as int[])
    }

    /**
     * Permutes list and returns the result.
     *
     * @param array array
     * @return permuted array
     */
    static List rightShift(Permutation p, List i) {
        return p.permute(i)
    }

    static <T> void permutations(List<T> list, Closure<List<T>> closure) {
        IntPermutationsGenerator generator = new IntPermutationsGenerator(list.size());
        for (int[] permutation : generator) {
            def temp = []
            for (int i : permutation)
                temp << list[i]
            closure.call(temp)
        }
    }

    ////////////////////////////////////////////// LISTS ///////////////////////////////////////////////////////

    static Tensor sum(Collection collection) {
        return Tensors.sum(getT(collection).toArray(new Tensor[collection.size()]))
    }

    static Tensor multiply(Collection collection) {
        return Tensors.multiplyAndRenameConflictingDummies(getT(collection).toArray(new Tensor[collection.size()]))
    }
}
