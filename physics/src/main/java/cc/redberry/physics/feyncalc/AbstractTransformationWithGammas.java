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
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.parser.ParseTokenTransformer;
import cc.redberry.core.parser.preprocessor.ChangeIndicesTypesAndTensorNames;
import cc.redberry.core.parser.preprocessor.TypesAndNamesTransformer;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationToStringAble;
import cc.redberry.core.transformations.substitutions.SubstitutionTransformation;
import cc.redberry.core.utils.ArraysUtils;

import java.util.List;

import static cc.redberry.core.indices.IndicesFactory.createSimple;
import static cc.redberry.core.indices.IndicesUtils.*;
import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.simpleTensor;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class AbstractTransformationWithGammas implements TransformationToStringAble {
    /* Defaults */
    protected static final String gammaMatrixStringName = "G",
            gamma5StringName = "G5",
            leviCivitaStringName = "eps";
    protected final int gammaName, gamma5Name;
    protected final IndexType metricType, matrixType;
    protected final Expression deltaTrace, traceOfOne;
    protected final SubstitutionTransformation deltaTraces;
    protected final ParseTokenTransformer tokenTransformer;
    protected final Transformation expandAndEliminate;

    protected AbstractTransformationWithGammas(DiracOptions options) {
        checkNotation(options.gammaMatrix, options.gamma5, options.leviCivita);
        if (!options.created)
            options.triggerCreate();
        this.gammaName = options.gammaMatrix.getName();
        this.gamma5Name = options.gamma5 == null ? Integer.MIN_VALUE : options.gamma5.getName();
        final IndexType[] types = TraceUtils.extractTypesFromMatrix(options.gammaMatrix);
        this.metricType = types[0];
        this.matrixType = types[1];
        this.tokenTransformer = createTokenTransformer(metricType, matrixType, options.gammaMatrix, options.gamma5, options.leviCivita);
        this.expandAndEliminate = options.expandAndEliminate;
        this.traceOfOne = (Expression) tokenTransformer.transform(CC.current().getParseManager().getParser().parse("d^a'_a'=" + options.traceOfOne)).toTensor();
        this.deltaTrace = (Expression) tokenTransformer.transform(CC.current().getParseManager().getParser().parse("d^a_a=" + options.dimension)).toTensor();
        this.deltaTraces = new SubstitutionTransformation(new Expression[]{this.traceOfOne, this.deltaTrace}, true);
    }

    @Override
    public String toString() {
        return this.toString(CC.getDefaultOutputFormat());
    }

    protected final boolean containsGammaOr5Matrices(final Tensor t) {
        if (t.getClass().equals(SimpleTensor.class))
            return t.hashCode() == gammaName || t.hashCode() == gamma5Name;
        else for (Tensor p : t)
            if (containsGammaOr5Matrices(p))
                return true;
        return false;
    }

    private static ChangeIndicesTypesAndTensorNames createTokenTransformer(
            final IndexType metricType, final IndexType matrixType, final SimpleTensor gammaMatrix,
            final SimpleTensor gamma5, final SimpleTensor leviCivita) {
        return new ChangeIndicesTypesAndTensorNames(new TypesAndNamesTransformer() {
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
            public String newName(String oldName, NameAndStructureOfIndices oldDescriptor) {
                switch (oldName) {
                    case gammaMatrixStringName:
                        return gammaMatrix.getStringName();
                    case gamma5StringName:
                        if (gamma5 == null)
                            throw new IllegalArgumentException("Gamma5 is not specified.");
                        return gamma5.getStringName();
                    case leviCivitaStringName:
                        if (leviCivita == null)
                            throw new IllegalArgumentException("Levi-Civita is not specified.");
                        return leviCivita.getStringName();
                    default:
                        return oldDescriptor.getName();
                }
            }
        });
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

        if (gamma5Matrix != null && (gamma5Matrix.getIndices().size() != 2
                || gamma5Matrix.getIndices().size(matrixType) != 2))
            throw new IllegalArgumentException("Not a gamma5: " + gamma5Matrix);

        if (leviCivita != null && (leviCivita.getIndices().size() != 4
                || leviCivita.getIndices().size(metricType) != 4))
            throw new IllegalArgumentException("Not a Levi-Civita: " + leviCivita);
    }


    protected final boolean containsGammaMatrices(final Tensor t) {
        if (t.getClass().equals(SimpleTensor.class))
            return t.hashCode() == gammaName;
        else for (Tensor p : t)
            if (containsGammaMatrices(p))
                return true;
        return false;
    }

    protected final boolean isGammaOrGamma5(final Tensor tensor) {
        int h = tensor.hashCode();
        return (h == gammaName || h == gamma5Name) && tensor.getClass().equals(SimpleTensor.class);
    }

    protected final boolean isGamma(final Tensor tensor) {
        int h = tensor.hashCode();
        return (h == gammaName) && tensor.getClass().equals(SimpleTensor.class);
    }

    protected final boolean isGamma5(final Tensor tensor) {
        int h = tensor.hashCode();
        return (h == gamma5Name) && tensor.getClass().equals(SimpleTensor.class);
    }

    protected final SimpleTensor setUpperMatrixIndex(SimpleTensor gamma, int matrixUpper) {
        return setMatrixIndices(gamma, matrixUpper, gamma.getIndices().getLower().get(matrixType, 0));
    }

    protected final SimpleTensor setLowerMatrixIndex(SimpleTensor gamma, int matrixLower) {
        return setMatrixIndices(gamma, gamma.getIndices().getUpper().get(matrixType, 0), matrixLower);
    }

    protected final void swapAdj(Tensor[] gammas, int j) {
        Tensor t = gammas[j];
        gammas[j] = setMatrixIndices((SimpleTensor) gammas[j + 1], gammas[j].getIndices().getOfType(matrixType));
        gammas[j + 1] = setMatrixIndices((SimpleTensor) t, gammas[j + 1].getIndices().getOfType(matrixType));
    }

    protected Tensor[] cutAdj(Tensor[] original, int i) {
        if (original.length < 2)
            return original;

        Tensor[] n = new Tensor[original.length - 2];
        System.arraycopy(original, 0, n, 0, i);
        System.arraycopy(original, i + 2, n, i, original.length - i - 2);

        if (n.length == 0)
            return n;

        int u, l;
        if (i == 0) {
            i = 1;
            u = original[0].getIndices().getUpper().get(matrixType, 0);
            l = n[i - 1].getIndices().getLower().get(matrixType, 0);
        } else if (i == original.length - 2) {
            u = n[i - 1].getIndices().getUpper().get(matrixType, 0);
            l = original[original.length - 1].getIndices().getLower().get(matrixType, 0);
        } else {
            u = n[i - 1].getIndices().getUpper().get(matrixType, 0);
            l = n[i].getIndices().getUpper().get(matrixType, 0);
        }

        n[i - 1] = setMatrixIndices((SimpleTensor) n[i - 1], u, l);
        return n;
    }

    protected Tensor[] createLine(final int length) {
        Tensor[] gammas = new Tensor[length];
        int matrixIndex, u = matrixIndex = setType(matrixType, 0);
        for (int i = 0; i < length; ++i)
            gammas[i] = Tensors.simpleTensor(gammaName,
                    createSimple(null,
                            u | 0x80000000,
                            u = ++matrixIndex,
                            setType(metricType, i)));
        return gammas;
    }

    protected Tensor[] del(Tensor[] arr, int i) {
        Tensor t = arr[i];
        arr = ArraysUtils.remove(arr, i);
        if (arr.length == 0)
            return arr;
        if (i == 0)
            arr[0] = setUpperMatrixIndex((SimpleTensor) arr[0],
                    t.getIndices().getUpper().get(matrixType, 0));
        else if (i == arr.length)
            arr[arr.length - 1] = setLowerMatrixIndex((SimpleTensor) arr[arr.length - 1],
                    t.getIndices().getLower().get(matrixType, 0));
        else
            arr[i] = setUpperMatrixIndex((SimpleTensor) arr[i],
                    t.getIndices().getUpper().get(matrixType, 0));
        return arr;
    }

    //returns matrix Index
    protected int del(List<Tensor> arr, int i) {
        Tensor t = arr.remove(i);
        if (arr.isEmpty())
            return t.getIndices().getLower().get(matrixType, 0);
        if (i == 0) {
            arr.set(0, setUpperMatrixIndex((SimpleTensor) arr.get(0),
                    t.getIndices().getUpper().get(matrixType, 0)));
            return t.getIndices().getLower().get(matrixType, 0);
        } else if (i == arr.size()) {
            arr.set(arr.size() - 1, setLowerMatrixIndex((SimpleTensor) arr.get(arr.size() - 1),
                    t.getIndices().getLower().get(matrixType, 0)));
            return t.getIndices().getUpper().get(matrixType, 0);
        } else {
            arr.set(i, setUpperMatrixIndex((SimpleTensor) arr.get(i),
                    t.getIndices().getUpper().get(matrixType, 0)));
            return t.getIndices().getLower().get(matrixType, 0);
        }
    }

    protected static SimpleTensor setMatrixIndices(SimpleTensor gamma, Indices matrixIndices) {
        return setMatrixIndices(gamma, matrixIndices.getUpper().get(0), matrixIndices.getLower().get(0));
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

    protected static SimpleTensor setMetricIndex(SimpleTensor gamma, int metricIndex) {
        int[] indices = gamma.getIndices().getAllIndices().copy();
        for (int i = indices.length - 1; i >= 0; --i)
            if (CC.isMetric(getType(indices[i])))
                indices[i] = metricIndex;
        return simpleTensor(gamma.getName(), IndicesFactory.createSimple(null, indices));
    }
}
