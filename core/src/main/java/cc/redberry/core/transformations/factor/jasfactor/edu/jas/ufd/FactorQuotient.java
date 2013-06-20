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
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;

import java.util.ArrayList;
import java.util.List;


/**
 * Rational function coefficients factorization algorithms. This class
 * implements factorization methods for polynomials over rational functions,
 * that is, with coefficients from class <code>application.Quotient</code>.
 *
 * @author Heinz Kredel
 */

public class FactorQuotient<C extends GcdRingElem<C>> extends FactorAbstract<Quotient<C>> {


    //private final boolean debug = false;


    /**
     * Factorization engine for normal coefficients.
     */
    protected final FactorAbstract<C> nengine;


    /**
     * Constructor.
     *
     * @param fac coefficient quotient ring factory.
     */
    public FactorQuotient(QuotientRing<C> fac) {
        this(fac, FactorFactory.<C>getImplementation(fac.ring.coFac));
    }


    /**
     * Constructor.
     *
     * @param fac     coefficient quotient ring factory.
     * @param nengine factorization engine for polynomials over base
     *                coefficients.
     */
    public FactorQuotient(QuotientRing<C> fac, FactorAbstract<C> nengine) {
        super(fac);
        this.nengine = nengine;
    }


    /**
     * GenPolynomial base factorization of a squarefree polynomial.
     *
     * @param P squarefree GenPolynomial.
     * @return [p_1, ..., p_k] with P = prod_{i=1, ..., k} p_i.
     */
    @Override
    public List<GenPolynomial<Quotient<C>>> baseFactorsSquarefree(GenPolynomial<Quotient<C>> P) {
        return factorsSquarefree(P);
    }


    /**
     * GenPolynomial factorization of a squarefree polynomial.
     *
     * @param P squarefree GenPolynomial.
     * @return [p_1, ..., p_k] with P = prod_{i=1, ..., k} p_i.
     */
    @Override
    public List<GenPolynomial<Quotient<C>>> factorsSquarefree(GenPolynomial<Quotient<C>> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P == null");
        }
        List<GenPolynomial<Quotient<C>>> factors = new ArrayList<>();
        if (P.isZERO()) {
            return factors;
        }
        if (P.isONE()) {
            factors.add(P);
            return factors;
        }
        GenPolynomialRing<Quotient<C>> pfac = P.ring;
        GenPolynomial<Quotient<C>> Pr = P;
        Quotient<C> ldcf = P.leadingBaseCoefficient();
        if (!ldcf.isONE()) {
            Pr = Pr.monic();
        }
        QuotientRing<C> qi = (QuotientRing<C>) pfac.coFac;
        GenPolynomialRing<C> ci = qi.ring;
        GenPolynomialRing<GenPolynomial<C>> ifac = new GenPolynomialRing<>(ci, pfac);
        GenPolynomial<GenPolynomial<C>> Pi = PolyUfdUtil.integralFromQuotientCoefficients(ifac, Pr);

        // factor in C[x_1,...,x_n][y_1,...,y_m]
        List<GenPolynomial<GenPolynomial<C>>> irfacts = nengine.recursiveFactorsSquarefree(Pi);
        if (irfacts.size() <= 1) {
            factors.add(P);
            return factors;
        }
        List<GenPolynomial<Quotient<C>>> qfacts = PolyUfdUtil.quotientFromIntegralCoefficients(pfac,
                irfacts);
        //qfacts = PolyUtil.monic(qfacts);
        if (!ldcf.isONE()) {
            GenPolynomial<Quotient<C>> r = qfacts.get(0);
            qfacts.remove(r);
            r = r.multiply(ldcf);
            qfacts.add(0, r);
        }
        factors.addAll(qfacts);
        return factors;
    }

}
