/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
 *   Stanislav Poslavsky   <stvlpos@mail.ru>
 *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
 *
 * This file is part of Redberry.
 *
 * Redberry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Redberry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Redberry. If not, see <http://www.gnu.org/licenses/>.
 */
package cc.redberry.core.number;

import org.apache.commons.math3.util.FastMath;

public final class ComplexUtils {
    private ComplexUtils() {
    }

    /**
     * Numerical sine.
     *
     * @param complex argument
     * @return sine
     */
    public static Complex sin(Complex complex) {
        if (complex.isReal())
            return new Complex(FastMath.sin(complex.getReal().doubleValue()));
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).sin());
    }


    /**
     * Numerical cosine.
     *
     * @param complex argument
     * @return cosine
     */
    public static Complex cos(Complex complex) {
        if (complex.isReal())
            return new Complex(FastMath.cos(complex.getReal().doubleValue()));
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).cos());
    }


    /**
     * Numerical tangent.
     *
     * @param complex argument
     * @return tangent
     */
    public static Complex tan(Complex complex) {
        if (complex.isReal())
            return new Complex(FastMath.tan(complex.getReal().doubleValue()));
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).tan());
    }


    /**
     * Numerical cotangent.
     *
     * @param complex argument
     * @return cotangent
     */
    public static Complex cot(Complex complex) {
        if (complex.isReal())
            return new Complex(1 / FastMath.tan(complex.getReal().doubleValue()));
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).tan().reciprocal());
    }

    /**
     * Numerical arcsine.
     *
     * @param complex argument
     * @return arcsine
     */
    public static Complex arcsin(Complex complex) {
        if (complex.isReal()) {
            double x = complex.getReal().doubleValue();
            if (x <= 1.0 && x >= -1)
                return new Complex(FastMath.asin(complex.getReal().doubleValue()));
        }
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).asin());
    }

    /**
     * Numerical arccosine.
     *
     * @param complex argument
     * @return arccosine
     */
    public static Complex arccos(Complex complex) {
        if (complex.isReal()) {
            double x = complex.getReal().doubleValue();
            if (x <= 1.0 && x >= -1)
                return new Complex(FastMath.acos(complex.getReal().doubleValue()));
        }
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).acos());
    }

    /**
     * Numerical arctangent.
     *
     * @param complex argument
     * @return arctangent
     */
    public static Complex arctan(Complex complex) {
        if (complex.isReal())
            return new Complex(FastMath.atan(complex.getReal().doubleValue()));
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).atan());
    }

    /**
     * Numerical arccotangent.
     *
     * @param complex argument
     * @return arccotangent
     */
    public static Complex arccot(Complex complex) {
        if (complex.isReal())
            return new Complex(FastMath.atan(1 / complex.getReal().doubleValue()));
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).reciprocal().atan());
    }

    /**
     * Numerical natural logarithm.
     *
     * @param complex argument
     * @return natural logarithm
     */
    public static Complex log(Complex complex) {
        if (complex.isReal()) {
            double x = complex.getReal().doubleValue();
            if (x >= 0)
                return new Complex(FastMath.log(complex.getReal().doubleValue()));
        }
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).log());
    }


    /**
     * Numerical exponent.
     *
     * @param complex argument
     * @return exponent
     */
    public static Complex exp(Complex complex) {
        if (complex.isReal())
            return new Complex(FastMath.exp(complex.getReal().doubleValue()));
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).exp());
    }
}
