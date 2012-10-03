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

package cc.redberry.core.tensor;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import org.junit.Test;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ProductBuilder1Test {

    private static Tensor parse(String s) {
        //TODO replace tests after placing new ProductBuilder as default
        Tensor t = Tensors.parse(s);
        if (!(t instanceof Product))
            return t;
        ProductBuilder1 productBuilder1 = new ProductBuilder1();
        for (Tensor f : t)
            productBuilder1.put(f);
        return productBuilder1.build();
    }

    @Test
    public void test1() {
        Tensor t = parse("(a+b)*(a+b)");
        TAssert.assertEquals(t, "(a+b)**2");
    }

    @Test
    public void test2() {
        Tensor t = parse("(a+b)**2*(a+b)");
        TAssert.assertEquals(t, "(a+b)**3");
    }

    @Test
    public void test3() {
        CC.resetTensorNames(-1394473649739479577L);
        Tensor t = parse("(-a+b)**2*(a-b)");
        TAssert.assertEquals(t, "(a-b)**3");
    }

    @Test
    public void test4() {
        Tensor t = parse("p_a*p^a*p_b*p^b");
        TAssert.assertEquals(t, "(p_a*p^a)**2");
    }

    @Test
    public void test5() {
        Tensor t = parse("a*a*(a-b)*(a-b)*p_a*p^a*p_b*p^b");
        TAssert.assertEquals(t, "a**2*(a-b)**2*(p_a*p^a)**2");
    }
}
