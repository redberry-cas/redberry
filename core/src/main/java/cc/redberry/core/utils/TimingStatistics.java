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

import cc.redberry.core.transformations.Transformation;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TimingStatistics {
    final Set<WeakReference<TransformationWithTimer>> set = new HashSet<>();

    public TimingStatistics() {
    }

    public void collectStatistics(TransformationWithTimer transformation) {
        set.add(new WeakReference<>(transformation));
    }

    public void resetAll() {
        Iterator<WeakReference<TransformationWithTimer>> it = set.iterator();
        while (it.hasNext()) {
            TransformationWithTimer tr = it.next().get();
            if (tr == null)
                it.remove();
            else
                tr.resetTiming();
        }
    }

    private Map<Transformation, Long> getStatistics(long div) {
        Map<Transformation, Long> map = new HashMap<>(this.set.size());
        Iterator<WeakReference<TransformationWithTimer>> it = this.set.iterator();
        while (it.hasNext()) {
            WeakReference<TransformationWithTimer> next = it.next();
            TransformationWithTimer tr = next.get();
            if (tr == null)
                it.remove();
            else map.put(tr, tr.elapsedNanos() / div);
        }
        return map;
    }

    public Map<Transformation, Long> getStatisticsNanos() {
        return getStatistics(1L);
    }

    public Map<Transformation, Long> getStatisticsMicros() {
        return getStatistics(1000L);
    }

    public Map<Transformation, Long> getStatisticsMillis() {
        return getStatistics(1000_000L);
    }

    public Map<Transformation, Long> getStatisticsSeconds() {
        return getStatistics(1000_000_000L);
    }
}
