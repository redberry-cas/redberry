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
import cc.redberry.core.graph.GraphType;
import cc.redberry.core.graph.PrimitiveSubgraph;
import cc.redberry.core.graph.PrimitiveSubgraphPartition;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParseToken;
import cc.redberry.core.parser.ParseUtils;
import cc.redberry.core.parser.Parser;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.ExpandAndEliminateTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.TensorUtils;

import java.util.HashMap;

import static cc.redberry.core.indices.IndicesFactory.createSimple;
import static cc.redberry.core.indices.IndicesUtils.*;
import static cc.redberry.core.tensor.Tensors.*;

/**
 * Calculate traces of gamma matrices in D dimensions (D = 4 in case of gamma5 traces).
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class DiracTraceTransformation extends AbstractTransformationWithGammas {
    private final LeviCivitaSimplifyTransformation simplifyLeviCivita;
    private final Transformation expandAndEliminate;

    /**
     * Creates transformation with specified notation for gamma matrix.
     *
     * @param gammaMatrix tensor, which will be considered as gamma matrix
     */
    public DiracTraceTransformation(final SimpleTensor gammaMatrix) {
        this(gammaMatrix, new Transformation[0]);
    }

    /**
     * Creates transformation with specified notation for gamma matrix.
     *
     * @param gammaMatrix     tensor, which will be considered as gamma matrix
     * @param simplifications additional simplification rules
     */
    public DiracTraceTransformation(final SimpleTensor gammaMatrix,
                                    final Transformation[] simplifications) {
        this(gammaMatrix, simplifications, Complex.FOUR, Complex.FOUR);
    }

    /**
     * Creates transformation with specified notation for gamma matrix.
     *
     * @param gammaMatrix     tensor, which will be considered as gamma matrix
     * @param simplifications additional simplification rules
     * @param dimension       space-time dimension (so Tr[1] = 2^(D/2) or 2^((D-1)/2))
     */
    public DiracTraceTransformation(final SimpleTensor gammaMatrix,
                                    final Transformation[] simplifications,
                                    final Tensor dimension) {
        this(gammaMatrix, simplifications, dimension, guessTraceOfOne(dimension));
    }

    /**
     * Creates transformation with specified notation for gamma matrix.
     *
     * @param gammaMatrix     tensor, which will be considered as gamma matrix
     * @param simplifications additional simplification rules
     * @param dimension       space-time dimension
     * @param traceOfOne      value of Tr[1]
     */
    public DiracTraceTransformation(final SimpleTensor gammaMatrix,
                                    final Transformation[] simplifications,
                                    final Tensor dimension,
                                    final Tensor traceOfOne) {
        super(gammaMatrix, dimension, traceOfOne);
        this.expandAndEliminate = new ExpandAndEliminateTransformation(simplifications);
        this.simplifyLeviCivita = null;
    }


    /**
     * Creates transformation with specified notations for gamma matrices and Levi-Civita tensor.
     *
     * @param gammaMatrix tensor, which will be considered as gamma matrix
     * @param gamma5      tensor, which will be considered as gamma5 matrix
     * @param leviCivita  tensor, which will be considered as Levi-Civita tensor
     */
    public DiracTraceTransformation(final SimpleTensor gammaMatrix,
                                    final SimpleTensor gamma5,
                                    final SimpleTensor leviCivita) {
        this(gammaMatrix, gamma5, leviCivita, new Transformation[0], true);
    }

    /**
     * Creates transformation with specified notations for gamma matrices and Levi-Civita tensor.
     *
     * @param gammaMatrix     tensor, which will be considered as gamma matrix
     * @param gamma5          tensor, which will be considered as gamma5 matrix
     * @param leviCivita      tensor, which will be considered as Levi-Civita tensor
     * @param simplifications additional simplification rules
     */
    public DiracTraceTransformation(final SimpleTensor gammaMatrix,
                                    final SimpleTensor gamma5,
                                    final SimpleTensor leviCivita,
                                    final Transformation[] simplifications) {
        this(gammaMatrix, gamma5, leviCivita, simplifications, true);
    }

    /**
     * Creates transformation with specified notations for gamma matrices and Levi-Civita tensor.
     *
     * @param gammaMatrix     tensor, which will be considered as gamma matrix
     * @param gamma5          tensor, which will be considered as gamma5 matrix
     * @param leviCivita      tensor, which will be considered as Levi-Civita tensor
     * @param simplifications additional simplification rules
     * @param minkowskiSpace  if {@code true}, then Levi-Civita tensor will be considered in Minkovski
     *                        space (so e.g. e_abcd*e^abcd = -24), otherwise in Euclidean space
     *                        (so e.g. e_abcd*e^abcd = +24)
     */
    public DiracTraceTransformation(final SimpleTensor gammaMatrix,
                                    final SimpleTensor gamma5,
                                    final SimpleTensor leviCivita,
                                    final Transformation[] simplifications,
                                    final boolean minkowskiSpace) {
        this(gammaMatrix, gamma5, leviCivita, simplifications, minkowskiSpace, Complex.FOUR, Complex.FOUR);
    }

    /**
     * Creates transformation with specified notations for gamma matrices and Levi-Civita tensor.
     *
     * @param gammaMatrix     tensor, which will be considered as gamma matrix
     * @param gamma5          tensor, which will be considered as gamma5 matrix
     * @param leviCivita      tensor, which will be considered as Levi-Civita tensor
     * @param simplifications additional simplification rules
     * @param minkowskiSpace  if {@code true}, then Levi-Civita tensor will be considered in Minkovski
     *                        space (so e.g. e_abcd*e^abcd = -24), otherwise in Euclidean space
     *                        (so e.g. e_abcd*e^abcd = +24)
     * @param dimension       space-time dimension (not applicable in case of gamma5, i.e. must be 4 for correct result)
     */
    public DiracTraceTransformation(final SimpleTensor gammaMatrix,
                                    final SimpleTensor gamma5,
                                    final SimpleTensor leviCivita,
                                    final Transformation[] simplifications,
                                    final boolean minkowskiSpace,
                                    final Tensor dimension) {
        this(gammaMatrix, gamma5, leviCivita, simplifications, minkowskiSpace, dimension, guessTraceOfOne(dimension));
    }

    /**
     * Creates transformation with specified notations for gamma matrices and Levi-Civita tensor.
     *
     * @param gammaMatrix     tensor, which will be considered as gamma matrix
     * @param gamma5          tensor, which will be considered as gamma5 matrix
     * @param leviCivita      tensor, which will be considered as Levi-Civita tensor
     * @param simplifications additional simplification rules
     * @param minkowskiSpace  if {@code true}, then Levi-Civita tensor will be considered in Minkovski
     *                        space (so e.g. e_abcd*e^abcd = -24), otherwise in Euclidean space
     *                        (so e.g. e_abcd*e^abcd = +24)
     * @param dimension       space-time dimension (not applicable in case of gamma5, i.e. must be 4 for correct result)
     * @param traceOfOne      value of Tr[1] (not applicable in case of gamma5, i.e. must be 4 for correct result)
     */
    public DiracTraceTransformation(final SimpleTensor gammaMatrix,
                                    final SimpleTensor gamma5,
                                    final SimpleTensor leviCivita,
                                    final Transformation[] simplifications,
                                    final boolean minkowskiSpace,
                                    final Tensor dimension,
                                    final Tensor traceOfOne) {
        super(gammaMatrix, gamma5, leviCivita, dimension, traceOfOne);
        this.expandAndEliminate = new ExpandAndEliminateTransformation(simplifications);
        this.simplifyLeviCivita = new LeviCivitaSimplifyTransformation(leviCivita, minkowskiSpace);
    }


    private Tensor expandDiracStructures(final Tensor t) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t);
        Tensor current;
        while ((current = iterator.next()) != null) {
            if (!(current instanceof Product))
                continue;

            //early termination
            if (!containsGammaOr5Matrices(current))
                continue;

            Product product = (Product) current;

            //positions of matrices
            IntArrayList positionsOfMatrices = new IntArrayList();
            int sizeOfIndexless = product.sizeOfIndexlessPart();
            PrimitiveSubgraph[] partition
                    = PrimitiveSubgraphPartition.calculatePartition(product.getContent(), matrixType);

            //traces (expand brackets)
            ProductBuilder traces = new ProductBuilder();

            traces:
            for (PrimitiveSubgraph subgraph : partition) {
                if (subgraph.getGraphType() != GraphType.Cycle)
                    continue traces;

                int[] positions = subgraph.getPartition();
                for (int i = positions.length - 1; i >= 0; --i)
                    positions[i] = positions[i] + sizeOfIndexless;

                positionsOfMatrices.addAll(positions);

                //expand each cycle
                traces.put(expandAndEliminate.transform(product.select(positions)));
            }

            if (positionsOfMatrices.isEmpty())
                continue;
            traces.put(product.remove(positionsOfMatrices.toArray()));
            iterator.set(traces.build());
        }

        return iterator.result();
    }


    @Override
    public Tensor transform(Tensor tensor) {
        if (!containsGammaOr5Matrices(tensor))
            return tensor;

        tensor = expandDiracStructures(tensor);
        tensor = EliminateMetricsTransformation.eliminate(tensor);
        FromChildToParentIterator iterator = new FromChildToParentIterator(tensor);
        Tensor current;
        out:
        while ((current = iterator.next()) != null) {
            if (TensorUtils.equals(traceOfOne.get(0), current))
                iterator.set(traceOfOne.get(1));
            else if (isGammaOrGamma5(current)
                    && current.getIndices().getFree().size(matrixType) == 0)
                iterator.set(Complex.ZERO);
            else if (current instanceof Product) {
                if (current.getIndices().getFree().size(matrixType) != 0)
                    continue;
                //fast check
                boolean needTrace = false;
                for (Tensor t : current)
                    if (isGammaOrGamma5(t)) {
                        needTrace = true;
                        break;
                    }
                if (!needTrace)
                    continue;

                //selecting unitary matrices from product
                //extracting trace combinations from product
                Product product = (Product) current;
                //positions of matrices
                IntArrayList positionsOfMatrices = new IntArrayList();
                int sizeOfIndexless = product.sizeOfIndexlessPart();
                ProductContent pc = product.getContent();
                PrimitiveSubgraph[] partition
                        = PrimitiveSubgraphPartition.calculatePartition(pc, matrixType);

                //calculated traces
                ProductBuilder traces = new ProductBuilder();

                traces:
                for (PrimitiveSubgraph subgraph : partition) {
                    if (subgraph.getGraphType() != GraphType.Cycle)
                        continue;

                    int numberOfGammas = 0, numberOfGamma5s = 0;

                    Tensor gamma;
                    //actual positions in current
                    int[] positions = subgraph.getPartition();
                    assert positions.length > 1;

                    for (int i = positions.length - 1; i >= 0; --i) {
                        positions[i] = positions[i] + sizeOfIndexless;
                        gamma = product.get(positions[i]);
                        if (gamma instanceof SimpleTensor) {
                            if (((SimpleTensor) gamma).getName() == gammaName)
                                ++numberOfGammas;
                            else if (((SimpleTensor) gamma).getName() == gamma5Name)
                                ++numberOfGamma5s;
                            else
                                //not a gamma matrix
                                continue traces;
                        } else {
                            //not a gamma matrix
                            continue traces;
                        }
                    }

                    //early terminations
                    if (numberOfGammas % 2 == 1
                            || (numberOfGammas == 2 && numberOfGamma5s % 2 == 1)) {
                        iterator.set(Complex.ZERO);
                        continue out;
                    }
                    if (numberOfGammas == 0 && numberOfGamma5s % 2 == 1) {
                        iterator.set(Complex.ZERO);
                        continue out;
                    }

                    positionsOfMatrices.addAll(positions);
                    if (numberOfGamma5s == 0)
                        traces.put(traceWithout5(product.select(positions), numberOfGammas, traceOfOne.get(1)));
                    else {
                        //early check
                        if (numberOfGammas == 0) {
                            //numberOfGamma5s % 2 == 0
                            traces.put(Complex.FOUR);
                            continue traces;
                        }


                        //eliminating excess products of gamma5s
                        if (numberOfGamma5s > 1) {
                            //take into account odd number of swaps
                            boolean sign = false;
                            //product of gammas as ordered array (will be filled without excess gamma5s)
                            final SimpleTensor[] orderedProduct = new SimpleTensor[numberOfGammas + (numberOfGamma5s % 2 == 0 ? 0 : 1)];
                            int counter = -1;

                            //index of tensor in product content, which is contracted with current gamma5
                            int positionOfPreviousGamma = -2;

                            SimpleTensor currentGamma;
                            for (int positionOfGamma = 0; positionOfGamma < positions.length; ++positionOfGamma) {
                                currentGamma = (SimpleTensor) product.get(positions[positionOfGamma]);
                                if (currentGamma.getName() == gamma5Name) {
                                    //adding one gamma5 if they are odd number
                                    if (positionOfPreviousGamma == -2) {
                                        if (numberOfGamma5s % 2 == 1) {
                                            orderedProduct[++counter] = currentGamma;
                                            positionOfPreviousGamma = -1;
                                        } else {
                                            positionOfPreviousGamma = positionOfGamma;
                                        }
                                        continue;
                                    }
                                    if (positionOfPreviousGamma == -1)
                                        positionOfPreviousGamma = positionOfGamma;
                                    else {
                                        //odd number of swaps
                                        if ((positionOfGamma - positionOfPreviousGamma) % 2 == 0)
                                            sign ^= true;
                                        positionOfPreviousGamma = -1;
                                    }
                                } else
                                    orderedProduct[++counter] = currentGamma;
                            }

                            //fixing new indices contractions
                            int u = 0, l = 0;
                            for (int i = 0; ; ++i) {
                                if (i == orderedProduct.length - 1) {
                                    orderedProduct[i] = setMatrixIndices(orderedProduct[i], u, 0);
                                    break;
                                }
                                orderedProduct[i] = setMatrixIndices(orderedProduct[i], u, ++l);
                                u = l;
                            }

                            Tensor withoutExcessGamma5s = multiply(orderedProduct);

                            if (numberOfGamma5s % 2 == 0)
                                withoutExcessGamma5s = traceWithout5(withoutExcessGamma5s, numberOfGammas, traceOfOne.get(1));
                            else {
                                withoutExcessGamma5s = traceWith5(withoutExcessGamma5s, numberOfGammas);
                                withoutExcessGamma5s = simplifyLeviCivita.transform(withoutExcessGamma5s);
                            }

                            if (sign)
                                withoutExcessGamma5s = negate(withoutExcessGamma5s);
                            traces.put(withoutExcessGamma5s);
                        } else
                            traces.put(traceWith5(product.select(positions), numberOfGammas));
                    }
                }
                if (positionsOfMatrices.isEmpty())
                    continue out;

                //final simplifications
                traces.put(product.remove(positionsOfMatrices.toArray()));
                current = traces.build();
                current = expandAndEliminate.transform(current);
                current = deltaTrace.transform(current);
                current = traceOfOne.transform(current);
                if (simplifyLeviCivita != null)
                    current = simplifyLeviCivita.transform(current);
                iterator.set(current);
            }
        }
        return iterator.result();
    }
    /*
     * *********************
     * Trace without gamma5
     * *********************
     */

    private static final class Index {
        final int numberOfGammas;
        final Tensor traceOfOne;

        public Index(int numberOfGammas, Tensor traceOfOne) {
            this.numberOfGammas = numberOfGammas;
            this.traceOfOne = traceOfOne;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Index oth = (Index) o;
            return numberOfGammas == oth.numberOfGammas && TensorUtils.equals(traceOfOne, oth.traceOfOne);
        }

        @Override
        public int hashCode() {
            return 31 * numberOfGammas + traceOfOne.hashCode();
        }
    }

    /**
     * cached substitutions of traces without 5
     */
    private final HashMap<Index, Expression> cachedSubstitutions = new HashMap<>();

    private Tensor traceWithout5(Tensor tensor, int numberOfGammas, Tensor traceOfOne) {
        Index key = new Index(numberOfGammas, traceOfOne);
        Expression substitution = cachedSubstitutions.get(key);
        if (substitution == null) {
            ParseToken rawSubstitution = createRawGammaSubstitution(numberOfGammas, traceOfOne);
            substitution = (Expression) tokenTransformer.transform(rawSubstitution).toTensor();
            cachedSubstitutions.put(key, substitution);
        }

        tensor = substitution.transform(tensor);
        tensor = ExpandAndEliminateTransformation.expandAndEliminate(tensor);
        tensor = deltaTrace.transform(tensor);
        tensor = this.traceOfOne.transform(tensor);
        return tensor;
    }

    synchronized
    private static ParseToken createRawGammaSubstitution(int numberOfGammas, Tensor traceOfOne) {
        Index key = new Index(numberOfGammas, traceOfOne);
        ParseToken substitution = cachedRawGammaTraces.get(key);
        if (substitution == null) {

            //product of gamma matrices as array
            Tensor[] gammas = new Tensor[numberOfGammas];
            int matrixIndex = setType(IndexType.Matrix1, 0) - 1, metricIndex = -1;
            int firstUpper, u = firstUpper = ++matrixIndex, i;
            for (i = 0; i < numberOfGammas; ++i) {
                gammas[i] = Tensors.simpleTensor(gammaMatrixStringName,
                        createSimple(null,
                                u | 0x80000000,
                                i == numberOfGammas - 1 ? firstUpper : (u = ++matrixIndex),
                                ++metricIndex));

            }
            Expression expression = expression(Tensors.multiply(gammas), traceOfArray(gammas, traceOfOne));
            substitution = ParseUtils.tensor2AST(expression);
            cachedRawGammaTraces.put(key, substitution);
        }
        return substitution;
    }

    private static Tensor traceOfArray(Tensor[] product, Tensor traceOfOne) {
        //calculates trace using recursive algorithm
        if (product.length == 1)
            return Complex.ZERO;
        if (product.length == 2)
            return multiply(traceOfOne,
                    createMetricOrKronecker(product[0].getIndices().get(IndexType.LatinLower, 0),
                            product[1].getIndices().get(IndexType.LatinLower, 0)));
        if (product.length % 2 != 0)
            return Complex.ZERO;
        SumBuilder sb = new SumBuilder();
        Tensor temp;
        //todo why to multiply by Complex.TWO and after Complex.ONE_HALF???
        for (int i = 0; i < product.length - 1; ++i) {
            temp = multiply(Complex.TWO,
                    createMetricOrKronecker(product[i].getIndices().get(IndexType.LatinLower, 0),
                            product[i + 1].getIndices().get(IndexType.LatinLower, 0)),
                    traceOfArray(subArray(product, i, i + 1), traceOfOne));
            if (i % 2 != 0)
                temp = negate(temp);
            sb.put(temp);
            swap(product, i, i + 1);
        }
        return multiply(Complex.ONE_HALF, sb.build());
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

    /*
    * Cached parse tokens
    */
    private static HashMap<Index, ParseToken> cachedRawGammaTraces = new HashMap<>();

    public static void resetCache() {
        cachedRawGammaTraces = new HashMap<>();
    }

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
    private Transformation chiholmKahaneIdentity;


    private Tensor traceWith5(Tensor product, int numberOfGammas) {
        if (traceOf4GammasWith5 == null) {
            traceOf4GammasWith5 = (Expression) tokenTransformer.transform(traceOf4GammasWith5Token).toTensor();
            chiholmKahaneIdentity = (Expression) tokenTransformer.transform(chiholmKahaneToken).toTensor();
        }

        if (numberOfGammas == 4)
            return traceOf4GammasWith5.transform(product);

        product = chiholmKahaneIdentity.transform(product);
        product = ExpandAndEliminateTransformation.expandAndEliminate(product);
        product = traceOf4GammasWith5.transform(product);
        product = simplifyLeviCivita.transform(product);
        product = deltaTrace.transform(product);
        product = traceOfOne.transform(product);
        return transform(product);
    }

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

    static {
        parser = CC.current().getParseManager().getParser();
        traceOf4GammasWith5Token = parser.parse("G_a^a'_b'*G_b^b'_c'*G_c^c'_d'*G_d^d'_e'*G5^e'_a' = -4*I*eps_abcd");
        chiholmKahaneToken = parser.parse("G_a^a'_c'*G_b^c'_d'*G_c^d'_b' = g_ab*G_c^a'_b'-g_ac*G_b^a'_b'+g_bc*G_a^a'_b'-I*e_abcd*G5^a'_c'*G^dc'_b'");
    }
}
