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
package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;

/**
 * Parent interface for tree iterators.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public interface TreeIterator {
    /**
     * Returns the next tensor in tree or null if iteration is finished
     *
     * @return the next tensor in tree or null if iteration is finished
     */
    Tensor next();

    /**
     * Set current tensor in tree.
     *
     * @param tensor tensor
     */
    void set(Tensor tensor);

    /**
     * Returns the result after iteration finished (or stopped). After this step the
     * iterator becomes broken.
     *
     * @return the result after iteration finished (or stopped)
     */
    Tensor result();

    /**
     * Returns the current depth in the tree.
     *
     * @return current depth in the tree
     */
    int depth();
}
