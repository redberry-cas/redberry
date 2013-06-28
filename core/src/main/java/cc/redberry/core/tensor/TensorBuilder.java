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

import cc.redberry.core.utils.TensorUtils;

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
 * <code>TensorFactory</code>. In fact TensorBuilder and TensorFactory for one
 * particular tensor will produce exactly the same result for the same input
 * sub-tensor sequence.</p>
 * <p>This class determines the common interface for tensor builders. Objects
 * of this type are produced by {@link cc.redberry.core.tensor.Tensor#getBuilder()}
 * method, or can be directly created using constructors.</p>
 * <p>Main contract for the builder infrastructure could be expressed
 * in the following code:</p>
 * <pre><code>
 * Tensor tensor = ....;
 * TensorBuilder builder = tensor.getBuilder();
 * for(Tensor t: tensor)
 *     builder.put(t);
 * assert TensorUtils.compare(builder.build(),tensor);</code></pre>
 * <p>So, using a builder of any tensor you can rebuild it into the
 * equivalent tensor.</p>
 * <p>The main goal of the builders infrastructure is reduction of all
 * tensors in the program to some general form. So, result of tensor
 * creation by the builder can be very different from simple sequential
 * concatenation of tensors passed to <code>put()</code> method,
 * even the type of resulting tensor could be different form the
 * original one. Here are several examples:</p>
 * <p>Example 1</p>
 * <pre><code>
 * TensorBuilder builder = new ProductBuilder(); //builder of product
 * builder.put(Tensors.parse("2"));
 * builder.put(Tensors.parse("3"));
 * builder.put(Tensors.parse("a"));
 * assert TensorUtils.equals(builder.build(),Tensors.parse("6*a"));</code></pre>
 * <p>Example 2</p>
 * <pre><code>
 * Tensor tensor = Tensors.parse("b*a");
 * TensorBuilder builder = tensor.getBuilder(); //builder of product
 * builder.put(Tensors.parse("2"));
 * builder.put(Tensors.parse("1/2"));
 * builder.put(Tensors.parse("a+q"));
 * assert TensorUtils.equals(builder.build(),Tensors.parse("a+q")); //Resulting tensor class is Sum</code></pre>
 * <p>There is a mimic infrastructure for tensor creation in the system,
 * see {@link TensorFactory} for more information.</p>
 * <p>For general tensor creation use factory methods in {@link Tensors}
 * class.</p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see cc.redberry.core.tensor.Tensor#getBuilder()
 * @see TensorUtils#equals(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)
 * @see <a href="http://en.wikipedia.org/wiki/Builder_pattern">http://en.wikipedia.org/wiki/Builder_pattern</a>
 * @since 1.0
 */
public interface TensorBuilder {

    /**
     * Method to sequentially add sub-tensors to the future tensor.
     *
     * @param tensor sub-tensor to be added
     */
    void put(Tensor tensor);

    /**
     * Builds the tensor. This method should be called only once for
     * the particular object.
     *
     * @return resulting tensor
     */
    Tensor build();

    TensorBuilder clone();
}
