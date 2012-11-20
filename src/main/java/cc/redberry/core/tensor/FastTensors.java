package cc.redberry.core.tensor;

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.number.Complex;
import cc.redberry.core.transformations.expand.ExpandUtils;
import cc.redberry.core.utils.TensorUtils;

import static cc.redberry.core.tensor.Tensors.multiply;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class FastTensors {

    public static Tensor multiplySumElementsOnFactor(Sum sum, Tensor factor) {
        if (TensorUtils.isZero(factor))
            return Complex.ZERO;
        if (TensorUtils.isOne(factor))
            return sum;
        if (TensorUtils.haveIndicesIntersections(sum, factor)) {
            SumBuilder sb = new SumBuilder(sum.size());
            for (Tensor t : sum)
                sb.put(multiply(t, factor));
            return sb.build();
        }

        final Tensor[] newSumData = new Tensor[sum.size()];
        for (int i = newSumData.length - 1; i >= 0; --i)
            newSumData[i] = multiply(factor, sum.get(i));
        return new Sum(newSumData, IndicesFactory.createSorted(newSumData[0].getIndices().getFree()));
    }

    public static Tensor multiplySumElementsOnFactors(Sum sum, OutputPortUnsafe<Tensor> factorsProvider) {
        final Tensor[] newSumData = new Tensor[sum.size()];
        for (int i = newSumData.length - 1; i >= 0; --i)
            newSumData[i] = multiply(factorsProvider.take(), sum.get(i));
        return new Sum(newSumData, IndicesFactory.createSorted(newSumData[0].getIndices().getFree()));
    }

    public static Tensor multiplySumElementsOnScalarFactorAndExpandScalars(Sum sum, Tensor factor) {
        if (TensorUtils.isZero(factor))
            return Complex.ZERO;
        if (TensorUtils.isOne(factor))
            return sum;
        if (factor.getIndices().size() != 0)
            throw new IllegalArgumentException();
        final Tensor[] newSumData = new Tensor[sum.size()];
        for (int i = newSumData.length - 1; i >= 0; --i)
            newSumData[i] = ExpandUtils.expandIndexlessSubproduct.transform(multiply(factor, sum.get(i)));
        return new Sum(newSumData, IndicesFactory.createSorted(newSumData[0].getIndices().getFree()));
    }
}
