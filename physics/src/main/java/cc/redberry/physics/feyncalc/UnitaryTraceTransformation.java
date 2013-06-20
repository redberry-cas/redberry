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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.NameAndStructureOfIndices;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.graph.GraphType;
import cc.redberry.core.graph.PrimitiveSubgraph;
import cc.redberry.core.graph.PrimitiveSubgraphPartition;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParseToken;
import cc.redberry.core.parser.Parser;
import cc.redberry.core.parser.preprocessor.ChangeIndicesTypesAndTensorNames;
import cc.redberry.core.parser.preprocessor.TypesAndNamesTransformer;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.ExpandAndEliminateTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.IntArrayList;

import static cc.redberry.core.tensor.Tensors.multiply;
import static cc.redberry.core.tensor.Tensors.parseExpression;
import static cc.redberry.physics.feyncalc.TraceUtils.*;

/**
 * Calculates trace of unitary matrices.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class UnitaryTraceTransformation implements Transformation {
    private final int unitaryMatrix;
    private final IndexType matrixType;

    private final Expression pairProduct;
    private final Expression singleTrace;
    private final Transformation simplifications;

    /**
     * Creates transformation with given definitions.
     *
     * @param unitaryMatrix     unitary matrix
     * @param structureConstant structure constants of SU(N)
     * @param symmetricConstant symmetric constants of SU(N)
     * @param dimension         dimension
     */
    public UnitaryTraceTransformation(final SimpleTensor unitaryMatrix,
                                      final SimpleTensor structureConstant,
                                      final SimpleTensor symmetricConstant,
                                      final Tensor dimension) {
        checkUnitaryInput(unitaryMatrix, structureConstant, symmetricConstant, dimension);
        this.unitaryMatrix = unitaryMatrix.getName();
        final IndexType[] types = extractTypesFromMatrix(unitaryMatrix);
        this.matrixType = types[1];

        ChangeIndicesTypesAndTensorNames tokenTransformer = new ChangeIndicesTypesAndTensorNames(new TypesAndNamesTransformer() {
            @Override
            public IndexType newType(IndexType oldType, NameAndStructureOfIndices old) {
                if (oldType == IndexType.LatinLower)
                    return types[0];
                if (oldType == IndexType.Matrix1)
                    return types[1];
                return oldType;
            }

            @Override
            public String newName(NameAndStructureOfIndices old) {
                switch (old.getName()) {
                    case unitaryMatrixName:
                        return unitaryMatrix.getStringName();
                    case structureConstantName:
                        return structureConstant.getStringName();
                    case symmetricConstantName:
                        return symmetricConstant.getStringName();
                    case dimensionName:
                        if (!(dimension instanceof Complex))
                            return dimension.toString(OutputFormat.Redberry);
                    default:
                        return old.getName();
                }
            }
        });

        Expression pairProduct = (Expression) tokenTransformer.transform(pairProductToken).toTensor();
        if (dimension instanceof Complex)
            pairProduct = (Expression) parseExpression("N = " + dimension).transform(pairProduct);
        this.pairProduct = pairProduct;

        this.singleTrace = (Expression) tokenTransformer.transform(singleTraceToken).toTensor();
        //simplifications with SU(N) combinations
        this.simplifications = new UnitarySimplifyTransformation(dimension, tokenTransformer);
    }

    @Override
    public Tensor transform(Tensor t) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t);
        Tensor c;
        while ((c = iterator.next()) != null) {
            if (c instanceof SimpleTensor) {
                // Tr[T_a] = 0
                if (((SimpleTensor) c).getName() == unitaryMatrix && c.getIndices().getOfType(matrixType).getFree().size() == 0)
                    iterator.set(Complex.ZERO);
            } else if (c instanceof Product) {
                //selecting unitary matrices from product
                //extracting trace combinations from product
                Product product = (Product) c;
                int sizeOfIndexless = product.sizeOfIndexlessPart();
                ProductContent productContent = product.getContent();
                PrimitiveSubgraph[] subgraphs
                        = PrimitiveSubgraphPartition.calculatePartition(productContent, matrixType);

                //no unitary matrices in product
                if (subgraphs.length == 0)
                    continue;

                //positions of unitary matrices
                IntArrayList positionsOfMatrices = new IntArrayList();
                //calculated traces
                ProductBuilder calculatedTraces = new ProductBuilder();

                out:
                for (PrimitiveSubgraph subgraph : subgraphs) {
                    //not a trace
                    if (subgraph.getGraphType() != GraphType.Cycle)
                        continue;

                    //positions of unitary matrices
                    int[] partition = subgraph.getPartition();
                    for (int i = partition.length - 1; i >= 0; --i) {
                        partition[i] = sizeOfIndexless + partition[i];
                        //contains not only unitary matrices
                        if (!isUnitaryMatrix(product.get(partition[i]), unitaryMatrix))
                            continue out;
                    }

                    //calculate trace
                    calculatedTraces.put(traceOfProduct(product.select(partition)));
                    positionsOfMatrices.addAll(partition);
                }
                //compiling the result
                c = product.remove(positionsOfMatrices.toArray());
                c = multiply(c, calculatedTraces.build());
                iterator.set(simplifications.transform(c));
            }
        }
        return simplifications.transform(iterator.result());
    }

    private Tensor traceOfProduct(Tensor tensor) {
        Tensor oldTensor = tensor, newTensor;
        while (true) {
            newTensor = oldTensor;
            newTensor = simplifications.transform(newTensor);
            newTensor = singleTrace.transform(newTensor);
            newTensor = pairProduct.transform(newTensor);
            newTensor = ExpandAndEliminateTransformation.expandAndEliminate(newTensor);
            if (newTensor == oldTensor)
                break;
            oldTensor = newTensor;
        }
        return newTensor;
    }

    private static final boolean isUnitaryMatrix(Tensor tensor, int unitaryMatrix) {
        return tensor instanceof SimpleTensor && ((SimpleTensor) tensor).getName() == unitaryMatrix;
    }

    /*
     * Substitutions
     */

    private static final Parser parser;
    /**
     * T_a*T_b  = 1/2N g_ab + I/2*f_abc*T^c + 1/2*d_abc*T^c
     */
    private static final ParseToken pairProductToken;

    private static final ParseToken singleTraceToken;

    static {
        parser = CC.current().getParseManager().getParser();

        pairProductToken = parser.parse("T_a^a'_c'*T_b^c'_b' = 1/(2*N)*g_ab*d^a'_b' + I/2*F_abc*T^ca'_b' + 1/2*D_abc*T^ca'_b'");
        singleTraceToken = parser.parse("T_a^a'_a' = 0");
    }


}
