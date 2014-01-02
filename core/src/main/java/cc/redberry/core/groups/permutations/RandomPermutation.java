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
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937a;

import java.util.List;

/**
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
     * by corresponding set of generators. This method is a variant of PREINTIALIZE described
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
     * by corresponding set of generators. This method is a variant of PREINTIALIZE described
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
