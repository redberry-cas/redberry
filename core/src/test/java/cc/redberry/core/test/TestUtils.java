/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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
package cc.redberry.core.test;

import org.junit.Test;

import java.util.Objects;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class TestUtils {

    public static boolean doLongTests() {
        return Objects.equals(System.getProperty("longTests"), "") ||
                Objects.equals(System.getProperty("longTests"), "true") ||
                Objects.equals(System.getProperty("longTest"), "") ||
                Objects.equals(System.getProperty("longTest"), "true");
    }

    public static boolean doPerformanceTests() {
        return Objects.equals(System.getProperty("performanceTests"), "") ||
                Objects.equals(System.getProperty("performanceTests"), "true") ||
                Objects.equals(System.getProperty("longTest"), "") ||
                Objects.equals(System.getProperty("longTest"), "true");
    }


    public static int its(int shortTest, int longTest) {
        return doLongTests() ? longTest : shortTest;
    }

    public static long its(long shortTest, long longTest) {
        return doLongTests() ? longTest : shortTest;
    }


    @Test
    public void testLT() throws Exception {
        if (doLongTests())
            System.out.println("Long tests.");
        else
            System.out.println("Short tests.");
    }
}
