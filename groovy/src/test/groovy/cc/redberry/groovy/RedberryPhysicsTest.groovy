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

package cc.redberry.groovy

import org.junit.Test

import static cc.redberry.core.indices.IndexType.Matrix1
import static cc.redberry.core.indices.IndexType.Matrix2
import static cc.redberry.groovy.RedberryPhysics.*
import static cc.redberry.groovy.RedberryStatic.*
import static junit.framework.Assert.assertTrue

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class RedberryPhysicsTest {

    @Test
    public void testDiracTrace1() {
        use(Redberry) {
            defineMatrices 'G_a', Matrix1.matrix,
                    'G_\\alpha', Matrix2.matrix

            assertTrue DiracTrace['G_a'] >> 'Tr[G_a*G_b]'.t == '4*g_ab'.t
            assertTrue DiracTrace['G_\\alpha'] >> 'Tr[G_\\alpha*G_\\beta]'.t == '4*g_\\alpha\\beta'.t
        }
    }

    @Test
    public void testSUNTrace1() {
        use(Redberry) {
            defineMatrices 'T_a', Matrix1.matrix,
                    'T_A', Matrix2.matrix

            assertTrue UnitaryTrace['T_a', 'f_abc', 'd_abc', 'N'] >> 'Tr[T_a*T_b]'.t == '(1/2)*g_{ba}'.t
            assertTrue UnitaryTrace['T_A', 'f_ABC', 'd_ABC', 'N'] >> 'Tr[T_A*T_B]'.t == '(1/2)*g_{AB}'.t
        }
    }

    @Test
    public void testLeviCivita() {
        use(Redberry) {
            assertTrue LeviCivitaSimplify.minkowski['e_abcd'] >> 'e_abcd*e^abcd'.t == '-24'.t
        }
    }
}
