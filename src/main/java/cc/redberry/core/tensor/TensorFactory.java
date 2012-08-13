/*
 * org.redberry.concurrent: high-level Java concurrent library.
 * Copyright (c) 2010-2012.
 * Bolotin Dmitriy <bolotin.dmitriy@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 */

package cc.redberry.core.tensor;

/**
 * <p>All tensors in the system are reduced to the general form. For example:
 * if there are several numerical factors in the product they will be
 * multiplied and only one resulting numerical factor will be in the
 * resulting product. To maintain this reduced form of expressions, specific
 * infrastructure for tensor creation is provided. All creations of tensors
 * are performed using this infrastructure and explicit tensor creation
 * is architecturally forbidden for the user. Tensor creation infrastructure
 * consists of two functionally redundant but optimized for different usage
 * strategies interfaces: <code>TensorBuilder</code> and
 * <code>TensorFactory</code>. In fact TensorBuilder and TensorFactory returned
 * by one particular tensor will produce exactly the same result for the same
 * input sub-tensor sequence.</p>
 * <p>This class determines the common interface for tensor factories. Objects
 * of this type are produced by {@link Tensor#getFactory()} method.</p>
 * <p>Main contract for the factory infrastructure could be expressed
 * in the following code:</p>
 * <pre><code>
 * Tensor tensor = ....;
 * TensorFactory factory = tensor.getFactory();
 * Tensor createdTensor = factory.create(tensor.getRange(0, tensor.size()));
 * assert createdTensor.equals(tensor);</code></pre>
 * <p>So, using a tensor factory of any tensor you can rebuild it into the
 * equivalent tensor.</p>
 * <p>Behavior of TensorFactory is fully consistent with TensorBuilder,
 * see {@link TensorBuilder} for more information.</p>
 * <p>There is a mimic infrastructure for tensor creation in the system,
 * see {@link TensorFactory} for more information.</p>
 * <p>For general tensor creation use factory methods in {@link Tensors}
 * class.</p>
 */
public interface TensorFactory {

    /**
     * Creates tensor with provided array of sub-tensors.
     *
     * @param tensors array of sub-tensors
     * @return created tensor
     */
    Tensor create(Tensor... tensors);
}
