package cc.redberry.core.number;

public final class ComplexUtils {
    private ComplexUtils() {
    }

    /**
     * Numeric sine form complex number.
     *
     * @param complex argument
     * @return sinus
     */
    public static Complex sin(Complex complex) {
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).sin());
    }


    /**
     * Numeric cosine form complex number.
     *
     * @param complex argument
     * @return cosine
     */
    public static Complex cos(Complex complex) {
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).cos());
    }


    /**
     * Numeric tangent form complex number.
     *
     * @param complex argument
     * @return tangent
     */
    public static Complex tan(Complex complex) {
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).tan());
    }


    /**
     * Numeric cotangent form complex number.
     *
     * @param complex argument
     * @return cotangent
     */
    public static Complex cot(Complex complex) {
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).tan().reciprocal());
    }

    /**
     * Numeric arcsine form complex number.
     *
     * @param complex argument
     * @return arcsine
     */
    public static Complex arcsin(Complex complex) {
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).asin());
    }

    /**
     * Numeric arccosine form complex number.
     *
     * @param complex argument
     * @return arccosine
     */
    public static Complex arccos(Complex complex) {
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).acos());
    }

    /**
     * Numeric arctangent form complex number.
     *
     * @param complex argument
     * @return arctangent
     */
    public static Complex arctan(Complex complex) {
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).atan());
    }

    /**
     * Numeric arccotangent form complex number.
     *
     * @param complex argument
     * @return arccotangent
     */
    public static Complex arccot(Complex complex) {
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).reciprocal().atan());
    }

    /**
     * Numeric natural logarithm form complex number.
     *
     * @param complex argument
     * @return natural logarithm
     */
    public static Complex log(Complex complex) {
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).log());
    }


    /**
     * Numeric exponent form complex number.
     *
     * @param complex argument
     * @return exponent
     */
    public static Complex exp(Complex complex) {
        return new Complex(new org.apache.commons.math3.complex.Complex(complex.getReal().doubleValue(), complex.getImaginary().doubleValue()).exp());
    }
}
