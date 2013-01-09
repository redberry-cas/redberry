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
package cc.redberry.core.tensorgenerator;

import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.TAssert.assertTrue;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
//@Ignore
public class TensorGeneratorTest {

    public TensorGeneratorTest() {
    }

    @Test
    public void test0() {
        Tensor res = Tensors.parse("g_{ab}*g_{mn}+g_{bm}*g_{na}+g_{am}*g_{nb}");
        assertEquals(TensorGenerator.generate("",
                                              ParserIndices.parseSimple("_{mnab}"),
                                              false,
                                              Tensors.parse("g_mn", "g^mn", "d_m^n")), res);
    }

    @Test
    public void test1() {
        Tensor res = Tensors.parse("d_{m}^{a}*d_{n}^{b}+d_{m}^{b}*d_{n}^{a}+g_{mn}*g^{ab}");
        assertEquals(res, TensorGenerator.generate("",
                                                   ParserIndices.parseSimple("_{mn}^{ab}"),
                                                   false,
                                                   Tensors.parse("g_mn", "g^mn", "d_m^n")));
    }

    @Test
    public void test2() {
        Tensor res = Tensors.parse("d_{m}^{a}*d_{n}^{b}+d_{m}^{b}*d_{n}^{a}");
        assertEquals(TensorGenerator.generate("",
                                              ParserIndices.parseSimple("_{mn}^{ab}"),
                                              false,
                                              Tensors.parse("d_m^n")), res);
    }

    @Test
    public void test3() {
        Tensor res = Tensors.parse("d_{a}^{m}*d_{y}^{x}+g_{ay}*g^{mx}+d_{a}^{x}*d_{y}^{m}+k^{x}*k_{y}*d_{a}^{m}+k^{m}*k_{y}*d_{a}^{x}+k_{a}*k_{y}*g^{mx}+k_{a}*k^{m}*d_{y}^{x}+k^{m}*k^{x}*g_{ay}+k_{a}*k^{x}*d_{y}^{m}+k_{a}*k^{m}*k^{x}*k_{y}");
        assertEquals(res, TensorGenerator.generate("",
                                                   ParserIndices.parseSimple("_{ay}^{mx}"),
                                                   false,
                                                   Tensors.parse("k_a", "k^b", "g_mn", "g^mn", "d_m^n")));
    }

    @Test
    public void test4() {
        Sum t = (Sum) TensorGenerator.generate("",
                                               ParserIndices.parseSimple("_{abmn}^{pqrs}"),
                                               false,
                                               Tensors.parse("g_mn", "g^mn"));
        assertTrue(t.size() == 9);
    }

    @Test
    public void test5() {
        Sum t = (Sum) TensorGenerator.generate("",
                                               ParserIndices.parseSimple("_{abc}^{pqr}"),
                                               false,
                                               Tensors.parse("g_mn", "g^mn", "d_m^n", "k_a", "k^b"));
        assertTrue(t.size() == 76);
    }

    @Test
    public void test6() {
        Tensor res = Tensors.parse("1/6*(d_{a}^{p}*d_{b}^{q}*d_{c}^{r}+d_{a}^{q}*d_{b}^{p}*d_{c}^{r}+d_{a}^{q}*d_{b}^{r}*d_{c}^{p}+d_{b}^{p}*d_{c}^{q}*d_{a}^{r}+d_{b}^{q}*d_{c}^{p}*d_{a}^{r}+d_{a}^{p}*d_{b}^{r}*d_{c}^{q})");
        Tensor t = TensorGenerator.generate("",
                                            ParserIndices.parseSimple("_{abc}^{pqr}"),
                                            true,
                                            Tensors.parse("d_m^n"));

        assertEquals(res, t);
    }
//
//    @Test
//    public void symTest0() {
//        Symmetry s = new Symmetry(new int[]{1, 0, 2, 3}, false);
//        Symmetry s2 = new Symmetry(new int[]{3, 0, 1, 2}, false);
//        Symmetries symmetries = SymmetriesFactory.createSymmetries(4);
//        symmetries.add(s);
//        symmetries.add(s2);
//        Tensor res = Tensors.parse("(g_{ab}*g_{mn}+g_{bm}*g_{na}+g_{am}*g_{nb})");
//        assertEquals(res, TensorGenerator.generate("",
//                                                   ParserIndices.parseSimple("_{mnab}"),
//                                                   symmetries,
//                                                   false,
//                                                   Tensors.parse("g_mn", "g^mn", "d_m^n")));
//    }

//    @Ignore
//    @Test
//    public void symTest1() {
//        Symmetries symmetries = SymmetriesFactory.createFullSymmetries(3, 3);
//        Tensor gen = TensorGenerator.generate("",
//                                              ParserIndices.parseSimple("_{abc}^{pqr}"),
//                                              symmetries,
//                                              false,
//                                              Tensors.parse("g_mn", "g^mn", "d_m^n", "n_i", "n^j"));
//
//        for (Tensor s : gen) {
//            Tensor p = s.get(1);
//            if (p instanceof Sum)
//                System.out.println(p.get(0));
//            else
//                System.out.println(s);
//        }
////        assertEquals(res, TensorGenerator.generate("_{abc}^{pqr}", symmetries, "g_mn", "g^mn", "d_m^n", "n_i", "n^j"));
//    }
    @Test
    public void test7() {
        Tensor res = Tensors.parse("1/2*(d_{m}^{a}*d_{n}^{b}+d_{m}^{b}*d_{n}^{a})");
        assertEquals(TensorGenerator.generate("",
                                              ParserIndices.parseSimple("_{mn}^{ab}"),
                                              true,
                                              Tensors.parse("d_m^n")), res);
    }

    @Test
    public void test8() {
        Tensor expected = Tensors.parse("1/3*(g_mn*g_ab+g_ma*g_nb+g_mb*g_na)+"
                + "1/6*(g_ab*k_n*k_m+g_mn*k_a*k_b+g_am*k_n*k_b+g_an*k_b*k_m+g_nb*k_a*k_m+g_mb*k_n*k_a)+"
                + "k_a*k_b*k_m*k_n");
        assertEquals(TensorGenerator.generate("",
                                              ParserIndices.parseSimple("_{mnab}"),
                                              true,
                                              Tensors.parse("g_mn", "k_a")), expected);
    }

    @Test
    public void test9() {
        Tensor expected = Tensors.parse("p_\\mu*G^{\\mu i}_j+d^i_j");
        assertEquals(TensorGenerator.generate("",
                ParserIndices.parseSimple("^i_j"),
                true,
                Tensors.parse("d^i_j", "p_\\mu*G^{\\mu i}_j")), expected);
    }
}
