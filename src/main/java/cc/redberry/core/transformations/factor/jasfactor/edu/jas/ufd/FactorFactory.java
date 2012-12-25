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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.*;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.*;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;


/**
 * Factorization algorithms factory. Select appropriate factorization engine
 * based on the coefficient types.
 *
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
 * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd.Factorization#factors(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial P)
 */

public class FactorFactory {


    /**
     * Determine suitable implementation of factorization algorithm, case
     * ModInteger.
     *
     * @param fac ModIntegerRing.
     * @return factorization algorithm implementation.
     */
    public static FactorAbstract<ModInteger> getImplementation(ModIntegerRing fac) {
        return new FactorModular<>(fac);
    }


    /**
     * Determine suitable implementation of factorization algorithm, case
     * ModInteger.
     *
     * @param fac ModIntegerRing.
     * @return factorization algorithm implementation.
     */
    public static FactorAbstract<ModLong> getImplementation(ModLongRing fac) {
        return new FactorModular<>(fac);
    }


    /**
     * Determine suitable implementation of factorization algorithm, case
     * BigInteger.
     *
     * @param fac BigInteger.
     * @return factorization algorithm implementation.
     */
    public static FactorAbstract<BigInteger> getImplementation(BigInteger fac) {
        return new FactorInteger<ModLong>();
    }


    /**
     * Determine suitable implementation of factorization algorithms, case
     * BigRational.
     *
     * @param fac BigRational.
     * @return factorization algorithm implementation.
     */
    public static FactorAbstract<BigRational> getImplementation(BigRational fac) {
        return new FactorRational();
    }


    /**
     * Determine suitable implementation of factorization algorithms, case
     * AlgebraicNumber&lt;C&gt;.
     *
     * @param fac AlgebraicNumberRing&lt;C&gt;.
     * @param <C> coefficient type, e.g. BigRational, ModInteger.
     * @return factorization algorithm implementation.
     */
    public static <C extends GcdRingElem<C>> FactorAbstract<AlgebraicNumber<C>> getImplementation(
            AlgebraicNumberRing<C> fac) {
        return new FactorAlgebraic<>(fac);
    }


    /**
     * Determine suitable implementation of factorization algorithms, case
     * Complex&lt;C&gt;.
     *
     * @param fac ComplexRing&lt;C&gt;.
     * @param <C> coefficient type, e.g. BigRational, ModInteger.
     * @return factorization algorithm implementation.
     */
    public static <C extends GcdRingElem<C>> FactorAbstract<Complex<C>> getImplementation(ComplexRing<C> fac) {
        return new FactorComplex<>(fac);
    }


    /**
     * Determine suitable implementation of factorization algorithms, case
     * Quotient&lt;C&gt;.
     *
     * @param fac QuotientRing&lt;C&gt;.
     * @param <C> coefficient type, e.g. BigRational, ModInteger.
     * @return factorization algorithm implementation.
     */
    public static <C extends GcdRingElem<C>> FactorAbstract<Quotient<C>> getImplementation(QuotientRing<C> fac) {
        return new FactorQuotient<>(fac);
    }


    /**
     * Determine suitable implementation of factorization algorithms, case
     * recursive GenPolynomial&lt;C&gt;. Use <code>recursiveFactors()</code>.
     *
     * @param fac GenPolynomialRing&lt;C&gt;.
     * @param <C> coefficient type, e.g. BigRational, ModInteger.
     * @return factorization algorithm implementation.
     */
    public static <C extends GcdRingElem<C>> FactorAbstract<C> getImplementation(GenPolynomialRing<C> fac) {
        return getImplementation(fac.coFac);
    }


    /**
     * Determine suitable implementation of factorization algorithms, other
     * cases.
     *
     * @param <C> coefficient type
     * @param fac RingFactory&lt;C&gt;.
     * @return factorization algorithm implementation.
     */
    @SuppressWarnings("unchecked")
    public static <C extends GcdRingElem<C>> FactorAbstract<C> getImplementation(RingFactory<C> fac) {
        FactorAbstract/*raw type<C>*/ufd;
        AlgebraicNumberRing afac;
        ComplexRing cfac;
        QuotientRing qfac;
        GenPolynomialRing pfac;
        Object ofac = fac;
        if (ofac instanceof BigInteger) {
            ufd = new FactorInteger();
        } else if (ofac instanceof BigRational) {
            ufd = new FactorRational();
        } else if (ofac instanceof ModIntegerRing) {
            ufd = new FactorModular(fac);
        } else if (ofac instanceof ModLongRing) {
            ufd = new FactorModular(fac);
        } else if (ofac instanceof ComplexRing) {
            cfac = (ComplexRing<C>) ofac;
            ufd = new FactorComplex(cfac);
        } else if (ofac instanceof AlgebraicNumberRing) {
            afac = (AlgebraicNumberRing) ofac;
            //ofac = afac.ring.coFac;
            ufd = new FactorAlgebraic/*raw <C>*/(afac);
        } else if (ofac instanceof QuotientRing) {
            qfac = (QuotientRing) ofac;
            ufd = new FactorQuotient/*raw <C>*/(qfac);
        } else if (ofac instanceof GenPolynomialRing) {
            pfac = (GenPolynomialRing) ofac;
            ufd = getImplementation(pfac.coFac);
        } else {
            throw new IllegalArgumentException("no factorization implementation for "
                    + fac.getClass().getName());
        }
        return (FactorAbstract<C>) ufd;
    }

}
