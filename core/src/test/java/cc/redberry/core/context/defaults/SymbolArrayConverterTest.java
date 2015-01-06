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
package cc.redberry.core.context.defaults;

import cc.redberry.core.context.OutputFormat;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SymbolArrayConverterTest {
    @Test
    public void test1() throws Exception {
        String[] symbols = new String[11];
        String[] utf = new String[11];
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

        SymbolArrayConverter converter = GreekLaTeXUpperCaseConverter.INSTANCE;
        for (int i = 0; i < symbols.length; ++i) {
            Assert.assertTrue(converter.applicableToSymbol(symbols[i]));
            Assert.assertEquals(converter.getCode(symbols[i]), i);
            Assert.assertEquals(converter.getSymbol(i, OutputFormat.LaTeX), symbols[i]);
            Assert.assertEquals(converter.getSymbol(i, OutputFormat.UTF8), utf[i]);
        }
    }

    @Test
    public void test2() throws Exception {
        String[] symbols = new String[23];
        String[] utf = new String[23];

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


        SymbolArrayConverter converter = GreekLaTeXLowerCaseConverter.INSTANCE;
        for (int i = 0; i < symbols.length; ++i) {
            Assert.assertTrue(converter.applicableToSymbol(symbols[i]));
            Assert.assertEquals(converter.getCode(symbols[i]), i);
            Assert.assertEquals(converter.getSymbol(i, OutputFormat.LaTeX), symbols[i]);
            Assert.assertEquals(converter.getSymbol(i, OutputFormat.UTF8), utf[i]);
        }
    }
}
