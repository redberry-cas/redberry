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

package cc.redberry.core.utils;

import cc.redberry.core.tensor.Tensor;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public interface Indicator<E> {

    boolean is(E object);
    public static final Indicator TRUE_INDICATOR = new Indicator() {

        @Override
        public boolean is(Object object) {
            return true;
        }
    };
    public static final Indicator FALSE_INDICATOR = new Indicator() {

        @Override
        public boolean is(Object object) {
            return false;
        }
    };

    public static class Utils {

        public static <T> Indicator<T> and(final Indicator<T>... indicators) {
            return new Indicator<T>() {

                @Override
                public boolean is(T object) {
                    for (Indicator<T> indicator : indicators)
                        if (!indicator.is(object))
                            return false;
                    return true;
                }
            };
        }

        public static <T> Indicator<T> or(final Indicator<T>... indicators) {
            return new Indicator<T>() {

                @Override
                public boolean is(T object) {
                    for (Indicator<T> indicator : indicators)
                        if (indicator.is(object))
                            return true;
                    return false;
                }
            };
        }

        public static <T> Indicator<T> not(final Indicator<T> indicator) {
            return new Indicator<T>() {

                @Override
                public boolean is(T object) {
                    return !indicator.is(object);
                }
            };
        }

        public static Indicator<Tensor> classIndicator(final Class<? extends Tensor> clazz) {
            return new Indicator<Tensor>() {

                @Override
                public boolean is(Tensor object) {
                    return clazz == object.getClass();
                }
            };
        }
    ;
}
}
