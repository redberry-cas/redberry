package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;

/**
 * Wrapper for TreeTraverseIterator. Return only
 * <code>Leaving</code> elements. Traverse into.
 */
public final class TensorFirstIterator extends TreeIteratorAbstract {

    public TensorFirstIterator(Tensor tensor, TraverseGuide guide) {
        super(tensor, guide, TraverseState.Entering);
    }

    public TensorFirstIterator(Tensor tensor) {
        super(tensor, TraverseState.Entering);
    }
    
}
