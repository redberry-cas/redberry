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
import cc.redberry.core.graph.GraphType;
import cc.redberry.core.graph.PrimitiveSubgraph;
import cc.redberry.core.graph.PrimitiveSubgraphPartition;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParseToken;
import cc.redberry.core.parser.Parser;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.ExpandTensorsAndEliminateTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.IntArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static cc.redberry.core.indexmapping.IndexMappings.anyMappingExists;
import static cc.redberry.core.indices.IndicesUtils.*;
import static cc.redberry.core.tensor.FastTensors.multiplySumElementsOnFactor;
import static cc.redberry.core.tensor.StructureOfContractions.getToTensorIndex;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.transformations.EliminateMetricsTransformation.eliminate;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SpinorsSimplifyTransformation extends AbstractTransformationWithGammas {
    private final SimpleTensor u, v, uBar, vBar, momentum, mass;
    private final Expression uSubs, vSubs, uBarSubs, vBarSubs, p2;
    private final Transformation simplifyG5;
    private final Transformation expandAndEliminate;
    private final DiracSimplifyTransformation diracSimplify;

    public SpinorsSimplifyTransformation(SimpleTensor gammaMatrix,
                                         SimpleTensor u, SimpleTensor v,
                                         SimpleTensor uBar, SimpleTensor vBar,
                                         SimpleTensor momentum, SimpleTensor mass) {
        this(gammaMatrix, null, Complex.FOUR, Complex.FOUR, u, v, uBar, vBar,
                momentum, mass, Transformation.INDENTITY, true);
    }

    public SpinorsSimplifyTransformation(SimpleTensor gammaMatrix, SimpleTensor gamma5,
                                         Tensor dimension, Tensor traceOfOne,
                                         SimpleTensor u, SimpleTensor v,
                                         SimpleTensor uBar, SimpleTensor vBar,
                                         SimpleTensor momentum, SimpleTensor mass,
                                         Transformation simplifications,
                                         boolean diracSimplify) {
        super(gammaMatrix, gamma5, null, dimension, traceOfOne);
        this.u = u;
        this.v = v;
        this.uBar = uBar;
        this.vBar = vBar;
        this.momentum = momentum;
        this.mass = mass;

        Transformation match = new TransformationCollection(
                expression(simpleTensor("cu", IndicesFactory.createSimple(null, setType(matrixType, 0))),
                        setIndices(uBar, IndicesFactory.createSimple(null, setType(matrixType, 0)))),
                expression(simpleTensor("u", IndicesFactory.createSimple(null, setState(true, setType(matrixType, 0)))),
                        setIndices(u, IndicesFactory.createSimple(null, setState(true, setType(matrixType, 0))))),
                expression(simpleTensor("cv", IndicesFactory.createSimple(null, setType(matrixType, 0))),
                        setIndices(vBar, IndicesFactory.createSimple(null, setType(matrixType, 0)))),
                expression(simpleTensor("v", IndicesFactory.createSimple(null, setState(true, setType(matrixType, 0)))),
                        setIndices(v, IndicesFactory.createSimple(null, setState(true, setType(matrixType, 0))))),
                expression(simpleTensor("p", IndicesFactory.createSimple(null, setState(true, setType(metricType, 0)))),
                        setIndices(momentum, IndicesFactory.createSimple(null, setState(true, setType(metricType, 0))))),
                expression(simpleTensor("mass", IndicesFactory.EMPTY_SIMPLE_INDICES),
                        setIndices(mass, IndicesFactory.EMPTY_SIMPLE_INDICES)));
        this.uSubs = (Expression) match.transform(tokenTransformer.transform(uPatt).toTensor());
        this.uBarSubs = (Expression) match.transform(tokenTransformer.transform(uBarPatt).toTensor());
        this.vSubs = (Expression) match.transform(tokenTransformer.transform(vPatt).toTensor());
        this.vBarSubs = (Expression) match.transform(tokenTransformer.transform(vBarPatt).toTensor());
        this.p2 = (Expression) match.transform(tokenTransformer.transform(p2Patt).toTensor());

        this.simplifyG5 = gamma5 == null ? null : new SimplifyGamma5Transformation(gammaMatrix, gamma5);
        this.expandAndEliminate = new ExpandTensorsAndEliminateTransformation(simplifications);
        this.diracSimplify = diracSimplify ? new DiracSimplifyTransformation(gammaMatrix, gamma5, new TransformationCollection(simplifications, p2)) : null;
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

            IntArrayList changed = new IntArrayList();
            PrimitiveSubgraph[] partition
                    = PrimitiveSubgraphPartition.calculatePartition(pc, matrixType);

            ArrayList<Tensor> simplified = new ArrayList<>();
            IntArrayList matched = new IntArrayList();
            IntArrayList momentums = new IntArrayList();
            gammas:
            for (PrimitiveSubgraph subgraph : partition) {
                matched.clear();
                if (subgraph.getGraphType() != GraphType.Line)
                    continue;
                SpinorType left = isSpinor(pc.get(subgraph.getPosition(0))),
                        right = isSpinor(pc.get(subgraph.getPosition(subgraph.size() - 1)));
                if (left == null && right == null)
                    continue;
                for (int i = 0; i < subgraph.size(); ++i) {
                    if (!isGamma(pc.get(subgraph.getPosition(i))))
                        continue;
                    int mIndex = withMomentum(subgraph.getPosition(i), pc, st);
                    if (mIndex != -1) {
                        Tensor co = pc.get(mIndex);
                        if (co.getIndices().size(matrixType) == 0) {
                            momentums.add(offset + mIndex);
                            if (IndexMappings.anyMappingExists(momentum, co))
                                matched.add(i);
                        }
                    }
                }
                if (matched.isEmpty())
                    continue;
                matched.sort();

                int gSize = subgraph.size();
                Tensor spinors = product.select(momentums.toArray());
                if (left != null) {
                    spinors = multiply(spinors, pc.get(subgraph.getPosition(0)));
                    --gSize;
                }
                if (right != null) {
                    spinors = multiply(spinors, pc.get(subgraph.getPosition(subgraph.size() - 1)));
                    --gSize;
                }

                Tensor moved = null;
                if (right == null || (left != null && matched.first() < subgraph.size() - matched.last())) {
                    //move left
                    Tensor[] gammas = new Tensor[gSize];
                    int i = 1;
                    for (; i <= matched.first(); ++i) {
                        Tensor r = pc.get(subgraph.getPosition(i));
                        if (!isGamma(r)) {
                            gammas = null;
                            break;
                        }
                        gammas[i - 1] = r;
                    }
                    if (gammas != null) {
                        //all ok
                        for (; i <= gSize; ++i)
                            gammas[i - 1] = pc.get(subgraph.getPosition(i));

                        moved = move(gammas, matched.first() - 1, true);
                        if (moved instanceof Sum)
                            moved = FastTensors.multiplySumElementsOnFactorAndResolveDummies((Sum) moved, spinors);
                        else moved = multiply(moved, spinors);

                        //apply substitutions
                        if (left == SpinorType.uBar)
                            moved = uBarSubs.transform(moved);
                        else
                            moved = vBarSubs.transform(moved);
                        simplified.add(moved);

                        changed.addAll(momentums);
                        changed.ensureCapacity(subgraph.size());
                        for (i = 0; i < subgraph.size(); ++i)
                            changed.add(subgraph.getPosition(i) + offset);
                    }
                }
                if (moved == null) {
                    //move right
                    Tensor[] gammas = new Tensor[gSize];
                    int lOffset = left == null ? 0 : 1;
                    int i = subgraph.size() - 2;
                    for (; i >= matched.last(); --i) {
                        Tensor r = pc.get(subgraph.getPosition(i));
                        if (!isGamma(r)) {
                            gammas = null;
                            break;
                        }
                        gammas[i - lOffset] = r;
                    }
                    if (gammas != null) {
                        //all ok
                        for (; i >= lOffset; --i)
                            gammas[i - lOffset] = pc.get(subgraph.getPosition(i));

                        moved = move(gammas, matched.last() - lOffset, false);
                        if (moved instanceof Sum)
                            moved = FastTensors.multiplySumElementsOnFactor((Sum) moved, spinors);
                        else moved = multiply(moved, spinors);

                        //apply substitutions
                        if (right == SpinorType.u)
                            moved = uSubs.transform(moved);
                        else
                            moved = vSubs.transform(moved);
                        simplified.add(moved);

                        changed.addAll(momentums);
                        changed.ensureCapacity(subgraph.size());
                        for (i = 0; i < subgraph.size(); ++i)
                            changed.add(subgraph.getPosition(i) + offset);
                    }
                }
            }
            if (changed.isEmpty())
                continue;

            simplified.add(product.remove(changed.toArray()));
            Tensor simple = expandAndEliminate.transform(multiplyAndRenameConflictingDummies(simplified));
            if (diracSimplify != null)
                simple = diracSimplify.transform(simple);
            else {
                simple = traceOfOne.transform(simple);
                simple = deltaTrace.transform(simple);
            }
            simple = p2.transform(simple);
            iterator.safeSet(transform(simple));
        }
        return iterator.result();
    }

    private static final class Holder {
        final int index, length;
        final boolean left;

        public Holder(int index, int length, boolean left) {
            this.index = index;
            this.length = length;
            this.left = left;
        }
    }

    private final Map<Holder, Tensor> cache = new HashMap<>();

    Tensor move(Tensor[] gammas, int index, boolean left) {
        if (gammas.length == 1)
            return gammas[0];
        if ((index == 0 && left) || (index == gammas.length - 1 && !left))
            return multiply(gammas);

        int numberOfGammas = gammas.length;
        Holder key = new Holder(index, numberOfGammas, left);
        Tensor tensor = cache.get(key);
        if (tensor == null)
            cache.put(key, tensor = left ? toLeft0(createLine(numberOfGammas), index) : toRight0(createLine(numberOfGammas), index));

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

    Tensor toLeft0(Tensor[] gammas, int index) {
        if (index == 0)
            return multiply(gammas);
        if (gammas.length == 1)
            return gammas[0];

        SumBuilder sb = new SumBuilder();

        Tensor metric = multiply(Complex.TWO,
                createMetricOrKronecker(gammas[index - 1].getIndices().get(metricType, 0),
                        gammas[index].getIndices().get(metricType, 0)));
        Tensor[] cadj = cutAdj(gammas, index - 1);
        Tensor adj;
        if (cadj.length == 0)
            adj = createMetricOrKronecker(gammas[index - 1].getIndices().getUpper().get(matrixType, 0),
                    gammas[index].getIndices().getLower().get(matrixType, 0));
        else if (cadj.length == 1)
            adj = cadj[0];
        else
            adj = multiply(cadj);
        adj = adj instanceof Sum ?
                multiplySumElementsOnFactor((Sum) adj, metric) : multiply(adj, metric);
        sb.put(adj);

        swapAdj(gammas, index - 1);
        sb.put(negate(move(gammas, index - 1, true)));
        return sb.build();
    }

    Tensor toRight0(Tensor[] gammas, int index) {
        if (index == gammas.length - 1)
            return multiply(gammas);

        if (gammas.length == 1)
            return gammas[0];

        SumBuilder sb = new SumBuilder();

        Tensor metric = multiply(Complex.TWO,
                createMetricOrKronecker(gammas[index].getIndices().get(metricType, 0),
                        gammas[index + 1].getIndices().get(metricType, 0)));
        Tensor[] cadj = cutAdj(gammas, index);
        Tensor adj;
        if (cadj.length == 0)
            adj = createMetricOrKronecker(gammas[index].getIndices().getUpper().get(matrixType, 0),
                    gammas[index + 1].getIndices().getLower().get(matrixType, 0));
        else if (cadj.length == 1)
            adj = cadj[0];
        else
            adj = multiply(cadj);
        adj = adj instanceof Sum ?
                multiplySumElementsOnFactor((Sum) adj, metric) : multiply(adj, metric);
        sb.put(adj);

        swapAdj(gammas, index);
        sb.put(negate(move(gammas, index + 1, false)));
        return sb.build();
    }

    private int withMomentum(int gamma,
                             ProductContent pc,
                             StructureOfContractions sc) {
        Indices indices = pc.get(gamma).getIndices();
        int j = 0;
        for (; j < indices.size(); ++j)
            if (metricType.getType() == getType(indices.get(j)))
                break;
        return getToTensorIndex(sc.contractions[gamma][j]);
    }

    private SpinorType isSpinor(Tensor st) {
        if (anyMappingExists(st, u))
            return SpinorType.u;
        else if (anyMappingExists(st, v))
            return SpinorType.v;
        else if (anyMappingExists(st, uBar))
            return SpinorType.uBar;
        else if (anyMappingExists(st, vBar))
            return SpinorType.vBar;
        else return null;
    }

    private enum SpinorType {
        u, v, uBar, vBar
    }


    private static final Parser parser;
    private static final ParseToken uPatt, vPatt, uBarPatt, vBarPatt, p2Patt;

    static {
        parser = CC.current().getParseManager().getParser();
        uBarPatt = parser.parse("cu_a'*G^aa'_b'*p_a = cu_b'*mass");
        vBarPatt = parser.parse("cv_a'*G^aa'_b'*p_a = -cv_b'*mass");
        uPatt = parser.parse("G^aa'_b'*p_a*u^b'= u^a'*mass");
        vPatt = parser.parse("G^aa'_b'*p_a*v^b'= -v^a'*mass");
        p2Patt = parser.parse("p_a*p^a = mass**2");
    }
}
