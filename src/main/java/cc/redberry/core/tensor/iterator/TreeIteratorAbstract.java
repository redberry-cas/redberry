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

/*
 * Wrapper for TreeTraverseIterator. Traverse tensor into or out according to
 * mode params in next().
 */
abstract class TreeIteratorAbstract implements TreeIterator {

    private final TreeTraverseIterator iterator;
    private final TraverseState state;

    TreeIteratorAbstract(Tensor tensor, TraverseGuide guide, TraverseState state) {
        this.iterator = new TreeTraverseIterator(tensor, guide);
        this.state = state;
    }

    TreeIteratorAbstract(Tensor tensor, TraverseState state) {
        this.iterator = new TreeTraverseIterator(tensor);
        this.state = state;
    }

    @Override
    public int depth() {
        return iterator.depth();
    }

    @Override
    public Tensor next() {
        TraverseState nextState;
        while ((nextState = iterator.next()) != state && nextState != null);
        if (nextState == null)
            return null;
        return iterator.current();
    }

    @Override
    public Tensor result() {
        return iterator.result();
    }

    @Override
    public void set(Tensor tensor) {
        iterator.set(tensor);
    }
}
