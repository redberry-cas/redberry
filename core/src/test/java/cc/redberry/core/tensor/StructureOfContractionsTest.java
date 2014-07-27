/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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

import cc.redberry.core.context.CC;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class StructureOfContractionsTest {
    @Test
    public void test1() throws Exception {
        CC.resetTensorNames(1233444123);
        Product product = (Product) Tensors.parse("f_ab*t^bca*g_cd");
        System.out.println(product);
        System.out.println(
                Arrays.toString(
                        product.getContent().getStructureOfContractions().getContractedWith(1)));
    }
}