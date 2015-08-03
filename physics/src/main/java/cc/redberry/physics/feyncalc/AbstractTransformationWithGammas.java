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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.NameAndStructureOfIndices;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.parser.ParseTokenTransformer;
import cc.redberry.core.parser.preprocessor.ChangeIndicesTypesAndTensorNames;
import cc.redberry.core.parser.preprocessor.TypesAndNamesTransformer;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;

import static cc.redberry.core.indices.IndicesUtils.*;
import static cc.redberry.core.indices.IndicesUtils.getType;
import static cc.redberry.core.tensor.Tensors.simpleTensor;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class AbstractTransformationWithGammas implements Transformation {
    /*
    * Defaults
    */
    protected static final String gammaMatrixStringName = "G", gamma5StringName = "G5",
            leviCivitaStringName = "eps";
    protected final int gammaName, gamma5Name;
    protected final IndexType metricType, matrixType;
    protected final Expression deltaTrace, traceOfOne;
    protected final ParseTokenTransformer tokenTransformer;

    public AbstractTransformationWithGammas(final SimpleTensor gammaMatrix,
                                            final Tensor dimension,
                                            final Tensor traceOfOne) {
        checkNotation(gammaMatrix);
        this.gammaName = gammaMatrix.getName();
        //no gamma5 should be in tensors
        this.gamma5Name = Integer.MIN_VALUE;
        final IndexType[] types = TraceUtils.extractTypesFromMatrix(gammaMatrix);
        this.metricType = types[0];
        this.matrixType = types[1];

        tokenTransformer = new ChangeIndicesTypesAndTensorNames(new TypesAndNamesTransformer() {
            @Override
            public int newIndex(int oldIndex, NameAndStructureOfIndices oldDescriptor) {
                return oldIndex;
            }

            @Override
            public IndexType newType(IndexType oldType, NameAndStructureOfIndices oldDescriptor) {
                switch (oldType) {
                    case LatinLower:
                        return metricType;
                    case Matrix1:
                        return matrixType;
                }
                return oldType;
            }

            @Override
            public String newName(NameAndStructureOfIndices oldDescriptor) {
                switch (oldDescriptor.getName()) {
                    case gamma5StringName:
                        throw new IllegalArgumentException("Gamma5 is not specified.");
                    case leviCivitaStringName:
                        throw new IllegalArgumentException("Levi-Civita is not specified.");
                    default:
                        return oldDescriptor.getName();
                }
            }
        });
        this.traceOfOne = (Expression) tokenTransformer.transform(CC.current().getParseManager().getParser().parse("d^a'_a'=" + traceOfOne)).toTensor();
        this.deltaTrace = (Expression) tokenTransformer.transform(CC.current().getParseManager().getParser().parse("d^a_a=" + dimension)).toTensor();
    }

    public AbstractTransformationWithGammas(final SimpleTensor gammaMatrix,
                                            final SimpleTensor gamma5,
                                            final SimpleTensor leviCivita,
                                            final Tensor dimension,
                                            final Tensor traceOfOne) {
        checkNotation(gammaMatrix, gamma5, leviCivita);
        this.gammaName = gammaMatrix.getName();
        this.gamma5Name = gamma5.getName();
        final IndexType[] types = TraceUtils.extractTypesFromMatrix(gammaMatrix);
        this.metricType = types[0];
        this.matrixType = types[1];


        tokenTransformer = new ChangeIndicesTypesAndTensorNames(new TypesAndNamesTransformer() {
            @Override
            public int newIndex(int oldIndex, NameAndStructureOfIndices oldDescriptor) {
                return oldIndex;
            }

            @Override
            public IndexType newType(IndexType oldType, NameAndStructureOfIndices oldDescriptor) {
                switch (oldType) {
                    case LatinLower:
                        return metricType;
                    case Matrix1:
                        return matrixType;
                }
                return oldType;
            }

            @Override
            public String newName(NameAndStructureOfIndices oldDescriptor) {
                switch (oldDescriptor.getName()) {
                    case gammaMatrixStringName:
                        return gammaMatrix.getStringName();
                    case gamma5StringName:
                        return gamma5.getStringName();
                    case leviCivitaStringName:
                        return leviCivita.getStringName();
                    default:
                        return oldDescriptor.getName();
                }
            }
        });

        this.traceOfOne = (Expression) tokenTransformer.transform(CC.current().getParseManager().getParser().parse("d^a'_a'=" + traceOfOne)).toTensor();
        this.deltaTrace = (Expression) tokenTransformer.transform(CC.current().getParseManager().getParser().parse("d^a_a=" + dimension)).toTensor();
    }

    protected final boolean isGammaOrGamma5(Tensor tensor) {
        int h = tensor.hashCode();
        return (h == gammaName || h == gamma5Name) && tensor.getClass().equals(SimpleTensor.class);
    }

    protected final boolean isGamma(Tensor tensor) {
        int h = tensor.hashCode();
        return (h == gammaName) && tensor.getClass().equals(SimpleTensor.class);
    }

    protected final boolean isGamma5(Tensor tensor) {
        int h = tensor.hashCode();
        return (h == gamma5Name) && tensor.getClass().equals(SimpleTensor.class);
    }

    protected final boolean containsGammaMatrices(Tensor t) {
        if (t.getClass().equals(SimpleTensor.class))
            return t.hashCode() == gammaName || t.hashCode() == gamma5Name;
        else for (Tensor p : t)
            if (containsGammaMatrices(p))
                return true;
        return false;
    }

    protected static void checkNotation(SimpleTensor gammaMatrix) {
        final IndexType[] types = TraceUtils.extractTypesFromMatrix(gammaMatrix);
        IndexType metricType = types[0];
        IndexType matrixType = types[1];
        if (gammaMatrix.getIndices().size() != 3
                || gammaMatrix.getIndices().size(metricType) != 1
                || gammaMatrix.getIndices().size(matrixType) != 2)
            throw new IllegalArgumentException("Not a gamma: " + gammaMatrix);
    }

    protected static void checkNotation(SimpleTensor gammaMatrix,
                                        SimpleTensor gamma5Matrix,
                                        SimpleTensor leviCivita) {
        final IndexType[] types = TraceUtils.extractTypesFromMatrix(gammaMatrix);
        IndexType metricType = types[0];
        IndexType matrixType = types[1];

        if (gammaMatrix.getIndices().size() != 3
                || gammaMatrix.getIndices().size(metricType) != 1
                || gammaMatrix.getIndices().size(matrixType) != 2)
            throw new IllegalArgumentException("Not a gamma: " + gammaMatrix);

        if (gamma5Matrix.getIndices().size() != 2
                || gamma5Matrix.getIndices().size(matrixType) != 2)
            throw new IllegalArgumentException("Not a gamma5: " + gamma5Matrix);

        if (leviCivita.getIndices().size() != 4
                || leviCivita.getIndices().size(metricType) != 4)
            throw new IllegalArgumentException("Not a Levi-Civita: " + leviCivita);
    }

    protected static SimpleTensor setMatrixIndices(SimpleTensor gamma, int matrixUpper, int matrixLower) {
        int[] indices = gamma.getIndices().getAllIndices().copy();
        for (int i = indices.length - 1; i >= 0; --i)
            if (!CC.isMetric(getType(indices[i]))) {
                indices[i] = getState(indices[i]) ?
                        createIndex(matrixUpper, getType(indices[i]), getState(indices[i]))
                        : createIndex(matrixLower, getType(indices[i]), getState(indices[i]));
            }
        return simpleTensor(gamma.getName(), IndicesFactory.createSimple(null, indices));
    }
}
