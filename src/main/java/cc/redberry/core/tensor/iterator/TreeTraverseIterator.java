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
 * the Free Software Foundation, either version 2 of the License, or
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
package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorBuilder;
import cc.redberry.core.tensor.TensorWrapper;
import cc.redberry.core.utils.Indicator;

/**
 * An iterator for tensors that allows the programmer to traverse the tensor
 * tree structure, modify the tensor during iteration, and obtain information
 * about iterator's current position in the tensor. A {@code TensorTreeIterator}
 * has current element, so all methods are defined in terms of the cursor
 * position. *<p>Example: <blockquote><pre>
 *      Tensor tensor = Tensors.parse("Cos[a+b+Sin[x]]");
 *      TensorIterator iterator = new TreeTraverseIterator(tensor);
 *      TraverseState state;
 *      while ((state = iterator.next()) != null)
 *           System.out.println(state + " " + iterator.depth() + " " + iterator.current());
 * </pre></blockquote> This code will print: <blockquote><pre>
 *    Entering   Cos[a+b+Sin[y*c+x]]
 *    Entering   a+b+Sin[y*c+x]
 *    Entering   a
 *    Leaving   a
 *    Entering   b
 *    Leaving   b
 *    Entering   Sin[y*c+x]
 *    Entering   y*c+x
 *    Entering   y*c
 *    Entering   y
 *    Leaving   y
 *    Entering   c
 *    Leaving   c
 *    Leaving   y*c
 *    Entering   x
 *    Leaving   x
 *    Leaving   y*c+x
 *    Leaving   Sin[y*c+x]
 *    Leaving   a+b+Sin[y*c+x]
 *    Leaving   Cos[a+b+Sin[y*c+x]]
 * </pre></blockquote> </p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class TreeTraverseIterator<T extends Payload<T>> {

    private final TraverseGuide iterationGuide;
    private LinkedPointer currentPointer;
    private TraverseState lastState;
    private Tensor current = null;
    private final PayloadFactory<T> payloadFactory;

    public TreeTraverseIterator(Tensor tensor, TraverseGuide guide, PayloadFactory<T> payloadFactory) {
        currentPointer = new LinkedPointer(null, TensorWrapper.wrap(tensor), true);
        iterationGuide = guide;
        this.payloadFactory = payloadFactory;
    }

    public TreeTraverseIterator(Tensor tensor, TraverseGuide guide) {
        this(tensor, guide, null);
    }

    public TreeTraverseIterator(Tensor tensor) {
        this(tensor, TraverseGuide.ALL);
    }

    public TreeTraverseIterator(Tensor tensor, PayloadFactory<T> payloadFactory) {
        this(tensor, TraverseGuide.ALL, payloadFactory);
    }

    /**
     * Moves iterator to the next state.
     *
     * @return next traverse state or {@code null} if there is no next element
     */
    public TraverseState next() {

        if (lastState == TraverseState.Leaving) {

            Tensor cur = null;
            if (currentPointer.payload != null)
                cur = currentPointer.payload.onLeaving(currentPointer);

            if (cur != null)
                current = cur;

            currentPointer = currentPointer.previous;
            currentPointer.set(current);
        }

        Tensor next;
        while (true) {
            next = currentPointer.next();
            if (next == null) {
                if (currentPointer.previous == null)
                    return lastState = null;

                current = currentPointer.getTensor();

                return lastState = TraverseState.Leaving;
            } else {
                TraversePermission permission = iterationGuide.getPermission(next, currentPointer.tensor, currentPointer.position - 1);

                if (permission == null)
                    throw new NullPointerException();

                if (permission == TraversePermission.DontShow)
                    continue;

                current = next;
                currentPointer = new LinkedPointer(currentPointer, next, permission == TraversePermission.Enter);
                return lastState = TraverseState.Entering;
            }
        }
    }

    /**
     * Replaces the current cursor with the specified element.
     *
     * @param tensor the element with which to replace the current cursor
     */
    public void set(Tensor tensor) {
        if (current == tensor)
            return;
        if (tensor == null)
            throw new NullPointerException();

        current = currentPointer.tensor = tensor;

        lastState = TraverseState.Leaving;

        /*if (lastState == TraverseState.Entering)
            //currentPointer.previous.set(tensor);
            currentPointer = new LinkedPointer(currentPointer.previous, tensor, false);
        else if (lastState == TraverseState.Leaving)
            currentPointer.set(tensor);*/
    }

    /**
     * Returns depth in the tree, relatively to the current cursor position.
     * Note that depth is counted from zero (e.g. zero will be returned after
     * first next()). If next() never called or last next() was null , this
     * method returns -1.
     *
     * @return depth in the tree relatively to the current cursor position
     */
    public int depth() {
        return currentPointer.getDepth();
    }

    public boolean isUnder(Indicator<Tensor> indicator, int searchDepth) {
        return currentPointer.isUnder(indicator, searchDepth);
    }

    /**
     * Checks specified condition at position specified by relative level to
     * current cursor.
     *
     * @param indicator level relative position of element to be tested
     * @return
     */
    public boolean checkLevel(Indicator<Tensor> indicator, int level)//TODO better name
    {
        StackPosition s = currentPointer.previous(level);
        if (s == null)
            return false;
        return indicator.is(s.getInitialTensor());
    }

    //    public void levelUp(int levels) {
    //        if (levels == 0)
    //            return;
    //        LinkedPointer currentPointer = null;
    //        if (lastState == TraverseState.Entering)
    //            currentPointer = this.currentPointer.previous;
    //        else if (lastState == TraverseState.Leaving)
    //            currentPointer = this.currentPointer;
    //        while (--levels >= 0 && currentPointer != null)
    //            currentPointer = currentPointer.previous;
    //        this.currentPointer = currentPointer;
    //    }

    /**
     * Returns current cursor.
     *
     * @return current cursor.
     */
    public Tensor current() {
        return current;
    }

    /**
     * Return the resulting tensor.
     *
     * @return the resulting tensor
     */
    public Tensor result() {
        if (currentPointer.previous != null)
            throw new RuntimeException("Iteration not finished.");
        return currentPointer.getTensor().get(0);
    }

    public StackPosition<T> currentStackPosition() {
        return currentPointer;
    }

    private final class LinkedPointer implements StackPosition<T> {

        int position = 0;
        Tensor tensor;
        Tensor current = null;
        Tensor toSet = null;
        TensorBuilder builder = null;
        final LinkedPointer previous;
        boolean isModified = false;
        T payload = null;

        public LinkedPointer(LinkedPointer pair, Tensor tensor, boolean goInside) {
            this.tensor = tensor;
            if (!goInside)
                position = Integer.MAX_VALUE;
            this.previous = pair;
            if (previous != null && payloadFactory != null && !payloadFactory.allowLazyInitialization()) {
                this.payload = payloadFactory.create(this);
                if (this.payload == null)
                    throw new NullPointerException("Payload factory returned null payload.");
            }
        }

        Tensor next() {
            if (toSet != null) {
                if (builder == null) {
                    builder = tensor.getBuilder();
                    for (int i = 0; i < position - 1; ++i)
                        builder.put(tensor.get(i));
                }
                builder.put(toSet);
                toSet = null;
            } else if (builder != null)
                builder.put(current);

            if (position >= tensor.size())
                return current = null;
            return current = tensor.get(position++);
        }

        @Override
        public Tensor getTensor() {
            if (builder != null)
                if (position != tensor.size())
                    throw new IllegalStateException("Iteration not finished.");
                else {
                    tensor = builder.build();
                    position = Integer.MAX_VALUE;
                    builder = null;
                }
            return tensor;
        }

        @Override
        public Tensor getInitialTensor() {
            if (position == Integer.MAX_VALUE)
                throw new IllegalStateException("Initial tensor was rebuilt.");
            return tensor;
        }

        @Override
        public boolean isModified() {
            return isModified;
        }

        @SuppressWarnings("unchecked")
        @Override
        public StackPosition previous() {
            if (previous.previous == null)
                return null;
            return previous;
        }

        @Override
        public T getPayload() {
            if (payloadFactory == null || previous == null)
                return null;
            if (payload == null) {
                payload = payloadFactory.create(this);
                if (this.payload == null)
                    throw new NullPointerException("Payload factory returned null payload.");
            }
            return payload;
        }

        void setModified() {
            isModified = true;
            if (previous != null)
                previous.setModified();
        }

        /*
        * void close() { position = tensor.size(); }
        */
        void set(Tensor t) {
            if (current == t)
                return;
            toSet = t;
            setModified();
        }

        @Override
        public int getDepth() {
            int depth = -2;
            LinkedPointer pointer = this;
            while (pointer != null) {
                pointer = pointer.previous;
                ++depth;
            }
            return depth;
        }

        @Override
        public boolean isUnder(Indicator<Tensor> indicator, int searchDepth) {
            LinkedPointer pointer = this;
            do {
                if (indicator.is(pointer.tensor))
                    return true;
                pointer = pointer.previous;
            } while (pointer != null && searchDepth-- > 0);
            return false;
        }

        @Override
        public StackPosition<T> previous(int level) {
            LinkedPointer pointer = this;
            while (pointer != null && level-- > 0)
                pointer = pointer.previous;
            return pointer;
        }

        @Override
        public int currentIndex() {
            return position - 1;
        }
    }
}
