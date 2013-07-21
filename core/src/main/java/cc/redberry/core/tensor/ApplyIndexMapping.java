/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
 *   Stanislav Poslavsky   <stvlpos@mail.ru>
 *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
 *
 * This file is part of Redberry.
 *
 * Redberry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Redberry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Redberry. If not, see <http://www.gnu.org/licenses/>.
 */
package cc.redberry.core.tensor;

import cc.redberry.core.indexgenerator.IndexGenerator;
import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.IndicesBuilder;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.functions.ScalarFunction;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArray;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;

import static cc.redberry.core.indices.IndicesUtils.getIndicesNames;
import static cc.redberry.core.indices.IndicesUtils.getType;

/**
 * Static methods to rename indices of tensors.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class ApplyIndexMapping {
    /**
     * Renames dummy indices of tensor prohibiting some dummy index to be equal to one of the specified
     * <i>forbidden</i> indices.
     *
     * @param tensor         tensor
     * @param forbiddenNames forbidden indices names
     * @param added          set which will be added by generated dummy indices
     * @return tensor with renamed dummies
     */
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

    /**
     * Renames dummy indices of tensor prohibiting some dummy index to be equal to one of the specified
     * <i>forbidden</i> indices.
     *
     * @param tensor         tensor
     * @param forbiddenNames forbidden indices names
     * @return tensor with renamed dummies
     */
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

    private static Tensor renameDummyWithSign(Tensor tensor, int[] forbidden, boolean sign) {
        Tensor result = renameDummy(tensor, forbidden);
        return sign ? Tensors.negate(result) : result;
    }

    /**
     * Applies given mapping of indices to tensor. In contrast to {@link #applyIndexMapping(Tensor, Mapping, int[])}
     * this method does not assumes that {@code from} indices matches exactly free indices of tensor:
     * if some free index of specified tensor does not present in map, then it will be mapped on itself;
     * if some index present in map, but does not present in free indices of tensor, then this mapping rule will
     * be ignored.
     *
     * @param tensor  tensor
     * @param mapping mapping of indices
     * @return tensor with renamed indices
     * @throws IllegalArgumentException if {@code from.length != to.length}
     */
    public static Tensor applyIndexMappingAutomatically(Tensor tensor, Mapping mapping) {
        return applyIndexMappingAutomatically(tensor, mapping, new int[0]);
    }

    /**
     * Applies given mapping of indices to tensor. In contrast to {@link #applyIndexMapping(Tensor, Mapping, int[])}
     * this method does not assumes that {@code from} indices matches exactly free indices of tensor:
     * if some free index of specified tensor does not present in map, then it will be mapped on itself;
     * if some index present in map, but does not present in free indices of tensor, then this mapping rule will
     * be ignored.
     *
     * @param tensor    tensor
     * @param mapping   mapping of indices
     * @param forbidden forbidden indices names
     * @return tensor with renamed indices
     * @throws IllegalArgumentException if {@code from.length != to.length}
     */
    public static Tensor applyIndexMappingAutomatically(Tensor tensor, Mapping mapping, int[] forbidden) {
        if (mapping.isEmpty() || tensor.getIndices().getFree().size() == 0)
            return renameDummyWithSign(tensor, forbidden, mapping.getSign());


        final int[] freeIndices = IndicesUtils.getIndicesNames(tensor.getIndices().getFree());
        Arrays.sort(freeIndices);

        //removing indices from {@code from} array that are not contained in free indices of tensor
        int[] from = mapping.getFromNames().copy(),
                to = mapping.getToData().copy();

        int i, pointer = 0, oldFromLength = from.length;
        for (i = 0; i < oldFromLength; ++i) {
            if (Arrays.binarySearch(freeIndices, from[i]) >= 0) {
                from[pointer] = from[i];
                to[pointer] = to[i];
                ++pointer;
            }
        }

        //no indices to map
        if (pointer == 0)
            return renameDummyWithSign(tensor, forbidden, mapping.getSign());

        int newFromLength = pointer;

        //adding free indices that do not present in {@code from} array to it
        ArraysUtils.quickSort(from, 0, pointer, to);
        IntArrayList list = new IntArrayList();
        for (i = 0; i < freeIndices.length; ++i)
            if (Arrays.binarySearch(from, 0, pointer, freeIndices[i]) < 0) {
                if (newFromLength < oldFromLength)
                    from[newFromLength] = to[newFromLength] = freeIndices[i];
                else
                    list.add(freeIndices[i]);
                ++newFromLength;
            }

        // if newFromLength < oldFromLength then list must be
        // empty and {@code from} and {@code to} arrays
        // will be simply truncated so subsequent
        // {@code arraycopy(...)} will do nothing
        // if newFromLength > oldFromLength then list is not empty
        // and it will be appended to {@code from} and {@code to}
        if (newFromLength < oldFromLength) {
            from = Arrays.copyOfRange(from, 0, newFromLength);
            to = Arrays.copyOfRange(to, 0, newFromLength);
        } else if (newFromLength > oldFromLength) {
            int[] toAdd = list.toArray();
            from = Arrays.copyOfRange(from, 0, newFromLength);
            to = Arrays.copyOfRange(to, 0, newFromLength);
            System.arraycopy(toAdd, 0, from, oldFromLength, toAdd.length);
            System.arraycopy(toAdd, 0, to, oldFromLength, toAdd.length);
        }

        assert from.length == freeIndices.length;

        return applyIndexMapping(tensor, new Mapping(from, to, mapping.getSign()), forbidden);
    }

    /**
     * Applies specified mapping of indices to tensor.
     *
     * @param tensor  tensor
     * @param mapping mapping of indices
     * @return tensor with renamed indices
     */
    public static Tensor applyIndexMapping(Tensor tensor, Mapping mapping) {
        return applyIndexMapping(tensor, mapping, new int[0]);
    }

    /**
     * Applies specified mapping of indices to tensor preventing some dummy index to be equal to one of the specified
     * <i>forbidden</i> indices.
     *
     * @param tensor    tensor
     * @param mapping   mapping of indices
     * @param forbidden forbidden indices
     * @return tensor with renamed indices
     */
    public static Tensor applyIndexMapping(Tensor tensor, Mapping mapping, int[] forbidden) {
        if (mapping.isEmpty()) {
            if (tensor.getIndices().getFree().size() != 0)
                throw new IllegalArgumentException("From length does not match free indices size.");
            return renameDummyWithSign(tensor, forbidden, mapping.getSign());
        }
        int[] freeIndicesNames = IndicesUtils.getIndicesNames(tensor.getIndices().getFree());
        Arrays.sort(freeIndicesNames);
        if (!mapping.getFromNames().equalsToArray(freeIndicesNames))
            throw new IllegalArgumentException("From indices names does not match free indices names of tensor.");
        Tensor result = _applyIndexMapping(tensor, mapping, forbidden);
        return mapping.getSign() ? Tensors.negate(result) : result;
    }

    private static Tensor _applyIndexMapping(Tensor tensor, Mapping mapping, int[] forbidden) {
        final int mappingSize = mapping.size();
        int[] allForbidden = new int[mappingSize + forbidden.length];
        IntArray toData = mapping.getToData(), fromNames = mapping.getFromNames();
        ArraysUtils.arraycopy(toData, 0, allForbidden, 0, mappingSize);
        System.arraycopy(forbidden, 0, allForbidden, mappingSize, forbidden.length);
        int i;
        for (i = allForbidden.length - 1; i >= 0; --i)
            allForbidden[i] = IndicesUtils.getNameWithType(allForbidden[i]);

        IntArrayList fromL = new IntArrayList(mappingSize), toL = new IntArrayList(mappingSize);
        fromL.addAll(fromNames);
        toL.addAll(toData);

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
                assert ArraysUtils.binarySearch(fromNames, index) < 0;
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
                // we can pass the hash code, since we did not changed the order of
                // indexless data, and its hash cannot been changed by the renaming of dummies
                return new Product(product.indices, product.factor, newIndexless, data, product.contentReference);

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
            return new Sum(newData, IndicesFactory.create(newData[0].getIndices().getFree()));
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

    //todo discuss with Dima
    public static Tensor renameIndicesOfFieldsArguments(Tensor tensor, TIntSet forbidden) {
        if (tensor instanceof TensorField) {
            TensorField field = (TensorField) tensor;
            Tensor[] args = null;
            SimpleIndices[] argsIndices = null;
            Tensor arg;

            //indices of arg to be renamed
            int[] _from, _to;
            IndexMapper mapping;
            int j;
            int[] _forbidden = forbidden.toArray();
            for (int i = field.size() - 1; i >= 0; --i) {
                arg = field.args[i];
                IndexGenerator ig = new IndexGenerator(_forbidden);
                _from = TensorUtils.getAllIndicesNamesT(arg).toArray();
                Arrays.sort(_from);
                _to = new int[_from.length];
                for (j = _from.length - 1; j >= 0; --j) {
                    if (forbidden.contains(_from[j]))
                        _to[j] = ig.generate(getType(_from[j]));
                    else _to[j] = _from[j];
                    forbidden.add(_to[j]);
                }
                arg = applyIndexMapping(arg, mapping = new IndexMapper(_from, _to));
                if (arg != field.args[i]) {
                    if (args == null) {
                        args = field.args.clone();
                        argsIndices = field.argIndices.clone();
                    }
                    args[i] = arg;
                    argsIndices[i] = field.argIndices[i].applyIndexMapping(mapping);
                }
            }


//            //rename dummies and save newly generated forbidden indices
//            //we need do this before renaming free since IndexGenerator must know about all forbidden indices
//            for (int i = field.size() - 1; i >= 0; --i) {
//                arg = field.args[i];
//                arg = renameDummy(arg, forbidden.toArray(), forbidden);
//                if (arg != field.args[i]) {
//                    if (args == null)
//                        args = field.args.clone();
//                    args[i] = arg;
//                }
//            }
//
//            IndexGenerator ig = new IndexGenerator(forbidden.toArray());
//            IndexMapper mapping;
//            for (int i = field.size() - 1; i >= 0; --i) {
//                arg = field.args[i];
//                _from = getIndicesNames(arg.getIndices().getFree());
//                _to = new int[_from.length];
//                forbidden.ensureCapacity(_from.length);
//                for (j = _from.length - 1; j >= 0; --j)                 {
//                    //free index of arg is forbidden
//                    if (forbidden.contains(getNameWithType(_from[j]))) {
//                        _to[j] = ig.generate(getType(_from[j]));
//                        forbidden.add(_to[j]);
//                    } else _to[j] = _from[j];
//                    forbidden.add(_from[j]);
//                }
//
//                arg = applyIndexMapping(arg, mapping = new IndexMapper(_from, _to));
//                if (arg != field.args[i]) {
//                    if (args == null)
//                        args = field.args.clone();
//                    if (argsIndices == null)
//                        argsIndices = field.argIndices.clone();
//                    args[i] = arg;
//                    argsIndices[i] = field.argIndices[i].applyIndexMapping(mapping);
//                }
//            }
            if (args == null)
                return tensor;
            return Tensors.field(field.name, field.indices, argsIndices, args);
        }
        //further straightforward
        if (tensor instanceof Product) {
            Product p = (Product) tensor;
            Tensor[] data = null, indexless = null;
            Tensor temp;
            int i;
            for (i = p.data.length - 1; i >= 0; --i) {
                temp = renameIndicesOfFieldsArguments(p.data[i], forbidden);
                if (temp != p.data[i]) {
                    if (data == null)
                        data = p.data.clone();
                    data[i] = temp;
                }
            }
            for (i = p.indexlessData.length - 1; i >= 0; --i) {
                temp = renameIndicesOfFieldsArguments(p.indexlessData[i], forbidden);
                if (temp != p.indexlessData[i]) {
                    if (indexless == null)
                        indexless = p.indexlessData.clone();
                    indexless[i] = temp;
                }
            }

            if (data == null && indexless == null) return tensor;

            if (data == null) data = p.data;//no cloning
            if (indexless == null) indexless = p.indexlessData;//no cloning

            return new Product(p.indices, p.factor, indexless, data, p.contentReference, p.hash);
        }
        if (tensor instanceof Sum) {
            Sum s = (Sum) tensor;
            Tensor temp, data[] = null;
            for (int i = s.size() - 1; i >= 0; --i) {
                temp = renameIndicesOfFieldsArguments(s.data[i], forbidden);
                if (temp != s.data[i]) {
                    if (data == null)
                        data = s.data.clone();
                    data[i] = temp;
                }
            }
            if (data == null) return tensor;
            return new Sum(s.indices, data, s.hash);
        }

        if (tensor instanceof Complex || tensor instanceof SimpleTensor)
            return tensor;

        if (tensor instanceof Power || tensor instanceof Expression) {
            Tensor a = renameIndicesOfFieldsArguments(tensor.get(0), forbidden),
                    b = renameIndicesOfFieldsArguments(tensor.get(1), forbidden);
            if (a == tensor.get(0) && b == tensor.get(1))
                return tensor;
            return tensor.getFactory().create(a, b);
        }

        if (tensor instanceof ScalarFunction) {
            Tensor arg = renameIndicesOfFieldsArguments(tensor.get(0), forbidden);
            if (arg == tensor.get(0)) return tensor;
            return tensor.getFactory().create(arg);
        }

        throw new RuntimeException();
    }

    private static void checkConsistent(Tensor tensor, final int[] from) {
        int[] freeIndices = tensor.getIndices().getFree().getAllIndices().copy();
        Arrays.sort(freeIndices);
        if (!Arrays.equals(freeIndices, from))
            throw new IllegalArgumentException("From indices are not equal to free indices of tensor.");
    }
}
