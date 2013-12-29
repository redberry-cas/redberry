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
import cc.redberry.core.context.CC;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.IntComparator;
import cc.redberry.core.utils.MathUtils;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.math3.primes.Primes;
import org.apache.commons.math3.util.FastMath;

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
     * Generators of group
     */
    private final List<Permutation> generators;
    /**
     * Group degree
     */
    private final int degree;
    /**
     * Points accessory in orbits
     */
    private final int[] positionsInOrbits;
    /**
     * Group orbits
     */
    private final int[][] orbits;

    //lazy fields:
    /**
     * Base and strong generating set
     */
    private List<BSGSElement> bsgs = null;
    /**
     * Cached base array
     */
    private int[] base = null;
    /**
     * Group order
     */
    private BigInteger order = null;
    /**
     * Ordering induced by base
     */
    private InducedOrdering ordering = null;

    /**
     * @param generators
     */
    public PermutationGroup(Permutation... generators) {
        this(Arrays.asList(generators));
    }

    /**
     * @param generators
     */
    public PermutationGroup(List<Permutation> generators) {
        if (generators.isEmpty())
            throw new IllegalArgumentException("Empty generators.");
        this.generators = Collections.unmodifiableList(new ArrayList<>(generators));
        this.degree = generators.get(0).degree();
        this.positionsInOrbits = new int[degree];
        this.orbits = Permutations.orbits(generators, this.positionsInOrbits);
    }

    /**
     * @param bsgs
     * @param precalculated
     */
    public PermutationGroup(List<BSGSElement> bsgs, boolean precalculated) {
        if (bsgs.isEmpty())
            throw new IllegalArgumentException("Empty BSGS specified.");
        this.bsgs = Collections.unmodifiableList(bsgs);
        this.base = getBaseAsArray(bsgs);
        this.degree = bsgs.get(0).degree();
        this.order = calculateOrder(bsgs);
        this.positionsInOrbits = new int[degree];
        this.generators = bsgs.get(0).stabilizerGenerators;
        this.orbits = Permutations.orbits(bsgs.get(0).stabilizerGenerators, this.positionsInOrbits);
        this.ordering = new InducedOrdering(base, degree);
    }

    /**
     * Initializes lazy fields
     */
    private void ensureBSGSIsInitialized() {
        if (bsgs == null) {
            bsgs = AlgorithmsBase.createBSGSList(generators);
            if (bsgs.isEmpty())
                bsgs = AlgorithmsBase.createEmptyBSGS(degree);
            base = getBaseAsArray(bsgs);
            order = calculateOrder(bsgs);
            ordering = new InducedOrdering(base, degree);
        }
    }

    ////////////////////// METHODS THAT NOT USE BSGS /////////////////////////

    /**
     * Returns an unmodifiable list of group generators.
     *
     * @return unmodifiable list of group generators
     */
    public List<Permutation> generators() {
        return generators;
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
        if (points.length == 0)
            throw new IllegalArgumentException("No points specified.");
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
     * Returns true if this group is transitive and false otherwise.
     *
     * @return true if this group is transitive and false otherwise
     */
    public boolean isTransitive() {
        return orbits().length == 1;
    }

    /**
     * Returns true if this group is trivial and false otherwise.
     *
     * @return true if this group is trivial and false otherwise
     */
    public boolean isTrivial() {
        boolean trivial = true;
        for (Permutation p : generators)
            if (!p.isIdentity()) {
                trivial = false;
                break;
            }
        return trivial;
    }

    Boolean isAbelian = null;

    /**
     * Returns true if this group is abelian and false otherwise.
     *
     * @return true if this group is abelian and false otherwise.
     */
    public boolean isAbelian() {
        if (isAbelian != null)
            return isAbelian.booleanValue();

        isAbelian = true;
        List<Permutation> generators = generators();
        final int size = generators.size();
        out:
        for (int i = 0; i < size; ++i)
            for (int j = i + 1; j < size; ++j)
                if (!generators.get(i).commutator(generators.get(j)).isIdentity()) {
                    isAbelian = false;
                    break out;
                }

        return isAbelian.booleanValue();
    }

    List<Permutation> randomSource = null;

    /**
     * Returns a random source of permutations in this group.
     *
     * @return a random source of permutations in this group
     * @see cc.redberry.core.groups.permutations.RandomPermutation#random(java.util.List, org.apache.commons.math3.random.RandomGenerator)
     */
    public List<Permutation> randomSource() {
        if (randomSource == null) {
            ArrayList<Permutation> randomSource = new ArrayList<>(generators());
            RandomPermutation.randomness(randomSource, 10, 20, CC.getRandomGenerator());
            return this.randomSource = randomSource;
        }
        return randomSource;
    }


    ////////////////////// METHODS THAT USE BSGS /////////////////////////

    /**
     * Returns base and strong generating set of this group.
     *
     * @return base and strong generating set of this group
     */
    public List<BSGSElement> getBSGS() {
        ensureBSGSIsInitialized();
        return bsgs;
    }

    /**
     * Returns base of this group.
     *
     * @return base of this group
     */
    public int[] getBase() {
        ensureBSGSIsInitialized();
        return base.clone();
    }

    /**
     * Returns reference to base array.
     *
     * @return reference to base array
     */
    private int[] base() {
        ensureBSGSIsInitialized();
        return base;
    }

    /**
     * Returns the number of permutations in this group
     *
     * @return number of permutations in this group
     */
    public BigInteger order() {
        ensureBSGSIsInitialized();
        return order;
    }

    /**
     * Returns an ordering on Ω(n) induced by a base of this group
     *
     * @return ordering on Ω(n) induced by a base of this group
     */
    public InducedOrdering ordering() {
        ensureBSGSIsInitialized();
        return ordering;
    }

    /**
     * Returns whether the specified permutation is member of this group
     *
     * @param permutation permutation
     * @return true if specified permutation is member of this group
     */
    public boolean membershipTest(Permutation permutation) {
        return AlgorithmsBase.membershipTest(getBSGS(), permutation);
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
     * Returns a mutable copy of base and strong generating set.
     *
     * @return a mutable copy of base and strong generating set
     */
    public ArrayList<BSGSCandidateElement> getBSGSCandidate() {
        return asBSGSCandidatesList(getBSGS());
    }

    private Boolean isSymmetric = null;

    /**
     * Returns whether this group is symmetric group
     *
     * @return true is this group is full symmetric group and false otherwise.
     */
    public boolean isSymmetric() {
        if (isSymmetric != null)
            return isSymmetric;

        if (isTrivial() || !isTransitive())
            return isSymmetric = false;
        if (degree > 2 && generators().size() == 1)
            return isSymmetric = false;

        isSymmetric = isSymOrAlt(DEFAULT_CONFIDENCE_LEVEL);
        if (isSymmetric) {
            boolean containsOdd = false;
            for (Permutation p : generators())
                if (p.parity() == 1) {
                    containsOdd = true;
                    break;
                }
            return isSymmetric = containsOdd;
        } else
            return isSymmetric = order().equals(factorial(degree));
    }

    private Boolean isAlternating = null;

    /**
     * Returns whether this group is alternating group
     *
     * @return true is this group is alternating group Alt(degree) and false otherwise
     */
    public boolean isAlternating() {
        if (isAlternating != null)
            return isAlternating;

        if (isTrivial() || !isTransitive())
            return isAlternating = false;

        isAlternating = isSymOrAlt(DEFAULT_CONFIDENCE_LEVEL);
        if (!isAlternating)
            isAlternating = order().equals(factorial(degree).divide(BigInteger.valueOf(2)));

        if (isAlternating) {
            List<Permutation> generators = generators();
            for (Permutation p : generators)
                if (p.parity() == 1)
                    return isAlternating = false;
        }

        return isAlternating;
    }

    private static double DEFAULT_CONFIDENCE_LEVEL = 1 - 1E-6;

    /**
     * Tests whether this is Sym or Alt (for degree > 8) using random, see Sec. 4.2 in [Holt05].
     *
     * @param CL confidence level
     */
    private boolean isSymOrAlt(double CL) {
        if (degree < 8)
            return false;
        double c = degree <= 16 ? 0.34 : 0.57;
        int num = (int) (-FastMath.log(1 - CL) * FastMath.log(2, degree) / c);
        List<Permutation> randomSource = randomSource();
        for (int i = 0; i < num; ++i) {
            int[] lengths = RandomPermutation.random(randomSource).lengthsOfCycles();
            for (int length : lengths)
                if (length > degree / 2 && length < degree - 2 && Primes.isPrime(length))
                    return true;
        }
        return false;
    }

    /**
     * Returns whether this group is regular (i.e. it is transitive and order == degree).
     *
     * @return true is this group is transitive and its order equals to degree
     */
    public boolean isRegular() {
        return isTransitive() && order().compareTo(BigInteger.valueOf(degree)) == 0;
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
        ArraysUtils.quickSort(set, ordering());

        ArrayList<BSGSCandidateElement> bsgs = getBSGSCandidate();
        AlgorithmsBase.rebase(bsgs, set);

        if (bsgs.size() <= set.length)
            return new PermutationGroup(createEmptyBSGS(degree), true);

        return new PermutationGroup(asBSGSList(bsgs.subList(set.length, bsgs.size())), true);
    }

    /**
     * Calculates normal closure of specified subgroup. The algorithm follows NORMALCLOSURE (randomized version)
     * described in Sec. 3.3.2 in [Holt05].
     *
     * @param subgroup subgroup of this
     * @return normal closure
     */
    public PermutationGroup normalClosureOf(PermutationGroup subgroup) {
        if (subgroup.isTrivial())
            return subgroup;

        if (isAlternating() && degree > 4)
            return this;

        if (isSymmetric() && degree != 4) {
            //in this case the only nontrivial normal subgroup is Alt(degree)
            //check that all generators of subgroups are even:
            for (Permutation p : subgroup.generators)
                if (p.parity() == 1)
                    return this; //subgroup contains odd permutations
            return PermutationGroupFactory.alternatingGroup(degree);
        }
        //resulting BSGS
        ArrayList<BSGSCandidateElement> closure = subgroup.getBSGSCandidate();
        //random source of this
        List<Permutation> randomSource = randomSource();

        double CL = 1 - 1E-6;
        boolean completed = false, added, globalAdded = false;
        while (!completed) {
            //random source of closure
            ArrayList<Permutation> closureSource = new ArrayList<>(closure.get(0).stabilizerGenerators);
            RandomPermutation.randomness(closureSource, 10, 10, CC.getRandomGenerator());

            added = false;
            for (int i = 0; i < 10; ++i) {
                //adding some random conjugation
                Permutation c = RandomPermutation.random(randomSource).conjugate(
                        RandomPermutation.random(closureSource));

                if (!AlgorithmsBase.membershipTest(closure, c)) {
                    closure.get(0).stabilizerGenerators.add(c);
                    added = true;
                    globalAdded = true;
                }
            }
            //We use random version of Schreier-Sims; although constructed BSGS is not guaranteed to be a real BSGS,
            // if some element belongs to closure, the the result of membership test will be guaranteed true (nos such
            // guarantee in the case of false).
            if (added)
                AlgorithmsBase.RandomSchreierSimsAlgorithm(closure, CL, CC.getRandomGenerator());
            //testing closure
            completed = true;
            for (Permutation generator : generators)
                for (Permutation cGenerator : closure.get(0).stabilizerGenerators)
                    if (!AlgorithmsBase.membershipTest(closure, generator.conjugate(cGenerator))) {
                        completed = false;
                        break;
                    }
        }
        //check BSGS
        if (globalAdded)
            AlgorithmsBase.SchreierSimsAlgorithm(closure);
        return new PermutationGroup(asBSGSList(closure), true);
    }

    /**
     * Returns a commutator of this and specified group.
     *
     * @param group permutation group
     * @return commutator of this and specified group
     */
    public PermutationGroup commutator(PermutationGroup group) {
        //commutator is normal closure of set [generators, group.generators] in <generators,group.generators>.
        ArrayList<Permutation> commutator = new ArrayList<>();
        Permutation c;
        for (Permutation a : generators)
            for (Permutation b : group.generators) {
                c = a.commutator(b);
                if (!c.isIdentity())
                    commutator.add(c);
            }
        if (commutator.isEmpty())
            return new PermutationGroup(createEmptyBSGS(degree), true);
        return union(group).normalClosureOf(new PermutationGroup(commutator));
    }

    /**
     * Returns a derived subgroup, i.e. commutator subgroup of this with itself.
     *
     * @return derived subgroup, i.e. commutator subgroup of this with itself
     */
    public PermutationGroup derivedSubgroup() {
        if (isSymmetric())
            return PermutationGroupFactory.alternatingGroup(degree);
        if (isAlternating() && degree > 4)
            return this;
        return commutator(this);
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
        ArraysUtils.quickSort(set, ordering());
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

        return new PermutationGroup(asBSGSList(stabilizer), true);
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
    public boolean containsSubgroup(PermutationGroup subgroup) {
        if (subgroup.order().compareTo(order()) > 0)
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
        return AlgorithmsBacktrack.leftCosetRepresentatives(getBSGS(), subgroup.getBSGS(), base(), ordering());
    }

    /**
     * Returns coset representative of specified element; the returned representative will be minimal in its coset.
     *
     * @param subgroup a subgroup of this group
     * @param element  some element of this group
     * @throws IllegalArgumentException if {@code subgroup.degree() != this.degree() }
     */
    public Permutation leftTransversalOf(PermutationGroup subgroup, Permutation element) {
        return AlgorithmsBacktrack.leftTransversalOf(element, getBSGS(), subgroup.getBSGS(), base(), ordering());
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

        if (this == group)
            return this;
        if (isTrivial())
            return group;
        if (group.isTrivial())
            return this;

        if (containsSubgroup(group))
            return this;
        if (group.containsSubgroup(this))
            return group;

        int[] base = MathUtils.intSetUnion(getBase(), group.getBase());
        //new generators
        ArrayList<Permutation> generators = new ArrayList<>(generators());
        generators.addAll(group.generators());
        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(base, generators);
        SchreierSimsAlgorithm(bsgs);

        return new PermutationGroup(asBSGSList(bsgs), true);
    }

    /**
     * Returns an intersection of this group with specified group
     *
     * @param subgroup permutation group
     * @return intersections of groups
     * @throws IllegalArgumentException if {@code group.degree() != this.degree() }
     */
    public PermutationGroup intersection(PermutationGroup subgroup) {
        if (isTrivial())
            return this;
        if (subgroup.isTrivial())
            return subgroup;
        ArrayList<BSGSCandidateElement> intersection = new ArrayList<>();
        AlgorithmsBacktrack.intersection(getBSGS(), subgroup.getBSGS(), intersection);
        return new PermutationGroup(asBSGSList(intersection), true);
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
        return new PermutationGroup(AlgorithmsBase.directProduct(getBSGS(), group.getBSGS()), true);
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
        ArraysUtils.quickSort(_from_, _to_, ordering());//<= ensureBSGSIsInitialized();
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
        ensureBSGSIsInitialized();
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

    public PermutationGroup centralizerOf(final PermutationGroup subgroup) {
        IntComparator comparator = new IntComparator() {
            @Override
            public int compare(int a, int b) {
                if (subgroup.positionsInOrbits[a] == subgroup.positionsInOrbits[b])
                    return 0;
                return -Integer.compare(
                        subgroup.orbits[subgroup.positionsInOrbits[a]].length,
                        subgroup.orbits[subgroup.positionsInOrbits[b]].length);
            }
        };
        int[] base = getBase();
        ArraysUtils.quickSort(base, comparator);

        final ArrayList<BSGSCandidateElement> group_bsgs = getBSGSCandidate();
        AlgorithmsBase.rebase(group_bsgs, base);

        final ArrayList<BSGSCandidateElement> subgroup_bsgs = subgroup.getBSGSCandidate();
        AlgorithmsBacktrack.rebaseWithRedundancy(subgroup_bsgs, base, degree);

        final Permutation[] mappings = new Permutation[base.length - 1];
        for (int i = 1; i < base.length; ++i) {
            if (subgroup.positionsInOrbits[base[i]] != subgroup.positionsInOrbits[base[i - 1]])
                continue;

            if (subgroup_bsgs.get(i - 1).belongsToOrbit(base[i]))
                mappings[i - 1] = subgroup_bsgs.get(i - 1).getTransversalOf(base[i]);
            else
                mappings[i - 1] = subgroup.mapping(new int[]{base[i - 1]}, new int[]{base[i]}).take();
        }

        CentralizerSearchTest centralizerSearch = new CentralizerSearchTest(group_bsgs, subgroup_bsgs,
                subgroup.orbits, subgroup.positionsInOrbits, base, mappings);

        ArrayList<BSGSCandidateElement> centralizer;
        if (subgroup.generators().size() == 1)
            centralizer = AlgorithmsBase.clone(subgroup_bsgs);
        else
            centralizer = new ArrayList<>();
        AlgorithmsBacktrack.subgroupSearch(group_bsgs, centralizer, centralizerSearch, centralizerSearch);
        return new PermutationGroup(asBSGSList(centralizer), true);
    }

    private static class CentralizerSearchTest
            implements BacktrackSearchTestFunction, Indicator<Permutation> {
        final List<? extends BSGSElement> group_bsgs, subgroup_bsgs;
        final int[][] subgroup_orbits;
        final int[] subgroup_positionsInOrbits;
        final Permutation[] mappings;
        final int[] group_base;

        private CentralizerSearchTest(List<? extends BSGSElement> group_bsgs,
                                      List<? extends BSGSElement> subgroup_bsgs,
                                      int[][] subgroup_orbits,
                                      int[] subgroup_positionsInOrbits,
                                      int[] group_base,
                                      Permutation[] mappings) {
            this.group_bsgs = group_bsgs;
            this.subgroup_bsgs = subgroup_bsgs;
            this.subgroup_orbits = subgroup_orbits;
            this.subgroup_positionsInOrbits = subgroup_positionsInOrbits;
            this.group_base = group_base;
            this.mappings = mappings;
        }


        @Override
        public boolean test(Permutation permutation, int level) {
            if (level == 0)
                return true;
            //find previous base point that lies in same orbit of subgroup
            if (subgroup_positionsInOrbits[group_base[level - 1]] !=
                    subgroup_positionsInOrbits[group_base[level]])
                return true;
            Permutation mapping = mappings[level - 1];
            int expected = mapping.newIndexOf(permutation.newIndexOf(group_base[level - 1]));
            return permutation.newIndexOf(group_base[level]) == expected;
        }

        @Override
        public boolean is(Permutation permutation) {
            if (permutation.isIdentity())
                return false;
            for (Permutation p : subgroup_bsgs.get(0).stabilizerGenerators)
                if (!p.commutator(permutation).isIdentity())
                    return false;
            return true;
        }
    }

    private Permutation someMapping(int from, int to) {
        assert positionsInOrbits[from] == positionsInOrbits[to];
        return null;
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

        if (orbits.length != oth.orbits.length)
            return false;
        //todo add orbits equals!
        //if (!Arrays.deepEquals(orbits, oth.orbits))
        //    return false;
        if (!order().equals(oth.order()))
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
