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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;

import java.io.Serializable;


/**
 * Factorization algorithms interface.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 * @usage To create objects that implement the <code>Factorization</code>
 * interface use the <code>FactorFactory</code>. It will select an
 * appropriate implementation based on the types of polynomial
 * coefficients C. To obtain an implementation use
 * <code>getImplementation()</code>, it returns an object of a class
 * which extends the <code>FactorAbstract</code> class which implements
 * the <code>Factorization</code> interface.
 * <p/>
 * <pre>
 * Factorization&lt;CT&gt; engine;
 * engine = FactorFactory.&lt;CT&gt; getImplementation(cofac);
 * c = engine.factors(a);
 * </pre>
 * <p/>
 * For example, if the coefficient type is BigInteger, the usage looks
 * like
 * <p/>
 * <pre>
 * BigInteger cofac = new BigInteger();
 * Factorization&lt;BigInteger&gt; engine;
 * engine = FactorFactory.getImplementation(cofac);
 * Sm = engine.factors(poly);
 * </pre>
 * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd.FactorFactory#getImplementation
 */

public interface Factorization<C extends GcdRingElem<C>> extends Serializable {


    /**
     * GenPolynomial test if is irreducible.
     *
     * @param P GenPolynomial.
     * @return true if P is irreducible, else false.
     */
    public boolean isIrreducible(GenPolynomial<C> P);
}
