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
package cc.redberry.core.number.parser;

import cc.redberry.core.number.Complex;

import java.math.BigInteger;

/**
 * @author Stanislav Poslavsky
 */
public class ComplexToken implements TokenParser<Complex> {
    public static final ComplexToken INSTANCE = new ComplexToken();

    private ComplexToken() {
    }

    @Override
    public Complex parse(String expression, NumberParser<Complex> parser) {
        if (expression.equals("I"))
            return Complex.IMAGINARY_UNIT;
        try {
            return new Complex(new BigInteger(expression));
        } catch (NumberFormatException ignored) {
        }
        try {
            return new Complex(Double.parseDouble(expression));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
