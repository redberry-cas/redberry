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
package cc.redberry.core.tensorgenerator;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorGeneratorUtilsTest {
    @Test
    public void test1() throws Exception {
//        Assert.assertEquals(TensorGeneratorUtils.allStatesCombinations(Tensors.parseSimple("f_ab")).length, 4);
//        Assert.assertEquals(TensorGeneratorUtils.allStatesCombinations(Tensors.parseSimple("p_b")).length, 2);
        Assert.assertEquals(TensorGeneratorUtils.allStatesCombinations(Tensors.parseSimple("g_ab")).length, 3);
    }

    @Test
    public void test2() {
        Tensor[] comb = TensorGeneratorUtils.allStatesCombinations(Tensors.parseSimple("F^a_ab"));
        Assert.assertEquals(2, comb.length);
    }
}
