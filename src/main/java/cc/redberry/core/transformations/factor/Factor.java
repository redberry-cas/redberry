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
package cc.redberry.core.transformations.factor;

import cc.redberry.core.number.Complex;
import cc.redberry.core.number.NumberUtils;
import cc.redberry.core.tensor.Power;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.functions.ScalarFunction;
import cc.redberry.core.tensor.iterator.TensorFirstIterator;
import cc.redberry.core.tensor.iterator.TensorLastIterator;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.fractions.Together;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class Factor implements Transformation {
    public static final Factor FACTOR = new Factor();

    private Factor() {
    }

    @Override
    public Tensor transform(Tensor t) {
        return factor(t);
    }

    public static Tensor factor(Tensor tensor) {
        TensorFirstIterator iterator = new TensorFirstIterator(tensor);
        TensorLastIterator iterator1;
        Tensor c, t;
        Complex e;
        out:
        while ((c = iterator.next()) != null) {
            if (!(c instanceof Sum))
                continue;
            iterator1 = new TensorLastIterator(tensor);
            boolean needTogether = false;
            while ((t = iterator1.next()) != null) {
                if (t.getIndices().size() != 0 || t instanceof ScalarFunction)
                    continue out;
                if (t instanceof Power) {
                    if (!(t.get(1) instanceof Complex))
                        continue out;
                    e = (Complex) t.get(1);
                    if (!e.isReal() || e.isNumeric())
                        continue out;
                    if (e.getReal().signum() < 0)
                        needTogether = true;
                }
            }
            if (needTogether) {
                c = Together.together(c);
                if (c instanceof Product) {
                    for (int i = c.size() - 1; i >= 0; --i) {
                        if (c.get(i) instanceof Sum)
                            c = c.set(i, JasFactor.factor(c.get(i)));
                    }
                    iterator.set(c);
                }
            } else {
                iterator.set(JasFactor.factor(c));
            }

        }
        return iterator.result();
    }


}
