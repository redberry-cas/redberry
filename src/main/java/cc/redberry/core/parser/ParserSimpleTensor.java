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

import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.utils.IntArrayList;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParserSimpleTensor implements NodeParser {
    public static final ParserSimpleTensor INSTANCE = new ParserSimpleTensor();

    private ParserSimpleTensor() {
    }

    @Override
    public ParseNodeSimpleTensor parseNode(String expression, Parser parser) {
        IntArrayList indicesList = new IntArrayList();
        boolean indexMode = false;
        int indexState = 0;
        StringBuilder nameBuilder = new StringBuilder();
        StringBuilder indicesString = null;
        int level = 0;
        for (char c : expression.toCharArray()) {
            if (c == '{') {
                level++;
                if (!indexMode)
                    continue;
            }
            if (c == '}') {
                level--;
                if (!indexMode)
                    continue;
            }
            if (c == '^') {
                assert level == 0;
                if (indexMode)
                    ParserIndices.parseIndices(indicesList, indicesString, indexState);
                indexMode = true;
                indexState = 1;
                indicesString = new StringBuilder();
                continue;
            }
            if (c == '_' && level == 0) {
                if (indexMode)
                    ParserIndices.parseIndices(indicesList, indicesString, indexState);
                indexMode = true;
                indexState = 0;
                indicesString = new StringBuilder();
                continue;
            }
            if (!indexMode)
                nameBuilder.append(c);
            else
                indicesString.append(c);
        }
        if (level != 0)
            throw new BracketsError();
        if (indexMode)
            ParserIndices.parseIndices(indicesList, indicesString, indexState);
        SimpleIndices indices = IndicesFactory.createSimple(null, indicesList.toArray());
        String name = nameBuilder.toString();
        return new ParseNodeSimpleTensor(indices, name);
    }

    @Override
    public int priority() {
        return 0;
    }
}
