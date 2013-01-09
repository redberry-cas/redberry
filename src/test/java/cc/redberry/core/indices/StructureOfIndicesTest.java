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

import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.Tensors;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class StructureOfIndicesTest {

    @Test
    public void test1() {
        SimpleIndices si = ParserIndices.parseSimple("_ab'c'^d'");
        StructureOfIndices st = new StructureOfIndices(si);
        Assert.assertTrue(st.isStructureOf(si));
        Assert.assertFalse(st.isStructureOf(ParserIndices.parseSimple("_ab'^c'd'")));
        Assert.assertTrue(st.isStructureOf(ParserIndices.parseSimple("^a_b'c'^d'")));
    }

    @Test
    public void testDiffNames1() {
        Assert.assertTrue(Tensors.parse("v_a'").hashCode() != Tensors.parse("v^a'").hashCode());
        Assert.assertTrue(Tensors.parse("v_a").hashCode() == Tensors.parse("v^a").hashCode());
    }
}
