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

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ProviderSumTest {
    @Test
    public void test2() {
        Tensor f = parse("a*b");
        Tensor t = parse("-a*b");
        MappingsPort opu = IndexMappings.createPort(f, t);
        IndexMappingBuffer buffer;
        while ((buffer = opu.take()) != null)
            System.out.println(buffer);
    }

    @Test
    public void test3() {
        Tensor f = parse("1");
        Tensor t = parse("-1");
        MappingsPort opu = IndexMappings.createPort(f, t);
        IndexMappingBuffer buffer;
        while ((buffer = opu.take()) != null)
            System.out.println(buffer);
    }

    @Test
    public void test1() {
        Tensor f = parse("A_ab^ab-d");
        Tensor t = parse("A_ba^ab+d");
        Tensors.addSymmetry("A_abmn", IndexType.LatinLower, true, 0, 1, 3, 2);
        OutputPortUnsafe<IndexMappingBuffer> opu = IndexMappings.createPort(f, t);
        int counter = 0;
        IndexMappingBuffer buffer;
        while ((buffer = opu.take()) != null)
            System.out.println(buffer);
        //        assertTrue(counter == 2);


    }
}