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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;


/**
 * Absolute factorization algorithms class. This class contains implementations
 * of methods for factorization over algebraically closed fields. The required
 * field extension is computed along with the factors. The methods have been
 * tested for prime fields of characteristic zero, that is for
 * <code>BigRational</code>. It might eventually also be used for prime
 * fields of non-zero characteristic, that is with <code>ModInteger</code>.
 * The field extension may yet not be minimal.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public abstract class FactorAbsolute<C extends GcdRingElem<C>> extends FactorAbstract<C> {


    /*
    * Factorization engine for algebraic number coefficients.
    */
    //not possible here because of recursion AN -> Int|Mod -> AN -> ...
    //public final FactorAbstract<AlgebraicNumber<C>> aengine;

    /**
     * No argument constructor. <b>Note:</b> can't use this constructor.
     */
    protected FactorAbsolute() {
        throw new IllegalArgumentException("don't use this constructor");
    }


    /**
     * Constructor.
     *
     * @param cfac coefficient ring factory.
     */
    public FactorAbsolute(RingFactory<C> cfac) {
        super(cfac);
        //GenPolynomialRing<C> fac = new GenPolynomialRing<C>(cfac,1);
        //GenPolynomial<C> p = fac.univariate(0);
        //AlgebraicNumberRing<C> afac = new AlgebraicNumberRing<C>(p);
        //aengine = null; //FactorFactory.<C>getImplementation(afac); // hack
    }


    /**
     * Get the String representation.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getName();
    }

}
