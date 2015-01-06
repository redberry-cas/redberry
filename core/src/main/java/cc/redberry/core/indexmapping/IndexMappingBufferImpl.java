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

import cc.redberry.core.context.CC;
import cc.redberry.core.context.Context;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.IndicesUtils;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import static cc.redberry.core.indices.IndicesUtils.setRawState;

/**
 * Basic implementation of {@link IndexMappingBuffer}.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
final class IndexMappingBufferImpl implements IndexMappingBuffer {

    protected final TIntObjectHashMap<IndexMappingBufferRecord> map;
    protected boolean sign = false;

    /**
     * Constructs empty buffer.
     */
    public IndexMappingBufferImpl() {
        this.map = new TIntObjectHashMap<>();
    }

    /**
     * Constructs empty buffer with specified sign.
     */
    public IndexMappingBufferImpl(boolean sign) {
        this.map = new TIntObjectHashMap<>();
        this.sign = sign;
    }

    private IndexMappingBufferImpl(TIntObjectHashMap<IndexMappingBufferRecord> map, boolean sign) {
        this.map = map;
        this.sign = sign;
    }

    @Override
    public void addSign(boolean sign) {
        this.sign ^= sign;
    }

    @Override
    public boolean tryMap(int from, int to) {
        int fromState = IndicesUtils.getStateInt(from);
        if (fromState != IndicesUtils.getStateInt(to) && !CC.isMetric(IndicesUtils.getType(from)))
            return false;
        int fromName = IndicesUtils.getNameWithType(from);
        IndexMappingBufferRecord record = map.get(fromName);
        if (record == null) {
            record = new IndexMappingBufferRecord(from, to);
            map.put(fromName, record);
            return true;
        }
        return record.tryMap(from, to);
    }

    @Override
    public void removeContracted() {
        TIntObjectIterator<IndexMappingBufferRecord> iterator = map.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            if (iterator.value().isContracted())
                iterator.remove();
        }
    }

    @Override
    public boolean getSign() {
        return sign;
    }

    @Override
    public TIntObjectHashMap<IndexMappingBufferRecord> getMap() {
        return map;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public FromToHolder export() {
        final int size = map.size();
        int[] from = new int[size],
                to = new int[size];
        int i = 0;
        TIntObjectIterator<IndexMappingBufferRecord> iterator = map.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            from[i] = iterator.key();
            to[i++] = iterator.value().getIndexName();
        }
        return new FromToHolder(from, to, sign);
    }

    @Override
    public IndexMappingBufferImpl clone() {
        TIntObjectHashMap<IndexMappingBufferRecord> newMap = new TIntObjectHashMap<>(map);
        TIntObjectIterator<IndexMappingBufferRecord> iterator = newMap.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            iterator.setValue(iterator.value().clone());
        }
        return new IndexMappingBufferImpl(newMap, sign);
    }

    //    @Override
    //    public String toString() {
    //        StringBuilder sb = new StringBuilder();
    //        sb.append(sign ? '-' : '+');
    //        if (map.isEmpty())
    //            return sb.append(":empty buffer").toString();
    //        for (Map.Entry<Integer, IndexMappingBufferRecord> entry : map.entrySet()) {
    //            sb.append(Context.get().getIndexConverterManager().getSymbol(entry.getKey(), OutputFormat.UTF8));
    //            sb.append("->");
    //            sb.append(Context.get().getIndexConverterManager().getSymbol(entry.getValue().getIndexName(), OutputFormat.UTF8));
    //            sb.append(":");
    //            for (int i = 2; i >= 0; --i)
    //                sb.append(entry.getValue().getStatesBit(i) ? 1 : 0);
    //            sb.append(",");
    //        }
    //        sb.deleteCharAt(sb.length() - 1);
    //        return sb.toString();
    //    }
    @Override
    public String toString() {
        return toString(CC.getDefaultOutputFormat());
    }

    private static String toStringIndex(int index, OutputFormat mode) {
        return (IndicesUtils.getState(index) ? "^" : "_") + Context.get().getIndexConverterManager().getSymbol(index, mode);
    }

    /**
     * String representation in specified output format.
     *
     * @param format output format
     * @return string representation in specified output format
     */
    public String toString(OutputFormat format) {
        StringBuilder sb = new StringBuilder();
        sb.append(sign ? '-' : '+').append('{');
        if (map.isEmpty())
            return sb.append('}').toString();
        String from, to;
        TIntObjectIterator<IndexMappingBufferRecord> iterator = map.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            if (iterator.value().isContracted()) {
                from = toStringIndex(iterator.key(), format).substring(1);
                to = toStringIndex(iterator.value().getIndexName(), format).substring(1);
                sb.append(',');
            } else {
                from = toStringIndex(setRawState(iterator.value().getFromRawState(), iterator.key()), format);
                to = toStringIndex(setRawState(iterator.value().getToRawState(), iterator.value().getIndexName()), format);
            }
            sb.append(from).append(" -> ").append(to).append(", ");
        }
        sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1).append('}');
        return sb.toString();
    }
    //    public String toStringWithStates() {
    //        StringBuilder sb = new StringBuilder();
    //        sb.append(sign ? '-' : '+');
    //        if (map.isEmpty())
    //            return sb.append(":empty buffer").toString();
    //        Map<Integer,Integer> map = getMap();
    //        int from,to;
    //        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
    //            from=entry.getKey();
    //            to = entry.getValue();
    //            if((from & 0x80000000) == 0x80000000){
    //                sb.append("^");
    //            }else
    //                sb.append("_");
    //            sb.append(Context.get().getIndexConverterManager().getSymbol(from & 0x7FFFFFFF , OutputFormat.UTF8));
    //            sb.append("->");
    //              if((to & 0x80000000) == 0x80000000){
    //                sb.append("^");
    //            }else
    //                sb.append("_");
    //            sb.append(Context.get().getIndexConverterManager().getSymbol(to & 0x7FFFFFFF, OutputFormat.UTF8));
    //            sb.append(",");
    //        }
    //        sb.deleteCharAt(sb.length() - 1);
    //        return sb.toString();
    //    }
    //    @Override
    //    public IndexMappingImpl toMapping() {
    //        int[] from = new int[map.size()], to = new int[map.size()];
    //        int i = 0;
    //        for (Map.Entry<Integer, IndexMappingBufferRecord> entry : map.entrySet()) {
    //            from[i] = entry.getKey().intValue();
    //            to[i++] = entry.getValue().getIndexName();
    //        }
    //        return new IndexMappingImpl(new int[0], from, to);
    //    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final IndexMappingBufferImpl other = (IndexMappingBufferImpl) obj;
        if (sign != other.sign)
            return false;
        if (map.size() != other.map.size())
            return false;
        if (hashCode() != other.hashCode())
            return false;
        return map.equals(other.map);

        //
        //
        //        Map.Entry<Integer, IndexMappingBufferRecord>[] first = map.entrySet().toArray(new Map.Entry[map.size()]);
        //        Map.Entry<Integer, IndexMappingBufferRecord>[] second = other.map.entrySet().toArray(new Map.Entry[map.size()]);
        //
        //        Arrays.sort(first, entryComparator);
        //        Arrays.sort(second, entryComparator);
        //        for (int i = 0; i < first.length; ++i) {
        //            if (!first[i].getKey().equals(second[i].getKey()))
        //                return false;
        //            if (!first[i].getValue().equals(second[i].getValue()))
        //                return false;
        //        }
        //        return true;
    }

    private static Comparator<Map.Entry<Integer, IndexMappingBufferRecord>> entryComparator = new Comparator<Map.Entry<Integer, IndexMappingBufferRecord>>() {

        @Override
        public int compare(Entry<Integer, IndexMappingBufferRecord> o1, Entry<Integer, IndexMappingBufferRecord> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    };

    @Override
    public int hashCode() {
        return map.hashCode() + (sign ? 1 : 0);
    }
}
