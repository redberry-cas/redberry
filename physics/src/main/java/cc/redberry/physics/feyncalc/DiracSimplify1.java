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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParseToken;
import cc.redberry.core.parser.Parser;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.IntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

import static cc.redberry.core.indices.IndicesUtils.areContracted;
import static cc.redberry.core.tensor.Tensors.*;

/**
 * Chisholm identities
 *
 * @author Stanislav Poslavsky
 */
final class DiracSimplify1 extends AbstractFeynCalcTransformation {
    private final Expression[] subs;

    DiracSimplify1(DiracOptions options) {
        super(options, IDENTITY);
        ParseToken[] ss = {s1, s2, s3, s4};
        this.subs = new Expression[ss.length];
        for (int i = 0; i < ss.length; ++i)
            subs[i] = (Expression) deltaTrace.transform(tokenTransformer.transform(ss[i]).toTensor());
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "DiracSimplify1";
    }

    @Override
    protected Tensor transformLine(ProductOfGammas pg, IntArrayList modifiedElements) {
        assert pg.g5Positions.size() == 0 || (pg.g5Positions.size() == 1 && pg.g5Positions.first() == pg.length - 1)
                : "G5s are not simplified";

        int length = pg.length;
        if (pg.g5Positions.size() == 1)
            --length;

        if (length <= 1)
            return null;

        ProductContent pc = pg.pc;
        IntArrayList lengths = new IntArrayList();
        for (int i = 0; i < length - 1; ++i) {
            Tensor g1 = pc.get(pg.gPositions.get(i));
            for (int j = i + 1; j < length; ++j) {
                Tensor g2 = pc.get(pg.gPositions.get(j));
                if (areContracted(g1.getIndices().get(metricType, 0), g2.getIndices().get(metricType, 0)))
                    lengths.add(j - i + 1);
            }
        }

        if (lengths.isEmpty())
            return null;
        lengths.sort();

        Transformation[] overall = new Transformation[lengths.size() + 3];
        for (int i = lengths.size() - 1; i >= 0; --i)
            overall[i] = createSubstitution(lengths.get(i));
        overall[lengths.size()] = expandAndEliminate;
        overall[lengths.size() + 1] = deltaTrace;
        overall[lengths.size() + 2] = traceOfOne;
        return transform(Transformation.Util.applyUntilUnchanged(multiply(pg.toArray()), overall));
    }

    private TIntObjectHashMap<Expression> substitutions = new TIntObjectHashMap<>();

    Expression createSubstitution(int length) {
        if (length <= 2)
            return subs[length - 2];
        Expression expr = substitutions.get(length);
        if (expr == null) {
            Tensor[] line = createLine(length);
            line[length - 1] = setMetricIndex((SimpleTensor) line[length - 1], IndicesUtils.inverseIndexState(line[0].getIndices().get(metricType, 0)));
            substitutions.put(length, expr = expression(multiply(line), createSubstitution0(line)));
        }
        return expr;
    }

    private Tensor createSubstitution0(Tensor[] gammas) {
        gammas = del(gammas, 0);
        gammas = del(gammas, gammas.length - 1);
        int length = gammas.length;
        SumBuilder sb = new SumBuilder();
        if (length % 2 == 1) {//odd
            //d-4 term
            sb.put(multiply(subtract(Complex.FOUR, deltaTrace.get(1)), multiply(gammas)));

            //reverse term
            int[] indices = new int[length];
            for (int i = 0; i < length; ++i)
                indices[i] = gammas[i].getIndices().get(metricType, 0);
            for (int i = 0; i < length; ++i)
                gammas[i] = setMetricIndex((SimpleTensor) gammas[i], indices[length - i - 1]);
            sb.put(multiply(Complex.MINUS_TWO, multiply(gammas)));

        } else {//even
            //d-4 term
            sb.put(multiply(subtract(deltaTrace.get(1), Complex.FOUR), multiply(gammas)));

            int[] indices = new int[length];
            for (int i = 0; i < length; ++i)
                indices[i] = gammas[i].getIndices().get(metricType, 0);
            //move last to the left
            Tensor[] shifted = gammas.clone();
            for (int i = 0; i < length - 1; i++)
                shifted[i + 1] = setMetricIndex((SimpleTensor) gammas[i + 1], indices[i]);
            shifted[0] = setMetricIndex((SimpleTensor) gammas[0], indices[length - 1]);
            sb.put(multiply(Complex.TWO, multiply(shifted)));

            //reverse except last
            for (int i = 0; i < length - 1; ++i)
                gammas[i] = setMetricIndex((SimpleTensor) gammas[i], indices[length - i - 2]);
            sb.put(multiply(Complex.TWO, multiply(gammas)));
        }

        return sb.build();
    }

    private static final Parser parser;
    /**
     * G_a*G^a = d
     * G_a*G_b*G^a = -(d-2)*G_b
     * G_a*G_b*G_c*G^a = 4*g_bc - (4-d)*G_b*G_c
     * G_a*G_b*G_c*G_d*G^a = -2*G_d*G_c*G_b + (4-d)*G_b*G_c*G_d
     */
    private static final ParseToken s1, s2, s3, s4;

    static {
        parser = CC.current().getParseManager().getParser();
        //G_a*G^a = d
        s1 = parser.parse("G_a^a'_b'*G^a^b'_c' = d^z_z*d^a'_c'");
        //G_a*G_b*G^a = -(d-2)*G_b
        s2 = parser.parse("G_a^a'_b'*G_b^b'_c'*G^ac'_d' = -(d^z_z-2)*G_b^a'_d'");
        //G_a*G_b*G_c*G^a = 4*g_bc - (4-d)*G_b*G_c
        s3 = parser.parse("G_a^a'_b'*G_b^b'_c'*G_c^c'_d'*G^ad'_e' = 4*g_bc*d^a'_e' - (4-d^z_z)*G_b^a'_b'*G_c^b'_e'");
        //G_a*G_b*G_c*G_d*G^a = -2*G_d*G_c*G_b + (4-d)*G_b*G_c*G_d
        s4 = parser.parse("G_a^a'_b'*G_b^b'_c'*G_c^c'_d'*G_d^d'_e'*G^ae'_f' = -2*G_d^a'_b'*G_c^b'_c'*G_b^c'_f' + (4-d^z_z)*G_b^a'_b'*G_c^b'_c'*G_d^c'_f'");
    }
}
