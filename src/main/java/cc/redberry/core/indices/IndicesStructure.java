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
 * This class describes index structure of some indices.
 * 
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
//TODO comment
public class IndicesStructure {
    protected final byte[] data;

    public IndicesStructure(Indices indices) {
        data = new byte[indices.size()];
        for (int i = 0; i < indices.size(); i++)
            data[i] = IndicesUtils.getTypeWithState(indices.get(i));
        Arrays.sort(data);
    }

    protected IndicesStructure(byte[] data) {
        this.data = data;
    }

    public int size() {
        return data.length;
    }

    public byte get(int i) {
        return data[i];
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final IndicesStructure other = (IndicesStructure) obj;
        if (!Arrays.equals(this.data, other.data))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + Arrays.hashCode(this.data);
        return hash;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (byte b : data)
            sb.append(Byte.toString(b)).append(":");
        return sb.toString();
    }
}