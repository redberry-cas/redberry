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

package cc.redberry.core.performance;

import cc.redberry.core.context.CC;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class Main {

    public static void main(String[] args) {
        CC.resetTensorNames(121313L);
        List<Tensor> l = new ArrayList<>();
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                while (true);
//            }
//        }).start();
        for (int i = 0; i < Integer.MAX_VALUE; ++i) {
            System.out.println(i);
            Tensor t = Tensors.parse("(a+b)*(a*f_m+b*g_m)*(b*f^m+a*g^m)");
            Tensor y = Tensors.parse("(Power[a, 2]*b+a*Power[b, 2])*g_{m}*g^{m}+(Power[a, 3]+Power[a, 2]*b+a*Power[b, 2]+Power[b, 3])*f^{m}*g_{m}+(Power[a, 2]*b+a*Power[b, 2])*f_{m}*f^{m}");
            y = Tensors.parse("(Power[a, 2]*b+a*Power[b, 2])*g_{m}*g^{m}+(Power[a, 3]+Power[a, 2]*b+a*Power[b, 2]+Power[b, 3])*f^{m}*g_{m}+(Power[a, 2]*b+a*Power[b, 2])*f_{m}*f^{m}");
            y = Tensors.parse("(Power[a, 2]*b+a*Power[b, 2])*g_{m}*g^{m}+(Power[a, 3]+Power[a, 2]*b+a*Power[b, 2]+Power[b, 3])*f^{m}*g_{m}+(Power[a, 2]*b+a*Power[b, 2])*f_{m}*f^{m}");
            l.add(y);
            l.add(y);
            y = Tensors.parse("(Power[a, 2]*b+a*Power[b, 2])*g_{m}*g^{m}+(Power[a, 3]+Power[a, 2]*b+a*Power[b, 2]+Power[b, 3])*f^{m}*g_{m}+(Power[a, 2]*b+a*Power[b, 2])*f_{m}*f^{m}");
            l.add(t);
        }
    }
}
