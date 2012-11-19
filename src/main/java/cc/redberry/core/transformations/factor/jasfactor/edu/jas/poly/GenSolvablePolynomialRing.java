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
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * GenSolvablePolynomialRing generic solvable polynomial factory implementing
 * RingFactory and extending GenPolynomialRing factory; Factory for n-variate
 * ordered solvable polynomials over C. The non-commutative multiplication
 * relations are maintained in a relation table. Almost immutable object, except
 * variable names and relation table contents.
 *
 * @param <C> coefficient type.
 * @author Heinz Kredel
 */

public class GenSolvablePolynomialRing<C extends RingElem<C>> extends GenPolynomialRing<C> {


    //  implements RingFactory< GenSolvablePolynomial<C> > {


    /**
     * The solvable multiplication relations.
     */
    public final RelationTable<C> table;


    /**
     * The constant polynomial 0 for this ring. Hides super ZERO.
     */
    public final GenSolvablePolynomial<C> ZERO;


    /**
     * The constant polynomial 1 for this ring. Hides super ONE.
     */
    public final GenSolvablePolynomial<C> ONE;

    /**
     * The constructor creates a solvable polynomial factory object with the
     * given term order and commutative relations.
     *
     * @param cf factory for coefficients of type C.
     * @param n  number of variables.
     * @param t  a term order.
     * @param v  names for the variables.
     */
    public GenSolvablePolynomialRing(RingFactory<C> cf, int n, TermOrder t, String[] v) {
        this(cf, n, t, v, null);
    }


    /**
     * The constructor creates a solvable polynomial factory object with the
     * given term order.
     *
     * @param cf factory for coefficients of type C.
     * @param n  number of variables.
     * @param t  a term order.
     * @param v  names for the variables.
     * @param rt solvable multiplication relations.
     */
    public GenSolvablePolynomialRing(RingFactory<C> cf, int n, TermOrder t, String[] v, RelationTable<C> rt) {
        super(cf, n, t, v);
        if (rt == null) {
            table = new RelationTable<>(this);
        } else {
            table = rt;
        }
        ZERO = new GenSolvablePolynomial<>(this);
        C coeff = coFac.getONE();
        //evzero = ExpVector.create(nvar); // from super
        ONE = new GenSolvablePolynomial<>(this, coeff, evzero);
    }


    /**
     * Get the String representation.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String res = super.toString();
        res += "\n" + table.toString(vars);
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
        if (!(other instanceof GenSolvablePolynomialRing)) {
            return false;
        }
        // do a super.equals( )
        if (!super.equals(other)) {
            return false;
        }
        GenSolvablePolynomialRing<C> oring = null;
        try {
            oring = (GenSolvablePolynomialRing<C>) other;
        } catch (ClassCastException ignored) {
        }
        if (oring == null) {
            return false;
        }
        // @todo check same base relations
        //if ( ! table.equals(oring.table) ) {
        //    return false;
        //}
        return true;
    }


    /**
     * Hash code for this polynomial ring.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int h;
        h = super.hashCode();
        h = 37 * h + table.hashCode();
        return h;
    }


    /**
     * Get the zero element.
     *
     * @return 0 as GenSolvablePolynomial<C>.
     */
    @Override
    public GenSolvablePolynomial<C> getZERO() {
        return ZERO;
    }


    /**
     * Get the one element.
     *
     * @return 1 as GenSolvablePolynomial<C>.
     */
    @Override
    public GenSolvablePolynomial<C> getONE() {
        return ONE;
    }


    /**
     * Query if this ring is commutative.
     *
     * @return true if this ring is commutative, else false.
     */
    @Override
    public boolean isCommutative() {
        if (table.size() == 0) {
            return super.isCommutative();
        }
        // todo: check structure of relations
        return false;
    }


    /**
     * Query if this ring is associative. Test if the relations define an
     * associative solvable ring.
     *
     * @return true, if this ring is associative, else false.
     */
    @Override
    public boolean isAssociative() {
        GenSolvablePolynomial<C> Xi;
        GenSolvablePolynomial<C> Xj;
        GenSolvablePolynomial<C> Xk;
        GenSolvablePolynomial<C> p;
        GenSolvablePolynomial<C> q;
        for (int i = 0; i < nvar; i++) {
            Xi = univariate(i);
            for (int j = i + 1; j < nvar; j++) {
                Xj = univariate(j);
                for (int k = j + 1; k < nvar; k++) {
                    Xk = univariate(k);
                    p = Xk.multiply(Xj).multiply(Xi);
                    q = Xk.multiply(Xj.multiply(Xi));
                    if (!p.equals(q)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Get a (constant) GenSolvablePolynomial&lt;C&gt; element from a long
     * value.
     *
     * @param a long.
     * @return a GenSolvablePolynomial&lt;C&gt;.
     */
    @Override
    public GenSolvablePolynomial<C> fromInteger(long a) {
        return new GenSolvablePolynomial<>(this, coFac.fromInteger(a), evzero);
    }


    /**
     * Get a (constant) GenSolvablePolynomial&lt;C&gt; element from a BigInteger
     * value.
     *
     * @param a BigInteger.
     * @return a GenSolvablePolynomial&lt;C&gt;.
     */
    @Override
    public GenSolvablePolynomial<C> fromInteger(BigInteger a) {
        return new GenSolvablePolynomial<>(this, coFac.fromInteger(a), evzero);
    }


    /**
     * Copy polynomial c.
     *
     * @param c
     * @return a copy of c.
     */
    public GenSolvablePolynomial<C> copy(GenSolvablePolynomial<C> c) {
        return new GenSolvablePolynomial<>(this, c.val);
    }


    /**
     * Generate univariate solvable polynomial in a given variable.
     *
     * @param i the index of the variable.
     * @return X_i as solvable univariate polynomial.
     */
    @Override
    public GenSolvablePolynomial<C> univariate(int i) {
        return (GenSolvablePolynomial<C>) super.univariate(i);
    }


    /**
     * Generate univariate solvable polynomial in a given variable with given
     * exponent.
     *
     * @param i the index of the variable.
     * @param e the exponent of the variable.
     * @return X_i^e as solvable univariate polynomial.
     */
    @Override
    public GenSolvablePolynomial<C> univariate(int i, long e) {
        return (GenSolvablePolynomial<C>) super.univariate(i, e);
    }


    /**
     * Generate univariate solvable polynomial in a given variable with given
     * exponent.
     *
     * @param modv number of module variables.
     * @param i    the index of the variable.
     * @param e    the exponent of the variable.
     * @return X_i^e as solvable univariate polynomial.
     */
    @Override
    public GenSolvablePolynomial<C> univariate(int modv, int i, long e) {
        return (GenSolvablePolynomial<C>) super.univariate(modv, i, e);
    }


    /**
     * Generate list of univariate polynomials in all variables.
     *
     * @return List(X_1, ..., X_n) a list of univariate polynomials.
     */
    @Override
    public List<GenSolvablePolynomial<C>> univariateList() {
        //return castToSolvableList( super.univariateList() );
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
    @Override
    public List<GenSolvablePolynomial<C>> univariateList(int modv, long e) {
        List<GenSolvablePolynomial<C>> pols = new ArrayList<>(nvar);
        int nm = nvar - modv;
        for (int i = 0; i < nm; i++) {
            GenSolvablePolynomial<C> p = univariate(modv, nm - 1 - i, e);
            pols.add(p);
        }
        return pols;
    }


    /*
     * Generate list of univariate polynomials in all variables with given exponent.
     * @param modv number of module variables.
     * @param e the exponent of the variables.
     * @return List(X_1^e,...,X_n^e) a list of univariate polynomials.
     @Override
     public List<GenSolvablePolynomial<C>> univariateList(int modv, long e) {
     List<GenPolynomial<C>> pol = super.univariateList(modv,e);
     UnaryFunctor<GenPolynomial<C>,GenSolvablePolynomial<C>> fc 
     = new UnaryFunctor<GenPolynomial<C>,GenSolvablePolynomial<C>>() {
     public GenSolvablePolynomial<C> eval(GenPolynomial<C> p) {
     if ( ! (p instanceof GenSolvablePolynomial) ) {
     throw new RuntimeException("no solvable polynomial "+p);
     }
     return (GenSolvablePolynomial<C>) p;
     }
     };
     List<GenSolvablePolynomial<C>> pols 
     = ListUtil.<GenPolynomial<C>,GenSolvablePolynomial<C>>map(this,pol,fc);
     return pols;
     }
    */


    /* include here ?
     * Get list as List of GenSolvablePolynomials.
     * Required because no List casts allowed. Equivalent to 
     * cast (List&lt;GenSolvablePolynomial&lt;C&gt;&gt;) list.
     * @return solvable polynomial list from this.
     public List<GenSolvablePolynomial<C>> castToSolvableList(List<GenPolynomial<C>> list) {
     List< GenSolvablePolynomial<C> > slist = null;
     if ( list == null ) {
     return slist;
     }
     slist = new ArrayList< GenSolvablePolynomial<C> >( list.size() ); 
     GenSolvablePolynomial<C> s;
     for ( GenPolynomial<C> p: list ) {
     if ( ! (p instanceof GenSolvablePolynomial) ) {
     throw new RuntimeException("no solvable polynomial "+p);
     }
     s = (GenSolvablePolynomial<C>) p;
     slist.add( s );
     }
     return slist;
     }
    */


    /**
     * Extend variables. Used e.g. in module embedding. Extend number of
     * variables by i.
     *
     * @param i number of variables to extend.
     * @return extended solvable polynomial ring factory.
     */
    @Override
    public GenSolvablePolynomialRing<C> extend(int i) {
        GenPolynomialRing<C> pfac = super.extend(i);
        GenSolvablePolynomialRing<C> spfac = new GenSolvablePolynomialRing<>(pfac.coFac, pfac.nvar,
                pfac.tord, pfac.vars);
        spfac.table.extend(this.table);
        return spfac;
    }


    /**
     * Contract variables. Used e.g. in module embedding. Contract number of
     * variables by i.
     *
     * @param i number of variables to remove.
     * @return contracted solvable polynomial ring factory.
     */
    @Override
    public GenSolvablePolynomialRing<C> contract(int i) {
        GenPolynomialRing<C> pfac = super.contract(i);
        GenSolvablePolynomialRing<C> spfac = new GenSolvablePolynomialRing<>(pfac.coFac, pfac.nvar,
                pfac.tord, pfac.vars);
        spfac.table.contract(this.table);
        return spfac;
    }


    /**
     * Reverse variables. Used e.g. in opposite rings.
     *
     * @return solvable polynomial ring factory with reversed variables.
     */
    @Override
    public GenSolvablePolynomialRing<C> reverse() {
        return reverse(false);
    }


    /**
     * Reverse variables. Used e.g. in opposite rings.
     *
     * @param partial true for partialy reversed term orders.
     * @return solvable polynomial ring factory with reversed variables.
     */
    @Override
    public GenSolvablePolynomialRing<C> reverse(boolean partial) {
        GenPolynomialRing<C> pfac = super.reverse(partial);
        GenSolvablePolynomialRing<C> spfac = new GenSolvablePolynomialRing<>(pfac.coFac, pfac.nvar,
                pfac.tord, pfac.vars);
        spfac.partial = partial;
        spfac.table.reverse(this.table);
        return spfac;
    }

}
