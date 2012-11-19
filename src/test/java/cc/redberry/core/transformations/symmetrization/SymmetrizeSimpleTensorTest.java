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
package cc.redberry.core.transformations.symmetrization;

import cc.redberry.core.TAssert;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.combinatorics.symmetries.SymmetriesFactory;
import cc.redberry.core.context.CC;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import org.junit.Test;

import static cc.redberry.core.combinatorics.symmetries.SymmetriesFactory.createFullSymmetries;
import static cc.redberry.core.indices.IndexType.LatinLower;
import static cc.redberry.core.tensor.Tensors.addSymmetry;
import static cc.redberry.core.tensor.Tensors.parseSimple;
import static cc.redberry.core.transformations.symmetrization.SymmetrizeSimpleTensor.symmetrize;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SymmetrizeSimpleTensorTest {
    @Test
    public void test1() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            SimpleTensor t = parseSimple("T_abcd");
            TAssert.assertEquals(
                    symmetrize(t, new int[]{0, 1, 2}, createFullSymmetries(3)),
                    "1/6*(T_{acbd}+T_{abcd}+T_{cbad}+T_{cabd}+T_{bcad}+T_{bacd})");
        }
    }

    @Test
    public void test2() {
        SimpleTensor t = parseSimple("T_abcd");
        Symmetries symmetries = SymmetriesFactory.createSymmetries(4);
        symmetries.addUnsafe(new Symmetry(new int[]{2, 3, 0, 1}, false));
        symmetries.addUnsafe(new Symmetry(new int[]{1, 0, 2, 3}, true));

        Tensor r = symmetrize(t, new int[]{0, 1, 2, 3}, symmetries);

        TAssert.assertEquals(r,
                "(1/8)*(-T_{abdc}+T_{badc}+T_{dcba}+T_{abcd}+T_{cdab}-T_{bacd}-T_{dcab}-T_{cdba})");
    }

    @Test
    public void test3() {
        SimpleTensor t = parseSimple("T_abcd");
        addSymmetry(t, LatinLower, false, new int[]{1, 0, 2, 3});

        Tensor r = symmetrize(t, new int[]{0, 1, 2, 3}, SymmetriesFactory.createFullSymmetries(4));
        TAssert.assertEquals(r,
                "(1/12)*(T_{adbc}+T_{acdb}+T_{abcd}+T_{bcda}+T_{bcad}+T_{bdac}+T_{acbd}+T_{bdca}+T_{cdab}+T_{adcb}+T_{cdba}+T_{abdc})");
    }

    @Test
    public void test4() {
        SimpleTensor t = parseSimple("T_abcd");
        addSymmetry(t, LatinLower, false, new int[]{2, 3, 0, 1});

        Symmetries symmetries = SymmetriesFactory.createSymmetries(4);
        symmetries.addUnsafe(new Symmetry(new int[]{2, 3, 0, 1}, false));
        symmetries.addUnsafe(new Symmetry(new int[]{1, 0, 2, 3}, true));

        Tensor r = symmetrize(t, new int[]{0, 1, 2, 3}, symmetries);
        System.out.println(r);

        TAssert.assertEquals(r,
                "(1/4)*(-T_{dcab}-T_{bacd}+T_{abcd}+T_{dcba})");
    }

}
