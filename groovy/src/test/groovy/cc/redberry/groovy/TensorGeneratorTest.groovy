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
import org.junit.Test

import static cc.redberry.groovy.RedberryStatic.Collect
import static cc.redberry.groovy.RedberryStatic.FindIndicesSymmetries
import static cc.redberry.groovy.RedberryStatic.GenerateTensor

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class TensorGeneratorTest {

    @Test
    public void testSymmetries1() throws Exception {
        use(Redberry) {
            def indices = '_{abcd}'.si
            indices.symmetries.add(-[0, 2, 1, 3].p)

            def t = GenerateTensor(indices, ['g_ab', 'k_a'])
            t = Collect['C[x]'.t] >> t
            assert indices.symmetries.permutationGroup ==
                    new PermutationGroup(FindIndicesSymmetries(indices, t))
        }
    }

    @Test
    public void testSymmetricForm1() throws Exception {
        use(Redberry) {
            def indices = '_{abc}'.si
            def t = GenerateTensor(indices, ['g_mn', 'k_m'], [SymmetricForm: true])
            assert new PermutationGroup(FindIndicesSymmetries(indices, t)).isSymmetric()
        }
    }

    @Test
    public void testGeneratedParameters1() throws Exception {
        use(Redberry) {
            def t = GenerateTensor('_{ab}'.si, ['g_mn', 'k_m'], [GeneratedParameters: { i -> "K$i".t }])
            assert t == 'K1*g_{ab}+K0*k_{a}*k_{b}'.t || t == 'K0*g_{ab}+K1*k_{a}*k_{b}'.t
        }
    }

    @Test
    public void testGenerateParameters1() throws Exception {
        use(Redberry) {
            def t = GenerateTensor('_{ab}'.si, ['g_mn', 'k_m'], [GenerateParameters: false])
            assert t == 'g_{ab} + k_{a}*k_{b}'.t
        }
    }

    @Test
    public void testRaiseLower1() throws Exception {
        use(Redberry) {
            def t = GenerateTensor('_{ab}^{cd}'.si, ['g_mn', 'k_m', 'k^m'], [RaiseLower: false])
            assert t == 'C[0]*g_{ab}*k^{c}*k^{d}+C[1]*k_{a}*k_{b}*k^{c}*k^{d}'.t ||
                    t == 'C[1]*g_{ab}*k^{c}*k^{d}+C[0]*k_{a}*k_{b}*k^{c}*k^{d}'
        }
    }
}
