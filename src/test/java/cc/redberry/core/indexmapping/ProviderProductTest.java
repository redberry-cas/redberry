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
package cc.redberry.core.indexmapping;

import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Tensor;
import org.junit.Assert;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.addSymmetry;
import static cc.redberry.core.tensor.Tensors.parse;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ProviderProductTest {

    @Test
    public void test1() {
        addSymmetry("F_mn", IndexType.LatinLower, true, 1, 0);
        Tensor from = parse("F_mn*F^mn");
        Tensor to = parse("F_mn*F^nm");
        MappingsPort mp = IndexMappings.createPort(from, to);
        IndexMappingBuffer buffer;
        boolean sign = false;
        while ((buffer = mp.take()) != null)
            if (!sign)
                if (buffer.getSignum()) {
                    sign = true;
                    break;
                }
        Assert.assertTrue(sign);
    }
}
