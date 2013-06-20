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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.NameDescriptor;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.StructureOfIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.TensorUtils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class TraceUtils {
    static final IndexType[] extractTypesFromMatrix(SimpleTensor matrix) {
        if (matrix.getIndices().size() != 3)
            throw new IllegalArgumentException("Not a matrix: " + matrix + ".");
        NameDescriptor descriptor = CC.getNameDescriptor(matrix.getName());
        StructureOfIndices typeStructure = descriptor.getStructureOfIndices();
        byte metricType = -1, matrixType = -1;
        int typeCount;
        for (byte type = 0; type < IndexType.TYPES_COUNT; ++type) {
            typeCount = typeStructure.typeCount(type);
            if (typeCount == 0)
                continue;
            else if (typeCount == 2) {
                if (matrixType != -1)
                    throw new IllegalArgumentException("Not a matrix: " + matrix + ".");
                matrixType = type;
                if (CC.isMetric(matrixType))
                    throw new IllegalArgumentException("Not a matrix: " + matrix + ".");
            } else if (typeCount == 1) {
                if (metricType != -1)
                    throw new IllegalArgumentException("Not a matrix: " + matrix + ".");
                metricType = type;
                if (!CC.isMetric(metricType))
                    throw new IllegalArgumentException("Not a matrix: " + matrix + ".");
            } else
                throw new IllegalArgumentException("Not a matrix: " + matrix + ".");
        }
        return new IndexType[]{IndexType.getType(metricType), IndexType.getType(matrixType)};
    }

    static void checkUnitaryInput(final SimpleTensor unitaryMatrix,
                                  final SimpleTensor structureConstant,
                                  final SimpleTensor symmetricConstant,
                                  final Tensor dimension) {

        if (dimension instanceof Complex && !TensorUtils.isNaturalNumber(dimension))
            throw new IllegalArgumentException("Non natural dimension.");

        if (unitaryMatrix.getIndices().size() != 3)
            throw new IllegalArgumentException("Not a unitary matrix: " + unitaryMatrix);
        IndexType[] types = TraceUtils.extractTypesFromMatrix(unitaryMatrix);
        IndexType metricType = types[0];
        if (!TensorUtils.isScalar(dimension))
            throw new IllegalArgumentException("Non scalar dimension.");
        if (structureConstant.getName() == symmetricConstant.getName())
            throw new IllegalArgumentException("Structure and symmetric constants have same names.");
        SimpleTensor[] ss = {structureConstant, symmetricConstant};
        for (SimpleTensor st : ss) {
            if (st.getIndices().size() != 3)
                throw new IllegalArgumentException("Illegal input for SU(N) constants: " + st);
            for (int i = 0; i < 3; ++i)
                if (IndicesUtils.getTypeEnum(st.getIndices().get(i)) != metricType)
                    throw new IllegalArgumentException("Different indices metric types: " + unitaryMatrix + " and " + st);
        }
    }

    /*
    * Default unitary notations
    */
    static final String unitaryMatrixName = "T";
    static final String structureConstantName = "F";
    static final String symmetricConstantName = "D";
    static final String dimensionName = "N";
}
