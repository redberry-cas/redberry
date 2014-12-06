/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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

import cc.redberry.core.groups.permutations.PermutationGroup
import cc.redberry.core.transformations.factor.JasFactor
import org.junit.Test

import static cc.redberry.core.TAssert.assertEquals
import static cc.redberry.core.TAssert.assertTrue
import static cc.redberry.core.indices.IndexType.*
import static cc.redberry.core.tensor.Tensors.addAntiSymmetry
import static cc.redberry.core.tensor.Tensors.addSymmetry
import static cc.redberry.groovy.RedberryStatic.*

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
                    Differentiate[ExpandAndEliminate, 'R^ma_m^b', 'R^mc_m^d'] & EliminateDueSymmetries & 'd_m^m = 4'.t & 'R^a_man = R_mn'.t & 'R^a_a = R'.t

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

            assertEquals "B^{a'}_{A'}^{\\alpha'\\beta'}_{\\gamma'\\delta'}".t.toString(), "B".t.toString()
            assertEquals "C^{a'}_{A'}^{\\alpha'\\beta'}_{\\gamma'\\delta'}".t.toString(), "C".t.toString()

            defineMatrices "G", Matrix1.vector,
                    "M", "K", Matrix2.covector, Matrix3.tensor(2, 2),
                    "O", "T", Matrix4.tensor(3, 3)

            assertEquals 'G^{a\'}'.t.toString(), "G".t.toString()
            assertEquals 'M_{A\'}^{\\alpha\'\\beta\'}_{\\gamma\'\\delta\'}'.t.toString(), "M".t.toString()
            assertEquals 'K_{A\'}^{\\alpha\'\\beta\'}_{\\gamma\'\\delta\'}'.t.toString(), "K".t.toString()
            assertEquals 'O^{\\Gamma\'\\Delta\'\\Theta\'}_{\\Lambda\'\\Xi\'\\Pi\'}'.t.toString(), "O".t.toString()
            assertEquals 'T^{\\Gamma\'\\Delta\'\\Theta\'}_{\\Lambda\'\\Xi\'\\Pi\'}'.t.toString(), "T".t.toString()
        }
    }

    @Test
    public void testCollect1() {
        use(Redberry) {
            def t = 'f[x]*f[-x] + a*f[x]*f[-x]'.t
            assertTrue Collect['f[x]'] >> t == '(1+a)*f[-x]*f[x]'.t
        }
    }

    @Test
    public void testCollect2() {
        use(Redberry) {
            def t = 'f[x,x**2]+(x+1)*f~(1,0)[x,x**2]+2*x*(x+1)*f~(0,1)[x,x**2]'.t
            assertTrue Collect['f~(0,1)[x, y]', 'f~(1,0)[x, y]', Factor] >> t == 'f[x,x**2]+(x+1)*f~(1,0)[x,x**2]+2*x*(x+1)*f~(0,1)[x,x**2]'.t
        }

    }

    @Test
    public void testExpand3() {
        use(Redberry) {
            assertTrue Expand['x = y'.t, 'f = a'.t] >> 'x*(x + f)'.t == 'a*y+y**2'.t
            assertTrue Expand >> 'x*(x + f)'.t == 'f*x+x**2'.t
            assertTrue Expand['x = a'] >> 'x*(x + f)'.t == 'f*a+a**2'.t
            assertTrue Expand['x = a'.t] >> 'x*(x + f)'.t == 'f*a+a**2'.t
        }
    }

    @Test
    public void testPowerExpand() {
        use(Redberry) {
            assertTrue PowerExpand >> '(a*b*c)**d'.t == 'a**d*b**d*c**d'.t
            assertTrue PowerExpand['a'] >> '(a*b*c)**d'.t == 'a**d*(b*c)**d'.t
            assertTrue PowerExpand['a'.t] >> '(a*b*c)**d'.t == 'a**d*(b*c)**d'.t
            assertTrue PowerExpand['a'.t, 'b'.t] >> '(a*b*c*d)**e'.t == 'a**e*b**e*(c*d)**e'.t
        }
    }

    @Test
    public void testPowerExpand1() {
        use(Redberry) {
            assertTrue PowerExpand >> 'Sqrt[a*b]'.t == 'Sqrt[a]*Sqrt[b]'.t
        }
    }

    @Test
    public void testGenerateTensor() {
        use(Redberry) {
            def t
            t = GenerateTensor('_abcd'.si, ['g_mn', 'g_ab'])
            assertEquals t.size(), 3

            t = GenerateTensor('_abcd'.si, ['g_mn', 'g^ab'], [GenerateParameters: 'False'])
            assertEquals t, 'g_{ad}*g_{bc}+g_{ac}*g_{bd}+g_{ab}*g_{cd}'.t

            t = GenerateTensor('_abcd'.si, ['g_mn'], [GenerateParameters: 'false'])
            assertEquals t, 'g_{ad}*g_{bc}+g_{ac}*g_{bd}+g_{ab}*g_{cd}'.t

            t = GenerateTensor('_abcd'.si, ['g_mn'], [GenerateParameters: 'false', SymmetricForm: 'true'])
            assertEquals t, '(1/3)*(g_{ac}*g_{bd}+g_{ab}*g_{cd}+g_{ad}*g_{bc})'.t

            t = GenerateTensor('_abcd'.si, ['g_mn'], [GenerateParameters: 'false', SymmetricForm: 'true',])
            assertEquals t, '(1/3)*(g_{ac}*g_{bd}+g_{ab}*g_{cd}+g_{ad}*g_{bc})'.t
        }
    }

    @Test
    public void testGenerateTensor1() {
        use(Redberry) {
            def t
            t = GenerateTensor('_ab^cd'.si, ['g_mn', 'g^mn'], [RaiseLower: 'false', GenerateParameters: 'false'])
            assertEquals t, 'g_ab*g^cd'
            t = GenerateTensor('_ab^cd'.si, ['g_mn', 'g^mn'], [RaiseLower: false, GenerateParameters: false])
            assertEquals t, 'g_ab*g^cd'
            t = GenerateTensor('_ab^cd'.si, ['g_mn', 'g^mn'], [GenerateParameters: false])
            assertEquals t, 'g_ab*g^cd + d_a^c*d_b^d + d_a^d*d_b^c'
            t = GenerateTensor('_ab^cd'.si, ['g_mn', 'g^mn'], [GenerateParameters: false, SymmetricForm: true])
            assertEquals t, 'g_ab*g^cd + (d_a^c*d_b^d + d_a^d*d_b^c)/2'
        }
    }

    @Test
    public void testGenerateTensor2() {
        use(Redberry) {
            def t
            t = GenerateTensorWithCoefficients('_ab^cd'.si, ['g_mn', 'g^mn'], [RaiseLower: false])
            assertEquals t[1].size(), 1
            t = GenerateTensorWithCoefficients('_ab^cd'.si, ['g_mn', 'g^mn'])
            assertEquals t[1].size(), 3
            t = GenerateTensorWithCoefficients('_ab^cd'.si, ['g_mn'], [SymmetricForm: true])
            assertEquals t[1].size(), 2
        }
    }

    @Test
    public void testFrobenius1() {
        use(Redberry) {
            def s = FrobeniusSolve([[1, 1, 1, 12]], 4)
            assertEquals s.size(), 4
        }
    }

    @Test
    public void testFactor1() {
        use(Redberry) {
            assertEquals Factor >> 'a**2+2*a*b+b**2'.t, '(a+b)**2'
            assertEquals Factor[[FactorScalars: false]] >> 'a**2+2*a*b+b**2'.t, '(a+b)**2'
            assertEquals Factor[[FactorScalars: true]] >> 'a**2+2*a*b+b**2'.t, '(a+b)**2'
            assertEquals Factor[false] >> 'a**2+2*a*b+b**2'.t, '(a+b)**2'
            assertEquals Factor[true] >> 'a**2+2*a*b+b**2'.t, '(a+b)**2'
            assertEquals Factor[[FactorScalars: false, FactorizationEngine: JasFactor.ENGINE]] >> 'a**2+2*a*b+b**2'.t, '(a+b)**2'
            assertEquals Factor[[FactorScalars: true, FactorizationEngine: JasFactor.ENGINE]] >> 'a**2+2*a*b+b**2'.t, '(a+b)**2'
            assertEquals Factor[false, JasFactor.ENGINE] >> 'a**2+2*a*b+b**2'.t, '(a+b)**2'
            assertEquals Factor[true, JasFactor.ENGINE] >> 'a**2+2*a*b+b**2'.t, '(a+b)**2'


            assertEquals Factor >> 'k_m*k^m*(a+b) + k_m*f^m*(a+b)'.t, '(k_m*k^m + k_m*f^m)*(a+b)'
            assertEquals Factor[[FactorScalars: false]] >> 'k_m*k^m*(a+b) + k_m*f^m*(a+b)'.t, 'k_m*k^m*(a+b) + k_m*f^m*(a+b)'
            assertEquals Factor[[FactorScalars: true]] >> 'k_m*k^m*(a+b) + k_m*f^m*(a+b)'.t, '(k_m*k^m + k_m*f^m)*(a+b)'
            assertEquals Factor[false] >> 'k_m*k^m*(a+b) + k_m*f^m*(a+b)'.t, 'k_m*k^m*(a+b) + k_m*f^m*(a+b)'
            assertEquals Factor[true] >> 'k_m*k^m*(a+b) + k_m*f^m*(a+b)'.t, '(k_m*k^m + k_m*f^m)*(a+b)'
        }
    }

    @Test
    public void testSymmetrize() {
        use(Redberry) {
            def si = '_ab'.si
            si.symmetries.addSymmetry([1, 0].p)
            assertEquals Symmetrize[si] >> 'T_ab'.t, 'T_ab/2 + T_ba/2'.t
            si = '_ab'.si
            si.symmetries.addSymmetry(-[1, 0].p)
            assertEquals Symmetrize[si] >> 'T_ab'.t, 'T_ab/2 - T_ba/2'.t
        }
    }

    @Test
    public void testPermutationGroup() {
        use(Redberry) {
            def gr
            gr = Group([1, 0])
            assert gr instanceof PermutationGroup
            gr = Group(+[[0, 1]], -[[2, 3]])
            assert gr instanceof PermutationGroup
        }
    }
}

