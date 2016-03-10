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
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParseToken;
import cc.redberry.core.parser.Parser;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import cc.redberry.core.transformations.options.Creator;
import cc.redberry.core.transformations.options.Options;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
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
    private final boolean cache;

    @Creator
    public DiracTraceTransformation(@Options DiracOptions options) {
        super(doLC(options), new SimplifyGamma5Transformation(options));
        this.simplifyLeviCivita = options.simplifyLeviCivita;
        this.cache = options.cache;
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
            final ProductContent pc = product.getContent();
            PrimitiveSubgraph[] partition
                    = PrimitiveSubgraphPartition.calculatePartition(pc, matrixType);

            //traces (expand brackets)
            boolean containsTraces = false, containsSums = false;
            traces:
            for (PrimitiveSubgraph subgraph : partition) {
                if (subgraph.getGraphType() != GraphType.Cycle)
                    continue traces;
                //expand each cycle
                containsTraces = true;
                for (int i = 0; i < subgraph.size(); ++i) {
                    containsSums = pc.get(subgraph.getPosition(i)) instanceof Sum;
                    if (containsSums)
                        break traces;
                }
            }

            if (containsTraces && containsSums)
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
        tensor = deltaTraces.transform(tensor);
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

        return trace(pg.toProduct(), pg.length, pg.g5Positions.isEmpty());
    }

    private final TIntObjectHashMap<Tensor[]> globalCache = new TIntObjectHashMap<>();

    private Tensor trace(final Tensor productOfGammas, final int numberOfGammas, final boolean without5) {
        if (!cache)
            return trace_do_calc(productOfGammas, numberOfGammas, without5);

        int hashCode = ((Product) productOfGammas).iHashCode();
        Tensor[] r = globalCache.get(hashCode);
        if (r == null) {
            Tensor res = trace_do_calc(productOfGammas, numberOfGammas, without5);
            globalCache.put(hashCode, new Tensor[]{productOfGammas, res});
            return res;
        }
        Mapping mapping = IndexMappings.getFirst(r[0], productOfGammas);
        if (mapping == null)
            return trace_do_calc(productOfGammas, numberOfGammas, without5);

        return ApplyIndexMapping.applyIndexMapping(r[1], mapping);
    }

    private Tensor trace_do_calc(final Tensor productOfGammas, final int numberOfGammas, final boolean without5) {
        return without5 ? traceWithout5_do_calc(productOfGammas, numberOfGammas)
                : traceWith5_do_calc(productOfGammas, numberOfGammas);
    }

    private Tensor traceWithout5_do_calc(Tensor productOfGammas, final int numberOfGammas) {
        productOfGammas = getTraceSubstitution(numberOfGammas).transform(productOfGammas);
        productOfGammas = EliminateMetricsTransformation.eliminate(productOfGammas);
        productOfGammas = deltaTraces.transform(productOfGammas);
        return productOfGammas;
    }

    private final TIntObjectHashMap<Expression> cachedTraces = new TIntObjectHashMap<>();

    private Expression getTraceSubstitution(final int length) {
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

    private Tensor traceOfArray(final Tensor[] data) {
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

    private Tensor traceWith5_do_calc(Tensor product, final int numberOfGammas) {
        if (traceOf4GammasWith5 == null) {
            traceOf4GammasWith5 = (Expression) tokenTransformer.transform(traceOf4GammasWith5Token).toTensor();
            chiholmKahaneIdentity = (Expression) tokenTransformer.transform(chiholmKahaneToken).toTensor();
            chiholmKahaneIdentityReversed = (Expression) tokenTransformer.transform(chiholmKahaneTokenReversed).toTensor();
            chiholmKahaneIdentityReversed = (Expression) deltaTraces.transform(chiholmKahaneIdentityReversed);
        }

        if (numberOfGammas == 5)//including one gama5
            product = traceOf4GammasWith5.transform(product);
        else {
            product = chiholmKahaneIdentityReversed.transform(product);
            //no gamma5 leaved
            product = getTraceSubstitution(numberOfGammas + 1).transform(product);
        }
        product = expandAndEliminate.transform(product);
        product = deltaTraces.transform(product);
        if (simplifyLeviCivita != null) {
            product = simplifyLeviCivita.transform(product);
            product = deltaTraces.transform(product);
        }
        return product;
    }

    private static Tensor[] subArray(final Tensor[] array, final int a, final int b) {
        Tensor[] result = new Tensor[array.length - 2];
        int k = 0;
        for (int i = 0; i < array.length; ++i) {
            if (i == a || i == b)
                continue;
            result[k++] = array[i];
        }
        return result;
    }

    private static void swap(final Tensor[] array, final int a, final int b) {
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
        chiholmKahaneTokenReversed = parser.parse("G5^a'_c'*G^dc'_b' = -I*e^abcd*G_a^a'_c'*G_b^c'_d'*G_c^d'_b'/(4-3)/(4-2)/(4-1)");
    }
}
