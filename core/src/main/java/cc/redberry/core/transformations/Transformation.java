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
package cc.redberry.core.transformations;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorBuilder;

/**
 * Transformation of tensor.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public interface Transformation {
    /**
     * Singleton instance for identity transformation.
     */
    Transformation INDENTITY = new Transformation() {
        @Override
        public Tensor transform(Tensor t) {
            return t;
        }

        @Override
        public String toString() {
            return "Identity";
        }
    };

    final class Util {
        private Util() {
        }

        public static Tensor applySequentially(Tensor tensor, final Transformation... transformations) {
            for (Transformation tr : transformations)
                tensor = tr.transform(tensor);
            return tensor;
        }

        /**
         * Applies transformation to all first descendants (but not to the tensor itself)
         *
         * @param tensor
         * @param transformation
         * @return result
         */
        public static Tensor applyToEachChild(Tensor tensor, final Transformation transformation) {
            TensorBuilder builder = null;
            Tensor c, oc;
            for (int i = 0, size = tensor.size(); i < size; ++i) {
                c = transformation.transform(oc = tensor.get(i));
                if (builder != null || c != oc) {
                    if (builder == null) {
                        builder = tensor.getBuilder();
                        for (int j = 0; j < i; ++j)
                            builder.put(tensor.get(j));
                    }
                    builder.put(c);
                }
            }
            if (builder == null)
                return tensor;
            return builder.build();
        }

        /**
         * Applies transformation until the specified expression is unchanged under transformation.
         *
         * @param t              tensor
         * @param transformation transformation
         * @return result
         */
        public static Tensor applyUntilUnchanged(Tensor t, final Transformation transformation) {
            Tensor r;
            do {
                r = t;
                t = transformation.transform(r);
            } while (r != t);
            return r;
        }

    }

    /**
     * Transforms specified tensor and returns the result.
     *
     * @param t tensor
     * @return transformed tensor
     */
    Tensor transform(Tensor t);
}
