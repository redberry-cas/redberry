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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.Ignore;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
@Ignore
public class IndexMappingTestUtils {
    /**
     * Parse mapping string:  "+;_a->_p;_b->_q;_c->_r;_d->_s";
     * @param str +;_a->_p;_b->_q;_c->_r;_d->_s
     * @return mapping
     */
    @SuppressWarnings("unchecked")
    public static IndexMappingBufferImpl parse(String str) {
        IndexMappingBufferImpl im = new IndexMappingBufferImpl();
        String[] singleMaps = str.split(";");
        switch (singleMaps[0]) {
            case "+":
                im.addSignum(false);
                break;
            case "-":
                im.addSignum(true);
                break;
            default:
                throw new RuntimeException();
        }
        if (singleMaps.length == 1)
            return im;
        for (int i = 1; i < singleMaps.length; ++i) {
            String[] parts = singleMaps[i].split("->");
            int from = parseIndex(parts[0]);
            int to = parseIndex(parts[1]);
            im.tryMap(from, to);
        }
        return im;
    }

    @SuppressWarnings("unchecked")
    private static int parseIndex(String str) {
        int state = str.charAt(0) == '^' ? 0x80000000 : 0;
        int index = CC.getIndexConverterManager().getCode(str.substring(1));
        index |= state;
        return index;
    }

    public static boolean compare(List<IndexMappingBuffer> l1, List<IndexMappingBuffer> l2) {
        Collections.sort(l1, COMPARATOR);
        Collections.sort(l2, COMPARATOR);
        int size;
        if ((size = l1.size()) != l2.size())
            return false;
        for (int i = 0; i < size; ++i)
            if (!l1.get(i).equals(l2.get(i)))
                return false;
        return true;
    }

    public static Comparator<IndexMappingBuffer> getComparator() {
        return COMPARATOR;
    }
    private static final Comparator<IndexMappingBuffer> COMPARATOR = new Comparator<IndexMappingBuffer>() {
        @Override
        public int compare(IndexMappingBuffer o1, IndexMappingBuffer o2) {
            return Integer.compare(o1.hashCode(), o2.hashCode());
        }
    };
}
