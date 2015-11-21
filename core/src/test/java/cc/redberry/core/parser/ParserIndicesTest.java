/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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

import cc.redberry.core.indices.Indices;
import junit.framework.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParserIndicesTest {
    @Test
    public void test1() {
        Indices in = ParserIndices.parseSimple("_{AC_{21}B}");
        Assert.assertTrue(in.size() == 3);
    }

    @Test
    public void test2() {
        Assert.assertEquals(ParserIndices.parseSimple("_μν^αβ"),
                ParserIndices.parseSimple("_\\mu\\nu^\\alpha\\beta"));
    }
}
