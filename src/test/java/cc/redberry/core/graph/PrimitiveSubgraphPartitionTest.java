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
 * the Free Software Foundation, either version 2 of the License, or
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
package cc.redberry.core.graph;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.ProductBuilder;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.random.TRandom;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static cc.redberry.core.graph.PrimitiveSubgraphPartition.calculatePartition;
import static cc.redberry.core.tensor.Tensors.addSymmetry;
import static cc.redberry.core.tensor.Tensors.parse;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PrimitiveSubgraphPartitionTest {

    @Test
    public void test1() {

//        CC.resetTensorNames(-760323547556625542L);
        for (int i = 0; i < 1000; ++i) {
            CC.resetTensorNames();
//            System.out.println(CC.getNameManager().getSeed());
            Tensor t = parse("A^m_n*B^nk_b*G^b_c*X^i_jk*Y^j_i*2");
//            System.out.println(t);
            PrimitiveSubgraph[] ss = calculatePartition((Product) t, IndexType.LatinLower);
            for (PrimitiveSubgraph ps : ss) {
                System.out.println(ps);
            }

        }
    }

    @Test
    public void test2() {
        TRandom random = new TRandom(
                5, 20,
                new int[]{1, 0, 0, 0},
                new int[]{3, 0, 0, 0}, false, -7201529248298620939L);
        System.out.println(random.getSeed());

        for (int i = 0; i < 1000; ++i) {
            Product product = (Product) ((Product) random.nextProduct(10)).getDataSubProduct();
            PrimitiveSubgraph[] ss = calculatePartition(product, IndexType.LatinLower);
            ProductBuilder pb = new ProductBuilder();
            for (PrimitiveSubgraph ps : ss)
                pb.put(extract(product, ps.getPartition()));
            TAssert.assertEquals(pb.build(), product);
        }
    }

    @Test
    public void test2a() {
        CC.resetTensorNames(-6981255382429807323L);
        TRandom random = new TRandom(
                5, 20,
                new int[]{1, 0, 0, 0},
                new int[]{3, 0, 0, 0}, false,
                -7201529248298620939L);

        Product product = (Product) parse("H_{gf}*J_{a}*I^{j}_{h}*I^{dc}*I^{i}_{j}*I^{e}_{d}*A_{i}^{h}*E^{g}*E^{f}*E_{e}*J_{cb}");
        System.out.println(product);
        PrimitiveSubgraph[] ss = calculatePartition(product, IndexType.LatinLower);
        ProductBuilder pb = new ProductBuilder();
        for (PrimitiveSubgraph ps : ss) {
            System.out.println(ps + "  " + Arrays.toString(extractArray(product, ps.getPartition())));
            pb.put(extract(product, ps.getPartition()));
        }
        TAssert.assertEquals(pb.build(), product);

    }

    @Test
    public void test3() {
        Tensor t = parse("A^a_b*B^b_c*C^c_a");
        PrimitiveSubgraph[] ss = calculatePartition((Product) t, IndexType.LatinLower);
        TAssert.assertEquals(ss.length, 1);
        int[] p = ss[0].getPartition();
        Arrays.sort(p);
        Assert.assertArrayEquals(p, new int[]{0, 1, 2});
    }

    private static int indexOf(Tensor t, Tensor in) {
        for (int i = 0; i < in.size(); ++i) {
            if (TensorUtils.equals(t, in.get(i)))
                return i;
        }
        return -1;
    }

    private static Tensor[] extractArray(Tensor t, int[] positions) {
        Tensor[] pb = new Tensor[positions.length];
        int c = -1;
        for (int i : positions)
            pb[++c] = t.get(i);
        return pb;
    }

    private static Tensor extract(Tensor t, int[] positions) {
        ProductBuilder pb = new ProductBuilder();
        for (int i : positions)
            pb.put(t.get(i));
        return pb.build();
    }
}
