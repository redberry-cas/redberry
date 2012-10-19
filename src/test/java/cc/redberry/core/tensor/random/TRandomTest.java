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
package cc.redberry.core.tensor.random;

import cc.redberry.core.context.CC;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.Tensor;
import junit.framework.Assert;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TRandomTest {

    @Test
    public void test1() {
        TRandom rp = new TRandom(
                4,
                10,
                new int[]{4, 0, 0, 0},
                new int[]{10, 0, 0, 0},
                false, new Well19937c());
        Tensor t = rp.nextProduct(4, ParserIndices.parseSimple("_nm"));
        Assert.assertTrue(t.getIndices().getFree().equalsRegardlessOrder(ParserIndices.parseSimple("_nm")));
    }

    @Test
    public void testSum1() {
        TRandom rp = new TRandom(
                4,
                10,
                new int[]{4, 0, 0, 0},
                new int[]{10, 0, 0, 0},
                false, new Well19937c());
        Tensor t = rp.nextSum(5, 4, ParserIndices.parseSimple("_nm"));
        Assert.assertTrue(t.getIndices().equalsRegardlessOrder(ParserIndices.parseSimple("_nm")));
    }

    @Test
    public void testProduct1() {
        TRandom random = new TRandom(5, 20, new int[]{2, 0, 0, 0}, new int[]{10, 0, 0, 0}, true, 76543L);
        for (int i = 0; i < 100; ++i) {
            Tensor t = random.nextProduct(5, ParserIndices.parseSimple("_mnab^cd"));
            Assert.assertTrue(t.getIndices().getFree().equalsRegardlessOrder(ParserIndices.parseSimple("_mnab^cd")));
        }
    }

    @Test
    public void testNullPointer() {
        CC.resetTensorNames(2312);
        TRandom random = new TRandom(5, 6, new int[]{2, 0, 0, 0}, new int[]{3, 0, 0, 0}, true, 7643543L);
        random.nextSum(5, 2, ParserIndices.parseSimple("_mn"));
        random.nextSum(5, 2, ParserIndices.parseSimple("^mn"));
    }
}
