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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.TAssert;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.TransformationCollection;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.transformations.expand.ExpandTransformation.expand;

/**
 * @author Stanislav Poslavsky
 */
public class PassarinoVeltmanTest {
    @Test
    public void test1() throws Exception {
        Expression subs = PassarinoVeltman.generateSubstitution(1, parseSimple("q_a"),
                new SimpleTensor[]{parseSimple("k1_a")});
        TAssert.assertEquals("q_a = q_b*k1^b * k1_a/(k1_c*k1^c)", subs);
    }

    @Test
    public void test2() throws Exception {
        TransformationCollection simpl = new TransformationCollection(
                parseExpression("k1_a*k1^a = 0"),
                parseExpression("k2_a*k2^a = 0"),
                parseExpression("k1_a*k2^a = s")
        );
        Expression subs = PassarinoVeltman.generateSubstitution(1, parseSimple("q_a"),
                new SimpleTensor[]{parseSimple("k1_a"), parseSimple("k2_a")}, simpl);
        TAssert.assertEquals("q_a = k1_a*(q^b*k2_b)/s + k2_a*(q^b*k1_b)/s", subs);
    }

    @Test
    public void test3() throws Exception {
        TransformationCollection simpl = new TransformationCollection(
                parseExpression("k1_a*k1^a = m1"),
                parseExpression("k2_a*k2^a = m2"),
                parseExpression("k1_a*k2^a = s")
        );
        Expression subs = PassarinoVeltman.generateSubstitution(1, parseSimple("q_a"),
                new SimpleTensor[]{parseSimple("k1_a"), parseSimple("k2_a")}, simpl);
        Tensor expected = parse("k1_a*((q^b*k2_b)*s - (q^b*k1_b)*m2)/(s**2 - m1*m2) + k2_a*((q^b*k1_b)*s - (q^b*k2_b)*m1)/(s**2 - m1*m2)");
        TAssert.assertEquals(expand(expected), expand(subs.get(1)));
    }

    @Test
    public void test4() throws Exception {
        Tensor[][] input = new Tensor[][]{
                {parse("k1_i"), parse("0")},
                {parse("k2_i"), parse("0")},
                {parse("k3_i"), parse("m")},
                {parse("k4_i"), parse("m")}
        };
        TransformationCollection simpl = new TransformationCollection(FeynCalcUtils.setMandelstam(input));

        Expression subs = PassarinoVeltman.generateSubstitution(4, parseSimple("q_a"),
                new SimpleTensor[]{
                }, simpl);

        Tensor tensor = subs.get(1);
        System.out.println(expand(tensor).size());
    }
}