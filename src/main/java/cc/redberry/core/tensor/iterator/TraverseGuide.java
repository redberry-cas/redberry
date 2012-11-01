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
import cc.redberry.core.tensor.TensorField;
import cc.redberry.core.tensor.functions.ScalarFunction;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public interface TraverseGuide {

    /**
     * This method specifies restrictions on iteration procedure. Possible kinds
     * of restrictions specified in {@link TraversePermission} {@code enum}. Any
     * cursor position can be characterized by three parameters: current cursor,
     * its position tensor and position in position tensor. So, they are the method
     * arguments.
     *
     * @param parent        current cursor position tensor
     * @param indexInParent position of the current cursor in position tensor
     * @param tensor        current cursor
     * @return TraversePermission
     * @see TraversePermission
     * @see TreeTraverseIterator
     */
    TraversePermission getPermission(Tensor tensor, Tensor parent, int indexInParent);

    /**
     * Traverse guide, which always return {@link TraversePermission#Enter}.
     */
    public static final TraverseGuide ALL = new TraverseGuide() {

        @Override
        public TraversePermission getPermission(Tensor tensor, Tensor parent, int indexInParent) {
            return TraversePermission.Enter;
        }
    };
    public static final TraverseGuide EXCEPT_FUNCTIONS_AND_FIELDS = new TraverseGuide() {

        @Override
        public TraversePermission getPermission(Tensor tensor, Tensor parent, int indexInParent) {
            if (tensor instanceof ScalarFunction)
                return TraversePermission.DontShow;
            else if (tensor instanceof TensorField)
                return TraversePermission.ShowButNotEnter;
            else
                return TraversePermission.Enter;
        }
    };

    public static final TraverseGuide EXCEPT_FIELDS = new TraverseGuide() {

        @Override
        public TraversePermission getPermission(Tensor tensor, Tensor parent, int indexInParent) {
            if (tensor instanceof TensorField)
                return TraversePermission.ShowButNotEnter;
            else
                return TraversePermission.Enter;
        }
    };
}
