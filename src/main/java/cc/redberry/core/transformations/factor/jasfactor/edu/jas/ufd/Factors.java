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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.AlgebraicNumber;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.AlgebraicNumberRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;

import java.util.List;


/**
 * Container for the factors of absolute factorization.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public class Factors<C extends GcdRingElem<C>> {


    /**
     * Original (irreducible) polynomial to be factored with coefficients from C.
     */
    public final GenPolynomial<C> poly;


    /**
     * Algebraic field extension over C. Should be null, if p is absolutely
     * irreducible.
     */
    public final AlgebraicNumberRing<C> afac;


    /**
     * Original polynomial to be factored with coefficients from
     * AlgebraicNumberRing&lt;C&gt;. Should be null, if p is absolutely irreducible.
     */
    public final GenPolynomial<AlgebraicNumber<C>> apoly;


    /**
     * List of factors with coefficients from AlgebraicNumberRing&lt;C&gt;. Should be
     * null, if p is absolutely irreducible.
     */
    public final List<GenPolynomial<AlgebraicNumber<C>>> afactors;


    /**
     * List of factors with coefficients from AlgebraicNumberRing&lt;AlgebraicNumber&lt;C&gt;&gt;.
     * Should be null, if p is absolutely irreducible.
     */
    public final List<Factors<AlgebraicNumber<C>>> arfactors;


    /**
     * Constructor.
     *
     * @param p      irreducible GenPolynomial over C.
     * @param af     algebraic extension field of C where p has factors from afact.
     * @param ap     GenPolynomial p represented with coefficients from af.
     * @param afact  absolute irreducible factors of p with coefficients from af.
     * @param arfact further absolute irreducible factors of p with coefficients from extensions of af.
     */
    public Factors(GenPolynomial<C> p, AlgebraicNumberRing<C> af, GenPolynomial<AlgebraicNumber<C>> ap,
                   List<GenPolynomial<AlgebraicNumber<C>>> afact, List<Factors<AlgebraicNumber<C>>> arfact) {
        poly = p;
        afac = af;
        apoly = ap;
        afactors = afact;
        arfactors = arfact;
    }

    /**
     * Hash code for this Factors.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int h;
        h = poly.hashCode();
        if (afac == null) {
            return h;
        }
        h = (h << 27);
        h += afac.hashCode();
        if (afactors != null) {
            h = (h << 27);
            h += afactors.hashCode();
        }
        if (arfactors != null) {
            h = (h << 27);
            h += arfactors.hashCode();
        }
        return h;
    }


}
