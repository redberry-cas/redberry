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
package cc.redberry.core.number.parser;

import cc.redberry.core.number.*;
import java.util.Collection;
import org.junit.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ComplexTest {

    @Test
    public void test1() {
        Complex a = new Complex(1);
        Complex b = new Complex(0);
        System.out.println(a.divide(b));
    }

    @Test
    public void test2() {
        org.apache.commons.math3.complex.Complex a = new org.apache.commons.math3.complex.Complex(1);
        org.apache.commons.math3.complex.Complex b = new org.apache.commons.math3.complex.Complex(0);
        System.out.println(a.divide(b));
        Object[] s = new Integer[2];
        System.out.println(s instanceof Double[]);
    }
}