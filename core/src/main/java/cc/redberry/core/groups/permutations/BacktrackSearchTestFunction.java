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

/**
 * Test function which can be applied at each level of search tree; if if is not applicable at some level then it
 * must return true. See Sec. 4.6.2 in [Holt05] for details.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see BacktrackSearch
 */
public interface BacktrackSearchTestFunction {
    /**
     * Tests some permutation at specified level. Specified permutation fixes partial base image (images of all base
     * points with index <= level); if test return false, then no any more permutations with same partial base image
     * will be scanned in search tree.
     *
     * @param permutation permutation
     * @param level       level in search tree
     * @return the result of test
     */
    boolean test(Permutation permutation, int level);

    /**
     * Always returns true
     */
    public static BacktrackSearchTestFunction TRUE = new BacktrackSearchTestFunction() {
        @Override
        public boolean test(Permutation permutation, int level) {
            return true;
        }
    };
}
