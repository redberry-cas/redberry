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
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Power;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Hensel utilities for ufd.
 *
 * @author Heinz Kredel
 */

public class HenselUtil {


    /**
     * Modular quadratic Hensel lifting algorithm on coefficients. Let p =
     * A.ring.coFac.modul() = B.ring.coFac.modul() and assume C == A*B mod p
     * with gcd(A,B) == 1 mod p and S A + T B == 1 mod p. See algorithm 6.1. in
     * Geddes et.al. and algorithms 3.5.{5,6} in Cohen. Quadratic version, as it
     * also lifts S A + T B == 1 mod p^{e+1}.
     *
     * @param C GenPolynomial
     * @param A GenPolynomial
     * @param B other GenPolynomial
     * @param S GenPolynomial
     * @param T GenPolynomial
     * @param M bound on the coefficients of A1 and B1 as factors of C.
     * @return [A1, B1] = lift(C,A,B), with C = A1 * B1.
     */
    @SuppressWarnings("unchecked")
    public static <MOD extends GcdRingElem<MOD> & Modular> HenselApprox<MOD> liftHenselQuadratic(
            GenPolynomial<BigInteger> C, BigInteger M, GenPolynomial<MOD> A, GenPolynomial<MOD> B,
            GenPolynomial<MOD> S, GenPolynomial<MOD> T) throws NoLiftingException {
        if (C == null || C.isZERO()) {
            return new HenselApprox<>(C, C, A, B);
        }
        if (A == null || A.isZERO() || B == null || B.isZERO()) {
            throw new IllegalArgumentException("A and B must be nonzero");
        }
        GenPolynomialRing<BigInteger> fac = C.ring;
        if (fac.nvar != 1) { // todo assert
            throw new IllegalArgumentException("polynomial ring not univariate");
        }
        // setup factories
        GenPolynomialRing<MOD> pfac = A.ring;
        RingFactory<MOD> p = pfac.coFac;
        RingFactory<MOD> q = p;
        ModularRingFactory<MOD> P = (ModularRingFactory<MOD>) p;
        ModularRingFactory<MOD> Q = (ModularRingFactory<MOD>) q;
        BigInteger Qi = Q.getIntegerModul();
        BigInteger M2 = M.multiply(M.fromInteger(2));
        BigInteger Mq = Qi;
        GenPolynomialRing<MOD> qfac;
        qfac = new GenPolynomialRing<>(Q, pfac);

        // normalize c and a, b factors, assert p is prime
        GenPolynomial<BigInteger> Ai;
        GenPolynomial<BigInteger> Bi;
        BigInteger c = C.leadingBaseCoefficient();
        C = C.multiply(c); // sic
        MOD a = A.leadingBaseCoefficient();
        if (!a.isONE()) { // A = A.monic();
            A = A.divide(a);
            S = S.multiply(a);
        }
        MOD b = B.leadingBaseCoefficient();
        if (!b.isONE()) { // B = B.monic();
            B = B.divide(b);
            T = T.multiply(b);
        }
        MOD cm = P.fromInteger(c.getVal());
        A = A.multiply(cm);
        B = B.multiply(cm);
        T = T.divide(cm);
        S = S.divide(cm);
        Ai = PolyUtil.integerFromModularCoefficients(fac, A);
        Bi = PolyUtil.integerFromModularCoefficients(fac, B);
        // replace leading base coefficients
        ExpVector ea = Ai.leadingExpVector();
        ExpVector eb = Bi.leadingExpVector();
        Ai.doPutToMap(ea, c);
        Bi.doPutToMap(eb, c);

        // polynomials mod p
        GenPolynomial<MOD> Ap;
        GenPolynomial<MOD> Bp;
        GenPolynomial<MOD> A1p = A;
        GenPolynomial<MOD> B1p = B;
        GenPolynomial<MOD> Ep;
        GenPolynomial<MOD> Sp = S;
        GenPolynomial<MOD> Tp = T;

        // polynomials mod q
        GenPolynomial<MOD> Aq;
        GenPolynomial<MOD> Bq;
        GenPolynomial<MOD> Eq;

        // polynomials over the integers
        GenPolynomial<BigInteger> E;
        GenPolynomial<BigInteger> Ea;
        GenPolynomial<BigInteger> Eb;
        GenPolynomial<BigInteger> Ea1;
        GenPolynomial<BigInteger> Eb1;
        GenPolynomial<BigInteger> Si;
        GenPolynomial<BigInteger> Ti;

        Si = PolyUtil.integerFromModularCoefficients(fac, S);
        Ti = PolyUtil.integerFromModularCoefficients(fac, T);

        Aq = PolyUtil.fromIntegerCoefficients(qfac, Ai);
        Bq = PolyUtil.fromIntegerCoefficients(qfac, Bi);

        while (Mq.compareTo(M2) < 0) {
            // compute E=(C-AB)/q over the integers
            E = C.subtract(Ai.multiply(Bi));
            if (E.isZERO()) {
                break;
            }
            E = E.divide(Qi);
            // E mod p
            Ep = PolyUtil.fromIntegerCoefficients(qfac, E);
            //if (Ep.isZERO()) {
            //??break;
            //}

            // construct approximation mod p
            Ap = Sp.multiply(Ep); // S,T ++ T,S
            Bp = Tp.multiply(Ep);
            GenPolynomial<MOD>[] QR;
            QR = Ap.quotientRemainder(Bq);
            GenPolynomial<MOD> Qp;
            GenPolynomial<MOD> Rp;
            Qp = QR[0];
            Rp = QR[1];
            A1p = Rp;
            B1p = Bp.sum(Aq.multiply(Qp));

            // construct q-adic approximation, convert to integer
            Ea = PolyUtil.integerFromModularCoefficients(fac, A1p);
            Eb = PolyUtil.integerFromModularCoefficients(fac, B1p);
            Ea1 = Ea.multiply(Qi);
            Eb1 = Eb.multiply(Qi);
            Ea = Ai.sum(Eb1); // Eb1 and Ea1 are required
            Eb = Bi.sum(Ea1); //--------------------------
            assert (Ea.degree(0) + Eb.degree(0) <= C.degree(0));
            //if ( Ea.degree(0)+Eb.degree(0) > C.degree(0) ) { // debug
            //   throw new RuntimeException("deg(A)+deg(B) > deg(C)");
            //}
            Ai = Ea;
            Bi = Eb;

            // gcd representation factors error --------------------------------
            // compute E=(1-SA-TB)/q over the integers
            E = fac.getONE();
            E = E.subtract(Si.multiply(Ai)).subtract(Ti.multiply(Bi));
            E = E.divide(Qi);
            // E mod q
            Ep = PolyUtil.fromIntegerCoefficients(qfac, E);

            // construct approximation mod q
            Ap = Sp.multiply(Ep); // S,T ++ T,S
            Bp = Tp.multiply(Ep);
            QR = Bp.quotientRemainder(Aq); // Ai == A mod p ?
            Qp = QR[0];
            Rp = QR[1];
            B1p = Rp;
            A1p = Ap.sum(Bq.multiply(Qp));

            // construct q-adic approximation, convert to integer
            Ea = PolyUtil.integerFromModularCoefficients(fac, A1p);
            Eb = PolyUtil.integerFromModularCoefficients(fac, B1p);
            Ea1 = Ea.multiply(Qi);
            Eb1 = Eb.multiply(Qi);
            Ea = Si.sum(Ea1); // Eb1 and Ea1 are required
            Eb = Ti.sum(Eb1); //--------------------------
            Si = Ea;
            Ti = Eb;

            // prepare for next iteration
            Mq = Qi;
            Qi = Q.getIntegerModul().multiply(Q.getIntegerModul());
            if (ModLongRing.MAX_LONG.compareTo(Qi.getVal()) > 0) {
                Q = (ModularRingFactory) new ModLongRing(Qi.getVal());
            } else {
                Q = (ModularRingFactory) new ModIntegerRing(Qi.getVal());
            }
            //Q = new ModIntegerRing(Qi.getVal());

            qfac = new GenPolynomialRing<>(Q, pfac);

            Aq = PolyUtil.fromIntegerCoefficients(qfac, Ai);
            Bq = PolyUtil.fromIntegerCoefficients(qfac, Bi);
            Sp = PolyUtil.fromIntegerCoefficients(qfac, Si);
            Tp = PolyUtil.fromIntegerCoefficients(qfac, Ti);
        }
        GreatestCommonDivisorAbstract<BigInteger> ufd = new GreatestCommonDivisorPrimitive<>();

        // remove normalization if possible
        BigInteger ai = ufd.baseContent(Ai);
        Ai = Ai.divide(ai);
        BigInteger bi = null;
        try {
            bi = c.divide(ai);
            Bi = Bi.divide(bi); // divide( c/a )
        } catch (RuntimeException e) {
            throw new NoLiftingException("no exact lifting possible " + e);
        }
        return new HenselApprox<>(Ai, Bi, A1p, B1p);
    }


    /**
     * Modular quadratic Hensel lifting algorithm on coefficients. Let p =
     * A.ring.coFac.modul() = B.ring.coFac.modul() and assume C == A*B mod p
     * with gcd(A,B) == 1 mod p. See algorithm 6.1. in Geddes et.al. and
     * algorithms 3.5.{5,6} in Cohen. Quadratic version.
     *
     * @param C GenPolynomial
     * @param A GenPolynomial
     * @param B other GenPolynomial
     * @param M bound on the coefficients of A1 and B1 as factors of C.
     * @return [A1, B1] = lift(C,A,B), with C = A1 * B1.
     */
    @SuppressWarnings("unchecked")
    public static <MOD extends GcdRingElem<MOD> & Modular> HenselApprox<MOD> liftHenselQuadratic(
            GenPolynomial<BigInteger> C, BigInteger M, GenPolynomial<MOD> A, GenPolynomial<MOD> B)
            throws NoLiftingException {
        if (C == null || C.isZERO()) {
            return new HenselApprox<>(C, C, A, B);
        }
        if (A == null || A.isZERO() || B == null || B.isZERO()) {
            throw new IllegalArgumentException("A and B must be nonzero");
        }
        GenPolynomialRing<BigInteger> fac = C.ring;
        if (fac.nvar != 1) { // todo assert
            throw new IllegalArgumentException("polynomial ring not univariate");
        }
        // one Hensel step on part polynomials
        try {
            GenPolynomial<MOD>[] gst = A.egcd(B);
            if (!gst[0].isONE()) {
                throw new NoLiftingException("A and B not coprime, gcd = " + gst[0] + ", A = " + A + ", B = " + B);
            }
            GenPolynomial<MOD> s = gst[1];
            GenPolynomial<MOD> t = gst[2];
            HenselApprox<MOD> ab = HenselUtil.liftHenselQuadratic(C, M, A, B, s, t);
            return ab;
        } catch (ArithmeticException e) {
            throw new NoLiftingException("coefficient error " + e);
        }
    }


    /**
     * Constructing and lifting algorithm for extended Euclidean relation. Let p
     * = A.ring.coFac.modul() and assume gcd(A,B) == 1 mod p.
     *
     * @param A modular GenPolynomial
     * @param B modular GenPolynomial
     * @param k desired approximation exponent p^k.
     * @return [s, t] with s A + t B = 1 mod p^k.
     */
    public static <MOD extends GcdRingElem<MOD> & Modular> GenPolynomial<MOD>[] liftExtendedEuclidean(
            GenPolynomial<MOD> A, GenPolynomial<MOD> B, long k) throws NoLiftingException {
        if (A == null || A.isZERO() || B == null || B.isZERO()) {
            throw new IllegalArgumentException("A and B must be nonzero, A = " + A + ", B = " + B);
        }
        GenPolynomialRing<MOD> fac = A.ring;
        if (fac.nvar != 1) { // todo assert
            throw new IllegalArgumentException("polynomial ring not univariate");
        }
        // start with extended Euclidean relation mod p
        GenPolynomial<MOD>[] gst = null;
        try {
            gst = A.egcd(B);
            if (!gst[0].isONE()) {
                throw new NoLiftingException("A and B not coprime, gcd = " + gst[0] + ", A = " + A + ", B = " + B);
            }
        } catch (ArithmeticException e) {
            throw new NoLiftingException("coefficient error " + e);
        }
        GenPolynomial<MOD> S = gst[1];
        GenPolynomial<MOD> T = gst[2];

        // setup integer polynomial ring
        GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<>(new BigInteger(), fac);
        GenPolynomial<BigInteger> one = ifac.getONE();
        GenPolynomial<BigInteger> Ai = PolyUtil.integerFromModularCoefficients(ifac, A);
        GenPolynomial<BigInteger> Bi = PolyUtil.integerFromModularCoefficients(ifac, B);
        GenPolynomial<BigInteger> Si = PolyUtil.integerFromModularCoefficients(ifac, S);
        GenPolynomial<BigInteger> Ti = PolyUtil.integerFromModularCoefficients(ifac, T);

        // approximate mod p^i
        ModularRingFactory<MOD> mcfac = (ModularRingFactory<MOD>) fac.coFac;
        BigInteger p = mcfac.getIntegerModul();
        BigInteger modul = p;
        GenPolynomialRing<MOD> mfac; // = new GenPolynomialRing<MOD>(mcfac, fac);
        for (int i = 1; i < k; i++) {
            // e = 1 - s a - t b in Z[x]
            GenPolynomial<BigInteger> e = one.subtract(Si.multiply(Ai)).subtract(Ti.multiply(Bi));
            if (e.isZERO()) {
                break;
            }
            e = e.divide(modul);
            // move to Z_p[x] and compute next approximation 
            GenPolynomial<MOD> c = PolyUtil.fromIntegerCoefficients(fac, e);
            GenPolynomial<MOD> s = S.multiply(c);
            GenPolynomial<MOD> t = T.multiply(c);

            GenPolynomial<MOD>[] QR = s.quotientRemainder(B); // watch for ordering 
            GenPolynomial<MOD> q = QR[0];
            s = QR[1];
            t = t.sum(q.multiply(A));

            GenPolynomial<BigInteger> si = PolyUtil.integerFromModularCoefficients(ifac, s);
            GenPolynomial<BigInteger> ti = PolyUtil.integerFromModularCoefficients(ifac, t);
            // add approximation to solution
            Si = Si.sum(si.multiply(modul));
            Ti = Ti.sum(ti.multiply(modul));
            modul = modul.multiply(p);
        }
        // setup ring mod p^i
        if (ModLongRing.MAX_LONG.compareTo(modul.getVal()) > 0) {
            mcfac = (ModularRingFactory) new ModLongRing(modul.getVal());
        } else {
            mcfac = (ModularRingFactory) new ModIntegerRing(modul.getVal());
        }
        mfac = new GenPolynomialRing<>(mcfac, fac);
        S = PolyUtil.fromIntegerCoefficients(mfac, Si);
        T = PolyUtil.fromIntegerCoefficients(mfac, Ti);
        GenPolynomial<MOD>[] rel = (GenPolynomial<MOD>[]) new GenPolynomial[2];
        rel[0] = S;
        rel[1] = T;
        return rel;
    }


    /**
     * Constructing and lifting algorithm for extended Euclidean relation. Let p
     * = A_i.ring.coFac.modul() and assume gcd(A_i,A_j) == 1 mod p, i != j.
     *
     * @param A list of modular GenPolynomials
     * @param k desired approximation exponent p^k.
     * @return [s_0, ..., s_n-1] with sum_i s_i * B_i = 1 mod p^k, with B_i =
     *         prod_{i!=j} A_j.
     */
    public static <MOD extends GcdRingElem<MOD> & Modular> List<GenPolynomial<MOD>> liftExtendedEuclidean(
            List<GenPolynomial<MOD>> A, long k) throws NoLiftingException {
        if (A == null || A.size() == 0) {
            throw new IllegalArgumentException("A must be non null and non empty");
        }
        GenPolynomialRing<MOD> fac = A.get(0).ring;
        if (fac.nvar != 1) { // todo assert
            throw new IllegalArgumentException("polynomial ring not univariate");
        }
        GenPolynomial<MOD> zero = fac.getZERO();
        int r = A.size();
        List<GenPolynomial<MOD>> Q = new ArrayList<>(r);
        for (GenPolynomial<MOD> aA1 : A) {
            Q.add(zero);
        }
        Q.set(r - 2, A.get(r - 1));
        for (int j = r - 3; j >= 0; j--) {
            GenPolynomial<MOD> q = A.get(j + 1).multiply(Q.get(j + 1));
            Q.set(j, q);
        }
        List<GenPolynomial<MOD>> B = new ArrayList<>(r + 1);
        List<GenPolynomial<MOD>> lift = new ArrayList<>(r);
        for (GenPolynomial<MOD> aA : A) {
            B.add(zero);
            lift.add(zero);
        }
        GenPolynomial<MOD> one = fac.getONE();
        GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<>(new BigInteger(), fac);
        B.add(0, one);
        GenPolynomial<MOD> b = one;
        for (int j = 0; j < r - 1; j++) {
            List<GenPolynomial<MOD>> S = liftDiophant(Q.get(j), A.get(j), B.get(j), k);
            b = S.get(0);
            GenPolynomial<MOD> bb = PolyUtil.fromIntegerCoefficients(fac, PolyUtil
                    .integerFromModularCoefficients(ifac, b));
            B.set(j + 1, bb);
            lift.set(j, S.get(1));
        }
        lift.set(r - 1, b);
        return lift;
    }


    /**
     * Modular diophantine equation solution and lifting algorithm. Let p =
     * A_i.ring.coFac.modul() and assume gcd(A,B) == 1 mod p.
     *
     * @param A modular GenPolynomial, mod p^k
     * @param B modular GenPolynomial, mod p^k
     * @param C modular GenPolynomial, mod p^k
     * @param k desired approximation exponent p^k.
     * @return [s, t] with s A' + t B' = C mod p^k, with A' = B, B' = A.
     */
    public static <MOD extends GcdRingElem<MOD> & Modular> List<GenPolynomial<MOD>> liftDiophant(
            GenPolynomial<MOD> A, GenPolynomial<MOD> B, GenPolynomial<MOD> C, long k)
            throws NoLiftingException {
        if (A == null || A.isZERO() || B == null || B.isZERO()) {
            throw new IllegalArgumentException("A and B must be nonzero, A = " + A + ", B = " + B + ", C = " + C);
        }
        List<GenPolynomial<MOD>> sol = new ArrayList<>();
        GenPolynomialRing<MOD> fac = C.ring;
        if (fac.nvar != 1) { // todo assert
            throw new IllegalArgumentException("polynomial ring not univariate");
        }
        GenPolynomial<MOD> zero = fac.getZERO();
        for (int i = 0; i < 2; i++) {
            sol.add(zero);
        }
        GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<>(new BigInteger(), fac);
        for (Monomial<MOD> m : C) {
            long e = m.e.getVal(0);
            List<GenPolynomial<MOD>> S = liftDiophant(A, B, e, k);
            MOD a = m.c;
            a = fac.coFac.fromInteger(a.getSymmetricInteger().getVal());
            int i = 0;
            for (GenPolynomial<MOD> d : S) {
                d = PolyUtil.fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, d));
                d = d.multiply(a);
                d = sol.get(i).sum(d);
                sol.set(i++, d);
            }
        }
        //GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<BigInteger>(new BigInteger(), fac);
        A = PolyUtil.fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, A));
        B = PolyUtil.fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, B));
        C = PolyUtil.fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, C));
        GenPolynomial<MOD> y = B.multiply(sol.get(0)).sum(A.multiply(sol.get(1)));
        if (!y.equals(C)) {
        }
        return sol;
    }


    /**
     * Modular diophantine equation solution and lifting algorithm. Let p =
     * A_i.ring.coFac.modul() and assume gcd(a,b) == 1 mod p, for a, b in A.
     *
     * @param A list of modular GenPolynomials, mod p^k
     * @param C modular GenPolynomial, mod p^k
     * @param k desired approximation exponent p^k.
     * @return [s_1, ..., s_n] with sum_i s_i A_i' = C mod p^k, with Ai' = prod_{j!=i} A_j.
     */
    public static <MOD extends GcdRingElem<MOD> & Modular> List<GenPolynomial<MOD>> liftDiophant(
            List<GenPolynomial<MOD>> A, GenPolynomial<MOD> C, long k)
            throws NoLiftingException {
        List<GenPolynomial<MOD>> sol = new ArrayList<>();
        GenPolynomialRing<MOD> fac = C.ring;
        if (fac.nvar != 1) { // todo assert
            throw new IllegalArgumentException("polynomial ring not univariate");
        }
        GenPolynomial<MOD> zero = fac.getZERO();
        for (GenPolynomial<MOD> aA : A) {
            sol.add(zero);
        }
        GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<>(new BigInteger(), fac);
        for (Monomial<MOD> m : C) {
            long e = m.e.getVal(0);
            List<GenPolynomial<MOD>> S = liftDiophant(A, e, k);
            MOD a = m.c;
            a = fac.coFac.fromInteger(a.getSymmetricInteger().getVal());
            int i = 0;
            for (GenPolynomial<MOD> d : S) {
                d = PolyUtil.fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, d));
                d = d.multiply(a);
                d = sol.get(i).sum(d);
                sol.set(i++, d);
            }
        }
        /*
        if (true || debug) {
            //GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<BigInteger>(new BigInteger(), fac);
            A = PolyUtil.<MOD> fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, A));
            B = PolyUtil.<MOD> fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, B));
            C = PolyUtil.<MOD> fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, C));
            GenPolynomial<MOD> y = B.multiply(sol.get(0)).sum(A.multiply(sol.get(1)));
            if (!y.equals(C)) {
            }
        }
        */
        return sol;
    }


    /**
     * Modular diophantine equation solution and lifting algorithm. Let p =
     * A_i.ring.coFac.modul() and assume gcd(A,B) == 1 mod p.
     *
     * @param A modular GenPolynomial
     * @param B modular GenPolynomial
     * @param e exponent for x^e
     * @param k desired approximation exponent p^k.
     * @return [s, t] with s A' + t B' = x^e mod p^k, with A' = B, B' = A.
     */
    public static <MOD extends GcdRingElem<MOD> & Modular> List<GenPolynomial<MOD>> liftDiophant(
            GenPolynomial<MOD> A, GenPolynomial<MOD> B, long e, long k) throws NoLiftingException {
        if (A == null || A.isZERO() || B == null || B.isZERO()) {
            throw new IllegalArgumentException("A and B must be nonzero, A = " + A + ", B = " + B);
        }
        List<GenPolynomial<MOD>> sol = new ArrayList<>();
        GenPolynomialRing<MOD> fac = A.ring;
        if (fac.nvar != 1) { // todo assert
            throw new IllegalArgumentException("polynomial ring not univariate");
        }
        // lift EE relation to p^k
        GenPolynomial<MOD>[] lee = liftExtendedEuclidean(B, A, k);
        GenPolynomial<MOD> s1 = lee[0];
        GenPolynomial<MOD> s2 = lee[1];
        if (e == 0L) {
            sol.add(s1);
            sol.add(s2);
            return sol;
        }
        fac = s1.ring;
        GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<>(new BigInteger(), fac);
        A = PolyUtil.fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, A));
        B = PolyUtil.fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, B));

        //      this is the wrong sequence:
        //         GenPolynomial<MOD> xe = fac.univariate(0,e);
        //         GenPolynomial<MOD> q = s1.multiply(xe);
        //         GenPolynomial<MOD>[] QR = q.quotientRemainder(B);
        //         q = QR[0];
        //         GenPolynomial<MOD> r1 = QR[1];
        //         GenPolynomial<MOD> r2 = s2.multiply(xe).sum( q.multiply(A) );

        GenPolynomial<MOD> xe = fac.univariate(0, e);
        GenPolynomial<MOD> q = s1.multiply(xe);
        GenPolynomial<MOD>[] QR = q.quotientRemainder(A);
        q = QR[0];
        GenPolynomial<MOD> r1 = QR[1];
        GenPolynomial<MOD> r2 = s2.multiply(xe).sum(q.multiply(B));
        sol.add(r1);
        sol.add(r2);
        GenPolynomial<MOD> y = B.multiply(r1).sum(A.multiply(r2));
        if (!y.equals(xe)) {
        }
        return sol;
    }


    /**
     * Modular diophantine equation solution and lifting algorithm. Let p =
     * A_i.ring.coFac.modul() and assume gcd(a,b) == 1 mod p, for a, b in A.
     *
     * @param A list of modular GenPolynomials
     * @param e exponent for x^e
     * @param k desired approximation exponent p^k.
     * @return [s_1, ..., s_n] with sum_i s_i A_i' = x^e mod p^k, with Ai' = prod_{j!=i} A_j.
     */
    public static <MOD extends GcdRingElem<MOD> & Modular> List<GenPolynomial<MOD>> liftDiophant(
            List<GenPolynomial<MOD>> A, long e, long k) throws NoLiftingException {
        List<GenPolynomial<MOD>> sol = new ArrayList<>();
        GenPolynomialRing<MOD> fac = A.get(0).ring;
        if (fac.nvar != 1) { // todo assert
            throw new IllegalArgumentException("polynomial ring not univariate");
        }
        // lift EE relation to p^k
        List<GenPolynomial<MOD>> lee = liftExtendedEuclidean(A, k);
        if (e == 0L) {
            return lee;
        }
        fac = lee.get(0).ring;
        GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<>(new BigInteger(), fac);
        List<GenPolynomial<MOD>> S = new ArrayList<>(lee.size());
        for (GenPolynomial<MOD> a : lee) {
            a = PolyUtil.fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, a));
            S.add(a);
        }
        GenPolynomial<MOD> xe = fac.univariate(0, e);
        //List<GenPolynomial<MOD>> Sr = new ArrayList<GenPolynomial<MOD>>(lee.size());
        int i = 0;
        for (GenPolynomial<MOD> s : S) {
            GenPolynomial<MOD> q = s.multiply(xe);
            GenPolynomial<MOD> r = q.remainder(A.get(i++));
            sol.add(r);
        }
        /*
        if (true || debug) {
            GenPolynomial<MOD> y = B.multiply(r1).sum(A.multiply(r2));
            if (!y.equals(xe)) {
            }
        }
        */
        return sol;
    }


    /**
     * Modular Diophant relation lifting test.
     *
     * @param A  modular GenPolynomial
     * @param B  modular GenPolynomial
     * @param C  modular GenPolynomial
     * @param S1 modular GenPolynomial
     * @param S2 modular GenPolynomial
     * @return true if A*S1 + B*S2 = C, else false.
     */
    public static <MOD extends GcdRingElem<MOD> & Modular> boolean isDiophantLift(
            GenPolynomial<MOD> A, GenPolynomial<MOD> B, GenPolynomial<MOD> S1, GenPolynomial<MOD> S2, GenPolynomial<MOD> C) {
        GenPolynomialRing<MOD> fac = C.ring;
        GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<>(new BigInteger(), fac);
        GenPolynomial<MOD> a = PolyUtil.fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, A));
        GenPolynomial<MOD> b = PolyUtil.fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, B));
        GenPolynomial<MOD> s1 = PolyUtil.fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, S1));
        GenPolynomial<MOD> s2 = PolyUtil.fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, S2));
        GenPolynomial<MOD> t = a.multiply(s1).sum(b.multiply(s2));
        return t.equals(C);
    }

    /**
     * Modular Diophant relation lifting test.
     *
     * @param A list of GenPolynomials
     * @param S = [s_0,...,s_{n-1}] list of GenPolynomials
     * @param C = GenPolynomial
     * @return true if prod_{0,...,n-1} s_i * B_i = C mod p^k, with B_i =
     *         prod_{i!=j} A_j, else false.
     */
    public static <MOD extends GcdRingElem<MOD> & Modular> boolean isDiophantLift(
            List<GenPolynomial<MOD>> A, List<GenPolynomial<MOD>> S, GenPolynomial<MOD> C) {
        GenPolynomialRing<MOD> fac = A.get(0).ring;
        GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<>(new BigInteger(), fac);
        List<GenPolynomial<MOD>> B = new ArrayList<>(A.size());
        int i = 0;
        for (GenPolynomial<MOD> ai : A) {
            GenPolynomial<MOD> b = fac.getONE();
            int j = 0;
            for (GenPolynomial<MOD> aj : A) {
                if (i != j /*!ai.equals(aj)*/) {
                    b = b.multiply(aj);
                }
                j++;
            }
            b = PolyUtil.fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, b));
            B.add(b);
            i++;
        }
        // check mod p^e 
        GenPolynomial<MOD> t = fac.getZERO();
        i = 0;
        for (GenPolynomial<MOD> a : B) {
            GenPolynomial<MOD> b = S.get(i++);
            b = PolyUtil.fromIntegerCoefficients(fac, PolyUtil.integerFromModularCoefficients(ifac, b));
            GenPolynomial<MOD> s = a.multiply(b);
            t = t.sum(s);
        }
        return t.equals(C);
    }


    /**
     * Modular Hensel lifting algorithm on coefficients. Let p =
     * f_i.ring.coFac.modul() and assume C == prod_{0,...,n-1} f_i mod p with
     * gcd(f_i,f_j) == 1 mod p for i != j
     *
     * @param C monic integer polynomial
     * @param F = [f_0,...,f_{n-1}] list of monic modular polynomials.
     * @param k approximation exponent.
     * @return [g_0, ..., g_{n-1}] with C = prod_{0,...,n-1} g_i mod p^k.
     */
    public static <MOD extends GcdRingElem<MOD> & Modular>
    List<GenPolynomial<MOD>> liftHenselMonic(GenPolynomial<BigInteger> C, List<GenPolynomial<MOD>> F, long k)
            throws NoLiftingException {
        if (C == null || C.isZERO() || F == null || F.size() == 0) {
            throw new IllegalArgumentException("C must be nonzero and F must be nonempty");
        }
        GenPolynomialRing<BigInteger> fac = C.ring;
        if (fac.nvar != 1) { // todo assert
            throw new IllegalArgumentException("polynomial ring not univariate");
        }
        List<GenPolynomial<MOD>> lift = new ArrayList<>(F.size());
        GenPolynomialRing<MOD> pfac = F.get(0).ring;
        RingFactory<MOD> pcfac = pfac.coFac;
        ModularRingFactory<MOD> PF = (ModularRingFactory<MOD>) pcfac;
        BigInteger P = PF.getIntegerModul();
        int n = F.size();
        if (n == 1) { // lift F_0, this case will probably never be used
            GenPolynomial<MOD> f = F.get(0);
            ModularRingFactory<MOD> mcfac;
            if (ModLongRing.MAX_LONG.compareTo(P.getVal()) > 0) {
                mcfac = (ModularRingFactory) new ModLongRing(P.getVal());
            } else {
                mcfac = (ModularRingFactory) new ModIntegerRing(P.getVal());
            }
            GenPolynomialRing<MOD> mfac = new GenPolynomialRing<>(mcfac, fac);
            f = PolyUtil.fromIntegerCoefficients(mfac, PolyUtil.integerFromModularCoefficients(fac, f));
            lift.add(f);
            return lift;
        }
        //         if (n == 2) { // only one step
        //             HenselApprox<MOD> ab = HenselUtil.<MOD> liftHenselQuadratic(C, M, F.get(0), F.get(1));
        //             lift.add(ab.Am);
        //             lift.add(ab.Bm);
        //             return lift;
        //         }

        // setup integer polynomial ring
        GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<>(new BigInteger(), fac);
        List<GenPolynomial<BigInteger>> Fi = PolyUtil.integerFromModularCoefficients(ifac, F);

        List<GenPolynomial<MOD>> S = liftExtendedEuclidean(F, k + 1); // lift works for any k, TODO: use this
        List<GenPolynomial<BigInteger>> Si = PolyUtil.integerFromModularCoefficients(ifac, S);

        // approximate mod p^i
        ModularRingFactory<MOD> mcfac = PF;
        BigInteger p = mcfac.getIntegerModul();
        BigInteger modul = p;
        GenPolynomialRing<MOD> mfac = new GenPolynomialRing<>(mcfac, fac);
        List<GenPolynomial<MOD>> Sp = PolyUtil.fromIntegerCoefficients(mfac, Si);
        for (int i = 1; i < k; i++) {
            GenPolynomial<BigInteger> e = fac.getONE();
            for (GenPolynomial<BigInteger> fi : Fi) {
                e = e.multiply(fi);
            }
            e = C.subtract(e);
            if (e.isZERO()) {
                break;
            }
            try {
                e = e.divide(modul);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                throw ex;
            }
            // move to in Z_p[x]
            GenPolynomial<MOD> c = PolyUtil.fromIntegerCoefficients(mfac, e);

            List<GenPolynomial<MOD>> s = new ArrayList<>(S.size());
            int j = 0;
            for (GenPolynomial<MOD> f : Sp) {
                f = f.multiply(c);
                f = f.remainder(F.get(j++));
                s.add(f);
            }
            List<GenPolynomial<BigInteger>> si = PolyUtil.integerFromModularCoefficients(ifac, s);

            List<GenPolynomial<BigInteger>> Fii = new ArrayList<>(F.size());
            j = 0;
            for (GenPolynomial<BigInteger> f : Fi) {
                f = f.sum(si.get(j++).multiply(modul));
                Fii.add(f);
            }
            Fi = Fii;
            modul = modul.multiply(p);
            if (i >= k - 1) {
            }
        }
        // setup ring mod p^k
        modul = Power.positivePower(p, k);
        if (ModLongRing.MAX_LONG.compareTo(modul.getVal()) > 0) {
            mcfac = (ModularRingFactory) new ModLongRing(modul.getVal());
        } else {
            mcfac = (ModularRingFactory) new ModIntegerRing(modul.getVal());
        }
        mfac = new GenPolynomialRing<>(mcfac, fac);
        lift = PolyUtil.fromIntegerCoefficients(mfac, Fi);
        return lift;
    }

}
