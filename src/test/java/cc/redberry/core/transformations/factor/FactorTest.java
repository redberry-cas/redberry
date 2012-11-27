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
package cc.redberry.core.transformations.factor;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.TensorUtils;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Random;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;
import static cc.redberry.core.tensor.Tensors.pow;
import static cc.redberry.core.transformations.expand.Expand.expand;
import static cc.redberry.core.transformations.factor.Factor.factor;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class FactorTest {
    @Test
    public void test1() {
        for (int i = 0; i < 20; ++i) {
            CC.resetTensorNames();
            Tensor t = parse("2304*m**2*N*m**8 - 1152*s*N*m**8 + 288*m**6*N*s**2 - 1536*m**8*N*t + 480*m**6*N*s*t - 48*m**4*N*s**2*t + 352*m**6*N*t**2 - 56*m**4*N*s*t**2 + 2*m**2*N*s**2*t**2 - 32*m**4*N*t**3 + 2*m**2*N*s*t**3 + m**2*N*t**4");
            TAssert.assertEquals(t, expand(factor(t)));
        }
    }

    @Test
    public void test2() {
        for (int i = 0; i < 20; ++i) {
            CC.resetTensorNames();
            Tensor t = parse("2304*m**2*N*m**8 - 1152*s*N*m**8 + 288*m**6*N*s**2 - 1536*m**8*N*t + 480*m**6*N*s*t - 48*m**4*N*s**2*t + 352*m**6*N*t**2 - 56*m**4*N*s*t**2 + 2*m**2*N*s**2*t**2 - 32*m**4*N*t**3 + 2*m**2*N*s*t**3 + m**2*N*t**4 + 1");
            TAssert.assertEquals(t, expand(factor(t)));
        }
    }

    @Test
    public void test3() {
        Tensor t = parse("-(1/4)*e**4*m**2*s**3+(3/8)*e**4*m**4*s**2+(1/8)*s*e**4*m**4*t+(1/16)*e**4*t**2*m**4+(1/16)*e**4*m**8-(1/4)*e**4*m**2*t*s**2+(1/16)*e**4*t**2*s**2+(1/16)*e**4*s**4+(1/8)*e**4*t*s**3-(1/4)*s*e**4*m**6");
        TAssert.assertEquals(factor(t), "(1/16)*e**4*(t**2*s**2-4*m**2*s**3+6*m**4*s**2+2*t*s**3-4*s*m**6-4*t*m**2*s**2+2*s*t*m**4+m**8+m**4*t**2+s**4)");
    }

    @Test
    public void test4() {
        Tensor t = parse("(x + y + z + 56*x + i)**10");
        Tensor t1 = parse("362033331456891249*((1/57)*z+(1/57)*y+(1/57)*i+x)**10");
        Tensor exp = expand(t);
        TAssert.assertTrue(TensorUtils.equals(factor(exp), t) || TensorUtils.equals(factor(exp), t1));
    }

    @Test
    public void test5() {

        Tensor t, exp;
        t = parse("(x - y + z)**2*(a+b)**3");
        exp = expand(t);
        TAssert.assertEquals(factor(exp), t);

        t = parse("(x - y + a)**2*(a+b)**3");
        exp = expand(t);
        TAssert.assertEquals(factor(exp), t);

        t = parse("(x - y + a)**2*(a+b)**3*(x + b)");
        exp = expand(t);
        TAssert.assertEquals(factor(exp), t);

        t = parse("(x - y - a)**2*(a - b)**3*(x - b)");
        exp = expand(t);
        TAssert.assertEquals(factor(exp), t);

        t = parse("(x - y - a)**2*(a - b)**3*(x - b)**2");
        exp = expand(t);
        TAssert.assertEquals(factor(exp), t);

        t = parse("(x - y - a)**2*(a - b)**3*(x - b)**2*(p + q)");
        exp = expand(t);
        TAssert.assertEquals(factor(exp), t);

        t = parse("(x**12 - y**2 - a)**2*(a - b**3)**3*(x**5 - b**9)**2*(p + q)");
        exp = expand(t);
        TAssert.assertEquals(factor(exp), t);
    }

    @Test
    public void test6r() {
        Random random = new Random();
        for (int i = 0; i < 10; ++i) {
            CC.resetTensorNames();
            Tensor t = randomFactorableProduct(random);
            Tensor expand = expand(t);
            Tensor factor = JasFactor.factor(expand);
            TAssert.assertEquals(expand(factor), expand);
        }
    }

    @Test
    public void test7(){
        Tensor t = expand(parse("2*((1/2)*m*t**4-4*m**3*t**3+8*m**5*t**2)"));
        System.out.println(factor(t));
    }

    @Test
    public void test6ra() {
        Random random = new Random();
        long seed = random.nextLong();
//        seed = -2405124035495815364L;
        random.setSeed(seed);
        System.out.println(seed);
        Tensor t = randomFactorableProduct(random);
        Tensor expand = expand(t);
        System.out.println(t);
        Tensor factor = JasFactor.factor(expand);

        System.out.println(factor);
        System.out.println(expand);
        TAssert.assertEquals(expand(factor), expand);
    }


    private static Tensor randomSum(Random random) {
        SimpleTensor[] simpleTensors = {parseSimple("a"), parseSimple("b"),
                parseSimple("c")};
        int sumSize = 2 + random.nextInt(4);
        SumBuilder sb = new SumBuilder();
        int productSize;
        for (int i = 0; i < sumSize; ++i) {
            productSize = 1 + random.nextInt(3);
            ProductBuilder pb = new ProductBuilder();
            pb.put(new Complex(1 + random.nextInt(10)));
            for (int j = 0; j < productSize; ++j)
                pb.put(simpleTensors[random.nextInt(simpleTensors.length)]);
            sb.put(pb.build());
        }
        return sb.build();
    }

    private static Tensor randomFactorableProduct(Random random) {
        int productSize = 2 + random.nextInt(2);
        ProductBuilder pb = new ProductBuilder();
        for (int i = 0; i < productSize; ++i)
            pb.put(pow(randomSum(random), 1 + random.nextInt(3)));
        return pb.build();
    }
}
