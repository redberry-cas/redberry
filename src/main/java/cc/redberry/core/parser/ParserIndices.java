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

import cc.redberry.core.context.Context;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.utils.IntArrayList;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParserIndices {
    public static SimpleIndices parseSimple(String expression) {
        return IndicesFactory.createSimple(null, parse(expression));
    }

    public static int[] parse(String expression) {
        IntArrayList indicesList = new IntArrayList();
        boolean indexMode = false;
        int indexState = 0;
        int level = 0;
        StringBuilder indicesString = null;
        for (char c : expression.toCharArray()) {
            if (c == '{') {
                level++;
                continue;
            }
            if (c == '}') {
                level--;
                continue;
            }
            if (c == '^') {
                assert level == 0;
                if (indexMode)
                    parseIndices(indicesList, indicesString, indexState);
                indexMode = true;
                indexState = 1;
                indicesString = new StringBuilder();
                continue;
            }
            if (c == '_' && level == 0) {
                if (indexMode)
                    parseIndices(indicesList, indicesString, indexState);
                indexMode = true;
                indexState = 0;
                indicesString = new StringBuilder();
                continue;
            }
            indicesString.append(c);
        }
        if (level != 0)
            throw new BracketsError();
        if (indexMode)
            parseIndices(indicesList, indicesString, indexState);
        return indicesList.toArray();
    }

    /**
     *
     * Parse string representation and put result indices in indices
     *
     * @throws BracketsError if brackets are inconsistent (e.g. (a+(b)))) )
     *
     * @param indices       integer array list of parsed indices
     * @param indicesString string representation of indices
     * @param state         index state (upper or lower)
     */
    static void parseIndices(IntArrayList indices, StringBuilder indicesString, int state) {
        char c;
        boolean toBuffer = false;
        StringBuilder indexBuffer = new StringBuilder();
        for (int i = 0; i < indicesString.length(); ++i) {
            c = indicesString.charAt(i);
            if (c == '{' || c == '}')
                continue;

            if (c == '_') {
                indexBuffer.append(c);
                toBuffer = true;
                continue;
            }
            if (c == '\\') {
                if (indexBuffer.length() != 0) {
                    indices.add(Context.get().getIndexConverterManager().getCode(indexBuffer.toString()) | state << 31);
                    indexBuffer = new StringBuilder();
                }
                indexBuffer.append(c);
                toBuffer = true;
                continue;
            }
            if (c == ' ') {
                if (indexBuffer.length() != 0) {
                    indices.add(Context.get().getIndexConverterManager().getCode(indexBuffer.toString()) | state << 31);
                    indexBuffer = new StringBuilder();
                }
                continue;
            }

            if (toBuffer || (i < indicesString.length() - 1 && indicesString.charAt(i + 1) == '_')) {
                indexBuffer.append(c);
                continue;
            }
            indices.add(Context.get().getIndexConverterManager().getCode(String.valueOf(c)) | state << 31);
        }
        if (indexBuffer.length() != 0)
            indices.add(Context.get().getIndexConverterManager().getCode(indexBuffer.toString()) | state << 31);
    }
}
