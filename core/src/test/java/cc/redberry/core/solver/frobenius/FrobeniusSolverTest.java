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
package cc.redberry.core.solver.frobenius;

import cc.redberry.core.context.CC;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static cc.redberry.core.solver.frobenius.FrobeniusUtils.*;

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
    public void test20_recursive() {
        int[][] equations = {{12, 16, 20, 27, 123}};
        List<int[]> solutions = FbUtils.getAllSolutions(equations);
        List<int[]> actual = allFBSolutionsRecursive(equations[0]);
        Comparator<int[]> comparator = new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return Integer.compare(Arrays.hashCode(o1), Arrays.hashCode(o2));
            }
        };
        Collections.sort(solutions, comparator);
        Collections.sort(actual, comparator);
        Assert.assertEquals(solutions.size(), actual.size());
        for (int i = 0; i < actual.size(); ++i)
            Assert.assertArrayEquals(solutions.get(i), actual.get(i));
    }

    @Ignore
    @Test
    public void testTiming() throws Exception {
        for (int c = 0; c < 100; ++c) {
            int N = 7;
            int[][] equations = new int[1][N];
            int total = 0;
            for (int i = 0; i < N - 1; ++i) {
                equations[0][i] = 5 + CC.getRandomGenerator().nextInt(5);
                total += equations[0][i];
            }
            equations[0][N - 1] = 2 * total - 1;
            int[] equation = Arrays.copyOfRange(equations[0], 0, equations[0].length - 1);

            ArrayList<int[]> expected = (ArrayList) FbUtils.getAllSolutions(equations);
            ArrayList<int[]> actual = (ArrayList) allFBSolutions(equation, equations[0][equations[0].length - 1]);

            Comparator<int[]> comparator = new Comparator<int[]>() {
                @Override
                public int compare(int[] o1, int[] o2) {
                    return Integer.compare(Arrays.hashCode(o1), Arrays.hashCode(o2));
                }
            };
            Collections.sort(expected, comparator);
            Collections.sort(actual, comparator);

            Assert.assertEquals(expected.size(), actual.size());
            for (int i = 0; i < actual.size(); ++i)
                Assert.assertArrayEquals(expected.get(i), actual.get(i));
        }
    }

    private static List<int[]> allFBSolutions(int[] system, int total) {
        ArrayList<int[]> all = new ArrayList<>();
        int[] solution = new int[system.length];//initialized by zeros
        int pointer = system.length - 1, temp;
        out:
        while (true) {
            do { //the following loop can be optimized by calculation of remainder
                ++solution[pointer];
            } while ((temp = total(system, solution)) < total);

            if (temp == total && pointer != 0)
                all.add(solution.clone());
            do {
                if (pointer == 0) {
                    if (temp == total) //not lose the last solution!
                        all.add(solution.clone());
                    break out;
                }
                for (int i = pointer; i < system.length; ++i)
                    solution[i] = 0;
                ++solution[--pointer];
            } while ((temp = total(system, solution)) > total);
            pointer = system.length - 1;
            if (temp == total)
                all.add(solution.clone());
        }
        return all;
    }

    private static List<int[]> allFBSolutionsRecursive(int[] equation) {
        return allFBSolutionsRecursive(
                Arrays.copyOfRange(equation, 0, equation.length - 1),
                equation[equation.length - 1]);
    }

    private static List<int[]> allFBSolutionsRecursive(int[] system, int total) {
        List<int[]> all = new ArrayList<>();
        final int[] solution = new int[system.length];
        addFBSolution(system, total, all, 0, solution, 0);
        return all;
    }

    private static void addFBSolution(final int[] system,
                                      int total,
                                      final List<int[]> solutions,
                                      int index,
                                      final int[] solution,
                                      int tempSum) {
        if (index == system.length)
            return;
        for (int i = 0; tempSum <= total; ++i) {
            solution[index] = i;
            if (tempSum == total && index == system.length - 1)
                solutions.add(solution.clone());
            addFBSolution(system, total, solutions, index + 1, solution, tempSum);
            tempSum = tempSum + system[index];
        }
    }


    public static int total(int[] system, int[] solution) {
        int total = 0;
        for (int i = 0; i < system.length; ++i)
            total += system[i] * solution[i];
        return total;
    }
}
