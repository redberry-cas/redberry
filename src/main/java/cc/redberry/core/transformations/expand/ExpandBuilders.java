package cc.redberry.core.transformations.expand;

import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.ArraysUtils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class ExpandBuilders {

    static final TensorBuilder createExpandBuilderIndexless(Transformation... transformations) {
        return new ExpandBuilder(transformations);
    }

    static final TensorBuilder createExpandBuilderData(Transformation... transformations) {
        return new ExpandBuilder(ArraysUtils.addAll(new Transformation[]{
                ExpandUtils.expandIndexlessSubproduct}, transformations));
    }

    static final TensorBuilder createTotalBuilder(Transformation... transformations) {
        return new TotalExpandBuilder(transformations);
    }

    private static final class TotalExpandBuilder implements TensorBuilder {
        //first is for indexless, second is for data
        final TensorBuilder[] builders = new ExpandBuilder[2];
        final Transformation[] transformations;

        TotalExpandBuilder(Transformation[] transformations) {
            this.transformations = transformations;
            builders[0] = createExpandBuilderIndexless(transformations);
        }

        @Override
        public Tensor build() {
            if (builders[1] == null)
                return builders[0].build();
            Tensor indexless = builders[0].build(), data = builders[1].build();
            if (data instanceof Sum)
                return Tensors.multiplySumElementsOnScalarFactorAndExpandScalars((Sum) data, indexless);
            Tensor result = Tensors.multiply(indexless, data);
            return ExpandUtils.expandIndexlessSubproduct.transform(result);
        }

        @Override
        public void put(Tensor tensor) {
            if (tensor instanceof Product) {
                for (Tensor t : tensor)
                    put(t);
                return;
            }
            if (tensor.getIndices().size() == 0) {
                if (ExpandUtils.sumContainsNonIndexless(tensor))
                    putData(tensor);
                else
                    builders[0].put(tensor);
            } else
                putData(tensor);
        }

        void putData(Tensor t) {
            if (builders[1] == null)
                builders[1] = createExpandBuilderData(transformations);
            builders[1].put(t);
        }


        @Override
        public TensorBuilder clone() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ExpandBuilder implements TensorBuilder {
        private final Transformation[] transformations;
        private final TensorBuilder nonSum = new DefaultBuilder(ProductFactory.FACTORY);
        private Sum sum;


        ExpandBuilder(Transformation[] transformations) {
            this.transformations = transformations;
        }

        @Override
        public Tensor build() {
            if (sum == null)
                return nonSum.build();
            return Tensors.multiplySumElementsOnFactor(sum, nonSum.build());
        }

        @Override
        public void put(Tensor tensor) {
            if (tensor instanceof Product) {
                for (Tensor t : tensor)
                    put(t);
                return;
            }
            if (!(tensor instanceof Sum)) {
                nonSum.put(tensor);
                return;
            }
            Sum s = (Sum) tensor;
            if (sum == null) {
                sum = s;
                return;
            }
            Tensor temp = ExpandUtils.expandPairOfSums(sum, s, transformations);
            if (temp instanceof Sum)
                sum = (Sum) temp;
            else {
                nonSum.put(temp);
                sum = null;
            }
        }

        @Override
        public TensorBuilder clone() {
            throw new UnsupportedOperationException();
        }
    }


}
