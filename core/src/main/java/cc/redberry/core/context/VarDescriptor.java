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
package cc.redberry.core.context;


import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesSymmetries;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.indices.StructureOfIndices;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Descriptor of a pure symbol with indices (x_a, A_mn etc.)
 *
 * @author Stanislav Poslavsky
 */
public final class VarDescriptor {
    /**
     * Unique identifier
     */
    public final int id;
    /**
     * Base name of a symbol (e.g. for X_a -- "X)
     */
    final String baseName;
    /**
     * Name aliases (e.g. Pi and π)
     */
    final List<String> aliases = new ArrayList<>();
    /**
     * Structure of symbol indices
     */
    final StructureOfIndices varIndicesStructure;
    /**
     * Symmetries of base symbol indices
     */
    final IndicesSymmetries indicesSymmetries;
    /**
     * Computes indices of symbol acting as function
     */
    final VarIndicesProvider provider;
    /**
     * Unique cached instance for symbol without indices
     */
    private final SimpleTensor uniqueInstance;
    /**
     * Formatter for symbol name
     */
    NameFormatter nameFormatter = NameFormatter.DefaultName;

    VarDescriptor(int id, String baseName,
                  StructureOfIndices varIndicesStructure,
                  IndicesSymmetries indicesSymmetries,
                  VarIndicesProvider provider,
                  SimpleTensor uniqueInstance) {
        this.id = id;
        this.baseName = baseName;
        this.varIndicesStructure = varIndicesStructure;
        this.indicesSymmetries = indicesSymmetries;
        this.provider = provider;
        this.uniqueInstance = uniqueInstance;
    }


    /**
     * Returns unique symbol id
     *
     * @return unique symbol id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns unique instance for scalar symbol or null if this symbol has indices
     *
     * @return unique instance for scalar symbol or null if this symbol has indices
     */
    public SimpleTensor getUniqueInstance() {
        return uniqueInstance;
    }

    /**
     * Returns structure of symbol indices
     *
     * @return structure of symbol indices
     */
    public StructureOfIndices getStructureOfIndices() {
        return varIndicesStructure;
    }

    /**
     * Return unique symmetries (unique instance) of base symbol indices
     *
     * @return unique symmetries (unique instance) of base symbol indices
     */
    public IndicesSymmetries getSymmetries() {
        return indicesSymmetries;
    }

    /**
     * Computes indices of symbol acting as a function (e.g. f_ab[x_a, y_b])
     *
     * @param self      head indices (e.g. for function {@code f_ab[x_a, y_a] -- "_ab"})
     * @param arguments indices of arguments (e.g. for function {@code f_ab[x_a, y_a] -- {"_a", "_a"}})
     * @return resulting indices of function
     */
    public SimpleIndices computeIndices(SimpleIndices self, Indices... arguments) {
        return provider.compute(self, arguments);
    }

    /**
     * Computes indices of symbol acting as a function (e.g. f_ab[x_a, y_b]). Delegates actual computation
     * to {@link #computeIndices(SimpleIndices, Indices...)}.
     *
     * @param self      head indices (e.g. for function {@code f_ab[x_a, y_a] -- "_ab"})
     * @param arguments arguments (e.g. for function {@code f_ab[x_a, y_a] -- {"_a", "_a"}})
     * @return resulting indices of function
     */
    public SimpleIndices computeIndices(SimpleIndices self, final Tensor... arguments) {
        Indices[] indices = new Indices[arguments.length];
        for (int i = 0; i < arguments.length; i++)
            indices[i] = arguments[i].getIndices();
        return provider.compute(self, indices);
    }

    /**
     * Whether i-th argument propagates indices to outer environment. For example, Expand[x_a*(y_b + z_b)] propagates
     * indices as is, while for abstract f_ab[x_a] indices of argument are irrelevant
     *
     * @param i index of argument
     */
    public boolean propagatesIndices(int i) {return provider.propagatesIndices(i);}

    /**
     * Returns whether any of arguments should propagate its indices outside of the field scope (like e.g. Expand[x_a])
     *
     * @return whether any of arguments should propagate its indices outside of the field scope (like e.g. Expand[x_a])
     */
    public boolean propagatesIndices() {return provider.propagatesIndices();}

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

    /**
     * String representation of symbol name
     *
     * @param indices      self indices
     * @param outputFormat
     * @return String representation of symbol name
     */
    public String getName(SimpleIndices indices, OutputFormat outputFormat) {
        return nameFormatter.getVarName(baseName, aliases, indices, outputFormat);
    }

    public NameAndStructureOfIndices getKey() {
        return new NameAndStructureOfIndices(baseName, varIndicesStructure);
    }

    @Override
    public String toString() {
        return baseName + ":" + varIndicesStructure;
    }
}
