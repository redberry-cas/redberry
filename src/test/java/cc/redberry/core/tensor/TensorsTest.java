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
package cc.redberry.core.tensor;

import cc.redberry.core.transformations.Expand;
import cc.redberry.core.transformations.expand.*;
import cc.redberry.core.utils.*;
import org.junit.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorsTest {

    @Test
    public void testRenameConflicts1() {
        Tensor tensor = Tensors.parse("(A_ijk^ij+B_ijk^ij)*K_ij^ij");
        Tensor result = Expand.expand(tensor);
        Tensor expected = Tensors.parse("K_{ij}^{ij}*A_{abk}^{ab}+K_{ij}^{ij}*B_{abk}^{ab}");
        junit.framework.Assert.assertTrue(TensorUtils.compare(result, expected));
    }

    @Test
    public void testRenameConflicts2() {
        Tensor tensor = Tensors.parse("(A_ijk^ij+B_ijk^ij)*(K_ij^ij+T)");
        Tensor result = Expand.expand(tensor);
        Tensor expected = Tensors.parse("(T+K_{ij}^{ij})*B_{abk}^{ab}+(T+K_{ij}^{ij})*A_{abk}^{ab}");
        junit.framework.Assert.assertTrue(TensorUtils.compare(result, expected));
    }

    @Test
    public void testRenameConflicts3() {
        Tensor tensor = Tensors.parse("(A_ijk^ij+B_ijk^ij)*(K_ij^ijk+T^k)*a_ij");
        Tensor result = Expand.expand(tensor);
        Tensor expected = Tensors.parse("T^{k}*A_{abk}^{ab}*a_{ij}+T^{k}*B_{abk}^{ab}*a_{ij}+K_{cd}^{cdk}*A_{abk}^{ab}*a_{ij}+B_{abk}^{ab}*K_{cd}^{cdk}*a_{ij}");
        junit.framework.Assert.assertTrue(TensorUtils.compare(result, expected));
    }

    @Test
    public void testRenameConflicts4() {
        Tensor tensor = Tensors.parse("(A_ij^ijt+B_ijk^ijt*(H_ij^ijk+L_ij^ijk))*(K_ij^ijp+T^p)*a_ijpt");
        Tensor result = Expand.expand(tensor);
        Tensor expected = Tensors.parse("K_{ab}^{abp}*H_{cd}^{cdk}*B_{efk}^{eft}*a_{ijpt}+K_{ab}^{abp}*A_{ef}^{eft}*a_{ijpt}+K_{ab}^{abp}*L_{cd}^{cdk}*B_{efk}^{eft}*a_{ijpt}+T^{p}*L_{cd}^{cdk}*B_{efk}^{eft}*a_{ijpt}+A_{ef}^{eft}*T^{p}*a_{ijpt}+H_{cd}^{cdk}*T^{p}*B_{efk}^{eft}*a_{ijpt}");
        junit.framework.Assert.assertTrue(TensorUtils.compare(result, expected));
    }

    @Test
    public void testRenameConflicts5() {
        Tensor[] tensors = new Tensor[]{Tensors.parse("A_ij^ijk+B^k"), Tensors.parse("B_ik^i+N_jk^jl*L_l")};
        Tensor result = Tensors.multiplyAndRenameConflictingDummies(tensors);
        result = Expand.expand(result);
        Tensor expected = Tensors.parse("B^{k}*N_{bk}^{bl}*L_{l}+A_{ij}^{ijk}*B_{ak}^{a}+A_{ij}^{ijk}*N_{bk}^{bl}*L_{l}+B^{k}*B_{ak}^{a}");
        junit.framework.Assert.assertTrue(TensorUtils.compare(result, expected));
    }

    @Test
    public void testRenameConflicts6() {
        Tensor[] tensors = new Tensor[]{Tensors.parse("A_ij^ijk"), Tensors.parse("(B_ik^i+Y_k)"), Tensors.parse("(C_ijk^ijkl+O^l)")};
        Tensor result = Tensors.multiplyAndRenameConflictingDummies(tensors);
        result = Expand.expand(result);
        Tensor expected = Tensors.parse("Y_{k}*A_{ij}^{ijk}*C_{abc}^{abcl}+A_{ij}^{ijk}*B_{dk}^{d}*C_{abc}^{abcl}+Y_{k}*A_{ij}^{ijk}*O^{l}+A_{ij}^{ijk}*B_{dk}^{d}*O^{l}");
        junit.framework.Assert.assertTrue(TensorUtils.compare(result, expected));
    }
}