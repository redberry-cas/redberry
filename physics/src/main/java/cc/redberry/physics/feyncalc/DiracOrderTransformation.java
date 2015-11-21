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
import cc.redberry.core.indices.Indices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.ExpandAndEliminateTransformation;
import cc.redberry.core.transformations.ExpandTensorsAndEliminateTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationToStringAble;
import cc.redberry.core.transformations.options.Creator;
import cc.redberry.core.transformations.options.Options;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArray;
import cc.redberry.core.utils.IntArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cc.redberry.core.indices.IndicesFactory.createSimple;
import static cc.redberry.core.indices.IndicesUtils.*;
import static cc.redberry.core.tensor.FastTensors.multiplySumElementsOnFactor;
import static cc.redberry.core.tensor.StructureOfContractions.getToTensorIndex;
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
        this.expandAndEliminate = ExpandTensorsAndEliminateTransformation.EXPAND_TENSORS_AND_ELIMINATE;
        this.simplifyG5 = null;
    }

    public DiracOrderTransformation(SimpleTensor gammaMatrix, Tensor dimension, Tensor traceOfOne) {
        super(gammaMatrix, dimension, traceOfOne);
        this.expandAndEliminate = ExpandTensorsAndEliminateTransformation.EXPAND_TENSORS_AND_ELIMINATE;
        this.simplifyG5 = null;
    }

    public DiracOrderTransformation(SimpleTensor gammaMatrix, Tensor dimension) {
        this(gammaMatrix, dimension, guessTraceOfOne(dimension));
    }

    public DiracOrderTransformation(SimpleTensor gammaMatrix, SimpleTensor gamma5) {
        super(gammaMatrix, gamma5, null, Complex.FOUR, Complex.FOUR);
        this.expandAndEliminate = ExpandTensorsAndEliminateTransformation.EXPAND_TENSORS_AND_ELIMINATE;
        this.simplifyG5 = new SimplifyGamma5Transformation(gammaMatrix, gamma5);
    }

    public DiracOrderTransformation(SimpleTensor gammaMatrix, SimpleTensor gamma5, Tensor dimension, Transformation simplifications) {
        this(gammaMatrix, gamma5, dimension, guessTraceOfOne(dimension), simplifications);
    }

    public DiracOrderTransformation(SimpleTensor gammaMatrix, SimpleTensor gamma5, Tensor dimension, Tensor traceOfOne,
                                    Transformation simplification) {
        super(gammaMatrix, gamma5, null, dimension, traceOfOne);
        this.expandAndEliminate = new ExpandAndEliminateTransformation(simplification);
        this.simplifyG5 = new SimplifyGamma5Transformation(gammaMatrix, gamma5);
    }

    @Creator
    public DiracOrderTransformation(@Options DiracOptions options) {
        super(options.gammaMatrix, options.gamma5, null, options.dimension, options.traceOfOne);
        this.expandAndEliminate = options.expandAndEliminate;
        this.simplifyG5 = new SimplifyGamma5Transformation(options.gammaMatrix, options.gamma5);
    }

    @Override
    public Tensor transform(Tensor tensor) {
        SubstitutionIterator iterator = new SubstitutionIterator(tensor);
        Tensor current;
        while ((current = iterator.next()) != null) {
            if (!(current instanceof Product))
                continue;
            if (current.getIndices().size(matrixType) == 0)
                continue;
            if (!containsGammaOr5Matrices(current))
                continue;
            if (simplifyG5 != null)
                current = simplifyG5.transform(current);
            Product product = (Product) current;
            int offset = product.sizeOfIndexlessPart();
            ProductContent pc = product.getContent();
            StructureOfContractions st = pc.getStructureOfContractions();

            PrimitiveSubgraph[] partition
                    = PrimitiveSubgraphPartition.calculatePartition(pc, matrixType);

            IntArrayList positionsOfGammas = new IntArrayList();
            List<Tensor> ordered = new ArrayList<>();
            gammas:
            for (PrimitiveSubgraph subgraph : partition) {
                if (subgraph.getGraphType() != GraphType.Cycle && subgraph.getGraphType() != GraphType.Line)
                    continue;

                IntArrayList tPositionsOfGammas = new IntArrayList();
                List<Gamma> gammas = new ArrayList<>();
                for (int i = 0; i < subgraph.size(); ++i) {
                    Tensor g5 = null;
                    for (; i < subgraph.size(); ++i) {
                        Tensor t = pc.get(subgraph.getPosition(i));
                        if (!isGammaOrGamma5(t))
                            break;
                        else {
                            tPositionsOfGammas.add(offset + subgraph.getPosition(i));
                            if (isGamma5(t))
                                g5 = t;
                            else
                                gammas.add(new Gamma(t, t.getIndices().get(metricType, 0), getContraction(subgraph.getPosition(i), pc, st)));
                        }
                    }
                    if (!gammas.isEmpty()) {
                        Tensor o = orderArray(gammas.toArray(new Gamma[gammas.size()]));
                        if (o == null)
                            continue gammas;
                        positionsOfGammas.addAll(tPositionsOfGammas);
                        if (g5 != null) {
                            if (o instanceof Sum) {
                                Tensor[] pair = resolveDummy(o, g5);
                                o = FastTensors.multiplySumElementsOnFactor((Sum) pair[0], pair[1]);
                            } else
                                o = multiplyAndRenameConflictingDummies(o, g5);
                        }
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

    private Tensor getContraction(int gamma,
                                  ProductContent pc,
                                  StructureOfContractions sc) {
        Indices indices = pc.get(gamma).getIndices();
        int j = 0;
        for (; j < indices.size(); ++j)
            if (metricType.getType() == getType(indices.get(j)))
                break;
        int to = getToTensorIndex(sc.contractions[gamma][j]);
        if (to == -1)
            return null;
        return pc.get(to);
    }

    private static final class Gamma implements Comparable<Gamma> {
        final Tensor gamma;
        final int index;
        final Tensor contraction;

        public Gamma(Tensor gamma, int index, Tensor contraction) {
            this.gamma = gamma;
            this.index = index;
            this.contraction = contraction;
        }

        boolean contracted() {
            return contraction instanceof SimpleTensor && contraction.getIndices().size() == 1;
        }

        @Override
        public int compareTo(Gamma o) {
            if (contracted() && o.contracted())
                return ((SimpleTensor) contraction).getStringName().compareTo((((SimpleTensor) o.contraction)).getStringName());
            else if (contracted() && !o.contracted())
                return -1;
            else if (o.contracted() && !contracted())
                return 1;
            else return Integer.compare(getNameWithoutType(index), getNameWithoutType(o.index));
        }
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

        metricIndices = Permutations.permute(metricIndices, Permutations.inverse(permutation));
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
        Gamma[] gs = new Gamma[gammas.length];
        for (int i = 0; i < gammas.length; ++i)
            gs[i] = new Gamma(gammas[i], gammas[i].getIndices().get(metricType, 0), null);
        return orderArray(gs);
    }

    private Tensor orderArray(Gamma[] gammas) {
        final int numberOfGammas = gammas.length;
        int[] permutation = Permutations.createIdentityArray(numberOfGammas);

        Tensor fGamma = gammas[0].gamma, lGamma = gammas[gammas.length - 1].gamma;
        int[] mIndices = new int[numberOfGammas];
        for (int i = 0; i < numberOfGammas; ++i)
            mIndices[i] = gammas[i].index;

        //use stable sort!
        //!! gammas is lost now !!
        ArraysUtils.insertionSort(gammas, permutation);
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
        iTo[numberOfGammas] = fGamma.getIndices().getUpper().get(matrixType, 0);
        iFrom[numberOfGammas + 1] = cached.originalArray[numberOfGammas - 1].getIndices().getLower().get(matrixType, 0);
        iTo[numberOfGammas + 1] = lGamma.getIndices().getLower().get(matrixType, 0);
        return eliminate(ApplyIndexMapping.applyIndexMapping(cached.ordered, new Mapping(iFrom, iTo)));
    }

    private Tensor orderArray0(Tensor[] gammas) {
        SumBuilder sb = new SumBuilder();
        int swaps = 0;
        for (int i = 0; i < gammas.length - 1; i++)
            for (int j = 0; j < gammas.length - i - 1; j++)
                if (getNameWithoutType(gammas[j].getIndices().get(metricType, 0)) >
                        getNameWithoutType(gammas[j + 1].getIndices().get(metricType, 0))) {
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
                    swapAdj(gammas, j);
                    ++swaps;
                }
        Tensor ordered = multiply(gammas);
        if (swaps % 2 == 1)
            ordered = negate(ordered);
        sb.put(ordered);
        return sb.build();
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "DiracOrder";
    }
}
