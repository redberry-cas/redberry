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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class BacktrackSearchPayload
        implements BacktrackSearchTestFunction {

    protected Permutation[] wordReference;

    void setWordReference(Permutation[] wordReference) {
        this.wordReference = wordReference;
    }

    public abstract void beforeLevelIncrement(int level);

    public abstract void afterLevelIncrement(int level);


    public static BacktrackSearchPayload createDefaultPayload(BacktrackSearchTestFunction test) {
        return new DefaultPayload(test);
    }

    private static final class DefaultPayload extends BacktrackSearchPayload {
        private final BacktrackSearchTestFunction test;

        public DefaultPayload(BacktrackSearchTestFunction test) {
            this.test = test;
        }

        @Override
        public void beforeLevelIncrement(int level) {
        }

        @Override
        public void afterLevelIncrement(int level) {
        }

        @Override
        public boolean test(Permutation permutation, int level) {
            return test.test(permutation, level);
        }
    }
}
