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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.context.CC;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SubstitutionIteratorTest {

    @Test
    public void test1() {
        Tensor t = parse("A_mn*(a+b+c)");
        SubstitutionIterator iterator = new SubstitutionIterator(t);
        Tensor f;
        while ((f = iterator.next()) != null)
//            System.out.println(f);
            if (TensorUtils.equalsExactly(f, parse("c")))
                System.out.println(iterator.stack);
    }

    @Test
    public void test2() {
        Tensor t = parse("D_nm+A_mn*(a+b+f[A_ij*c])");
        SubstitutionIterator iterator = new SubstitutionIterator(t);
        Tensor f;
        while ((f = iterator.next()) != null)
//            System.out.println(f);
            if (TensorUtils.equalsExactly(f, parse("c")))
                System.out.println(iterator.stack);
    }

    @Test
    public void test3() {
        CC.resetTensorNames(12445697);
        Tensor t = parse("c*f[h*f[d*a*f]]");
        SubstitutionIterator iterator = new SubstitutionIterator(t);
        Tensor f;
        while ((f = iterator.next()) != null)
//            System.out.println(f);
            if (TensorUtils.equalsExactly(f, parse("a")) || TensorUtils.equalsExactly(f, parse("d")) || TensorUtils.equalsExactly(f, parse("c")))
                System.out.println(f + " " + iterator.stack);

    }

    @Test
    public void test4() {
        CC.resetTensorNames(212499456971L);
        Tensor t = parse("F[p,q]*G_i");
        SubstitutionIterator iterator = new SubstitutionIterator(t);
        Tensor f;
        while ((f = iterator.next()) != null) {
            System.out.println(f + ":");
            System.out.println("\t Field depth: " + iterator.fieldDepth);
            System.out.println("\t Stack: " + iterator.stack);
            System.out.println("\t Forbidden indices size: " + iterator.forbiddenIndices().size());
        }
    }
}