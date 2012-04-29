/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
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
package cc.redberry.concurrent;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ConcurrentGrowingList<T> {
    private final AtomicReference<AtomicReferenceArray<T>> array =
            new AtomicReference<>(new AtomicReferenceArray<T>(3));

    public ConcurrentGrowingList() {
    }

    public GrowingIterator iterator() {
        return new GrowingIterator();
    }

    public class GrowingIterator {
        int position = -1;

        private GrowingIterator() {
        }

        public T next() {
            return array.get().get(++position);
        }

        public T set(T t) {
            AtomicReferenceArray<T> _array = array.get();
            if (position == _array.length() - 1) {
                T[] newInstance = (T[]) new Object[3 * (position + 1) / 2 + 1];
                for (int i = 0; i <= position; ++i)
                    newInstance[i] = _array.get(i);
                AtomicReferenceArray<T> newArray = new AtomicReferenceArray<>(newInstance);

                if (!array.compareAndSet(_array, newArray))
                    _array = array.get();
                else
                    _array = newArray;
            }
            return _array.compareAndSet(position, null, t) ? null : _array.get(position);
        }
    }
}
