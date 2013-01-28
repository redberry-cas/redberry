/*
 * JAS: Java Algebra System.
 *
 * Copyright (c) 2000-2013:
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
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.vector.BasicLinAlg;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Groebner Bases abstract class.
 * Implements common Groebner bases and GB test methods.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public class GroebnerBase<C extends RingElem<C>>
        implements Serializable {

    /**
     * Reduction engine.
     */
    public final Reduction<C> red;


    /**
     * linear algebra engine.
     */
    public final BasicLinAlg<GenPolynomial<C>> blas;


    /**
     * Constructor.
     */
    public GroebnerBase() {
        this(new ReductionSeq<C>());
    }


    /**
     * Constructor.
     *
     * @param red Reduction engine
     */
    public GroebnerBase(Reduction<C> red) {
        this.red = red;
        blas = new BasicLinAlg<>();
    }

    /**
     * Common zero test.
     *
     * @param F polynomial list.
     * @return -1, 0 or 1 if dimension(ideal(F)) &eq; -1, 0 or &ge; 1.
     */
    public int commonZeroTest(List<GenPolynomial<C>> F) {
        if (F == null || F.isEmpty()) {
            return 1;
        }
        GenPolynomialRing<C> pfac = F.get(0).ring;
        if (pfac.nvar <= 0) {
            return -1;
        }
        //int uht = 0;
        Set<Integer> v = new HashSet<>(); // for non reduced GBs
        for (GenPolynomial<C> p : F) {
            if (p.isZERO()) {
                continue;
            }
            if (p.isConstant()) { // for non-monic lists
                return -1;
            }
            ExpVector e = p.leadingExpVector();
            if (e == null) {
                continue;
            }
            int[] u = e.dependencyOnVariables();
            if (u == null) {
                continue;
            }
            if (u.length == 1) {
                //uht++;
                v.add(u[0]);
            }
        }
        if (pfac.nvar == v.size()) {
            return 0;
        }
        return 1;
    }

}
