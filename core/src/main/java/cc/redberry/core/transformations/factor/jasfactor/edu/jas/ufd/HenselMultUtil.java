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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigInteger;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.Modular;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.ModularRingFactory;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.ps.PolynomialTaylorFunction;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.ps.TaylorFunction;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.ps.UnivPowerSeries;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.ps.UnivPowerSeriesRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;

import java.util.ArrayList;
import java.util.List;


/**
 * Hensel multivariate lifting utilities.
 *
 * @author Heinz Kredel
 */

public class HenselMultUtil {


    private static final boolean debug = false;


    /**
     * Modular diophantine equation solution and lifting algorithm. Let p =
     * A_i.ring.coFac.modul() and assume ggt(A,B) == 1 mod p.
     *
     * @param A modular GenPolynomial, mod p^k
     * @param B modular GenPolynomial, mod p^k
     * @param C modular GenPolynomial, mod p^k
     * @param V list of substitution values, mod p^k
     * @param d desired approximation exponent (x_i-v_i)^d.
     * @param k desired approximation exponent p^k.
     * @return [s, t] with s A' + t B' = C mod p^k, with A' = B, B' = A.
     */
    public static <MOD extends GcdRingElem<MOD> & Modular> List<GenPolynomial<MOD>> liftDiophant(
            GenPolynomial<MOD> A, GenPolynomial<MOD> B, GenPolynomial<MOD> C, List<MOD> V, long d,
            long k) throws NoLiftingException {
        GenPolynomialRing<MOD> pkfac = C.ring;
        if (pkfac.nvar == 1) { // V, d ignored
            return HenselUtil.liftDiophant(A, B, C, k);
        }
        if (!pkfac.equals(A.ring)) {
            throw new IllegalArgumentException("A.ring != pkfac: " + A.ring + " != " + pkfac);
        }

        // evaluate at v_n:
        List<MOD> Vp = new ArrayList<>(V);
        MOD v = Vp.remove(Vp.size() - 1);
        //GenPolynomial<MOD> zero = pkfac.getZERO();
        // (x_n - v)
        GenPolynomial<MOD> mon = pkfac.getONE();
        GenPolynomial<MOD> xv = pkfac.univariate(0, 1);
        xv = xv.subtract(pkfac.fromInteger(v.getSymmetricInteger().getVal()));
        // A(v), B(v), C(v)
        ModularRingFactory<MOD> cf = (ModularRingFactory<MOD>) pkfac.coFac;
        MOD vp = cf.fromInteger(v.getSymmetricInteger().getVal());
        GenPolynomialRing<MOD> ckfac = pkfac.contract(1);
        GenPolynomial<MOD> Ap = PolyUtil.evaluateMain(ckfac, A, vp);
        GenPolynomial<MOD> Bp = PolyUtil.evaluateMain(ckfac, B, vp);
        GenPolynomial<MOD> Cp = PolyUtil.evaluateMain(ckfac, C, vp);

        // recursion:
        List<GenPolynomial<MOD>> su = HenselMultUtil.liftDiophant(Ap, Bp, Cp, Vp, d, k);
        if (pkfac.nvar == 2 && !HenselUtil.isDiophantLift(Bp, Ap, su.get(0), su.get(1), Cp)) {
        }
        if (!ckfac.equals(su.get(0).ring)) {
            throw new IllegalArgumentException("qfac != ckfac: " + su.get(0).ring + " != " + ckfac);
        }
        GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<>(new BigInteger(), pkfac);
        //GenPolynomialRing<BigInteger> cifac = new GenPolynomialRing<BigInteger>(new BigInteger(), ckfac);
        String[] mn = new String[]{pkfac.getVars()[pkfac.nvar - 1]};
        GenPolynomialRing<GenPolynomial<MOD>> qrfac = new GenPolynomialRing<>(ckfac, 1, mn);

        List<GenPolynomial<MOD>> sup = new ArrayList<>(su.size());
        List<GenPolynomial<BigInteger>> supi = new ArrayList<>(su.size());
        for (GenPolynomial<MOD> s : su) {
            GenPolynomial<MOD> sp = s.extend(pkfac, 0, 0L);
            sup.add(sp);
            GenPolynomial<BigInteger> spi = PolyUtil.integerFromModularCoefficients(ifac, sp);
            supi.add(spi);
        }
        GenPolynomial<BigInteger> Ai = PolyUtil.integerFromModularCoefficients(ifac, A);
        GenPolynomial<BigInteger> Bi = PolyUtil.integerFromModularCoefficients(ifac, B);
        GenPolynomial<BigInteger> Ci = PolyUtil.integerFromModularCoefficients(ifac, C);
        //GenPolynomial<MOD> aq = PolyUtil.<MOD> fromIntegerCoefficients(pkfac, Ai);
        //GenPolynomial<MOD> bq = PolyUtil.<MOD> fromIntegerCoefficients(pkfac, Bi);

        // compute error:
        GenPolynomial<BigInteger> E = Ci; // - sum_i s_i b_i
        E = E.subtract(Bi.multiply(supi.get(0)));
        E = E.subtract(Ai.multiply(supi.get(1)));
        if (E.isZERO()) {
            return sup;
        }
        GenPolynomial<MOD> Ep = PolyUtil.fromIntegerCoefficients(pkfac, E);
        if (Ep.isZERO()) {
            return sup;
        }
        for (int e = 1; e <= d; e++) {
            GenPolynomial<GenPolynomial<MOD>> Epr = PolyUtil.recursive(qrfac, Ep);
            UnivPowerSeriesRing<GenPolynomial<MOD>> psfac = new UnivPowerSeriesRing<>(qrfac);
            TaylorFunction<GenPolynomial<MOD>> F = new PolynomialTaylorFunction<>(Epr);
            GenPolynomial<MOD> vq = ckfac.fromInteger(v.getSymmetricInteger().getVal());
            UnivPowerSeries<GenPolynomial<MOD>> Epst = psfac.seriesOfTaylor(F, vq);
            GenPolynomial<MOD> cm = Epst.coefficient(e);

            // recursion:
            List<GenPolynomial<MOD>> S = HenselMultUtil.liftDiophant(Ap, Bp, cm, Vp, d, k);
            if (!ckfac.coFac.equals(S.get(0).ring.coFac)) {
                throw new IllegalArgumentException("ckfac != pkfac: " + ckfac.coFac + " != "
                        + S.get(0).ring.coFac);
            }
            if (pkfac.nvar == 2 && !HenselUtil.isDiophantLift(Ap, Bp, S.get(1), S.get(0), cm)) {
            }
            mon = mon.multiply(xv); // Power.<GenPolynomial<MOD>> power(pkfac,xv,e);
            int i = 0;
            supi = new ArrayList<>(su.size());
            for (GenPolynomial<MOD> dd : S) {
                GenPolynomial<MOD> de = dd.extend(pkfac, 0, 0L);
                GenPolynomial<MOD> dm = de.multiply(mon);
                de = sup.get(i).sum(dm);
                sup.set(i++, de);
                GenPolynomial<BigInteger> spi = PolyUtil.integerFromModularCoefficients(ifac, dm);
                supi.add(spi);
            }
            // compute new error
            //E = E; // - sum_i s_i b_i
            E = E.subtract(Bi.multiply(supi.get(0)));
            E = E.subtract(Ai.multiply(supi.get(1)));
            if (E.isZERO()) {
                return sup;
            }
            Ep = PolyUtil.fromIntegerCoefficients(pkfac, E);
            if (Ep.isZERO()) {
                return sup;
            }
        }
        return sup;
    }


    /**
     * Modular diophantine equation solution and lifting algorithm. Let p =
     * A_i.ring.coFac.modul() and assume ggt(a,b) == 1 mod p, for a, b in A.
     *
     * @param A list of modular GenPolynomials, mod p^k
     * @param C modular GenPolynomial, mod p^k
     * @param V list of substitution values, mod p^k
     * @param d desired approximation exponent (x_i-v_i)^d.
     * @param k desired approximation exponent p^k.
     * @return [s_1, ..., s_n] with sum_i s_i A_i' = C mod p^k, with Ai' =
     *         prod_{j!=i} A_j.
     */
    public static <MOD extends GcdRingElem<MOD> & Modular> List<GenPolynomial<MOD>> liftDiophant(
            List<GenPolynomial<MOD>> A, GenPolynomial<MOD> C, List<MOD> V, long d, long k)
            throws NoLiftingException {
        GenPolynomialRing<MOD> pkfac = C.ring;
        if (pkfac.nvar == 1) { // V, d ignored
            return HenselUtil.liftDiophant(A, C, k);
        }
        if (!pkfac.equals(A.get(0).ring)) {
            throw new IllegalArgumentException("A.ring != pkfac: " + A.get(0).ring + " != " + pkfac);
        }
        // co-products
        GenPolynomial<MOD> As = pkfac.getONE();
        for (GenPolynomial<MOD> a : A) {
            As = As.multiply(a);
        }
        List<GenPolynomial<MOD>> Bp = new ArrayList<>(A.size());
        for (GenPolynomial<MOD> a : A) {
            GenPolynomial<MOD> b = PolyUtil.basePseudoDivide(As, a);
            Bp.add(b);
        }

        // evaluate at v_n:
        List<MOD> Vp = new ArrayList<>(V);
        MOD v = Vp.remove(Vp.size() - 1);
        // (x_n - v)
        GenPolynomial<MOD> mon = pkfac.getONE();
        GenPolynomial<MOD> xv = pkfac.univariate(0, 1);
        xv = xv.subtract(pkfac.fromInteger(v.getSymmetricInteger().getVal()));
        // A(v), B(v), C(v)
        ModularRingFactory<MOD> cf = (ModularRingFactory<MOD>) pkfac.coFac;
        MOD vp = cf.fromInteger(v.getSymmetricInteger().getVal());
        GenPolynomialRing<MOD> ckfac = pkfac.contract(1);
        List<GenPolynomial<MOD>> Ap = new ArrayList<>(A.size());
        for (GenPolynomial<MOD> a : A) {
            GenPolynomial<MOD> ap = PolyUtil.evaluateMain(ckfac, a, vp);
            Ap.add(ap);
        }
        GenPolynomial<MOD> Cp = PolyUtil.evaluateMain(ckfac, C, vp);

        // recursion:
        List<GenPolynomial<MOD>> su = HenselMultUtil.liftDiophant(Ap, Cp, Vp, d, k);
        if (pkfac.nvar == 2 && !HenselUtil.isDiophantLift(Ap, su, Cp)) {
        }
        if (!ckfac.equals(su.get(0).ring)) {
            throw new IllegalArgumentException("qfac != ckfac: " + su.get(0).ring + " != " + ckfac);
        }
        GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<>(new BigInteger(), pkfac);
        //GenPolynomialRing<BigInteger> cifac = new GenPolynomialRing<BigInteger>(new BigInteger(), ckfac);
        String[] mn = new String[]{pkfac.getVars()[pkfac.nvar - 1]};
        GenPolynomialRing<GenPolynomial<MOD>> qrfac = new GenPolynomialRing<>(ckfac, 1, mn);

        List<GenPolynomial<MOD>> sup = new ArrayList<>(su.size());
        List<GenPolynomial<BigInteger>> supi = new ArrayList<>(su.size());
        for (GenPolynomial<MOD> s : su) {
            GenPolynomial<MOD> sp = s.extend(pkfac, 0, 0L);
            sup.add(sp);
            GenPolynomial<BigInteger> spi = PolyUtil.integerFromModularCoefficients(ifac, sp);
            supi.add(spi);
        }
        List<GenPolynomial<BigInteger>> Ai = new ArrayList<>(A.size());
        for (GenPolynomial<MOD> a : A) {
            GenPolynomial<BigInteger> ai = PolyUtil.integerFromModularCoefficients(ifac, a);
            Ai.add(ai);
        }
        List<GenPolynomial<BigInteger>> Bi = new ArrayList<>(A.size());
        for (GenPolynomial<MOD> b : Bp) {
            GenPolynomial<BigInteger> bi = PolyUtil.integerFromModularCoefficients(ifac, b);
            Bi.add(bi);
        }
        GenPolynomial<BigInteger> Ci = PolyUtil.integerFromModularCoefficients(ifac, C);


        // compute error:
        GenPolynomial<BigInteger> E = Ci; // - sum_i s_i b_i
        int i = 0;
        for (GenPolynomial<BigInteger> bi : Bi) {
            E = E.subtract(bi.multiply(supi.get(i++)));
        }
        if (E.isZERO()) {
            return sup;
        }
        GenPolynomial<MOD> Ep = PolyUtil.fromIntegerCoefficients(pkfac, E);
        if (Ep.isZERO()) {
            return sup;
        }
        for (int e = 1; e <= d; e++) {
            GenPolynomial<GenPolynomial<MOD>> Epr = PolyUtil.recursive(qrfac, Ep);
            UnivPowerSeriesRing<GenPolynomial<MOD>> psfac = new UnivPowerSeriesRing<>(qrfac);
            TaylorFunction<GenPolynomial<MOD>> F = new PolynomialTaylorFunction<>(Epr);
            GenPolynomial<MOD> vq = ckfac.fromInteger(v.getSymmetricInteger().getVal());
            UnivPowerSeries<GenPolynomial<MOD>> Epst = psfac.seriesOfTaylor(F, vq);
            GenPolynomial<MOD> cm = Epst.coefficient(e);
            if (cm.isZERO()) {
                continue;
            }
            // recursion:
            List<GenPolynomial<MOD>> S = HenselMultUtil.liftDiophant(Ap, cm, Vp, d, k);
            if (!ckfac.coFac.equals(S.get(0).ring.coFac)) {
                throw new IllegalArgumentException("ckfac != pkfac: " + ckfac.coFac + " != "
                        + S.get(0).ring.coFac);
            }
            if (pkfac.nvar == 2 && !HenselUtil.isDiophantLift(Ap, S, cm)) {
            }
            mon = mon.multiply(xv); // Power.<GenPolynomial<MOD>> power(pkfac,xv,e);
            i = 0;
            supi = new ArrayList<>(su.size());
            for (GenPolynomial<MOD> dd : S) {
                GenPolynomial<MOD> de = dd.extend(pkfac, 0, 0L);
                GenPolynomial<MOD> dm = de.multiply(mon);
                de = sup.get(i).sum(dm);
                sup.set(i++, de);
                GenPolynomial<BigInteger> spi = PolyUtil.integerFromModularCoefficients(ifac, dm);
                supi.add(spi);
            }
            // compute new error
            //E = E; // - sum_i s_i b_i
            i = 0;
            for (GenPolynomial<BigInteger> bi : Bi) {
                E = E.subtract(bi.multiply(supi.get(i++)));
            }
            if (E.isZERO()) {
                return sup;
            }
            Ep = PolyUtil.fromIntegerCoefficients(pkfac, E);
            if (Ep.isZERO()) {
                return sup;
            }
        }
        return sup;
    }


    /**
     * Modular Hensel lifting algorithm. Let p = A_i.ring.coFac.modul() and
     * assume ggt(a,b) == 1 mod p, for a, b in A.
     *
     * @param C  GenPolynomial with integer coefficients
     * @param Cp GenPolynomial C mod p^k
     * @param F  list of modular GenPolynomials, mod (I_v, p^k )
     * @param V  list of substitution values, mod p^k
     * @param k  desired approximation exponent p^k.
     * @param G  list of leading coefficients of the factors of C.
     * @return [g'_1,..., g'_n] with prod_i g'_i = Cp mod p^k.
     */
    public static <MOD extends GcdRingElem<MOD> & Modular> List<GenPolynomial<MOD>> liftHensel(
            GenPolynomial<BigInteger> C, GenPolynomial<MOD> Cp, List<GenPolynomial<MOD>> F,
            List<MOD> V, long k, List<GenPolynomial<BigInteger>> G) throws NoLiftingException {
        GenPolynomialRing<MOD> pkfac = Cp.ring;
        long d = C.degree();

        //GenPolynomial<BigInteger> cd = G.get(0); // 1
        //if ( cd.equals(C.ring.univariate(0)) ) {
        //}
        // G mod p^k, in all variables
        GenPolynomialRing<MOD> pkfac1 = new GenPolynomialRing<>(pkfac.coFac, G.get(0).ring);
        List<GenPolynomial<MOD>> Lp = new ArrayList<>(G.size());
        for (GenPolynomial<BigInteger> cd1 : G) {
            GenPolynomial<MOD> cdq = PolyUtil.fromIntegerCoefficients(pkfac1, cd1);
            cdq = cdq.extendLower(pkfac, 0, 0L); // reintroduce lower variable
            Lp.add(cdq);
        }

        // prepare stack of polynomial rings, polynomials and evaluated leading coefficients
        List<GenPolynomialRing<MOD>> Pfac = new ArrayList<>();
        List<GenPolynomial<MOD>> Ap = new ArrayList<>();
        List<List<GenPolynomial<MOD>>> Gp = new ArrayList<>();
        List<MOD> Vb = new ArrayList<>();
        //MOD v = V.get(0);
        Pfac.add(pkfac);
        Ap.add(Cp);
        Gp.add(Lp);
        GenPolynomialRing<MOD> pf = pkfac;
        //GenPolynomialRing<MOD> pf1 = pkfac1;
        GenPolynomial<MOD> ap = Cp;
        List<GenPolynomial<MOD>> Lpp = Lp;
        for (int j = pkfac.nvar; j > 2; j--) {
            pf = pf.contract(1);
            Pfac.add(0, pf);
            MOD vp = pkfac.coFac.fromInteger(V.get(pkfac.nvar - j).getSymmetricInteger().getVal());
            Vb.add(vp);
            ap = PolyUtil.evaluateMain(pf, ap, vp);
            Ap.add(0, ap);
            List<GenPolynomial<MOD>> Lps = new ArrayList<>(Lpp.size());
            for (GenPolynomial<MOD> qp : Lpp) {
                GenPolynomial<MOD> qpe = PolyUtil.evaluateMain(pf, qp, vp);
                Lps.add(qpe);
            }
            Lpp = Lps;
            Gp.add(0, Lpp);
        }
        Vb.add(V.get(pkfac.nvar - 2));
        if (debug) {
        }

        // check bi-variate base case
        GenPolynomialRing<MOD> pk1fac = F.get(0).ring;
        if (!pkfac.coFac.equals(pk1fac.coFac)) {
            throw new IllegalArgumentException("F.ring != pkfac: " + pk1fac + " != " + pkfac);
        }

        // init recursion
        List<GenPolynomial<MOD>> U = F;
        GenPolynomial<BigInteger> E = C.ring.getZERO();
        List<MOD> Vh = new ArrayList<>();
        List<GenPolynomial<BigInteger>> Si; // = new ArrayList<GenPolynomial<BigInteger>>(F.size());
        MOD v = null;

        while (Pfac.size() > 0) { // loop through stack of polynomial rings
            pkfac = Pfac.remove(0);
            Cp = Ap.remove(0);
            Lpp = Gp.remove(0);
            v = Vb.remove(Vb.size() - 1); // last in stack

            List<GenPolynomial<MOD>> U1 = U;
            U = new ArrayList<>(U1.size());

            // update U, replace leading coefficient if required
            int j = 0;
            for (GenPolynomial<MOD> b : U1) {
                GenPolynomial<MOD> bi = b.extend(pkfac, 0, 0L);
                GenPolynomial<MOD> li = Lpp.get(j);
                if (!li.isONE()) {
                    GenPolynomialRing<GenPolynomial<MOD>> pkrfac = pkfac.recursive(pkfac.nvar - 1);
                    GenPolynomial<GenPolynomial<MOD>> br = PolyUtil.recursive(pkrfac, bi);
                    GenPolynomial<GenPolynomial<MOD>> bs = PolyUtil.switchVariables(br);

                    GenPolynomial<GenPolynomial<MOD>> lr = PolyUtil.recursive(pkrfac, li);
                    GenPolynomial<GenPolynomial<MOD>> ls = PolyUtil.switchVariables(lr);
                    if (!ls.isConstant()) {
                        throw new RuntimeException("ls not constant " + ls);
                    }
                    bs.doPutToMap(bs.leadingExpVector(), ls.leadingBaseCoefficient());
                    br = PolyUtil.switchVariables(bs);
                    bi = PolyUtil.distribute(pkfac, br);
                }
                U.add(bi);
                j++;
            }

            // (x_n - v)
            GenPolynomial<MOD> mon = pkfac.getONE();
            GenPolynomial<MOD> xv = pkfac.univariate(0, 1);
            xv = xv.subtract(pkfac.fromInteger(v.getSymmetricInteger().getVal()));

            long deg = Cp.degree(pkfac.nvar - 1);

            // convert to integer polynomials
            GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<>(new BigInteger(), pkfac);
            List<GenPolynomial<BigInteger>> Bi = PolyUtil.integerFromModularCoefficients(ifac, U);
            GenPolynomial<BigInteger> Ci = PolyUtil.integerFromModularCoefficients(ifac, Cp);

            // compute error:
            E = ifac.getONE();
            for (GenPolynomial<BigInteger> bi : Bi) {
                E = E.multiply(bi);
            }
            E = Ci.subtract(E);
            GenPolynomial<MOD> Ep = PolyUtil.fromIntegerCoefficients(pkfac, E);

            GenPolynomialRing<GenPolynomial<MOD>> pkrfac = pkfac.recursive(1);
            GenPolynomialRing<MOD> ckfac = (GenPolynomialRing<MOD>) pkrfac.coFac;

            for (int e = 1; e <= deg && !Ep.isZERO(); e++) {
                GenPolynomial<GenPolynomial<MOD>> Epr = PolyUtil.recursive(pkrfac, Ep);
                UnivPowerSeriesRing<GenPolynomial<MOD>> psfac = new UnivPowerSeriesRing<>(
                        pkrfac);
                TaylorFunction<GenPolynomial<MOD>> T = new PolynomialTaylorFunction<>(Epr);
                GenPolynomial<MOD> vq = ckfac.fromInteger(v.getSymmetricInteger().getVal());
                UnivPowerSeries<GenPolynomial<MOD>> Epst = psfac.seriesOfTaylor(T, vq);
                GenPolynomial<MOD> cm = Epst.coefficient(e);
                if (cm.isZERO()) {
                    continue;
                }
                List<GenPolynomial<MOD>> Ud = HenselMultUtil.liftDiophant(U1, cm, Vh, d, k);

                mon = mon.multiply(xv); // Power.<GenPolynomial<MOD>> power(pkfac,xv,e);
                int i = 0;
                Si = new ArrayList<>(Ud.size());
                for (GenPolynomial<MOD> dd : Ud) {
                    GenPolynomial<MOD> de = dd.extend(pkfac, 0, 0L);
                    GenPolynomial<MOD> dm = de.multiply(mon);
                    de = U.get(i).sum(dm);
                    U.set(i++, de);
                    GenPolynomial<BigInteger> si = PolyUtil.integerFromModularCoefficients(ifac, de);
                    Si.add(si);
                }

                // compute new error:
                E = ifac.getONE();
                for (GenPolynomial<BigInteger> bi : Si) {
                    E = E.multiply(bi);
                }
                E = Ci.subtract(E);
                Ep = PolyUtil.fromIntegerCoefficients(pkfac, E);
            }
            Vh.add(v);
            GenPolynomial<MOD> Uf = U.get(0).ring.getONE();
            for (GenPolynomial<MOD> Upp : U) {
                Uf = Uf.multiply(Upp);
            }
        }
        if (E.isZERO()) {
        }
        return U;
    }
}
