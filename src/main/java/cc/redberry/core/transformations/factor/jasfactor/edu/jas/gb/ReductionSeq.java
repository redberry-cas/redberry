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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.gb;

import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.ExpVector;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;

import java.util.List;
import java.util.Map;


/**
 * Polynomial Reduction sequential use algorithm.
 * Implements normalform.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public class ReductionSeq<C extends RingElem<C>> // should be FieldElem<C>>
        extends ReductionAbstract<C> {


    /**
     * Constructor.
     */
    public ReductionSeq() {
    }


    /**
     * Normalform.
     *
     * @param Ap polynomial.
     * @param Pp polynomial list.
     * @return nf(Ap) with respect to Pp.
     */
    @SuppressWarnings("unchecked")
    public GenPolynomial<C> normalform(List<GenPolynomial<C>> Pp,
                                       GenPolynomial<C> Ap) {
        if (Pp == null || Pp.isEmpty()) {
            return Ap;
        }
        if (Ap == null || Ap.isZERO()) {
            return Ap;
        }
        if (!Ap.ring.coFac.isField()) {
            throw new IllegalArgumentException("coefficients not from a field");
        }
        Map.Entry<ExpVector, C> m;
        int l;
        GenPolynomial<C>[] P;

        l = Pp.size();
        P = new GenPolynomial[l];
        //P = Pp.toArray();
        for (int i = 0; i < Pp.size(); i++) {
            P[i] = Pp.get(i);
        }
        ExpVector[] htl = new ExpVector[l];
        Object[] lbc = new Object[l]; // want C[]
        GenPolynomial<C>[] p = new GenPolynomial[l];
        int i;
        int j = 0;
        for (i = 0; i < l; i++) {
            p[i] = P[i];
            m = p[i].leadingMonomial();
            if (m != null) {
                p[j] = p[i];
                htl[j] = m.getKey();
                lbc[j] = m.getValue();
                j++;
            }
        }
        l = j;
        ExpVector e;
        C a;
        boolean mt = false;
        GenPolynomial<C> R = Ap.ring.getZERO();

        //GenPolynomial<C> T = null;
        GenPolynomial<C> Q = null;
        GenPolynomial<C> S = Ap;
        while (S.length() > 0) {
            m = S.leadingMonomial();
            e = m.getKey();
            a = m.getValue();
            for (i = 0; i < l; i++) {
                mt = e.multipleOf(htl[i]);
                if (mt) break;
            }
            if (!mt) {
                //T = new OrderedMapPolynomial( a, e );
                R = R.sum(a, e);
                S = S.subtract(a, e);
            } else {
                e = e.subtract(htl[i]);
                a = a.divide((C) lbc[i]);
                Q = p[i].multiply(a, e);
                S = S.subtract(Q);
            }
        }
        return R;
    }


}
