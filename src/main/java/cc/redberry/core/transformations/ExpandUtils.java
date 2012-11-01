package cc.redberry.core.transformations;

import cc.redberry.concurrent.OutputPort;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ExpandUtils {

    /**
     * This is a safe concurrent port, which expands out product of two sums.
     */
    public static final class ExpandPairPort implements OutputPort<Tensor> {

        private final Tensor sum1, sum2;
        private final AtomicLong atomicLong = new AtomicLong();

        /**
         * Creates port from two sums.
         *
         * @param s1 first sum
         * @param s2 second sum
         */
        public ExpandPairPort(Sum s1, Sum s2) {
            sum1 = s1;
            sum2 = s2;
        }

        /**
         * Consequently returns terms of the resulting expanded expression,
         * calculating the next term on invocation.
         *
         * @return next term in the resulting expanded expression
         */
        @Override
        public Tensor take() {
            long index = atomicLong.getAndIncrement();
            if (index >= sum1.size() * sum2.size())
                return null;
            int i1 = (int) (index / sum2.size());
            int i2 = (int) (index % sum2.size());
            return Tensors.multiply(sum1.get(i1), sum2.get(i2));
        }
    }

    /**
     * Expands out the product of two sums.
     *
     * @param s1              first sum
     * @param s2              second sum
     * @param transformations additional transformations to be
     *                        consequently applied on each term
     *                        in the resulting expression.
     * @return the resulting expanded tensor
     */
    public static Tensor expandPairOfSums(Sum s1, Sum s2, Transformation... transformations) {
        ExpandPairPort epp = new ExpandPairPort(s1, s2);
        TensorBuilder sum = new SumBuilder();
        Tensor t;
        while ((t = epp.take()) != null) {
            for (Transformation transformation : transformations)
                t = transformation.transform(t);
            sum.put(t);
        }
        return sum.build();
    }

    static Tensor expandProductOfSums(Product product, Transformation[] transformations) {
        Tensor indexless = product.getIndexlessSubProduct(),
                data = product.getDataSubProduct();
        boolean expandIndexless = false, expandData = false;
        if (indexless instanceof Sum && ExpandUtils.sumContainsNonIndexless(indexless)) {
            //data is not 1 at this point
            expandIndexless = true;
            expandData = true;
        }
        if (indexless instanceof Product) {
            for (Tensor t : indexless) {
                if (t instanceof Sum) {
                    if (ExpandUtils.sumContainsNonIndexless(t)) {
                        //even if data is 1 it will be recreated
                        expandData = true;
                        expandIndexless = true;
                        break;
                    } else
                        expandIndexless = true;
                }
            }
        }
        if (!expandData) {
            if (data instanceof Sum)
                expandData = true;
            if (data instanceof Product) {
                for (Tensor t : data)
                    if (t instanceof Sum) {
                        expandData = true;
                        break;
                    }
            }
        }

        if (!expandData && !expandIndexless)
            return product;

        if (!expandData) {
            TensorBuilder expandBuilder = ExpandBuilders.createExpandBuilderIndexless(transformations);
            expandBuilder.put(indexless);
            return Tensors.multiply(expandBuilder.build(), data);
        }

        if (!expandIndexless) {
            TensorBuilder expandBuilder = ExpandBuilders.createExpandBuilderData(transformations);
            expandBuilder.put(data);
            Tensor newData = expandBuilder.build();
            if (newData instanceof Sum)
                return Tensors.multiplySumElementsOnScalarFactorAndExpandScalars((Sum) newData, indexless);
            else
                return ExpandUtils.expandIndexlessSubproduct.transform(Tensors.multiply(indexless, newData));
        }

        TensorBuilder expandBuilder = ExpandBuilders.createTotalBuilder(transformations);
        expandBuilder.put(product);
        return expandBuilder.build();
    }

    static boolean isIntegerPower(Tensor t) {
        return t instanceof Power && t.get(0) instanceof Sum && TensorUtils.isInteger(t.get(1));
    }

    static boolean isPositiveIntegerPower(Tensor t) {
        return t instanceof Power && t.get(0) instanceof Sum && TensorUtils.isNaturalNumber(t.get(1));
    }

    static boolean isNegativeIntegerPower(Tensor t) {
        return t instanceof Power && t.get(0) instanceof Sum && TensorUtils.isNegativeIntegerNumber(t.get(1));
    }

    static boolean sumContainsNonIndexless(Tensor t) {
        if (!(t instanceof Sum))
            return false;
        for (Tensor s : t)
            if (s.getIndices().size() != 0)
                return true;
        return false;
    }

    public static Tensor expandSymbolicPower(Sum argument, int power, Transformation[] transformations) {
        //TODO improve algorithm using Newton formula!!!
        int i;
        Tensor temp = argument;
        for (i = power - 1; i >= 1; --i)
            temp = expandPairOfSums((Sum) temp,
                    argument, transformations);
        return temp;
    }

    public static Tensor expandPower(Sum argument, int power, int[] forbiddenIndices, Transformation[] transformations) {
        //TODO improve algorithm using Newton formula!!!
        int i;
        Tensor temp = argument;
        TIntHashSet forbidden = new TIntHashSet(forbiddenIndices);
        TIntHashSet argIndices = TensorUtils.getAllIndicesNamesT(argument);
        forbidden.ensureCapacity(argIndices.size() * power);
        forbidden.addAll(argIndices);
        for (i = power - 1; i >= 1; --i)
            temp = expandPairOfSums((Sum) temp,
                    (Sum) ApplyIndexMapping.renameDummy(argument, forbidden.toArray(), forbidden),
                    transformations);

        return temp;
    }

    public static final Transformation expandIndexlessSubproduct = new Transformation() {
        @Override
        public Tensor transform(Tensor t) {
            if (!(t instanceof Product))
                return t;
            Product p = (Product) t;
            Tensor indexless = p.getIndexlessSubProduct();

            boolean needExpand = false;
            if (indexless instanceof Product)
                for (Tensor i : indexless)
                    if (i instanceof Sum) {
                        needExpand = true;
                        break;
                    }
            if (needExpand)
                return Tensors.multiply(expandProductOfSums((Product) indexless, new Transformation[0]), p.getDataSubProduct());
            return t;
        }
    };

    static final class NumDen {
        final Tensor numerator, denominator;

        NumDen(Tensor numerator, Tensor denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
        }
    }

    static NumDen getNumDen(Product product) {
        ProductBuilder denominators = new ProductBuilder();
        Tensor temp = product;
        Tensor t;
        for (int i = product.size() - 1; i >= 0; --i) {
            t = product.get(i);
            if (isNegativeIntegerPower(t)) {
                assert ((Complex) t.get(1)).isMinusOne();
                denominators.put(t.get(0));
                if (temp instanceof Product)
                    temp = ((Product) temp).remove(i);
                else
                    temp = Complex.ONE;
            }
        }
        return new NumDen(temp, denominators.build());
    }
}
