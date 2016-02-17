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
    final Set<WeakReference<TransformationWithTimer>> set;
    final Comparator<Transformation> comparator;

    public TimingStatistics() {
        this.set = new HashSet<>();
        this.comparator = null;
    }

    public TimingStatistics(final Comparator<Transformation> comp) {
        this.comparator = comp;
        this.set = new TreeSet<>(new Comparator<WeakReference<TransformationWithTimer>>() {
            @Override
            public int compare(WeakReference<TransformationWithTimer> o1, WeakReference<TransformationWithTimer> o2) {
                TransformationWithTimer t1 = o1.get(), t2 = o2.get();
                if (t1 == null || t2 == null)
                    return 1;
                return comp.compare(t1, t2);
            }
        });
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

    public synchronized void merge(TimingStatistics stats) {
        merge(stats, comparator);
    }

    public synchronized void merge(TimingStatistics stats, Comparator<Transformation> comparator) {
        out:
        for (WeakReference<TransformationWithTimer> oth : stats.set) {
            TransformationWithTimer othTr = oth.get();
            if (othTr == null)
                continue;
            for (WeakReference<TransformationWithTimer> reference : set) {
                TransformationWithTimer tr = reference.get();
                if (tr != null)
                    if (comparator.compare(tr, othTr) == 0) {
                        tr.incrementNanos(othTr.elapsedNanos());
                        continue out;
                    }
            }
            collectStatistics(othTr);
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
