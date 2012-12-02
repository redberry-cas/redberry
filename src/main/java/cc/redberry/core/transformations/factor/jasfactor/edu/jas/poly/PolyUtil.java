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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigInteger;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigRational;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.Modular;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.ModularRingFactory;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.UnaryFunctor;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.util.ListUtil;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;


/**
 * Polynomial utilities, for example conversion between different
 * representations, evaluation and interpolation.
 *
 * @author Heinz Kredel
 */

public class PolyUtil {


    private static boolean debug = false;


    /**
     * Recursive representation. Represent as polynomial in i variables with
     * coefficients in n-i variables. Works for arbitrary term orders.
     *
     * @param <C>  coefficient type.
     * @param rfac recursive polynomial ring factory.
     * @param A    polynomial to be converted.
     * @return Recursive represenations of this in the ring rfac.
     */
    public static <C extends RingElem<C>> GenPolynomial<GenPolynomial<C>> recursive(
            GenPolynomialRing<GenPolynomial<C>> rfac, GenPolynomial<C> A) {

        GenPolynomial<GenPolynomial<C>> B = rfac.getZERO().copy();
        if (A.isZERO()) {
            return B;
        }
        int i = rfac.nvar;
        GenPolynomial<C> zero = rfac.getZEROCoefficient();
        Map<ExpVector, GenPolynomial<C>> Bv = B.val; //getMap();
        for (Map.Entry<ExpVector, C> y : A.getMap().entrySet()) {
            ExpVector e = y.getKey();
            C a = y.getValue();
            ExpVector f = e.contract(0, i);
            ExpVector g = e.contract(i, e.length() - i);
            GenPolynomial<C> p = Bv.get(f);
            if (p == null) {
                p = zero;
            }
            p = p.sum(a, g);
            Bv.put(f, p);
        }
        return B;
    }


    /**
     * Distribute a recursive polynomial to a generic polynomial. Works for
     * arbitrary term orders.
     *
     * @param <C>  coefficient type.
     * @param dfac combined polynomial ring factory of coefficients and this.
     * @param B    polynomial to be converted.
     * @return distributed polynomial.
     */
    public static <C extends RingElem<C>> GenPolynomial<C> distribute(GenPolynomialRing<C> dfac,
                                                                      GenPolynomial<GenPolynomial<C>> B) {
        GenPolynomial<C> C = dfac.getZERO().copy();
        if (B.isZERO()) {
            return C;
        }
        Map<ExpVector, C> Cm = C.val; //getMap();
        for (Map.Entry<ExpVector, GenPolynomial<C>> y : B.getMap().entrySet()) {
            ExpVector e = y.getKey();
            GenPolynomial<C> A = y.getValue();
            for (Map.Entry<ExpVector, C> x : A.val.entrySet()) {
                ExpVector f = x.getKey();
                C b = x.getValue();
                ExpVector g = e.combine(f);
//                assert (Cm.get(g) != null);
                //if ( Cm.get(g) != null ) { // todo assert, done
                //   throw new RuntimeException("PolyUtil debug error");
                //}
                Cm.put(g, b);
            }
        }
        return C;
    }


    /**
     * Recursive representation. Represent as polynomials in i variables with
     * coefficients in n-i variables. Works for arbitrary term orders.
     *
     * @param <C>  coefficient type.
     * @param rfac recursive polynomial ring factory.
     * @param L    list of polynomials to be converted.
     * @return Recursive represenations of the list in the ring rfac.
     */
    public static <C extends RingElem<C>> List<GenPolynomial<GenPolynomial<C>>> recursive(
            GenPolynomialRing<GenPolynomial<C>> rfac, List<GenPolynomial<C>> L) {
        return ListUtil.map(L, new DistToRec<>(rfac));
    }


    /**
     * BigInteger from ModInteger coefficients, symmetric. Represent as
     * polynomial with BigInteger coefficients by removing the modules and
     * making coefficients symmetric to 0.
     *
     * @param fac result polynomial factory.
     * @param A   polynomial with ModInteger coefficients to be converted.
     * @return polynomial with BigInteger coefficients.
     */
    public static <C extends RingElem<C> & Modular> GenPolynomial<BigInteger> integerFromModularCoefficients(
            GenPolynomialRing<BigInteger> fac, GenPolynomial<C> A) {
        return PolyUtil.map(fac, A, new ModSymToInt<C>());
    }


    /**
     * BigInteger from ModInteger coefficients, symmetric. Represent as
     * polynomial with BigInteger coefficients by removing the modules and
     * making coefficients symmetric to 0.
     *
     * @param fac result polynomial factory.
     * @param L   list of polynomials with ModInteger coefficients to be
     *            converted.
     * @return list of polynomials with BigInteger coefficients.
     */
    public static <C extends RingElem<C> & Modular> List<GenPolynomial<BigInteger>> integerFromModularCoefficients(
            final GenPolynomialRing<BigInteger> fac, List<GenPolynomial<C>> L) {
        return ListUtil.map(L,
                new UnaryFunctor<GenPolynomial<C>, GenPolynomial<BigInteger>>() {


                    public GenPolynomial<BigInteger> eval(GenPolynomial<C> c) {
                        return PolyUtil.integerFromModularCoefficients(fac, c);
                    }
                });
    }


    /**
     * BigInteger from BigRational coefficients. Represent as polynomial with
     * BigInteger coefficients by multiplication with the lcm of the numerators
     * of the BigRational coefficients.
     *
     * @param fac result polynomial factory.
     * @param A   polynomial with BigRational coefficients to be converted.
     * @return polynomial with BigInteger coefficients.
     */
    public static GenPolynomial<BigInteger> integerFromRationalCoefficients(
            GenPolynomialRing<BigInteger> fac, GenPolynomial<BigRational> A) {
        if (A == null || A.isZERO()) {
            return fac.getZERO();
        }
        java.math.BigInteger c = null;
        int s = 0;
        // lcm of denominators
        for (BigRational y : A.val.values()) {
            java.math.BigInteger x = y.denominator();
            // c = lcm(c,x)
            if (c == null) {
                c = x;
                s = x.signum();
            } else {
                java.math.BigInteger d = c.gcd(x);
                c = c.multiply(x.divide(d));
            }
        }
        if (s < 0) {
            c = c.negate();
        }
        return PolyUtil.map(fac, A, new RatToInt(c));
    }

    /**
     * BigInteger from BigRational coefficients. Represent as polynomial with
     * BigInteger coefficients by multiplication with the gcd of the numerators
     * and the lcm of the denominators of the BigRational coefficients. <br
     * />
     * <b>Author:</b> Axel Kramer
     *
     * @param fac result polynomial factory.
     * @param A   polynomial with BigRational coefficients to be converted.
     * @return Object[] with 3 entries: [0]->gcd [1]->lcm and [2]->polynomial
     *         with BigInteger coefficients.
     */
    public static Object[] integerFromRationalCoefficientsFactor(GenPolynomialRing<BigInteger> fac,
                                                                 GenPolynomial<BigRational> A) {
        Object[] result = new Object[3];
        if (A == null || A.isZERO()) {
            result[0] = java.math.BigInteger.ONE;
            result[1] = java.math.BigInteger.ZERO;
            result[2] = fac.getZERO();
            return result;
        }
        java.math.BigInteger gcd = null;
        java.math.BigInteger lcm = null;
        int sLCM = 0;
        int sGCD = 0;
        // lcm of denominators
        for (BigRational y : A.val.values()) {
            java.math.BigInteger numerator = y.numerator();
            java.math.BigInteger denominator = y.denominator();
            // lcm = lcm(lcm,x)
            if (lcm == null) {
                lcm = denominator;
                sLCM = denominator.signum();
            } else {
                java.math.BigInteger d = lcm.gcd(denominator);
                lcm = lcm.multiply(denominator.divide(d));
            }
            // gcd = gcd(gcd,x)
            if (gcd == null) {
                gcd = numerator;
                sGCD = numerator.signum();
            } else {
                gcd = gcd.gcd(numerator);
            }
        }
        if (sLCM < 0) {
            lcm = lcm.negate();
        }
        if (sGCD < 0) {
            gcd = gcd.negate();
        }
        result[0] = gcd;
        result[1] = lcm;
        result[2] = PolyUtil.<BigRational, BigInteger>map(fac, A, new RatToIntFactor(gcd, lcm));
        return result;
    }

    /**
     * From BigInteger coefficients. Represent as polynomial with type C
     * coefficients, e.g. ModInteger or BigRational.
     *
     * @param <C> coefficient type.
     * @param fac result polynomial factory.
     * @param A   polynomial with BigInteger coefficients to be converted.
     * @return polynomial with type C coefficients.
     */
    public static <C extends RingElem<C>> GenPolynomial<C> fromIntegerCoefficients(GenPolynomialRing<C> fac,
                                                                                   GenPolynomial<BigInteger> A) {
        return PolyUtil.map(fac, A, new FromInteger<>(fac.coFac));
    }


    /**
     * From BigInteger coefficients. Represent as list of polynomials with type
     * C coefficients, e.g. ModInteger or BigRational.
     *
     * @param <C> coefficient type.
     * @param fac result polynomial factory.
     * @param L   list of polynomials with BigInteger coefficients to be
     *            converted.
     * @return list of polynomials with type C coefficients.
     */
    public static <C extends RingElem<C>> List<GenPolynomial<C>> fromIntegerCoefficients(
            GenPolynomialRing<C> fac, List<GenPolynomial<BigInteger>> L) {
        return ListUtil.map(L, new FromIntegerPoly<>(fac));
    }

    /**
     * From AlgebraicNumber coefficients. Represent as polynomial with type
     * GenPolynomial&lt;C&gt; coefficients, e.g. ModInteger or BigRational.
     *
     * @param rfac result polynomial factory.
     * @param A    polynomial with AlgebraicNumber coefficients to be converted.
     * @return polynomial with type GenPolynomial&lt;C&gt; coefficients.
     */
    public static <C extends GcdRingElem<C>> GenPolynomial<GenPolynomial<C>> fromAlgebraicCoefficients(
            GenPolynomialRing<GenPolynomial<C>> rfac, GenPolynomial<AlgebraicNumber<C>> A) {
        return PolyUtil.map(rfac, A, new AlgToPoly<C>());
    }


    /**
     * Convert to AlgebraicNumber coefficients. Represent as polynomial with
     * AlgebraicNumber<C> coefficients, C is e.g. ModInteger or BigRational.
     *
     * @param pfac result polynomial factory.
     * @param A    polynomial with C coefficients to be converted.
     * @return polynomial with AlgebraicNumber&lt;C&gt; coefficients.
     */
    public static <C extends GcdRingElem<C>> GenPolynomial<AlgebraicNumber<C>> convertToAlgebraicCoefficients(
            GenPolynomialRing<AlgebraicNumber<C>> pfac, GenPolynomial<C> A) {
        AlgebraicNumberRing<C> afac = (AlgebraicNumberRing<C>) pfac.coFac;
        return PolyUtil.map(pfac, A, new CoeffToAlg<>(afac));
    }


    /**
     * Complex from algebraic coefficients.
     *
     * @param fac result polynomial factory.
     * @param A   polynomial with AlgebraicNumber coefficients Q(i) to be
     *            converted.
     * @return polynomial with Complex coefficients.
     */
    public static <C extends GcdRingElem<C>> GenPolynomial<Complex<C>> complexFromAlgebraic(
            GenPolynomialRing<Complex<C>> fac, GenPolynomial<AlgebraicNumber<C>> A) {
        ComplexRing<C> cfac = (ComplexRing<C>) fac.coFac;
        return PolyUtil.map(fac, A, new AlgebToCompl<>(cfac));
    }


    /**
     * AlgebraicNumber from complex coefficients.
     *
     * @param fac result polynomial factory over Q(i).
     * @param A   polynomial with Complex coefficients to be converted.
     * @return polynomial with AlgebraicNumber coefficients.
     */
    public static <C extends GcdRingElem<C>> GenPolynomial<AlgebraicNumber<C>> algebraicFromComplex(
            GenPolynomialRing<AlgebraicNumber<C>> fac, GenPolynomial<Complex<C>> A) {
        AlgebraicNumberRing<C> afac = (AlgebraicNumberRing<C>) fac.coFac;
        return PolyUtil.map(fac, A, new ComplToAlgeb<>(afac));
    }


    /**
     * ModInteger chinese remainder algorithm on coefficients.
     *
     * @param fac GenPolynomial&lt;ModInteger&gt; result factory with
     *            A.coFac.modul*B.coFac.modul = C.coFac.modul.
     * @param A   GenPolynomial&lt;ModInteger&gt;.
     * @param B   other GenPolynomial&lt;ModInteger&gt;.
     * @param mi  inverse of A.coFac.modul in ring B.coFac.
     * @return S = cra(A,B), with S mod A.coFac.modul == A and S mod
     *         B.coFac.modul == B.
     */
    public static <C extends RingElem<C> & Modular> GenPolynomial<C> chineseRemainder(
            GenPolynomialRing<C> fac, GenPolynomial<C> A, C mi, GenPolynomial<C> B) {
        ModularRingFactory<C> cfac = (ModularRingFactory<C>) fac.coFac; // get RingFactory
        GenPolynomial<C> S = fac.getZERO().copy();
        GenPolynomial<C> Ap = A.copy();
        SortedMap<ExpVector, C> av = Ap.val; //getMap();
        SortedMap<ExpVector, C> bv = B.getMap();
        SortedMap<ExpVector, C> sv = S.val; //getMap();
        C c = null;
        for (Map.Entry<ExpVector, C> me : bv.entrySet()) {
            ExpVector e = me.getKey();
            C y = me.getValue(); //bv.get(e); // assert y != null
            C x = av.get(e);
            if (x != null) {
                av.remove(e);
                c = cfac.chineseRemainder(x, mi, y);
                if (!c.isZERO()) { // 0 cannot happen
                    sv.put(e, c);
                }
            } else {
                //c = cfac.fromInteger( y.getVal() );
                c = cfac.chineseRemainder(A.ring.coFac.getZERO(), mi, y);
                if (!c.isZERO()) { // 0 cannot happen
                    sv.put(e, c); // c != null
                }
            }
        }
        // assert bv is empty = done
        for (Map.Entry<ExpVector, C> me : av.entrySet()) { // rest of av
            ExpVector e = me.getKey();
            C x = me.getValue(); // av.get(e); // assert x != null
            //c = cfac.fromInteger( x.getVal() );
            c = cfac.chineseRemainder(x, mi, B.ring.coFac.getZERO());
            if (!c.isZERO()) { // 0 cannot happen
                sv.put(e, c); // c != null
            }
        }
        return S;
    }


    /**
     * GenPolynomial monic, i.e. leadingBaseCoefficient == 1. If
     * leadingBaseCoefficient is not invertible returns this unmodified.
     *
     * @param <C> coefficient type.
     * @param p   recursive GenPolynomial<GenPolynomial<C>>.
     * @return monic(p).
     */
    public static <C extends RingElem<C>> GenPolynomial<GenPolynomial<C>> monic(
            GenPolynomial<GenPolynomial<C>> p) {
        if (p == null || p.isZERO()) {
            return p;
        }
        C lc = p.leadingBaseCoefficient().leadingBaseCoefficient();
        if (!lc.isUnit()) {
            return p;
        }
        C lm = lc.inverse();
        GenPolynomial<C> L = p.ring.coFac.getONE();
        L = L.multiply(lm);
        return p.multiply(L);
    }


    /**
     * Polynomial list monic.
     *
     * @param <C> coefficient type.
     * @param L   list of polynomials with field coefficients.
     * @return list of polynomials with leading coefficient 1.
     */
    public static <C extends RingElem<C>> List<GenPolynomial<C>> monic(List<GenPolynomial<C>> L) {
        return ListUtil.map(L,
                new UnaryFunctor<GenPolynomial<C>, GenPolynomial<C>>() {


                    public GenPolynomial<C> eval(GenPolynomial<C> c) {
                        if (c == null) {
                            return null;
                        }
                        return c.monic();
                    }
                });
    }


    /**
     * Polynomial list leading exponent vectors.
     *
     * @param <C> coefficient type.
     * @param L   list of polynomials.
     * @return list of leading exponent vectors.
     */
    public static <C extends RingElem<C>> List<ExpVector> leadingExpVector(List<GenPolynomial<C>> L) {
        return ListUtil.map(L, new UnaryFunctor<GenPolynomial<C>, ExpVector>() {


            public ExpVector eval(GenPolynomial<C> c) {
                if (c == null) {
                    return null;
                }
                return c.leadingExpVector();
            }
        });
    }


    /**
     * GenPolynomial sparse pseudo remainder. For univariate polynomials.
     *
     * @param <C> coefficient type.
     * @param P   GenPolynomial.
     * @param S   nonzero GenPolynomial.
     * @return remainder with ldcf(S)<sup>m'</sup> P = quotient * S + remainder.
     *         m' &le; deg(P)-deg(S)
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial#remainder(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial).
     */
    public static <C extends RingElem<C>> GenPolynomial<C> baseSparsePseudoRemainder(GenPolynomial<C> P,
                                                                                     GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            throw new ArithmeticException(P.toString() + " division by zero " + S);
        }
        if (P.isZERO()) {
            return P;
        }
        if (S.isONE()) {
            return P.ring.getZERO();
        }
        C c = S.leadingBaseCoefficient();
        ExpVector e = S.leadingExpVector();
        GenPolynomial<C> h;
        GenPolynomial<C> r = P;
        while (!r.isZERO()) {
            ExpVector f = r.leadingExpVector();
            if (f.multipleOf(e)) {
                C a = r.leadingBaseCoefficient();
                f = f.subtract(e);
                C x = a.remainder(c);
                if (x.isZERO()) {
                    C y = a.divide(c);
                    h = S.multiply(y, f); // coeff a
                } else {
                    r = r.multiply(c); // coeff ac
                    h = S.multiply(a, f); // coeff ac
                }
                r = r.subtract(h);
            } else {
                break;
            }
        }
        return r;
    }


    /**
     * GenPolynomial dense pseudo remainder. For univariate polynomials.
     *
     * @param P GenPolynomial.
     * @param S nonzero GenPolynomial.
     * @return remainder with ldcf(S)<sup>m</sup> P = quotient * S + remainder.
     *         m == deg(P)-deg(S)
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial#remainder(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial).
     */
    public static <C extends RingElem<C>> GenPolynomial<C> baseDensePseudoRemainder(GenPolynomial<C> P,
                                                                                    GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            throw new ArithmeticException(P + " division by zero " + S);
        }
        if (P.isZERO()) {
            return P;
        }
        if (S.degree() <= 0) {
            return P.ring.getZERO();
        }
        long m = P.degree(0);
        long n = S.degree(0);
        C c = S.leadingBaseCoefficient();
        ExpVector e = S.leadingExpVector();
        GenPolynomial<C> h;
        GenPolynomial<C> r = P;
        for (long i = m; i >= n; i--) {
            if (r.isZERO()) {
                return r;
            }
            long k = r.degree(0);
            if (i == k) {
                ExpVector f = r.leadingExpVector();
                C a = r.leadingBaseCoefficient();
                f = f.subtract(e); // EVDIF( f, e );
                r = r.multiply(c); // coeff ac
                h = S.multiply(a, f); // coeff ac
                r = r.subtract(h);
            } else {
                r = r.multiply(c);
            }
        }
        return r;
    }


    /**
     * GenPolynomial sparse pseudo divide. For univariate polynomials or exact
     * division.
     *
     * @param <C> coefficient type.
     * @param P   GenPolynomial.
     * @param S   nonzero GenPolynomial.
     * @return quotient with ldcf(S)<sup>m'</sup> P = quotient * S + remainder.
     *         m' &le; deg(P)-deg(S)
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial#divide(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial).
     */
    public static <C extends RingElem<C>> GenPolynomial<C> basePseudoDivide(GenPolynomial<C> P,
                                                                            GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            throw new ArithmeticException(P.toString() + " division by zero " + S);
        }
        //if (S.ring.nvar != 1) {
        // ok if exact division
        // throw new RuntimeException(this.getClass().getName()
        //                            + " univariate polynomials only");
        //}
        if (P.isZERO() || S.isONE()) {
            return P;
        }
        C c = S.leadingBaseCoefficient();
        ExpVector e = S.leadingExpVector();
        GenPolynomial<C> h;
        GenPolynomial<C> r = P;
        GenPolynomial<C> q = S.ring.getZERO().copy();

        while (!r.isZERO()) {
            ExpVector f = r.leadingExpVector();
            if (f.multipleOf(e)) {
                C a = r.leadingBaseCoefficient();
                f = f.subtract(e);
                C x = a.remainder(c);
                if (x.isZERO()) {
                    C y = a.divide(c);
                    q = q.sum(y, f);
                    h = S.multiply(y, f); // coeff a
                } else {
                    q = q.multiply(c);
                    q = q.sum(a, f);
                    r = r.multiply(c); // coeff ac
                    h = S.multiply(a, f); // coeff ac
                }
                r = r.subtract(h);
            } else {
                break;
            }
        }
        return q;
    }

    /**
     * GenPolynomial divide. For recursive polynomials. Division by coefficient
     * ring element.
     *
     * @param <C> coefficient type.
     * @param P   recursive GenPolynomial.
     * @param s   GenPolynomial.
     * @return this/s.
     */
    public static <C extends RingElem<C>> GenPolynomial<GenPolynomial<C>> recursiveDivide(
            GenPolynomial<GenPolynomial<C>> P, GenPolynomial<C> s) {
        if (s == null || s.isZERO()) {
            throw new ArithmeticException("division by zero " + P + ", " + s);
        }
        if (P.isZERO()) {
            return P;
        }
        if (s.isONE()) {
            return P;
        }
        GenPolynomial<GenPolynomial<C>> p = P.ring.getZERO().copy();
        SortedMap<ExpVector, GenPolynomial<C>> pv = p.val; //getMap();
        for (Map.Entry<ExpVector, GenPolynomial<C>> m1 : P.getMap().entrySet()) {
            GenPolynomial<C> c1 = m1.getValue();
            ExpVector e1 = m1.getKey();
            GenPolynomial<C> c = PolyUtil.basePseudoDivide(c1, s);
            if (!c.isZERO()) {
                pv.put(e1, c); // or m1.setValue( c )
            } else {
                throw new RuntimeException("something is wrong");
            }
        }
        return p;
    }


    /**
     * GenPolynomial base divide. For recursive polynomials. Division by
     * coefficient ring element.
     *
     * @param <C> coefficient type.
     * @param P   recursive GenPolynomial.
     * @param s   coefficient.
     * @return this/s.
     */
    public static <C extends RingElem<C>> GenPolynomial<GenPolynomial<C>> baseRecursiveDivide(
            GenPolynomial<GenPolynomial<C>> P, C s) {
        if (s == null || s.isZERO()) {
            throw new ArithmeticException("division by zero " + P + ", " + s);
        }
        if (P.isZERO()) {
            return P;
        }
        if (s.isONE()) {
            return P;
        }
        GenPolynomial<GenPolynomial<C>> p = P.ring.getZERO().copy();
        SortedMap<ExpVector, GenPolynomial<C>> pv = p.val; //getMap();
        for (Map.Entry<ExpVector, GenPolynomial<C>> m1 : P.getMap().entrySet()) {
            GenPolynomial<C> c1 = m1.getValue();
            ExpVector e1 = m1.getKey();
            GenPolynomial<C> c = PolyUtil.coefficientBasePseudoDivide(c1, s);
            if (!c.isZERO()) {
                pv.put(e1, c); // or m1.setValue( c )
            } else {
                throw new RuntimeException("something is wrong");
            }
        }
        return p;
    }


    /**
     * GenPolynomial sparse pseudo remainder. For recursive polynomials.
     *
     * @param <C> coefficient type.
     * @param P   recursive GenPolynomial.
     * @param S   nonzero recursive GenPolynomial.
     * @return remainder with ldcf(S)<sup>m'</sup> P = quotient * S + remainder.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial#remainder(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial).
     * @deprecated Use
     *             {@link #recursiveSparsePseudoRemainder(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial, cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial)}
     *             instead
     */
    @Deprecated
    public static <C extends RingElem<C>> GenPolynomial<GenPolynomial<C>> recursivePseudoRemainder(
            GenPolynomial<GenPolynomial<C>> P, GenPolynomial<GenPolynomial<C>> S) {
        return recursiveSparsePseudoRemainder(P, S);
    }


    /**
     * GenPolynomial sparse pseudo remainder. For recursive polynomials.
     *
     * @param <C> coefficient type.
     * @param P   recursive GenPolynomial.
     * @param S   nonzero recursive GenPolynomial.
     * @return remainder with ldcf(S)<sup>m'</sup> P = quotient * S + remainder.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial#remainder(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial).
     */
    public static <C extends RingElem<C>> GenPolynomial<GenPolynomial<C>> recursiveSparsePseudoRemainder(
            GenPolynomial<GenPolynomial<C>> P, GenPolynomial<GenPolynomial<C>> S) {
        if (S == null || S.isZERO()) {
            throw new ArithmeticException(P + " division by zero " + S);
        }
        if (P == null || P.isZERO()) {
            return P;
        }
        if (S.isONE()) {
            return P.ring.getZERO();
        }
        GenPolynomial<C> c = S.leadingBaseCoefficient();
        ExpVector e = S.leadingExpVector();
        GenPolynomial<GenPolynomial<C>> h;
        GenPolynomial<GenPolynomial<C>> r = P;
        while (!r.isZERO()) {
            ExpVector f = r.leadingExpVector();
            if (f.multipleOf(e)) {
                GenPolynomial<C> a = r.leadingBaseCoefficient();
                f = f.subtract(e);
                GenPolynomial<C> x = c; //test basePseudoRemainder(a,c);
                if (x.isZERO()) {
                    GenPolynomial<C> y = PolyUtil.basePseudoDivide(a, c);
                    h = S.multiply(y, f); // coeff a
                } else {
                    r = r.multiply(c); // coeff ac
                    h = S.multiply(a, f); // coeff ac
                }
                r = r.subtract(h);
            } else {
                break;
            }
        }
        return r;
    }


    /**
     * GenPolynomial dense pseudo remainder. For recursive polynomials.
     *
     * @param P recursive GenPolynomial.
     * @param S nonzero recursive GenPolynomial.
     * @return remainder with ldcf(S)<sup>m'</sup> P = quotient * S + remainder.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial#remainder(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial).
     */
    public static <C extends RingElem<C>> GenPolynomial<GenPolynomial<C>> recursiveDensePseudoRemainder(
            GenPolynomial<GenPolynomial<C>> P, GenPolynomial<GenPolynomial<C>> S) {
        if (S == null || S.isZERO()) {
            throw new ArithmeticException(P + " division by zero " + S);
        }
        if (P == null || P.isZERO()) {
            return P;
        }
        if (S.degree() <= 0) {
            return P.ring.getZERO();
        }
        long m = P.degree(0);
        long n = S.degree(0);
        GenPolynomial<C> c = S.leadingBaseCoefficient();
        ExpVector e = S.leadingExpVector();
        GenPolynomial<GenPolynomial<C>> h;
        GenPolynomial<GenPolynomial<C>> r = P;
        for (long i = m; i >= n; i--) {
            if (r.isZERO()) {
                return r;
            }
            long k = r.degree(0);
            if (i == k) {
                ExpVector f = r.leadingExpVector();
                GenPolynomial<C> a = r.leadingBaseCoefficient();
                f = f.subtract(e); //EVDIF( f, e );
                r = r.multiply(c); // coeff ac
                h = S.multiply(a, f); // coeff ac
                r = r.subtract(h);
            } else {
                r = r.multiply(c);
            }
        }
        return r;
    }


    /**
     * GenPolynomial recursive pseudo divide. For recursive polynomials.
     *
     * @param <C> coefficient type.
     * @param P   recursive GenPolynomial.
     * @param S   nonzero recursive GenPolynomial.
     * @return quotient with ldcf(S)<sup>m'</sup> P = quotient * S + remainder.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial#remainder(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial).
     */
    public static <C extends RingElem<C>> GenPolynomial<GenPolynomial<C>> recursivePseudoDivide(
            GenPolynomial<GenPolynomial<C>> P, GenPolynomial<GenPolynomial<C>> S) {
        if (S == null || S.isZERO()) {
            throw new ArithmeticException(P + " division by zero " + S);
        }
        //if (S.ring.nvar != 1) {
        // ok if exact division
        // throw new RuntimeException(this.getClass().getName()
        //                            + " univariate polynomials only");
        //}
        if (P == null || P.isZERO()) {
            return P;
        }
        if (S.isONE()) {
            return P;
        }
        GenPolynomial<C> c = S.leadingBaseCoefficient();
        ExpVector e = S.leadingExpVector();
        GenPolynomial<GenPolynomial<C>> h;
        GenPolynomial<GenPolynomial<C>> r = P;
        GenPolynomial<GenPolynomial<C>> q = S.ring.getZERO().copy();
        while (!r.isZERO()) {
            ExpVector f = r.leadingExpVector();
            if (f.multipleOf(e)) {
                GenPolynomial<C> a = r.leadingBaseCoefficient();
                f = f.subtract(e);
                GenPolynomial<C> x = PolyUtil.baseSparsePseudoRemainder(a, c);
                if (x.isZERO() && !c.isConstant()) {
                    GenPolynomial<C> y = PolyUtil.basePseudoDivide(a, c);
                    q = q.sum(y, f);
                    h = S.multiply(y, f); // coeff a
                } else {
                    q = q.multiply(c);
                    q = q.sum(a, f);
                    r = r.multiply(c); // coeff ac
                    h = S.multiply(a, f); // coeff ac
                }
                r = r.subtract(h);
            } else {
                break;
            }
        }
        return q;
    }


    /**
     * GenPolynomial pseudo divide. For recursive polynomials.
     *
     * @param <C> coefficient type.
     * @param P   recursive GenPolynomial.
     * @param s   nonzero GenPolynomial.
     * @return quotient with ldcf(s)<sup>m</sup> P = quotient * s + remainder.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial#remainder(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial).
     */
    public static <C extends RingElem<C>> GenPolynomial<GenPolynomial<C>> coefficientPseudoDivide(
            GenPolynomial<GenPolynomial<C>> P, GenPolynomial<C> s) {
        if (s == null || s.isZERO()) {
            throw new ArithmeticException(P + " division by zero " + s);
        }
        if (P.isZERO()) {
            return P;
        }
        GenPolynomial<GenPolynomial<C>> p = P.ring.getZERO().copy();
        SortedMap<ExpVector, GenPolynomial<C>> pv = p.val;
        for (Map.Entry<ExpVector, GenPolynomial<C>> m : P.getMap().entrySet()) {
            ExpVector e = m.getKey();
            GenPolynomial<C> c1 = m.getValue();
            GenPolynomial<C> c = basePseudoDivide(c1, s);
            if (debug) {
                GenPolynomial<C> x = c1.remainder(s);
                if (!x.isZERO()) {
                    throw new ArithmeticException(" no exact division: " + c1 + "/" + s);
                }
            }
            if (c.isZERO()) {
                //throw new ArithmeticException(" no exact division: " + c1 + "/" + s);
            } else {
                pv.put(e, c); // or m1.setValue( c )
            }
        }
        return p;
    }


    /**
     * GenPolynomial pseudo divide. For polynomials.
     *
     * @param <C> coefficient type.
     * @param P   GenPolynomial.
     * @param s   nonzero coefficient.
     * @return quotient with ldcf(s)<sup>m</sup> P = quotient * s + remainder.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial#remainder(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial).
     */
    public static <C extends RingElem<C>> GenPolynomial<C> coefficientBasePseudoDivide(GenPolynomial<C> P, C s) {
        if (s == null || s.isZERO()) {
            throw new ArithmeticException(P + " division by zero " + s);
        }
        if (P.isZERO()) {
            return P;
        }
        GenPolynomial<C> p = P.ring.getZERO().copy();
        SortedMap<ExpVector, C> pv = p.val;
        for (Map.Entry<ExpVector, C> m : P.getMap().entrySet()) {
            ExpVector e = m.getKey();
            C c1 = m.getValue();
            C c = c1.divide(s);
            if (debug) {
                C x = c1.remainder(s);
                if (!x.isZERO()) {
                    throw new ArithmeticException(" no exact division: " + c1 + "/" + s);
                }
            }
            if (c.isZERO()) {
                //throw new ArithmeticException(" no exact division: " + c1 + "/" + s);
            } else {
                pv.put(e, c); // or m1.setValue( c )
            }
        }
        return p;
    }


    /**
     * GenPolynomial polynomial derivative main variable.
     *
     * @param <C> coefficient type.
     * @param P   GenPolynomial.
     * @return deriviative(P).
     */
    public static <C extends RingElem<C>> GenPolynomial<C> baseDeriviative(GenPolynomial<C> P) {
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar > 1) {
            // baseContent not possible by return type
            throw new IllegalArgumentException(P.getClass().getName() + " only for univariate polynomials");
        }
        RingFactory<C> rf = pfac.coFac;
        GenPolynomial<C> d = pfac.getZERO().copy();
        Map<ExpVector, C> dm = d.val; //getMap();
        for (Map.Entry<ExpVector, C> m : P.getMap().entrySet()) {
            ExpVector f = m.getKey();
            long fl = f.getVal(0);
            if (fl > 0) {
                C cf = rf.fromInteger(fl);
                C a = m.getValue();
                C x = a.multiply(cf);
                if (x != null && !x.isZERO()) {
                    ExpVector e = ExpVector.create(1, 0, fl - 1L);
                    dm.put(e, x);
                }
            }
        }
        return d;
    }


    /**
     * GenPolynomial recursive polynomial derivative main variable.
     *
     * @param <C> coefficient type.
     * @param P   recursive GenPolynomial.
     * @return deriviative(P).
     */
    public static <C extends RingElem<C>> GenPolynomial<GenPolynomial<C>> recursiveDeriviative(
            GenPolynomial<GenPolynomial<C>> P) {
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomialRing<GenPolynomial<C>> pfac = P.ring;
        if (pfac.nvar > 1) {
            // baseContent not possible by return type
            throw new IllegalArgumentException(P.getClass().getName() + " only for univariate polynomials");
        }
        GenPolynomialRing<C> pr = (GenPolynomialRing<C>) pfac.coFac;
        RingFactory<C> rf = pr.coFac;
        GenPolynomial<GenPolynomial<C>> d = pfac.getZERO().copy();
        Map<ExpVector, GenPolynomial<C>> dm = d.val; //getMap();
        for (Map.Entry<ExpVector, GenPolynomial<C>> m : P.getMap().entrySet()) {
            ExpVector f = m.getKey();
            long fl = f.getVal(0);
            if (fl > 0) {
                C cf = rf.fromInteger(fl);
                GenPolynomial<C> a = m.getValue();
                GenPolynomial<C> x = a.multiply(cf);
                if (x != null && !x.isZERO()) {
                    ExpVector e = ExpVector.create(1, 0, fl - 1L);
                    dm.put(e, x);
                }
            }
        }
        return d;
    }


    /**
     * Factor coefficient bound. See SACIPOL.IPFCB: the product of all maxNorms
     * of potential factors is less than or equal to 2**b times the maxNorm of
     * A.
     *
     * @param e degree vector of a GenPolynomial A.
     * @return 2**b.
     */
    public static BigInteger factorBound(ExpVector e) {
        int n = 0;
        java.math.BigInteger p = java.math.BigInteger.ONE;
        java.math.BigInteger v;
        if (e == null || e.isZERO()) {
            return BigInteger.ONE;
        }
        for (int i = 0; i < e.length(); i++) {
            if (e.getVal(i) > 0) {
                n += (2 * e.getVal(i) - 1);
                v = new java.math.BigInteger("" + (e.getVal(i) - 1));
                p = p.multiply(v);
            }
        }
        n += (p.bitCount() + 1); // log2(p)
        n /= 2;
        v = new java.math.BigInteger("" + 2);
        v = v.shiftLeft(n);
        BigInteger N = new BigInteger(v);
        return N;
    }


    /**
     * Evaluate at main variable.
     *
     * @param <C>  coefficient type.
     * @param cfac coefficent polynomial ring factory.
     * @param A    recursive polynomial to be evaluated.
     * @param a    value to evaluate at.
     * @return A(x_1, ..., x_{n-1}, a).
     */
    public static <C extends RingElem<C>> GenPolynomial<C> evaluateMainRecursive(GenPolynomialRing<C> cfac,
                                                                                 GenPolynomial<GenPolynomial<C>> A, C a) {
        if (A == null || A.isZERO()) {
            return cfac.getZERO();
        }
        if (A.ring.nvar != 1) { // todo assert
            throw new IllegalArgumentException("evaluateMain no univariate polynomial");
        }
        if (a == null || a.isZERO()) {
            return A.trailingBaseCoefficient();
        }
        // assert descending exponents, i.e. compatible term order
        Map<ExpVector, GenPolynomial<C>> val = A.getMap();
        GenPolynomial<C> B = null;
        long el1 = -1; // undefined
        long el2 = -1;
        for (Map.Entry<ExpVector, GenPolynomial<C>> me : val.entrySet()) {
            ExpVector e = me.getKey();
            el2 = e.getVal(0);
            if (B == null /*el1 < 0*/) { // first turn
                B = me.getValue(); //val.get(e);
            } else {
                for (long i = el2; i < el1; i++) {
                    B = B.multiply(a);
                }
                B = B.sum(me.getValue()); //val.get(e));
            }
            el1 = el2;
        }
        for (long i = 0; i < el2; i++) {
            B = B.multiply(a);
        }
        return B;
    }


    /**
     * Evaluate at main variable.
     *
     * @param <C>  coefficient type.
     * @param cfac coefficent polynomial ring factory.
     * @param A    distributed polynomial to be evaluated.
     * @param a    value to evaluate at.
     * @return A(x_1, ..., x_{n-1}, a).
     */
    public static <C extends RingElem<C>> GenPolynomial<C> evaluateMain(GenPolynomialRing<C> cfac,
                                                                        GenPolynomial<C> A, C a) {
        if (A == null || A.isZERO()) {
            return cfac.getZERO();
        }
        GenPolynomialRing<GenPolynomial<C>> rfac = new GenPolynomialRing<>(cfac, 1);
        if (rfac.nvar + cfac.nvar != A.ring.nvar) {
            throw new IllegalArgumentException("evaluateMain number of variabes mismatch");
        }
        GenPolynomial<GenPolynomial<C>> Ap = recursive(rfac, A);
        return PolyUtil.evaluateMainRecursive(cfac, Ap, a);
    }


    /**
     * Evaluate at main variable.
     *
     * @param <C>  coefficient type.
     * @param cfac coefficent ring factory.
     * @param L    list of univariate polynomials to be evaluated.
     * @param a    value to evaluate at.
     * @return list(A( x_1, ..., x_{n-1}, a) ) for A in L.
     */
    public static <C extends RingElem<C>> List<GenPolynomial<C>> evaluateMain(GenPolynomialRing<C> cfac,
                                                                              List<GenPolynomial<C>> L, C a) {
        return ListUtil.map(L, new EvalMainPol<>(cfac, a));
    }


    /**
     * Evaluate at main variable.
     *
     * @param <C>  coefficient type.
     * @param cfac coefficent ring factory.
     * @param A    univariate polynomial to be evaluated.
     * @param a    value to evaluate at.
     * @return A(a).
     */
    public static <C extends RingElem<C>> C evaluateMain(RingFactory<C> cfac, GenPolynomial<C> A, C a) {
        if (A == null || A.isZERO()) {
            return cfac.getZERO();
        }
        if (A.ring.nvar != 1) { // todo assert
            throw new IllegalArgumentException("evaluateMain no univariate polynomial");
        }
        if (a == null || a.isZERO()) {
            return A.trailingBaseCoefficient();
        }
        // assert decreasing exponents, i.e. compatible term order
        Map<ExpVector, C> val = A.getMap();
        C B = null;
        long el1 = -1; // undefined
        long el2 = -1;
        for (Map.Entry<ExpVector, C> me : val.entrySet()) {
            ExpVector e = me.getKey();
            el2 = e.getVal(0);
            if (B == null /*el1 < 0*/) { // first turn
                B = me.getValue(); // val.get(e);
            } else {
                for (long i = el2; i < el1; i++) {
                    B = B.multiply(a);
                }
                B = B.sum(me.getValue()); //val.get(e));
            }
            el1 = el2;
        }
        for (long i = 0; i < el2; i++) {
            B = B.multiply(a);
        }
        return B;
    }


    /**
     * Evaluate at main variable.
     *
     * @param <C>  coefficient type.
     * @param cfac coefficent ring factory.
     * @param L    list of univariate polynomial to be evaluated.
     * @param a    value to evaluate at.
     * @return list(A( a) ) for A in L.
     */
    public static <C extends RingElem<C>> List<C> evaluateMain(RingFactory<C> cfac, List<GenPolynomial<C>> L,
                                                               C a) {
        return ListUtil.map(L, new EvalMain<>(cfac, a));
    }

    /**
     * Evaluate at first (lowest) variable.
     *
     * @param <C>  coefficient type. Could also be called evaluateFirst(), but
     *             type erasure of A parameter does not allow same name.
     * @param cfac coefficient polynomial ring in first variable C[x_1] factory.
     * @param dfac polynomial ring in n-1 variables. C[x_2, ..., x_n] factory.
     * @param A    recursive polynomial to be evaluated.
     * @param a    value to evaluate at.
     * @return A(a, x_2, ..., x_n).
     */
    public static <C extends RingElem<C>> GenPolynomial<C> evaluateFirstRec(GenPolynomialRing<C> cfac,
                                                                            GenPolynomialRing<C> dfac, GenPolynomial<GenPolynomial<C>> A, C a) {
        if (A == null || A.isZERO()) {
            return dfac.getZERO();
        }
        Map<ExpVector, GenPolynomial<C>> Ap = A.getMap();
        GenPolynomial<C> B = dfac.getZERO().copy();
        Map<ExpVector, C> Bm = B.val; //getMap();
        for (Map.Entry<ExpVector, GenPolynomial<C>> m : Ap.entrySet()) {
            ExpVector e = m.getKey();
            GenPolynomial<C> b = m.getValue();
            C d = evaluateMain(cfac.coFac, b, a);
            if (d != null && !d.isZERO()) {
                Bm.put(e, d);
            }
        }
        return B;
    }


    /**
     * Substitute main variable.
     *
     * @param A univariate polynomial.
     * @param s polynomial for substitution.
     * @return polynomial A(x <- s).
     */
    public static <C extends RingElem<C>> GenPolynomial<C> substituteMain(GenPolynomial<C> A,
                                                                          GenPolynomial<C> s) {
        return substituteUnivariate(A, s);
    }


    /**
     * Substitute univariate polynomial.
     *
     * @param f univariate polynomial.
     * @param t polynomial for substitution.
     * @return polynomial A(x <- t).
     */
    public static <C extends RingElem<C>> GenPolynomial<C> substituteUnivariate(GenPolynomial<C> f,
                                                                                GenPolynomial<C> t) {
        if (f == null || t == null) {
            return null;
        }
        GenPolynomialRing<C> fac = f.ring;
        if (fac.nvar > 1) {
            throw new IllegalArgumentException("only for univariate polynomial f");
        }
        if (f.isZERO() || f.isConstant()) {
            return f;
        }
        if (t.ring.nvar > 1) {
            fac = t.ring;
        }
        // assert decending exponents, i.e. compatible term order
        Map<ExpVector, C> val = f.getMap();
        GenPolynomial<C> s = null;
        long el1 = -1; // undefined
        long el2 = -1;
        for (Map.Entry<ExpVector, C> me : val.entrySet()) {
            ExpVector e = me.getKey();
            el2 = e.getVal(0);
            if (s == null /*el1 < 0*/) { // first turn
                s = fac.getZERO().sum(me.getValue()); //val.get(e));
            } else {
                for (long i = el2; i < el1; i++) {
                    s = s.multiply(t);
                }
                s = s.sum(me.getValue()); //val.get(e));
            }
            el1 = el2;
        }
        for (long i = 0; i < el2; i++) {
            s = s.multiply(t);
        }
        return s;
    }


    /**
     * ModInteger interpolate on first variable.
     *
     * @param <C> coefficient type.
     * @param fac GenPolynomial<C> result factory.
     * @param A   GenPolynomial.
     * @param M   GenPolynomial interpolation modul of A.
     * @param mi  inverse of M(am) in ring fac.coFac.
     * @param B   evaluation of other GenPolynomial.
     * @param am  evaluation point (interpolation modul) of B, i.e. P(am) = B.
     * @return S, with S mod M == A and S(am) == B.
     */
    public static <C extends RingElem<C>> GenPolynomial<GenPolynomial<C>> interpolate(
            GenPolynomialRing<GenPolynomial<C>> fac, GenPolynomial<GenPolynomial<C>> A,
            GenPolynomial<C> M, C mi, GenPolynomial<C> B, C am) {
        GenPolynomial<GenPolynomial<C>> S = fac.getZERO().copy();
        GenPolynomial<GenPolynomial<C>> Ap = A.copy();
        SortedMap<ExpVector, GenPolynomial<C>> av = Ap.val; //getMap();
        SortedMap<ExpVector, C> bv = B.getMap();
        SortedMap<ExpVector, GenPolynomial<C>> sv = S.val; //getMap();
        GenPolynomialRing<C> cfac = (GenPolynomialRing<C>) fac.coFac;
        RingFactory<C> bfac = cfac.coFac;
        GenPolynomial<C> c = null;
        for (Map.Entry<ExpVector, C> me : bv.entrySet()) {
            ExpVector e = me.getKey();
            C y = me.getValue(); //bv.get(e); // assert y != null
            GenPolynomial<C> x = av.get(e);
            if (x != null) {
                av.remove(e);
                c = PolyUtil.interpolate(cfac, x, M, mi, y, am);
                if (!c.isZERO()) { // 0 cannot happen
                    sv.put(e, c);
                }
            } else {
                c = PolyUtil.interpolate(cfac, cfac.getZERO(), M, mi, y, am);
                if (!c.isZERO()) { // 0 cannot happen
                    sv.put(e, c); // c != null
                }
            }
        }
        // assert bv is empty = done
        for (Map.Entry<ExpVector, GenPolynomial<C>> me : av.entrySet()) { // rest of av
            ExpVector e = me.getKey();
            GenPolynomial<C> x = me.getValue(); //av.get(e); // assert x != null
            c = PolyUtil.interpolate(cfac, x, M, mi, bfac.getZERO(), am);
            if (!c.isZERO()) { // 0 cannot happen
                sv.put(e, c); // c != null
            }
        }
        return S;
    }


    /**
     * Univariate polynomial interpolation.
     *
     * @param <C> coefficient type.
     * @param fac GenPolynomial<C> result factory.
     * @param A   GenPolynomial.
     * @param M   GenPolynomial interpolation modul of A.
     * @param mi  inverse of M(am) in ring fac.coFac.
     * @param a   evaluation of other GenPolynomial.
     * @param am  evaluation point (interpolation modul) of a, i.e. P(am) = a.
     * @return S, with S mod M == A and S(am) == a.
     */
    public static <C extends RingElem<C>> GenPolynomial<C> interpolate(GenPolynomialRing<C> fac,
                                                                       GenPolynomial<C> A, GenPolynomial<C> M, C mi, C a, C am) {
        GenPolynomial<C> s;
        C b = PolyUtil.evaluateMain(fac.coFac, A, am);
        // A mod a.modul
        C d = a.subtract(b); // a-A mod a.modul
        if (d.isZERO()) {
            return A;
        }
        b = d.multiply(mi); // b = (a-A)*mi mod a.modul
        // (M*b)+A mod M = A mod M = 
        // (M*mi*(a-A)+A) mod a.modul = a mod a.modul
        s = M.multiply(b);
        s = s.sum(A);
        return s;
    }


    /**
     * Recursive GenPolynomial switch varaible blocks.
     *
     * @param <C> coefficient type.
     * @param P   recursive GenPolynomial in R[X,Y].
     * @return this in R[Y,X].
     */
    public static <C extends RingElem<C>> GenPolynomial<GenPolynomial<C>> switchVariables(
            GenPolynomial<GenPolynomial<C>> P) {
        if (P == null) {
            throw new IllegalArgumentException("P == null");
        }
        GenPolynomialRing<GenPolynomial<C>> rfac1 = P.ring;
        GenPolynomialRing<C> cfac1 = (GenPolynomialRing<C>) rfac1.coFac;
        GenPolynomialRing<C> cfac2 = new GenPolynomialRing<>(cfac1.coFac, rfac1);
        GenPolynomial<C> zero = cfac2.getZERO();
        GenPolynomialRing<GenPolynomial<C>> rfac2 = new GenPolynomialRing<>(cfac2, cfac1);
        GenPolynomial<GenPolynomial<C>> B = rfac2.getZERO().copy();
        if (P.isZERO()) {
            return B;
        }
        for (Monomial<GenPolynomial<C>> mr : P) {
            GenPolynomial<C> cr = mr.c;
            for (Monomial<C> mc : cr) {
                GenPolynomial<C> c = zero.sum(mc.c, mr.e);
                B = B.sum(c, mc.e);
            }
        }
        return B;
    }


    /**
     * Maximal degree in the coefficient polynomials.
     *
     * @param <C> coefficient type.
     * @return maximal degree in the coefficients.
     */
    public static <C extends RingElem<C>> long coeffMaxDegree(GenPolynomial<GenPolynomial<C>> A) {
        if (A.isZERO()) {
            return 0; // 0 or -1 ?;
        }
        long deg = 0;
        for (GenPolynomial<C> a : A.getMap().values()) {
            long d = a.degree();
            if (d > deg) {
                deg = d;
            }
        }
        return deg;
    }


    /**
     * Map a unary function to the coefficients.
     *
     * @param ring result polynomial ring factory.
     * @param p    polynomial.
     * @param f    evaluation functor.
     * @return new polynomial with coefficients f(p(e)).
     */
    public static <C extends RingElem<C>, D extends RingElem<D>> GenPolynomial<D> map(
            GenPolynomialRing<D> ring, GenPolynomial<C> p, UnaryFunctor<C, D> f) {
        GenPolynomial<D> n = ring.getZERO().copy();
        SortedMap<ExpVector, D> nv = n.val;
        for (Monomial<C> m : p) {
            D c = f.eval(m.c);
            if (c != null && !c.isZERO()) {
                nv.put(m.e, c);
            }
        }
        return n;
    }


    /**
     * Remove all upper variables which do not occur in polynomial.
     *
     * @param p polynomial.
     * @return polynomial with removed variables
     */
    public static <C extends RingElem<C>> GenPolynomial<C> removeUnusedUpperVariables(GenPolynomial<C> p) {
        GenPolynomialRing<C> fac = p.ring;
        if (fac.nvar <= 1) { // univariate
            return p;
        }
        int[] dep = p.degreeVector().dependencyOnVariables();
        if (fac.nvar == dep.length) { // all variables appear
            return p;
        }
        if (dep.length == 0) { // no variables
            GenPolynomialRing<C> fac0 = new GenPolynomialRing<>(fac.coFac, 0);
            GenPolynomial<C> p0 = new GenPolynomial<>(fac0, p.leadingBaseCoefficient());
            return p0;
        }
        int l = dep[0]; // higher variable
        int r = dep[dep.length - 1]; // lower variable
        if (l == 0 /*|| l == fac.nvar-1*/) { // upper variable appears
            return p;
        }
        int n = l;
        GenPolynomialRing<C> facr = fac.contract(n);
        Map<ExpVector, GenPolynomial<C>> mpr = p.contract(facr);
        if (mpr.size() != 1) {
            throw new RuntimeException("this should not happen " + mpr);
        }
        GenPolynomial<C> pr = mpr.values().iterator().next();
        n = fac.nvar - 1 - r;
        if (n == 0) {
            return pr;
        } // else case not implemented
        return pr;
    }

}


/**
 * Conversion of distributive to recursive representation.
 */
class DistToRec<C extends RingElem<C>> implements
        UnaryFunctor<GenPolynomial<C>, GenPolynomial<GenPolynomial<C>>> {


    GenPolynomialRing<GenPolynomial<C>> fac;


    public DistToRec(GenPolynomialRing<GenPolynomial<C>> fac) {
        this.fac = fac;
    }


    public GenPolynomial<GenPolynomial<C>> eval(GenPolynomial<C> c) {
        if (c == null) {
            return fac.getZERO();
        }
        return PolyUtil.recursive(fac, c);
    }
}


/**
 * Conversion of symmetric ModInteger to BigInteger functor.
 */
class ModSymToInt<C extends RingElem<C> & Modular> implements UnaryFunctor<C, BigInteger> {


    public BigInteger eval(C c) {
        if (c == null) {
            return new BigInteger();
        }
        return c.getSymmetricInteger();
    }
}


/**
 * Conversion of BigRational to BigInteger with division by lcm functor. result
 * = num*(lcm/denom).
 */
class RatToInt implements UnaryFunctor<BigRational, BigInteger> {


    java.math.BigInteger lcm;


    public RatToInt(java.math.BigInteger lcm) {
        this.lcm = lcm; //.getVal();
    }


    public BigInteger eval(BigRational c) {
        if (c == null) {
            return new BigInteger();
        }
        // p = num*(lcm/denom)
        java.math.BigInteger b = lcm.divide(c.denominator());
        return new BigInteger(c.numerator().multiply(b));
    }
}


/**
 * Conversion from BigInteger functor.
 */
class FromInteger<D extends RingElem<D>> implements UnaryFunctor<BigInteger, D> {


    RingFactory<D> ring;


    public FromInteger(RingFactory<D> ring) {
        this.ring = ring;
    }


    public D eval(BigInteger c) {
        if (c == null) {
            return ring.getZERO();
        }
        return ring.fromInteger(c.getVal());
    }
}


/**
 * Conversion from GenPolynomial<BigInteger> functor.
 */
class FromIntegerPoly<D extends RingElem<D>> implements
        UnaryFunctor<GenPolynomial<BigInteger>, GenPolynomial<D>> {


    GenPolynomialRing<D> ring;


    FromInteger<D> fi;


    public FromIntegerPoly(GenPolynomialRing<D> ring) {
        if (ring == null) {
            throw new IllegalArgumentException("ring must not be null");
        }
        this.ring = ring;
        fi = new FromInteger<>(ring.coFac);
    }


    public GenPolynomial<D> eval(GenPolynomial<BigInteger> c) {
        if (c == null) {
            return ring.getZERO();
        }
        return PolyUtil.map(ring, c, fi);
    }
}


/**
 * Algebraic to generic complex functor.
 */
class AlgebToCompl<C extends GcdRingElem<C>> implements UnaryFunctor<AlgebraicNumber<C>, Complex<C>> {


    final protected ComplexRing<C> cfac;


    public AlgebToCompl(ComplexRing<C> fac) {
        if (fac == null) {
            throw new IllegalArgumentException("fac must not be null");
        }
        cfac = fac;
    }


    public Complex<C> eval(AlgebraicNumber<C> a) {
        if (a == null || a.isZERO()) { // should not happen
            return cfac.getZERO();
        } else if (a.isONE()) {
            return cfac.getONE();
        } else {
            GenPolynomial<C> p = a.getVal();
            C real = cfac.ring.getZERO();
            C imag = cfac.ring.getZERO();
            for (Monomial<C> m : p) {
                if (m.exponent().getVal(0) == 1L) {
                    imag = m.coefficient();
                } else if (m.exponent().getVal(0) == 0L) {
                    real = m.coefficient();
                } else {
                    throw new IllegalArgumentException("unexpected monomial " + m);
                }
            }
            //Complex<C> c = new Complex<C>(cfac,real,imag);
            return new Complex<>(cfac, real, imag);
        }
    }
}


/**
 * Ceneric complex to algebraic number functor.
 */
class ComplToAlgeb<C extends GcdRingElem<C>> implements UnaryFunctor<Complex<C>, AlgebraicNumber<C>> {


    final protected AlgebraicNumberRing<C> afac;


    final protected AlgebraicNumber<C> I;


    public ComplToAlgeb(AlgebraicNumberRing<C> fac) {
        if (fac == null) {
            throw new IllegalArgumentException("fac must not be null");
        }
        afac = fac;
        I = afac.getGenerator();
    }


    public AlgebraicNumber<C> eval(Complex<C> c) {
        if (c == null || c.isZERO()) { // should not happen
            return afac.getZERO();
        } else if (c.isONE()) {
            return afac.getONE();
        } else if (c.isIMAG()) {
            return I;
        } else {
            return I.multiply(c.getIm()).sum(c.getRe());
        }
    }
}


/**
 * Algebraic to polynomial functor.
 */
class AlgToPoly<C extends GcdRingElem<C>> implements UnaryFunctor<AlgebraicNumber<C>, GenPolynomial<C>> {


    public GenPolynomial<C> eval(AlgebraicNumber<C> c) {
        if (c == null) {
            return null;
        }
        return c.val;
    }
}


/**
 * Coefficient to algebriac functor.
 */
class CoeffToAlg<C extends GcdRingElem<C>> implements UnaryFunctor<C, AlgebraicNumber<C>> {


    final protected AlgebraicNumberRing<C> afac;


    final protected GenPolynomial<C> zero;


    public CoeffToAlg(AlgebraicNumberRing<C> fac) {
        if (fac == null) {
            throw new IllegalArgumentException("fac must not be null");
        }
        afac = fac;
        GenPolynomialRing<C> pfac = afac.ring;
        zero = pfac.getZERO();
    }


    public AlgebraicNumber<C> eval(C c) {
        if (c == null) {
            return afac.getZERO();
        }
        return new AlgebraicNumber<>(afac, zero.sum(c));
    }
}

/**
 * Evaluate main variable functor.
 */
class EvalMain<C extends RingElem<C>> implements UnaryFunctor<GenPolynomial<C>, C> {


    final RingFactory<C> cfac;


    final C a;


    public EvalMain(RingFactory<C> cfac, C a) {
        this.cfac = cfac;
        this.a = a;
    }


    public C eval(GenPolynomial<C> c) {
        if (c == null) {
            return cfac.getZERO();
        }
        return PolyUtil.evaluateMain(cfac, c, a);
    }
}


/**
 * Evaluate main variable functor.
 */
class EvalMainPol<C extends RingElem<C>> implements UnaryFunctor<GenPolynomial<C>, GenPolynomial<C>> {


    final GenPolynomialRing<C> cfac;


    final C a;


    public EvalMainPol(GenPolynomialRing<C> cfac, C a) {
        this.cfac = cfac;
        this.a = a;
    }


    public GenPolynomial<C> eval(GenPolynomial<C> c) {
        if (c == null) {
            return cfac.getZERO();
        }
        return PolyUtil.evaluateMain(cfac, c, a);
    }
}
/**
 *  * Conversion of BigRational to BigInteger. result = (num/gcd)*(lcm/denom).  
 */
class RatToIntFactor implements UnaryFunctor<BigRational, BigInteger> {


    final java.math.BigInteger lcm;


    final java.math.BigInteger gcd;


    public RatToIntFactor(java.math.BigInteger gcd, java.math.BigInteger lcm) {
        this.gcd = gcd;
        this.lcm = lcm; // .getVal();
    }


    public BigInteger eval(BigRational c) {
        if (c == null) {
            return new BigInteger();
        }
        if (gcd.equals(java.math.BigInteger.ONE)) {
            // p = num*(lcm/denom)
            java.math.BigInteger b = lcm.divide(c.denominator());
            return new BigInteger(c.numerator().multiply(b));
        }
        // p = (num/gcd)*(lcm/denom)
        java.math.BigInteger a = c.numerator().divide(gcd);
        java.math.BigInteger b = lcm.divide(c.denominator());
        return new BigInteger(a.multiply(b));
    }
}