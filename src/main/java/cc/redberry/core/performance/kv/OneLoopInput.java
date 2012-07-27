package cc.redberry.core.performance.kv;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.NameDescriptor;
import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.indices.*;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParseNodeSimpleTensor;
import cc.redberry.core.parser.preprocessor.IndicesInsertion;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.ContractIndices;
import cc.redberry.core.transformations.Expand;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.*;
import java.util.Arrays;

public final class OneLoopInput {

    //[KINV,K,S,W,N,M]
    private final Expression[] inputValues;
    private final int operatorOrder, matrixIndicesCount;
    private final Expression[][] hatQuantities;
    private final Expression[] kn;
    private final Expression L;
    private static final int HAT_QUANTITIES_GENERAL_COUNT = 5;//K,S,W,N,M
    private static final int INPUT_VALUES_GENERAL_COUNT = 6;//K,S,W,N,M
    private final int actualInput, actualHatQuantities;
    private final Transformation[] riemannBackround;
    private final Expression F;
    private final Expression HATF;

    public OneLoopInput(int operatorOrder, Expression KINV, Expression K, Expression S, Expression W, Expression N, Expression M) {
        this(operatorOrder, KINV, K, S, W, N, M, new Transformation[0]);
    }

    public OneLoopInput(int operatorOrder, Expression KINV, Expression K, Expression S, Expression W, Expression N, Expression M, Transformation[] riemannBackround) {
        this.operatorOrder = operatorOrder;
        if (operatorOrder > 4)
            throw new IllegalArgumentException();
        this.riemannBackround = riemannBackround;
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

        Indicator<ParseNodeSimpleTensor> indicator = new Indicator<ParseNodeSimpleTensor>() {

            @Override
            public boolean is(ParseNodeSimpleTensor object) {
                String name = object.name;
                int i;
                for (i = 0; i < INPUT_VALUES_GENERAL_COUNT; ++i)
                    if (name.equals(getStringInputName(i)))
                        return true;

                for (i = 0; i < HAT_QUANTITIES_GENERAL_COUNT; ++i)
                    if (name.equals(getStringHatQuantitieName(i)))
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
        Transformation[] transformations = ArraysUtils.addAll(new Transformation[]{ContractIndices.INSTANCE}, riemannBackround);
        for (i = 0; i < actualHatQuantities; ++i) {
            hatQuantities[i] = new Expression[operatorOrder + 1 - i];
            covariantIndicesString = IndicesUtils.toString(Arrays.copyOfRange(covariantIndices, 0, covariantIndices.length - i), ToStringMode.REDBERRY);
            for (j = 0; j < operatorOrder + 1 - i; ++j) {
                sb = new StringBuilder();
                sb.append(getStringHatQuantitieName(i)).
                        append(IndicesUtils.toString(Arrays.copyOfRange(covariantIndices, j, covariantIndices.length - i), ToStringMode.REDBERRY)).
                        append("=KINV*").
                        append(getStringInputName(1 + i)).
                        append(covariantIndicesString);
                for (k = 0; k < j; ++k)
                    sb.append("*n").append(IndicesUtils.toString(IndicesUtils.inverseIndexState(covariantIndices[k]), ToStringMode.REDBERRY));

                temp = Tensors.parse(sb.toString(), insertion);
                temp = inputValues[0].transform(temp);
                temp = inputValues[i + 1].transform(temp);
                temp = Expand.expand(temp, Indicator.TRUE_INDICATOR, transformations, 1);
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
        covariantIndicesString = IndicesUtils.toString(covariantIndices, ToStringMode.REDBERRY);
        String matricIndices = IndicesUtils.toString(ArraysUtils.addAll(upper, lower), ToStringMode.REDBERRY);
        for (i = 0; i < operatorOrder + 1; ++i) {
            sb = new StringBuilder();
            sb.append("Kn").append(IndicesUtils.toString(Arrays.copyOfRange(covariantIndices, i, covariantIndices.length), ToStringMode.REDBERRY)).
                    append(matricIndices).
                    append("=K").
                    append(covariantIndicesString).
                    append(matricIndices);
            for (k = 0; k < i; ++k)
                sb.append("*n").append(IndicesUtils.toString(IndicesUtils.inverseIndexState(covariantIndices[k]), ToStringMode.REDBERRY));
            temp = Tensors.parse(sb.toString());
            temp = inputValues[0].transform(temp);
            temp = inputValues[1].transform(temp);
            temp = Expand.expand(temp, Indicator.TRUE_INDICATOR, transformations, 1);
            kn[i] = (Expression) temp;
        }

        //FIXME temporary
        F = (Expression) Tensors.parse("F_\\mu\\nu\\alpha\\beta = R_\\mu\\nu\\alpha\\beta");
        HATF = (Expression) inputValues[0].transform(F.transform(Tensors.parse("HATF_\\mu\\nu^\\alpha_\\beta = KINV^\\alpha_\\gamma*R_\\mu\\nu^\\gamma_\\beta")));
        
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
        if (operatorOrder % 2 != 0)
            throw new IllegalArgumentException();
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
        IndicesTypeStructure indicesTypeStructure = indices.getIndicesTypeStructure();
        if (indicesTypeStructure.getTypeData(IndexType.GreekLower.getType()).length != indicesTypeStructure.size())
            throw new IllegalArgumentException("Only Greek lower indices are legal.");

        int matrixIndicesCount = indices.size() - operatorOrder;
        if (matrixIndicesCount % 2 != 0)
            throw new IllegalArgumentException();

        if (inputValues[0].get(0).getIndices().size() != matrixIndicesCount)
            throw new IllegalArgumentException();

        for (i = 1; i < actualInput; ++i) {
            indicesTypeStructure = ((SimpleIndices) inputValues[i].get(0).getIndices()).getIndicesTypeStructure();
            if (indicesTypeStructure.getTypeData(IndexType.GreekLower.getType()).length != indicesTypeStructure.size())
                throw new IllegalArgumentException("Only Greek lower indices are legal.");
            if (indicesTypeStructure.size() + i - 1 != operatorOrder + matrixIndicesCount)
                throw new IllegalArgumentException();
        }
    }

    public Expression getInputParameter(int i) {
        return inputValues[i];
    }

    public Expression[] getHatQuantities(int k) {
        return hatQuantities[k];
    }

    Expression[][] getHatQuantities() {
        return hatQuantities;
    }

    public Expression[] getKnQuantities() {
        return kn.clone();
    }

    public Expression getHatF() {
        return HATF;
    }

    public Expression getF() {
        return F;
//        int[] indices = new int[2 + matrixIndicesCount];
//        for (int i = 0; i < indices.length; ++i)
//            indices[i] = IndicesUtils.createIndex(i, IndexType.GreekLower, false);
//        return (Expression) Tensors.parse("F" + IndicesUtils.toString(indices, ToStringMode.REDBERRY) + "=0");
    }

    public Expression[] getNablaS() {
        if (operatorOrder < 1)
            return new Expression[0];
        Expression[] nablaS = new Expression[getHatQuantities(1).length];
        StringBuilder sb;
        for (int i = 0; i < nablaS.length; ++i) {
            sb = new StringBuilder().append("NABLAS_{\\mu_{9}}").append(getHatQuantities(1)[i].get(0).getIndices().toString(ToStringMode.REDBERRY)).append("=0");
            nablaS[i] = (Expression) Tensors.parse(sb.toString());
        }
        return nablaS;
    }

    public Expression getL() {
        return L;
    }

    public int getMatrixIndicesCount() {
        return matrixIndicesCount;
    }

    public int getOperatorOrder() {
        return operatorOrder;
    }

    public Transformation[] getRiemannBackround() {
        return riemannBackround;
    }
}
