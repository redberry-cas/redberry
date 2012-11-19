/*
 * JAS: Java Algebra System.
 *
 * Copyright (c) 2000-2012:
 *    Heinz Kredel   <kredel@rz.uni-mannheim.de>
 *
 * This file is part of Java Algeba System (JAS).
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

import java.io.Serializable;
import java.util.Comparator;


/**
 * Comparator for polynomials.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel.
 */
public class PolynomialComparator<C extends RingElem<C>> implements Serializable,
        Comparator<GenPolynomial<C>> {


    public final TermOrder tord;


    public final boolean reverse;


    /**
     * Constructor.
     *
     * @param t       TermOrder.
     * @param reverse flag if reverse ordering is requested.
     */
    public PolynomialComparator(TermOrder t, boolean reverse) {
        tord = t;
        this.reverse = reverse;
    }


    /**
     * Compare polynomials.
     *
     * @param p1 first polynomial.
     * @param p2 second polynomial.
     * @return 0 if ( p1 == p2 ), -1 if ( p1 < p2 ) and +1 if ( p1 > p2 ).
     */
    public int compare(GenPolynomial<C> p1, GenPolynomial<C> p2) {
        // check if p1.tord = p2.tord = tord ?
        int s = p1.compareTo(p2);
        if (reverse) {
            return -s;
        }
        return s;
    }


    /**
     * Equals test of comparator.
     *
     * @param o other object.
     * @return true if this = o, else false.
     */
    @Override
    public boolean equals(Object o) {
        PolynomialComparator pc = null;
        try {
            pc = (PolynomialComparator) o;
        } catch (ClassCastException ignored) {
            return false;
        }
        if (pc == null) {
            return false;
        }
        return tord.equals(pc.tord);
    }


    /**
     * Hash code for this PolynomialComparator.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return tord.hashCode();
    }


    /**
     * toString.
     */
    @Override
    public String toString() {
        return "PolynomialComparator(" + tord + ")";
    }

}
