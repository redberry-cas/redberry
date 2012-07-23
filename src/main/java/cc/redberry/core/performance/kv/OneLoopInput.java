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
import cc.redberry.core.utils.Indicator;
import java.util.Arrays;

public final class OneLoopInput {

    //[KINV,K,S,W,N,M]
    private final Expression[] inputValues;
    private final int operatorOrder, matrixIndicesCount;
    private final Expression[][] hatQuantities;
    private final Expression L;

    public OneLoopInput(int operatorOrder, Expression KINV, Expression K, Expression S, Expression W, Expression N, Expression M) {
        this.operatorOrder = operatorOrder;
        if (operatorOrder > 4)
            throw new IllegalArgumentException();
        inputValues = new Expression[operatorOrder + 2];
        int i;
        for (i = 0; i < inputValues.length; ++i)
            switch (i) {
                case 0:
                    inputValues[0] = KINV;
                    break;
                case 1:
                    inputValues[1] = K;
                    break;
                case 2:
                    inputValues[2] = S;
                    break;
                case 3:
                    inputValues[3] = W;
                    break;
                case 4:
                    inputValues[4] = N;
                    break;
                case 5:
                    inputValues[5] = M;
                    break;
            }

        checkConsistency();
        this.L = Tensors.expression(Tensors.parse("L"), new Complex(operatorOrder));
        this.hatQuantities = new Expression[operatorOrder + 1][];
        this.matrixIndicesCount = inputValues[1].get(0).getIndices().size() - operatorOrder;

        //all are upper
        int[] covariantIndices = new int[operatorOrder];
        int j, k;
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
                for (i = 0; i < inputValues.length; ++i)
                    if (name.equals(getStringInputName(i)))
                        return true;

                for (i = 0; i < hatQuantities.length; ++i)
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
        for (i = 0; i < hatQuantities.length; ++i) {
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
                temp = Expand.expand(temp, Indicator.TRUE_INDICATOR, new Transformation[]{ContractIndices.CONTRACT_INDICES}, 1);
                hatQuantities[i][j] = (Expression) temp;
            }
        }
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

        for (int i = 0; i < inputValues.length; ++i) {
            if (!(inputValues[i].get(0) instanceof SimpleTensor))
                throw new IllegalArgumentException();
            SimpleTensor st = (SimpleTensor) inputValues[i].get(0);
            NameDescriptor nd = CC.getNameDescriptor(st.getName());
            if (!nd.getName(null).equals(getStringInputName(i)))
                throw new IllegalArgumentException();
        }


        SimpleIndices indices = (SimpleIndices) inputValues[1].get(0).getIndices();
        IndicesTypeStructure indicesTypeStructure = indices.getIndicesTypeStructure();
        if (indicesTypeStructure.getTypeData(IndexType.GreekLower.getType()).length != indicesTypeStructure.size())
            throw new IllegalArgumentException("Only Greek lower indices are legal.");

        int matrixIndicesCount = indices.size() - operatorOrder;
        if (matrixIndicesCount % 2 != 0)
            throw new IllegalArgumentException();

        if (inputValues[0].get(0).getIndices().size() != matrixIndicesCount)
            throw new IllegalArgumentException();

        for (int i = 1; i < inputValues.length; ++i) {
            indicesTypeStructure = ((SimpleIndices) inputValues[i].get(0).getIndices()).getIndicesTypeStructure();
            if (indicesTypeStructure.getTypeData(IndexType.GreekLower.getType()).length != indicesTypeStructure.size())
                throw new IllegalArgumentException("Only Greek lower indices are legal.");
            if (indicesTypeStructure.size() + i - 1 != operatorOrder + matrixIndicesCount)
                throw new IllegalArgumentException();
        }
    }

    public Expression getInputParameter(int i) {
        if (i >= inputValues.length)
            throw new IllegalArgumentException();
        return inputValues[i];
    }

    public Expression[] getHatQuantities(int k) {
        if (k >= hatQuantities.length)
            throw new IllegalArgumentException();
        return hatQuantities[k];
    }
    private final Expression F = (Expression) Tensors.parse("F_\\mu\\nu = 0");
    private final Expression HATF = (Expression) Tensors.parse("HATF_\\mu\\nu = 0");

    public Expression getHatF() {
        return HATF;
    }

    public Expression getF() {
        int[] indices = new int[2 + matrixIndicesCount];
        for (int i = 0; i < indices.length; ++i)
            indices[i] = IndicesUtils.createIndex(i, IndexType.GreekLower, false);
        return (Expression) Tensors.parse("F" + IndicesUtils.toString(indices, ToStringMode.REDBERRY) + "=0");
    }

    public Expression[] getNablaS() {
        if (operatorOrder < 1)
            return new Expression[0];
        Expression[] nablaS = new Expression[getHatQuantities(1).length];
        StringBuilder sb;
        for (int i = 0; i < nablaS.length; ++i) {
            sb = new StringBuilder().append("HATS").append(getHatQuantities(1)[i].get(0).getIndices().toString(ToStringMode.REDBERRY)).append("=0");
            nablaS[i] = (Expression) Tensors.parse(sb.toString());
        }
        return nablaS;
    }

    public Expression getL() {
        return L;
    }
}
