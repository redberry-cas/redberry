/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.substitutions.SubstitutionTransformation;
import org.junit.Test;

import static cc.redberry.core.transformations.Transformation.Util.applyUntilUnchanged;
import static cc.redberry.core.transformations.expand.ExpandTransformation.expand;

/**
 * Created by poslavsky on 03/03/16.
 */
public class AbbreviationsBuilderTest {
    @Test
    public void test1() throws Exception {
        AbbreviationsBuilder abbrs = new AbbreviationsBuilder();

        Tensor t = Tensors.parse("(c*(a+b) + f)*k_a*k^a*f_q + 2*(c*(-a-b) - f)*k_a*k^a*t_q");
        assertCorrectAbbreviations(abbrs, t);

        Tensor r = abbrs.transform(t);

        System.out.println(r);
    }

    @Test
    public void test2() throws Exception {
        AbbreviationsBuilder abbrs = new AbbreviationsBuilder();
        abbrs.abbreviateTopLevel = true;

        Tensor t = Tensors.parse("a*(c+d) + b*(c+d)");
        assertCorrectAbbreviations(abbrs, t);

        Tensor r = abbrs.transform(t);

        System.out.println(r);
    }

    @Test
    public void test3() throws Exception {
        AbbreviationsBuilder abbrs = new AbbreviationsBuilder();
        abbrs.abbreviateTopLevel = false;

        Tensor t = Tensors.parse("(a+b)*k_a*p^a + (a+b)*f_a*t^a");
        assertCorrectAbbreviations(abbrs, t);

        Tensor r = abbrs.transform(t);

        System.out.println(r);
        for (AbbreviationsBuilder.Abbreviation abbreviation : abbrs.getAbbreviations()) {
            System.out.println(abbreviation);
        }
    }

    private static void assertCorrectAbbreviations(AbbreviationsBuilder abbrs, Tensor t) {
        Tensor r = abbrs.transform(t);
        SubstitutionTransformation subs = abbrs.abbreviationReplacements();
        TAssert.assertEquals(expand(t), expand(applyUntilUnchanged(r, subs)));
    }
}