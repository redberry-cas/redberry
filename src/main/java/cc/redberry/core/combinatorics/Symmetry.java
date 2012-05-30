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
package cc.redberry.core.combinatorics;

import cc.redberry.core.indices.Indices;
import cc.redberry.core.tensor.Tensor;
import java.util.Arrays;

/**
 * This class represents symmetry property, and mainly using for represent
 * symmetries of tensor indices . In project specification symmetry is a simple
 * permutation and a sign - result of it action on {@code Indices}. Permutation,
 * acting on tensor indices, can change sign of tensor, i.e. {@code A_{mn} =
 * -A_{nm}}, and can not - {@code g_{mn} = g_{nm}}. For more information see
 * methods summary.
 *
 * @see Permutation
 * @see Indices
 * @see Tensor
 * @see Symmetries
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class Symmetry extends Permutation {

    private final boolean signum;

    /**
     * Constructs symmetry, specified by {@code permutation} integer array in
     * <i>one-line</i> notation and {@code signum} - property of permutation. If {@code signum == false},
     * it means that this permutation does not change object, when acting on it.
     * If {@code signum == true}, it means that permutation changes sign of
     * object, when acting on it, i.e. this is antisymmetry.
     *
     * @param permutation <i>one-line</i> notated integer array, representing a
     *                    permutation.
     * @param signum      property of permutation. {@code false} means that
     * permutation does not change object, when acting on it and {@code true}
     * means that permutation changes sign of object, when acting on it, i.e.
     * this is antisymmetry.
     *
     * @throws IllegalArgumentException if array is inconsistent with
     *                                  <i>one-line</i> notation
     */
    public Symmetry(int[] permutation, boolean signum) {
        super(permutation);
        this.signum = signum;
    }

    /**
     * Constructs identity symmetry with specified dimension, i.e. identity
     * permutation, witch does not change sign.
     *
     * @param dimension dimension of permutation
     */
    public Symmetry(int dimension) {
        super(dimension);
        this.signum = false;
    }

    protected Symmetry(int[] permutation, boolean signum, boolean b) {
        super(permutation, b);
        this.signum = signum;
    }

    /**
     * Returns identity symmetry, i.e. identity permutation, witch does not
     * change sign.
     *
     * @return identity symmetry
     */
    @Override
    public Symmetry getOne() {
        return new Symmetry(permutation.length);
    }

    /**
     * Returns true if this is antisymmetry and false if not.
     *
     * @return true if this is antisymmetry and false if not
     */
    public boolean isAntiSymmetry() {
        return signum;
    }

    /**
     * Returns a composition of this symmetry (A) and specified (B), i.e. A*B.
     * The result symmetry represents the compositions of combinatorics and
     * multiplying {@code signums} (the result {@code signum} will be
     * {@code this.signum ^ element.signum}).
     *
     * @param element is a right multiplicand symmetry
     *
     * @return composition of element and this symmetry
     *
     * @throws IllegalArgumentException if element has different dimension than
     *                                  this one
     */
    @Override
    public Symmetry composition(Permutation element) {
        Symmetry s = (Symmetry) element;
        return new Symmetry(compositionArray(element), signum ^ s.signum, true);
    }

    /**
     * Returns symmetry representing inverse permutation and similar
     * {@code signum}.
     *
     * @return symmetry representing inverse permutation and similar
     * {@code signum}
     */
    @Override
    public Symmetry inverse() {
        return new Symmetry(calculateInverse(), signum);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Symmetry other = (Symmetry) obj;
        if (this.signum != other.signum)
            return false;
        return Arrays.equals(permutation, other.permutation);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + (this.signum ? 1 : 0);
    }

    /**
     * Returns the string representation of this symmetry in form
     * {@code permutation : signum}.
     *
     * @see Permutation#toString()
     * @return the string representation of this symmetry
     */
    @Override
    public String toString() {
        return super.toString() + "(" + (signum ? "-" : "+") + ")";
    }
}
