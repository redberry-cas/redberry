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
package cc.redberry.core.tensor.playground;

import cc.redberry.core.tensor.Tensor;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class IAlgorithm {
    long timing;
    final String name;

    public IAlgorithm(String name) {
        this.name = name;
    }


    long timingMillis() {
        return timing / 1_000_000;
    }

    ProductData calc(Tensor t) {
        long start = System.nanoTime();
        ProductData pd = calc0(t);
        timing += System.nanoTime() - start;
        return pd;
    }

    void restart() {
        timing = 0;
    }

    abstract ProductData calc0(Tensor t);
}
