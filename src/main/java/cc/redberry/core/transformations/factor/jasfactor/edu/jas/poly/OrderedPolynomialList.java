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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


/**
 * Ordered list of polynomials. Mainly for storage and printing / toString and
 * conversions to other representations. Polynomials in this list are sorted
 * according to their head terms.
 *
 * @author Heinz Kredel
 */

public class OrderedPolynomialList<C extends RingElem<C>> extends PolynomialList<C> {


    /**
     * Constructor.
     *
     * @param r polynomial ring factory.
     * @param l list of polynomials.
     */
    public OrderedPolynomialList(GenPolynomialRing<C> r, List<GenPolynomial<C>> l) {
        super(r, sort(r, l));
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object p) {
        if (!super.equals(p)) {
            return false;
        }
        OrderedPolynomialList<C> pl = null;
        try {
            pl = (OrderedPolynomialList<C>) p;
        } catch (ClassCastException ignored) {
        }
        if (pl == null) {
            return false;
        }
        // compare sorted lists
        // done already in super.equals()
        return true;
    }

    /**
     * Sort a list of polynomials with respect to the ascending order of the
     * leading Exponent vectors. The term order is taken from the ring.
     *
     * @param r polynomial ring factory.
     * @param L polynomial list.
     * @return sorted polynomial list from L.
     */
    @SuppressWarnings("unchecked")
    public static <C extends RingElem<C>> List<GenPolynomial<C>> sort(GenPolynomialRing<C> r,
                                                                      List<GenPolynomial<C>> L) {
        if (L == null) {
            return L;
        }
        if (L.size() <= 1) { // nothing to sort
            return L;
        }
        final Comparator<ExpVector> evc = r.tord.getAscendComparator();
        Comparator<GenPolynomial<C>> cmp = new Comparator<GenPolynomial<C>>() {


            public int compare(GenPolynomial<C> p1, GenPolynomial<C> p2) {
                ExpVector e1 = p1.leadingExpVector();
                ExpVector e2 = p2.leadingExpVector();
                if (e1 == null) {
                    return -1; // dont care
                }
                if (e2 == null) {
                    return 1; // dont care
                }
                if (e1.length() != e2.length()) {
                    if (e1.length() > e2.length()) {
                        return 1; // dont care
                    }
                    return -1; // dont care
                }
                return evc.compare(e1, e2);
            }
        };
        GenPolynomial<C>[] s = null;
        try {
            s = (GenPolynomial<C>[]) new GenPolynomial[L.size()];
            int i = 0;
            for (GenPolynomial<C> p : L) {
                s[i++] = p;
            }
            Arrays.sort(s, cmp);
            return new ArrayList<>(Arrays.<GenPolynomial<C>>asList(s));
        } catch (ClassCastException ok) {
        }
        return L; // unsorted
    }

}
