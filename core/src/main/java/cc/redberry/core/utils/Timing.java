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
package cc.redberry.core.utils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Timing {
    private Timing() {
    }

    public static interface TimingJob<T> {
        T doJob();
    }

    public static Object[] timing(TimingJob job, boolean printMessage) {
        long start = System.currentTimeMillis();
        Object result = job.doJob();
        long elapsed = System.currentTimeMillis() - start;
        if (printMessage)
            System.out.println("Timing: " + elapsed + "ms");
        return new Object[]{elapsed, result};
    }

    public static Object[] timing(TimingJob job) {
        return timing(job, true);
    }
}
