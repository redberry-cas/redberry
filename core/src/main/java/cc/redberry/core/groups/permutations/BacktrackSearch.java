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

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.IntComparator;

import java.util.Arrays;
import java.util.List;

/**
 * An iterator (organized as output port {@link OutputPortUnsafe}) over group elements, that scans group in increasing
 * order of base images; to be precise: if base B = [b1, b2, b3,..,bn], then element g <i>which maps all base points
 * into themselves</i> (g(b) ∈ B for each b ∈ B) is guaranteed to precedes (≺) an element h, which base image succeeds
 * (≻) the base image of g according to ordering specified by {@link InducedOrdering}.
 * <p/>
 * <b>Pruning the tree:</b>
 * <br>The iteration is organized as a depth-first search in the search tree of specified permutation group. This search
 * tree is organized as follows: each vertex <i>V</i> on a particular level <i>l</i> specifies some partial base image
 * <i>B(l)</i>, this means that all permutations produced during iteration over child nodes of vertex <i>V</i> have
 * same partial base images (for all g and <i>i ∈ 0..l</i>, g(B(i)) is fixed). If we want to iterate over elements that
 * satisfy some property <i>P</i> for which we have a test function that guarantees false answer using the knowledge
 * of partial base image, we can rule out (prune tree) all element under the level <i>l</i>. The property <i>P</i> and
 * the corresponding test function can be changed via {@link #setTestFunction(BacktrackSearchTestFunction)}  and
 * {@link #setProperty(cc.redberry.core.utils.Indicator)} during iteration. If {@link BacktrackSearchTestFunction} was
 * specified, then it is guaranteed that all unnecessary tree branches will be ruled out.
 * <p/>
 * If property or test function were specified, then the iterator will search only those elements that satisfies
 * specified conditions.
 * <br><b>NOTE:</b> {@link BacktrackSearchTestFunction} should be consistent with the base of permutation group.
 * <p/>
 * The main algorithm used in this
 * implementation is an iterator-like modification of PRINTELEMENTS and GENERALSEARCH described in Sec. 4.6.1 and 4.6.2
 * in [Holt05].
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see BacktrackSearchTestFunction
 */
public class BacktrackSearch implements OutputPortUnsafe<Permutation> {
    final List<? extends BSGSElement> bsgs;
    //tuple[i] - current transversal of i-th base point
    final int[] tuple;
    //orbit images; sortedOrbits[i] - is an i-th orbit image under permutation word
    //u_{i_indexInBase}*u_{i_{indexInBase - 1}}*...*u_{i_0}, where each i_{j} = tuple[i]
    int[][] sortedOrbits;
    //last first changed position in tuple
    //(the image of first <i>level</i> points is fixed)
    int level = 0;
    //permutation word
    Permutation[] word;
    //bsgs size
    final int size;
    //comparator
    final IntComparator ordering;
    //sorted orbits
    final int[][] cachedSortedOrbits;

    //test condition
    BacktrackSearchTestFunction testFunction;
    //permutation property
    Indicator<Permutation> property;

    /**
     * Creates an iterator over group elements that satisfy specified property.
     *
     * @param bsgs     base and strong generating set
     * @param test     test function that applies at each level of search tree
     * @param property property of permutations
     */
    public BacktrackSearch(List<? extends BSGSElement> bsgs,
                           BacktrackSearchTestFunction test,
                           Indicator<Permutation> property) {
        if (bsgs.size() == 0)
            throw new IllegalArgumentException("Empty BSGS.");

        this.bsgs = bsgs;
        this.size = bsgs.size();
        this.tuple = new int[size];
        Arrays.fill(tuple, -1);
        //comparator of points in Ω(n)
        this.ordering = new InducedOrdering(AlgorithmsBase.getBaseAsArray(bsgs));
        //permutation word
        this.word = new Permutation[bsgs.size()];
        this.sortedOrbits = new int[bsgs.size()][];
        //cached sorted orbits of base points
        this.cachedSortedOrbits = new int[bsgs.size()][];
        for (int i = bsgs.size() - 1; i >= 0; --i) {
            this.cachedSortedOrbits[i] = bsgs.get(i).orbitList.toArray();
            ArraysUtils.quickSort(this.cachedSortedOrbits[i], this.ordering);
        }
        this.sortedOrbits[0] = cachedSortedOrbits[0];
        this.testFunction = test;
        this.property = property;
    }

    /**
     * Creates an iterator over group elements.
     *
     * @param bsgs base and strong generating set
     */
    public BacktrackSearch(List<BSGSElement> bsgs) {
        this(bsgs, BacktrackSearchTestFunction.TRUE, Indicator.TRUE_INDICATOR);
    }


    /**
     * Returns the test function
     *
     * @return test function
     */
    public BacktrackSearchTestFunction getTestFunction() {
        return testFunction;
    }

    /**
     * Sets the test function used to rule out unnecessary tree branches during search. The specified test
     * function should be consistent with the base of current permutation group.
     *
     * @param test test function
     */
    public void setTestFunction(BacktrackSearchTestFunction test) {
        this.testFunction = test;
    }

    /**
     * Returns the property of elements that we are search in group
     *
     * @return property of elements that we are search in group
     */
    public Indicator<Permutation> getProperty() {
        return property;
    }

    /**
     * Sets the property of elements that we search in group
     *
     * @param property property of permutations which we search in group
     */
    public void setProperty(Indicator<Permutation> property) {
        this.property = property;
    }

    /**
     * Returns the ordering on Ω(n) induced by a base.
     *
     * @return ordering on Ω(n) induced by a base
     * @see InducedOrdering
     */
    public IntComparator getInducedOrdering() {
        return ordering;
    }

    /**
     * Returns level of the last changed element.
     *
     * @return level of the last changed element
     */
    public int lastModifiedLevel() {
        return level;
    }

    /**
     * Returns reference on current permutation word.
     *
     * @return reference on current permutation word
     */
    public Permutation[] getWordReference() {
        return word;
    }

    /**
     * Searches and returns the next element in group.
     *
     * @return next element in group
     */
    @Override
    public Permutation take() {
        if (level == -1)
            return null;

        while (true) {
            backtrack();

            if (level == -1)
                return null;

            while (level < size - 1 && testFunction.test(word[level], level)) {
                ++level;
                calculateSortedOrbit(level);
                tuple[level] = 0;
                word[level] =
                        bsgs.get(level).getTransversalOf(
                                word[level - 1].newIndexOfUnderInverse(sortedOrbits[level][tuple[level]])
                        ).composition(word[level - 1]);
            }

            if (level != size - 1 || !testFunction.test(word[level], level) || !property.is(word[level]))
                continue;

            return word[level];
        }
    }

    private void backtrack() {
        while (level >= 0 && tuple[level] == bsgs.get(level).orbitList.size() - 1)
            --level;

        if (level == -1)
            return;

        ++tuple[level];

        if (level == 0)
            word[0] = bsgs.get(0).getTransversalOf(sortedOrbits[0][tuple[0]]);
        else
            word[level] =
                    bsgs.get(level).getTransversalOf(
                            word[level - 1].newIndexOfUnderInverse(sortedOrbits[level][tuple[level]])
                    ).composition(word[level - 1]);
    }

    private void calculateSortedOrbit(int level) {
        if (word[level - 1].isIdentity())
            sortedOrbits[level] = cachedSortedOrbits[level];
        else {
            sortedOrbits[level] = word[level - 1].imageOf(bsgs.get(level).orbitList.toArray());
            ArraysUtils.quickSort(sortedOrbits[level], ordering);
        }
    }
}