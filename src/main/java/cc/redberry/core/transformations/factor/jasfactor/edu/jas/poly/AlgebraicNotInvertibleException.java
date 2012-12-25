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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.NotInvertibleException;


/**
 * Algebraic number NotInvertibleException class.
 * Runtime Exception to be thrown for not invertible algebraic numbers.
 * Container for the non-trivial factors found by the inversion algorithm.
 * <b>Note: </b> cannot be generic because of Throwable.
 *
 * @author Heinz Kredel
 */
public class AlgebraicNotInvertibleException extends NotInvertibleException {


    public final GenPolynomial f; // = f1 * f2

    public final GenPolynomial f1;

    public final GenPolynomial f2;

    public AlgebraicNotInvertibleException(String c, Throwable t) {
        this(c, t, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param f  polynomial with f = f1 * f2.
     * @param f1 polynomial.
     * @param f2 polynomial.
     */
    public AlgebraicNotInvertibleException(String c, GenPolynomial f, GenPolynomial f1, GenPolynomial f2) {
        super(c);
        this.f = f;
        this.f1 = f1;
        this.f2 = f2;
    }


    /**
     * Constructor.
     *
     * @param f  polynomial with f = f1 * f2.
     * @param f1 polynomial.
     * @param f2 polynomial.
     */
    public AlgebraicNotInvertibleException(String c, Throwable t, GenPolynomial f, GenPolynomial f1, GenPolynomial f2) {
        super(c, t);
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
