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
 * Facilities for generating tensor of the most general
 * form with specified free indices from specified tensors.
 * <br>Consider the example:</br>
 * <pre><code>
 *     Tensor[] samples = Tensors.parse("g_mn", "g^mn", "d_m^n");
 *     Tensor t = TensorGenerator.generate("c", ParserIndices.parseSimple("_{mnab}"), false, samples);
 *     System.out.println(t);
 * </code></pre>
 * This code produces
 * <pre><code>
 *  c1*g_mn*g_ab + c2*g_ma*g_nb + c3*g_mb*gna
 * </code></pre>
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
package cc.redberry.core.tensorgenerator;