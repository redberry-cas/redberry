/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
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

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.context.CC;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.analysis.function.Pow;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndexMappings {

    private IndexMappings() {
    }

    public static OutputPortUnsafe<IndexMappingBuffer> createPortForSimpleTensor(SimpleTensor from, SimpleTensor to, boolean allowDiffStates) {
        final IndexMappingProvider provider = map.get(SimpleTensor.class).create(IndexMappingProvider.Util.singleton(new IndexMappingBufferImpl(allowDiffStates)), from, to, allowDiffStates);
        provider.tick();
        return new OutputPortUnsafe<IndexMappingBuffer>() {

            @Override
            public IndexMappingBuffer take() {
                IndexMappingBuffer buf = provider.take();
                if (buf != null)
                    buf.removeContracted();
                return buf;
            }
        };
    }

    public static MappingsPort createPort(Tensor from, Tensor to) {
        return createPort(from, to, CC.withMetric());
    }

    public static MappingsPort createPort(Tensor from, Tensor to, boolean allowDiffStates) {
        final IndexMappingProvider provider = createPort(IndexMappingProvider.Util.singleton(new IndexMappingBufferImpl(allowDiffStates)), from, to, allowDiffStates);
        provider.tick();
        return new MappingsPort() {

            @Override
            public IndexMappingBuffer take() {
                IndexMappingBuffer buf = provider.take();
                if (buf != null)
                    buf.removeContracted();
                return buf;
            }
        };
    }

    public static MappingsPort createPort(final IndexMappingBuffer buffer,
                                          final Tensor from, final Tensor to) {
        final IndexMappingProvider provider = createPort(IndexMappingProvider.Util.singleton(buffer), from, to, buffer.allowDiffStates());
        provider.tick();
        return new MappingsPort() {

            @Override
            public IndexMappingBuffer take() {
                IndexMappingBuffer buf = provider.take();
                if (buf != null)
                    buf.removeContracted();
                return buf;
            }
        };
    }

    public static IndexMappingBuffer getFirst(Tensor from, Tensor to, final boolean allowDiffStates) {
        return createPort(from, to, allowDiffStates).take();
    }

    public static boolean mappingExists(Tensor from, Tensor to, final boolean allowDiffStates) {
        return getFirst(from, to, allowDiffStates) != null;
    }

    public static boolean testMapping(Tensor from, Tensor to, boolean allowDiffStates, IndexMappingBuffer buffer) {
        IndexMappingBufferTester tester = IndexMappingBufferTester.create(buffer);
        OutputPortUnsafe<IndexMappingBuffer> provider = createPort(tester, from, to);
        return provider.take() != null;
    }

    private static Tensor extractNonComplexFactor(Tensor t) {
        //for (int i = 0; i < 2; ++i)
        //    if (t.get(i) instanceof Complex && ((Complex) t.get(i)).equals(Complex.MINUSE_ONE))
        //        return t.get(1 - i);
        Product p = (Product) t;
        if (p.getFactor().isMinusOne())
            return p.get(1);
        else
            return null;
    }

    static IndexMappingProvider createPort(IndexMappingProvider opu, Tensor from, Tensor to, final boolean allowDiffStates) {
        if (from.hashCode() != to.hashCode())
            return IndexMappingProvider.Util.EMPTY_PROVIDER;

        if (from.getClass() != to.getClass()) {

            Tensor nonComplex;
            //Processing case -2*(1/2)*g_mn -> g_mn
            if (from instanceof Product && !(to instanceof Product)) {
                if (from.size() != 2)
                    return IndexMappingProvider.Util.EMPTY_PROVIDER;

                if ((nonComplex = extractNonComplexFactor(from)) != null)
                    return new MinusIndexMappingProviderWrapper(createPort(opu, nonComplex, to, allowDiffStates));
                return IndexMappingProvider.Util.EMPTY_PROVIDER;
            }

            //Processing case g_mn -> -2*(1/2)*g_mn
            if (to instanceof Product && !(from instanceof Product)) {
                if (to.size() != 2)
                    return IndexMappingProvider.Util.EMPTY_PROVIDER;
                if ((nonComplex = extractNonComplexFactor(to)) != null)
                    return new MinusIndexMappingProviderWrapper(createPort(opu, from, nonComplex, allowDiffStates));
                return IndexMappingProvider.Util.EMPTY_PROVIDER;
            }

            return IndexMappingProvider.Util.EMPTY_PROVIDER;
        }

        IndexMappingProviderFactory factory = map.get(from.getClass());
//                if (factory == null)
//                    if (from instanceof AbstractScalarFunction)
//                        factory = ProviderScalarFunctionsFactory.INSTANCE;
//                    else
//                        throw new RuntimeException("Unsupported tensor type: " + from.getClass());

        return factory.create(opu, from, to, allowDiffStates);
    }
    private static final Map<Class, IndexMappingProviderFactory> map;

    static {
        map = new HashMap<>();
        map.put(SimpleTensor.class, ProviderSimpleTensor.FACTORY_SIMPLETENSOR);
        map.put(TensorField.class, ProviderSimpleTensor.FACTORY_TENSORFIELD);
        map.put(Product.class, ProviderProduct.FACTORY);
        map.put(Sum.class, ProviderSum.FACTORY);
        map.put(Complex.class, ProviderComplex.FACTORY);
        map.put(Pow.class, ProviderPowFactory.INSTANCE);
    }
}
