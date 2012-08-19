/*
 * org.redberry.concurrent: high-level Java concurrent library.
 * Copyright (c) 2010-2012.
 * Bolotin Dmitriy <bolotin.dmitriy@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 */

package cc.redberry.core.utils;

import java.util.Iterator;

public final class ArrayIterator<T> implements Iterator<T> {
    private final T[] array;
    private int i = -1;

    public ArrayIterator(T[] array) {
        this.array = array;
    }

    @Override
    public boolean hasNext() {
        return i < array.length - 1;
    }

    @Override
    public T next() {
        return array[++i];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
