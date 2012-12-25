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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure;


import java.io.Serializable;


/**
 * Element interface. Basic functionality of elements, e.g. compareTo, equals,
 * clone.
 * <b>Note:</b> extension of <code>Cloneable</code> removed in
 * 2012-08-18, <code>clone()</code> is renamed to <code>copy()</code>.
 *
 * @param <C> element type. See the discussion in
 *            <a href="http://www.artima.com/intv/bloch13.html">Bloch on Cloning</a>.
 * @author Heinz Kredel
 */

public interface Element<C extends Element<C>> extends Comparable<C>, Serializable {


    /**
     * Clone this Element.
     *
     * @return Creates and returns a copy of this Element.
     */
    public C copy();


    /**
     * Test if this is equal to b.
     *
     * @param b
     * @return true if this is equal to b, else false.
     */
    public boolean equals(Object b);


    /**
     * Hashcode of this Element.
     *
     * @return the hashCode.
     */
    public int hashCode();


    /**
     * Compare this to b. I.e. this &lt; b iff this.compareTo(b) &lt; 0.
     * <b>Note:</b> may not be meaningful if structure has no order.
     *
     * @param b
     * @return 0 if this is equal to b, -1 if this is less then b, else +1.
     */
    public int compareTo(C b);


    /**
     * Get the corresponding element factory.
     *
     * @return factory for this Element.
     */
    public ElemFactory<C> factory();
}
