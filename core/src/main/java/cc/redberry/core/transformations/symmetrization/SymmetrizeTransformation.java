/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
package cc.redberry.core.transformations.symmetrization;

import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.groups.permutations.PermutationGroup;
import cc.redberry.core.groups.permutations.PermutationOneLine;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.number.Rational;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.ArrayIterator;
import cc.redberry.core.utils.TensorUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;

import static cc.redberry.core.indices.IndicesUtils.getNameWithType;

/**
 * Gives a symmetrization of tensor with respect to specified indices under the specified symmetries.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1.6
 */
public final class SymmetrizeTransformation implements Transformation {
    private final SimpleIndices indices;
    private final int[] indicesArray;
    private final int[] sortedIndicesNames;
    private final boolean multiplyBySymmetryFactor;
    private final PermutationGroup indicesGroup;

    public SymmetrizeTransformation(SimpleIndices indices, boolean multiplyBySymmetryFactor) {
        this.indices = indices;
        this.indicesArray = indices.toArray();
        this.sortedIndicesNames = IndicesUtils.getIndicesNames(indices);
        Arrays.sort(this.sortedIndicesNames);
        this.indicesGroup = indices.getSymmetries().getPermutationGroup();
        this.multiplyBySymmetryFactor = multiplyBySymmetryFactor;
    }

    private static final BigInteger SMALL_ORDER_MAX_VALUE = BigInteger.valueOf(1_000);

    @Override
    public Tensor transform(Tensor t) {
        if (t.getIndices().size() == 0)
            return t;

        if (!containsSubIndices(t.getIndices(), indices))
            throw new IllegalArgumentException("Indices of specified tensor do not contain " +
                    "indices that should be symmetrized.");

        Iterator<Permutation> cosetRepresentatives;
        BigInteger factor;
        //for a simple tensors we can compute coset representatives directly:
        if (t instanceof SimpleTensor) {
            PermutationGroup t_group =
                    conjugatedSymmetriesOfSubIndices(((SimpleTensor) t).getIndices());
            PermutationGroup union = t_group.union(indicesGroup);
            Permutation[] reps = union.leftCosetRepresentatives(t_group);
            cosetRepresentatives = new ArrayIterator<>(reps);
            factor = BigInteger.valueOf(reps.length);
        } else {
            //in case of multitensor, we do not know its group of symmetries
            //if the resulting symmetries are small, then we'll just apply all of them
            if (indicesGroup.order().compareTo(SMALL_ORDER_MAX_VALUE) < 0) {
                cosetRepresentatives = indicesGroup.iterator();
                factor = indicesGroup.order();
            } else {
                //otherwise we might will be more lucky if compute it group of symmetries and then compute coset reps.
                PermutationGroup t_group = new PermutationGroup(
                        TensorUtils.findIndicesSymmetries(indices, t));
                PermutationGroup union = t_group.union(indicesGroup);
                Permutation[] reps = union.leftCosetRepresentatives(t_group);
                cosetRepresentatives = new ArrayIterator<>(reps);
                factor = BigInteger.valueOf(reps.length);
            }
        }

        SumBuilder sb = new SumBuilder();
        for (Permutation permutation; cosetRepresentatives.hasNext(); ) {
            permutation = cosetRepresentatives.next();
            sb.put(ApplyIndexMapping.applyIndexMappingAutomatically(t,
                    new Mapping(indicesArray, permutation.permute(indicesArray), permutation.antisymmetry())));
        }

        t = sb.build();

        if (multiplyBySymmetryFactor) {
            Complex frac = new Complex(new Rational(BigInteger.ONE, factor));
            if (t instanceof Sum)
                return FastTensors.multiplySumElementsOnFactor((Sum) t, frac);
            return Tensors.multiply(frac, t);
        } else
            return sb.build();
    }

    private static boolean containsSubIndices(Indices indices, Indices subIndices) {
        int[] indicesArray = IndicesUtils.getIndicesNames(indices);
        Arrays.sort(indicesArray);
        for (int i = 0, size = subIndices.size(); i < size; ++i)
            if (Arrays.binarySearch(indicesArray, getNameWithType(subIndices.get(i))) < 0)
                return false;
        return true;
    }

    private PermutationGroup conjugatedSymmetriesOfSubIndices(SimpleIndices allIndices) {
        //positions of indices in allIndices that should be stabilized
        int[] stabilizedPoints = new int[allIndices.size() - indices.size()];
        int[] nonStabilizedPoints = new int[indices.size()];
        int[] mapping = new int[indices.size()];
        int sPointer = 0, nPointer = 0, index;
        for (int s = 0; s < allIndices.size(); ++s) {
            index = Arrays.binarySearch(sortedIndicesNames, getNameWithType(allIndices.get(s)));
            if (index < 0)
                stabilizedPoints[sPointer++] = s;
            else {
                nonStabilizedPoints[nPointer] = s;
                mapping[nPointer++] = index;
            }
        }
        PermutationGroup result = allIndices.getSymmetries().getPermutationGroup().
                pointwiseStabilizerRestricted(stabilizedPoints);
        return result.conjugate(new PermutationOneLine(mapping));
    }
}
