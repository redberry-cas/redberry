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
package cc.redberry.core.utils;

import cc.redberry.core.tensor.Tensor;
import java.lang.reflect.*;
import java.util.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorsSet<T extends Tensor> implements Set<T> {

    private final HashSet<TensorWrapper> set;

    public TensorsSet() {
        set = new HashSet<>();
    }

    public TensorsSet(int initialCapacity) {
        set = new HashSet<>(initialCapacity);
    }

    public TensorsSet(int initialCapacity, float loadFactor) {
        set = new HashSet<>(initialCapacity, loadFactor);
    }

    public TensorsSet(Collection<? extends Tensor> tensors) {
        List<TensorWrapper> wrappers = new ArrayList<>(tensors.size());
        for (Tensor t : tensors)
            wrappers.add(new TensorWrapper(t));
        set = new HashSet<>(wrappers);
    }

    @Override
    public boolean add(T e) {
        return set.add(new TensorWrapper(e));
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        List<TensorWrapper> wrappers = new ArrayList<>(c.size());
        for (Tensor t : c)
            wrappers.add(new TensorWrapper(t));
        return set.addAll(wrappers);
    }

    @Override
    public void clear() {
        set.clear();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Tensor)
            return set.contains(new TensorWrapper((Tensor) o));
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c)
            if (!contains(o))
                return false;
        return true;
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    private final class SetIterator<T> implements Iterator<T> {

        private final Iterator<TensorWrapper> iterator;

        public SetIterator(Iterator<TensorWrapper> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            return (T) iterator.next().tensor;
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new SetIterator<>(set.iterator());
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Tensor)
            return set.remove(new TensorWrapper((Tensor) o));
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean b = false;
        for (Object o : c)
            if (remove(o))
                b = true;
        return b;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public Object[] toArray() {
        Object[] a = new Object[size()];
        int i = -1;
        for (TensorWrapper tw : set)
            a[++i] = tw.tensor;
        return a;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        T[] r = a.length >= size() ? a
                : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size());
        int i = -1;
        for (TensorWrapper tw : set)
            r[++i] = (T) tw.tensor;
        return r;
    }

    private static class TensorWrapper {

        private final Tensor tensor;

        public TensorWrapper(Tensor tensor) {
            this.tensor = tensor;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final TensorWrapper other = (TensorWrapper) obj;
            return TensorUtils.equals(tensor, other.tensor);
        }

        @Override
        public int hashCode() {
            return tensor.hashCode();
        }
    }
}
