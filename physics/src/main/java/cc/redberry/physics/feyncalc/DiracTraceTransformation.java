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
import cc.redberry.core.graph.GraphType;
import cc.redberry.core.graph.PrimitiveSubgraph;
import cc.redberry.core.graph.PrimitiveSubgraphPartition;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParseToken;
import cc.redberry.core.parser.Parser;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import cc.redberry.core.transformations.options.Creator;
import cc.redberry.core.transformations.options.Options;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.transformations.substitutions.SubstitutionTransformation;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.IntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

import static cc.redberry.core.indices.IndicesFactory.createSimple;
import static cc.redberry.core.indices.IndicesUtils.setType;
import static cc.redberry.core.tensor.Tensors.*;

/**
 * Calculate traces of gamma matrices in D dimensions (D = 4 in case of gamma5 traces).
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class DiracTraceTransformation extends AbstractFeynCalcTransformation {
    private final Transformation simplifyLeviCivita;

    @Creator
    public DiracTraceTransformation(@Options DiracOptions options) {
        super(doLC(options), new SimplifyGamma5Transformation(options));
        this.simplifyLeviCivita = options.simplifyLeviCivita;
    }

    private static DiracOptions doLC(DiracOptions options) {
        if (options.simplifyLeviCivita == null)
            return options;
        return options.setExpand(new TransformationCollection(
                options.expandAndEliminate, options.simplifyLeviCivita));
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "DiracTrace";
    }

    private Tensor expandDiracStructures(final Tensor t) {
        SubstitutionIterator iterator = new SubstitutionIterator(t);
        Tensor current;
        while ((current = iterator.next()) != null) {
            if (!(current instanceof Product))
                continue;

            //early termination
            if (!containsGammaOr5Matrices(current))
                continue;

            Product product = (Product) current;

            //positions of matrices
            PrimitiveSubgraph[] partition
                    = PrimitiveSubgraphPartition.calculatePartition(product.getContent(), matrixType);

            //traces (expand brackets)
            boolean containsTraces = false;
            traces:
            for (PrimitiveSubgraph subgraph : partition) {
                if (subgraph.getGraphType() != GraphType.Cycle)
                    continue traces;
                //expand each cycle
                containsTraces = true;
            }

            if (containsTraces)
                iterator.safeSet(multiply(product.getIndexlessSubProduct(),
                        expandAndEliminate.transform(product.getDataSubProduct())));
        }

        return iterator.result();
    }

    @Override
    public Tensor transform(Tensor tensor) {
        if (!containsGammaOr5Matrices(tensor))
            return tensor;

        tensor = expandDiracStructures(tensor);
        tensor = new SubstitutionTransformation(new Expression[]{traceOfOne, deltaTrace}).transform(tensor);
        return super.transform(tensor);
    }

    @Override
    protected Indicator<GraphType> graphFilter() {
        return traceFilter;
    }

    @Override
    protected Tensor transformLine(ProductOfGammas pg, IntArrayList modifiedElements) {
        assert pg.g5Positions.size() == 0 || (pg.g5Positions.size() == 1 && pg.g5Positions.first() == pg.length - 1)
                : "G5s are not simplified";
        assert pg.graphType == GraphType.Cycle;

        Tensor p = pg.toProduct();
        if (pg.g5Positions.isEmpty())
            return traceWithout5(p, pg.length);
        else
            return traceWith5(p, pg.length);
    }

    private TIntObjectHashMap<Expression> cachedTraces = new TIntObjectHashMap<>();

    private Expression getTraceSubstitution(int length) {
        Expression trace = cachedTraces.get(length);
        if (trace == null) {
            //product of gamma matrices as array
            Tensor[] data = new Tensor[length];
            int matrixIndex = setType(matrixType, 0) - 1, metricIndex = -1;
            int firstUpper, u = firstUpper = ++matrixIndex, i;
            for (i = 0; i < length; ++i) {
                data[i] = Tensors.simpleTensor(gammaMatrixStringName,
                        createSimple(null,
                                u | 0x80000000,
                                i == length - 1 ? firstUpper : (u = ++matrixIndex),
                                setType(metricType, ++metricIndex)));

            }
            Tensor rhs = traceOfArray(data);
            rhs = expandAndEliminate.transform(rhs);
            cachedTraces.put(length, trace = expression(multiply(data), rhs));
        }
        return trace;
    }

    private Tensor traceOfArray(Tensor[] data) {
        //calculates trace using recursive algorithm
        if (data.length == 1)
            return Complex.ZERO;
        if (data.length == 2)
            return multiply(traceOfOne.get(1),
                    createMetricOrKronecker(data[0].getIndices().get(metricType, 0),
                            data[1].getIndices().get(metricType, 0)));
        if (data.length % 2 != 0)
            return Complex.ZERO;
        SumBuilder sb = new SumBuilder();
        Tensor temp;
        for (int i = 0; i < data.length - 1; ++i) {
            temp = multiply(Complex.TWO,
                    createMetricOrKronecker(data[i].getIndices().get(metricType, 0),
                            data[i + 1].getIndices().get(metricType, 0)),
                    traceOfArray(subArray(data, i, i + 1)));
            if (i % 2 != 0)
                temp = negate(temp);
            sb.put(temp);
            swap(data, i, i + 1);
        }
        return multiply(Complex.ONE_HALF, sb.build());
    }

    private Tensor traceWithout5(Tensor product, int numberOfGammas) {
        product = getTraceSubstitution(numberOfGammas).transform(product);
        product = EliminateMetricsTransformation.eliminate(product);
        product = deltaTrace.transform(product);
        product = traceOfOne.transform(product);
        return product;
    }

    private Tensor traceWith5(Tensor product, int numberOfGammas) {
        if (traceOf4GammasWith5 == null) {
            traceOf4GammasWith5 = (Expression) tokenTransformer.transform(traceOf4GammasWith5Token).toTensor();
            chiholmKahaneIdentity = (Expression) tokenTransformer.transform(chiholmKahaneToken).toTensor();
            chiholmKahaneIdentityReversed = (Expression) tokenTransformer.transform(chiholmKahaneTokenReversed).toTensor();
            chiholmKahaneIdentityReversed = (Expression) deltaTrace.transform(chiholmKahaneIdentityReversed);
        }

        if (numberOfGammas == 5)//including one gama5
            product = traceOf4GammasWith5.transform(product);
        else {
            product = chiholmKahaneIdentityReversed.transform(product);
            //no gamma5 leaved
            product = getTraceSubstitution(numberOfGammas + 1).transform(product);
        }
        product = expandAndEliminate.transform(product);
        product = deltaTrace.transform(product);
        product = traceOfOne.transform(product);
        if (simplifyLeviCivita != null) {
            product = simplifyLeviCivita.transform(product);
            product = deltaTrace.transform(product);
            product = traceOfOne.transform(product);
        }
        return product;
    }

    private static Tensor[] subArray(Tensor[] array, int a, int b) {
        Tensor[] result = new Tensor[array.length - 2];
        int k = 0;
        for (int i = 0; i < array.length; ++i) {
            if (i == a || i == b)
                continue;
            result[k++] = array[i];
        }
        return result;
    }

    private static void swap(Tensor[] array, int a, int b) {
        Tensor temp = array[a];
        array[a] = array[b];
        array[b] = temp;
    }

    private static Indicator<GraphType> traceFilter = new Indicator<GraphType>() {
        @Override
        public boolean is(GraphType object) {
            return object == GraphType.Cycle;
        }
    };

    /*
     * *********************
     * Trace with gamma5
     * *********************
     */
    //cached substitutions of traces with 5
    /**
     * Tr[G_a*G_b*G_c*G_d*G5] = -4*I*e_abcd
     */
    private Transformation traceOf4GammasWith5;
    /**
     * Chiholm-Kahane identitie:
     * G_a*G_b*G_c = g_ab*G_c-g_ac*G_b+g_bc*G_a-I*e_abcd*G5*G^d
     */
    private Expression chiholmKahaneIdentity, chiholmKahaneIdentityReversed;

    private static final Parser parser;
    /**
     * Tr[G_a*G_b*G_c*G_d*G5] = -4*I*e_abcd
     */
    private static final ParseToken traceOf4GammasWith5Token;
    /**
     * Chiholm-Kahane identitie:
     * G_a*G_b*G_c = g_ab*G_c-g_ac*G_b+g_bc*G_a-I*e_abcd*G5*G^d
     */
    private static final ParseToken chiholmKahaneToken;
    /**
     * Chiholm-Kahane identitie:
     * G5*G^d  = -I*e_abcd*G_a*G_b*G_c/(D-3)/(D-2)/(D-1)
     */
    private static final ParseToken chiholmKahaneTokenReversed;

    static {
        parser = CC.current().getParseManager().getParser();
        traceOf4GammasWith5Token = parser.parse("G_a^a'_b'*G_b^b'_c'*G_c^c'_d'*G_d^d'_e'*G5^e'_a' = -4*I*eps_abcd");
        chiholmKahaneToken = parser.parse("G_a^a'_c'*G_b^c'_d'*G_c^d'_b' = g_ab*G_c^a'_b'-g_ac*G_b^a'_b'+g_bc*G_a^a'_b'-I*e_abcd*G5^a'_c'*G^dc'_b'");
        chiholmKahaneTokenReversed = parser.parse("G5^a'_c'*G^dc'_b' = -I*e^abcd*G_a^a'_c'*G_b^c'_d'*G_c^d'_b'/(d^n_n-3)/(d^n_n-2)/(d^n_n-1)");
    }
}
