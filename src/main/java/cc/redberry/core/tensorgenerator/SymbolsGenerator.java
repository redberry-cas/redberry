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

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.iterator.TensorLastIterator;
import cc.redberry.core.utils.TensorUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SymbolsGenerator implements OutputPortUnsafe<Tensor> {

    private final String name;
    private int count = 0;
    private final String[] usedNames;

    public SymbolsGenerator(String name, Tensor... forbiddenTensors) {
        checkName(name);
        this.name = name;

        Set<String> set = new HashSet<>();
        TensorLastIterator iterator;
        for (Tensor f : forbiddenTensors) {
            iterator = new TensorLastIterator(f);
            Tensor c;
            while ((c = iterator.next()) != null)
                if (TensorUtils.isSymbol(c))
                    set.add(((SimpleTensor) c).toString());
        }
        this.usedNames = new String[set.size()];
        int i = -1;
        for (String str : set)
            usedNames[++i] = str;
        Arrays.sort(usedNames);
        i = 0;

    }

    public SymbolsGenerator(String name) {
        checkName(name);
        this.name = name;
        this.usedNames = new String[0];
    }

    private static void checkName(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Empty string is illegal.");
    }

    @Override
    public SimpleTensor take() {
        String newName;
        do
            newName = name + (count++);
        while (Arrays.binarySearch(usedNames, newName) >= 0);
        return Tensors.parseSimple(newName);
    }
}
