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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.NotInvertibleException;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;

import java.util.*;


/**
 * GenPolynomial generic polynomials implementing RingElem. n-variate ordered
 * polynomials over C. Objects of this class are intended to be immutable. The
 * implementation is based on TreeMap respectively SortedMap from exponents to
 * coefficients. Only the coefficients are modeled with generic types, the
 * exponents are fixed to ExpVector with long entries (this will eventually be
 * changed in the future). C can also be a non integral domain, e.g. a
 * ModInteger, i.e. it may contain zero divisors, since multiply() does now
 * check for zeros. <b>Note:</b> multiply() now checks for wrong method dispatch
 * for GenSolvablePolynomial.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 */
public class GenPolynomial<C extends RingElem<C>> implements RingElem<GenPolynomial<C>>, /* not yet Polynomial<C> */
        Iterable<Monomial<C>> {


    /**
     * The factory for the polynomial ring.
     */
    public final GenPolynomialRing<C> ring;


    /**
     * The data structure for polynomials.
     */
    protected final SortedMap<ExpVector, C> val; // do not change to TreeMap


    // protected GenPolynomial() { ring = null; val = null; } // don't use


    /**
     * Private constructor for GenPolynomial.
     *
     * @param r polynomial ring factory.
     * @param t TreeMap with correct ordering.
     */
    private GenPolynomial(GenPolynomialRing<C> r, TreeMap<ExpVector, C> t) {
        ring = r;
        val = t;
    }


    /**
     * Constructor for zero GenPolynomial.
     *
     * @param r polynomial ring factory.
     */
    public GenPolynomial(GenPolynomialRing<C> r) {
        this(r, new TreeMap<ExpVector, C>(r.tord.getDescendComparator()));
    }


    /**
     * Constructor for GenPolynomial c * x<sup>e</sup>.
     *
     * @param r polynomial ring factory.
     * @param c coefficient.
     * @param e exponent.
     */
    public GenPolynomial(GenPolynomialRing<C> r, C c, ExpVector e) {
        this(r);
        if (!c.isZERO()) {
            val.put(e, c);
        }
    }


    /**
     * Constructor for GenPolynomial c * x<sup>0</sup>.
     *
     * @param r polynomial ring factory.
     * @param c coefficient.
     */
    public GenPolynomial(GenPolynomialRing<C> r, C c) {
        this(r, c, r.evzero);
    }


    /**
     * Constructor for GenPolynomial x<sup>e</sup>.
     *
     * @param r polynomial ring factory.
     * @param e exponent.
     */
    public GenPolynomial(GenPolynomialRing<C> r, ExpVector e) {
        this(r, r.coFac.getONE(), e);
    }


    /**
     * Constructor for GenPolynomial.
     *
     * @param r polynomial ring factory.
     * @param v the SortedMap of some other polynomial.
     */
    protected GenPolynomial(GenPolynomialRing<C> r, SortedMap<ExpVector, C> v) {
        this(r);
        val.putAll(v); // assume no zero coefficients
    }


    /**
     * Get the corresponding element factory.
     *
     * @return factory for this Element.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Element#factory()
     */
    public GenPolynomialRing<C> factory() {
        return ring;
    }


    /**
     * Copy this GenPolynomial.
     *
     * @return copy of this.
     */
    public GenPolynomial<C> copy() {
        return new GenPolynomial<>(ring, this.val);
    }


    /**
     * Length of GenPolynomial.
     *
     * @return number of coefficients of this GenPolynomial.
     */
    public int length() {
        return val.size();
    }


    /**
     * ExpVector to coefficient map of GenPolynomial.
     *
     * @return val as unmodifiable SortedMap.
     */
    public SortedMap<ExpVector, C> getMap() {
        // return val;
        return Collections.unmodifiableSortedMap(val);
    }


    /**
     * Put an ExpVector to coefficient entry into the internal map of this
     * GenPolynomial. <b>Note:</b> Do not use this method unless you are
     * constructing a new polynomial. this is modified and breaks the
     * immutability promise of this class.
     *
     * @param c coefficient.
     * @param e exponent.
     */
    public void doPutToMap(ExpVector e, C c) {
        if (!c.isZERO()) {
            val.put(e, c);
        }
    }

    /**
     * String representation of GenPolynomial.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (ring.vars != null) {
            return toString(ring.vars);
        }
        StringBuilder s = new StringBuilder();
        s.append(this.getClass().getSimpleName()).append(":");
        s.append(ring.coFac.getClass().getSimpleName());
        if (ring.coFac.characteristic().signum() != 0) {
            s.append("(").append(ring.coFac.characteristic()).append(")");
        }
        s.append("[ ");
        boolean first = true;
        for (Map.Entry<ExpVector, C> m : val.entrySet()) {
            if (first) {
                first = false;
            } else {
                s.append(", ");
            }
            s.append(m.getValue().toString());
            s.append(" ");
            s.append(m.getKey().toString());
        }
        s.append(" ] "); // no not use: ring.toString() );
        return s.toString();
    }


    /**
     * String representation of GenPolynomial.
     *
     * @param v names for variables.
     * @see java.lang.Object#toString()
     */
    public String toString(String[] v) {
        StringBuilder s = new StringBuilder();
        if (val.size() == 0) {
            s.append("0");
        } else {
            // s.append( "( " );
            boolean first = true;
            for (Map.Entry<ExpVector, C> m : val.entrySet()) {
                C c = m.getValue();
                if (first) {
                    first = false;
                } else {
                    if (c.signum() < 0) {
                        s.append(" - ");
                        c = c.negate();
                    } else {
                        s.append(" + ");
                    }
                }
                ExpVector e = m.getKey();
                if (!c.isONE() || e.isZERO()) {
                    String cs = c.toString();
                    if (c instanceof GenPolynomial || c instanceof AlgebraicNumber) {
                        s.append("( ");
                        s.append(cs);
                        s.append(" )");
                    } else {
                        s.append(cs);
                    }
                    s.append(" ");
                }
                if (e != null && v != null) {
                    s.append(e.toString(v));
                } else {
                    s.append(e);
                }
            }
            //s.append(" )");
        }
        return s.toString();
    }

    /**
     * Is GenPolynomial&lt;C&gt; zero.
     *
     * @return If this is 0 then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isZERO()
     */
    public boolean isZERO() {
        return (val.size() == 0);
    }


    /**
     * Is GenPolynomial&lt;C&gt; one.
     *
     * @return If this is 1 then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isONE()
     */
    public boolean isONE() {
        if (val.size() != 1) {
            return false;
        }
        C c = val.get(ring.evzero);
        if (c == null) {
            return false;
        }
        return c.isONE();
    }


    /**
     * Is GenPolynomial&lt;C&gt; a unit.
     *
     * @return If this is a unit then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isUnit()
     */
    public boolean isUnit() {
        if (val.size() != 1) {
            return false;
        }
        C c = val.get(ring.evzero);
        if (c == null) {
            return false;
        }
        return c.isUnit();
    }


    /**
     * Is GenPolynomial&lt;C&gt; a constant.
     *
     * @return If this is a constant polynomial then true is returned, else
     *         false.
     */
    public boolean isConstant() {
        if (val.size() != 1) {
            return false;
        }
        C c = val.get(ring.evzero);
        return c != null;
    }

    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object B) {
        if (!(B instanceof GenPolynomial)) {
            return false;
        }
        GenPolynomial<C> a = null;
        try {
            a = (GenPolynomial<C>) B;
        } catch (ClassCastException ignored) {
        }
        if (a == null) {
            return false;
        }
        return this.compareTo(a) == 0;
    }


    /**
     * Hash code for this polynomial.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int h;
        h = (ring.hashCode() << 27);
        h += val.hashCode();
        return h;
    }


    /**
     * GenPolynomial comparison.
     *
     * @param b GenPolynomial.
     * @return sign(this-b).
     */
    public int compareTo(GenPolynomial<C> b) {
        if (b == null) {
            return 1;
        }
        SortedMap<ExpVector, C> av = this.val;
        SortedMap<ExpVector, C> bv = b.val;
        Iterator<Map.Entry<ExpVector, C>> ai = av.entrySet().iterator();
        Iterator<Map.Entry<ExpVector, C>> bi = bv.entrySet().iterator();
        int s;
        int c = 0;
        while (ai.hasNext() && bi.hasNext()) {
            Map.Entry<ExpVector, C> aie = ai.next();
            Map.Entry<ExpVector, C> bie = bi.next();
            ExpVector ae = aie.getKey();
            ExpVector be = bie.getKey();
            s = ae.compareTo(be);
            if (s != 0) {
                return s;
            }
            if (c == 0) {
                C ac = aie.getValue(); //av.get(ae);
                C bc = bie.getValue(); //bv.get(be);
                c = ac.compareTo(bc);
            }
        }
        if (ai.hasNext()) {
            return 1;
        }
        if (bi.hasNext()) {
            return -1;
        }
        // now all keys are equal
        return c;
    }


    /**
     * GenPolynomial signum.
     *
     * @return sign(ldcf(this)).
     */
    public int signum() {
        if (this.isZERO()) {
            return 0;
        }
        ExpVector t = val.firstKey();
        C c = val.get(t);
        return c.signum();
    }


    /**
     * Number of variables.
     *
     * @return ring.nvar.
     */
    public int numberOfVariables() {
        return ring.nvar;
    }


    /**
     * Leading monomial.
     *
     * @return first map entry.
     */
    public Map.Entry<ExpVector, C> leadingMonomial() {
        if (val.size() == 0)
            return null;
        Iterator<Map.Entry<ExpVector, C>> ai = val.entrySet().iterator();
        return ai.next();
    }


    /**
     * Leading exponent vector.
     *
     * @return first exponent.
     */
    public ExpVector leadingExpVector() {
        if (val.size() == 0) {
            return null; // ring.evzero? needs many changes 
        }
        return val.firstKey();
    }


    /**
     * Trailing exponent vector.
     *
     * @return last exponent.
     */
    public ExpVector trailingExpVector() {
        if (val.size() == 0) {
            return ring.evzero; // or null ?;
        }
        return val.lastKey();
    }


    /**
     * Leading base coefficient.
     *
     * @return first coefficient.
     */
    public C leadingBaseCoefficient() {
        if (val.size() == 0) {
            return ring.coFac.getZERO();
        }
        return val.get(val.firstKey());
    }


    /**
     * Trailing base coefficient.
     *
     * @return coefficient of constant term.
     */
    public C trailingBaseCoefficient() {
        C c = val.get(ring.evzero);
        if (c == null) {
            return ring.coFac.getZERO();
        }
        return c;
    }


    /**
     * Coefficient.
     *
     * @param e exponent.
     * @return coefficient for given exponent.
     */
    public C coefficient(ExpVector e) {
        C c = val.get(e);
        if (c == null) {
            c = ring.coFac.getZERO();
        }
        return c;
    }

    /**
     * Degree in variable i.
     *
     * @return maximal degree in the variable i.
     */
    public long degree(int i) {
        if (val.size() == 0) {
            return 0; // 0 or -1 ?;
        }
        int j;
        if (i >= 0) {
            j = ring.nvar - 1 - i;
        } else { // python like -1 means main variable
            j = ring.nvar + i;
        }
        long deg = 0;
        for (ExpVector e : val.keySet()) {
            long d = e.getVal(j);
            if (d > deg) {
                deg = d;
            }
        }
        return deg;
    }


    /**
     * Maximal degree.
     *
     * @return maximal degree in any variables.
     */
    public long degree() {
        if (val.size() == 0) {
            return 0; // 0 or -1 ?;
        }
        long deg = 0;
        for (ExpVector e : val.keySet()) {
            long d = e.maxDeg();
            if (d > deg) {
                deg = d;
            }
        }
        return deg;
    }


    /**
     * Maximal degree vector.
     *
     * @return maximal degree vector of all variables.
     */
    public ExpVector degreeVector() {
        ExpVector deg = ring.evzero;
        if (val.size() == 0) {
            return deg;
        }
        for (ExpVector e : val.keySet()) {
            deg = deg.lcm(e);
        }
        return deg;
    }


    /**
     * GenPolynomial maximum norm.
     *
     * @return ||this||.
     */
    public C maxNorm() {
        C n = ring.getZEROCoefficient();
        for (C c : val.values()) {
            C x = c.abs();
            if (n.compareTo(x) < 0) {
                n = x;
            }
        }
        return n;
    }


    /**
     * GenPolynomial sum norm.
     *
     * @return sum of all absolute values of coefficients.
     */
    public C sumNorm() {
        C n = ring.getZEROCoefficient();
        for (C c : val.values()) {
            C x = c.abs();
            n = n.sum(x);
        }
        return n;
    }


    /**
     * GenPolynomial summation.
     *
     * @param S GenPolynomial.
     * @return this+S.
     */
    //public <T extends GenPolynomial<C>> T sum(T /*GenPolynomial<C>*/ S) {
    public GenPolynomial<C> sum(GenPolynomial<C> S) {
        if (S == null) {
            return this;
        }
        if (S.isZERO()) {
            return this;
        }
        if (this.isZERO()) {
            return S;
        }
        assert (ring.nvar == S.ring.nvar);
        GenPolynomial<C> n = this.copy(); //new GenPolynomial<C>(ring, val); 
        SortedMap<ExpVector, C> nv = n.val;
        SortedMap<ExpVector, C> sv = S.val;
        for (Map.Entry<ExpVector, C> me : sv.entrySet()) {
            ExpVector e = me.getKey();
            C y = me.getValue(); //sv.get(e); // assert y != null
            C x = nv.get(e);
            if (x != null) {
                x = x.sum(y);
                if (!x.isZERO()) {
                    nv.put(e, x);
                } else {
                    nv.remove(e);
                }
            } else {
                nv.put(e, y);
            }
        }
        return n;
    }


    /**
     * GenPolynomial addition. This method is not very efficient, since this is
     * copied.
     *
     * @param a coefficient.
     * @param e exponent.
     * @return this + a x<sup>e</sup>.
     */
    public GenPolynomial<C> sum(C a, ExpVector e) {
        if (a == null) {
            return this;
        }
        if (a.isZERO()) {
            return this;
        }
        GenPolynomial<C> n = this.copy(); //new GenPolynomial<C>(ring, val); 
        SortedMap<ExpVector, C> nv = n.val;
        //if ( nv.size() == 0 ) { nv.put(e,a); return n; }
        C x = nv.get(e);
        if (x != null) {
            x = x.sum(a);
            if (!x.isZERO()) {
                nv.put(e, x);
            } else {
                nv.remove(e);
            }
        } else {
            nv.put(e, a);
        }
        return n;
    }


    /**
     * GenPolynomial addition. This method is not very efficient, since this is
     * copied.
     *
     * @param a coefficient.
     * @return this + a x<sup>0</sup>.
     */
    public GenPolynomial<C> sum(C a) {
        return sum(a, ring.evzero);
    }


    /**
     * GenPolynomial subtraction.
     *
     * @param S GenPolynomial.
     * @return this-S.
     */
    public GenPolynomial<C> subtract(GenPolynomial<C> S) {
        if (S == null) {
            return this;
        }
        if (S.isZERO()) {
            return this;
        }
        if (this.isZERO()) {
            return S.negate();
        }
        assert (ring.nvar == S.ring.nvar);
        GenPolynomial<C> n = this.copy(); //new GenPolynomial<C>(ring, val); 
        SortedMap<ExpVector, C> nv = n.val;
        SortedMap<ExpVector, C> sv = S.val;
        for (Map.Entry<ExpVector, C> me : sv.entrySet()) {
            ExpVector e = me.getKey();
            C y = me.getValue(); //sv.get(e); // assert y != null
            C x = nv.get(e);
            if (x != null) {
                x = x.subtract(y);
                if (!x.isZERO()) {
                    nv.put(e, x);
                } else {
                    nv.remove(e);
                }
            } else {
                nv.put(e, y.negate());
            }
        }
        return n;
    }


    /**
     * GenPolynomial subtraction. This method is not very efficient, since this
     * is copied.
     *
     * @param a coefficient.
     * @param e exponent.
     * @return this - a x<sup>e</sup>.
     */
    public GenPolynomial<C> subtract(C a, ExpVector e) {
        if (a == null) {
            return this;
        }
        if (a.isZERO()) {
            return this;
        }
        GenPolynomial<C> n = this.copy(); //new GenPolynomial<C>(ring, val); 
        SortedMap<ExpVector, C> nv = n.val;
        C x = nv.get(e);
        if (x != null) {
            x = x.subtract(a);
            if (!x.isZERO()) {
                nv.put(e, x);
            } else {
                nv.remove(e);
            }
        } else {
            nv.put(e, a.negate());
        }
        return n;
    }


    /**
     * GenPolynomial subtract. This method is not very efficient, since this is
     * copied.
     *
     * @param a coefficient.
     * @return this + a x<sup>0</sup>.
     */
    public GenPolynomial<C> subtract(C a) {
        return subtract(a, ring.evzero);
    }


    /**
     * GenPolynomial negation.
     *
     * @return -this.
     */
    public GenPolynomial<C> negate() {
        GenPolynomial<C> n = ring.getZERO().copy();
        //new GenPolynomial<C>(ring, ring.getZERO().val);
        SortedMap<ExpVector, C> v = n.val;
        for (Map.Entry<ExpVector, C> m : val.entrySet()) {
            C x = m.getValue(); // != null, 0
            v.put(m.getKey(), x.negate());
            // or m.setValue( x.negate() ) if this cloned 
        }
        return n;
    }


    /**
     * GenPolynomial absolute value, i.e. leadingCoefficient &gt; 0.
     *
     * @return abs(this).
     */
    public GenPolynomial<C> abs() {
        if (leadingBaseCoefficient().signum() < 0) {
            return this.negate();
        }
        return this;
    }


    /**
     * GenPolynomial multiplication.
     *
     * @param S GenPolynomial.
     * @return this*S.
     */
    public GenPolynomial<C> multiply(GenPolynomial<C> S) {
        if (S == null) {
            return ring.getZERO();
        }
        if (S.isZERO()) {
            return ring.getZERO();
        }
        if (this.isZERO()) {
            return this;
        }
        assert (ring.nvar == S.ring.nvar);
        if (this instanceof GenSolvablePolynomial || S instanceof GenSolvablePolynomial) {
            //throw new RuntimeException("wrong method dispatch in JRE ");
            GenSolvablePolynomial<C> T = (GenSolvablePolynomial<C>) this;
            GenSolvablePolynomial<C> Sp = (GenSolvablePolynomial<C>) S;
            return T.multiply(Sp);
        }
        GenPolynomial<C> p = ring.getZERO().copy();
        SortedMap<ExpVector, C> pv = p.val;
        for (Map.Entry<ExpVector, C> m1 : val.entrySet()) {
            C c1 = m1.getValue();
            ExpVector e1 = m1.getKey();
            for (Map.Entry<ExpVector, C> m2 : S.val.entrySet()) {
                C c2 = m2.getValue();
                ExpVector e2 = m2.getKey();
                C c = c1.multiply(c2); // check non zero if not domain
                if (!c.isZERO()) {
                    ExpVector e = e1.sum(e2);
                    C c0 = pv.get(e);
                    if (c0 == null) {
                        pv.put(e, c);
                    } else {
                        c0 = c0.sum(c);
                        if (!c0.isZERO()) {
                            pv.put(e, c0);
                        } else {
                            pv.remove(e);
                        }
                    }
                }
            }
        }
        return p;
    }


    /**
     * GenPolynomial multiplication. Product with coefficient ring element.
     *
     * @param s coefficient.
     * @return this*s.
     */
    public GenPolynomial<C> multiply(C s) {
        if (s == null) {
            return ring.getZERO();
        }
        if (s.isZERO()) {
            return ring.getZERO();
        }
        if (this.isZERO()) {
            return this;
        }
        GenPolynomial<C> p = ring.getZERO().copy();
        SortedMap<ExpVector, C> pv = p.val;
        for (Map.Entry<ExpVector, C> m1 : val.entrySet()) {
            C c1 = m1.getValue();
            ExpVector e1 = m1.getKey();
            C c = c1.multiply(s); // check non zero if not domain
            if (!c.isZERO()) {
                pv.put(e1, c); // or m1.setValue( c )
            }
        }
        return p;
    }


    /**
     * GenPolynomial monic, i.e. leadingCoefficient == 1. If leadingCoefficient
     * is not invertible returns this unmodified.
     *
     * @return monic(this).
     */
    public GenPolynomial<C> monic() {
        if (this.isZERO()) {
            return this;
        }
        C lc = leadingBaseCoefficient();
        if (!lc.isUnit()) {
            return this;
        }
        C lm = lc.inverse();
        return multiply(lm);
    }


    /**
     * GenPolynomial multiplication. Product with ring element and exponent
     * vector.
     *
     * @param s coefficient.
     * @param e exponent.
     * @return this * s x<sup>e</sup>.
     */
    public GenPolynomial<C> multiply(C s, ExpVector e) {
        if (s == null) {
            return ring.getZERO();
        }
        if (s.isZERO()) {
            return ring.getZERO();
        }
        if (this.isZERO()) {
            return this;
        }
        if (this instanceof GenSolvablePolynomial) {
            //throw new RuntimeException("wrong method dispatch in JRE ");
            GenSolvablePolynomial<C> T = (GenSolvablePolynomial<C>) this;
            return T.multiply(s, e);
        }
        GenPolynomial<C> p = ring.getZERO().copy();
        SortedMap<ExpVector, C> pv = p.val;
        for (Map.Entry<ExpVector, C> m1 : val.entrySet()) {
            C c1 = m1.getValue();
            ExpVector e1 = m1.getKey();
            C c = c1.multiply(s); // check non zero if not domain
            if (!c.isZERO()) {
                ExpVector e2 = e1.sum(e);
                pv.put(e2, c);
            }
        }
        return p;
    }


    /**
     * GenPolynomial multiplication. Product with exponent vector.
     *
     * @param e exponent (!= null).
     * @return this * x<sup>e</sup>.
     */
    public GenPolynomial<C> multiply(ExpVector e) {
        // assert e != null. This is never allowed.
        if (this.isZERO()) {
            return this;
        }
        if (this instanceof GenSolvablePolynomial) {
            //throw new RuntimeException("wrong method dispatch in JRE ");
            GenSolvablePolynomial<C> T = (GenSolvablePolynomial<C>) this;
            return T.multiply(e);
        }
        GenPolynomial<C> p = ring.getZERO().copy();
        SortedMap<ExpVector, C> pv = p.val;
        for (Map.Entry<ExpVector, C> m1 : val.entrySet()) {
            C c1 = m1.getValue();
            ExpVector e1 = m1.getKey();
            ExpVector e2 = e1.sum(e);
            pv.put(e2, c1);
        }
        return p;
    }


    /**
     * GenPolynomial multiplication. Product with 'monomial'.
     *
     * @param m 'monomial'.
     * @return this * m.
     */
    public GenPolynomial<C> multiply(Map.Entry<ExpVector, C> m) {
        if (m == null) {
            return ring.getZERO();
        }
        return multiply(m.getValue(), m.getKey());
    }


    /**
     * GenPolynomial division. Division by coefficient ring element. Fails, if
     * exact division is not possible.
     *
     * @param s coefficient.
     * @return this/s.
     */
    public GenPolynomial<C> divide(C s) {
        if (s == null || s.isZERO()) {
            throw new ArithmeticException("division by zero");
        }
        if (this.isZERO()) {
            return this;
        }
        //C t = s.inverse();
        //return multiply(t);
        GenPolynomial<C> p = ring.getZERO().copy();
        SortedMap<ExpVector, C> pv = p.val;
        for (Map.Entry<ExpVector, C> m : val.entrySet()) {
            ExpVector e = m.getKey();
            C c1 = m.getValue();
            C c = c1.divide(s);
            if (c.isZERO()) {
                throw new ArithmeticException("no exact division: " + c1 + "/" + s + ", in " + this);
            }
            pv.put(e, c); // or m1.setValue( c )
        }
        return p;
    }


    /**
     * GenPolynomial division with remainder. Fails, if exact division by
     * leading base coefficient is not possible. Meaningful only for univariate
     * polynomials over fields, but works in any case.
     *
     * @param S nonzero GenPolynomial with invertible leading coefficient.
     * @return [ quotient , remainder ] with this = quotient * S + remainder and
     *         deg(remainder) &lt; deg(S) or remiander = 0.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil#baseSparsePseudoRemainder(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial, cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial)
     *      .
     */
    @SuppressWarnings("unchecked")
    public GenPolynomial<C>[] quotientRemainder(GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            throw new ArithmeticException("division by zero");
        }
        C c = S.leadingBaseCoefficient();
        if (!c.isUnit()) {
            throw new ArithmeticException("lbcf not invertible " + c);
        }
        C ci = c.inverse();
        assert (ring.nvar == S.ring.nvar);
        ExpVector e = S.leadingExpVector();
        GenPolynomial<C> h;
        GenPolynomial<C> q = ring.getZERO().copy();
        GenPolynomial<C> r = this.copy();
        while (!r.isZERO()) {
            ExpVector f = r.leadingExpVector();
            if (f.multipleOf(e)) {
                C a = r.leadingBaseCoefficient();
                f = f.subtract(e);
                a = a.multiply(ci);
                q = q.sum(a, f);
                h = S.multiply(a, f);
                r = r.subtract(h);
            } else {
                break;
            }
        }
        GenPolynomial<C>[] ret = new GenPolynomial[2];
        ret[0] = q;
        ret[1] = r;
        return ret;
    }

    /**
     * GenPolynomial division. Fails, if exact division by leading base
     * coefficient is not possible. Meaningful only for univariate polynomials
     * over fields, but works in any case.
     *
     * @param S nonzero GenPolynomial with invertible leading coefficient.
     * @return quotient with this = quotient * S + remainder.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil#baseSparsePseudoRemainder(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial, cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial)
     *      .
     */
    public GenPolynomial<C> divide(GenPolynomial<C> S) {
        return quotientRemainder(S)[0];
    }


    /**
     * GenPolynomial remainder. Fails, if exact division by leading base
     * coefficient is not possible. Meaningful only for univariate polynomials
     * over fields, but works in any case.
     *
     * @param S nonzero GenPolynomial with invertible leading coefficient.
     * @return remainder with this = quotient * S + remainder.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil#baseSparsePseudoRemainder(cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial, cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial)
     *      .
     */
    public GenPolynomial<C> remainder(GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            throw new ArithmeticException("division by zero");
        }
        C c = S.leadingBaseCoefficient();
        if (!c.isUnit()) {
            throw new ArithmeticException("lbc not invertible " + c);
        }
        C ci = c.inverse();
        assert (ring.nvar == S.ring.nvar);
        ExpVector e = S.leadingExpVector();
        GenPolynomial<C> h;
        GenPolynomial<C> r = this.copy();
        while (!r.isZERO()) {
            ExpVector f = r.leadingExpVector();
            if (f.multipleOf(e)) {
                C a = r.leadingBaseCoefficient();
                f = f.subtract(e);
                a = a.multiply(ci);
                h = S.multiply(a, f);
                r = r.subtract(h);
            } else {
                break;
            }
        }
        return r;
    }


    /**
     * GenPolynomial greatest common divisor. Only for univariate polynomials
     * over fields.
     *
     * @param S GenPolynomial.
     * @return gcd(this, S).
     */
    public GenPolynomial<C> gcd(GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            return this;
        }
        if (this.isZERO()) {
            return S;
        }
        if (ring.nvar != 1) {
            throw new IllegalArgumentException("not univariate polynomials" + ring);
        }
        GenPolynomial<C> x;
        GenPolynomial<C> q = this;
        GenPolynomial<C> r = S;
        while (!r.isZERO()) {
            x = q.remainder(r);
            q = r;
            r = x;
        }
        return q.monic(); // normalize
    }


    /**
     * GenPolynomial extended greatest comon divisor. Only for univariate
     * polynomials over fields.
     *
     * @param S GenPolynomial.
     * @return [ gcd(this,S), a, b ] with a*this + b*S = gcd(this,S).
     */
    @SuppressWarnings("unchecked")
    public GenPolynomial<C>[] egcd(GenPolynomial<C> S) {
        GenPolynomial<C>[] ret = new GenPolynomial[3];
        ret[0] = null;
        ret[1] = null;
        ret[2] = null;
        if (S == null || S.isZERO()) {
            ret[0] = this;
            ret[1] = this.ring.getONE();
            ret[2] = this.ring.getZERO();
            return ret;
        }
        if (this.isZERO()) {
            ret[0] = S;
            ret[1] = this.ring.getZERO();
            ret[2] = this.ring.getONE();
            return ret;
        }
        if (ring.nvar != 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " not univariate polynomials"
                    + ring);
        }
        if (this.isConstant() && S.isConstant()) {
            C t = this.leadingBaseCoefficient();
            C s = S.leadingBaseCoefficient();
            C[] gg = t.egcd(s);
            GenPolynomial<C> z = this.ring.getZERO();
            ret[0] = z.sum(gg[0]);
            ret[1] = z.sum(gg[1]);
            ret[2] = z.sum(gg[2]);
            return ret;
        }
        GenPolynomial<C>[] qr;
        GenPolynomial<C> q = this;
        GenPolynomial<C> r = S;
        GenPolynomial<C> c1 = ring.getONE().copy();
        GenPolynomial<C> d1 = ring.getZERO().copy();
        GenPolynomial<C> c2 = ring.getZERO().copy();
        GenPolynomial<C> d2 = ring.getONE().copy();
        GenPolynomial<C> x1;
        GenPolynomial<C> x2;
        while (!r.isZERO()) {
            qr = q.quotientRemainder(r);
            q = qr[0];
            x1 = c1.subtract(q.multiply(d1));
            x2 = c2.subtract(q.multiply(d2));
            c1 = d1;
            c2 = d2;
            d1 = x1;
            d2 = x2;
            q = r;
            r = qr[1];
        }
        // normalize ldcf(q) to 1, i.e. make monic
        C g = q.leadingBaseCoefficient();
        if (g.isUnit()) {
            C h = g.inverse();
            q = q.multiply(h);
            c1 = c1.multiply(h);
            c2 = c2.multiply(h);
        }
        //assert ( ((c1.multiply(this)).sum( c2.multiply(S)).equals(q) )); 
        ret[0] = q;
        ret[1] = c1;
        ret[2] = c2;
        return ret;
    }


    /**
     * GenPolynomial half extended greatest comon divisor. Only for univariate
     * polynomials over fields.
     *
     * @param S GenPolynomial.
     * @return [ gcd(this,S), a ] with a*this + b*S = gcd(this,S).
     */
    @SuppressWarnings("unchecked")
    public GenPolynomial<C>[] hegcd(GenPolynomial<C> S) {
        GenPolynomial<C>[] ret = new GenPolynomial[2];
        ret[0] = null;
        ret[1] = null;
        if (S == null || S.isZERO()) {
            ret[0] = this;
            ret[1] = this.ring.getONE();
            return ret;
        }
        if (this.isZERO()) {
            ret[0] = S;
            return ret;
        }
        if (ring.nvar != 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " not univariate polynomials"
                    + ring);
        }
        GenPolynomial<C>[] qr;
        GenPolynomial<C> q = this;
        GenPolynomial<C> r = S;
        GenPolynomial<C> c1 = ring.getONE().copy();
        GenPolynomial<C> d1 = ring.getZERO().copy();
        GenPolynomial<C> x1;
        while (!r.isZERO()) {
            qr = q.quotientRemainder(r);
            q = qr[0];
            x1 = c1.subtract(q.multiply(d1));
            c1 = d1;
            d1 = x1;
            q = r;
            r = qr[1];
        }
        // normalize ldcf(q) to 1, i.e. make monic
        C g = q.leadingBaseCoefficient();
        if (g.isUnit()) {
            C h = g.inverse();
            q = q.multiply(h);
            c1 = c1.multiply(h);
        }
        //assert ( ((c1.multiply(this)).remainder(S).equals(q) )); 
        ret[0] = q;
        ret[1] = c1;
        return ret;
    }


    /**
     * GenPolynomial inverse. Required by RingElem. Throws not invertible
     * exception.
     */
    public GenPolynomial<C> inverse() {
        if (isUnit()) { // only possible if ldbcf is unit
            C c = leadingBaseCoefficient().inverse();
            return ring.getONE().multiply(c);
        }
        throw new NotInvertibleException("element not invertible " + this + " :: " + ring);
    }


    /**
     * GenPolynomial modular inverse. Only for univariate polynomials over
     * fields.
     *
     * @param m GenPolynomial.
     * @return a with with a*this = 1 mod m.
     */
    public GenPolynomial<C> modInverse(GenPolynomial<C> m) {
        if (this.isZERO()) {
            throw new NotInvertibleException("zero is not invertible");
        }
        GenPolynomial<C>[] hegcd = this.hegcd(m);
        GenPolynomial<C> a = hegcd[0];
        if (!a.isUnit()) { // gcd != 1
            throw new AlgebraicNotInvertibleException("element not invertible, gcd != 1", m, a, m.divide(a));
        }
        GenPolynomial<C> b = hegcd[1];
        if (b.isZERO()) { // when m divides this, e.g. m.isUnit()
            throw new NotInvertibleException("element not invertible, divisible by modul");
        }
        return b;
    }


    /**
     * Extend variables. Used e.g. in module embedding. Extend all ExpVectors by
     * i elements and multiply by x_j^k.
     *
     * @param pfac extended polynomial ring factory (by i variables).
     * @param j    index of variable to be used for multiplication.
     * @param k    exponent for x_j.
     * @return extended polynomial.
     */
    public GenPolynomial<C> extend(GenPolynomialRing<C> pfac, int j, long k) {
        if (ring.equals(pfac)) { // nothing to do
            return this;
        }
        GenPolynomial<C> Cp = pfac.getZERO().copy();
        if (this.isZERO()) {
            return Cp;
        }
        int i = pfac.nvar - ring.nvar;
        Map<ExpVector, C> C = Cp.val; //getMap();
        Map<ExpVector, C> A = val;
        for (Map.Entry<ExpVector, C> y : A.entrySet()) {
            ExpVector e = y.getKey();
            C a = y.getValue();
            ExpVector f = e.extend(i, j, k);
            C.put(f, a);
        }
        return Cp;
    }


    /**
     * Extend lower variables. Used e.g. in module embedding. Extend all
     * ExpVectors by i lower elements and multiply by x_j^k.
     *
     * @param pfac extended polynomial ring factory (by i variables).
     * @param j    index of variable to be used for multiplication.
     * @param k    exponent for x_j.
     * @return extended polynomial.
     */
    public GenPolynomial<C> extendLower(GenPolynomialRing<C> pfac, int j, long k) {
        if (ring.equals(pfac)) { // nothing to do
            return this;
        }
        GenPolynomial<C> Cp = pfac.getZERO().copy();
        if (this.isZERO()) {
            return Cp;
        }
        int i = pfac.nvar - ring.nvar;
        Map<ExpVector, C> C = Cp.val; //getMap();
        Map<ExpVector, C> A = val;
        for (Map.Entry<ExpVector, C> y : A.entrySet()) {
            ExpVector e = y.getKey();
            C a = y.getValue();
            ExpVector f = e.extendLower(i, j, k);
            C.put(f, a);
        }
        return Cp;
    }


    /**
     * Contract variables. Used e.g. in module embedding. Remove i elements of
     * each ExpVector.
     *
     * @param pfac contracted polynomial ring factory (by i variables).
     * @return Map of exponents and contracted polynomials. <b>Note:</b> could
     *         return SortedMap
     */
    public Map<ExpVector, GenPolynomial<C>> contract(GenPolynomialRing<C> pfac) {
        GenPolynomial<C> zero = pfac.getZERO();
        TermOrder t = new TermOrder(TermOrder.INVLEX);
        Map<ExpVector, GenPolynomial<C>> B = new TreeMap<>(t.getAscendComparator());
        if (this.isZERO()) {
            return B;
        }
        int i = ring.nvar - pfac.nvar;
        Map<ExpVector, C> A = val;
        for (Map.Entry<ExpVector, C> y : A.entrySet()) {
            ExpVector e = y.getKey();
            C a = y.getValue();
            ExpVector f = e.contract(0, i);
            ExpVector g = e.contract(i, e.length() - i);
            GenPolynomial<C> p = B.get(f);
            if (p == null) {
                p = zero;
            }
            p = p.sum(a, g);
            B.put(f, p);
        }
        return B;
    }

    /**
     * Reverse variables. Used e.g. in opposite rings.
     *
     * @return polynomial with reversed variables.
     */
    public GenPolynomial<C> reverse(GenPolynomialRing<C> oring) {
        GenPolynomial<C> Cp = oring.getZERO().copy();
        if (this.isZERO()) {
            return Cp;
        }
        int k = -1;
        if (oring.tord.getEvord2() != 0 && oring.partial) {
            k = oring.tord.getSplit();
        }

        Map<ExpVector, C> C = Cp.val; //getMap();
        Map<ExpVector, C> A = val;
        ExpVector f;
        for (Map.Entry<ExpVector, C> y : A.entrySet()) {
            ExpVector e = y.getKey();
            if (k >= 0) {
                f = e.reverse(k);
            } else {
                f = e.reverse();
            }
            C a = y.getValue();
            C.put(f, a);
        }
        return Cp;
    }

    /**
     * Iterator over monomials.
     *
     * @return a PolyIterator.
     */
    public Iterator<Monomial<C>> iterator() {
        return new PolyIterator<>(val);
    }

}
