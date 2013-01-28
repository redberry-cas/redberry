/*
 * JAS: Java Algebra System.
 *
 * Copyright (c) 2000-2013:
 *    Heinz Kredel   <kredel@rz.uni-mannheim.de>
 *
 * This file is part of Java Algebra System (JAS).
 *
 * JAS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * JAS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAS. If not, see <http://www.gnu.org/licenses/>.
 */

/*
* $Id$
*/

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly;

import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;


/**
 * Iterator over monomials of a polynomial.
 * Adaptor for val.entrySet().iterator().
 *
 * @author Heinz Kredel
 */

public class PolyIterator<C extends RingElem<C>>
        implements Iterator<Monomial<C>> {


    /**
     * Internal iterator over polynomial map.
     */
    protected final Iterator<Map.Entry<ExpVector, C>> ms;


    /**
     * Constructor of polynomial iterator.
     *
     * @param m SortetMap of a polynomial.
     */
    public PolyIterator(SortedMap<ExpVector, C> m) {
        ms = m.entrySet().iterator();
    }


    /**
     * Test for availability of a next monomial.
     *
     * @return true if the iteration has more monomials, else false.
     */
    public boolean hasNext() {
        return ms.hasNext();
    }


    /**
     * Get next monomial element.
     *
     * @return next monomial.
     */
    public Monomial<C> next() {
        return new Monomial<>(ms.next());
    }


    /**
     * Remove the last monomial returned from underlying set if allowed.
     */
    public void remove() {
        ms.remove();
    }

}
