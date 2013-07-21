/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
package cc.redberry.groovy

import cc.redberry.core.indexmapping.IndexMappings
import org.junit.Test

import static cc.redberry.core.tensor.Tensors.setAntiSymmetric
import static cc.redberry.core.tensor.Tensors.setSymmetric

class RedberryTest {

    @Test
    public void testOr() throws Exception {
        use(Redberry) {
            def x = 'x = y'.t,
                y = 'y = x'.t,
                z = 'z = x + y'.t,
                t = 'x*y**2'.t

            def and = x & y & z,
                or = x | y | z

            assert and >> t == 'x**3'.t
            assert or >> t == 'x**2*y'.t

            t = 'x + y + z'.t
            assert and >> t == '3*x + y'.t
            assert or >> t == '2*x + 2*y'.t
        }
    }

    @Test
    public void testGet() {
        use(Redberry) {
            def a = 'Sin[Sin[x]*Sin[y]]'.t
            assert a[0] == 'Sin[x]*Sin[y]'.t
            assert (a[0, 0, 0] == 'x'.t || a[0, 0, 0] == 'y'.t)

            a = 'a*b*c*F_mn*G_ab'.t
            assert a[0..3] == ('a*b*c'.t.toArray() as List)
            assert a[0..5] == (a.toArray() as List)
            assert a[3..5] == ('F_mn*G_ab'.t.toArray() as List)
        }
    }

//    @Test
//    public void testMapping1() {
//        use(Redberry) {
//            println 'yyyy'
//            setAntiSymmetric 'f_qwruip'
//            println 'xxxx'
//            def from = 'f_qwruio'.t, to = 'f_qwruio'.t
//            println IndexMappings.getFirst(from, to)
//            def mappings = from % to
//
//            println mappings.first
////            mappings.each { println it }
//            println mappings >> from
//            println mappings.first >> from
//            println 'xui'
//            println mappings.find { it.sign }
//        }
//    }
}
