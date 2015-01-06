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
package cc.redberry.core.indices;

import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class StructureOfIndicesTest {

    @Test
    public void test1() {
        SimpleIndices si = ParserIndices.parseSimple("_ab'c'^d'");
        StructureOfIndices st = StructureOfIndices.create(si);
        assertTrue(st.isStructureOf(si));
        assertFalse(st.isStructureOf(ParserIndices.parseSimple("_ab'^c'd'")));
        assertTrue(st.isStructureOf(ParserIndices.parseSimple("^a_b'c'^d'")));
    }

    @Test
    public void testDiffNames1() {
        assertTrue(Tensors.parse("v_a'").hashCode() != Tensors.parse("v^a'").hashCode());
        assertTrue(Tensors.parse("v_a").hashCode() == Tensors.parse("v^a").hashCode());
    }

    @Test
    public void testInverse() {
        SimpleIndices si = ParserIndices.parseSimple("_ab'c'^d'");
        SimpleIndices inv = si.getInverted();
        assertEquals(si.getStructureOfIndices().getInverted(), inv.getStructureOfIndices());
    }

    @Test
    public void testAppend() {
        SimpleIndices si = ParserIndices.parseSimple("_ab'c'^d'_g'"),
                so = ParserIndices.parseSimple("_ab_\\alpha_b'c'^d'_g'"),
                siso = ParserIndices.parseSimple("_ab'c'^d'_g'_xy_\\beta_y't'^w'_q'"),
                sosi = ParserIndices.parseSimple("_xy_\\beta_y't'^w'_q'_ab'c'^d'_g'");
        assertEquals(siso.getStructureOfIndices(), si.getStructureOfIndices().append(so.getStructureOfIndices()));
        assertEquals(sosi.getStructureOfIndices(), so.getStructureOfIndices().append(si.getStructureOfIndices()));

        assertEquals(siso.getInverted().getStructureOfIndices(),
                si.getInverted().getStructureOfIndices().append(so.getInverted().getStructureOfIndices()));
        assertEquals(sosi.getInverted().getStructureOfIndices(),
                so.getInverted().getStructureOfIndices().append(si.getInverted().getStructureOfIndices()));

        assertEquals(siso.getInverted().getStructureOfIndices(),
                si.getInverted().getStructureOfIndices().append(so.getStructureOfIndices().getInverted()));
        assertEquals(sosi.getInverted().getStructureOfIndices(),
                so.getInverted().getStructureOfIndices().append(si.getStructureOfIndices().getInverted()));

        assertEquals(siso.getInverted().getStructureOfIndices(),
                si.getStructureOfIndices().getInverted().append(so.getStructureOfIndices().getInverted()));
        assertEquals(sosi.getInverted().getStructureOfIndices(),
                so.getStructureOfIndices().getInverted().append(si.getStructureOfIndices().getInverted()));
    }

    @Test
    public void testSubtract1() {
        SimpleIndices si = ParserIndices.parseSimple("_ab'c'^d'_g'"),
                so = ParserIndices.parseSimple("_ab_\\alpha_b'c'^d'_g'"),
                siso = ParserIndices.parseSimple("_ab'c'^d'_g'_xy_\\beta_y't'^w'_q'"),
                sosi = ParserIndices.parseSimple("_xy_\\beta_y't'^w'_q'_ab'c'^d'_g'");

        assertEquals(si.getStructureOfIndices(), siso.getStructureOfIndices().subtract(so.getStructureOfIndices()));
        assertEquals(so.getStructureOfIndices(), sosi.getStructureOfIndices().subtract(si.getStructureOfIndices()));

        assertEquals(si.getStructureOfIndices().getInverted(),
                siso.getStructureOfIndices().getInverted().subtract(so.getStructureOfIndices().getInverted()));
        assertEquals(so.getStructureOfIndices().getInverted(),
                sosi.getStructureOfIndices().getInverted().subtract(si.getStructureOfIndices().getInverted()));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtract2() {
        SimpleIndices si = ParserIndices.parseSimple("_ab'c'^d'_g'"),
                so = ParserIndices.parseSimple("_ab_\\alpha_b'c'^d'_g'"),
                siso = ParserIndices.parseSimple("_ab'c'^d'_g'_xy_\\beta_y't'w'q'");
        siso.getStructureOfIndices().subtract(so.getStructureOfIndices());
    }

    @Test
    public void testPartition1() {
        SimpleIndices
                si = ParserIndices.parseSimple("_ab'c'^d'_g'"),
                so = ParserIndices.parseSimple("_ab_\\alpha_b'c'^d'_g'"),
                siso = ParserIndices.parseSimple("_ab'c'^d'_g'_xy_\\beta_y't'^w'_q'");

        StructureOfIndices[] siso_ = {si.getStructureOfIndices(), so.getStructureOfIndices()};

        int[][] map = siso.getStructureOfIndices().getPartitionMappings(siso_);

        int ik = 0;
        for (int[] ma : map) {
            SimpleIndicesBuilder builder = new SimpleIndicesBuilder();
            for (int i : ma)
                builder.append(siso.get(i));
            assertEquals(builder.getIndices().getStructureOfIndices(), siso_[ik++]);
        }
    }
}
