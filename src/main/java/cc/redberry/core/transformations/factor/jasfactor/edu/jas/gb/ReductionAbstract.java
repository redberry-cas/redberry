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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.ExpVector;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;

import java.util.ArrayList;
import java.util.List;


/**
 * Polynomial Reduction abstract class. Implements common S-Polynomial,
 * normalform, criterion 4 module criterion and irreducible set.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public abstract class ReductionAbstract<C extends RingElem<C>> implements Reduction<C> {


    /**
     * Constructor.
     */
    public ReductionAbstract() {
    }


    /**
     * Irreducible set.
     *
     * @param Pp polynomial list.
     * @return a list P of monic polynomials which are in normalform wrt. P and
     *         with ideal(Pp) = ideal(P).
     */
    public List<GenPolynomial<C>> irreducibleSet(List<GenPolynomial<C>> Pp) {
        ArrayList<GenPolynomial<C>> P = new ArrayList<>();
        for (GenPolynomial<C> a : Pp) {
            if (a.length() != 0) {
                a = a.monic();
                if (a.isONE()) {
                    P.clear();
                    P.add(a);
                    return P;
                }
                P.add(a);
            }
        }
        int l = P.size();
        if (l <= 1)
            return P;

        int irr = 0;
        ExpVector e;
        ExpVector f;
        GenPolynomial<C> a;
        while (irr != l) {
            //it = P.listIterator(); 
            //a = P.get(0); //it.next();
            a = P.remove(0);
            e = a.leadingExpVector();
            a = normalform(P, a);
            if (a.length() == 0) {
                l--;
                if (l <= 1) {
                    return P;
                }
            } else {
                f = a.leadingExpVector();
                if (f.signum() == 0) {
                    P = new ArrayList<>();
                    P.add(a.monic());
                    return P;
                }
                if (e.equals(f)) {
                    irr++;
                } else {
                    irr = 0;
                    a = a.monic();
                }
                P.add(a);
            }
        }
        return P;
    }
}
