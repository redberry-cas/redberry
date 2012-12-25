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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.NotInvertibleException;


/**
 * Modular integer NotInvertibleException class. Runtime Exception to be thrown
 * for not invertible modular integers. Container for the non-trivial factors
 * found by the inversion algorithm. <b>Note: </b> cannot be generic because of
 * Throwable.
 *
 * @author Heinz Kredel
 */
public class ModularNotInvertibleException extends NotInvertibleException {


    public final GcdRingElem f; // = f1 * f2


    public final GcdRingElem f1;


    public final GcdRingElem f2;

    /**
     * Constructor.
     *
     * @param f  gcd ring element with f = f1 * f2.
     * @param f1 gcd ring element.
     * @param f2 gcd ring element.
     */
    public ModularNotInvertibleException(String c, GcdRingElem f, GcdRingElem f1, GcdRingElem f2) {
        super(c);
        this.f = f;
        this.f1 = f1;
        this.f2 = f2;
    }


    /**
     * Constructor.
     *
     * @param f  gcd ring element with f = f1 * f2.
     * @param f1 gcd ring element.
     * @param f2 gcd ring element.
     */
    public ModularNotInvertibleException(Throwable t, GcdRingElem f, GcdRingElem f1, GcdRingElem f2) {
        super("ModularNotInvertibleException", t);
        this.f = f;
        this.f1 = f1;
        this.f2 = f2;
    }


    /**
     * Get the String representation.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String s = super.toString();
        if (f != null || f1 != null || f2 != null) {
            s += ", f = " + f + ", f1 = " + f1 + ", f2 = " + f2;
        }
        return s;
    }

}
