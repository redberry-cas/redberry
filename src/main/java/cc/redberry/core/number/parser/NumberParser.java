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
package cc.redberry.core.number.parser;

import cc.redberry.core.number.Complex;
import cc.redberry.core.number.Rational;
import cc.redberry.core.number.Real;

/**
 *
 * @author Stanislav Poslavsky
 */
public class NumberParser<T extends cc.redberry.core.number.Number<T>> {

    @SuppressWarnings("unchecked")
    private static final TokenParser<Complex>[] ComplexTokens = new TokenParser[]{
        BracketToken.INSTANCE,
        new OperatorToken<Complex>('+', '-') {

            @Override
            protected Complex neutral() {
                return Complex.ZERO;
            }

            @Override
            protected Complex operation(Complex c1, Complex c2, boolean mode) {
                if (mode)
                    return c1.subtract(c2);
                return c1.add(c2);
            }
        },
        new OperatorToken<Complex>('*', '/') {

            @Override
            protected Complex neutral() {
                return Complex.ONE;
            }

            @Override
            protected Complex operation(Complex c1, Complex c2, boolean mode) {
                if (mode)
                    return c1.divide(c2);
                return c1.multiply(c2);
            }
        },
        ComplexToken.INSTANCE
    };
    @SuppressWarnings("unchecked")
    private static final TokenParser<Real>[] RealTokens = new TokenParser[]{
        BracketToken.INSTANCE,
        new OperatorToken<Real>('+', '-') {

            @Override
            protected Real neutral() {
                return Rational.ZERO;
            }

            @Override
            protected Real operation(Real c1, Real c2, boolean mode) {
                if (mode)
                    return c1.subtract(c2);
                return c1.add(c2);
            }
        },
        new OperatorToken<Real>('*', '/') {

            @Override
            protected Real neutral() {
                return Rational.ONE;
            }

            @Override
            protected Real operation(Real c1, Real c2, boolean mode) {
                if (mode)
                    return c1.divide(c2);
                return c1.multiply(c2);
            }
        },
        RealToken.INSTANCE
    };
    public static final NumberParser<Real> REAL_PARSER = new NumberParser<>(RealTokens);
    public static final NumberParser<Complex> COMPLEX_PARSER = new NumberParser<>(ComplexTokens);
    private final TokenParser<T>[] parsers;

    private NumberParser(TokenParser<T>[] parsers) {
        this.parsers = parsers;
    }

    public T parse(String expression) {
        for (TokenParser parser : parsers) {
            @SuppressWarnings("unchecked") T element = (T) parser.parse(expression, this);
            if (element == null)
                continue;
            return element;
        }
        throw new NumberFormatException();
    }
}
