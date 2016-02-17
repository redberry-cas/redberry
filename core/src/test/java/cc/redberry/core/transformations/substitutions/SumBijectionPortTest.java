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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import junit.framework.Assert;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.addSymmetry;
import static cc.redberry.core.tensor.Tensors.parse;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SumBijectionPortTest {

    @Test
    public void test1() {
        Tensor from = Tensors.parse("a+b");
        Tensor to = Tensors.parse("a+b+c");
        System.out.println(from);
        System.out.println(to);
        SumBijectionPort port = new SumBijectionPort(from, to);
        SumBijectionPort.BijectionContainer bc;
        while ((bc = port.take()) != null)
            System.out.println(bc);
    }

    @Test
    public void test2() {
        Tensors.addSymmetry("b_nm", IndexType.LatinLower, true, 1, 0);
        Tensor from = Tensors.parse("a_mn+b_mn");
        Tensor to = Tensors.parse("a_mn-b_nm+c_mn");
        System.out.println(from);
        System.out.println(to);
        SumBijectionPort port = new SumBijectionPort(from, to);
        SumBijectionPort.BijectionContainer bc;
        while ((bc = port.take()) != null)
            System.out.println(bc);
    }

    @Test
    public void test3() {
        Tensor from = Tensors.parse("a_mn+a_nm+x_mn+x_nm");
        Tensor to = Tensors.parse("a_mn+a_nm+c_mn+x_mn+x_nm");
        System.out.println(from);
        System.out.println(to);
        SumBijectionPort port = new SumBijectionPort(from, to);
        SumBijectionPort.BijectionContainer bc;
        while ((bc = port.take()) != null)
            System.out.println(bc);
    }

    @Test
    public void test4() {
        Tensor u = Tensors.parse("f_{cd}+V_{cd}");
        Tensor v = Tensors.parse("c + d");
        Assert.assertTrue(new SumBijectionPort(v, u).take() == null);
    }

    @Test
    public void test5() {
        for (int i = 0; i < 10; i++) {
            CC.reset();
            Tensor target = parse("f_i + R_ijk*F^kj + R_ijk*F^jk - R_kij*F^jk");
            Tensor from = parse("f_i + R_ijk*F^kj - R_kij*F^jk");

            SumBijectionPort port = new SumBijectionPort(from, target);
            SumBijectionPort.BijectionContainer take;
            int a = 0;
            while ((take = port.take()) != null) {
                ++a;
                TAssert.assertEquals(from, ((Sum) target).select(take.bijection));
            }
            Assert.assertEquals(1, a);
        }
    }

    @Test
    public void test6() {
        for (int i = 0; i < 10; i++) {
            CC.reset();
            addSymmetry("R_mnp", IndexType.LatinLower, true, 2, 1, 0);

//            Tensor target = parse("f_i + R_ijk*F^kj - R_kij*F^jk         +  R_ijk*F^jk ");
            Tensor target = parse("f_i + R_ijk*F^jk + R_ijk*F^kj         - R_kij*F^jk");
            Tensor from = parse("  f_m - R_ljm*F^lj + R_bma*F^ba   ");// =  R_bam*F^ab
            //                     f_m + R_mjk*F^kj - R_kmj*F^jk         = -R_mjk*F^jk

            Tensor to = parse("R_bam*F^ab");

            SumBijectionPort port = new SumBijectionPort(from, target);
            SumBijectionPort.BijectionContainer take;
            int a = 0;
            while ((take = port.take()) != null) {
                ++a;
                Sum sum = ((Sum) target);
                TAssert.assertEquals(take.mapping.transform(from), sum.select(take.bijection));
                TAssert.assertEquals("0", Tensors.sum(sum.remove(take.bijection), take.mapping.transform(to)));
            }
            Assert.assertEquals(1, a);
        }
    }
}
