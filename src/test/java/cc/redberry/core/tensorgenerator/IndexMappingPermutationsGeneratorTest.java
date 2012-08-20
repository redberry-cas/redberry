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
package cc.redberry.core.tensorgenerator;

import cc.redberry.core.context.CC;
import cc.redberry.core.tensor.SumBuilder;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IndexMappingPermutationsGeneratorTest {

    public IndexMappingPermutationsGeneratorTest() {
    }

    @Test
    public void test1() {
        for (Tensor t : IndexMappingPermutationsGenerator.getAllPermutations(Tensors.parse("g_mn*g_ab")))
            System.out.println(t);
    }

    @Test
    public void test2() {
        for (Tensor t : IndexMappingPermutationsGenerator.getAllPermutations(Tensors.parse("g_mn*g^ab")))
            System.out.println(t);
    }

    @Test
    public void test3() {
        CC.resetTensorNames(-9039046884230366966L);
        Tensor tensor = Tensors.parse("g_ab*g^rs*g^pq*g_mn");
        //g_{ab}*g^{rs}*g_{mn}*g^{pq}
        //indices ^{pqrs}_{abmn}

        int c = 0;
        SumBuilder sb = new SumBuilder();
        for (Tensor t : IndexMappingPermutationsGenerator.getAllPermutations(tensor)) {
            c++;
            sb.put(t);
        }
        assertTrue(c == 9);

//          g_{mn}*g_{ab}*g^{pq}*g^{rs} 
//          g_{mn}*g_{ab}*g^{pr}*g^{qs}
//          g_{mn}*g_{ab}*g^{ps}*g^{qr} 
//          g_{bn}*g_{am}*g^{pq}*g^{rs}
//          g_{bn}*g_{am}*g^{pr}*g^{qs} 
//          g_{bn}*g_{am}*g^{ps}*g^{qr}
//          g_{bm}*g_{an}*g^{pq}*g^{rs} 
//          g_{bm}*g_{an}*g^{pr}*g^{qs}
//          g_{bm}*g_{an}*g^{ps}*g^{qr}

    }
}
