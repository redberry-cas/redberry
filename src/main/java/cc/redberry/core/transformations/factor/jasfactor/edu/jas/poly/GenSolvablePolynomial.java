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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly;

import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;


/**
 * GenSolvablePolynomial generic solvable polynomials implementing RingElem.
 * n-variate ordered solvable polynomials over C.
 * Objects of this class are intended to be immutable.
 * The implementation is based on TreeMap respectively SortedMap
 * from exponents to coefficients by extension of GenPolybomial.
 * Only the coefficients are modeled with generic types,
 * the exponents are fixed to ExpVector with long entries
 * (this will eventually be changed in the future).
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public class GenSolvablePolynomial<C extends RingElem<C>>
        extends GenPolynomial<C> {
    //not possible: implements RingElem< GenSolvablePolynomial<C> > {


    /**
     * The factory for the solvable polynomial ring.
     * Hides super.ring.
     */
    public final GenSolvablePolynomialRing<C> ring;


    /**
     * Constructor for zero GenSolvablePolynomial.
     *
     * @param r solvable polynomial ring factory.
     */
    public GenSolvablePolynomial(GenSolvablePolynomialRing<C> r) {
        super(r);
        ring = r;
    }


    /**
     * Constructor for GenSolvablePolynomial.
     *
     * @param r solvable polynomial ring factory.
     * @param c coefficient.
     * @param e exponent.
     */
    public GenSolvablePolynomial(GenSolvablePolynomialRing<C> r,
                                 C c, ExpVector e) {
        this(r);
        if (c != null && !c.isZERO()) {
            val.put(e, c);
        }
    }


    /**
     * Constructor for GenSolvablePolynomial.
     *
     * @param r solvable polynomial ring factory.
     * @param v the SortedMap of some other (solvable) polynomial.
     */
    protected GenSolvablePolynomial(GenSolvablePolynomialRing<C> r,
                                    SortedMap<ExpVector, C> v) {
        this(r);
        val.putAll(v); // assume no zero coefficients
    }


    /**
     * Get the corresponding element factory.
     *
     * @return factory for this Element.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Element#factory()
     */
    public GenSolvablePolynomialRing<C> factory() {
        return ring;
    }


    /**
     * Clone this GenSolvablePolynomial.
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public GenSolvablePolynomial<C> copy() {
        //return ring.copy(this);
        return new GenSolvablePolynomial<>(ring, this.val);
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object B) {
        if (!(B instanceof GenSolvablePolynomial)) {
            return false;
        }
        return super.equals(B);
    }


    /**
     * GenSolvablePolynomial multiplication.
     *
     * @param Bp GenSolvablePolynomial.
     * @return this*Bp, where * denotes solvable multiplication.
     */
    public GenSolvablePolynomial<C> multiply(GenSolvablePolynomial<C> Bp) {
        if (Bp == null || Bp.isZERO()) {
            return ring.getZERO();
        }
        if (this.isZERO()) {
            return this;
        }
        assert (ring.nvar == Bp.ring.nvar);
        ExpVector Z = ring.evzero;
        GenSolvablePolynomial<C> Cp = ring.getZERO().copy();
        GenSolvablePolynomial<C> zero = ring.getZERO().copy();
        C one = ring.getONECoefficient();

        GenSolvablePolynomial<C> C1;
        GenSolvablePolynomial<C> C2;
        Map<ExpVector, C> A = val;
        Map<ExpVector, C> B = Bp.val;
        Set<Map.Entry<ExpVector, C>> Bk = B.entrySet();
        for (Map.Entry<ExpVector, C> y : A.entrySet()) {
            C a = y.getValue();
            ExpVector e = y.getKey();
            int[] ep = e.dependencyOnVariables();
            int el1 = ring.nvar + 1;
            if (ep.length > 0) {
                el1 = ep[0];
            }
            int el1s = ring.nvar + 1 - el1;
            for (Map.Entry<ExpVector, C> x : Bk) {
                C b = x.getValue();
                ExpVector f = x.getKey();
                int[] fp = f.dependencyOnVariables();
                int fl1 = 0;
                if (fp.length > 0) {
                    fl1 = fp[fp.length - 1];
                }
                int fl1s = ring.nvar + 1 - fl1;
                GenSolvablePolynomial<C> Cs;
                if (el1s <= fl1s) { // symmetric
                    ExpVector g = e.sum(f);
                    Cs = (GenSolvablePolynomial<C>) zero.sum(one, g); // symmetric!
                    //Cs = new GenSolvablePolynomial<C>(ring,one,g); // symmetric!
                } else { // unsymmetric
                    // split e = e1 * e2, f = f1 * f2
                    ExpVector e1 = e.subst(el1, 0);
                    ExpVector e2 = Z.subst(el1, e.getVal(el1));
                    ExpVector e4;
                    ExpVector f1 = f.subst(fl1, 0);
                    ExpVector f2 = Z.subst(fl1, f.getVal(fl1));
                    TableRelation<C> rel = ring.table.lookup(e2, f2);
                    Cs = rel.p; //ring.copy( rel.p ); // do not clone() 
                    if (rel.f != null) {
                        C2 = (GenSolvablePolynomial<C>) zero.sum(one, rel.f);
                        Cs = Cs.multiply(C2);
                        if (rel.e == null) {
                            e4 = e2;
                        } else {
                            e4 = e2.subtract(rel.e);
                        }
                        ring.table.update(e4, f2, Cs);
                    }
                    if (rel.e != null) {
                        C1 = (GenSolvablePolynomial<C>) zero.sum(one, rel.e);
                        Cs = C1.multiply(Cs);
                        ring.table.update(e2, f2, Cs);
                    }
                    if (!f1.isZERO()) {
                        C2 = (GenSolvablePolynomial<C>) zero.sum(one, f1);
                        Cs = Cs.multiply(C2);
                        //ring.table.update(?,f1,Cs)
                    }
                    if (!e1.isZERO()) {
                        C1 = (GenSolvablePolynomial<C>) zero.sum(one, e1);
                        Cs = C1.multiply(Cs);
                        //ring.table.update(e1,?,Cs)
                    }
                }
                //C c = a.multiply(b);
                Cs = Cs.multiply(a, b); // now non-symmetric // Cs.multiply(c); is symmetric!
                Cp = (GenSolvablePolynomial<C>) Cp.sum(Cs);
            }
        }
        return Cp;
    }


    /**
     * GenSolvablePolynomial left and right multiplication. Product with
     * two polynomials.
     *
     * @param S GenSolvablePolynomial.
     * @param T GenSolvablePolynomial.
     * @return S*this*T.
     */
    public GenSolvablePolynomial<C> multiply(GenSolvablePolynomial<C> S, GenSolvablePolynomial<C> T) {
        if (S.isZERO() || T.isZERO() || this.isZERO()) {
            return ring.getZERO();
        }
        if (S.isONE()) {
            return multiply(T);
        }
        if (T.isONE()) {
            return S.multiply(this);
        }
        return S.multiply(this).multiply(T);
    }


    /**
     * GenSolvablePolynomial multiplication.
     * Product with coefficient ring element.
     *
     * @param b coefficient.
     * @return this*b, where * is usual multiplication.
     */
    @Override
    public GenSolvablePolynomial<C> multiply(C b) {
        GenSolvablePolynomial<C> Cp = ring.getZERO().copy();
        if (b == null || b.isZERO()) {
            return Cp;
        }
        Map<ExpVector, C> Cm = Cp.val; //getMap();
        Map<ExpVector, C> Am = val;
        for (Map.Entry<ExpVector, C> y : Am.entrySet()) {
            ExpVector e = y.getKey();
            C a = y.getValue();
            C c = a.multiply(b);
            if (!c.isZERO()) {
                Cm.put(e, c);
            }
        }
        return Cp;
    }


    /**
     * GenSolvablePolynomial left and right multiplication.
     * Product with coefficient ring element.
     *
     * @param b coefficient.
     * @param c coefficient.
     * @return b*this*c, where * is coefficient multiplication.
     */
    public GenSolvablePolynomial<C> multiply(C b, C c) {
        GenSolvablePolynomial<C> Cp = ring.getZERO().copy();
        if (b == null || b.isZERO()) {
            return Cp;
        }
        if (c == null || c.isZERO()) {
            return Cp;
        }
        Map<ExpVector, C> Cm = Cp.val; //getMap();
        Map<ExpVector, C> Am = val;
        for (Map.Entry<ExpVector, C> y : Am.entrySet()) {
            ExpVector e = y.getKey();
            C a = y.getValue();
            C d = b.multiply(a).multiply(c);
            if (!d.isZERO()) {
                Cm.put(e, d);
            }
        }
        return Cp;
    }


    /**
     * GenSolvablePolynomial multiplication.
     * Product with exponent vector.
     *
     * @param e exponent.
     * @return this * x<sup>e</sup>,
     *         where * denotes solvable multiplication.
     */
    @Override
    public GenSolvablePolynomial<C> multiply(ExpVector e) {
        if (e == null || e.isZERO()) {
            return this;
        }
        C b = ring.getONECoefficient();
        return multiply(b, e);
    }


    /**
     * GenSolvablePolynomial left and right multiplication.
     * Product with exponent vector.
     *
     * @param e exponent.
     * @param f exponent.
     * @return x<sup>e</sup> * this * x<sup>f</sup>,
     *         where * denotes solvable multiplication.
     */
    public GenSolvablePolynomial<C> multiply(ExpVector e, ExpVector f) {
        if (e == null || e.isZERO()) {
            return this;
        }
        if (f == null || f.isZERO()) {
            return this;
        }
        C b = ring.getONECoefficient();
        return multiply(b, e, b, f);
    }


    /**
     * GenSolvablePolynomial multiplication.
     * Product with ring element and exponent vector.
     *
     * @param b coefficient.
     * @param e exponent.
     * @return this * b x<sup>e</sup>,
     *         where * denotes solvable multiplication.
     */
    @Override
    public GenSolvablePolynomial<C> multiply(C b, ExpVector e) {
        if (b == null || b.isZERO()) {
            return ring.getZERO();
        }
        GenSolvablePolynomial<C> Cp = new GenSolvablePolynomial<>(ring, b, e);
        return multiply(Cp);
    }


    /**
     * GenSolvablePolynomial left and right multiplication.
     * Product with ring element and exponent vector.
     *
     * @param b coefficient.
     * @param e exponent.
     * @param c coefficient.
     * @param f exponent.
     * @return b x<sup>e</sup> * this * c x<sup>f</sup>,
     *         where * denotes solvable multiplication.
     */
    public GenSolvablePolynomial<C> multiply(C b, ExpVector e, C c, ExpVector f) {
        if (b == null || b.isZERO()) {
            return ring.getZERO();
        }
        if (c == null || c.isZERO()) {
            return ring.getZERO();
        }
        GenSolvablePolynomial<C> Cp = new GenSolvablePolynomial<>(ring, b, e);
        GenSolvablePolynomial<C> Dp = new GenSolvablePolynomial<>(ring, c, f);
        return multiply(Cp, Dp);
    }

}
