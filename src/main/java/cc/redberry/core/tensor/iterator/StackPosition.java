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
package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public interface StackPosition<T extends Payload<T>> {
    /**
     * Returns tensor, with which this position was initialized
     * or throws the {@link IllegalStateException} if initial
     * tensor is no longer available (was rebuilt).
     *
     * @return tensor, with which this position was initialized
     * @throws IllegalStateException if initial tensor is no
     *                               longer available (was rebuilt)
     */
    Tensor getInitialTensor();

    /**
     * If tensor was modified somewhere during iteration (by the invocation of
     * {@link TreeTraverseIterator#set(cc.redberry.core.tensor.Tensor)}) this
     * method returns the resulting tensors. Otherwise it returns tensor, with
     * which this position was initialized. If tensor was modified, but iteration
     * was not finished yet it throws the {@link IllegalStateException} exception.
     *
     * @return the resulting tensor, or the initial tensor if it was not modified.
     * @throws IllegalStateException if tensor was modified, but iteration was not finished.
     */
    Tensor getTensor();

    /**
     * Returns {@code true} if initial tensor was modified during iteration.
     *
     * @return {@code true} if initial tensor was modified during iteration
     */
    boolean isModified();

    /**
     * Returns the parent tree node.
     *
     * @return the parent tree node
     */
    StackPosition previous();

    /**
     * Returns the payload of current stack position.
     *
     * @return the payload of current stack position
     */
    Payload<T> getPayload();
}
