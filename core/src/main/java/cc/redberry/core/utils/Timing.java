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
package cc.redberry.core.utils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Timing {
    private Timing() {
    }

    public interface TimingJob<T> {
        T doJob();
    }

    private static Object[] timing(TimingJob job, boolean printMessage, long div, String pf) {
        long start = System.nanoTime();
        Object result = job.doJob();
        long elapsed = (System.nanoTime() - start) / div;
        if (printMessage)
            System.out.println("Timing: " + elapsed + pf);
        return new Object[]{elapsed, result};
    }

    public static Object[] timing(TimingJob job, boolean printMessage) {
        return timing(job, printMessage, 1000_000L, "ms");
    }

    public static Object[] microTiming(TimingJob job, boolean printMessage) {
        return timing(job, printMessage, 1000L, "µs");
    }

    public static Object[] nanoTiming(TimingJob job, boolean printMessage) {
        return timing(job, printMessage, 1L, "ns");
    }

    public static Object[] timing(TimingJob job) {
        return timing(job, true);
    }

    public static Object[] microTiming(TimingJob job) {
        return microTiming(job, true);
    }

    public static Object[] nanoTiming(TimingJob job) {
        return nanoTiming(job, true);
    }
}
