package cc.redberry.core.utils;

import java.util.Iterator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class InfiniteLoopIterable<T> implements Iterable<T> {
    private final T[] array;

    public InfiniteLoopIterable(T[] array) {
        this.array = array;
    }

    @Override
    public Iterator<T> iterator() {
        return new InfiniteLoopIterator<T>(array);
    }
}
