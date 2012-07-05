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

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class FrobeniusUtils {
    public static void assertFbEquation(final int[] equation, final int[] solution) {
        assertTrue(equation.length - 1 == solution.length);
        int t = 0, i = 0;
        for (; i < equation.length - 1; ++i)
            t += equation[i] * solution[i];
        assertTrue(equation[i] == t);
    }

    public static void assertFbSystem(final int[][] equations, final int[] solution) {
        for (int[] equation : equations)
            assertFbEquation(equation, solution);
    }

    public static void assertSolutionsCount(final long count, final int[]... equations) {
        assertTrue(count == getSolutionsCount(equations));
    }

    public static long getSolutionsCount(final int[]... equations) {
        FrobeniusSolver fbSolver = new FrobeniusSolver(equations);
        long count = 0;
        int[] solution;
        while ((solution = fbSolver.take()) != null) {
            assertFbSystem(equations, solution);
            count++;
        }
        return count;
    }

    public static List<int[]> getAllSolutions(final int[]... equations) {
        return FbUtils.getAllSolutions(equations);
    }

    public static Iterable<int[]> iterable(int[][] equations) {
        return FbUtils.iterable(equations);
    }

    public static void assertFbSystem(int[][] solutions, int[][] equations) {
        List<int[]> solutionsList = getAllSolutions(equations);
        assertTrue(solutionsList.size() == solutions.length);

        int[][] solutions1 = (solutionsList).toArray(new int[solutionsList.size()][]);
        Arrays.sort(solutions, FbUtils.SOLUTION_COMPARATOR);
        Arrays.sort(solutions1, FbUtils.SOLUTION_COMPARATOR);
        for (int i = 0; i < solutions.length; ++i)
            assertTrue(Arrays.equals(solutions[i], solutions1[i]));
    }
}
