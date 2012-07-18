package cc.redberry.core.transformations.expand;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static org.junit.Assert.assertTrue;


public class DummyIndicesTest {
    @Test
    public void test1() {
        Tensor tensor = parse("(A_ijk^ij+B_ijk^ij)*K_ij^ij");
        Tensor result = ExpandBrackets.expandBrackets(tensor);
        Tensor expected = parse("K_{ij}^{ij}*A_{abk}^{ab}+K_{ij}^{ij}*B_{abk}^{ab}");
        assertTrue(TensorUtils.compare(result, expected));
    }

    @Test
    public void test2() {
        Tensor tensor = parse("(A_ijk^ij+B_ijk^ij)*(K_ij^ij+T)");
        Tensor result = ExpandBrackets.expandBrackets(tensor);
        Tensor expected = parse("(T+K_{ij}^{ij})*B_{abk}^{ab}+(T+K_{ij}^{ij})*A_{abk}^{ab}");
        assertTrue(TensorUtils.compare(result, expected));
    }

    @Test
    public void test3() {
        Tensor tensor = parse("(A_ijk^ij+B_ijk^ij)*(K_ij^ijk+T^k)*a_ij");
        Tensor result = ExpandBrackets.expandBrackets(tensor);
        Tensor expected = parse("T^{k}*A_{abk}^{ab}*a_{ij}+T^{k}*B_{abk}^{ab}*a_{ij}+K_{cd}^{cdk}*A_{abk}^{ab}*a_{ij}+B_{abk}^{ab}*K_{cd}^{cdk}*a_{ij}");
        assertTrue(TensorUtils.compare(result, expected));
    }

    @Test
    public void test4() {
        Tensor tensor = parse("(A_ij^ijt+B_ijk^ijt*(H_ij^ijk+L_ij^ijk))*(K_ij^ijp+T^p)*a_ijpt");
        Tensor result = ExpandBrackets.expandBrackets(tensor);
        Tensor expected = parse("K_{ab}^{abp}*H_{cd}^{cdk}*B_{efk}^{eft}*a_{ijpt}+K_{ab}^{abp}*A_{ef}^{eft}*a_{ijpt}+K_{ab}^{abp}*L_{cd}^{cdk}*B_{efk}^{eft}*a_{ijpt}+T^{p}*L_{cd}^{cdk}*B_{efk}^{eft}*a_{ijpt}+A_{ef}^{eft}*T^{p}*a_{ijpt}+H_{cd}^{cdk}*T^{p}*B_{efk}^{eft}*a_{ijpt}");
        assertTrue(TensorUtils.compare(result, expected));
    }

    @Test
    public void test5() {
        Tensor[] tensors = new Tensor[]{parse("A_ij^ijk+B^k"), parse("B_ik^i+N_jk^jl*L_l")};
        Tensor result = Tensors.multiplyAndRenameConflictingDummies(tensors);
        result = ExpandBrackets.expandBrackets(result);
        Tensor expected = parse("B^{k}*N_{bk}^{bl}*L_{l}+A_{ij}^{ijk}*B_{ak}^{a}+A_{ij}^{ijk}*N_{bk}^{bl}*L_{l}+B^{k}*B_{ak}^{a}");
        assertTrue(TensorUtils.compare(result, expected));
    }

    @Test
    public void test6() {
        Tensor[] tensors = new Tensor[]{parse("A_ij^ijk"), parse("(B_ik^i+Y_k)"), parse("(C_ijk^ijkl+O^l)")};
        Tensor result = Tensors.multiplyAndRenameConflictingDummies(tensors);
        result = ExpandBrackets.expandBrackets(result);
        Tensor expected = parse("Y_{k}*A_{ij}^{ijk}*C_{abc}^{abcl}+A_{ij}^{ijk}*B_{dk}^{d}*C_{abc}^{abcl}+Y_{k}*A_{ij}^{ijk}*O^{l}+A_{ij}^{ijk}*B_{dk}^{d}*O^{l}");
        assertTrue(TensorUtils.compare(result, expected));
    }
}
