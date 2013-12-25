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
import cc.redberry.core.number.NumberUtils;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well1024a;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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

        b = new PermutationOneLine(1, 0, 2, 3, 4, 5);
        c = new PermutationOneLine(1, 2, 3, 4, 5, 0);

        PermutationGroup pg = PermutationGroupFactory.create(b, c);
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

        PermutationGroup pg = PermutationGroupFactory.create(b, c);
        assertEquals(24, pg.order().intValue());
    }

    @Test
    public void testIterator1() {
        Permutation b, c;

        b = new PermutationOneLine(1, 0, 2, 3, 4, 5);
        c = new PermutationOneLine(1, 2, 3, 4, 5, 0);

        PermutationGroup pg = PermutationGroupFactory.create(b, c);
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

        PermutationGroup pg = PermutationGroupFactory.create(b, c);
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

        PermutationGroup pg = PermutationGroupFactory.create(b, c);
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

        PermutationGroup pg = PermutationGroupFactory.create(b, c);
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

        PermutationGroup pg = PermutationGroupFactory.create(b, c);
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
                PermutationGroupFactory.create(permutations.toArray(new Permutation[0]));
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
        group = PermutationGroupFactory.create(generators);
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
        group = PermutationGroupFactory.create(generators);
        assertEquals(group.order().longValue(), 5040L);
        assertGroupIterator(group);

        b = new PermutationOneLine(1, 2, 3, 4, 5, 6, 0);
        a = new PermutationOneLine(1, 0, 2, 3, 4, 5, 6);
        c = new PermutationOneLine(1, 2, 3, 5, 6, 4, 0);
        d = new PermutationOneLine(1, 4, 3, 2, 5, 6, 0);
        generators = new Permutation[]{a, b, c, d};
        group = PermutationGroupFactory.create(generators);
        assertEquals(group.order().longValue(), 5040L);
        assertGroupIterator(group);

        b = new PermutationOneLine(1, 2, 3, 4, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        a = new PermutationOneLine(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        c = new PermutationOneLine(1, 2, 3, 5, 6, 4, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        d = new PermutationOneLine(1, 4, 3, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        e = new PermutationOneLine(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 18);
        generators = new Permutation[]{a, b, c, d, e};
        group = PermutationGroupFactory.create(generators);
        assertEquals(group.order().longValue(), 10080L);
        assertGroupIterator(group);

        b = new PermutationOneLine(1, 2, 3, 4, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        a = new PermutationOneLine(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        c = new PermutationOneLine(1, 2, 3, 5, 6, 4, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        d = new PermutationOneLine(1, 4, 3, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        e = new PermutationOneLine(1, 0, 2, 3, 14, 5, 6, 7, 8, 9, 10, 11, 12, 13, 4, 15, 16, 17, 18, 19);
        generators = new Permutation[]{a, b, c, d, e};
        group = PermutationGroupFactory.create(generators);
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
        PermutationGroup id = PermutationGroupFactory.create(Permutations.getIdentityOneLine(10));

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
        group = PermutationGroupFactory.create(generators);

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
    public void testPointwiseStabilizer3_WithGap() {
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

    @Test
    public void testSetwiseStabilasizer1_WithGap() {
        PermutationGroup group = getGapInterface().primitiveGroup(12, 0);

        PermutationGroup stab = group.setwiseStabilizer(0, 1, 2);
        System.out.println(stab.order());
        System.out.println(group.order().divide(stab.order()));

    }

    @Test
    public void testAlt1() {
        //n = 5
        PermutationOneLine a1 = new PermutationOneLine(new int[]{1, 2, 0, 3, 4, 5, 6});
        PermutationOneLine a2 = new PermutationOneLine(new int[]{1, 2, 3, 4, 5, 6, 0});
        PermutationGroup pg = PermutationGroupFactory.create(a1, a2);
        assertTrue(pg.isAlternating());
    }

    @Test
    public void testAlt2() {
        //n = 5
        PermutationOneLine a1 = new PermutationOneLine(new int[]{1, 2, 0, 3, 4, 5});
        PermutationOneLine a2 = new PermutationOneLine(new int[]{0, 2, 3, 4, 5, 1});
        PermutationGroup pg = PermutationGroupFactory.create(a1, a2);
        assertTrue(pg.isAlternating());
    }

    @Test
    public void testAlt3() {
        PermutationGroup alt = PermutationGroupFactory.alternatingGroup(30);
        PermutationGroup sym = PermutationGroupFactory.symmetricGroup(30);
        assertTrue(alt.isAlternating());
        assertTrue(!sym.isAlternating());
        assertTrue(sym.isTransitive());
        assertTrue(sym.isSymmetric());
        assertTrue(sym.isSubgroup(alt));
        Permutation[] tr = sym.leftCosetRepresentatives(alt);
        assertEquals(2, tr.length);
        Arrays.sort(tr);
        assertTrue(tr[0].isIdentity());
        assertTrue(tr[1].parity() == 1);
    }

    @Test
    public void testDirectProduct1() {
        PermutationGroup alt = PermutationGroupFactory.alternatingGroup(3);
        PermutationGroup sym = PermutationGroupFactory.symmetricGroup(4);
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
        PermutationGroup alt = PermutationGroupFactory.alternatingGroup(15);

        PermutationGroup sw1 = alt.setwiseStabilizer(1, 2, 3, 4, 5, 6);
        PermutationGroup sw2 = alt.setwiseStabilizer(4, 5, 6, 7, 8, 9, 10);
        PermutationGroup intr = sw1.intersection(sw2);
        PermutationGroup sw3 = alt.setwiseStabilizer(4, 5, 6);
        assertTrue(sw3.isSubgroup(intr));
    }

    @Test
    public void testDirectProduct2() {
        //Alt(13)
        PermutationGroup alt = PermutationGroupFactory.alternatingGroup(13);
        //setwise stabilizer in Alt(13)
        PermutationGroup altStab = alt.setwiseStabilizer(2, 3, 4);
        assertTrue(alt.isSubgroup(altStab));
        //Sym(14)
        PermutationGroup sym = PermutationGroupFactory.symmetricGroup(14);
        //setwise stabilizer in Sym(14)
        PermutationGroup symStab = sym.setwiseStabilizer(5, 6, 1);
        assertTrue(sym.isSubgroup(symStab));
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
        assertTrue(prStab3.isSubgroup(intersection));
        //pointwise stabilizer
        PermutationGroup prStab4 = pr.pointwiseStabilizer(1, 2, 3, 4, 21, 22, 23);
        //union of subgroups
        PermutationGroup union = prStab1.union(prStab4);
        assertTrue(union.isSubgroup(prStab1));
        assertTrue(union.isSubgroup(prStab4));
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
        PermutationGroup pg = PermutationGroupFactory.alternatingGroup(degree);

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
        final PermutationGroup pg = PermutationGroupFactory.alternatingGroup(degree);
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


    private static PermutationGroup pointWiseStabilizerBruteForce(PermutationGroup pg, int[] points) {
        PermutationGroup stab = pg;
        for (int i : points)
            stab = stab.pointwiseStabilizer(i);
        return stab;
    }
}
