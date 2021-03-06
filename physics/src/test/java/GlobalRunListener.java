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


import cc.redberry.core.context.CC;
import cc.redberry.core.context.ContextManager;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class GlobalRunListener extends RunListener {

    public GlobalRunListener() {
    }

    @Override
    public void testStarted(Description description) throws Exception {
        ContextManager.initializeNew();
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        System.out.println("Test " + failure.getTestHeader() + " failed with name manager seed: " + CC.getNameManager().getSeed());
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        System.out.println("###IGNORED: " + description.getDisplayName());
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        System.out.println("###IGNORED: " + failure.getTestHeader());
    }
}
