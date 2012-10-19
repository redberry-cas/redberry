package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.ApplyIndexMapping;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class NewSubstitution implements Transformation {
    private final NodeTransformer[] transformers;

    public NewSubstitution(Tensor[] from, Tensor[] to) {
        if (from.length != to.length)
            throw new IllegalArgumentException();
        transformers = new NodeTransformer[from.length];
        for (int i = 0; i < from.length; ++i)
            transformers[i] = createNodeTransformer(from[i], to[i]);
    }

    @Override
    public Tensor transform(Tensor t) {
        SubstitutionIterator iterator = new SubstitutionIterator(t);
        Tensor current;
        while ((current = iterator.next()) != null) {
            for (NodeTransformer transformer : transformers)
                current = transformer.transform(current, iterator.getForbidden());
            iterator.set(current);
        }
        return iterator.result();
    }

    private static NodeTransformer createNodeTransformer(Tensor from, Tensor to) {
        return null;
    }

    private static abstract class NodeTransformer {
        final Tensor from, to;
        final boolean symbolic;

        protected NodeTransformer(Tensor from, Tensor to) {
            this.from = from;
            this.to = to;
            symbolic = TensorUtils.isSymbolic(to);
        }

        protected abstract Tensor transform(Tensor current, int[] forbidden);
    }

    private static class SimpleTensorTransformer extends NodeTransformer {
        private SimpleTensorTransformer(Tensor from, Tensor to) {
            super(from, to);
        }

        @Override
        protected Tensor transform(Tensor current, int[] forbidden) {
            if (current.getClass() != SimpleTensor.class)
                return current;
            IndexMappingBuffer buffer =
                    IndexMappings.getFirst(from, current);
            if (buffer == null)
                return current;
            Tensor newTo;
            if (symbolic)
                newTo = to;
            else
                newTo = ApplyIndexMapping.applyIndexMapping(to, buffer, forbidden);
            if (buffer.getSignum())
                newTo = Tensors.negate(newTo);
            return newTo;
        }
    }

    private static class ProductTransformer extends NodeTransformer {
        private final Complex fromFactor;
        private final Tensor[] fromIndexless, fromData;
        private final ProductContent fromContent;

        private ProductTransformer(Tensor from, Tensor to) {
            super(from, to);
            Product product = (Product) from;
            this.fromFactor = product.getFactor();
            this.fromIndexless = product.getIndexless();
            this.fromContent = product.getContent();
            this.fromData = fromContent.getDataCopy();
        }

        @Override
        protected Tensor transform(Tensor current, int[] forabidden) {
            TIntHashSet forbidden = null;
            while (current instanceof Product) {
                Product cp = (Product) current;
                IndexMappingBuffer buffer = null;

                final Tensor[] currentIndexless = cp.getIndexless();
                int[] indexlessBijection;
                IndexlessBijectionsPort indexlessPort = new IndexlessBijectionsPort(fromIndexless, currentIndexless);
                while ((indexlessBijection = indexlessPort.take()) != null) {
                    buffer = IndexMappings.createBijectiveProductPort(fromIndexless, extract(currentIndexless, indexlessBijection)).take();
                    if (buffer != null)
                        break;
                }
                if (buffer == null)
                    break;

                boolean sign = buffer.getSignum();
                buffer = null;
                ProductContent currentContent = cp.getContent();
                final Tensor[] currentData = currentContent.getDataCopy();
                int[] dataBijection;
                ProductsBijectionsPort dataPort = new ProductsBijectionsPort(fromContent, currentContent);
                while ((dataBijection = dataPort.take()) != null) {
                    buffer = IndexMappings.createBijectiveProductPort(fromData, extract(currentData, dataBijection)).take();
                    if (buffer != null)
                        break;
                }
                if (buffer == null)
                    break;

                buffer.addSignum(sign);
                Tensor newTo;
                int i;
                if (symbolic)
                    newTo = to;
                else {
                    if (forbidden == null) {
                        //TODO review
                        forbidden = new TIntHashSet(iterator.getForbidden());
                        int pivot = 0;
                        for (i = 0; i < currentIndexless.length; ++i) {
                            if (pivot < indexlessBijection.length && i == indexlessBijection[pivot])
                                ++pivot;
                            else
                                forbidden.addAll(TensorUtils.getAllIndicesNames(currentIndexless[i]));
                        }
                        pivot = 0;
                        for (i = 0; i < currentData.length; ++i) {
                            if (pivot < dataBijection.length && i == dataBijection[pivot])
                                ++pivot;
                            else
                                forbidden.addAll(TensorUtils.getAllIndicesNames(currentData[i]));
                        }

                    }
                    newTo = ApplyIndexMapping.applyIndexMapping(to, buffer, forbidden.toArray());
                    if (newTo != to)
                        forbidden.addAll(TensorUtils.getAllIndicesNames(newTo));
                }

                Arrays.sort(indexlessBijection);
                Arrays.sort(dataBijection);

                ProductBuilder builder = new ProductBuilder();
                builder.put(newTo);

                int pivot = 0;
                for (i = 0; i < currentIndexless.length; ++i) {
                    if (pivot < indexlessBijection.length && i == indexlessBijection[pivot])
                        ++pivot;
                    else
                        builder.put(currentIndexless[i]);
                }
                pivot = 0;
                for (i = 0; i < currentData.length; ++i) {
                    if (pivot < dataBijection.length && i == dataBijection[pivot])
                        ++pivot;
                    else
                        builder.put(currentData[i]);
                }


                builder.put(cp.getFactor().divide(fromFactor));
                current = builder.build();
            }

            return null;
        }
    }

}
