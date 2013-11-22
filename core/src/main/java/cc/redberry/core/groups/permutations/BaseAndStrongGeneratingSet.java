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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class is a container for base and strong generating set of a group.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see AlgorithmsBase
 */
public final class BaseAndStrongGeneratingSet implements Iterable<Permutation> {
    public static final BaseAndStrongGeneratingSet EMPTY = new BaseAndStrongGeneratingSet(Collections.EMPTY_LIST);

    final List<BSGSElement> BSGSList;
    final int[] base;

    BaseAndStrongGeneratingSet(List<BSGSElement> BSGSList) {
        this.BSGSList = Collections.unmodifiableList(BSGSList);
        this.base = AlgorithmsBase.getBaseAsArray(BSGSList);
    }

    /**
     * Returns unmodifiable list of BSGS elements
     *
     * @return unmodifiable list of BSGS elements
     */
    public List<BSGSElement> getBSGSList() {
        return BSGSList;
    }

    /**
     * Returns unmodifiable list of BSGS elements
     *
     * @return unmodifiable list of BSGS elements
     */
    public ArrayList<BSGSCandidateElement> getBSGSCandidateList() {
        return AlgorithmsBase.asBSGSCandidatesList(BSGSList);
    }

    /**
     * Returns array of base points.
     *
     * @return array of base points
     */
    public int[] getBaseArray() {
        return base;
    }

    /**
     * Returns whether the specified permutation is member of group represented by this BSGS
     *
     * @param permutation permutation
     * @return true if specified permutation is member of group represented by this BSGS
     */
    public boolean isMember(Permutation permutation) {
        AlgorithmsBase.StripContainer container = AlgorithmsBase.strip(BSGSList, permutation);
        return container.terminationLevel == BSGSList.size() && container.remainder.isIdentity();
    }

    /**
     * Returns the number of permutations in group represented by this BSGS
     *
     * @return number of permutations in group represented by this BSGS
     */
    public BigInteger order() {
        BigInteger order = BigInteger.ONE;
        for (BSGSElement element : BSGSList)
            order = order.multiply(BigInteger.valueOf(element.orbitSize()));
        return order;
    }

    @Override
    public Iterator<Permutation> iterator() {
        return new PermIterator();
    }

    /**
     * An iterator over all permutations in group
     */
    private class PermIterator implements Iterator<Permutation> {
        private final IntTuplesPort tuplesPort;
        int[] tuple;

        private PermIterator() {
            final int[] orbitSizes = new int[BSGSList.size()];
            for (int i = 0; i < orbitSizes.length; ++i)
                orbitSizes[i] = BSGSList.get(i).orbitSize();
            tuplesPort = new IntTuplesPort(orbitSizes);
            tuple = tuplesPort.take();
        }

        @Override
        public boolean hasNext() {
            return tuple != null;
        }

        @Override
        public Permutation next() {
            Permutation p = BSGSList.get(0).getInverseTransversalOf(BSGSList.get(0).getOrbitPoint(tuple[0]));
            BSGSElement e;
            for (int i = 1, size = BSGSList.size(); i < size; ++i) {
                e = BSGSList.get(i);
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
}
