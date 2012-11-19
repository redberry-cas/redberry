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
package cc.redberry.core.tensor;

import cc.redberry.core.TAssert;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.number.Complex;
import cc.redberry.core.transformations.ContractIndices;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.expand.Expand;
import cc.redberry.core.transformations.expand.ExpandAll;
import cc.redberry.core.transformations.expand.ExpandNumerator;
import cc.redberry.core.transformations.fractions.Together;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorsTest {

    @Test
    public void testRenameConflicts1() {
        Tensor tensor = parse("(A_ijk^ij+B_ijk^ij)*K_ij^ij");
        Tensor result = Expand.expand(tensor);
        Tensor expected = parse("K_{ij}^{ij}*A_{abk}^{ab}+K_{ij}^{ij}*B_{abk}^{ab}");
        junit.framework.Assert.assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void testRenameConflicts2() {
        Tensor tensor = parse("(A_ijk^ij+B_ijk^ij)*(K_ij^ij+T)");
        Tensor result = Expand.expand(tensor);
        Tensor expected = parse("T*B_{abk}^{ab}+K_{ij}^{ij}*B_{abk}^{ab}+T*A_{abk}^{ab}+K_{ij}^{ij}*A_{abk}^{ab}");
        junit.framework.Assert.assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void testRenameConflicts3() {
        Tensor tensor = parse("(A_ijk^ij+B_ijk^ij)*(K_ij^ijk+T^k)*a_ij");
        Tensor result = Expand.expand(tensor);
        Tensor expected = parse("T^{k}*A_{abk}^{ab}*a_{ij}+T^{k}*B_{abk}^{ab}*a_{ij}+K_{cd}^{cdk}*A_{abk}^{ab}*a_{ij}+B_{abk}^{ab}*K_{cd}^{cdk}*a_{ij}");
        junit.framework.Assert.assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void testRenameConflicts4() {
        Tensor tensor = parse("(A_ij^ijt+B_ijk^ijt*(H_ij^ijk+L_ij^ijk))*(K_ij^ijp+T^p)*a_ijpt");
        Tensor result = Expand.expand(tensor);
        Tensor expected = parse("K_{ab}^{abp}*H_{cd}^{cdk}*B_{efk}^{eft}*a_{ijpt}+K_{ab}^{abp}*A_{ef}^{eft}*a_{ijpt}+K_{ab}^{abp}*L_{cd}^{cdk}*B_{efk}^{eft}*a_{ijpt}+T^{p}*L_{cd}^{cdk}*B_{efk}^{eft}*a_{ijpt}+A_{ef}^{eft}*T^{p}*a_{ijpt}+H_{cd}^{cdk}*T^{p}*B_{efk}^{eft}*a_{ijpt}");
        junit.framework.Assert.assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void testRenameConflicts5() {
        Tensor[] tensors = new Tensor[]{parse("A_ij^ijk+B^k"), parse("B_ik^i+N_jk^jl*L_l")};
        Tensor result = multiplyAndRenameConflictingDummies(tensors);
        result = Expand.expand(result);
        Tensor expected = parse("B^{k}*N_{bk}^{bl}*L_{l}+A_{ij}^{ijk}*B_{ak}^{a}+A_{ij}^{ijk}*N_{bk}^{bl}*L_{l}+B^{k}*B_{ak}^{a}");
        junit.framework.Assert.assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void testRenameConflicts6() {
        Tensor[] tensors = new Tensor[]{parse("A_ij^ijk"), parse("(B_ik^i+Y_k)"), parse("(C_ijk^ijkl+O^l)")};
        Tensor result = multiplyAndRenameConflictingDummies(tensors);
        result = Expand.expand(result);
        Tensor expected = parse("Y_{k}*A_{ij}^{ijk}*C_{abc}^{abcl}+A_{ij}^{ijk}*B_{dk}^{d}*C_{abc}^{abcl}+Y_{k}*A_{ij}^{ijk}*O^{l}+A_{ij}^{ijk}*B_{dk}^{d}*O^{l}");
        junit.framework.Assert.assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void testProductPower1() {
        Tensor t = Tensors.parse("Power[2*a,3]");
        Tensor expected = Tensors.parse("8*Power[a,3]");
        Assert.assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testPowerPower1() {
        Tensor t = Tensors.parse("Power[a,4]*Power[Power[a,2],-2]");
        Assert.assertTrue(TensorUtils.isOne(t));
    }

    @Test
    public void testSum1() {
        Tensor actual = parse("(x_a^a+y_b^b)*X_m*X^m - (z_n^n+y_d^d)*X_a*X^a ");
        Tensor expected = parse("(x_a^a-z_n^n)*X_m*X^m");
        TAssert.assertEquals(actual, expected);
    }


    @Test
    public void testSum2() {
        Tensor actual = parse("a+b-(a+b)");
        TAssert.assertTrue(TensorUtils.isZero(actual));
    }

    @Test
    public void testSum3() {
        SimpleTensor r = parseSimple("R_abcd");
        addSymmetry(r, IndexType.LatinLower, false, 2, 3, 0, 1);
        addSymmetry(r, IndexType.LatinLower, true, 1, 0, 2, 3);
        Tensor actual = parse("R^abcd*R_abcd + R^abcd*R_abdc");
        TAssert.assertEquals(actual, Complex.ZERO);
    }

    @Test
    public void testSum4() {
        Tensors.addSymmetry("R_\\mu\\nu", IndexType.GreekLower, false, new int[]{1, 0});
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, true, new int[]{0, 1, 3, 2});
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, false, new int[]{2, 3, 0, 1});
        Tensor t = parse("d^{\\alpha}_{\\sigma}*g^{\\beta\\gamma}*R^{\\sigma}_{\\alpha\\beta\\gamma}+g^{\\alpha\\gamma}*d^{\\beta}_{\\sigma}*R^{\\sigma}_{\\alpha\\beta\\gamma}+g^{\\alpha\\beta}*d^{\\gamma}_{\\sigma}*R^{\\sigma}_{\\alpha\\beta\\gamma}");
        TAssert.assertEquals(t, parse("g^{\\beta\\gamma}*d^{\\alpha}_{\\sigma}*R^{\\sigma}_{\\alpha\\beta\\gamma}"));
    }
      @Test
      public void testSum5(){
          Tensor t = parse("(a-2*b)*c**2/(a-b) + (-a+2*b)*c**(2)/(b-a)");
          TAssert.assertEquals(t, "2*(a-2*b)*c**2/(a-b)");
      }
//    @Test
//    public void riemannInvariants3() {
//        SimpleTensor r = parseSimple("R_abcd");
//        addSymmetry(r, IndexType.LatinLower, false, 2, 3, 0, 1);
//        addSymmetry(r, IndexType.LatinLower, true, 1, 0, 2, 3);
//
//        Tensor t = parse("R^abcd*R^efkl*R^mnop");
//        int[] upper = ParserIndices.parseSimple("^abcdef").getAllIndices().copy();
//        int[] lower = ParserIndices.parseSimple("_klmnop").getAllIndices().copy();
//        IntPermutationsGenerator generator = new IntPermutationsGenerator(6);
//        Set<Wrr> set = new HashSet<>();
//        for (int[] permutation : generator) {
//            IntArrayList from = new IntArrayList(upper.length * 2);
//            IntArrayList to = new IntArrayList(upper.length * 2);
//            for (int i = 0; i < permutation.length; ++i) {
//                from.add(upper[i]);
//                from.add(0x80000000 | lower[permutation[i]]);
//                to.add(0x80000000 | lower[permutation[i]]);
//                to.add(lower[permutation[i]]);
//            }
////            System.out.println(IndicesUtils.toString(from.toArray()));
////            System.out.println(IndicesUtils.toString(to.toArray()));
//            Product att = (Product) ApplyIndexMapping.applyIndexMapping(t, from.toArray(), to.toArray(), new int[0]);
//            if (att.factor.isMinusOne()) {
//                System.out.println(att + "   asasasas");
//            }
//            set.add(new Wrr(att));
//        }
//
//        Expression[] expressions = {parseExpression("R^a_man = R_mn"), parseExpression("R^a_a = R")};
//        System.out.println(set.size());
//        for (Wrr wrr : set) {
//            Tensor tt = wrr.tensor;
//            tt = expressions[0].transform(tt);
//            tt = expressions[1].transform(tt);
//            System.out.println(tt);
//        }
//    }
//
//    private static class Wrr {
//        final Tensor tensor;
//
//        private Wrr(Tensor tensor) {
//            this.tensor = tensor;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            return TensorUtils.compare1(tensor, ((Wrr) o).tensor) != null;
//        }
//
//        @Override
//        public int hashCode() {
//            return tensor.hashCode();
//        }
//    }

    private static Expression expression(String expression) {
        return (Expression) parse(expression);
    }

    @Test
    public void example1() {
        Tensor target = parse("Power[M, 14] - 135*s*Power[M, 14] + 27*Power[M, 16] + 45*Power[M, 20] - 211*s*Power[M, 12]*Power[pT, 2] + 38*Power[M, 14]*Power[pT, 2] + 2*Power[M, 12]*(-3*s + Power[pT, 2]) + 45*Power[M, 18]*(-2*s + Power[pT, 2]) + 60*Power[M, 18]*(-3*s + 2*Power[pT, 2]) - 374*s*Power[M, 10]*Power[pT, 4] + 38*Power[M, 12]*Power[pT, 4] - 92*s*Power[M, 8]*Power[pT, 6] + 27*Power[M, 10]*Power[pT, 6] - 54*s*Power[M, 6]*Power[pT, 8] + 324*Power[M, 12]*Power[s, 2] + 616*Power[M, 10]*Power[pT, 2]*Power[s, 2] + 882*Power[M, 8]*Power[pT, 4]*Power[s, 2] + 698*Power[M, 6]*Power[pT, 6]*Power[s, 2] + 135*Power[M, 4]*Power[pT, 8]*Power[s, 2] + 27*Power[M, 2]*Power[pT, 10]*Power[s, 2] + Power[M, 10]*(-11*s*Power[pT, 2] + 3*Power[pT, 4] + 16*Power[s, 2]) + 12*Power[M, 16]*(-24*s*Power[pT, 2] + 6*Power[pT, 4] + 17*Power[s, 2]) + 3*Power[M, 16]*(-41*s*Power[pT, 2] + 8*Power[pT, 4] + 34*Power[s, 2]) + 3*Power[M, 16]*(-59*s*Power[pT, 2] + 12*Power[pT, 4] + 51*Power[s, 2]) + 6*Power[M, 16]*(-70*s*Power[pT, 2] + 18*Power[pT, 4] + 51*Power[s, 2]) + Power[M, 14]*(-456*s*Power[pT, 4] + 48*Power[pT, 6] + 771*Power[pT, 2]*Power[s, 2] - 360*Power[s, 3]) + 3*Power[M, 14]*(-55*s*Power[pT, 4] + 6*Power[pT, 6] + 118*Power[pT, 2]*Power[s, 2] - 60*Power[s, 3]) + 12*Power[M, 14]*(-54*s*Power[pT, 4] + 6*Power[pT, 6] + 98*Power[pT, 2]*Power[s, 2] - 45*Power[s, 3]) + 9*Power[M, 14]*(-27*s*Power[pT, 4] + 3*Power[pT, 6] + 59*Power[pT, 2]*Power[s, 2] - 30*Power[s, 3]) + 2*Power[M, 8]*(-5*s*Power[pT, 4] + Power[pT, 6] + 15*Power[pT, 2]*Power[s, 2] - 12*Power[s, 3]) - 486*Power[M, 10]*Power[s, 3] - 1091*Power[M, 8]*Power[pT, 2]*Power[s, 3] - 1557*Power[M, 6]*Power[pT, 4]*Power[s, 3] - 1087*Power[M, 4]*Power[pT, 6]*Power[s, 3] - 346*Power[M, 2]*Power[pT, 8]*Power[s, 3] - 27*Power[pT, 10]*Power[s, 3] + s*Power[M, 10]*(616*s*Power[pT, 6] - 87*Power[pT, 8] - 1365*Power[pT, 4]*Power[s, 2] + 1089*Power[pT, 2]*Power[s, 3] - 270*Power[s, 4]) + s*Power[M, 10]*(310*s*Power[pT, 6] - 63*Power[pT, 8] - 696*Power[pT, 4]*Power[s, 2] + 651*Power[pT, 2]*Power[s, 3] - 180*Power[s, 4]) + s*Power[M, 4]*(13*s*Power[pT, 6] - 3*Power[pT, 8] - 33*Power[pT, 4]*Power[s, 2] + 39*Power[pT, 2]*Power[s, 3] - 10*Power[s, 4]) + 486*Power[M, 8]*Power[s, 4] + 1134*Power[M, 6]*Power[pT, 2]*Power[s, 4] + 1340*Power[M, 4]*Power[pT, 4]*Power[s, 4] + 665*Power[M, 2]*Power[pT, 6]*Power[s, 4] - 27*Power[pT, 8]*Power[s, 4] + Power[M, 6]*(-6*s*Power[pT, 6] + Power[pT, 8] + 22*Power[pT, 4]*Power[s, 2] - 47*Power[pT, 2]*Power[s, 3] + 21*Power[s, 4]) - 4*s*Power[M, 10]*(-377*s*Power[pT, 6] + 51*Power[pT, 8] + 753*Power[pT, 4]*Power[s, 2] - 561*Power[pT, 2]*Power[s, 3] + 135*Power[s, 4]) - 2*s*Power[M, 10]*(-391*s*Power[pT, 6] + 72*Power[pT, 8] + 747*Power[pT, 4]*Power[s, 2] - 651*Power[pT, 2]*Power[s, 3] + 180*Power[s, 4]) + 2*Power[M, 12]*(-180*s*Power[pT, 6] + 6*Power[pT, 8] + 557*Power[pT, 4]*Power[s, 2] - 612*Power[pT, 2]*Power[s, 3] + 216*Power[s, 4]) + Power[M, 12]*(-129*s*Power[pT, 6] + 6*Power[pT, 8] + 463*Power[pT, 4]*Power[s, 2] - 591*Power[pT, 2]*Power[s, 3] + 216*Power[s, 4]) + 2*Power[M, 12]*(-252*s*Power[pT, 6] + 9*Power[pT, 8] + 920*Power[pT, 4]*Power[s, 2] - 1008*Power[pT, 2]*Power[s, 3] + 324*Power[s, 4]) + Power[M, 12]*(-189*s*Power[pT, 6] + 9*Power[pT, 8] + 769*Power[pT, 4]*Power[s, 2] - 951*Power[pT, 2]*Power[s, 3] + 324*Power[s, 4]) + Power[M, 6]*Power[s, 2]*(-218*s*Power[pT, 8] + 75*Power[pT, 10] + 513*Power[pT, 6]*Power[s, 2] - 842*Power[pT, 4]*Power[s, 3] + 483*Power[pT, 2]*Power[s, 4] - 72*Power[s, 5]) + Power[M, 6]*Power[s, 2]*(-439*s*Power[pT, 8] + 81*Power[pT, 10] + 898*Power[pT, 6]*Power[s, 2] - 844*Power[pT, 4]*Power[s, 3] + 357*Power[pT, 2]*Power[s, 4] - 54*Power[s, 5]) + Power[M, 6]*Power[s, 2]*(-115*s*Power[pT, 8] + 39*Power[pT, 10] + 271*Power[pT, 6]*Power[s, 2] - 433*Power[pT, 4]*Power[s, 3] + 243*Power[pT, 2]*Power[s, 4] - 36*Power[s, 5]) + 4*Power[M, 6]*Power[s, 2]*(-227*s*Power[pT, 8] + 42*Power[pT, 10] + 456*Power[pT, 6]*Power[s, 2] - 425*Power[pT, 4]*Power[s, 3] + 180*Power[pT, 2]*Power[s, 4] - 27*Power[s, 5]) - 324*Power[M, 6]*Power[s, 5] - 702*Power[M, 4]*Power[pT, 2]*Power[s, 5] - 697*Power[M, 2]*Power[pT, 4]*Power[s, 5] - 27*Power[pT, 6]*Power[s, 5] + s*Power[M, 8]*(133*s*Power[pT, 8] - 12*Power[pT, 10] - 381*Power[pT, 6]*Power[s, 2] + 665*Power[pT, 4]*Power[s, 3] - 492*Power[pT, 2]*Power[s, 4] + 102*Power[s, 5]) + 2*s*Power[M, 8]*(149*s*Power[pT, 8] - 12*Power[pT, 10] - 407*Power[pT, 6]*Power[s, 2] + 663*Power[pT, 4]*Power[s, 3] - 486*Power[pT, 2]*Power[s, 4] + 102*Power[s, 5]) + 2*s*Power[M, 8]*(344*s*Power[pT, 8] - 18*Power[pT, 10] - 1142*Power[pT, 6]*Power[s, 2] + 1476*Power[pT, 4]*Power[s, 3] - 810*Power[pT, 2]*Power[s, 4] + 153*Power[s, 5]) + s*Power[M, 8]*(301*s*Power[pT, 8] - 18*Power[pT, 10] - 1041*Power[pT, 6]*Power[s, 2] + 1415*Power[pT, 4]*Power[s, 3] - 798*Power[pT, 2]*Power[s, 4] + 153*Power[s, 5]) + 135*Power[M, 4]*Power[s, 6] + 243*Power[M, 2]*Power[pT, 2]*Power[s, 6] + 108*Power[pT, 4]*Power[s, 6] + 2*Power[M, 4]*Power[s, 2]*(-23*s*Power[pT, 10] + 6*Power[pT, 12] + 32*Power[pT, 8]*Power[s, 2] - 122*Power[pT, 6]*Power[s, 3] + 173*Power[pT, 4]*Power[s, 4] - 66*Power[pT, 2]*Power[s, 5] + 6*Power[s, 6]) + Power[M, 4]*Power[s, 2]*(-25*s*Power[pT, 10] + 6*Power[pT, 12] + 42*Power[pT, 8]*Power[s, 2] - 127*Power[pT, 6]*Power[s, 3] + 175*Power[pT, 4]*Power[s, 4] - 66*Power[pT, 2]*Power[s, 5] + 6*Power[s, 6]) + 2*Power[M, 4]*Power[s, 2]*(-86*s*Power[pT, 10] + 9*Power[pT, 12] + 269*Power[pT, 8]*Power[s, 2] - 374*Power[pT, 6]*Power[s, 3] + 263*Power[pT, 4]*Power[s, 4] - 84*Power[pT, 2]*Power[s, 5] + 9*Power[s, 6]) + Power[M, 4]*Power[s, 2]*(-88*s*Power[pT, 10] + 9*Power[pT, 12] + 276*Power[pT, 8]*Power[s, 2] - 382*Power[pT, 6]*Power[s, 3] + 265*Power[pT, 4]*Power[s, 4] - 84*Power[pT, 2]*Power[s, 5] + 9*Power[s, 6]) - 27*Power[M, 2]*Power[s, 7] - 27*Power[pT, 2]*Power[s, 7] - s*Power[M, 2]*(-(s*Power[pT, 4]) + 2*Power[pT, 6] + 10*Power[pT, 2]*Power[s, 2] - 2*Power[s, 3])*Power[-s + Power[pT, 2], 2] + Power[pT, 2]*Power[s, 2]*Power[-s + Power[pT, 2], 4] + 6*Power[pT, 4]*Power[s, 4]*Power[-s + Power[pT, 2], 4] - Power[M, 2]*(17*s*Power[pT, 4] + 12*Power[pT, 6] + 44*Power[pT, 2]*Power[s, 2] - 12*Power[s, 3])*Power[s, 3]*Power[-(pT*s) + Power[pT, 3], 2] - Power[M, 2]*(9*s*Power[pT, 4] + 6*Power[pT, 6] + 22*Power[pT, 2]*Power[s, 2] - 6*Power[s, 3])*Power[s, 3]*Power[-(pT*s) + Power[pT, 3], 2] + 4*Power[M, 2]*Power[s, 3]*(10*s*Power[pT, 4] - 3*Power[pT, 6] - 11*Power[pT, 2]*Power[s, 2] + 3*Power[s, 3])* Power[-(pT*s) + Power[pT, 3], 2] + Power[M, 2]*Power[s, 3]*(21*s*Power[pT, 4] - 6*Power[pT, 6] - 22*Power[pT, 2]*Power[s, 2] + 6*Power[s, 3])*Power[-(pT*s) + Power[pT, 3], 2]");
        Expression pT = expression("pT = M");
        Expression s = expression("s = Power[M,2]");
        Tensor wolframResult = parse("-368*Power[M, 16] + 40*Power[M, 20]");

        Tensor temp = target;
        temp = s.transform(pT.transform(temp));
        Assert.assertTrue(TensorUtils.equalsExactly(temp, wolframResult));

        temp = target;
        temp = pT.transform(s.transform(temp));
        Assert.assertTrue(TensorUtils.equalsExactly(temp, wolframResult));

        temp = target;
        temp = pT.transform(s.transform(Expand.expand(temp)));
        Assert.assertTrue(TensorUtils.equalsExactly(temp, wolframResult));

        temp = target;
        temp = pT.transform(Expand.expand(s.transform(temp)));
        Assert.assertTrue(TensorUtils.equalsExactly(temp, wolframResult));

        temp = target;
        temp = s.transform(Expand.expand(pT.transform(temp)));
        Assert.assertTrue(TensorUtils.equalsExactly(temp, wolframResult));

        temp = pow(target, 2);
        temp = s.transform(pT.transform(Expand.expand(temp)));
        Assert.assertTrue(TensorUtils.equalsExactly(temp, Expand.expand(pow(wolframResult, 2))));//135424*Power[M, 32]+1600*Power[M, 40]-29440*Power[M, 36]
    }

    @Ignore
    @Test
    public void example2() {
        Tensor t = Tensors.parse("HATK^{\\mu \\epsilon \\zeta }_{\\eta \\theta }*HATK^{\\alpha \\gamma \\delta }_{\\epsilon \\zeta }*HATK^{\\nu \\eta \\theta }_{\\kappa_1 \\lambda_1 }*HATK^{\\beta \\theta_1 \\iota_1 }_{\\gamma \\delta }");
        Expression e1 = (Expression) Tensors.parse("HATK^{\\alpha \\beta \\gamma \\delta }_{\\epsilon \\zeta } = 2*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\alpha }_{\\epsilon }*n_{\\zeta }*n^{\\beta }+2*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\beta }_{\\epsilon }*n^{\\alpha }*n_{\\zeta }+2*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\alpha }_{\\zeta }*n_{\\epsilon }*n^{\\beta }+2*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\beta }_{\\zeta }*n^{\\alpha }*n_{\\epsilon }+2*(-1/4+-1/2*beta)*c*g^{\\gamma \\delta }*g_{\\epsilon \\zeta }*n^{\\beta }*n^{\\alpha }+-1/4*c*g^{\\gamma \\delta }*g^{\\alpha \\beta }*P_{\\theta }^{\\theta }_{\\epsilon \\zeta }+(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*P^{\\gamma \\delta }_{\\zeta }^{\\beta }+(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*P^{\\gamma \\delta }_{\\epsilon }^{\\alpha }+(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*P^{\\gamma \\delta \\alpha }_{\\zeta }+(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*P^{\\gamma \\delta \\beta \\alpha }+(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*P^{\\gamma \\delta }_{\\epsilon }^{\\beta }+(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*P^{\\gamma \\delta }_{\\zeta }^{\\alpha }+(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*P^{\\gamma \\delta \\beta }_{\\epsilon }+(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*P^{\\gamma \\delta \\alpha \\beta }+(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*P^{\\gamma \\delta \\alpha }_{\\epsilon }+(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*P^{\\gamma \\delta \\beta }_{\\zeta }+(1/8+1/4*beta)*c*g^{\\gamma \\delta }*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n_{\\eta }*n^{\\eta }+-1/4*(1/8+1/4*beta)*c*d^{\\theta }_{\\theta }*g^{\\gamma \\delta }*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }+-1*b*c*g^{\\alpha \\beta }*n_{\\eta }*n_{\\theta }*n^{\\delta }*n^{\\gamma }*P^{\\eta \\theta }_{\\epsilon \\zeta }+(1/8+1/4*beta)*c*d^{\\theta }_{\\theta }*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n^{\\gamma }*n^{\\delta }+c*g^{\\alpha \\beta }*g^{\\gamma \\delta }*n_{\\eta }*n_{\\theta }*P^{\\eta \\theta }_{\\epsilon \\zeta }+c*g^{\\alpha \\beta }*n^{\\gamma }*n^{\\delta }*P_{\\theta }^{\\theta }_{\\epsilon \\zeta }+(-1/4+-1/2*beta)*c*g^{\\gamma \\delta }*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*n_{\\eta }*n^{\\eta }+(-1/4+-1/2*beta)*c*g^{\\gamma \\delta }*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*n_{\\eta }*n^{\\eta }+1/4*b*g^{\\alpha \\beta }*n_{\\theta }*n^{\\gamma }*P^{\\delta \\theta }_{\\epsilon \\zeta }+1/4*b*g^{\\alpha \\beta }*n_{\\theta }*n^{\\delta }*P^{\\gamma \\theta }_{\\epsilon \\zeta }+(b*(-1/4+-1/2*beta)+4*(1/4+1/2*beta)*c)*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*n^{\\delta }*n^{\\gamma }+(b*(-1/4+-1/2*beta)+4*(1/4+1/2*beta)*c)*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*n^{\\delta }*n^{\\gamma }+1/4*b*g^{\\alpha \\beta }*n_{\\eta }*n^{\\delta }*P^{\\eta \\gamma }_{\\epsilon \\zeta }+1/4*b*g^{\\alpha \\beta }*n_{\\eta }*n^{\\gamma }*P^{\\eta \\delta }_{\\epsilon \\zeta }+-2*b*(1/4+1/2*beta)*c*d^{\\beta }_{\\epsilon }*n^{\\delta }*n^{\\gamma }*n_{\\zeta }*n^{\\alpha }+-2*b*(-1/4+-1/2*beta)*c*g_{\\epsilon \\zeta }*n^{\\delta }*n^{\\gamma }*n^{\\alpha }*n^{\\beta }+-2*b*(1/4+1/2*beta)*c*d^{\\alpha }_{\\epsilon }*n^{\\delta }*n^{\\gamma }*n^{\\beta }*n_{\\zeta }+-2*b*(1/4+1/2*beta)*c*d^{\\alpha }_{\\zeta }*n^{\\delta }*n^{\\gamma }*n^{\\beta }*n_{\\epsilon }+-2*b*(1/4+1/2*beta)*c*d^{\\beta }_{\\zeta }*n^{\\delta }*n^{\\gamma }*n_{\\epsilon }*n^{\\alpha }+(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*n^{\\gamma }*n^{\\delta }+(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*n^{\\gamma }*n^{\\delta }+-1*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\beta }_{\\epsilon }*d^{\\alpha }_{\\zeta }+-1*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }+-1*b*(-1/4+-1/2*beta)*c*d^{\\beta }_{\\zeta }*d^{\\alpha }_{\\epsilon }*n_{\\theta }*n^{\\theta }*n^{\\delta }*n^{\\gamma }+-1*b*(-1/4+-1/2*beta)*c*d^{\\beta }_{\\epsilon }*d^{\\alpha }_{\\zeta }*n_{\\theta }*n^{\\theta }*n^{\\delta }*n^{\\gamma }+-1/2*(-1/4+-1/2*beta)*c*g^{\\gamma \\delta }*g_{\\epsilon \\zeta }*g^{\\alpha \\beta }+-1*b*(1/8+1/4*beta)*c*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n_{\\theta }*n^{\\theta }*n^{\\delta }*n^{\\gamma }+(b*(1/8+1/4*beta)+2*(-1/4+-1/2*beta)*c)*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n^{\\delta }*n^{\\gamma }+-1/4*(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*g^{\\gamma \\delta }*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }+-1/4*(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*g^{\\gamma \\delta }*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }+(-1/4+-1/2*beta)*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*P^{\\gamma \\delta \\theta }_{\\theta }+(-1/4+-1/2*beta)*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*P^{\\gamma \\delta \\theta }_{\\theta }+(1/8+1/4*beta)*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*P^{\\gamma \\delta \\theta }_{\\theta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*g^{\\beta \\gamma }*n^{\\delta }*n_{\\epsilon }+1/2*b*(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*g^{\\alpha \\delta }*n^{\\gamma }*n^{\\beta }+1/2*b*(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*g^{\\beta \\delta }*n^{\\gamma }*n^{\\alpha }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*d_{\\zeta }^{\\delta }*n^{\\gamma }*n^{\\alpha }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*d_{\\epsilon }^{\\gamma }*n^{\\delta }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*g^{\\alpha \\delta }*n^{\\gamma }*n_{\\zeta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*d_{\\zeta }^{\\gamma }*n^{\\delta }*n^{\\beta }+1/2*b*(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*g^{\\beta \\gamma }*n^{\\delta }*n^{\\alpha }+1/2*b*(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*g^{\\alpha \\gamma }*n^{\\delta }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*d_{\\zeta }^{\\delta }*n^{\\gamma }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*d_{\\epsilon }^{\\delta }*n^{\\gamma }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*g^{\\alpha \\delta }*n^{\\gamma }*n_{\\epsilon }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*d_{\\epsilon }^{\\gamma }*n^{\\delta }*n^{\\alpha }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*g^{\\alpha \\gamma }*n^{\\delta }*n_{\\zeta }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*d_{\\epsilon }^{\\delta }*n^{\\gamma }*n^{\\alpha }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*g^{\\beta \\delta }*n^{\\gamma }*n_{\\zeta }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*d_{\\zeta }^{\\gamma }*n^{\\delta }*n^{\\alpha }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*g^{\\beta \\delta }*n^{\\gamma }*n_{\\epsilon }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*g^{\\alpha \\gamma }*n^{\\delta }*n_{\\epsilon }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*g^{\\beta \\gamma }*n^{\\delta }*n_{\\zeta }+g^{\\alpha \\beta }*P^{\\eta \\theta }_{\\epsilon \\zeta }*P^{\\gamma \\delta }_{\\eta \\theta }");
        Expression e2 = (Expression) Tensors.parse("HATK^{\\beta \\gamma \\delta }_{\\epsilon \\zeta } = -1/4*(1/8+1/4*beta)*c*d^{\\theta }_{\\theta }*g^{\\alpha \\beta }*g^{\\gamma \\delta }*g_{\\epsilon \\zeta }*n_{\\alpha }+(b*(-1/4+-1/2*beta)+4*(1/4+1/2*beta)*c)*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }+(b*(-1/4+-1/2*beta)+4*(1/4+1/2*beta)*c)*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }+c*g^{\\alpha \\beta }*n_{\\alpha }*n^{\\gamma }*n^{\\delta }*P_{\\theta }^{\\theta }_{\\epsilon \\zeta }+1/4*b*g^{\\alpha \\beta }*n_{\\alpha }*n_{\\theta }*n^{\\gamma }*P^{\\delta \\theta }_{\\epsilon \\zeta }+1/4*b*g^{\\alpha \\beta }*n_{\\alpha }*n_{\\theta }*n^{\\delta }*P^{\\gamma \\theta }_{\\epsilon \\zeta }+c*g^{\\alpha \\beta }*g^{\\gamma \\delta }*n_{\\alpha }*n_{\\eta }*n_{\\theta }*P^{\\eta \\theta }_{\\epsilon \\zeta }+-1*b*(1/8+1/4*beta)*c*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n_{\\alpha }*n_{\\theta }*n^{\\theta }*n^{\\gamma }*n^{\\delta }+2*(1/4+1/2*beta)*c*d^{\\alpha }_{\\epsilon }*g^{\\gamma \\delta }*n_{\\alpha }*n_{\\zeta }*n^{\\beta }+2*(1/4+1/2*beta)*c*d^{\\alpha }_{\\zeta }*g^{\\gamma \\delta }*n_{\\alpha }*n_{\\epsilon }*n^{\\beta }+(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*n_{\\alpha }*P^{\\gamma \\delta \\alpha }_{\\zeta }+(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*n_{\\alpha }*P^{\\gamma \\delta \\alpha \\beta }+(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*n_{\\alpha }*P^{\\gamma \\delta \\alpha }_{\\epsilon }+g^{\\alpha \\beta }*n_{\\alpha }*P^{\\eta \\theta }_{\\epsilon \\zeta }*P^{\\gamma \\delta }_{\\eta \\theta }+-1/4*(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*d^{\\alpha }_{\\epsilon }*g^{\\gamma \\delta }*d^{\\beta }_{\\zeta }*n_{\\alpha }+-1/4*(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*d^{\\alpha }_{\\zeta }*g^{\\gamma \\delta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }+-1/2*(-1/4+-1/2*beta)*c*g^{\\alpha \\beta }*g^{\\gamma \\delta }*g_{\\epsilon \\zeta }*n_{\\alpha }+(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*n_{\\alpha }*P^{\\gamma \\delta }_{\\zeta }^{\\beta }+(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*n_{\\alpha }*P^{\\gamma \\delta }_{\\epsilon }^{\\beta }+(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*n_{\\alpha }*P^{\\gamma \\delta \\beta }_{\\epsilon }+(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*n_{\\alpha }*P^{\\gamma \\delta \\beta }_{\\zeta }+1/2*b*(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*g^{\\beta \\delta }*n^{\\alpha }*n_{\\alpha }*n^{\\gamma }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*d_{\\zeta }^{\\delta }*n^{\\alpha }*n_{\\alpha }*n^{\\gamma }+1/2*b*(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*g^{\\beta \\gamma }*n^{\\alpha }*n_{\\alpha }*n^{\\delta }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*d_{\\epsilon }^{\\gamma }*n^{\\alpha }*n_{\\alpha }*n^{\\delta }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*d_{\\epsilon }^{\\delta }*n^{\\alpha }*n_{\\alpha }*n^{\\gamma }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*d_{\\zeta }^{\\gamma }*n^{\\alpha }*n_{\\alpha }*n^{\\delta }+(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*n_{\\alpha }*P^{\\gamma \\delta }_{\\epsilon }^{\\alpha }+(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*n_{\\alpha }*P^{\\gamma \\delta \\beta \\alpha }+(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*n_{\\alpha }*P^{\\gamma \\delta }_{\\zeta }^{\\alpha }+-2*b*(1/4+1/2*beta)*c*d^{\\beta }_{\\epsilon }*n^{\\alpha }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }*n_{\\zeta }+-2*b*(-1/4+-1/2*beta)*c*g_{\\epsilon \\zeta }*n^{\\alpha }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }*n^{\\beta }+-2*b*(1/4+1/2*beta)*c*d^{\\beta }_{\\zeta }*n^{\\alpha }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }*n_{\\epsilon }+-1*b*c*g^{\\alpha \\beta }*n_{\\alpha }*n_{\\eta }*n_{\\theta }*n^{\\gamma }*n^{\\delta }*P^{\\eta \\theta }_{\\epsilon \\zeta }+1/4*b*g^{\\alpha \\beta }*n_{\\alpha }*n_{\\eta }*n^{\\delta }*P^{\\eta \\gamma }_{\\epsilon \\zeta }+1/4*b*g^{\\alpha \\beta }*n_{\\alpha }*n_{\\eta }*n^{\\gamma }*P^{\\eta \\delta }_{\\epsilon \\zeta }+(b*(1/8+1/4*beta)+2*(-1/4+-1/2*beta)*c)*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }+-2*b*(1/4+1/2*beta)*c*d^{\\alpha }_{\\epsilon }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }*n^{\\beta }*n_{\\zeta }+-2*b*(1/4+1/2*beta)*c*d^{\\alpha }_{\\zeta }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }*n^{\\beta }*n_{\\epsilon }+(-1/4+-1/2*beta)*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*n_{\\alpha }*P^{\\gamma \\delta \\theta }_{\\theta }+(-1/4+-1/2*beta)*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }*P^{\\gamma \\delta \\theta }_{\\theta }+2*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\beta }_{\\epsilon }*n^{\\alpha }*n_{\\alpha }*n_{\\zeta }+2*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\beta }_{\\zeta }*n^{\\alpha }*n_{\\alpha }*n_{\\epsilon }+2*(-1/4+-1/2*beta)*c*g^{\\gamma \\delta }*g_{\\epsilon \\zeta }*n^{\\alpha }*n_{\\alpha }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*g^{\\beta \\gamma }*n_{\\alpha }*n^{\\delta }*n_{\\epsilon }+1/2*b*(-1/4+-1/2*beta)*g^{\\alpha \\delta }*g_{\\epsilon \\zeta }*n_{\\alpha }*n^{\\gamma }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*d_{\\epsilon }^{\\gamma }*n_{\\alpha }*n^{\\delta }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*g^{\\alpha \\delta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }*n^{\\gamma }*n_{\\zeta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*d_{\\zeta }^{\\gamma }*n_{\\alpha }*n^{\\delta }*n^{\\beta }+1/2*b*(-1/4+-1/2*beta)*g^{\\alpha \\gamma }*g_{\\epsilon \\zeta }*n_{\\alpha }*n^{\\delta }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*d_{\\zeta }^{\\delta }*n_{\\alpha }*n^{\\gamma }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*d_{\\epsilon }^{\\delta }*n_{\\alpha }*n^{\\gamma }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*g^{\\alpha \\delta }*d^{\\beta }_{\\zeta }*n_{\\alpha }*n^{\\gamma }*n_{\\epsilon }+1/2*b*(1/4+1/2*beta)*g^{\\alpha \\gamma }*d^{\\beta }_{\\epsilon }*n_{\\alpha }*n^{\\delta }*n_{\\zeta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*g^{\\beta \\delta }*n_{\\alpha }*n^{\\gamma }*n_{\\zeta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*g^{\\beta \\delta }*n_{\\alpha }*n^{\\gamma }*n_{\\epsilon }+1/2*b*(1/4+1/2*beta)*g^{\\alpha \\gamma }*d^{\\beta }_{\\zeta }*n_{\\alpha }*n^{\\delta }*n_{\\epsilon }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*g^{\\beta \\gamma }*n_{\\alpha }*n^{\\delta }*n_{\\zeta }+-1*b*(-1/4+-1/2*beta)*c*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*n_{\\alpha }*n_{\\theta }*n^{\\theta }*n^{\\gamma }*n^{\\delta }+-1*b*(-1/4+-1/2*beta)*c*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }*n_{\\theta }*n^{\\theta }*n^{\\gamma }*n^{\\delta }+(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*n_{\\alpha }*n^{\\gamma }*n^{\\delta }+(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }*n^{\\gamma }*n^{\\delta }+(1/8+1/4*beta)*c*d^{\\theta }_{\\theta }*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n_{\\alpha }*n^{\\gamma }*n^{\\delta }+(-1/4+-1/2*beta)*c*d^{\\alpha }_{\\zeta }*g^{\\gamma \\delta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }*n_{\\eta }*n^{\\eta }+(-1/4+-1/2*beta)*c*d^{\\alpha }_{\\epsilon }*g^{\\gamma \\delta }*d^{\\beta }_{\\zeta }*n_{\\alpha }*n_{\\eta }*n^{\\eta }+-1*(1/4+1/2*beta)*c*d^{\\alpha }_{\\zeta }*g^{\\gamma \\delta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }+-1*(1/4+1/2*beta)*c*d^{\\alpha }_{\\epsilon }*g^{\\gamma \\delta }*d^{\\beta }_{\\zeta }*n_{\\alpha }+(1/8+1/4*beta)*c*g^{\\alpha \\beta }*g^{\\gamma \\delta }*g_{\\epsilon \\zeta }*n_{\\alpha }*n_{\\eta }*n^{\\eta }+-1/4*c*g^{\\alpha \\beta }*g^{\\gamma \\delta }*n_{\\alpha }*P_{\\theta }^{\\theta }_{\\epsilon \\zeta }+(1/8+1/4*beta)*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n_{\\alpha }*P^{\\gamma \\delta \\theta }_{\\theta }");
        t = e1.transform(t);
        t = e2.transform(t);
        Expression kronecker = (Expression) Tensors.parse("d_\\mu^\\mu=4");
        t = Expand.expand(t, new Transformation[]{ContractIndices.ContractIndices, kronecker});
        System.out.println(t);
    }

    @Test
    public void test3() {
        //************************************************//
        //******** Compton scattering in scalar QED ******//
        //************************************************//

        //photon-scalar-scalar vertex
        Expression V1 = Tensors.parseExpression("V_{i}[p_a, q_b] = "
                + "-I*e*(p_i+q_i)");
        //photon-photon-scalar-scalar vertex
        Expression V2 = Tensors.parseExpression("V_{ij} = "
                + "2*I*e**2*g_ij");
        //scalar propagator                
        Expression P = Tensors.parseExpression("D[k_a] = -I/(k^a*k_a-m**2)");

        //matrix element
        Tensor M = Tensors.parseExpression("M^ij ="
                + "V^i[p1_a,p1_a+k1_a]*D[p1_a+k1_a]*V^j[-p2_a,-p1_a-k1_a]"
                + "+V^j[p1_a,p1_a-k2_a]*D[p1_a-k2_a]*V^i[-p1_a+k2_a,-p2_a]+V^ij");
        M = P.transform(M);
        M = V1.transform(M);
        M = V2.transform(M);
        //to common denominator
        M = Together.together(M);
        //expand transformation
        M = Expand.expand(M);

        //defining mass shell and Mandelstam variables
        Expression[] mandelstam = new Expression[]{
                Tensors.parseExpression("k1_a*k1^a = 0"),
                Tensors.parseExpression("k2_a*k2^a = 0"),
                Tensors.parseExpression("p1_a*p1^a = m**2"),
                Tensors.parseExpression("p2_a*p2^a = m**2"),
                Tensors.parseExpression("2*p1_a*k1^a = s-m**2"),
                Tensors.parseExpression("2*p2_a*k2^a = s-m**2"),
                Tensors.parseExpression("-2*k1_a*k2^a = t"),
                Tensors.parseExpression("-2*p1_a*p2^a = t-2*m**2"),
                Tensors.parseExpression("-2*k1_a*p2^a = u-m**2"),
                Tensors.parseExpression("-2*p1_a*k2^a = u-m**2")
        };
        //subsituting in matrix element
        for (Expression e : mandelstam)
            M = e.transform(M);

        //squared matrix element with sum over final photon polarizations 
        //and averaging over initial photon polarizations
        //here minus is due to complex conjugation
        Tensor M2 = Tensors.parse("M2 = -(1/2)*M_ij*M^ij");
        M2 = ((Expression) M).transform(M2);
        //expand squared matrix element and contract indices
        M2 = ExpandAll.expandAll(M2, ContractIndices.ContractIndices);
        M2 = Tensors.parseExpression("d_i^i = 4").transform(M2);

        //substituting mass shell and Mandelstam definitions 
        for (Expression e : mandelstam)
            M2 = e.transform(M2);
        M2 = Tensors.parseExpression("u=2*m**2-s-t").transform(M2);

        //some simplifications
        M2 = Together.together(M2);
        M2 = ExpandNumerator.expandNumerator(M2);

        //final cross section
        Tensor cs = ((Expression) M2).transform(Tensors.parse("1/(64*pi**2*s)*M2"));
        cs = Expand.expand(cs);
        System.out.println(cs);
        Tensor expected = Tensors.parse("1/16*(-s+m**2-t)**(-2)*(s-m**2)**(-2)*s**(-1)*m**4*pi**(-2)*t**2*e**4-1/4*(-s+m**2-t)**(-2)*(s-m**2)**(-2)*m**6*pi**(-2)*e**4+1/16*(-s+m**2-t)**(-2)*(s-m**2)**(-2)*s*pi**(-2)*t**2*e**4+1/8*(-s+m**2-t)**(-2)*(s-m**2)**(-2)*m**4*pi**(-2)*t*e**4+1/16*(-s+m**2-t)**(-2)*(s-m**2)**(-2)*s**(-1)*m**8*pi**(-2)*e**4-1/4*(-s+m**2-t)**(-2)*(s-m**2)**(-2)*s*m**2*pi**(-2)*t*e**4+1/16*(-s+m**2-t)**(-2)*(s-m**2)**(-2)*s**3*pi**(-2)*e**4+3/8*(-s+m**2-t)**(-2)*(s-m**2)**(-2)*s*m**4*pi**(-2)*e**4+1/8*(-s+m**2-t)**(-2)*(s-m**2)**(-2)*s**2*pi**(-2)*t*e**4-1/4*(-s+m**2-t)**(-2)*(s-m**2)**(-2)*s**2*m**2*pi**(-2)*e**4");
        TAssert.assertEquals(cs, expected);
    }

}
