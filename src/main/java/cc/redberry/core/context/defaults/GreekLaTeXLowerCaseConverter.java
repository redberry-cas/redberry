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
package cc.redberry.core.context.defaults;

public final class GreekLaTeXLowerCaseConverter extends SymbolArrayConverter {
    private static final String[] symbols = new String[23];
    private static final String[] utf = new String[23];

    static {
        symbols[0] = "\\alpha";
        symbols[1] = "\\beta";
        symbols[2] = "\\gamma";
        symbols[3] = "\\delta";
        symbols[4] = "\\epsilon";
        symbols[5] = "\\zeta";
        symbols[6] = "\\eta";
        symbols[7] = "\\theta";
        symbols[8] = "\\iota";
        symbols[9] = "\\kappa";
        symbols[10] = "\\lambda";
        symbols[11] = "\\mu";
        symbols[12] = "\\nu";
        symbols[13] = "\\xi";
        //symbols[14] = "o";//\\omicron
        symbols[14] = "\\pi";
        symbols[15] = "\\rho";
        symbols[16] = "\\sigma";
        //symbols[17]= "final sigma??"
        symbols[17] = "\\tau";
        symbols[18] = "\\upsilon";
        symbols[19] = "\\phi";
        symbols[20] = "\\chi";
        symbols[21] = "\\psi";
        symbols[22] = "\\omega";
        for (int i = 0; i < 23; ++i) {
            char greekLetter;
            if (i >= 16)
                greekLetter = (char) ((char) 0x03b1 + i + 2);
            else if (i >= 14)
                greekLetter = (char) ((char) 0x03b1 + i + 1);
            else
                greekLetter = (char) ((char) 0x03b1 + i);
            utf[i] = Character.toString(greekLetter);
        }
    }
    
    public static final GreekLaTeXLowerCaseConverter INSTANCE = new GreekLaTeXLowerCaseConverter();

    private GreekLaTeXLowerCaseConverter() {
        super(symbols, utf);
    }

    private static final byte TYPE = 2;

    @Override
    public byte getType() {
        return TYPE;
    }
}
