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

package cc.redberry.physics.oneloopdiv;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.NameDescriptor;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.*;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParseTokenSimpleTensor;
import cc.redberry.core.parser.preprocessor.IndicesInsertion;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.iterator.TraverseState;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.Transformer;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.Indicator;

import java.util.Arrays;

/**
 * This class is a container of input parameters for one-loop counterterms
 * calculation. It is always used in conjunction with {@link OneLoopCounterterms},
 * which performs the main calculation. The notation used for input matrices is
 * same as used in original works (see references in <a href =
 * "package-summary.html">package info</a>).
 * summary.
 * <p/>
 * The main goal of this class is to prepare the hat-quantities
 * (\hat K, \hat W etc.) for further processing in {@link OneLoopCounterterms}.
 * All input expressions must be in the same notation as in the original works
 * (see references in <a href ="package-summary.html">package info</a>) and
 * satisfy the following conditions:
 * <ul>
 * <li>L.h.s of input expressions should have only Greek lowercase indices</li>
 * <li>Each l.h.s tensor should have string name defind by the following rules:
 * tensor KINV - "KINV", tensor K - "K", tensor S - "S", tensor W - "W",
 * tensor N - "N", tensor F - "F". </li>
 * <li>The first {@code (L - k)} indices of each l.h.s. of expression are specified
 * to be 'covariant' indices, i.e. indices which are contracted with derivatives in
 * operator expansion. The rest {@code 2n} indices are the 'matrix' indices, i.e.
 * indices which are contracted with fields in the Lagrangian.</li>
 * <li>Each of the input tensors, except {@code F} and {@code KINV} must be
 * symmetric on their 'covariant' indices.</li>
 * <li>The Riemann and Ricci tensors identified as {@code R_{\mu\nu\alpha\beta}} and
 * {@code R_{\mu\nu}} respectively.</li>
 * </ul>
 * If the symmetries of the Riemann or Ricci tensors are not set up, it will be done
 * automatically.
 * <p>Look the {@link OneLoopCounterterms} description for the example of usage. </p>
 * <p><b>Note</b>: Currently supported are not all arbitrary Lagrangians. There
 * is a full support of {@code L = 2} and {@code L = 4} theories with no odd on
 * the number of derivatives terms in the operator, so input tensors {@code S^{...}_{...}}
 * and {@code N^{...}_{...}} should be always zero. Also, input tensors should
 * have only Greek lowercase indices.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see OneLoopCounterterms
 * @since 1.0
 */
public final class OneLoopInput {

    //[KINV,K,S,W,N,M]
    private final Expression[] inputValues;
    private final int operatorOrder, matrixIndicesCount;
    private final Expression[][] hatQuantities;
    private final Expression[] kn;
    private final Expression L;
    private static final int HAT_QUANTITIES_GENERAL_COUNT = 5;//K,S,W,N,M
    private static final int INPUT_VALUES_GENERAL_COUNT = 6;//KINV,K,S,W,N,M
    private final int actualInput, actualHatQuantities;
    private final Transformation[] riemannBackground;
    private final Expression F;
    private final Expression HATF;

    /**
     * Constructs the {@code OneLoopInput} instance with specified {@code operatorOrder}
     * (i.e. {@code L} value) and input expressions. Input expressions must be in the
     * notation, discussed in the class summary.
     *
     * @param operatorOrder the order of the differential operator in the
     *                      Lagrangian, i.e. the integer value of {@code L}.
     *                      Currently supported second and fourth order
     *                      operators.
     * @param KINV          inverse of {@code Kn} tensor. The input
     *                      expression should be in the form
     *                      {@code KINV^{...}_{...} = ...}.
     * @param K             tensor {@code K} in the form {@code K^{...}_{...} = ....}.
     * @param S             tensor {@code S}. Since odd terms in operator expansion
     *                      is not supported yet, this tensor should be zeroed, so
     *                      the r.h.s. of the expression should be always zero:
     *                      {@code S^{...}_{...} = 0}.
     * @param W             tensor {@code W} in the form {@code W^{...}_{...} = ....}.
     * @param N             tensor {@code N}. Since odd terms in operator expansion
     *                      is not supported yet, this tensor should be zeroed, so
     *                      the r.h.s. of the expression should be always zero:
     *                      {@code N^{...}_{...} = 0}. <b>Note:</b> if
     *                      {@code operatorOrder = 2} this param should be {@code null}.
     * @param M             tensor {@code M} in the form {@code M^{...}_{...} = ....}.
     *                      <b>Note:</b> if {@code operatorOrder = 2} this param
     *                      should be {@code null}                                    .
     * @param F             tensor {@code F} in the form {@code F^{...}_{...} = ....}.
     * @throws IllegalArgumentException if {@code operatorOrder} is not eqaul to 2 or 4
     * @throws IllegalArgumentException if {@code S} or {@code N} are not zeroed
     * @throws IllegalArgumentException if some of the input tensors have name different
     *                                  from the specified
     * @throws IllegalArgumentException if indices number of some of the input tensors
     *                                  does not corresponds to the actual {@code operatorOrder}
     * @throws IllegalArgumentException if indices of l.h.s. of input expressions contains non Greek lowercase indices.
     */
    public OneLoopInput(int operatorOrder, Expression KINV, Expression K, Expression S, Expression W, Expression N, Expression M, Expression F) {
        this(operatorOrder, KINV, K, S, W, N, M, F, new Transformation[0]);
    }

    //TODO add references on the paper and Redberry site

    /**
     * Constructs the {@code OneLoopInput} instance with specified {@code operatorOrder}
     * (i.e. {@code L} value), input expressions and riemann background rules. Input
     * expressions must be in the notation, discussed in the class summary. The Riemann
     * background is a number of transformations (usually substitutions) which defines the
     * additional rules for Riemann tensor processing. For example, it can be the anti de
     * Sitter background ({@link OneLoopUtils#antiDeSitterBackground}) or flat background
     * (with R_\alpha\beta\gamma\rho = 0) and so on.
     *
     * @param operatorOrder     the order of the differential operator in the
     *                          Lagrangian, i.e. the integer value of {@code L}.
     *                          Currently supported second and fourth order
     *                          operators.
     * @param KINV              inverse tensors to tensor {@code Kn}. The input
     *                          expression should be in the form
     *                          {@code KINV^{...}_{...} = ...}.
     * @param K                 tensor {@code K} in the form {@code K^{...}_{...} = ....}.
     * @param S                 tensor {@code S}. Since odd terms in operator expansion
     *                          is not supported yet, this tensor should be zeroed, so
     *                          the r.h.s. of the expression should be always zero:
     *                          {@code S^{...}_{...} = 0}.
     * @param W                 tensor {@code W} in the form {@code W^{...}_{...} = ....}.
     * @param N                 tensor {@code N}. Since odd terms in operator expansion
     *                          is not supported yet, this tensor should be zeroed, so
     *                          the r.h.s. of the expression should be always zero:
     *                          {@code N^{...}_{...} = 0}. <b>Note:</b> if
     *                          {@code operatorOrder = 2} this param should be {@code null}.
     * @param M                 tensor {@code M} in the form {@code M^{...}_{...} = ....}.
     *                          <b>Note:</b> if {@code operatorOrder = 2} this param
     *                          should be {@code null}                                    .
     * @param F                 tensor {@code F} in the form {@code F^{...}_{...} = ....}.
     * @param riemannBackground additional background conditions, such as anti de Sitter etc.
     *                          Empty array should be placed if no conditions specified.
     * @throws IllegalArgumentException if {@code operatorOrder} is not eqaul to 2 or 4
     * @throws IllegalArgumentException if {@code S} or {@code N} are not zeroed
     * @throws IllegalArgumentException if some of the input tensors have name different
     *                                  from the specified
     * @throws IllegalArgumentException if indices number of some of the input tensors
     *                                  does not corresponds to the actual {@code operatorOrder}
     * @throws IllegalArgumentException if indices of l.h.s. of input expressions contains non Greek lowercase indices.
     * @see cc.redberry.physics.oneloopdiv.OneLoopUtils#antiDeSitterBackground()
     */
    public OneLoopInput(int operatorOrder,
                        Expression KINV,
                        Expression K, Expression S, Expression W, Expression N, Expression M,
                        Expression F,
                        Transformation[] riemannBackground) {
        this.operatorOrder = operatorOrder;
        if (operatorOrder != 2 && operatorOrder != 4)
            throw new IllegalArgumentException();
        this.riemannBackground = riemannBackground;
        this.actualInput = operatorOrder + 2;
        this.actualHatQuantities = operatorOrder + 1;

        inputValues = new Expression[INPUT_VALUES_GENERAL_COUNT];

        inputValues[0] = KINV;
        inputValues[1] = K;
        inputValues[2] = S;
        inputValues[3] = W;
        inputValues[4] = N;
        inputValues[5] = M;

        checkConsistency();
        Tensors.addSymmetry("R_\\mu\\nu", IndexType.GreekLower, false, new int[]{1, 0});
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, true, new int[]{0, 1, 3, 2});
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, false, new int[]{2, 3, 0, 1});


        this.L = Tensors.expression(Tensors.parse("L"), new Complex(operatorOrder));
        this.hatQuantities = new Expression[HAT_QUANTITIES_GENERAL_COUNT][];
        this.matrixIndicesCount = inputValues[1].get(0).getIndices().size() - operatorOrder;

        //all are upper
        int[] covariantIndices = new int[operatorOrder];
        int i, j, k;
        for (i = 0; i < operatorOrder; ++i)
            covariantIndices[i] = IndicesUtils.createIndex(i, IndexType.GreekLower, true);

        int[] upper = new int[matrixIndicesCount / 2], lower = upper.clone();
        for (; i < operatorOrder + matrixIndicesCount / 2; ++i) {
            upper[i - operatorOrder] = IndicesUtils.createIndex(i, IndexType.GreekLower, true);
            lower[i - operatorOrder] = IndicesUtils.createIndex(i + matrixIndicesCount / 2, IndexType.GreekLower, false);
        }

        Indicator<ParseTokenSimpleTensor> indicator = new Indicator<ParseTokenSimpleTensor>() {

            private final StructureOfIndices F_TYPES = new StructureOfIndices(IndexType.GreekLower, 2);

            @Override
            public boolean is(ParseTokenSimpleTensor object) {
                String name = object.name;
                int i;
                for (i = 0; i < INPUT_VALUES_GENERAL_COUNT; ++i)
                    if (name.equals(getStringInputName(i)))
                        return true;

                for (i = 0; i < HAT_QUANTITIES_GENERAL_COUNT; ++i)
                    if (name.equals(getStringHatQuantitieName(i)))
                        return true;
                if (name.equals("F") && F_TYPES.isStructureOf(object.indices))
                    return true;
                if (name.equals("HATF"))
                    return true;
                return false;
            }
        };

        IndicesInsertion insertion = new IndicesInsertion(IndicesFactory.createSimple(null, upper),
                IndicesFactory.createSimple(null, lower),
                indicator);

        StringBuilder sb;
        Tensor temp;
        String covariantIndicesString;
        Transformation n2 = new SqrSubs(Tensors.parseSimple("n_\\mu")), n2Transformer = new Transformer(TraverseState.Leaving, new Transformation[]{n2});
        Transformation[] transformations = ArraysUtils.addAll(new Transformation[]{EliminateMetricsTransformation.ELIMINATE_METRICS, n2Transformer}, riemannBackground);
        for (i = 0; i < actualHatQuantities; ++i) {
            hatQuantities[i] = new Expression[operatorOrder + 1 - i];
            covariantIndicesString = IndicesUtils.toString(Arrays.copyOfRange(covariantIndices, 0, covariantIndices.length - i), OutputFormat.Redberry);
            for (j = 0; j < operatorOrder + 1 - i; ++j) {
                sb = new StringBuilder();
                sb.append(getStringHatQuantitieName(i)).
                        append(IndicesUtils.toString(Arrays.copyOfRange(covariantIndices, j, covariantIndices.length - i), OutputFormat.Redberry)).
                        append("=KINV*").
                        append(getStringInputName(1 + i)).
                        append(covariantIndicesString);
                for (k = 0; k < j; ++k)
                    sb.append("*n").append(IndicesUtils.toString(IndicesUtils.inverseIndexState(covariantIndices[k]), OutputFormat.Redberry));

                temp = Tensors.parse(sb.toString(), insertion);
                temp = inputValues[0].transform(temp);
                temp = inputValues[i + 1].transform(temp);
                temp = ExpandTransformation.expand(temp, transformations);
                for (Transformation t : transformations)
                    temp = t.transform(temp);
                hatQuantities[i][j] = (Expression) temp;
            }
        }
        for (; i < HAT_QUANTITIES_GENERAL_COUNT; ++i) {
            hatQuantities[i] = new Expression[1];
            sb = new StringBuilder();
            sb.append(getStringHatQuantitieName(i)).append("=0");
            hatQuantities[i][0] = (Expression) Tensors.parse(sb.toString(), insertion);
        }
        kn = new Expression[operatorOrder + 1];
        covariantIndicesString = IndicesUtils.toString(covariantIndices, OutputFormat.Redberry);
        String matricIndices = IndicesUtils.toString(ArraysUtils.addAll(upper, lower), OutputFormat.Redberry);
        for (i = 0; i < operatorOrder + 1; ++i) {
            sb = new StringBuilder();
            sb.append("Kn").append(IndicesUtils.toString(Arrays.copyOfRange(covariantIndices, i, covariantIndices.length), OutputFormat.Redberry)).
                    append(matricIndices).
                    append("=K").
                    append(covariantIndicesString).
                    append(matricIndices);
            for (k = 0; k < i; ++k)
                sb.append("*n").append(IndicesUtils.toString(IndicesUtils.inverseIndexState(covariantIndices[k]), OutputFormat.Redberry));
            temp = Tensors.parse(sb.toString());
            temp = inputValues[0].transform(temp);
            temp = inputValues[1].transform(temp);
            temp = ExpandTransformation.expand(temp, transformations);
            for (Transformation t : transformations)
                temp = t.transform(temp);
            kn[i] = (Expression) temp;
        }

        final int[] symmetry = new int[F.get(0).getIndices().size()];
        symmetry[0] = 1;
        symmetry[1] = 0;
        for (i = 2; i < symmetry.length; ++i)
            symmetry[i] = i;
        Tensors.addSymmetry((SimpleTensor) F.get(0), IndexType.GreekLower, true, symmetry);
        this.F = F;

        covariantIndicesString = IndicesUtils.toString(Arrays.copyOfRange(covariantIndices, 0, 2), OutputFormat.Redberry);
        sb = new StringBuilder();
        sb.append("HATF").
                append(covariantIndicesString).
                append("=KINV*F").
                append(covariantIndicesString);
        Tensor HATF = (Expression) Tensors.parse(sb.toString(), insertion);

        HATF = F.transform(HATF);
        HATF = inputValues[0].transform(HATF);
        this.HATF = (Expression) HATF;
    }

    private String getStringInputName(int i) {
        switch (i) {
            case 0:
                return "KINV";
            case 1:
                return "K";
            case 2:
                return "S";
            case 3:
                return "W";
            case 4:
                return "N";
            case 5:
                return "M";
        }
        throw new IllegalArgumentException();
    }

    private String getStringHatQuantitieName(int i) {
        switch (i) {
            case 0:
                return "HATK";
            case 1:
                return "HATS";
            case 2:
                return "HATW";
            case 3:
                return "HATN";
            case 4:
                return "HATM";
        }
        throw new IllegalArgumentException();
    }

    private void checkConsistency() {
        int i;
        for (i = 0; i < actualInput; ++i) {
            if (!(inputValues[i].get(0) instanceof SimpleTensor))
                throw new IllegalArgumentException();
            SimpleTensor st = (SimpleTensor) inputValues[i].get(0);
            NameDescriptor nd = CC.getNameDescriptor(st.getName());
            if (!nd.getName(null).equals(getStringInputName(i)))
                throw new IllegalArgumentException();
        }
        for (; i < INPUT_VALUES_GENERAL_COUNT; ++i)
            if (inputValues[i] != null)
                throw new IllegalArgumentException();


        SimpleIndices indices = (SimpleIndices) inputValues[1].get(0).getIndices();
        StructureOfIndices structureOfIndices = indices.getStructureOfIndices();
        if (structureOfIndices.getTypeData(IndexType.GreekLower.getType()).length != structureOfIndices.size())
            throw new IllegalArgumentException("Only Greek lower indices are legal.");

        int matrixIndicesCount = indices.size() - operatorOrder;
        if (matrixIndicesCount % 2 != 0)
            throw new IllegalArgumentException();

        if (inputValues[0].get(0).getIndices().size() != matrixIndicesCount)
            throw new IllegalArgumentException();

        for (i = 1; i < actualInput; ++i) {
            structureOfIndices = ((SimpleIndices) inputValues[i].get(0).getIndices()).getStructureOfIndices();
            if (structureOfIndices.getTypeData(IndexType.GreekLower.getType()).length != structureOfIndices.size())
                throw new IllegalArgumentException("Only Greek lower indices are legal.");
            if (structureOfIndices.size() + i - 1 != operatorOrder + matrixIndicesCount)
                throw new IllegalArgumentException();
        }
    }

    /**
     * Return i-th input expression from the [KINV, K, S, W, N, M] array.
     *
     * @param i position of the input expression in the array [KINV, K, S, W, N, M]
     * @return i-th expression from the [KINV, K, S, W, N, M] array
     */
    public Expression getInputParameter(int i) {
        return inputValues[i];
    }

    /**
     * Return the array of hat-quantities according to their
     * position in the [HATK[],HATS[],HATW[], HATN[], HATM[]] array.
     *
     * @param k position of the [HATK[],HATS[],HATW[], HATN[], HATM[]] array
     * @return k-th array from [HATK[],HATS[],HATW[], HATN[], HATM[]]
     */
    public Expression[] getHatQuantities(int k) {
        return hatQuantities[k].clone();
    }

    Expression[][] getHatQuantities() {
        return hatQuantities;
    }

    /**
     * Returns the array of Kn expressions.
     *
     * @return array of Kn expressions
     */
    public Expression[] getKnQuantities() {
        return kn.clone();
    }

    /**
     * Returns \hat F^{...}_{..} expression
     *
     * @return \hat F^{...}_{..} expression
     */
    public Expression getHatF() {
        return HATF;
    }

    public Expression getF() {
        return F;
    }

    public Expression[] getNablaS() {
        if (operatorOrder < 1)
            return new Expression[0];
        Expression[] nablaS = new Expression[getHatQuantities(1).length];
        StringBuilder sb;
        for (int i = 0; i < nablaS.length; ++i) {
            sb = new StringBuilder().append("NABLAS_{\\mu_{9}}").append(getHatQuantities(1)[i].get(0).getIndices().toString(OutputFormat.Redberry)).append("=0");
            nablaS[i] = (Expression) Tensors.parse(sb.toString());
        }
        return nablaS;
    }

    /**
     * Returns an operator order as expression
     *
     * @return operator order as expression
     */
    public Expression getL() {
        return L;
    }

    /**
     * Returns the number of 'matrix' indices in the expressions
     *
     * @return number of 'matrix' indices in the expressions
     */
    public int getMatrixIndicesCount() {
        return matrixIndicesCount;
    }

    /**
     * Returns an operator order
     *
     * @return operator order
     */
    public int getOperatorOrder() {
        return operatorOrder;
    }

    public Transformation[] getRiemannBackground() {
        return riemannBackground;
    }
}
