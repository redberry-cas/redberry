/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
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

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Fore more examples look {@link PermutationTest}.
 * 
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SymmetryTest {
    public SymmetryTest() {
    }
    
    @Test
    public void testComposition() {
        Symmetry a = new Symmetry(new int[]{2, 3, 0, 1}, true);
        Symmetry b = new Symmetry(new int[]{0, 1, 3, 2}, true);
        Symmetry c = new Symmetry(new int[]{3, 2, 0, 1}, false);
        assertTrue(a.composition(b).equals(c));
    }
}
