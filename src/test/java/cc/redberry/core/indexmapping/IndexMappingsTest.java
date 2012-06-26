/*
 * org.redberry.concurrent: high-level Java concurrent library.
 * Copyright (c) 2010-2012.
 * Bolotin Dmitriy <bolotin.dmitriy@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 */

package cc.redberry.core.indexmapping;

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.context.CC;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Assert;
import org.junit.Test;

public class IndexMappingsTest {
    @Test
    public void test4() {
        for (int i = 0; i < 10000; ++i) {
            CC.resetTensorNames();
            Tensor t = Tensors.parse("A_a*B_bc-A_b*B_ac");
            OutputPortUnsafe<IndexMappingBuffer> opu = IndexMappings.createPort(t, t);
            int counter = 0;
            while (opu.take() != null)
                counter++;
            Assert.assertTrue(counter == 2);
        }
    }

}
