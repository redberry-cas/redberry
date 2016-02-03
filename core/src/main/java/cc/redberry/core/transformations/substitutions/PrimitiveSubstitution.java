/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.context.ToString;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.tensor.ApplyIndexMapping;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.iterator.TIntIterator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
abstract class PrimitiveSubstitution implements ToString {
    final Tensor from, to;
    final boolean toIsSymbolic;
    //if positive, then adds dummies
    final boolean possiblyAddsDummies;

    PrimitiveSubstitution(Tensor from, Tensor to) {
        this.from = ApplyIndexMapping.optimizeDummies(from);
        this.to = ApplyIndexMapping.optimizeDummies(to);

        int[] typesCounts = new int[IndexType.TYPES_COUNT];

        TIntIterator iterator = TensorUtils.getAllDummyIndicesIncludingScalarFunctionsT(to).iterator();
        while (iterator.hasNext())
            ++typesCounts[IndicesUtils.getType(iterator.next())];

        iterator = TensorUtils.getAllDummyIndicesT(from).iterator();
        while (iterator.hasNext())
            --typesCounts[IndicesUtils.getType(iterator.next())];

        boolean possiblyAddsDummies = false;
        for (int i : typesCounts)
            if (i > 0) {
                possiblyAddsDummies = true;
                break;
            }
        this.possiblyAddsDummies = possiblyAddsDummies;
        this.toIsSymbolic = TensorUtils.isSymbolic(to);
    }

    Tensor newTo(Tensor current, SubstitutionIterator iterator) {
        if (current.getClass() != from.getClass())
            return current;
        return newTo_(current, iterator);
    }


    Tensor applyIndexMappingToTo(Tensor oldFrom, Tensor to, Mapping mapping, SubstitutionIterator iterator) {
        if (toIsSymbolic)
            return mapping.getSign() ? Tensors.negate(to) : to;
        if (possiblyAddsDummies)
            return ApplyIndexMapping.applyIndexMapping(to, mapping, iterator.getForbidden());
        return ApplyIndexMapping.applyIndexMappingAndRenameAllDummies(to, mapping, TensorUtils.getAllDummyIndicesT(oldFrom).toArray());
    }

    Tensor applyIndexMappingToTo(int[] oldDummies, Tensor to, Mapping mapping, SubstitutionIterator iterator) {
        if (toIsSymbolic)
            return mapping.getSign() ? Tensors.negate(to) : to;
        if (possiblyAddsDummies)
            return ApplyIndexMapping.applyIndexMapping(to, mapping, iterator.getForbidden());
        return ApplyIndexMapping.applyIndexMappingAndRenameAllDummies(to, mapping, oldDummies);
    }

    abstract Tensor newTo_(Tensor currentNode, SubstitutionIterator iterator);

    @Override
    public String toString(OutputFormat outputFormat) {
        String symb;
        if (outputFormat.is(OutputFormat.WolframMathematica))
            symb = "->";
        else symb = "=";
        return from.toString(outputFormat) + symb + to.toString(outputFormat);
    }

    @Override
    public String toString() {
        return toString(CC.getDefaultOutputFormat());
    }
}
