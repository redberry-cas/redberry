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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationsSpanIteratorTest {
    public PermutationsSpanIteratorTest() {
    }

    @Test
    public void testIterator1() {
        //In this method we checking well known theorem that one cycle and one 
        //transposition generates all combinatorics. We building all combinatorics
        //dimension 4

        //Sycle permutation
        Permutation a = new Permutation(new int[]{3, 0, 1, 2});
        //Transposition 
        Permutation b = new Permutation(new int[]{1, 0, 2, 3});
        List<Permutation> permutations = new ArrayList<>();
        permutations.add(a);
        permutations.add(b);
        PermutationsSpanIterator<Permutation> iterator = new PermutationsSpanIterator<>(permutations);

        //Creating all combinatorics
        Permutation[] arr1 = new Permutation[24];//24 = 4!        
        int i = 0;
        while (iterator.hasNext()) {
            Permutation p = iterator.next();
            arr1[i++] = p;
        }

        //Creating all combinatorics by using IntPermutationsGenerator
        Permutation[] arr2 = new Permutation[24];
        IntPermutationsGenerator ig = new IntPermutationsGenerator(4);
        i = 0;
        while (ig.hasNext()) {
            int[] n = Arrays.copyOf(ig.next(), 4);//for more see IntPermutationsGenerator specification
            arr2[i++] = new Permutation(n);
        }//Comparing results
        Arrays.sort(arr1);
        Arrays.sort(arr2);
        assertArrayEquals(arr1, arr2);
    }

    @Test
    public void testIterator2() {
        //In this method we checking well known theorem that one cycle and one 
        //transposition generates all combinatorics. We building symmetries of
        //levi-civita symbol (http://en.wikipedia.org/wiki/Levi-Civita_symbol)
        //with dimension = 3

        //Sycle permutation
        Symmetry a = new Symmetry(new int[]{2, 0, 1}, false);
        //Transposition witch is antysimmetry
        Symmetry b = new Symmetry(new int[]{1, 0, 2}, true);
        List<Symmetry> symmetries = new ArrayList<>();
        symmetries.add(a);
        symmetries.add(b);

        //Creating all combinatorics
        PermutationsSpanIterator<Symmetry> iterator = new PermutationsSpanIterator<>(symmetries);
        Symmetry[] arr = new Symmetry[6];//6 = 3!        
        int i = 0;
        while (iterator.hasNext()) {
            Symmetry s = iterator.next();
            arr[i++] = s;
        }

        //Levi-Civita combinatorics
        Symmetry[] l = new Symmetry[6];
        //Cycles
        l[0] = new Symmetry(new int[]{0, 1, 2}, false);
        l[1] = new Symmetry(new int[]{2, 0, 1}, false);
        l[2] = new Symmetry(new int[]{1, 2, 0}, false);
        //Transpositions
        l[3] = new Symmetry(new int[]{1, 0, 2}, true);
        l[4] = new Symmetry(new int[]{0, 2, 1}, true);
        l[5] = new Symmetry(new int[]{2, 1, 0}, true);

        Arrays.sort(arr);
        Arrays.sort(l);
        assertArrayEquals(arr, l);
    }

    @Test
    public void testIterator3() {
        System.out.println("Testing iterator...");
        //Generating all symmetries of Rieman tensor using generators:
        //R_{abcd}=-R_{abdc}=-R_{bacd} and R_{abcd}=R_{cdab}
        System.out.println("Generating all symmetries of Rieman tensor using generators:\n"
                + "R_{abcd}=-R_{abdc}=-R_{bacd} and R_{abcd}=R_{cdab}");
        Symmetry a = new Symmetry(new int[]{0, 1, 3, 2}, true);
        Symmetry b = new Symmetry(new int[]{2, 3, 0, 1}, false);
        Symmetry c = new Symmetry(new int[]{1, 0, 2, 3}, true);
        List<Symmetry> symmetries = new ArrayList<>();
        symmetries.add(a);
        symmetries.add(b);
        symmetries.add(c);
        PermutationsSpanIterator<Symmetry> iterator = new PermutationsSpanIterator<>(symmetries);
        int i = 0;
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
            i++;
        }
        assertTrue(i == 8);
        System.out.println("End testing.\n");
    }

    @Test(expected = InconsistentGeneratorsException.class)
    public void testInconsistentGenerators1() {
        //This is inconsistent symmetry: there is N such that a^N = 1 with 
        //signum = true, but identity permutation can not change sign
        Symmetry a = new Symmetry(new int[]{2, 1, 3, 0}, true);
        PermutationsSpanIterator<Symmetry> iterator = new PermutationsSpanIterator<>(Collections.singletonList(a));
        //iterating will throw exception
        while (iterator.hasNext())
            iterator.next();
    }

    @Test(expected = InconsistentGeneratorsException.class)
    public void testInconsistentGenerators2() {
        //This is consistent symmetry
        Symmetry a = new Symmetry(new int[]{2, 1, 3, 0}, false);
        //This is consistent symmetry to, but it is insonsistent with a: there 
        //is some way to multiply a*b*a.... = {0,2,3,1} :false and other way, that 
        // gives a*b*b*... = {0,2,3,1} : true
        Symmetry b = new Symmetry(new int[]{2, 3, 0, 1}, true);
        //add will throw exception
        List<Symmetry> list = new ArrayList<>();
        list.add(a);
        list.add(b);
        PermutationsSpanIterator<Symmetry> iterator = new PermutationsSpanIterator<>(list);
        while (iterator.hasNext())
            iterator.next();
    }

    @Test
    public void testTransposition() {
        //This testing checks that transposition generates only identity permutation
        Permutation a = new Symmetry(new int[]{1, 0, 2}, false);
        PermutationsSpanIterator<Permutation> iterator = new PermutationsSpanIterator<>(Collections.singletonList(a));
        List<Permutation> result  = new ArrayList<>();
        while (iterator.hasNext())
            result.add(iterator.next());
        Permutation[] standart = new Permutation[2];
        standart[0] = a;
        standart[1] = a.getOne();
        Permutation[] _result = result.toArray(new Permutation[result.size()]);
        Arrays.sort(_result);
        Arrays.sort(standart);
        assertArrayEquals(standart, _result);
    }
}
