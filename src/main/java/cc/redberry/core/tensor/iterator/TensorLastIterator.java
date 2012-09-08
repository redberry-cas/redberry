package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;

/**
 * Wrapper for TreeTraverseIterator. Return only
 * <code>Entering</code> elements. Traverse from out.
 */
public final class TensorLastIterator extends TreeIteratorAbstract {

    public TensorLastIterator(Tensor tensor, TraverseGuide guide) {
        super(tensor, guide, TraverseState.Leaving);
    }

    public TensorLastIterator(Tensor tensor) {
        super(tensor, TraverseState.Leaving);
    }
}
