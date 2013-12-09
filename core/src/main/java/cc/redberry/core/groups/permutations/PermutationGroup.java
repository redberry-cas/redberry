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

import cc.redberry.core.combinatorics.IntTuplesPort;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.MathUtils;
import gnu.trove.set.hash.TIntHashSet;

import java.math.BigInteger;
import java.util.*;

import static cc.redberry.core.groups.permutations.AlgorithmsBase.*;
import static cc.redberry.core.number.NumberUtils.factorial;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class PermutationGroup
        implements Iterable<Permutation> {
    /**
     * Base and strong generating set
     */
    private final List<BSGSElement> bsgs;
    /**
     * Saved base
     */
    private final int[] base;
    /**
     * Group degree
     */
    private final int degree;
    /**
     * Group order
     */
    private final BigInteger order;
    /**
     * Points accessory in orbits
     */
    private final int[] positionsInOrbits;
    /**
     * Group orbits
     */
    private final int[][] orbits;
    /**
     * Ordering induced by base
     */
    private final InducedOrdering ordering;

    PermutationGroup(List<BSGSElement> bsgs) {
        this.bsgs = bsgs;
        this.base = getBaseAsArray(bsgs);
        this.degree = bsgs.get(0).degree();
        this.order = calculateOrder(bsgs);
        this.positionsInOrbits = new int[degree];
        this.orbits = Permutations.orbits(bsgs.get(0).stabilizerGenerators, this.positionsInOrbits);
        this.ordering = new InducedOrdering(base, degree);
    }

    /**
     * Returns whether the specified permutation is member of this group
     *
     * @param permutation permutation
     * @return true if specified permutation is member of this group
     */
    public boolean membershipTest(Permutation permutation) {
        return AlgorithmsBase.membershipTest(bsgs, permutation);
    }

    /**
     * Returns whether all specified permutations are members of this group
     * <p/>
     * //     * @param permutations permutations
     *
     * @return true if all specified permutations are members of this group
     */
    public boolean membershipTest(Collection<Permutation> permutations) {
        for (Permutation p : permutations)
            if (!membershipTest(p))
                return false;
        return true;
    }

    /**
     * Returns the number of permutations in this group
     *
     * @return number of permutations in this group
     */
    public BigInteger order() {
        return order;
    }

    /**
     * Returns an unmodifiable list of group generators.
     *
     * @return unmodifiable list of group generators
     */
    public List<Permutation> generators() {
        return bsgs.get(0).stabilizerGenerators;
    }

    /**
     * Return the degree of group (length of each permutation)
     *
     * @return degree of group (length of each permutation)
     */
    public int degree() {
        return degree;
    }

    /**
     * Returns base and strong generating set of this group.
     *
     * @return base and strong generating set of this grou
     */
    public List<BSGSElement> getBSGS() {
        return bsgs;
    }

    /**
     * Returns a mutable copy of base and strong generating set.
     *
     * @return a mutable copy of base and strong generating set
     */
    public ArrayList<BSGSCandidateElement> getBSGSCandidate() {
        return asBSGSCandidatesList(bsgs);
    }

    /**
     * Returns base of this group.
     *
     * @return base of this group
     */
    public int[] getBase() {
        return base.clone();
    }

    /**
     * Returns an ordering on Ω(n) induced by a base of this group
     *
     * @return ordering on Ω(n) induced by a base of this group
     */
    public InducedOrdering ordering() {
        return ordering;
    }

    /**
     * Returns the orbit of specified point
     *
     * @param point point
     * @return orbit of specified point
     */
    int[] orbit(int point) {
        return orbits[positionsInOrbits[point]].clone();
    }

    /**
     * Returns the orbit of specified set of points
     *
     * @param points set of points
     * @return orbit of specified point
     */
    public int[] orbit(int... points) {
        TIntHashSet orbitsIndexesSet = new TIntHashSet();
        for (int i : points)
            orbitsIndexesSet.add(positionsInOrbits[i]);

        int[] orbitsIndexes = orbitsIndexesSet.toArray();
        int orbitSize = 0;
        for (int i : orbitsIndexes)
            orbitSize += this.orbits[i].length;

        int[] orbit = new int[orbitSize];
        orbitSize = 0;
        for (int i : orbitsIndexes) {
            System.arraycopy(this.orbits[i], 0, orbit, orbitSize, this.orbits[i].length);
            orbitSize += this.orbits[i].length;
        }

        return orbit;
    }

    /**
     * Returns a set of all orbits
     *
     * @return set of all orbits
     */
    public int[][] orbits() {
        int[][] r = new int[orbits.length][];
        for (int i = 0; i < orbits.length; ++i)
            r[i] = orbits[i].clone();
        return r;
    }

    /**
     * Returns true if this group is transitive.
     *
     * @return true if this group is transitive
     */
    public boolean isTransitive() {
        return orbits().length == 1;
    }

    /**
     * Returns true if this group is trivial (order == 1).
     *
     * @return true if this group is trivial (order == 1)
     */
    public boolean isTrivial() {
        return order.compareTo(BigInteger.ONE) == 0;
    }

    private Boolean isSymmetric = null;

    /**
     * Returns whether this group is symmetric group
     *
     * @return true is this group is full symmetric group S(n)
     */
    public boolean isSymmetric() {
        if (isSymmetric == null)
            isSymmetric = order.compareTo(factorial(degree)) == 0;
        return isSymmetric.booleanValue();
    }

    private Boolean isAlternating = null;

    /**
     * Returns whether this group is symmetric group
     *
     * @return true is this group is full symmetric group S(n)
     */
    public boolean isAlternating() {
        if (isAlternating == null) {
            isAlternating = factorial(degree).divide(BigInteger.valueOf(2)).equals(order);
            if (isAlternating) {
                List<Permutation> generators = generators();
                for (Permutation p : generators)
                    if (p.parity() == 1) {
                        isAlternating = false;
                        break;
                    }
            }
        }
        return isAlternating.booleanValue();
    }

    /**
     * Returns whether this group is regular (i.e. it is transitive and order == degree).
     *
     * @return true is this group is transitive and its order equals to degree
     */
    public boolean isRegular() {
        return isTransitive() && order.compareTo(BigInteger.valueOf(degree)) == 0;
    }

    /**
     * Calculates a pointwise stabilizer of specified set of points.
     *
     * @param set set of points
     * @return pointwise stabilizer of specified set of points.
     */
    public PermutationGroup pointwiseStabilizer(int... set) {
        if (set.length == 0)
            return this;
        set = MathUtils.getSortedDistinct(set.clone());
        ArraysUtils.quickSort(set, ordering);

        ArrayList<BSGSCandidateElement> bsgs = getBSGSCandidate();
        AlgorithmsBase.rebase(bsgs, set);

        if (bsgs.size() <= set.length)
            return new PermutationGroup(createEmptyBSGS(degree));

        return new PermutationGroup(
                Collections.unmodifiableList(
                        asBSGSList(bsgs.subList(set.length, bsgs.size()))));
    }

    /**
     * Calculates a setwise stabilizer of specified set of points.
     *
     * @param set set of points
     * @return setwise stabilizer of specified set of points.
     */
    public PermutationGroup setwiseStabilizer(int... set) {
        if (set.length == 0)
            return this;
        set = MathUtils.getSortedDistinct(set.clone());
        //let's rebase such that the initial segment of base is equal to set
        //so at each level l < set.length we test that g(β_l) ∈ set, and if l >= set.length, then g(β_l) !∈ set
        ArraysUtils.quickSort(set, ordering);
        ArrayList<BSGSCandidateElement> bsgs = getBSGSCandidate();
        AlgorithmsBase.rebase(bsgs, set);

        //sorting set according to natural ordering
        Arrays.sort(set);

        //we need to ensure that no any base point β_i with i >= set.length belongs to set (such points are redundant)
        for (int i = bsgs.size() - 1; i >= set.length; --i)
            if (Arrays.binarySearch(set, bsgs.get(i).basePoint) >= 0)
                bsgs.remove(i);

        //lets take the initial subgroup equal to pointwise stabilizer
        ArrayList<BSGSCandidateElement> stabilizer
                = AlgorithmsBase.clone(new ArrayList<>(bsgs.subList(set.length, bsgs.size())));

        //run subgroup search
        int[] newBase = getBaseAsArray(bsgs);
        SetwiseStabilizerSearchTest swTest = new SetwiseStabilizerSearchTest(newBase, set);
        AlgorithmsBacktrack.subgroupSearch(bsgs, stabilizer,
                swTest, swTest, newBase, new InducedOrdering(newBase, degree));

        return new PermutationGroup(Collections.unmodifiableList(asBSGSList(stabilizer)));
    }

    /**
     * TEST function for backtrack search to find setwise stabilizer.
     */
    private static class SetwiseStabilizerSearchTest
            implements BacktrackSearchTestFunction, Indicator<Permutation> {
        //prepared base: the initial segment of base is equal to set
        final int[] base;
        //prepared set: set is sorted
        final int[] set;

        SetwiseStabilizerSearchTest(int[] base, int[] set) {
            this.base = base;
            this.set = set;
        }

        @Override
        public boolean test(Permutation permutation, int level) {
            if (level < set.length) {
                assert Arrays.binarySearch(set, base[level]) >= 0;
                return Arrays.binarySearch(set, permutation.newIndexOf(base[level])) >= 0;
            } else
                return Arrays.binarySearch(set, permutation.newIndexOf(base[level])) < 0;
        }

        @Override
        public boolean is(Permutation p) {
            for (int s : set)
                if (Arrays.binarySearch(set, p.newIndexOf(s)) < 0)
                    return false;
            return true;
        }
    }

    /**
     * Returns true if specified group is a subgroup of this group
     *
     * @param subgroup permutation group
     * @return true if specified group is a subgroup of this group
     * @throws IllegalArgumentException if {@code subgroup.degree() != this.degree() }
     */
    public boolean isSubgroup(PermutationGroup subgroup) {
        checkDegree(subgroup.degree());
        if (subgroup.order.compareTo(order) > 0)
            return false;
        return membershipTest(subgroup.generators());
    }


    /**
     * Returns a set of right coset representatives of a given subgroup in this group. The number of such
     * representatives is {@code this.order() / subgroup.order() } (according to Lagrange theorem).
     *
     * @param subgroup a subgroup of this group
     * @return set of right coset representatives
     * @throws IllegalArgumentException if {@code subgroup.degree() != this.degree() }
     */
    public Permutation[] leftCosetRepresentatives(PermutationGroup subgroup) {
        checkDegree(subgroup.degree());
        //todo implement special cases for Alt and Sym
        return AlgorithmsBacktrack.leftCosetRepresentatives(bsgs, subgroup.getBSGS(), base, ordering);
    }

    /**
     * Returns coset representative of specified element; the returned representative will be minimal in its coset.
     *
     * @param subgroup a subgroup of this group
     * @param element  some element of this group
     * @throws IllegalArgumentException if {@code subgroup.degree() != this.degree() }
     */
    public Permutation leftTransversalOf(PermutationGroup subgroup, Permutation element) {
        return AlgorithmsBacktrack.leftTransversalOf(element, bsgs, subgroup.getBSGS(), base, ordering);
    }

    /**
     * Returns a union of this and specified groups, i.e. group which is generated by union of generators of this and
     * specified groups.
     *
     * @param group permutation group
     * @return union of this and specified groups
     * @throws IllegalArgumentException if {@code group.degree() != this.degree() }
     */
    public PermutationGroup union(PermutationGroup group) {
        checkDegree(group.degree);

        if (isTrivial())
            return group;
        if (group.isTrivial())
            return this;

        if (isSubgroup(group))
            return this;
        if (group.isSubgroup(this))
            return group;

        int[] base = MathUtils.intSetUnion(this.base.clone(), group.base.clone());
        //new generators
        ArrayList<Permutation> generators = new ArrayList<>(generators());
        generators.addAll(group.generators());
        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(base, generators);
        SchreierSimsAlgorithm(bsgs);

        return new PermutationGroup(asBSGSList(bsgs));
    }

    /**
     * Returns an intersection of this group with specified group
     *
     * @param subgroup permutation group
     * @return intersections of groups
     * @throws IllegalArgumentException if {@code group.degree() != this.degree() }
     */
    public PermutationGroup intersection(PermutationGroup subgroup) {
        ArrayList<BSGSCandidateElement> intersection = new ArrayList<>();
        AlgorithmsBacktrack.intersection(bsgs, subgroup.bsgs, intersection);
        return new PermutationGroup(Collections.unmodifiableList(asBSGSList(intersection)));
    }

    /**
     * Returns direct product of this group and specified group. This product is organized as follows:
     * the initial segment of each permutation is equal to permutation taken from this, while the rest is taken from
     * specified permutation.
     *
     * @param group another group
     * @return direct product this × other
     */
    public PermutationGroup directProduct(PermutationGroup group) {
        return new PermutationGroup(
                Collections.unmodifiableList(AlgorithmsBase.directProduct(bsgs, group.bsgs)));
    }

    /**
     * Returns an output port of permutations that preserves specified mapping between points. To be precise: for each
     * permutation <i>p</i> returned by {@link cc.redberry.core.groups.permutations.BacktrackSearch#take()} and for
     * all <i>i</i> ∈ {@code {0..from.length}}, it is guaranteed to be {@code p.newIndexOf(from[i]) == to[i]}.
     *
     * @param from from
     * @param to   to
     * @return output port of permutations that preserves specified mapping between points
     * @throws IllegalArgumentException if {@code from.length != to.length}
     */
    public BacktrackSearch mapping(final int[] from, final int[] to) {
        if (from.length != to.length)
            throw new IllegalArgumentException("Length of from is not equal to length of to.");

        final int[] _from_ = from.clone(), _to_ = to.clone();
        //make rebase as simple as possible
        ArraysUtils.quickSort(_from_, _to_, ordering);
        ArrayList<BSGSCandidateElement> bsgs = getBSGSCandidate();
        AlgorithmsBacktrack.rebaseWithRedundancy(bsgs, _from_, degree);

        SearchForMapping mapping = new SearchForMapping(_from_, _to_);
        return new BacktrackSearch(bsgs, mapping, mapping);
    }

    private static final class SearchForMapping
            implements BacktrackSearchTestFunction, Indicator<Permutation> {
        final int[] from, to;

        private SearchForMapping(int[] from, int[] to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean test(Permutation permutation, int level) {
            if (level < from.length)
                return permutation.newIndexOf(from[level]) == to[level];
            return true;
        }

        @Override
        public boolean is(Permutation object) {
            return true;
        }
    }

    @Override
    public Iterator<Permutation> iterator() {
        return new PermIterator(); //new OutputPortUnsafe.PortIterator<>(new BacktrackSearch(bsgs));
    }

    /**
     * An iterator over all permutations in group
     */
    public class PermIterator implements Iterator<Permutation> {
        private final IntTuplesPort tuplesPort;
        int[] tuple;

        public PermIterator() {
            final int[] orbitSizes = new int[base.length];
            for (int i = 0; i < orbitSizes.length; ++i)
                orbitSizes[i] = bsgs.get(i).orbitSize();
            tuplesPort = new IntTuplesPort(orbitSizes);
            tuple = tuplesPort.take();
        }

        @Override
        public boolean hasNext() {
            return tuple != null;
        }

        @Override
        public Permutation next() {
            Permutation p = bsgs.get(0).getInverseTransversalOf(bsgs.get(0).getOrbitPoint(tuple[0]));
            BSGSElement e;
            for (int i = 1, size = bsgs.size(); i < size; ++i) {
                e = bsgs.get(i);
                p = p.composition(e.getInverseTransversalOf(e.getOrbitPoint(tuple[i])));
            }
            tuple = tuplesPort.take();
            return p;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Illegal operation.");
        }
    }

    /**
     * Returns whether specified group is equal to this group.
     *
     * @param oth permutation group
     * @return true if specified group has sam order and all its generators are contained in this group
     * @throws IllegalArgumentException {@code oth.degree != this.degree()}
     */
    public boolean equals(PermutationGroup oth) {
        if (degree != oth.degree)
            throw new IllegalArgumentException("Not same degrees.");

        if (!order.equals(oth.order))
            return false;
        if (orbits.length != oth.orbits.length)
            return false;

        if (generators().size() < oth.generators().size())
            return oth.membershipTest(generators());
        else
            return membershipTest(oth.generators());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PermutationGroup( ");
        List<Permutation> gens = generators();
        for (int i = 0; ; ++i) {
            sb.append(gens.get(i));
            if (i == gens.size() - 1)
                return sb.append(" )").toString();
            sb.append(", ");
        }
    }

    private void checkDegree(int degree) {
        if (this.degree != degree)
            throw new IllegalArgumentException("Not same degrees.");
    }
}
