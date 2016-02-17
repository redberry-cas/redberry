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
package cc.redberry.core.tensor;

import cc.redberry.core.utils.ArrayIterator;

import java.util.Arrays;
import java.util.Iterator;

/**
 * This class is a container of information about graph structure of product.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class ProductContent implements Iterable<Tensor> {
    /**
     * Singleton for empty instance.
     */
    public static final ProductContent EMPTY_INSTANCE = new ProductContent();
    private final StructureOfContractions structureOfContractions;
    final Tensor[] data;
    final int[] hashCodes;
    final int[] iHashCodes;
    final Tensor nonScalar, scalars[];

    ProductContent(StructureOfContractions structureOfContractions,
                   Tensor[] data, int[] hashCodes, int[] iHashCodes,
                   Tensor nonScalar, Tensor[] scalars) {
        this.structureOfContractions = structureOfContractions;
        this.data = data;
        this.hashCodes = hashCodes;
        this.iHashCodes = iHashCodes;
        this.nonScalar = nonScalar;
        this.scalars = scalars;
    }

    private ProductContent() {
        this.structureOfContractions = StructureOfContractions.EMPTY_FULL_CONTRACTIONS_STRUCTURE;
        this.data = new Tensor[0];
        this.hashCodes = new int[0];
        this.iHashCodes = new int[0];
        this.nonScalar = null;
        this.scalars = new Tensor[0];
    }

    /**
     * Graph hash code
     *
     * @return hash code of underlying graph
     */
    int graphHash() {
        return Arrays.hashCode(hashCodes);
    }

    /**
     * Graph hash code with indices
     *
     * @return hash code of underlying graph
     */
    int iGraphHash() {
        return Arrays.hashCode(iHashCodes);
    }

    /**
     * Returns hash code of a specified vertex of graph ("clever" hash)
     *
     * @param i i-th vertex
     * @return hash code of a specified vertex
     */
    public int getVertexHash(int i) {
        return hashCodes[i];
    }

    public boolean compatibleWithGraph(ProductContent other) {
        return Arrays.equals(hashCodes, other.hashCodes);
    }

    public boolean iCompatibleWithGraph(ProductContent other) {
        return Arrays.equals(iHashCodes, other.iHashCodes);
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
     * Returns the first element of this content data
     *
     * @return first element of this content data
     */
    public Tensor first() {
        return data[0];
    }

    /**
     * Returns the last element of this content data
     *
     * @return last element of this content data
     */
    public Tensor last() {
        return data[data.length - 1];
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

    @Override
    public Iterator<Tensor> iterator() {
        return new ArrayIterator<>(data);
    }
}
