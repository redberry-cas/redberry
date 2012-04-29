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

package cc.redberry.concurrent;

public interface Filter<T> {
    boolean accept(T object);

    public static class Utill {
        private Utill() {
        }

        public static Filter getNullFilter() {
            return new Filter() {
                @Override
                public boolean accept(Object object) {
                    return true;
                }
            };
        }

        public static <T> Filter<T> or(final Filter<T>... filters) {
            return new Filter<T>() {
                @Override
                public boolean accept(T object) {
                    for (Filter<T> f : filters)
                        if (f.accept(object))
                            return true;
                    return false;
                }
            };
        }

        public static <T> Filter<T> and(final Filter<T>... filters) {
            return new Filter<T>() {
                @Override
                public boolean accept(T object) {
                    for (Filter<T> f : filters)
                        if (!f.accept(object))
                            return false;
                    return true;
                }
            };
        }

        public static <T> Filter<T> not(final Filter<T> filter) {
            return new Filter<T>() {
                @Override
                public boolean accept(T object) {
                    return !filter.accept(object);
                }
            };
        }

        public static <T> Filter<T> count(final int min, final int max, final Filter<T>... filters) {
            return new Filter<T>() {
                @Override
                public boolean accept(T object) {
                    int count = 0;
                    for (Filter<T> f : filters)
                        if (f.accept(object))
                            if (++count > max)
                                return false;
                    return count >= min;
                }
            };
        }
    }
}
