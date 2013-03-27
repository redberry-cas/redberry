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
package cc.redberry.core.transformations.collect;

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.combinatorics.Combinatorics;
import cc.redberry.core.indexgenerator.IndexGenerator;
import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappingBufferRecord;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesBuilder;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.expand.ExpandPort;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static cc.redberry.core.indices.IndicesUtils.*;
import static cc.redberry.core.tensor.Tensors.createKronecker;
import static cc.redberry.core.tensor.Tensors.createMetricOrKronecker;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class CollectTransformation implements Transformation {
    private final SimpleTensor[] patterns;
    private final TIntHashSet patternsNames;

    public CollectTransformation(SimpleTensor... patterns) {
        this.patterns = patterns;
        patternsNames = new TIntHashSet();
        for (SimpleTensor t : patterns)
            patternsNames.add(t.getName());
    }

    @Override
    public Tensor transform(Tensor t) {
        TIntObjectHashMap<ArrayList<Split>> map = new TIntObjectHashMap<>();
        OutputPortUnsafe<Tensor> port = ExpandPort.createPort(t);
        Tensor current;
        Split toAdd;
        ArrayList<Split> nodes;
        IndexGenerator generator = new IndexGenerator(TensorUtils.getAllIndicesNamesT(t).toArray());
        out:
        while ((current = port.take()) != null) {
            toAdd = split(current, generator);
            nodes = map.get(toAdd.hashCode);
            if (nodes == null) {
                nodes = new ArrayList<>();
                nodes.add(toAdd);
                map.put(toAdd.hashCode, nodes);
                continue;
            }

            int[] match;
            for (Split base : nodes) {
                if ((match = matchFactors(base.factors, toAdd.factors)) != null) {
                    //main routine
                    SimpleTensor[] toAddFactors = Combinatorics.shuffle(toAdd.factors, match);
                    IndexMappingBuffer mapping =
                            IndexMappings.createBijectiveProductPort(base.factors, toAddFactors).take();

                    ArrayList<SimpleTensor> baseKroneckers = new ArrayList(),
                            toAddKroneckers = new ArrayList();

                    TIntHashSet baseContractions = new TIntHashSet(base.getContractedIndices()),
                            toAddContractions = new TIntHashSet(toAdd.getContractedIndices());

                    IntArrayList toAddFrom2Rename = new IntArrayList(),
                            toAddTo2Rename = new IntArrayList(),
                            baseFrom2Rename = new IntArrayList(),
                            baseTo2Rename = new IntArrayList();

                    int fromIndex, toIndex, fromIndexName, toIndexName, rawState;
                    boolean diffStates;
                    for (Map.Entry<Integer, IndexMappingBufferRecord> entry : mapping.getMap().entrySet()) {
                        if (entry.getKey() == entry.getValue().getIndexName())
                            continue;
                        rawState = ((entry.getValue().getStates() & 1) ^ 1) << 31;
                        fromIndexName = entry.getKey();
                        diffStates = entry.getValue().diffStatesInitialized();
                        fromIndex = (diffStates ? 0x80000000 : 0) ^ (rawState | fromIndexName);
                        toIndexName = entry.getValue().getIndexName();
                        toIndex = rawState | toIndexName;

                        if (baseContractions.contains(fromIndexName)
                                && toAddContractions.contains(toIndexName)) {
                            toAddFrom2Rename.add(toIndexName);
                            toAddTo2Rename.add(fromIndexName);
                        } else if (baseContractions.contains(fromIndexName)) {
                            toAddKroneckers.add(
                                    createMetricOrKronecker(toIndex,
                                            diffStates ? inverseIndexState(fromIndex) : fromIndex));

                        } else if (toAddContractions.contains(toIndexName)) {
                            baseFrom2Rename.add(fromIndexName);
                            baseTo2Rename.add(toIndexName);
                            baseKroneckers.add(
                                    createMetricOrKronecker(fromIndex,
                                            diffStates ? inverseIndexState(toIndex) : toIndex));

                        } else {
                            int newIndex = generator.generate(getType(fromIndex));
                            baseFrom2Rename.add(fromIndexName);
                            baseTo2Rename.add(newIndex);
                            baseKroneckers.add(
                                    createMetricOrKronecker(fromIndex,
                                            getState(fromIndex) ? newIndex : inverseIndexState(newIndex)));
                            toAddKroneckers.add(
                                    createMetricOrKronecker(toIndex,
                                            getState(toIndex) ? newIndex : inverseIndexState(newIndex)));
                        }
                    }
                    DirectIndexMapping toRenameMapping = new StateInsensitiveMapping(baseFrom2Rename.toArray(),
                            baseTo2Rename.toArray());
                    for (int i = base.factors.length - 1; i >= 0; --i)
                        base.factors[i] = applyDirectMapping(base.factors[i], toRenameMapping);
                    Tensor kroneckerChain = Tensors.multiply(baseKroneckers.toArray(new Tensor[baseKroneckers.size()]));
                    for (int i = base.summands.size() - 1; i >= 0; --i)
                        base.summands.set(i, Tensors.multiply(base.summands.get(i), kroneckerChain));

                    int[] toAddFree = IndicesUtils.getIndicesNames(toAdd.summands.get(0).getIndices().getFree());
                    TIntHashSet temp = new TIntHashSet(toAddFrom2Rename.toArray());
                    for (int i : toAddFree)
                        if (!temp.contains(i)) {
                            toAddFrom2Rename.add(i);
                            toAddTo2Rename.add(i);
                        }

                    int[] _toAddFrom = toAddFrom2Rename.toArray(), _toAddTo = toAddTo2Rename.toArray();
                    kroneckerChain = Tensors.multiply(toAddKroneckers.toArray(new Tensor[toAddKroneckers.size()]));
                    for (int i = toAdd.summands.size() - 1; i >= 0; --i)
                        base.summands.add(
                                Tensors.multiply(
                                        ApplyIndexMapping.applyIndexMapping(toAdd.summands.get(i), _toAddFrom, _toAddTo, new int[0]),
                                        kroneckerChain));
                    continue out;
                }
            }
            nodes.add(toAdd);
        }

        SumBuilder sb = new SumBuilder(map.size());
        for (ArrayList<Split> splits : map.valueCollection())
            for (Split split : splits)
                sb.put(split.toTensor());


        return sb.build();
    }

    private static final class Split {
        final SimpleTensor[] factors;
        final ArrayList<Tensor> summands = new ArrayList<>();
        final int hashCode;//real hash code (with fields args)

        private Split(SimpleTensor[] factors, Tensor summand) {
            this.factors = factors;
            this.summands.add(summand);
            Arrays.sort(factors);
            int hash = 17;
            for (SimpleTensor f : factors)
                hash = hash * 17 + f.hashCode();
            this.hashCode = hash;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        int[] getContractedIndices() {
            return IndicesUtils.getIntersections(
                    new IndicesBuilder().append(factors).getIndices(),
                    summands.get(0).getIndices()
            );
        }

        Tensor toTensor() {
            Tensor sum = Tensors.sum(summands.toArray(new Tensor[summands.size()]));
            Tensor[] ms = new Tensor[factors.length + 1];
            ms[ms.length - 1] = sum;
            System.arraycopy(factors, 0, ms, 0, factors.length);
            return Tensors.multiply(ms);
        }
    }

    private static int[] matchFactors(SimpleTensor[] a, SimpleTensor[] b) {
        if (a.length != b.length) return null;
        int begin = 0, j, n, length = a.length;

        int[] permutation = new int[length];
        Arrays.fill(permutation, -1);

        for (int i = 1; i <= length; ++i) {
            if (i == length || a[i].getClass() == SimpleTensor.class || a[i].hashCode() != b[i - 1].hashCode()) {
                if (i - 1 != begin) {
                    OUT:
                    for (n = begin; n < i; ++n) {
                        for (j = begin; j < i; ++j)
                            if (permutation[j] == -1 && matchSimpleTensors(a[n], b[j])) {
                                permutation[n] = j;
                                continue OUT;
                            }
                        return null;
                    }
                } else {
                    if (!matchSimpleTensors(a[i - 1], b[i - 1])) return null;
                    else permutation[i - 1] = i - 1;
                }
                begin = i;
            }
        }
        return permutation;
    }

    private static boolean matchSimpleTensors(SimpleTensor a, SimpleTensor b) {
        if (a.hashCode() != b.hashCode()) return false;
        if (a.getClass() != b.getClass()) return false;
        if (a instanceof TensorField)
            for (int i = a.size() - 1; i >= 0; --i)
                if (!IndexMappings.positiveMappingExists(a.get(i), b.get(i))) return false;
        return true;
    }

    private Split split(Tensor tensor, IndexGenerator generator) {
        if (tensor instanceof SimpleTensor)
            if (patternsNames.contains(tensor.hashCode()))
                return new Split(new SimpleTensor[]{(SimpleTensor) tensor}, Complex.ONE);

        if (tensor instanceof Product) {
            //early check
            boolean containsMatches = false;
            for (Tensor t : tensor)
                if (t instanceof SimpleTensor && patternsNames.contains(((SimpleTensor) t).getName())) {
                    containsMatches = true;
                    break;
                }
            if (!containsMatches)
                return new Split(new SimpleTensor[0], tensor);

            ArrayList<SimpleTensor> factorsList = new ArrayList<>();
            Tensor summand = tensor;
            for (Tensor t : tensor) {
                if (t instanceof SimpleTensor && patternsNames.contains(((SimpleTensor) t).getName())) {
                    factorsList.add((SimpleTensor) t);
                    assert summand != Complex.ONE;
                    if (summand instanceof Product)
                        summand = ((Product) summand).remove(t);
                    else summand = Complex.ONE;
                }
            }
            final SimpleTensor[] factors = factorsList.toArray(new SimpleTensor[factorsList.size()]);

            //now we need to uncontract dummies 
            Indices factorIndices = new IndicesBuilder().append(factors).getIndices();
            if (factorIndices.size() != factorIndices.getFree().size()) {
                TIntHashSet upperIndices = new TIntHashSet(factorIndices.getUpper().length());

                SimpleIndices currentFactorIndices;
                IntArrayList from = new IntArrayList(), to = new IntArrayList();
                ArrayList<Tensor> kroneckers = new ArrayList<>();
                int j, index, newIndex;
                for (int i = 0; i < factors.length; ++i) {
                    from.clear();
                    to.clear();
                    currentFactorIndices = factors[i].getIndices();

                    for (j = currentFactorIndices.size() - 1; j >= 0; --j) {
                        index = currentFactorIndices.get(j);
                        if (IndicesUtils.getState(index))
                            upperIndices.add(index);
                        else if (upperIndices.contains(inverseIndexState(index))) {
                            from.add(index);
                            to.add(newIndex = inverseIndexState(generator.generate(getType(index))));
                            kroneckers.add(createKronecker(inverseIndexState(newIndex), index));
                        }
                    }
                    factors[i] = applyDirectMapping(factors[i],
                            new StateSensitiveMapping(from.toArray(), to.toArray()));
                }
                kroneckers.add(summand);
                summand = Tensors.multiply(kroneckers.toArray(new Tensor[kroneckers.size()]));
            }
            return new Split(factors, summand);
        }
        return new Split(new SimpleTensor[0], tensor);
    }

    private static SimpleTensor applyDirectMapping(SimpleTensor st, DirectIndexMapping mapping) {
        SimpleIndices newIndices = st.getIndices().applyIndexMapping(mapping);
        if (st instanceof TensorField)
            return Tensors.field(st.getName(), newIndices, ((TensorField) st).getArgIndices(), ((TensorField) st).getArguments());
        else
            return Tensors.simpleTensor(st.getName(), newIndices);
    }


    private static abstract class DirectIndexMapping implements IndexMapping {
        final int[] from, to;

        private DirectIndexMapping(int[] from, int[] to) {
            ArraysUtils.quickSort(from, to);
            this.from = from;
            this.to = to;
        }

    }

    private static final class StateSensitiveMapping extends DirectIndexMapping {
        private StateSensitiveMapping(int[] from, int[] to) {
            super(from, to);
        }

        @Override
        public int map(int from) {
            int index;
            if ((index = Arrays.binarySearch(this.from, from)) >= 0)
                return to[index];
            return from;
        }
    }

    private static final class StateInsensitiveMapping extends DirectIndexMapping {
        private StateInsensitiveMapping(int[] from, int[] to) {
            super(from, to);
        }

        @Override
        public int map(int from) {
            int index;
            if ((index = Arrays.binarySearch(this.from, getNameWithType(from))) >= 0)
                return setRawState(getRawStateInt(from), to[index]);
            return from;
        }
    }


}
