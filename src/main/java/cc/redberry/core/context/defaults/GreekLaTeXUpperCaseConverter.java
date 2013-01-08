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

public final class GreekLaTeXUpperCaseConverter extends SymbolArrayConverter {
    private static final String[] symbols = new String[11];
    private static final String[] utf = new String[11];

    static {
        symbols[0] = "\\Gamma";
        symbols[1] = "\\Delta";
        symbols[2] = "\\Theta";
        symbols[3] = "\\Lambda";
        symbols[4] = "\\Xi";
        symbols[5] = "\\Pi";
        symbols[6] = "\\Sigma";
        symbols[7] = "\\Upsilon";
        symbols[8] = "\\Phi";
        symbols[9] = "\\Psi";
        symbols[10] = "\\Omega";

        utf[0] = Character.toString((char) 0x0393);
        utf[1] = Character.toString((char) 0x0394);
        utf[2] = Character.toString((char) 0x0398);
        utf[3] = Character.toString((char) 0x039B);
        utf[4] = Character.toString((char) 0x039E);
        utf[5] = Character.toString((char) 0x03A0);
        utf[6] = Character.toString((char) 0x03A3);
        utf[7] = Character.toString((char) 0x03A5);
        utf[8] = Character.toString((char) 0x03A6);
        utf[9] = Character.toString((char) 0x03A8);
        utf[10] = Character.toString((char) 0x03A9);

    }

    /*static {
            symbols[0] = "\\Alpha";
            symbols[1] = "\\Beta";
            symbols[2] = "\\Gamma";
            symbols[3] = "\\Delta";
            symbols[4] = "\\Epsilon";
            symbols[5] = "\\Zeta";
            symbols[6] = "\\Eta";
            symbols[7] = "\\Theta";
            symbols[8] = "\\Iota";
            symbols[9] = "\\Kappa";
            symbols[10] = "\\Lambda";
            symbols[11] = "\\Mu";
            symbols[12] = "\\Nu";
            symbols[13] = "\\Xi";
            symbols[14] = "\\Omicron";
            symbols[15] = "\\Pi";
            symbols[16] = "\\Rho";
            symbols[17] = "\\Sigma";
            symbols[18] = "\\Tau";
            symbols[19] = "\\Upsilon";
            symbols[20] = "\\Phi";
            symbols[21] = "\\Chi";
            symbols[22] = "\\Psi";
            symbols[23] = "\\Omega";
            for (int i = 0; i < 24; ++i) {
                char greekLetter;
                if (i >= 17)
                    greekLetter = (char) ((char) 0x0391 + i + 1);
                else
                    greekLetter = (char) ((char) 0x0391 + i);
                utf[i] = Character.toString(greekLetter);
            }
        }*/

    public static final GreekLaTeXUpperCaseConverter INSTANCE = new GreekLaTeXUpperCaseConverter();

    private GreekLaTeXUpperCaseConverter() {
        super(symbols, utf);
    }

    private static final byte TYPE = 3;

    @Override
    public byte getType() {
        return TYPE;
    }
}
