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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.gb;

import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;

import java.io.Serializable;
import java.util.List;


/**
 * Polynomial Reduction interface.
 * Defines S-Polynomial, normalform, criterion 4, module criterion
 * and irreducible set.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public interface Reduction<C extends RingElem<C>>
        extends Serializable {

    /**
     * Normalform.
     *
     * @param A polynomial.
     * @param P polynomial list.
     * @return nf(A) with respect to P.
     */
    public GenPolynomial<C> normalform(List<GenPolynomial<C>> P,
                                       GenPolynomial<C> A);


    /**
     * Irreducible set.
     *
     * @param Pp polynomial list.
     * @return a list P of polynomials which are in normalform wrt. P and with ideal(Pp) = ideal(P).
     */
    public List<GenPolynomial<C>> irreducibleSet(List<GenPolynomial<C>> Pp);

}
