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
package cc.redberry.core.transformations;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import junit.framework.TestCase;

import static cc.redberry.core.transformations.ApplyDiracDeltasTransformation.APPLY_DIRAC_DELTAS_TRANSFORMATION;

/**
 * Created by poslavsky on 06/01/16.
 */
public class ApplyDiracDeltasTransformationTest extends TestCase {
    public void test1() throws Exception {
        for (int i = 0; i < 30; i++) {
            CC.reset();
            Tensor t = Tensors.parse("DiracDelta[q,p-l]*DiracDelta[l,f]*(q**2-2)");
            TAssert.assertEquals("(p-f)**2-2", APPLY_DIRAC_DELTAS_TRANSFORMATION.transform(t));
        }
    }

    public void test2() throws Exception {
        for (int i = 0; i < 30; i++) {
            CC.reset();
            Tensor t = Tensors.parse("DiracDelta[q,p-l]*DiracDelta[l,f-k]*DiracDelta[-k,r]*(q**2-2)");
            TAssert.assertEquals("(p-f-r)**2-2", APPLY_DIRAC_DELTAS_TRANSFORMATION.transform(t));
        }
    }

    public void test3() throws Exception {
        Tensor t = Tensors.parse("DiracDelta[q,f]*DiracDelta[-q,p]*(q**2-2)");
        Tensor e = APPLY_DIRAC_DELTAS_TRANSFORMATION.transform(t);
        TAssert.assertEquals("p**2-2", e);
    }
}