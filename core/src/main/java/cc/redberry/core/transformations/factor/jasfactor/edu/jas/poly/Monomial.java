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

import java.util.Map;


/**
 * Monomial class.
 * Represents pairs of exponent vectors and coefficients.
 * Adaptor for Map.Entry.
 *
 * @author Heinz Kredel
 */

public final class Monomial<C extends RingElem<C>> {

    /**
     * Exponent of monomial.
     */
    public final ExpVector e;


    /**
     * Coefficient of monomial.
     */
    public final C c;


    /**
     * Constructor of monomial.
     *
     * @param me a MapEntry.
     */
    public Monomial(Map.Entry<ExpVector, C> me) {
        this(me.getKey(), me.getValue());
    }


    /**
     * Constructor of monomial.
     *
     * @param e exponent.
     * @param c coefficient.
     */
    public Monomial(ExpVector e, C c) {
        this.e = e;
        this.c = c;
    }


    /**
     * Getter for exponent.
     *
     * @return exponent.
     */
    public ExpVector exponent() {
        return e;
    }


    /**
     * Getter for coefficient.
     *
     * @return coefficient.
     */
    public C coefficient() {
        return c;
    }

    /**
     * String representation of Monomial.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return c.toString() + " " + e.toString();
    }

}
