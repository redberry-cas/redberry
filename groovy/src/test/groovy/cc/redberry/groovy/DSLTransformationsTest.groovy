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

import org.junit.Test

import static cc.redberry.core.indices.IndexType.Matrix1
import static cc.redberry.core.indices.IndexType.Matrix2
import static cc.redberry.core.tensor.Tensors.antiSymmetric
import static cc.redberry.core.tensor.Tensors.setSymmetric
import static cc.redberry.groovy.RedberryPhysics.*
import static cc.redberry.groovy.RedberryStatic.*

/**
 * Created by poslavsky on 20/11/15.
 */
class DSLTransformationsTest {
    @Test
    public void testGeneral() throws Exception {
        use(Redberry) {
            assert Collect['x'] >> 'a*x + b*x + c*x**2 + d*x**2 + f*e'.t ==
                    '(a + b)*x + (c + d)*x**2 + f*e'.t

            assert Collect['x', Factor] >> '(1 + a + x)**4'.t ==
                    'x**4 + 4*x**3*(a+1) + 6*x**2*(a+1)**2 + 4*(a+1)**3*x + (a+1)**4'.t

            assert Collect['A_i'] >> 'A_m*B_n + A_n*C_m + B_n*C_m'.t ==
                    'A_{a}*(C_{m}*d_{n}^{a} + B_{n}*d_{m}^{a}) + B_{n}*C_{m}'.t

            assert Collect['A_i', 'B_i', [Simplifications: Factor, ExpandSymbolic: false]] >>
                    '(a+b)**5 * A_a * B^b + A^b * B_a + A_a * C^b + A^b * D_a'.t ==
                    'A_c*B^d*((b+a)**5*d_a^c*d^b_d+g_ad*g^bc)+(C^b*d_a^c+g^bc*D_a)*A_c'.t

            assert Collect['A_i', 'B_i', [Factor, false]] >>
                    '(a+b)**5 * A_a * B^b + A^b * B_a + A_a * C^b + A^b * D_a'.t ==
                    'A_c*B^d*((b+a)**5*d_a^c*d^b_d+g_ad*g^bc)+(C^b*d_a^c+g^bc*D_a)*A_c'.t

            assert a ==
                    'A_c*B^d*((b**2+a**2+2*a*b)*d_a^c*d^b_d+g_ad*g^bc)+(C^b*d_a^c+g^bc*D_a)*A_c'.t

            assert Collect['A_i', 'B_i', [Identity, true]] >>
                    '(a+b)**2 * A_a * B^b + A^b * B_a + A_a * C^b + A^b * D_a'.t ==
                    'A_c*B^d*((b**2+a**2+2*a*b)*d_a^c*d^b_d+g_ad*g^bc)+(C^b*d_a^c+g^bc*D_a)*A_c'.t

            assert CollectScalars >> 't_mn*f^mn*t_ba*f^ba*R_ij + (t_mn*f^mn)**2*R_ij'.t ==
                    '2*(t_{mn}*f^{mn})**2*R_{ij}'.t

            assert CollectScalars >> '(A_i + B_i)*(A^i + B^i)*(A_a + B_a)*(A^a + B^a)'.t ==
                    '((A^{a}+B^{a})*(A_{a}+B_{a}))**2'.t

            assert CollectNonScalars >> 'A_i*A^i*B_m + B_m'.t ==
                    '(A_i*A^i + 1)*B_m'.t

            assert Conjugate >> 'a + I*b'.t ==
                    'a - I*b'.t

            assert Denominator >> '(a + b)/(c + d)'.t ==
                    'c + d'.t

            assert Differentiate['x'] >> 'x**n'.t ==
                    'n*x**(n-1)'.t

            assert Differentiate['f_mn'] >> 'f_ab*f_cd'.t ==
                    'd_a^m*d_b^n * f_cd + d_d^n*d_c^m*f_ab'.t

            assert Differentiate['t_mn'] >> 't_ab*f^ab[t_pq]'.t ==
                    'f^{mn}[t_{dc}] + t_{ab}*f~(1)^{abmn}[t_{dc}]'.t

            assert ((Differentiate['x_m', 'x^m', 'y_a', 'y^a'] & CollectScalars) >> '(x_a*y^a)**5'.t) ==
                    '240*(y^b*x_b)**3+40*(y^b*x_b)**3*d^a_a+120*y_m*y^m*y^b*x_b*x_a*x^a'.t

            assert Differentiate['R^ma', 'R_ma', ExpandAndEliminate & 'd^a_a = 4'.t] >> 'R_ab*R^ab*Sin[R_ab*R^ab]'.t ==
                    '-4*Sin[R_{ab}*R^{ab}]*R^{ma}*R_{ma}*R_{cb}*R^{cb}+40*Cos[R_{ab}*R^{ab}]*R_{cb}*R^{cb}+32*Sin[R_{ab}*R^{ab}]'.t

            assert EliminateDueSymmetries >> '(A_mn - A_nm)*(A^mn + A^nm)'.t ==
                    0.t

            assert EliminateMetrics >> 'g_mn*A^m + d_n^a*B_a'.t

            assert ExpandAndEliminate >> '(g_mn - A_nm)*(A^mn + g^nm)'.t ==
                    '-A^{mn}*A_{nm}+d^{n}_{n}'.t

            assert Expand >> '(x_n + y_n)*(f_m - r_m)'.t ==
                    'x_{n}*f_{m}+f_{m}*y_{n}-r_{m}*y_{n}-x_{n}*r_{m}'.t

            assert Expand['k_a*k^a = 0'.t] >> '(k_a + t_a)*(k^a + t^a)'.t ==
                    '2*k_a*t^a + t_a*t^a'.t

            assert Expand[[Simplifications: 'k_a*k^a = 0'.t]] >> '(k_a + t_a)*(k^a + t^a)'.t ==
                    '2*k_a*t^a + t_a*t^a'.t

            assert ExpandAll[[Simplifications: 'k_a*k^a = 0'.t]] >> '(k_a + t_a)*(k^a + t^a)'.t ==
                    '2*k_a*t^a + t_a*t^a'.t

            assert ExpandTensors[[Simplifications: 'k_a*k^a = 0'.t]] >> '(k_a + t_a)*(k^a + t^a)'.t ==
                    '2*k_a*t^a + t_a*t^a'.t

            assert ExpandTensors['k_a*k^a = 0'.t] >> '(k_a + t_a)*(k^a + t^a)'.t ==
                    '2*k_a*t^a + t_a*t^a'.t

            assert ExpandTensors[[Simplifications: 'k_a*k^a = 0'.t, LeaveScalars: true]] >> '(f_a*f^a + t_a*t^a)*(n_a*n^a + j_a*j^a)'.t ==
                    '(f_a*f^a + t_a*t^a)*(n_a*n^a + j_a*j^a)'.t

            assert Factor >> '2*x**3*y - 2*a**2*x*y - 3*a**2*x**2 + 3*a**4'.t ==
                    '(x+a)*(x-a)*(-3*a**2+2*y*x)'.t

            assert Factor[[FactorScalars: false]] >> '(a+b)**2*f_m*f^m + (a**2 - b**2)*f_a*f^a*f_b*f^b'.t ==
                    '(a+b)**2*f_{m}*f^{m}-(b+a)*(b-a)*f_{a}*f^{a}*f_{b}*f^{b}'.t

            assert PowerExpand >> '(a*b*c)**d'.t ==
                    'a**d*b**d*c**d'.t

            assert PowerExpand['a'] >> '(a*b*c)**d'.t ==
                    'a**d*(b*c)**d'.t

            assert PowerUnfold >> '(a*b*c)**d'.t ==
                    'a**d*b**d*c**d'.t

            assert PowerUnfold['A_m'] >> '(A_m*A^m)**3*(B_m*B^m)**2'.t ==
                    '(B_m*B^m)**2*A_{m}*A^{m}*A_{a}*A^{a}*A_{b}*A^{b}'.t

            def indices = '_abc'.si
            indices.symmetries.setSymmetric()
            assert Symmetrize[indices] >> 't_abc'.t ==
                    '(1/6)*t_{cab} + (1/6)*t_{acb} + (1/6)*t_{bca} + (1/6)*t_{cba} + (1/6)*t_{abc} + (1/6)*t_{bac}'.t

            assert Symmetrize[indices, [SymmetryFactor: false]] >> 't_abc'.t ==
                    '(t_{cab} + t_{acb} + t_{bca} + t_{cba} + t_{abc} + t_{bac})'.t

            assert Together >> '1/a + 1/b'.t ==
                    '(a + b)/(a*b)'.t

            assert TogetherFactor >> 'x**2/(x**2 - 1) + x/(x**2 - 1)'.t ==
                    '(-1+x)**(-1)*x'.t

            assert Together[[Factor: Factor]] >> 'x**2/(x**2 - 1) + x/(x**2 - 1)'.t ==
                    '(-1+x)**(-1)*x'.t

            assert Together[[Factor: Identity]] >> 'x**2/(x**2 - 1) + x/(x**2 - 1)'.t ==
                    '(x**2+x)/(x**2 - 1)'.t
        }
    }

    @Test
    public void testPhysics() throws Exception {
        use(Redberry) {
            defineMatrices 'A', 'B', 'C', Matrix1.matrix
            assert Reverse[Matrix1] >> 'A*B*C'.t ==
                    'C*B*A'.t
            assert Reverse >> 'A*B*C'.t ==
                    'C*B*A'.t

            Reset()

            defineMatrices 'T_A', 'F_A', Matrix2.matrix
            assert UnitarySimplify >> 'T_A*T^A'.t ==
                    '(1/2)*N**(-1)*(N**2-1) + 0 * T_A*T^A'.t

            assert UnitarySimplify[[Matrix: 'T_A']] >> 'T_A*T^A'.t ==
                    '(1/2)*N**(-1)*(N**2-1) + 0 * T_A*T^A'.t

            assert UnitarySimplify[[Matrix: 'T_A']] >> 'T_A*T_B*T^A'.t ==
                    '-(1/2)*N**(-1)*T_{B}'.t

            assert UnitarySimplify[[Matrix: 'F_A']] >> 'F_A*F_B*F^A*F_C'.t ==
                    '-(1/2)*N**(-1)*F_B*F_C'.t

            assert UnitarySimplify[[d: 'r_ABC', N: 7]] >> 'r_ABC*r^ABC'.t ==
                    '(7**2-4)*7**(-1)*(7**2-1)'.t

            assert UnitaryTrace >> 'Tr[T_A*T_B]'.t ==
                    '(1/2)*g_AB'.t

            assert UnitaryTrace >> 'Tr[T_A*T_B*T_C]'.t ==
                    '(1/4*I)*f_{CAB}+(1/4)*d_{CAB}'.t

            assert UnitaryTrace[[Matrix: 'F_A', f: 'u_ABC', d: 'o_ABC']] >> 'Tr[F_A*F_B*F_C]'.t ==
                    '(1/4*I)*u_{CAB}+(1/4)*o_{CAB}'.t

            assert UnitaryTrace['F_A', 'u_ABC', 'o_ABC'] >> 'Tr[F_A*F_B*F_C]'.t ==
                    '(1/4*I)*u_{CAB}+(1/4)*o_{CAB}'.t

            Reset()

            defineMatrices 'G_m', 'G5', 'R_a', 'R5', Matrix1.matrix
            assert DiracTrace >> 'Tr[G_m*G_n]'.t ==
                    '4*g_mn'.t

            assert DiracTrace[[Gamma: 'G_m']] >> 'Tr[G_m*G_n]'.t ==
                    '4*g_mn'.t

            assert DiracTrace[[Gamma: 'G_a']] >> 'Tr[(p_a*G^a + m)*G_m*(q_a*G^a-m)*G_n]'.t ==
                    '4*p_{m}*q_{n}+4*p_{n}*q_{m}-4*m**2*g_{mn}-4*p^{a}*g_{mn}*q_{a}'.t

            assert DiracTrace[[Gamma: 'G_a', Gamma5: 'G5', LeviCivita: 'e_abcd']] >> 'Tr[G_a*G_b*G_c*G_d*G5]'.t ==
                    '-4*I*e_{abcd}'.t

            assert DiracTrace[[Gamma: 'R_a', Gamma5: 'R5', LeviCivita: 'E_abcd']] >> 'Tr[R_a*R_b*R_c*R_d*R5]'.t ==
                    '-4*I*E_{abcd}'.t

            assert DiracTrace['R_a', 'R5', 'E_abcd'] >> 'Tr[R_a*R_b*R_c*R_d*R5]'.t ==
                    '-4*I*E_{abcd}'.t

            assert DiracTrace[[Dimension: 'D']] >> 'Tr[G_a*G_b*G_c*G_d]'.t ==
                    '2**((1/2)*D)*g_{ad}*g_{bc}+2**((1/2)*D)*g_{ab}*g_{cd}-2**((1/2)*D)*g_{ac}*g_{bd}'.t

            assert DiracTrace[[Dimension: 'D', TraceOfOne: 4]] >> 'Tr[G_a*G_b*G_c*G_d]'.t ==
                    '4*g_{ad}*g_{bc}+4*g_{ab}*g_{cd}-4*g_{ac}*g_{bd}'.t

            def expr = 'Tr[(p^a + k^a)*(p^b + k^b)*G_a*G_b*G_c*G_d]'.t
            def mandelstam = setMandelstam([k_a: '0', p_a: '0', q_a: 'm', r_a: 'm'], 's', 't', 'u')
            assert DiracTrace[[Simplifications: mandelstam]] >> expr ==
                    '4*s*g_{cd}'.t

            Reset()

            setAntiSymmetric 'e_abcd'
            setAntiSymmetric 'f_abcd'
            assert LeviCivitaSimplify.minkowski['e_abcd'.t] >> 'e_abcm*e^abcn'.t ==
                    '-6*d_{m}^{n}'.t

            assert LeviCivitaSimplify.minkowski['e_abcde'] >> 'e^{m}_{g}^{kci}*e_{pdj}^{l}_{o}*e_{c}^{n}_{mi}^{p}*e_{khnef}*e^{g}_{a}^{efd}*e_{l}^{hj}_{b}^{o}'.t ==
                    '864*g_{ab}'.t

            assert LeviCivitaSimplify.euclidean[[LeviCivita: 'f_abcd'.t]] >> '4*f^h_d^fb*f_abch*f_e^d_gf'.t ==
                    '16*f_{eagc}'.t

            Reset()

            defineMatrices 'F_\\mu', 'F5', Matrix2.matrix
            def dTrace = DiracTrace[[Gamma: 'F_\\mu', Gamma5: 'F5', LeviCivita: 'Eps_{\\mu\\nu\\alpha\\beta}']]
            assert dTrace >> 'Tr[F_\\mu*F_\\nu*F_\\alpha*F_\\beta * F5]'.t ==
                    '-4*I*Eps_{\\mu\\nu\\alpha\\beta}'.t

            Reset()

            defineMatrices 'G_a', 'G5', Matrix1.matrix
            assert DiracOrder >> 'G_b*G_a'.t ==
                    '2*g_{ba}-G_{a}*G_{b}'.t

            assert DiracOrder[[Dimension: 10]] >> 'G_b*G_a'.t ==
                    '2*g_{ba}-G_{a}*G_{b}'.t

            assert DiracOrder >> 'G5*G_b*G_a'.t ==
                    '2*G5*g_{ba}-G_{a}*G_{b}*G5'.t

            assert DiracSimplify >> 'G_a*G^a'.t ==
                    '4 + 0*G_a*G^a'.t

            assert DiracSimplify >> 'G_a*G_b*G^a'.t ==
                    '-2*G_{b}'.t

            assert DiracSimplify >> 'G_a*G_b*G^a*G^b'.t ==
                    '-8 + 0*G_a*G^a'.t

            assert DiracSimplify[[Dimension: 'N']] >> 'G_a*G^a'.t ==
                    'N + 0*G_a*G^a'.t

            assert DiracSimplify[[Dimension: 'N']] >> 'G_a*G^a'.t ==
                    'N + 0*G_a*G^a'.t

            assert DiracSimplify[[Dimension: 'N']] >> 'G_a*G^a'.t ==
                    'N + 0*G_a*G^a'.t
        }
    }
}
