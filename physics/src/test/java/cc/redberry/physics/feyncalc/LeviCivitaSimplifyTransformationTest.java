/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.random.RandomTensor;
import cc.redberry.core.transformations.*;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static cc.redberry.core.number.Complex.ZERO;
import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class LeviCivitaSimplifyTransformationTest {
    @Before
    public void before() {
        CC.reset();
    }

    @Test
    public void test1() {
        SimpleTensor eps = parseSimple("e_abcd");
        Tensor t;

        t = parse("e_abcd*k^a*k^b");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), ZERO);

        t = parse("e_abcd*k^ac*k^be");
        System.out.println(simplifyLeviCivita(t, eps));
        TAssert.assertEquals(simplifyLeviCivita(t, eps), t);

        t = parse("e_abed*k^ac*k^b_c");
        System.out.println(simplifyLeviCivita(t, eps));
        TAssert.assertEquals(simplifyLeviCivita(t, eps), ZERO);

        t = parse("e_abed*g^ed");
        System.out.println(simplifyLeviCivita(t, eps));
        TAssert.assertEquals(simplifyLeviCivita(t, eps), ZERO);

        t = parse("e_abed*e^abpq*g^ed");
        System.out.println(simplifyLeviCivita(t, eps));
        TAssert.assertEquals(simplifyLeviCivita(t, eps), ZERO);

        t = parse("e_abed*e^abpq*(g^ek*g^dl+g^el*g^dk)");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), ZERO);
    }

    @Test
    public void test2() {
        SimpleTensor eps = parseSimple("e_ab");
        Tensor t;

        t = parse("e_ed*e^pq");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), "d^{p}_{d}*d^{q}_{e}-d^{q}_{d}*d^{p}_{e}");

        t = parse("e_ed*e^eq");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), "-d_{d}^{q}");
    }


    @Test
    public void test3() {
        SimpleTensor eps = parseSimple("e_abc");
        Tensor t;

        t = parse("e_abc*e^abd");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), "2*d^d_c");
        t = parse("e_abc*e^abc");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), "6");
    }

    @Test
    public void test4() {
        SimpleTensor eps = parseSimple("e_abcf");
        addAntiSymmetry("e_abcd", 1, 0, 2, 3);
        addAntiSymmetry("e_abcd", 1, 2, 3, 0);
        Tensor t;
        t = parse("e_abcx");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), t);

        t = parse("e_abcx*e^abcy");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), "-6*d^y_x");
        t = parse("e_abcx*e^acby");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), "6*d^y_x");
        t = parse("e_abcx*e^acby");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), "6*d^y_x");
        t = parse("e_abcd*e^abcd");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), "-24");
        t = parse("e_abce*e^pqrs*e_rs^ce");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), "-4*e_{ab}^{pq}");
        t = parse("-4*I*e^{dh}_{b}^{f}*e_{g}^{b}_{ah}*e_{cdef}");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), "16*I*e_aceg");
        t = parse("(4*I)*e^{h}_{d}^{fb}*e_{abch}*e_{e}^{d}_{gf}");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), "16*I*e_aceg");

        t = parse("(4*I)*e^{h}_{d}^{fb}*e_{abch}*e_{e}^{d}_{gf}+g_mn*e^mn_ac*g_eg");
        TAssert.assertEquals(simplifyLeviCivita(t, eps), "16*I*e_aceg");
    }

    @Test
    public void test5() {
        Tensors.setAntiSymmetric("e_abcd");
        SimpleTensor eps = parseSimple("e_abcf");
        Tensor t = parse("e_{abcd} * e_{mnkl} * e_{pqrs} *k1^{a}*k1^{m}*k1^{p} *k2^{b}*k2^{n}*k2^{q} *k3^{c}*k3^{k}*k3^{r}");
        t = simplifyLeviCivita(t, eps);
        t = parseExpression("k1_m*k1^m = 0").transform(t);
        t = parseExpression("k2_m*k2^m = 0").transform(t);
        t = parseExpression("k3_m*k3^m = 0").transform(t);

        t = parseExpression("k1_m*k2^m = s/2").transform(t);
        t = parseExpression("k1_m*k3^m = -t/2").transform(t);
        t = parseExpression("k2_m*k3^m = -u/2").transform(t);

        t = simplifyLeviCivita(t, eps);
        Assert.assertEquals(t.size(), 10);
    }

    @Test
    public void test6() {
        setAntiSymmetric("e_abcd");
        SimpleTensor eps = parseSimple("e_abcf");
        Tensor t = parse("e_abcd*e^b_n^a_m*e^m_e^n_f");
        t = simplifyLeviCivita(t, eps);
        TAssert.assertEquals(t, "-4*e_{cdef}");
    }

    @Test
    public void test7() {
        setAntiSymmetric("e_abcd");
        SimpleTensor eps = parseSimple("e_abcf");
        Tensor t = parse("e_abcd*e_k^c_mn*e^dam_s*e^n_x^bk");
        t = simplifyLeviCivita(t, eps);
        TAssert.assertEquals(t, "12*g_sx");
    }

    @Test
    public void test8() {
        setAntiSymmetric("e_abcd");
        SimpleTensor eps = parseSimple("e_abcf");
        Tensor t = parse("e_abcd*e_k^c_mn*e^dam_s*e^n_x^bk");
        t = simplifyLeviCivita(t, eps);
        TAssert.assertEquals(t, "12*g_sx");
    }

    @Test
    public void test9() {
        setAntiSymmetric("e_abcd");
        setAntiSymmetric("D_ac");
        setAntiSymmetric("B_abc");
        setAntiSymmetric("A_abc");

        RandomTensor rnd = new RandomTensor(false);
        rnd.addToNamespace(parse("F_a"));
        rnd.addToNamespace(parse("A_ab"));
        rnd.addToNamespace(parse("B_abc"));
        rnd.addToNamespace(parse("D_ac"));
        rnd.addToNamespace(parse("g_ac"));
        rnd.addToNamespace(parse("e_abcd"));


        Tensor t1 = rnd.nextSum(20, 8, IndicesFactory.EMPTY_INDICES);
        Tensor t2 = rnd.nextSum(20, 8, IndicesFactory.EMPTY_INDICES);
        Transformation tr = new TransformationCollection(
                EliminateMetricsTransformation.ELIMINATE_METRICS,
                Tensors.parseExpression("A_ab*B^bac = T^c"),
                Tensors.parseExpression("A_ab*A^ba = xx"),
                Tensors.parseExpression("D_ab*D^ba = yy"),
                EliminateDueSymmetriesTransformation.ELIMINATE_DUE_SYMMETRIES,
                new LeviCivitaSimplifyTransformation(parseSimple("e_abcd"), true),
                ExpandAndEliminateTransformation.EXPAND_AND_ELIMINATE,
                Tensors.parseExpression("A_ab*B^bac = T^c"),
                Tensors.parseExpression("A_ab*A^ba = xx"),
                Tensors.parseExpression("D_ab*D^ba = yy")
        );

        new ExpandTransformation(tr).transform(multiplyAndRenameConflictingDummies(t1, t2));
    }

    private static Tensor simplifyLeviCivita(Tensor t, SimpleTensor eps) {
        return new LeviCivitaSimplifyTransformation(eps, true).transform(t);
    }
}
