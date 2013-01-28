/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
interface IndexMappingProvider extends MappingsPort {

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
