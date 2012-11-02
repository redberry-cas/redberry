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
package cc.redberry.core.tensor;

import java.util.ArrayList;
import java.util.List;

public class DefaultBuilder implements TensorBuilder {

    private final TensorFactory factory;
    private final List<Tensor> list;

    public DefaultBuilder(TensorFactory factory) {
        this.factory = factory;
        this.list = new ArrayList<>();
    }

    public DefaultBuilder(TensorFactory factory, int capacity) {
        this.factory = factory;
        this.list = new ArrayList<>(capacity);
    }

    private DefaultBuilder(TensorFactory factory, List<Tensor> list) {
        this.factory = factory;
        this.list = list;
    }

    @Override
    public void put(Tensor tensor) {
        list.add(tensor);
    }

    @Override
    public Tensor build() {
        return factory.create(list.toArray(new Tensor[list.size()]));
    }

    @Override
    public TensorBuilder clone() {
        return new DefaultBuilder(factory, new ArrayList<>(list));
    }
}
