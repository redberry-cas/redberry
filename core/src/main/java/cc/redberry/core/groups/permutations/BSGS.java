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

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

/**
 * This class is a container for base and strong generating set of a group.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see BSGSFactory
 */
public final class BSGS {
    public static final BSGS EMPTY = new BSGS(Collections.EMPTY_LIST);

    final List<BSGSElement> BSGSList;
    final int[] base;

    BSGS(List<BSGSElement> BSGSList) {
        this.BSGSList = Collections.unmodifiableList(BSGSList);
        this.base = BSGSFactory.getBaseAsArray(BSGSList);
    }

    /**
     * Returns unmodifiable list of _BSGS elements
     *
     * @return unmodifiable list of _BSGS elements
     */
    public List<BSGSElement> getBSGSList() {
        return BSGSList;
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
     * Returns whether the specified permutation is member of group represented by this _BSGS
     *
     * @param permutation permutation
     * @return true if specified permutation is member of group represented by this _BSGS
     */
    public boolean isMember(Permutation permutation) {
        BSGSFactory.StripContainer container = BSGSFactory.strip(BSGSList, permutation);
        return container.terminationLevel == BSGSList.size() && container.remainder.isIdentity();
    }

    /**
     * Returns the number of permutations in group represented by this _BSGS
     *
     * @return number of permutations in group represented by this _BSGS
     */
    public BigInteger order() {
        BigInteger order = BigInteger.ONE;
        for (BSGSElement element : BSGSList)
            order = order.multiply(BigInteger.valueOf(element.orbitSize()));
        return order;
    }
}
