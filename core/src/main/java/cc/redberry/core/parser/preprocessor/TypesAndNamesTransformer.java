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
package cc.redberry.core.parser.preprocessor;

import cc.redberry.core.context.NameAndStructureOfIndices;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.utils.ArraysUtils;

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public interface TypesAndNamesTransformer {
    IndexType newType(IndexType oldType, NameAndStructureOfIndices oldDescriptor);

    int newIndex(int oldIndex, NameAndStructureOfIndices oldDescriptor);

    String newName(NameAndStructureOfIndices oldDescriptor);

    class Utils {

        public static TypesAndNamesTransformer setIndices(final int[] from, final int[] to) {
            final int[] from0 = from.clone(), to0 = to.clone();
            ArraysUtils.quickSort(from0, to0);
            return new TypesAndNamesTransformer() {

                @Override
                public IndexType newType(IndexType oldType, NameAndStructureOfIndices oldDescriptor) {
                    return oldType;
                }

                @Override
                public int newIndex(int oldIndex, NameAndStructureOfIndices oldDescriptor) {
                    int i = Arrays.binarySearch(from0, oldIndex);
                    if (i >= 0)
                        return to0[i];
                    return oldIndex;
                }

                @Override
                public String newName(NameAndStructureOfIndices oldDescriptor) {
                    return oldDescriptor.getName();
                }
            };
        }

        public static TypesAndNamesTransformer changeType(final IndexType oldType, final IndexType newType) {
            return new TypesAndNamesTransformer() {
                @Override
                public int newIndex(int oldIndex, NameAndStructureOfIndices oldDescriptor) {
                    return oldIndex;
                }

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
