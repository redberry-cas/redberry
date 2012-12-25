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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigInteger;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.Modular;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;

import java.io.Serializable;


/**
 * Container for the approximation result from a Hensel algorithm.
 *
 * @param <MOD> coefficient type
 * @author Heinz Kredel
 */

public class HenselApprox<MOD extends GcdRingElem<MOD> & Modular> implements Serializable {


    /**
     * Approximated polynomial with integer coefficients.
     */
    public final GenPolynomial<BigInteger> A;


    /**
     * Approximated polynomial with integer coefficients.
     */
    public final GenPolynomial<BigInteger> B;


    /**
     * Modular approximated polynomial with modular coefficients.
     */
    public final GenPolynomial<MOD> Am;


    /**
     * Modular approximated polynomial with modular coefficients.
     */
    public final GenPolynomial<MOD> Bm;


    /**
     * Constructor.
     *
     * @param A  approximated polynomial.
     * @param B  approximated polynomial.
     * @param Am approximated modular polynomial.
     * @param Bm approximated modular polynomial.
     */
    public HenselApprox(GenPolynomial<BigInteger> A, GenPolynomial<BigInteger> B, GenPolynomial<MOD> Am,
                        GenPolynomial<MOD> Bm) {
        this.A = A;
        this.B = B;
        this.Am = Am;
        this.Bm = Bm;
    }


    /**
     * Get the String representation.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(A.toString());
        sb.append(",");
        sb.append(B.toString());
        sb.append(",");
        sb.append(Am.toString());
        sb.append(",");
        sb.append(Bm.toString());
        return sb.toString();
    }

    /**
     * Hash code for this Factors.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int h = A.hashCode();
        h = 37 * h + B.hashCode();
        h = 37 * h + Am.hashCode();
        h = 37 * h + Bm.hashCode();
        return h;
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object B) {
        if (!(B instanceof HenselApprox)) {
            return false;
        }
        HenselApprox<MOD> a = null;
        try {
            a = (HenselApprox<MOD>) B;
        } catch (ClassCastException ignored) {
        }
        if (a == null) {
            return false;
        }
        return A.equals(a.A) && B.equals(a.B) && Am.equals(a.Am) && Bm.equals(a.Bm);
    }

}
