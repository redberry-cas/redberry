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


/**
 * ExpVectorInteger implements exponent vectors for polynomials using arrays of
 * int as storage unit. This class is used by ExpVector internally, there is no
 * need to use this class directly.
 *
 * @author Heinz Kredel
 * @see ExpVector
 */

public final class ExpVectorInteger extends ExpVector
/*implements AbelianGroupElem<ExpVectorInteger>*/ {


    /**
     * The data structure is an array of longs.
     */
    /*package*/final int[] val;


    /**
     * Largest integer.
     */
    public static final long maxInt = (long) Integer.MAX_VALUE / 2;


    /**
     * Smallest integer.
     */
    public static final long minInt = (long) Integer.MIN_VALUE / 2;


    /**
     * Constructor for ExpVector.
     *
     * @param n length of exponent vector.
     */
    public ExpVectorInteger(int n) {
        this(new int[n]);
    }

    /**
     * Constructor for ExpVector. Sets exponent i to e.
     *
     * @param n length of exponent vector.
     * @param i index of exponent to be set.
     * @param e exponent to be set.
     */
    public ExpVectorInteger(int n, int i, long e) {
        this(n);
        if (e >= maxInt || e <= minInt) {
            throw new IllegalArgumentException("exponent to large: " + e);
        }
        val[i] = (int) e;
    }


    /**
     * Internal constructor for ExpVector. Sets val.
     *
     * @param v internal representation array.
     */
    protected ExpVectorInteger(int[] v) {
        super();
        val = v;
    }


    /**
     * Constructor for ExpVector. Sets val, converts from long array.
     *
     * @param v long representation array.
     */
    public ExpVectorInteger(long[] v) {
        this(v.length);
        for (int i = 0; i < v.length; i++) {
            if (v[i] >= maxInt || v[i] <= minInt) {
                throw new IllegalArgumentException("exponent to large: " + v[i]);
            }
            val[i] = (int) v[i];
        }
    }


    /**
     * Clone this.
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public ExpVectorInteger copy() {
        int[] w = new int[val.length];
        System.arraycopy(val, 0, w, 0, val.length);
        return new ExpVectorInteger(w);
    }


    /**
     * Get the exponent vector.
     *
     * @return val as long.
     */
    @Override
    /*package*/long[] getVal() {
        long v[] = new long[val.length];
        for (int i = 0; i < val.length; i++) {
            v[i] = val[i];
        }
        return v;
    }


    /**
     * Get the exponent at position i.
     *
     * @param i position.
     * @return val[i].
     */
    @Override
    public long getVal(int i) {
        return val[i];
    }


    /**
     * Set the exponent at position i to e.
     *
     * @param i
     * @param e
     * @return old val[i].
     */
    @Override
    protected long setVal(int i, long e) {
        int x = val[i];
        if (e >= maxInt || e <= minInt) {
            throw new IllegalArgumentException("exponent to large: " + e);
        }
        val[i] = (int) e;
        hash = 0; // beware of race condition
        return x;
    }

    /**
     * Get the length of this exponent vector.
     *
     * @return val.length.
     */
    @Override
    public int length() {
        return val.length;
    }


    /**
     * Extend variables. Used e.g. in module embedding. Extend this by i
     * elements and set val[j] to e.
     *
     * @param i number of elements to extend.
     * @param j index of element to be set.
     * @param e new exponent for val[j].
     * @return extended exponent vector.
     */
    @Override
    public ExpVectorInteger extend(int i, int j, long e) {
        int[] w = new int[val.length + i];
        System.arraycopy(val, 0, w, i, val.length);
        if (j >= i) {
            throw new IllegalArgumentException("i " + i + " <= j " + j + " invalid");
        }
        if (e >= maxInt || e <= minInt) {
            throw new IllegalArgumentException("exponent to large: " + e);
        }
        w[j] = (int) e;
        return new ExpVectorInteger(w);
    }


    /**
     * Extend lower variables. Extend this by i lower elements and set val[j] to
     * e.
     *
     * @param i number of elements to extend.
     * @param j index of element to be set.
     * @param e new exponent for val[j].
     * @return extended exponent vector.
     */
    @Override
    public ExpVectorInteger extendLower(int i, int j, long e) {
        int[] w = new int[val.length + i];
        System.arraycopy(val, 0, w, 0, val.length);
        if (j >= i) {
            throw new IllegalArgumentException("i " + i + " <= j " + j + " invalid");
        }
        w[val.length + j] = (int) e;
        return new ExpVectorInteger(w);
    }


    /**
     * Contract variables. Used e.g. in module embedding. Contract this to len
     * elements.
     *
     * @param i   position of first element to be copied.
     * @param len new length.
     * @return contracted exponent vector.
     */
    @Override
    public ExpVectorInteger contract(int i, int len) {
        if (i + len > val.length) {
            throw new IllegalArgumentException("len " + len + " > val.len " + val.length);
        }
        int[] w = new int[len];
        System.arraycopy(val, i, w, 0, len);
        return new ExpVectorInteger(w);
    }


    /**
     * Reverse variables. Used e.g. in opposite rings.
     *
     * @return reversed exponent vector.
     */
    @Override
    public ExpVectorInteger reverse() {
        int[] w = new int[val.length];
        for (int i = 0; i < val.length; i++) {
            w[i] = val[val.length - 1 - i];
        }
        return new ExpVectorInteger(w);
    }


    /**
     * Reverse j variables. Used e.g. in opposite rings. Reverses the first j-1
     * variables, the rest is unchanged.
     *
     * @param j index of first variable not reversed.
     * @return reversed exponent vector.
     */
    @Override
    public ExpVectorInteger reverse(int j) {
        if (j <= 0 || j > val.length) {
            return this;
        }
        int[] w = new int[val.length];
        for (int i = 0; i < j; i++) {
            w[i] = val[j - 1 - i];
        }
        // copy rest
        System.arraycopy(val, j, w, j, val.length - j);
        return new ExpVectorInteger(w);
    }


    /**
     * Combine with ExpVector. Combine this with the other ExpVector V.
     *
     * @param V the other exponent vector.
     * @return combined exponent vector.
     */
    @Override
    public ExpVectorInteger combine(ExpVector V) {
        if (V == null || V.length() == 0) {
            return this;
        }
        ExpVectorInteger Vi = (ExpVectorInteger) V;
        if (val.length == 0) {
            return Vi;
        }
        int[] w = new int[val.length + Vi.val.length];
        System.arraycopy(val, 0, w, 0, val.length);
        System.arraycopy(Vi.val, 0, w, val.length, Vi.val.length);
        return new ExpVectorInteger(w);
    }


    /**
     * Get the string representation.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString() + ":int";
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object B) {
        if (!(B instanceof ExpVectorInteger)) {
            return false;
        }
        ExpVectorInteger b = (ExpVectorInteger) B;
        int t = this.invLexCompareTo(b);
        return (0 == t);
    }


    /**
     * ExpVector absolute value.
     *
     * @return abs(this).
     */
    @Override
    public ExpVectorInteger abs() {
        int[] u = val;
        int[] w = new int[u.length];
        for (int i = 0; i < u.length; i++) {
            if (u[i] >= 0L) {
                w[i] = u[i];
            } else {
                w[i] = -u[i];
            }
        }
        return new ExpVectorInteger(w);
        //return EVABS(this);
    }


    /**
     * ExpVector negate.
     *
     * @return -this.
     */
    @Override
    public ExpVectorInteger negate() {
        int[] u = val;
        int[] w = new int[u.length];
        for (int i = 0; i < u.length; i++) {
            w[i] = -u[i];
        }
        return new ExpVectorInteger(w);
        // return EVNEG(this);
    }


    /**
     * ExpVector summation.
     *
     * @param V
     * @return this+V.
     */
    @Override
    public ExpVectorInteger sum(ExpVector V) {
        int[] u = val;
        int[] v = ((ExpVectorInteger) V).val;
        int[] w = new int[u.length];
        for (int i = 0; i < u.length; i++) {
            w[i] = u[i] + v[i];
        }
        return new ExpVectorInteger(w);
        // return EVSUM(this, V);
    }


    /**
     * ExpVector subtract. Result may have negative entries.
     *
     * @param V
     * @return this-V.
     */
    @Override
    public ExpVectorInteger subtract(ExpVector V) {
        int[] u = val;
        int[] v = ((ExpVectorInteger) V).val;
        int[] w = new int[u.length];
        for (int i = 0; i < u.length; i++) {
            w[i] = u[i] - v[i];
        }
        return new ExpVectorInteger(w);
        //return EVDIF(this, V);
    }


    /**
     * ExpVector substitution. Clone and set exponent to d at position i.
     *
     * @param i position.
     * @param d new exponent.
     * @return substituted ExpVector.
     */
    @Override
    public ExpVectorInteger subst(int i, long d) {
        ExpVectorInteger V = this.copy();
        return V;
        //return EVSU(this, i, d);
    }

    /**
     * ExpVector signum.
     *
     * @return 0 if this is zero, -1 if some entry is negative, 1 if no entry is
     *         negative and at least one entry is positive.
     */
    @Override
    public int signum() {
        int t = 0;
        int[] u = val;
        for (int anU : u) {
            if (anU < 0) {
                return -1;
            }
            if (anU > 0) {
                t = 1;
            }
        }
        return t;
        //return EVSIGN(this);
    }


    /**
     * ExpVector total degree.
     *
     * @return sum of all exponents.
     */
    @Override
    public long totalDeg() {
        long t = 0;
        int[] u = val; // U.val;
        for (int anU : u) {
            t += anU;
        }
        return t;
        //return EVTDEG(this);
    }


    /**
     * ExpVector maximal degree.
     *
     * @return maximal exponent.
     */
    @Override
    public long maxDeg() {
        long t = 0;
        int[] u = val;
        for (int anU : u) {
            if (anU > t) {
                t = anU;
            }
        }
        return t;
        //return EVMDEG(this);
    }


    /**
     * ExpVector least common multiple.
     *
     * @param V
     * @return component wise maximum of this and V.
     */
    @Override
    public ExpVectorInteger lcm(ExpVector V) {
        int[] u = val;
        int[] v = ((ExpVectorInteger) V).val;
        int[] w = new int[u.length];
        for (int i = 0; i < u.length; i++) {
            w[i] = (u[i] >= v[i] ? u[i] : v[i]);
        }
        return new ExpVectorInteger(w);
        //return EVLCM(this, V);
    }


    /**
     * ExpVector greatest common divisor.
     *
     * @param V
     * @return component wise minimum of this and V.
     */
    @Override
    public ExpVectorInteger gcd(ExpVector V) {
        int[] u = val;
        int[] v = ((ExpVectorInteger) V).val;
        int[] w = new int[u.length];
        for (int i = 0; i < u.length; i++) {
            w[i] = (u[i] <= v[i] ? u[i] : v[i]);
        }
        return new ExpVectorInteger(w);
        //return EVGCD(this, V);
    }


    /**
     * ExpVector dependency on variables.
     *
     * @return array of indices where val has positive exponents.
     */
    @Override
    public int[] dependencyOnVariables() {
        int[] u = val;
        int l = 0;
        for (int anU : u) {
            if (anU > 0) {
                l++;
            }
        }
        int[] dep = new int[l];
        if (l == 0) {
            return dep;
        }
        int j = 0;
        for (int i = 0; i < u.length; i++) {
            if (u[i] > 0) {
                dep[j] = i;
                j++;
            }
        }
        return dep;
    }


    /**
     * ExpVector multiple test. Test if this is component wise greater or equal
     * to V.
     *
     * @param V
     * @return true if this is a multiple of V, else false.
     */
    @Override
    public boolean multipleOf(ExpVector V) {
        int[] u = val;
        int[] v = ((ExpVectorInteger) V).val;
        boolean t = true;
        for (int i = 0; i < u.length; i++) {
            if (u[i] < v[i]) {
                return false;
            }
        }
        return t;
        //return EVMT(this, V);
    }


    /**
     * ExpVector compareTo.
     *
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    @Override
    public int compareTo(ExpVector V) {
        return this.invLexCompareTo(V);
    }


    /**
     * ExpVector inverse lexicographical compareTo.
     *
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    @Override
    public int invLexCompareTo(ExpVector V) {
        int[] u = val;
        int[] v = ((ExpVectorInteger) V).val;
        int t = 0;
        for (int i = 0; i < u.length; i++) {
            if (u[i] > v[i])
                return 1;
            if (u[i] < v[i])
                return -1;
        }
        return t;
        //return EVILCP(this, V);
    }


    /**
     * ExpVector inverse lexicographical compareTo.
     *
     * @param V
     * @param begin
     * @param end
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    @Override
    public int invLexCompareTo(ExpVector V, int begin, int end) {
        int[] u = val;
        int[] v = ((ExpVectorInteger) V).val;
        int t = 0;
        for (int i = begin; i < end; i++) {
            if (u[i] > v[i])
                return 1;
            if (u[i] < v[i])
                return -1;
        }
        return t;
        //return EVILCP(this, V, begin, end);
    }


    /**
     * ExpVector inverse graded lexicographical compareTo.
     *
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    @Override
    public int invGradCompareTo(ExpVector V) {
        int[] u = val;
        int[] v = ((ExpVectorInteger) V).val;
        int t = 0;
        int i;
        for (i = 0; i < u.length; i++) {
            if (u[i] > v[i]) {
                t = 1;
                break;
            }
            if (u[i] < v[i]) {
                t = -1;
                break;
            }
        }
        if (t == 0) {
            return t;
        }
        long up = 0;
        long vp = 0;
        for (int j = i; j < u.length; j++) {
            up += u[j];
            vp += v[j];
        }
        if (up > vp) {
            t = 1;
        } else {
            if (up < vp) {
                t = -1;
            }
        }
        return t;
        //return EVIGLC(this, V);
    }


    /**
     * ExpVector inverse graded lexicographical compareTo.
     *
     * @param V
     * @param begin
     * @param end
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    @Override
    public int invGradCompareTo(ExpVector V, int begin, int end) {
        int[] u = val;
        int[] v = ((ExpVectorInteger) V).val;
        int t = 0;
        int i;
        for (i = begin; i < end; i++) {
            if (u[i] > v[i]) {
                t = 1;
                break;
            }
            if (u[i] < v[i]) {
                t = -1;
                break;
            }
        }
        if (t == 0) {
            return t;
        }
        long up = 0;
        long vp = 0;
        for (int j = i; j < end; j++) {
            up += u[j];
            vp += v[j];
        }
        if (up > vp) {
            t = 1;
        } else {
            if (up < vp) {
                t = -1;
            }
        }
        return t;
        //return EVIGLC(this, V, begin, end);
    }


    /**
     * ExpVector reverse inverse lexicographical compareTo.
     *
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    @Override
    public int revInvLexCompareTo(ExpVector V) {
        int[] u = val;
        int[] v = ((ExpVectorInteger) V).val;
        int t = 0;
        for (int i = u.length - 1; i >= 0; i--) {
            if (u[i] > v[i])
                return 1;
            if (u[i] < v[i])
                return -1;
        }
        return t;
        //return EVRILCP(this, V);
    }


    /**
     * ExpVector reverse inverse lexicographical compareTo.
     *
     * @param V
     * @param begin
     * @param end
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    @Override
    public int revInvLexCompareTo(ExpVector V, int begin, int end) {
        int[] u = val;
        int[] v = ((ExpVectorInteger) V).val;
        int t = 0;
        for (int i = end - 1; i >= begin; i--) {
            if (u[i] > v[i])
                return 1;
            if (u[i] < v[i])
                return -1;
        }
        return t;
        //return EVRILCP(this, V, begin, end);
    }


    /**
     * ExpVector reverse inverse graded compareTo.
     *
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    @Override
    public int revInvGradCompareTo(ExpVector V) {
        int[] u = val;
        int[] v = ((ExpVectorInteger) V).val;
        int t = 0;
        int i;
        for (i = u.length - 1; i >= 0; i--) {
            if (u[i] > v[i]) {
                t = 1;
                break;
            }
            if (u[i] < v[i]) {
                t = -1;
                break;
            }
        }
        if (t == 0) {
            return t;
        }
        long up = 0;
        long vp = 0;
        for (int j = i; j >= 0; j--) {
            up += u[j];
            vp += v[j];
        }
        if (up > vp) {
            t = 1;
        } else {
            if (up < vp) {
                t = -1;
            }
        }
        return t;
        //return EVRIGLC(this, V);
    }


    /**
     * ExpVector reverse inverse graded compareTo.
     *
     * @param V
     * @param begin
     * @param end
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    @Override
    public int revInvGradCompareTo(ExpVector V, int begin, int end) {
        int[] u = val;
        int[] v = ((ExpVectorInteger) V).val;
        int t = 0;
        int i;
        for (i = end - 1; i >= begin; i--) {
            if (u[i] > v[i]) {
                t = 1;
                break;
            }
            if (u[i] < v[i]) {
                t = -1;
                break;
            }
        }
        if (t == 0) {
            return t;
        }
        long up = 0;
        long vp = 0;
        for (int j = i; j >= begin; j--) {
            up += u[j];
            vp += v[j];
        }
        if (up > vp) {
            t = 1;
        } else {
            if (up < vp) {
                t = -1;
            }
        }
        return t;
        //return EVRIGLC(this, V, begin, end);
    }


    /**
     * ExpVector inverse weighted lexicographical compareTo.
     *
     * @param w weight array.
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    @Override
    public int invWeightCompareTo(long[][] w, ExpVector V) {
        int[] u = val;
        int[] v = ((ExpVectorInteger) V).val;
        int t = 0;
        int i;
        for (i = 0; i < u.length; i++) {
            if (u[i] > v[i]) {
                t = 1;
                break;
            }
            if (u[i] < v[i]) {
                t = -1;
                break;
            }
        }
        if (t == 0) {
            return t;
        }
        for (long[] wk : w) {
            long up = 0;
            long vp = 0;
            for (int j = i; j < u.length; j++) {
                up += wk[j] * u[j];
                vp += wk[j] * v[j];
            }
            if (up > vp) {
                return 1;
            } else if (up < vp) {
                return -1;
            }
        }
        return t;
        //return EVIWLC(w, this, V);
    }
}
