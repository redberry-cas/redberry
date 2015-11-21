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
import cc.redberry.core.utils.TensorUtils
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static cc.redberry.core.TAssert.assertEquals
import static cc.redberry.core.indices.IndexType.Matrix1
import static cc.redberry.core.indices.IndexType.Matrix2
import static cc.redberry.core.tensor.Tensors.setAntiSymmetric
import static cc.redberry.core.tensor.Tensors.symmetric
import static cc.redberry.groovy.RedberryPhysics.*
import static cc.redberry.groovy.RedberryStatic.*

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class BulkTests {
    @Before
    public void setUp() throws Exception {
        CC.reset()
    }

    @Test
    public void testBhaBhaScattering() throws Exception {
        use(Redberry) {
            defineMatrices 'G_a', 'V_i', Matrix1.matrix,
                    'v[p_a]', 'u[p_a]', Matrix1.vector,
                    'cv[p_a]', 'cu[p_a]', Matrix1.covector

            //photon propagator
            def G = 'G_mn[k_a] = -I*g_mn/(k_a*k^a)'.t
            //vertex
            def V = 'V_i = -I*e*G_i'.t
            //diagram a)
            def Ma = 'cv[p2_a]*V_i*u[p1_a]*G^ij[p1_a + p2_a]*cu[k1_a]*V_j*v[k2_a]'.t
            //diagram b)
            def Mb = 'cu[k1_a]*V_i*u[p1_a]*G^ij[p1_a - k1_a]*cv[p2_a]*V_j*v[k2_a]'.t
            //matrix element
            def M = Ma - Mb
            M = (V & G) >> M

            def mandelstam = setMandelstam(
                    ['p1_m': 'm', 'p2_m': 'm', 'k1_m': 'm', 'k2_m': 'm'])

            M = (EliminateMetrics & ExpandDenominator & mandelstam) >> M

            //complex conjugation
            def MC = Conjugate >> M
            MC = 'u[p1_a]*cv[p2_a] = v[p2_a]*cu[p1_a]'.t >> MC
            MC = 'v[k2_a]*cu[k1_a] = u[k1_a]*cv[k2_a]'.t >> MC
            MC = 'u[p1_a]*cu[k1_a] = u[k1_a]*cu[p1_a]'.t >> MC
            MC = 'v[k2_a]*cv[p2_a] = v[p2_a]*cv[k2_a]'.t >> MC

            def M2 = ExpandAll >> (M * MC / 4)

            //electron polarizations
            M2 = 'u[p1_a]*cu[p1_a] = m + p1_a*G^a'.t >> M2
            M2 = 'u[k1_a]*cu[k1_a] = m + k1_a*G^a'.t >> M2
            M2 = 'v[p2_a]*cv[p2_a] = -m + p2_a*G^a'.t >> M2
            M2 = 'v[k2_a]*cv[k2_a] = -m + k2_a*G^a'.t >> M2

            M2 = DiracTrace['G_a'] >> M2

            M2 = (ExpandAndEliminate & mandelstam & 'd^i_i = 4'.t) >> M2
            M2 = 'u = 4*m**2 - s - t'.t >> M2
            M2 = Factor >> M2

            assertEquals M2, '4*(-4*m**2*s**3+4*m**4*s**2+2*s*t**3-4*t*m**4*s+2*t*s**3+3*t**2*s**2+t**4-4*m**2*t**3+4*m**4*t**2+s**4)*e**4*t**(-2)*s**(-2)'.t
        }
    }

    @Test
    public void testAnnihilationToPhotons() throws Exception {
        use(Redberry) {
            defineMatrices 'G_a', 'G5', 'V_i', 'D[x_m]', Matrix1.matrix,
                    'v[p_a]', 'u[p_a]', Matrix1.vector,
                    'cv[p_a]', 'cu[p_a]', Matrix1.covector

            def V = 'V_m = -I*e*G_m'.t,
                D = 'D[p_m] = -I*(m + p_m*G^m)/(m**2 - p_m*p^m)'.t,
                Ma = 'cv[p2_m]*V_m*e^m[k2_m]*D[p1_m-k1_m]*V_n*e^n[k1_m]*u[p1_m]'.t,
                Mb = 'cv[p2_m]*V_m*e^m[k1_m]*D[p1_m-k2_m]*V_n*e^n[k2_m]*u[p1_m]'.t,
                M = Ma + Mb

            M = (V & D) >> M

            def mandelstam = setMandelstam(
                    ['p1_m': 'm', 'p2_m': 'm', 'k1_m': '0', 'k2_m': '0'])

            M = (ExpandAll & mandelstam) >> M

            def MC = 'u[p1_m]*cv[p2_m] = v[p2_m]*cu[p1_m]'.t >> M

            MC = (Conjugate & Reverse[Matrix1]) >> MC

            def M2 = ExpandAll >> (M * MC / 4)

            //photon polarizations
            M2 = 'e_m[k1_a]*e_n[k1_a] = -g_mn'.t >> M2
            M2 = 'e_m[k2_a]*e_n[k2_a] = -g_mn'.t >> M2

            //electron polarizations
            M2 = 'u[p1_m]*cu[p1_m] =  m + p1^m*G_m'.t >> M2
            M2 = 'v[p2_m]*cv[p2_m] = -m + p2^m*G_m'.t >> M2
            println 'dtr'
            //M2 <<= EliminateMetrics & mandelstam
            M2 = DiracTrace >> M2
            println 'dtr ok'

            M2 = (ExpandAndEliminate & 'd^m_m = 4'.t & mandelstam) >> M2
            M2 = 'u = 2*m**2 -s-t'.t >> M2
            println 'factor'
            M2 = Factor >> M2

            assertEquals M2, '-2*(-s**3*m**2-8*t**2*s*m**2+2*m**8+3*s**2*m**4+2*t**4-8*t**3*m**2+12*t**2*m**4-2*s**2*t*m**2+s**3*t+3*t**2*s**2-8*t*m**6+4*t**3*s+4*s*t*m**4)*(-s-t+m**2)**(-2)*(t-m**2)**(-2)*e**4'.t
        }
    }

    @Test
    public void testAnnihilationToMuons() throws Exception {

        use(Redberry) {
            //setting up matrices
            //gamma, vertex
            defineMatrices 'G_a', 'V_i', Matrix1.matrix,
                    //electron & muon wave functions
                    'v[p_a]', 'u[p_a]', Matrix1.vector,
                    //their conjugations
                    'cv[p_a]', 'cu[p_a]', Matrix1.covector

            //photon propagator
            def G = 'G_mn[k_a] = -I*g_mn/(k_a*k^a)'.t
            //vertex
            def V = 'V_i = -I*e*G_i'.t
            //matrix element
            def M = 'cv[p2_a]*V_i*u[p1_a]*G^ij[p1_a + p2_a]*cu[k1_a]*V_j*v[k2_a]'.t
            //substitute Feynman rules
            M = (V & G) >> M
            //list of Mandelstam & mass shell substitutions
            def mandelstam = setMandelstam(
                    ['p1_m': 'me', 'p2_m': 'me', 'k1_m': 'mu', 'k2_m': 'mu'])
            //simplify matrix element
            M = (EliminateMetrics & ExpandDenominator & mandelstam) >> M
            //complex conjugation of matrix element
            def MC = Conjugate >> M
            MC = 'u[p1_a]*cv[p2_a] = v[p2_a]*cu[p1_a]'.t >> MC
            MC = 'v[k2_a]*cu[k1_a] = u[k1_a]*cv[k2_a]'.t >> MC
            //squared matrix element
            def M2 = ExpandAll >> (M * MC / 4)
            //sum over electron and muon polarizations
            M2 = 'u[p1_a]*cu[p1_a] =  me + p1_a*G^a'.t >> M2
            M2 = 'u[k1_a]*cu[k1_a] =  mu + k1_a*G^a'.t >> M2
            M2 = 'v[p2_a]*cv[p2_a] =  -me + p2_a*G^a'.t >> M2
            M2 = 'v[k2_a]*cv[k2_a] =  -mu + k2_a*G^a'.t >> M2
            //trace of gamma matrices
            M2 <<= DiracTrace['G_a']
            //simplifications
            M2 = (ExpandAndEliminate & mandelstam & 'd^i_i = 4'.t) >> M2
            M2 = 'u = 2*(mu**2 + me**2) - s - t'.t >> M2
            M2 = Factor >> M2

            assertEquals M2, '2*(-4*me**2*t+2*mu**4+4*me**2*mu**2-4*t*mu**2+2*t**2+s**2+2*s*t+2*me**4)*s**(-2)*e**4'.t
        }
    }

    @Test
    public void testComptonScattering() throws Exception {
        use(Redberry) {
            defineMatrices 'G_a', 'V_i', 'D[x_m]', Matrix1.matrix,
                    'vu[p_a]', Matrix1.vector,
                    'cu[p_a]', Matrix1.covector

            def V = 'V_m = -I*e*G_m'.t,
                D = 'D[p_m] = -I*(m + p_m*G^m)/(m**2 - p_m*p^m)'.t,

                Ma = 'cu[p2_m]*V_m*e^m[k2_m]*D[k1_m+p1_m]*V_n*e^n[k1_m]*vu[p1_m]'.t,
                Mb = 'cu[p2_m]*V_m*e^m[k1_m]*D[p1_m-k2_m]*V_n*e^n[k2_m]*vu[p1_m]'.t,

                M = Ma + Mb

            M = (V & D) >> M

            def mandelstam = setMandelstam(
                    ['p1_m': 'm', 'k1_m': '0', 'p2_m': 'm', 'k2_m': '0'])


            M = (ExpandAll & mandelstam) >> M

            def MC = 'vu[p1_m]*cu[p2_m] = vu[p2_m]*cu[p1_m]'.t >> M
            MC = (Conjugate & Reverse[Matrix1]) >> MC


            def M2 = ExpandAll >> (M * MC / 4)

            //photon polarizations
            M2 = 'e_m[k1_a]*e_n[k1_a] = -g_mn'.t >> M2
            M2 = 'e_m[k2_a]*e_n[k2_a] = -g_mn'.t >> M2

            //electron polarizations
            M2 = 'vu[p2_m]*cu[p2_m] =  m + p2^m*G_m'.t >> M2
            M2 = 'vu[p1_m]*cu[p1_m] = m + p1^m*G_m'.t >> M2

            M2 = DiracTrace['G_a'] >> M2

            M2 = (ExpandAndEliminate & 'd^m_m = 4'.t & mandelstam) >> M2
            M2 = 'u = 2*m**2 -s-t'.t >> M2
            M2 = Factor >> M2

            assertEquals M2, '2*(m**2-s-t)**(-2)*(-8*m**6*s+2*s**4+t**3*s-8*m**2*s**3+12*m**4*s**2-2*m**2*t**2*s+2*m**8-m**2*t**3+3*t**2*s**2+4*t*s**3-8*m**2*t*s**2+3*m**4*t**2+4*m**4*s*t)*(-m**2+s)**(-2)*e**4'.t
        }
    }

    @Ignore
    @Test
    public void testComptonScatteringQCD_longTest() throws Exception {
        use(Redberry) {
            //setting up matrix objects
            //unitary matrices
            defineMatrices 'T_A', Matrix2.matrix,
                    //gamma matrices
                    'G5', 'G_a', Matrix1.matrix,
                    //quark wave function
                    'u[p_a]', Matrix1.vector, Matrix2.vector,
                    //its conjugation
                    'cu[p_a]', Matrix1.covector, Matrix2.covector,
                    //quark-gluon vertex
                    'V_iA', Matrix1.matrix, Matrix2.matrix,
                    //quark propagator
                    'D[p_m]', Matrix1.matrix

            //setting up symmetries of tensors
            //SU(N) symmetric constants
            setSymmetric 'd_ABC'
            //SU(N) structure constants
            setAntiSymmetric 'f_ABC'

            //Feynman rules
            //quark-gluon vertex
            def V = 'V_mA = -I*Q*G_m*T_A'.t
            //quark propagator
            def D = 'D[p_m] = -I*(m + p_m*G^m)/(m**2 - p_m*p^m)'.t
            //sum over gluon polarizations in axial gauge
            def P = '''P_mn[k_a] =
            - g_mn + 1/(k_a*n^a)*(k_m*n_n + k_n*n_m)
            - 1/(k_a*n^a)**2 * k_m*k_n'''.t
            //auxiliary vector is unit
            def n2 = 'n_a*n^a = 1'.t
            //gluon propagator
            def G = P >> 'G_mnAB[k_a] = I*g_AB*P_mn[k_a]/(k_a*k^a)'.t
            //3-gluon vertex
            def V3 = '''V_{mnr}^{ABC}[k1_m, k2_m, k3_m] = Q*f^{ABC}*(
               g_mn*(k2_r - k1_r)
              + g_nr*(k3_m - k2_m)
              + g_mr*(k1_n-k3_n))'''.t

            //Matrix element
            //diagram a)
            def Ma = 'cu[k2_m]*V_mA*e^m[p2_m]*D[p1_m+k1_m]*V_nB*e^n[p1_m]*u[k1_m]'.t
            //diagram b)
            def Mb = 'cu[k2_m]*V_mB*e^m[p1_m]*D[k1_m-p2_m]*V_nA*e^n[p2_m]*u[k1_m]'.t
            //diagram c)
            def Mc = '''cu[k2_m]*V_mC*u[k1_m]*G^mnCD[k1_a-k2_a]*
            V_{nabDBA}[k1_a-k2_a, p1_m, -p2_m]*e^a[p1_m]*e^b[p2_m]'''.t
            //matrix element
            def M = Ma + Mb + Mc
            //substitute Feynman rules
            M = (D & G & V & V3) >> M
            //Mandelstam and mass shell substitutions
            def mandelstam = setMandelstam(
                    ['k1_m': 'm', 'p1_m': '0', 'k2_m': 'm', 'p2_m': '0'])
            mandelstam &= n2

            //simplify matrix element
            M = (ExpandAll & EliminateMetrics & n2 & mandelstam) >> M

            //Complex conjugation of matrix element
            def MC = Conjugate >> M
            //exchange spinor momentums
            MC = 'u[k1_m]*cu[k2_m] = u[k2_m]*cu[k1_m]'.t >> MC
            //reorder gamma and SU(N) matrices
            MC = Reverse[Matrix1, Matrix2] >> MC

            //Squared matrix element
            //set M_AB = matrix element
            M = 'M_AB'.eq M
            //set MC_AB = conjugated matrix element
            MC = 'MC_AB'.eq MC
            //squared matrix element
            def M2 = (M & MC) >> 'M_AB*MC^AB/32'.t
            //expand and eliminate contractions with metrics and deltas
            M2 = (ExpandAndEliminate & n2) >> M2

            //Sum over polarizations
            //sum over gluon polarizations
            M2 = ('e_m[p1_a]*e_n[p1_a] = P_mn[p1_a]'.t & P) >> M2
            M2 = ('e_m[p2_a]*e_n[p2_a] = P_mn[p2_a]'.t & P) >> M2
            //sum over quark polarizations
            M2 = 'u[k2_m]*cu[k2_m] = m + k2^m*G_m'.t >> M2
            M2 = 'u[k1_m]*cu[k1_m] = m + k1^m*G_m'.t >> M2

            //trace of gamma matrices
            println 'before tr'
            M2 = ExpandTensors[EliminateMetrics & mandelstam] & EliminateMetrics & mandelstam & DiracTrace[[Gamma: 'G_a', Simplifications: EliminateMetrics & mandelstam]] >> M2
            assert false
            println 'tr'
            //trace of unitary matrices
            M2 = UnitaryTrace >> M2

            //simplifications and substitutions
            M2 = (ExpandAndEliminate & n2 & mandelstam) >> M2
            //simplify combinations of unitary constants
            M2 = UnitarySimplify >> M2

            //performing further simplifications
            M2 = (ExpandAndEliminate & mandelstam & 'd^m_m = 4'.t) >> M2
            //mandelstam u
            M2 = 'u = 2*m**2-s-t'.t >> M2
            //momentum conservation:
            // using to reduce the number of scalar combinations
            M2 = 'p2_a = k1_a + p1_a - k2_a'.t >> M2
            M2 = ExpandAll >> M2
            // replace scalar contractions with auxiliary vector n_m
            // (like p1_m*n^m) with some symbols (e.g. p1_m*n^m = p1n)
            ['k1', 'k2', 'p1'].each {
                M2 = "${it}_a *n^a = ${it}n".t >> M2
            }

            assert TensorUtils.isSymbolic(M2)
            println 'factoring'
            println(Factor >> M2)
        }
    }
}
