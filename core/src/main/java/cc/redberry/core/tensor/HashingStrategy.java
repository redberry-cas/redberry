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
package cc.redberry.core.tensor;

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;

import java.util.Arrays;

import static cc.redberry.core.indices.IndicesUtils.getNameWithType;
import static cc.redberry.core.utils.HashFunctions.JenkinWang32shift;

/**
 * Special hash algorithms for tensors.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class HashingStrategy {
    private HashingStrategy() {
    }

    public static int iHash(final Tensor tensor) {
        if (tensor instanceof Product)
            return ((Product) tensor).iHashCode();
        final Indices freeIndices = tensor.getIndices().getFree();
        final int[] sortedNames = IndicesUtils.getIndicesNames(freeIndices);
        Arrays.sort(sortedNames);
        return iHash(tensor, sortedNames);
    }

    public static int iHash(final Tensor tensor, final Indices indices) {
        final int[] sortedNames = IndicesUtils.getIndicesNames(indices.getFree());
        if (indices instanceof SimpleIndices || indices.getUpper().size() != 0)
            Arrays.sort(sortedNames);
        return iHash(tensor, sortedNames);
    }

    public static int iGraphHash(final SimpleTensor tensor) {
        final int[] sortedNames = IndicesUtils.getIndicesNames(tensor.getIndices().getFree());
        Arrays.sort(sortedNames);
        return iGraphHash(tensor, sortedNames);
    }

    public static int iGraphHashWithoutIndices(final SimpleTensor tensor) {
        return tensor.hashCode() + tensor.getIndices().contractionsHash();
    }

    public static int iGraphHash(final SimpleTensor tensor, final int[] sortedNames) {
        int hash = iGraphHashWithoutIndices(tensor);
        if (sortedNames.length == 0)
            return hash;
        SimpleIndices si = tensor.getIndices();
        short[] orbits = si.getPositionsInOrbits();
        int pos;
        for (int i = 0; i < si.size(); ++i)
            if ((pos = Arrays.binarySearch(sortedNames, getNameWithType(si.get(i)))) >= 0)
                hash += (JenkinWang32shift(orbits[i] + 1) ^ (JenkinWang32shift(pos + 1) * 37));
        if (tensor instanceof TensorField)
            for (Tensor t : tensor) //<- args order is important!
                hash += hash * 7 + 13 * t.hashCode();
        return hash;
    }

    public static int iHash(final Tensor tensor, final int[] sortedNames) {
        if (sortedNames.length == 0)
            return tensor.hashCode();

        final Indices freeIndices = tensor.getIndices().getFree();
        if (freeIndices.size() == 0)
            return tensor.hashCode();

        if (tensor instanceof SimpleTensor) {
            SimpleIndices si = ((SimpleTensor) tensor).getIndices();
            int hash = tensor.hashCode();
            short[] orbits = si.getPositionsInOrbits();
            int pos;
            for (int i = 0; i < si.size(); ++i)
                if ((pos = Arrays.binarySearch(sortedNames, getNameWithType(si.get(i)))) >= 0)
                    hash += (JenkinWang32shift(orbits[i] + 1) ^ (JenkinWang32shift(pos + 1) * 37));
            if (tensor instanceof TensorField)
                for (Tensor t : tensor) //<- args order is important!
                    hash += hash * 7 + t.hashCode();
            return hash;
        }

        final int[] sortedFree = sortedFree(freeIndices);
        if (!shareIndices(sortedFree, sortedNames))
            return tensor.hashCode();

        if (tensor instanceof Product) {
            Product product = ((Product) tensor);
            ProductContent pc = product.getContent();
            if (pc.size() == 1)
                return product.factor.isOneOrMinusOne() && product.indexlessData.length == 0
                        ? iHash(pc.get(0), sortedNames)
                        : tensor.hashCode() + 17 * iHash(pc.get(0), sortedNames);

            if (Arrays.equals(sortedFree, sortedNames))
                return product.iHashCode();

            final int[] iHashCodes = new int[pc.size()];
            for (int i = 0; i < iHashCodes.length; ++i)
                iHashCodes[i] = iHash(pc.get(i), sortedNames);
            Product.iRefineIHashCodesOnly(pc.hashCodes, iHashCodes, pc.getStructureOfContractions().contractions, pc.data);
            int hash = 0;
            for (int iHashCode : iHashCodes)
                hash += iHashCode;
            return hash;
        }

        int hash = 0;
        for (Tensor t : tensor)
            hash += iHash(t, sortedNames);
        return hash;
    }

    private static int[] sortedFree(final Indices free) {
        assert !(free instanceof SimpleIndices);
        final int[] names = IndicesUtils.getIndicesNames(free);
        Arrays.sort(names);
        return names;
    }

    private static boolean shareIndices(final int[] free, final int[] sortedNames) {
        if (free.length == 0 || sortedNames.length == 0)
            return false;
        int fPointer = 0, sPointer = 0;
        for (; fPointer < free.length && sPointer < sortedNames.length; ) {
            assert fPointer == 0 || free[fPointer] > free[fPointer - 1];
            assert sPointer == 0 || sortedNames[sPointer] > sortedNames[sPointer - 1];
            if (free[fPointer] < sortedNames[sPointer])
                ++fPointer;
            else if (free[fPointer] > sortedNames[sPointer])
                ++sPointer;
            else
                return true;
        }
        return false;
    }
//
//    private static int _hashWithIndices(final Tensor tensor, final int[] indices) {
//        if (tensor instanceof SimpleTensor) {
//            SimpleIndices si = ((SimpleTensor) tensor).getIndices();
//            short[] sInds = si.getPositionsInOrbits();
//            int hash = tensor.hashCode();
//            int pos;
//            for (int i = 0; i < si.size(); ++i)
//                if ((pos = Arrays.binarySearch(indices, si.get(i))) >= 0)
//                    hash += (HashFunctions.JenkinWang32shift(sInds[i])
//                            ^ (HashFunctions.JenkinWang32shift(pos) * 37));
//            if (tensor instanceof TensorField)
//                for (int i = 0; i < tensor.size(); i++)
//                    hash += hash * 7 + tensor.get(i).hashCode();
//            return HashFunctions.JenkinWang32shift(hash);
//        }
//        if (tensor instanceof ScalarFunction)
//            return tensor.hashCode();
//
//        int hash = tensor.hashCode();
//        if (tensor instanceof Product) {
//            Product product = (Product) tensor;
//            ProductContent pc = product.getContent();
//            if (pc.size() == 1) {
//                int dataHash = _hashWithIndices(pc.get(0), indices);
//                return product.getFactor().isOneOrMinusOne()
//                        ? dataHash : dataHash * product.getFactor().hashCode();
//            }
//
//            for (int i = pc.size() - 1; i >= 0; --i)
//                hash += hash * 17 + HashFunctions.JenkinWang32shift(pc.getVertexHash(i) * _hashWithIndices(pc.get(i), indices));
//            return hash;
//        }
//
//        for (Tensor t : tensor)
//            hash ^= _hashWithIndices(t, indices);
//        return hash;
//    }
//
//    public static int hashWithIndices(final Tensor tensor, final int[] indices) {
//        if (indices.length == 0)
//            return tensor.hashCode();
//        Arrays.sort(indices);
//        return _hashWithIndices(tensor, indices);
//    }
//
//    public static int hashWithIndices(final Tensor tensor, final Indices indices) {
//        return hashWithIndices(tensor, indices.getAllIndices().copy());
//    }
//
//    public static int hashWithIndices(final Tensor tensor) {
//        return hashWithIndices(tensor, tensor.getIndices().getFree());
//    }
}
