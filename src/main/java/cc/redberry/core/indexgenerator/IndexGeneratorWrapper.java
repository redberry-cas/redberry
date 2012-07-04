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
package cc.redberry.core.indexgenerator;

import java.util.HashMap;
import java.util.Map;
import cc.redberry.core.utils.IntArrayList;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IndexGeneratorWrapper extends IndexGenerator {
    private final IndexGenerator indexGenerator;
    private IntArrayList arrayList = new IntArrayList();
    
    public IndexGeneratorWrapper(IndexGenerator indexGenerator) {
        super(indexGenerator.clone().generators);
        this.indexGenerator = indexGenerator;
    }
    
    private IndexGeneratorWrapper(IndexGenerator indexGenerator, Map<Byte, IntGenerator> generators, IntArrayList arrayList) {
        super(generators);
        this.indexGenerator = indexGenerator;
        this.arrayList = arrayList;
    }
    
    public void dump() {
        generators = indexGenerator.clone().generators;
    }
    
    public void write() {
        for (int i = 0; i < arrayList.size(); ++i)
            indexGenerator.add(arrayList.get(i));
    }
    
    @Override
    public int generate(byte type) {
        int index = super.generate(type);
        if (!arrayList.contains(index))
            arrayList.add(index);        
        return index;
    }
    
    @Override
    public void add(int index) {
        indexGenerator.add(index);
//        throw new UnsupportedOperationException();
////            super.addAll(index);
    }
    
    @Override
    public boolean contains(int index) {
        throw new UnsupportedOperationException();
//        return indexGenerator.contains(index);
//            return super.contains(index);
    }
    
    @Override
    public IndexGenerator clone() {
        Map<Byte, IntGenerator> newMap = new HashMap<>(generators.size());
        for (Map.Entry<Byte, IntGenerator> entry : generators.entrySet())
            newMap.put(entry.getKey(), entry.getValue().clone());
        return new IndexGeneratorWrapper(indexGenerator, newMap, arrayList.clone());
    }
}
