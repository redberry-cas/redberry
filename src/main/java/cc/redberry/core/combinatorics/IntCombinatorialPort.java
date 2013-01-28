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
package cc.redberry.core.combinatorics;

import cc.redberry.concurrent.OutputPortUnsafe;

/**
 * This interface if common for all combinatorial iterators.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public interface IntCombinatorialPort extends OutputPortUnsafe<int[]> {
    /**
     * Resets the iteration
     */
    void reset();

    /**
     * Returns the reference on the current iteration element.
     *
     * @return the reference on the current iteration element
     */
    int[] getReference();

    /**
     * Calculates and returns the next combination or null, if no more combinations exist.
     *
     * @return the next combination or null, if no more combinations exist
     */
    @Override
    int[] take();
}
