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

import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigInteger;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class PermutationGroup {
    /**
     * Strong generating set
     */
    final SGSElement[] sgs;

    public PermutationGroup(Permutation[] generators) {
        this.sgs = SchreierSimsAlgorithm.createSGS(generators);
    }

    public boolean isMember(Permutation permutation) {
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

    public SGSElement[] getStrongGeneratingSet() {
        return sgs;
    }
}
