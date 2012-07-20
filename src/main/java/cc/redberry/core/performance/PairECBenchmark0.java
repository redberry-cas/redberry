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

import cc.redberry.core.transformations.Expand;
import cc.redberry.core.context.CC;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.random.TRandom;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class PairECBenchmark0 {

    public static void main(String[] args) {
        go(1); //JVM cold start
        go(10);
    }

    public static void go(int trys) {
        try {
            //Threads from 1 to maxThread inclusively
            int maxThread = 2;

            //Initialization of outer statistic agregators
            DescriptiveStatistics[] statsMean = new DescriptiveStatistics[maxThread];
            DescriptiveStatistics[] statsSTD = new DescriptiveStatistics[maxThread];
            for (int threads = 1; threads <= maxThread; ++threads) {
                statsMean[threads - 1] = new DescriptiveStatistics();
                statsSTD[threads - 1] = new DescriptiveStatistics();
            }

            for (; trys-- > 0;) { //Each try == one random sum
                DescriptiveStatistics[] stats = new DescriptiveStatistics[maxThread];
                Sum[] sums = random2Sums();
                System.out.println("try " + trys);
                for (int threads = 1; threads <= maxThread; ++threads) {
                    DescriptiveStatistics ds = new DescriptiveStatistics();

                    for (int i = 0; i < 10; ++i) { //Each random sum repeatedly tested 1000 times
                        long start = System.nanoTime();

                        Tensor res = Expand.expandPairOfSumsConcurrent(sums[0], sums[1], threads);
//                        System.out.println(res.size() + " " + res);
                        ds.addValue(System.nanoTime() - start);
                    }

                    stats[threads - 1] = ds; //Saving statistics for one thread
                }

                double norm = stats[0].getMean(); //Normalization to mean execution time in one thread

                for (int threads = 1; threads <= maxThread; ++threads) {
                    statsMean[threads - 1].addValue(stats[threads - 1].getMean() / norm);
                    statsSTD[threads - 1].addValue(stats[threads - 1].getStandardDeviation() / norm);
                }
            }

            //Outputting mean values of relative mean execution time and it's mean STD
            for (int threads = 1; threads <= maxThread; ++threads)
                System.out.println(threads + ": " + statsMean[threads - 1].getMean() + " +- " + statsSTD[threads - 1].getMean());

            System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        } finally {
        }
    }

    public static Sum[] random2Sums() { //Random sums pair with renamed indices
        Sum[] sums = random2Sums_();
        Tensor p = Tensors.multiplyAndRenameConflictingDummies(sums);
        return new Sum[]{(Sum) p.get(0), (Sum) p.get(1)};
    }
    public static TRandom random;

    static {
        CC.resetTensorNames(2312);
        random = new TRandom(1, 2, new int[]{1, 0, 0, 0}, new int[]{2, 0, 0, 0}, true, 7643543L);
    }

    public static Sum[] random2Sums_() { //Здесь могла бы быть ваша реклама
        return new Sum[]{
                    (Sum) random.nextSum(20, 5, ParserIndices.parseSimple("_mn")),
                    (Sum) random.nextSum(20, 5, ParserIndices.parseSimple("^mn"))
                };
    }
}
