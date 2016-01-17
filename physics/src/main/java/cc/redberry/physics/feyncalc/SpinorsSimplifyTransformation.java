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
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import cc.redberry.core.transformations.options.Creator;
import cc.redberry.core.transformations.options.Options;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.IntArrayList;

import java.util.*;

import static cc.redberry.core.indexmapping.IndexMappings.anyMappingExists;
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
public final class SpinorsSimplifyTransformation extends AbstractTransformationWithGammas {
    private final SimpleTensor u, v, uBar, vBar, momentum, mass;
    private final Transformation uSubs, vSubs, uBarSubs, vBarSubs, p2;
    private final Transformation simplifyG5;
    private final Transformation ortohonality;
    private final Transformation diracSimplify;

    @Creator
    public SpinorsSimplifyTransformation(@Options SpinorsSimplifyOptions options) {
        super(options);
        checkSpinorNotation(options.u, false);
        checkSpinorNotation(options.v, false);
        checkSpinorNotation(options.uBar, true);
        checkSpinorNotation(options.vBar, true);

        this.u = options.u;
        this.v = options.v;
        this.uBar = options.uBar;
        this.vBar = options.vBar;
        this.momentum = options.momentum;
        this.mass = options.mass;

        this.uSubs = createSubs(u, false);
        this.uBarSubs = createBarSubs(uBar, false);
        this.vSubs = createSubs(v, true);
        this.vBarSubs = createBarSubs(vBar, true);
        this.p2 = createP2Subs();
        this.simplifyG5 = options.gamma5 == null ? IDENTITY : new SimplifyGamma5Transformation(options);
        this.diracSimplify = options.doDiracSimplify ? new DiracSimplifyTransformation(options) : IDENTITY;

        List<Transformation> ortoh = new ArrayList<>();
        Expression[] ort = createOrtIdentities(uBar, v);
        if (ort != null) ortoh.addAll(Arrays.asList(ort));
        ort = createOrtIdentities(vBar, u);
        if (ort != null) ortoh.addAll(Arrays.asList(ort));
        this.ortohonality = new TransformationCollection(ortoh);
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "SpinorsSimplify";
    }

    private void checkSpinorNotation(SimpleTensor spinor, boolean bar) {
        if (spinor == null)
            return;
        SimpleIndices m = spinor.getIndices().getOfType(matrixType);
        if (m.size() != 1 || bar == getState(m.get(0)))
            throw new IllegalArgumentException("Illegal notation for spinor " + spinor);
    }

    private Expression createBarSubs(SimpleTensor spinor, boolean negate) {
        if (spinor == null) return null;
        int dummy = spinor.getIndices().get(matrixType, 0), free = dummy + 1;
        SimpleTensor gamma = simpleTensor(gammaName, createSimple(null,
                setState(true, dummy), free, setType(metricType, 0)));
        SimpleTensor mom = setIndices(momentum, createSimple(null, setState(true, setType(metricType, 0))));
        SimpleTensor rhs = setMatrixIndices0(spinor, free);
        return expression(multiply(spinor, gamma, mom), negate ? negate(multiply(mass, rhs)) : multiply(mass, rhs));
    }

    private Expression createSubs(SimpleTensor spinor, boolean negate) {
        if (spinor == null) return null;
        int dummy = setState(false, spinor.getIndices().get(matrixType, 0)), free = setState(true, dummy + 1);
        SimpleTensor gamma = simpleTensor(gammaName, createSimple(null,
                free, dummy, setType(metricType, 0)));
        SimpleTensor mom = setIndices(momentum, createSimple(null, setState(true, setType(metricType, 0))));
        SimpleTensor rhs = setMatrixIndices0(spinor, free);
        return expression(multiply(spinor, gamma, mom), negate ? negate(multiply(mass, rhs)) : multiply(mass, rhs));
    }

    private Expression createP2Subs() {
        return expression(multiply(momentum, setIndices(momentum, momentum.getIndices().getInverted())), pow(mass, 2));
    }

    private Expression[] createOrtIdentities(SimpleTensor bar, SimpleTensor spinor) {
        if (bar == null || spinor == null) return null;
        int dummy = setState(false, bar.getIndices().get(matrixType, 0));
        Tensor lhs0 = multiply(setMatrixIndices0(bar, dummy), setMatrixIndices0(spinor, inverseIndexState(dummy)));
        Tensor lhs1 = multiply(
                setMatrixIndices0(bar, dummy),
                simpleTensor(gammaName, createSimple(null, inverseIndexState(dummy), dummy + 1, setType(metricType, 0))),
                setIndices(momentum, createSimple(null, setState(true, setType(metricType, 0)))),
                setMatrixIndices0(spinor, inverseIndexState(dummy + 1)));
        return new Expression[]{expression(lhs0, Complex.ZERO), expression(lhs1, Complex.ZERO)};
    }

    private SimpleTensor setMatrixIndices0(SimpleTensor tensor, int... indices) {
        int[] newIndices = new int[tensor.getIndices().size()];
        int j = 0;
        for (int i = 0; i < tensor.getIndices().size(); ++i)
            if (getType(tensor.getIndices().get(i)) == matrixType.getType())
                newIndices[i] = indices[j++];
            else newIndices[i] = tensor.getIndices().get(i);
        return setIndices(tensor, createSimple(null, newIndices));
    }

    @Override
    public Tensor transform(Tensor tensor) {
        tensor = ortohonality.transform(tensor);
        SubstitutionIterator iterator = new SubstitutionIterator(tensor);
        Tensor current;
        while ((current = iterator.next()) != null) {
            if (!(current instanceof Product))
                continue;
            if (current.getIndices().size(matrixType) == 0)
                continue;
            if (!containsGammaOr5Matrices(current))
                continue;
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
                        if (!isGammaOrGamma5(r)) {
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
                        if (!isGammaOrGamma5(r)) {
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
            simple = diracSimplify.transform(simple);
            simple = traceOfOne.transform(simple);
            simple = deltaTrace.transform(simple);
            simple = p2.transform(simple);
            iterator.safeSet(transform(simple));
        }
        return iterator.result();
    }

    private static final class Holder {
        final int index, length;
        final IntArrayList g5s;
        final boolean left;

        public Holder(int index, int length, IntArrayList g5s, boolean left) {
            this.index = index;
            this.length = length;
            this.g5s = g5s;
            this.left = left;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Holder holder = (Holder) o;

            return index == holder.index
                    && length == holder.length
                    && left == holder.left
                    && g5s.equals(holder.g5s);
        }

        @Override
        public int hashCode() {
            int result = index;
            result = 31 * result + length;
            result = 31 * result + (left ? 1 : 0);
            result = 31 * result + g5s.hashCode();
            return result;
        }
    }

    private final Map<Holder, Tensor> cache = new HashMap<>();

    Tensor move(Tensor[] gammas, int index, boolean left) {
        if (gammas.length == 1)
            return gammas[0];
        if ((index == 0 && left) || (index == gammas.length - 1 && !left))
            return multiply(gammas);
        Tensor gPart, rest;
        if (left) {
            gPart = move0(Arrays.copyOfRange(gammas, 0, index + 1), index, left);
            rest = multiply(Arrays.copyOfRange(gammas, index + 1, gammas.length));
        } else {
            gPart = move0(Arrays.copyOfRange(gammas, index, gammas.length), 0, left);
            rest = multiply(Arrays.copyOfRange(gammas, 0, index));
        }

        if (gPart instanceof Sum)
            gPart = FastTensors.multiplySumElementsOnFactorAndResolveDummies((Sum) gPart, rest);
        else
            gPart = multiplyAndRenameConflictingDummies(gPart, rest);
        return eliminate(gPart);
    }

    Tensor move0(Tensor[] gammas, int index, boolean left) {
        if (gammas.length == 1)
            return gammas[0];
        if ((index == 0 && left) || (index == gammas.length - 1 && !left))
            return multiply(gammas);

        int numberOfGammas = gammas.length;
        IntArrayList iFrom = new IntArrayList(numberOfGammas + 2),
                iTo = new IntArrayList(numberOfGammas + 2),
                g5s = new IntArrayList();

        for (int i = 0; i < numberOfGammas; ++i) {
            if (isGamma5(gammas[i]))
                g5s.add(i);
            else {
                iFrom.add(setType(metricType, i));
                iTo.add(gammas[i].getIndices().get(metricType, 0));
            }
        }
        iFrom.add(setType(matrixType, 0) | 0x80000000);
        iTo.add(gammas[0].getIndices().getUpper().get(matrixType, 0));
        iFrom.add(setType(matrixType, numberOfGammas));
        iTo.add(gammas[numberOfGammas - 1].getIndices().getLower().get(matrixType, 0));

        Holder key = new Holder(index, numberOfGammas, g5s, left);
        Tensor tensor = cache.get(key);
        if (tensor == null)
            cache.put(key, tensor = left ?
                    toLeft0(createLine(numberOfGammas, g5s), index)
                    : toRight0(createLine(numberOfGammas, g5s), index));

        return eliminate(ApplyIndexMapping.applyIndexMapping(tensor,
                new Mapping(iFrom.toArray(), iTo.toArray())));
    }

    Tensor toLeft0(Tensor[] gammas, int index) {
        if (index == 0)
            return multiply(gammas);
        if (gammas.length == 1)
            return gammas[0];

        if (isGamma5(gammas[index]) && isGamma5(gammas[index - 1])) {
            swapAdj(gammas, index - 1);
            return toLeft0(gammas, index - 1);
        } else if (isGamma5(gammas[index]) || isGamma5(gammas[index - 1])) {
            swapAdj(gammas, index - 1);
            return negate(toLeft0(gammas, index - 1));
        } else {
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
            sb.put(negate(move0(gammas, index - 1, true)));
            return sb.build();
        }
    }

    Tensor toRight0(Tensor[] gammas, int index) {
        if (index == gammas.length - 1)
            return multiply(gammas);

        if (gammas.length == 1)
            return gammas[0];

        if (isGamma5(gammas[index]) && isGamma5(gammas[index + 1])) {
            swapAdj(gammas, index);
            return toRight0(gammas, index + 1);
        } else if (isGamma5(gammas[index]) || isGamma5(gammas[index + 1])) {
            swapAdj(gammas, index);
            return negate(toRight0(gammas, index + 1));
        } else {
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
            sb.put(negate(move0(gammas, index + 1, false)));
            return sb.build();
        }
    }

    private Tensor[] createLine(final int length, final IntArrayList g5s) {
        Tensor[] gammas = new Tensor[length];
        int matrixIndex, metricIndex = 0, u = matrixIndex = setType(matrixType, 0);
        int j = 0;
        for (int i = 0; i < length; ++i) {
            if (j < g5s.size() && g5s.get(j) == i) {
                gammas[i] = Tensors.simpleTensor(gamma5Name,
                        createSimple(null,
                                u | 0x80000000,
                                u = ++matrixIndex));
                ++j;
            } else
                gammas[i] = Tensors.simpleTensor(gammaName,
                        createSimple(null,
                                u | 0x80000000,
                                u = ++matrixIndex,
                                setType(metricType, metricIndex++)));
        }
        return gammas;
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
        if (u != null && anyMappingExists(st, u))
            return SpinorType.u;
        else if (v != null && anyMappingExists(st, v))
            return SpinorType.v;
        else if (uBar != null && anyMappingExists(st, uBar))
            return SpinorType.uBar;
        else if (vBar != null && anyMappingExists(st, vBar))
            return SpinorType.vBar;
        else return null;
    }

    private enum SpinorType {
        u, v, uBar, vBar
    }
}
