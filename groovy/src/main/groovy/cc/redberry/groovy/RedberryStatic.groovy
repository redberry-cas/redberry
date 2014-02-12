/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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

import cc.redberry.core.context.CC
import cc.redberry.core.context.OutputFormat
import cc.redberry.core.groups.permutations.Permutation
import cc.redberry.core.groups.permutations.PermutationGroup
import cc.redberry.core.indices.SimpleIndices
import cc.redberry.core.indices.StructureOfIndices
import cc.redberry.core.parser.ParseTokenSimpleTensor
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion
import cc.redberry.core.solver.ExternalSolver
import cc.redberry.core.solver.ReduceEngine
import cc.redberry.core.solver.ReducedSystem
import cc.redberry.core.solver.frobenius.FrobeniusSolver
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
import cc.redberry.core.transformations.factor.FactorizationEngine
import cc.redberry.core.transformations.factor.JasFactor
import cc.redberry.core.transformations.fractions.GetDenominatorTransformation
import cc.redberry.core.transformations.fractions.GetNumeratorTransformation
import cc.redberry.core.transformations.fractions.TogetherTransformation
import cc.redberry.core.transformations.powerexpand.PowerExpandTransformation
import cc.redberry.core.transformations.powerexpand.PowerExpandUnwrapTransformation
import cc.redberry.core.transformations.symmetrization.SymmetrizeTransformation
import cc.redberry.core.utils.BitArray
import cc.redberry.core.utils.TensorUtils

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
     * @see EliminateDueSymmetriesTransformation
     */
    public static final Transformation EliminateDueSymmetries =
            EliminateDueSymmetriesTransformation.ELIMINATE_DUE_SYMMETRIES;

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
    public static final FactorWrapper Factor = FactorWrapper.INSTANCE;

    public static final class FactorWrapper implements Transformation {
        public static final FactorWrapper INSTANCE = new FactorWrapper()

        private FactorWrapper() {
        }

        @Override
        public Tensor transform(Tensor t) {
            return FactorTransformation.factor(t)
        }

        private static final def defaultOptions = [FactorScalars: true, FactorizationEngine: JasFactor.ENGINE]

        public Transformation getAt(boolean factorScalars, FactorizationEngine factorizationEngine = JasFactor.ENGINE) {
            return new FactorTransformation(factorScalars, factorizationEngine)
        }

        public Transformation getAt(Map map = [FactorScalars: true, FactorizationEngine: JasFactor.ENGINE]) {
            def allOptions = new HashMap(defaultOptions)
            allOptions.putAll(map)
            return new FactorTransformation(allOptions['FactorScalars'], allOptions['FactorizationEngine'])
        }
    }

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

    /**
     * Gives a symmetrization of tensor with respect to specified indices under the specified symmetries.
     */
    public static final SymmetrizeWrapper Symmetrize = SymmetrizeWrapper.INSTANCE;

    public static final class SymmetrizeWrapper {
        public static final SymmetrizeWrapper INSTANCE = new SymmetrizeWrapper()

        SymmetrizeWrapper() {
        }

        public Transformation getAt(SimpleIndices indices) {
            return new SymmetrizeTransformation(indices, true)
        }
    }

    /***********************************************************************
     ********************* Matrices definition *****************************
     **********************************************************************/


    private static final GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();

    private static void ensureIndicesInsertionAddedToParser() {
        if (!CC.current().getParseManager().defaultParserPreprocessors.contains(indicesInsertion))
            CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
    }

    /**
     * Tells Redberry to consider specified tensors as matrices and use matrix multiplication rules
     * @param objs input
     * @see GeneralIndicesInsertion
     */
    public static void defineMatrices(Object... objs) {
        ensureIndicesInsertionAddedToParser()
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
    }

    /**
     * Tells Redberry to consider specified tensor as matrix and use matrix multiplication rules
     * @param tensor string representation of tensor (without matrix indices)
     * @deprecated matrix descriptors
     */
    public static void defineMatrix(String tensor, MatrixDescriptor... descriptors) {
        ensureIndicesInsertionAddedToParser()
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

/************************************************************************************
 ******************************** Permutation group *********************************
 ************************************************************************************/

    public static PermutationGroup Group(Object... permutations) {
        use(Redberry) {
            return PermutationGroup.createPermutationGroup(permutations.collect({ it.p }))
        }
    }

/************************************************************************************
 **************************************** Solver ************************************
 ************************************************************************************/

    private static final Map GenerateTensorDefaultOptions =
            [Symmetries: null, GeneratedParameters: { i -> "C[$i]" }, GenerateParameters: 'true', SymmetricForm: 'false', RaiseLower: 'true'];

    /**
     * Generates tensor of the most general form with specified free indices and from specified samples.
     *
     * @param indices free indices of the resulting tensor
     * @param samples samples
     * @param options options
     * @return tensor of the most general form
     */
    public static Tensor GenerateTensor(SimpleIndices indices,
                                        Collection samples,
                                        Map options = [GeneratedParameters: { i -> "C[$i]" }, GenerateParameters: 'true', SymmetricForm: 'false', RaiseLower: 'true']) {
        use(Redberry) {
            def allOptions = new HashMap(GenerateTensorDefaultOptions)
            allOptions.putAll(options)
            def symmetricForm = Boolean.valueOf(allOptions.get('SymmetricForm'))
            def generateCoefficients = Boolean.valueOf(allOptions.get('GenerateParameters'))
            def raiseLower = Boolean.valueOf(allOptions.get('RaiseLower'))
            def struct = TensorGenerator.generateStructure(indices, samples.t as Tensor[], symmetricForm, generateCoefficients, raiseLower)
            def result = struct.generatedTensor
            def generatedParameters = allOptions.get('GeneratedParameters')
            int i = 0
            for (def coef in struct.coefficients)
                result = coef.eq(generatedParameters(i++).t) >> result
            return result
        }
    }

    /**
     * Generates tensor of the most general form with specified free indices and from specified samples and returns
     * tensor and its coefficients in form {@code [tensor , [coefficients]]}.
     *
     * @param indices free indices of the resulting tensor
     * @param samples samples
     * @param options options
     * @return tensor of the most general form
     */
    public static Collection GenerateTensorWithCoefficients(SimpleIndices indices,
                                                            Collection samples,
                                                            Map options = [SymmetricForm: 'false', RaiseLower: 'true']) {
        use(Redberry) {
            def allOptions = new HashMap(GenerateTensorDefaultOptions)
            allOptions.putAll(options)
            def symmetricForm = Boolean.valueOf(allOptions.get('SymmetricForm'))
            def raiseLower = Boolean.valueOf(allOptions.get('RaiseLower'))
            def struct = TensorGenerator.generateStructure(indices, samples.t as Tensor[], symmetricForm, true, raiseLower)
            return [struct.generatedTensor, struct.coefficients as Collection]
        }
    }

    private static final Map ReduceDefaultOptions = Collections.unmodifiableMap(
            [Transformations: [], SymmetricForm: [], GeneratedParameters: { i -> "C[$i]" },
                    ExternalSolver: [Solver: '', Path: '', KeepFreeParams: 'false', TmpDir: System.getProperty("java.io.tmpdir")]])

    private static final Map ReduceDefaultExternalSolverOptions = Collections.unmodifiableMap(
            [Solver: '', Path: '', KeepFreeParams: 'true', TmpDir: System.getProperty("java.io.tmpdir")])

    /**
     * Reduces a system of tensorial equations.
     * @param equations equations
     * @param vars unknown variables
     * @param options options
     * @return solution or reduced system
     */
    public static Collection Reduce(Collection equations,
                                    Collection vars,
                                    Map options = [Transformations: [], SymmetricForm: [], GeneratedParameters: { i -> "C[$i]" }, ExternalSolver: [Solver: '', Path: '', KeepFreeParams: 'true', TmpDir: System.getProperty("java.io.tmpdir")]]) {
        use(Redberry) {
            Map allOptions = new HashMap(ReduceDefaultOptions)
            allOptions.putAll(options)

            def transformations = allOptions.get('Transformations') as Transformation[]
            def symmetricForm = allOptions.get('SymmetricForm') as boolean[]
            if (symmetricForm.length == 0) symmetricForm = new boolean[vars.size()]

            ReducedSystem reducedSystem = ReduceEngine.reduceToSymbolicSystem(equations.t as Expression[],
                    vars.t as SimpleTensor[], transformations, symmetricForm)
            if (reducedSystem == null)
                return equations

            def externalSolverOptions = new HashMap(ReduceDefaultExternalSolverOptions)
            externalSolverOptions.putAll((Map) allOptions.get('ExternalSolver'))

            def keepFreeParams = Boolean.valueOf(externalSolverOptions.get('KeepFreeParams'))
            def externalSolverPath = externalSolverOptions.get('Path')
            def tmpDir = externalSolverOptions.get('TmpDir')
            def externalProgram = (String) externalSolverOptions.get('Solver')

            def solution
            if (!externalProgram.isEmpty()) {
                switch (externalProgram) {
                    case 'Mathematica':
                        solution = ExternalSolver.solveSystemWithMathematica(reducedSystem, keepFreeParams, externalSolverPath, tmpDir);
                        solution = solution.collect { it as List }
                        break;
                    case 'Maple':
                        solution = ExternalSolver.solveSystemWithMaple(reducedSystem, keepFreeParams, externalSolverPath, tmpDir);
                        solution = solution.collect { it as List }
                        break;
                    default:
                        throw new IllegalArgumentException('Uncknown solver:' + externalProgram)
                }
            } else {
                solution = []
                solution.addAll(Arrays.asList(reducedSystem.generalSolutions))
                solution.addAll(Arrays.asList(reducedSystem.equations))
            }

            def _generatedParameters = (Closure) allOptions.get('GeneratedParameters')

            int i = 0
            def replacements = [:]

            def tensorEach = { t ->
                t.parentAfterChild { ten ->
                    if (ten.class == SimpleTensor &&
                            ten.toString(OutputFormat.Redberry) ==~
                            /${CC.getNameManager().DEFAULT_VAR_SYMBOL_PREFIX}\d+/) {
                        if (!replacements.containsKey(ten.toString(OutputFormat.Redberry)))
                            replacements.put(ten.toString(), _generatedParameters(i++))
                    }
                }
            }


            def through
            through = { collection, closure ->
                if (collection instanceof List)
                    collection.each { cc -> through(cc, closure) }
                else {
                    assert collection instanceof Tensor
                    closure(collection)
                }
            }

            through(solution, tensorEach)

            replacements.each { from, to -> solution = "$from = $to".t >> solution }
            return solution
        }
    }

    /**
     * Gives a list of solutions of the Frobenius equation.
     *
     * @param equations equations
     * @param maxSolutions if specified, then gives at most {@code maxSolutions}
     * @return a list of solutions
     */
    public static Collection FrobeniusSolve(Collection equations, int maxSolutions = -1) {
        def solver = new FrobeniusSolver(equations as int[][])
        def solutions = []
        def solution
        while (maxSolutions-- != 0 && (solution = solver.take()) != null)
            solutions << solution
        return solutions
    }

    /***********************************
     ************* Utilities ***********
     ***********************************/

//    /**
//     * Creates an instance of {@link Symmetries} from a set of symmetries.
//     * @param collection collection of symmetries
//     * @return instance of {@link Symmetries}
//     */
//    public static PermutationGroup CreateSymmetries(Object... collection) {
//        def s = CreateSymmetry(collection[0])
//        Symmetries symmetries = SymmetriesFactory.createSymmetries(s.dimension())
//        collection.each { symmetries.add(CreateSymmetry(it)) }
//        return symmetries
//    }

    public static List<Permutation> FindIndicesSymmetries(SimpleIndices indices, tensor) {
        use(Redberry) {
            return TensorUtils.findIndicesSymmetries(indices, tensor.t)
        }
    }
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
