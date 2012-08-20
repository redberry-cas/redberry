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
package cc.redberry.core.tensorgenerator;

import cc.redberry.concurrent.*;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import java.util.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @author Konstantin Kiselev
 */
public final class ScalarTensorGenerator implements OutputPortUnsafe<Tensor> {

    private final String name;
    private int count = 0;
    final List<Tensor> generatedTensors;
    private final boolean rememberHistory;

    public ScalarTensorGenerator(String name) {
        this(name, false);
    }

    public ScalarTensorGenerator(String name, boolean rememberHistory) {
        this.name = name;
        this.rememberHistory = rememberHistory;
        if (rememberHistory)
            generatedTensors = new ArrayList<>();
        else
            generatedTensors = Collections.EMPTY_LIST;
    }

    @Override
    public Tensor take() {
        Tensor t;
        if (name.isEmpty())
            t = Complex.ONE;
        else
            t = Tensors.parse(name + (count++));
        if (rememberHistory)
            generatedTensors.add(t);
        return t;
    }
}
