package cc.redberry.core.performance.kv;

import cc.redberry.core.indices.IndicesBuilder;
import cc.redberry.core.number.*;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.Expand;
import cc.redberry.core.transformations.Transformation;
import java.util.*;
import java.util.List;
import org.apache.commons.math3.util.*;

public class Averaging implements Transformation {

    public static final Averaging INSTANCE = new Averaging();
    private final static SimpleTensor const_n = Tensors.parseSimple("n_\\mu");

    private Averaging() {
    }

    private static Tensor average(final int[] indices) {
        if (indices.length == 2)
            return Tensors.createMetricOrKronecker(indices[0], indices[1]);
        SumBuilder sb = new SumBuilder();
        for (int i = 1; i < indices.length; ++i) {
            int[] suffix = new int[indices.length - 2];
            System.arraycopy(indices, 1, suffix, 0, i - 1);
            System.arraycopy(indices, i + 1, suffix, i - 1, indices.length - i - 1);
            sb.put(Tensors.multiply(Tensors.createMetricOrKronecker(indices[0], indices[i]), average(suffix)));
        }
        return sb.build();
    }

    @Override
    public Tensor transform(Tensor tensor) {
        if (tensor instanceof Sum || tensor instanceof Expression) {
            int i;
            Tensor tensorCurrent, tempResult;
            Tensor[] newSumElements = new Tensor[tensor.size()];
            boolean needRebuild = false;
            for (i = tensor.size() - 1; i >= 0; --i) {
                tensorCurrent = tensor.get(i);
                tempResult = transform(tensorCurrent);
                if (tensorCurrent != tempResult)
                    needRebuild = true;
                newSumElements[i] = tempResult;
            }
            if (needRebuild)
                return tensor.getFactory().create(newSumElements);
            else
                return tensor;
        }

        if (tensor instanceof Product) {
            int i;
            int count = 0;
            Tensor current;
            IndicesBuilder ib = new IndicesBuilder();
            List<Tensor> newProductElements = new ArrayList<>();
            for (i = tensor.size() - 1; i >= 0; --i) {
                current = tensor.get(i);
                if (current instanceof SimpleTensor && ((SimpleTensor) current).getName() == const_n.getName()) {
                    ib.append(current);
                    ++count;
                } else
                    newProductElements.add(current);
            }
            if (count == 0)
                return tensor;
            if (count % 2 != 0)
                return Complex.ZERO;
            count = count / 2;
            Tensor averaged = average(ib.getIndices().getAllIndices().copy());
            long factor = ArithmeticUtils.pow((long) 2, count) * ArithmeticUtils.factorial(count + 1);
            Complex number = new Complex((long) factor).reciprocal();
            averaged = Expand.expand(averaged);
            newProductElements.add(number);
            newProductElements.add(averaged);
            return Tensors.multiply(newProductElements.toArray(new Tensor[newProductElements.size()]));
        }
        return tensor;
    }
}
