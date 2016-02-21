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
package cc.redberry.core.utils;

import cc.redberry.core.context.CC;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;
import org.junit.Test;

/**
 * Created by poslavsky on 19/02/16.
 */
public class TimingStatisticsTest {
    @Test
    public void test1() throws Exception {
        class dummyTr implements Transformation {
            @Override
            public Tensor transform(Tensor t) {
                try {
                    Thread.sleep((long) CC.getRandomGenerator().nextInt(3));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return t;
            }
        }
        TransformationWithTimer tr1 = new TransformationWithTimer(new dummyTr(), "tr_1");
        TransformationWithTimer tr2 = new TransformationWithTimer(new dummyTr(), "tr__2");
        TransformationWithTimer tr3 = new TransformationWithTimer(new dummyTr(), "tr___3");
        TransformationWithTimer tr4 = new TransformationWithTimer(new dummyTr(), "tr____4");

        final TimingStatistics stats = new TimingStatistics();
        final TimingStatistics stats4 = new TimingStatistics();
        stats.track(tr1, tr2, tr3);
        stats4.track(tr4);
        for (int i = 0; i < 10; i++) {
            tr1.transform(null);
            tr2.transform(null);
            tr3.transform(null);
            tr4.transform(null);
        }

        System.out.println(stats);
        System.out.println();
        System.out.println(stats.toStringMicros());
        System.out.println();
        System.out.println(stats4.toStringNanos());
        System.out.println();
        stats.merge(stats);
        System.out.println(stats);
        System.out.println();
        stats.merge(stats4);
        System.out.println(stats);
        System.out.println();
    }
}