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
package cc.redberry.core.performance.kv;

import cc.redberry.core.tensor.*;
import org.junit.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class OneLoopInputTest {

    @Test
    public void testSomeMethod() {
        int operatorOrder = 2;
        Expression KINV = (Expression) Tensors.parse("KINV^\\mu_\\nu=f^\\mu_\\nu");
        Expression K = (Expression) Tensors.parse("K^\\mu_\\nu^\\alpha_\\beta=g^{\\mu\\alpha}_{\\nu\\beta}");
        Expression S = (Expression) Tensors.parse("S^\\mu^\\alpha_\\beta=g^{\\mu\\alpha}_{\\beta}");
        Expression W = (Expression) Tensors.parse("W^\\alpha_\\beta=d^{\\alpha}_{\\beta}");
        OneLoopInput input = new OneLoopInput(operatorOrder, KINV, K, S, W, null, null);
        for (int k = 0; k < 3; ++k)
            for (int i = 0; i < operatorOrder + 1 - k; ++i)
                System.out.println(input.getHatQuantities(k)[i]);
        
        for(Tensor t : input.getNablaS())
            System.out.println(t);
        
        System.out.println(input.getF());
    }
}