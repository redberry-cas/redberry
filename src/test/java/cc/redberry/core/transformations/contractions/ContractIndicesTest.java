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
package cc.redberry.core.transformations.contractions;

import cc.redberry.core.*;
import cc.redberry.core.tensor.Tensor;
import org.junit.*;
import static cc.redberry.core.tensor.Tensors.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ContractIndicesTest {

    private static Tensor contract(String tensor) {
        return ContractIndices.CONTRACT_INDICES.transform(parse(tensor));
    }

    @Test
    public void test01() {
        Tensor t = contract("g_mn*A^mn");
        Tensor e = parse("A^n_n");
        TAssert.assertParity(t, e);
    }

    @Test
    public void test02() {
        Tensor t = contract("d^n_m*A^m_n");
        Tensor e = parse("A^n_n");
        TAssert.assertParity(t, e);
    }

    @Test
    public void test03() {
        Tensor t = contract("d_m^n*A^m_n");
        Tensor e = parse("A^n_n");
        TAssert.assertParity(t, e);
    }

    @Test
    public void test04() {
        Tensor t = contract("d_m^n*d^m_n");
        Tensor e = parse("d^n_n");
        TAssert.assertParity(t, e);
    }

    @Test
    public void test05() {
        Tensor t = contract("g_mn*g^mn");
        Tensor e = parse("d^n_n");
        TAssert.assertParity(t, e);
    }

    @Test
    public void test06() {
        Tensor t = contract("2*a*g_mn*g^mn");
        Tensor e = parse("2*a*d^n_n");
        TAssert.assertParity(t, e);
    }

    @Test
    public void test07() {
        Tensor t = contract("B^ma*g_mn*A^nb");
        Tensor e = parse("B^ma*A_m^b");
        TAssert.assertParity(t, e);
    }

    @Test
    public void test08() {
        Tensor t = contract("B^ma*d_m^n*A_n^b");
        Tensor e = parse("B^ma*A_m^b");
        TAssert.assertParity(t, e);
    }

    @Test
    public void test09() {
        Tensor t = contract("g^mx*g_xa");
        Tensor e = parse("d^m_a");
        TAssert.assertParity(t, e);
    }

    @Test
    public void test010() {
        Tensor t = contract("d^m_x*g^xa");
        Tensor e = parse("g^ma");
        TAssert.assertParity(t, e);
    }

    @Test
    public void test011() {
        Tensor t = contract("d^m_x*d^x_a");
        Tensor e = parse("d^m_a");
        TAssert.assertParity(t, e);
    }

    @Test
    public void test012() {
        Tensor t;
        t = contract("g_mn*g^na*g_ab");
        Tensor e = parse("g_mb");
        TAssert.assertParity(t, e);
        t = contract("g^na*g_mn*g_ab");
        TAssert.assertParity(t, e);
        t = contract("g^na*g_ab*g_mn");
        TAssert.assertParity(t, e);
        t = contract("g_ab*g^na*g_mn");
        TAssert.assertParity(t, e);
    }

    @Test
    public void test013() {
        Tensor t = contract("g_mn*g^mn*g_ab*g^ab");
        Tensor e = parse("d_m^m*d_a^a");
        TAssert.assertParity(t, e);
    }
    
    @Test
    public void test014() {
        Tensor t = contract("g_mn*g^ma*g_ab*g^bn");
        System.out.println(t);
        Tensor e = parse("d_m^m");
        TAssert.assertParity(t, e);
    }
}