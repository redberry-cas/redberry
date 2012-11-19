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
 * the Free Software Foundation, either version 2 of the License, or
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
package cc.redberry.core.transformations.symmetrization;

import cc.redberry.core.TAssert;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;


/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SymmetrizeUpperLowerIndicesTest {

    public SymmetrizeUpperLowerIndicesTest() {
    }

    @Test
    public void test1() {
        Tensor expected = Tensors.parse("g_mn*g_ab+g_ma*g_nb+g_mb*g_an");
        TAssert.assertEquals(expected, SymmetrizeUpperLowerIndices.symmetrizeUpperLowerIndices(Tensors.parse("g_mn*g_ab")));
    }

    @Test
    public void test2() {
        Tensor expected = Tensors.parse("g_mn*g^ab");
        TAssert.assertEquals(expected, SymmetrizeUpperLowerIndices.symmetrizeUpperLowerIndices(Tensors.parse("g_mn*g^ab")));
    }

    @Test
    public void test3() {
        Tensor expected = Tensors.parse(""
                + " g_{mn}*g_{ab}*g^{pq}*g^{rs}"
                + "+g_{mn}*g_{ab}*g^{pr}*g^{qs}"
                + "+g_{mn}*g_{ab}*g^{ps}*g^{qr}"
                + "+g_{bn}*g_{am}*g^{pq}*g^{rs}"
                + "+g_{bn}*g_{am}*g^{pr}*g^{qs}"
                + "+g_{bn}*g_{am}*g^{ps}*g^{qr}"
                + "+g_{bm}*g_{an}*g^{pq}*g^{rs}"
                + "+g_{bm}*g_{an}*g^{pr}*g^{qs}"
                + "+g_{bm}*g_{an}*g^{ps}*g^{qr}");
        TAssert.assertEquals(expected, SymmetrizeUpperLowerIndices.symmetrizeUpperLowerIndices(Tensors.parse("g_ab*g^rs*g^pq*g_mn")));
    }
}
