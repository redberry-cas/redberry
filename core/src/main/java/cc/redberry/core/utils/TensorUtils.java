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
package cc.redberry.core.utils;

import cc.redberry.core.context.VarDescriptor;
import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.groups.permutations.Permutations;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indexmapping.MappingsPort;
import cc.redberry.core.indices.InconsistentIndicesException;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.number.NumberUtils;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.ScalarFunction;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static cc.redberry.core.tensor.Tensors.*;

/**
 * This class contains various useful methods related with tensors.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class TensorUtils {
    private TensorUtils() {
    }

    /**
     * Returns a brief list of tensor properties (type, indices, size, etc.)
     *
     * @param expr expression
     * @return brief list of tensor properties
     */
    public static String info(Tensor expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("// [")
                .append(expr.getClass().getSimpleName()).append(",\n//  ")
                .append("size = ").append(expr.size()).append(",\n//  ")
                .append("symbolic = ").append(isSymbolic(expr)).append(",\n//  ")
                .append("freeIndices = ").append(expr.getIndices().getFree()).append(",\n//  ")
                .append("indices = ").append(expr.getIndices()).append(",\n//  ")
                .append("symbolsCount = ").append(symbolsCount(expr)).append(",\n//  ")
                .append("symbolsAppear = ").append(getAllDiffSimpleTensors(expr))
                .append("\n//]");
        return sb.toString();
    }

    /**
     * Returns the number of symbols contained in expression (including duplicates)
     *
     * @param expr expression
     * @return number of symbols contained in expression (including duplicates)
     */
    public static long symbolsCount(Tensor expr) {
        AtomicLong counter = new AtomicLong();
        symbolsCount(expr, counter);
        return counter.get();
    }

    private static void symbolsCount(Tensor expr, AtomicLong counter) {
        if (expr instanceof SimpleTensor)
            counter.incrementAndGet();
        for (Tensor t : expr)
            symbolsCount(t, counter);
    }

    /**
     * Returns true if at least one free index of {@code u} is contracted
     * with some free index of {@code v}.
     *
     * @param u tensor
     * @param v tensor
     * @return true if at least one free index of {@code u} is contracted
     * with some free index of {@code v}
     */
    public static boolean haveIndicesIntersections(Tensor u, Tensor v) {
        return IndicesUtils.haveIntersections(u.getIndices(), v.getIndices());
    }

    /*
     *       isSomething()
     */

    public static boolean isZeroOrIndeterminate(Tensor tensor) {
        return tensor instanceof Complex && NumberUtils.isZeroOrIndeterminate((Complex) tensor);
    }

    public static boolean isIndeterminate(Tensor tensor) {
        return tensor instanceof Complex && NumberUtils.isIndeterminate((Complex) tensor);
    }

    public static boolean isInteger(Tensor tensor) {
        return tensor instanceof Complex && ((Complex) tensor).isInteger();
    }

    public static boolean isNaturalNumber(Tensor tensor) {
        return tensor instanceof Complex && ((Complex) tensor).isNatural();
    }

    public static boolean isNumeric(Tensor tensor) {
        return tensor instanceof Complex && ((Complex) tensor).isNumeric();
    }

    public static boolean isNegativeNaturalNumber(Tensor tensor) {
        return tensor instanceof Complex && ((Complex) tensor).isNegativeNatural();
    }

    public static boolean isPositiveNaturalNumber(Tensor tensor) {
        return tensor instanceof Complex && ((Complex) tensor).isPositiveNatural();
    }

    public static boolean isRealPositiveNumber(Tensor tensor) {
        if (tensor instanceof Complex) {
            Complex complex = (Complex) tensor;
            return complex.isReal() && complex.getReal().signum() > 0;
        }
        return false;
    }

    public static boolean isRealNegativeNumber(Tensor tensor) {
        if (tensor instanceof Complex) {
            Complex complex = (Complex) tensor;
            return complex.isReal() && complex.getReal().signum() < 0;
        }
        return false;
    }

    public static boolean isIndexless(Tensor... tensors) {
        for (Tensor t : tensors)
            if (!isIndexless1(t))
                return false;
        return true;
    }

    private static boolean isIndexless1(Tensor tensor) {
        return tensor.getIndices().size() == 0;
    }

    public static boolean isScalar(Tensor... tensors) {
        for (Tensor t : tensors)
            if (!isScalar1(t))
                return false;
        return true;
    }

    private static boolean isScalar1(Tensor tensor) {
        return tensor.getIndices().getFree().size() == 0;
    }


    public static boolean isSymbol(Tensor t) {
        return t.getClass() == SimpleTensor.class && t.getIndices().size() == 0;
    }

    public static boolean isSymbolOrNumber(Tensor t) {
        return t instanceof Complex || isSymbol(t);
    }

    public static boolean isSymbolic(Tensor t) {
        if (t.getIndices().size() != 0)
            return false;
        for (Tensor c : t)
            if (!isSymbolic(c))
                return false;
        return true;
    }

    public static boolean isSymbolic(Tensor... tensors) {
        for (Tensor t : tensors)
            if (!isSymbolic(t))
                return false;
        return true;
    }

    public static boolean isOne(Tensor tensor) {
        return tensor instanceof Complex && ((Complex) tensor).isOne();
    }

    public static boolean isZero(Tensor tensor) {
        return tensor instanceof Complex && ((Complex) tensor).isZero();
    }

    public static boolean isImageOne(Tensor tensor) {
        return tensor instanceof Complex && tensor.equals(Complex.IMAGINARY_UNIT);
    }

    public static boolean isMinusOne(Tensor tensor) {
        return tensor instanceof Complex && tensor.equals(Complex.MINUS_ONE);
    }

    public static boolean isIntegerOdd(Tensor tensor) {
        return tensor instanceof Complex && NumberUtils.isIntegerOdd((Complex) tensor);
    }

    public static boolean isIntegerEven(Tensor tensor) {
        return tensor instanceof Complex && NumberUtils.isIntegerEven((Complex) tensor);
    }

    /**
     * Returns true, if specified tensor is a^(N), where N - a natural number
     *
     * @param t tensor
     * @return true, if specified tensor is a^(N), where N - a natural number
     */
    public static boolean isPositiveIntegerPower(Tensor t) {
        return t instanceof Power && isPositiveNaturalNumber(t.get(1));
    }


    /**
     * Returns true, if specified tensor is {@code a^(N)}, where {@code N} - a natural number and {@code a} - is a
     * simple tensor
     *
     * @param t tensor
     * @return true, if specified tensor is {@code a^(N)}, where {@code N} - a natural number and {@code a} - is a
     * simple tensor
     */
    public static boolean isPositiveIntegerPowerOfSimpleTensor(Tensor t) {
        return isPositiveIntegerPower(t) && t.get(0) instanceof SimpleTensor;
    }

    /**
     * Returns true, if specified tensor is {@code a^(N)}, where {@code N} - a natural number and {@code a} - is a
     * product of tensors
     *
     * @param t tensor
     * @return true, if specified tensor is {@code a^(N)}, where {@code N} - a natural number and {@code a} - is a
     * product of tensors
     */
    public static boolean isPositiveIntegerPowerOfProduct(Tensor t) {
        return isPositiveIntegerPower(t) && t.get(0) instanceof Product;
    }

    /**
     * Returns true, if specified tensor is a^(-N), where N - a natural number
     *
     * @param t tensor
     * @return true, if specified tensor is a^(-N), where N - a natural number
     */
    public static boolean isNegativeIntegerPower(Tensor t) {
        return t instanceof Power && TensorUtils.isNegativeNaturalNumber(t.get(1));
    }

    /**
     * Returns {@code true} if tensor contains dummy indices.
     *
     * @param tensor tensor
     * @return {@code true} if tensor contains dummy indices
     */
    public static boolean passOutDummies(Tensor tensor) {
        return getAllDummyIndicesT(tensor).size() != 0;
    }


    /**
     * Returns whether specified tensor contains at least one of the simple tensors from the set. The set represents a
     * unique names of simple tensors.
     *
     * @param tensor     tensors
     * @param setOfNames int set of simple tensors names
     * @return true if tensor contains at least one of simple tensor with name that contains in the set
     */
    public static boolean containsSimpleTensors(Tensor tensor, TIntSet setOfNames) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(tensor);
        Tensor current;
        boolean contains = false;
        while ((current = iterator.next()) != null)
            if (current instanceof SimpleTensor && setOfNames.contains(((SimpleTensor) current).getName())) {
                contains = true;
                break;
            }
        return contains;
    }

    /**
     * Returns whether expressions contains imaginary parts (I)
     *
     * @param t expression
     * @return whether expressions contains imaginary parts (I)
     */
    public static boolean hasImaginaryPart(Tensor t) {
        if (t instanceof Complex)
            return !((Complex) t).getImaginary().isZero();
        else for (Tensor f : t)
            if (hasImaginaryPart(f))
                return true;
        return false;
    }

    public static boolean equalsExactly(Tensor[] u, Tensor[] v) {
        if (u.length != v.length)
            return false;
        for (int i = 0; i < u.length; ++i)
            if (!TensorUtils.equalsExactly(u[i], v[i]))
                return false;
        return true;
    }

    public static boolean equalsExactly(Tensor u, String v) {
        return equalsExactly(u, Tensors.parse(v));
    }

    public static boolean equalsExactly(Tensor u, Tensor v) {
        if (u == v)
            return true;
        if (u.getClass() != v.getClass())
            return false;
        if (u instanceof Complex)
            return u.equals(v);
        if (u.hashCode() != v.hashCode())
            return false;
        if (u.getClass() == SimpleTensor.class)
            return u.getIndices().equals(v.getIndices());
        if (u.size() != v.size())
            return false;
        if (u instanceof MultiTensor) {
            final int size = u.size();

            int[] hashArray = new int[size];
            int i;
            for (i = 0; i < size; ++i)
                if ((hashArray[i] = u.get(i).hashCode()) != v.get(i).hashCode())
                    return false;
            int begin = 0, stretchLength, j, n;
            for (i = 1; i <= size; ++i)
                if (i == size || hashArray[i] != hashArray[i - 1]) {
                    if (i - 1 != begin) {
                        stretchLength = i - begin;
                        boolean[] usedPos = new boolean[stretchLength];
                        OUT:
                        for (n = begin; n < i; ++n) {
                            for (j = begin; j < i; ++j)
                                if (!usedPos[(j - begin)] && equalsExactly(u.get(n), v.get(j))) {
                                    usedPos[j - begin] = true;
                                    continue OUT;
                                }
                            return false;
                        }
                        return true;
                    } else if (!equalsExactly(u.get(i - 1), v.get(i - 1)))
                        return false;
                    begin = i;
                }
        }
        if (u.getClass() == TensorField.class) {
            if (((TensorField) u).getHead().getName() != ((TensorField) v).getHead().getName()
                    || !u.getIndices().equals(v.getIndices()))
                return false;
        }

        final int size = u.size();
        for (int i = 0; i < size; ++i)
            if (!equalsExactly(u.get(i), v.get(i)))
                return false;
        return true;
    }

    public static TIntHashSet getAllDummyIndicesT(Tensor tensor) {
        return getAllDummyIndicesT(false, tensor);
    }

    public static TIntHashSet getAllDummyIndicesIncludingScalarFunctionsT(Tensor tensor) {
        return getAllDummyIndicesT(true, tensor);
    }

    private static TIntHashSet getAllDummyIndicesT(boolean includeScalarFunctions, Tensor tensor) {
        TIntHashSet set = new TIntHashSet();
        appendAllIndicesNamesT(tensor, set, includeScalarFunctions);
        set.removeAll(IndicesUtils.getIndicesNames(tensor.getIndices().getFree()));
        return set;
    }

    public static TIntHashSet getAllIndicesNamesT(Collection<? extends Tensor> tensors) {
        TIntHashSet set = new TIntHashSet();
        for (Tensor tensor : tensors)
            appendAllIndicesNamesT(tensor, set);
        return set;
    }

    public static TIntHashSet getAllIndicesNamesT(Tensor... tensors) {
        TIntHashSet set = new TIntHashSet();
        for (Tensor tensor : tensors)
            appendAllIndicesNamesT(tensor, set, false);
        return set;
    }


    public static void appendAllIndicesNamesT(Tensor tensor, TIntHashSet set) {
        appendAllIndicesNamesT(tensor, set, false);
    }

    public static void appendAllIndicesNamesIncludingScalarFunctionsT(Tensor tensor, TIntHashSet set) {
        appendAllIndicesNamesT(tensor, set, true);
    }

    private static void appendAllIndicesNamesT(Tensor tensor, TIntHashSet set, boolean includeScalarFunctions) {
        if (tensor instanceof SimpleTensor) {
            Indices ind = tensor.getIndices();
            set.ensureCapacity(ind.size());
            final int size = ind.size();
            for (int i = 0; i < size; ++i)
                set.add(IndicesUtils.getNameWithType(ind.get(i)));
        } else if (tensor instanceof TensorField) {
            TensorField tf = (TensorField) tensor;
            appendAllIndicesNamesT(tf.getHead(), set, includeScalarFunctions);
            VarDescriptor headDescriptor = tf.getHead().getVarDescriptor();
            if (headDescriptor.propagatesIndices())
                for (int i = tf.size() - 1; i >= 0; --i)
                    if (headDescriptor.propagatesIndices(i))
                        appendAllIndicesNamesT(tf.get(i), set, includeScalarFunctions);
        } else if (tensor instanceof Power) {
            appendAllIndicesNamesT(tensor.get(0), set);
        } else if (tensor instanceof ScalarFunction && !includeScalarFunctions)
            return;
        else {
            Tensor t;
            for (int i = tensor.size() - 1; i >= 0; --i) {
                t = tensor.get(i);
                appendAllIndicesNamesT(t, set);
            }
        }
    }

    /**
     * Returns {@code true} if tensor u mathematically (not programming) equals to tensor v.
     *
     * @param u tensor
     * @param v tensor
     * @return {@code true} if specified tensors are mathematically (not programming) equal
     */
    public static boolean equals(Tensor u, Tensor v) {
        return IndexMappings.equals(u, v);
    }

    /**
     * Returns {@code true} if tensor u mathematically (not programming) equals to tensor v,
     * {@code false} if they they differ only in the sign and {@code null} otherwise.
     *
     * @param u tensor
     * @param v tensor
     * @return {@code true} {@code false} if tensor u mathematically (not programming) equals to tensor v,
     * {@code true} if they they differ only in the sign and {@code null} otherwise
     */
    public static Boolean compare1(Tensor u, Tensor v) {
        return IndexMappings.compare1(u, v);
    }

    public static void assertIndicesConsistency(Tensor t) {
        assertIndicesConsistency(t, new TIntHashSet());
    }

    private static void assertIndicesConsistency(Tensor t, TIntHashSet indices) {
        if (t instanceof SimpleTensor) {
            Indices ind = t.getIndices();
            for (int i = ind.size() - 1; i >= 0; --i)
                if (indices.contains(ind.get(i)))
                    throw new AssertionError(new InconsistentIndicesException(ind.get(i)));
                else
                    indices.add(ind.get(i));
        }
        if (t instanceof Product)
            for (int i = t.size() - 1; i >= 0; --i)
                assertIndicesConsistency(t.get(i), indices);
        if (t instanceof Sum) {
            TIntHashSet sumIndices = new TIntHashSet(), temp;
            for (int i = t.size() - 1; i >= 0; --i) {
                temp = new TIntHashSet(indices);
                assertIndicesConsistency(t.get(i), temp);
                appendAllIndicesT(t.get(i), sumIndices);
            }
            indices.addAll(sumIndices);
        }
        if (t instanceof Expression)//FUTURE incorporate expression correctly
            for (Tensor c : t)
                assertIndicesConsistency(c, new TIntHashSet(indices));
    }

    private static void appendAllIndicesT(Tensor tensor, TIntHashSet set) {
        if (tensor instanceof SimpleTensor) {
            Indices ind = tensor.getIndices();
            final int size = ind.size();
            for (int i = 0; i < size; ++i)
                set.add(ind.get(i));
        } else if (tensor instanceof Power) {
            appendAllIndicesT(tensor.get(0), set);
        } else {
            Tensor t;
            for (int i = tensor.size() - 1; i >= 0; --i) {
                t = tensor.get(i);
                if (t instanceof ScalarFunction)
                    continue;
                appendAllIndicesT(t, set);
            }
        }
    }

    /**
     * Returns {@code true} if specified tensor is zero in consequence of its symmetries: is both symmetric and
     * asymmetric with respect to some permutation at the same time.
     *
     * @param t tensor
     * @return {@code true} if specified tensor is zero in consequence of its symmetries
     */
    public static boolean isZeroDueToSymmetry(Tensor t) {
        return IndexMappings.isZeroDueToSymmetry(t);
    }

    private static Permutation getSymmetryFromMapping1(final int[] sortedIndicesNames, final int[] _sortPermutation, Mapping mapping) {
        final int dimension = sortedIndicesNames.length;

        IntArray _fromIndices = mapping.getFromNames();
        IntArray _toIndices = mapping.getToData();

        int[] permutation = new int[dimension];
        Arrays.fill(permutation, -1);

        int i, fromIndex, positionInFrom, positionInIndices;
        for (i = 0; i < dimension; ++i) {
            fromIndex = sortedIndicesNames[i];
            positionInFrom = ArraysUtils.binarySearch(_fromIndices, fromIndex);
            if (positionInFrom < 0)
                continue;

            positionInIndices = Arrays.binarySearch(sortedIndicesNames,
                    IndicesUtils.getNameWithType(_toIndices.get(positionInFrom)));

            if (positionInIndices < 0)
//                 throw new IllegalArgumentException();
//                todo review
                return Permutations.createIdentityPermutation(dimension);

            permutation[_sortPermutation[i]] = _sortPermutation[positionInIndices];
        }
        for (i = 0; i < dimension; ++i)
            if (permutation[i] == -1)
                permutation[i] = i;

        return Permutations.createPermutation(mapping.getSign(), permutation); //this is inverse permutation
    }

    public static Permutation getSymmetryFromMapping(final int[] indices, Mapping mapping) {
        int[] sortedIndicesNames = IndicesUtils.getIndicesNames(indices);
        int[] _sortPermutation = ArraysUtils.quickSortP(sortedIndicesNames);
        return getSymmetryFromMapping1(sortedIndicesNames, _sortPermutation, mapping);
    }

    public static List<Permutation> getSymmetriesFromMappings(final int[] indices, MappingsPort mappingsPort) {
        List<Permutation> symmetries = new ArrayList<>();
        int[] sortedIndicesNames = IndicesUtils.getIndicesNames(indices);
        int[] sortPermutation = ArraysUtils.quickSortP(sortedIndicesNames);
        Mapping buffer;
        while ((buffer = mappingsPort.take()) != null)
            symmetries.add(getSymmetryFromMapping1(sortedIndicesNames, sortPermutation, buffer));
        return symmetries;
    }

    public static List<Permutation> findIndicesSymmetries(int[] indices, Tensor tensor) {
        if (isZero(tensor))
            return Collections.singletonList(Permutations.createIdentityPermutation(0));
        return getSymmetriesFromMappings(indices, IndexMappings.createPort(tensor, tensor));
    }

    public static List<Permutation> findIndicesSymmetries(SimpleIndices indices, Tensor tensor) {
        if (isZero(tensor))
            return Collections.singletonList(Permutations.createIdentityPermutation(0));
        return getSymmetriesFromMappings(indices.getAllIndices().copy(), IndexMappings.createPort(tensor, tensor));
    }

    public static List<Permutation> getIndicesSymmetriesForIndicesWithSameStates(final int[] indices, Tensor tensor) {
        List<Permutation> total = findIndicesSymmetries(indices, tensor);
        List<Permutation> symmetries = new ArrayList<>();
        int i;
        OUT:
        for (Permutation s : total) {
            for (i = 0; i < indices.length; ++i)
                if (IndicesUtils.getRawStateInt(indices[i]) != IndicesUtils.getRawStateInt(indices[s.newIndexOf(i)]))
                    continue OUT;
            symmetries.add(s);
        }
        return symmetries;
    }

    public static THashSet<Tensor> getAllScalars(Tensor... tensors) {
        THashSet<Tensor> set = new THashSet<>();
        for (Tensor tensor : tensors)
            addScalars(tensor, set);
        return set;
    }

    private static void addScalars(Tensor t, THashSet<Tensor> set) {
        if (t instanceof Product)
            set.addAll(Arrays.asList(((Product) t).getContent().getScalars()));
        else
            for (Tensor r : t)
                addScalars(r, set);
    }


    public static Set<SimpleTensor> getAllSymbols(Tensor... tensors) {
        Set<SimpleTensor> set = new HashSet<>();
        for (Tensor tensor : tensors)
            addSymbols(tensor, set);
        return set;
    }

    private static void addSymbols(Tensor tensor, Set<SimpleTensor> set) {
        if (isSymbol(tensor)) {
            set.add((SimpleTensor) tensor);
        } else
            for (Tensor t : tensor)
                addSymbols(t, set);
    }

    public static Set<SimpleTensor> getAllSymbolsAndSymbolicFields(Tensor... tensors) {
        THashSet<SimpleTensor> set = new THashSet<>();
        for (Tensor tensor : tensors)
            addSymbols(tensor, set);
        return set;
    }


    private static void addSymbolsAndSymbolicFields(Tensor tensor, Set<SimpleTensor> set) {
        if (tensor instanceof SimpleTensor && tensor.getIndices().size() == 0) {
            boolean contentSymbolicQ = true;
            for (Tensor t : tensor)
                if (!isSymbolic(t)) {
                    contentSymbolicQ = false;
                    break;
                }
            if (contentSymbolicQ)
                set.add((SimpleTensor) tensor);
        } else
            for (Tensor t : tensor)
                addSymbolsAndSymbolicFields(t, set);
    }


    public static Collection<SimpleTensor> getAllDiffSimpleTensors(Tensor... tensors) {
        TIntObjectHashMap<SimpleTensor> names = new TIntObjectHashMap<>();
        for (Tensor tensor : tensors)
            addAllDiffSimpleTensors(tensor, names);
        return names.valueCollection();
    }

    private static void addAllDiffSimpleTensors(Tensor tensor, TIntObjectHashMap<SimpleTensor> names) {
        if (tensor instanceof SimpleTensor)
            names.put(((SimpleTensor) tensor).getName(), (SimpleTensor) tensor);
        else
            for (Tensor t : tensor)
                addAllDiffSimpleTensors(t, names);
    }


    public static TIntHashSet getAllNamesOfSymbols(Tensor... tensors) {
        TIntHashSet set = new TIntHashSet();
        for (Tensor tensor : tensors)
            addSymbolsNames(tensor, set);
        return set;
    }


    private static void addSymbolsNames(Tensor tensor, TIntHashSet set) {
        if (isSymbol(tensor)) {
            set.add(((SimpleTensor) tensor).getName());
        } else
            for (Tensor t : tensor)
                addSymbolsNames(t, set);
    }


    public static int treeDepth(Tensor tensor) {
        if (tensor.getClass() == SimpleTensor.class
                || tensor instanceof Complex)
            return 0;
        int depth = 1, temp;
        for (Tensor t : tensor) {
            if ((temp = treeDepth(t) + 1) > depth)
                depth = temp;
        }
        return depth;
    }

    /**
     * Gives a determinant of matrix.
     *
     * @param matrix matrix
     * @return determinant
     */
    public static Tensor det(Tensor[][] matrix) {
        checkMatrix(matrix);
        return det1(matrix);
    }

    /**
     * Gives inverse matrix.
     *
     * @param matrix matrix
     * @return inverse matrix
     */
    public static Tensor[][] inverse(Tensor[][] matrix) {
        checkMatrix(matrix);
        if (matrix.length == 1)
            return new Tensor[][]{{reciprocal(matrix[0][0])}};

        Tensor det = det(matrix);

        int length = matrix.length;
        Tensor[][] inverse = new Tensor[length][length];
        for (int i = 0; i < length; ++i) {
            for (int j = 0; j < length; ++j) {
                inverse[j][i] = divideAndRenameConflictingDummies(det(deleteFromMatrix(matrix, i, j)), det);
                if ((i + j) % 2 != 0)
                    inverse[j][i] = negate(inverse[j][i]);
            }
        }
        return inverse;
    }

    private static void checkMatrix(Tensor[][] tensors) {
        if (tensors.length == 0)
            throw new RuntimeException("Empty matrix.");
        int cc = tensors.length;
        for (Tensor[] tt : tensors)
            if (tt.length != cc)
                throw new IllegalArgumentException("Non square matrix");
    }

    private static Tensor det1(Tensor[][] matrix) {
        if (matrix.length == 1)
            return matrix[0][0];

        SumBuilder sum = new SumBuilder();
        Tensor temp;
        for (int i = 0; i < matrix.length; ++i) {
            temp = multiplyAndRenameConflictingDummies(matrix[0][i], det(deleteFromMatrix(matrix, 0, i)));
            if (i % 2 == 1)
                temp = negate(temp);
            sum.put(temp);
        }
        return sum.build();
    }

    private static Tensor[][] deleteFromMatrix(final Tensor[][] matrix, int row, int column) {
        if (matrix.length == 1)
            return new Tensor[0][0];
        Tensor[][] newMatrix = new Tensor[matrix.length - 1][matrix.length - 1];
        int cRow = 0, cColumn, j;
        for (int i = 0; i < matrix.length; ++i) {
            if (i == row)
                continue;
            cColumn = 0;
            for (j = 0; j < matrix.length; ++j) {
                if (j == column)
                    continue;
                newMatrix[cRow][cColumn++] = matrix[i][j];
            }
            ++cRow;
        }
        return newMatrix;
    }

    public static boolean containsFractions(Tensor tensor) {
        if (tensor instanceof SimpleTensor)
            return false;
        if (tensor instanceof Power)
            return isNegativeNaturalNumber(tensor.get(1));
        for (Tensor t : tensor) {
            if (containsFractions(t))
                return true;
        }
        return false;
    }

    public static TIntHashSet getSimpleTensorsNames(Tensor t) {
        return addSimpleTensorsNames(t, new TIntHashSet());
    }

    private static TIntHashSet addSimpleTensorsNames(Tensor t, TIntHashSet names) {
        if (t instanceof TensorField)
            names.add(((TensorField) t).getHead().getName());
        if (t instanceof SimpleTensor)
            names.add(((SimpleTensor) t).getName());
        for (Tensor tt : t)
            addSimpleTensorsNames(tt, names);

        return names;
    }

    public static boolean shareSimpleTensors(Tensor a, Tensor b) {
        return testContainsNames(b, getSimpleTensorsNames(a));
    }

    private static boolean testContainsNames(Tensor t, TIntHashSet names) {
        if (t instanceof TensorField) {
            if (names.contains(((TensorField) t).getHead().getName())) return true;
        } else if (t instanceof SimpleTensor)
            return names.contains(((SimpleTensor) t).getName());

        for (Tensor tt : t)
            if (testContainsNames(tt, names)) return true;

        return false;
    }
//
//    /**
//     * Generates a set of replacement rules for all scalar (but not symbolic) sub-tensors appearing in the specified
//     * tensor.
//     *
//     * @param tensor tensor
//     * @return set of replacement rules for all scalar (but not symbolic) sub-tensors appearing in the specified
//     * tensor
//     */
//    public static Expression[] generateReplacementsOfScalars(Tensor tensor) {
//        return generateReplacementsOfScalars(tensor, CC.getParametersGenerator());
//    }

    /**
     * Generates a set of replacement rules for all scalar (but not symbolic) sub-tensors appearing in the specified
     * tensor.
     *
     * @param tensor                tensor
     * @param generatedCoefficients allows to control how coefficients are generated
     * @return set of replacement rules for all scalar (but not symbolic) sub-tensors appearing in the specified
     * tensor
     * @see LocalSymbolsProvider
     */
    public static Expression[] generateReplacementsOfScalars(Tensor tensor,
                                                             OutputPort<SimpleTensor> generatedCoefficients) {
        THashSet<Tensor> scalars = getAllScalars(tensor);
        Expression[] replacements = new Expression[scalars.size()];
        int i = -1;
        for (Tensor scalar : scalars)
            replacements[++i] = expression(scalar, generatedCoefficients.take());
        return replacements;
    }

    /**
     * Returns the number of occurences of {@code patterns} in {@code expression}
     *
     * @param expression expression
     * @param patterns   patterns
     * @return number of occurences of {@code patterns} in {@code expression}
     */
    public static int Count(final Tensor expression, final Tensor... patterns) {
        return Count(expression, 1, Arrays.asList(patterns), false);
    }

    /**
     * Returns the number of occurences of {@code patterns} in {@code expression}
     *
     * @param expression expression
     * @param patterns   patterns
     * @return number of occurences of {@code patterns} in {@code expression}
     */
    public static int Count(final Tensor expression, final List<Tensor> patterns) {
        return Count(expression, Integer.MAX_VALUE, patterns, false);
    }

    /**
     * Returns the number of occurences of {@code patterns} in {@code expression}
     *
     * @param expression expression
     * @param patterns   patterns
     * @param level      level specification
     * @return number of occurences of {@code patterns} in {@code expression}
     */
    public static int Count(final Tensor expression, final int level, final Tensor... patterns) {
        return Count(expression, level, Arrays.asList(patterns), false);
    }

    /**
     * Returns the number of occurences of {@code patterns} in {@code expression}
     *
     * @param expression expression
     * @param patterns   patterns
     * @param level      level specification
     * @return number of occurences of {@code patterns} in {@code expression}
     */
    public static int Count(final Tensor expression, final int level, final List<Tensor> patterns, final boolean sumPowers) {
        if (level == 0)
            return 0;
        if (level < 0)
            throw new IllegalArgumentException();
        int count = 0;

        out:
        for (Tensor el : expression) {
            for (Tensor p : patterns) {
                int c = match0(el, p, sumPowers);
                count += c;
                if (c > 0)
                    continue out;
            }
            count += Count(el, level - 1, patterns, sumPowers);
        }

        return count;
    }

    /**
     * Gives the maximum power with which {@code pattern} appears in the expanded form of {@code expression}.
     *
     * @param expression expression
     * @param pattern    simple tensor or field
     * @return maximum power with which {@code pattern} appears in the expanded form of {@code expression}
     */
    public static int Exponent(final Tensor expression, Tensor... pattern) {
        return Exponent(expression, Arrays.asList(pattern));
    }

    /**
     * Gives the maximum power with which {@code pattern} appears in the expanded form of {@code expression}.
     *
     * @param expression expression
     * @param pattern    simple tensor or field
     * @return maximum power with which {@code pattern} appears in the expanded form of {@code expression}
     */
    public static int Exponent(final Tensor expression, List<Tensor> pattern) {
        if (expression instanceof SimpleTensor)
            return match1(expression, pattern);
        else if (isPositiveIntegerPower(expression)) {
            return ((Complex) expression.get(1)).intValue() * Exponent(expression.get(0), pattern);
        } else if (expression instanceof Product) {
            int exponent = 0;
            for (Tensor tensor : expression)
                exponent += Exponent(tensor, pattern);
            return exponent;
        } else if (expression instanceof Sum) {
            int exponent = 0;
            for (Tensor tensor : expression)
                exponent = Math.max(exponent, Exponent(tensor, pattern));
            return exponent;
        } else return 0;
    }

    private static int match0(final Tensor el, final Tensor patt, final boolean sumPowers) {
        if (sumPowers && isPositiveIntegerPower(el))
            return ((Complex) el.get(1)).intValue() * match0(el.get(0), patt, false);
        else if (IndexMappings.anyMappingExists(patt, el))
            return 1;
//        else if (patt instanceof TensorField && el instanceof TensorField
//                && !((TensorField) patt).isDerivative())
//            return (((TensorField) el).getParentField().getName() == ((TensorField) patt).getName()) ? 1 : 0;
        return 0;
    }

    private static int match1(final Tensor el, final List<Tensor> patterns) {
        for (Tensor patt : patterns) {
            if (IndexMappings.anyMappingExists(patt, el))
                return 1;
//            else if (patt instanceof TensorField
//                    && el instanceof TensorField
//                    && !((TensorField) patt).isDerivative()
//                    && (((TensorField) el).getParentField().getName() == ((TensorField) patt).getName())
//                    )
//                return 1;
        }
        return 0;
    }

    public static boolean containsImaginary(final Tensor t) {
        if (t instanceof Complex)
            return !((Complex) t).getImaginary().isZero();
        for (Tensor tensor : t)
            if (containsImaginary(tensor))
                return true;
        return false;
    }
}
