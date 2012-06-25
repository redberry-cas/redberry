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

import cc.redberry.concurrent.*;
import cc.redberry.core.context.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.tensor.Tensor;
import static cc.redberry.core.tensor.Tensors.*;
import org.junit.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ProviderSumTest {
  @Test
    public void test2() {
        Tensor f = parse("a*d");
        Tensor t = parse("-a*d");
        MappingsPort opu = IndexMappings.createPort(f, t);
        IndexMappingBuffer buffer;       
        while ((buffer = opu.take()) != null)
            System.out.println(buffer);
    }
    
    
    @Test
    public void test1() {
        Tensor f = parse("A_ab^ab-d");
        Tensor t = parse("A_ab^ab-d");
        parseSimple("A_abmn").getIndices().getSymmetries().add(IndexType.LatinLower, true, 1, 0, 2, 3);
        OutputPortUnsafe<IndexMappingBuffer> opu = IndexMappings.createPort(f, t);
        int counter = 0;
        IndexMappingBuffer buffer;
        while ((buffer = opu.take()) != null)
            System.out.println(buffer);
//        assertTrue(counter == 2);


    }
}