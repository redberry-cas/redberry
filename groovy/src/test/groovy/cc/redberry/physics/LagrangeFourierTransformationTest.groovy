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
package cc.redberry.physics

import cc.redberry.core.context.CC
import cc.redberry.groovy.Redberry

import static cc.redberry.groovy.RedberryPhysics.LagrangeFourier

/**
 * Created by poslavsky on 06/01/16.
 */
class LagrangeFourierTransformationTest extends GroovyTestCase {
    void test1() {
        use(Redberry) {
            CC.parserAllowsSameVariance = true
            def t = 'f~(1)_abc[x_a]*f~(1)_abc[x_a]*f~(1)_pqr[x_a]'.t
            println LagrangeFourier >> t

            t = 'f_a[x_a]*f_a[x_a]'.t
            println LagrangeFourier >> t

            t = 'f[x_a]**2'.t
            println LagrangeFourier >> t

            t = 'f[x_a]**2 + f~(1)_abc[x_a]*f~(1)_abc[x_a]'.t
            println LagrangeFourier >> t
        }

    }
}
