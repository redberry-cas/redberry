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
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParseToken;
import cc.redberry.core.parser.Parser;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import cc.redberry.core.transformations.TransformationToStringAble;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.ArraysUtils;

import java.util.ArrayList;
import java.util.List;

import static cc.redberry.core.transformations.EliminateMetricsTransformation.ELIMINATE_METRICS;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class DiracSimplifyTransformation extends AbstractTransformationWithGammas
        implements TransformationToStringAble {
    private final Transformation overall;

    public DiracSimplifyTransformation(SimpleTensor gammaMatrix) {
        this(gammaMatrix, null, Complex.FOUR, Complex.FOUR, Transformation.IDENTITY);
    }

    public DiracSimplifyTransformation(SimpleTensor gammaMatrix, SimpleTensor gamma5) {
        this(gammaMatrix, gamma5, Complex.FOUR, Complex.FOUR, Transformation.IDENTITY);
    }

    public DiracSimplifyTransformation(SimpleTensor gammaMatrix, SimpleTensor gamma5, Transformation simplifications) {
        this(gammaMatrix, gamma5, Complex.FOUR, Complex.FOUR, simplifications);
    }

    public DiracSimplifyTransformation(SimpleTensor gammaMatrix, SimpleTensor gamma5,
                                       Tensor dimension, Transformation simplifications) {
        this(gammaMatrix, gamma5, dimension, guessTraceOfOne(dimension), simplifications);
    }

    public DiracSimplifyTransformation(SimpleTensor gammaMatrix, SimpleTensor gamma5,
                                       Tensor dimension, final Tensor traceOfOne,
                                       Transformation simplifications) {
        super(gammaMatrix, gamma5, null, dimension, traceOfOne);

        List<Transformation> overall = new ArrayList<>();
        if (gamma5 != null)
            overall.add(new SimplifyGamma5Transformation(gammaMatrix, gamma5));
        overall.add(new ApplySubstitutions(setupSubs()));
        overall.add(new DiracSimplify0(gammaMatrix, gamma5, dimension, traceOfOne, simplifications));
        overall.add(this.traceOfOne);
        overall.add(this.deltaTrace);
        this.overall = new TransformationCollection(overall);
    }

    private Transformation[] setupSubs() {
        ParseToken[] ss = {s1, s2, s3, s4};
        Transformation[] subs = new Transformation[ss.length];
        for (int i = 0; i < ss.length; ++i)
            subs[i] = (Transformation) deltaTrace.transform(tokenTransformer.transform(ss[i]).toTensor());
        return subs;
    }

    @Override
    public Tensor transform(Tensor tensor) {
        return Transformation.Util.applyUntilUnchanged(tensor, 1000, overall);
    }

    private final class ApplySubstitutions implements Transformation {
        final Transformation[] substitutions;

        public ApplySubstitutions(Transformation... substitutions) {
            this.substitutions = ArraysUtils.addAll(substitutions, ELIMINATE_METRICS);
        }

        @Override
        public Tensor transform(Tensor tensor) {
            SubstitutionIterator iterator = new SubstitutionIterator(tensor);
            Tensor current;
            while ((current = iterator.next()) != null) {
                if (current instanceof Product)
                    current = Transformation.Util.applyUntilUnchanged(current, substitutions);
                iterator.safeSet(current);
            }
            return iterator.result();
        }
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "DiracSimplify";
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
