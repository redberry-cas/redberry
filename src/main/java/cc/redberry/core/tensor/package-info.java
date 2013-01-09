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

/**
 * Data structures and algorithms for creating mathematical expressions. Class {@link cc.redberry.core.tensor.Tensor}
 * is parent for all mathematical expressions. Each type of expressions strictly satisfies some standard form, which
 * is defined in the corresponding {@link cc.redberry.core.tensor.TensorFactory} and {@link cc.redberry.core.tensor.TensorBuilder}. Thus, there is no public
 * constructors in Tensor inheritors, and any expression should be created via factory or builder. Simple tensors
 * and fields should be instantiated via corresponding static methods  in {@link cc.redberry.core.tensor.Tensors} class.
 *
 * @see cc.redberry.core.tensor.Tensor
 * @see cc.redberry.core.tensor.TensorBuilder
 * @see cc.redberry.core.tensor.TensorFactory
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
package cc.redberry.core.tensor;