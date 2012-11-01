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
package cc.redberry.core;

import gnu.trove.set.hash.TIntHashSet;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;
import java.util.Random;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class BlackList {

    public enum Name {

        E1("a"), E2("b");
        final String name;

        Name(String name) {
            this.name = name;
        }

        public static final Name[] values = values();
    }

    @Test
    public void etwer() {
         System.out.println((-8 & 0x7FFFFFFF));
    }

    @Ignore
    @Test
    public void te() {
        TIntHashSet ihs = new TIntHashSet();
        HashSet<Integer> hs = new HashSet<>();
        Random random = new Random();
        long it = 0, t = 0, start, stop;
        for (int i = 0; i < 10000000; ++i) {
            int r = random.nextInt(Integer.MAX_VALUE);
            start = System.currentTimeMillis();
            ihs.add(r);
            ihs.remove(r);
            ihs.contains(r);
            stop = System.currentTimeMillis();
            it += stop - start;

            start = System.currentTimeMillis();
            hs.add(r);
            hs.remove(r);
            hs.contains(r);
            stop = System.currentTimeMillis();
            t += stop - start;
        }
        System.out.println("primitive   " + it);
        System.out.println("object   " + t);
    }

    @Ignore
    @Test
    public void te1() {
        TIntHashSet ihs = new TIntHashSet();
        HashSet<Integer> hs = new HashSet<>();
        Random random = new Random();
        long it = 0, t = 0, start, stop;
        int size = 10000000;
        int[] r = new int[size];
        for (int i = 0; i < size; ++i)
            r[i] = random.nextInt(Integer.MAX_VALUE);

        start = System.currentTimeMillis();
        for (int i = 0; i < size; ++i)
            ihs.add(r[i]);
        for (int i = 0; i < size; ++i) {
            ihs.remove(r[i]);
            ihs.contains(r[i]);
        }
        stop = System.currentTimeMillis();
        it += stop - start;

        start = System.currentTimeMillis();
        for (int i = 0; i < size; ++i)
            hs.add(r[i]);
        for (int i = 0; i < size; ++i) {
            hs.remove(r[i]);
            hs.contains(r[i]);
        }
        stop = System.currentTimeMillis();
        t += stop - start;
        System.out.println("primitive   " + it);
        System.out.println("object   " + t);
    }


}
