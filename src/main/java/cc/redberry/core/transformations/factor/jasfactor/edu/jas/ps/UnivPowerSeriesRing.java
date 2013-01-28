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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.ps;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Univariate power series ring implementation. Uses lazy evaluated generating
 * function for coefficients.
 *
 * @param <C> ring element type
 * @author Heinz Kredel
 */

public class UnivPowerSeriesRing<C extends RingElem<C>> implements RingFactory<UnivPowerSeries<C>> {


    /**
     * A default random sequence generator.
     */
    protected final static Random random = new Random();


    /**
     * Default truncate.
     */
    public final static int DEFAULT_TRUNCATE = 11;


    /**
     * Truncate.
     */
    int truncate;

    /**
     * Variable name.
     */
    String var;


    /**
     * Coefficient ring factory.
     */
    public final RingFactory<C> coFac;


    /**
     * The constant power series 1 for this ring.
     */
    public final UnivPowerSeries<C> ONE;


    /**
     * The constant power series 0 for this ring.
     */
    public final UnivPowerSeries<C> ZERO;


    /**
     * No argument constructor.
     */
    @SuppressWarnings("unused")
    private UnivPowerSeriesRing() {
        throw new IllegalArgumentException("do not use no-argument constructor");
    }

    /**
     * Constructor.
     *
     * @param pfac polynomial ring factory.
     */
    public UnivPowerSeriesRing(GenPolynomialRing<C> pfac) {
        this(pfac.coFac, DEFAULT_TRUNCATE, pfac.getVars()[0]);
    }


    /**
     * Constructor.
     *
     * @param cofac    coefficient ring factory.
     * @param truncate index of truncation.
     * @param name     of the variable.
     */
    public UnivPowerSeriesRing(RingFactory<C> cofac, int truncate, String name) {
        this.coFac = cofac;
        this.truncate = truncate;
        this.var = name;
        this.ONE = new UnivPowerSeries<>(this, new Coefficients<C>() {


            @Override
            public C generate(int i) {
                if (i == 0) {
                    return coFac.getONE();
                }
                return coFac.getZERO();
            }
        });
        this.ZERO = new UnivPowerSeries<>(this, new Coefficients<C>() {


            @Override
            public C generate(int i) {
                return coFac.getZERO();
            }
        });
    }


    /**
     * To String.
     *
     * @return string representation of this.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String scf = coFac.getClass().getSimpleName();
        sb.append(scf).append("((").append(var).append("))");
        return sb.toString();
    }

    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object B) {
        UnivPowerSeriesRing<C> a = null;
        try {
            a = (UnivPowerSeriesRing<C>) B;
        } catch (ClassCastException ignored) {
        }
        if (a == null) {
            return false;
        }
        if (!coFac.equals(a.coFac)) {
            return false;
        }
        return var.equals(a.var);
    }


    /**
     * Hash code for this .
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int h = coFac.hashCode();
        h += (var.hashCode() << 27);
        h += truncate;
        return h;
    }


    /**
     * Get the zero element.
     *
     * @return 0 as UnivPowerSeries<C>.
     */
    public UnivPowerSeries<C> getZERO() {
        return ZERO;
    }


    /**
     * Get the one element.
     *
     * @return 1 as UnivPowerSeries<C>.
     */
    public UnivPowerSeries<C> getONE() {
        return ONE;
    }


    /**
     * Get a list of the generating elements.
     *
     * @return list of generators for the algebraic structure.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#generators()
     */
    public List<UnivPowerSeries<C>> generators() {
        List<C> rgens = coFac.generators();
        List<UnivPowerSeries<C>> gens = new ArrayList<>(rgens.size());
        for (final C cg : rgens) {
            UnivPowerSeries<C> g = new UnivPowerSeries<>(this, new Coefficients<C>() {


                @Override
                public C generate(int i) {
                    if (i == 0) {
                        return cg;
                    }
                    return coFac.getZERO();
                }
            });
            gens.add(g);
        }
        gens.add(ONE.shift(1));
        return gens;
    }


    /**
     * Is this structure finite or infinite.
     *
     * @return true if this structure is finite, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#isFinite()
     */
    public boolean isFinite() {
        return false;
    }

    /**
     * Is commutative.
     *
     * @return true, if this ring is commutative, else false.
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
     * Get a (constant) UnivPowerSeries&lt;C&gt; from a long value.
     *
     * @param a long.
     * @return a UnivPowerSeries&lt;C&gt;.
     */
    public UnivPowerSeries<C> fromInteger(long a) {
        return ONE.multiply(coFac.fromInteger(a));
    }


    /**
     * Get a (constant) UnivPowerSeries&lt;C&gt; from a java.math.BigInteger.
     *
     * @param a BigInteger.
     * @return a UnivPowerSeries&lt;C&gt;.
     */
    public UnivPowerSeries<C> fromInteger(java.math.BigInteger a) {
        return ONE.multiply(coFac.fromInteger(a));
    }

    /**
     * Generate a random power series with k = 5, d = 0.7.
     *
     * @return a random power series.
     */
    public UnivPowerSeries<C> random() {
        return random(5, 0.7f, random);
    }


    /**
     * Generate a random power series with d = 0.7.
     *
     * @param k bitsize of random coefficients.
     * @return a random power series.
     */
    public UnivPowerSeries<C> random(int k) {
        return random(k, 0.7f, random);
    }


    /**
     * Generate a random power series with d = 0.7.
     *
     * @param k   bit-size of random coefficients.
     * @param rnd is a source for random bits.
     * @return a random power series.
     */
    public UnivPowerSeries<C> random(int k, Random rnd) {
        return random(k, 0.7f, rnd);
    }


    /**
     * Generate a random power series.
     *
     * @param k bit-size of random coefficients.
     * @param d density of non-zero coefficients.
     * @return a random power series.
     */
    public UnivPowerSeries<C> random(int k, float d) {
        return random(k, d, random);
    }


    /**
     * Generate a random power series.
     *
     * @param k   bit-size of random coefficients.
     * @param d   density of non-zero coefficients.
     * @param rnd is a source for random bits.
     * @return a random power series.
     */
    public UnivPowerSeries<C> random(final int k, final float d, final Random rnd) {
        return new UnivPowerSeries<>(this, new Coefficients<C>() {


            @Override
            public C generate(int i) {
                // cached coefficients returned by get
                C c;
                float f = rnd.nextFloat();
                if (f < d) {
                    c = coFac.random(k, rnd);
                } else {
                    c = coFac.getZERO();
                }
                return c;
            }
        });
    }


    /**
     * Copy power series.
     *
     * @param c a power series.
     * @return a copy of c.
     */
    public UnivPowerSeries<C> copy(UnivPowerSeries<C> c) {
        return new UnivPowerSeries<>(this, c.lazyCoeffs);
    }

    /**
     * Taylor power series.
     *
     * @param f function.
     * @param a expansion point.
     * @return Taylor series of f.
     */
    public UnivPowerSeries<C> seriesOfTaylor(final TaylorFunction<C> f, final C a) {
        return new UnivPowerSeries<>(this, new Coefficients<C>() {


            TaylorFunction<C> der = f;


            long k = 0;


            long n = 1;


            @Override
            public C generate(int i) {
                C c;
                if (i == 0) {
                    c = der.evaluate(a);
                    der = der.deriviative();
                    return c;
                }
                if (i > 0) {
                    c = get(i - 1); // ensure deriv is updated
                }
                k++;
                n *= k;
                c = der.evaluate(a);
                c = c.divide(coFac.fromInteger(n));
                der = der.deriviative();
                return c;
            }
        });
    }

}
