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
package cc.redberry.core.transformations.expand;

import cc.redberry.core.context.*;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.SumBuilder;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorBuilder;
import java.util.concurrent.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpandUtils {

    public static Tensor expandPairOfSums(Sum s1, Sum s2) {
        ExpandPairPort epp = new ExpandPairPort(s1, s2);
        TensorBuilder sum = new SumBuilder();
        Tensor t;
        while ((t = epp.take()) != null)
            sum.put(t);
        return sum.build();
    }

    public static Tensor expandPairOfSumsConcurrent(Sum s1, Sum s2, int threads) throws InterruptedException {
        if (threads == 1)
            return expandPairOfSums(s1, s2);
        Future[] futures = new Future[threads];
        ExpandPairPort epp = new ExpandPairPort(s1, s2);
        TensorBuilder sum = new SumBuilder();

        for (int i = 0; i < threads; ++i)
            futures[i] = ContextManager.getExecutorService().submit(new Worker(epp, sum));

        try {
            for (Future future : futures)
                future.get();
            return sum.build();
        } catch (ExecutionException ee) {
            throw new RuntimeException(ee);
        }
    }

    private static class Worker implements Runnable {

        private final ExpandPairPort epp;
        private final TensorBuilder builder;

        public Worker(ExpandPairPort epp, TensorBuilder builder) {
            this.epp = epp;
            this.builder = builder;
        }

        @Override
        public void run() {
            Tensor term;
            while ((term = epp.take()) != null)
                builder.put(term);
        }
    }
}
