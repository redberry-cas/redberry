/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.IntArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class AbstractFeynCalcTransformation extends AbstractTransformationWithGammas {
    protected final Transformation preprocessor;

    protected AbstractFeynCalcTransformation(DiracOptions options, Transformation preprocessor) {
        super(options);
        this.preprocessor = preprocessor;
    }

    @Override
    public Tensor transform(Tensor tensor) {
        SubstitutionIterator iterator = new SubstitutionIterator(tensor);
        Tensor original;
        out:
        while ((original = iterator.next()) != null) {
            if (!(original instanceof Product))
                continue;
            if (original.getIndices().size(matrixType) == 0)
                continue;
            if (!containsGammaOr5Matrices(original))
                continue;

            //preparing product, e.g. simplify all gamma5s
            Tensor current = preprocessor.transform(original);
            if (!(current instanceof Product)) {
                iterator.safeSet(transform(current));
                continue;
            }

            Product product = (Product) current;
            IntArrayList modifiedElements = new IntArrayList();
            List<Tensor> processed = new ArrayList<>();

            ProductOfGammas.It gammas = new ProductOfGammas.It(gammaName, gamma5Name, product, matrixType, graphFilter());
            ProductOfGammas line;
            while ((line = gammas.take()) != null) {
                if (isZeroTrace(line)) {
                    iterator.set(Complex.ZERO);
                    continue out;
                }

                Tensor tr = transformLine(line, modifiedElements);
                if (tr == null)
                    continue;

                processed.add(tr);
                modifiedElements.addAll(line.gPositions);
            }
            if (processed.isEmpty()) {
                iterator.safeSet(current);//no change if current is the same
                continue;
            }


            modifiedElements.sumWith(product.sizeOfIndexlessPart());
            Tensor result = product.remove(modifiedElements.toArray());
            processed.add(result);
            result = expandAndEliminate.transform(
                    Tensors.multiplyAndRenameConflictingDummies(processed));
            result = traceOfOne.transform(result);
            result = deltaTrace.transform(result);
            iterator.safeSet(result);
        }
        return iterator.result();
    }

    protected Indicator<GraphType> graphFilter() {
        return ProductOfGammas.It.defaultFilter;
    }

    protected final boolean isZeroTrace(ProductOfGammas pg) {
        return pg.graphType == GraphType.Cycle &&
                (((pg.length - pg.g5Positions.size()) % 2) == 1 ||
                        (pg.g5Positions.size() % 2 == 1 && (pg.length - pg.g5Positions.size()) < 4));
    }

    protected abstract Tensor transformLine(ProductOfGammas pg, IntArrayList modifiedElements);
}
