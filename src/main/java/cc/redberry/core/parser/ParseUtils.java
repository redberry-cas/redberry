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
package cc.redberry.core.parser;

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesUtils;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParseUtils {

    private ParseUtils() {
    }

    public static Set<Integer> getAllIndices(ParseNode node) {
        Set<Integer> s = new HashSet<>();
        getAllIndices1(node, s);
        return s;
    }

    private static void getAllIndices1(ParseNode node, Set<Integer> set) {
        if (node instanceof ParseNodeSimpleTensor) {
            Indices indices = node.getIndices();
            for (int i = indices.size() - 1; i >= 0; --i)
                set.add(IndicesUtils.getNameWithType(indices.get(i)));
        } else
            for (ParseNode pn : node.content)
                if (!(pn instanceof ParseNodeScalarFunction))
                    getAllIndices1(pn, set);
    }
}
