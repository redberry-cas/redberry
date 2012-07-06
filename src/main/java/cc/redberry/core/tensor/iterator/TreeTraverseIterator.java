/*
 * Redberry: symbolic current computations.
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
package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorBuilder;
import cc.redberry.core.tensor.TensorWrapper;

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
public final class TreeTraverseIterator {

    private final TraverseGuide iterationGuide;
    private LinkedPointer currentPointer;
    private TraverseState lastState;
    private Tensor current = null;

    public TreeTraverseIterator(Tensor tensor, TraverseGuide guide) {
        currentPointer = new LinkedPointer(null, TensorWrapper.wrap(tensor), true);
        iterationGuide = guide;
    }

    public TreeTraverseIterator(Tensor tensor) {
        this(tensor, TraverseGuide.ALL);
    }

    /**
     *
     * @return next traverse state and null if there is no next element
     */
    public TraverseState next() {
        if (current != null && currentPointer.previous == null)
            return lastState = null;
        Tensor next;
        while (true) {
            next = currentPointer.next();
            if (next == null) {
                if (currentPointer.previous == null)
                    return lastState = null;
                current = currentPointer.getTensor();
                currentPointer = currentPointer.previous;

                if (currentPointer.current != null)
                    currentPointer.set(current);

                return lastState = TraverseState.Leaving;
            } else {
                TraversePermission permission = iterationGuide.getPermission(currentPointer.tensor, currentPointer.position - 1, next);
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
     * @param t the element with which to replace the current cursor
     */
    public void set(Tensor tensor) {
        if (current == tensor)
            return;
        if (tensor == null)
            throw new NullPointerException();
        if (lastState == TraverseState.Entering) {
            currentPointer.previous.set(tensor);
            currentPointer = new LinkedPointer(currentPointer.previous, tensor, false);
        } else if (lastState == TraverseState.Leaving)
            currentPointer.set(tensor);
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
        LinkedPointer currentPointer = null;
        if (lastState == TraverseState.Entering)
            currentPointer = this.currentPointer.previous;
        else if (lastState == TraverseState.Leaving)
            currentPointer = this.currentPointer;
        if (currentPointer == null)
            return -1;
        int depth = -1;
        do {
            ++depth;
            currentPointer = currentPointer.previous;
        } while (currentPointer != null);
        return depth;
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

    private static final class LinkedPointer {

        int position = 0;
        Tensor tensor;
        Tensor current = null;
        TensorBuilder builder = null;
        final LinkedPointer previous;

        public LinkedPointer(LinkedPointer pair, Tensor tensor, boolean goInside) {
            this.tensor = tensor;
            if (!goInside)
                position = tensor.size();
            this.previous = pair;
        }

        Tensor next() {
            if (builder != null && current != null)
                builder.put(current);
            if (position == tensor.size())
                return current = null;
            return current = tensor.get(position++);
        }

        Tensor getTensor() {
            if (builder != null)
                if (position != tensor.size())
                    throw new IllegalStateException();
                else {
                    tensor = builder.build();
                    builder = null;
                }
            return tensor;
        }

        void close() {
            position = tensor.size();
        }

        void set(Tensor t) {
            if (current == null)
                throw new IllegalStateException("Double set.");
            if (current == t)
                return;
            if (builder == null) {
                builder = tensor.getBuilder();
                for (int i = 0; i < position - 1; ++i)
                    builder.put(tensor.get(i));
            }
            builder.put(t);
            current = null;
        }
    }
}
