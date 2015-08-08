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

import cc.redberry.core.graph.GraphType;
import cc.redberry.core.graph.PrimitiveSubgraph;
import cc.redberry.core.graph.PrimitiveSubgraphPartition;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.ExpandTensorsAndEliminateTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.IntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cc.redberry.core.indices.IndicesUtils.getType;
import static cc.redberry.core.indices.IndicesUtils.setType;
import static cc.redberry.core.tensor.StructureOfContractions.getToTensorIndex;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.transformations.EliminateMetricsTransformation.eliminate;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class DiracSimplify0
        extends AbstractTransformationWithGammas {
    private final Transformation expandAndEliminate;

    public DiracSimplify0(SimpleTensor gammaMatrix, Tensor dimension, Tensor traceOfOne, Transformation simplifications) {
        super(gammaMatrix, dimension, traceOfOne);
        this.expandAndEliminate = new ExpandTensorsAndEliminateTransformation(simplifications);
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
            if (!containsGammaMatrices(current))
                continue;
            Product product = (Product) current;
            int offset = product.sizeOfIndexlessPart();
            ProductContent pc = product.getContent();
            StructureOfContractions sc = pc.getStructureOfContractions();

            PrimitiveSubgraph[] partition
                    = PrimitiveSubgraphPartition.calculatePartition(pc, matrixType);

            IntArrayList modifiedMultipliers = new IntArrayList();
            List<Tensor> ordered = new ArrayList<>();
            boolean needRetry = false;

            gammas:
            for (PrimitiveSubgraph subgraph : partition) {
                if (subgraph.getGraphType() != GraphType.Cycle && subgraph.getGraphType() != GraphType.Line)
                    continue;

                List<Element> couples = new ArrayList<>();
                for (int i = 0; i < subgraph.size(); ++i) {
                    couples.clear();
                    boolean coupled;
                    for (; i < subgraph.size(); ++i) {
                        Tensor t = pc.get(subgraph.getPosition(i));
                        if (!isGamma(t))
                            break;
                        else {
                            coupled = false;
                            Element contraction = getContraction(i, subgraph.getPosition(i), pc, sc);
                            if (contraction == null)
                                continue;
                            Tensor t1 = pc.get(contraction.tIndex1);
                            if (!(t1 instanceof SimpleTensor) || t1.getIndices().size() != 1)
                                continue;
                            for (int k = 0; k < couples.size(); ++k)
                                if (match(pc, couples.get(k), contraction)) {
                                    couples.get(k).couple(contraction);
                                    coupled = true;
                                    break;
                                }
                            if (!coupled)
                                couples.add(contraction);
                        }
                    }

                    //filter couples
                    for (int j = couples.size() - 1; j >= 0; --j)
                        if (!couples.get(j).coupled())
                            couples.remove(j);

                    if (couples.isEmpty())
                        continue;

                    Element[] els = couples.toArray(new Element[couples.size()]);
                    Arrays.sort(els);

                    int[] mask = new int[subgraph.size()];
                    for (Element el : els) {
                        if (!el.available(mask)) {
                            needRetry = true;
                            continue;
                        }
                        el.cover(mask, modifiedMultipliers, subgraph, offset);
                        ordered.add(simplify(el, subgraph, pc));
                    }
                }

            }
            if (modifiedMultipliers.isEmpty())
                continue;

            ordered.add(product.remove(modifiedMultipliers.toArray()));
            Tensor simple = expandAndEliminate.transform(multiplyAndRenameConflictingDummies(ordered));
            simple = traceOfOne.transform(simple);
            simple = deltaTrace.transform(simple);
            if (needRetry)
                simple = transform(simple);
            iterator.safeSet(simple);
        }
        return iterator.result();
    }

    private Tensor simplify(Element el, PrimitiveSubgraph partition, ProductContent pc) {
        Tensor[] gammas = new Tensor[el.gIndex2 - el.gIndex1 + 1];
        for (int i = el.gIndex1; i <= el.gIndex2; ++i)
            gammas[i - el.gIndex1] = pc.get(partition.getPosition(i));

        Tensor r = order(gammas);
        Tensor m = multiply(pc.get(el.tIndex1), pc.get(el.tIndex2));
        if (r instanceof Sum)
            r = FastTensors.multiplySumElementsOnFactor((Sum) r, m);
        else r = multiply(r, m);
        r = expandAndEliminate.transform(r);
        r = traceOfOne.transform(r);
        r = deltaTrace.transform(r);
        return r;
    }

    private Tensor metric(Tensor[] gammas) {
        return multiply(
                //metric
                createMetricOrKronecker(gammas[0].getIndices().get(metricType, 0),
                        gammas[1].getIndices().get(metricType, 0)),
                //matrix
                createMetricOrKronecker(gammas[0].getIndices().getUpper().get(matrixType, 0),
                        gammas[1].getIndices().getLower().get(matrixType, 0)));

    }

    private final TIntObjectHashMap<Tensor> cache = new TIntObjectHashMap<>();

    private Tensor order(Tensor[] gammas) {
        int numberOfGammas = gammas.length;
        Tensor tensor = cache.get(numberOfGammas);
        if (tensor == null)
            cache.put(numberOfGammas, tensor = order0(createLine(numberOfGammas)));
        int[] iFrom = new int[numberOfGammas + 2], iTo = new int[numberOfGammas + 2];
        for (int i = 0; i < numberOfGammas; ++i) {
            iFrom[i] = setType(metricType, i);
            iTo[i] = gammas[i].getIndices().get(metricType, 0);
        }
        iFrom[numberOfGammas] = setType(matrixType, 0) | 0x80000000;
        iTo[numberOfGammas] = gammas[0].getIndices().getUpper().get(matrixType, 0);
        iFrom[numberOfGammas + 1] = setType(matrixType, numberOfGammas);
        iTo[numberOfGammas + 1] = gammas[numberOfGammas - 1].getIndices().getLower().get(matrixType, 0);
        return eliminate(ApplyIndexMapping.applyIndexMapping(tensor, new Mapping(iFrom, iTo)));
    }

    private Tensor order0(Tensor[] gammas) {
        if (gammas.length == 1)
            return gammas[0];
        else if (gammas.length == 2)
            return metric(gammas);
        else {
            Tensor[] a = gammas.clone();
            a[0] = Complex.TWO;
            a[1] = metric(gammas);

            Tensor[] b0 = gammas.clone();
            swapAdj(b0, 0);

            Tensor b = order(Arrays.copyOfRange(b0, 1, b0.length));
            if (b instanceof Sum) {
                Tensor[] pair = resolveDummy(b, b0[0]);
                b = FastTensors.multiplySumElementsOnFactor((Sum) pair[0], pair[1]);
            } else b = multiplyAndRenameConflictingDummies(b, b0[0]);
            return expandAndEliminate.transform(subtract(multiply(a), b));
        }
    }

    private static boolean match(ProductContent pc, Element a, Element b) {
        return IndexMappings.anyMappingExists(pc.get(a.tIndex1), pc.get(b.tIndex1));
    }

    private Element getContraction(int gIndex, int gamma,
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
        return new Element(to, gIndex);
    }

    private static final class Element implements Comparable<Element> {
        final int tIndex1, gIndex1;
        int tIndex2, gIndex2 = tIndex2 = -1;

        public Element(int tIndex1, int gIndex1) {
            this.tIndex1 = tIndex1;
            this.gIndex1 = gIndex1;
        }

        void couple(Element o) {
            tIndex2 = o.tIndex1;
            gIndex2 = o.gIndex1;
            assert gIndex2 > gIndex1;
        }

        boolean coupled() {
            return tIndex2 != -1;
        }

        void cover(int[] mask, IntArrayList modified, PrimitiveSubgraph subgraph, int offset) {
            modified.ensureCapacity(gIndex2 - gIndex1 + 3);
            for (int i = gIndex1; i <= gIndex2; ++i) {
                mask[i] = -1;
                modified.add(subgraph.getPosition(i) + offset);
            }
            modified.addAll(tIndex1 + offset, tIndex2 + offset);
        }

        boolean available(int[] mask) {
            for (int i = gIndex1; i <= gIndex2; ++i)
                if (mask[i] != 0)
                    return false;
            return true;
        }

        @Override
        public int compareTo(Element o) {
            return Integer.compare(gIndex2 - gIndex1, o.gIndex2 - o.gIndex1);
        }
    }
}
