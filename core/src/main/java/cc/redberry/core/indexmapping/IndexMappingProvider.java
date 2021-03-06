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

import cc.redberry.core.utils.OutputPort;

/**
 * Internal interface representing a index mapping provider (IMP).
 *
 * <p>IMP could be imagine as a processing element of a pipeline. Each such element takes a result (index mapping) of
 * the previous element and adds its own information to this result. One input index mapping could be "transformed" by
 * processing element (IMP) into zero, one, or several elements, which in turn, will be one-by-one processed by the next
 * IMP.</p>
 *
 * <p>It has two methods {@code tick()} and {@code take()}. The {@code tick()} method tells the current IMP to take one
 * index mapping from the previous IMP in the chin and load it into "register" inside. The {@code take()} method returns
 * the next resulting index mapping derived from mapping stored in the mentioned above register.</p>
 *
 * <p>Such pattern helps to avoid very long stacks in case of very complex expressions. The average stack depth is
 * proportional to the depth (number of nested tensors) of expression. In the naive implementation, otherwise, it
 * would have been proportional to the number of element in the expression.</p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
interface IndexMappingProvider extends OutputPort<IndexMappingBuffer> {

    boolean tick();

    public static class Util {

        public static final IndexMappingProvider EMPTY_PROVIDER = new IndexMappingProvider() {

            @Override
            public boolean tick() {
                return false;
            }

            @Override
            public IndexMappingBuffer take() {
                return null;
            }
        };

        public static IndexMappingProvider singleton(final IndexMappingBuffer buffer) {
            return new IndexMappingProvider() {

                IndexMappingBuffer buf = buffer;

                @Override
                public boolean tick() {
                    return false;
                }

                @Override
                public IndexMappingBuffer take() {
                    IndexMappingBuffer tmp = buf;
                    buf = null;
                    return tmp;
                }
            };
        }
    }
}
