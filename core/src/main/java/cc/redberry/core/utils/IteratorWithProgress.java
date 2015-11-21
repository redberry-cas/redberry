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

import java.util.Iterator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IteratorWithProgress<E> implements Iterator<E> {
    protected final Iterator<E> innerIterator;
    protected final long totalCount;
    protected final Consumer out;

    public IteratorWithProgress(Iterator<E> innerIterator, long totalCount, Consumer out) {
        this.innerIterator = innerIterator;
        this.totalCount = totalCount;
        this.out = out;
    }

    @Override
    public boolean hasNext() {
        return innerIterator.hasNext();
    }

    @Override
    public void remove() {
        innerIterator.remove();
    }

    protected int prevPercent = -1;
    protected long currentPosition = 0;

    @Override
    public E next() {
        ++currentPosition;
        E next = innerIterator.next();
        int percent = (int) (100.0 * currentPosition / totalCount);
        if (percent != prevPercent) {
            out.consume(percent);
            prevPercent = percent;
        }
        return next;
    }

    public interface Consumer {
        void consume(int a);
    }
}
