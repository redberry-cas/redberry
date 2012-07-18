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

import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorField;
import cc.redberry.core.tensor.iterator.TraverseState;
import cc.redberry.core.tensor.iterator.TreeTraverseIterator;
import cc.redberry.core.utils.TensorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SubstitutionIterator {

    private final TreeTraverseIterator iterator;
    private boolean waitingForProduct = true;
    Stack stack;

    public SubstitutionIterator(Tensor tensor) {
        iterator = new TreeTraverseIterator(tensor);
    }

    public Tensor next() {
        TraverseState state = iterator.next();
        if (state == null)
            return null;
        Tensor current = iterator.current();
        if (current instanceof TensorField)
            if (state == TraverseState.Entering)
                waitingForProduct = true;
            else if (state == TraverseState.Leaving)
                if (!waitingForProduct) {
                    stack = stack.previous;
                    return current;
                }
        if (current instanceof Product)
            if (state == TraverseState.Entering)
                if (waitingForProduct) {
                    stack = new Stack(stack, current, iterator.depth());
                    waitingForProduct = false;
                } else if (state == TraverseState.Leaving) {
                    if (stack != null && stack.depth >= iterator.depth())
                        stack = stack.previous;
                    return current;
                }

        if (state == TraverseState.Leaving)
            return current;
        return next();
    }

    //TODO chache forbidden in Stack after finishing all tests
    public int[] forbiddenIndices() {
        if (stack == null || waitingForProduct)
            return new int[0];

        Set<Integer> set = TensorUtils.getAllIndices(stack.tensor);
        int[] result = new int[set.size()];
        int i = -1;
        for (Integer integer : set)
            result[++i] = integer;
        return result;
    }

    public void set(Tensor tensor) {
        iterator.set(tensor);
    }

    public Tensor result() {
        return iterator.result();
    }

    private static final class Stack {

        private final Stack previous;
        private final Tensor tensor;
        private final int depth;

        public Stack(Stack previous, Tensor tensor, int depth) {
            this.previous = previous;
            this.tensor = tensor;
            this.depth = depth;
        }

        @Override
        public String toString() {
            List<Tensor> tensors = new ArrayList<>();
            Stack c = this;
            while (c != null) {
                tensors.add(0, c.tensor);
                c = c.previous;
            }
            return tensors.toString();
        }
    }
}
