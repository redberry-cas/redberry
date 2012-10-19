package cc.redberry.core.transformations;

import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.ScalarFunction;
import cc.redberry.core.tensor.iterator.TensorLastIterator;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import static cc.redberry.core.indices.IndicesUtils.getIndicesNames;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.transformations.ContractIndices.contract;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Differentiate implements Transformation {
    private final SimpleTensor var;

    public Differentiate(SimpleTensor var) {
        this.var = var;
    }

    @Override
    public Tensor transform(Tensor t) {
        return differentiate(t, var);
    }

    public static Tensor differentiate(Tensor tensor, SimpleTensor var, int order) {
        if (var.getIndices().size() != 0 && order > 1)
            throw new IllegalArgumentException();
        for (; order > 0; --order)
            tensor = differentiate(tensor, var);
        return tensor;
    }

    public static Tensor differentiate(Tensor tensor, SimpleTensor... vars) {
        TIntHashSet forbidden = TensorUtils.getAllIndicesNamesT(tensor);
        for (SimpleTensor var : vars)
            forbidden.addAll(getIndicesNames(var.getIndices().getFree()));

        SimpleTensor[] vars1 = vars.clone();
        for (int i = 0; i < vars.length; ++i) {
            if (!forbidden.isEmpty() && vars1[i].getIndices().size() != 0) {
                vars1[i] = (SimpleTensor) ApplyIndexMapping.renameDummy(vars1[i], forbidden.toArray());
                forbidden.addAll(getIndicesNames(vars1[i].getIndices()));
            }
        }
        tensor = ApplyIndexMapping.renameDummy(tensor, TensorUtils.getAllIndicesNamesT(vars1).toArray());
        for (SimpleTensor var : vars1)
            tensor = differentiate1(tensor, var);
        return tensor;
    }

    public static Tensor differentiate(Tensor tensor, SimpleTensor var) {
        if (var.getIndices().size() != 0) {
            if (var.getIndices().size() != var.getIndices().getFree().size())
                var = (SimpleTensor) ApplyIndexMapping.renameDummy(var, TensorUtils.getAllIndicesNamesT(tensor).toArray());
            tensor = ApplyIndexMapping.renameDummy(tensor, TensorUtils.getAllIndicesNamesT(var).toArray());
        }
        return differentiate1(tensor, var);
    }

    private static Tensor differentiate1(Tensor tensor, SimpleTensor var) {
        if (tensor.getClass() == SimpleTensor.class) {
            SimpleTensor st = (SimpleTensor) tensor;
            if (st.getName() == var.getName()) {
                if (var.getIndices().size() == 0)
                    return Complex.ONE;
                int size = var.getIndices().size();
                ProductBuilder builder = new ProductBuilder(0, size);
                for (int i = 0; i < size; ++i) {
                    builder.put(createMetricOrKronecker(IndicesUtils.inverseIndexState(var.getIndices().get(i)), st.getIndices().get(i)));
                }
                return contract(builder.build());
            } else
                return Complex.ZERO;
        }
        if (tensor.getClass() == TensorField.class) {
            TensorLastIterator iterator = new TensorLastIterator(tensor);
            Tensor c;
            while ((c = iterator.next()) != null) {
                if (c.getClass() == SimpleTensor.class) {
                    if (((SimpleTensor) c).getName() == var.getName())
                        throw new UnsupportedOperationException();
                }
            }
            return Complex.ZERO;
        }
        if (tensor instanceof Sum) {
            SumBuilder builder = new SumBuilder();
            for (Tensor t : tensor)
                builder.put(differentiate(t, var));
            return builder.build();
        }
        if (tensor instanceof ScalarFunction) {
            return multiply(((ScalarFunction) tensor).derivative(), differentiate(tensor.get(0), var));
        }
        if (tensor instanceof Power) {
            //e^f*ln(g) -> g^f*(f'*ln(g)+f/g*g') ->f*g^(f-1)*g' + g^f*ln(g)*f'
            return sum(
                    multiply(tensor.get(1),
                            pow(tensor.get(0), sum(tensor.get(1), Complex.MINUSE_ONE)),
                            differentiate(tensor.get(0), var)),
                    multiply(tensor,
                            log(tensor.get(0)),
                            differentiate(tensor.get(1), var)));
        }
        if (tensor instanceof Product) {
            SumBuilder sums = new SumBuilder();
            for (int i = tensor.size() - 1; i >= 0; --i) {
                if (!(tensor.get(i) instanceof Complex))
                    if (var.getIndices().size() == 0)
                        sums.put(tensor.set(i, differentiate(tensor.get(i), var)));
                    else
                        sums.put(contract(tensor.set(i, differentiate(tensor.get(i), var))));
            }
            return sums.build();
        }
        if (tensor instanceof Complex)
            return Complex.ZERO;
        throw new UnsupportedOperationException();
    }
}
