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
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigInteger;

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class PermutationGroup implements Iterable<Permutation> {
    /**
     * Strong generating set
     */
    final SGSElement[] sgs;
    /**
     * Dimension of permutation group
     */
    final int length;

    /**
     * Creates identity permutation group
     *
     * @param length
     */
    public PermutationGroup(int length) {
        this.sgs = new SGSElement[0];
        this.length = length;
    }

    public PermutationGroup(Permutation... generators) {
        if (generators.length == 0)
            throw new IllegalArgumentException();
        this.length = generators[0].length();
        this.sgs = SchreierSimsAlgorithm.createSGS(generators);
    }

    public boolean isMember(Permutation permutation) {
        if (permutation.length() != length)
            throw new IllegalArgumentException();

        Permutation temp = permutation;
        int i, beta;
        for (i = 0; i < sgs.length; ++i) {
            beta = temp.newIndexOf(sgs[i].basePoint);
            if (!sgs[i].belongsToOrbit(beta))
                return false;
            temp = temp.composition(sgs[i].getInverseTransversalOf(beta));
        }
        return temp.isIdentity();
    }

    public BigInteger getOrder() {
        BigInteger order = BigInteger.ONE;
        for (SGSElement element : sgs)
            order = order.multiply(BigInteger.valueOf(element.getOrbitSize()));
        return order;
    }

    @Override
    public Iterator<Permutation> iterator() {
        if (sgs.length == 0)
            return Collections.singletonList(Combinatorics.createIdentity(length)).iterator();

        return new PermutationsIterator(sgs);
    }

    private static final class PermutationsIterator implements Iterator<Permutation> {
        final IntTuplesPort tuplePort;
        final SGSElement[] sgs;
        final Permutation[] partialProducts;

        private PermutationsIterator(SGSElement[] sgs) {
            this.sgs = sgs;
            this.partialProducts = new Permutation[sgs.length];

            //Creating tuple port
            int[] dims = new int[sgs.length];
            for (int i = sgs.length - 1; i >= 0; --i)
                dims[i] = sgs[i].getOrbitSize();
            tuplePort = new IntTuplesPort(dims);
        }

        @Override
        public boolean hasNext() {
            int[] orbitPointers = tuplePort.take();
            if (orbitPointers == null)
                return false;

            for (int i = tuplePort.getLastUpdateDepth(); i < orbitPointers.length; ++i)
                if (i == 0)
                    partialProducts[0] = sgs[0].getTransversalOf(sgs[0].getOrbitPoint(orbitPointers[0]));
                else
                    partialProducts[i] = sgs[i].getTransversalOf(sgs[i].getOrbitPoint(orbitPointers[i])).composition(
                            partialProducts[i - 1]);

            return true;
        }

        @Override
        public Permutation next() {
            return partialProducts[partialProducts.length - 1];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
