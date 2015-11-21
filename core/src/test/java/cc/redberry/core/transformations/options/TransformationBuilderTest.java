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
package cc.redberry.core.transformations.options;

import cc.redberry.core.TAssert;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.DifferentiateTransformation;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.expand.ExpandTensorsTransformation;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;
import static cc.redberry.core.transformations.options.TransformationBuilder.createTransformation;

/**
 * Created by poslavsky on 07/10/15.
 */
public class TransformationBuilderTest {
    static class opts {
        @Option(name = "integer", index = 0)
        public int integer;

        @Option(name = "requiredInteger", index = 1)
        public int requiredInteger;

        @Option(name = "defaultString", index = 2)
        public String defaultString = "default";

        @Option(name = "string", index = 3)
        public String string;

        public opts() {
        }

        public opts(int requiredInteger, String string) {
            this.requiredInteger = requiredInteger;
            this.string = string;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            opts opts = (opts) o;

            if (integer != opts.integer) return false;
            if (requiredInteger != opts.requiredInteger) return false;
            if (defaultString != null ? !defaultString.equals(opts.defaultString) : opts.defaultString != null)
                return false;
            if (string != null ? !string.equals(opts.string) : opts.string != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = integer;
            result = 31 * result + requiredInteger;
            result = 31 * result + (defaultString != null ? defaultString.hashCode() : 0);
            result = 31 * result + (string != null ? string.hashCode() : 0);
            return result;
        }
    }

    @Test
    public void test2() throws Exception {
        Assert.assertEquals(new opts(1, "string"),
                TransformationBuilder.buildOptionsFromMap(opts.class, new HashMap<String, Object>() {{
                    put("requiredInteger", 1);
                    put("string", "string");
                }}));
    }

    @Test
    public void testExpandAndEliminate() throws Exception {
        ExpandTransformation expand = createTransformation(ExpandTransformation.class,
                Collections.emptyList(), new HashMap<String, Object>() {{
                    put("Simplifications", EliminateMetricsTransformation.ELIMINATE_METRICS);
                }});
        TAssert.assertEquals("t_{n}^{n}+d^{n}_{n}", expand.transform(Tensors.parse("g_mn*(g^mn + t^mn)")));
    }

    @Test
    public void testExpand() throws Exception {
        ExpandTransformation expand = createTransformation(ExpandTransformation.class,
                Collections.emptyList(), new HashMap<String, Object>());
        TAssert.assertEquals("g_mn*g^mn + g_mn*t^mn", expand.transform(Tensors.parse("g_mn*(g^mn + t^mn)")));
    }

    @Test
    public void testDifferentiate() throws Exception {
        DifferentiateTransformation d = createTransformation(DifferentiateTransformation.class,
                new ArrayList<Object>() {{
                    add(parseSimple("x"));
                }}, new HashMap<String, Object>() {{
                    put("Simplifications", EliminateMetricsTransformation.ELIMINATE_METRICS);
                }});
        TAssert.assertEquals("2*x", d.transform(parse("x**2")));
    }


    @Test
    public void testExpandTensors() throws Exception {
        ExpandTensorsTransformation d = createTransformation(ExpandTensorsTransformation.class,
                Collections.EMPTY_LIST, new HashMap<String, Object>() {{
                    put("LeaveScalars", false);
                    put("Simplifications", EliminateMetricsTransformation.ELIMINATE_METRICS);
                }});
        Tensor t = parse("2*(x_a^a + 1)*f_mn");
        TAssert.assertEquals("2*x_{a}^{a}*f_{mn}+2*f_{mn}", d.transform(t));

        d = createTransformation(ExpandTensorsTransformation.class,
                Collections.EMPTY_LIST, new HashMap<String, Object>() {{
                    put("LeaveScalars", true);
                    put("Simplifications", EliminateMetricsTransformation.ELIMINATE_METRICS);
                }});

        TAssert.assertEquals(t, d.transform(t));
    }


    @Test
    public void testExpand2() throws Exception {
        ExpandTransformation expand = createTransformation(ExpandTransformation.class,
                Collections.emptyList());
        TAssert.assertEquals("g_mn*g^mn + g_mn*t^mn", expand.transform(Tensors.parse("g_mn*(g^mn + t^mn)")));
    }
}