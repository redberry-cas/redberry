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
package cc.redberry.core.transformations.collect;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class CollectTransformationTest {
    @Test
    public void test1() {
        SimpleTensor[] simpleTensors = {parseSimple("a")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("a*b + a*c");
        TAssert.assertEquals(ct.transform(t), "a*(b+c)");
    }

    @Test
    public void test2() {
        SimpleTensor[] simpleTensors = {parseSimple("a"), parseSimple("b")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("a*b + a*c + a*d + b*e + b*r");
        TAssert.assertEquals(ct.transform(t), "a*b + a*(c+d) + b*(e+r)");
    }

    @Test
    public void test3() {
        SimpleTensor[] simpleTensors = {parseSimple("A_m")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("A_m*B_n + A_m*C_n");
        TAssert.assertEquals(ct.transform(t), "A_m*(B_n + C_n)");
    }


    @Test
    public void test4() {
        SimpleTensor[] simpleTensors = {parseSimple("A_m")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("A_m*B_n + A_n*C_m");
        TAssert.assertEquals(ct.transform(t), "A_i*(d^i_m*B_n + d^i_n*C_m)");
    }

    @Test
    public void test5() {
        SimpleTensor[] simpleTensors = {parseSimple("A_mn")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("A_mq*B_n^q + A_nq*C_m^q");
        TAssert.assertEquals(ct.transform(t), "A_iq*(d^i_m*B_n^q + d^i_n*C_m^q)");
    }

    @Test
    public void test6() {
        SimpleTensor[] simpleTensors = {parseSimple("A_mn")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("A_mq*B_n^q + A_nq*C_m^q");
        TAssert.assertEquals(ct.transform(t), "A_iq*(d^i_m*B_n^q + d^i_n*C_m^q)");
    }

    @Test
    public void test7() {
        SimpleTensor[] simpleTensors = {parseSimple("A_mn")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("A_mq*B_n^q + A_qn*C_m^q");
        TAssert.assertEquals(ct.transform(t), "A_iq*(d^i_m*B_n^q + d^q_n*C_m^i)");
    }

    @Test
    public void test8() {
        CC.resetTensorNames(8816281755326274707L);
        SimpleTensor[] simpleTensors = {parseSimple("A_mn")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("A_mq*B_n^q + A^q_n*C_mq");
        System.out.println(t);
        TAssert.assertEquals(ct.transform(t), "A_iq*(d^i_m*B_n^q + d^q_n*C_m^i)");
    }


    @Test
    public void test9() {
        CC.resetTensorNames(4662401180622313834L);
        SimpleTensor[] simpleTensors = {parseSimple("A_mnpq")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("A_mnpq*B^np_ac + A_abcd*B^ndb_nmq");
        System.out.println(ct.transform(t));
//        TAssert.assertEquals(ct.transform(t), "A_mnpq*(B^np_ac + A_abcd*B^ndb_nmq)");
    }

}
