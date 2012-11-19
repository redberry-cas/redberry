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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigInteger;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigRational;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Rational number coefficients factorization algorithms. This class implements
 * factorization methods for polynomials over rational numbers.
 *
 * @author Heinz Kredel
 */

public class FactorRational extends FactorAbsolute<BigRational> {


    /**
     * Factorization engine for integer base coefficients.
     */
    protected final FactorAbstract<BigInteger> iengine;


    /**
     * No argument constructor.
     */
    protected FactorRational() {
        super(BigRational.ONE);
        iengine = FactorFactory.getImplementation(BigInteger.ONE);
    }


    /**
     * GenPolynomial base factorization of a squarefree polynomial.
     *
     * @param P squarefree GenPolynomial.
     * @return [p_1, ..., p_k] with P = prod_{i=1, ..., k} p_i.
     */
    @Override
    public List<GenPolynomial<BigRational>> baseFactorsSquarefree(GenPolynomial<BigRational> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P == null");
        }
        List<GenPolynomial<BigRational>> factors = new ArrayList<>();
        if (P.isZERO()) {
            return factors;
        }
        if (P.isONE()) {
            factors.add(P);
            return factors;
        }
        GenPolynomialRing<BigRational> pfac = P.ring;
        if (pfac.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " only for univariate polynomials");
        }
        GenPolynomial<BigRational> Pr = P;
        BigRational ldcf = P.leadingBaseCoefficient();
        if (!ldcf.isONE()) {
            Pr = Pr.monic();
        }
        BigInteger bi = BigInteger.ONE;
        GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<>(bi, pfac);
        GenPolynomial<BigInteger> Pi = PolyUtil.integerFromRationalCoefficients(ifac, Pr);
        List<GenPolynomial<BigInteger>> ifacts = iengine.baseFactorsSquarefree(Pi);
        if (ifacts.size() <= 1) {
            factors.add(P);
            return factors;
        }
        List<GenPolynomial<BigRational>> rfacts = PolyUtil.fromIntegerCoefficients(pfac, ifacts);
        rfacts = PolyUtil.monic(rfacts);
        if (!ldcf.isONE()) {
            GenPolynomial<BigRational> r = rfacts.get(0);
            rfacts.remove(r);
            r = r.multiply(ldcf);
            rfacts.set(0, r);
        }
        factors.addAll(rfacts);
        return factors;
    }


    /**
     * GenPolynomial factorization of a squarefree polynomial.
     *
     * @param P squarefree GenPolynomial.
     * @return [p_1, ..., p_k] with P = prod_{i=1, ..., k} p_i.
     */
    @Override
    public List<GenPolynomial<BigRational>> factorsSquarefree(GenPolynomial<BigRational> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P == null");
        }
        List<GenPolynomial<BigRational>> factors = new ArrayList<>();
        if (P.isZERO()) {
            return factors;
        }
        if (P.isONE()) {
            factors.add(P);
            return factors;
        }
        GenPolynomialRing<BigRational> pfac = P.ring;
        if (pfac.nvar == 1) {
            return baseFactorsSquarefree(P);
        }
        GenPolynomial<BigRational> Pr = P;
        BigRational ldcf = P.leadingBaseCoefficient();
        if (!ldcf.isONE()) {
            Pr = Pr.monic();
        }
        BigInteger bi = BigInteger.ONE;
        GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<>(bi, pfac);
        GenPolynomial<BigInteger> Pi = PolyUtil.integerFromRationalCoefficients(ifac, Pr);
        List<GenPolynomial<BigInteger>> ifacts = iengine.factorsSquarefree(Pi);
        if (ifacts.size() <= 1) {
            factors.add(P);
            return factors;
        }
        List<GenPolynomial<BigRational>> rfacts = PolyUtil.fromIntegerCoefficients(pfac, ifacts);
        rfacts = PolyUtil.monic(rfacts);
        if (!ldcf.isONE()) {
            GenPolynomial<BigRational> r = rfacts.get(0);
            rfacts.remove(r);
            r = r.multiply(ldcf);
            rfacts.set(0, r);
        }
        factors.addAll(rfacts);
        return factors;
    }

}
