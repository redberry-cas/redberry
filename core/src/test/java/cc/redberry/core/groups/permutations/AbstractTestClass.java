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
package cc.redberry.core.groups.permutations;

import cc.redberry.core.utils.ArraysUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class AbstractTestClass {
    @Rule
    public TestName name = new TestName();

    public GapGroupsInterface getGapInterface() {
        return getStaticInstance();
    }

    @Before
    public void beforeMethod() {
        if (name.getMethodName().toLowerCase().contains("withgap"))
            Assume.assumeTrue(getGapInterface() != null);
    }

    private static GapGroupsInterface gapStaticInstance = null;

    private static boolean gapStaticInstanceInitialized = false;

    private static GapGroupsInterface getStaticInstance() {
        if (gapStaticInstanceInitialized)
            return gapStaticInstance;
        gapStaticInstanceInitialized = true;
        String dGapPath = System.getProperty("gapPath");
        String[] gapSearchPath = {
                "/usr/local/bin/gap", "/bin/gap", "/usr/bin/gap"};
        if (dGapPath != null)
            gapSearchPath = ArraysUtils.addAll(gapSearchPath, dGapPath);

        GapGroupsInterface gapInterface = null;
        for (String s : gapSearchPath) {
            try {
                gapInterface = new GapGroupsInterface(s);
                break;
            } catch (IOException e) {
            }
        }
        gapStaticInstance = gapInterface;
        if (gapInterface != null)
            System.out.println("[Redberry] GAP system started.");
        else
            System.out.println("[Redberry] no GAP found on the system.");
        return gapStaticInstance;
    }
}
