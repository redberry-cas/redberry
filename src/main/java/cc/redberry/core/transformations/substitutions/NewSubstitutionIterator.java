/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
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
import cc.redberry.core.tensor.iterator.*;
import cc.redberry.core.utils.ByteBackedBitArray;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.TCollections;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;

public final class NewSubstitutionIterator implements TreeIterator {
    private static final TIntSet EMPTY_INT_SET = TCollections.unmodifiableSet(new TIntHashSet(0));
    private final TreeTraverseIterator<ForbiddenContainer> innerIterator;
    //private ForbiddenContainer fc = null;

    public NewSubstitutionIterator(Tensor tensor) {
        this.innerIterator = new TreeTraverseIterator<>(tensor, new FCPayloadFactory());
    }

    @Override
    public Tensor next() {
        TraverseState nextState;
        Tensor tensor;

        while ((nextState = innerIterator.next()) == TraverseState.Entering) { //"Diving"
            /*tensor = innerIterator.current();
            if (fc == null || fc instanceof OpaqueFC) {
                if (tensor instanceof Product)
                    fc = new TopProductFC(fc, tensor);
            } else {
                if (tensor instanceof Sum)
                    fc = new SumFC(fc, tensor);
                else if (tensor instanceof Product)
                    fc = new ProductFC(fc, tensor);
                else if (tensor instanceof Power)
                    fc = new TransparentFC(fc);
                else if (tensor instanceof TensorField)
                    fc = new OpaqueFC(fc);
                else //Next state will be leaving
                    isSimpleTensor = true;*/
            //}
        }


        if (nextState == null)
            return null;


        //assert nextState == Leaving
        /*if (fc != null)

        {
            if (!isSimpleTensor || (fc instanceof OpaqueFC && innerIterator.current() instanceof TensorField)) {
                fc = fc.getParent();
            }
        } */

//        if (!isSimpleTensor &&
//                (!waitingForProduct ||
//                        (innerIterator.current() instanceof TensorField && fc != null))) {
//            fc = fc.getParent();
//        }

        /*ForbiddenContainer f = fc;

  do {
    System.out.println(((AbstractFC) f).currentBranch + " : " +
            f.getClass().getSimpleName() + " : " +
            ((AbstractFC) f).tensor);
  } while ((f = f.getParent()) != null);*/

        return innerIterator.current();
    }

    @Override
    public void set(Tensor tensor) {
        Tensor oldTensor = innerIterator.current();
        ForbiddenContainer fc = innerIterator.currentStackPosition().getPayload();

        if (!tensor.getIndices().getFree().equalsRegardlessOrder(tensor.getIndices().getFree()))
            throw new RuntimeException("Substitution with different free indices.");

        if (fc != null) {
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

    @Override
    public Tensor result() {
        return innerIterator.result();
    }

    @Override
    public int depth() {
        return innerIterator.depth();
    }

    public int[] getForbidden() {
        ForbiddenContainer fc = innerIterator.currentStackPosition().getPayload();
        if (fc == null)
            return new int[0];
        return fc.getForbidden().toArray();
    }

    private static interface ForbiddenContainer extends Payload<ForbiddenContainer> {
        TIntSet getForbidden();

        void submit(TIntSet removed, TIntSet added);

        void next();
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
                parent = null;
            else
                parent = previousPosition.getPayload();

            if (parent == null)
                parent = EMTY_CONTAINER;

            if (parent == EMTY_CONTAINER || parent instanceof OpaqueFC) {
                if (tensor instanceof Product)
                    return new TopProductFC(parent, tensor);
                if (parent == null)
                    return EMTY_CONTAINER;
                else
                    return new OpaqueFC(parent);
            }

            if (tensor instanceof Product)
                return new ProductFC(parent, tensor);
            if (tensor instanceof Sum)
                return new SumFC(parent, tensor);
            if (tensor instanceof Power)
                return new TransparentFC(parent);
            if (tensor instanceof TensorField)
                return new OpaqueFC(parent);
            return new TransparentFC(parent);
        }
    }

    private static abstract class AbstractFC extends DummyPayload<ForbiddenContainer> implements ForbiddenContainer {
        protected final ForbiddenContainer parent;
        protected final Tensor tensor;
        protected int currentBranch = 0;
        protected TIntSet forbidden = null;

        private AbstractFC(ForbiddenContainer parent, Tensor tensor) {
            this.parent = parent;
            this.tensor = tensor;
        }

        @Override
        public void next() {
            ++currentBranch;
        }

        public abstract void insureInitialized();

        @Override
        public TIntSet getForbidden() {
            insureInitialized();
            TIntHashSet result = new TIntHashSet(forbidden);
            result.removeAll(TensorUtils.getAllIndicesNamesT(tensor.get(currentBranch)));
            return result;
        }
    }

    private final static class ProductFC extends AbstractFC {
        private ProductFC(ForbiddenContainer parent, Tensor tensor) {
            super(parent, tensor);
        }

        @Override
        public void insureInitialized() {
            if (forbidden != null)
                return;

            forbidden = new TIntHashSet(parent.getForbidden());
            forbidden.addAll(TensorUtils.getAllIndicesNamesT(tensor));
        }

        @Override
        public void submit(TIntSet removed, TIntSet added) {
            insureInitialized();
            forbidden.addAll(added);
            forbidden.removeAll(removed);
            parent.submit(removed, added);
        }
    }

    private final static class SumFC extends AbstractFC {
        private int[] allDummyIndices;
        private ByteBackedBitArray[] usedArrays; //index index in allDummyIndices is index

        private SumFC(ForbiddenContainer parent, Tensor tensor) {
            super(parent, tensor);
        }

        public void insureInitialized() {
            if (forbidden != null)
                return;

            //Getting parent forbidden indices
            //The set of forbidden indices do not contain current sum
            //dummy indices (see getForbidden() e.g. for Product)
            forbidden = parent.getForbidden();

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
            usedArrays = new ByteBackedBitArray[allDummyIndices.length];
            for (i = allDummyIndices.length - 1; i >= 0; --i)
                usedArrays[i] = new ByteBackedBitArray(size);

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
            TIntSet parentRemoved = null, parentAdded;

            //Calculating really removed indices set
            TIntIterator iterator = removed.iterator();
            int iIndex, index;
            while (iterator.hasNext()) {
                iIndex = Arrays.binarySearch(allDummyIndices, index = iterator.next());
                usedArrays[iIndex].clear(currentBranch);

                if (usedArrays[iIndex].bitCount() == 0) {
                    if (parentRemoved == null)
                        parentRemoved = new TIntHashSet();
                    parentRemoved.add(index);
                }
            }
            if (parentRemoved == null)
                parentRemoved = EMPTY_INT_SET;

            //Processing added indices and calculating added set to
            //propagate to parent.
            parentAdded = new TIntHashSet(added);
            iterator = parentAdded.iterator();
            while (iterator.hasNext()) {
                //Searching index in initial dummy indices set
                iIndex = Arrays.binarySearch(allDummyIndices, index = iterator.next());

                //If this index is new for this sum it will never be removed,
                //so we don't need to store information about it.
                if (iIndex < 0)
                    continue;

                //If this index was already somewhere in the sum,
                //we don't have to propagate it to parent
                if (usedArrays[iIndex].bitCount() >= 0)
                    iterator.remove();

                //Marking this index as added to current summand
                usedArrays[iIndex].set(currentBranch);
            }

            //Propagating events to parent
            parent.submit(parentRemoved, parentAdded);
        }

        @Override
        public TIntSet getForbidden() {
            insureInitialized();
            return new TIntHashSet(forbidden);
        }
    }


    private final static class TopProductFC extends AbstractFC {
        private TopProductFC(ForbiddenContainer parent, Tensor tensor) {
            super(parent, tensor);
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

        @Override
        public void next() {
        }
    }

    private static final class OpaqueFC extends DummyPayload<ForbiddenContainer> implements ForbiddenContainer {
        private final ForbiddenContainer parent;

        private OpaqueFC(ForbiddenContainer parent) {
            this.parent = parent;
        }

        @Override
        public TIntSet getForbidden() {
            return EMPTY_INT_SET;
        }

        @Override
        public void submit(TIntSet removed, TIntSet added) {
        }

        @Override
        public void next() {
        }
    }

    ForbiddenContainer EMTY_CONTAINER = new ForbiddenContainer() {
        @Override
        public TIntSet getForbidden() {
            return EMPTY_INT_SET;
        }

        @Override
        public void submit(TIntSet removed, TIntSet added) {
        }

        @Override
        public void next() {
        }

        @Override
        public Tensor onLeaving(StackPosition<ForbiddenContainer> stackPosition) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    };
}