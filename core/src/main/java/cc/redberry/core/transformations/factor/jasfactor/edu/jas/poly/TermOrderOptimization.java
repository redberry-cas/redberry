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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly;

import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigInteger;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.vector.BasicLinAlg;

import java.util.*;


/**
 * Term order optimization.
 * See mas10/maspoly/DIPTOO.m{di}.
 *
 * @author Heinz Kredel
 */

public class TermOrderOptimization {


    //private static boolean debug = false;


    /**
     * Degree matrix.
     *
     * @param A polynomial to be considered.
     * @return degree matrix.
     */
    public static <C extends RingElem<C>>
    List<GenPolynomial<BigInteger>> degreeMatrix(GenPolynomial<C> A) {

        List<GenPolynomial<BigInteger>> dem = null;
        if (A == null) {
            return dem;
        }

        BigInteger cfac = new BigInteger();
        GenPolynomialRing<BigInteger> ufac
                = new GenPolynomialRing<>(cfac, 1);

        int nvar = A.numberOfVariables();
        dem = new ArrayList<>(nvar);

        for (int i = 0; i < nvar; i++) {
            dem.add(ufac.getZERO());
        }
        if (A.isZERO()) {
            return dem;
        }

        for (ExpVector e : A.getMap().keySet()) {
            dem = expVectorAdd(dem, e);
        }
        return dem;
    }


    /**
     * Degree matrix exponent vector add.
     *
     * @param dm degree matrix.
     * @param e  exponent vector.
     * @return degree matrix + e.
     */
    public static List<GenPolynomial<BigInteger>> expVectorAdd(List<GenPolynomial<BigInteger>> dm, ExpVector e) {
        for (int i = 0; i < dm.size() && i < e.length(); i++) {
            GenPolynomial<BigInteger> p = dm.get(i);
            long u = e.getVal(i);
            ExpVector f = ExpVector.create(1, 0, u);
            p = p.sum(p.ring.getONECoefficient(), f);
            dm.set(i, p);
        }
        return dm;
    }


    /**
     * Degree matrix.
     *
     * @param L list of polynomial to be considered.
     * @return degree matrix.
     */
    public static <C extends RingElem<C>>
    List<GenPolynomial<BigInteger>> degreeMatrix(Collection<GenPolynomial<C>> L) {
        if (L == null) {
            throw new IllegalArgumentException("list must be non null");
        }
        BasicLinAlg<GenPolynomial<BigInteger>> blas = new BasicLinAlg<>();
        List<GenPolynomial<BigInteger>> dem = null;
        for (GenPolynomial<C> p : L) {
            List<GenPolynomial<BigInteger>> dm = degreeMatrix(p);
            if (dem == null) {
                dem = dm;
            } else {
                dem = blas.vectorAdd(dem, dm);
            }
        }
        return dem;
    }


    /**
     * Optimal permutation for the Degree matrix.
     *
     * @param D degree matrix.
     * @return optimal permutation for D.
     */
    public static List<Integer> optimalPermutation(List<GenPolynomial<BigInteger>> D) {
        if (D == null) {
            throw new IllegalArgumentException("list must be non null");
        }
        List<Integer> P = new ArrayList<>(D.size());
        if (D.size() == 0) {
            return P;
        }
        if (D.size() == 1) {
            P.add(0);
            return P;
        }
        SortedMap<GenPolynomial<BigInteger>, List<Integer>> map
                = new TreeMap<>();
        int i = 0;
        for (GenPolynomial<BigInteger> p : D) {
            List<Integer> il = map.get(p);
            if (il == null) {
                il = new ArrayList<>(3);
            }
            il.add(i);
            map.put(p, il);
            i++;
        }
        List<List<Integer>> V = new ArrayList<>(map.values());
        //for ( int j = V.size()-1; j >= 0; j-- ) {
        for (List<Integer> v : V) {
            for (Integer k : v) {
                P.add(k);
            }
        }
        return P;
    }


    /**
     * Inverse of a permutation.
     *
     * @param P permutation.
     * @return S with S*P = id.
     */
    public static List<Integer> inversePermutation(List<Integer> P) {
        if (P == null || P.size() <= 1) {
            return P;
        }
        List<Integer> ip = new ArrayList<>(P); // ensure size and content
        for (int i = 0; i < P.size(); i++) {
            ip.set(P.get(i), i); // inverse
        }
        return ip;
    }


    /**
     * Permutation of an array.
     *
     * @param a array.
     * @param P permutation.
     * @return P(a).
     */
    public static String[] stringArrayPermutation(List<Integer> P, String[] a) {
        if (a == null || a.length <= 1) {
            return a;
        }
        String[] b = new String[a.length];    // jdk 1.5
        //T[] b = Arrays.<T>copyOf( a, a.length ); // jdk 1.6
        int j = 0;
        for (Integer i : P) {
            b[j] = a[i];
            j++;
        }
        return b;
    }


    /**
     * Permutation of a long array.
     *
     * @param a array of long.
     * @param P permutation.
     * @return P(a).
     */
    public static long[] longArrayPermutation(List<Integer> P, long[] a) {
        if (a == null || a.length <= 1) {
            return a;
        }
        long[] b = new long[a.length];
        int j = 0;
        for (Integer i : P) {
            b[j] = a[i];
            j++;
        }
        return b;
    }


    /**
     * Permutation of an exponent vector.
     *
     * @param e exponent vector.
     * @param P permutation.
     * @return P(e).
     */
    public static ExpVector permutation(List<Integer> P, ExpVector e) {
        if (e == null) {
            return e;
        }
        long[] u = longArrayPermutation(P, e.getVal());
        ExpVector f = ExpVector.create(u);
        return f;
    }


    /**
     * Permutation of polynomial exponent vectors.
     *
     * @param A polynomial.
     * @param R polynomial ring.
     * @param P permutation.
     * @return P(A).
     */
    public static <C extends RingElem<C>>
    GenPolynomial<C> permutation(List<Integer> P, GenPolynomialRing<C> R, GenPolynomial<C> A) {
        if (A == null) {
            return A;
        }
        GenPolynomial<C> B = R.getZERO().copy();
        Map<ExpVector, C> Bv = B.val; //getMap();
        for (Map.Entry<ExpVector, C> y : A.getMap().entrySet()) {
            ExpVector e = y.getKey();
            C a = y.getValue();
            ExpVector f = permutation(P, e);
            Bv.put(f, a); // assert f not in Bv
        }
        return B;
    }


    /**
     * Permutation of polynomial exponent vectors.
     *
     * @param L list of polynomials.
     * @param R polynomial ring.
     * @param P permutation.
     * @return P(L).
     */
    public static <C extends RingElem<C>>
    List<GenPolynomial<C>>
    permutation(List<Integer> P, GenPolynomialRing<C> R, List<GenPolynomial<C>> L) {
        if (L == null || L.size() == 0) {
            return L;
        }
        List<GenPolynomial<C>> K = new ArrayList<>(L.size());
        for (GenPolynomial<C> a : L) {
            GenPolynomial<C> b = permutation(P, R, a);
            K.add(b);
        }
        return K;
    }


    /**
     * Permutation of polynomial ring variables.
     *
     * @param R polynomial ring.
     * @param P permutation.
     * @return P(R).
     */
    public static <C extends RingElem<C>>
    GenPolynomialRing<C>
    permutation(List<Integer> P, GenPolynomialRing<C> R) {
        if (R == null) {
            return R;
        }
        if (R.vars == null || R.nvar <= 1) {
            return R;
        }
        GenPolynomialRing<C> S;
        TermOrder tord = R.tord;
        if (tord.getEvord2() != 0) {
            //throw new IllegalArgumentException("split term orders not permutable");
            tord = new TermOrder(tord.getEvord2());
        }
        long[][] weight = tord.getWeight();
        if (weight != null) {
            long[][] w = new long[weight.length][];
            for (int i = 0; i < weight.length; i++) {
                w[i] = longArrayPermutation(P, weight[i]);
            }
            tord = new TermOrder(w);
        }
        String[] v1 = new String[R.vars.length];
        for (int i = 0; i < v1.length; i++) {
            v1[i] = R.vars[v1.length - 1 - i];
        }
        String[] vars = stringArrayPermutation(P, v1);
        String[] v2 = new String[R.vars.length];
        for (int i = 0; i < v1.length; i++) {
            v2[i] = vars[v2.length - 1 - i];
        }
        S = new GenPolynomialRing<>(R.coFac, R.nvar, tord, v2);
        return S;
    }


    /**
     * Optimize variable order.
     *
     * @param R polynomial ring.
     * @param L list of polynomials.
     * @return optimized polynomial list.
     */
    public static <C extends RingElem<C>>
    OptimizedPolynomialList<C>
    optimizeTermOrder(GenPolynomialRing<C> R, List<GenPolynomial<C>> L) {
        List<Integer> perm = optimalPermutation(degreeMatrix(L));
        GenPolynomialRing<C> pring = TermOrderOptimization.permutation(perm, R);
        List<GenPolynomial<C>> ppolys = TermOrderOptimization.permutation(perm, pring, L);
        OptimizedPolynomialList<C> op = new OptimizedPolynomialList<>(perm, pring, ppolys);
        return op;
    }

}
