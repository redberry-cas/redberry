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

import cc.redberry.core.context.CC;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.parser.ParseNodeSimpleTensor;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.TensorUtils;
import junit.framework.Assert;
import org.junit.Test;

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

    public static Tensor parse(String tensor, String upper, String lower, String... indicator) {
        Indicator indicator1 = indicator.length == 0 ? Indicator.TRUE_INDICATOR : new NamesIndicator(indicator);
        return CC.current().getParseManager().parse(tensor, new IndicesInsertion(ParserIndices.parseSimple(upper), ParserIndices.parseSimple(lower), indicator1));
    }

    @Test
    public void test1() {
        Tensor t = parse("A*B*C", "^i", "_j", "A", "B", "C");
        Indices indices = ParserIndices.parseSimple("^i_j");
        assertIndicesParity(t.getIndices().getFree(), indices);
    }

    @Test
    public void test2() {
        Tensor t = parse("A*(B*A+C*K)*F", "^i", "_j");
        Indices indices = ParserIndices.parseSimple("^i_j");
        Tensor e = Tensors.parse("A^{i}_{a}*F^{b}_{j}*(B^{a}_{c}*A^{c}_{b}+K^{c}_{b}*C^{a}_{c})");
        assertIndicesParity(t.getIndices().getFree(), indices);
        assertParity(t, e);
    }

    @Test
    public void test3() {
        Tensor t = parse("A^{\\alpha n}*B*C", "^ij", "_pq");
        assertIndicesParity(t.getIndices().getFree(), ParserIndices.parseSimple("^{\\alpha n i j}_pq"));
    }

    @Test
    public void test4() {
        Tensor t = parse("a*b*A*c*B*C", "^ij", "_pq", "a", "b", "c", "d");
        Indices indices = ParserIndices.parseSimple("^ij_pq");
        assertIndicesParity(t.getIndices().getFree(), indices);
    }

    @Test
    public void test5() {
        Tensor t = parse("a*(b+a)*A*(c+d)*B*C", "^ij", "_pq", "a", "b", "c", "d");
        Tensor e = Tensors.parse("a^{ij}_{ab}*(b^{ab}_{cd}+a^{ab}_{cd})*A*(d^{cd}_{pq}+c^{cd}_{pq})*B*C");
        assertParity(t, e);
    }

    @Test
    public void test6() {
        Tensor t = parse("a*(b+a)*A*(c+d)*B*C", "^ij", "_pq", "A", "B", "C", "F");
        Indices indices = ParserIndices.parseSimple("^ij_pq");
        assertIndicesParity(t.getIndices().getFree(), indices);
    }

    @Test
    public void test7() {
        Tensor t = parse("A*(B+E*(R+K*U))", "^i", "_j");
        Indices indices = ParserIndices.parseSimple("^i_j");
        Tensor e = Tensors.parse("A^i_a*(B^a_j+E^a_b*(R^b_j+K^b_c*U^c_j))");
        assertIndicesParity(t.getIndices().getFree(), indices);
        assertEquals(t, e);
    }

    @Test
    public void test8() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor t = parse("L*L*(L-1)*A*B", "^ijpq", "_pqrs", "A", "B");
            Indices indices = ParserIndices.parseSimple("^ijpq_pqrs");
            assertIndicesParity(t.getIndices().getFree(), indices.getFree());
        }
    }

    @Test
    public void test9() {
        Tensor t = parse("a*A*B+((1/2)*a+b)*A*(A+B*(A+X*A))*c", "^a", "_b", "A", "B");
        Indices indices = ParserIndices.parseSimple("^a_b");
        assertIndicesParity(t.getIndices().getFree(), indices.getFree());
    }

    @Test
    public void test10() {
        Tensor t = parse("A=2*B+A*B", "^i", "_j");
        Indices indices = ParserIndices.parseSimple("^i_j");
        assertIndicesParity(t.getIndices().getFree(), indices.getFree());
    }

    @Test
    public void performance() {
        Tensor t = parse("(1/10)*L*L*HATK^{\\delta}*DELTA^{\\mu\\nu\\alpha\\beta}*HATK^{\\gamma}*n_{\\sigma}*n_{\\lambda}*R^{\\sigma}_{\\alpha\\beta\\gamma}*R^{\\lambda}_{\\mu\\nu\\delta} + "
                + "L*L*(L-1)*(L-1)*(L-2)*HATK^{\\beta\\gamma\\delta}*DELTA^{\\alpha}*HATK^{\\mu\\nu}*n_{\\sigma}*n_{\\lambda}*((2/45)*R^{\\lambda}_{\\alpha\\delta\\nu}*R^{\\sigma}_{\\beta\\mu\\gamma}-(1/120)*R^{\\lambda}_{\\delta\\alpha\\nu}*R^{\\sigma}_{\\beta\\mu\\gamma}) +"
                + "L*L*(L-1)*HATK^{\\delta}*DELTA^{\\alpha\\beta\\gamma}*HATK^{\\mu\\nu}*n_{\\sigma}*n_{\\lambda}*(-(1/10)*R^{\\lambda}_{\\mu\\gamma\\nu}*R^{\\sigma}_{\\alpha\\delta\\beta}+(1/15)*R^{\\lambda}_{\\delta\\alpha\\nu}*R^{\\sigma}_{\\beta\\mu\\gamma}+(1/60)*R^{\\lambda}_{\\beta\\delta\\nu}*R^{\\sigma}_{\\gamma\\mu\\alpha})+"
                + "L*L*(L-1)*(L-1)*HATK^{\\gamma\\delta}*DELTA^{\\alpha\\beta}*HATK^{\\mu\\nu}*n_{\\sigma}*n_{\\lambda}*(-(1/20)*R^{\\lambda}_{\\mu\\beta\\nu}*R^{\\sigma}_{\\delta\\alpha\\gamma}+(1/180)*R^{\\lambda}_{\\alpha\\nu\\beta}*R^{\\sigma}_{\\gamma\\delta\\mu}-(7/360)*R^{\\lambda}_{\\mu\\gamma\\nu}*R^{\\sigma}_{\\alpha\\delta\\beta}-(1/240)*R^{\\lambda}_{\\delta\\beta\\nu}*R^{\\sigma}_{\\gamma\\alpha\\mu}-(1/120)*R^{\\lambda}_{\\beta\\gamma\\nu}*R^{\\sigma}_{\\alpha\\delta\\mu}-(1/30)*R^{\\lambda}_{\\delta\\beta\\nu}*R^{\\sigma}_{\\alpha\\gamma\\mu})+"
                + "L*L*(L-1)*HATK^{\\mu\\nu}*DELTA^{\\alpha\\beta\\gamma}*HATK^{\\delta}*n_{\\sigma}*n_{\\lambda}*((7/120)*R^{\\lambda}_{\\beta\\gamma\\nu}*R^{\\sigma}_{\\mu\\alpha\\delta}-(3/40)*R^{\\lambda}_{\\beta\\gamma\\delta}*R^{\\sigma}_{\\mu\\alpha\\nu}+(1/120)*R^{\\lambda}_{\\delta\\gamma\\nu}*R^{\\sigma}_{\\alpha\\beta\\mu})+"
                + "L*L*HATK^{\\mu}*DELTA^{\\alpha\\beta\\gamma}*HATK^{\\nu}*{\\nu}_{\\lambda}*(-(1/8)*R_{\\beta\\gamma}*R^{\\lambda}_{\\nu\\alpha\\mu}+(3/20)*R_{\\beta\\gamma}*R^{\\lambda}_{\\mu\\alpha\\nu}+(3/40)*R_{\\alpha\\mu}*R^{\\lambda}_{\\beta\\gamma\\nu}+(1/40)*R^{\\sigma}_{\\beta\\gamma\\mu}*R^{\\lambda}_{\\nu\\alpha\\sigma}-(3/20)*R^{\\sigma}_{\\alpha\\beta\\mu}*R^{\\lambda}_{\\gamma\\nu\\sigma}+(1/10)*R^{\\sigma}_{\\alpha\\beta\\nu}*R^{\\lambda}_{\\gamma\\mu\\sigma})+"
                + "L*L*(L-1)*HATK^{\\gamma}*DELTA^{\\alpha\\beta}*HATK^{\\mu\\nu}*n_{\\lambda}*((1/20)*R_{\\alpha\\nu}*R^{\\lambda}_{\\gamma\\beta\\mu}+(1/20)*R_{\\alpha\\gamma}*R^{\\lambda}_{\\mu\\beta\\nu}+(1/10)*R_{\\alpha\\beta}*R^{\\lambda}_{\\mu\\gamma\\nu}+(1/20)*R^{\\sigma}_{\\alpha\\nu\\gamma}*R^{\\lambda}_{\\sigma\\beta\\mu}-(1/60)*R^{\\sigma}_{\\mu\\alpha\\nu}*R^{\\lambda}_{\\beta\\sigma\\gamma}+(1/10)*R^{\\sigma}_{\\alpha\\beta\\gamma}*R^{\\lambda}_{\\mu\\sigma\\nu}-(1/12)*R^{\\sigma}_{\\alpha\\beta\\nu}*R^{\\lambda}_{\\mu\\sigma\\gamma})+"
                + "L*L*(L-1)*(L-1)*HATK^{\\alpha\\beta}*DELTA^{\\gamma}*HATK^{\\mu\\nu}*n_{\\lambda}*((1/60)*R_{\\alpha\\mu}*R^{\\lambda}_{\\beta\\nu\\gamma}-(1/20)*R_{\\alpha\\mu}*R^{\\lambda}_{\\gamma\\nu\\beta}+(1/120)*R_{\\alpha\\beta}*R^{\\lambda}_{\\mu\\nu\\gamma}+(3/40)*R_{\\alpha\\gamma}*R^{\\lambda}_{\\nu\\beta\\mu}+(1/20)*R^{\\sigma}_{\\gamma\\mu\\alpha}*R^{\\lambda}_{\\nu\\sigma\\beta}+(1/120)*R^{\\sigma}_{\\alpha\\mu\\gamma}*R^{\\lambda}_{\\beta\\nu\\sigma}-(1/40)*R^{\\sigma}_{\\alpha\\mu\\gamma}*R^{\\lambda}_{\\sigma\\nu\\beta}+(1/40)*R^{\\sigma}_{\\alpha\\mu\\beta}*R^{\\lambda}_{\\sigma\\nu\\gamma}-(1/20)*R^{\\sigma}_{\\alpha\\mu\\beta}*R^{\\lambda}_{\\gamma\\nu\\sigma}-(1/40)*R^{\\sigma}_{\\mu\\beta\\nu}*R^{\\lambda}_{\\gamma\\sigma\\alpha})+"
                + "L*L*(L-1)*HATK^{\\alpha\\beta}*DELTA^{\\mu\\nu}*HATK^{\\gamma}*n_{\\lambda}*((1/20)*R^{\\sigma}_{\\mu\\nu\\beta}*R^{\\lambda}_{\\gamma\\sigma\\alpha}-(7/60)*R^{\\sigma}_{\\beta\\mu\\alpha}*R^{\\lambda}_{\\gamma\\nu\\sigma}+(1/20)*R^{\\sigma}_{\\beta\\mu\\alpha}*R^{\\lambda}_{\\sigma\\nu\\gamma}+(1/10)*R^{\\sigma}_{\\mu\\beta\\gamma}*R^{\\lambda}_{\\nu\\alpha\\sigma}+(1/60)*R^{\\sigma}_{\\mu\\beta\\gamma}*R^{\\lambda}_{\\alpha\\nu\\sigma}+(7/120)*R_{\\alpha\\beta}*R^{\\lambda}_{\\nu\\gamma\\mu}+(11/60)*R_{\\beta\\mu}*R^{\\lambda}_{\\nu\\alpha\\gamma})", "^ijpq", "_pqrs", "HATK", "DELTA");

        Indices indices = ParserIndices.parseSimple("^ijpq_pqrs");
        assertIndicesParity(t.getIndices().getFree(), indices.getFree());
    }

    @Test
    public void test11() {
        String expression = "DELTA^m=-L*HATK^m";
        Tensors.parse(expression);

        final String[] matrices = new String[]{"KINV", "HATK", "HATW", "HATS", "NABLAS", "HATN", "HATF", "NABLAF", "HATM", "DELTA", "Flat", "FF", "WR", "SR", "SSR", "FR", "RR"};
        Indicator<ParseNodeSimpleTensor> matricesIndicator = new Indicator<ParseNodeSimpleTensor>() {

            @Override
            public boolean is(ParseNodeSimpleTensor object) {
                String name = object.name;
                for (String matrix : matrices)
                    if (name.equals(matrix))
                        return true;
                return false;
            }
        };

        IndicesInsertion indicesInsertion = new IndicesInsertion(ParserIndices.parseSimple("^{a}"), ParserIndices.parseSimple("_{a}"), matricesIndicator);
        Expression e = (Expression) Tensors.parse(expression, indicesInsertion);

        Tensor expected = Tensors.parse("DELTA^ma_a=-L*HATK^ma_a");
        Assert.assertTrue(TensorUtils.equalsExactly(e, expected));
    }

    @Test
    public void test12() {
        final String[] matrices = new String[]{"KINV", "HATK", "HATW", "HATS", "NABLAS", "HATN", "HATF", "NABLAF", "HATM", "DELTA", "Flat", "FF", "WR", "SR", "SSR", "FR", "RR"};
        Indicator<ParseNodeSimpleTensor> matricesIndicator = new Indicator<ParseNodeSimpleTensor>() {

            @Override
            public boolean is(ParseNodeSimpleTensor object) {
                String name = object.name;
                for (String matrix : matrices)
                    if (name.equals(matrix))
                        return true;
                return false;
            }
        };

        IndicesInsertion indicesInsertion = new IndicesInsertion(ParserIndices.parseSimple("^{a}"), ParserIndices.parseSimple("_{a}"), matricesIndicator);
        Expression e = (Expression) Tensors.parse("ACTION = Flat + WR + SR + SSR + FF + FR + RR ", indicesInsertion);
        assertTrue(true);
    }

    @Test
    public void test13() {
        Tensor t = parse("A+B","^i", "_i", "A");
        Tensor e = Tensors.parse("A^i_i+B");
        assertEquals(t, e);
    }
}
