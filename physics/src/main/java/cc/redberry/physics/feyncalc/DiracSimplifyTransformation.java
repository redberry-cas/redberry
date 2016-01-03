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
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.options.Creator;
import cc.redberry.core.transformations.options.Options;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class DiracSimplifyTransformation extends AbstractTransformationWithGammas {
    private final SimplifyGamma5Transformation simplify5;
    private final DiracSimplify0 ds0;
    private final DiracSimplify1 ds1;

    @Creator
    public DiracSimplifyTransformation(@Options DiracOptions options) {
        super(options);
        this.simplify5 = new SimplifyGamma5Transformation(options);
        this.ds0 = new DiracSimplify0(options);
        this.ds1 = new DiracSimplify1(options);
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

            original = simplify5.transform(original);
            original = ds0.transform(original);
            original = ds1.transform(original);
            iterator.safeSet(original);
        }
        return iterator.result();
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "DiracSimplify";
    }
}
