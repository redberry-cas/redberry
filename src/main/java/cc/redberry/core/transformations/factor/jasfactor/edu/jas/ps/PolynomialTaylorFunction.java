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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;


/**
 * Polynomial functions capable for Taylor series expansion.
 *
 * @param <C> ring element type
 * @author Heinz Kredel
 */

public class PolynomialTaylorFunction<C extends RingElem<C>> implements TaylorFunction<C> {


    final GenPolynomial<C> pol;


    public PolynomialTaylorFunction(GenPolynomial<C> p) {
        pol = p;
    }


    /**
     * To String.
     *
     * @return string representation of this.
     */
    @Override
    public String toString() {
        return pol.toString();
    }


    /**
     * Test if this is zero.
     *
     * @return true if this is 0, else false.
     */
    public boolean isZERO() {
        return pol.isZERO();
    }


    /**
     * Deriviative.
     *
     * @return deriviative of this.
     */
    //JAVA6only: @Override
    public TaylorFunction<C> deriviative() {
        return new PolynomialTaylorFunction<>(PolyUtil.<C>baseDeriviative(pol));
    }


    /*
     * Partial deriviative.
     * @param r index of the variable.
     * @return partial deriviative of this with respect to variable r.
    public TaylorFunction<C> deriviative(int r) {
        return new PolynomialTaylorFunction<C>(PolyUtil. <C> baseDeriviative(pol,r)); 
    }
     */


    /**
     * Evaluate.
     *
     * @param a element.
     * @return this(a).
     */
    //JAVA6only: @Override
    public C evaluate(C a) {
        return PolyUtil.evaluateMain(pol.ring.coFac, pol, a);
    }


}
