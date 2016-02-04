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
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
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
 * Simplifies contractions of gammas with same momentums like k^a*k^b*G_a*G_b
 *
 * @author Stanislav Poslavsky
 */
final class DiracSimplify0 extends AbstractFeynCalcTransformation {
    DiracSimplify0(DiracOptions options) {
        super(options, IDENTITY);
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "DiracSimplify0";
    }

    @Override
    protected Tensor transformLine(ProductOfGammas pg, IntArrayList modifiedElements) {
        assert pg.g5Positions.size() == 0 || (pg.g5Positions.size() == 1 && pg.g5Positions.first() == pg.length - 1)
                : "G5s are not simplified";

        int length = pg.length;
        if (pg.g5Positions.size() == 1)
            --length;

        ProductContent pc = pg.pc;
        StructureOfContractions sc = pc.getStructureOfContractions();

        int numberOfCouples = 0;
        List<Element> couples = new ArrayList<>();
        out:
        for (int i = 0; i < length; ++i) {
            Element contraction = getContraction(i, pg.gPositions.get(i), pc, sc);
            if (contraction == null)
                continue;
            Tensor t1 = pc.get(contraction.tIndex1);
            if (!(t1 instanceof SimpleTensor) || t1.getIndices().size() != 1)
                continue;
            for (Element couple : couples)
                if (match(pc, couple, contraction)) {
                    if (couple.coupled())
                        continue;
                    couple.couple(contraction);
                    ++numberOfCouples;
                    continue out;
                }
            couples.add(contraction);
        }

        //no any couples
        if (numberOfCouples == 0)
            return null;

        Element[] els = new Element[numberOfCouples];
        for (Element couple : couples)
            if (couple.coupled())
                els[--numberOfCouples] = couple;

        Arrays.sort(els);

        List<Tensor> ordered = new ArrayList<>();
        int[] mask = new int[length];
        for (Element el : els) {
            modifiedElements.addAll(el.tIndex1, el.tIndex2);
            if (!el.available(mask)) {
                ordered.add(pc.get(el.tIndex1));
                ordered.add(pc.get(el.tIndex2));
                continue;
            }
            Tensor simplified = simplify(el, pg.gPositions, pc);
            if (simplified != null) {
                ordered.add(simplified);
                el.cover(mask);
            }
        }
        for (int i = 0; i < mask.length; i++)
            if (mask[i] != -1)
                ordered.add(pc.get(pg.gPositions.get(i)));

        Tensor result = expandAndEliminate.transform(multiplyAndRenameConflictingDummies(ordered));
        if (pg.g5Positions.size() == 1) {
            Tensor g5 = pc.get(pg.gPositions.get(pg.g5Positions.first()));
            if (result instanceof Sum)
                result = FastTensors.multiplySumElementsOnFactorAndResolveDummies((Sum) result, g5);
            else
                result = multiplyAndRenameConflictingDummies(result, g5);
        }

        return transform(result);
    }

    private Tensor simplify(Element el, IntArrayList gPositions, ProductContent pc) {
        Tensor[] gammas = new Tensor[el.gIndex2 - el.gIndex1 + 1];
        for (int i = el.gIndex1; i <= el.gIndex2; ++i) {
            gammas[i - el.gIndex1] = pc.get(gPositions.get(i));
            if (!isGamma(pc.get(gPositions.get(i))))
                return null;//can not simplify
        }

        Tensor r = order(gammas);
        Tensor m = multiply(pc.get(el.tIndex1), pc.get(el.tIndex2));
        if (r instanceof Sum)
            r = FastTensors.multiplySumElementsOnFactor((Sum) r, m);
        else r = multiply(r, m);
        r = expandAndEliminate.transform(r);
        r = deltaTraces.transform(r);
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
            assert tIndex2 == -1;
            tIndex2 = o.tIndex1;
            gIndex2 = o.gIndex1;
            assert gIndex2 > gIndex1;
        }

        boolean coupled() {
            return tIndex2 != -1;
        }

        void cover(int[] mask) {
            for (int i = gIndex1; i <= gIndex2; ++i)
                mask[i] = -1;
        }

        int length() {
            return gIndex2 - gIndex1;
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
