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
import cc.redberry.core.utils.IntComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic algorithms for backtrack search in permutation groups.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class AlgorithmsBacktrack {
    private AlgorithmsBacktrack() {
    }

    public static void subgroupSearch(List<? extends BSGSElement> bsgs,
                                      ArrayList<BSGSCandidateElement> subgroup,
                                      BacktrackSearchTestFunction testFunction,
                                      Indicator<Permutation> property) {
        if (bsgs.size() == 0 || bsgs.get(0).stabilizerGenerators.isEmpty())
            throw new IllegalArgumentException("Empty group.");

        /* The algorithm SUBGROUPSEARCH described in Sec. 4.6.3 in [Holt05] */

        //<= initialization

        final int degree = bsgs.get(0).groupDegree();
        final int[] base = AlgorithmsBase.getBaseAsArray(bsgs);
        final int size = bsgs.size();

        //induced ordering of base points
        final InducedOrdering ordering = new InducedOrdering(base);

        //we'll start from the end
        int level = size - 1;
        //current tree branch
        final Permutation[] word = new Permutation[size];
        final Permutation identity = bsgs.get(0).stabilizerGenerators.get(0).getIdentity();

        //cached sorted orbits of base points
        final int[][] cachedSortedOrbits = new int[size][];
        //sorted orbits images under word[l-1] (as used in general search)
        // at each level l, the sortedOrbit[l] is a sorted orbit of g(β_l) under the stabilizer
        // G_[g(β_1), g(β_2), ..., g(β_l-1)]
        final int[][] sortedOrbits = new int[size][];
        //initializing sorted orbits and word
        for (int i = 0; i < size; ++i) {
            cachedSortedOrbits[i] = bsgs.get(i).orbitList.toArray();
            ArraysUtils.quickSort(cachedSortedOrbits[i], ordering);
            sortedOrbits[i] = cachedSortedOrbits[i];
            word[i] = identity;   //initializing with identity
        }

        //tuple[i] - current transversal of i-th base point
        final int[] tuple = new int[size];//initialized with zeros
        //rebase to base of basic group
        rebaseWithRedundancy(subgroup, base, degree);

        int subgroupLevel = level;

        //<= data structure to test condition (i) in PROPOSITION 4.7
        // subgroup_rebase used to provide quick access to subgroup stabilizers of partial base images
        // i.e. at each level l and vertex V this BSGS is organized as follows: for each i ∈ 0...l i-th base point is
        // equal to new index of i-th base point in the initial base under word[l] obtained at vertex V
        // we'll use this information to test condition (i) in PROPOSITION 4.7
        ArrayList<BSGSCandidateElement> subgroup_rebase = AlgorithmsBase.clone(subgroup);
        // sorted orbits of subgroup_rebase needed to choose the minimal elements (we can have several minimal elements)
        // in each orbit
        final int[][] sortedSubgroupOrbits = new int[size][];
        sortedSubgroupOrbits[level] = subgroup_rebase.get(level).orbitList.toArray();
        ArraysUtils.quickSort(sortedSubgroupOrbits[level], ordering);
        //todo remove beta_f to avoid identities


        //<= data structure to test condition (ii) in PROPOSITION 4.7
        // this used to test that g(β_j) ≺ g(β_l) for all j < l such that β_l ∈ j-th basic orbit of subgroup
        // (PROOF: according to (ii) g(β_j) -- ≺-least in g(j-th Δ_K) => if g(β_l) ∈ g(j-th Δ_K) (<=> β_l ∈ j-th Δ_K)
        //  then g(β_j) ≺ g(β_l) )
        // maxImages[l] - is the ≺-greatest element of the set defined by { word[l](β_j) | β_l ∈ j-th Δ_K }
        final int[] maxImages = new int[size];//initialized with zeros
        maxImages[level] = ordering.minElement();//element smaller then all points
        // this used to test COROLLARY 4.8: if g -- ≺-least in Kg, then according to (ii) g(β_l) -- ≺-least in
        // g(l-th Δ_K) = { hg(β_l) |  h ∈ l-1 stabilizer of K w.r.t. to B}, but hg = g * (g^{-1} h g) and (g^{-1} h g)
        // ∈ stabilizer of [g(β_1), g(β_2), ..., g(β_l-1)] in G (recall g = word[l] - fixes partial base image), so
        // g(l-th Δ_K) lies in orbit of β_l under the stabilizer of [g(β_1), g(β_2), ..., g(β_l-1)] in G. Then this
        // orbit (orbit of β_l in G_[g(β_1), g(β_2), ..., g(β_l-1)]) contains at least |g(l-th Δ_K)|-1 points that
        // greater then g(β_l).
        // the above enables us to choose a point in this orbit that must be greater then g(β_l)
        final int[] maxRepresentative = new int[size];
        maxRepresentative[level] = maxRepresentative(sortedOrbits[level], subgroup.get(level).orbitSize(), ordering);

        //<= initialized

        int image;
        while (true) {
            // at each vertex at fixed level we try to find a ≺-least double coset representative of KgK, where K - is a
            // subgroup we search for and g - word[level], i.e. all permutations that have fixed partial base image at
            // this level

            // in order to find ≺-least element in KgK, we'll try to prune tree using PROPOSITION 4.7 and COROLLARY 4.8
            // (note that if g is ≺-least element in KgK then it is guaranteed to be ≺-least element both in gK and Kg)

            //at each level we test our basic image to satisfy required conditions for a double coset minimality
            for (image = word[level].newIndexOf(base[level]);
                 level < size - 1
                         //avoid getting identity
                         && image != base[level]
                         // check PROPOSITION 4.7 (i)
                         //≺-least element of subgroup orbits with respect to base g(B(l))
                         && isMinimalInOrbit(sortedSubgroupOrbits[level], image, ordering)
                         // modified PROPOSITION 4.7 (ii)
                         && ordering.compare(image, maxImages[level]) > 0
                         // COROLLARY 4.8
                         && ordering.compare(image, maxRepresentative[level]) < 0
                         //test
                         && testFunction.test(word[level], level)
                    ; ) {
                //<= entering next level

                //rebase for next iteration
                replaceBasePointWithRedundancy(subgroup_rebase, level, image);

                ++level;
                //recalculate subgroup sorted orbit
                sortedSubgroupOrbits[level] = subgroup_rebase.get(level).orbitList.toArray();
                ArraysUtils.quickSort(sortedSubgroupOrbits[level], ordering);
                //recalculate sorted orbit
                if (word[level - 1].isIdentity())
                    sortedOrbits[level] = cachedSortedOrbits[level];
                else {
                    sortedOrbits[level] = word[level - 1].imageOf(bsgs.get(level).orbitList.toArray());
                    ArraysUtils.quickSort(sortedOrbits[level], ordering);
                }

                //<= recalculate data needed to test (ii) from PROPOSITION 4.7
                //try to find those orbits that contain current image
                int max = -1;
                for (int j = 0; j < level; ++j) {
                    if (subgroup.get(j).belongsToOrbit(base[level])) {
                        if (max == -1)
                            max = word[j].newIndexOf(base[j]);
                        else max = ordering.max(max, word[j].newIndexOf(base[j]));
                    }
                }
                maxImages[level] = max;
                maxRepresentative[level] = maxRepresentative(sortedOrbits[level], subgroup.get(level).orbitSize(), ordering);

                //reset tuple and calculate next permutation
                tuple[level] = 0;
                word[level] =
                        bsgs.get(level).getTransversalOf(
                                word[level - 1].newIndexOfUnderInverse(sortedOrbits[level][tuple[level]])
                        ).composition(word[level - 1]);
            }

            //<= here we obtained next permutation in group
            //we need to test whether it belongs to subgroup
            if (level == size - 1
                    // check PROPOSITION 4.7 (i)
                    //avoid getting identity
                    && image != base[level]
                    //≺-least elements of subgroup orbits with respect to base g(B(l))
                    && isMinimalInOrbit(sortedSubgroupOrbits[level], image, ordering)
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
//                recalculate
//                AlgorithmsBase.SchreierSimsAlgorithm(subgroup);

                //rebase back to old base
                subgroup_rebase = AlgorithmsBase.clone(subgroup);
                //recalculate sorted subgroup orbits
                for (int i = bsgs.size() - 1; i >= 0; --i) {
                    sortedSubgroupOrbits[i] = subgroup_rebase.get(i).orbitList.toArray();
                    ArraysUtils.quickSort(sortedSubgroupOrbits[i], ordering);
                }
            }

            //<= now we need to go down the tree
            while (level >= 0 && tuple[level] == bsgs.get(level).orbitList.size() - 1)
                --level;

            if (level == -1)
                return; //all elements scanned

            if (level < subgroupLevel) {
                subgroupLevel = level;
                tuple[level] = 0;
                sortedSubgroupOrbits[subgroupLevel] = subgroup_rebase.get(subgroupLevel).orbitList.toArray();
                ArraysUtils.quickSort(sortedSubgroupOrbits[subgroupLevel], ordering);

                //<= recalculate data needed to test (ii) from PROPOSITION 4.7
                maxImages[level] = -1;
                maxRepresentative[level] = maxRepresentative(sortedOrbits[level], subgroup.get(level).orbitSize(), ordering);
            }

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

    private static int maxRepresentative(final int[] sortedOrbit, final int subgroupOrbitSize,
                                         final InducedOrdering ordering) {
        int temp = sortedOrbit.length - subgroupOrbitSize + 1;
        if (temp >= sortedOrbit.length)
            return ordering.maxElement();
        return sortedOrbit[temp];
    }

    private static void rebaseWithRedundancy(final ArrayList<BSGSCandidateElement> group,
                                             final int[] base, final int degree) {
        AlgorithmsBase.rebase(group, base);
        if (group.size() < base.length)
            for (int i = group.size(); i < base.length; ++i)
                group.add(new BSGSCandidateElement(base[i], new ArrayList<Permutation>(), new int[degree]));
    }

    private static void replaceBasePointWithRedundancy(final ArrayList<BSGSCandidateElement> group,
                                                       final int index, final int newPoint) {
        if (group.get(index).basePoint == newPoint)
            return;
        int oldSize = group.size();
        AlgorithmsBase.changeBasePointWithTranspositions(group, index, newPoint);
        //keep bsgs with a fixed size
        assert group.size() >= oldSize;
        if (group.size() != oldSize) {
            assert group.get(oldSize).stabilizerGenerators.isEmpty();
            group.remove(oldSize);
        }
    }

    private static boolean isMinimalInOrbit(final int[] sortedOrbit, final int point, final IntComparator ordering) {
        int compare;
        for (int i = 0; i < sortedOrbit.length; ++i) {
            compare = ordering.compare(sortedOrbit[i], point);
            if (compare < 0)
                return false;
            if (compare == 0)
                return true;
        }
        throw new RuntimeException("This should never happen.");
    }


}
