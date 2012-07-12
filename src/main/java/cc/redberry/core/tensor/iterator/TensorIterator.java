package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;

/*
* Wrapper for TreeTraverseIterator. Return only <blockquote>
*/
public class TensorIterator {
    private final TreeTraverseIterator traverseIterator;
    private TraverseState traverseState = null;

    public TensorIterator(Tensor tensor, TraverseGuide guide) {
        traverseIterator = new TreeTraverseIterator(tensor, guide);
    }

    public TensorIterator(Tensor tensor) {
        traverseIterator = new TreeTraverseIterator(tensor);
    }

    public Tensor next(TraverseState mode) {
        while ((traverseState = traverseIterator.next()) == mode) ;
        return traverseState == null ? null : traverseIterator.current();
    }

    public void set(Tensor tensor) {
        traverseIterator.set(tensor);
    }

    public int depth() {
        return traverseIterator.depth();
    }

    public Tensor current() {
        return traverseState == null ? null : traverseIterator.current();
    }

    public Tensor result() {
        return traverseIterator.result();
    }
}
