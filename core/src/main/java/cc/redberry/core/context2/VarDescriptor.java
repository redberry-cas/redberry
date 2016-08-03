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
package cc.redberry.core.context2;


import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.indices.StructureOfIndices;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stanislav Poslavsky
 */
public final class VarDescriptor {
    public final int id;
    final String baseName;
    final List<String> aliases = new ArrayList<>();
    final StructureOfIndices varIndicesStructure;
    final VarIndicesProvider provider;
    final boolean isFunction;

    NameFormatter nameFormatter = NameFormatter.DefaultName;

    public VarDescriptor(int id, String baseName, StructureOfIndices varIndicesStructure, VarIndicesProvider provider, boolean isFunction) {
        this.id = id;
        this.baseName = baseName;
        this.varIndicesStructure = varIndicesStructure;
        this.provider = provider;
        this.isFunction = isFunction;
    }

    public SimpleIndices computeIndices(SimpleIndices self, Indices... arguments) {
        return provider.compute(self, arguments);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VarDescriptor that = (VarDescriptor) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public void setNameFormatter(NameFormatter nameFormatter) {
        this.nameFormatter = nameFormatter;
    }

    public NameFormatter getNameFormatter() {
        return nameFormatter;
    }

    public String getName(SimpleIndices indices, OutputFormat outputFormat) {
        return nameFormatter.getVarName(baseName, aliases, indices, outputFormat);
    }
}
