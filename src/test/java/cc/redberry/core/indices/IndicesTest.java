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
package cc.redberry.core.indices;

import cc.redberry.core.context.CC;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.SimpleTensor;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.addSymmetry;
import static cc.redberry.core.tensor.Tensors.parse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IndicesTest {

    public IndicesTest() {
    }

    @Test
    public void testGetSymmetries() {
        SimpleTensor t = (SimpleTensor) parse("g_mn");
        addSymmetry(t, IndexType.LatinLower, false, 1, 0);
        assertTrue(CC.getNameDescriptor(t.getName()).getSymmetries() == t.getIndices().getSymmetries());
    }

    @Test
    public void testGetUpper1() {
        Indices indices = parse("g_mn*T^ab*D^n_b").getIndices(); //sorted indices
        Indices upper = ParserIndices.parseSimple("^a");
        assertTrue(indices.getFree().getUpper().equals(upper.getAllIndices()));
    }

    @Test
    public void testGetLower() {
        Indices indices = parse("g_mn*T^ab*D^n_b").getIndices(); //sorted indices
        Indices upper = ParserIndices.parseSimple("_m");
        assertTrue(indices.getFree().getLower().equals(upper.getAllIndices()));
    }

    @Test
    public void testGetFreeIndices() {
        Indices indices = parse("g_mn*T^ab*D^n_b").getIndices(); //sorted indices
        Indices free = ParserIndices.parseSimple("_m^a");
        assertTrue(indices.getFree().equalsRegardlessOrder(free));

        Indices indices1 = parse("g_mn^abn_b").getIndices(); //ordered indices
        assertTrue(indices1.getFree().equalsRegardlessOrder(free));
    }

    @Test
    public void testGetInverseIndices1() {
        Indices indices = parse("g_mn*T^ab*D^n_b").getIndices(); //sorted indices
        Indices inverse = indices.getInverse();
        Indices expected = ParserIndices.parseSimple("^mn_abn^b");

        assertTrue(expected.equalsRegardlessOrder(inverse));
        expected = IndicesFactory.createSorted(expected);
        assertTrue(inverse.getLower().equals(expected.getLower()));
        assertTrue(inverse.getUpper().equals(expected.getUpper()));

        SimpleIndices indices1 = (SimpleIndices) parse("g_mn^abn_b").getIndices(); //ordered indices
        assertTrue(indices1.getInverse().equalsRegardlessOrder(expected));
        assertTrue(indices1.getSymmetries() == indices1.getInverse().getSymmetries());
    }

    @Test
    public void testGetInverseIndices2() {
        Indices indices = parse("g_mn*T_ab").getIndices(); //sorted indices
        Indices inverse = indices.getInverse();
        Indices expected = ParserIndices.parseSimple("^abmn");

        assertTrue(expected.equalsRegardlessOrder(inverse));
        assertTrue(inverse.getLower().equals(expected.getLower()));
        assertTrue(inverse.getUpper().equals(expected.getUpper()));
    }

    @Test
    public void testGetInverseIndices3() {
        Indices indices = parse("g^mn*T^ab").getIndices(); //sorted indices
        Indices inverse = indices.getInverse();
        Indices expected = ParserIndices.parseSimple("_abmn");

        assertTrue(expected.equalsRegardlessOrder(inverse));
        assertTrue(inverse.getLower().equals(expected.getLower()));
        assertTrue(inverse.getUpper().equals(expected.getUpper()));
    }

    @Test
    public void testGetInverseIndices4() {
        Indices indices = parse("g_n*T^a*D_bzx").getIndices(); //sorted indices
        Indices inverse = indices.getInverse();
        Indices expected = ParserIndices.parseSimple("^n_a^bzx");

        assertTrue(expected.equalsRegardlessOrder(inverse));
        expected = IndicesFactory.createSorted(expected);
        assertTrue(inverse.getLower().equals(expected.getLower()));
        assertTrue(inverse.getUpper().equals(expected.getUpper()));
    }

    @Test(expected = InconsistentIndicesException.class)
    public void testTestConsistent1() {
        //_dd
        Indices indices = ParserIndices.parseSimple("^abcio_sdd");
        indices.testConsistentWithException();
    }

    @Test(expected = InconsistentIndicesException.class)
    public void testTestConsistent2() {
        //_dd^d
        Indices indices1 = ParserIndices.parseSimple("^abcio_sdd^d");
        indices1.testConsistentWithException();
    }
//    @Test
//    public void applyEmptyIndexMapping() {
//        IndexMappingImpl im = new IndexMappingImpl();
//        Indices indices = CC.parse("G_MN").getIndices();
//        Indices copy = indices.clone();
//        indices.applyIndexMapping(im);
//        assertTrue(indices.equals(copy));
//    }
}