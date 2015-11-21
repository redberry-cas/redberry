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
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.ProductContent;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.options.Creator;
import cc.redberry.core.transformations.options.Options;
import cc.redberry.core.utils.IntArrayList;

import java.util.ArrayList;
import java.util.List;

import static cc.redberry.core.indices.IndicesFactory.createSimple;
import static cc.redberry.core.indices.IndicesUtils.raise;
import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SimplifyGamma5Transformation extends AbstractTransformationWithGammas {
    public SimplifyGamma5Transformation(SimpleTensor gammaMatrix, SimpleTensor gamma5) {
        super(gammaMatrix, gamma5, null, null, null);
    }

    @Creator
    public SimplifyGamma5Transformation(@Options DiracOptions options) {
        this(options.gammaMatrix, options.gamma5);
    }

    @Override
    public Tensor transform(Tensor tensor) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(tensor);
        Tensor current;
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
            out:
            for (PrimitiveSubgraph subgraph : partition) {
                if (subgraph.getGraphType() != GraphType.Cycle && subgraph.getGraphType() != GraphType.Line)
                    continue;

                if (subgraph.size() < 2)
                    continue;

                int g5 = 0;
                for (int i = 0; i < subgraph.size(); ++i)
                    if (isGamma5(pc.get(subgraph.getPosition(i))))
                        ++g5;
                if (g5 == 0)
                    continue;
                if (g5 == 1) {
                    if (subgraph.getGraphType() == GraphType.Cycle)
                        continue;
                    //check g5 is not last
                    for (int i = subgraph.size() - 1; i >= 0; --i)
                        if (isGamma(pc.get(subgraph.getPosition(i))))
                            break;
                        else if (isGamma5(pc.get(subgraph.getPosition(i))))
                            continue out;
                }

                List<Tensor> gammas = new ArrayList<>();
                for (int i = 0; i < subgraph.size(); ++i) {
                    for (; i < subgraph.size(); ++i) {
                        if (!isGammaOrGamma5(pc.get(subgraph.getPosition(i))))
                            break;
                        else {
                            positionsOfGammas.add(offset + subgraph.getPosition(i));
                            gammas.add(pc.get(subgraph.getPosition(i)));
                        }
                    }
                    if (!gammas.isEmpty())
                        ordered.add(simplifyProduct(gammas));
                    gammas.clear();
                }
            }
            if (positionsOfGammas.isEmpty())
                continue;

            ordered.add(product.remove(positionsOfGammas.toArray()));
            iterator.set(multiply(ordered));
        }
        return iterator.result();
    }

    private Tensor simplifyProduct(List<Tensor> gammas) {
        if (gammas.size() == 0)
            return Complex.ONE;
        else if (gammas.size() == 1)
            return gammas.get(0);
        int upper = gammas.get(0).getIndices().getUpper().get(matrixType, 0),
                lower = gammas.get(gammas.size() - 1).getIndices().getLower().get(matrixType, 0);
        int initialSize = gammas.size();
        boolean sign = false;
        int dummy = -1;
        for (int i = gammas.size() - 1; i >= 0; --i) {
            if (isGamma5(gammas.get(i))) {
                sign ^= (gammas.size() - i) % 2 == 0;
                dummy = del(gammas, i);
            }
        }
        if ((initialSize - gammas.size()) % 2 == 1) {
            //adding last gamma
            if (gammas.isEmpty())
                gammas.add(simpleTensor(gamma5Name,
                        createSimple(null, upper, lower)));
            else {
                Tensor t = gammas.get(gammas.size() - 1);
                gammas.set(gammas.size() - 1, setLowerMatrixIndex((SimpleTensor) t, dummy));
                gammas.add(simpleTensor(gamma5Name,
                        createSimple(null, raise(dummy), t.getIndices().getLower().get(matrixType, 0))));
            }
        }
        Tensor r = multiply(gammas);
        if (sign) r = negate(r);
        return r;
    }

    private int del(List<Tensor> arr, int i) {
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
}
