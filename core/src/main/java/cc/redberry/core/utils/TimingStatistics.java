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
package cc.redberry.core.utils;

import java.text.NumberFormat;
import java.util.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class TimingStatistics {
    final TreeMap<TransformationWithTimer, StatEntry> set = new TreeMap<>();

    public TimingStatistics() {}

    public void track(TransformationWithTimer... transformations) {
        track(Arrays.asList(transformations));
    }

    public void track(Collection<TransformationWithTimer> transformations) {
        for (TransformationWithTimer transformation : transformations) {
            set.put(transformation, EMPTY);
            transformation.reset();
        }
    }

    public void resetAll() {
        for (TransformationWithTimer tr : set.keySet())
            tr.reset();
    }

    public synchronized void merge(TimingStatistics stats) {
        for (Map.Entry<TransformationWithTimer, StatEntry> entry : stats.set.entrySet()) {
            final TransformationWithTimer tr = entry.getKey();
            final TransformationWithTimer in = set.floorKey(tr);
            if (in == null || in.compareTo(tr) != 0) {
                set.put(tr, entry.getValue());
                tr.reset();
            } else
                set.put(in, set.get(in).add(entry.getValue()));
        }
    }

    private void collectStatistics() {
        for (Map.Entry<TransformationWithTimer, StatEntry> entry : set.entrySet()) {
            final TransformationWithTimer tr = entry.getKey();
            set.put(tr, entry.getValue().add(tr.stats()));
            tr.reset();
        }
    }

    public static String toStringStatistics(TreeMap<TransformationWithTimer, StatEntry> data, long div, String dc) {
        String totalStr = "Total";
        int longestString = totalStr.length();
        for (TransformationWithTimer tr : data.keySet())
            longestString = Math.max(tr.toString().length(), longestString);
        StringBuilder sb = new StringBuilder();
        final Iterator<Map.Entry<TransformationWithTimer, StatEntry>> it = data.entrySet().iterator();
        long totalTiming = 0;
        long totalInvocations = 0;
        for (; it.hasNext(); ) {
            final Map.Entry<TransformationWithTimer, StatEntry> entry = it.next();
            String k = entry.getKey().toString();
            k = k.concat(emptyString(longestString - k.length()));
            final long timing = entry.getValue().elapsed / div;
            final long invocations = entry.getValue().invocations;
            totalTiming += timing;
            totalInvocations += invocations;
            sb.append(k).append(FORMAT.format(timing)).append(dc).append(" (").append(FORMAT.format(invocations)).append(" invocations)");
            sb.append("\n");
        }
        totalStr = totalStr.concat(emptyString(longestString - totalStr.length()));
        sb.append(totalStr).append(FORMAT.format(totalTiming)).append(dc).append(" (").append(FORMAT.format(totalInvocations)).append(" invocations)");
        return sb.toString();
    }

    private String toStringStatistics(long div, String dc) {
        collectStatistics();
        return toStringStatistics(set, div, dc);
    }

    public String toStringNanos() {
        return toStringStatistics(1, "ns");
    }

    public String toStringMicros() {
        return toStringStatistics(1_000, "us");
    }

    public String toStringMillis() {
        return toStringStatistics(1_000_000, "ms");
    }

    public String toStringSeconds() {
        return toStringStatistics(1_000_000_000, "s");
    }

    @Override
    public String toString() {
        return toStringMillis();
    }

    private static String emptyString(int length) {
        final char[] arr = new char[length + 3];
        Arrays.fill(arr, ' ');
        arr[length + 1] = ':';
        return new String(arr);
    }

    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance(Locale.US);
    private static final StatEntry EMPTY = new StatEntry(0, 0);

    public static final class StatEntry {
        public final long elapsed;
        public final long invocations;

        public StatEntry(long elapsed, long invocations) {
            this.elapsed = elapsed;
            this.invocations = invocations;
        }

        private StatEntry add(StatEntry oth) {
            return new StatEntry(elapsed + oth.elapsed, invocations + oth.invocations);
        }
    }
}
