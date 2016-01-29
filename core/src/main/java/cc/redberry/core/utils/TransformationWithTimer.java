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

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class TransformationWithTimer implements TransformationToStringAble {
    public final Transformation transformation;
    private volatile long elapsedTime = 0;

    public TransformationWithTimer(Transformation transformation) {
        this.transformation = transformation;
    }

    public long elapsedNanos() {
        return elapsedTime;
    }

    public long elapsedMicros() {
        return elapsedTime / 1000L;
    }

    public long elapsedMillis() {
        return elapsedTime / 1000_000L;
    }

    public long elapsedSeconds() {
        return elapsedTime / 1000_000_000L;
    }

    public long elapsedMinutes() {
        return elapsedTime / 60_000_000_000L;
    }

    public void resetTiming() { elapsedTime = 0;}

    @Override
    public Tensor transform(Tensor t) {
        long start = System.nanoTime();
        Tensor r = transformation.transform(t);
        elapsedTime += System.nanoTime() - start;
        return r;
    }

    @Override
    public String toString() {
        return this.toString(CC.getDefaultOutputFormat());
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        if (transformation instanceof TransformationToStringAble)
            return ((TransformationToStringAble) transformation).toString(outputFormat);
        else return transformation.toString();
    }
}
