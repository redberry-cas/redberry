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

import cc.redberry.core.context.*;
import cc.redberry.core.indexmapping.*;
import cc.redberry.core.tensor.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IndexMappingPerformance {

    public static void main(String[] args) {
        while (true)
            System.out.println(t());
    }

    private static long t() {
        String e0 = "g_ax*(d_c*G^x_bd-d_d*G^x_bc+G^x_yc*G^y_bd-G^x_yd*G^y_bc)";
        String e1 = "g_px*(d_r*G^x_qs-d_s*G^x_qr+G^x_yr*G^y_qs-G^x_ys*G^y_qr)";
        e1 = e1.replace('_', '#').replace('^', '_').replace('#', '^');


        long time = 0;

        for (int k = 0; k < 500; ++k) {
            ContextManager.initializeNew();
            Tensor riman1 = Tensors.parse(e0);
            Tensor riman2 = Tensors.parse(e1);

            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000; ++i) {
                MappingsPort mp = IndexMappings.createPort(riman1, riman2);
                IndexMappingBuffer buffera;
                while ((buffera = mp.take()) != null);
            }
            time += System.currentTimeMillis() - start;
        }
        return time;
    }
}
