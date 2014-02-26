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

import cc.redberry.core.context.CC;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937a;

import java.util.List;

/**
 * Algorithms for generating uniform distributed random elements of permutation group.
 * <p><b>Example:</b><p>
 * In the following example we use a two generators, which generates a group of order 5616 and choose 5616 random
 * elements of this group with uniform distribution
 * <pre style="background:#f1f1f1;color:#000"><span style="color:#406040"> //primitive permutation group with 5616 elements</span>
 * <span style="color:#a08000">Permutation</span> perm1 <span style="color:#2060a0">=</span> <span style="color:#2060a0">new</span> <span style="color:#a08000">PermutationOneLineInt</span>(<span style="color:#0080a0">9</span>, <span style="color:#0080a0">1</span>, <span style="color:#0080a0">2</span>, <span style="color:#0080a0">0</span>, <span style="color:#0080a0">4</span>, <span style="color:#0080a0">8</span>, <span style="color:#0080a0">5</span>, <span style="color:#0080a0">11</span>, <span style="color:#0080a0">6</span>, <span style="color:#0080a0">3</span>, <span style="color:#0080a0">10</span>, <span style="color:#0080a0">12</span>, <span style="color:#0080a0">7</span>);
 * <span style="color:#a08000">Permutation</span> perm2 <span style="color:#2060a0">=</span> <span style="color:#2060a0">new</span> <span style="color:#a08000">PermutationOneLineInt</span>(<span style="color:#0080a0">2</span>, <span style="color:#0080a0">0</span>, <span style="color:#0080a0">1</span>, <span style="color:#0080a0">8</span>, <span style="color:#0080a0">3</span>, <span style="color:#0080a0">5</span>, <span style="color:#0080a0">7</span>, <span style="color:#0080a0">11</span>, <span style="color:#0080a0">4</span>, <span style="color:#0080a0">12</span>, <span style="color:#0080a0">9</span>, <span style="color:#0080a0">6</span>, <span style="color:#0080a0">10</span>);
 * <span style="color:#a08000">ArrayList&lt;<span style="color:#a08000">Permutation</span>></span> generators <span style="color:#2060a0">=</span> <span style="color:#2060a0">new</span> <span style="color:#a08000">ArrayList&lt;></span>(<span style="color:#a08000">Arrays</span><span style="color:#2060a0">.</span>asList(perm1, perm2));
 * <span style="color:#406040">//we'll use a list of generators as a source of randomness</span>
 * <span style="color:#406040">//this brings some randomization in generators list</span>
 * <span style="color:#a08000">RandomPermutation</span><span style="color:#2060a0">.</span>randomness(generators);
 * <span style="color:#a08000">Set&lt;<span style="color:#a08000">Permutation</span>></span> set <span style="color:#2060a0">=</span> <span style="color:#2060a0">new</span> <span style="color:#a08000">HashSet&lt;></span>();
 * <span style="color:#a08000">int</span> k <span style="color:#2060a0">=</span> <span style="color:#0080a0">5616</span>; <span style="color:#406040">//equal to order of group</span>
 * <span style="color:#406040">//choosing 5616 random elements</span>
 * <span style="color:#2060a0">for</span> (; k <span style="color:#2060a0">></span> <span style="color:#0080a0">0</span>; <span style="color:#2060a0">--</span>k)
 *     set<span style="color:#2060a0">.</span>add(<span style="color:#a08000">RandomPermutation</span><span style="color:#2060a0">.</span>random(generators));
 * <span style="color:#406040">//uniform</span>
 * <span style="color:#a08000">System</span><span style="color:#2060a0">.</span>out<span style="color:#2060a0">.</span>println(set<span style="color:#2060a0">.</span>size());<span style="color:#406040">//~3500</span>
 * </pre>
 * </p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1.6
 */
public final class RandomPermutation {
    private RandomPermutation() {
    }

    //------------------------------ RANDOM --------------------------------------------//

    /**
     * Default size of random source list of generators
     */
    public static final int DEFAULT_RANDOMNESS_EXTEND_TO_SIZE = 10;
    /**
     * Default size of random refinements
     */
    public static final int DEFAULT_NUMBER_OF_RANDOM_REFINEMENTS = 20;

    /**
     * Brings randomness to a list of generators: the source list will be extended and filled by an equivalent set
     * of generators generated randomly; commonly it should be used in a combination with
     * {@link #random(java.util.List, org.apache.commons.math3.random.RandomGenerator)} to produce almost uniform distributed permutations in a group defined
     * by corresponding set of generators. This method is a variant of PREINITIALIZE described
     * in Sec. 3.2.2 in [Holt05].
     *
     * @param generators a list of generators
     * @see #random(java.util.List, org.apache.commons.math3.random.RandomGenerator)
     */

    public static void randomness(List<Permutation> generators) {
        randomness(generators, DEFAULT_RANDOMNESS_EXTEND_TO_SIZE,
                DEFAULT_NUMBER_OF_RANDOM_REFINEMENTS, CC.getRandomGenerator());
    }

    /**
     * Brings randomness to a list of generators: the source list will be extended and filled by an equivalent set
     * of generators generated randomly; commonly it should be used in a combination with
     * {@link #random(java.util.List, org.apache.commons.math3.random.RandomGenerator)} to produce almost uniform distributed permutations in a group defined
     * by corresponding set of generators. This method is a variant of PREINITIALIZE described
     * in Sec. 3.2.2 in [Holt05].
     *
     * @param generators          a list of generators
     * @param extendToSize        extend specified list to this size with additional (equivalent) random elements
     * @param numberOfRefinements number of invocations of random procedure to refine the randomness
     * @param random              random generator
     */
    public static void randomness(List<Permutation> generators, int extendToSize,
                                  int numberOfRefinements, RandomGenerator random) {
        if (generators.size() < 2 && extendToSize < 2)
            throw new IllegalArgumentException("List should extended by at least one element.");

        if (generators.size() < extendToSize) {
            int delta = extendToSize - generators.size() + 1;
            int i = 0;
            while (--delta >= 0)
                generators.add(generators.get(i++));
        }
        //hold identity for use in PRRANDOM
        if (!generators.get(generators.size() - 1).isIdentity())
            generators.add(generators.get(0).getIdentity());
        while (--numberOfRefinements >= 0)
            random(generators, random);
    }

    /**
     * Produces almost uniformly distributed elements of a group specified by specified generators (only if method
     * {@link #randomness(java.util.List, int, int, org.apache.commons.math3.random.RandomGenerator)} was invoked with specified generators); and brings additional
     * randomness in the specified list. See algorithm PRRANDOM in Sec. 3.2.2 in [Holt05].
     *
     * @param generators generators (method {@link #randomness(java.util.List, int, int, org.apache.commons.math3.random.RandomGenerator)} should be invoked before)
     * @return random element of a group
     */
    public static Permutation random(List<Permutation> generators) {
        return random(generators, CC.getRandomGenerator());
    }

    /**
     * Produces almost uniformly distributed elements of a group specified by specified generators (only if method
     * {@link #randomness(java.util.List, int, int, org.apache.commons.math3.random.RandomGenerator)} was invoked with specified generators); and brings additional
     * randomness in the specified list. See algorithm PRRANDOM in Sec. 3.2.2 in [Holt05].
     *
     * @param generators generators (method {@link #randomness(java.util.List, int, int, org.apache.commons.math3.random.RandomGenerator)} should be invoked before)
     * @param random     random generator
     * @return random element of a group
     */
    public static Permutation random(List<Permutation> generators, RandomGenerator random) {
        if (generators.size() < 3)
            throw new IllegalArgumentException("List size should be >= 3");

        int generatorsSize = generators.size() - 1;
        //do not take last element
        int s = random.nextInt(generatorsSize);
        int t;
        do {
            t = random.nextInt(generatorsSize);
        } while (t == s);

        Permutation ps = generators.get(s), pt = generators.get(t), x0 = generators.get(generatorsSize);
        if (random.nextBoolean())
            pt = pt.inverse();


        if (random.nextBoolean()) {
            generators.set(s, ps = ps.composition(pt));
            generators.set(generatorsSize, x0 = x0.composition(ps));
        } else {
            generators.set(s, ps = pt.composition(ps));
            generators.set(generatorsSize, x0 = ps.composition(x0));
        }

        return x0;
    }
}
