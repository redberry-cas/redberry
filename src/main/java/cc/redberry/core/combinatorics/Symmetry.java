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


import java.util.Arrays;

/**
 * This class represents a permutational symmetry, i.e. a permutation and its sign (+1 or -1).
 * The sign of a composition of two symmetries is a product of their signs.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Permutation
 * @see cc.redberry.core.indices.Indices
 * @see cc.redberry.core.indices.IndicesSymmetries
 * @see cc.redberry.core.combinatorics.symmetries.Symmetries
 * @since 1.0
 */
public class Symmetry extends Permutation {

    private final boolean sign;

    /**
     * Constructs symmetry, specified by {@code permutation} integer array in
     * <i>single-line</i> notation and boolean sign ({@code true} corresponds to -1
     * and {@code false} to +1).
     *
     * @param permutation <i>single-line</i> notated integer array, representing
     *                    a permutation.
     * @param sign        {@code true} corresponds to -1 and {@code false} to +1
     * @throws IllegalArgumentException if array is inconsistent with
     *                                  <i>one-line</i> notation
     */
    public Symmetry(int[] permutation, boolean sign) {
        super(permutation);
        this.sign = sign;
    }

    /**
     * Constructs identity symmetry with specified dimension (sign = +)
     *
     * @param dimension dimension of permutation
     */
    public Symmetry(int dimension) {
        super(dimension);
        this.sign = false;
    }

    protected Symmetry(int[] permutation, boolean sign, boolean notClone) {
        super(permutation, notClone);
        this.sign = sign;
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
     * Returns true if this is antisymmetry (sign == -) and false if not.
     *
     * @return true if this is antisymmetry (sign == -) and false if not
     */
    public boolean isAntiSymmetry() {
        return sign;
    }

    /**
     * Returns a composition of this symmetry (A) and specified (B), i.e. A*B.
     * The result symmetry represents the compositions of combinatorics and
     * multiplying {@code signums} (the result {@code sign} will be
     * {@code this.sign ^ element.sign}).
     *
     * @param element is a right multiplicand symmetry
     * @return composition of element and this symmetry
     * @throws IllegalArgumentException if element has different dimension than
     *                                  this one
     */
    @Override
    public Symmetry composition(Permutation element) {
        Symmetry s = (Symmetry) element;
        return new Symmetry(compositionArray(element), sign ^ s.sign, true);
    }

    /**
     * Returns symmetry representing inverse permutation and with same
     * {@code sign} as this.
     *
     * @return symmetry representing inverse permutation and with same
     *         {@code sign} as this
     */
    @Override
    public Symmetry inverse() {
        return new Symmetry(calculateInverse(), sign);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Symmetry other = (Symmetry) obj;
        if (this.sign != other.sign)
            return false;
        return Arrays.equals(permutation, other.permutation);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + (this.sign ? 1 : 0);
    }

    /**
     * Returns the string representation of this symmetry in form
     * {@code permutation : sign}.
     *
     * @return the string representation of this symmetry
     * @see Permutation#toString()
     */
    @Override
    public String toString() {
        return super.toString() + "(" + (sign ? "-" : "+") + ")";
    }
}
