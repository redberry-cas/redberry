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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure;


/**
 * Monoid element interface. Defines the multiplicative methods.
 *
 * @param <C> element type
 * @author Heinz Kredel
 */

public interface MonoidElem<C extends MonoidElem<C>> extends Element<C> {


    /**
     * Test if this is one.
     *
     * @return true if this is 1, else false.
     */
    public boolean isONE();


    /**
     * Test if this is a unit. I.e. there exists x with this.multiply(x).isONE()
     * == true.
     *
     * @return true if this is a unit, else false.
     */
    public boolean isUnit();


    /**
     * Multiply this with S.
     *
     * @param S
     * @return this * S.
     */
    public C multiply(C S);


    /**
     * Divide this by S.
     *
     * @param S
     * @return this / S.
     */
    public C divide(C S);


    /**
     * Remainder after division of this by S.
     *
     * @param S
     * @return this - (this / S) * S.
     */
    public C remainder(C S);


    /**
     * Inverse of this. Some implementing classes will throw
     * NotInvertibleException if the element is not invertible.
     *
     * @return x with this * x = 1, if it exists.
     */
    public C inverse(); /*throws NotInvertibleException*/

}
