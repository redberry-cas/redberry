package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;

/*
*
*/
public class TensorFirstIterator {
    private final TreeTraverseIterator traverseIterator;

    public TensorFirstIterator(Tensor tensor, TraverseGuide guide) {
        traverseIterator = new TreeTraverseIterator(tensor, guide);
    }

    public TensorFirstIterator(Tensor tensor) {
        traverseIterator = new TreeTraverseIterator(tensor);
    }
}
