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
package cc.redberry.core;

import cc.redberry.core.math.MathUtils;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensors;
import gnu.trove.set.hash.TIntHashSet;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class BlackList {

    @Test
    public void etwer() {
//        int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
//        int[] p = {1, 2, 4, 5, 6, 9, 10, 12};
//
//
//        int size = p.length, pointer = 0, s = array.length;
//
//        int[] r = new int[array.length - p.length];
//
//        pointer = 0;
//        int i = -1;
//        for (int j = 0; j < s; ++j) {
//            if (pointer < size - 1 && j > p[pointer])
//                ++pointer;
//            if (j == p[pointer]) continue;
//            else r[++i] = array[j];
//        }
//        System.out.println(Arrays.toString(r));

        int[] a = {1,2};
        int[] b = {1,2};
        System.out.println(a.hashCode());
        System.out.println(b.hashCode());
    }

    private static void swap(int x[], int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
//
//    private static void aaa(int[] xxx) {
//        System.out.println("as");
//    }
//
//    private static void bbb(Integer[] xxx) {
//        System.out.println("sa");
//    }


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

//    private static final Pattern pattern = Pattern.compile(
//                    "\\([\\s]*\"([a-zA-Z0-9=\\s\\*\\\\:+\\/\\-\\.\\,\\\\_\\^\\}\\{\\]\\[\\)\\(\"\n\"]*)\"\\)[\\s]*" +
//                    "(\\;|\\,|[\\)]*[\\;]*|\\.)");

    @Test
    public void test12131() {
        String p = "((?>(?>[a-zA-Z])|(?>\\\\[a-zA-A]*))(?>_(?>(?>[0-9])|(?>[\\{][0-9\\s]*[\\}])))?[']*)";
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher("{x {\\gamma_2}} {{a}df''xc}{ e_{9 2}'' d\\alpha}\\beta");
        while (matcher.find()) {
            String group = matcher.group();
            System.out.println(group);
            System.out.println();
        }
    }


    @Test
    public void test121() {
        int t = 0;
        foo(++t, ++t);
    }

    private static void foo(int i, int j) {
        System.out.println(i);
        System.out.println(j);
    }

//    @Test
//    public void test() {
////        burnJvm();
//        long start;
//        int[] a;
//        for (int i = 0; i < 1; ++i) {
//            a = randomArray();
//            start = System.currentTimeMillis();
//            Arrays.sort(a);
//            System.out.println(Arrays.toString(a));
//            System.out.println(System.currentTimeMillis() - start);
//        }
//    }

    public static void burnJvm() {
        int[] a = null;
        int t = 0;
        for (int i = 0; i < 11000; ++i) {
            a = randomArray(1000000);
            Arrays.sort(a);
            int s = 0;
            for (int j = 0; j < 1000000; ++j)
                s += a[j];
            t += s;
        }
        t = ~t;
    }

    public static int[][] randomArray1() {
        int[][] a = new int[1000000][];
        for (int i = 0; i < 1000000; ++i) {
            a[i] = randomArray(3);
        }
        return a;
    }

    public static int[] randomArray(int size) {
        int[] a = new int[size];
        Random random = new Random();
        for (int i = 0; i < size; ++i)
            a[i] = random.nextInt(11);
        return a;
    }
}
