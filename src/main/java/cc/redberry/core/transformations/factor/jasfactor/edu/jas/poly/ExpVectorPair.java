/*
 * JAS: Java Algebra System.
 *
 * Copyright (c) 2000-2012:
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

import java.io.Serializable;


/**
 * ExpVectorPair
 * implements pairs of exponent vectors for S-polynomials.
 * Objects of this class are immutable.
 *
 * @author Heinz Kredel
 */


public class ExpVectorPair implements Serializable {

    private final ExpVector e1;
    private final ExpVector e2;


    /**
     * Constructors for ExpVectorPair.
     *
     * @param e first part.
     * @param f second part.
     */
    public ExpVectorPair(ExpVector e, ExpVector f) {
        e1 = e;
        e2 = f;
    }


    /**
     * @return first part.
     */
    public ExpVector getFirst() {
        return e1;
    }


    /**
     * @return second part.
     */
    public ExpVector getSecond() {
        return e2;
    }


    /**
     * toString.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("ExpVectorPair[");
        s.append(e1.toString());
        s.append(",");
        s.append(e2.toString());
        s.append("]");
        return s.toString();
    }


    /**
     * equals.
     *
     * @param B other.
     * @return true, if this == b, else false.
     */
    @Override
    public boolean equals(Object B) {
        if (!(B instanceof ExpVectorPair)) return false;
        return equals((ExpVectorPair) B);
    }


    /**
     * equals.
     *
     * @param b other.
     * @return true, if this == b, else false.
     */
    public boolean equals(ExpVectorPair b) {
        boolean t = e1.equals(b.getFirst());
        t = t && e2.equals(b.getSecond());
        return t;
    }


    /**
     * hash code.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (e1.hashCode() << 16) + e2.hashCode();
    }


    /**
     * isMultiple.
     *
     * @param p other.
     * @return true, if this is a multiple of b, else false.
     */
    public boolean isMultiple(ExpVectorPair p) {
        boolean w = e1.multipleOf(p.getFirst());
        if (!w) {
            return w;
        }
        w = e2.multipleOf(p.getSecond());
        if (!w) {
            return w;
        }
        return true;
    }

}
