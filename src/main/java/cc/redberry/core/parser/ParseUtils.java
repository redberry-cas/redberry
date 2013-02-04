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
package cc.redberry.core.parser;

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorField;
import cc.redberry.core.tensor.functions.ScalarFunction;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods to work with AST.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ParseUtils {

    private ParseUtils() {
    }

    public static ParseToken tensor2AST(Tensor tensor) {
        if (tensor instanceof TensorField) {
            TensorField tf = (TensorField) tensor;
            ParseToken[] content = new ParseToken[tf.size()];
            int i = 0;
            for (Tensor t : tf)
                content[i++] = tensor2AST(t);
            return new ParseTokenTensorField(tf.getIndices(), tf.getStringName(), content, tf.getArgIndices());
        }
        if (tensor instanceof SimpleTensor) {
            SimpleTensor st = (SimpleTensor) tensor;
            return new ParseTokenSimpleTensor(st.getIndices(), st.getStringName());
        }
        if (tensor instanceof Complex)
            return new ParseTokenNumber((Complex) tensor);

        ParseToken[] content = new ParseToken[tensor.size()];
        int i = 0;
        for (Tensor t : tensor)
            content[i++] = tensor2AST(t);

        if (tensor instanceof ScalarFunction)
            return new ParseTokenScalarFunction(tensor.getClass().getSimpleName(), content);
        return new ParseToken(TokenType.valueOf(tensor.getClass().getSimpleName()), content);
    }

    public static Set<Integer> getAllIndices(ParseToken node) {
        Set<Integer> s = new HashSet<>();
        getAllIndices1(node, s);
        return s;
    }

    private static void getAllIndices1(ParseToken node, Set<Integer> set) {
        if (node instanceof ParseTokenSimpleTensor) {
            Indices indices = node.getIndices();
            for (int i = indices.size() - 1; i >= 0; --i)
                set.add(IndicesUtils.getNameWithType(indices.get(i)));
        } else
            for (ParseToken pn : node.content)
                if (!(pn instanceof ParseTokenScalarFunction))
                    getAllIndices1(pn, set);
    }

    public static TIntSet getAllIndicesT(ParseToken node) {
        TIntSet set = new TIntHashSet();
        getAllIndicesT1(node, set);
        return set;
    }

    private static void getAllIndicesT1(ParseToken node, TIntSet set) {
        if (node instanceof ParseTokenSimpleTensor) {
            Indices indices = node.getIndices();
            for (int i = indices.size() - 1; i >= 0; --i)
                set.add(IndicesUtils.getNameWithType(indices.get(i)));
        } else
            for (ParseToken pn : node.content)
                if (!(pn instanceof ParseTokenScalarFunction))
                    getAllIndicesT1(pn, set);
    }
}
