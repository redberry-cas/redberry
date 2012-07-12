package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;

/*
* Wrapper for TreeTraverseIterator. Return only <blockquote>
*/
public class TensorFirstIterator {
    private final TreeTraverseIterator traverseIterator;

    public TensorFirstIterator(Tensor tensor, TraverseGuide guide) {
        traverseIterator = new TreeTraverseIterator(tensor, guide);
    }

    public TensorFirstIterator(Tensor tensor) {
        traverseIterator = new TreeTraverseIterator(tensor);
    }

    public Tensor next(){
        while(traverseIterator.next() == TraverseState.Leaving);
        return traverseIterator.current();
    }

    public void set(Tensor tensor){
         traverseIterator.set(tensor);
    }

    public int depth(){
        return traverseIterator.depth();
    }

    public Tensor current(){
        return traverseIterator.current();
    }

    public Tensor result(){
        return traverseIterator.result();
    }
}
