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

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.ExpandAndEliminateTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.options.IOptions;
import cc.redberry.core.transformations.options.Option;
import cc.redberry.core.utils.TensorUtils;

import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.transformations.Transformation.IDENTITY;


/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class DiracOptions implements IOptions, Cloneable {
    @Option(name = "Gamma", index = 0)
    public SimpleTensor gammaMatrix = Tensors.parseSimple("G_a");

    @Option(name = "Gamma5", index = 1)
    public SimpleTensor gamma5 = Tensors.parseSimple("G5");

    @Option(name = "LeviCivita", index = 2)
    public SimpleTensor leviCivita = Tensors.parseSimple("e_abcd");

    @Option(name = "Dimension", index = 3)
    public Tensor dimension = Complex.FOUR;

    @Option(name = "TraceOfOne", index = 4)
    public Tensor traceOfOne;

    @Option(name = "Simplifications", index = 5)
    public Transformation simplifications = IDENTITY;

    @Option(name = "Minkowski", index = 6)
    public boolean minkowskiSpace = true;

    @Option(name = "LeviCivitaSimplify", index = 7)
    public Transformation simplifyLeviCivita = null;

    @Option(name = "ExpandAndEliminate", index = 8)
    public Transformation expandAndEliminate = null;

    @Option(name = "Cache", index = 9)
    public boolean cache = false;

    public DiracOptions() {}

    @Override
    public DiracOptions clone() {
        try {
            return (DiracOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    protected DiracOptions setExpand(Transformation expand) {
        DiracOptions options = clone();
        options.expandAndEliminate = expand;
        return options;
    }

    boolean created = false;

    @Override
    public void triggerCreate() {
        created = true;
        if (traceOfOne == null)
            traceOfOne = guessTraceOfOne(dimension);
        if (expandAndEliminate == null)
            expandAndEliminate = new ExpandAndEliminateTransformation(simplifications);
        if (simplifyLeviCivita == null)
            simplifyLeviCivita = new LeviCivitaSimplifyTransformation(leviCivita, minkowskiSpace, simplifications, IDENTITY, dimension);
    }

    protected static Tensor guessTraceOfOne(Tensor dimension) {
        if (TensorUtils.isIntegerOdd(dimension))
            return pow(Complex.TWO, divide(subtract(dimension, Complex.ONE), Complex.TWO));
        else return pow(Complex.TWO, divide(dimension, Complex.TWO));
    }
}
