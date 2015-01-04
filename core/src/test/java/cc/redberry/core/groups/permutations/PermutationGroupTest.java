/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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

import cc.redberry.core.combinatorics.IntCombinationPermutationGenerator;
import cc.redberry.core.combinatorics.IntCombinationsGenerator;
import cc.redberry.core.context.CC;
import cc.redberry.core.number.NumberUtils;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntComparator;
import cc.redberry.core.utils.MathUtils;
import cc.redberry.core.utils.OutputPort;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well1024a;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationGroupTest extends AbstractTestClass {
    @Test
    public void test1() {
        Permutation b, c;

        b = Permutations.createPermutation(1, 0, 2, 3, 4, 5);
        c = Permutations.createPermutation(1, 2, 3, 4, 5, 0);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(b, c);
        assertEquals(NumberUtils.factorial(6), pg.order());

        assertTrue(pg.membershipTest(b));
        assertTrue(pg.membershipTest(c));
        b = Permutations.createPermutation(1, 0, 2, 5, 4, 3);
        c = Permutations.createPermutation(1, 3, 2, 5, 0, 4);
        assertTrue(pg.membershipTest(b));
        assertTrue(pg.membershipTest(c));
    }

    @Test
    public void test2() {
        Permutation b, c;

        b = Permutations.createPermutation(1, 0, 2, 3, 4, 5);
        c = Permutations.createPermutation(2, 3, 4, 5, 0, 1);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(b, c);
        assertEquals(24, pg.order().intValue());
    }

    @Test
    public void testIterator1() {
        Permutation b, c;

        b = Permutations.createPermutation(1, 0, 2, 3, 4, 5);
        c = Permutations.createPermutation(1, 2, 3, 4, 5, 0);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(b, c);
        int order = pg.order().intValue();
        Set<Permutation> allPermutations = new HashSet<>();
        for (Permutation p : pg)
            allPermutations.add(p);
        assertEquals(allPermutations.size(), order);
    }

    @Test
    public void testIterator2() {
        Permutation b, c;

        b = Permutations.createPermutation(1, 4, 2, 3, 0, 5);
        c = Permutations.createPermutation(1, 0, 4, 3, 5, 2);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(b, c);
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

        b = Permutations.createPermutation(1, 4, 2, 3, 0, 5);
        c = Permutations.createPermutation(true, 2, 0, 4, 5, 3, 1);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(b, c);
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

        b = Permutations.createPermutation(true, 1, 0, 2);
        c = Permutations.createPermutation(true, 0, 2, 1);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(b, c);
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

        b = Permutations.createPermutation(true, 1, 0, 2);
        c = Permutations.createPermutation(0, 2, 1);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(b, c);
        pg.order();
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
                    p = Permutations.createPermutation(i % 2 == 0 ? true : false, Permutations.randomPermutation(6, random));
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
                Permutation[] generators = permutations.toArray(new Permutation[0]);
                PermutationGroup.createPermutationGroup(generators).order();
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
        a = Permutations.createPermutation(1, 0, 2, 3, 4, 5, 6);
        b = Permutations.createPermutation(2, 1, 3, 4, 5, 6, 0);
        generators = new Permutation[]{a, b};
        group = PermutationGroup.createPermutationGroup(generators);
        System.out.println(
                Arrays.toString(group.getBase()));
    }

    @Test
    public void testMembership() {
        Permutation a, b, c, d, e, generators[];
        PermutationGroup group;

        a = Permutations.createPermutation(1, 0, 2, 3, 4, 5, 6);
        b = Permutations.createPermutation(1, 2, 3, 4, 5, 6, 0);
        generators = new Permutation[]{a, b};
        group = PermutationGroup.createPermutationGroup(generators);
        assertEquals(group.order().longValue(), 5040L);
        assertGroupIterator(group);

        b = Permutations.createPermutation(1, 2, 3, 4, 5, 6, 0);
        a = Permutations.createPermutation(1, 0, 2, 3, 4, 5, 6);
        c = Permutations.createPermutation(1, 2, 3, 5, 6, 4, 0);
        d = Permutations.createPermutation(1, 4, 3, 2, 5, 6, 0);
        generators = new Permutation[]{a, b, c, d};
        group = PermutationGroup.createPermutationGroup(generators);
        assertEquals(group.order().longValue(), 5040L);
        assertGroupIterator(group);

        b = Permutations.createPermutation(1, 2, 3, 4, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        a = Permutations.createPermutation(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        c = Permutations.createPermutation(1, 2, 3, 5, 6, 4, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        d = Permutations.createPermutation(1, 4, 3, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        e = Permutations.createPermutation(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 18);
        generators = new Permutation[]{a, b, c, d, e};
        group = PermutationGroup.createPermutationGroup(generators);
        assertEquals(group.order().longValue(), 10080L);
        assertGroupIterator(group);

        b = Permutations.createPermutation(1, 2, 3, 4, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        a = Permutations.createPermutation(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        c = Permutations.createPermutation(1, 2, 3, 5, 6, 4, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        d = Permutations.createPermutation(1, 4, 3, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        e = Permutations.createPermutation(1, 0, 2, 3, 14, 5, 6, 7, 8, 9, 10, 11, 12, 13, 4, 15, 16, 17, 18, 19);
        generators = new Permutation[]{a, b, c, d, e};
        group = PermutationGroup.createPermutationGroup(generators);
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
        PermutationGroup id = PermutationGroup.createPermutationGroup(Permutations.createIdentityPermutation(10));

        assertTrue(id.membershipTest(Permutations.createIdentityPermutation(10)));

        Set<Permutation> set = new HashSet<>();
        set.add(Permutations.createIdentityPermutation(10));

        for (Permutation p : id)
            assertTrue(set.remove(p));

        assertTrue(set.isEmpty());
    }

    @Test
    public void testOrbit1() {
        Permutation a, b, c, d, e, generators[];
        PermutationGroup group;
        a = Permutations.createPermutation(1, 0, 2, 3, 4, 5, 6, 7);
        b = Permutations.createPermutation(2, 1, 3, 4, 5, 6, 0, 7);
        generators = new Permutation[]{a, b};
        group = PermutationGroup.createPermutationGroup(generators);

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
    public void testPointwiseStabilizer1_WithGap() {
        PermutationGroup group = getGapInterface().primitiveGroup(23, 1);
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
    public void testPointwiseStabilizer2_WithGap() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 4; degree < 50; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            //System.out.println("DEGREE: " + degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 7)
                    continue;

                PermutationGroup group = gap.primitiveGroup(degree, i);
                BigInteger order = group.order();
                for (int j = 0; j < group.degree(); ++j) {
                    int orbSize = group.orbit(j).length;
                    PermutationGroup stab = group.pointwiseStabilizer(j);
                    assertEquals(order.divide(BigInteger.valueOf(orbSize)), stab.order());
                    List<Permutation> gens = stab.generators();
                    for (Permutation p : gens)
                        assertEquals(p.newIndexOf(j), j);
                }
            }
        }
    }

    @Test
    public void testPointwiseStabilizer3_WithGap_longTest() {
        GapGroupsInterface gap = getGapInterface();
        for (int asa = 0; asa < 10; ++asa) {
            for (int degree = 4; degree < 50; ++degree) {
                int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
                //System.out.println("DEGREE: " + degree);
                for (int i = 0; i < nrPrimitiveGroups; ++i) {
                    gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                    if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                            gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 7)
                        continue;

                    PermutationGroup group = gap.primitiveGroup(degree, i);
                    if (group.degree() < 11)
                        continue;
                    for (int aa = 0; aa < 10; ++aa) {
                        int[] set = new int[5];
                        Arrays.fill(set, -1);
                        int step = group.degree() / set.length;
                        int j = 0;
                        for (int ii = 0; ii < group.degree() && j < set.length; ii += 1 + CC.getRandomGenerator().nextInt(step), ++j)
                            set[j] = ii;
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
        }
    }

    @Test
    public void testPointwiseStabilizer3a() {
        PermutationGroup parent = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(new int[][]{{1, 27, 20, 9, 31, 21, 18, 11, 28, 13, 30, 14, 6, 2, 3, 24, 12, 5, 26, 15, 29, 22, 10, 7, 25, 23, 17, 19, 16, 8, 4}}),
                Permutations.createPermutation(new int[][]{{1, 3, 22, 13, 9}, {2, 21, 27, 4, 8}, {5, 11, 20, 24, 18}, {6, 29, 25, 17, 19}, {7, 30, 15, 28, 26}, {10, 23, 14, 31, 12}}),
                Permutations.createPermutation(new int[][]{{2, 18}, {3, 19}, {6, 22}, {7, 23}, {10, 26}, {11, 27}, {14, 30}, {15, 31}}),
                Permutations.createPermutation(new int[][]{{0, 1}, {2, 3}, {4, 5}, {6, 7}, {8, 9}, {10, 11}, {12, 13}, {14, 15}, {16, 17}, {18, 19}, {20, 21}, {22, 23}, {24, 25}, {26, 27}, {28, 29}, {30, 31}}));
        int[] set = {0, 5, 10, 16};
        PermutationGroup stab = parent.pointwiseStabilizer(set);
        PermutationGroup stabBruteForce = pointWiseStabilizerBruteForce(parent, set);
        Assert.assertEquals(stab, stabBruteForce);
    }

    @Test
    public void testSetwiseStabilizer1_WithGap() {
        PermutationGroup group = getGapInterface().primitiveGroup(12, 0);
        PermutationGroup stab = group.setwiseStabilizer(0, 1, 2);
        assertTrue(group.containsSubgroup(stab));
    }

    @Test
    public void testAlt1() {
        //n = 5
        Permutation a1 = Permutations.createPermutation(new int[]{1, 2, 0, 3, 4, 5, 6});
        Permutation a2 = Permutations.createPermutation(new int[]{1, 2, 3, 4, 5, 6, 0});
        PermutationGroup pg = PermutationGroup.createPermutationGroup(a1, a2);
        assertTrue(pg.isAlternating());
    }

    @Test
    public void testAlt2() {
        //n = 5
        Permutation a1 = Permutations.createPermutation(new int[]{1, 2, 0, 3, 4, 5});
        Permutation a2 = Permutations.createPermutation(new int[]{0, 2, 3, 4, 5, 1});
        PermutationGroup pg = PermutationGroup.createPermutationGroup(a1, a2);
        assertTrue(pg.isAlternating());
    }

    @Test
    public void testAlt3() {
        PermutationGroup alt = PermutationGroup.alternatingGroup(30);
        PermutationGroup sym = PermutationGroup.symmetricGroup(30);
        assertTrue(alt.isAlternating());
        assertTrue(!sym.isAlternating());
        assertTrue(sym.isTransitive());
        assertTrue(sym.isSymmetric());
        assertTrue(sym.containsSubgroup(alt));
        Permutation[] tr = sym.leftCosetRepresentatives(alt);
        assertEquals(2, tr.length);
        Arrays.sort(tr);
        assertTrue(tr[0].isIdentity());
        assertTrue(tr[1].parity() == 1);
    }

    @Test
    public void testDirectProduct1() {
        PermutationGroup alt = PermutationGroup.alternatingGroup(3);
        PermutationGroup sym = PermutationGroup.symmetricGroup(4);
        PermutationGroup pr = alt.directProduct(sym);
        assertTrue(AlgorithmsBase.isBSGS(pr.getBSGS()));
        assertEquals(pr.order(), alt.order().multiply(sym.order()));
        pr = sym.directProduct(alt);
        assertTrue(AlgorithmsBase.isBSGS(pr.getBSGS()));
        assertEquals(pr.order(), alt.order().multiply(sym.order()));
        assertFalse(pr.isTransitive());
        assertTrue(pr.orbits().length == 2);
    }

    @Test
    public void testIntersection1() {
        PermutationGroup alt = PermutationGroup.alternatingGroup(15);

        PermutationGroup sw1 = alt.setwiseStabilizer(1, 2, 3, 4, 5, 6);
        PermutationGroup sw2 = alt.setwiseStabilizer(4, 5, 6, 7, 8, 9, 10);
        PermutationGroup intr = sw1.intersection(sw2);
        PermutationGroup sw3 = alt.setwiseStabilizer(4, 5, 6);
        assertTrue(sw3.containsSubgroup(intr));
    }

    @Test
    public void testDirectProduct2() {
        //Alt(13)
        PermutationGroup alt = PermutationGroup.alternatingGroup(13);
        //setwise stabilizer in Alt(13)
        PermutationGroup altStab = alt.setwiseStabilizer(2, 3, 4);
        assertTrue(alt.containsSubgroup(altStab));
        //Sym(14)
        PermutationGroup sym = PermutationGroup.symmetricGroup(14);
        //setwise stabilizer in Sym(14)
        PermutationGroup symStab = sym.setwiseStabilizer(5, 6, 1);
        assertTrue(sym.containsSubgroup(symStab));
        //direct product of stabilizers
        PermutationGroup prStab = altStab.directProduct(symStab);
        //direct product Alt(13)×Sym(14)
        PermutationGroup pr = alt.directProduct(sym);
        //setwise stabilizer in product
        PermutationGroup prStab1 = pr.setwiseStabilizer(2, 3, 4, 18, 19, 14);
        assertTrue(prStab1.equals(prStab));
        assertFalse(prStab.isTransitive());
        //another setwise stabilizer
        PermutationGroup prStab2 = pr.setwiseStabilizer(23, 14, 4, 1, 6);
        //their intersection
        PermutationGroup intersection = prStab1.intersection(prStab2);
        //another larger setwise stabilizer
        PermutationGroup prStab3 = pr.setwiseStabilizer(4, 14);
        assertTrue(prStab3.containsSubgroup(intersection));
        //pointwise stabilizer
        PermutationGroup prStab4 = pr.pointwiseStabilizer(1, 2, 3, 4, 21, 22, 23);
        //union of subgroups
        PermutationGroup union = prStab1.union(prStab4);
        assertTrue(union.containsSubgroup(prStab1));
        assertTrue(union.containsSubgroup(prStab4));
        //left coset representatives of union in pr
        Permutation[] cosets = pr.leftCosetRepresentatives(union);
        assertEquals(pr.order().divide(union.order()), BigInteger.valueOf(cosets.length));
    }

    @Test
    public void testMapping1() {

        final int degree = 15,
                minFixed = degree - 7;

        System.out.println("Max possible mappings: "
                + NumberUtils.factorial(degree - minFixed).divide(BigInteger.valueOf(2)));

        DescriptiveStatistics stat = new DescriptiveStatistics();
        PermutationGroup pg = PermutationGroup.alternatingGroup(degree);

        for (int C = 0; C < 100; ++C) {
            int[] from = Permutations.randomPermutation(degree);
            int[] to = Permutations.randomPermutation(degree);
            final int cut = minFixed + CC.getRandomGenerator().nextInt(degree - minFixed);

            from = Arrays.copyOf(from, cut);
            to = Arrays.copyOf(to, cut);

            BacktrackSearch search = pg.mapping(from, to);
            Permutation p;
            int counter = 0;
            while ((p = search.take()) != null) {
                for (int i = 0; i < cut; ++i)
                    assertEquals(to[i], p.newIndexOf(from[i]));
                ++counter;
            }
            counter = counter < 0 ? ~counter : counter;
            stat.addValue(counter);
        }
        System.out.println("Statistics of mapping elements: \n" + stat);
    }

    @Test
    public void testMapping1a() {
        CC.resetTensorNames(-1242939475613841754L);
        final int degree = 15;
        final PermutationGroup pg = PermutationGroup.alternatingGroup(degree);
        //4, 1, 14, 13, 12, 10, 11, 3, 8, 9, 6, 5, 0, 7, 2
        int[] from = {1, 12, 11, 7, 3, 8, 0, 13, 10, 6, 2, 14, 9, 5};
        int[] to = {1, 0, 5, 3, 13, 8, 4, 7, 6, 11, 12, 2, 9, 10};

        BacktrackSearch search = pg.mapping(from, to);

        Permutation p;
        while ((p = search.take()) != null)
            for (int i = 0; i < from.length; ++i)
                assertEquals(to[i], p.newIndexOf(from[i]));
    }


    @Test
    public void testMappingPerformance1_WithGap_PerformanceTest() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 4; degree < 50; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            //System.out.println("DEGREE: " + degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 7)
                    continue;

                PermutationGroup pg = gap.primitiveGroup(degree, i);

                for (int C = 0; C < 100; ++C) {
                    int[] from = Permutations.randomPermutation(degree);
                    int[] to = Permutations.randomPermutation(degree);
                    final int cut = 1 + CC.getRandomGenerator().nextInt(degree - 1);

                    from = Arrays.copyOf(from, cut);
                    to = Arrays.copyOf(to, cut);

                    Permutation p = pg.mapping(from, to).take();
                    if (p != null) {
                        for (int j = 0; j < cut; ++j)
                            assertEquals(to[j], p.newIndexOf(from[j]));
                    }
                }
            }
        }

        DescriptiveStatistics timeStat = new DescriptiveStatistics();
        DescriptiveStatistics notNullStat = new DescriptiveStatistics();
        DescriptiveStatistics mixed = new DescriptiveStatistics();

        for (int degree = 4; degree < 50; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            //System.out.println("DEGREE: " + degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 7)
                    continue;

                PermutationGroup pg = gap.primitiveGroup(degree, i);


                long start, elapsed = 0;
                int notNull = 0;
                for (int C = 0; C < 100; ++C) {
                    int[] from = Permutations.randomPermutation(degree);
                    int[] to = Permutations.randomPermutation(degree);
                    final int cut = 1 + CC.getRandomGenerator().nextInt(degree - 1);

                    from = Arrays.copyOf(from, cut);
                    to = Arrays.copyOf(to, cut);

                    start = System.currentTimeMillis();
                    Permutation p = pg.mapping(from, to).take();
                    elapsed += System.currentTimeMillis() - start;
                    if (p != null) {
                        ++notNull;
                        for (int j = 0; j < cut; ++j)
                            assertEquals(to[j], p.newIndexOf(from[j]));
                    }
                }
                if (notNull != 0)
                    mixed.addValue(elapsed / notNull);
                timeStat.addValue(elapsed);
                notNullStat.addValue(notNull);
            }
        }
        System.out.println("Time " + timeStat);
        System.out.println("Not null " + notNullStat);
    }

    @Test
    public void testMapping2() {
        Permutation a = Permutations.createPermutation(true, 1, 0, 2, 3),
                b = Permutations.createPermutation(true, 0, 1, 3, 2),
                c = Permutations.createPermutation(false, 2, 3, 0, 1);
        final PermutationGroup pg = PermutationGroup.createPermutationGroup(a, b, c);

        Permutation[] all = new Permutation[pg.order().intValue()];
        int counter = 0;
        for (Permutation p : pg)
            all[counter++] = p;

        int degree = pg.degree();
        for (int k = 0; k < degree; ++k) {
            IntCombinationsGenerator comb = new IntCombinationsGenerator(degree, k);
            int[] from;
            while (comb.hasNext()) {
                from = comb.next();
                IntCombinationPermutationGenerator mapGen = new IntCombinationPermutationGenerator(degree, k);
                while (mapGen.hasNext()) {
                    int[] to = mapGen.next();
                    Iterator<Permutation> search = new OutputPort.PortIterator<>(pg.mapping(from, to));

                    Set<Permutation> actual = new HashSet<>();
                    while (search.hasNext())
                        actual.add(search.next());

                    Set<Permutation> expected = new HashSet<>();
                    out:
                    for (Permutation ppp : all) {
                        for (int i = 0; i < k; ++i)
                            if (ppp.newIndexOf(from[i]) != to[i])
                                continue out;
                        expected.add(ppp);
                    }

                    try {
                        assertEquals(expected, actual);
                    } catch (AssertionError err) {
                        int asas = 0;
                        throw err;
                    }
                }
            }
        }
    }


    @Test
    public void testNormalClosure1() throws Exception {
        PermutationGroup s3 = PermutationGroup.symmetricGroup(3);
        PermutationGroup a3 = PermutationGroup.alternatingGroup(3);
        assertTrue(s3.normalClosureOf(a3).equals(a3));
    }

    @Test
    public void testNormalClosure2() throws Exception {
        PermutationGroup s3 = PermutationGroup.symmetricGroup(3);
        PermutationGroup a3 = PermutationGroup.alternatingGroup(3);
        assertTrue(s3.normalClosureOf(a3).equals(a3));
    }

    @Test
    public void testNormalClosure() {
        int[][] a = {{1, 2}};
        int[][] b = {{1, 2, 3, 4, 5, 6, 7}};
        int[][] c = {{1, 2, 3}};
        int[][] d = {{3, 4, 5, 6, 7}};
        PermutationGroup pg1 = PermutationGroup.createPermutationGroup(Permutations.createPermutation(a), Permutations.createPermutation(b));
        PermutationGroup pg2 = PermutationGroup.createPermutationGroup(Permutations.createPermutation(c), Permutations.createPermutation(d));

        PermutationGroup nc = pg1.normalClosureOf(pg2);
        assertTrue(nc.equals(pg2));
    }

    @Test
    public void testNormalClosure1_WithGap() {
        GapGroupsInterface gap = getGapInterface();
        int degree = 157, i = 6, j = 8;
        System.out.println(gap.nrPrimitiveGroups(degree));
        PermutationGroup gr = gap.primitiveGroup(degree, i);
        PermutationGroup sg = gap.primitiveGroup(degree, j);
        System.out.println(gr.order());
        System.out.println(sg.order());
        System.out.println(gr.normalClosureOf(sg).order());
    }

    @Test
    public void testDerivedSubgroup1_WithGap() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 4; degree < 50; degree += 5) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                PermutationGroup group = gap.primitiveGroup(degree, i);
                PermutationGroup d = group.derivedSubgroup();
                gap.evaluateRedberryGroup("rc", d.generators());
                gap.evaluate("gc:= CommutatorSubgroup(g, g);");
                assertTrue(gap.evaluateToBoolean("gc = rc;"));
            }
        }
    }

    @Test
    public void testDerivedSubgroup1_WithGap_longtest() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 4; degree < 50; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                PermutationGroup group = gap.primitiveGroup(degree, i);
                PermutationGroup d = group.derivedSubgroup();
                gap.evaluateRedberryGroup("rc", d.generators());
                gap.evaluate("gc:= CommutatorSubgroup(g, g);");
                assertTrue(gap.evaluateToBoolean("gc = rc;"));
            }
        }
    }

    @Test
    public void testDerivedSubgroup2() {
        PermutationGroup alt = PermutationGroup.alternatingGroup(4);
        assertEquals(BigInteger.valueOf(4), alt.derivedSubgroup().order());
    }

    @Test
    public void testDerivedSubgroup3() {
        PermutationGroup s4 = PermutationGroup.symmetricGroup(4);
        PermutationGroup a4 = s4.derivedSubgroup();
        assertTrue(a4.isAlternating());
        //Klein4
        PermutationGroup v4 = a4.derivedSubgroup();
        int[][] a = {{0, 1}, {2, 3}};
        int[][] b = {{0, 2}, {1, 3}};
        int[][] c = {{0, 3}, {1, 2}};
        PermutationGroup expected = PermutationGroup.createPermutationGroup(Permutations.createPermutation(a), Permutations.createPermutation(b), Permutations.createPermutation(c));
        assertTrue(expected.equals(v4));
    }

    @Test
    public void testIsSymOrAlt1() {
        for (int degree = 3; degree < 100; ++degree) {
            PermutationGroup p = PermutationGroup.symmetricGroup(degree);
            assertTrue(p.isSymmetric());
            assertFalse(p.isAlternating());
            p = PermutationGroup.alternatingGroup(degree);
            assertTrue(p.isAlternating());
            assertFalse(p.isSymmetric());
        }
    }

    @Test
    public void testIsSymOrAlt2_WithGap() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 3; degree < 150; degree += 5) {
            for (int i = 0; i < gap.nrPrimitiveGroups(degree); i += 2) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                PermutationGroup p = gap.primitiveGroup(degree, i);
                assertEquals(gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);"), p.isSymmetric());
                assertEquals(gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);"), p.isAlternating());
            }
        }
    }

    @Test
    public void testIsSymOrAlt2a_WithGap() {
        GapGroupsInterface gap = getGapInterface();
        gap.evaluate("g:= PrimitiveGroup(128, 7);");
        PermutationGroup p = gap.primitiveGroup(128, 6);
        assertEquals(gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);"), p.isSymmetric());
        assertEquals(gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);"), p.isAlternating());
    }

    @Test
    public void testIsSymOrAlt2_WithGap_longtest() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 3; degree < 150; ++degree) {
            for (int i = 0; i < gap.nrPrimitiveGroups(degree); ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                PermutationGroup p = gap.primitiveGroup(degree, i);
                assertEquals(gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);"), p.isSymmetric());
                assertEquals(gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);"), p.isAlternating());
            }
        }
    }

    @Test
    public void testSortOrbitsLengths1() throws Exception {
        final int[] orbits = {
                0,
                1, 1,
                2, 2, 2,
                3, 3, 3, 3,
                4, 4, 4, 4, 4,
                5, 5, 5, 5, 5, 5,
                6, 6, 6, 6, 6, 6, 6,
                7, 7, 7, 7, 7, 7, 7, 7
        };
        Permutations.shuffle(orbits);
        final int[] sizes = {1, 2, 4, 5, 6, 7, 8, 9};
        IntComparator comparator = new IntComparator() {
            @Override
            public int compare(int a, int b) {
                if (orbits[a] == orbits[b])
                    return 0;
                return -Integer.compare(sizes[orbits[a]], sizes[orbits[b]]);
            }
        };
        int[] arr = new int[orbits.length];
        for (int i = 1; i < arr.length; ++i)
            arr[i] = i;
        ArraysUtils.quickSort(arr, comparator);
        for (int i = 1; i < arr.length; ++i)
            assertTrue(sizes[orbits[arr[i]]] <= sizes[orbits[arr[i - 1]]]);
    }

    @Test
    public void testCentralizer1() throws Exception {
        PermutationGroup s4 = PermutationGroup.symmetricGroup(4);

        PermutationGroup s2 = PermutationGroup.createPermutationGroup(Permutations.createPermutation(1, 0));
        PermutationGroup v4 = s4.centralizerOf(s2);
        PermutationGroup expected = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(1, 0, 2, 3),
                Permutations.createPermutation(0, 1, 3, 2)
        );
        assertTrue(expected.equals(v4));
    }

    @Test
    public void testCentralizer2() throws Exception {
        PermutationGroup a8 = PermutationGroup.alternatingGroup(8);
        int[][] p1 = {{0, 1, 2}, {3, 4, 5}};
        PermutationGroup c = a8.centralizerOf(Permutations.createPermutation(p1));
        int[][] p2 = {{3, 4, 5}};
        int[][] p3 = {{0, 3}, {1, 4}, {2, 5}, {6, 7}};
        PermutationGroup expected = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(p1),
                Permutations.createPermutation(p2),
                Permutations.createPermutation(p3));
        assertTrue(expected.equals(c));
        assertTrue(c.center().equals(
                PermutationGroup.createPermutationGroup(Permutations.createPermutation(p1))));
        int[][] p4 = {{0, 2, 1}, {3, 4, 5}};
        assertTrue(c.derivedSubgroup().equals(
                PermutationGroup.createPermutationGroup(Permutations.createPermutation(p4))));
    }

    @Test
    public void testCenter_WithGap() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 3; degree < 50; ++degree) {
            for (int i = 0; i < gap.nrPrimitiveGroups(degree); ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                PermutationGroup p = gap.primitiveGroup(degree, i);
                PermutationGroup center = p.center();
                assertEquals(gap.evaluateToBigInteger("Order(Centre(g));"), center.order());
            }
        }
    }

    @Test
    public void testExample1() throws Exception {
        //Construct permutation group of degree 13 with two generators written in one-line notation
        PermutationGroup pg = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(9, 1, 2, 0, 4, 8, 5, 11, 6, 3, 10, 12, 7),
                Permutations.createPermutation(2, 0, 1, 8, 3, 5, 7, 11, 4, 12, 9, 6, 10));
        //this group is transitive
        assert pg.isTransitive();
        //its order = 5616
        System.out.println(pg.order());

        //Create alternating group Alt(13)
        PermutationGroup alt13 = PermutationGroup.alternatingGroup(13);
        //its order = 3113510400
        System.out.println(alt13.order());
        assert alt13.containsSubgroup(pg);

        //Direct product of two groups
        PermutationGroup pp = pg.directProduct(PermutationGroup.symmetricGroup(8));

        //Setwise stabilizer
        PermutationGroup sw = pp.setwiseStabilizer(1, 2, 3, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18);
        assert pp.containsSubgroup(sw);
        //its order = 17280
        System.out.println(sw.order());

        //Center of this stabilizer
        PermutationGroup center = sw.center();
        //it is abelian group
        assert center.isAbelian();
        //generators of center
        System.out.println(center.generators());
        //[+{}, +{{19, 20}}, +{{2, 10}, {3, 9}, {6, 8}, {11, 12}}]
        //orbits of center
        int[][] orbits = center.orbits();
        for (int[] orbit : orbits)
            if (orbit.length != 1)
                System.out.print(Arrays.toString(orbit));
        //[2, 10], [3, 9], [6, 8], [11, 12], [19, 20]
    }

    @Test
    public void testExample2() {
        PermutationGroup group = PermutationGroup.symmetricGroup(4);
        PermutationGroup subgroup = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(1, 0, 2, 3),
                Permutations.createPermutation(0, 1, 3, 2));
        Permutation[] cosetRepresentatives = group.leftCosetRepresentatives(subgroup);
        assert cosetRepresentatives.length == group.order().divide(subgroup.order()).intValue();
        System.out.println(Arrays.toString(cosetRepresentatives));
    }

    @Test
    public void testExample3() {
        Permutation perm1 = Permutations.createPermutation(new int[][]{{1, 2, 3}});
        Permutation perm2 = Permutations.createPermutation(new int[][]{{3, 4, 5, 6, 7}});
        PermutationGroup pg = PermutationGroup.createPermutationGroup(perm1, perm2);

        BacktrackSearch mappings = pg.mapping(new int[]{7, 2, 1, 3}, new int[]{5, 3, 6, 1});
        Permutation perm;
        while ((perm = mappings.take()) != null)
            System.out.println(perm);
    }

    @Test
    public void testPointwiseStabilizerRestricted1() throws Exception {
        Permutation perm1 = Permutations.createPermutation(new int[][]{{1, 2, 3}});
        Permutation perm2 = Permutations.createPermutation(new int[][]{{3, 4, 5, 6, 7}});
        PermutationGroup pg = PermutationGroup.createPermutationGroup(perm1, perm2);
        PermutationGroup ps = pg.pointwiseStabilizer(1, 2, 3);
        PermutationGroup psr = pg.pointwiseStabilizerRestricted(1, 2, 3);
        assertEquals(ps.order(), psr.order());
        assertTrue(AlgorithmsBase.isBSGS(psr.getBSGS()));
    }

    @Test
    public void testPointwiseStabilizerRestricted2_WithGap() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 4; degree < 50; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            //System.out.println("DEGREE: " + degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                PermutationGroup group = gap.primitiveGroup(degree, i);
                int[] set = new int[degree / 3];
                for (int k = 0; k < set.length; ++k)
                    set[k] = CC.getRandomGenerator().nextInt(degree);

                PermutationGroup ps = group.pointwiseStabilizer(set);
                PermutationGroup psr = group.pointwiseStabilizerRestricted(set);
                assertTrue(AlgorithmsBase.isBSGS(psr.getBSGS()));
                assertTrue(psr.degree() <= (degree - MathUtils.getSortedDistinct(set).length));
                assertEquals(ps.order(), psr.order());

                if (doLongTest()) {
                    if (ps.order().compareTo(BigInteger.valueOf(1_000_000)) > 0)
                        continue;
                    gap.evaluateRedberryGroup("ps", ps.generators());
                    gap.evaluateRedberryGroup("psr", psr.generators());
                    assertTrue(!gap.evaluate("IsomorphismGroups(ps, psr)").contains("fail"));
                }
            }
        }
    }

    @Test
    public void testPointwiseStabilizerRestricted2() throws Exception {
        Permutation[] generators = {
                Permutations.createPermutation(new int[][]{{1, 12, 6, 5, 14}, {2, 7, 9, 8, 4}, {3, 11, 15, 13, 10}}),
                Permutations.createPermutation(new int[][]{{1, 4}, {2, 15}, {3, 11}, {6, 14}, {7, 10}, {9, 12}}),
                Permutations.createPermutation(new int[][]{{0, 1}, {2, 3}, {4, 5}, {6, 7}, {8, 9}, {10, 11}, {12, 13}, {14, 15}})};
        PermutationGroup pg = PermutationGroup.createPermutationGroup(generators);
        int[] set = {5, 12, 13, 15, 15};
        PermutationGroup ps = pg.pointwiseStabilizer(set);
        assertTrue(AlgorithmsBase.isBSGS(ps.getBSGS()));

        PermutationGroup psr = pg.pointwiseStabilizerRestricted(set);
        assertTrue(AlgorithmsBase.isBSGS(psr.getBSGS()));

        assertEquals(ps.order(), psr.order());
    }

    @Test
    public void testConjugate1() {
        Permutation perm1 = Permutations.createPermutation(new int[][]{{1, 2, 3}});
        Permutation perm2 = Permutations.createPermutation(new int[][]{{3, 4, 5, 6, 7}});
        PermutationGroup pg = PermutationGroup.createPermutationGroup(perm1, perm2);

        Permutation c = Permutations.createPermutation(1, 0, 2, 3, 4, 5, 6, 7);

        PermutationGroup expected = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(new int[][]{{0, 2, 3}}), perm2);
        assertEquals(expected, pg.conjugate(c));

        c = Permutations.createPermutation(0, 1, 2, 4, 3, 5, 6, 7);
        expected = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(new int[][]{{4, 3, 5, 6, 7}}), perm1);
        assertEquals(expected, pg.conjugate(c));
    }

    @Test
    public void testConjugate2_WithGap() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 4; degree < 50; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                PermutationGroup group = gap.primitiveGroup(degree, i);
                if (group.isSymmetric() || group.isAlternating())
                    continue;
                //calc bsgs
                group.order();
                assertTrue(AlgorithmsBase.isBSGS(group.conjugate(Permutations.createPermutation(
                        Permutations.randomPermutation(degree))).getBSGS()));
            }
        }
    }

    @Test
    public void testCentralizer() {
        PermutationGroup gr = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(new int[][]{{0, 1, 4, 5}}),
                Permutations.createPermutation(new int[][]{{3, 5, 9, 7}}),
                Permutations.createPermutation(new int[][]{{11, 2, 9, 10}}));

        PermutationGroup c = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(new int[][]{{3, 4, 5}}));

        //todo test with GAP
        Assert.assertEquals(15120, gr.centralizerOf(c).order().intValue());
    }

    private static PermutationGroup pointWiseStabilizerBruteForce(PermutationGroup pg, int[] points) {
        PermutationGroup stab = pg;
        for (int i : points)
            stab = stab.pointwiseStabilizer(i);
        return stab;
    }

    @Test
    public void testUniformity1() throws Exception {
        RandomGenerator rg = new Well19937c(123234L);

        testUniformity(PermutationGroup.createPermutationGroup(
                        Permutations.createPermutation(2, 0, 1, 3, 4)),
                rg);
        testUniformity(PermutationGroup.createPermutationGroup(
                        Permutations.createPermutation(1, 2, 0, 4, 3),
                        Permutations.createPermutation(0, 1, 2, 4, 3)),
                rg);
    }

    @Test
    public void test3() {
        int[][] perm1 = {
                {1, 28, 5, 3, 13, 25, 8, 4, 17, 11, 29, 7, 2, 21, 23, 10, 6},
                {9, 16, 22, 30, 18, 15, 20, 19, 14}};
        int[][] perm2 = {{1, 4, 27}, {2, 5, 3}};
        PermutationGroup group = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(perm1),
                Permutations.createPermutation(perm2));
        Assert.assertEquals(group.order(), new BigInteger("28810681675776000"));

        Assert.assertFalse(group.isSymmetric());
        Assert.assertFalse(group.isAlternating());
        Assert.assertFalse(group.isAbelian());


        int[] o = group.orbit(20);
        int[] e = {9, 16, 22, 30, 18, 15, 20, 19, 14};
        Arrays.sort(o);
        Arrays.sort(e);
        Assert.assertArrayEquals(e, o);


        PermutationGroup pStabilizer = group.pointwiseStabilizer(3, 27, 25);
        Assert.assertFalse(pStabilizer.isAbelian());
        Assert.assertEquals(pStabilizer.order(), new BigInteger("5884534656000"));
        //println pStabilizer


        Permutation p = Permutations.createPermutation(new int[][]{{1, 28, 2, 8, 27, 10, 11}, {21, 29, 23}});
        Assert.assertTrue(group.membershipTest(p));


        Permutation n = Permutations.createPermutation(new int[][]{{7, 19, 5, 17, 3, 4, 21, 25}});
        Assert.assertFalse(group.membershipTest(n));

        PermutationGroup sStabilizer = group.setwiseStabilizer(3, 27, 25);
        Assert.assertFalse(sStabilizer.isAbelian());
        //println sStabilizer

        Assert.assertEquals(sStabilizer.order(), new BigInteger("35307207936000"));


        PermutationGroup dSubgroup = group.derivedSubgroup();
        Assert.assertFalse(dSubgroup.isSymmetric());
        Assert.assertFalse(dSubgroup.isAlternating());
        Assert.assertFalse(dSubgroup.isAbelian());
        Assert.assertEquals(dSubgroup.order(), new BigInteger("3201186852864000"));

        int[][] perm3 = {{1, 17, 12, 19}, {23, 25}};
        int[][] perm4 = {{4, 14, 26}};
        PermutationGroup oth = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(perm3),
                Permutations.createPermutation(perm4));

        Assert.assertFalse(group.containsSubgroup(oth));

        PermutationGroup union = group.union(oth);
        Assert.assertTrue(union.containsSubgroup(group));
        Assert.assertTrue(union.containsSubgroup(oth));

        Assert.assertEquals(union.order(), new BigInteger("4420880996869850977271808000000"));
        Assert.assertFalse(union.isSymmetric());
        Assert.assertFalse(union.isAlternating());

        PermutationGroup intersection = union.intersection(group);
        Assert.assertEquals(group, intersection);
    }

    @Test
    public void testIntersection2() {
        int[][] perm1 = {
                {1, 28, 5, 3, 13, 25, 8, 4, 17, 11, 29, 7, 2, 21, 23, 10, 6},
                {9, 16, 22, 30, 18, 15, 20, 19, 14}};
        int[][] perm2 = {{1, 4, 27}, {2, 5, 3}};
        PermutationGroup group = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(perm1),
                Permutations.createPermutation(perm2));
        //build bsgs
        group.order();

        int[][] perm3 = {{1, 17, 12, 19}, {23, 25}};
        int[][] perm4 = {{4, 14, 26}};
        PermutationGroup oth = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(perm3),
                Permutations.createPermutation(perm4));
        //build bsgs
        oth.order();


        PermutationGroup union = group.union(oth);
        //build bsgs
        union.order();
        PermutationGroup intersection = union.intersection(group);
        Assert.assertEquals(group, intersection);
    }

    @Test
    public void testIntersection3() {
        int[][] perm1 = {
                {1, 28, 5, 3, 13, 25, 8, 4, 17, 11, 29, 7, 2, 21, 23, 10, 6},
                {9, 16, 22, 30, 18, 15, 20, 19, 14}};
        int[][] perm2 = {{1, 4, 27}, {2, 5, 3}};
        PermutationGroup group = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(perm1),
                Permutations.createPermutation(perm2));
        //build bsgs
        group.order();

        int[][] perm3 = {{1, 17, 12, 19}, {23, 25}};
        int[][] perm4 = {{4, 14, 26}};
        PermutationGroup oth = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(perm3),
                Permutations.createPermutation(perm4));
        //build bsgs
        oth.order();


        PermutationGroup union = group.union(oth);
        //build bsgs
        union.order();
        PermutationGroup intersection = union.intersection(group);
        Assert.assertEquals(group, intersection);
    }

    @Test
    public void testIntersection4() {
int[][] perm1 = {{0, 1}};
int[][] perm2 = {{2, 3}};
int[][] perm3 = {{0, 1, 3}};
int[][] perm4 = {{2, 4, 5, 6}};

Permutation p1 = Permutations.createPermutation(perm1);
Permutation p2 = Permutations.createPermutation(perm2);
Permutation p3 = Permutations.createPermutation(perm3);
Permutation p4 = Permutations.createPermutation(perm4);
PermutationGroup group = PermutationGroup.createPermutationGroup(p1, p2);
PermutationGroup oth = PermutationGroup.createPermutationGroup(p3, p4);

Assert.assertTrue(oth.intersection(group).isTrivial());
        Assert.assertTrue(group.intersection(oth).isTrivial());
        System.out.println(oth.membershipTest(p1));
    }

    private void testUniformity(PermutationGroup group,
                                RandomGenerator randomGenerator) {
        if (group.order().compareTo(BigInteger.valueOf(200)) > 0)
            throw new IllegalArgumentException("Group is too big.");

        Map<Permutation, Integer> counts = new HashMap<>();

        int N = doLongTest() ? 100000 : 10000;

        int k = group.order().intValue();

        Permutation p;
        for (int i = 0; i < N; ++i) {
            p = group.randomElement(randomGenerator);

            assertTrue(group.membershipTest(p));

            Integer value = counts.get(p);
            if (value == null)
                counts.put(p, 1);
            else
                counts.put(p, 1 + value);
        }

        //Calculating chiSqare value

        double chiSq = 0.0, s;
        double m = 1.0 * N / k;

        for (Map.Entry<Permutation, Integer> e : counts.entrySet()) {
            s = (e.getValue() - m);
            chiSq += s * s;
        }

        chiSq /= m;

        ChiSquaredDistribution distribution = new ChiSquaredDistribution(k - 1);

        for (Map.Entry<Permutation, Integer> e : counts.entrySet()) {
            System.out.println("" + e.getKey() + ": " + e.getValue());
        }

        double pValue = distribution.cumulativeProbability(chiSq);

        // Can't reject uniformity hypothesis
        assertTrue(pValue < 0.99);
    }
}
