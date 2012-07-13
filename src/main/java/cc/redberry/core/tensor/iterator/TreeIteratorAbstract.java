package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;

/*
 * Wrapper for TreeTraverseIterator. Traverse tensor into or out according to
 * mode params in next().
 */
abstract class TreeIteratorAbstract implements TreeIterator {

    private final TreeTraverseIterator iterator;
    private final TraverseState state;

    TreeIteratorAbstract(Tensor tensor, TraverseGuide guide, TraverseState state) {
        this.iterator = new TreeTraverseIterator(tensor, guide);
        this.state = state;
    }

    TreeIteratorAbstract(Tensor tensor, TraverseState state) {
        this.iterator = new TreeTraverseIterator(tensor);
        this.state = state;
    }

    @Override
    public int depth() {
        return iterator.depth();
    }

    @Override
    public Tensor next() {
        TraverseState nextState;
        while ((nextState = iterator.next()) != state && nextState != null);
        if (nextState == null)
            return null;
        return iterator.current();
    }

    @Override
    public Tensor result() {
        return iterator.result();
    }

    @Override
    public void set(Tensor tensor) {
        iterator.set(tensor);
    }
}
