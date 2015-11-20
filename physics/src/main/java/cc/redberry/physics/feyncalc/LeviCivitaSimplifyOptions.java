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

import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.options.Option;

import static cc.redberry.core.tensor.Tensors.parseSimple;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class LeviCivitaSimplifyOptions {
    @Option(name = "LeviCivita", index = 0)
    public SimpleTensor leviCivita = parseSimple("e_abcd");

    @Option(name = "Minkowski", index = 1)
    public boolean minkowskiSpace = true;

    @Option(name = "Simplifications", index = 2)
    public Transformation simplifications = Transformation.IDENTITY;

    @Option(name = "OverallSimplifications", index = 3)
    public Transformation overallSimplifications = Transformation.IDENTITY;

    public LeviCivitaSimplifyOptions() {}

    public LeviCivitaSimplifyOptions(boolean minkowskiSpace) {this.minkowskiSpace = minkowskiSpace;}
}
