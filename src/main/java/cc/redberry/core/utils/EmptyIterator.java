/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
 * This class represents empty iterator instance. It is singleton, but has 
 * protected constructor, to provide inheritance.
 * 
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class EmptyIterator<T> implements Iterator<T> {
    /**
     * Empty iterator singleton instance.
     */
    public static final EmptyIterator INSTANCE = new EmptyIterator();

    protected EmptyIterator() {
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
