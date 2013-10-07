/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
 *   Stanislav Poslavsky   <stvlpos@mail.ru>
 *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
 *
 * This file is part of Redberry.
 *
 * Redberry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Redberry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Redberry. If not, see <http://www.gnu.org/licenses/>.
 */

package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.ScalarFunction;
import cc.redberry.core.tensor.iterator.*;
import cc.redberry.core.utils.BitArray;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.TCollections;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class SubstitutionIterator implements TreeIterator {
    private static final TIntSet EMPTY_INT_SET = TCollections.unmodifiableSet(new TIntHashSet(0));
    private final TreeTraverseIterator<ForbiddenContainer> innerIterator;

    public SubstitutionIterator(Tensor tensor) {
        this.innerIterator = new TreeTraverseIterator<>(tensor, new FCPayloadFactory());
    }

    public SubstitutionIterator(Tensor tensor, TraverseGuide traverseGuide) {
        this.innerIterator = new TreeTraverseIterator<>(tensor, traverseGuide, new FCPayloadFactory());
    }

    @Override
    public Tensor next() {
        TraverseState nextState;

        while ((nextState = innerIterator.next()) == TraverseState.Entering) ;
        if (nextState == null)
            return null;

        return innerIterator.current();
    }

    public void unsafeSet(Tensor tensor) {
        innerIterator.set(tensor);
    }

    @Override
    public void set(Tensor tensor) {
        Tensor oldTensor = innerIterator.current();
        if (oldTensor == tensor)
            return;
        if (TensorUtils.isZeroOrIndeterminate(tensor) || TensorUtils.isSymbolic(tensor)) {
            innerIterator.set(tensor);
            return;
        }

        if (!tensor.getIndices().getFree().equalsRegardlessOrder(oldTensor.getIndices().getFree()))
            throw new RuntimeException("Substitution with different free indices.");

        StackPosition<ForbiddenContainer> previous = innerIterator.currentStackPosition().previous();
        if (previous != null) {
            ForbiddenContainer fc = previous.getPayload();
            TIntHashSet oldDummyIndices = TensorUtils.getAllDummyIndicesT(oldTensor);
            TIntHashSet newDummyIndices = TensorUtils.getAllDummyIndicesT(tensor);

            TIntHashSet removed = new TIntHashSet(oldDummyIndices),
                    added = new TIntHashSet(newDummyIndices);

            removed.removeAll(newDummyIndices);
            added.removeAll(oldDummyIndices);

            fc.submit(removed, added);
        }
        innerIterator.set(tensor);
    }

    public void safeSet(Tensor tensor) {
        if (innerIterator.current() != tensor)
            set(ApplyIndexMapping.renameDummy(tensor, getForbidden()));
    }

    public boolean isCurrentModified() {
        return innerIterator.currentStackPosition().isModified();
    }

    @Override
    public Tensor result() {
        return innerIterator.result();
    }

    @Override
    public int depth() {
        return innerIterator.depth();
    }

    public int[] getForbidden() {
        StackPosition<ForbiddenContainer> previous = innerIterator.currentStackPosition().previous();
        if (previous == null)
            return new int[0];
        return previous.getPayload().getForbidden().toArray();
//        ForbiddenContainer fc = innerIterator.currentStackPosition().getPayload();
//        if (fc == null)
//            return new int[0];
//        return fc.getForbidden().toArray();
    }

    private static interface ForbiddenContainer extends Payload<ForbiddenContainer> {
        TIntSet getForbidden();

        void submit(TIntSet removed, TIntSet added);
    }

    private class FCPayloadFactory implements PayloadFactory<ForbiddenContainer> {
        @Override
        public boolean allowLazyInitialization() {
            return true;
        }

        @Override
        public ForbiddenContainer create(StackPosition<ForbiddenContainer> stackPosition) {
            Tensor tensor = stackPosition.getInitialTensor();
            StackPosition<ForbiddenContainer> previousPosition = stackPosition.previous();
            ForbiddenContainer parent;
            if (previousPosition == null)
                parent = EMPTY_CONTAINER;
            else
                parent = previousPosition.getPayload();

            if (parent == EMPTY_CONTAINER) {
                if (tensor instanceof Product)
                    return new TopProductFC(stackPosition);
                return EMPTY_CONTAINER;
            }

            if (tensor instanceof Product)
                return new ProductFC(stackPosition);
            if (tensor instanceof Sum)
                return new SumFC(stackPosition);
            if (tensor instanceof TensorField)
                return EMPTY_CONTAINER;
            if (tensor instanceof ScalarFunction)
                return scalarFunctionContainer;
            return new TransparentFC(parent);
        }
    }

    private static abstract class AbstractFC extends DummyPayload<ForbiddenContainer> implements ForbiddenContainer {
        protected final StackPosition<ForbiddenContainer> position;
        protected TIntSet forbidden = null;
        protected final Tensor tensor;

        private AbstractFC(StackPosition<ForbiddenContainer> position) {
            this.position = position;
            this.tensor = position.getInitialTensor();
        }

        public abstract void insureInitialized();

        @Override
        public TIntSet getForbidden() {
            insureInitialized();
            TIntHashSet result = new TIntHashSet(forbidden);
//            result.removeAll(TensorUtils.getAllIndicesNamesT(position.tensor.get(currentBranch)));
            result.removeAll(TensorUtils.getAllIndicesNamesT(tensor.get(position.currentIndex())));
            return result;
        }
    }

    private final static class ProductFC extends AbstractFC {
        private ProductFC(StackPosition<ForbiddenContainer> position) {
            super(position);
        }

        @Override
        public void insureInitialized() {
            if (forbidden != null)
                return;

            forbidden = new TIntHashSet(position.previous().getPayload().getForbidden());
            forbidden.addAll(TensorUtils.getAllIndicesNamesT(tensor));
        }

        @Override
        public void submit(TIntSet removed, TIntSet added) {
            insureInitialized();
            forbidden.addAll(added);
            forbidden.removeAll(removed);
            position.previous().getPayload().submit(removed, added);
        }
    }

    private final static class SumFC extends AbstractFC {
        private int[] allDummyIndices;
        private BitArray[] usedArrays; //index index in allDummyIndices is index

        private SumFC(StackPosition<ForbiddenContainer> position) {
            super(position);
        }

        public void insureInitialized() {
            if (forbidden != null)
                return;

            //Getting position forbidden indices
            //The set of forbidden indices do not contain current sum
            //dummy indices (see getForbidden() e.g. for Product)
            forbidden = position.previous().getPayload().getForbidden();

            //All dummy indices in this sum
            TIntHashSet allDummyIndicesT = TensorUtils.getAllDummyIndicesT(tensor);

            //Creating array to index individual indices origin
            allDummyIndices = allDummyIndicesT.toArray();
            Arrays.sort(allDummyIndices);

            //For performance
            final int size = tensor.size();

            TIntHashSet dummy;
            int i;

            //Allocating origins arrays
            usedArrays = new BitArray[allDummyIndices.length];
            for (i = allDummyIndices.length - 1; i >= 0; --i)
                usedArrays[i] = new BitArray(size);

            //Full-filling origins array
            for (i = size - 1; i >= 0; --i) {
                dummy = TensorUtils.getAllDummyIndicesT(tensor.get(i));
                TIntIterator iterator = dummy.iterator();

                while (iterator.hasNext())
                    usedArrays[Arrays.binarySearch(allDummyIndices, iterator.next())].set(i);
            }
        }

        @Override
        public void submit(TIntSet removed, TIntSet added) {
            insureInitialized();
            TIntSet parentRemoved = null, parentAdded;
            //Calculating really removed indices set
            TIntIterator iterator = removed.iterator();
            int iIndex, index;
            while (iterator.hasNext()) {
                iIndex = Arrays.binarySearch(allDummyIndices, index = iterator.next());
                usedArrays[iIndex].clear(position.currentIndex());

                if (usedArrays[iIndex].bitCount() == 0) {
                    if (parentRemoved == null)
                        parentRemoved = new TIntHashSet(removed.size());
                    parentRemoved.add(index);
                }
            }
            if (parentRemoved == null)
                parentRemoved = EMPTY_INT_SET;

            //Processing added indices and calculating added set to
            //propagate to position.
            parentAdded = new TIntHashSet(added);
            iterator = parentAdded.iterator();
            while (iterator.hasNext()) {
                //Searching index in initial dummy indices set
                iIndex = Arrays.binarySearch(allDummyIndices, iterator.next());

                //If this index is new for this sum it will never be removed,
                //so we don't need to store information about it.
                if (iIndex < 0)
                    continue;

                //If this index was already somewhere in the sum,
                //we don't have to propagate it to position
//                if (usedArrays[iIndex].bitCount() >= 0)
//                    iterator.remove();

                //Marking this index as added to current summand
                usedArrays[iIndex].set(position.currentIndex());
            }

            //Propagating events to position
            position.previous().getPayload().submit(parentRemoved, parentAdded);
        }

        @Override
        public TIntSet getForbidden() {
            insureInitialized();
            return new TIntHashSet(forbidden);
        }
    }


    private final static class TopProductFC extends AbstractFC {
        private TopProductFC(StackPosition<ForbiddenContainer> position) {
            super(position);
        }

        @Override
        public void insureInitialized() {
            if (forbidden != null)
                return;
            forbidden = TensorUtils.getAllIndicesNamesT(tensor);
        }

        @Override
        public void submit(TIntSet removed, TIntSet added) {
            insureInitialized();
            forbidden.addAll(added);
            forbidden.removeAll(removed);
        }
    }

    private static final class TransparentFC extends DummyPayload<ForbiddenContainer> implements ForbiddenContainer {
        private final ForbiddenContainer parent;

        private TransparentFC(ForbiddenContainer parent) {
            this.parent = parent;
        }

        @Override
        public TIntSet getForbidden() {
            return parent.getForbidden();
        }

        @Override
        public void submit(TIntSet removed, TIntSet added) {
            parent.submit(removed, added);
        }
    }

    private static final ForbiddenContainer scalarFunctionContainer = new ForbiddenContainer() {
        @Override
        public TIntSet getForbidden() {
            return EMPTY_INT_SET;
        }

        @Override
        public void submit(TIntSet removed, TIntSet added) {
        }

        @Override
        public Tensor onLeaving(StackPosition<ForbiddenContainer> stackPosition) {
            if (!stackPosition.isModified())
                return null;
            StackPosition<ForbiddenContainer> prev = stackPosition.previous();
            if (prev == null)
                return null;
            Tensor tensor = stackPosition.getTensor();
            tensor = ApplyIndexMapping.renameDummy(tensor, prev.getPayload().getForbidden().toArray());
            prev.getPayload().submit(EMPTY_INT_SET, TensorUtils.getAllIndicesNamesT(tensor));
            return tensor;
        }
    };

    private static final ForbiddenContainer EMPTY_CONTAINER = new ForbiddenContainer() {
        @Override
        public TIntSet getForbidden() {
            return EMPTY_INT_SET;
        }

        @Override
        public void submit(TIntSet removed, TIntSet added) {
        }

        @Override
        public Tensor onLeaving(StackPosition<ForbiddenContainer> stackPosition) {
            return null;
        }
    };

}