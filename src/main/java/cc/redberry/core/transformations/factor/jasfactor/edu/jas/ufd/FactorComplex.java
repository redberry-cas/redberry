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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.*;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;

import java.util.ArrayList;
import java.util.List;


/**
 * Complex coefficients factorization algorithms. This class implements
 * factorization methods for polynomials over Complex numbers via the algebraic
 * number C(i) over rational numbers or over (prime) modular integers. <b>Note:</b>
 * Decomposition to linear factors is only via absolute factorization since
 * Complex are not the analytic complex numbers.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public class FactorComplex<C extends GcdRingElem<C>> extends FactorAbsolute<Complex<C>> {

    /**
     * Factorization engine for algebraic coefficients.
     */
    public final FactorAbstract<AlgebraicNumber<C>> factorAlgeb;


    /**
     * Complex algebraic factory.
     */
    public final AlgebraicNumberRing<C> afac;


    /**
     * Constructor.
     *
     * @param fac complex number factory.
     */
    public FactorComplex(ComplexRing<C> fac) {
        super(fac);
        this.afac = fac.algebraicRing();
        this.factorAlgeb = FactorFactory.getImplementation(afac);
    }


    /**
     * GenPolynomial base factorization of a squarefree polynomial.
     *
     * @param P squarefree GenPolynomial&lt;AlgebraicNumber&lt;C&gt;&gt;.
     * @return [p_1, ..., p_k] with P = prod_{i=1, ..., k} p_i.
     */
    @Override
    public List<GenPolynomial<Complex<C>>> baseFactorsSquarefree(GenPolynomial<Complex<C>> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P == null");
        }
        List<GenPolynomial<Complex<C>>> factors = new ArrayList<>();
        if (P.isZERO()) {
            return factors;
        }
        if (P.isONE()) {
            factors.add(P);
            return factors;
        }
        GenPolynomialRing<Complex<C>> pfac = P.ring; // CC[x]
        if (pfac.nvar > 1) {
            throw new IllegalArgumentException("only for univariate polynomials");
        }
        ComplexRing<C> cfac = (ComplexRing<C>) pfac.coFac;
        if (!afac.ring.coFac.equals(cfac.ring)) {
            throw new IllegalArgumentException("coefficient rings do not match");
        }
        Complex<C> ldcf = P.leadingBaseCoefficient();
        if (!ldcf.isONE()) {
            P = P.monic();
            factors.add(pfac.getONE().multiply(ldcf));
        }
        GenPolynomialRing<AlgebraicNumber<C>> pafac = new GenPolynomialRing<>(afac, pfac);
        GenPolynomial<AlgebraicNumber<C>> A = PolyUtil.algebraicFromComplex(pafac, P);
        List<GenPolynomial<AlgebraicNumber<C>>> afactors = factorAlgeb.baseFactorsSquarefree(A);
        for (GenPolynomial<AlgebraicNumber<C>> pa : afactors) {
            GenPolynomial<Complex<C>> pc = PolyUtil.complexFromAlgebraic(pfac, pa);
            factors.add(pc);
        }
        return factors;
    }

}
