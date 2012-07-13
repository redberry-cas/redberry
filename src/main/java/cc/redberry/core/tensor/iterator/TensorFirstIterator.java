package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;

/*
*
*/
public final class TensorFirstIterator extends TreeIteratorImpl {
    private final TreeTraverseIterator traverseIterator;

    public TensorFirstIterator(Tensor tensor, TraverseGuide guide) {
        traverseIterator = new TreeTraverseIterator(tensor, guide);
    }

    public TensorFirstIterator(Tensor tensor) {
        traverseIterator = new TreeTraverseIterator(tensor);
    }

    public void next(){

    }
}
