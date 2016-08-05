/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
package cc.redberry.core.indexmapping;

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.*;
import cc.redberry.core.utils.OutputPort;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Central public facade of this package. Provides static methods for calculation of mappings between indices.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class IndexMappings {

    private IndexMappings() {
    }

    /**
     * Creates output port of mappings from tensor <i>{@code from}</i> onto tensor <i>{@code to}</i>.
     *
     * @param from from tensor
     * @param to   to tensor
     * @return output port of mappings
     */
    public static MappingsPort createPort(Tensor from, Tensor to) {
        return new MappingsPort(createPortOfBuffers(new IndexMappingBufferImpl(), from, to));
    }

    /**
     * Creates output port of mappings of two simple tensors and does not take into account the arguments of fields.
     *
     * @param from from tensor
     * @param to   to tensor
     * @return port of mappings of indices
     */
    public static MappingsPort simpleTensorsPort(SimpleTensor from, SimpleTensor to) {
        final IndexMappingProvider provider = ProviderSimpleTensor.FACTORY_SIMPLETENSOR.create(IndexMappingProvider.Util.singleton(new IndexMappingBufferImpl()), from, to);
        provider.tick();
        return new MappingsPort(new MappingsPortRemovingContracted(provider));
    }

    /**
     * Creates output port of mappings of two products of tensors represented as arrays of multipliers, where
     * each multiplier of {@code from} will be mapped on the multiplier of {@code to} at the same
     * position. Such ordering can be obtained via {@link cc.redberry.core.transformations.substitutions.ProductsBijectionsPort}.
     * In contrast to {@link #createPortOfBuffers(cc.redberry.core.tensor.Tensor, cc.redberry.core.tensor.Tensor)}, this method
     * will fully handles mappings of free indices on contracted ones (like e.g. _i^j -> _k^k).
     *
     * @param from from tensor
     * @param to   to tensor
     * @return port of mappings of indices
     */
    public static MappingsPort createBijectiveProductPort(Tensor[] from, Tensor[] to) {
        if (from.length != to.length)
            throw new IllegalArgumentException("From length != to length.");
        if (from.length == 0)
            return new MappingsPort(IndexMappingProvider.Util.singleton(new IndexMappingBufferImpl()));
        if (from.length == 1)
            return new MappingsPort(createPortOfBuffers(from[0], to[0]));
        return new MappingsPort(
                new MappingsPortRemovingContracted(
                        new SimpleProductMappingsPort(IndexMappingProvider.Util.singleton(new IndexMappingBufferImpl()), from, to)));
    }


    /**
     * Returns the first mapping of tensor {@code from} on tensor {@code to}.
     *
     * @param from from tensor
     * @param to   to tensor
     * @return mapping of indices of tensor {@code from} on tensor {@code to}
     */
    public static Mapping getFirst(Tensor from, Tensor to) {
        IndexMappingBuffer buffer = createPortOfBuffers(from, to).take();
        if (buffer == null) return null;
        return new Mapping(buffer);
    }

    /**
     * Tests whether specified mapping is a mapping from <i>{@code from}</i> tensor onto <i>{@code to}</i> tensor.
     *
     * @param mapping mapping
     * @param from    tensor <i>{@code from}</i>
     * @param to      tensor <i>{@code to}</i>
     * @return {@code true} if specified mapping is a mapping from <i>{@code from}</i> tensor onto
     * <i>{@code to}</i> tensor and {@code false} in other case.
     */
    public static boolean testMapping(Mapping mapping, Tensor from, Tensor to) {
        return IndexMappingBufferTester.test(new IndexMappingBufferTester(mapping), from, to);
    }


    /**
     * Returns {@code true} if there is mapping from {@code a} on tensor {@code b} or vice versa.
     *
     * @param a tensor
     * @param b tensor
     * @return {@code true} if there is mapping from {@code a} on tensor {@code b} or vice versa
     */
    public static boolean anyMappingExists(Tensor a, Tensor b) {
        return mappingExists(a, b) || mappingExists(b, a);
    }

    /**
     * Returns {@code true} if there is mapping of tensor {@code from} on tensor {@code to}.
     *
     * @param from from tensor
     * @param to   to tensor
     * @return {@code true} if there is mapping of tensor {@code from} on tensor {@code to}
     */
    public static boolean mappingExists(Tensor from, Tensor to) {
        return getFirstBuffer(from, to) != null;
    }

    /**
     * Returns {@code true} if there is positive mapping of tensor {@code from} on tensor {@code to}.
     *
     * @param from from tensor
     * @param to   to tensor
     * @return {@code true} if there is positivemapping of tensor {@code from} on tensor {@code to}
     */
    public static boolean positiveMappingExists(Tensor from, Tensor to) {
        IndexMappingBuffer buffer;
        OutputPort<IndexMappingBuffer> port = createPortOfBuffers(from, to);
        while ((buffer = port.take()) != null)
            if (!buffer.getSign())
                return true;
        return false;
    }


    /**
     * Returns {@code true} if tensor u mathematically (not programming) equals to tensor v.
     *
     * @param u tensor
     * @param v tensor
     * @return {@code true} if specified tensors are mathematically (not programming) equal
     */
    public static boolean equals(Tensor u, Tensor v) {
        if (u == v)
            return true;
        Indices freeIndices = u.getIndices().getFree();
        if (!freeIndices.equalsRegardlessOrder(v.getIndices().getFree()))
            return false;
        if (HashingStrategy.iHash(u) != HashingStrategy.iHash(v))
            return false;
        if (u instanceof Product && v instanceof Product)
            if (!((Product) u).getContent().iCompatibleWithGraph(((Product) v).getContent()))
                return false;
        int[] free = freeIndices.getAllIndices().copy();
        IndexMappingBuffer tester = new IndexMappingBufferTester(free, false);
        OutputPort<IndexMappingBuffer> mp = IndexMappings.createPortOfBuffers(tester, u, v);
        IndexMappingBuffer buffer;

        while ((buffer = mp.take()) != null)
            if (!buffer.getSign())
                return true;

        return false;
    }

    /**
     * Returns {@code true} if tensor u mathematically (not programming) equals to tensor v,
     * {@code false} if they they differ only in the sign and {@code null} otherwise.
     *
     * @param u tensor
     * @param v tensor
     * @return {@code true} {@code false} if tensor u mathematically (not programming) equals to tensor v,
     * {@code true} if they they differ only in the sign and {@code null} otherwise
     */
    public static Boolean compare1(Tensor u, Tensor v) {
        Indices freeIndices = u.getIndices().getFree();
        if (!freeIndices.equalsRegardlessOrder(v.getIndices().getFree()))
            return null;
        if (HashingStrategy.iHash(u) != HashingStrategy.iHash(v))
            return null;
        if (u instanceof Product && v instanceof Product)
            if (!((Product) u).getContent().iCompatibleWithGraph(((Product) v).getContent()))
                return null;
        int[] free = freeIndices.getAllIndices().copy();
        IndexMappingBuffer tester = new IndexMappingBufferTester(free, false);
        IndexMappingBuffer buffer = IndexMappings.createPortOfBuffers(tester, u, v).take();
        if (buffer == null)
            return null;
        return buffer.getSign();
    }

    public static Boolean compare1_withoutCheck(Tensor u, Tensor v) {
        Indices freeIndices = u.getIndices().getFree();
        int[] free = freeIndices.getAllIndices().copy();
        IndexMappingBuffer tester = new IndexMappingBufferTester(free, false);
        IndexMappingBuffer buffer = IndexMappings.createPortOfBuffers(tester, u, v).take();
        if (buffer == null)
            return null;
        return buffer.getSign();
    }

    /**
     * Returns {@code true} if specified tensor is zero in consequence of its symmetries: is both symmetric and
     * asymmetric with respect to some permutation at the same time.
     *
     * @param t tensor
     * @return {@code true} if specified tensor is zero in consequence of its symmetries
     */
    public static boolean isZeroDueToSymmetry(Tensor t) {
        int[] indices = IndicesUtils.getIndicesNames(t.getIndices().getFree());
        IndexMappingBufferTester bufferTester = new IndexMappingBufferTester(indices, false);
        OutputPort<IndexMappingBuffer> mp = IndexMappings.createPortOfBuffers(bufferTester, t, t);
        IndexMappingBuffer buffer;
        while ((buffer = mp.take()) != null)
            if (buffer.getSign())
                return true;
        return false;
    }

    /**
     * Returns a set of all possible mappings of tensor {@code from} on tensor {@code to}.
     *
     * @param from from tensor
     * @param to   to tensor
     * @return a set of all possible mappings of tensor {@code from} on tensor {@code to}
     */
    public static Set<Mapping> getAllMappings(Tensor from, Tensor to) {
        return getAllMappings(IndexMappings.createPort(from, to));
    }

    private static Set<Mapping> getAllMappings(OutputPort<Mapping> opu) {
        Set<Mapping> res = new HashSet<>();
        Mapping c;
        while ((c = opu.take()) != null)
            res.add(c);
        return res;
    }

    /* *
     * *
     * * Non public methods
     * *
     * */

    /**
     * Creates output port of mappings of tensor {@code from} on tensor {@code to}.
     *
     * @param from from tensor
     * @param to   to tensor
     * @return output port of mappings
     */
    static OutputPort<IndexMappingBuffer> createPortOfBuffers(Tensor from, Tensor to) {
        return createPortOfBuffers(new IndexMappingBufferImpl(), from, to);
    }

    /**
     * Creates output port of mappings of tensor {@code from} on tensor {@code to} with specified
     * mappings rules defined in specified {@link IndexMappingBuffer}.
     *
     * @param buffer initial mapping rules
     * @param from   from tensor
     * @param to     to tensor
     * @return output port of mapping
     */
    static OutputPort<IndexMappingBuffer> createPortOfBuffers(final IndexMappingBuffer buffer,
                                                              final Tensor from, final Tensor to) {
        final IndexMappingProvider provider = createPort(IndexMappingProvider.Util.singleton(buffer), from, to);
        provider.tick();
        return new MappingsPortRemovingContracted(provider);
    }

    static IndexMappingBuffer getFirstBuffer(Tensor from, Tensor to) {
        return createPortOfBuffers(from, to).take();
    }

    private static Tensor extractNonComplexFactor(Tensor t) {
        Product p = (Product) t;
        if (p.getFactor().isMinusOne())
            return p.get(1);
        else
            return null;
    }

    /* Main routine */

    static IndexMappingProvider createPort(IndexMappingProvider opu, Tensor from, Tensor to) {
        if (from.hashCode() != to.hashCode())
            return IndexMappingProvider.Util.EMPTY_PROVIDER;

        if (from.getClass() != to.getClass()) {

            Tensor nonComplex;
            //Processing case -2*(1/2)*g_mn -> g_mn
            if (from instanceof Product && !(to instanceof Product)) {
                if (from.size() != 2)
                    return IndexMappingProvider.Util.EMPTY_PROVIDER;

                if ((nonComplex = extractNonComplexFactor(from)) != null)
                    return new MinusIndexMappingProviderWrapper(createPort(opu, nonComplex, to));
                return IndexMappingProvider.Util.EMPTY_PROVIDER;
            }

            //Processing case g_mn -> -2*(1/2)*g_mn
            if (to instanceof Product && !(from instanceof Product)) {
                if (to.size() != 2)
                    return IndexMappingProvider.Util.EMPTY_PROVIDER;
                if ((nonComplex = extractNonComplexFactor(to)) != null)
                    return new MinusIndexMappingProviderWrapper(createPort(opu, from, nonComplex));
                return IndexMappingProvider.Util.EMPTY_PROVIDER;
            }

            return IndexMappingProvider.Util.EMPTY_PROVIDER;
        }

        IndexMappingProviderFactory factory = map.get(from.getClass());
        if (factory == null)
            throw new RuntimeException("Unsupported tensor type: " + from.getClass());

        return factory.create(opu, from, to);
    }

    private static final Map<Class, IndexMappingProviderFactory> map;

    static {
        map = new HashMap<>();
        map.put(SimpleTensor.class, ProviderSimpleTensor.FACTORY_SIMPLETENSOR);
        map.put(TensorField.class, ProviderTensorField.FACTORY_TENSORFIELD);
        map.put(Product.class, ProviderProduct.FACTORY);
        map.put(Sum.class, ProviderSum.FACTORY);
        map.put(Expression.class, ProviderSum.FACTORY);
        map.put(Complex.class, ProviderComplex.FACTORY);
        map.put(Power.class, ProviderPower.INSTANCE);

        map.put(Sin.class, ProviderFunctions.ODD_FACTORY);
        map.put(ArcSin.class, ProviderFunctions.ODD_FACTORY);
        map.put(Tan.class, ProviderFunctions.ODD_FACTORY);
        map.put(ArcTan.class, ProviderFunctions.ODD_FACTORY);

        map.put(Cos.class, ProviderFunctions.EVEN_FACTORY);
        map.put(ArcCos.class, ProviderFunctions.EVEN_FACTORY);
        map.put(Cot.class, ProviderFunctions.EVEN_FACTORY);
        map.put(ArcCot.class, ProviderFunctions.EVEN_FACTORY);

        map.put(Log.class, ProviderFunctions.FACTORY);
    }
}
