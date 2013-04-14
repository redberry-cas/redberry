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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cc.redberry.core.groups.permutations.PermutationGroup.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationGroupTest {

    @Test
    public void testSchreierVector1() {
        int[] p1 = {1, 0, 2, 3, 4};
        int[] p2 = {0, 1, 3, 2, 4};
        int[] p3 = {0, 3, 1, 2, 4};
        int[] generators[] = {p1, p2, p3};

        int[] schreier = calculateSchreierVector(generators, 0);
        System.out.println(Arrays.toString(schreier));


        System.out.println(
                Arrays.toString(
                        decomposeSchreierVectorSequence(generators, schreier, 0)));


        System.out.println(
                Arrays.toString(
                        decomposeSchreierVectorSequence(generators, schreier, 1)));

        System.out.println(
                Arrays.toString(
                        decomposeSchreierVector(generators, schreier, 1)));


        System.out.println(
                Arrays.toString(
                        decomposeSchreierVectorSequence(generators, schreier, 2)));


        System.out.println(
                Arrays.toString(
                        decomposeSchreierVector(generators, schreier, 2)));


    }


    @Test
    public void test1() {
        int[] p1 = {1, 0, 2, 3, 4};
        int[] p2 = {0, 1, 3, 2, 4};
        int[] p3 = {0, 3, 1, 2, 4};
        int[] generators[] = {p1, p2, p3};

        OrbitStabilizer os = calculateOrbitStabilizer(generators, 0);

        for (int[] gen : os.stabilizerGenerators)
            System.out.println(Arrays.toString(gen));
    }

    @Test
    public void test2() {
        int[] p1 = {1, 0, 2, 3, 4};
        int[] p2 = {1, 2, 3, 4, 0};
        int[] generators[] = {p1, p2};

        int[] schreier = calculateSchreierVector(generators, 2);
        System.out.println(Arrays.toString(schreier));

        OrbitStabilizer os = calculateOrbitStabilizer(generators, 2);

        for (int[] gen : os.stabilizerGenerators)
            System.out.println(Arrays.toString(gen));
    }

}
