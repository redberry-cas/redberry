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
package cc.redberry.core.tensor;

import cc.redberry.core.indices.SimpleIndices;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;

import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorFieldTest {

    @Test
    public void testIterator() {
        Tensor t = parse("f[a,b,c]");
        int i = 0;
        for (Tensor c : t)
            ++i;
        Assert.assertEquals(i, 3);
    }

    @Test
    public void testDerivativeSymmetries() {
        SimpleTensor t = parseSimple("f_mn[x_a, y_b]");
        addSymmetry(t, 1, 0);

        SimpleTensor d = parseSimple("f~(1,1)_mnab[x_a, y_b]");
//        System.out.println(d.getIndices().getSymmetries().getInnerSymmetries());

        d = parseSimple("f~(2,0)_{mn {ab}}[x_a, y_b]");
//        System.out.println(d.getIndices().getSymmetries().getInnerSymmetries());

        d = parseSimple("f~(2, 1)_{mn {ab} {c}}[x_a, y_b]");
//        System.out.println(d.getIndices().getSymmetries().getInnerSymmetries());

        d = parseSimple("f~(2, 2)_{mn {ab} {cd}}[x_a, y_b]");
//        System.out.println(d.getIndices().getSymmetries().getInnerSymmetries());

        //D[y_n, x_m][f[x_m,y_p]] == f~(1,1)^mn[x_m, y_p]

        //D[y_n, x_m][f[x_m*x^m,y_p]] == f~(1,1)^n[x_m*x^m, y_p]*2*x^m


        addSymmetry("f_mn[x_ab, y_c]", 1, 0);
        d = parseSimple("f~(2, 2)_{mn {ax by} {cd}}[x_ab, y_b]");
//        System.out.println(d.getIndices().getSymmetries().getInnerSymmetries());
    }

    @Test
    public void testPartition1() {
        TensorField f = (TensorField) parse("f~(2,3, 2)_{mn {ab cd} {x y z} {AB}}[x_ab,f_c, x_A]");
        SimpleIndices[][] iP = f.calcIndicesPartition();
        for(int i=0; i<iP.length; ++i){
            System.out.println(Arrays.toString(iP[i]));
        }
    }

    @Test
    public void testNames() {
        Tensor t1 = parse("F[S_A'^B']"), t2 = parse("F[S^A'_B']");
        Assert.assertEquals(((TensorField) t1).getName(), ((TensorField) t2).getName());
    }
}
