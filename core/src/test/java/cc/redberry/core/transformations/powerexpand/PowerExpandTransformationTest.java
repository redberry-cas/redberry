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
package cc.redberry.core.transformations.powerexpand;

import cc.redberry.core.TAssert;
import cc.redberry.core.tensor.Power;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.Indicator;
import org.junit.Test;

import java.util.Arrays;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PowerExpandTransformationTest {
    @Test
    public void test1() {
        Tensor t = parse("(a*b*c)**d");
        PowerExpandTransformation pe = PowerExpandTransformation.POWER_EXPAND_TRANSFORMATION;
        TAssert.assertEquals(pe.transform(t), "a**d*b**d*c**d");
    }

    @Test
    public void test2() {
        Tensor t = parse("(a*b*c)**d");
        PowerExpandTransformation pe = new PowerExpandTransformation(new SimpleTensor[]{parseSimple("a")});
        TAssert.assertEquals(pe.transform(t), "a**d*(b*c)**d");
    }

    @Test
    public void test3() {
        Tensor t = parse("(a*b*c)**d");
        final SimpleTensor[] vars = new SimpleTensor[]{parseSimple("a")};
        Indicator<Tensor> indicator = new Indicator<Tensor>() {
            @Override
            public boolean is(Tensor object) {
                if (object instanceof SimpleTensor)
                    for (SimpleTensor var : vars)
                        if (var.getName() == ((SimpleTensor) object).getName())
                            return true;
                return false;
            }
        };
        TAssert.assertEquals(Tensors.multiply(PowerExpandUtils.powerExpandIntoChainToArray((Power) t,
                new int[0], indicator)), "a**d*(b*c)**d");
    }

    @Test
    public void test4() {
        Tensor t = parse("(a*b*c)**d");
        Tensor[] arr = PowerExpandUtils.powerExpandToArray((Power) t);
        Tensor[] expected = {parse("a**d"), parse("b**d"), parse("c**d")};
        Arrays.sort(arr);
        Arrays.sort(expected);
        TAssert.assertEquals(arr, expected);
    }

    @Test
    public void test5(){
        Tensor t = parse("(a**e*b*c)**d");
        SimpleTensor[] vars = {parseSimple("a")};
        PowerExpandTransformation pe = new PowerExpandTransformation(vars);
        TAssert.assertEquals(pe.transform(t),"a**(e*d)*(b*c)**d");
    }

    @Test
    public void test6(){
        Tensor t = parse("((a**r*g)**e*b*c)**d");
        SimpleTensor[] vars = {parseSimple("a")};
        PowerExpandTransformation pe = new PowerExpandTransformation(vars);
        TAssert.assertEquals(pe.transform(t),"a**(r*e*d)*(g**e*b*c)**d");
    }

    @Test
    public void test7(){
        Tensor t = parse("((a**r*g)**e*b*c)**d");
        SimpleTensor[] vars = {parseSimple("a"),parseSimple("g")};
        PowerExpandTransformation pe = new PowerExpandTransformation(vars);
        TAssert.assertEquals(pe.transform(t),"a**(r*e*d)*g**(e*d)*(b*c)**d");
    }


    @Test
    public void test8(){
        Tensor t = parse("((a**r*g)**e*b*c)**d*(a+b) + x");
        SimpleTensor[] vars = {parseSimple("a"),parseSimple("c")};
        PowerExpandTransformation pe = new PowerExpandTransformation(vars);
        TAssert.assertEquals(pe.transform(t),"a**(r*e*d)*c**d*(g**e*b)**d*(a+b) + x");
    }

}
