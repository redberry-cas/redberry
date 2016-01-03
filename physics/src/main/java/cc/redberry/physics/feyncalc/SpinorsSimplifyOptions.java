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
import cc.redberry.core.transformations.options.Option;

import static cc.redberry.core.tensor.Tensors.parseSimple;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SpinorsSimplifyOptions extends DiracOptions {
    @Option(name = "u", index = 9)
    public SimpleTensor u;

    @Option(name = "v", index = 10)
    public SimpleTensor v;

    @Option(name = "uBar", index = 11)
    public SimpleTensor uBar;

    @Option(name = "vBar", index = 12)
    public SimpleTensor vBar;

    @Option(name = "Momentum", index = 13)
    public SimpleTensor momentum;

    @Option(name = "Mass", index = 14)
    public SimpleTensor mass;

    @Option(name = "DiracSimplify", index = 15)
    public boolean doDiracSimplify = false;

    public SpinorsSimplifyOptions() {}

    public SpinorsSimplifyOptions(String u, String v, String uBar, String vBar,
                                  String momentum, String mass) {
        this.u = u == null ? null : parseSimple(u);
        this.v = v == null ? null : parseSimple(v);
        this.uBar = uBar == null ? null : parseSimple(uBar);
        this.vBar = vBar == null ? null : parseSimple(vBar);
        this.momentum = parseSimple(momentum);
        this.mass = parseSimple(mass);
    }
}
