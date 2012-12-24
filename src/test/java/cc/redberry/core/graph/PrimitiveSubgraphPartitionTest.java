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

import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

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
            PrimitiveSubgraphPartition partition = new PrimitiveSubgraphPartition((Product) t, IndexType.LatinLower);
            PrimitiveSubgraph[] ss = partition.getPartition();
            for (PrimitiveSubgraph ps : ss) {
                System.out.println(ps);
            }

        }
    }
}
