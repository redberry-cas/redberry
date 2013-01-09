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
package cc.redberry.core.tensor;

/**
 * <p>All tensors in the system are reduced to the general form. For example: if
 * there are several numerical factors in the product they will be multiplied
 * and only one resulting numerical factor will be in the resulting product. To
 * maintain this reduced form of expressions, specific infrastructure for tensor
 * creation is provided. All creations of tensors are performed using this
 * infrastructure and explicit tensor creation is architecturally forbidden for
 * the user. Tensor creation infrastructure consists of two functionally
 * redundant but optimized for different usage strategies interfaces:
 * <code>TensorBuilder</code> and
 * <code>TensorFactory</code>. In fact TensorBuilder and TensorFactory returned
 * by one particular tensor will produce exactly the same result for the same
 * input sub-tensor sequence.</p> <p>This class determines the common interface
 * for tensor factories. Objects of this type are produced by {@link Tensor#getFactory()}
 * method.</p> <p>Main contract for the factory infrastructure could be
 * expressed in the following code:</p>
 * <pre><code>
 * Tensor tensor = ....;
 * TensorFactory factory = tensor.getFactory();
 * Tensor createdTensor = factory.create(tensor.getRange(0, tensor.size()));
 * assert TensorUtils.compare(createdTensor,tensor);</code></pre> <p>So, using a
 * tensor factory of any tensor you can rebuild it into the equivalent
 * tensor.</p> <p>Behavior of TensorFactory is fully consistent with
 * TensorBuilder, see {@link TensorBuilder} for more information. For general
 * tensor creation use factory methods in {@link Tensors} class.</p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see cc.redberry.core.tensor.Tensor#getBuilder()
 * @see TensorUtils#compare(cc.redberry.core.tensor.Tensor,
 * cc.redberry.core.tensor.Tensor)
 * @see TensorUtils#equals(cc.redberry.core.tensor.Tensor,
 * cc.redberry.core.tensor.Tensor)
 * @see <a
 * href="http://en.wikipedia.org/wiki/Builder_pattern">http://en.wikipedia.org/wiki/Builder_pattern</a>
 */
public interface TensorFactory {

    /**
     * Creates tensor with provided array of sub-tensors.
     *
     * @param tensors array of sub-tensors
     *
     * @return created tensor
     */
    Tensor create(Tensor... tensors);
}
