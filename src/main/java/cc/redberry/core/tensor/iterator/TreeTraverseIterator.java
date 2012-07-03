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
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TreeTraverseIterator {

    private final TraverseGuide iterationGuide;
    private LinkedPointer currentPair;
    private TraverseState lastState;
    private Tensor current = null;

    public TreeTraverseIterator(Tensor tensor, TraverseGuide guide) {
        currentPair = new LinkedPointer(null, TensorWrapper.wrap(tensor), true);
        iterationGuide = guide;
    }

    public TreeTraverseIterator(Tensor tensor) {
        this(tensor, TraverseGuide.ALL);
    }

    public TraverseState next() {
        if (current != null && currentPair.previous == null)
            return lastState = null;
        Tensor next;
        while (true) {
            next = currentPair.next();
            if (next == null) {
                current = currentPair.getTensor();
                currentPair = currentPair.previous;

                if (currentPair.current != null)
                    currentPair.set(current);

                return lastState = TraverseState.Leaving;
            } else {
                TraversePermission permission = iterationGuide.getPermission(currentPair.tensor, currentPair.position, next);
                if (permission == null)
                    throw new NullPointerException();
                if (permission == TraversePermission.DontShow)
                    continue;

                current = next;
                currentPair = new LinkedPointer(currentPair, next, permission == TraversePermission.Enter);
                return lastState = TraverseState.Entering;
            }
        }
    }

    public void set(Tensor tensor) {
        if (current == tensor)
            return;
        if (tensor == null)
            throw new NullPointerException();
        if (lastState == TraverseState.Entering) {
            currentPair.previous.set(tensor);
            currentPair = new LinkedPointer(currentPair.previous, tensor, false);
        } else if (lastState == TraverseState.Leaving)
            currentPair.set(tensor);
    }

    public Tensor current() {
        return current;
    }

    public Tensor result() {
        if (currentPair.previous != null)
            throw new RuntimeException("Iteration not finished.");
        return currentPair.getTensor().get(0);
    }

    private static class LinkedPointer {

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
