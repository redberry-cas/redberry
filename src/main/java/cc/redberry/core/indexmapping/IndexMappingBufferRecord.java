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
package cc.redberry.core.indexmapping;

import cc.redberry.core.context.Context;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.InconsistentIndicesException;
import cc.redberry.core.indices.IndicesUtils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndexMappingBufferRecord {

    /*
     * Mask 0b00000SUL
     *
     * S - initialized with different states U - "to" upper index mapped L -
     * "to" lower index mapped
     *
     */
    byte states;
    private final int toName;

    public IndexMappingBufferRecord(int from, int to) {
        this.toName = IndicesUtils.getNameWithType(to);
        states |= 1 << IndicesUtils.getStateInt(to);
        states |= (IndicesUtils.getStateInt(from) ^ IndicesUtils.getStateInt(to)) << 2;
    }

    public IndexMappingBufferRecord(byte usedStates, int indexName) {
        this.states = usedStates;
        this.toName = indexName;
    }

    public boolean tryMap(int from, int to) {
        if (IndicesUtils.getNameWithType(to) != toName)
            return false;
        if ((IndicesUtils.getStateInt(from) != IndicesUtils.getStateInt(to)) != ((states & 0x4) == 0x4))
            throw new InconsistentIndicesException(from);
        if ((states & (1 << IndicesUtils.getStateInt(to))) != 0)
            throw new InconsistentIndicesException(to);
        states |= 1 << IndicesUtils.getStateInt(to);
        return true;
    }

    /**
     *
     * @return name with type
     */
    public int getIndexName() {
        return toName;
    }

    //    public int getFromRawState() {
    //        return ((states & 4) == 4 ? 0x80000000 : 0) ^ (((states & 1) ^ 1) << 31);
    //    }
    //
    //    public int getToRawState() {
    //        return ((states & 1) ^ 1) << 31;
    //    }
    public byte getStates() {
        return states;
    }

    public boolean getStatesBit(int state) {
        return ((states >>> state) & 1) == 1;
    }

    public boolean isContracted() {
        return (states & 3) == 3;
    }

    public boolean diffStatesInitialized() {
        return (states & 4) == 4;
    }

    @Override
    public IndexMappingBufferRecord clone() {
        return new IndexMappingBufferRecord(states, toName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final IndexMappingBufferRecord other = (IndexMappingBufferRecord) obj;
        if (this.states != other.states)
            return false;
        return this.toName == other.toName;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * 7 + this.states) + this.toName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(Context.get().getIndexConverterManager().getSymbol(getIndexName(), OutputFormat.UTF8));
        sb.append(":");
        for (int i = 2; i >= 0; --i)
            sb.append(getStatesBit(i) ? 1 : 0);
        return sb.toString();
    }
}
