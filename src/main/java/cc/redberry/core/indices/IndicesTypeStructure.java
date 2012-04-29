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
package cc.redberry.core.indices;

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndicesTypeStructure extends IndicesStructure {
    private TypeData[] typeDatas = null;

    public IndicesTypeStructure(byte[] types, int[] counts) {
        super(createData(types, counts));
    }

    public IndicesTypeStructure(Indices indices) {
        super(extractTypes(indices));
    }

    private static byte[] extractTypes(Indices indices) {
        byte[] typeData = new byte[indices.size()];
        for (int i = 0; i < indices.size(); i++)
            typeData[i] = IndicesUtils.getType(indices.get(i));
        Arrays.sort(typeData); //Redundant ?
        return typeData;
    }

    private static byte[] createData(final byte[] types, final int[] counts) {
        int sum = 0;
        int i;
        for (i = 0; i < counts.length; ++i)
            sum += counts[i];
        byte[] data = new byte[sum];
        sum = 0;
        for (i = 0; i < counts.length; ++i)
            Arrays.fill(data, sum, sum += counts[i], types[i]);
        Arrays.sort(data);
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final IndicesTypeStructure other = (IndicesTypeStructure) obj;
        if (!Arrays.equals(this.data, other.data))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 365 + Arrays.hashCode(this.data);
    }

    public TypeData getTypeDatas(byte type) {
        if (typeDatas == null)
            calculateTypesData();
        return typeDatas[type];
    }

    private void calculateTypesData() {
        int last = -1;
        typeDatas = new TypeData[IndexType.TYPES_COUNT];
        for (int i = 0; i < data.length; ++i)
            if (i == data.length - 1 || data[i] != data[i + 1]) {
                typeDatas[data[i]] = new TypeData(last + 1, i - last);
                last = i;
            }
    }

    public static class TypeData {
        public final int from;
        public final int length;

        public TypeData(int from, int length) {
            this.from = from;
            this.length = length;
        }
    }
}
