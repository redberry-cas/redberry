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

import java.util.List;


/**
 * Container for optimization results.
 *
 * @author Heinz Kredel
 */

public class OptimizedPolynomialList<C extends RingElem<C>> extends PolynomialList<C> {


    /**
     * Permutation vector used to optimize term order.
     */
    public final List<Integer> perm;


    /**
     * Constructor.
     */
    public OptimizedPolynomialList(List<Integer> P, GenPolynomialRing<C> R, List<GenPolynomial<C>> L) {
        super(R, L);
        perm = P;
    }


    /**
     * String representation.
     */
    @Override
    public String toString() {
        return "permutation = " + perm + "\n" + super.toString();
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object B) {
        if (!(B instanceof OptimizedPolynomialList)) {
            return false;
        }
        return super.equals(B);
    }

}
