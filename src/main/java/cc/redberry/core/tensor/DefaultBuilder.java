/*
 * org.redberry.concurrent: high-level Java concurrent library.
 * Copyright (c) 2010-2012.
 * Bolotin Dmitriy <bolotin.dmitriy@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 */

package cc.redberry.core.tensor;

import java.util.ArrayList;
import java.util.List;

public class DefaultBuilder implements TensorBuilder {
    private final TensorFactory factory;
    private final List<Tensor> list;

    public DefaultBuilder(TensorFactory factory, int capacity) {
        this.factory = factory;
        this.list = new ArrayList<>(capacity);
    }

    @Override
    public void put(Tensor tensor) {
        list.add(tensor);
    }

    @Override
    public Tensor build() {
        return factory.create(list.toArray(new Tensor[list.size()]));
    }
}
