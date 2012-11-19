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
 * the Free Software Foundation, either version 2 of the License, or
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

import java.util.*;

/**
 * Implementation of tensors set.
 * 
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class THashSet<T extends Tensor> implements Set<T> {

    private final HashSet<TensorWrapperWithEquals> set;

    public THashSet() {
        set = new HashSet<>();
    }

    public THashSet(int initialCapacity) {
        set = new HashSet<>(initialCapacity);
    }

    public THashSet(int initialCapacity, float loadFactor) {
        set = new HashSet<>(initialCapacity, loadFactor);
    }

    public THashSet(Collection<? extends Tensor> tensors) {
        List<TensorWrapperWithEquals> wrappers = new ArrayList<>(tensors.size());
        for (Tensor t : tensors)
            wrappers.add(new TensorWrapperWithEquals(t));
        set = new HashSet<>(wrappers);
    }

    @Override
    public boolean add(T e) {
        return set.add(new TensorWrapperWithEquals(e));
    }
    
    @Override
    public boolean addAll(Collection<? extends T> c) {
        List<TensorWrapperWithEquals> wrappers = new ArrayList<>(c.size());
        for (Tensor t : c)
            wrappers.add(new TensorWrapperWithEquals(t));
        return set.addAll(wrappers);
    }

    @Override
    public void clear() {
        set.clear();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Tensor)
            return set.contains(new TensorWrapperWithEquals((Tensor) o));
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

        private final Iterator<TensorWrapperWithEquals> iterator;

        public SetIterator(Iterator<TensorWrapperWithEquals> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() {
            return (T) iterator.next().tensor;
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<T> iterator() {
        return new SetIterator<T>(set.iterator());
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Tensor)
            return set.remove(new TensorWrapperWithEquals((Tensor) o));
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
        for (TensorWrapperWithEquals tw : set)
            a[++i] = tw.tensor;
        return a;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
         T[] r = a.length >= size() ? a
                : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size());
        int i = -1;
        for (TensorWrapperWithEquals tw : set)
            r[++i] = (T) tw.tensor;
        return r;
    }
}
