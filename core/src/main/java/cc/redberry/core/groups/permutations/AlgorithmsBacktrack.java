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

import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.IntArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Basic algorithms for backtrack search in permutation groups.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class AlgorithmsBacktrack {
    private AlgorithmsBacktrack() {
    }

    //TODO: remove after all tests
    public static long[] ____VISITED_NODES___ = {0};

    /**
     * The algorithm performs subgroup search in specified group. If a nonempty initial {@code subgroup} was provided,
     * then it will be extended to the subgroup which we search for. Implementation issues can be found in Sec. 4.6.3
     * in [Holt05].
     *
     * @param bsgs         base and strong generating set of group
     * @param subgroup     initial base and strong generating set of subgroup for which we perform search
     * @param testFunction test function that applies at each level of search tree
     * @param property     property of subgroup elements
     * @see BacktrackSearch
     */
    public static void subgroupSearch(final List<? extends BSGSElement> bsgs,
                                      final ArrayList<BSGSCandidateElement> subgroup,
                                      final BacktrackSearchTestFunction testFunction,
                                      final Indicator<Permutation> property) {
        if (bsgs.size() == 0 || bsgs.get(0).stabilizerGenerators.isEmpty())
            throw new IllegalArgumentException("Empty group.");

        ____VISITED_NODES___[0] = 0;//just for performance debugging


        /* The algorithm SUBGROUPSEARCH described in Sec. 4.6.3 in [Holt05] */

        //<= initialization

        final int degree = bsgs.get(0).degree();
        final int[] base = AlgorithmsBase.getBaseAsArray(bsgs);
        final int size = bsgs.size();

        if (subgroup.isEmpty())//if subgroup was empty
            subgroup.add(new BSGSCandidateElement(base[0], new ArrayList<Permutation>(), new int[degree]));

        //induced ordering of base points
        final InducedOrdering ordering = new InducedOrdering(base, degree);

        //we'll start from the end
        int level = size - 1;
        //current tree branch
        final Permutation[] word = new Permutation[size];
        final Permutation identity = bsgs.get(0).stabilizerGenerators.get(0).getIdentity();

        //cached sorted orbits of base points
        final int[][] cachedSortedOrbits = new int[size][];
        //Sorted orbits images under word[l-1] (as used in general search):
        // at each level l, sortedOrbit[l] is a sorted orbit of g(β_l) under the stabilizer
        // G_[g(β_1), g(β_2), ..., g(β_l-1)].
        final int[][] sortedOrbits = new int[size][];
        //initializing sorted orbits and word
        for (int i = 0; i < size; ++i) {
            cachedSortedOrbits[i] = bsgs.get(i).orbitList.toArray();
            ArraysUtils.quickSort(cachedSortedOrbits[i], ordering);
            sortedOrbits[i] = cachedSortedOrbits[i];
            word[i] = identity;   //initializing with identity
        }

        //tuple[l] - current transversal of l-th base point, i.e. position of vertex at level l in search tree
        final int[] tuple = new int[size];//initialized with zeros!
        //rebase to base of basic group
        rebaseWithRedundancy(subgroup, base, degree);

        //subgroupLevel is a level in a search tree at each we are looking for subgroup representatives.
        //For each vertex at current subgroupLevel we need to find only one subgroup representative,
        // if we've found a new subgroup representative at level > subgroupLevel, then we can skip all the rest elements
        // in this tree branch by setting level = subgroupLevel.
        int subgroupLevel = level;

        //<= data structure to test condition (i) in PROPOSITION 4.7
        //subgroup_rebase used to provide a quick access to subgroup stabilizers of partial base images,
        // i.e. at each level l and vertex V this BSGS is organized as follows: for each i ∈ 0...l i-th base point is
        // equal to new index of i-th base point in the initial base under word[l] obtained at vertex V.
        // We'll use this information to test condition (i) in PROPOSITION 4.7.
        ArrayList<BSGSCandidateElement> subgroup_rebase = AlgorithmsBase.clone(subgroup);
        //todo remove beta_f to avoid identities (and how?)


        //<= data structure to test condition (ii) in PROPOSITION 4.7
        //This used to test that g(β_j) ≺ g(β_l) for all j < l such that β_l ∈ j-th basic orbit of subgroup
        // (PROOF: according to (ii) g(β_j) -- ≺-least in g(j-th Δ_K) => if g(β_l) ∈ g(j-th Δ_K) (<=> β_l ∈ j-th Δ_K)
        //  then g(β_j) ≺ g(β_l) ).
        // maxImages[l] - is the ≺-greatest element of the set defined by { word[l](β_j) | β_l ∈ j-th Δ_K }
        final int[] maxImages = new int[size];//initialized with zeros
        maxImages[level] = ordering.minElement();//element smaller then all points
        //This used to test COROLLARY 4.8: if g -- ≺-least in Kg, then according to (ii) g(β_l) -- ≺-least in
        // g(l-th Δ_K) = { hg(β_l) |  h ∈ l-1 stabilizer of K w.r.t. to B}, but hg = g * (g^{-1} h g) and (g^{-1} h g)
        // ∈ stabilizer of [g(β_1), g(β_2), ..., g(β_l-1)] in G (recall g = word[l] - fixes partial base image), so
        // g(l-th Δ_K) lies in orbit of g(β_l) under the stabilizer of [g(β_1), g(β_2), ..., g(β_l-1)] in G. Then this
        // orbit (orbit of g(β_l) in G_[g(β_1), g(β_2), ..., g(β_l-1)]) contains at least |g(l-th Δ_K)|-1 points that
        // greater then g(β_l).
        // The above enables us to choose a point in this orbit that must be greater then g(β_l)
        final int[] maxRepresentative = new int[size];
        maxRepresentative[level] = maxRepresentative(sortedOrbits[level], subgroup.get(level).orbitSize(), ordering);

        //<= initialized

        int image;
        while (true) {
            //At each vertex at fixed level we try to find a ≺-least double coset representative of KgK, where K - is a
            // subgroup we search for and g - word[level], i.e. all permutations that have fixed partial base image at
            // this level.
            //In order to find ≺-least element in KgK, we'll try to prune tree using PROPOSITION 4.7 and COROLLARY 4.8
            // (note that if g is ≺-least element in KgK then it is guaranteed to be ≺-least element both in gK and Kg).

            //at each level we test our basic image to satisfy required conditions for a double coset minimality

            image = word[level].newIndexOf(base[level]);
            //Calculating the orbit of g(β_l) under stabilizer of [g(β_1), g(β_1),...g(β_l-1)] which will be used in
            // the first iteration (the line with "isMinimalInOrbit(subgroup_rebase.get(level).orbitList, image,
            // ordering)").
            replaceBasePointWithRedundancy(subgroup_rebase, level, image);
            while (level < size - 1
                    // check PROPOSITION 4.7 (i)
                    //≺-least element of subgroup orbits with respect to base g(B(l))
                    && isMinimalInOrbit(subgroup_rebase.get(level).orbitList, image, ordering)
                    // modified PROPOSITION 4.7 (ii)
                    && ordering.compare(image, maxImages[level]) > 0
                    // COROLLARY 4.8
                    && ordering.compare(image, maxRepresentative[level]) < 0
                    //test
                    && testFunction.test(word[level], level)) {
                //<= entering next level
                ++level;

                //recalculate sorted orbit
                if (word[level - 1].isIdentity())
                    sortedOrbits[level] = cachedSortedOrbits[level];
                else {
                    sortedOrbits[level] = word[level - 1].imageOf(bsgs.get(level).orbitList.toArray());
                    ArraysUtils.quickSort(sortedOrbits[level], ordering);
                }

                //<= recalculate data needed to test (ii) from PROPOSITION 4.7
                //try to find those orbits that contain current image
                int max = ordering.minElement();
                for (int j = 0; j < level; ++j)
                    if (subgroup.get(j).belongsToOrbit(base[level]))
                        max = ordering.max(max, word[j].newIndexOf(base[j]));

                maxImages[level] = max;
                maxRepresentative[level] = maxRepresentative(sortedOrbits[level], subgroup.get(level).orbitSize(), ordering);

                //reset tuple and calculate next permutation
                tuple[level] = 0;
                word[level] =
                        bsgs.get(level).getTransversalOf(
                                word[level - 1].newIndexOfUnderInverse(sortedOrbits[level][tuple[level]])
                        ).composition(word[level - 1]);


                image = word[level].newIndexOf(base[level]);
                //calculating the orbit of g(β_l) under stabilizer of [g(β_1), g(β_1),...g(β_l-1)] which will be used in
                //the next iteration cycle (the line with "isMinimalInOrbit(subgroup_rebase.get(level).orbitList, image, ordering)")
                replaceBasePointWithRedundancy(subgroup_rebase, level, image);

                //NOTE: I suppose there is a mistake at line 12 in SUBGROUPSEARCH in [Holt05], since we cannot
                // calculate minimal orbit representative R[l+1] before we have calculated next u_l (in line 16). In
                // order to fix it, we calculate this representatives after we've found next u_l.
            }

            ++____VISITED_NODES___[0];
            //<= here we obtained next permutation in group
            //we need to test whether it belongs to subgroup
            if (level == size - 1
                    // check PROPOSITION 4.7 (i)
                    //≺-least element of subgroup orbits with respect to base g(B(l))
                    && isMinimalInOrbit(subgroup_rebase.get(level).orbitList, image, ordering)
                    // modified PROPOSITION 4.7 (ii)
                    && ordering.compare(image, maxImages[level]) > 0
                    // COROLLARY 4.8
                    && ordering.compare(image, maxRepresentative[level]) < 0
                    //test
                    && testFunction.test(word[level], level)
                    //property
                    && property.is(word[level])) {

                //<= here we obtained next permutation in group that is a new generator in the subgroup we search for
                //extend group with a new generator
                subgroup.get(0).stabilizerGenerators.add(word[level]);
                subgroup.get(0).recalculateOrbitAndSchreierVector();
                //recalculate subgroup
                AlgorithmsBase.SchreierSimsAlgorithm(subgroup);

                //dump subgroup_rebase
                subgroup_rebase = AlgorithmsBase.clone(subgroup);

                //<= we've found new subgroup generator and we can skip all other elements in current branch:
                level = subgroupLevel;
            }

            //<= now we need to go down the tree
            while (level >= 0 && tuple[level] == bsgs.get(level).orbitList.size() - 1)
                --level;

            if (level == -1)
                return; //all elements scanned

            if (level < subgroupLevel) {
                //setup new subgroupLevel
                subgroupLevel = level;
                tuple[level] = 0;
                //<= recalculate data needed to test (ii) from PROPOSITION 4.7
                maxImages[level] = ordering.minElement();
                maxRepresentative[level] = maxRepresentative(sortedOrbits[level], subgroup.get(level).orbitSize(), ordering);
            }

            //<= next vertex at current level
            ++tuple[level];
            if (level == 0)
                word[0] = bsgs.get(0).getTransversalOf(sortedOrbits[0][tuple[0]]);
            else
                word[level] =
                        bsgs.get(level).getTransversalOf(
                                word[level - 1].newIndexOfUnderInverse(sortedOrbits[level][tuple[level]])
                        ).composition(word[level - 1]);
        }

    }

    /**
     * For COROLLARY 4.8: Returns a element greater then g(β_l) in group orbit, or ordering.maxElement() if
     * subgroup orbit is not yet initialized (subgroupOrbitSize <= 1).
     *
     * @param sortedOrbit       sorted orbit of group
     * @param subgroupOrbitSize size of a subgroup
     * @param ordering          ordering
     * @return
     */
    private static int maxRepresentative(final int[] sortedOrbit, final int subgroupOrbitSize,
                                         final InducedOrdering ordering) {
        if (subgroupOrbitSize <= 1)
            return ordering.maxElement();
        return sortedOrbit[sortedOrbit.length - subgroupOrbitSize + 1];
    }

    /**
     * Changes base keeping redundant remnant points.
     *
     * @param group  group
     * @param base   new base
     * @param degree group degree
     */
    private static void rebaseWithRedundancy(final ArrayList<BSGSCandidateElement> group,
                                             final int[] base, final int degree) {
        AlgorithmsBase.rebase(group, base);
        if (group.size() < base.length)
            for (int i = group.size(); i < base.length; ++i)
                group.add(new BSGSCandidateElement(base[i], new ArrayList<Permutation>(), new int[degree]));
    }

    /**
     * For PROPOSITION 4.7 (i): Returns true if specified point belongs to specified orbit and is minimal according to
     * specified ordering.
     *
     * @param orbit    subgroup orbit
     * @param point    point
     * @param ordering ordering
     * @return true if specified point belongs to specified orbit and is minimal according to
     *         specified ordering
     */
    private static boolean isMinimalInOrbit(IntArrayList orbit, int point, InducedOrdering ordering) {
        boolean belongsToOrbit = false;
        int compare;
        for (int i = orbit.size() - 1; i >= 0; --i) {
            if ((compare = ordering.compare(orbit.get(i), point)) < 0)
                return false;
            if (compare == 0)
                belongsToOrbit = true;
        }
        return belongsToOrbit;
    }

    /**
     * Changes single base point keeping redundant remnant points.
     *
     * @param group    group
     * @param index    position of point to change
     * @param newPoint new point
     */
    private static void replaceBasePointWithRedundancy(final ArrayList<BSGSCandidateElement> group,
                                                       final int index, final int newPoint) {
        if (group.get(index).basePoint == newPoint)
            return;

        int oldSize = group.size();

        /*//replace with conjugation
        if (group.get(index).belongsToOrbit(newPoint)) {
            //calculate conjugation
            Permutation conjugation = group.get(index).getTransversalOf(newPoint);
            int image;
            for (int i = index + 1; i < group.size(); ++i) {
                image = conjugation.newIndexOfUnderInverse(group.get(i).basePoint);
                if (group.get(i).belongsToOrbit(image))
                    conjugation = group.get(i).getTransversalOf(image).composition(conjugation);
                else
                    AlgorithmsBase.changeBasePointWithTranspositions(group, i, image);
            }

            Permutation inverse = conjugation.inverse();
            ListIterator<BSGSCandidateElement> elementsIterator = group.listIterator(index);
            int degree = group.get(0).degree();
            while (elementsIterator.hasNext()) {
                BSGSCandidateElement element = elementsIterator.next();
                //conjugating stabilizers
                ArrayList<Permutation> newStabilizers = new ArrayList<>(element.stabilizerGenerators.size());
                for (Permutation oldStabilizer : element.stabilizerGenerators)
                    newStabilizers.add(inverse.composition((oldStabilizer.composition(conjugation))));

                //conjugating base point
                int newBasePoint = conjugation.newIndexOf(element.basePoint);
                elementsIterator.set(
                        new BSGSCandidateElement(newBasePoint, newStabilizers, new int[degree]));
            }
        } else //replace with transpositions
            AlgorithmsBase.changeBasePointWithTranspositions(group, index, newPoint);*/

        //replace with transpositions
        AlgorithmsBase.changeBasePointWithTranspositions(group, index, newPoint);

        //keep bsgs with a fixed size
        assert group.size() >= oldSize;

        //remove redundant points for performance
        while (group.size() > oldSize) {
            if (group.get(group.size() - 1).stabilizerGenerators.isEmpty())
                group.remove(group.size() - 1);
            else break;
        }
    }

}
