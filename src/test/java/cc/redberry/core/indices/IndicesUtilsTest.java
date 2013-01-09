/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
package cc.redberry.core.indices;

import cc.redberry.core.context.OutputFormat;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IndicesUtilsTest {

    @Test
    public void parse() {
        int index = IndicesUtils.parseIndex("_{\\mu}");
        assertTrue("_{\\mu}".equals(IndicesUtils.toString(index, OutputFormat.LaTeX)));

        int index1 = IndicesUtils.parseIndex("_\\mu");
        assertTrue("_{\\mu}".equals(IndicesUtils.toString(index1, OutputFormat.LaTeX)));

        int index2 = IndicesUtils.parseIndex("_a");
        assertTrue("_{a}".equals(IndicesUtils.toString(index2, OutputFormat.LaTeX)));
    }
}
