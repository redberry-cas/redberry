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
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.graph.GraphType;
import cc.redberry.core.graph.PrimitiveSubgraph;
import cc.redberry.core.graph.PrimitiveSubgraphPartition;
import cc.redberry.core.groups.permutations.Permutations;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParseToken;
import cc.redberry.core.parser.preprocessor.ChangeIndicesTypesAndTensorNames;
import cc.redberry.core.parser.preprocessor.TypesAndNamesTransformer;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.TransformationToStringAble;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArray;
import cc.redberry.core.utils.IntArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cc.redberry.core.indices.IndicesUtils.getNameWithType;
import static cc.redberry.core.tensor.FastTensors.multiplySumElementsOnFactor;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.transformations.EliminateMetricsTransformation.eliminate;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class DiracOrderTransformation extends AbstractTransformationWithGammas
        implements TransformationToStringAble {
    public DiracOrderTransformation(SimpleTensor gammaMatrix) {
        super(gammaMatrix, Complex.FOUR, Complex.FOUR);
    }

    public DiracOrderTransformation(SimpleTensor gammaMatrix, Tensor dimension, Tensor traceOfOne) {
        super(gammaMatrix, dimension, traceOfOne);
    }

    public DiracOrderTransformation(SimpleTensor gammaMatrix, SimpleTensor gamma5, SimpleTensor leviCivita, Tensor dimension, Tensor traceOfOne) {
        super(gammaMatrix, gamma5, leviCivita, dimension, traceOfOne);
    }

    @Override
    public Tensor transform(Tensor tensor) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(tensor);
        Tensor current;
        out:
        while ((current = iterator.next()) != null) {
            if (!(current instanceof Product))
                continue;
            if (current.getIndices().size(matrixType) == 0)
                continue;
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

                int[] p = subgraph.getPartition();
                if (p.length < 2)
                    continue;

                IntArrayList tPositionsOfGammas = new IntArrayList();
                List<Tensor> array = new ArrayList<>(p.length);
                Tensor g5 = null;
                for (int i = 0; i < p.length; ++i) {
                    Tensor t = pc.get(p[i]);
                    if (!isGammaOrGamma5(t))
                        continue gammas;
                    tPositionsOfGammas.add(offset + p[i]);
                    if (isGamma5(t)) {
                        g5 = t;
                        break;
                    }
                    array.add(t);
                }

                Tensor o = orderArray(array.toArray(new Tensor[array.size()]));
                if (o == null)
                    continue gammas;
                positionsOfGammas.addAll(tPositionsOfGammas);
                if (g5 != null)
                    o = o instanceof Sum ? FastTensors.multiplySumElementsOnFactor((Sum) o, g5) : multiply(o, g5);
                ordered.add(o);
            }
            ordered.add(product.remove(positionsOfGammas.toArray()));
            iterator.set(multiply(ordered));
        }
        return iterator.result();
    }

    //todo static thread local cache!
    private final HashMap<IntArray, Cached> cache = new HashMap<>();

    private static final class Cached {
        private final int[] originalIndices;
        private final ParseToken string;

        public Cached(int[] originalIndices, ParseToken string) {
            this.originalIndices = originalIndices;
            this.string = string;
        }

        private Tensor setIndices(int[] indices) {
            return new ChangeIndicesTypesAndTensorNames(
                    TypesAndNamesTransformer.Utils.setIndices(originalIndices, indices))
                    .transform(string).toTensor();
        }
    }

    private Tensor orderArray(Tensor[] gammas) {
        int[] indices = new int[3 * gammas.length], mIndicesNames = new int[gammas.length];
        int j = 0;
        for (int i = 0; i < gammas.length; ++i) {
            for (int k = 0; k < gammas[i].getIndices().size(); ++k)
                indices[j++] = gammas[i].getIndices().get(k);
            mIndicesNames[i] = getNameWithType(gammas[i].getIndices().get(metricType, 0));
        }
        int[] permutation = Permutations.createIdentityArray(gammas.length);
        //use stable sort!
        ArraysUtils.insertionSort(mIndicesNames, permutation);
        if (Permutations.isIdentity(permutation))
            return null;//signals that array is sorted!

        Cached cached = cache.get(new IntArray(permutation));
//        if (false || cached != null)
//            return eliminate(cached.setIndices(indices));
//        else {
        Tensor r = eliminate(orderArray0(gammas));
        cache.put(new IntArray(permutation), new Cached(indices,
                CC.current().getParseManager().getParser().parse(r.toString(OutputFormat.Redberry))));
        return r;
//        }
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
            u = original[0].getIndices().getOfType(matrixType).getUpper().get(0);
            l = n[i - 1].getIndices().getOfType(matrixType).getLower().get(0);
        } else if (i == original.length - 2) {
            u = n[i - 1].getIndices().getOfType(matrixType).getUpper().get(0);
            l = original[original.length - 1].getIndices().getOfType(matrixType).getLower().get(0);
        } else {
            u = n[i - 1].getIndices().getOfType(matrixType).getUpper().get(0);
            l = n[i].getIndices().getOfType(matrixType).getUpper().get(0);
        }

        n[i - 1] = setMatrixIndices((SimpleTensor) n[i - 1], u, l);
        return n;
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "OrderGammas";
    }
}
