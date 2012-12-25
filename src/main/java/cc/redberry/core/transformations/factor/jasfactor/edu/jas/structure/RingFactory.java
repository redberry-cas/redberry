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


/**
 * Ring factory interface. Defines test for field and access of characteristic.
 *
 * @author Heinz Kredel
 */

public interface RingFactory<C extends RingElem<C>> extends AbelianGroupFactory<C>, MonoidFactory<C> {


    /**
     * Query if this ring is a field. May return false if it is to hard to
     * determine if this ring is a field.
     *
     * @return true if it is known that this ring is a field, else false.
     */
    public boolean isField();


    /**
     * Characteristic of this ring.
     *
     * @return characteristic of this ring.
     */
    public java.math.BigInteger characteristic();

}
