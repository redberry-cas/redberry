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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.AlgebraicNumber;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.AlgebraicNumberRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;

import java.util.ArrayList;
import java.util.List;


/**
 * Algebraic number coefficients factorization algorithms. This class implements
 * factorization methods for polynomials over algebraic numbers over rational
 * numbers or over (prime) modular integers.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public class FactorAlgebraic<C extends GcdRingElem<C>> extends FactorAbsolute<AlgebraicNumber<C>> {

    /**
     * Factorization engine for base coefficients.
     */
    public final FactorAbstract<C> factorCoeff;


    /**
     * Constructor.
     *
     * @param fac algebraic number factory.
     */
    public FactorAlgebraic(AlgebraicNumberRing<C> fac) {
        this(fac, FactorFactory.<C>getImplementation(fac.ring.coFac));
    }


    /**
     * Constructor.
     *
     * @param fac         algebraic number factory.
     * @param factorCoeff factorization engine for polynomials over base coefficients.
     */
    public FactorAlgebraic(AlgebraicNumberRing<C> fac, FactorAbstract<C> factorCoeff) {
        super(fac);
        this.factorCoeff = factorCoeff;
    }


    /**
     * GenPolynomial base factorization of a squarefree polynomial.
     *
     * @param P squarefree GenPolynomial&lt;AlgebraicNumber&lt;C&gt;&gt;.
     * @return [p_1, ..., p_k] with P = prod_{i=1, ..., k} p_i.
     */
    @Override
    public List<GenPolynomial<AlgebraicNumber<C>>> baseFactorsSquarefree(GenPolynomial<AlgebraicNumber<C>> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P == null");
        }
        List<GenPolynomial<AlgebraicNumber<C>>> factors = new ArrayList<>();
        if (P.isZERO()) {
            return factors;
        }
        if (P.isONE()) {
            factors.add(P);
            return factors;
        }
        GenPolynomialRing<AlgebraicNumber<C>> pfac = P.ring; // Q(alpha)[x]
        if (pfac.nvar > 1) {
            throw new IllegalArgumentException("only for univariate polynomials");
        }
        AlgebraicNumber<C> ldcf = P.leadingBaseCoefficient();
        if (!ldcf.isONE()) {
            P = P.monic();
            factors.add(pfac.getONE().multiply(ldcf));
        }

        // search squarefree norm
        long k = 0L;
        long ks = k;
        GenPolynomial<C> res = null;
        boolean sqf = false;
        //int[] klist = new int[]{ 0, 1, 2, 3, -1, -2, -3 , 5, -5, 7, -7, 101, -101, 1001, -1001 };
        //int[] klist = new int[]{ 0, 1, 2, 3, -1, -2, -3 , 5, -5, 7, -7, 23, -23, 167, -167 };
        //int[] klist = new int[] { 0, -1, -2, 1, 2, -3, 3 };
        int[] klist = new int[]{0, -1, -2, 1, 2};
        int ki = 0;
        while (!sqf) {
            // k = 0,1,2,-1,-2
            if (ki >= klist.length) {
                break;
            }
            k = klist[ki];
            ki++;
            // compute norm with x -> ( y - k x )
            ks = k;
            res = PolyUfdUtil.norm(P, ks);
            if (res.isZERO() || res.isConstant()) {
                continue;
            }
            sqf = factorCoeff.isSquarefree(res);
        }
        // if Res is now squarefree, else must take radical factorization
        List<GenPolynomial<C>> nfacs;
        if (!sqf) {
            //res = factorCoeff.squarefreePart(res); // better use obtained factors
            //res = factorCoeff.baseFactors(res).lastKey();
        }
        //res = res.monic();
        nfacs = factorCoeff.baseFactorsRadical(res);
        if (nfacs.size() == 1) {
            factors.add(P);
            return factors;
        }

        // compute gcds of factors with polynomial in Q(alpha)[X]
        GenPolynomial<AlgebraicNumber<C>> Pp = P;
        GenPolynomial<AlgebraicNumber<C>> Ni;
        for (GenPolynomial<C> nfi : nfacs) {
            Ni = PolyUfdUtil.substituteConvertToAlgebraicCoefficients(pfac, nfi, ks);
            // compute gcds of factors with polynomial
            GenPolynomial<AlgebraicNumber<C>> pni = engine.gcd(Ni, Pp);
            if (!pni.leadingBaseCoefficient().isONE()) {
                pni = pni.monic();
            }
            if (!pni.isONE()) {
                factors.add(pni);
                Pp = Pp.divide(pni);
                //             } else {
                //                 GenPolynomial<AlgebraicNumber<C>> qni = Pp.divide(Ni);
                //                 GenPolynomial<AlgebraicNumber<C>> rni = Pp.remainder(Ni);
                //                 continue;
                //                 //throw new RuntimeException("gcd(Ni,Pp) == 1");
            }
        }
        if (!Pp.isZERO() && !Pp.isONE()) { // irreducible rest
            factors.add(Pp);
        }
        return factors;
    }

}
