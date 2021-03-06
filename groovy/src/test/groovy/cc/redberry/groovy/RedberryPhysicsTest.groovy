/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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

import cc.redberry.core.context.CC
import org.junit.Before
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
    @Before
    public void setUp() throws Exception {
        Reset()
    }

    @Test
    public void testDiracTrace1() {
        use(Redberry) {
            defineMatrices 'G_a', 'G5', Matrix1.matrix
            assertTrue DiracTrace['G_a'] >> 'Tr[G_a*G_b]'.t == '4*g_ab'.t

            Reset()

            defineMatrices 'G_\\alpha', 'G5', Matrix2.matrix
            assertTrue DiracTrace['G_\\alpha', 'G5', 'e_\\alpha\\beta\\gamma\\delta'] >> 'Tr[G_\\alpha*G_\\beta]'.t == '4*g_\\alpha\\beta'.t
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
            assertTrue LeviCivitaSimplify.minkowski['e_abcd'.t] >> 'e_abcd*e^abcd'.t == '-24'.t
            def tr = LeviCivitaSimplify.minkowski[[OverallSimplifications: EliminateMetrics & 't^i_i = 3'.t]]
            assert tr >> 't^i_j*e_abci*e^abcj'.t == 18.t
        }
    }

    @Test
    public void testSetMandelstam() {
        use(Redberry) {
            println setMandelstam([k1_a: 'm1', k2_a: 'm2', k3_a: 'm3', k4_a: 'm4'], 'r', 'p', 'q')
        }
    }

    @Test
    public void testDT1() throws Exception {
        use(Redberry) {

            defineMatrices 'G_a', Matrix1.matrix,
                    'G5', Matrix1.matrix

            def dt = DiracTrace[[Simplifications: 't_a^a = y'.t]]
            assert '-4*t_{cd}+4*t_{dc}+(4*y+4*x)*g_{cd}'.t == (dt >> 'Tr[(G_a*G_b*t^ab + x)*G_c*G_d]'.t)
        }
    }

    @Test
    public void testDiracTrace2() {
        use(Redberry) {
            defineMatrices 'G_a', 'G5', Matrix1.matrix

            def dTrace = DiracTrace[[Dimension: 'D']]
            assert dTrace >> 'Tr[G_a*G_b*G_c*G_d]'.t == '2**((1/2)*D)*g_{ad}*g_{bc}+2**((1/2)*D)*g_{ab}*g_{cd}-2**((1/2)*D)*g_{ac}*g_{bd}'.t

            dTrace = DiracTrace[[Dimension: 'D', TraceOfOne: 4]]
            assert dTrace >> 'Tr[G_a*G_b*G_c*G_d]'.t == '4*g_{ad}*g_{bc}+4*g_{ab}*g_{cd}-4*g_{ac}*g_{bd}'.t
        }
    }

    @Test
    public void testDiracSimplify1() throws Exception {
        use(Redberry) {
            defineMatrices 'G_a', 'G5', Matrix1.matrix

            def dS = DiracSimplify[[Dimension: 'D']]

            assert dS >> '2*G_a*G^a*G_b'.t == '2*D*G_b'.t

            assert (Factor >> (dS >> '2*G_a*G_b*G^a'.t)) == '-2*(D-2)*G_{b}'.t
        }
    }


    @Test
    public void testSpinorsSimplify1() throws Exception {
        use(Redberry) {
            defineMatrices 'G_a', 'G5', Matrix1.matrix,
                    'cu', Matrix1.covector

            def dS = SpinorsSimplify[[uBar: 'cu', Momentum: 'p_a', Mass: 'm']]
            assert dS >> 'cu*G^a*p_a'.t == 'm*cu'.t

            assert dS >> 'cu*G_b*G^a*p_a'.t == '-m*cu*G_{b}+2*cu*p_{b}'.t
        }
    }

    @Test
    public void testPV() throws Exception {
        use(Redberry) {
            PassarinoVeltman(1, 'q_a', ['k_a', 'p_a'], Identity)
            PassarinoVeltman(1, 'q_a'.t, ['k_a', 'p_a'].t, 'k_a*p^a = X'.t)
            PassarinoVeltman(1, 'q_a'.t, ['k_a', 'p_a'])
        }
    }

    @Test
    public void testFourier() throws Exception {
        use(Redberry) {
            println LagrangeFourier >> 'f[x_a]*f[x_a] + f*f[x_a]*f[x_a]'.t
        }
    }

    @Test
    public void testContextReset() throws Exception {
        use(Redberry) {
            defineMatrices 'G_a', 'G5', Matrix1.matrix
            assert DiracTrace >> 'Tr[G_a*G_b]'.t == '4*g_ab'.t

            Reset()

            defineMatrices 'G_a', 'G5', Matrix1.matrix
            assert DiracTrace >> 'Tr[G_a*G_b]'.t == '4*g_ab'.t
        }
    }
}
