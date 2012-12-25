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
package cc.redberry.core.math.frobenius;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static cc.redberry.core.math.frobenius.FrobeniusUtils.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class FrobeniusSolverTest {

    @Test
    public void test1() {
        int[][] equations = {{1, 1, 1, 2}};
        FrobeniusSolver frobeniusSolver = new FrobeniusSolver(equations);
        int[] solution;
        while ((solution = frobeniusSolver.take()) != null)
            assertFbSystem(equations, solution);
        assertSolutionsCount(6, equations);
    }

    @Test
    public void test2() {
        FrobeniusSolver frobeniusSolver = new FrobeniusSolver(new int[]{17, 1, 2, 5, 1, 2, 9, 1, 49});
        int[] solution;
        long count = 0;
        while ((solution = frobeniusSolver.take()) != null) {
            assertFbEquation(new int[]{17, 1, 2, 5, 1, 2, 9, 1, 49}, solution);
            count++;
        }
    }

    @Test
    public void test4() {
        int[][] equations = {
                {17, 1, 2, 5, 1, 2, 9, 1, 49},
                {17, 1, 2, 5, 1, 2, 9, 1, 48}};
        assertSolutionsCount(0, equations);
    }

    @Test
    public void test5() {
        int[][] equations = {{1, 1, 5}};
        assertSolutionsCount(6, equations);
    }

    @Test
    public void test6() {
        int[][] equations = {{1, 0, 2}, {0, 1, 2}};
        int[][] solutions = {{2, 2}};
        assertFbSystem(solutions, equations);
    }

    @Test
    public void test7() {
        int[][] equations = {{0, 1, 1}, {2, 0, 2}};
        int[][] solutions = {{1, 1}};
        assertFbSystem(solutions, equations);
    }

    @Test
    public void test8() {
        int[][] equations = {{2, 0, 2}, {0, 1, 1}};
        int[][] solutions = {{1, 1}};
        assertFbSystem(solutions, equations);
    }

    @Test
    public void test9() {
        int[][] equations = {{2, 0, 2}, {0, 1, 1}};
        int[][] solutions = {{1, 1}};
        assertFbSystem(solutions, equations);
    }

    @Test
    public void test10() {
        int[][] equations = {{2, 0, 4}, {0, 1, 3}};
        int[][] solutions = {{2, 3}};
        assertFbSystem(solutions, equations);
    }

    @Test
    public void test11() {
        int[][] equations = {
                {2, 0, 0, 4},
                {0, 1, 0, 3},
                {0, 0, 7, 21}};
        int[][] solutions = {{2, 3, 3}};
        assertFbSystem(solutions, equations);
    }

    @Test
    public void test12() {
        int[][] equations = {
                {2, 2, 0, 0, 4},
                {0, 0, 2, 2, 4}};
        int[][] solutions = {
                {1, 1, 1, 1},
                {1, 1, 0, 2},
                {1, 1, 2, 0},
                {2, 0, 1, 1},
                {2, 0, 2, 0},
                {2, 0, 0, 2},
                {0, 2, 1, 1},
                {0, 2, 0, 2},
                {0, 2, 2, 0}};
        assertFbSystem(solutions, equations);
    }

    @Test
    public void test13() {
        int[][] equations = {
                {0, 2, 1, 0, 1, 3},
                {2, 0, 1, 1, 0, 3}};
        int[][] solutions = {
                {0, 0, 0, 3, 3},
                {0, 0, 1, 2, 2},
                {0, 0, 2, 1, 1},
                {0, 0, 3, 0, 0},
                {0, 1, 0, 3, 1},
                {0, 1, 1, 2, 0},
                {1, 0, 0, 1, 3},
                {1, 0, 1, 0, 2},
                {1, 1, 0, 1, 1},
                {1, 1, 1, 0, 0}};
        assertFbSystem(solutions, equations);
    }

    @Test
    public void test14() {
        int[][] equations = {
                {2, 0, 0, 0, 4},
                {0, 1, 0, 0, 3},
                {0, 0, 7, 0, 21}};
        int[][] solutions = {{2, 3, 3, -1}};
        assertFbSystem(solutions, equations);
    }

    @Test
    public void test15() {
        int[][] equations = {
                {2, 0, 0, 0, 4},
                {0, 1, 0, 0, 3},
                {0, 0, 0, 7, 21}};
        int[][] solutions = {{2, 3, -1, 3}};
        assertFbSystem(solutions, equations);
    }

    @Test
    public void test16() {
        int[][] equations = {
                {0, 2, 0, 0, 4},
                {0, 0, 1, 0, 3},
                {0, 0, 0, 7, 21}};
        int[][] solutions = {{-1, 2, 3, 3}};
        assertFbSystem(solutions, equations);
    }

    @Test
    public void test17() {
        int[][] equations = {
                {1, 2},
                {1, 2}};
        int[][] solutions = {{2}};
        assertFbSystem(solutions, equations);
    }

    @Ignore
    @Test(timeout = 500)
    public void test18() {
        int[][] equations = {
                {1, 0, 12, 3, 43, 1, 4, 54, 1, 32, 9, 1, 242131},
                {12, 2, 0, 213, 0, 11, 7, 8, 9, 67, 4, 0, 21242432}};
        FrobeniusSolver solver = new FrobeniusSolver(equations);

        //from Wolfram Mathematica
        int[] solution1 = {0, 2124046, 0, 79780, 0, 0, 0, 0, 0, 0, 300, 91};
        assertFbEquation(equations[0], solution1);
        assertFbEquation(equations[1], solution1);

        int[] solution = solver.take();

        assertFbEquation(equations[0], solution);
        assertFbEquation(equations[1], solution);
    }

    @Test
    public void test19() {
        int[][] equations = {{12, 16, 20, 27, 123}, {1, 0, 3, 0, 12}};

        FrobeniusSolver solver = new FrobeniusSolver(equations);
        int[] solution;
        while ((solution = solver.take()) != null) {
            System.out.println(Arrays.toString(solution));
            System.out.println(solution);
        }
    }


}
