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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * List of polynomials.
 * Mainly for storage and printing / toString and
 * conversions to other representations.
 *
 * @author Heinz Kredel
 */

public class PolynomialList<C extends RingElem<C>>
        implements Comparable<PolynomialList<C>>, Serializable {


    /**
     * The factory for the solvable polynomial ring.
     */
    public final GenPolynomialRing<C> ring;


    /**
     * The data structure is a List of polynomials.
     */
    public final List<GenPolynomial<C>> list;


    /**
     * Constructor.
     *
     * @param r polynomial ring factory.
     * @param l list of polynomials.
     */
    public PolynomialList(GenPolynomialRing<C> r,
                          List<GenPolynomial<C>> l) {
        ring = r;
        list = l;
    }

    /**
     * Copy this.
     *
     * @return a copy of this.
     */
    public PolynomialList<C> copy() {
        return new PolynomialList<>(ring, new ArrayList<>(list));
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object p) {
        if (!(p instanceof PolynomialList)) {
            return false;
        }
        PolynomialList<C> pl = null;
        try {
            pl = (PolynomialList<C>) p;
        } catch (ClassCastException ignored) {
        }
        if (pl == null) {
            return false;
        }
        if (!ring.equals(pl.ring)) {
            return false;
        }
        return (compareTo(pl) == 0);
        // otherwise tables may be different
    }


    /**
     * Polynomial list comparison.
     *
     * @param L other PolynomialList.
     * @return lexicographical comparison, sign of first different polynomials.
     */
    public int compareTo(PolynomialList<C> L) {
        int si = L.list.size();
        if (list.size() < si) { // minimum
            si = list.size();
        }
        int s = 0;
        List<GenPolynomial<C>> l1 = OrderedPolynomialList.sort(ring, list);
        List<GenPolynomial<C>> l2 = OrderedPolynomialList.sort(ring, L.list);
        for (int i = 0; i < si; i++) {
            GenPolynomial<C> a = l1.get(i);
            GenPolynomial<C> b = l2.get(i);
            s = a.compareTo(b);
            if (s != 0) {
                return s;
            }
        }
        if (list.size() > si) {
            return 1;
        }
        if (L.list.size() > si) {
            return -1;
        }
        return s;
    }


    /**
     * Hash code for this polynomial list.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int h;
        h = ring.hashCode();
        h = 37 * h + (list == null ? 0 : list.hashCode());
        return h;
    }


    /**
     * String representation of the polynomial list.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder erg = new StringBuilder();
        String[] vars = null;
        if (ring != null) {
            erg.append(ring.toString());
            vars = ring.getVars();
        }
        boolean first = true;
        erg.append("\n(\n");
        String sa = null;
        for (GenPolynomial<C> oa : list) {
            if (vars != null) {
                sa = oa.toString(vars);
            } else {
                sa = oa.toString();
            }
            if (first) {
                first = false;
            } else {
                erg.append(", ");
                if (sa.length() > 10) {
                    erg.append("\n");
                }
            }
            erg.append("( ").append(sa).append(" )");
        }
        erg.append("\n)");
        return erg.toString();
    }

}
