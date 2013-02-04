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
package cc.redberry.core.parser.preprocessor;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndexType;
import org.junit.Ignore;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;

public class GeneralIndicesInsertionTest {

    @Test
    public void test1() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);

        indicesInsertion.addInsertionRule(parseSimple("S^a'_b'"), IndexType.Matrix1);
        TAssert.assertEquals(parse("S"), "S^a'_b'");
        TAssert.assertEquals(parse("S*S"), "S^a'_c'*S^c'_b'");
        TAssert.assertEquals(parse("S*S*S"), "S^a'_c'*S^c'_d'*S^d'_b'");
        TAssert.assertEquals(parse("S + S"), "2*S^a'_b'");
        TAssert.assertEquals(parse("S*A_m + S*B_m"), "S^a'_b'*A_m + S^a'_b'*B_m");
        TAssert.assertEquals(parse("S*(A_m + S*B_m)"), "S^a'_c'*(d^c'_b'*A_m + S^c'_b'*B_m)");
        TAssert.assertEquals(parse("S*(A_m + S*B_m) + S*C_m"), "S^a'_c'*(d^c'_b'*A_m + S^c'_b'*B_m) + S^a'_b'*C_m");
        TAssert.assertEquals(parse("S*(A_m + S*B_m) + S*C_m = F_m"), "S^a'_c'*(d^c'_b'*A_m + S^c'_b'*B_m) + S^a'_b'*C_m = d^a'_b'*F_m");
        TAssert.assertEquals(parse("S*(A_m + S*B_m) + S*C_m = F_m + D_m"), "S^a'_c'*(d^c'_b'*A_m + S^c'_b'*B_m) + S^a'_b'*C_m = d^a'_b'*(F_m + D_m)");
        TAssert.assertEquals(parse("S*(A_m + S*(B_m + S*S*A_m*(S + F_m^m*S))*S)"),
                "S^a'_c'*(d^c'_b'*A_m + S^c'_d'*(B_m*d^d'_e' + S^d'_f'*S^f'_g'*A_m*(S^g'_e' + F^m_m*S^g'_e'))*S^e'_b')");
    }

    @Test
    public void test2() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("S^a'_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("K^A'_B'"), IndexType.Matrix2);

        TAssert.assertEquals(parse("Tr[S*S*S*Y^y+S*Y^y+K*R^y,l'] + K*Y^y"),
                "Y^{y}*K^{A'}_{B'}+Y^{y}*d^{A'}_{B'}*S^{a'}_{a'}+Y^{y}*d^{A'}_{B'}*S^{c'}_{a'}*S^{b'}_{c'}*S^{a'}_{b'}+K^{A'}_{B'}*d^{a'}_{a'}*R^{y}");

        TAssert.assertEquals(parse("Tr[S*S*S*Y^y+S*Y^y+K*R^y,L']"),
                "Y^{y}*d^{A'}_{A'}*S^{a'}_{b'}+Y^{y}*d^{A'}_{A'}*S^{d'}_{b'}*S^{c'}_{d'}*S^{a'}_{c'}+K^{A'}_{A'}*d^{a'}_{b'}*R^{y}");

        TAssert.assertEquals(parse("Tr[S*S*S*Y^y+S*Y^y+K*R^y,L'] + S*Y^y"),
                "Y^{y}*S^{a'}_{b'}+Y^{y}*d^{A'}_{A'}*S^{a'}_{b'}+Y^{y}*d^{A'}_{A'}*S^{d'}_{b'}*S^{c'}_{d'}*S^{a'}_{c'}+K^{A'}_{A'}*d^{a'}_{b'}*R^{y}");
    }

    @Test
    public void test3() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("S^a'_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("K^A'_B'"), IndexType.Matrix2);
        TAssert.assertEquals(parse("Tr[K] + Tr[S]"), "K^A'_A'+S^a'_a'");
    }

    @Test
    public void test4() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("S^a'A'_b'B'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("S^a'A'_b'B'"), IndexType.Matrix2);
        indicesInsertion.addInsertionRule(parseSimple("v^a'A'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("v^a'A'"), IndexType.Matrix2);
        indicesInsertion.addInsertionRule(parseSimple("cv_a'A'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("cv_a'A'"), IndexType.Matrix2);

        TAssert.assertEquals(parse("cv*v"), "v^{a'A'}*cv_{a'A'}");
        TAssert.assertEquals(parse("v*cv"), "v^{a'A'}*cv_{b'B'}");
        TAssert.assertEquals(parse("cv*S*v"), "v^{b'B'}*S^{a'}_{b'}^{A'}_{B'}*cv_{a'A'}");
    }

    @Test
    public void test5() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("f^a'A'_b'B'[x]"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("f^a'A'_b'B'[x]"), IndexType.Matrix2);

        TAssert.assertEquals(parse("f[x] = 1 + c"), "f^{a'}_{b'}^{A'}_{B'}[x] = (c+1)*d^{a'}_{b'}*d^{A'}_{B'}");
    }


    @Test
    public void test6() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("A_a^a'_b'"), IndexType.Matrix1);

        TAssert.assertEquals(parse("Tr[A_a*A_b] = g_ab"), "A_{a}^{a'}_{b'}*A_{b}^{b'}_{a'} = g_{ab}");
    }
//
//    @Test
//    public void test7() {
//        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
//        gii.addInsertionRule(parseSimple("S^a'_b'"), IndexType.Matrix1);
//        gii.addInsertionRule(parseSimple("K^A'_B'"), IndexType.Matrix2);
//        gii.addInsertionRule(parseSimple("V^a'"), IndexType.Matrix1);
//        gii.addInsertionRule(parseSimple("cV_b'"), IndexType.Matrix1);
//        Tensor t = parse("Sin[Tr[S*S*S+S+K]]+K", gii);
//        System.out.println(t);
//    }

    @Test
    public void test8() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(gii);
        gii.addInsertionRule(parseSimple("G_m^a'_b'"), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple("pv_a'[p2_m]"), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple("v^a'[p2_m]"), IndexType.Matrix1);
        TAssert.assertEquals(parse("v[p2_m]*pv[p2_m] = m + p2^m*G_m"),
                "v^a'[p2_m]*pv_b'[p2_m] = m*d^a'_b' + p2^m*G^a'_{b' m}");
    }

    @Test
    public void test8a() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(gii);
        gii.addInsertionRule(parseSimple("G^a'_b'"), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple(" pv_a' "), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple(" v^a' "), IndexType.Matrix1);
        TAssert.assertEquals(parse("v*pv = G"),
                "pv_b'*v^a' = G^a'_b'");
    }

    @Test
    public void test9() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(gii);
        gii.addInsertionRule(parseSimple("G^a'_b'"), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple(" pv_a' "), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple(" v^a' "), IndexType.Matrix1);
        TAssert.assertEquals(parse("pv*v = G"),
                "pv_a'*v^a' = G^a'_a'");
    }

    @Test
    public void test10() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("A^a'_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("B^a'b'_c'd'e'"), IndexType.Matrix1);
        TAssert.assertEquals(parse("A*B"), "A^{a'}_{f'}*B^{f'b'}_{c'd'e'}");
    }

}
