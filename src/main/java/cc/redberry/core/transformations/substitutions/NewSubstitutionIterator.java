package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.TraverseState;
import cc.redberry.core.tensor.iterator.TreeIterator;
import cc.redberry.core.tensor.iterator.TreeTraverseIterator;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

public class NewSubstitutionIterator implements TreeIterator {
    TreeTraverseIterator innerIterator;
    private ForbiddenCounter fc = null;

    @Override
    public Tensor next() {
        TraverseState nextState;
        while ((nextState = innerIterator.next()) != TraverseState.Leaving
                && nextState != null) {
            //"Diving"

        }
        if (nextState == null)
            return null;
        return innerIterator.current();
    }

    @Override
    public void set(Tensor tensor) {

    }

    @Override
    public Tensor result() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int depth() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private static interface ForbiddenCounter {
        TIntHashSet getForbidden();

        void submitRemoved(int[] removed);

        void submitAdded(int[] added);
    }

    private static abstract class AbstractFC implements ForbiddenCounter {
        protected final ForbiddenCounter parent;
        protected final Tensor tensor;
        protected TIntHashSet forbidden = null;

        private AbstractFC(ForbiddenCounter parent, Tensor tensor) {
            this.parent = parent;
            this.tensor = tensor;
        }

        public abstract void calculateForbidden();

        @Override
        public TIntHashSet getForbidden() {
            return forbidden;
        }

        @Override
        public void submitRemoved(int[] removed) {
            if (forbidden == null)
                calculateForbidden();
            forbidden.removeAll(removed);
            if (parent != null)
                parent.submitRemoved(removed);
        }

        @Override
        public void submitAdded(int[] added) {
            if (forbidden == null)
                calculateForbidden();
            forbidden.addAll(added);
            if (parent != null)
                parent.submitAdded(added);
        }
    }

    private final static class TopProductFC extends AbstractFC {
        private TopProductFC(ForbiddenCounter parent, Tensor tensor) {
            super(parent, tensor);
        }

        @Override
        public void calculateForbidden() {
            forbidden = TensorUtils.getAllIndicesNamesT(tensor);
        }
    }

    private final static class SimpleFC extends AbstractFC {
        private SimpleFC(ForbiddenCounter parent, Tensor tensor) {
            super(parent, tensor);
        }

        @Override
        public void calculateForbidden() {
            forbidden = new TIntHashSet(parent.getForbidden());
            forbidden.removeAll(TensorUtils.getAllIndicesNamesT(tensor));
        }
    }
}