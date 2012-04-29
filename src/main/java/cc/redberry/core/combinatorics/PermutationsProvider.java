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
package cc.redberry.core.combinatorics;

/**
 * In contract with {@link PermutationsGenerator}, this interface postulates 
 * basic functionality for iterators, witch iterates only over a part of 
 * combinatorics, in following way.
 * 
 * <p>Let we have sequence {0,1,2,3,4,5,6,7,8} and we want iterate over 
 * combinatorics of elements {1,2,3} and {7,8} (<i>target positions</i>).
 * Permutations of each group are performing by its own iterator 
 * (<i>disjoint provider</i>). So in this example method 
 * {@link #getDisjointProviders()} returns array with length 2.
 * 
 * <p>Method {@link #allPermutations()} returns {@code Iterable<Permutations>} 
 * which iterates over specified combinatorics of array of target positions. For
 * clearly understanding see next example.
 * 
 * <p>Example
 * <blockquote><pre>
 *       //constructs provider witch permutes {1,2,3}
 *       SimplePermutationProvider a = new SimplePermutationProvider(1,4); 
 *       //constructs provider witch permutes {1,2,3}
 *       SimplePermutationProvider b = new SimplePermutationProvider(7,9);
 *       //constructs provider over {1,2,3} and {7,8}
 *       PermutationsProvider provider = new PermutationsProviderImpl(a,b);
 *       System.out.println("Target positions: "+Arrays.toString(provider.targetPositions()));
 *       for(Permutation p: provider.allPermutations())
 *           System.out.println("Permutation:      "+p); 
 *      </pre></blockquote>
 * <p>The result will be 
 * <blockquote><pre>
 *  Target positions: [1, 2, 3, 7, 8]
 *  Permutation:      [0, 1, 2, 3, 4]
 *  Permutation:      [0, 1, 2, 4, 3]
 *  Permutation:      [0, 2, 1, 3, 4]
 *  Permutation:      [0, 2, 1, 4, 3]
 *  Permutation:      [1, 0, 2, 3, 4]
 *  Permutation:      [1, 0, 2, 4, 3]
 *  Permutation:      [1, 2, 0, 3, 4]
 *  Permutation:      [1, 2, 0, 4, 3]
 *  Permutation:      [2, 0, 1, 3, 4]
 *  Permutation:      [2, 0, 1, 4, 3]
 *  Permutation:      [2, 1, 0, 3, 4]
 *  Permutation:      [2, 1, 0, 4, 3]
 * </pre></blockquote>
 * 
 * <p> In this example it is clearly seen, that iterating goes over specified 
 * combinatorics over array of target indices. Each permutation permutes positions
 * of target indices, and for iterating directly over target indices see wrapper 
 * class {@link PermutationsProviderWrapper}.
 * 
 * <p>One of the useful using of {@code PermutationProvider} is to compare two 
 * arrays ({@code arr1} and {@code arr2}). Let we have two arrays of 
 * objects,  sorted, for example, by their hash code. First we shall compare them hash
 * code arrays. Let that corresponding hash arrays are equals and looks like
 * {1,3,5,6,6,6,7,9,11,11}. After comparing hash arrays we shall iterate over 
 * arrays {@code arr1} and {@code arr2} and consecutively compare corresponding 
 * elements. For elements with equals hash code we must try <i>all 
 * combinatorics</i>. In our example we must permute elements with hash code 6
 * and 11. We can construct {@code PermutationProvider} with target positions 
 * {3,4,5,8,9} and use it to provide such combinatorics. Illustration code below:
 *
 * <blockquote><pre> 
 * public PermutationProvider generateProvider(final Object[] array) // array is sorted by hashCode
 * {
 *        int begin = 0;
 *        int i;
 *        List<PermutationsProvider> disjointProviders = new ArrayList<>();
 *        for (i = 1; i < array.length; ++i)
 *           if (i == size() || array[i].hash() != array[i-1].hash()) {
 *                if (i - 1 != begin)
 *                     disjointProviders.add(new SimplePermutationProvider(begin, i));
 *               begin = i;
 *               }
 *       return new PermutationsProviderImpl(disjointProviders);
 * }
 * 
 * public boolean compareArrays(final Object[] array1, final Object[] array2) // arrays are sorted by hashCode
 * {
 *       int size;
 *       if ((size = array1.length) != array2.length)
 *           return false;
 *       // if arrays are equals their providers are equals to
 *       PermutationsProvider provider = generateProvider(array1);
 *       int[] nonPermutablePositions = PermutationsProvider.Util.getNonpermutablePositions(size, provider);
 *       for (int i : nonPermutablePositions)
 *           if (!array1[i].equals(array2[i]))
 *               return false;
 *       int[] targetPositions = provider.targetPositions();
 *       out_for:
 *       for (Permutation permutation : provider.allPermutations()) {
 *           for (int i = 0; i < targetPositions.length; ++i)
 *               if (!array1[targetPositions[i]].equals(array2[targetPositions[permutation.newIndexOf(i)]]))
 *                   continue out_for;
 *           return true;
 *       }
 *       return false;
 *   }
 * </pre></blockquote>
 *
 * 
 * <p><b>NOTE</b>: Implementations are not obliged to look after disjoint 
 * providers, so them target positions are really differs (disjoint). 
 * For example, {@code PermutationsProviderImpl} allows for disjoint providers
 * to have parts with same target positions.
 * 
 * <p>For more information see implementations and testing files.
 * 
 * @see SimplePermutationProvider
 * @see PermutationsProviderImpl
 * @see PermutationsProviderWrapper
 * @see EmptyPermutationsProvider
 * @see TensorSortedContentImpl
 * 
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public interface PermutationsProvider {
    /**
     * Returns array of disjoint providers composing this provider.
     * 
     * @return array of disjoint providers composing this provider
     */
    PermutationsProvider[] getDisjointProviders();

    /**
     * Returns array of target positions, i.e. positions witch incurs permuting
     * 
     * @return array of target positions, i.e. positions witch incurs permuting
     */
    int[] targetPositions();

    /**
     * Returns {@code Iterable<Permutation>} over combinatorics over array of
     * target positions.
     * 
     * @return {@code Iterable<Permutation>} over combinatorics over array of
     * target positions
     */
    Iterable<Permutation> allPermutations();

    /**
     * Utility class witch have one method, calculating non permutable positions
     * of specified provider.
     */
    public static class Util {
        private Util() {
        }

        /**
         * Calculates non permutable positions of array with specified length under 
         * specified provider.
         * <p><b>NOTE:</b> this method correctly works only with providers witch
         * disjoint providers, contains strictly different (disjoint) target 
         * positions, otherwise this method throws 
         * {@code InconsistentGeneratorsException}.
         * 
         * @param size size of array, witch non permutable positions under 
         * specified provider must be calculated
         * @param provider combinatorics provider
         * @return array of non permutable positions
         * @throws InconsistentGeneratorsException when provider contains 
         * disjoint providers, witch are actually not disjoint
         */
        public static int[] getNonpermutablePositions(int size, PermutationsProvider provider) {
            boolean[] b = new boolean[size];
            int s = 0;
            for (PermutationsProvider p : provider.getDisjointProviders()) {
                int[] array = p.targetPositions();
                s += array.length;
                for (int i : array) {
                    if (b[i])
                        throw new InconsistentGeneratorsException("Inconsistent disjoint providers.");
                    b[i] = true;
                }
            }
            int[] ret = new int[size - s];
            s = -1;
            for (int i = 0; i < size; ++i)
                if (!b[i])
                    ret[++s] = i;
            return ret;
        }
    }
}
