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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

/**
 * Created by poslavsky on 03/03/16.
 */
public class AbbreviationTest {
    @Test
    public void test1() throws Exception {
        AbbreviationsBuilder abbrs = new AbbreviationsBuilder();

        final Tensor t = abbrs.transform(Tensors.parse("(c*(a+b) + f)*f_a + (c*(a+b) + f)*t_a"));

        System.out.println(t);
        for (AbbreviationsBuilder.Abbreviation abb : abbrs.getAbbreviations()) {
            System.out.println(abb);
        }
    }
}