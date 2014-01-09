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
package cc.redberry.core.utils;

import cc.redberry.core.TAssert;
import cc.redberry.core.combinatorics.Combinatorics;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.combinatorics.symmetries.SymmetriesFactory;
import cc.redberry.core.context.CC;
import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.groups.permutations.PermutationOneLine;
import cc.redberry.core.groups.permutations.Permutations;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.EliminateFromSymmetriesTransformation;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static cc.redberry.core.TAssert.*;
import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;
import static cc.redberry.core.utils.TensorUtils.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorUtilsTest {

    @Test
    public void test1() {
        Tensor tensor = parse("A_ij");
        Tensor expected = parse("A_ij");
        assertTrue(TensorUtils.equalsExactly(tensor, expected));
    }

    @Test
    public void test2() {
        Tensor tensor = parse("A_ij*A_kl");
        Tensor expected = parse("A_kl*A_ij");
        assertTrue(TensorUtils.equalsExactly(tensor, expected));
    }

    @Test
    public void test3() {
        Tensor tensor = parse("A_ij*A_kl*A_mn+A_km*A_nl*A_ij");
        Tensor expected = parse("A_ij*A_kl*A_mn+A_km*A_nl*A_ij");
        assertTrue(TensorUtils.equalsExactly(tensor, expected));
    }

    @Test
    public void test4() {
        Tensor tensor = parse("A_ij*A_kl*A_mn+A_km*A_nl*A_ij");
        Tensor expected = parse("A_mn*A_kl*A_ij+A_nl*A_km*A_ij");
        assertTrue(TensorUtils.equalsExactly(tensor, expected));
    }

    @Test
    public void test5() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor tensor = parse("A_ij*A_kl*A_mn+A_km*A_nl*B_ij+B_ijk*C_lmn");
            Tensor expected = parse("C_lmn*B_ijk+A_mn*A_kl*A_ij+A_nl*A_km*B_ij");
            assertTrue(TensorUtils.equalsExactly(tensor, expected));
        }
    }

    @Test
    public void testParity1() {
        Tensor tensor = parse("A_ij*A_kl*A_mn+A_km*A_nl*B_ij+B_ijk*C_lmn");
        Tensor expected = parse("A_mn*A_kt*A_ij+A_nt*A_km*B_ij+C_tmn*B_ijk");
        assertFalse(TensorUtils.equals(tensor, expected));
    }

    @Test
    public void testParity2() {
        Tensor tensor = parse("A_ij^m*B_mlk+C_ijlkmn*T^mn");
        Tensor expected = parse("A_ij^u*B_ulk+C_ijlknp*T^np");
        assertTrue(TensorUtils.equals(tensor, expected));
    }

    @Test
    public void testParity3() {
        Tensor tensor = parse("A_ij^m*B_mlk");
        Tensor expected = parse("A_ij^u*B_ulk");
        assertTrue(TensorUtils.equals(tensor, expected));
    }

    @Test
    public void testIsZeroDueToSymmetry1() {
        Tensors.addSymmetry("A_mn", IndexType.LatinLower, true, 1, 0);
        Tensors.addSymmetry("S_mn", IndexType.LatinLower, false, 1, 0);
        Tensor t = parse("A_mn*S^mn");
        assertTrue(TensorUtils.isZeroDueToSymmetry(t));
    }

    @Test
    public void testIsZeroDueToSymmetry2() {
        Tensors.addSymmetry("A_mn", IndexType.LatinLower, true, 1, 0);
        Tensors.addSymmetry("S_mn", IndexType.LatinLower, false, 1, 0);
        Tensors.addSymmetry("F_mnab", IndexType.LatinLower, false, 1, 0, 2, 3);
        Tensors.addSymmetry("F_mnab", IndexType.LatinLower, true, 0, 1, 3, 2);
        Tensor t = parse("A_mn*S^mn+F_mn^mn");
        assertTrue(TensorUtils.isZeroDueToSymmetry(t));
    }

    @Test
    public void testIsZeroDueToSymmetry3() {
        Tensors.addSymmetry("F_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, true, 1, 0, 2, 3);
        Tensors.addSymmetry("R_\\mu\\nu", IndexType.GreekLower, false, 1, 0);
        Tensor t = parse("-23/60*R^{\\gamma \\mu }*F_{\\gamma \\mu }^{\\rho_5 }_{\\rho_5 }");
        assertTrue(TensorUtils.isZeroDueToSymmetry(t));
    }

    @Test
    public void testIsZeroDueToSymmetry4() {
        Tensors.addSymmetry("F_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, true, 1, 0, 2, 3);
        Tensors.addSymmetry("R_\\mu\\nu", IndexType.GreekLower, false, 1, 0);
        Tensor t = parse("1/15*R_{\\delta }^{\\rho }*R_{\\rho }^{\\delta }+-23/60*R^{\\gamma \\mu }*F_{\\gamma \\mu }^{\\rho_5 }_{\\rho_5 }+1/12*F^{\\alpha }_{\\nu }^{\\rho_5 }_{\\zeta }*F_{\\alpha }^{\\nu \\zeta }_{\\rho_5 }+1/2*W^{\\alpha }_{\\rho_5 }*W^{\\rho_5 }_{\\alpha }+1/30*Power[R, 2]+1/6*R*W^{\\beta }_{\\beta }");
        t = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES.transform(t);
        Tensor e = parse("1/15*R_{\\delta }^{\\rho }*R_{\\rho }^{\\delta }+1/12*F^{\\alpha }_{\\nu }^{\\rho_5 }_{\\zeta }*F_{\\alpha }^{\\nu \\zeta }_{\\rho_5 }+1/2*W^{\\alpha }_{\\rho_5 }*W^{\\rho_5 }_{\\alpha }+1/30*Power[R, 2]+1/6*R*W^{\\beta }_{\\beta }");
        assertTrue(TensorUtils.equals(t, e));
    }

    @Test
    public void testSymmetries1() {
        CC.resetTensorNames(2103403802553543528L);
        Tensor t = parse("g_{ab}*g^{rs}*g_{mn}*g^{pq}");
        //t.indices = ^{pqrs}_{abmn}
        List<Permutation> actual = TensorUtils.findIndicesSymmetries(t.getIndices().getAllIndices().copy(), t);

        Permutation[] symmetries = new Permutation[]{
                new PermutationOneLine(false, new int[]{0, 1, 2, 3, 4, 5, 6, 7}),
                new PermutationOneLine(false, new int[]{1, 0, 2, 3, 4, 5, 6, 7}),
                new PermutationOneLine(false, new int[]{0, 1, 2, 3, 4, 5, 7, 6}),
                new PermutationOneLine(false, new int[]{0, 1, 3, 2, 4, 5, 6, 7}),
                new PermutationOneLine(false, new int[]{0, 1, 2, 3, 5, 4, 6, 7}),
                new PermutationOneLine(false, new int[]{6, 7, 2, 3, 4, 5, 0, 1}),
                new PermutationOneLine(false, new int[]{0, 1, 6, 7, 4, 5, 2, 3}),
                new PermutationOneLine(false, new int[]{0, 1, 4, 5, 2, 3, 6, 7})};
        List<Permutation> expected = new ArrayList<>();
        for (Permutation symmetry : symmetries)
            expected.add(symmetry);

        assertEqualsSymmetries(actual, expected);
    }

    @Test
    public void testSymmetries2() {
        int[] from = {0, 1, 2, 3};
        int[] to = {2, 3, 0, 1};
        int[] indices = {0, 1, 2, 3};
        Mapping mapping = new Mapping(from, to);
        assertTrue(TensorUtils.getSymmetryFromMapping(indices, mapping).equals(new Symmetry(false, to)));
    }

    @Test
    public void testSymmetries3() {
        int[] from = {0, 1, 2, 3};
        int[] to = {2, 3, 1, 0};
        int[] indices = {0, 1, 2, 3};
        Mapping mapping = new Mapping(from, to);
        assertTrue(
                TensorUtils.getSymmetryFromMapping(indices, mapping).equals(
                        new Symmetry(false, to)));
    }

    @Test
    public void testSymmetries4() {
        for (int i = 0; i < 30; ++i) {
            CC.resetTensorNames();
            assertFindSymmetries(parseSimple("R_abcd"));
        }

        for (int i = 0; i < 30; ++i) {
            CC.resetTensorNames();
            assertFindSymmetries(parseSimple("R_abcde"));
        }
    }

    private static void assertFindSymmetries(SimpleTensor tensor) {
        int dimension = tensor.getIndices().size();
        int baseSize = 1 + (dimension / 3);
        Permutation[] base = new Permutation[baseSize];
        for (int i = 0; i < baseSize; ++i)
            base[i] = new PermutationOneLine(false, Permutations.randomPermutation(dimension));

        List<Permutation> expectedSymmetries = new ArrayList<>();
        for (Permutation s : base) {
            tensor.getIndices().getSymmetries().add((byte) 0, s);
            expectedSymmetries.add(s);
        }

        assertEqualsSymmetries(expectedSymmetries,
                findIndicesSymmetries(tensor.getIndices(), tensor));
    }

    @Test
    public void testSymmetries5() {
        Tensor t = parse("d_a^b*d_c^d");
        List<Permutation> actual = getIndicesSymmetriesForIndicesWithSameStates(ParserIndices.parse("_ab^cd"), t);
        List<Permutation> expected = new ArrayList<>();
        expected.add(new PermutationOneLine(new int[]{1, 0, 2, 3}));
        expected.add(new PermutationOneLine(new int[]{0, 1, 3, 2}));
        assertEqualsSymmetries(actual, expected);
    }

    @Test
    public void testSymmetries6() {
        Tensor t = parse("g_ab*g^cd");
        List<Permutation> actual = getIndicesSymmetriesForIndicesWithSameStates(ParserIndices.parse("_ab^cd"), t);
        List<Permutation> expected =  new ArrayList<>();
        expected.add(new PermutationOneLine(false, new int[]{1, 0, 2, 3}));
        expected.add(new PermutationOneLine(false, new int[]{0, 1, 3, 2}));
        assertEqualsSymmetries(actual, expected);
    }

    @Test
    public void testEquals1() {
        Tensor a = parse("d_{b}^{a}*d_{c}^{s}*d^{r}_{q}");
        Tensor b = parse("d^{a}_{q}*d_{c}^{s}*d_{b}^{r}");
        assertFalse(TensorUtils.equals(a, b));
    }

    @Test
    public void testTreeDepth1() {
        Tensor t;
        t = parse("f+g");
        assertEquals(treeDepth(t), 1);
        t = parse("f+g*h");
        assertEquals(treeDepth(t), 2);
        t = parse("f+g*Sin[h]");
        assertEquals(treeDepth(t), 3);
        t = parse("f+g*Sin[h+p]");
        assertEquals(treeDepth(t), 4);
        t = parse("(f+g*Sin[h])*(a+b)");
        assertEquals(treeDepth(t), 4);
        t = parse("(f+g*Sin[h])*(a+b*c)");
        assertEquals(treeDepth(t), 4);
        t = parse("(f+g*Sin[h])*(a+b*c*d*f*g*j)");
        assertEquals(treeDepth(t), 4);
        t = parse("(f+g*Sin[h])*(a+b*c**2)");
        assertEquals(treeDepth(t), 4);
        t = parse("(f+g*Sin[h])*(a+b*c**(a+b))+x");
        assertEquals(treeDepth(t), 6);
        t = parse("Sin[(f+g*Sin[h])*(a+b*c**(a+b))+x]");
        assertEquals(treeDepth(t), 7);
    }

    @Test
    public void testDet1() {
        Tensor[][] matrix = {
                {parse("a"), parse("b")},
                {parse("c"), parse("d")}
        };
        TAssert.assertEquals(det(matrix), "a*d-c*b");
    }

    @Test
    public void testDet2() {
        Tensor[][] matrix = {
                {parse("a"), parse("b"), parse("c")},
                {parse("d"), parse("e"), parse("f")},
                {parse("g"), parse("h"), parse("i")}
        };
        TAssert.assertEquals(det(matrix), "a*(e*i-f*h)-b*(d*i-f*g)+c*(d*h-e*g)");
    }

    @Test
    public void testDet3() {
        Tensor[][] matrix = {
                {parse("a"), parse("b"), parse("c"), parse("x")},
                {parse("d"), parse("e"), parse("f"), parse("y")},
                {parse("g"), parse("h"), parse("i"), parse("z")},
                {parse("p"), parse("q"), parse("r"), parse("s")}
        };
        TAssert.assertEquals(det(matrix), "(e*(s*i-z*r)-f*(-q*z+s*h)+y*(-q*i+h*r))*a-(f*(g*q-p*h)-e*(-p*i+g*r)+d*(-q*i+h*r))*x-(d*(s*i-z*r)-f*(-p*z+s*g)+(-p*i+g*r)*y)*b+c*(y*(g*q-p*h)-e*(-p*z+s*g)+(-q*z+s*h)*d)");
    }

    @Test
    public void testIntersec1() {
        TAssert.assertTrue(shareSimpleTensors(parse("x+y"),
                parse("x+z")));

        TAssert.assertTrue(shareSimpleTensors(parse("x+y"),
                parse("f[x]+z")));

        TAssert.assertTrue(shareSimpleTensors(parse("x+e[r[h[y]]]"),
                parse("z+f[g[y]]")));

        TAssert.assertTrue(shareSimpleTensors(parse("x+t[r[f~1[t]]]"),
                parse("z+f[g[y]]")));

        TAssert.assertFalse(shareSimpleTensors(parse("x+t[r[ff~1[t]]]"),
                parse("z+f[g[y]]")));

        TAssert.assertTrue(shareSimpleTensors(parse("x"),
                parse("-x")));
    }

    @Test
    public void testGenerateReplacementsOfScalars1() {
        Tensor t;
        Expression[] expressions;

        t = parse("a*b");
        assertEquals(generateReplacementsOfScalars(t).length, 0);

        t = parse("1/(k_m*k^m)");
        assertEquals(generateReplacementsOfScalars(t).length, 1);

        t = parse("1/(k_m*k^m) + f[k_m*k^m]");
        assertEquals(generateReplacementsOfScalars(t).length, 1);

        t = parse("k_a*k^m + f_a^m[k_m*k^m]");
        assertEquals(generateReplacementsOfScalars(t).length, 1);

        t = parse("k_a*k^m + f_a^m[k_m*k^m] + t_d*k^d*ff_a^m");
        assertEquals(generateReplacementsOfScalars(t).length, 2);
    }
}
