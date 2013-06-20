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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure;


/**
 * Abelian group element interface. Defines the additive methods.
 *
 * @param <C> element type
 * @author Heinz Kredel
 */

public interface AbelianGroupElem<C extends AbelianGroupElem<C>> extends Element<C> {


    /**
     * Test if this is zero.
     *
     * @return true if this is 0, else false.
     */
    public boolean isZERO();


    /**
     * Signum.
     *
     * @return the sign of this.
     */
    public int signum();


    /**
     * Sum of this and S.
     *
     * @param S
     * @return this + S.
     */
    public C sum(C S);


    //public <T extends C> T sum(T S);


    /**
     * Subtract S from this.
     *
     * @param S
     * @return this - S.
     */
    public C subtract(C S);


    /**
     * Negate this.
     *
     * @return - this.
     */
    public C negate();


    /**
     * Absolute value of this.
     *
     * @return |this|.
     */
    public C abs();

}
