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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.*;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.TensorUtils;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Substitutions {

    private Substitutions() {
    }

    public static Transformation getTransformation(Tensor from, Tensor to) {
        if (TensorUtils.equals(from, to))
            return DummyTransformation.INSTANCE;
        SubstitutionProvider provider = map.get(from.getClass());
        if (provider == null)
            throw new UnsupportedOperationException("Not supported");
        return provider.createSubstitution(from, to);
    }
    private static final Map<Class, SubstitutionProvider> map = new HashMap<>();

    static {
        map.put(SimpleTensor.class, SimpleSubstitution.SIMPLE_SUBSTITUTION_PROVIDER);
        map.put(TensorField.class, TensorFieldSubstitution.TENSOR_FIELD_PROVIDER);
        map.put(Sum.class, SumSubstitution.SUM_SUBSTITUTION_PROVIDER);
        map.put(Product.class, ProductSubstitution.PRODUCT_SUBSTITUTION_PROVIDER);
        map.put(Sin.class, SimpleSubstitution.SIMPLE_SUBSTITUTION_PROVIDER);
        map.put(Cos.class, SimpleSubstitution.SIMPLE_SUBSTITUTION_PROVIDER);
        map.put(Tan.class, SimpleSubstitution.SIMPLE_SUBSTITUTION_PROVIDER);
        map.put(Cot.class, SimpleSubstitution.SIMPLE_SUBSTITUTION_PROVIDER);
        map.put(ArcSin.class, SimpleSubstitution.SIMPLE_SUBSTITUTION_PROVIDER);
        map.put(ArcCos.class, SimpleSubstitution.SIMPLE_SUBSTITUTION_PROVIDER);
        map.put(ArcTan.class, SimpleSubstitution.SIMPLE_SUBSTITUTION_PROVIDER);
        map.put(ArcCot.class, SimpleSubstitution.SIMPLE_SUBSTITUTION_PROVIDER);
        map.put(Exp.class, SimpleSubstitution.SIMPLE_SUBSTITUTION_PROVIDER);
        map.put(Power.class, SimpleSubstitution.SIMPLE_SUBSTITUTION_PROVIDER);
        map.put(Log.class, SimpleSubstitution.SIMPLE_SUBSTITUTION_PROVIDER);
    }

    private static final class DummyTransformation implements Transformation {

        final static Transformation INSTANCE = new DummyTransformation();

        private DummyTransformation() {
        }

        @Override
        public Tensor transform(Tensor t) {
            return t;
        }
    }
}
