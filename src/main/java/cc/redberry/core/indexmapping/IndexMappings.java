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
 * the Free Software Foundation, either version 2 of the License, or
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

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndexMappings {

    private IndexMappings() {
    }

    public static MappingsPort simpleTensorsPort(SimpleTensor from, SimpleTensor to) {
        final IndexMappingProvider provider = ProviderSimpleTensor.FACTORY_SIMPLETENSOR.create(IndexMappingProvider.Util.singleton(new IndexMappingBufferImpl()), from, to);
        provider.tick();
        return new MappingsPortRemovingContracted(provider);
    }

    public static MappingsPort createBijectiveProductPort(Tensor[] from, Tensor[] to) {
        if (from.length != to.length)
            throw new IllegalArgumentException("From length != to length.");
        if (from.length == 0)
            return IndexMappingProvider.Util.singleton(new IndexMappingBufferImpl());
        if (from.length == 1)
            return createPort(from[0], to[0]);
        return new MappingsPortRemovingContracted(new SimpleProductMappingsPort(IndexMappingProvider.Util.singleton(new IndexMappingBufferImpl()), from, to));
    }

    public static MappingsPort createPort(Tensor from, Tensor to) {
        return createPort(new IndexMappingBufferImpl(), from, to);
    }

    public static MappingsPort createPort(final IndexMappingBuffer buffer,
                                          final Tensor from, final Tensor to) {
        final IndexMappingProvider provider = createPort(IndexMappingProvider.Util.singleton(buffer), from, to);
        provider.tick();
        return new MappingsPortRemovingContracted(provider);
    }

    public static IndexMappingBuffer getFirst(Tensor from, Tensor to) {
        return createPort(from, to).take();
    }

    public static boolean mappingExists(Tensor from, Tensor to) {
        return getFirst(from, to) != null;
    }

    public static boolean testMapping(Tensor from, Tensor to, IndexMappingBuffer buffer) {
        return createPort(IndexMappingBufferTester.create(buffer), from, to).take() != null;
    }

    private static Tensor extractNonComplexFactor(Tensor t) {
        Product p = (Product) t;
        if (p.getFactor().isMinusOne())
            return p.get(1);
        else
            return null;
    }

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
        map.put(TensorField.class, ProviderSimpleTensor.FACTORY_TENSORFIELD);
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
    }

    private static Set<IndexMappingBuffer> getAllMappings(MappingsPort opu) {
        Set<IndexMappingBuffer> res = new HashSet<>();
        IndexMappingBuffer c;
        while ((c = opu.take()) != null)
            res.add(c);
        return res;
    }

    public static Set<IndexMappingBuffer> getAllMappings(Tensor from, Tensor to) {
        return getAllMappings(IndexMappings.createPort(from, to));
    }
}
