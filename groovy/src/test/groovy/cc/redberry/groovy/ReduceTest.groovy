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

import cc.redberry.core.utils.TensorUtils
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName

import static cc.redberry.core.TAssert.*
import static cc.redberry.core.tensor.Tensors.addSymmetry
import static cc.redberry.groovy.RedberryStatic.*

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class ReduceTest {
    @Rule
    public TestName name = new TestName();

    public final String mapleBinDir;
    public final String temporaryDir;
    public final String mathematicaBinDir;

    public ReduceTest() {
        String mapleBinDir;
        mapleBinDir = "/home/stas/maple13/bin";
        if (!new File(mapleBinDir + "/maple").exists()) {
            mapleBinDir = System.getenv("MAPLE");
            if (mapleBinDir == null)
                mapleBinDir = System.getProperty("redberry.maple");
            if (mapleBinDir != null) {
                //check maple
                File maple = new File(mapleBinDir + "/maple");
                if (!maple.exists())
                    mapleBinDir = null;
                else {
                    //todo check licence
                }
            }
        }

        this.mapleBinDir = mapleBinDir;

        String mathematicaBinDir = "/usr/local/bin";
        File mathematicaScriptExecutor = new File(mathematicaBinDir + "/MathematicaScript");
        if (!mathematicaScriptExecutor.exists())
            mathematicaBinDir = null;

        this.mathematicaBinDir = mathematicaBinDir;
        temporaryDir = System.getProperty("java.io.tmpdir");
    }

    private String getPath(String solver) {
        switch (solver) {
            case 'Maple': return mapleBinDir;
            case 'Mathematica': return mathematicaBinDir;
            default:
                return null;
        }
    }

    @Before
    public void beforeMethod() {
        def system = name.methodName.split('_')
        if (system.length > 2) {
            if (system.equals('Maple'))
                Assume.assumeTrue(mapleBinDir != null)
            if (system.equals('Mathematica'))
                Assume.assumeTrue(mathematicaBinDir != null)
        }
    }

    @Test
    public void testReduce1_Mathematica() {
        use(Redberry) {
            def options = [ExternalSolver: [Solver: 'Mathematica', Path: mathematicaBinDir]]
            assertEquals Reduce(['F_mn + F_nm + A_mn + A_nm = 0', '1/2*F_mn + F_nm + A_mn + 1/2*A_nm = 0'], ['F_mn'], options)[0][0], 'F_{mn} = -A_{nm}'
        }
    }

    @Test
    public void testReduce1_Maple() {
        use(Redberry) {
            def options = [ExternalSolver: [Solver: 'Maple', Path: mapleBinDir]]
            assertEquals Reduce(['F_mn + F_nm + A_mn + A_nm = 0', '1/2*F_mn + F_nm + A_mn + 1/2*A_nm = 0'], ['F_mn'], options)[0][0], 'F_{mn} = -A_{nm}'
        }
    }


    @Test
    public void testReduce2_Mathematica() {
        use(Redberry) {
            def F = 'F_p^mn_q^rs = d^s_q*d^r_p*g^mn + d^m_q*d^n_p*g^rs - d^r_p*d^n_q*g^ms - d^s_p*d^m_q*g^rn'.t
            def equation = [F >> 'F_p^mn_q^rs*iF^p_mn^a_bc = d^a_q*d_b^r*d_c^s - x*d^r_q*d_b^a*d_c^s'.t]
            def s = Reduce(equation, ['iF^p_mn^a_bc', 'x'], [Transformations: 'd_m^m = 4'.t, ExternalSolver: [Solver: 'Mathematica', Path: mathematicaBinDir]])
            assertTrue s[0].find { it == 'x = 1/4'.t } != null
        }
    }

    @Test
    public void testReduce2_Maple() {
        use(Redberry) {
            def F = 'F_p^mn_q^rs = d^s_q*d^r_p*g^mn + d^m_q*d^n_p*g^rs - d^r_p*d^n_q*g^ms - d^s_p*d^m_q*g^rn'.t
            def equation = [F >> 'F_p^mn_q^rs*iF^p_mn^a_bc = d^a_q*d_b^r*d_c^s - x*d^r_q*d_b^a*d_c^s'.t]
            def s = Reduce(equation, ['iF^p_mn^a_bc', 'x'], [Transformations: 'd_m^m = 4'.t, ExternalSolver: [Solver: 'Maple', Path: mapleBinDir]])
            assertTrue s[0].find { it == 'x = 1/4'.t } != null
        }
    }


    @Test
    public void testReduce3() {
        use(Redberry) {
            for (def externalSolver in ['Maple', 'Mathematica']) {
                if (getPath(externalSolver) == null)
                    continue

                def genTensor = GenerateTensorWithCoefficients('_abc'.si, ['g_mn', 'k_m'])
                def equations = ['F_mnp*F^mnp = 1', 'F_abc'.eq(genTensor[0]), 'F_abc*F^ab_d*F^cde = k^e']
                def options = [Transformations: 'd_m^m = 1'.t & 'k_m*k^m = 1'.t, SymmetricForm: true, ExternalSolver: [Solver: externalSolver, Path: getPath(externalSolver), TmpDir: '/home/stas/Projects/redberry']]

                def s = Reduce(equations, ['F_abc'.t, * genTensor[1]], options)
                def result = s.collect { it[0] }

                def generatedVar
                result.each { r -> r.parentAfterChild { term -> if (TensorUtils.isSymbolic(term)) generatedVar = term; return; } }
                assertEquals result.size(), 1

                result = result[0]
                def check1 = result >> 'F_abc = k_a*k_b*k_c'.t
                def check2 = result >> 'F_abc = (k_a*g_bc + k_b*g_ac + k_c*g_ab)/3'.t

                assertFalse Reduce([check1], [generatedVar], options).isEmpty()
                assertFalse Reduce([check2], [generatedVar], options).isEmpty()
            }
        }
    }


    @Test
    public void testReduce4() {
        use(Redberry) {
            for (def externalSolver in ['Maple', 'Mathematica']) {
                if (getPath(externalSolver) == null)
                    continue

                def genTensor = GenerateTensorWithCoefficients('_{ab cd}'.si, ['g_mn', 'k_m'])
                def equations = ['F_{ab cd}*F^{ab pq} = F_{cd}^{pq}', 'F_{ab cd} = F_{cd ab}', 'F_{ba cd} = F_{cd ab}', 'F_a^acd = 0', 'k^a*F_abcd = 0', 'F_abcd'.eq(genTensor[0])]
                def options = [Transformations: 'd_m^m = 4'.t & 'k_m*k^m = M**2'.t, ExternalSolver: [Solver: externalSolver, Path: getPath(externalSolver), TmpDir: '/home/stas/Projects/redberry']]

                def s = Reduce(equations, ['F_abcd'.t, * genTensor[1]] as Collection, options)
                def result = s.collect { it[0] }
                result = result.find({ it[1] != 0.t })[1]

                def J = 'J_ab = k_a*k_b/M**2 - g_ab'.t
                def J2 = (J & Expand) >> '(J_ac*J_bd + J_ad*J_bc)/2 -J_ab*J_cd/3'.t
                assertEquals result - J2, 0.t
            }
        }
    }

    @Test
    public void testReduce5() {
        use(Redberry) {
            for (def externalSolver in ['Maple', 'Mathematica']) {
                if (getPath(externalSolver) == null)
                    continue
                addSymmetry 'F_abcd', 1, 0, 2, 3
                addSymmetry 'F_abcd', 2, 3, 0, 1

                def genTensor = GenerateTensorWithCoefficients('_{ab cd}'.si, ['g_mn', 'k_m'])
                def equations = ['F_{ab cd}*F^{ab pq} = F_{cd}^{pq}', 'F_a^acd = 0', 'k^a*F_abcd = 0', 'F_abcd'.eq(genTensor[0])]
                def options = [Transformations: 'd_m^m = 4'.t & 'k_m*k^m = M**2'.t, ExternalSolver: [Solver: externalSolver, Path: getPath(externalSolver), TmpDir: '/home/stas/Projects/redberry']]

                def s = Reduce(equations, ['F_abcd'.t, * genTensor[1]] as Collection, options)
                def result = s.collect { it[0] }
                result = result.find({ it[1] != 0.t })[1]


                def J = 'J_ab = k_a*k_b/M**2 - g_ab'.t
                def J2 = (J & Expand) >> '(J_ac*J_bd + J_ad*J_bc)/2 -J_ab*J_cd/3'.t
                assertEquals result - J2, 0.t
            }
        }
    }

    @Test
    public void testReduce6() {
        use(Redberry) {
            for (def externalSolver in ['Maple', 'Mathematica']) {
                if (getPath(externalSolver) == null)
                    continue
                addSymmetry 'F_abcd', 1, 0, 2, 3
                addSymmetry 'F_abcd', 2, 3, 0, 1

                def genTensor = GenerateTensorWithCoefficients('_{ab cd}'.si, ['g_mn', 'k_m'])
                def equations = ['F_{ab cd}*F^{ab pq} = F_{cd}^{pq}', 'F_a^acd = 0', 'F_abcd*F^acbd = F^a_ba^b/2', 'F_abcd'.eq(genTensor[0])]
                def options = [Transformations: 'd_m^m = 4'.t & 'k_m*k^m = M**2'.t, ExternalSolver: [Solver: externalSolver, Path: getPath(externalSolver), TmpDir: '/home/stas/Projects/redberry']]

                def s = Reduce(equations, ['F_abcd'.t, * genTensor[1]], options)
                def result = s.collect { it[0] }
                result = result.find({ it[1] != 0.t })[1]
                assertEquals result, '-2*M**(-4)*k_{a}*k_{b}*k_{c}*k_{d}+(1/2)*M**(-2)*g_{bc}*k_{a}*k_{d}+(1/2)*M**(-2)*g_{ac}*k_{b}*k_{d}+(1/2)*M**(-2)*g_{ad}*k_{b}*k_{c}+(1/2)*M**(-2)*g_{bd}*k_{a}*k_{c}'
            }
        }
    }

    @Test
    public void testReduce7() {
        use(Redberry) {
            for (def externalSolver in ['Maple', 'Mathematica']) {
                if (getPath(externalSolver) == null)
                    continue
                def eq1 = 'J_ac*J^ab + 3*J_c^b = -2*d^{b}_{c}'.t
                def eq2 = 'k^a*J_ab = -k_b'.t
                def eq3 = 'J_ab*J^ab = 9'.t
                def options = [Transformations: 'd_n^n = dim'.t, ExternalSolver: [Solver: externalSolver, Path: getPath(externalSolver)]]
                def s = Reduce([eq1, eq2, eq3], ['dim', 'J_ab'], options)
                println s
            }
        }
    }

    @Test
    public void testReduce8() {
        use(Redberry) {
            for (def externalSolver in [/*'Maple', */ 'Mathematica']) {
                if (getPath(externalSolver) == null)
                    continue
                def eq = '(2/3)*(J_{b}^{bpq}*g_{cd}+J_{dc}^{pq}+J_{cd}^{pq}) = (1/2)*(d_{c}^{q}*d_{d}^{p}+d_{c}^{p}*d_{d}^{q})+2*g_{cd}*g^{pq}'.t
                def options = [Transformations: 'd^m_m = 4'.t, SymmetricForm: [false], ExternalSolver: [Solver: externalSolver, Path: getPath(externalSolver)]]
                def s = Reduce([eq], ['J_abcd'], options)
                println s
            }
        }
    }
}
