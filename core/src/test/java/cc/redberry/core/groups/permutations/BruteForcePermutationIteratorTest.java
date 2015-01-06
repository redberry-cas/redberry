/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
@Ignore
public class BruteForcePermutationIteratorTest {
    @Test
    public void testInfLoop() {
        List<Permutation> permutations = new ArrayList<>();
        for (int tt = 0; tt < 1000; ++tt) {
            permutations.clear();
            for (int i = 0; i < 10; ++i) {
                Permutation p = null;
                try {
                    p = Permutations.createPermutation(i % 2 == 0 ? true : false, Permutations.randomPermutation(10));
                } catch (Exception e) {
                }
                if (p != null)
                    permutations.add(p);
            }

            if (permutations.isEmpty())
                continue;

            try {
                System.out.println("\n\n\n");
                for (Permutation p : permutations) {
                    String s = p.toString();
                    s = s.substring(2, s.length() - 1);
                    s = "permutations.add ( new Permutation(" + p.antisymmetry() + ", " + s + "));";
                    System.out.println(s);
                }
                BruteForcePermutationIterator bf = new BruteForcePermutationIterator(new ArrayList<>(permutations));
                while (bf.hasNext())
                    bf.next();
            } catch (Exception ex) {
                continue;
            }
        }
    }

    @Ignore//this test fails onto infinite loop ??
    @Test
    public void testInfLoop2() {
        List<Permutation> permutations = new ArrayList<>();
        permutations.add(Permutations.createPermutation(true, 1, 3, 9, 4, 7, 6, 2, 0, 8, 5));
        permutations.add(Permutations.createPermutation(false, 3, 8, 4, 6, 9, 1, 2, 0, 5, 7));
        permutations.add(Permutations.createPermutation(false, 9, 1, 6, 3, 8, 2, 4, 5, 7, 0));
        permutations.add(Permutations.createPermutation(false, 5, 7, 8, 1, 9, 6, 4, 3, 0, 2));
        permutations.add(Permutations.createPermutation(true, 5, 9, 3, 0, 8, 4, 1, 6, 7, 2));
        permutations.add(Permutations.createPermutation(false, 7, 0, 6, 2, 1, 3, 9, 4, 5, 8));
        permutations.add(Permutations.createPermutation(false, 0, 5, 8, 6, 7, 2, 9, 4, 1, 3));

        BruteForcePermutationIterator bf = new BruteForcePermutationIterator(new ArrayList<>(permutations));
        List<Permutation> all = new ArrayList<>(4628800);
        int tr = 0;
//        System.out.println(NumberUtils.factorial(10));
        System.out.println(4628800 / 10000);
        int cc = 0;
        int counter = 0;
        while (bf.hasNext() && tr++ < 4628800) {
            all.add(bf.next());
            counter++;
            if (counter == 10000) {
                System.out.println(cc++);
                counter = 0;
            }
        }
        Collections.sort(all, BruteForcePermutationIterator.JUST_PERMUTATION_COMPARATOR);
        for (Permutation pp : all.subList(0, 15)) {
            System.out.println(pp);
        }
    }
}
