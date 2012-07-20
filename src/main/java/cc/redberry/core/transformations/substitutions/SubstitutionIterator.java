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

import cc.redberry.core.indexmapping.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorField;
import cc.redberry.core.tensor.iterator.TraverseState;
import cc.redberry.core.tensor.iterator.TreeTraverseIterator;
import cc.redberry.core.utils.*;
import cc.redberry.core.utils.TensorUtils;
import java.util.*;

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
    private static final Indicator<Tensor> FieldIndicator = Indicator.Utils.classIndicator(TensorField.class);
    private int fieldDepth = 0;

    public Tensor next() {
        TraverseState state = iterator.next();
        if (state == null)
            return null;

        Tensor current = iterator.current();
        if (current instanceof TensorField)
            if (state == TraverseState.Leaving)
                if (fieldDepth == 0)
                    if (!waitingForProduct) {
                        stack = stack.previous;
                        return current;
                    } else
                        waitingForProduct = false;
                else
                    --fieldDepth;
        if (iterator.checkLevel(FieldIndicator, 1) && state == TraverseState.Entering) {
            if (waitingForProduct)
                ++fieldDepth;
            waitingForProduct = true;
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

    public Set<Integer> forbiddenIndices() {
        if (stack == null || waitingForProduct)
            return new HashSet<>();
        return stack.getForbidden();
    }

    public void set(Tensor tensor) {
        iterator.set(tensor);
    }

    public Tensor result() {
        return iterator.result();
    }

    private static final class Stack {

        private final Stack previous;
        private Set<Integer> forbiddenIndices;
        private final Tensor tensor;
        private final int depth;

        public Stack(Stack previous, Tensor tensor, int depth) {
            this.previous = previous;
            this.depth = depth;
            this.tensor = tensor;
            this.forbiddenIndices = null;
        }

        Set<Integer> getForbidden() {
            if (forbiddenIndices == null)
                forbiddenIndices = TensorUtils.getAllIndicesNames(tensor);
            return forbiddenIndices;
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
