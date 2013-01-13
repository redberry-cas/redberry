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
package cc.redberry.core.indexmapping;

import java.util.Map;

/**
 * Intermediate representation of mapping of indices needed inside the index mapping calculation pipeline (the basic
 * implementation idea could be found in the description of {@link IndexMappingProvider}).
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public interface IndexMappingBuffer {
    /**
     * Tries to put the mapping entry {@code from->to}
     *
     * @param from from index
     * @param to   to index
     * @return {@code true} if new mapping entry was created or {@code false} if this mapping is inconsistent with
     *         previously added (other mapping to -> from1, where from1 != from, exists)
     */
    boolean tryMap(int from, int to);

    /**
     * Multiplies the sign of this mapping on the specified one
     *
     * @param sign sign ({@code true} states '-' and {@code false} states '+')
     */
    void addSign(boolean sign);

    /**
     * Removes entries for contracted indices (like e.g. _i->_k, ^i->^k)
     */
    void removeContracted();

    /**
     * Returns whether this mapping is empty
     *
     * @return {@code true} if no entries in mapping exist
     */
    boolean isEmpty();

    /**
     * Returns sign of this mapping ({@code true} states '-' and {@code false} states '+')
     *
     * @return sign of this mapping ({@code true} states '-' and {@code false} states '+')
     */
    boolean getSign();

    /**
     * NOT PUBLIC A API
     *
     * @return NOT PUBLIC A API
     */
    FromToHolder export();

    /**
     * Returns the internal mapping container.
     *
     * @return the internal mapping container
     */
    //TODO TIntObjectHashMap ( YES issue #86 !)
    Map<Integer, IndexMappingBufferRecord> getMap();

    /**
     * Returns a deep copy of this mapping.
     *
     * @return a deep copy of this mapping
     */
    IndexMappingBuffer clone();
}
