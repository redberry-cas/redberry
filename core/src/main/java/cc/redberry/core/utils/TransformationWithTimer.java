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

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationToStringAble;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class TransformationWithTimer implements TransformationToStringAble, Comparable<TransformationWithTimer> {
    public final Transformation transformation;
    public final String name;
    public final AtomicLong invocations = new AtomicLong(0);
    public final AtomicLong elapsedTime = new AtomicLong(0);

    public TransformationWithTimer(Transformation transformation) {
        this(transformation, null);
    }

    public TransformationWithTimer(Transformation tr, String name) {
        this.transformation = tr instanceof TransformationWithTimer ? ((TransformationWithTimer) tr).transformation : tr;
        this.name = name;
    }

    public long invocations() {
        return invocations.get();
    }

    public long elapsed() {
        return elapsedTime.get();
    }

    public long elapsedNanos() {
        return elapsed();
    }

    public long elapsedMicros() {
        return elapsedNanos() / 1000L;
    }

    public long elapsedMillis() {
        return elapsedNanos() / 1000_000L;
    }

    public long elapsedSeconds() {
        return elapsedNanos() / 1000_000_000L;
    }

    public long elapsedMinutes() {
        return elapsedNanos() / 60_000_000_000L;
    }

    public void resetTiming() { elapsedTime.set(0);}

    public void resetInvocations() { invocations.set(0);}

    public void reset() {resetTiming(); resetInvocations();}

    public void incrementNanos(final long amount) { elapsedTime.addAndGet(amount);}

    @Override
    public Tensor transform(Tensor t) {
        long start = System.nanoTime();
        Tensor r = transformation.transform(t);
        elapsedTime.addAndGet(System.nanoTime() - start);
        invocations.incrementAndGet();
        return r;
    }

    @Override
    public int compareTo(TransformationWithTimer o) {
        if (this == o)
            return 0;
        return toString().compareTo(o.toString());
    }

    @Override
    public String toString() {
        return this.toString(CC.getDefaultOutputFormat());
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        if (name != null)
            return name;
        if (transformation instanceof TransformationToStringAble)
            return ((TransformationToStringAble) transformation).toString(outputFormat);
        else return transformation.toString();
    }

    TimingStatistics.StatEntry stats() {
        return new TimingStatistics.StatEntry(elapsed(), invocations());
    }
}
