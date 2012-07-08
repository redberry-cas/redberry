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

import cc.redberry.core.context.ContextManager;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.SumBuilderConcurrent;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorBuilder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PairEC {

    private final ExpandPairOP expander;
    private final TensorBuilder collector;
    private final Future[] futures;

    public PairEC(Sum s1, Sum s2, int threads) {
        this.collector = new SumBuilderConcurrent();
        this.expander = new ExpandPairOP(s1, s2);
        this.futures = new Future[threads];
        for (int i = 0; i < threads; ++i)
            this.futures[i] = ContextManager.getExecutorService().submit(new Worker());
    }

    public Tensor result() throws InterruptedException {
        try {
            for (Future future : futures)
                future.get();
            return collector.build();
        } catch (ExecutionException ee) {
            throw new RuntimeException(ee);
        }
    }

    private class Worker implements Runnable {

        @Override
        public void run() {
            final ExpandPairOP expander = PairEC.this.expander;
            Tensor term;
            while ((term = expander.take()) != null)
                collector.put(term);
        }
    }
}
