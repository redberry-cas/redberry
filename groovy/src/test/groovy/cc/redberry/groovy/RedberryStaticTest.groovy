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

import static cc.redberry.core.indices.IndexType.*
import static cc.redberry.core.tensor.Tensors.addAntiSymmetry
import static cc.redberry.core.tensor.Tensors.addSymmetry
import static cc.redberry.groovy.RedberryStatic.*
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class RedberryStaticTest {
    @Test
    void testExpand1() throws Exception {
        use(Redberry) {
            def tensor = '(f+d)*(f+d)'.t;
            assertTrue tensor << Expand['f**2=1'.t &
                    'd=c'.t] == 'c**2+1+2*f*c'.t
        }
    }

    @Test
    void testExpand2() throws Exception {
        use(Redberry) {
            def tensor = '(g_mn*g_ab + g_ma*g_nb)*(g^mn*g^ab + g^ma*g^nb)'.t;
            assertTrue tensor << Expand[EliminateMetrics & 'd^m_m = 4'.t] == '40'.t
            assertTrue tensor << ExpandAll[EliminateMetrics & 'd^m_m = 4'.t] == '40'.t
        }
    }


    @Test
    void testDifferentiate1() throws Exception {
        use(Redberry) {
            def tensor = '(f+d)*(f+d+c)'.t;
            assertTrue tensor << Differentiate['d=0'.t, 'f'] == '2*f+c'.t
        }
    }

    @Test
    void testDifferentiate2() throws Exception {
        use(Redberry) {
            addAntiSymmetry('R_abcd', 1, 0, 2, 3)
            addAntiSymmetry('R_abcd', 0, 1, 3, 2)
            addSymmetry('R_abcd', 2, 3, 0, 1)

            def tensor = 'R^acbd*Sin[R_abcd*R^abcd]'.t;
            def tr =
                Differentiate[ExpandAndEliminate, 'R^ma_m^b', 'R^mc_m^d'] & EliminateFromSymmetries & 'd_m^m = 4'.t & 'R^a_man = R_mn'.t & 'R^a_a = R'.t

            assertTrue tr >> tensor == '6*R*Cos[R_{abcd}*R^{abcd}]-4*Sin[R_{abcd}*R^{abcd}]*R_{ab}*R_{cd}*R^{acbd}'.t
        }
    }

    @Test
    void testNumerator1() throws Exception {
        use(Redberry) {
            assertTrue Numerator >> '1/a+1/b'.t == '1/a+1/b'.t
            assertTrue Denominator >> '1/a+1/b'.t == '1'.t
        }
    }


    @Test
    void testMatrices() throws Exception {
        use(Redberry) {
            defineMatrix "A", Matrix1.vector, Matrix2.covector, Matrix3.tensor(2, 2);
            assertEquals "A^{a'}_{A'}^{\\alpha'\\beta'}_{\\gamma'\\delta'}", "A".t.toString()

            defineMatrices "B", "C", Matrix1.vector, Matrix2.covector, Matrix3.tensor(2, 2);
            assertEquals "B^{a'}_{A'}^{\\alpha'\\beta'}_{\\gamma'\\delta'}", "B".t.toString()
            assertEquals "C^{a'}_{A'}^{\\alpha'\\beta'}_{\\gamma'\\delta'}", "C".t.toString()

            defineMatrices "G", Matrix1.vector,
                    "M", "K", Matrix2.covector, Matrix3.tensor(2, 2),
                    "O", "T", Matrix4.tensor(3, 3)

            assertEquals 'G^{a\'}', "G".t.toString()
            assertEquals 'M_{A\'}^{\\alpha\'\\beta\'}_{\\gamma\'\\delta\'}', "M".t.toString()
            assertEquals 'K_{A\'}^{\\alpha\'\\beta\'}_{\\gamma\'\\delta\'}', "K".t.toString()
            assertEquals 'O^{\\Gamma\'\\Delta\'\\Theta\'}_{\\Lambda\'\\Xi\'\\Pi\'}', "O".t.toString()
            assertEquals 'T^{\\Gamma\'\\Delta\'\\Theta\'}_{\\Lambda\'\\Xi\'\\Pi\'}', "T".t.toString()
        }
    }

    @Test
    public void testCollect1() {
        use(Redberry) {
            def t = 'f[x]*f[-x] + a*f[x]*f[-x]'.t
            assertTrue Collect['f[x]'] >> t == '(1+a)*f[-x]*f[x]'.t
        }

    }
}

