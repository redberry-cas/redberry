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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.*;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;


/**
 * Greatest common divisor algorithms factory. Select appropriate GCD engine
 * based on the coefficient types.
 *
 * @author Heinz Kredel
 * @todo Base decision also an degree vectors and number of variables of
 * polynomials. Incorporate also number of CPUs / threads available (done
 * with GCDProxy).
 * @usage To create objects that implement the
 * <code>GreatestCommonDivisor</code> interface use the
 * <code>GCDFactory</code>. It will select an appropriate implementation
 * based on the types of polynomial coefficients C. There are two methods
 * to obtain an implementation: <code>getProxy()</code> and
 * <code>getImplementation()</code>. <code>getImplementation()</code>
 * returns an object of a class which implements the
 * <code>GreatestCommonDivisor</code> interface. <code>getProxy()</code>
 * returns a proxy object of a class which implements the
 * <code>GreatestCommonDivisor</code>r interface. The proxy will run two
 * implementations in parallel, return the first computed result and
 * cancel the second running task. On systems with one CPU the computing
 * time will be two times the time of the fastest algorithm
 * implmentation. On systems with more than two CPUs the computing time
 * will be the time of the fastest algorithm implmentation.
 * <p/>
 * <pre>
 * GreatestCommonDivisor&lt;CT&gt; engine;
 * engine = GCDFactory.&lt;CT&gt; getImplementation(cofac);
 * or engine = GCDFactory.&lt;CT&gt; getProxy(cofac);
 * c = engine.gcd(a, b);
 * </pre>
 * <p/>
 * For example, if the coefficient type is BigInteger, the usage looks
 * like
 * <p/>
 * <pre>
 * BigInteger cofac = new BigInteger();
 * GreatestCommonDivisor&lt;BigInteger&gt; engine;
 * engine = GCDFactory.getImplementation(cofac);
 * or engine = GCDFactory.getProxy(cofac);
 * c = engine.gcd(a, b);
 * </pre>
 * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd.GreatestCommonDivisor#gcd(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial P,
 *      cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial S)
 */

public class GCDFactory {


    /**
     * Protected factory constructor.
     */
    private GCDFactory() {
    }


    /**
     * Determine suitable implementation of gcd algorithms, case ModLong.
     *
     * @param fac ModLongRing.
     * @return gcd algorithm implementation.
     */
    public static GreatestCommonDivisorAbstract<ModLong> getImplementation(ModLongRing fac) {
        GreatestCommonDivisorAbstract<ModLong> ufd;
        if (fac.isField()) {
            ufd = new GreatestCommonDivisorModEval<>();
            return ufd;
        }
        ufd = new GreatestCommonDivisorSubres<>();
        return ufd;
    }


    /**
     * Determine suitable implementation of gcd algorithms, case ModInteger.
     *
     * @param fac ModIntegerRing.
     * @return gcd algorithm implementation.
     */
    public static GreatestCommonDivisorAbstract<ModInteger> getImplementation(ModIntegerRing fac) {
        GreatestCommonDivisorAbstract<ModInteger> ufd;
        if (fac.isField()) {
            ufd = new GreatestCommonDivisorModEval<>();
            return ufd;
        }
        ufd = new GreatestCommonDivisorSubres<>();
        return ufd;
    }


    /**
     * Determine suitable implementation of gcd algorithms, case BigInteger.
     *
     * @param fac BigInteger.
     * @return gcd algorithm implementation.
     */
    public static GreatestCommonDivisorAbstract<BigInteger> getImplementation(BigInteger fac) {
        GreatestCommonDivisorAbstract<BigInteger> ufd;
        ufd = new GreatestCommonDivisorModular<ModLong>(); // dummy type
        return ufd;
    }


    /**
     * Determine suitable implementation of gcd algorithms, case BigRational.
     *
     * @param fac BigRational.
     * @return gcd algorithm implementation.
     */
    public static GreatestCommonDivisorAbstract<BigRational> getImplementation(BigRational fac) {
        GreatestCommonDivisorAbstract<BigRational> ufd;
        ufd = new GreatestCommonDivisorPrimitive<>();
        return ufd;
    }


    /**
     * Determine suitable implementation of gcd algorithms, other cases.
     *
     * @param fac RingFactory&lt;C&gt;.
     * @return gcd algorithm implementation.
     */
    @SuppressWarnings("unchecked")
    public static <C extends GcdRingElem<C>> GreatestCommonDivisorAbstract<C> getImplementation(
            RingFactory<C> fac) {
        GreatestCommonDivisorAbstract/*raw type<C>*/ufd;
        Object ofac = fac;
        if (ofac instanceof BigInteger) {
            ufd = new GreatestCommonDivisorModular<ModInteger>();
            //ufd = new GreatestCommonDivisorSubres<BigInteger>();
            //ufd = new GreatestCommonDivisorModular<ModInteger>(true);
        } else if (ofac instanceof ModIntegerRing) {
            ufd = new GreatestCommonDivisorModEval<ModInteger>();
        } else if (ofac instanceof ModLongRing) {
            ufd = new GreatestCommonDivisorModEval<ModLong>();
        } else if (ofac instanceof BigRational) {
            ufd = new GreatestCommonDivisorSubres<BigRational>();
        } else {
            if (fac.isField()) {
                ufd = new GreatestCommonDivisorSimple<C>();
            } else {
                ufd = new GreatestCommonDivisorSubres<C>();
            }
        }
        return ufd;
    }


    /**
     * Determine suitable proxy for gcd algorithms, other cases.
     *
     * @param fac RingFactory&lt;C&gt;.
     * @return gcd algorithm implementation. <b>Note:</b> This method contains a
     *         hack for Google app engine to not use threads.
     */
    @SuppressWarnings("unchecked")
    public static <C extends GcdRingElem<C>> GreatestCommonDivisorAbstract<C> getProxy(RingFactory<C> fac) {
        return GCDFactory.getImplementation(fac);
    }

}
