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
import cc.redberry.core.context.NameAndStructureOfIndices;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParseToken;
import cc.redberry.core.parser.Parser;
import cc.redberry.core.parser.preprocessor.ChangeIndicesTypesAndTensorNames;
import cc.redberry.core.parser.preprocessor.TypesAndNamesTransformer;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import cc.redberry.core.transformations.TransformationToStringAble;
import cc.redberry.core.transformations.options.Creator;
import cc.redberry.core.transformations.options.Options;

import java.util.ArrayList;

import static cc.redberry.core.tensor.Tensors.parseExpression;
import static cc.redberry.physics.feyncalc.TraceUtils.*;

/**
 * Applies simplification rules to combinations of unitary matrices.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class UnitarySimplifyTransformation implements TransformationToStringAble {
    final Transformation unitarySimplifications;

    @Creator
    public UnitarySimplifyTransformation(@Options UnitarySimplifyOptions options) {
        this(options.unitaryMatrix, options.structureConstant, options.symmetricConstant, options.dimension);
    }

    /**
     * Creates transformation with given definitions.
     *
     * @param unitaryMatrix     unitary matrix
     * @param structureConstant structure constants of SU(N)
     * @param symmetricConstant symmetric constants of SU(N)
     * @param dimension         dimension
     */
    public UnitarySimplifyTransformation(final SimpleTensor unitaryMatrix,
                                         final SimpleTensor structureConstant,
                                         final SimpleTensor symmetricConstant,
                                         final Tensor dimension) {

        checkUnitaryInput(unitaryMatrix, structureConstant, symmetricConstant, dimension);
        final IndexType[] types = extractTypesFromMatrix(unitaryMatrix);

        ChangeIndicesTypesAndTensorNames tokenTransformer = new ChangeIndicesTypesAndTensorNames(new TypesAndNamesTransformer() {
            @Override
            public int newIndex(int oldIndex, NameAndStructureOfIndices oldDescriptor) {
                return oldIndex;
            }

            @Override
            public IndexType newType(IndexType oldType, NameAndStructureOfIndices old) {
                if (oldType == IndexType.LatinLower)
                    return types[0];
                if (oldType == IndexType.Matrix1)
                    return types[1];
                return oldType;
            }

            @Override
            public String newName(String oldName, NameAndStructureOfIndices old) {
                switch (oldName) {
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
                        return oldName;
                }
            }
        });

        //simplifications with SU(N) combinations
        ArrayList<Transformation> unitarySimplifications = new ArrayList<>();

        if (dimension instanceof Complex) {
            Transformation nSub = parseExpression("N = " + dimension);
            for (ParseToken substitution : unitarySimplificationsTokens)
                unitarySimplifications.add((Transformation) nSub.transform(tokenTransformer.transform(substitution).toTensor()));
        } else
            for (ParseToken substitution : unitarySimplificationsTokens)
                unitarySimplifications.add((Transformation) tokenTransformer.transform(substitution).toTensor());

        //all simplifications
        ArrayList<Transformation> simplifications = new ArrayList<>();
        simplifications.add(EliminateMetricsTransformation.ELIMINATE_METRICS);
        simplifications.addAll(unitarySimplifications);
        this.unitarySimplifications = new TransformationCollection(simplifications);
    }

    UnitarySimplifyTransformation(final Tensor dimension,
                                  ChangeIndicesTypesAndTensorNames tokenTransformer) {
        //simplifications with SU(N) combinations
        ArrayList<Transformation> unitarySimplifications = new ArrayList<>();

        if (dimension instanceof Complex) {
            Transformation nSub = parseExpression("N = " + dimension);
            for (ParseToken substitution : unitarySimplificationsTokens)
                unitarySimplifications.add((Transformation) nSub.transform(tokenTransformer.transform(substitution).toTensor()));
        } else
            for (ParseToken substitution : unitarySimplificationsTokens)
                unitarySimplifications.add((Transformation) tokenTransformer.transform(substitution).toTensor());

        //all simplifications
        ArrayList<Transformation> simplifications = new ArrayList<>();
        simplifications.add(EliminateMetricsTransformation.ELIMINATE_METRICS);
        simplifications.addAll(unitarySimplifications);
        this.unitarySimplifications = new TransformationCollection(simplifications);
    }

    @Override
    public Tensor transform(Tensor t) {
        Tensor old = t;
        while (true) {
            t = unitarySimplifications.transform(old);
            if (t == old)
                break;
            old = t;
        }
        return t;
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "UnitarySimplify";
    }

    @Override
    public String toString() {
        return toString(CC.getDefaultOutputFormat());
    }

    private static final Parser parser;

    /**
     * T_a*T_a = (N**2-1)/(2*N)
     */
    private static final ParseToken contraction1Token;

    /**
     * T_a*T_b*T_a = -T_b/(2*N)
     */
    private static final ParseToken contraction2Token;


    /**
     * d_apq*d_b^pq = (N**2 - 4)/N * g_ab
     */
    private static final ParseToken symmetricCombinationToken;

    /**
     * d_a^ab = 0
     */
    private static final ParseToken symmetricTraceToken;

    /**
     * f_apq*f_b^pq = N * g_ab
     */
    private static final ParseToken aSymmetricCombinationToken;

    /**
     * d_iac*f_hbc*f_ghi = -N/2 * d_abg
     */
    private static final ParseToken triple1;
    /**
     * d_iac*d_hbc*f_ghi = (4-N^2)/2N f_abg
     */
    private static final ParseToken triple2;
    /**
     * d_iac*d_hbc*d_ghi = (N^2-12)/2N d_abg
     */
    private static final ParseToken triple3;
    /**
     * f_iac*f_hbc*f_ghi = -N/2 f_abg
     */
    private static final ParseToken triple4;
    /**
     * f_a^ab = 0
     */
    private static final ParseToken aSymmetricTraceToken;

    /**
     * f_apq*d_b^pq = 0
     */
    private static final ParseToken symmetrySimplificationToken;

    /**
     * d^a_a = N*(N-1)/2
     */
    private static final ParseToken numberOfGeneratorsToken;

    /**
     * Tr[1] = N
     */
    private static final ParseToken dimensionToken;

    private static final ParseToken[] unitarySimplificationsTokens;

    static {
        parser = CC.current().getParseManager().getParser();

        contraction1Token = parser.parse("T_a^a'_b'*T^ab'_c' = (N**2-1)/(2*N)*d^a'_c'");
        contraction2Token = parser.parse("T_a^a'_b'*T_b^b'_c'*T^ac'_d' = -T_b^a'_d'/(2*N)");
        symmetricCombinationToken = parser.parse("D_apq*D_b^pq = (N**2 - 4)/N * g_ab");
        symmetricTraceToken = parser.parse("D_a^ab = 0");
        aSymmetricTraceToken = parser.parse("F_a^ab = 0");
        aSymmetricCombinationToken = parser.parse("F_apq*F_b^pq = N * g_ab");
        symmetrySimplificationToken = parser.parse("F_apq*D_b^pq = 0");
        numberOfGeneratorsToken = parser.parse("d^a_a = N**2-1");
        dimensionToken = parser.parse("d^a'_a' = N");

        triple1 = parser.parse("D_iac*F_hb^c*F_g^hi = -N/2 * D_abg");
        triple2 = parser.parse("D_iac*D_hb^c*F_g^hi = (4-N**2)/(2*N)*F_abg");
        triple3 = parser.parse("D_iac*D_hb^c*D_g^hi = (N**2-12)/(2*N)*D_abg");
        triple4 = parser.parse("F_iac*F_hb^c*F_g^hi = -N/2*F_abg");

        unitarySimplificationsTokens = new ParseToken[]{
                contraction1Token, contraction2Token,
                symmetricCombinationToken, aSymmetricCombinationToken, symmetricTraceToken,
                aSymmetricTraceToken, symmetrySimplificationToken, numberOfGeneratorsToken, dimensionToken,
                triple1, triple2, triple3, triple4};
    }

}
