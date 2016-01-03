/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.combinatorics.IntDistinctTuplesPort;
import cc.redberry.core.context.CC;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.preprocessor.ChangeIndicesTypesAndTensorNames;
import cc.redberry.core.parser.preprocessor.TypesAndNamesTransformer;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.IntArrayList;

/**
 * @author Stanislav Poslavsky
 */
final class SchoutenIdentities4 implements Transformation {
    final SimpleTensor leviCivita;
    final Tensor[] schouten1, schouten2, schouten3;
    final Tensor[][] allSchouten;

    public SchoutenIdentities4(SimpleTensor leviCivita) {
        this.leviCivita = leviCivita;
        ChangeIndicesTypesAndTensorNames tokenTransformer = new ChangeIndicesTypesAndTensorNames(TypesAndNamesTransformer.Utils.and(
                TypesAndNamesTransformer.Utils.changeName(new String[]{"e"}, new String[]{leviCivita.getStringName()}),
                TypesAndNamesTransformer.Utils.changeType(IndexType.LatinLower, IndicesUtils.getTypeEnum(leviCivita.getIndices().get(0)))));
        this.schouten1 = new Tensor[schoutenCombinations1.length];
        for (int i = 0; i < schouten1.length; i++)
            schouten1[i] = tokenTransformer.transform(CC.current().getParseManager().getParser().parse(schoutenCombinations1[i])).toTensor();

        this.schouten2 = new Tensor[schoutenCombinations2.length];
        for (int i = 0; i < schouten2.length; i++)
            schouten2[i] = tokenTransformer.transform(CC.current().getParseManager().getParser().parse(schoutenCombinations2[i])).toTensor();

        this.schouten3 = new Tensor[schoutenCombinations3.length];
        for (int i = 0; i < schouten3.length; i++)
            schouten3[i] = tokenTransformer.transform(CC.current().getParseManager().getParser().parse(schoutenCombinations3[i])).toTensor();
        allSchouten = new Tensor[][]{schouten1, schouten2, schouten3};
    }

    @Override
    public Tensor transform(Tensor t) {
        FromChildToParentIterator it = new FromChildToParentIterator(t);
        Tensor current;
        main:
        while ((current = it.next()) != null) {
            if (!(current instanceof Sum))
                continue;

            for (Tensor[] schouten : allSchouten) {
                if (current.size() < schouten.length)
                    continue;
                IntArrayList[] positions = new IntArrayList[schouten.length];
                out:
                for (int i = 0; i < current.size(); i++) {
                    Mapping0 mp = buildMapping(schouten[0], current.get(i));
                    if (mp == null)
                        continue;
                    for (int j = 0; j < schouten.length; j++)
                        positions[j] = new IntArrayList();
                    positions[0].add(i);

                    for (int j = 1; j < schouten.length; ++j) {
                        for (int k = 0; k < current.size(); k++)
                            if (testMapping(mp, schouten[j], current.get(k)))
                                positions[j].add(k);
                        if (positions[j].isEmpty())
                            continue out;
                    }
                    int[][] positionsArr = new int[schouten.length][];
                    for (int j = 0; j < schouten.length; ++j)
                        positionsArr[j] = positions[j].toArray();
                    int[] ps = new IntDistinctTuplesPort(positionsArr).take();

                    if (ps != null) {
                        it.set(((Sum) current).remove(ps));
                        continue main;
                    }
                }
            }
        }
        Tensor result = it.result();
        if (result != t)
            return transform(result);
        return result;
    }

    static boolean contains(int[] a, int b) {
        for (int i : a)
            if (i == b)
                return true;
        return false;
    }

    private static Mapping0 buildMapping(Tensor eps, Tensor part) {
        if (!(part instanceof Product))
            return null;
        Product p = (Product) part;
        Complex factor = p.getFactor();
        Mapping mapping = IndexMappings.getFirst(eps, p.getDataSubProduct());
        if (mapping == null)
            return null;
        return new Mapping0(factor, mapping);
    }

    private static boolean testMapping(Mapping0 mapping0, Tensor eps, Tensor part) {
        if (!(part instanceof Product))
            return false;
        Product p = (Product) part;
        Complex factor = p.getFactor();
        Tensor ds = p.getDataSubProduct();

        if (IndexMappings.testMapping(mapping0.mapping, eps, ds))
            return factor.equals(mapping0.factor);
        else if (IndexMappings.testMapping(mapping0.mapping.addSign(true), eps, ds))
            return factor.equals(mapping0.factor.negate());
        return false;
    }


    private static final class Mapping0 {
        final Complex factor;
        final Mapping mapping;

        public Mapping0(Complex factor, Mapping mapping) {
            this.factor = factor;
            this.mapping = mapping;
        }
    }

    private static final String[] schoutenCombinations1 = {
            "-g_{ad}*e_{bcef}",
            "g_{ac}*e_{bdef}", "-g_{ab}*e_{cdef}",
            "-g_{af}*e_{bcde}", "g_{ae}*e_{bcdf}"
    };

    private static final String[] schoutenCombinations2 = {
            "e_{bfea}*g_{dc}", "-e_{dbfa}*g_{ec}",
            "e_{bcef}*g_{ad}", "e_{dbea}*g_{fc}",
            "e_{fdea}*g_{bc}", "-e_{bcdf}*g_{ae}",
            "e_{cdef}*g_{ab}", "e_{bcde}*g_{af}"
    };

    private static final String[] schoutenCombinations3 = {
            "-g_{db}*e_{efac}", "-g_{de}*e_{bafc}",
            "-g_{cf}*e_{abde}", "-g_{da}*e_{febc}",
            "-g_{ac}*e_{bdef}", "g_{bc}*e_{adef}",
            "g_{df}*e_{ebac}", "g_{ce}*e_{abdf}"
    };
}
