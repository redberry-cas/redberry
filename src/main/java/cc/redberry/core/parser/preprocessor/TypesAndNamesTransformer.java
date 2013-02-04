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
package cc.redberry.core.parser.preprocessor;

import cc.redberry.core.context.NameAndStructureOfIndices;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.StructureOfIndices;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public interface TypesAndNamesTransformer {
    IndexType newType(IndexType oldType, NameAndStructureOfIndices oldDescriptor);

    String newName(NameAndStructureOfIndices oldDescriptor);


    public static class Utils {
        public static TypesAndNamesTransformer changeType(final IndexType oldType, final IndexType newType) {
            return new TypesAndNamesTransformer() {
                @Override
                public IndexType newType(IndexType old, NameAndStructureOfIndices oldDescriptor) {
                    return old == oldType ? newType : old;
                }

                @Override
                public String newName(NameAndStructureOfIndices oldDescriptor) {
                    return oldDescriptor.getName();
                }
            };
        }
    }
}
