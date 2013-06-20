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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.AbelianGroupElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.AbelianGroupFactory;

import java.util.Collection;
import java.util.Random;


/**
 * ExpVector implements exponent vectors for polynomials. Exponent vectors are
 * implemented as arrays of Java elementary types, like long, int, short and
 * byte. ExpVector provides also the familiar MAS static method names. The
 * implementation is only tested for nonnegative exponents but should work also
 * for negative exponents. Objects of this class are intended to be immutable,
 * but exponents can be set (during construction); also the hash code is only
 * computed once, when needed. The different storage unit implementations are
 * <code>ExpVectorLong</code> <code>ExpVectorInteger</code>,
 * <code>ExpVectorShort</code> and <code>ExpVectorByte</code>. The static
 * factory methods <code>create()</code> of <code>ExpVector</code> select the
 * respective storage unit. The selection of the desired storage unit is
 * internally done via the static variable <code>storunit</code>. This varaible
 * should not be changed dynamically.
 *
 * @author Heinz Kredel
 */

public abstract class ExpVector implements AbelianGroupElem<ExpVector>
/*Cloneable, Serializable*/ {


    /**
     * Stored hash code.
     */
    protected int hash = 0;


    /**
     * Random number generator.
     */
    private final static Random random = new Random();


    /**
     * Storage representation of exponent arrays.
     */
    public static enum StorUnit {
        LONG, INT, SHORT, BYTE
    }


    /**
     * Used storage representation of exponent arrays. <b>Note:</b> Set this
     * only statically and not dynamically.
     */
    public final static StorUnit storunit = StorUnit.LONG;


    /**
     * Constructor for ExpVector.
     */
    public ExpVector() {
        hash = 0;
    }


    /**
     * Factory constructor for ExpVector.
     *
     * @param n length of exponent vector.
     */
    public static ExpVector create(int n) {
        switch (storunit) {
            case INT:
                return new ExpVectorInteger(n);
            case LONG:
                return new ExpVectorLong(n);
            case SHORT:
                return new ExpVectorShort(n);
            case BYTE:
                return new ExpVectorByte(n);
            default:
                return new ExpVectorInteger(n);
        }
    }


    /**
     * Factory constructor for ExpVector. Sets exponent i to e.
     *
     * @param n length of exponent vector.
     * @param i index of exponent to be set.
     * @param e exponent to be set.
     */
    public static ExpVector create(int n, int i, long e) {
        switch (storunit) {
            case INT:
                return new ExpVectorInteger(n, i, e);
            case LONG:
                return new ExpVectorLong(n, i, e);
            case SHORT:
                return new ExpVectorShort(n, i, e);
            case BYTE:
                return new ExpVectorByte(n, i, e);
            default:
                return new ExpVectorInteger(n, i, e);
        }
    }


    /**
     * Internal factory constructor for ExpVector. Sets val.
     *
     * @param v internal representation array.
     */
    public static ExpVector create(long[] v) {
        switch (storunit) {
            case INT:
                return new ExpVectorInteger(v);
            case LONG:
                return new ExpVectorLong(v);
            case SHORT:
                return new ExpVectorShort(v);
            case BYTE:
                return new ExpVectorByte(v);
            default:
                return new ExpVectorInteger(v);
        }
    }


    /**
     * Factory constructor for ExpVector. Sets val.
     *
     * @param v collection of exponents.
     */
    public static ExpVector create(Collection<Long> v) {
        long[] w = new long[v.size()];
        int i = 0;
        for (Long k : v) {
            w[i++] = k;
        }
        return create(w);
    }


    /**
     * Get the corresponding element factory.
     *
     * @return factory for this Element.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Element#factory()
     */
    public AbelianGroupFactory<ExpVector> factory() {
        throw new UnsupportedOperationException("no factory implemented for ExpVector");
    }


    /**
     * Is this structure finite or infinite.
     *
     * @return true if this structure is finite, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#isFinite() <b>Note: </b> returns true
     *      because of finite set of values in each index.
     */
    public boolean isFinite() {
        return true;
    }


    /**
     * Clone this.
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public abstract ExpVector copy();


    /**
     * Get the exponent vector.
     *
     * @return val.
     */
    /*package*/
    abstract long[] getVal();


    /**
     * Get the exponent at position i.
     *
     * @param i position.
     * @return val[i].
     */
    public abstract long getVal(int i);


    /**
     * Set the exponent at position i to e.
     *
     * @param i
     * @param e
     * @return old val[i].
     */
    protected abstract long setVal(int i, long e);


    /**
     * Get the length of this exponent vector.
     *
     * @return val.length.
     */
    public abstract int length();


    /**
     * Extend variables. Used e.g. in module embedding. Extend this by i
     * elements and set val[j] to e.
     *
     * @param i number of elements to extend.
     * @param j index of element to be set.
     * @param e new exponent for val[j].
     * @return extended exponent vector.
     */
    public abstract ExpVector extend(int i, int j, long e);


    /**
     * Extend lower variables. Extend this by i lower elements and set val[j] to
     * e.
     *
     * @param i number of elements to extend.
     * @param j index of element to be set.
     * @param e new exponent for val[j].
     * @return extended exponent vector.
     */
    public abstract ExpVector extendLower(int i, int j, long e);


    /**
     * Contract variables. Used e.g. in module embedding. Contract this to len
     * elements.
     *
     * @param i   position of first element to be copied.
     * @param len new length.
     * @return contracted exponent vector.
     */
    public abstract ExpVector contract(int i, int len);


    /**
     * Reverse variables. Used e.g. in opposite rings.
     *
     * @return reversed exponent vector.
     */
    public abstract ExpVector reverse();


    /**
     * Reverse j variables. Used e.g. in opposite rings. Reverses the first j-1
     * variables, the rest is unchanged.
     *
     * @param j index of first variable not reversed.
     * @return reversed exponent vector.
     */
    public abstract ExpVector reverse(int j);


    /**
     * Combine with ExpVector. Combine this with the other ExpVector V.
     *
     * @param V the other exponent vector.
     * @return combined exponent vector.
     */
    public abstract ExpVector combine(ExpVector V);


    /**
     * Get the string representation.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("(");
        for (int i = 0; i < length(); i++) {
            s.append(getVal(i));
            if (i < length() - 1) {
                s.append(",");
            }
        }
        s.append(")");
        return s.toString();
    }


    /**
     * Get the string representation with variable names.
     *
     * @param vars names of variables.
     * @see java.lang.Object#toString()
     */
    public String toString(String[] vars) {
        StringBuilder s = new StringBuilder();
        boolean pit;
        int r = length();
        if (r != vars.length) {
            return toString();
        }
        if (r == 0) {
            return s.toString();
        }
        long vi;
        for (int i = r - 1; i > 0; i--) {
            vi = getVal(i);
            if (vi != 0) {
                s.append(vars[r - 1 - i]);
                if (vi != 1) {
                    s.append("^").append(vi);
                }
                pit = false;
                for (int j = i - 1; j >= 0; j--) {
                    if (getVal(j) != 0) {
                        pit = true;
                    }
                }
                if (pit) {
                    s.append(" * ");
                }
            }
        }
        vi = getVal(0);
        if (vi != 0) {
            s.append(vars[r - 1]);
            if (vi != 1) {
                s.append("^").append(vi);
            }
        }
        return s.toString();
    }


    /**
     * Get the string representation of the variables.
     *
     * @param vars names of variables.
     * @return string representation of the variables.
     * @see java.util.Arrays#toString()
     */
    public static String varsToString(String[] vars) {
        if (vars == null) {
            return "null";
        }
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < vars.length; i++) {
            s.append(vars[i]);
            if (i < vars.length - 1) {
                s.append(",");
            }
        }
        return s.toString();
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object B) {
        if (!(B instanceof ExpVector)) {
            return false;
        }
        ExpVector b = (ExpVector) B;
        int t = this.invLexCompareTo(b);
        return (0 == t);
    }


    /**
     * hashCode. Optimized for small exponents, i.e. &le; 2<sup>4</sup> and
     * small number of variables, i.e. &le; 8.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (hash == 0) {
            for (int i = 0; i < length(); i++) {
                hash = hash << 4 + getVal(i);
            }
            if (hash == 0) {
                hash = 1;
            }
        }
        return hash;
    }


    /**
     * Is ExpVector zero.
     *
     * @return If this has all elements 0 then true is returned, else false.
     */
    public boolean isZERO() {
        return (0 == this.signum());
    }

    /**
     * ExpVector absolute value.
     *
     * @return abs(this).
     */
    public abstract ExpVector abs();


    /**
     * ExpVector negate.
     *
     * @return -this.
     */
    public abstract ExpVector negate();


    /**
     * ExpVector summation.
     *
     * @param V
     * @return this+V.
     */
    public abstract ExpVector sum(ExpVector V);


    /**
     * ExpVector subtract. Result may have negative entries.
     *
     * @param V
     * @return this-V.
     */
    public abstract ExpVector subtract(ExpVector V);

    /**
     * ExpVector substitution. Clone and set exponent to d at position i.
     *
     * @param i position.
     * @param d new exponent.
     * @return substituted ExpVector.
     */
    public ExpVector subst(int i, long d) {
        ExpVector V = this.copy();
        return V;
        //return EVSU(this, i, d);
    }


    /**
     * Generate a random ExpVector.
     *
     * @param r   length of new ExpVector.
     * @param k   maximal degree in each exponent.
     * @param q   density of nozero exponents.
     * @param rnd is a source for random bits.
     * @return random ExpVector.
     */
    public static ExpVector EVRAND(int r, long k, float q, Random rnd) {
        long[] w = new long[r];
        long e;
        float f;
        for (int i = 0; i < w.length; i++) {
            f = rnd.nextFloat();
            if (f > q) {
                e = 0;
            } else {
                e = rnd.nextLong() % k;
                if (e < 0) {
                    e = -e;
                }
            }
            w[i] = e;
        }
        return create(w);
        //return new ExpVector( w );
    }


    /**
     * Generate a random ExpVector.
     *
     * @param r length of new ExpVector.
     * @param k maximal degree in each exponent.
     * @param q density of nozero exponents.
     * @return random ExpVector.
     */
    public static ExpVector random(int r, long k, float q) {
        return EVRAND(r, k, q, random);
    }


    /**
     * Generate a random ExpVector.
     *
     * @param r   length of new ExpVector.
     * @param k   maximal degree in each exponent.
     * @param q   density of nozero exponents.
     * @param rnd is a source for random bits.
     * @return random ExpVector.
     */
    public static ExpVector random(int r, long k, float q, Random rnd) {
        return EVRAND(r, k, q, rnd);
    }

    /**
     * ExpVector signum.
     *
     * @return 0 if this is zero, -1 if some entry is negative, 1 if no entry is
     *         negative and at least one entry is positive.
     */
    public abstract int signum();


    /**
     * ExpVector degree.
     *
     * @return total degree of all exponents.
     */
    public long degree() {
        return totalDeg();
    }


    /**
     * ExpVector total degree.
     *
     * @return sum of all exponents.
     */
    public abstract long totalDeg();


    /**
     * ExpVector maximal degree.
     *
     * @return maximal exponent.
     */
    public abstract long maxDeg();

    /**
     * ExpVector least common multiple.
     *
     * @param V
     * @return component wise maximum of this and V.
     */
    public abstract ExpVector lcm(ExpVector V);


    /**
     * ExpVector greatest common divisor.
     *
     * @param V
     * @return component wise minimum of this and V.
     */
    public abstract ExpVector gcd(ExpVector V);


    /**
     * ExpVector dependency on variables.
     *
     * @return array of indices where val has positive exponents.
     */
    public abstract int[] dependencyOnVariables();


    /**
     * ExpVector multiple test. Test if this is component wise greater or equal
     * to V.
     *
     * @param V
     * @return true if this is a multiple of V, else false.
     */
    public abstract boolean multipleOf(ExpVector V);


    /**
     * ExpVector compareTo.
     *
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    //JAVA6only: @Override
    public int compareTo(ExpVector V) {
        return this.invLexCompareTo(V);
    }


    /**
     * Inverse lexicographical compare.
     *
     * @param U
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public static int EVILCP(ExpVector U, ExpVector V) {
        return U.invLexCompareTo(V);
    }


    /**
     * ExpVector inverse lexicographical compareTo.
     *
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public abstract int invLexCompareTo(ExpVector V);


    /**
     * Inverse lexicographical compare part. Compare entries between begin and
     * end (-1).
     *
     * @param U
     * @param V
     * @param begin
     * @param end
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public static int EVILCP(ExpVector U, ExpVector V, int begin, int end) {
        return U.invLexCompareTo(V, begin, end);
    }


    /**
     * ExpVector inverse lexicographical compareTo.
     *
     * @param V
     * @param begin
     * @param end
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public abstract int invLexCompareTo(ExpVector V, int begin, int end);


    /**
     * Inverse graded lexicographical compare.
     *
     * @param U
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public static int EVIGLC(ExpVector U, ExpVector V) {
        return U.invGradCompareTo(V);
    }


    /**
     * ExpVector inverse graded lexicographical compareTo.
     *
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public abstract int invGradCompareTo(ExpVector V);


    /**
     * Inverse graded lexicographical compare part. Compare entries between
     * begin and end (-1).
     *
     * @param U
     * @param V
     * @param begin
     * @param end
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public static int EVIGLC(ExpVector U, ExpVector V, int begin, int end) {
        return U.invGradCompareTo(V, begin, end);
    }


    /**
     * ExpVector inverse graded lexicographical compareTo.
     *
     * @param V
     * @param begin
     * @param end
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public abstract int invGradCompareTo(ExpVector V, int begin, int end);


    /**
     * Reverse inverse lexicographical compare.
     *
     * @param U
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public static int EVRILCP(ExpVector U, ExpVector V) {
        return U.revInvLexCompareTo(V);
    }


    /**
     * ExpVector reverse inverse lexicographical compareTo.
     *
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public abstract int revInvLexCompareTo(ExpVector V);


    /**
     * Reverse inverse lexicographical compare part. Compare entries between
     * begin and end (-1).
     *
     * @param U
     * @param V
     * @param begin
     * @param end
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public static int EVRILCP(ExpVector U, ExpVector V, int begin, int end) {
        return U.revInvLexCompareTo(V, begin, end);
    }


    /**
     * ExpVector reverse inverse lexicographical compareTo.
     *
     * @param V
     * @param begin
     * @param end
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public abstract int revInvLexCompareTo(ExpVector V, int begin, int end);


    /**
     * Reverse inverse graded lexicographical compare.
     *
     * @param U
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public static int EVRIGLC(ExpVector U, ExpVector V) {
        return U.revInvGradCompareTo(V);
    }


    /**
     * ExpVector reverse inverse graded compareTo.
     *
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public abstract int revInvGradCompareTo(ExpVector V);


    /**
     * Reverse inverse graded lexicographical compare part. Compare entries
     * between begin and end (-1).
     *
     * @param U
     * @param V
     * @param begin
     * @param end
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public static int EVRIGLC(ExpVector U, ExpVector V, int begin, int end) {
        return U.revInvGradCompareTo(V, begin, end);
    }


    /**
     * ExpVector reverse inverse graded compareTo.
     *
     * @param V
     * @param begin
     * @param end
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public abstract int revInvGradCompareTo(ExpVector V, int begin, int end);


    /**
     * Inverse weighted lexicographical compare.
     *
     * @param w weight array.
     * @param U
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public static int EVIWLC(long[][] w, ExpVector U, ExpVector V) {
        return U.invWeightCompareTo(w, V);
    }


    /**
     * ExpVector inverse weighted lexicographical compareTo.
     *
     * @param w weight array.
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    public abstract int invWeightCompareTo(long[][] w, ExpVector V);

}
