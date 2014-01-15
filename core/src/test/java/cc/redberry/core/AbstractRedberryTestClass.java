/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
package cc.redberry.core;

import cc.redberry.core.groups.permutations.GapGroupsInterface;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class AbstractRedberryTestClass {

    @Rule
    public TestName name = new TestName();

    @Before
    public void beforeMethod() {
        if (name.getMethodName().toLowerCase().contains("performancetest"))
            Assume.assumeTrue(doTestPerformance());
        if (name.getMethodName().toLowerCase().contains("longtest"))
            Assume.assumeTrue(doLongTest());
    }

    private static Boolean testPerformance;

    protected static boolean doTestPerformance() {
        if (testPerformance == null)
            testPerformance = Boolean.valueOf(System.getProperty("testPerformance"));
        return testPerformance.booleanValue();
    }

    private static Boolean doLongTest = null;

    protected boolean doLongTest() {
        if (doLongTest != null)
            return doLongTest.booleanValue();
        return doLongTest = Boolean.valueOf(System.getProperty("longTest"));
    }
}
