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
package cc.redberry.core.parser;

import org.junit.Test;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParserTest {
    @Test
    public void test1() {
        ParseNode node = Parser.DEFAULT.parse("2*a_\\mu-b_\\mu/(c*g)*g[x,y]");
        System.out.println(node);
    }

    @Test
    public void test2() {
        ParseNode node = Parser.DEFAULT.parse("f[a_\\mu]-f[b_\\mu/(c*g)*g[x,y]]");
        System.out.println(node);
    }

    @Test
    public void test3() {
        ParseNode node = Parser.DEFAULT.parse("f[b_\\mu/(c*g)*g[x,y]]");
        System.out.println(node);
        System.out.println(node.getClass());
    }

    @Test
    public void test4() {
        ParseNode node = Parser.DEFAULT.parse("a-b");
        System.out.println(node);
    }
}