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

import java.util.Arrays;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ProductContent {

    public static final ProductContent EMPTY_INSTANCE =
            new ProductContent(ContractionStructure.EMPTY_INSTANCE,
                               FullContractionsStructure.EMPTY_FULL_CONTRACTIONS_STRUCTURE,
                               new Tensor[0],
                               null,
                               new short[0],
                               new Tensor[0]);
    private final ContractionStructure contractionStructure;
    private final FullContractionsStructure fullContractionsStructure;
    private final Tensor[] scalars;
    private final Tensor nonScalar;
    private final short[] stretchIndices;
    private final Tensor[] data;

    public ProductContent(ContractionStructure contractionStructure,
                          FullContractionsStructure fullContractionsStructure,
                          Tensor[] scalars, Tensor nonScalar,
                          short[] stretchIndices,
                          Tensor[] data) {
        this.contractionStructure = contractionStructure;
        this.fullContractionsStructure = fullContractionsStructure;
        this.scalars = scalars;
        this.nonScalar = nonScalar;
        this.stretchIndices = stretchIndices;
        this.data = data;
    }

    public ContractionStructure getContractionStructure() {
        return contractionStructure;
    }

    public FullContractionsStructure getFullContractionsStructure() {
        return fullContractionsStructure;
    }

    public Tensor getNonScalar() {
        return nonScalar;
    }

    public Tensor[] getScalars() {
        return scalars.clone();
    }

    public short[] getStretchIds() {
        return stretchIndices.clone();
    }

    public short getStretchId(int i) {
        return stretchIndices[i];
    }

    public Tensor get(int i) {
        return data[i];
    }

    public int size() {
        return data.length;
    }

    public Tensor[] getRange(int from, int to) {
        return Arrays.copyOfRange(data, from, to);
    }

    public Tensor[] getDataCopy() {
        return data.clone();
    }
}