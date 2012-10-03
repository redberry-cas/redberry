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

import java.util.*;

public class ArrayContainer<E> implements List<E> {

    private final E[] elementData;

    public ArrayContainer(E[] array) {
        this.elementData = array;
    }

    @Override
    public int size() {
        return elementData.length;
    }

    @Override
    public boolean isEmpty() {
        return elementData.length == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public Object[] toArray() {
        return elementData.clone();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < elementData.length)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(elementData, elementData.length, a.getClass());
        System.arraycopy(elementData, 0, a, 0, elementData.length);
        if (a.length > elementData.length)
            a[elementData.length] = null;
        return a;
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>This implementation iterates over the specified collection, checking
     * each element returned by the iterator in turn to see if it's contained in
     * this collection. If all elements are so contained <tt>true</tt> is
     * returned, otherwise <tt>false</tt>.
     *
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @see #contains(Object)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E get(int index) {
        rangeCheck(index);
        return elementData[index];
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < elementData.length; i++)
                if (elementData[i] == null)
                    return i;
        } else
            for (int i = 0; i < elementData.length; i++)
                if (o.equals(elementData[i]))
                    return i;
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = elementData.length - 1; i >= 0; i--)
                if (elementData[i] == null)
                    return i;
        } else
            for (int i = elementData.length - 1; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        return -1;
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence), starting at the specified position in the list. The specified
     * index indicates the first element that would be returned by an initial
     * call to {@link ListIterator#next next}. An initial call to {@link ListIterator#previous previous}
     * would return the element with the specified index minus one.
     * <p/>
     * <p>The returned list iterator is <a
     * href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > elementData.length)
            throw new IndexOutOfBoundsException("Index: " + index);
        return new ListItr(index);
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence).
     * <p/>
     * <p>The returned list iterator is <a
     * href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @see #listIterator(int)
     */
    @Override
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, elementData.length);
        return new ArrayContainer<>(Arrays.copyOfRange(elementData, fromIndex, toIndex));
    }

    /**
     * Checks if the given index is in range. If not, throws an appropriate
     * runtime exception. This method does *not* check if the index is negative:
     * It is always used immediately prior to an array access, which throws an
     * ArrayIndexOutOfBoundsException if index is negative.
     */
    private void rangeCheck(int index) {
        if (index >= elementData.length)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * Constructs an IndexOutOfBoundsException detail message. Of the many
     * possible refactorings of the error handling code, this "outlining"
     * performs best with both server and client VMs.
     */
    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + elementData.length;
    }

    private static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex
                    + ") > toIndex(" + toIndex + ")");
    }

    /**
     * An optimized version of AbstractList.Itr
     */
    private class Itr implements Iterator<E> {

        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such

        @Override
        public boolean hasNext() {
            return cursor != elementData.length;
        }

        @SuppressWarnings("unchecked")
        @Override
        public E next() {
            int i = cursor;
            if (i >= elementData.length)
                throw new NoSuchElementException();
            Object[] elementData = ArrayContainer.this.elementData;
            cursor = i + 1;
            return (E) elementData[lastRet = i];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * An optimized version of AbstractList.ListItr
     */
    private class ListItr extends Itr implements ListIterator<E> {

        ListItr(int index) {
            super();
            cursor = index;
        }

        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @SuppressWarnings("unchecked")
        @Override
        public E previous() {
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = ArrayContainer.this.elementData;
            cursor = i;
            return (E) elementData[lastRet = i];
        }

        @Override
        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(E e) {
            throw new UnsupportedOperationException();
        }
    }
}
