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


import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;


/**
 * Element factory interface. Defines embedding of integers, parsing and random
 * element construction.
 *
 * @author Heinz Kredel
 */

public interface ElemFactory<C extends Element<C>> extends Serializable {


    /**
     * Get a list of the generating elements.
     *
     * @return list of generators for the algebraic structure.
     */
    public List<C> generators();


    /**
     * Is this structure finite or infinite.
     *
     * @return true if this structure is finite, else false.
     */
    public boolean isFinite();


    /**
     * Get the Element for a.
     *
     * @param a long
     * @return element corresponding to a.
     */
    public C fromInteger(long a);


    /**
     * Get the Element for a.
     *
     * @param a java.math.BigInteger.
     * @return element corresponding to a.
     */
    public C fromInteger(BigInteger a);


    /**
     * Generate a random Element with size less equal to n.
     *
     * @param n
     * @return a random element.
     */
    public C random(int n);


    /**
     * Generate a random Element with size less equal to n.
     *
     * @param n
     * @param random is a source for random bits.
     * @return a random element.
     */
    public C random(int n, Random random);


    /**
     * Create a copy of Element c.
     *
     * @param c
     * @return a copy of c.
     */
    public C copy(C c);
}
