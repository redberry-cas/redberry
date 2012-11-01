package cc.redberry.core.tensor;

import cc.redberry.core.indexgenerator.IndexGenerator;
import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappingBufferRecord;
import cc.redberry.core.indices.IndicesBuilder;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.functions.ScalarFunction;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.Map;

import static cc.redberry.core.indices.IndicesUtils.getIndicesNames;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ApplyIndexMapping {

    public static Tensor renameDummy(Tensor tensor, int[] forbiddenNames, TIntHashSet added) {
        if (forbiddenNames.length == 0)
            return tensor;
        if (tensor instanceof Complex || tensor instanceof ScalarFunction)
            return tensor;

        TIntHashSet allIndicesNames = TensorUtils.getAllDummyIndicesT(tensor);
        //no indices in tensor
        if (allIndicesNames.isEmpty())
            return tensor;

        allIndicesNames.ensureCapacity(forbiddenNames.length);

        IntArrayList fromL = null;
        for (int forbidden : forbiddenNames) {
            if (!allIndicesNames.add(forbidden)) {
                if (fromL == null)
                    fromL = new IntArrayList();
                fromL.add(forbidden);
            }
        }

        if (fromL == null)
            return tensor;

        allIndicesNames.addAll(getIndicesNames(tensor.getIndices().getFree()));
        IndexGenerator generator = new IndexGenerator(allIndicesNames.toArray());
        int[] from = fromL.toArray(), to = new int[fromL.size()];
        Arrays.sort(from);
        added.ensureCapacity(from.length);
        int i;
        for (i = from.length - 1; i >= 0; --i)
            added.add(to[i] = generator.generate(IndicesUtils.getType(from[i])));


        return applyIndexMapping(tensor, new IndexMapper(from, to), false);
    }

    public static Tensor renameDummy(Tensor tensor, int[] forbiddenNames) {
        if (forbiddenNames.length == 0)
            return tensor;
        if (tensor instanceof Complex || tensor instanceof ScalarFunction)
            return tensor;

        TIntHashSet allIndicesNames = TensorUtils.getAllDummyIndicesT(tensor);
        //no indices in tensor
        if (allIndicesNames.isEmpty())
            return tensor;

        allIndicesNames.ensureCapacity(forbiddenNames.length);

        IntArrayList fromL = null;
        for (int forbidden : forbiddenNames) {
            if (!allIndicesNames.add(forbidden)) {
                if (fromL == null)
                    fromL = new IntArrayList();
                fromL.add(forbidden);
            }
        }

        if (fromL == null)
            return tensor;

        allIndicesNames.addAll(getIndicesNames(tensor.getIndices().getFree()));
        IndexGenerator generator = new IndexGenerator(allIndicesNames.toArray());
        int[] from = fromL.toArray(), to = new int[fromL.size()];
        Arrays.sort(from);
        int i;
        for (i = from.length - 1; i >= 0; --i)
            to[i] = generator.generate(IndicesUtils.getType(from[i]));

        return applyIndexMapping(tensor, new IndexMapper(from, to), false);
    }

    public static Tensor applyIndexMapping(Tensor tensor, IndexMappingBuffer buffer) {
        return applyIndexMapping(tensor, buffer, new int[0]);
    }

    public static Tensor applyIndexMapping(Tensor tensor, IndexMappingBuffer buffer, int[] forbidden) {
        if (buffer.isEmpty()) {
            if (tensor.getIndices().getFree().size() != 0)
                throw new IllegalArgumentException("From indices are not equal to free indices of tensor.");
            Tensor t = renameDummy(tensor, forbidden);
            if (buffer.getSignum())
                return Tensors.negate(t);
            return t;
        }
        if (tensor instanceof Complex || tensor instanceof ScalarFunction)
            return tensor;

        Map<Integer, IndexMappingBufferRecord> map = buffer.getMap();
        int[] from = new int[map.size()], to = new int[map.size()];
        int count = 0;
        IndexMappingBufferRecord record;
        for (Map.Entry<Integer, IndexMappingBufferRecord> entry : map.entrySet()) {
            from[count] = entry.getKey();
            record = entry.getValue();
            to[count++] = record.getIndexName() ^ (record.diffStatesInitialized() ? 0x80000000 : 0);
        }

        final int[] freeIndices = getIndicesNames(tensor.getIndices().getFree());
        Arrays.sort(freeIndices);
        int[] _from = from.clone();
        Arrays.sort(_from);
        if (!Arrays.equals(freeIndices, _from))
            throw new IllegalArgumentException("From indices are not equal to free indices of tensor.");

        Tensor result = applyIndexMappingFromPreparedSource(tensor, from, to, forbidden);
        if (buffer.getSignum())
            return Tensors.negate(result);
        else
            return result;
    }

    /**
     * @param tensor
     * @param from   sorted indices
     */
    private static void checkConsistent(Tensor tensor, final int[] from) {
        int[] freeIndices = tensor.getIndices().getFree().getAllIndices().copy();
        Arrays.sort(freeIndices);
        if (!Arrays.equals(freeIndices, from))
            throw new IllegalArgumentException("From indices are not equal to free indices of tensor.");
    }

    public static Tensor applyIndexMapping(Tensor tensor, int[] from, int[] to, int[] forbidden) {
        if (from.length == 0) {
            if (tensor.getIndices().getFree().size() != 0 || to.length != 0)
                throw new IllegalArgumentException("from legth does not match free indices size or to length.");
            return renameDummy(tensor, forbidden);
        }
        return applyIndexMapping1(tensor, from.clone(), to.clone(), forbidden);
    }

    private static Tensor applyIndexMapping1(Tensor tensor, int[] from, int[] to, int[] forbidden) {
        if (tensor instanceof Complex || tensor instanceof ScalarFunction)
            return tensor;

        ArraysUtils.quickSort(from, to);
        checkConsistent(tensor, from);

        int i, rawState;
        for (i = from.length - 1; i >= 0; --i) {
            rawState = IndicesUtils.getRawStateInt(from[i]);
            from[i] ^= rawState;
            to[i] ^= rawState;
        }
        ArraysUtils.quickSort(from, to);
        return applyIndexMappingFromPreparedSource(tensor, from, to, forbidden);
    }

    private static Tensor applyIndexMappingFromPreparedSource(Tensor tensor, int[] from, int[] to, int[] forbidden) {

        int[] allForbidden = new int[to.length + forbidden.length];
        System.arraycopy(to, 0, allForbidden, 0, to.length);
        System.arraycopy(forbidden, 0, allForbidden, to.length, forbidden.length);
        int i;
        for (i = allForbidden.length - 1; i >= 0; --i)
            allForbidden[i] = IndicesUtils.getNameWithType(allForbidden[i]);

        IntArrayList fromL = new IntArrayList(from.length), toL = new IntArrayList(to.length);
        fromL.addAll(from);
        toL.addAll(to);

        Arrays.sort(allForbidden);

        final int[] dummyIndices = TensorUtils.getAllDummyIndicesT(tensor).toArray();
        final int[] forbiddenGeneratorIndices = new int[allForbidden.length + dummyIndices.length];
        System.arraycopy(allForbidden, 0, forbiddenGeneratorIndices, 0, allForbidden.length);
        System.arraycopy(dummyIndices, 0, forbiddenGeneratorIndices, allForbidden.length, dummyIndices.length);

        IndexGenerator generator = new IndexGenerator(forbiddenGeneratorIndices);
        for (int index : dummyIndices)
            if (Arrays.binarySearch(allForbidden, index) >= 0) {
                //if index is dummy it cannot be free, so from (which is equal to free)
                //cannot contain it
                //assert Arrays.binarySearch(from, index) < 0;
                fromL.add(index);
                toL.add(generator.generate(IndicesUtils.getType(index)));
            }

        int[] _from = fromL.toArray(), _to = toL.toArray();
        ArraysUtils.quickSort(_from, _to);

        return applyIndexMapping(tensor, new IndexMapper(_from, _to));
    }

    private static Tensor applyIndexMapping(Tensor tensor, IndexMapper indexMapper) {
        if (tensor instanceof SimpleTensor)
            return applyIndexMapping(tensor, indexMapper, false);
        if (tensor instanceof Complex || tensor instanceof ScalarFunction)
            return tensor;

        return applyIndexMapping(tensor, indexMapper, indexMapper.contract(getIndicesNames(tensor.getIndices().getFree())));
    }

    private static Tensor applyIndexMapping(Tensor tensor, IndexMapper indexMapper, boolean contractIndices) {
        if (tensor instanceof SimpleTensor) {
            SimpleTensor simpleTensor = (SimpleTensor) tensor;
            SimpleIndices oldIndices = simpleTensor.getIndices(),
                    newIndices = oldIndices.applyIndexMapping(indexMapper);
            if (oldIndices == newIndices)
                return tensor;
            if (tensor instanceof TensorField) {
                TensorField field = (TensorField) simpleTensor;
                return Tensors.field(field.name, newIndices, field.argIndices, field.args);
            }
            return Tensors.simpleTensor(simpleTensor.name, newIndices);
        }
        if (tensor instanceof Complex || tensor instanceof ScalarFunction)
            return tensor;

        if (tensor instanceof Expression) {
            boolean contract = indexMapper.contract(getIndicesNames(tensor.getIndices()));
            return Tensors.expression(applyIndexMapping(tensor.get(0), indexMapper, contract),
                    applyIndexMapping(tensor.get(1), indexMapper, contract));
        }

        if (tensor instanceof Power) {
            Tensor oldBase = tensor.get(0),
                    newBase = applyIndexMapping(oldBase, indexMapper, false);
            if (oldBase == newBase)
                return tensor;
            return new Power(newBase, tensor.get(1));
        }

        // all types except sums and products are already processed at this point

        if (contractIndices) {
            TensorBuilder builder = tensor.getBuilder();
            for (Tensor t : tensor)
                builder.put(applyIndexMapping(t, indexMapper));
            return builder.build();
        }

        if (tensor instanceof Product) {

            Product product = (Product) tensor;
            Tensor[] indexless = product.getIndexless(), newIndexless = null;
            Tensor[] data = product.data, newData = null;

            int i;
            Tensor oldTensor, newTensor;
            for (i = indexless.length - 1; i >= 0; --i) {
                oldTensor = indexless[i];
                newTensor = applyIndexMapping(oldTensor, indexMapper, false);
                if (oldTensor != newTensor) {
                    if (newIndexless == null)
                        newIndexless = indexless.clone();
                    newIndexless[i] = newTensor;
                }
            }

            for (i = data.length - 1; i >= 0; --i) {
                oldTensor = data[i];
                newTensor = applyIndexMapping(oldTensor, indexMapper, false);
                if (oldTensor != newTensor) {
                    if (newData == null)
                        newData = data.clone();
                    newData[i] = newTensor;
                }
            }
            if (newIndexless == null)
                newIndexless = indexless;

            if (newData == null)
                return new Product(product.indices, product.factor, newIndexless, data, product.contentReference, product.hash);

            return new Product(new IndicesBuilder().append(newData).getIndices(), product.factor, newIndexless, newData);
        }

        if (tensor instanceof Sum) {
            Sum sum = (Sum) tensor;
            Tensor[] data = sum.data, newData = null;
            Tensor oldTensor, newTensor;
            for (int i = data.length - 1; i >= 0; --i) {
                oldTensor = data[i];
                newTensor = applyIndexMapping(oldTensor, indexMapper, false);
                if (oldTensor != newTensor) {
                    if (newData == null)
                        newData = data.clone();
                    newData[i] = newTensor;
                }
            }
            if (newData == null)
                return tensor;
            return new Sum(newData, IndicesFactory.createSorted(newData[0].getIndices().getFree()));
        }

        throw new RuntimeException();
    }


    private final static class IndexMapper implements IndexMapping {

        private final int[] from, to;

        public IndexMapper(int[] from, int[] to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public int map(int index) {
            int position = Arrays.binarySearch(from, IndicesUtils.getNameWithType(index));
            if (position < 0)
                return index;
            return IndicesUtils.getRawStateInt(index) ^ to[position];
        }

        boolean contract(final int[] freeIndicesNames) {
            if (freeIndicesNames.length <= 1)
                return false;
            int i;
            for (i = 0; i < freeIndicesNames.length; ++i)
                freeIndicesNames[i] = 0x7FFFFFFF & map(freeIndicesNames[i]);
            Arrays.sort(freeIndicesNames);
            for (i = 1; i < freeIndicesNames.length; ++i)
                if (freeIndicesNames[i] == freeIndicesNames[i - 1])
                    return true;
            return false;
        }
    }
}
