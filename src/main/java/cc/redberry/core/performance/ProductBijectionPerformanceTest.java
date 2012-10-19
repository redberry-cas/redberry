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
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.random.TRandom;
import cc.redberry.core.transformations.substitutions.ProductsBijectionsPort;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author stas
 */
public class ProductBijectionPerformanceTest {

    public static void main(String[] args) {
        int badCounter = 0;
//        CC.resetTensorNames(-3912578993076521674L);
        TRandom rp = new TRandom(
                4,
                10,
                new int[]{4, 0, 0, 0},
                new int[]{10, 0, 0, 0},
                false);
        rp.reset(-3806751651286565680L);
        System.out.println("Random Seed = " + rp.getSeed());
        System.out.println("NM Seed = " + CC.getNameManager().getSeed());
        DescriptiveStatistics timeStats = new DescriptiveStatistics();
        DescriptiveStatistics trysStats = new DescriptiveStatistics();
        int count = 0;
        while (++count < 500) {
//            CC.resetTensorNames();
            Tensor t = rp.nextProduct(15);
            if (!(t instanceof Product))
                continue;

            Product from = (Product) t;

            long start = System.nanoTime();
            ProductsBijectionsPort port = new ProductsBijectionsPort(from.getContent(), from.getContent());

            int[] bijection;
            boolean good = false;
            int trys = 0;
            OUTER:
            while (trys++ < 5000 && (bijection = port.take()) != null) {
                for (int i = 0; i < bijection.length; ++i)
                    if (bijection[i] != i)
                        continue OUTER;
                good = true;
                break;
            }

            double millis = 1E-6 * (System.nanoTime() - start);
            timeStats.addValue(millis);
            trysStats.addValue(trys);

            if (!good)
                throw new RuntimeException();
        }
        System.out.println(timeStats);
        System.out.println(trysStats);
    }
}
