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
package cc.redberry.core.transformations.expand;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.*;
import org.junit.*;
import static cc.redberry.core.tensor.Tensors.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpandBracketsTest {

    @Test
    public void test1() {
        Tensor t = parse("(a+b)*c+a*c");
        System.out.println(t);
        System.out.println(ExpandBrackets.expandBrackets(t, Indicator.TRUE_INDICATOR));
    }
}