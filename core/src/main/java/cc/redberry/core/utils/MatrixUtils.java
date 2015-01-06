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
package cc.redberry.core.utils;

import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.tensor.SimpleTensor;

import static cc.redberry.core.indices.IndicesUtils.getState;

/**
 * Utils fot matrices.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1.7
 */
public final class MatrixUtils {
    private MatrixUtils() {
    }

    /**
     * Returns whether specified tensor is matrix of specified type and with specified signature.
     *
     * @param tensor simple tensor
     * @param type   type of matrix
     * @param upper  number of upper matrix indices
     * @param lower  number of lower matrix indices
     * @return true if specified tensor is matrix of specified type and with specified signature and false in other case
     */
    public static boolean isGeneralizedMatrix(SimpleTensor tensor, IndexType type, int upper, int lower) {
        if (CC.isMetric(type))
            throw new IllegalArgumentException("Matrices can not be of metric type.");
        SimpleIndices indices = tensor.getIndices().getOfType(type);
        int i = 0;
        for (; i < upper; ++i)
            if (!getState(indices.get(i)))
                return false;
        upper += lower;
        for (; i < upper; ++i)
            if (getState(indices.get(i)))
                return false;
        return true;
    }

    /**
     * Returns whether specified tensor is matrix of specified type.
     *
     * @param tensor simple tensor
     * @param type   type of matrix
     * @return true if specified tensor is matrix of specified type and false in other case
     */
    public static boolean isMatrix(SimpleTensor tensor, IndexType type) {
        return isGeneralizedMatrix(tensor, type, 1, 1);
    }

    /**
     * Returns whether specified tensor is vector of specified type.
     *
     * @param tensor simple tensor
     * @param type   type of matrix
     * @return true if specified tensor is vector of specified type and false in other case
     */
    public static boolean isVector(SimpleTensor tensor, IndexType type) {
        return isGeneralizedMatrix(tensor, type, 1, 0);
    }

    /**
     * Returns whether specified tensor is covector of specified type.
     *
     * @param tensor simple tensor
     * @param type   type of matrix
     * @return true if specified tensor is covector of specified type and false in other case
     */
    public static boolean isCovector(SimpleTensor tensor, IndexType type) {
        return isGeneralizedMatrix(tensor, type, 0, 1);
    }
}
