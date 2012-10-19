package cc.redberry.core.utils;

import java.util.Iterator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class InfiniteLoopIterator<T> implements Iterator<T> {
    private final T[] array;
    private int pointer = 0;

    public InfiniteLoopIterator(T[] array) {
        this.array = array;
    }

    /**
     * @return true
     */
    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public T next() {
        if (pointer == array.length)
            pointer = 0;
        return array[pointer++];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
