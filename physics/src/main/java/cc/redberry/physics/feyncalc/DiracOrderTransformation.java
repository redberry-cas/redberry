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

import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.graph.GraphType;
import cc.redberry.core.graph.PrimitiveSubgraph;
import cc.redberry.core.graph.PrimitiveSubgraphPartition;
import cc.redberry.core.groups.permutations.Permutations;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.ExpandTensorAndEliminateTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationToStringAble;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArray;
import cc.redberry.core.utils.IntArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cc.redberry.core.indices.IndicesFactory.createSimple;
import static cc.redberry.core.indices.IndicesUtils.getNameWithType;
import static cc.redberry.core.indices.IndicesUtils.setType;
import static cc.redberry.core.tensor.FastTensors.multiplySumElementsOnFactor;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.transformations.EliminateMetricsTransformation.eliminate;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class DiracOrderTransformation extends AbstractTransformationWithGammas
        implements TransformationToStringAble {
    private final Transformation simplifyG5;
    private final Transformation expandAndEliminate;

    public DiracOrderTransformation(SimpleTensor gammaMatrix) {
        super(gammaMatrix, Complex.FOUR, Complex.FOUR);
        this.expandAndEliminate = ExpandTensorAndEliminateTransformation.EXPAND_TENSORS_AND_ELIMINATE;
        this.simplifyG5 = null;
    }

    public DiracOrderTransformation(SimpleTensor gammaMatrix, Tensor dimension, Tensor traceOfOne) {
        super(gammaMatrix, dimension, traceOfOne);
        this.expandAndEliminate = ExpandTensorAndEliminateTransformation.EXPAND_TENSORS_AND_ELIMINATE;
        this.simplifyG5 = null;
    }

    public DiracOrderTransformation(SimpleTensor gammaMatrix, SimpleTensor gamma5) {
        super(gammaMatrix, gamma5, null, Complex.FOUR, Complex.FOUR);
        this.expandAndEliminate = ExpandTensorAndEliminateTransformation.EXPAND_TENSORS_AND_ELIMINATE;
        this.simplifyG5 = new SimplifyGamma5Transformation(gammaMatrix, gamma5);
    }

    public DiracOrderTransformation(SimpleTensor gammaMatrix, SimpleTensor gamma5, Tensor dimension, Tensor traceOfOne) {
        super(gammaMatrix, gamma5, null, dimension, traceOfOne);
        this.expandAndEliminate = ExpandTensorAndEliminateTransformation.EXPAND_TENSORS_AND_ELIMINATE;
        this.simplifyG5 = new SimplifyGamma5Transformation(gammaMatrix, gamma5);
    }

    @Override
    public Tensor transform(Tensor tensor) {
        SubstitutionIterator iterator = new SubstitutionIterator(tensor);
        Tensor current;
        out:
        while ((current = iterator.next()) != null) {
            if (!(current instanceof Product))
                continue;
            if (current.getIndices().size(matrixType) == 0)
                continue;
            if (!containsGammaMatrices(current))
                continue;
            if (simplifyG5 != null)
                current = simplifyG5.transform(current);
            Product product = (Product) current;
            int offset = product.sizeOfIndexlessPart();
            ProductContent pc = product.getContent();

            PrimitiveSubgraph[] partition
                    = PrimitiveSubgraphPartition.calculatePartition(pc, matrixType);

            IntArrayList positionsOfGammas = new IntArrayList();
            List<Tensor> ordered = new ArrayList<>();
            gammas:
            for (PrimitiveSubgraph subgraph : partition) {
                if (subgraph.getGraphType() != GraphType.Cycle && subgraph.getGraphType() != GraphType.Line)
                    continue;

                IntArrayList tPositionsOfGammas = new IntArrayList();
                List<Tensor> gammas = new ArrayList<>();
                for (int i = 0; i < subgraph.size(); ++i) {
                    Tensor g5 = null;
                    for (; i < subgraph.size(); ++i) {
                        Tensor t = pc.get(subgraph.getPosition(i));
                        if (!isGammaOrGamma5(t))
                            break;
                        else {
                            if (isGamma5(t))
                                g5 = t;
                            tPositionsOfGammas.add(offset + subgraph.getPosition(i));
                            gammas.add(t);
                        }
                    }
                    if (!gammas.isEmpty()) {
                        Tensor o = orderArray(gammas.toArray(new Tensor[gammas.size()]));
                        if (o == null)
                            continue gammas;
                        positionsOfGammas.addAll(tPositionsOfGammas);
                        if (g5 != null)
                            o = o instanceof Sum ? FastTensors.multiplySumElementsOnFactor((Sum) o, g5) : multiply(o, g5);
                        ordered.add(o);
                    }
                    gammas.clear();
                }
            }
            if (positionsOfGammas.isEmpty())
                continue;

            ordered.add(product.remove(positionsOfGammas.toArray()));
            Tensor simple = expandAndEliminate.transform(multiplyAndRenameConflictingDummies(ordered));
            simple = traceOfOne.transform(simple);
            simple = deltaTrace.transform(simple);
            iterator.safeSet(simple);
        }
        return iterator.result();
    }

    //todo static thread local cache!
    private final HashMap<IntArray, Cached> cache = new HashMap<>();

    private static final class Cached {
        protected final Tensor[] originalArray;
        private final Tensor ordered;

        public Cached(Tensor[] originalArray, Tensor ordered) {
            this.originalArray = originalArray;
            this.ordered = ordered;
        }

        int[] getOriginalIndices(IndexType metricType) {
            int[] metricIndices = new int[originalArray.length];
            for (int i = 0; i < originalArray.length; ++i)
                metricIndices[i] = originalArray[i].getIndices().get(metricType, 0);
            return metricIndices;
        }
    }

    private Tensor[] createArray(final int[] permutation) {
        int[] metricIndices = new int[permutation.length];
        for (int i = 0; i < permutation.length; ++i)
            metricIndices[i] = setType(metricType, i);

        metricIndices = Permutations.permute(metricIndices, permutation);
        Tensor[] gammas = new Tensor[permutation.length];
        int matrixIndex, u = matrixIndex = setType(matrixType, 0);
        for (int i = 0; i < permutation.length; ++i) {
            gammas[i] = Tensors.simpleTensor(gammaName,
                    createSimple(null,
                            u | 0x80000000,
                            u = ++matrixIndex,
                            metricIndices[i]));

        }
        return gammas;
    }

    private Tensor orderArray(Tensor[] gammas) {
        final int numberOfGammas = gammas.length;
        int[] mIndices = new int[numberOfGammas], mIndicesNames = new int[numberOfGammas];
        for (int i = 0; i < numberOfGammas; ++i) {
            int index = gammas[i].getIndices().get(metricType, 0);
            mIndices[i] = index;
            mIndicesNames[i] = getNameWithType(index);
        }
        int[] permutation = Permutations.createIdentityArray(numberOfGammas);
        //use stable sort!
        ArraysUtils.insertionSort(mIndicesNames, permutation);
        if (Permutations.isIdentity(permutation))
            return null;//signals that array is sorted!

        Cached cached = cache.get(new IntArray(permutation));
        if (cached == null) {
            Tensor[] arr = createArray(permutation);
            Tensor ordered = eliminate(orderArray0(arr.clone()));
            cache.put(new IntArray(permutation), cached = new Cached(arr, ordered));
        }

        int[] iFrom = new int[numberOfGammas + 2], iTo = new int[numberOfGammas + 2];
        System.arraycopy(cached.getOriginalIndices(metricType), 0, iFrom, 0, numberOfGammas);
        System.arraycopy(mIndices, 0, iTo, 0, numberOfGammas);
        iFrom[numberOfGammas] = cached.originalArray[0].getIndices().getUpper().get(matrixType, 0);
        iTo[numberOfGammas] = gammas[0].getIndices().getUpper().get(matrixType, 0);
        iFrom[numberOfGammas + 1] = cached.originalArray[numberOfGammas - 1].getIndices().getLower().get(matrixType, 0);
        iTo[numberOfGammas + 1] = gammas[numberOfGammas - 1].getIndices().getLower().get(matrixType, 0);
        return eliminate(ApplyIndexMapping.applyIndexMapping(cached.ordered, new Mapping(iFrom, iTo)));
    }

    private Tensor orderArray0(Tensor[] gammas) {
        SumBuilder sb = new SumBuilder();
        int swaps = 0;
        for (int i = 0; i < gammas.length - 1; i++)
            for (int j = 0; j < gammas.length - i - 1; j++)
                if (getNameWithType(gammas[j].getIndices().get(metricType, 0)) >
                        getNameWithType(gammas[j + 1].getIndices().get(metricType, 0))) {
                    Tensor metric = multiply(Complex.TWO,
                            createMetricOrKronecker(gammas[j].getIndices().get(metricType, 0),
                                    gammas[j + 1].getIndices().get(metricType, 0)));
                    Tensor[] cadj = cutAdj(gammas, j);
                    Tensor adj;
                    if (cadj.length == 0)
                        adj = createMetricOrKronecker(gammas[j].getIndices().getUpper().get(matrixType, 0),
                                gammas[j + 1].getIndices().getLower().get(matrixType, 0));
                    else if (cadj.length == 1)
                        adj = cadj[0];
                    else
                        adj = orderArray(cadj);
                    if (adj == null)
                        adj = multiply(cadj);
                    adj = adj instanceof Sum ?
                            multiplySumElementsOnFactor((Sum) adj, metric) : multiply(adj, metric);
                    if (swaps % 2 == 1)
                        adj = negate(adj);
                    sb.put(adj);
                    Tensor t = gammas[j];
                    gammas[j] = new Mapping(gammas[j].getIndices().getOfType(metricType).toArray(),
                            gammas[j + 1].getIndices().getOfType(metricType).toArray()).transform(gammas[j]);
                    gammas[j + 1] = new Mapping(gammas[j + 1].getIndices().getOfType(metricType).toArray(),
                            t.getIndices().getOfType(metricType).toArray()).transform(gammas[j + 1]);
                    ++swaps;
                }
        Tensor ordered = multiply(gammas);
        if (swaps % 2 == 1)
            ordered = negate(ordered);
        sb.put(ordered);
        return sb.build();
    }

    private Tensor[] cutAdj(Tensor[] original, int i) {
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

    @Override
    public String toString(OutputFormat outputFormat) {
        return "DiracOrder";
    }
}
