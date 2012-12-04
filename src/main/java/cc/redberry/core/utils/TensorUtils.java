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
 * the Free Software Foundation, either version 2 of the License, or
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

import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.combinatorics.symmetries.SymmetriesFactory;
import cc.redberry.core.indexmapping.*;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.number.NumberUtils;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.ScalarFunction;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorUtils {

    private TensorUtils() {
    }

    /**
     * Returns true if at least one free index of {@code u} is contracted
     * with some free index of {@code v}.
     *
     * @param u tensor
     * @param v tensor
     * @return true if at least one free index of {@code u} is contracted
     *         with some free index of {@code v}
     */
    public static boolean haveIndicesIntersections(Tensor u, Tensor v) {
        return IndicesUtils.haveIntersections(u.getIndices(), v.getIndices());
    }

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

    public static boolean isNegativeIntegerNumber(Tensor tensor) {
        return tensor instanceof Complex && ((Complex) tensor).isNegativeInteger();
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
        if (t.getClass() == SimpleTensor.class)
            return t.getIndices().size() == 0;
        if (t instanceof TensorField) {
            boolean b = t.getIndices().size() == 0;
            if (!b)
                return false;
        }
        if (t instanceof Complex)
            return true;
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

    public static boolean passOutDummies(Tensor tensor) {
        return getAllDummyIndicesT(tensor).size() != 0;
    }

    public static boolean isOne(Tensor tensor) {
        return tensor instanceof Complex && ((Complex) tensor).isOne();
    }

    public static boolean isZero(Tensor tensor) {
        return tensor instanceof Complex && ((Complex) tensor).isZero();
    }

    public static boolean isImageOne(Tensor tensor) {
        return tensor instanceof Complex && tensor.equals(Complex.IMAGE_ONE);
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
        return t instanceof Power && TensorUtils.isNaturalNumber(t.get(1));
    }

    /**
     * Returns true, if specified tensor is a^(-N), where N - a natural number
     *
     * @param t tensor
     * @return true, if specified tensor is a^(-N), where N - a natural number
     */
    public static boolean isNegativeIntegerPower(Tensor t) {
        return t instanceof Power && TensorUtils.isNegativeIntegerNumber(t.get(1));
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
            if (((SimpleTensor) u).getName() != ((SimpleTensor) v).getName()
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
        TIntHashSet set = new TIntHashSet();
        appendAllIndicesNamesT(tensor, set);
        set.removeAll(IndicesUtils.getIndicesNames(tensor.getIndices().getFree()));
        return set;
    }

    public static TIntHashSet getAllIndicesNamesT(Tensor... tensors) {
        TIntHashSet set = new TIntHashSet();
        for (Tensor tensor : tensors)
            appendAllIndicesNamesT(tensor, set);
        return set;
    }

    public static void appendAllIndicesNamesT(Tensor tensor, TIntHashSet set) {
        if (tensor instanceof SimpleTensor) {
            Indices ind = tensor.getIndices();
            set.ensureCapacity(ind.size());
            final int size = ind.size();
            for (int i = 0; i < size; ++i)
                set.add(IndicesUtils.getNameWithType(ind.get(i)));
        } else if (tensor instanceof Power) {
            appendAllIndicesNamesT(tensor.get(0), set);
        } else if (tensor instanceof ScalarFunction)
            return;
        else {
            Tensor t;
            for (int i = tensor.size() - 1; i >= 0; --i) {
                t = tensor.get(i);
                appendAllIndicesNamesT(t, set);
            }
        }
    }

    public static boolean equals(Tensor u, Tensor v) {
        if (u == v)
            return true;
        Indices freeIndices = u.getIndices().getFree();
        if (!freeIndices.equalsRegardlessOrder(v.getIndices().getFree()))
            return false;
        int[] free = freeIndices.getAllIndices().copy();
        IndexMappingBuffer tester = new IndexMappingBufferTester(free, false);
        MappingsPort mp = IndexMappings.createPort(tester, u, v);
        IndexMappingBuffer buffer;

        while ((buffer = mp.take()) != null)
            if (!buffer.getSignum())
                return true;

        return false;
    }

    public static Boolean compare1(Tensor u, Tensor v) {
        Indices freeIndices = u.getIndices().getFree();
        if (!freeIndices.equalsRegardlessOrder(v.getIndices().getFree()))
            return null;
        int[] free = freeIndices.getAllIndices().copy();
        IndexMappingBuffer tester = new IndexMappingBufferTester(free, false);
        IndexMappingBuffer buffer = IndexMappings.createPort(tester, u, v).take();
        if (buffer == null)
            return null;
        return buffer.getSignum();
    }

    public static void assertIndicesConsistency(Tensor t) {
        assertIndicesConsistency(t, new TIntHashSet());
    }

    private static void assertIndicesConsistency(Tensor t, TIntHashSet indices) {
        if (t instanceof SimpleTensor) {
            Indices ind = t.getIndices();
            for (int i = ind.size() - 1; i >= 0; --i)
                if (indices.contains(ind.get(i)))
                    throw new AssertionError();
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

    public static boolean isZeroDueToSymmetry(Tensor t) {
        int[] indices = IndicesUtils.getIndicesNames(t.getIndices().getFree());
        IndexMappingBufferTester bufferTester = new IndexMappingBufferTester(indices, false);
        MappingsPort mp = IndexMappings.createPort(bufferTester, t, t);
        IndexMappingBuffer buffer;
        while ((buffer = mp.take()) != null)
            if (buffer.getSignum())
                return true;
        return false;
    }

    private static Symmetry getSymmetryFromMapping1(final int[] indicesNames, IndexMappingBuffer indexMappingBuffer) {
        final int dimension = indicesNames.length;
        int[] permutation = new int[dimension];
        Arrays.fill(permutation, -1);
        int i;
        for (i = 0; i < dimension; ++i) {
            int fromIndex = indicesNames[i];
            IndexMappingBufferRecord record = indexMappingBuffer.getMap().get(fromIndex);
            if (record == null) {
                return new Symmetry(dimension);
                //todo discuss with Dima
                //throw new IllegalArgumentException("Index " + IndicesUtils.toString(fromIndex) + " does not contains in specified IndexMappingBuffer.");
            }
            int newPosition = -1;
            //TODO refactor with sort and binary search
            for (int j = 0; j < dimension; ++j)
                if (indicesNames[j] == record.getIndexName()) {
                    newPosition = j;
                    break;
                }
            if (newPosition < 0) {
                return new Symmetry(dimension);
                //todo discuss with Dima
                //throw new IllegalArgumentException("Index " + IndicesUtils.toString(record.getIndexName()) + " does not contains in specified indices array.");
            }
            permutation[i] = newPosition;
        }
        for (i = 0; i < dimension; ++i)
            if (permutation[i] == -1)
                permutation[i] = i;
        return new Symmetry(permutation, indexMappingBuffer.getSignum());
    }

    public static Symmetry getSymmetryFromMapping(final int[] indices, IndexMappingBuffer indexMappingBuffer) {
        return getSymmetryFromMapping1(IndicesUtils.getIndicesNames(indices), indexMappingBuffer);
    }

    public static Symmetries getSymmetriesFromMappings(final int[] indices, MappingsPort mappingsPort) {
        Symmetries symmetries = SymmetriesFactory.createSymmetries(indices.length);
        int[] indicesNames = IndicesUtils.getIndicesNames(indices);
        IndexMappingBuffer buffer;
        while ((buffer = mappingsPort.take()) != null)
            symmetries.add(getSymmetryFromMapping1(indicesNames, buffer));
        return symmetries;
    }

    public static Symmetries findIndicesSymmetries(int[] indices, Tensor tensor) {
        return getSymmetriesFromMappings(indices, IndexMappings.createPort(tensor, tensor));
    }

    public static Symmetries findIndicesSymmetries(SimpleIndices indices, Tensor tensor) {
        return getSymmetriesFromMappings(indices.getAllIndices().copy(), IndexMappings.createPort(tensor, tensor));
    }

    public static Symmetries getIndicesSymmetriesForIndicesWithSameStates(final int[] indices, Tensor tensor) {
        Symmetries total = findIndicesSymmetries(indices, tensor);
        Symmetries symmetries = SymmetriesFactory.createSymmetries(indices.length);
        int i;
        OUT:
        for (Symmetry s : total) {
            for (i = 0; i < indices.length; ++i)
                if (IndicesUtils.getRawStateInt(indices[i]) != IndicesUtils.getRawStateInt(indices[s.newIndexOf(i)]))
                    continue OUT;
            symmetries.add(s);
        }
        return symmetries;
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

    public static Tensor det(Tensor[][] matrix) {
        checkMatrix(matrix);
        return det1(matrix);
    }

    private static void checkMatrix(Tensor[][] tensors) {
        int cc = tensors.length;
        for (Tensor[] tt : tensors)
            if (tt.length != cc)
                throw new IllegalArgumentException("Non square matrix");
    }

    private static Tensor det1(Tensor[][] matrix) {
        if (matrix.length == 1)
            return matrix[0][0];

        Tensor sum = Complex.ZERO;
        Tensor temp;
        for (int i = 0; i < matrix.length; ++i) {
            temp = multiply(matrix[0][i], det(deleteFromMatrix(matrix, 0, i)));
            if (i % 2 == 1)
                temp = negate(temp);
            sum = sum(sum, temp);
        }
        return sum;
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
            return isNegativeIntegerNumber(tensor.get(1));
        for (Tensor t : tensor) {
            if (containsFractions(t))
                return true;
        }
        return false;
    }
}
