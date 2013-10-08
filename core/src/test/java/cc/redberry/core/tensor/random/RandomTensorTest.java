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
package cc.redberry.core.tensor.random;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.*;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import junit.framework.Assert;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseExpression;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class RandomTensorTest {

    @Test
    public void test1() {
        RandomTensor rp = new RandomTensor(
                4,
                10,
                new int[]{4, 0, 0, 0},
                new int[]{10, 0, 0, 0},
                false, new Well19937c());
        Tensor t = rp.nextProduct(4, ParserIndices.parseSimple("_nm"));
        Assert.assertTrue(t.getIndices().getFree().equalsRegardlessOrder(ParserIndices.parseSimple("_nm")));
    }

    @Test
    public void testSum1() {
        RandomTensor rp = new RandomTensor(
                4,
                10,
                new int[]{4, 0, 0, 0},
                new int[]{10, 0, 0, 0},
                false, new Well19937c());
        Tensor t = rp.nextSum(5, 4, ParserIndices.parseSimple("_nm"));
        Assert.assertTrue(t.getIndices().equalsRegardlessOrder(ParserIndices.parseSimple("_nm")));
    }

    @Test
    public void testProduct1() {
        for (int j = 0; j < 20; ++j) {
            CC.resetTensorNames();
            RandomTensor random = new RandomTensor(5, 20, new int[]{2, 0, 0, 0}, new int[]{10, 0, 0, 0}, true);
            for (int i = 0; i < 20; ++i) {
                Tensor t = random.nextProduct(5, ParserIndices.parseSimple("_mnab^cd"));
                Assert.assertTrue(t.getIndices().getFree().equalsRegardlessOrder(ParserIndices.parseSimple("_mnab^cd")));
            }
        }
    }


    @Test
    public void testNullPointer() {
        CC.resetTensorNames(2312);
        RandomTensor random = new RandomTensor(5, 6, new int[]{2, 0, 0, 0}, new int[]{3, 0, 0, 0}, true, 7643543L);
        random.nextSum(5, 2, ParserIndices.parseSimple("_mn"));
        random.nextSum(5, 2, ParserIndices.parseSimple("^mn"));
    }

    @Test
    public void testMetric() {
        RandomTensor random = new RandomTensor(0, 0, new int[]{2, 0, 0, 0}, new int[]{3, 0, 0, 0}, true);
        random.addTensors(parse("g_mn"));
        Assert.assertTrue(Tensors.isKroneckerOrMetric(random.nextSimpleTensor()));
        for (int i = 0; i < 10; ++i) {
            Tensor t = random.nextProduct(i + 2, ParserIndices.parseSimple("_ab"));
            t = ((Product) t).getDataSubProduct();
            t = EliminateMetricsTransformation.eliminate(t);
            t = parseExpression("d^n_n=1").transform(t);
            Assert.assertTrue(Tensors.isKroneckerOrMetric(t));
        }
    }

    @Test
    public void testTree1() {
        RandomTensor random = new RandomTensor(0, 0, new int[]{1, 0, 0, 0}, new int[]{3, 0, 0, 0}, true);
        random.addTensors(parse("f_n"), parse("g_mn"));
        for (int i = 0; i < 100; ++i) {
            Tensor r = random.nextTensorTree(RandomTensor.TensorType.Product, 5, 2, 2, ParserIndices.parseSimple("_abc"));
            TAssert.assertEquals(r.getIndices().getFree(), IndicesFactory.create(ParserIndices.parseSimple("_abc")));
            TAssert.assertIndicesConsistency(r);
        }
    }

    @Test
    public void testTree2() {

        for (int i = 0; i < 3000; ++i) {
            CC.resetTensorNames();
            RandomTensor random = new RandomTensor(0, 0, new int[]{1, 0, 0, 0}, new int[]{3, 0, 0, 0}, true);
            random.addTensors(parse("f_n"), parse("g_mn"));
            Tensor r = random.nextTensorTree(RandomTensor.TensorType.Product, 5, 2, 2, ParserIndices.parseSimple("_abc"));
//            System.out.println(r);
            TAssert.assertIndicesConsistency(r);
            Transformation tr = new TransformationCollection(new Transformation[]{EliminateMetricsTransformation.ELIMINATE_METRICS, parseExpression("d^{g}_{g} = f_{f}*f^{f}")});
            r = EliminateMetricsTransformation.ELIMINATE_METRICS.transform(r);
            TAssert.assertIndicesConsistency(r);
            r = parseExpression("d^{g}_{g} = f_{f}*f^{f}").transform(r);
            TAssert.assertIndicesConsistency(r);

            r = ExpandTransformation.expand(r, EliminateMetricsTransformation.ELIMINATE_METRICS, CollectScalarFactorsTransformation.COLLECT_SCALAR_FACTORS);
            r = tr.transform(r);
            r = CollectScalarFactorsTransformation.collectScalarFactors(r);
            r = CollectNonScalarsTransformation.collectNonScalars(r);
            TAssert.assertIndicesConsistency(r);
            TAssert.assertTrue(r instanceof Product || r.size() <= 4);
        }
    }
}
