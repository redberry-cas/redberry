/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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
package cc.redberry.core.transformations.factor;

import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.random.RandomTensor;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Ignore;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.transformations.expand.ExpandTransformation.expand;

/**
 * Created by poslavsky on 04/03/16.
 */
@Ignore
public class SingularFactorizationEngineTest {
    @Test
    public void testName() throws Exception {
        SingularFactorizationEngine fe = new SingularFactorizationEngine("/Applications/Singular.app/Contents/bin/Singular");

        System.out.println(fe.transform(parse("12387623*x**134-12387623*y**6")));
        fe.close();
    }

    @Test
    public void test1() throws Exception {
        RandomTensor rnd = new RandomTensor();
        rnd.clearNamespace();
        rnd.addToNamespace(parse("x"), parse("a"), parse("b"), parse("t"));

        SingularFactorizationEngine fe = new SingularFactorizationEngine("/Applications/Singular.app/Contents/bin/Singular");
        Tensor t = rnd.nextProductTree(3, 6, 8, IndicesFactory.EMPTY_INDICES);
        System.out.println(nSums(t));
        System.out.println(t);
        t = expand(t);
        System.out.println("expand");
        Tensor f = fe.transform(t);
        System.out.println(nSums(f));
        System.out.println(f);
        System.out.println(fe.transform(t));

        System.out.println(TensorUtils.equals(t, expand(f)));
    }

    static int nSums(Tensor t) {
        int s = 0;
        for (Tensor tensor : t) {
            if (tensor instanceof Sum)
                ++s;
        }
        return s;
    }
}