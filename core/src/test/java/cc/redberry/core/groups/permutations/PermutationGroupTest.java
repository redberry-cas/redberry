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
package cc.redberry.core.groups.permutations;

import cc.redberry.core.number.NumberUtils;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well1024a;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationGroupTest {
    @Test
    public void test1() {
        Permutation b, c;

        b = new Permutation(1, 0, 2, 3, 4, 5);
        c = new Permutation(1, 2, 3, 4, 5, 0);

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(b, c);
        assertEquals(NumberUtils.factorial(6), pg.order());

        assertTrue(pg.isMember(b));
        assertTrue(pg.isMember(c));
        b = new Permutation(1, 0, 2, 5, 4, 3);
        c = new Permutation(1, 3, 2, 5, 0, 4);
        assertTrue(pg.isMember(b));
        assertTrue(pg.isMember(c));
    }

    @Test
    public void test2() {
        Permutation b, c;

        b = new Permutation(1, 0, 2, 3, 4, 5);
        c = new Permutation(2, 3, 4, 5, 0, 1);

        System.out.println(c.composition(c));
        System.out.println(c.composition(c).composition(c));

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(b, c);
        assertEquals(NumberUtils.factorial(6), pg.order());


    }

    @Test
    public void testIterator1() {
        Permutation b, c;

        b = new Permutation(1, 0, 2, 3, 4, 5);
        c = new Permutation(1, 2, 3, 4, 5, 0);

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(b, c);
        int order = pg.order().intValue();
        Set<Permutation> allPermutations = new HashSet<>();
        for (Permutation p : pg)
            allPermutations.add(p);
        assertEquals(allPermutations.size(), order);
    }

    @Test
    public void testIterator2() {
        Permutation b, c;

        b = new Permutation(1, 4, 2, 3, 0, 5);
        c = new Permutation(1, 0, 4, 3, 5, 2);

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(b, c);
        int order = pg.order().intValue();

        HashSet<Permutation> allPermutations1 = new HashSet<>();
        for (Permutation p : pg)
            allPermutations1.add(p);
        assertEquals(allPermutations1.size(), order);

        BruteForcePermutationIterator bf = new BruteForcePermutationIterator(Arrays.asList(b, c));
        HashSet<Permutation> allPermutations2 = new HashSet<>();
        while (bf.hasNext())
            allPermutations2.add(bf.next());
        assertEquals(allPermutations1, allPermutations2);
    }

    @Test
    public void testIterator3() {
        Permutation b, c;

        b = new Permutation(1, 4, 2, 3, 0, 5);
        c = new Permutation(true, 2, 0, 4, 5, 3, 1);

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(b, c);
        int order = pg.order().intValue();

        HashSet<Permutation> allPermutations1 = new HashSet<>();
        for (Permutation p : pg)
            allPermutations1.add(p);
        assertEquals(allPermutations1.size(), order);

        BruteForcePermutationIterator bf = new BruteForcePermutationIterator(Arrays.asList(b, c));
        HashSet<Permutation> allPermutations2 = new HashSet<>();
        while (bf.hasNext())
            allPermutations2.add(bf.next());
        assertEquals(allPermutations1, allPermutations2);
    }

    @Test
    public void testIterator3a() {
        Permutation b, c;

        b = new Permutation(true, 1, 0, 2);
        c = new Permutation(true, 0, 2, 1);

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(b, c);
        int order = pg.order().intValue();

        HashSet<Permutation> allPermutations1 = new HashSet<>();
        for (Permutation p : pg)
            allPermutations1.add(p);
        assertEquals(allPermutations1.size(), order);

        BruteForcePermutationIterator bf = new BruteForcePermutationIterator(Arrays.asList(b, c));
        HashSet<Permutation> allPermutations2 = new HashSet<>();
        while (bf.hasNext())
            allPermutations2.add(bf.next());
        assertEquals(allPermutations1, allPermutations2);
    }

    @Test(expected = InconsistentGeneratorsException.class)
    public void testInconsistentGenerators() {
        Permutation b, c;

        b = new Permutation(true, 1, 0, 2);
        c = new Permutation(0, 2, 1);

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(b, c);
    }

    @Test
    public void testInconsistentGenerators1() {
        RandomGenerator random = new Well1024a();
        List<Permutation> permutations = new ArrayList<>();

        //long start = System.currentTimeMillis();
        int inconsistentGeneratorsCount = 0;
        while (inconsistentGeneratorsCount < 500) {
            permutations.clear();
            for (int i = 0; i < 10; ++i) {
                Permutation p = null;
                try {
                    p = new Permutation(i % 2 == 0 ? true : false, Combinatorics.randomPermutation(6, random));
                } catch (IllegalArgumentException e) {
                }
                if (p != null)
                    permutations.add(p);
            }

            if (permutations.isEmpty())
                continue;

            boolean inconsistent = false;
            try {
                BruteForcePermutationIterator bf = new BruteForcePermutationIterator(new ArrayList<>(permutations));
                while (bf.hasNext())
                    bf.next();
            } catch (Exception ex) {
                inconsistent = true;
            }

            if (!inconsistent)
                continue;

            ++inconsistentGeneratorsCount;

            try {
                PermutationGroupFactory.createPermutationGroup(permutations.toArray(new Permutation[0]));
            } catch (InconsistentGeneratorsException e) {
                continue;
            }

            assertTrue(false);
        }
        //seed = 123
        //System.out.println(System.currentTimeMillis() - start);
        //without any checks: 23976 ms
        //with checks       : 26467 ms
    }

    @Test
    public void testBSGS1() {
        Permutation a, b, c, d, e, generators[];
        PermutationGroup group;
        a = new Permutation(1, 0, 2, 3, 4, 5, 6);
        b = new Permutation(2, 1, 3, 4, 5, 6, 0);
        generators = new Permutation[]{a, b};
        group = PermutationGroupFactory.createPermutationGroup(generators);
        System.out.println(
                Arrays.toString(((PermutationGroupImpl) group).getBSGS().getBaseArray()));
    }

    @Test
    public void testMembership() {
        Permutation a, b, c, d, e, generators[];
        PermutationGroup group;

        a = new Permutation(1, 0, 2, 3, 4, 5, 6);
        b = new Permutation(1, 2, 3, 4, 5, 6, 0);
        generators = new Permutation[]{a, b};
        group = PermutationGroupFactory.createPermutationGroup(generators);
        assertEquals(group.order().longValue(), 5040L);
        assertGroupIterator(group);

        b = new Permutation(1, 2, 3, 4, 5, 6, 0);
        a = new Permutation(1, 0, 2, 3, 4, 5, 6);
        c = new Permutation(1, 2, 3, 5, 6, 4, 0);
        d = new Permutation(1, 4, 3, 2, 5, 6, 0);
        generators = new Permutation[]{a, b, c, d};
        group = PermutationGroupFactory.createPermutationGroup(generators);
        assertEquals(group.order().longValue(), 5040L);
        assertGroupIterator(group);

        b = new Permutation(1, 2, 3, 4, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        a = new Permutation(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        c = new Permutation(1, 2, 3, 5, 6, 4, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        d = new Permutation(1, 4, 3, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        e = new Permutation(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 18);
        generators = new Permutation[]{a, b, c, d, e};
        group = PermutationGroupFactory.createPermutationGroup(generators);
        assertEquals(group.order().longValue(), 10080L);
        assertGroupIterator(group);

        b = new Permutation(1, 2, 3, 4, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        a = new Permutation(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        c = new Permutation(1, 2, 3, 5, 6, 4, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        d = new Permutation(1, 4, 3, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        e = new Permutation(1, 0, 2, 3, 14, 5, 6, 7, 8, 9, 10, 11, 12, 13, 4, 15, 16, 17, 18, 19);
        generators = new Permutation[]{a, b, c, d, e};
        group = PermutationGroupFactory.createPermutationGroup(generators);
        assertEquals(group.order().longValue(), 5040L * 8L);
        assertGroupIterator(group);
    }

    public static void assertGroupIterator(PermutationGroup pg) {
        HashSet<Permutation> set = new HashSet<>();

        for (Permutation permutation : pg)
            set.add(permutation);

        for (Permutation permutation : set)
            assertTrue(pg.isMember(permutation));

        assertEquals(pg.order().longValue(), set.size());
    }

    @Test
    public void testIdentityGroup() throws Exception {
        PermutationGroup id = PermutationGroupFactory.createPermutationGroup(Combinatorics.createIdentity(10));

        assertTrue(id.isMember(Combinatorics.createIdentity(10)));

        Set<Permutation> set = new HashSet<>();
        set.add(Combinatorics.createIdentity(10));

        for (Permutation p : id)
            assertTrue(set.remove(p));

        assertTrue(set.isEmpty());
    }
}
