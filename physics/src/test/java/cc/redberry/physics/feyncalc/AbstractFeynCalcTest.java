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

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static cc.redberry.core.indices.IndexType.Matrix1;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.transformations.ExpandAndEliminateTransformation.expandAndEliminate;
import static cc.redberry.core.transformations.Transformation.IDENTITY;

/**
 * Created by poslavsky on 02/01/16.
 */
public class AbstractFeynCalcTest {
    protected GeneralIndicesInsertion indicesInsertion;
    protected DiracOrderTransformation dOrder;
    protected DiracTraceTransformation dTrace;
    protected DiracSimplify1 _dSimplify1;
    protected DiracSimplify0 _dSimplify0;
    protected Transformation dSimplify0, dSimplify1, dSimplify;
    protected Expression delDummy, traceOfOne, deltaTrace;
    protected Transformation simplifyG5;
    protected Transformation simplifyLeviCivita;
    protected SchoutenIdentities4 schouten4;
    protected DiracOptions diracOptions;

    @Before
    public void setUp() throws Exception {
        setUp(System.currentTimeMillis());
    }

    public void setUp(long seed) throws Exception {
        CC.reset();
        CC.resetTensorNames(seed);
        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        CC.setParserAllowsSameVariance(true);

        indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("Dummy^a'_b'"), Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("AMATRIX^a'_b'"), Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("GS^a'_b'[p_a]"), Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("T^A'_B'"), IndexType.Matrix2);


        parse("GS[p_a] := p_a*G^a");
        parse("Pair[p_a, q_a] := p_a*q^a");
        setAntiSymmetric("e_abcd");

        diracOptions = new DiracOptions();
        dOrder = new DiracOrderTransformation(diracOptions);
        dTrace = new DiracTraceTransformation(diracOptions);
        deltaTrace = parseExpression("d^a_a = 4");
        simplifyG5 = new SimplifyGamma5Transformation(diracOptions);

        _dSimplify0 = new DiracSimplify0(diracOptions);
        _dSimplify1 = new DiracSimplify1(diracOptions);

        dSimplify0 = new TransformationCollection(simplifyG5, _dSimplify0);
        dSimplify1 = new TransformationCollection(simplifyG5, _dSimplify1);
        dSimplify = new DiracSimplifyTransformation(diracOptions);

        delDummy = parseExpression("Dummy = 1");
        traceOfOne = parseExpression("d^a'_a' = 4");

        simplifyLeviCivita = new LeviCivitaSimplifyTransformation(parseSimple("e_abcd"), true);
        schouten4 = new SchoutenIdentities4(parseSimple("e_abcd"));
    }


    protected void testFeynCalcData(Transformation ds, String resourceFile) throws Exception {
        testFeynCalcData(ds, resourceFile, false, false);
    }

    protected void testFeynCalcData(Transformation ds, String resourceFile,
                                    boolean doTrace, boolean addGamma5) throws Exception {
        testFeynCalcData(ds, resourceFile, doTrace, addGamma5, true);
    }

    protected void testFeynCalcData(Transformation ds, String resourceFile,
                                    boolean doTrace, boolean addGamma5,
                                    boolean insertDummy) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                DiracSimplify0.class.getResourceAsStream(resourceFile)));

        Tensor test, expected;
        String line;
        int totalTests = 0;
        Transformation addon = doTrace ? dTrace : IDENTITY;
        while ((line = reader.readLine()) != null) {
            ++totalTests;
            String initial = line;
            if (addGamma5)
                initial = "(" + initial + ")*G5";
            if (doTrace)
                initial = "Tr[" + initial + "]";
            test = parse(initial);

            String answer = reader.readLine();
            if (insertDummy)
                answer = "(" + answer + ")*Dummy";
            if (addGamma5)
                answer = "(" + answer + ")*G5";
            if (doTrace)
                answer = "Tr[" + answer + "]";
            expected = parse(answer);
            expected = delDummy.transform(expected);
            expected = expandAndEliminate(expected);
            expected = traceOfOne.transform(expected);

            Tensor result = ds.transform(test);
            try {
                assertEquals(expected, result, addon);
            } catch (Throwable err) {
                System.out.println("Original:");
                System.out.println(initial);
                System.out.println("Answer:");
                System.out.println(answer);
                System.out.println("Redberry:");
                System.out.println(result);
                throw err;
            }
        }

        System.out.println(totalTests + " tests passed");
    }

    protected void assertEquals(String a, Tensor b) {
        assertEquals(parse(a), b);
    }

    protected void assertEquals(Tensor a, Tensor b) {
        assertEquals(a, b, IDENTITY);
    }

    protected void assertEquals(Tensor a, Tensor b, Transformation addon) {
        if (a.getIndices().getFree().size(Matrix1) != 0 && b.getIndices().getFree().size(Matrix1) == 0)
            assertEquals(b, a);

        if (a.getIndices().getFree().size(Matrix1) == 0 && b.getIndices().getFree().size(Matrix1) != 0)
            a = expandAndEliminate(multiplyAndRenameConflictingDummies(
                    createMetricOrKronecker(b.getIndices().getFree().getOfType(Matrix1)), a));

        a = expandAndEliminate(dOrder.transform(a));
        b = expandAndEliminate(dOrder.transform(b));
        a = traceOfOne.transform(a);
        b = traceOfOne.transform(b);
        a = addon.transform(a);
        b = addon.transform(b);
        TAssert.assertEquals(a, b);
    }
}