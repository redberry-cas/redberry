/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
 * This class is a container of information about graph structure of product.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class ProductContent {
    /**
     * Singleton for empty instance.
     */
    public static final ProductContent EMPTY_INSTANCE =
            new ProductContent(StructureOfContractionsHashed.EMPTY_INSTANCE,
                    StructureOfContractions.EMPTY_FULL_CONTRACTIONS_STRUCTURE,
                    new Tensor[0],
                    null,
                    new short[0],
                    new Tensor[0],
                    new int[0]);
    private final StructureOfContractionsHashed structureOfContractionsHashed;
    private final StructureOfContractions structureOfContractions;
    private final Tensor[] scalars;
    private final Tensor nonScalar;
    private final short[] stretchIndices;
    private final Tensor[] data;

    ProductContent(StructureOfContractionsHashed structureOfContractionsHashed,
                   StructureOfContractions structureOfContractions,
                   Tensor[] scalars, Tensor nonScalar,
                   short[] stretchIndices,
                   Tensor[] data) {
        this.structureOfContractionsHashed = structureOfContractionsHashed;
        this.structureOfContractions = structureOfContractions;
        this.scalars = scalars;
        this.nonScalar = nonScalar;
        this.stretchIndices = stretchIndices;
        this.data = data;
    }

    private ProductContent(StructureOfContractionsHashed structureOfContractionsHashed,
                           StructureOfContractions structureOfContractions,
                           Tensor[] scalars, Tensor nonScalar,
                           short[] stretchIndices,
                           Tensor[] data,
                           int[] stretchHashReflection) {
        this.structureOfContractionsHashed = structureOfContractionsHashed;
        this.structureOfContractions = structureOfContractions;
        this.scalars = scalars;
        this.nonScalar = nonScalar;
        this.stretchIndices = stretchIndices;
        this.data = data;
        this.stretchHashReflection = stretchHashReflection;
    }

    /**
     * Returns hashed structure of product contractions.
     *
     * @return hashed structure of product contractions
     */
    public StructureOfContractionsHashed getStructureOfContractionsHashed() {
        return structureOfContractionsHashed;
    }

    /**
     * Returns structure of product contractions.
     *
     * @return structure of product contractions
     */
    public StructureOfContractions getStructureOfContractions() {
        return structureOfContractions;
    }

    /**
     * Returns non-scalar connected component of product.
     *
     * @return non-scalar connected component of product
     */
    public Tensor getNonScalar() {
        return nonScalar;
    }

    /**
     * Return scalar subproducts of this product.
     *
     * @return an array of scalar subproducts of this product
     */
    public Tensor[] getScalars() {
        return scalars.clone();
    }

    /**
     * @return ids
     */
    public short[] getStretchIds() {
        return stretchIndices.clone();
    }

    /**
     * @param i position
     * @return id
     */
    public short getStretchId(int i) {
        return stretchIndices[i];
    }

    /**
     * Returns i-th element of indexed data in this product.
     *
     * @param i position
     * @return i-th element of indexed data in this product
     */
    public Tensor get(int i) {
        return data[i];
    }

    /**
     * Returns size of indexed data.
     *
     * @return size of indexed data
     */
    public int size() {
        return data.length;
    }

    /**
     * Returns a range of indexed data specified by {@code from} and {@code to}.
     *
     * @param from from position (inclusive)
     * @param to   to position (exclusive)
     * @return range
     */
    public Tensor[] getRange(int from, int to) {
        return Arrays.copyOfRange(data, from, to);
    }

    /**
     * Copy of indexed data array
     *
     * @return copy of indexed data array
     */
    public Tensor[] getDataCopy() {
        return data.clone();
    }

    private int[] stretchHashReflection;

    /**
     * @param hashCode hashCode
     * @return id
     */
    public short getStretchIndexByHash(final int hashCode) {
        if (stretchHashReflection == null) {
            stretchHashReflection = new int[stretchIndices[stretchIndices.length - 1] + 1];
            //TODO performance (!!!)
            for (int i = 0; i < stretchIndices.length; ++i)
                stretchHashReflection[stretchIndices[i]] = data[i].hashCode();
        }
        int index = Arrays.binarySearch(stretchHashReflection, hashCode);
        if (index < 0)
            return -1;
        return (short) index;
    }
}
