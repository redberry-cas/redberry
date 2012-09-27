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
package cc.redberry.core.utils;

import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.combinatorics.symmetries.SymmetriesFactory;
import cc.redberry.core.indexmapping.*;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.ScalarFunction;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorUtils {

    private TensorUtils() {
    }

    public static boolean isInteger(Tensor tensor) {
        if (!(tensor instanceof Complex))
            return false;
        return ((Complex) tensor).isInteger();
    }

    public static boolean isNatural(Tensor tensor) {
        if (!(tensor instanceof Complex))
            return false;
        return ((Complex) tensor).isNatural();
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

    public static boolean isOne(Tensor tensor) {
        return tensor instanceof Complex && ((Complex) tensor).isOne();
    }

    public static boolean isZero(Tensor tensor) {
        return tensor instanceof Complex && ((Complex) tensor).isZero();
    }

    public static boolean isImageOne(Tensor tensor) {
        return tensor instanceof Complex && ((Complex) tensor).equals(Complex.IMAGEONE);
    }

    public static boolean isMinusOne(Tensor tensor) {
        return tensor instanceof Complex && ((Complex) tensor).equals(Complex.MINUSE_ONE);
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
            if (!u.getIndices().equals(v.getIndices()))
                return false;
            else
                return true;
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
                                if (usedPos[j - begin] == false && equalsExactly(u.get(n), v.get(j))) {
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
                    || !u.getIndices().equals(v.getIndices())) ;
            return false;
        }

        final int size = u.size();
        for (int i = 0; i < size; ++i)
            if (!equalsExactly(u.get(i), v.get(i)))
                return false;
        return true;
    }

    @Deprecated
    public static Set<Integer> getAllDummyIndicesNames(Tensor tensor) {
        Set<Integer> dummy = getAllIndicesNames(tensor);
        Indices ind = tensor.getIndices().getFree();
        for (int i = ind.size() - 1; i >= 0; --i)
            dummy.remove(IndicesUtils.getNameWithType(ind.get(i)));
        return dummy;
    }

    public static Set<Integer> getAllIndicesNames(Tensor... tensors) {
        Set<Integer> indices = new HashSet<>();
        for (Tensor tensor : tensors)
            appendAllIndicesNames(tensor, indices);
        return indices;
    }

    private static void appendAllIndicesNames(Tensor tensor, Set<Integer> indices) {
        if (tensor instanceof SimpleTensor) {
            Indices ind = tensor.getIndices();
            final int size = ind.size();
            for (int i = 0; i < size; ++i)
                indices.add(IndicesUtils.getNameWithType(ind.get(i)));
        } else {
            final int size = tensor.size();
            Tensor t;
            for (int i = 0; i < size; ++i) {
                t = tensor.get(i);
                if (t instanceof ScalarFunction)
                    continue;
                appendAllIndicesNames(tensor.get(i), indices);
            }
        }
    }

    public static TIntHashSet getAllDummyIndicesT(Tensor tensor) {
        TIntHashSet indices = getAllIndicesNamesT(tensor);
        indices.removeAll(IndicesUtils.getIndicesNames(tensor.getIndices().getFree()));
        return indices;
    }

    public static TIntHashSet getAllIndicesNamesT(Tensor... tensors) {
        TIntHashSet set = new TIntHashSet();
        for (Tensor tensor : tensors)
            appendAllIndicesNamesT(tensor, set);
        return set;
    }

    private static void appendAllIndicesNamesT(Tensor tensor, TIntHashSet set) {
        if (tensor instanceof SimpleTensor) {
            Indices ind = tensor.getIndices();
            final int size = ind.size();
            for (int i = 0; i < size; ++i)
                set.add(IndicesUtils.getNameWithType(ind.get(i)));
        } else {
            final int size = tensor.size();
            Tensor t;
            for (int i = 0; i < size; ++i) {
                t = tensor.get(i);
                if (t instanceof ScalarFunction)
                    continue;
                appendAllIndicesNamesT(tensor.get(i), set);
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
            if (buffer.getSignum() == false)
                return true;

        return false;
    }

    public static Boolean compare1(Tensor u, Tensor v) {
        Indices freeIndices = u.getIndices().getFree();
        if (!freeIndices.equalsRegardlessOrder(v.getIndices().getFree()))
            return false;
        int[] free = freeIndices.getAllIndices().copy();
        IndexMappingBuffer tester = new IndexMappingBufferTester(free, false);
        IndexMappingBuffer buffer = IndexMappings.createPort(tester, u, v).take();
        if (buffer == null)
            return null;
        return buffer.getSignum();
    }

    /**
     * @param t s AssertionError
     */
    public static void assertIndicesConsistency(Tensor t) {
        assertIndicesConsistency(t, new HashSet<Integer>());
    }

    private static void assertIndicesConsistency(Tensor t, Set<Integer> indices) {
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
            Set<Integer> sumIndices = new HashSet<>(), temp;
            for (int i = t.size() - 1; i >= 0; --i) {
                temp = new HashSet<>(indices);
                assertIndicesConsistency(t.get(i), temp);
                appendAllIndices(t.get(i), sumIndices);
            }
            indices.addAll(sumIndices);
        }
        if (t instanceof Expression)//FUTURE incorporate expression correctly
            for (Tensor c : t)
                assertIndicesConsistency(c, new HashSet<>(indices));
    }

    private static void appendAllIndices(Tensor t, Set<Integer> set) {
        if (t instanceof SimpleTensor) {
            Indices ind = t.getIndices();
            for (int i = ind.size() - 1; i >= 0; --i)
                set.add(ind.get(i));
        } else
            for (Tensor c : t)
                if (c instanceof ScalarFunction)
                    continue;
                else
                    appendAllIndices(c, set);
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
            if (record == null)
                throw new IllegalArgumentException("Index " + IndicesUtils.toString(fromIndex) + " does not contains in specified IndexMappingBuffer.");
            int newPosition = -1;
            //TODO refactor with sort and binary search
            for (int j = 0; j < dimension; ++j)
                if (indicesNames[j] == record.getIndexName()) {
                    newPosition = j;
                    break;
                }
            if (newPosition < 0)
                throw new IllegalArgumentException("Index " + IndicesUtils.toString(record.getIndexName()) + " does not contains in specified indices array.");
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

    public static Symmetries getIndicesSymmetries(int[] indices, Tensor tensor) {
        return getSymmetriesFromMappings(indices, IndexMappings.createPort(tensor, tensor));
    }

    public static Symmetries getIndicesSymmetriesForIndicesWithSameStates(final int[] indices, Tensor tensor) {
        Symmetries total = getIndicesSymmetries(indices, tensor);
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

//    public static Tensor[] getDistinct(final Tensor[] array) {
//        final int length = array.length;
//        final Indices indices = array[0].getIndices().getFree();
//        final int[] hashes = new int[length];
//        int i;
//        for (i = 0; i < length; ++i)
//            hashes[i] = TensorHashCalculator.hashWithIndices(array[i], indices);
//        ArraysUtils.quickSort(hashes, array);
//
//        //Searching for stretches in from hashes
//        final List<Tensor> tensors = new ArrayList<>();
//        int begin = 0;
//        for (i = 1; i <= length; ++i)
//            if (i == length || hashes[i] != hashes[i - 1]) {
//                if (i - 1 != begin)
//                    _addDistinctToList(array, begin, i, tensors);
//                else
//                    tensors.add(array[begin]);
//                begin = i;
//            }
//        return tensors.toArray(new Tensor[tensors.size()]);
//    }
//
//    private static void _addDistinctToList(final Tensor[] array, final int from, final int to, final List<Tensor> tensors) {
//        int j;
//        OUTER:
//        for (int i = from; i < to; ++i) {
//            for (j = i + 1; j < to; ++j)
//                if (TTest.equals(array[i], array[j]))
//                    continue OUTER;
//            tensors.add(array[i]);
//        }
//    }
}
