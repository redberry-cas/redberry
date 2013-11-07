/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
package cc.redberry.core.groups.permutations;

import java.math.BigInteger;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public interface PermutationGroup
        extends Iterable<Permutation> {

    /**
     * Returns whether the specified permutation is member of this group
     *
     * @param permutation permutation
     * @return true if specified permutation is member of this group
     */
    boolean isMember(Permutation permutation);

    /**
     * Returns the number of permutations in this group
     *
     * @return number of permutations in this group
     */
    BigInteger order();

    /**
     * Returns an unmodifiable list of group generators.
     *
     * @return unmodifiable list of group generators
     */
    List<Permutation> generators();

    /**
     * Return the dimension of group (length of each permutation)
     *
     * @return dimension of group (length of each permutation)
     */
    //todo rename
    int dimension();

    /**
     * Returns the orbit of specified point
     *
     * @param point point
     * @return orbit of specified point
     */
    int[] orbit(int point);

    /**
     * Returns the orbit of specified set of points
     *
     * @param point set of points
     * @return orbit of specified point
     */
    int[] orbit(int[] point);

    /**
     * Returns a set of all orbits
     *
     * @return set of all orbits
     */
    int[][] orbits();

    /**
     * Returns true if this group is transitive.
     *
     * @return true if this group is transitive
     */
    boolean isTransitive();

    /**
     * Returns a setwise stabilizer of specified set of points.
     *
     * @param set set of points
     * @return setwise stabilizer of specified set of points.
     */
    PermutationGroup setwiseStabilizer(int[] set);

    /**
     * Returns a pointwise stabilizer of specified set of points.
     *
     * @param set set of points
     * @return pointwise stabilizer of specified set of points.
     */
    PermutationGroup pointwiseStabilizer(int[] set);

    /**
     * Returns a subgroup that maps two specified sets of points into each other.
     *
     * @param a set of points
     * @param b set of points
     * @return subgroup that maps two specified sets of points into each other.
     */
    PermutationGroup setwiseMapping(int[] a, int[] b);

    /**
     * Returns a subgroup that maps two specified sets of points into each other such that <i>i</i>-th point of
     * {@code a} maps onto <i>i</i>-th point of {@code b}.
     *
     * @param a set of points
     * @param b set of points
     * @return subgroup that maps pointwise two specified sets of points into each other.
     */
    PermutationGroup pointwiseMapping(int[] a, int[] b);

    /**
     * Returns true if specified group is a subgroup of this group
     *
     * @param group permutation group
     * @return true if specified group is a subgroup of this group
     */
    boolean isSubgroup(PermutationGroup group);


    /**
     * Returns a set of right coset representatives of a given subgroup in this group. The number of such
     * representatives is {@code this.order() / subgroup.order() } (according to Lagrange theorem).
     *
     * @param subgroup a subgroup of this group
     * @return set of right coset representatives
     * @throws IllegalArgumentException if specified {@code subgroup} is not a subgroup of this
     */
    Permutation[] rightCosetRepresentatives(PermutationGroup subgroup);
}
