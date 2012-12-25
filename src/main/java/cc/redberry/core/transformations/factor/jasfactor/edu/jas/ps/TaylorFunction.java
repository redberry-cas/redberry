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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.ps;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;


/**
 * Interface for functions capable for Taylor series expansion.
 *
 * @param <C> ring element type
 * @author Heinz Kredel
 */

public interface TaylorFunction<C extends RingElem<C>> {


    /**
     * Test if this is zero.
     *
     * @return true if this is 0, else false.
     */
    public boolean isZERO();


    /**
     * Deriviative.
     *
     * @return deriviative of this.
     */
    public TaylorFunction<C> deriviative();


    /**
     * Evaluate.
     *
     * @param a element.
     * @return this(a).
     */
    public C evaluate(C a);


}
