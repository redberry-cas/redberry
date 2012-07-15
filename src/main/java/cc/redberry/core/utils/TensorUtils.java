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

//import cc.redberry.core.indices.InconsistentIndicesException;
import cc.redberry.core.context.CC;
import cc.redberry.core.indexmapping.*;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;

import java.util.HashSet;
import java.util.Set;

//import cc.redberry.core.indices.Indices;
//import cc.redberry.core.indices.IndicesBuilderSorted;
//import cc.redberry.core.math.MathUtils;
//import cc.redberry.core.tensor.*;
//import cc.redberry.core.tensor.iterators.IterationGuide;
//import cc.redberry.core.tensor.iterators.TensorFirstTreeIterator;
//import cc.redberry.core.tensor.iterators.TensorLastTreeIterator;
//import cc.redberry.core.tensor.iterators.TensorTreeIterator;
//import cc.redberry.core.tensor.testing.TTest;
//
//import java.util.*;
//
//import static cc.redberry.core.indices.IndicesUtils.getNameWithType;
//import org.apache.commons.math.fraction.Fraction;
//import org.apache.commons.math.stat.inference.TTest;
//import org.apache.commons.math.util.MathUtils;
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
        return tensor.getIndices().getFreeIndices().size() == 0;
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

    public static boolean equals(Tensor[] u, Tensor[] v) {
        if (u.length != v.length)
            return false;
        for (int i = 0; i < u.length; ++i)
            if (!TensorUtils.equals(u[i], v[i]))
                return false;
        return true;
    }

    public static boolean equals(Tensor u, String v) {
        return equals(u, Tensors.parse(v));
    }

    public static boolean equals(Tensor u, Tensor v) {
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
                                if (usedPos[j - begin] == false && equals(u.get(n), v.get(j))) {
                                    usedPos[j - begin] = true;
                                    continue OUT;
                                }
                            return false;
                        }
                        return true;
                    } else if (!equals(u.get(i - 1), v.get(i - 1)))
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
            if (!equals(u.get(i), v.get(i)))
                return false;
        return true;
    }

    public static Set<Integer> getAllIndices(Tensor tensor) {
        Set<Integer> indices = new HashSet<>();
        appendAllIndices(tensor, indices);
        return indices;
    }

    private static void appendAllIndices(Tensor tensor, Set<Integer> indices) {
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
                if (t instanceof AbstractScalarFunction)
                    continue;
                appendAllIndices(tensor.get(i), indices);
            }
        }
    }

    public static boolean compare(Tensor u, Tensor v) {
        Indices freeIndices = u.getIndices().getFreeIndices();
        if (!freeIndices.equalsRegardlessOrder(v.getIndices().getFreeIndices()))
            return false;
        int[] free = freeIndices.getAllIndices().copy();
        IndexMappingBuffer tester = new IndexMappingBufferTester(free, false, CC.withMetric());
        MappingsPort mp = IndexMappings.createPort(tester, u, v);
        IndexMappingBuffer buffer;

        while ((buffer = mp.take()) != null)
            if (buffer.getSignum() == false)
                return true;

        return false;
    }

    public static Boolean compare1(Tensor u, Tensor v) {
        Indices freeIndices = u.getIndices().getFreeIndices();
        if (!freeIndices.equalsRegardlessOrder(v.getIndices().getFreeIndices()))
            return false;
        int[] free = freeIndices.getAllIndices().copy();
        IndexMappingBuffer tester = new IndexMappingBufferTester(free, false, CC.withMetric());
        IndexMappingBuffer buffer = IndexMappings.createPort(tester, u, v).take();
        if (buffer == null)
            return null;
        return buffer.getSignum();
    }
//
//    public static IndicesBuilderSorted getAllIndicesBuilder(final Tensor tensor) {
//        final IndicesBuilderSorted ib = new IndicesBuilderSorted();
//        TensorLastTreeIterator iterator = new TensorLastTreeIterator(tensor, IterationGuide.EXCEPT_DENOMINATOR_TENSORFIELD_ARGUMENTS);
//        Tensor current;
//        while (iterator.hasNext()) {
//            current = iterator.next();
//            if (!(current instanceof SimpleTensor))
//                continue;
//            if (Derivative.onVarsIndicator.is(iterator))
//                ib.append(current.getIndices().getInverseIndices());
//            else
//                ib.append(current.getIndices());
//        }
//        return ib;
//    }
//
//    public static Indices getAllIndices(final Tensor... tensors) {
//        return getAllIndicesBuilder(tensors).getDistinct();
//    }
//
//    public static IndicesBuilderSorted getAllIndicesBuilder(final Tensor... tensors) {
//        final IndicesBuilderSorted ib = new IndicesBuilderSorted();
//        for (Tensor t : tensors)
//            ib.append(getAllIndicesBuilder(t));
//        return ib;
//    }
//
//    /**
//     * Return sorted int array of distinct indices names
//     *
//     * @param t tensor
//     */
//    public static int[] getAllIndicesNames(final Tensor t) {
//        int[] indices = getAllIndices(t).getAllIndices().copy();
//        for (int i = 0; i < indices.length; ++i)
//            indices[i] = getNameWithType(indices[i]);
//        return MathUtils.getSortedDistinct(indices);
//    }
//
//    /**
//     * Utility method. Only in develop version.
//     *
//     * @param t
//     * @return false if specified indices are inconsistent and true if not
//     */
//    //TODO consider different implementation
//    public static boolean testIndicesConsistent(final Tensor t) {
//        try {
//            TensorFirstTreeIterator it = new TensorFirstTreeIterator(t);
//            while (it.hasNext())
//                it.next().getIndices().testConsistentWithException();
//        } catch (InconsistentIndicesException e) {
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * Returns list of contracted indices of two tensors, i.e. similar of free
//     * indices of first and second tensors. E.g. for tensors
//     * {@code A_mn} and {@code B^am}, list will contains only index {@code m}.
//     *
//     * @param first first tensor
//     * @param second second tensor
//     * @return list of contracted indices of two tensors, i.e. similar of free
//     * indices of first and second tensors
//     */
//    public static IntArrayList getContractedIndicesNames(final Tensor first, final Tensor second) {
//        //FIXME write better algotithm
//        Indices firstIndices = first.getIndices().getFreeIndices();
//        int[] secondIndices = second.getIndices().getFreeIndices().getAllIndices().copy();
//        Arrays.sort(secondIndices);
//        IntArrayList result = new IntArrayList();
//        for (int i = 0; i < firstIndices.size(); ++i)
//            if (Arrays.binarySearch(secondIndices, 0x80000000 ^ firstIndices.get(i)) >= 0)
//                result.add(getNameWithType(firstIndices.get(i)));
//        return result;
//    }
//
//    /**
//     * Returns length of the specified tensor. Specification : <br> If tensor
//     * instance of sum or product this method returns their size. <br> If tensor
//     * instance of Fraction this method returns 2. <br> If tensor instance of
//     * TensorField this method returns arguments number + 1. <br> If tensor
//     * instance of Derivative this method returns variations number + 1. <br>
//     * Else returns 1.
//     *
//     * @param t specified tensor
//     * @return length as in description explained
//     */
//    public static int size(final Tensor t) {
//        if (t instanceof MultiTensor)
//            return ((MultiTensor) t).size();
//        if (t instanceof Fraction)
//            return 2;
//        if (t instanceof TensorField)
//            return ((TensorField) t).getArgs().length + 1;
//        if (t instanceof Derivative)
//            return ((Derivative) t).getDerivativeOrder() + 1;
//        return 1;
//    }
//
//
//
//    /**
//     * Detects whether target tensor contains in its tree one of the simple
//     * tensors in the keys array, with no respect to their indices.
//     *
//     * @param target target tensor to find whether it contains one of the keys
//     * @param keys simple tensors array
//     * @return true if target tensor contains one of the keys and false if not
//     */
//    public static boolean contains(final Tensor target, final SimpleTensor... keys) {
//        final TensorTreeIterator iterator = new TensorLastTreeIterator(target);
//        Tensor c;
//        SimpleTensor s;
//        while (iterator.hasNext()) {
//            c = iterator.next();
//            if (!(c instanceof SimpleTensor))
//                continue;
//            s = (SimpleTensor) c;
//            for (SimpleTensor k : keys)
//                if (k.getName() == s.getName())
//                    return true;
//        }
//        return false;
//    }
//
//    /**
//     * Returns list of simple tensors, which are occurs in target tensor tree.
//     *
//     * @param target target tensor
//     * @return list of simple tensors, which are occurs in target tensor
//     */
//    public static Collection<SimpleTensor> getSimpleTensorContent(Tensor target) {
//        final List<SimpleTensor> result = new LinkedList<>();
//        final TensorTreeIterator iterator = new TensorLastTreeIterator(target);
//        Tensor c;
//        while (iterator.hasNext()) {
//            c = iterator.next();
//            if (c instanceof SimpleTensor)
//                result.add((SimpleTensor) c);
//        }
//        return result;
//    }
//
//    /**
//     * Returns list of simple tensors, which are occurs in target tensor tree
//     * and have different names.
//     *
//     * @param target target tensor
//     * @return list of simple tensors, which are occurs in target tensor and
//     * have different names.
//     */
//    public static Collection<SimpleTensor> getDiffSimpleTensorContent(Tensor target) {
//        final Map<Integer, SimpleTensor> map = new HashMap<>();
//        final TensorTreeIterator iterator = new TensorLastTreeIterator(target);
//        Tensor c;
//        while (iterator.hasNext()) {
//            c = iterator.next();
//            if (c instanceof SimpleTensor) {
//                SimpleTensor st = (SimpleTensor) c;
//                if (map.containsKey(st.getName()))
//                    continue;
//                map.put(st.getName(), st);
//            }
//        }
//        return map.values();
//    }
//
//    /**
//     * //TODO comment
//     */
//    public static Tensor[] getDistinct(final Tensor[] array) {
//        final int length = array.length;
//        final Indices indices = array[0].getIndices().getFreeIndices();
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
//                if (TTest.compare(array[i], array[j]))
//                    continue OUTER;
//            tensors.add(array[i]);
//        }
//    }
}
