package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;

/*
* Wrapper for TreeTraverseIterator.
* Return only <blockquote>Entering</blockquote> elements.
* Traverse from out.
*/
public class TensorLastIterator extends TensorIterator {
    public TensorLastIterator(Tensor tensor) {
        super(tensor);
    }

    public TensorLastIterator(Tensor tensor, TraverseGuide guide) {
        super(tensor, guide);
    }

    public Tensor next(){
        return super.next(TraverseState.Entering);
    }
}
