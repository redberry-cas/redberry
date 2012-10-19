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
package cc.redberry.core.combinatorics.symmetries;

import cc.redberry.core.combinatorics.InconsistentGeneratorsException;
import cc.redberry.core.combinatorics.IntPermutationsGenerator;
import cc.redberry.core.combinatorics.Symmetry;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SymmetriesImplTest {

    public SymmetriesImplTest() {
    }

    @Test(expected = InconsistentGeneratorsException.class)
    public void testAdd1() {
        SymmetriesImpl symmetries = new SymmetriesImpl(4);
        //This is inconsistent symmetry: there is N such that a^N = 1 with 
        //signum = true, but identity permutation can not change sign
        Symmetry a = new Symmetry(new int[]{2, 1, 3, 0}, true);
        //add will throw exception
        symmetries.add(a);
    }

    @Test(expected = InconsistentGeneratorsException.class)
    public void testAdd2() {
        SymmetriesImpl symmetries = new SymmetriesImpl(4);
        //This is consistent symmetry
        Symmetry a = new Symmetry(new int[]{2, 1, 3, 0}, false);
        //This is consistent symmetry to, but it is insonsistent with a: there 
        //is some way to multiply a*b*a.... = {0,2,3,1} :false and other way, that 
        // gives a*b*b*... = {0,2,3,1} : true
        Symmetry b = new Symmetry(new int[]{2, 3, 0, 1}, true);
        //add will throw exception
        symmetries.add(a);
        symmetries.add(b);
    }

    @Test
    public void testIterator1() {
        //In this method we checking well known theorem that one cycle and one 
        //transposition generates all combinatorics. We building all combinatorics
        //dimension 4
        SymmetriesImpl symmetries = new SymmetriesImpl(4);
        //Sycle permutation
        Symmetry a = new Symmetry(new int[]{3, 0, 1, 2}, false);
        //Transposition 
        Symmetry b = new Symmetry(new int[]{1, 0, 2, 3}, false);
        symmetries.add(a);
        symmetries.add(b);

        //Creating all combinatorics by s.iterator() wich works with
        //PermutationsSpanIterator               
        Symmetry[] arr1 = new Symmetry[24];//24 = 4!        
        int i = 0;
        for (Symmetry s : symmetries)
            arr1[i++] = s;

        //Creating all combinatorics by using IntPermutationsGenerator
        Symmetry[] arr2 = new Symmetry[24];
        IntPermutationsGenerator ig = new IntPermutationsGenerator(4);
        i = 0;
        while (ig.hasNext()) {
            int[] n = Arrays.copyOf(ig.next(), 4); //for more see IntPermutationsGenerator specification
            arr2[i++] = new Symmetry(n, false);
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
        SymmetriesImpl symmetries = new SymmetriesImpl(3);
        //Sycle permutation
        Symmetry a = new Symmetry(new int[]{2, 0, 1}, false);
        //Transposition witch is antysimmetry
        Symmetry b = new Symmetry(new int[]{1, 0, 2}, true);
        symmetries.add(a);
        symmetries.add(b);

        //Creating all combinatorics by s.iterator() wich works with
        //PermutationsSpanIterator               
        Symmetry[] arr = new Symmetry[6];//6 = 3!        
        int i = 0;
        for (Symmetry s : symmetries)
            arr[i++] = s;


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
    
   
}
