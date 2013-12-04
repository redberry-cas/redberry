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

import cc.redberry.core.context.CC;
import cc.redberry.core.groups.permutations.gap.GapPrimitiveGroupsReader;
import cc.redberry.core.number.NumberUtils;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well1024a;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;
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

        b = new PermutationOneLine(1, 0, 2, 3, 4, 5);
        c = new PermutationOneLine(1, 2, 3, 4, 5, 0);

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(b, c);
        assertEquals(NumberUtils.factorial(6), pg.order());

        assertTrue(pg.membershipTest(b));
        assertTrue(pg.membershipTest(c));
        b = new PermutationOneLine(1, 0, 2, 5, 4, 3);
        c = new PermutationOneLine(1, 3, 2, 5, 0, 4);
        assertTrue(pg.membershipTest(b));
        assertTrue(pg.membershipTest(c));
    }

    @Test
    public void test2() {
        Permutation b, c;

        b = new PermutationOneLine(1, 0, 2, 3, 4, 5);
        c = new PermutationOneLine(2, 3, 4, 5, 0, 1);

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(b, c);
        assertEquals(24, pg.order().intValue());
    }

    @Test
    public void testIterator1() {
        Permutation b, c;

        b = new PermutationOneLine(1, 0, 2, 3, 4, 5);
        c = new PermutationOneLine(1, 2, 3, 4, 5, 0);

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(b, c);
        int order = pg.order().intValue();
        Set<Permutation> allPermutations = new HashSet<>();
        for (Permutation p : pg)
            allPermutations.add(p);
        assertEquals(allPermutations.size(), order);
    }

    @Test
    public void testIterator2() {
        PermutationOneLine b, c;

        b = new PermutationOneLine(1, 4, 2, 3, 0, 5);
        c = new PermutationOneLine(1, 0, 4, 3, 5, 2);

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
        PermutationOneLine b, c;

        b = new PermutationOneLine(1, 4, 2, 3, 0, 5);
        c = new PermutationOneLine(true, 2, 0, 4, 5, 3, 1);

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
        PermutationOneLine b, c;

        b = new PermutationOneLine(true, 1, 0, 2);
        c = new PermutationOneLine(true, 0, 2, 1);

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

        b = new PermutationOneLine(true, 1, 0, 2);
        c = new PermutationOneLine(0, 2, 1);

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(b, c);
    }

    @Test
    public void testInconsistentGenerators1() {
        RandomGenerator random = new Well1024a();
        List<PermutationOneLine> permutations = new ArrayList<>();

        //long start = System.currentTimeMillis();
        int inconsistentGeneratorsCount = 0;
        while (inconsistentGeneratorsCount < 500) {
            permutations.clear();
            for (int i = 0; i < 10; ++i) {
                PermutationOneLine p = null;
                try {
                    p = new PermutationOneLine(i % 2 == 0 ? true : false, Permutations.randomPermutation(6, random));
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
        a = new PermutationOneLine(1, 0, 2, 3, 4, 5, 6);
        b = new PermutationOneLine(2, 1, 3, 4, 5, 6, 0);
        generators = new Permutation[]{a, b};
        group = PermutationGroupFactory.createPermutationGroup(generators);
        System.out.println(
                Arrays.toString(group.getBase()));
    }

    @Test
    public void testMembership() {
        Permutation a, b, c, d, e, generators[];
        PermutationGroup group;

        a = new PermutationOneLine(1, 0, 2, 3, 4, 5, 6);
        b = new PermutationOneLine(1, 2, 3, 4, 5, 6, 0);
        generators = new Permutation[]{a, b};
        group = PermutationGroupFactory.createPermutationGroup(generators);
        assertEquals(group.order().longValue(), 5040L);
        assertGroupIterator(group);

        b = new PermutationOneLine(1, 2, 3, 4, 5, 6, 0);
        a = new PermutationOneLine(1, 0, 2, 3, 4, 5, 6);
        c = new PermutationOneLine(1, 2, 3, 5, 6, 4, 0);
        d = new PermutationOneLine(1, 4, 3, 2, 5, 6, 0);
        generators = new Permutation[]{a, b, c, d};
        group = PermutationGroupFactory.createPermutationGroup(generators);
        assertEquals(group.order().longValue(), 5040L);
        assertGroupIterator(group);

        b = new PermutationOneLine(1, 2, 3, 4, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        a = new PermutationOneLine(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        c = new PermutationOneLine(1, 2, 3, 5, 6, 4, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        d = new PermutationOneLine(1, 4, 3, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        e = new PermutationOneLine(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 18);
        generators = new Permutation[]{a, b, c, d, e};
        group = PermutationGroupFactory.createPermutationGroup(generators);
        assertEquals(group.order().longValue(), 10080L);
        assertGroupIterator(group);

        b = new PermutationOneLine(1, 2, 3, 4, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        a = new PermutationOneLine(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        c = new PermutationOneLine(1, 2, 3, 5, 6, 4, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        d = new PermutationOneLine(1, 4, 3, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        e = new PermutationOneLine(1, 0, 2, 3, 14, 5, 6, 7, 8, 9, 10, 11, 12, 13, 4, 15, 16, 17, 18, 19);
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
            assertTrue(pg.membershipTest(permutation));

        assertEquals(pg.order().longValue(), set.size());
    }

    @Test
    public void testIdentityGroup() throws Exception {
        PermutationGroup id = PermutationGroupFactory.createPermutationGroup(Permutations.getIdentityOneLine(10));

        assertTrue(id.membershipTest(Permutations.getIdentityOneLine(10)));

        Set<Permutation> set = new HashSet<>();
        set.add(Permutations.getIdentityOneLine(10));

        for (Permutation p : id)
            assertTrue(set.remove(p));

        assertTrue(set.isEmpty());
    }

    @Test
    public void testOrbit1() {
        Permutation a, b, c, d, e, generators[];
        PermutationGroup group;
        a = new PermutationOneLine(1, 0, 2, 3, 4, 5, 6, 7);
        b = new PermutationOneLine(2, 1, 3, 4, 5, 6, 0, 7);
        generators = new Permutation[]{a, b};
        group = PermutationGroupFactory.createPermutationGroup(generators);

        int[] o = group.orbit(7, 0);
        Arrays.sort(o);
        assertArrayEquals(new int[]{0, 1, 2, 3, 4, 5, 6, 7}, o);

        o = group.orbit(7);
        assertArrayEquals(new int[]{7}, o);

        o = group.orbit(2, 0, 6);
        Arrays.sort(o);
        assertArrayEquals(new int[]{0, 1, 2, 3, 4, 5, 6}, o);
    }

    @Test
    public void testPointwiseStabilizer1() {
        PermutationGroup group = GapPrimitiveGroupsReader.readGroupFromGap("/home/stas/gap4r6/prim/grps/gps1.g", 27);
        List<BSGSElement> bsgs = group.getBSGS();
        int[] base = group.getBase();
        for (int i = 1; i < base.length; ++i) {
            int[] points = Arrays.copyOfRange(base, 0, i);
            PermutationGroup stab = group.pointwiseStabilizer(points);

            List<BSGSElement> stabBsgs = bsgs.subList(i, bsgs.size());

            assertEquals(AlgorithmsBase.calculateOrder(stabBsgs), stab.order());
            for (Permutation p : stabBsgs.get(0).stabilizerGenerators)
                assertTrue(stab.membershipTest(p));
        }
    }

    @Test
    public void testPointwiseStabilizer2() {
        final PermutationGroup[] groups = GapPrimitiveGroupsReader.readGroupsFromGap("/home/stas/gap4r6/prim/grps/gps1.g");
        for (int C = 0; C < groups.length; C += 2) {
            PermutationGroup group = groups[C];
            BigInteger order = group.order();
            for (int i = 0; i < group.degree(); ++i) {
                int orbSize = group.orbit(i).length;
                PermutationGroup stab = group.pointwiseStabilizer(i);
                assertEquals(order.divide(BigInteger.valueOf(orbSize)), stab.order());
                List<Permutation> gens = stab.generators();
                for (Permutation p : gens)
                    assertEquals(p.newIndexOf(i), i);
            }
        }
    }

    @Test
    public void testPointwiseStabilizer3() {
        PermutationGroup[] groups = GapPrimitiveGroupsReader.readGroupsFromGap("/home/stas/gap4r6/prim/grps/gps1.g");
        for (int C = 0; C < groups.length; C += 2) {
            PermutationGroup group = groups[C];
            if (group.degree() < 11)
                continue;
            for (int aa = 0; aa < 10; ++aa) {
                int[] set = new int[5];
                Arrays.fill(set, -1);
                int step = group.degree() / set.length;
                int j = 0;
                for (int i = 0; i < group.degree() && j < set.length; i += 1 + CC.getRandomGenerator().nextInt(step), ++j)
                    set[j] = i;
                j = set.length - 1;
                while (set[j] == -1) {
                    --j;
                }
                set = Arrays.copyOf(set, j);
                if (set.length == 0)
                    continue;


                PermutationGroup stab = group.pointwiseStabilizer(set);
                assertTrue(stab.equals(pointWiseStabilizerBruteForce(group, set)));
            }
        }
    }

    @Test
    public void testSetwiseStabilizer1() {
        PermutationGroup group = GapPrimitiveGroupsReader.readGroupFromGap("/home/stas/gap4r6/prim/grps/gps1.g", 34);
        System.out.println(group.setwiseStabilizer(2, 3));

    }

    private static PermutationGroup pointWiseStabilizerBruteForce(PermutationGroup pg, int[] points) {
        PermutationGroup stab = pg;
        for (int i : points)
            stab = stab.pointwiseStabilizer(i);
        return stab;
    }
}
