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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;

import java.io.Serializable;
import java.util.List;


/**
 * Greatest common divisor algorithm interface.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 * @usage To create classes that implement this interface use the
 * GreatestCommonDivisorFactory. It will select an appropriate
 * implementation based on the types of polynomial coefficients CT.
 * <p/>
 * <pre>
 * GreatestCommonDivisor&lt;CT&gt; engine = GCDFactory.&lt;CT&gt; getImplementation(cofac);
 * c = engine.gcd(a, b);
 * </pre>
 * <p/>
 * For example, if the coefficient type is BigInteger, the usage looks
 * like
 * <p/>
 * <pre>
 * BigInteger cofac = new BigInteger();
 * GreatestCommonDivisor&lt;BigInteger&gt; engine = GCDFactory.getImplementation(cofac);
 * c = engine.gcd(a, b);
 * </pre>
 * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd.GCDFactory#getImplementation
 */

public interface GreatestCommonDivisor<C extends GcdRingElem<C>> extends Serializable {


    /**
     * GenPolynomial content.
     *
     * @param P GenPolynomial.
     * @return cont(P).
     */
    public GenPolynomial<C> content(GenPolynomial<C> P);


    /**
     * GenPolynomial greatest comon divisor.
     *
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return gcd(P, S).
     */
    public GenPolynomial<C> gcd(GenPolynomial<C> P, GenPolynomial<C> S);


    /**
     * GenPolynomial least comon multiple.
     *
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return lcm(P, S).
     */
    public GenPolynomial<C> lcm(GenPolynomial<C> P, GenPolynomial<C> S);


    /**
     * GenPolynomial resultant.
     * The input polynomials are considered as univariate polynomials in the main variable.
     *
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return res(P, S).
     * @throws UnsupportedOperationException if there is no implementation in the sub-class.
     */
    public GenPolynomial<C> resultant(GenPolynomial<C> P, GenPolynomial<C> S);


    /**
     * GenPolynomial co-prime list.
     *
     * @param A list of GenPolynomials.
     * @return B with gcd(b,c) = 1 for all b != c in B and for all non-constant
     *         a in A there exists b in B with b|a. B does not contain zero or
     *         constant polynomials.
     */
    public List<GenPolynomial<C>> coPrime(List<GenPolynomial<C>> A);
}
