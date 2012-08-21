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
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import java.util.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @author Konstantin Kiselev
 */
final class SymbolsGenerator implements OutputPortUnsafe<Tensor> {

    private final String name;
    private int count = 0;
    final List<SimpleTensor> generatedSymbols;
    private final boolean rememberHistory;

    public SymbolsGenerator(String name) {
        this(name, false);
    }

    public SymbolsGenerator(String name, boolean rememberHistory) {
        this.name = name;
        this.rememberHistory = rememberHistory;
        if (rememberHistory)
            generatedSymbols = new ArrayList<>();
        else
            generatedSymbols = Collections.EMPTY_LIST;
    }

    @Override
    public Tensor take() {
        SimpleTensor t;
        if (name.isEmpty())
            return Complex.ONE;
        else
            t = Tensors.parseSimple(name + (count++));
        if (rememberHistory)
            generatedSymbols.add(t);
        return t;
    }
}
