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

import java.io.IOException;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class TestTool {
    private static final String[] GAP_PATH = {"gap", "/usr/local/bin/gap"};

    private GapGroupsInterface gap;
    private boolean gapIsInitialized;

    private TestTool() {
        initializeGap();
    }

    private void initializeGap() {
        if (!gapIsInitialized) {
            GapGroupsInterface gap = null;
            try {
                for (String path : GAP_PATH)
                    gap = new GapGroupsInterface(path);
            } catch (IOException e) {
            }
            this.gap = gap;
            gapIsInitialized = true;
        }
    }

    public boolean gapHasStarted() {
        return gap != null;
    }

    public static TestTool getTestTool() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static final TestTool INSTANCE = new TestTool();
    }

}
