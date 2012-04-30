/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
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

import cc.redberry.core.context.Context;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ComplexElement implements FieldElement<ComplexElement> {
    protected final RationalElement real;
    protected final RationalElement imagine;
    public static final ComplexElement ONE = new ComplexElement(RationalElement.ONE, RationalElement.ZERO);
    public static final ComplexElement MINUSONE = new ComplexElement(RationalElement.ONE.negotiate(), RationalElement.ZERO);
    public static final ComplexElement ZERO = new ComplexElement(RationalElement.ZERO, RationalElement.ZERO);
    public static final ComplexElement IMAGE_ONE = new ComplexElement(RationalElement.ZERO, RationalElement.ONE);
    private final String imagineOne_String = "I";

    public static enum ComplexElementMode {
        COMPLEX,
        REAL,
        IMAGINE,
        ZERO
    };
    ComplexElementMode mode;

    public RationalElement getImagine() {
        return imagine;
    }

    public RationalElement getReal() {
        return real;
    }

    public ComplexElementMode getMode() {
        return mode;
    }

    public ComplexElement(RationalElement real, RationalElement imagine) {
        this.real = real;
        this.imagine = imagine;
        if (!imagine.isEquals(imagine.getField().getZero()) && !real.isEquals(real.getField().getZero()))
            mode = ComplexElementMode.COMPLEX;
        if (!imagine.isEquals(imagine.getField().getZero()) && real.isEquals(real.getField().getZero()))
            mode = ComplexElementMode.IMAGINE;
        if (imagine.isEquals(imagine.getField().getZero()) && !real.isEquals(real.getField().getZero()))
            mode = ComplexElementMode.REAL;
        if (imagine.isEquals(imagine.getField().getZero()) && real.isEquals(real.getField().getZero()))
            mode = ComplexElementMode.ZERO;
    }

    public ComplexElement(int real, int image) {
        this(new NumberSimple(real), new NumberSimple(image));
    }

    @Override
    public ComplexElement add(ComplexElement a) {
        return new ComplexElement(real.add(a.real), imagine.add(a.imagine));
    }

    @Override
    public ComplexElement subtract(ComplexElement a) {
        return new ComplexElement(real.subtract(a.real), imagine.subtract(a.imagine));
    }

    @Override
    public ComplexElement divide(ComplexElement a) throws ArithmeticException {
        RationalElement denom = (a.imagine.multiply(a.imagine)).add(a.real.multiply(a.real));
        RationalElement resultReal = (real.multiply(a.real).add(imagine.multiply(a.imagine))).divide(denom);
        RationalElement resultImaginary = imagine.multiply(a.real).subtract(real.multiply(a.imagine)).divide(denom);
        return new ComplexElement(resultReal, resultImaginary);
    }

    @Override
    public ComplexElement multiply(ComplexElement a) {
        return new ComplexElement(real.multiply(a.real).subtract(imagine.multiply(a.imagine)), real.multiply(a.imagine).add(imagine.multiply(a.real)));
    }

    @Override
    public ComplexElement negotiate() {
        return new ComplexElement(real.negotiate(), imagine.negotiate());
    }

    public RationalElement abs() {
        return (real.multiply(real).add(imagine.multiply(imagine)));
    }

    @Override
    public boolean isEquals(ComplexElement a) {
        return real.isEquals(a.real) && imagine.isEquals(a.imagine);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof ComplexElement))
            return false;
        return isEquals((ComplexElement) obj);
    }

    @Override
    public ComplexField getField() {
        return ComplexField.getInstance();
    }

    @Override
    public String toString() {
        switch (mode) {
            default:
            case COMPLEX:
                RationalElement imagineAbs;
                return real + (imagine.positive() ? "+" : "-") + ((imagineAbs = imagine.abs()).isEquals(imagine.getField().getOne()) ? "" : imagineAbs + "*") + imagineOne_String;
            case REAL:
                return real.toString();
            case IMAGINE:
                return imagineOne_String + (imagine.isEquals(imagine.getField().getOne()) ? "" : "*" + (imagine.isNegotive() ? "(" + imagine + ")" : imagine));
            case ZERO:
                return real.getField().getZero().toString();
        }
    }

    @Override
    public ComplexElement clone() {
        return new ComplexElement(real.clone(), imagine.clone());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + this.real.hashCode();
        hash = 17 * hash + this.imagine.hashCode();
        return hash;
    }

    public boolean isZero() {
        return real.isZero() && imagine.isZero();
    }

    public boolean isOne() {
        return real.isOne() && imagine.isZero();
    }

    public boolean isMinusOne() {
        return real.isMinusOne() && imagine.isZero();
    }
}
