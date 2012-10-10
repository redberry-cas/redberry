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
package cc.redberry.core.context;

import cc.redberry.core.indices.Indices;
import cc.redberry.core.tensor.Tensor;

/**
 * This
 * <code>enum</code> specifies common modes of string representation of objects.
 * For using this
 * <code>enum</code> class must specifies method
 * <code>toString(ToStringMode mode)</code>. For the defaults, we provides two
 * modes {
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @link #UTF8} and {
 * @link #LaTeX}. Examples of using, you can find in see also section.
 * @see Tensor#toString(ToStringMode)
 * @see Indices#toString(ToStringMode)
 * @see Tensor#toString()
 */
public enum ToStringMode {

    /**
     * This mode supports LaTeX code generating via
     * <code>toString(ToStringMode mode)</code>. If you use this mode for
     * outputting expression, the result will be a LaTeX code, representing
     * expression, using common LaTeX functions.
     *
     * @see Tensor#toString(ToStringMode)
     * @see Indices#toString(ToStringMode)
     */
    LaTeX,
    /**
     * This mode specifies the simplest free output format. For, example Greek
     * letters will be represent directly &alpha &beta, and so on.
     * <p/>
     * <p>NOTE: some systems does not supports direct Greek printing, so instead
     * of '&alpha' you will see '?' </p>
     *
     * @see Tensor#toString(ToStringMode)
     * @see Indices#toString(ToStringMode)
     */
    UTF8,
    /**
     * This mode specifies such output format, that can be parsed again, by
     * directly call in parser, i.e.
     * {@code CC.parser(t.toString(ToStringMode.Redberry))}. Most of all it
     * touches tensor fields, functions and derivatives. Greek indices converted
     * to string with this mode will look like "\alpha" (as LaTeX mode)
     */
    Redberry,
    /**
     * This mode specifies such output format, that can be parsed again, by
     * copying string from output and pasting in parser method argument. Most of
     * all it touches tensor fields, functions and derivatives. In opposite to
     * {@link #Redberry}, Greek indices converted to string with this mode will
     * look like "\\alpha".
     */
    @Deprecated
    RedberryConsole,
    WolframMathematica
}
