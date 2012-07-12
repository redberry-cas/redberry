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

import cc.redberry.concurrent.OutputPort;
import cc.redberry.core.tensor.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ExpandPairPort implements OutputPort<Tensor> {

    private final Tensor sum1, sum2;
    private final AtomicLong atomicLong = new AtomicLong();

    public ExpandPairPort(Sum s1, Sum s2) {
        sum1 = s1;
        sum2 = s2;
    }

    @Override
    public Tensor take() {
        long index = atomicLong.getAndIncrement();
        if (index >= sum1.size() * sum2.size())
            return null;
        int i1 = (int) (index / sum2.size());
        int i2 = (int) (index % sum2.size());
        return Tensors.multiply(sum1.get(i1), sum2.get(i2));
    }
}
