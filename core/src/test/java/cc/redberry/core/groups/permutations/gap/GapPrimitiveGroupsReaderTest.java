/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
package cc.redberry.core.groups.permutations.gap;

import cc.redberry.core.groups.permutations.PermutationGroup;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class GapPrimitiveGroupsReaderTest {

    @Test
    public void test1() throws Exception {

        PermutationGroup[] groups = GapPrimitiveGroupsReader.readGroupsFromGap("/home/stas/gap4r6/prim/grps/gps1.g");
        for (PermutationGroup group : groups) {
            System.out.println(group.degree() + "   " + group.order());
        }
    }

    @Test
    public void testGAP() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("gap", "-b", "-q");
        Process process = processBuilder.start();


        PrintStream ps = new PrintStream(process.getOutputStream());
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);


        ps.println("12/3;");
        ps.flush();

        char[] charBuffer = new char[1024];
        int n;
        do {
            n = isr.read(charBuffer);
        } while (n == 0);

        System.out.println(String.valueOf(charBuffer, 0, n));
        ps.println("15/3;");
        ps.flush();

        do {
            n = isr.read(charBuffer);
        } while (n == 0);

        System.out.println(String.valueOf(charBuffer, 0, n));

        ps.close();
        process.waitFor();
    }
}
