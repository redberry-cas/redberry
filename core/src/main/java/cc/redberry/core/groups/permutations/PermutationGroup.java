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
package cc.redberry.core.groups.permutations;

import cc.redberry.core.combinatorics.IntTuplesPort;
import cc.redberry.core.context.CC;
import cc.redberry.core.utils.*;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.math3.primes.Primes;
import org.apache.commons.math3.util.FastMath;

import java.math.BigInteger;
import java.util.*;

import static cc.redberry.core.groups.permutations.AlgorithmsBase.*;
import static cc.redberry.core.number.NumberUtils.factorial;

/**
 * Implementation of permutation group; this class provides a number of methods for work wih permutation groups,
 * including membership testing, coset enumeration, searching for centralizers, stabilizers, etc (for details see
 * method summary). The instances of this class are immutable. The iterator returned by this class's {@code iterator()}
 * method iterates over all elements of this group.
 * <p>
 * <b><big>Example</big></b>
 * </p>
 * <p>
 * The following example gives a brief overview of the basic usage of {@code PermutationGroup}
 * <br>
 * <pre style="background:#f1f1f1;color:#000"><span style="color:#406040"> //Construct permutation group of degree 13 with two generators (written in one-line notation)</span>
 * <span style="color:#a08000">PermutationGroup</span> pg <span style="color:#2060a0">=</span> <span style="color:#2060a0">new</span> <span style="color:#a08000">PermutationGroup</span>(
 * <span style="color:#2060a0">new</span> <span style="color:#a08000">PermutationOneLine</span>(<span style="color:#0080a0">9</span>, <span style="color:#0080a0">1</span>, <span style="color:#0080a0">2</span>, <span style="color:#0080a0">0</span>, <span style="color:#0080a0">4</span>, <span style="color:#0080a0">8</span>, <span style="color:#0080a0">5</span>, <span style="color:#0080a0">11</span>, <span style="color:#0080a0">6</span>, <span style="color:#0080a0">3</span>, <span style="color:#0080a0">10</span>, <span style="color:#0080a0">12</span>, <span style="color:#0080a0">7</span>),
 * <span style="color:#2060a0">new</span> <span style="color:#a08000">PermutationOneLine</span>(<span style="color:#0080a0">2</span>, <span style="color:#0080a0">0</span>, <span style="color:#0080a0">1</span>, <span style="color:#0080a0">8</span>, <span style="color:#0080a0">3</span>, <span style="color:#0080a0">5</span>, <span style="color:#0080a0">7</span>, <span style="color:#0080a0">11</span>, <span style="color:#0080a0">4</span>, <span style="color:#0080a0">12</span>, <span style="color:#0080a0">9</span>, <span style="color:#0080a0">6</span>, <span style="color:#0080a0">10</span>));
 * <span style="color:#406040">//this group is transitive</span>
 * <span style="color:#2060a0">assert</span> pg<span style="color:#2060a0">.</span>isTransitive();
 * <span style="color:#406040">//its order = 5616</span>
 * <span style="color:#a08000">System</span><span style="color:#2060a0">.</span>out<span style="color:#2060a0">.</span>println(pg<span style="color:#2060a0">.</span>order());
 * <br><span style="color:#406040">//Create alternating group Alt(13)</span>
 * <span style="color:#a08000">PermutationGroup</span> alt13 <span style="color:#2060a0">=</span> <span style="color:#a08000">PermutationGroup</span><span style="color:#2060a0">.</span>alternatingGroup(<span style="color:#0080a0">13</span>);
 * <span style="color:#406040">//its order = 3113510400</span>
 * <span style="color:#a08000">System</span><span style="color:#2060a0">.</span>out<span style="color:#2060a0">.</span>println(alt13<span style="color:#2060a0">.</span>order());
 * <span style="color:#2060a0">assert</span> alt13<span style="color:#2060a0">.</span>containsSubgroup(pg);
 * <br><span style="color:#406040">//Direct product of two groups</span>
 * <span style="color:#a08000">PermutationGroup</span> pp <span style="color:#2060a0">=</span> pg<span style="color:#2060a0">.</span>directProduct(<span style="color:#a08000">PermutationGroup</span><span style="color:#2060a0">.</span>symmetricGroup(<span style="color:#0080a0">8</span>));
 * <br><span style="color:#406040">//Setwise stabilizer</span>
 * <span style="color:#a08000">PermutationGroup</span> sw <span style="color:#2060a0">=</span> pp<span style="color:#2060a0">.</span>setwiseStabilizer(<span style="color:#0080a0">1</span>, <span style="color:#0080a0">2</span>, <span style="color:#0080a0">3</span>, <span style="color:#0080a0">9</span>, <span style="color:#0080a0">10</span>, <span style="color:#0080a0">11</span>, <span style="color:#0080a0">12</span>, <span style="color:#0080a0">13</span>, <span style="color:#0080a0">14</span>, <span style="color:#0080a0">15</span>, <span style="color:#0080a0">16</span>, <span style="color:#0080a0">17</span>, <span style="color:#0080a0">18</span>);
 * <span style="color:#2060a0">assert</span> pp<span style="color:#2060a0">.</span>containsSubgroup(sw);
 * <span style="color:#406040">//its order = 17280</span>
 * <span style="color:#a08000">System</span><span style="color:#2060a0">.</span>out<span style="color:#2060a0">.</span>println(sw<span style="color:#2060a0">.</span>order());
 * <br><span style="color:#406040">//Center of this stabilizer</span>
 * <span style="color:#a08000">PermutationGroup</span> center <span style="color:#2060a0">=</span> sw<span style="color:#2060a0">.</span>center();
 * <span style="color:#406040">//it is abelian group</span>
 * <span style="color:#2060a0">assert</span> center<span style="color:#2060a0">.</span>isAbelian();
 * <span style="color:#406040">//generators of center</span>
 * <span style="color:#a08000">System</span><span style="color:#2060a0">.</span>out<span style="color:#2060a0">.</span>println(center<span style="color:#2060a0">.</span>generators());
 * <span style="color:#406040">//[+{}, +{{19, 20}}, +{{2, 10}, {3, 9}, {6, 8}, {11, 12}}]</span>
 * <span style="color:#406040">//orbits of center</span>
 * <span style="color:#a08000">int</span>[][] orbits <span style="color:#2060a0">=</span> center<span style="color:#2060a0">.</span>orbits();
 * <span style="color:#2060a0">for</span> (<span style="color:#a08000">int</span>[] orbit <span style="color:#2060a0">:</span> orbits)
 * <span style="color:#2060a0">    if</span> (orbit<span style="color:#2060a0">.</span>length <span style="color:#2060a0">!=</span> <span style="color:#0080a0">1</span>)
 * <span style="color:#a08000">        System</span><span style="color:#2060a0">.</span>out<span style="color:#2060a0">.</span>print(<span style="color:#a08000">Arrays</span><span style="color:#2060a0">.</span>toString(orbit));
 * <span style="color:#406040">    //[2, 10], [3, 9], [6, 8], [11, 12], [19, 20]</span>
 * </pre>
 * </p>
 * <p>
 * <b><big>Implementation and complexity</big></b>
 * </p>
 * <p>
 * The implementation is based on <i>base and strong generating set</i> (BSGS), which is constructed using Schreier-Sims
 * algorithm ({@link cc.redberry.core.groups.permutations.AlgorithmsBase#SchreierSimsAlgorithm(java.util.ArrayList)}).
 * Schreier-Sims algorithm has O(n^6 +k*n^2) complexity (where n is a degree of group), which can be crucial for
 * groups with large bases.  Since not all methods require BSGS, the BSGS structure of {@code PermutationGroup} is
 * <i>lazy initialized</i>, i.e. its initialization occurs on the first invocation of method that uses BSGS.
 * </p>
 * <p>
 * <b>Structural calculations.</b> Generally, all structural calculations have polynomial time complexity. The polynomial-time operations include:
 * calculation of orbits (do not require BSGS); membership testing; calculation of order, base and
 * strong generating set, pointwise stabilizers, union and direct product of groups; tests for
 * commutativity, transitivity, Alt(n) and Sym(n) testing; calculation of normal closure and derived subgroup.
 * </p>
 * <p>
 * <b>Backtrack search.</b> On the other hand, the algorithms that use backtrack search methods have exponential complexity in the
 * worst case, which also hardly depends on the input. This algorithms include: calculation of setwise stabilizers,
 * intersections of groups, coset representatives, centralizers. The exception is the calculation of center of group
 * which is always polynomial.
 * </p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see cc.redberry.core.groups.permutations.Permutation
 * @see cc.redberry.core.groups.permutations.AlgorithmsBase
 * @see cc.redberry.core.groups.permutations.AlgorithmsBacktrack
 * @see cc.redberry.core.groups.permutations.BacktrackSearch
 * @since 1.1.6
 */
public final class PermutationGroup
        implements Iterable<Permutation> {

    public static final PermutationGroup TRIVIAL_GROUP = new PermutationGroup();
    /**
     * Generators of group
     */
    private final List<Permutation> generators;
    /**
     * Degree that used to represent Schreier vectors etc.
     */
    private final int internalDegree;
    /**
     * Degree that used to represent Schreier vectors etc.
     */
    private final int naturalDegree;
    /**
     * Points accessory in orbits
     */
    private final int[] positionsInOrbits;
    /**
     * Group orbits
     */
    private final int[][] orbits;

    /*   lazy fields   */

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

    private PermutationGroup(List<Permutation> generators, int naturalDegree, int internalDegree, int b) {
        if (generators.isEmpty())
            throw new IllegalArgumentException("Empty generators.");
        this.generators = Collections.unmodifiableList(new ArrayList<>(generators));
        this.internalDegree = internalDegree;
        this.naturalDegree = naturalDegree;
        this.positionsInOrbits = new int[internalDegree];
        this.orbits = Permutations.orbits(generators, this.positionsInOrbits);
    }

    private PermutationGroup(List<BSGSElement> bsgs, int naturalDegree, int internalDegree) {
        if (bsgs.isEmpty())
            throw new IllegalArgumentException("Empty BSGS specified.");
        this.bsgs = Collections.unmodifiableList(bsgs);
        this.base = getBaseAsArray(bsgs);
        this.internalDegree = internalDegree;
        this.naturalDegree = naturalDegree;
        this.order = calculateOrder(bsgs);
        this.positionsInOrbits = new int[internalDegree];
        this.generators = bsgs.get(0).stabilizerGenerators;
        this.orbits = Permutations.orbits(bsgs.get(0).stabilizerGenerators, this.positionsInOrbits);
        this.ordering = new InducedOrdering(base, internalDegree);
    }

    //trivial group
    private PermutationGroup() {
        this.bsgs = AlgorithmsBase.TRIVIAL_BSGS;
        this.base = new int[0];
        this.naturalDegree = 0;
        this.internalDegree = 0;
        this.order = BigInteger.ONE;
        this.positionsInOrbits = new int[0];
        this.generators = Collections.singletonList(Permutations.createIdentityPermutation());
        this.orbits = new int[0][0];
        this.ordering = new InducedOrdering(base, 0);
    }

    public static PermutationGroup trivialGroup() {
        return TRIVIAL_GROUP;
    }

    /**
     * Creates permutation group with a given generating set.
     *
     * @param generators generating set
     */
    public static PermutationGroup createPermutationGroup(Permutation... generators) {
        return createPermutationGroup(Arrays.asList(generators));
    }

    /**
     * Creates permutation group with a given generating set.
     *
     * @param generators generating set
     */
    public static PermutationGroup createPermutationGroup(List<Permutation> generators) {
        int degree = Permutations.internalDegree(generators);
        if (degree == 0)
            return TRIVIAL_GROUP;
        return new PermutationGroup(generators, degree, degree, 0);
    }

    /**
     * Creates permutation group with a given base and strong generating set.
     *
     * @param bsgs base and strong generating set
     */
    public static PermutationGroup createPermutationGroupFromBSGS(List<BSGSElement> bsgs) {
        int degree = AlgorithmsBase.internalDegree(bsgs);
        if (degree == 0)
            return TRIVIAL_GROUP;
        return new PermutationGroup(bsgs, bsgs.get(0).maximumMovedPoint(), degree);
    }

    /**
     * Creates symmetric group of specified degree. BSGS structure of symmetric group will be constructed in O(n^2) time.
     *
     * @param degree degree
     * @return symmetric group of specified degree
     * @see cc.redberry.core.groups.permutations.AlgorithmsBase#createSymmetricGroupBSGS(int)
     */
    public static PermutationGroup symmetricGroup(int degree) {
        return createPermutationGroupFromBSGS(createSymmetricGroupBSGS(degree));
    }

    /**
     * Creates symmetric group of specified degree, where all odd permutations are antisymmetries. BSGS structure of
     * symmetric group will be constructed in O(n^2) time.
     *
     * @param degree degree
     * @return antisymmetric group of specified degree
     * @see cc.redberry.core.groups.permutations.AlgorithmsBase#createSymmetricGroupBSGS(int)
     */
    public static PermutationGroup antisymmetricGroup(int degree) {
        return createPermutationGroupFromBSGS(createAntisymmetricGroupBSGS(degree));
    }

    /**
     * Creates alternating group of specified degree. BSGS structure of alternating group will be constructed in O(n^2) time.
     *
     * @param degree degree
     * @return alternating group of specified degree
     * @see cc.redberry.core.groups.permutations.AlgorithmsBase#createAlternatingGroupBSGS(int)
     */
    public static PermutationGroup alternatingGroup(int degree) {
        return createPermutationGroupFromBSGS(createAlternatingGroupBSGS(degree));
    }

    /**
     * Initializes lazy fields
     */
    private void ensureBSGSIsInitialized() {
        if (bsgs == null) {
            if (base != null)
                bsgs = AlgorithmsBase.createBSGSList(base, generators, internalDegree);
            else
                bsgs = AlgorithmsBase.createBSGSList(generators, internalDegree);
            if (bsgs.isEmpty())
                bsgs = TRIVIAL_BSGS;
            base = getBaseAsArray(bsgs);
            order = calculateOrder(bsgs);
            ordering = new InducedOrdering(base, internalDegree);
        }
    }

    ////////////////////// METHODS THAT NOT USE BSGS /////////////////////////

    /**
     * Returns positions of points in array of orbits, i.e. for each point {@code orbits()[getPositionsInOrbits()[point]]} -
     * is its orbit.
     *
     * @return positions of points in array of orbits
     */
    public int[] getPositionsInOrbits() {
        return positionsInOrbits.clone();
    }

    /**
     * Returns an unmodifiable list of group generators.
     *
     * @return unmodifiable list of group generators
     */
    public List<Permutation> generators() {
        return generators;
    }

    /**
     * Returns the natural degree of this group, i.e. the largest point moved by this group plus one.
     *
     * @return the largest point moved by this group plus one
     */
    public int degree() {
        return naturalDegree;
    }

    /**
     * Returns the orbit of specified point.
     *
     * @param point point
     * @return orbit of specified point
     */
    public int[] orbit(int point) {
        if (point >= internalDegree)
            return new int[]{point};
        return orbits[positionsInOrbits[point]].clone();
    }

    /**
     * Returns size of orbit of specified point.
     *
     * @param point point
     * @return size of orbit of specified point
     */
    public int orbitSize(int point) {
        if (point >= internalDegree)
            return 1;
        return orbits[positionsInOrbits[point]].length;
    }

    /**
     * Returns the orbit of specified set of points.
     *
     * @param points set of points
     * @return orbit of specified set of points
     */
    public int[] orbit(int... points) {
        IntArrayList orbit = new IntArrayList();
        TIntHashSet orbitsIndexesSet = new TIntHashSet();
        for (int i : points) {
            if (i < internalDegree) {
                if (!orbitsIndexesSet.contains(positionsInOrbits[i])) {
                    orbitsIndexesSet.ensureCapacity(orbits[positionsInOrbits[i]].length);
                    orbitsIndexesSet.add(positionsInOrbits[i]);
                    orbit.addAll(orbits[positionsInOrbits[i]]);
                }
            } else if (!orbitsIndexesSet.contains(i)) {
                orbitsIndexesSet.add(i);
                orbit.add(i);
            }
        }

        return orbit.toArray();
    }

    /**
     * Returns an array of all orbits.
     *
     * @return an array of all orbits
     */
    public int[][] orbits() {
        int[][] r = new int[orbits.length][];
        for (int i = 0; i < orbits.length; ++i)
            r[i] = orbits[i].clone();
        return r;
    }

    /**
     * Returns an index of orbit of this point in the array of all orbits returned by method {@link #orbits()},
     * i.e. {@code orbits()[indexOfOrbit(point)]} is orbit of specified point, or -1 if {@code point >= degree()}.
     *
     * @param point point
     * @return index of orbit of this point in the array of all orbits or -1 if {@code point >= degree()}
     */
    public int indexOfOrbit(int point) {
        if (point >= internalDegree)
            return -1;
        return positionsInOrbits[point];
    }

    /**
     * Returns true if this group is transitive under the action on the set of its moved points and false otherwise.
     *
     * @return true if this group is transitive under the action on the set of its moved points and false otherwise
     */
    public boolean isTransitive() {
        if (isTrivial())
            return false;
        return orbits.length == 1;
    }

    /**
     * Returns true if this group acts transitively on the array {@code [from, from + 1,...,to-1]} and false if not.
     *
     * @return true if this group acts transitively on the array {@code [from, from + 1,...,to-1]} and false if not
     */
    public boolean isTransitive(int from, int to) {
        if (from > to)
            throw new IllegalArgumentException("Specified from less then specified to.");
        if (to >= internalDegree)
            return false;
        for (int i = from + 1; i < to; ++i)
            if (positionsInOrbits[i] != positionsInOrbits[i - 1])
                return false;
        return true;
    }

    Boolean isTrivial = null;

    /**
     * Returns true if this group is trivial and false otherwise.
     *
     * @return true if this group is trivial and false otherwise
     */
    public boolean isTrivial() {
        if (isTrivial != null)
            return isTrivial.booleanValue();
        isTrivial = true;
        for (Permutation p : generators)
            if (!p.isIdentity()) {
                isTrivial = false;
                break;
            }
        return isTrivial;
    }

    private Boolean isAbelian = null;

    /**
     * Returns true if this group is abelian and false otherwise.
     *
     * @return true if this group is abelian and false otherwise.
     */
    public boolean isAbelian() {
        if (isTrivial())
            return true;
        if (isAbelian != null)
            return isAbelian.booleanValue();

        isAbelian = true;
        List<Permutation> generators = generators();
        final int size = generators.size();
        out:
        for (int i = 0; i < size; ++i)
            for (int j = i + 1; j < size; ++j)
                if (!generators.get(i).commutator(generators.get(j)).isIdentity())
                    return isAbelian = false;


        return isAbelian.booleanValue();
    }

    private List<Permutation> randomSource = null;

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

    /**
     * Extends this group with specified generators, i.e. returns a union of this group and a group generated by
     * specified generators.
     *
     * @param generators new generators
     * @return a group generated by this and specified generators
     */
    public PermutationGroup union(Permutation... generators) {
        return union(Arrays.asList(generators));
    }

    /**
     * Extends this group with specified generators, i.e. returns a union of this group and a group generated by
     * specified generators.
     *
     * @param generators new generators
     * @return a group generated by this and specified generators
     */
    public PermutationGroup union(List<Permutation> generators) {
        if (isTrivial())
            return createPermutationGroup(generators);
        if (bsgs != null)
            if (membershipTest(generators))
                return this;
        List<Permutation> all_generators = new ArrayList<>(generators());
        all_generators.addAll(generators);
        PermutationGroup r = createPermutationGroup(all_generators);
        r.base = base;
        return r;
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
     * Returns the order of this group, i.e. the number of permutations in this group.
     *
     * @return the order of this group
     */
    public BigInteger order() {
        ensureBSGSIsInitialized();
        return order;
    }

    /**
     * Returns an ordering on &Omega;(degree) induced by a base of this group.
     *
     * @return ordering on &Omega;(degree) induced by a base of this group
     */
    public InducedOrdering ordering() {
        ensureBSGSIsInitialized();
        return ordering;
    }

    /**
     * Returns true if specified permutation is member of this group and false otherwise.
     *
     * @param permutation permutation
     * @return true if specified permutation is member of this group and false otherwise
     */
    public boolean membershipTest(Permutation permutation) {
        if (isTrivial())
            return permutation.isIdentity();
        return AlgorithmsBase.membershipTest(getBSGS(), permutation);
    }

    /**
     * Returns true if all specified permutations are members of this group and false otherwise.
     *
     * @param permutations permutations
     * @return true if all specified permutations are members of this group and false otherwise
     * @see #membershipTest(Permutation)
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
     * Returns true if this group is natural symmetric group and false otherwise.
     *
     * @return true is this group is natural symmetric group and false otherwise
     */
    public boolean isSymmetric() {
        if (isSymmetric != null)
            return isSymmetric;
        if (isTrivial())
            return isSymmetric = false;
        if (isTrivial() || !isTransitive())
            return isSymmetric = false;
        if (naturalDegree > 2 && generators().size() == 1)
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
            return isSymmetric = order().equals(factorial(naturalDegree));
    }

    private Boolean isAlternating = null;

    /**
     * Returns true is this group is natural alternating group Alt(degree) and false otherwise.
     *
     * @return true is this group is natural alternating group Alt(degree) and false otherwise
     */
    public boolean isAlternating() {
        if (isAlternating != null)
            return isAlternating;
        if (isTrivial())
            return isAlternating = false;

        if (isTrivial() || !isTransitive())
            return isAlternating = false;

        isAlternating = isSymOrAlt(DEFAULT_CONFIDENCE_LEVEL);
        if (!isAlternating)
            isAlternating = order().equals(factorial(naturalDegree).divide(BigInteger.valueOf(2)));

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
        if (naturalDegree < 8)
            return false;
        double c = naturalDegree <= 16 ? 0.34 : 0.57;
        int num = (int) (-FastMath.log(1 - CL) * FastMath.log(2, naturalDegree) / c);
        List<Permutation> randomSource = randomSource();
        for (int i = 0; i < num; ++i) {
            int[] lengths = RandomPermutation.random(randomSource).lengthsOfCycles();
            for (int length : lengths)
                if (length > naturalDegree / 2 && length < naturalDegree - 2 && Primes.isPrime(length))
                    return true;
        }
        return false;
    }

    /**
     * Returns true if this group is regular (transitive and its order equals to degree) and false otherwise,
     *
     * @return true if this group is regular and false otherwise
     */
    public boolean isRegular() {
        return isTransitive() && order().compareTo(BigInteger.valueOf(naturalDegree)) == 0;
    }

    /**
     * Calculates a pointwise stabilizer of specified set of points.
     *
     * @param set set of points
     * @return pointwise stabilizer of specified set of points.
     */
    public PermutationGroup pointwiseStabilizer(int... set) {
        if (isTrivial())
            return this;
        if (set.length == 0)
            return this;

        set = MathUtils.getSortedDistinct(set.clone());
        int newDegree = Math.max(internalDegree, set[set.length - 1] + 1);
        ArraysUtils.quickSort(set, ordering());

        ArrayList<BSGSCandidateElement> bsgs = getBSGSCandidate();
        AlgorithmsBase.rebase(bsgs, set, newDegree);

        if (bsgs.size() <= set.length)
            return createPermutationGroupFromBSGS(TRIVIAL_BSGS);

        return createPermutationGroupFromBSGS(asBSGSList(bsgs.subList(set.length, bsgs.size())));
    }

    /**
     * Calculates a group which isomorphic to a pointwise stabilizer of specified set but acts on points that are not
     * stabilized, i.e. the degree of the resulting group equal to {@code this.degree() - set.length} (under the
     * assumption that set contains distinct points).
     *
     * @param set set of points
     * @return pointwise stabilizer of specified set of points that acts on points which not contained in specified set
     */
    public PermutationGroup pointwiseStabilizerRestricted(int... set) {
        if (isTrivial())
            return this;
        if (set.length == 0)
            return this;

        set = MathUtils.getSortedDistinct(set);
        final int newDegree = naturalDegree - set.length;
        int[] newBase = set.clone();
        ArraysUtils.quickSort(newBase, ordering());

        ArrayList<BSGSCandidateElement> bsgs = getBSGSCandidate();
        int tempDegree = Math.max(internalDegree, set[set.length - 1] + 1);
        AlgorithmsBase.rebase(bsgs, newBase, tempDegree);

        if (bsgs.size() <= newBase.length)
            return createPermutationGroupFromBSGS(TRIVIAL_BSGS);

        int[] closure = new int[newDegree];
        int[] mapping = new int[internalDegree];
        Arrays.fill(mapping, -1);
        int pointer = 0, counter = 0;
        for (int i = 0; i < internalDegree; ++i) {
            if (pointer < set.length && i == set[pointer]) {
                ++pointer;
                continue;
            } else {
                closure[counter] = i;
                mapping[i] = counter;
                ++counter;
            }
        }
        ArrayList<BSGSCandidateElement> stab = new ArrayList<>();
        for (int i = newBase.length; i < bsgs.size(); ++i) {
            BSGSCandidateElement e = bsgs.get(i);
            if (mapping[e.basePoint] == -1)
                continue;
            ArrayList<Permutation> newStabs = new ArrayList<>(e.stabilizerGenerators.size());
            for (Permutation p : e.stabilizerGenerators) {
                int[] perm = new int[newDegree];
                for (int j = 0; j < newDegree; ++j)
                    perm[j] = mapping[p.newIndexOf(closure[j])];
                newStabs.add(new PermutationOneLine(p.antisymmetry(), perm));
            }
            stab.add(new BSGSCandidateElement(mapping[e.basePoint], newStabs, new int[newDegree]));
        }

        return createPermutationGroupFromBSGS(asBSGSList(stab));
    }

    private static final double NORMAL_CLOSURE_CONFIDENCE_LEVEL = 1 - 1E-6;

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

        if (isAlternating() && naturalDegree > 4)
            return this;

        if (isSymmetric() && naturalDegree != 4) {
            //in this case the only nontrivial normal subgroup is Alt(degree)
            //check that all generators of subgroups are even:
            for (Permutation p : subgroup.generators)
                if (p.parity() == 1)
                    return this; //subgroup contains odd permutations
            return alternatingGroup(naturalDegree);
        }
        //resulting BSGS
        ArrayList<BSGSCandidateElement> closure = subgroup.getBSGSCandidate();
        //random source of this
        List<Permutation> randomSource = randomSource();

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
                    if (subgroup.internalDegree < c.internalDegree()) {
                        //if we add new generator - be sure that Schreier vector has appropriate length
                        closure.set(0, new BSGSCandidateElement(closure.get(0).basePoint,
                                closure.get(0).stabilizerGenerators, new int[c.internalDegree()]));
                    }

                    added = true;
                    globalAdded = true;
                }
            }
            //We use random version of Schreier-Sims; although constructed BSGS is not guaranteed to be a real BSGS,
            // if some element belongs to closure, the the result of membership test will be guaranteed true (nos such
            // guarantee in the case of false).
            if (added)
                AlgorithmsBase.RandomSchreierSimsAlgorithm(closure, NORMAL_CLOSURE_CONFIDENCE_LEVEL, internalDegree, CC.getRandomGenerator());
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
            AlgorithmsBase.SchreierSimsAlgorithm(closure, internalDegree);
        return createPermutationGroupFromBSGS(asBSGSList(closure));
    }

    /**
     * Returns a commutator of this group with specified group.
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
            return createPermutationGroupFromBSGS(TRIVIAL_BSGS);
        return union(group).normalClosureOf(createPermutationGroup(commutator));
    }

    private PermutationGroup derivedSubgroup = null;

    /**
     * Returns a derived subgroup, i.e. commutator subgroup of this with itself.
     *
     * @return derived subgroup, i.e. commutator subgroup of this with itself
     */
    public PermutationGroup derivedSubgroup() {
        if (derivedSubgroup != null)
            return derivedSubgroup;
        if (isSymmetric())
            return derivedSubgroup = alternatingGroup(naturalDegree);
        if (isAlternating() && naturalDegree > 4)
            return derivedSubgroup = this;
        return derivedSubgroup = commutator(this);
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
        AlgorithmsBase.rebase(bsgs, set, internalDegree);

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
                swTest, swTest, newBase, new InducedOrdering(newBase, internalDegree));

        return createPermutationGroupFromBSGS(asBSGSList(stabilizer));
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
     * Returns true if specified group is a subgroup of this.
     *
     * @param subgroup permutation group
     * @return true if specified group is a subgroup of this
     */
    public boolean containsSubgroup(PermutationGroup subgroup) {
        if (isTrivial())
            return subgroup.isTrivial();
        if (isSymmetric())
            return true;
        if (isAlternating()) {
            for (Permutation p : subgroup.generators())
                if (p.parity() == 1)
                    return false;
            return true;
        }
        if (subgroup.order().compareTo(order()) > 0)
            return false;
        return membershipTest(subgroup.generators());
    }


    /**
     * Returns a set of left coset representatives of a given subgroup in this group (by definition, left coset of
     * subgroup K have a form g*K); each representative is minimal in its coset under the ordering returned by
     * {@code this.ordering()}. The number of these representatives is equals to
     * {@code this.order().divide(subgroup.order())}.
     *
     * @param subgroup a subgroup of this group
     * @return set of left coset representatives
     * @see cc.redberry.core.groups.permutations.AlgorithmsBacktrack#leftCosetRepresentatives(java.util.List, java.util.List)
     */
    public Permutation[] leftCosetRepresentatives(PermutationGroup subgroup) {
        if (isTrivial())
            return new Permutation[]{Permutations.createIdentityPermutation()};
        //todo implement special cases for Alt and Sym
        return AlgorithmsBacktrack.leftCosetRepresentatives(getBSGS(), subgroup.getBSGS(), base(), ordering());
    }

    /**
     * Returns a set of right coset representatives of a given subgroup in this group (by definition, right coset of
     * subgroup K have a form K*g). The number of these representatives is equals to
     * {@code this.order().divide(subgroup.order())}. This method calculates left coset representatives
     * using {@link #leftCosetRepresentatives(PermutationGroup)} and inverse each representative. In contrast to
     * {@link #leftCosetRepresentatives(PermutationGroup)} each right coset representative is not necessary minimal in
     * its coset.
     *
     * @param subgroup a subgroup of this group
     * @return set of right coset representatives
     * @see #leftCosetRepresentatives(PermutationGroup)
     */
    public Permutation[] rightCosetRepresentatives(PermutationGroup subgroup) {
        final Permutation[] reps = leftCosetRepresentatives(subgroup);
        for (int i = 0; i < reps.length; ++i)
            reps[i] = reps[i].inverse();
        return reps;
    }

    /**
     * Returns a unique left coset representative of specified element; the returned representative will be
     * minimal in its coset under the ordering returned by {@code ordering()}.
     *
     * @param subgroup a subgroup of this group
     * @param element  some element of this group
     * @see cc.redberry.core.groups.permutations.AlgorithmsBacktrack#leftTransversalOf(Permutation, java.util.List, java.util.List)
     */
    public Permutation leftTransversalOf(PermutationGroup subgroup, Permutation element) {
        return AlgorithmsBacktrack.leftTransversalOf(element, getBSGS(), subgroup.getBSGS(), base(), ordering());
    }

    /**
     * Returns a union of this and specified group, i.e. group which is generated by union of generators of this and
     * specified group.
     *
     * @param group permutation group
     * @return union of this and specified group
     */
    public PermutationGroup union(PermutationGroup group) {
        if (this == group)
            return this;
        if (isTrivial())
            return group;
        if (group.isTrivial())
            return this;

        if (bsgs == null && group.bsgs != null)
            return group.union(generators());
        if (group.bsgs == null)
            return union(group.generators());

        if (containsSubgroup(group))
            return this;
        if (group.containsSubgroup(this))
            return group;

        int[] base = MathUtils.intSetUnion(getBase(), group.getBase());
        //new generators
        ArrayList<Permutation> generators = new ArrayList<>(generators());
        generators.addAll(group.generators());
        PermutationGroup r = createPermutationGroup(generators);
        r.base = base;
        return r;
    }

    /**
     * Returns an intersection of this group with specified group.
     *
     * @param subgroup permutation group
     * @return intersections of groups
     */
    public PermutationGroup intersection(PermutationGroup subgroup) {
        if (isTrivial())
            return this;
        if (subgroup.isTrivial())
            return subgroup;
        ArrayList<BSGSCandidateElement> intersection = new ArrayList<>();
        AlgorithmsBacktrack.intersection(getBSGS(), subgroup.getBSGS(), intersection);
        return createPermutationGroupFromBSGS(asBSGSList(intersection));
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
        //todo consider all cases (bsgs calculated or not)
        return createPermutationGroupFromBSGS(AlgorithmsBase.directProduct(getBSGS(), group.getBSGS()));
    }

    /**
     * Returns some permutation that maps point <i>from</i> onto point <i>to</i> or {@code null} if no such permutation exists.
     *
     * @param from from point
     * @param to   to point
     * @return some permutation that maps point <i>from</i> onto point <i>to</i> or {@code null} if no such permutation exists
     */
    public Permutation mapping(int from, int to) {
        if (positionsInOrbits[from] != positionsInOrbits[to])
            return null;

        List<BSGSElement> bsgs = getBSGS();
        if (bsgs.get(0).basePoint == from)
            return bsgs.get(0).getTransversalOf(to);
        for (int i = 0, size = bsgs.size(); i < size; ++i)
            if (bsgs.get(i).basePoint == from && bsgs.get(i).belongsToOrbit(to))
                return bsgs.get(i).getTransversalOf(to);
        ArrayList<BSGSCandidateElement> bsgs_c = asBSGSCandidatesList(bsgs);
        AlgorithmsBase.changeBasePointWithTranspositions(bsgs_c, 0, from, internalDegree);
        return bsgs_c.get(0).getTransversalOf(to);
    }

    /**
     * Returns an output port of permutations that preserves specified mapping between points. To be precise: for each
     * permutation <i>p</i> returned by {@link cc.redberry.core.groups.permutations.BacktrackSearch#take()} and for
     * all <i>i</i> &isin; {@code {0..from.length}}, it is guaranteed to be {@code p.newIndexOf(from[i]) == to[i]}.
     * <p><b>Example:</b></p>
     * The following code
     * <br>
     * <pre style="background:#f1f1f1;color:#000"><span style="color:#a08000"> Permutation</span> perm1 <span style="color:#2060a0">=</span> <span style="color:#2060a0">new</span> <span style="color:#a08000">PermutationOneLine</span>(<span style="color:#0080a0">8</span>, <span style="color:#2060a0">new</span> <span style="color:#a08000">int</span>[][]{{<span style="color:#0080a0">1</span>, <span style="color:#0080a0">2</span>, <span style="color:#0080a0">3</span>}});
     * <span style="color:#a08000">Permutation</span> perm2 <span style="color:#2060a0">=</span> <span style="color:#2060a0">new</span> <span style="color:#a08000">PermutationOneLine</span>(<span style="color:#0080a0">8</span>, <span style="color:#2060a0">new</span> <span style="color:#a08000">int</span>[][]{{<span style="color:#0080a0">3</span>, <span style="color:#0080a0">4</span>, <span style="color:#0080a0">5</span>, <span style="color:#0080a0">6</span>, <span style="color:#0080a0">7</span>}});
     * <span style="color:#a08000">PermutationGroup</span> pg <span style="color:#2060a0">=</span> <span style="color:#2060a0">new</span> <span style="color:#a08000">PermutationGroup</span>(perm1, perm2);
     * <span style="color:#a08000">BacktrackSearch</span> mappings <span style="color:#2060a0">=</span> pg<span style="color:#2060a0">.</span>mapping(<span style="color:#2060a0">new</span> <span style="color:#a08000">int</span>[]{<span style="color:#0080a0">7</span>, <span style="color:#0080a0">2</span>, <span style="color:#0080a0">1</span>, <span style="color:#0080a0">3</span>}, <span style="color:#2060a0">new</span> <span style="color:#a08000">int</span>[]{<span style="color:#0080a0">5</span>, <span style="color:#0080a0">3</span>, <span style="color:#0080a0">6</span>, <span style="color:#0080a0">1</span>});
     * <span style="color:#a08000">Permutation</span> perm;
     * <span style="color:#2060a0">while</span> ((perm <span style="color:#2060a0">=</span> mappings<span style="color:#2060a0">.</span>take()) <span style="color:#2060a0">!=</span> null)
     * <span style="color:#a08000">    System</span><span style="color:#2060a0">.</span>out<span style="color:#2060a0">.</span>println(perm);
     * </pre>
     * will produce 3 permutations (in cycles notation):
     * <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;+{{1, 6, 2, 3}, {5, 7}}
     * <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;+{{1, 6, 7, 5, 4, 2, 3}}
     * <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;+{{1, 6, 4, 7, 5, 2, 3}}
     *
     * @param from points <i>from</i>
     * @param to   points <i>to</i>
     * @return output port of permutations that preserves specified mapping between points
     * @throws IllegalArgumentException if {@code from.length != to.length}
     */
    public BacktrackSearch mapping(final int[] from, final int[] to) {
        if (from.length != to.length)
            throw new IllegalArgumentException("Length of from is not equal to length of to.");
        //for (int i = 0; i < from.length; ++i)
        //    if (positionsInOrbits[from[i]] != positionsInOrbits[to[i]])
        //        return EMPTY;

        final int[] _from_ = from.clone(), _to_ = to.clone();
        //make rebase as simple as possible
        ArraysUtils.quickSort(_from_, _to_, ordering());
        ArrayList<BSGSCandidateElement> bsgs = getBSGSCandidate();
        int newDegree = Math.max(internalDegree, Math.max(ArraysUtils.max(from) + 1, ArraysUtils.max(to) + 1));

        AlgorithmsBacktrack.rebaseWithRedundancy(bsgs, _from_, newDegree);

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

    /**
     * Returns an iterator over all elements in this group.
     *
     * @return iterator over all elements in this group
     */
    @Override
    public Iterator<Permutation> iterator() {
        ensureBSGSIsInitialized();
        if (naturalDegree == 0)
            return new SingleIterator<>(Permutations.createIdentityPermutation());
        return new PermIterator(); //new OutputPortUnsafe.PortIterator<>(new BacktrackSearch(bsgs));
    }

    /**
     * An iterator over all permutations in group
     */
    private final class PermIterator implements Iterator<Permutation> {
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
     * Computes centralizer of specified permutation.
     *
     * @param permutation permutation
     * @return centralizer of specified element
     */
    public PermutationGroup centralizerOf(final Permutation permutation) {
        return centralizerOf(createPermutationGroup(permutation));
    }

    /**
     * Computes centralizer of specified subgroup.
     *
     * @param subgroup a subgroup of this
     * @return centralizer of specified subgroup
     */
    public PermutationGroup centralizerOf(final PermutationGroup subgroup) {
        if (subgroup.isAbelian() && subgroup.isTransitive(0, naturalDegree))
            return subgroup;
        //todo special case for Sym(n)

        int[] base = getBase();
        if (subgroup.orbits.length != 1) {
            //find for a better base
            final IntComparator comparator = new IntComparator() {
                @Override
                public int compare(int a, int b) {
                    return -Integer.compare(subgroup.orbitSize(a), subgroup.orbitSize(b));
                }
            };
            ArraysUtils.quickSort(base, comparator);
        }

        final ArrayList<BSGSCandidateElement> group_bsgs = getBSGSCandidate();
        AlgorithmsBase.rebase(group_bsgs, base, internalDegree);

        final ArrayList<BSGSCandidateElement> subgroup_bsgs = subgroup.getBSGSCandidate();
        AlgorithmsBacktrack.rebaseWithRedundancy(subgroup_bsgs, base, internalDegree);

        final Permutation[] mappings = new Permutation[base.length - 1];
        for (int i = 1; i < base.length; ++i) {
            if (base[i] < subgroup.internalDegree && subgroup.positionsInOrbits[base[i]] == subgroup.positionsInOrbits[base[i - 1]])
                mappings[i - 1] = subgroup.mapping(base[i - 1], base[i]);
        }

        CentralizerSearchTest centralizerSearch = new CentralizerSearchTest(group_bsgs, subgroup, base, mappings);

        ArrayList<BSGSCandidateElement> centralizer;
        if (subgroup.generators().size() == 1)
            centralizer = AlgorithmsBase.clone(subgroup_bsgs);
        else
            centralizer = new ArrayList<>();
        AlgorithmsBacktrack.subgroupSearch(group_bsgs, centralizer, centralizerSearch, centralizerSearch);
        return createPermutationGroupFromBSGS(asBSGSList(centralizer));
    }

    /**
     * Backtrack search TEST for centralizer.
     */
    private static final class CentralizerSearchTest
            implements BacktrackSearchTestFunction, Indicator<Permutation> {
        final List<? extends BSGSElement> group_bsgs;
        final PermutationGroup subgroup;
        final Permutation[] mappings;
        final int[] group_base;


        private CentralizerSearchTest(List<? extends BSGSElement> group_bsgs,
                                      PermutationGroup subgroup,
                                      int[] group_base,
                                      Permutation[] mappings) {
            this.group_bsgs = group_bsgs;
            this.subgroup = subgroup;
            this.group_base = group_base;
            this.mappings = mappings;
        }


        @Override
        public boolean test(Permutation permutation, int level) {
            if (level == 0)
                return true;
            //find previous base point that lies in same orbit of subgroup
            if (subgroup.internalDegree < group_base[level - 1])
                if (group_base[level - 1] != group_base[level])
                    return true;

            if (subgroup.indexOfOrbit(group_base[level - 1]) != subgroup.indexOfOrbit(group_base[level]))
                return true;
            Permutation mapping = mappings[level - 1];
            int expected = mapping.newIndexOf(permutation.newIndexOf(group_base[level - 1]));
            return permutation.newIndexOf(group_base[level]) == expected;
        }

        @Override
        public boolean is(Permutation permutation) {
            if (permutation.isIdentity())
                return false;
            for (Permutation p : subgroup.generators())
                if (!p.commutator(permutation).isIdentity())
                    return false;
            return true;
        }

    }

    /**
     * Lazy center
     */
    private PermutationGroup center = null;

    /**
     * Computes center of this group.
     *
     * @return center of this group
     */
    public PermutationGroup center() {
        if (center == null) {
            if (isSymmetric() && naturalDegree >= 3)
                return center = createPermutationGroup(generators().get(0).getIdentity());
            if (isAlternating() && naturalDegree >= 4)
                return center = createPermutationGroup(generators().get(0).getIdentity());
            return center = centralizerOf(this);
        }
        return center;
    }

    /**
     * Returns the conjugate permutation group of {@code this} with the specified permutation (this ^ permutation).
     *
     * @param permutation some permutation
     * @return conjugate permutation group of {@code this} with the specified permutation
     */
    public PermutationGroup conjugate(Permutation permutation) {
        if (this.isTrivial())
            return this;
        if (bsgs == null) {
            ArrayList<Permutation> newGens = new ArrayList<>(generators().size());
            for (Permutation p : generators())
                newGens.add(permutation.conjugate(p));
            return createPermutationGroup(newGens);
        } else {
            List<BSGSElement> bsgs = getBSGS();
            ArrayList<BSGSElement> new_bsgs = new ArrayList<>(bsgs.size());
            for (BSGSElement e : bsgs) {
                ArrayList<Permutation> newStabs = new ArrayList<>(e.stabilizerGenerators.size());
                for (Permutation p : e.stabilizerGenerators)
                    newStabs.add(permutation.conjugate(p));
                new_bsgs.add(new BSGSCandidateElement(permutation.newIndexOf(e.basePoint), newStabs, new int[internalDegree]).asBSGSElement());
            }
            return createPermutationGroupFromBSGS(new_bsgs);
        }
    }

    /**
     * Returns true if specified group is equals to this group, i.e. it is isomorphic and acts same on the &Omega;(degree).
     *
     * @param obj permutation group
     * @return true if specified group has the same order and all its generators are contained in this group
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        PermutationGroup oth = (PermutationGroup) obj;
        if (naturalDegree != oth.naturalDegree)
            return false;

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
}
