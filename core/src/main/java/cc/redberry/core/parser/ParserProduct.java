/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import gnu.trove.set.hash.TIntHashSet;

import java.util.List;

/**
 * Parser of mathematical product.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ParserProduct extends ParserOperator {
    /**
     * Singleton instance.
     */
    public static final ParserProduct INSTANCE = new ParserProduct();

    private ParserProduct() {
        super('*', '/');
    }

    @Override
    protected ParseToken compile(List<ParseToken> nodes) {
        return new ParseToken(TokenType.Product, nodes.toArray(new ParseToken[nodes.size()]));
    }

    @Override
    protected ParseToken inverseOperation(ParseToken node) {
        return new ParseToken(TokenType.Power, node, new ParseTokenNumber(Complex.MINUS_ONE));
    }

    @Override
    protected boolean testOperator(char[] expressionChars, int position) {
        return !((position + 1 < expressionChars.length && expressionChars[position + 1] == '*') ||
                (position - 1 >= 0 && expressionChars[position - 1] == '*'));
    }

    @Override
    public ParseToken parseToken(String expression, Parser parser) {
        ParseToken node = super.parseToken(expression, parser);
        if (node == null || !parser.isAllowSameVariance())
            return node;

        TIntHashSet indices = new TIntHashSet();
        for (ParseToken c : node.content) {
            Indices free = c.getIndices().getFree();
            for (int i = 0; i < free.size(); i++) {
                int ind = free.get(i);
                if (indices.contains(ind))
                    revertIndex(c, ind);
                else indices.add(ind);
            }
        }

        return node;
    }

    @Override
    public int priority() {
        return 999;
    }

    private static void revertIndex(ParseToken token, int index) {
        if (token instanceof ParseTokenSimpleTensor) {
            ParseTokenSimpleTensor pToken = (ParseTokenSimpleTensor) token;
            SimpleIndices indices = pToken.indices;
            for (int i = 0; i < indices.size(); i++) {
                if (indices.get(i) == index) {
                    int[] inds = indices.toArray();
                    inds[i] = IndicesUtils.inverseIndexState(index);
                    pToken.indices = IndicesFactory.createSimple(null, inds);
                    break;
                }
            }
        } else if (token.tokenType == TokenType.Product
                || token.tokenType == TokenType.Trace
                || token.tokenType == TokenType.Sum)
            for (ParseToken c : token.content)
                revertIndex(c, index);
    }
}
