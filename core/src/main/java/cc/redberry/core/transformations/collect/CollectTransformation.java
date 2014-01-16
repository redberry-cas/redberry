/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
import cc.redberry.core.groups.permutations.Permutations;
import cc.redberry.core.indexgenerator.IndexGeneratorImpl;
import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.*;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.expand.ExpandPort;
import cc.redberry.core.transformations.powerexpand.PowerExpandUnwrapTransformation;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;

import static cc.redberry.core.indices.IndicesUtils.*;
import static cc.redberry.core.tensor.Tensors.multiply;
import static cc.redberry.core.tensor.Tensors.sum;

/**
 * Collects together terms that involve the same powers of objects matching specified simple tensors or tensor fields.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1.5
 */
public class CollectTransformation implements Transformation {
    private final TIntHashSet patternsNames;
    private final Transformation powerExpand;
    private final Transformation[] transformations;

    /**
     * Creates Collect transformation that collects together terms that involve
     * the same powers of objects matching specified simple tensors or tensor fields and applies specified
     * transformations to the expression that forms the coefficient of each term obtained.
     *
     * @param patterns        specified simple tensors or tensor fields
     * @param transformations transformations to be applied to the expression that forms the coefficient
     *                        of each term obtained
     */
    public CollectTransformation(SimpleTensor[] patterns, Transformation[] transformations) {
        patternsNames = new TIntHashSet();
        powerExpand = new PowerExpandUnwrapTransformation(patterns);
        for (SimpleTensor t : patterns)
            patternsNames.add(t.getName());
        this.transformations = transformations;
    }

    /**
     * Creates Collect transformation that collects together terms that involve
     * the same powers of objects matching specified simple tensors or tensor fields.
     *
     * @param patterns specified simple tensors or tensor fields
     */
    public CollectTransformation(SimpleTensor... patterns) {
        this(patterns, new Transformation[0]);
    }

    @Override
    public Tensor transform(Tensor t) {
        if (t instanceof Expression)
            return Transformation.Util.applyToEachChild(t, this);
        else
            return transform1(t);
    }

    private Tensor transform1(Tensor t) {
        SumBuilder notMatched = new SumBuilder();
        TIntObjectHashMap<ArrayList<Split>> map = new TIntObjectHashMap<>();
        OutputPortUnsafe<Tensor> port = ExpandPort.createPort(t);
        Tensor current;
        Split toAdd;
        ArrayList<Split> nodes;
        out:
        while ((current = port.take()) != null) {
            toAdd = split(current);
            if (toAdd.factors.length == 0) {
                notMatched.put(current);
                continue;
            }

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
                    Tensor[] toAddFactors = Permutations.permute(toAdd.factors, match);
                    Mapping mapping =
                            IndexMappings.createBijectiveProductPort(toAddFactors, base.factors).take();

//                    mapping =  mapping.inverseStates();
//                    for (Map.Entry<Integer, IndexMappingBufferRecord> entry : mapping.getMap().entrySet())
//                        entry.getValue().invertStates();


                    base.summands.add(ApplyIndexMapping.applyIndexMappingAutomatically(toAdd.summands.get(0), mapping, base.forbidden));
                    continue out;
                }
            }

            nodes.add(toAdd);
        }


        for (ArrayList<Split> splits : map.valueCollection())
            for (Split split : splits)
                notMatched.put(split.toTensor(transformations));


        return notMatched.build();
    }

    private boolean match(Tensor t) {
        if (t instanceof SimpleTensor)
            return patternsNames.contains(t.hashCode());
        if (TensorUtils.isPositiveIntegerPower(t))
            return patternsNames.contains(t.get(0).hashCode());
        return false;
    }

    private Split split(Tensor tensor) {
        Tensor[] factors;
        Tensor summand;

        if (tensor instanceof SimpleTensor || TensorUtils.isPositiveIntegerPowerOfSimpleTensor(tensor))
            if (match(tensor)) {
                factors = new Tensor[1];
                factors[0] = tensor;
                summand = Complex.ONE;
            } else
                return new Split(new Tensor[0], tensor);
        else if (tensor instanceof Product || TensorUtils.isPositiveIntegerPowerOfProduct(tensor)) {
            //early check
            tensor = powerExpand.transform(tensor);

            boolean containsMatch = false;
            for (Tensor t : (tensor instanceof Product ? tensor : tensor.get(0))) {
                if (match(t)) {
                    containsMatch = true;
                    break;
                }
            }
            if (!containsMatch) return new Split(new Tensor[0], tensor);

            assert tensor instanceof Product;

            ArrayList<Tensor> factorsList = new ArrayList<>();
            summand = tensor;
            for (Tensor t : tensor) {
                if (match(t)) {
                    factorsList.add(t);
                    assert summand != Complex.ONE;
                    if (summand instanceof Product)
                        summand = ((Product) summand).remove(t);
                    else summand = Complex.ONE;
                }
            }
            factors = factorsList.toArray(new Tensor[factorsList.size()]);
        } else
            return new Split(new Tensor[0], tensor);


        TIntHashSet freeIndices = new TIntHashSet(IndicesUtils.getIndicesNames(tensor.getIndices().getFree()));

        //now we need to uncontract dummies and free

        Indices factorIndices = new IndicesBuilder().append(factors).getIndices();
        TIntHashSet dummies = new TIntHashSet(IndicesUtils.getIntersections(
                factorIndices.getUpper().copy(), factorIndices.getLower().copy()));
        SimpleIndices currentFactorIndices;
        IntArrayList from = new IntArrayList(), to = new IntArrayList();
        ArrayList<Tensor> kroneckers = new ArrayList<>();
        int j, index, newIndex;
        IndexGeneratorImpl generator = new IndexGeneratorImpl(TensorUtils.getAllIndicesNamesT(tensor).toArray());
        for (int i = 0; i < factors.length; ++i) {
            from.clear();
            to.clear();
            currentFactorIndices = IndicesFactory.createSimple(null, factors[i].getIndices());

            for (j = currentFactorIndices.size() - 1; j >= 0; --j) {
                index = currentFactorIndices.get(j);
                if (freeIndices.contains(getNameWithType(index))) {
                    newIndex = setRawState(getRawStateInt(index), generator.generate(getType(index)));
                    from.add(index);
                    to.add(newIndex);
                    kroneckers.add(Tensors.createKronecker(index, inverseIndexState(newIndex)));
                } else if (IndicesUtils.getState(index) && dummies.contains(getNameWithType(index))) {
                    newIndex = setRawState(getRawStateInt(index), generator.generate(getType(index)));
                    from.add(index);
                    to.add(newIndex);
                    kroneckers.add(Tensors.createKronecker(index, inverseIndexState(newIndex)));
                }
            }

            factors[i] = applyDirectMapping(factors[i],
                    new StateSensitiveMapping(from.toArray(), to.toArray()));
        }

        //temp check
//            factorIndices = new IndicesBuilder().append(factors).getIndices();
//            assert factorIndices.size() == factorIndices.getFree().size();

        kroneckers.add(summand);
        summand = Tensors.multiply(kroneckers.toArray(new Tensor[kroneckers.size()]));
        summand = EliminateMetricsTransformation.eliminate(summand);

        return new Split(factors, summand);
    }

    private static final class Split {
        final Tensor[] factors;
        final ArrayList<Tensor> summands = new ArrayList<>();
        final int hashCode;//real hash code (with fields args)
        final int[] forbidden;

        private Split(Tensor[] factors, Tensor summand) {
            this.factors = factors;
            this.summands.add(summand);
            Arrays.sort(factors);
            this.hashCode = Arrays.hashCode(factors);
            this.forbidden = IndicesUtils.getIndicesNames(new IndicesBuilder().append(factors).getIndices());
        }


        @Override
        public int hashCode() {
            return hashCode;
        }

        Tensor toTensor(Transformation[] transformations) {
            Tensor sum = Transformation.Util.applySequentially(
                    Tensors.sum(summands.toArray(new Tensor[summands.size()])),
                    transformations);
            Tensor[] ms = new Tensor[factors.length + 1];
            ms[ms.length - 1] = sum;
            System.arraycopy(factors, 0, ms, 0, factors.length);
            return Tensors.multiply(ms);
        }

        @Override
        public String toString() {
            return multiply(factors) + " : " + sum(summands.toArray(new Tensor[summands.size()]));
        }
    }

    static int[] matchFactors(final Tensor[] a, final Tensor[] b) {
        if (a.length != b.length) return null;
        int begin = 0, j, n, length = a.length;

        int[] permutation = new int[length];
        Arrays.fill(permutation, -1);

        for (int i = 1; i <= length; ++i) {
            if (i == length || a[i].hashCode() != b[i - 1].hashCode()) {
                if (i - 1 != begin) {
                    OUT:
                    for (n = begin; n < i; ++n) {
                        for (j = begin; j < i; ++j)
                            if (permutation[j] == -1 && matchSimpleTensors(a[n], b[j])) {
                                permutation[j] = n;
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
        return Permutations.inverse(permutation);
    }

    private static boolean matchSimpleTensors(Tensor a, Tensor b) {
        if (a.getClass() != b.getClass()) return false;
        if (a.hashCode() != b.hashCode()) return false;
        if (TensorUtils.isPositiveIntegerPowerOfSimpleTensor(a))
            return TensorUtils.isPositiveIntegerPowerOfSimpleTensor(b)
                    && a.get(1).equals(b.get(1))
                    && matchSimpleTensors(a.get(0), b.get(0));
        if (a instanceof TensorField)
            for (int i = a.size() - 1; i >= 0; --i)
                if (!IndexMappings.positiveMappingExists(a.get(i), b.get(i))) return false;
        return true;
    }

    private static Tensor applyDirectMapping(Tensor t, DirectIndexMapping mapping) {
        if (t instanceof SimpleTensor) {
            SimpleTensor st = (SimpleTensor) t;
            SimpleIndices newIndices = st.getIndices().applyIndexMapping(mapping);
            if (t instanceof TensorField)
                return Tensors.field(st.getName(), newIndices, ((TensorField) st).getArgIndices(), ((TensorField) st).getArguments());
            else
                return Tensors.simpleTensor(st.getName(), newIndices);
        } else {
            assert t.getIndices().size() == 0;
            return t;
        }
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
}
