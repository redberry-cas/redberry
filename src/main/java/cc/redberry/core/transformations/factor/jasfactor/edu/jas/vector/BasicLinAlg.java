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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.vector;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Basic linear algebra methods. Implements Basic linear algebra computations
 * and tests. <b>Note:</b> will use wrong method dispatch in JRE when used with
 * GenSolvablePolynomial.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public class BasicLinAlg<C extends RingElem<C>> {


    //private final boolean debug = false;


    /**
     * Constructor.
     */
    public BasicLinAlg() {
    }

    /**
     * Addition of vectors of ring elements.
     *
     * @param a a ring element list.
     * @param b a ring element list.
     * @return a+b, the vector sum of a and b.
     */

    public List<C> vectorAdd(List<C> a, List<C> b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        List<C> V = new ArrayList<>(a.size());
        Iterator<C> it = a.iterator();
        Iterator<C> jt = b.iterator();
        while (it.hasNext() && jt.hasNext()) {
            C pi = it.next();
            C pj = jt.next();
            C p = pi.sum(pj);
            V.add(p);
        }
        if (it.hasNext() || jt.hasNext()) {
        }
        return V;
    }


    /**
     * Test vector of zero ring elements.
     *
     * @param a a ring element list.
     * @return true, if all polynomial in a are zero, else false.
     */
    public boolean isZero(List<C> a) {
        if (a == null) {
            return true;
        }
        for (C pi : a) {
            if (pi == null) {
                continue;
            }
            if (!pi.isZERO()) {
                return false;
            }
        }
        return true;
    }

}
