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

package cc.redberry.core.indexmapping;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.Context;
import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.indices.IndicesUtils;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndexMappingBufferImpl implements IndexMappingBuffer {

    protected final Map<Integer, IndexMappingBufferRecord> map;
    protected boolean signum = false;

    public IndexMappingBufferImpl() {
        this.map = new HashMap<>();
    }

    private IndexMappingBufferImpl(Map<Integer, IndexMappingBufferRecord> map, boolean signum) {
        this.map = map;
        this.signum = signum;
    }

    @Override
    public void addSignum(boolean signum) {
        this.signum ^= signum;
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
        Iterator<IndexMappingBufferRecord> iterator = map.values().iterator();
        while (iterator.hasNext())
            if (iterator.next().isContracted())
                iterator.remove();
    }

    @Override
    public boolean getSignum() {
        return signum;
    }

    @Override
    public Map<Integer, IndexMappingBufferRecord> getMap() {
        return map;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public IndexMappingBufferImpl clone() {
        Map<Integer, IndexMappingBufferRecord> newMap = new HashMap<>();
        for (Map.Entry<Integer, IndexMappingBufferRecord> entry : map.entrySet())
            newMap.put(entry.getKey(), entry.getValue().clone());
        return new IndexMappingBufferImpl(newMap, signum);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(signum ? '-' : '+');
        if (map.isEmpty())
            return sb.append(":empty buffer").toString();
        for (Map.Entry<Integer, IndexMappingBufferRecord> entry : map.entrySet()) {
            sb.append(Context.get().getIndexConverterManager().getSymbol(entry.getKey().intValue(), ToStringMode.UTF8));
            sb.append("->");
            sb.append(Context.get().getIndexConverterManager().getSymbol(entry.getValue().getIndexName(), ToStringMode.UTF8));
            sb.append(":");
            for (int i = 2; i >= 0; --i)
                sb.append(entry.getValue().getStatesBit(i) ? 1 : 0);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
//    public String toStringWithStates() {
//        StringBuilder sb = new StringBuilder();
//        sb.append(signum ? '-' : '+');
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
//            sb.append(Context.get().getIndexConverterManager().getSymbol(from & 0x7FFFFFFF , ToStringMode.UTF8));
//            sb.append("->");
//              if((to & 0x80000000) == 0x80000000){
//                sb.append("^");
//            }else
//                sb.append("_");
//            sb.append(Context.get().getIndexConverterManager().getSymbol(to & 0x7FFFFFFF, ToStringMode.UTF8));            
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
        if (signum != other.signum)
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
        int hash = 7;
        for (Map.Entry<Integer, IndexMappingBufferRecord> entry : map.entrySet())
            hash = hash * 31 + entry.hashCode();
        hash = 79 * hash + (this.signum ? 1 : 0);
        return hash;
    }
}
