package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class PrimitiveProductSubstitution extends PrimitiveSubstitution {
    private final Complex fromFactor;
    private final Tensor[] fromIndexless, fromData;
    private final ProductContent fromContent;

    public PrimitiveProductSubstitution(Tensor from, Tensor to) {
        super(from, to);
        Product product = (Product) from;
        this.fromFactor = product.getFactor();
        this.fromIndexless = product.getIndexless();
        this.fromContent = product.getContent();
        this.fromData = fromContent.getDataCopy();
    }

    @Override
    Tensor newTo_(Tensor currentNode, int[] forbiddenIndices) {
        TIntHashSet forbidden = null;
        while (currentNode instanceof Product) {
            Product cp = (Product) currentNode;
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
            if (toIsSymbolic)
                newTo = buffer.getSignum() ? Tensors.negate(to) : to;
            else {
                if (forbidden == null) {
                    //TODO review
                    forbidden = new TIntHashSet(forbiddenIndices);
                    int pivot = 0;
                    for (i = 0; i < currentIndexless.length; ++i) {
                        if (pivot < indexlessBijection.length && i == indexlessBijection[pivot])
                            ++pivot;
                        else
                            forbidden.addAll(TensorUtils.getAllIndicesNamesT(currentIndexless[i]));
                    }
                    pivot = 0;
                    for (i = 0; i < currentData.length; ++i) {
                        if (pivot < dataBijection.length && i == dataBijection[pivot])
                            ++pivot;
                        else
                            forbidden.addAll(TensorUtils.getAllIndicesNamesT(currentData[i]));
                    }

                }
                newTo = ApplyIndexMapping.applyIndexMapping(to, buffer, forbidden.toArray());
                if (newTo != to)
                    forbidden.addAll(TensorUtils.getAllIndicesNamesT(newTo));
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
            currentNode = builder.build();
        }
        return currentNode;
    }


    private static Tensor[] extract(final Tensor[] source, final int[] positions) {
        Tensor[] r = new Tensor[positions.length];
        for (int i = 0; i < positions.length; ++i)
            r[i] = source[positions[i]];
        return r;
    }
}
