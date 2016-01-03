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
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.options.Creator;
import cc.redberry.core.transformations.options.Options;
import cc.redberry.core.utils.IntArrayList;

import java.util.List;

import static cc.redberry.core.indices.IndicesFactory.createSimple;
import static cc.redberry.core.indices.IndicesUtils.raise;
import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SimplifyGamma5Transformation extends AbstractFeynCalcTransformation {
    @Creator
    public SimplifyGamma5Transformation(@Options DiracOptions options) {
        super(options.setExpand(IDENTITY), IDENTITY);
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "SimplifyGamma5";
    }

    @Override
    protected Tensor transformLine(ProductOfGammas pg, IntArrayList modifiedElements) {
        //single matrix
        if (pg.length == 1)
            return null;
        //no g5s
        if (pg.g5Positions.isEmpty())
            return null;

        if (pg.g5Positions.size() == 1) {
            //single g5 in trace -> nothing to do
            if (pg.graphType == GraphType.Cycle)
                return null;
            //single g5 at the last position -> nothing to do
            if (pg.g5Positions.first() == pg.length - 1)
                return null;
        }

        if (pg.g5Positions.size() == pg.length) {//only g5s
            if (pg.length % 2 == 0) {
                //all gammas cancel
                if (pg.graphType == GraphType.Cycle)
                    return traceOfOne.get(1);
                else
                    return createMetricOrKronecker(pg.getIndices().getFree());
            } else {
                assert pg.graphType != GraphType.Cycle;//this should be already processed
                return setMatrixIndices((SimpleTensor) pg.pc.get(pg.gPositions.first()), pg.getIndices().getFree());
            }
        }

        return simplifyProduct(pg.toList());
    }

    private Tensor simplifyProduct(List<Tensor> gammas) {
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
}
