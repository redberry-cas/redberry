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

package cc.redberry.groovy

import cc.redberry.core.combinatorics.symmetries.Symmetries
import cc.redberry.core.context.CC
import cc.redberry.core.indices.SimpleIndices
import cc.redberry.core.indices.StructureOfIndices
import cc.redberry.core.parser.ParseTokenSimpleTensor
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion
import cc.redberry.core.solver.ExternalSolver
import cc.redberry.core.solver.ReduceEngine
import cc.redberry.core.solver.ReducedSystem
import cc.redberry.core.tensor.Expression
import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.tensorgenerator.TensorGenerator
import cc.redberry.core.transformations.*
import cc.redberry.core.transformations.collect.CollectTransformation
import cc.redberry.core.transformations.expand.ExpandAllTransformation
import cc.redberry.core.transformations.expand.ExpandDenominatorTransformation
import cc.redberry.core.transformations.expand.ExpandNumeratorTransformation
import cc.redberry.core.transformations.expand.ExpandTransformation
import cc.redberry.core.transformations.factor.FactorTransformation
import cc.redberry.core.transformations.fractions.GetDenominatorTransformation
import cc.redberry.core.transformations.fractions.GetNumeratorTransformation
import cc.redberry.core.transformations.fractions.TogetherTransformation
import cc.redberry.core.transformations.powerexpand.PowerExpandTransformation
import cc.redberry.core.transformations.powerexpand.PowerExpandUnwrapTransformation
import cc.redberry.core.utils.BitArray

/**
 * Groovy facade for Redberry transformations and utility methods.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class RedberryStatic {

    private static abstract class TransformationWrapper {
        protected final Class<Transformation> transformationClass;

        TransformationWrapper(Class<Transformation> transformationClass) {
            this.transformationClass = transformationClass
        }

        Transformation getAt(String string) {
            use(Redberry) {
                return transformationClass.newInstance(string.t)
            }
        }

        Transformation getAt(GString string) {
            use(Redberry) {
                return transformationClass.newInstance(string.t)
            }
        }

        Transformation getAt(Object object) {
            use(Redberry) {
                if (object instanceof String || object instanceof GString)
                    object = object.t
                return transformationClass.newInstance(object)
            }
        }

        abstract Transformation getAt(Collection args);
    }

    private static final class TransformationWrapper_SimpleTensors_Or_Transformations extends
            TransformationWrapper implements Transformation {

        final Transformation instance;

        TransformationWrapper_SimpleTensors_Or_Transformations(Class<Transformation> transformationClass, Transformation instance) {
            super(transformationClass)
            this.instance = instance
        }

        @Override
        Tensor transform(Tensor t) {
            return instance.transform(t)
        }

        @Override
        Transformation getAt(Collection args) {
            use(Redberry) {
                return transformationClass.newInstance(* args.collect { it instanceof String ? it.t : it })
            }
        }
    }

    private static final class TransformationWrapper_SimpleTensors_And_Transformations extends
            TransformationWrapper {

        TransformationWrapper_SimpleTensors_And_Transformations(Class<Transformation> transformationClass) {
            super(transformationClass)
        }

        @Override
        Transformation getAt(Collection args) {
            use(Redberry) {
                def _args = []
                def _tr = []
                args.each { t ->
                    if (t instanceof String || t instanceof GString)
                        t = t.t

                    if (t instanceof SimpleTensor)
                        _args << t
                    if (t instanceof Transformation)
                        _tr << t
                }
                return transformationClass.newInstance(_args as SimpleTensor[], _tr as Transformation[]);
            }
        }
    }

    /**
     * Expands out products and positive integer powers.
     * @see ExpandTransformation
     */
    public static final TransformationWrapper_SimpleTensors_Or_Transformations Expand =
        new TransformationWrapper_SimpleTensors_Or_Transformations(ExpandTransformation, ExpandTransformation.EXPAND)

    /**
     * Expands out all products and integer powers in any part of expression.
     * @see ExpandAllTransformation
     */
    public static final TransformationWrapper_SimpleTensors_Or_Transformations ExpandAll =
        new TransformationWrapper_SimpleTensors_Or_Transformations(ExpandAllTransformation, ExpandAllTransformation.EXPAND_ALL)

    /**
     * Expands out products and powers that appear as denominators.
     * @see ExpandNumeratorTransformation
     */
    public static final TransformationWrapper_SimpleTensors_Or_Transformations ExpandNumerator =
        new TransformationWrapper_SimpleTensors_Or_Transformations(ExpandNumeratorTransformation,
                ExpandNumeratorTransformation.EXPAND_NUMERATOR)

    /**
     * Expands out products and powers that appear in the numerator.
     * @see ExpandDenominatorTransformation
     */
    public static final TransformationWrapper_SimpleTensors_Or_Transformations ExpandDenominator =
        new TransformationWrapper_SimpleTensors_Or_Transformations(ExpandDenominatorTransformation,
                ExpandDenominatorTransformation.EXPAND_DENOMINATOR)

    /**
     * Collects terms by pattern
     */
    public static final TransformationWrapper Collect =
        new TransformationWrapper_SimpleTensors_And_Transformations(CollectTransformation)

    /**
     * Gives a partial derivative.
     * @see DifferentiateTransformation
     */
    public static final TransformationWrapper Differentiate =
        new TransformationWrapper_SimpleTensors_And_Transformations(DifferentiateTransformation)

    /**
     * Eliminates metrics and Kronecker deltas
     * @see EliminateMetricsTransformation
     */
    public static final Transformation EliminateMetrics = EliminateMetricsTransformation.ELIMINATE_METRICS

    /**
     * Expands out product of sums and positive integer powers and
     * permanently eliminates metric and Kronecker deltas
     */
    public static final Transformation ExpandAndEliminate = new TransformationCollection(
            new ExpandTransformation(EliminateMetricsTransformation.ELIMINATE_METRICS),
            EliminateMetricsTransformation.ELIMINATE_METRICS)

    /**
     * Gives the numerator of expression.
     * @see GetNumeratorTransformation
     */
    public static final Transformation Numerator = GetNumeratorTransformation.GET_NUMERATOR

    /**
     * Gives the denominator of expression.
     * @see GetDenominatorTransformation
     */
    public static final Transformation Denominator = GetDenominatorTransformation.GET_DENOMINATOR

    /**
     * Removes parts of expressions, which are zero because of the symmetries (symmetric and antisymmetric
     * at the same time).
     *
     * @see EliminateFromSymmetriesTransformation
     */
    public static final Transformation EliminateFromSymmetries =
        EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES;

    /**
     * Puts terms in a sum over a common denominator, and cancels factors in the result.
     * @see TogetherTransformation
     */
    public static final Transformation Together = TogetherTransformation.TOGETHER;

    /**
     * Puts terms in a sum over a common denominator, and cancels all symbolic factors in the result.
     * @see TogetherTransformation
     */
    public static final Transformation TogetherFactor = TogetherTransformation.TOGETHER_FACTOR;

    /**
     * Replaces complex numbers in the expression to their complex conjugation.
     * @see ComplexConjugateTransformation
     */
    public static final Transformation Conjugate = ComplexConjugateTransformation.COMPLEX_CONJUGATE;

    /**
     * Gives the numerical value of expression.
     * @see ToNumericTransformation
     */
    public static final Transformation Numeric = ToNumericTransformation.TO_NUMERIC;

    /**
     * Collects similar scalar factors in products.
     * @see CollectNonScalarsTransformation
     */
    public static final Transformation CollectScalars = CollectScalarFactorsTransformation.COLLECT_SCALAR_FACTORS

    /**
     * Puts terms in a sum together factoring out all scalars in each term.
     * @see CollectNonScalarsTransformation
     */
    public static final Transformation CollectNonScalars = CollectNonScalarsTransformation.COLLECT_NON_SCALARS;

    /**
     * Factors a polynomial over the integers.
     * @see FactorTransformation
     */
    public static final Transformation Factor = FactorTransformation.FACTOR;

    /**
     *  Expands all powers of products and powers with respect to specified variables.
     * @see PowerExpandTransformation
     */
    public static final TransformationWrapper_SimpleTensors_Or_Transformations PowerExpand =
        new TransformationWrapper_SimpleTensors_Or_Transformations(PowerExpandTransformation,
                PowerExpandTransformation.POWER_EXPAND_TRANSFORMATION)

    /**
     * Expands all powers of products and powers with respect to specified variables and unwraps powers of
     * indexed arguments into products (e.g. (A_m*A^m)**2 -> A_m*A^m*A_a*A^a).
     *
     * @see PowerExpandUnwrapTransformation
     */
    public static final TransformationWrapper_SimpleTensors_Or_Transformations PowerExpandUnwrap =
        new TransformationWrapper_SimpleTensors_Or_Transformations(PowerExpandUnwrapTransformation,
                PowerExpandUnwrapTransformation.POWER_EXPAND_UNWRAP_TRANSFORMATION)

    /***********************************************************************
     ********************* Matrices definition *****************************
     **********************************************************************/


    static GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();

    static {
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
    }

    /**
     * Tells Redberry to consider specified tensors as matrices and use matrix multiplication rules
     * @param objs input
     * @see GeneralIndicesInsertion
     */
    public static void defineMatrices(Object... objs) {
        def bufferOfTensors = [], bufferOfDescriptors = [];
        objs.each { obj ->
            if (obj instanceof MatrixDescriptor)
                bufferOfDescriptors << obj
            else {
                if (bufferOfDescriptors) {
                    bufferOfTensors.each { it -> defineMatrices(it, * bufferOfDescriptors) }
                    bufferOfTensors = []
                    bufferOfDescriptors = []
                }
                bufferOfTensors << obj
            }
        }
        bufferOfTensors.each { it -> defineMatrix(it, * bufferOfDescriptors) }
        //int index = objs.findIndexOf { it instanceof MatrixDescriptor }
        //objs[0..<index].each { defineMatrices(it, * (objs[index..-1])) }
    }

    /**
     * Tells Redberry to consider specified tensor as matrix and use matrix multiplication rules
     * @param tensor string representation of tensor (without matrix indices)
     * @deprecated matrix descriptors
     */
    public static void defineMatrix(String tensor, MatrixDescriptor... descriptors) {
        ParseTokenSimpleTensor token = CC.current().parseManager.parser.parse(tensor);

        use(Redberry) {
            StructureOfIndices[] st = token.indicesTypeStructureAndName.structure;

            int[] allTypesCounts = st[0].typesCounts;
            def BitArray[] allStates = st[0].states;

            descriptors.each { descriptor ->
                def type = descriptor.type.type
                if (allTypesCounts[type] != 0)
                    throw new IllegalArgumentException()
                allTypesCounts[type] = descriptor.lower + descriptor.upper
                allStates[type] = new BitArray(allTypesCounts[type])
                for (int i = 0; i < descriptor.upper; ++i)
                    allStates[type].set(i)
            }
            st[0] = new StructureOfIndices(allTypesCounts, allStates);
            descriptors.each {
                indicesInsertion.addInsertionRule(CC.getNameManager().mapNameDescriptor(token.name, st),
                        it.type)
            }
        }
    }

    /***********************************
     ************* Solver ** ***********
     **********************************/


    public static Tensor GenerateTensor(SimpleIndices indices,
                                        Collection samples,
                                        Map options = [Symmetries: null, GenerateCoefficients: 'true', SymmetricForm: 'false', RaiseLower: 'true']) {
        use(Redberry) {
            Symmetries symmetries = options.get('Symmetries')
            def symmetricForm = Boolean.valueOf(options.get('SymmetricForm'))
            def generateCoefficients = Boolean.valueOf(options.get('GenerateCoefficients'))
            def raiseLower = Boolean.valueOf(options.get('RaiseLower'))
            return TensorGenerator.generate(indices, samples.t as Tensor[], symmetries, symmetricForm, generateCoefficients, raiseLower)
        }
    }

    private static final Map ReduceDefaultOptions = Collections.unmodifiableMap(
            [Transformations: [], SymmetricForm: [],
                    ExternalSolver: [Solver: '', Path: '', KeepFreeParams: 'false', TmpDir: System.getProperty("java.io.tmpdir")]])

    private static final Map ReduceDefaultExternalSolverOptions = Collections.unmodifiableMap(
            [Solver: '', Path: '', KeepFreeParams: 'false', TmpDir: System.getProperty("java.io.tmpdir")])

    public static Collection Reduce(Collection equations,
                                    Collection vars,
                                    Map options = [Transformations: [], SymmetricForm: [], ExternalSolver: [Solver: '', Path: '', KeepFreeParams: 'false', TmpDir: System.getProperty("java.io.tmpdir")]]) {
        use(Redberry) {
            Map allOptions = new HashMap(ReduceDefaultOptions)
            allOptions.putAll(options)

            def transformations = allOptions.get('Transformations') as Transformation[]
            def symmetricForm = allOptions.get('SymmetricForm') as boolean[]
            if (symmetricForm.length == 0) symmetricForm = new boolean[vars.size()]

            ReducedSystem reducedSystem = ReduceEngine.reduceToSymbolicSystem(equations.t as Expression[],
                    vars.t as SimpleTensor[], transformations, symmetricForm)

            def externalSolverOptions = new HashMap(ReduceDefaultExternalSolverOptions)
            externalSolverOptions.putAll((Map) allOptions.get('ExternalSolver'))

            def keepFreeParams = Boolean.valueOf(externalSolverOptions.get('KeepFreeParams'))
            def externalSolverPath = externalSolverOptions.get('Path')
            def tmpDir = externalSolverOptions.get('TmpDir')
            def externalProgram = (String) externalSolverOptions.get('Solver')
            if (!externalProgram.isEmpty())
                switch (externalProgram) {
                    case 'Mathematica':
                        return ExternalSolver.solveSystemWithMathematica(reducedSystem, keepFreeParams, externalSolverPath, tmpDir);
                    case 'Maple':
                        return ExternalSolver.solveSystemWithMaple(reducedSystem, keepFreeParams, externalSolverPath, tmpDir);
                    default:
                        throw new IllegalArgumentException('Uncknown solver:' + externalProgram)
                }
            def system = []
            system.addAll(Arrays.asList(reducedSystem.generalSolutions))
            system.addAll(Arrays.asList(reducedSystem.equations))
            return system
        }
    }

    /***********************************
     ************* Utilities ***********
     ***********************************/

    /**
     * Evaluates closure, and returns a time in milliseconds used
     * @param closure do stuff
     * @return time spent in calculation
     */
    public static long timing(Closure closure, stdout = true) {
        long start = System.currentTimeMillis();
        closure.call();
        long stop = System.currentTimeMillis();
        if (stdout) println('Time: ' + (stop - start) + ' ms.')
        return (stop - start)
    }
}
