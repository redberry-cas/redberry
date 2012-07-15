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
package cc.redberry.core.parser.preprocessor;

import cc.redberry.core.context.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.parser.*;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.*;
import org.junit.*;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.TAssert.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IndicesInsertionTest {

    private static class NamesIndicator implements Indicator<ParseNodeSimpleTensor> {

        private final String[] names;

        public NamesIndicator(String... name) {
            this.names = name;
        }

        @Override
        public boolean is(ParseNodeSimpleTensor object) {
            for (String s : names)
                if (s.equals(object.name))
                    return true;
            return false;
        }
    }

    public static void attachPreprocessor(String upper, String lower, String... indicator) {
        CC.current().getParseManager().getNodesPreprocessors().add(new IndicesInsertion(ParserIndices.parseSimple(upper), ParserIndices.parseSimple(lower), new NamesIndicator(indicator)));
    }

    public static void attachPreprocessorWithTrueIndicator(String upper, String lower) {
        CC.current().getParseManager().getNodesPreprocessors().add(new IndicesInsertion(ParserIndices.parseSimple(upper), ParserIndices.parseSimple(lower), Indicator.TRUE_INDICATOR));
    }

    public static void clearPreprocessors() {
        CC.current().getParseManager().getNodesPreprocessors().clear();
    }

    @Test
    public void test1() {
        attachPreprocessor("^i", "_j", "A", "B", "C");
        Tensor t = parse("A*B*C");
        Indices indices = ParserIndices.parseSimple("^i_j");
        System.out.println(t);
        assertIndicesParity(t.getIndices().getFreeIndices(), indices);
    }

    @Test
    public void test2() {
        attachPreprocessorWithTrueIndicator("^i", "_j");
        Tensor t = parse("A*(B*A+C*K)*F");
        Indices indices = ParserIndices.parseSimple("^i_j");
        clearPreprocessors();
        Tensor e = parse("A^{i}_{a}*F^{b}_{j}*(B^{a}_{c}*A^{c}_{b}+K^{c}_{b}*C^{a}_{c})");
        assertIndicesParity(t.getIndices().getFreeIndices(), indices);
        assertParity(t, e);
    }

    @Test
    public void test3() {
        attachPreprocessorWithTrueIndicator("^ij", "_pq");
        Tensor t = parse("A^{\\alpha n}*B*C");
        assertIndicesParity(t.getIndices().getFreeIndices(), ParserIndices.parseSimple("^{\\alpha n i j}_pq"));
    }

    @Test
    public void test4() {
        attachPreprocessor("^ij", "_pq", "a", "b", "c", "d");
        Tensor t = parse("a*b*A*c*B*C");
        Indices indices = ParserIndices.parseSimple("^ij_pq");
        assertIndicesParity(t.getIndices().getFreeIndices(), indices);
    }

    @Test
    public void test5() {
        attachPreprocessor("^ij", "_pq", "a", "b", "c", "d");
        Tensor t = parse("a*(b+a)*A*(c+d)*B*C");
        clearPreprocessors();
        Tensor e = parse("a^{ij}_{ab}*(b^{ab}_{cd}+a^{ab}_{cd})*A*(d^{cd}_{pq}+c^{cd}_{pq})*B*C");
        assertParity(t, e);
    }

    @Test
    public void test6() {
        attachPreprocessor("^ij", "_pq", "A", "B", "C", "F");
        Tensor t = parse("a*(b+a)*A*(c+d)*B*C");
        Indices indices = ParserIndices.parseSimple("^ij_pq");
        assertIndicesParity(t.getIndices().getFreeIndices(), indices);
    }

    @Test
    public void test7() {
        attachPreprocessorWithTrueIndicator("^i", "_j");
        Tensor t = parse("A*(B+E*(R+K*U))");
        Indices indices = ParserIndices.parseSimple("^i_j");
        clearPreprocessors();
        Tensor e = parse("A^i_a*(B^a_j+E^a_b*(R^b_j+K^b_c*U^c_j))");
        assertIndicesParity(t.getIndices().getFreeIndices(), indices);
        assertEquals(t, e);
    }

    @Test
    public void test8() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            attachPreprocessor("^ijpq", "_pqrs", "A", "B");
            Tensor t = parse("L*L*(L-1)*A*B");
            clearPreprocessors();
            Indices indices = ParserIndices.parseSimple("^ijpq_pqrs");
            assertIndicesParity(t.getIndices().getFreeIndices(), indices.getFreeIndices());
        }
    }

    @Test
    public void test10() {
        attachPreprocessor("^a", "_b", "A", "B");
        Tensor t = parse("a*A*B+((1/2)*a+b)*A*(A+B*(A+X*A))*c");
        Indices indices = ParserIndices.parseSimple("^a_b");
        assertIndicesParity(t.getIndices().getFreeIndices(), indices.getFreeIndices());
    }
}
