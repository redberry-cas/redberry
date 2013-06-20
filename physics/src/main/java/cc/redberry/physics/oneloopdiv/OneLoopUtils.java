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

package cc.redberry.physics.oneloopdiv;

import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensors;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class OneLoopUtils {

    private OneLoopUtils() {
    }

    private static Expression[] antiDeSitterBackground = {
            Tensors.parseExpression("R_{\\mu\\nu\\alpha\\beta} = (1/3)*(g_{\\mu\\beta}*g_{\\nu\\alpha}-g_{\\mu\\alpha}*g_{\\nu\\beta})*La"),
            Tensors.parseExpression("R_{\\mu\\nu} = -g_{\\mu\\nu}*La")
    };

    /**
     * This method return the definition of the anti de Sitter background,
     * with the cosmological constant denoted as {@code La}. In other words
     * it returns the following two substitutions:
     * <pre>
     *     Tensors.parseExpression("R_{\\mu\\nu\\alpha\\beta} = (1/3)*(g_{\\mu\\beta}*g_{\\nu\\alpha}-g_{\\mu\\alpha}*g_{\\nu\\beta})*La");
     *     Tensors.parseExpression("R_{\\mu\\nu} = -g_{\\mu\\nu}*La");
     * </pre>
     *
     * @return the definition of the anti de Sitter background, with the
     *         cosmological constant denoted as {@code La}.
     */
    public static Expression[] antiDeSitterBackground() {
        return antiDeSitterBackground.clone();
    }
}
