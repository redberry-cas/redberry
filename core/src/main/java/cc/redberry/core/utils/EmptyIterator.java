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

import java.util.Iterator;

/**
 * Singleton empty iterator.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class EmptyIterator<T> implements Iterator<T> {
    /**
     * Empty iterator singleton instance.
     */
    public static final EmptyIterator INSTANCE = new EmptyIterator();

    private EmptyIterator() {
    }

    /**
     * Returns false.
     *
     * @return false
     */
    @Override
    public boolean hasNext() {
        return false;
    }

    /**
     * Throws {@code IllegalStateException}.
     *
     * @return throws {@code IllegalStateException}
     * @throws {@code IllegalStateException} always
     */
    @Override
    public T next() {
        throw new IllegalStateException();
    }

    /**
     * Throws {@code IllegalStateException}.
     *
     * @throws {@code IllegalStateException} always
     */
    @Override
    public void remove() {
        throw new IllegalStateException();
    }
}
