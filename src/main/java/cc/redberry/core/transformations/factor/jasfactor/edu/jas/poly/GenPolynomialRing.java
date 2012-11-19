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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.ModIntegerRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.util.CartesianProduct;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.util.CartesianProductInfinite;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.util.LongIterable;

import java.math.BigInteger;
import java.util.*;


/**
 * GenPolynomialRing generic polynomial factory implementing RingFactory;
 * Factory for n-variate ordered polynomials over C. Almost immutable object,
 * except variable names.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public class GenPolynomialRing<C extends RingElem<C>> implements RingFactory<GenPolynomial<C>>,
        Iterable<GenPolynomial<C>> {


    /**
     * The factory for the coefficients.
     */
    public final RingFactory<C> coFac;


    /**
     * The number of variables.
     */
    public final int nvar;


    /**
     * The term order.
     */
    public final TermOrder tord;


    /**
     * True for partially reversed variables.
     */
    protected boolean partial;


    /**
     * The names of the variables. This value can be modified.
     */
    protected String[] vars;


    /**
     * The names of all known variables.
     */
    private static Set<String> knownVars = new HashSet<>();


    /**
     * The constant polynomial 0 for this ring.
     */
    public final GenPolynomial<C> ZERO;


    /**
     * The constant polynomial 1 for this ring.
     */
    public final GenPolynomial<C> ONE;


    /**
     * The constant exponent vector 0 for this ring.
     */
    public final ExpVector evzero;


    /**
     * A default random sequence generator.
     */
    protected final static Random random = new Random();


    /**
     * Indicator if this ring is a field.
     */
    protected int isField = -1; // initially unknown


    /**
     * /**
     * The constructor creates a polynomial factory object with the default term
     * order.
     *
     * @param cf factory for coefficients of type C.
     * @param n  number of variables.
     */
    public GenPolynomialRing(RingFactory<C> cf, int n) {
        this(cf, n, new TermOrder(), null);
    }


    /**
     * The constructor creates a polynomial factory object.
     *
     * @param cf factory for coefficients of type C.
     * @param n  number of variables.
     * @param t  a term order.
     */
    public GenPolynomialRing(RingFactory<C> cf, int n, TermOrder t) {
        this(cf, n, t, null);
    }


    /**
     * The constructor creates a polynomial factory object.
     *
     * @param cf factory for coefficients of type C.
     * @param v  names for the variables.
     */
    public GenPolynomialRing(RingFactory<C> cf, String[] v) {
        this(cf, v.length, v);
    }


    /**
     * The constructor creates a polynomial factory object.
     *
     * @param cf factory for coefficients of type C.
     * @param n  number of variables.
     * @param v  names for the variables.
     */
    public GenPolynomialRing(RingFactory<C> cf, int n, String[] v) {
        this(cf, n, new TermOrder(), v);
    }


    /**
     * The constructor creates a polynomial factory object.
     *
     * @param cf factory for coefficients of type C.
     * @param t  a term order.
     * @param v  names for the variables.
     */
    public GenPolynomialRing(RingFactory<C> cf, TermOrder t, String[] v) {
        this(cf, v.length, t, v);
    }


    /**
     * The constructor creates a polynomial factory object.
     *
     * @param cf factory for coefficients of type C.
     * @param v  names for the variables.
     * @param t  a term order.
     */
    public GenPolynomialRing(RingFactory<C> cf, String[] v, TermOrder t) {
        this(cf, v.length, t, v);
    }


    /**
     * The constructor creates a polynomial factory object.
     *
     * @param cf factory for coefficients of type C.
     * @param n  number of variables.
     * @param t  a term order.
     * @param v  names for the variables.
     */
    public GenPolynomialRing(RingFactory<C> cf, int n, TermOrder t, String[] v) {
        coFac = cf;
        nvar = n;
        tord = t;
        partial = false;
        vars = v;
        ZERO = new GenPolynomial<>(this);
        C coeff = coFac.getONE();
        evzero = ExpVector.create(nvar);
        ONE = new GenPolynomial<>(this, coeff, evzero);
        if (vars == null) {
            vars = newVars("x", nvar);
        } else {
            if (vars.length != nvar) {
                throw new IllegalArgumentException("incompatible variable size " + vars.length + ", " + nvar);
            }
            addVars(vars);
        }
    }


    /**
     * The constructor creates a polynomial factory object with the the same
     * term order, number of variables and variable names as the given
     * polynomial factory, only the coefficient factories differ.
     *
     * @param cf factory for coefficients of type C.
     * @param o  other polynomial ring.
     */
    public GenPolynomialRing(RingFactory<C> cf, GenPolynomialRing o) {
        this(cf, o.nvar, o.tord, o.vars);
    }


    /**
     * The constructor creates a polynomial factory object with the the same
     * coefficient factory, number of variables and variable names as the given
     * polynomial factory, only the term order differs.
     *
     * @param to term order.
     * @param o  other polynomial ring.
     */
    public GenPolynomialRing(GenPolynomialRing<C> o, TermOrder to) {
        this(o.coFac, o.nvar, to, o.vars);
    }


    /**
     * Copy this factory.
     *
     * @return a clone of this.
     */
    public GenPolynomialRing<C> copy() {
        return new GenPolynomialRing<>(coFac, this);
    }


    /**
     * Get the String representation.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String res = null;
        if (coFac != null) {
            String scf = coFac.getClass().getSimpleName();
            if (coFac instanceof AlgebraicNumberRing) {
                AlgebraicNumberRing an = (AlgebraicNumberRing) coFac;
                //String[] v = an.ring.vars;
                res = "AN[ (" + an.ring.varsToString() + ") (" + an.toString() + ") ]";
            }
            if (coFac instanceof GenPolynomialRing) {
                GenPolynomialRing rf = (GenPolynomialRing) coFac;
                //String[] v = rf.vars;
                //RingFactory cf = rf.coFac;
                //String cs;
                //if (cf instanceof ModIntegerRing) {
                //    cs = cf.toString();
                //} else {
                //    cs = " " + cf.getClass().getSimpleName();
                //}
                //res = "IntFunc" + "{" + cs + "( " + rf.varsToString() + " )" + " } ";
                res = "IntFunc" + "( " + rf.toString() + " )";
            }
            if (coFac instanceof ModIntegerRing) {
                ModIntegerRing mn = (ModIntegerRing) coFac;
                res = "Mod " + mn.getModul() + " ";
            }
            if (res == null) {
                res = coFac.toString();
                if (res.matches("[0-9].*")) {
                    res = scf;
                }
            }
            res += "( " + varsToString() + " ) " + tord.toString() + " ";
        } else {
            res = this.getClass().getSimpleName() + "[ " + coFac.toString() + " ";
            //  + coFac.getClass().getSimpleName();
            if (coFac instanceof AlgebraicNumberRing) {
                AlgebraicNumberRing an = (AlgebraicNumberRing) coFac;
                res = "AN[ (" + an.ring.varsToString() + ") (" + an.modul + ") ]";
            }
            if (coFac instanceof GenPolynomialRing) {
                GenPolynomialRing rf = (GenPolynomialRing) coFac;
                //String[] v = rf.vars;
                //RingFactory cf = rf.coFac;
                //String cs;
                //if (cf instanceof ModIntegerRing) {
                //    cs = cf.toString();
                //} else {
                //    cs = " " + cf.getClass().getSimpleName();
                //}
                //res = "IntFunc{ " + cs + "( " + rf.varsToString() + " )" + " } ";
                res = "IntFunc" + "( " + rf.toString() + " )";
            }
            if (coFac instanceof ModIntegerRing) {
                ModIntegerRing mn = (ModIntegerRing) coFac;
                res = "Mod " + mn.getModul() + " ";
            }
            //res += ", " + nvar + ", " + tord.toString() + ", " + varsToString() + ", " + partial + " ]";
            res += "( " + varsToString() + " ) " + tord.toString() + " ]";
        }
        return res;
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object other) {
        if (!(other instanceof GenPolynomialRing)) {
            return false;
        }
        GenPolynomialRing<C> oring = null;
        try {
            oring = (GenPolynomialRing<C>) other;
        } catch (ClassCastException ignored) {
        }
        if (oring == null) {
            return false;
        }
        if (nvar != oring.nvar) {
            return false;
        }
        if (!coFac.equals(oring.coFac)) {
            return false;
        }
        if (!tord.equals(oring.tord)) {
            return false;
        }
        // same variables required ?
        return Arrays.equals(vars, oring.vars);
    }


    /**
     * Hash code for this polynomial ring.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int h;
        h = (nvar << 27);
        h += (coFac.hashCode() << 11);
        h += tord.hashCode();
        return h;
    }


    /**
     * Get the variable names.
     *
     * @return vars.
     */
    public String[] getVars() {
        return vars; // Java-5: Arrays.copyOf(vars,vars.length);
    }


    /**
     * Set the variable names.
     *
     * @return old vars.
     */
    public String[] setVars(String[] v) {
        if (v.length != nvar) {
            throw new IllegalArgumentException("v not matching number of variables: " + Arrays.toString(v)
                    + ", nvar " + nvar);
        }
        String[] t = vars;
        vars = v; // Java-5: Arrays.copyOf(v,v.length);
        return t;
    }


    /**
     * Get a String representation of the variable names.
     *
     * @return names seperated by commas.
     */
    public String varsToString() {
        if (vars == null) {
            return "#" + nvar;
        }
        //return Arrays.toString(vars);
        return ExpVector.varsToString(vars);
    }


    /**
     * Get the zero element from the coefficients.
     *
     * @return 0 as C.
     */
    public C getZEROCoefficient() {
        return coFac.getZERO();
    }


    /**
     * Get the one element from the coefficients.
     *
     * @return 1 as C.
     */
    public C getONECoefficient() {
        return coFac.getONE();
    }


    /**
     * Get the zero element.
     *
     * @return 0 as GenPolynomial<C>.
     */
    public GenPolynomial<C> getZERO() {
        return ZERO;
    }


    /**
     * Get the one element.
     *
     * @return 1 as GenPolynomial<C>.
     */
    public GenPolynomial<C> getONE() {
        return ONE;
    }


    /**
     * Query if this ring is commutative.
     *
     * @return true if this ring is commutative, else false.
     */
    public boolean isCommutative() {
        return coFac.isCommutative();
    }


    /**
     * Query if this ring is associative.
     *
     * @return true if this ring is associative, else false.
     */
    public boolean isAssociative() {
        return coFac.isAssociative();
    }


    /**
     * Query if this ring is a field.
     *
     * @return false.
     */
    public boolean isField() {
        if (isField > 0) {
            return true;
        }
        if (isField == 0) {
            return false;
        }
        if (coFac.isField() && nvar == 0) {
            isField = 1;
            return true;
        }
        isField = 0;
        return false;
    }


    /**
     * Characteristic of this ring.
     *
     * @return characteristic of this ring.
     */
    public java.math.BigInteger characteristic() {
        return coFac.characteristic();
    }


    /**
     * Get a (constant) GenPolynomial&lt;C&gt; element from a coefficient value.
     *
     * @param a coefficient.
     * @return a GenPolynomial&lt;C&gt;.
     */
    public GenPolynomial<C> valueOf(C a) {
        return new GenPolynomial<>(this, a);
    }


    /**
     * Get a GenPolynomial&lt;C&gt; element from an exponent vector.
     *
     * @param e exponent vector.
     * @return a GenPolynomial&lt;C&gt;.
     */
    public GenPolynomial<C> valueOf(ExpVector e) {
        return new GenPolynomial<>(this, coFac.getONE(), e);
    }


    /**
     * Get a GenPolynomial&lt;C&gt; element from a coeffcient and an exponent
     * vector.
     *
     * @param a coefficient.
     * @param e exponent vector.
     * @return a GenPolynomial&lt;C&gt;.
     */
    public GenPolynomial<C> valueOf(C a, ExpVector e) {
        return new GenPolynomial<>(this, a, e);
    }


    /**
     * Get a (constant) GenPolynomial&lt;C&gt; element from a long value.
     *
     * @param a long.
     * @return a GenPolynomial&lt;C&gt;.
     */
    public GenPolynomial<C> fromInteger(long a) {
        return new GenPolynomial<>(this, coFac.fromInteger(a), evzero);
    }


    /**
     * Get a (constant) GenPolynomial&lt;C&gt; element from a BigInteger value.
     *
     * @param a BigInteger.
     * @return a GenPolynomial&lt;C&gt;.
     */
    public GenPolynomial<C> fromInteger(BigInteger a) {
        return new GenPolynomial<>(this, coFac.fromInteger(a), evzero);
    }


    /**
     * Random polynomial. Generates a random polynomial with k = 5, l = n, d =
     * (nvar == 1) ? n : 3, q = (nvar == 1) ? 0.7 : 0.3.
     *
     * @param n number of terms.
     * @return a random polynomial.
     */
    public GenPolynomial<C> random(int n) {
        return random(n, random);
    }


    /**
     * Random polynomial. Generates a random polynomial with k = 5, l = n, d =
     * (nvar == 1) ? n : 3, q = (nvar == 1) ? 0.7 : 0.3.
     *
     * @param n   number of terms.
     * @param rnd is a source for random bits.
     * @return a random polynomial.
     */
    public GenPolynomial<C> random(int n, Random rnd) {
        if (nvar == 1) {
            return random(3, n, n, 0.7f, rnd);
        }
        return random(5, n, 3, 0.3f, rnd);
    }


    /**
     * Generate a random polynomial.
     *
     * @param k bitsize of random coefficients.
     * @param l number of terms.
     * @param d maximal degree in each variable.
     * @param q density of nozero exponents.
     * @return a random polynomial.
     */
    public GenPolynomial<C> random(int k, int l, int d, float q) {
        return random(k, l, d, q, random);
    }


    /**
     * Generate a random polynomial.
     *
     * @param k   bitsize of random coefficients.
     * @param l   number of terms.
     * @param d   maximal degree in each variable.
     * @param q   density of nozero exponents.
     * @param rnd is a source for random bits.
     * @return a random polynomial.
     */
    public GenPolynomial<C> random(int k, int l, int d, float q, Random rnd) {
        GenPolynomial<C> r = getZERO(); //.clone() or copy( ZERO );
        ExpVector e;
        C a;
        // add l random coeffs and exponents
        for (int i = 0; i < l; i++) {
            e = ExpVector.EVRAND(nvar, d, q, rnd);
            a = coFac.random(k, rnd);
            r = r.sum(a, e); // somewhat inefficient but clean
        }
        return r;
    }


    /**
     * Copy polynomial c.
     *
     * @param c
     * @return a copy of c.
     */
    public GenPolynomial<C> copy(GenPolynomial<C> c) {
        return new GenPolynomial<>(this, c.val);
    }


    /**
     * Generate univariate polynomial in a given variable.
     *
     * @param i the index of the variable.
     * @return X_i as univariate polynomial.
     */
    public GenPolynomial<C> univariate(int i) {
        return univariate(0, i, 1L);
    }


    /**
     * Generate univariate polynomial in a given variable with given exponent.
     *
     * @param i the index of the variable.
     * @param e the exponent of the variable.
     * @return X_i^e as univariate polynomial.
     */
    public GenPolynomial<C> univariate(int i, long e) {
        return univariate(0, i, e);
    }


    /**
     * Generate univariate polynomial in a given variable with given exponent.
     *
     * @param modv number of module variables.
     * @param i    the index of the variable.
     * @param e    the exponent of the variable.
     * @return X_i^e as univariate polynomial.
     */
    public GenPolynomial<C> univariate(int modv, int i, long e) {
        GenPolynomial<C> p = getZERO();
        int r = nvar - modv;
        if (0 <= i && i < r) {
            C one = coFac.getONE();
            ExpVector f = ExpVector.create(r, i, e);
            if (modv > 0) {
                f = f.extend(modv, 0, 0l);
            }
            p = p.sum(one, f);
        }
        return p;
    }


    /**
     * Get a list of the generating elements.
     *
     * @return list of generators for the algebraic structure.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#generators()
     */
    public List<GenPolynomial<C>> generators() {
        List<? extends C> cogens = coFac.generators();
        List<? extends GenPolynomial<C>> univs = univariateList();
        List<GenPolynomial<C>> gens = new ArrayList<>(univs.size() + cogens.size());
        for (C c : cogens) {
            gens.add(getONE().multiply(c));
        }
        gens.addAll(univs);
        return gens;
    }


    /**
     * Is this structure finite or infinite.
     *
     * @return true if this structure is finite, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#isFinite()
     */
    public boolean isFinite() {
        return (nvar == 0) && coFac.isFinite();
    }


    /**
     * Generate list of univariate polynomials in all variables.
     *
     * @return List(X_1, ..., X_n) a list of univariate polynomials.
     */
    public List<? extends GenPolynomial<C>> univariateList() {
        return univariateList(0, 1L);
    }


    /**
     * Generate list of univariate polynomials in all variables with given
     * exponent.
     *
     * @param modv number of module variables.
     * @param e    the exponent of the variables.
     * @return List(X_1^e, ..., X_n^e) a list of univariate polynomials.
     */
    public List<? extends GenPolynomial<C>> univariateList(int modv, long e) {
        List<GenPolynomial<C>> pols = new ArrayList<>(nvar);
        int nm = nvar - modv;
        for (int i = 0; i < nm; i++) {
            GenPolynomial<C> p = univariate(modv, nm - 1 - i, e);
            pols.add(p);
        }
        return pols;
    }


    /**
     * Extend variables. Used e.g. in module embedding. Extend number of
     * variables by i.
     *
     * @param i number of variables to extend.
     * @return extended polynomial ring factory.
     */
    public GenPolynomialRing<C> extend(int i) {
        // add module variable names
        String[] v = newVars("e", i);
        return extend(v);
    }


    /**
     * Extend variables. Used e.g. in module embedding. Extend number of
     * variables by length(vn).
     *
     * @param vn names for extended variables.
     * @return extended polynomial ring factory.
     */
    public GenPolynomialRing<C> extend(String[] vn) {
        if (vn == null || vars == null) {
            throw new IllegalArgumentException("vn and vars may not be null");
        }
        int i = vn.length;
        String[] v = new String[vars.length + i];
        System.arraycopy(vars, 0, v, 0, vars.length);
        System.arraycopy(vn, 0, v, vars.length, vn.length);

        TermOrder to = tord.extend(nvar, i);
        GenPolynomialRing<C> pfac = new GenPolynomialRing<>(coFac, nvar + i, to, v);
        return pfac;
    }


    /**
     * Contract variables. Used e.g. in module embedding. Contract number of
     * variables by i.
     *
     * @param i number of variables to remove.
     * @return contracted polynomial ring factory.
     */
    public GenPolynomialRing<C> contract(int i) {
        String[] v = null;
        if (vars != null) {
            v = new String[vars.length - i];
            System.arraycopy(vars, 0, v, 0, vars.length - i);
        }
        TermOrder to = tord.contract(i, nvar - i);
        GenPolynomialRing<C> pfac = new GenPolynomialRing<>(coFac, nvar - i, to, v);
        return pfac;
    }


    /**
     * Recursive representation as polynomial with i main variables.
     *
     * @param i number of main variables.
     * @return recursive polynomial ring factory.
     */
    public GenPolynomialRing<GenPolynomial<C>> recursive(int i) {
        if (i <= 0 || i >= nvar) {
            throw new IllegalArgumentException("wrong: 0 < " + i + " < " + nvar);
        }
        GenPolynomialRing<C> cfac = contract(i);
        String[] v = null;
        if (vars != null) {
            v = new String[i];
            int k = 0;
            for (int j = nvar - i; j < nvar; j++) {
                v[k++] = vars[j];
            }
        }
        TermOrder to = tord.contract(0, i); // ??
        GenPolynomialRing<GenPolynomial<C>> pfac = new GenPolynomialRing<>(cfac, i, to, v);
        return pfac;
    }


    /**
     * Reverse variables. Used e.g. in opposite rings.
     *
     * @return polynomial ring factory with reversed variables.
     */
    public GenPolynomialRing<C> reverse() {
        return reverse(false);
    }


    /**
     * Reverse variables. Used e.g. in opposite rings.
     *
     * @param partial true for partialy reversed term orders.
     * @return polynomial ring factory with reversed variables.
     */
    public GenPolynomialRing<C> reverse(boolean partial) {
        String[] v = null;
        if (vars != null) { // vars are not inversed
            v = new String[vars.length];
            int k = tord.getSplit();
            if (partial && k < vars.length) {
                for (int j = 0; j < k; j++) {
                    v[vars.length - k + j] = vars[vars.length - 1 - j];
                }
                System.arraycopy(vars, 0, v, 0, vars.length - k);
            } else {
                for (int j = 0; j < vars.length; j++) {
                    v[j] = vars[vars.length - 1 - j];
                }
            }
        }
        TermOrder to = tord.reverse(partial);
        GenPolynomialRing<C> pfac = new GenPolynomialRing<>(coFac, nvar, to, v);
        pfac.partial = partial;
        return pfac;
    }


    /**
     * Get PolynomialComparator.
     *
     * @return polynomial comparator.
     */
    public PolynomialComparator<C> getComparator() {
        return new PolynomialComparator<>(tord, false);
    }


    /**
     * New variable names. Generate new names for variables,
     *
     * @param prefix name prefix.
     * @param n      number of variables.
     * @return new variable names.
     */
    public static String[] newVars(String prefix, int n) {
        String[] vars = new String[n];

        int m = knownVars.size();
        String name = prefix + m;
        for (int i = 0; i < n; i++) {
            while (knownVars.contains(name)) {
                m++;
                name = prefix + m;
            }
            vars[i] = name;
            knownVars.add(name);
            m++;
            name = prefix + m;
        }
        return vars;
    }


    /**
     * New variable names. Generate new names for variables,
     *
     * @param prefix name prefix.
     * @return new variable names.
     */
    public String[] newVars(String prefix) {
        return newVars(prefix, nvar);
    }


    /**
     * Add variable names.
     *
     * @param vars variable names to be recorded.
     */
    public static void addVars(String[] vars) {
        if (vars == null) {
            return;
        }

        Collections.addAll(knownVars, vars);

    }


    /**
     * Get a GenPolynomial iterator.
     *
     * @return an iterator over all polynomials.
     */
    public Iterator<GenPolynomial<C>> iterator() {
        if (coFac.isFinite()) {
            return new GenPolynomialIterator<>(this);
        }
        return new GenPolynomialMonomialIterator<>(this);
        //throw new IllegalArgumentException("only for finite iterable coefficients implemented");
    }

}


/**
 * Polynomial iterator.
 *
 * @author Heinz Kredel
 */
class GenPolynomialIterator<C extends RingElem<C>> implements Iterator<GenPolynomial<C>> {


    /**
     * data structure.
     */
    final GenPolynomialRing<C> ring;


    final Iterator<List<Long>> eviter;


    final List<ExpVector> powers;


    final List<Iterable<C>> coeffiter;


    Iterator<List<C>> itercoeff;


    GenPolynomial<C> current;


    /**
     * Polynomial iterator constructor.
     */
    public GenPolynomialIterator(GenPolynomialRing<C> fac) {
        ring = fac;
        LongIterable li = new LongIterable();
        li.setNonNegativeIterator();
        List<Iterable<Long>> tlist = new ArrayList<>(ring.nvar);
        for (int i = 0; i < ring.nvar; i++) {
            tlist.add(li);
        }
        CartesianProductInfinite<Long> ei = new CartesianProductInfinite<>(tlist);
        eviter = ei.iterator();
        RingFactory<C> cf = ring.coFac;
        coeffiter = new ArrayList<>();
        if (cf instanceof Iterable && cf.isFinite()) {
            Iterable<C> cfi = (Iterable<C>) cf;
            coeffiter.add(cfi);
        } else {
            throw new IllegalArgumentException("only for finite iterable coefficients implemented");
        }
        CartesianProduct<C> tuples = new CartesianProduct<>(coeffiter);
        itercoeff = tuples.iterator();
        powers = new ArrayList<>();
        ExpVector e = ExpVector.create(eviter.next());
        powers.add(e);
        List<C> c = itercoeff.next();
        current = new GenPolynomial<>(ring, c.get(0), e);
    }


    /**
     * Test for availability of a next element.
     *
     * @return true if the iteration has more elements, else false.
     */
    public boolean hasNext() {
        return true;
    }


    /**
     * Get next polynomial.
     *
     * @return next polynomial.
     */
    public synchronized GenPolynomial<C> next() {
        GenPolynomial<C> res = current;
        if (!itercoeff.hasNext()) {
            ExpVector e = ExpVector.create(eviter.next());
            powers.add(0, e); // add new ev at beginning
            if (coeffiter.size() == 1) { // shorten frist iterator by one element
                coeffiter.add(coeffiter.get(0));
                Iterable<C> it = coeffiter.get(0);
                List<C> elms = new ArrayList<>();
                for (C elm : it) {
                    elms.add(elm);
                }
                elms.remove(0);
                coeffiter.set(0, elms);
            } else {
                coeffiter.add(coeffiter.get(1));
            }
            CartesianProduct<C> tuples = new CartesianProduct<>(coeffiter);
            itercoeff = tuples.iterator();
        }
        List<C> coeffs = itercoeff.next();
        //      while ( coeffs.get(0).isZERO() ) {
        //          coeffs = itercoeff.next(); // skip tuples with zero in first component
        //      }
        GenPolynomial<C> pol = ring.getZERO().copy();
        int i = 0;
        for (ExpVector f : powers) {
            C c = coeffs.get(i++);
            if (c.isZERO()) {
                continue;
            }
            if (pol.val.get(f) != null) {
                throw new RuntimeException("error in iterator");
            }
            pol.doPutToMap(f, c);
        }
        current = pol;
        return res;
    }


    /**
     * Remove an element if allowed.
     */
    public void remove() {
        throw new UnsupportedOperationException("cannnot remove elements");
    }
}


/**
 * Polynomial monomial iterator.
 *
 * @author Heinz Kredel
 */
class GenPolynomialMonomialIterator<C extends RingElem<C>> implements Iterator<GenPolynomial<C>> {


    /**
     * data structure.
     */
    final GenPolynomialRing<C> ring;


    final Iterator<List> iter;


    GenPolynomial<C> current;


    /**
     * Polynomial iterator constructor.
     */
    @SuppressWarnings("unchecked")
    public GenPolynomialMonomialIterator(GenPolynomialRing<C> fac) {
        ring = fac;
        LongIterable li = new LongIterable();
        li.setNonNegativeIterator();
        List<Iterable<Long>> tlist = new ArrayList<>(ring.nvar);
        for (int i = 0; i < ring.nvar; i++) {
            tlist.add(li);
        }
        CartesianProductInfinite<Long> ei = new CartesianProductInfinite<>(tlist);
        //Iterator<List<Long>> eviter = ei.iterator();

        RingFactory<C> cf = ring.coFac;
        Iterable<C> coeffiter;
        if (cf instanceof Iterable && !cf.isFinite()) {
            Iterable<C> cfi = (Iterable<C>) cf;
            coeffiter = cfi;
        } else {
            throw new IllegalArgumentException("only for infinite iterable coefficients implemented");
        }

        // Cantor iterator for exponents and coeffcients
        List<Iterable> eci = new ArrayList<>(2); // no type parameter
        eci.add(ei);
        eci.add(coeffiter);
        CartesianProductInfinite ecp = new CartesianProductInfinite(eci);
        iter = ecp.iterator();

        List ec = iter.next();
        List<Long> ecl = (List<Long>) ec.get(0);
        C c = (C) ec.get(1); // zero
        ExpVector e = ExpVector.create(ecl);
        current = new GenPolynomial<>(ring, c, e);
    }


    /**
     * Test for availability of a next element.
     *
     * @return true if the iteration has more elements, else false.
     */
    public boolean hasNext() {
        return true;
    }


    /**
     * Get next polynomial.
     *
     * @return next polynomial.
     */
    public synchronized GenPolynomial<C> next() {
        GenPolynomial<C> res = current;

        List ec = iter.next();
        C c = (C) ec.get(1);
        while (c.isZERO()) { // zero already done in first next
            ec = iter.next();
            c = (C) ec.get(1);
        }
        List<Long> ecl = (List<Long>) ec.get(0);
        ExpVector e = ExpVector.create(ecl);
        current = new GenPolynomial<>(ring, c, e);

        return res;
    }


    /**
     * Remove an element if allowed.
     */
    public void remove() {
        throw new UnsupportedOperationException("cannnot remove elements");
    }
}
