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
        //the algorithm SUBGROUPSEARCH described in Sec. 4.6.3 in [Holt05]

        //<= initialization
        final int degree = bsgs.get(0).groupDegree();

        final int[] base = AlgorithmsBase.getBaseAsArray(bsgs);
        final int size = bsgs.size();

        //we'll start from the end
        int level = size - 1;
        int subgroupLevel = level;

        //tuple[i] - current transversal of i-th base point
        final int[] tuple = new int[size];//initialized with zeros

        //rebase to base of basic group
        AlgorithmsBase.rebase(subgroup, base);
        //in the case when rebase removed redundant base points
        if (subgroup.size() < size)
            for (int i = subgroup.size(); i < size; ++i)
                subgroup.add(new BSGSCandidateElement(base[i], new ArrayList<Permutation>(), new int[degree]));

        ArrayList<BSGSCandidateElement> subgroup_rebase = AlgorithmsBase.clone(subgroup);

        final InducedOrdering ordering = new InducedOrdering(base);

        //cached sorted orbits of base points
        final int[][] cachedSortedOrbits = new int[size][];
        for (int i = bsgs.size() - 1; i >= 0; --i) {
            cachedSortedOrbits[i] = bsgs.get(i).orbitList.toArray();
            ArraysUtils.quickSort(cachedSortedOrbits[i], ordering);
        }

        final int[][] sortedOrbits = new int[size][];
        sortedOrbits[level] = cachedSortedOrbits[level];

        //sorted orbits of subgroup we search for with respect to base g(B(l)) generated at each tree level
        final int[][] sortedSubgroupOrbits = new int[size][];
        sortedSubgroupOrbits[level] = subgroup_rebase.get(level).orbitList.toArray();
        ArraysUtils.quickSort(sortedSubgroupOrbits[level], ordering);


        //max images to test (ii) from PROPOSITION 4.7
        final int[] maxImages = new int[size];//initialized with zeros
        //this need to test COROLLARY 4.8
        final int[] pivots = new int[size];
        pivots[level] = sortedOrbits[level][bsgs.get(level).orbitSize() - subgroup.get(level).orbitSize() + 2];

        //current tree branch
        final Permutation[] word = new Permutation[size];

        //<= initialized

        int image;
        while (true) {
            // at each vertex at fixed level we try to find a ≺-least double coset representative of KgK, where K - is a
            // subgroup we search for and g - word[level], i.e. all permutations that have fixed partial base image at
            // this level

            // in order to find ≺-least element in KgK, we'll try to prune tree using PROPOSITION 4.7 and COROLLARY 4.8
            // (note that if g is ≺-least element in KgK then it is guaranteed to be ≺-least element both in gK and Kg)

            //at each level we test our basic image to satisfy a double coset minimality
            for (image = word[level].newIndexOf(base[level]);
                 level < size - 1
                         // check PROPOSITION 4.7 (i)
                         //≺-least elements of subgroup orbits with respect to base g(B(l))
                         && subgroup_rebase.get(level).belongsToOrbit(image) //this is rather assertion then check
                         && isMinimal(sortedSubgroupOrbits[level], image, ordering)
                         // modified PROPOSITION 4.7 (ii)
                         && ordering.compare(image, maxImages[level]) > 0
                         // COROLLARY 4.8
                         && ordering.compare(image, pivots[level]) < 0
                         //test
                         && testFunction.test(word[level], level)
                    ; ) {
                //<= entering next level

                //rebase for next iteration
                AlgorithmsBase.changeBasePointWithTranspositions(subgroup_rebase, level, image);
                //remove redundant base point
                if (subgroup_rebase.size() > base.length) {
                    assert subgroup_rebase.get(base.length).stabilizerGenerators.isEmpty();
                    subgroup_rebase.remove(base.length);
                }

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
                pivots[level] = sortedOrbits[level][bsgs.get(level).orbitSize() - subgroup.get(level).orbitSize() + 2];

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
                    //≺-least elements of subgroup orbits with respect to base g(B(l))
                    && subgroup_rebase.get(level).belongsToOrbit(image) //this is rather assertion then check
                    && isMinimal(sortedSubgroupOrbits[level], image, ordering)
                    // modified PROPOSITION 4.7 (ii)
                    && ordering.compare(image, maxImages[level]) > 0
                    // COROLLARY 4.8
                    && ordering.compare(image, pivots[level]) < 0
                    //test
                    && testFunction.test(word[level], level)
                    //property
                    && property.is(word[level])) {

                //<= here we can extend our subgroup with new generator
                //extend group with a new generator
                subgroup.get(0).stabilizerGenerators.add(word[level]);
                AlgorithmsBase.SchreierSimsAlgorithm(subgroup);

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
                maxImages[level] = 0;
                pivots[level] = sortedOrbits[level][bsgs.get(level).orbitSize() - subgroup.get(level).orbitSize() + 2];

                ++tuple[level];
                word[level] =
                        bsgs.get(level).getTransversalOf(
                                word[level - 1].newIndexOfUnderInverse(sortedOrbits[level][tuple[level]])
                        ).composition(word[level - 1]);
            }

        }

    }

    private static boolean isMinimal(final int[] sortedOrbit, final int point, final IntComparator ordering) {
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
