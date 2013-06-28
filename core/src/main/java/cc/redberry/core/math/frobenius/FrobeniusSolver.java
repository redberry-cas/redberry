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
package cc.redberry.core.math.frobenius;

import cc.redberry.concurrent.OutputPortUnsafe;

/**
 * Solves system of Frobenius equations.
 * <p/>
 * <p><b>Example:</b> This gives all solutions of the system of Frobenius
 * equations { 12 x + 16 y + 20 z + 27 t = 123, x + 3 z = 12}:
 * <pre><code>
 * int[][] equations = {{12, 16, 20, 27, 123}, {1, 0, 3, 0, 12}};
 * FrobeniusSolver solver = new FrobeniusSolver(equations);
 * int[] solution;
 * while ((solution = solver.take()) != null)
 *      System.out.println(Arrays.toString(solution));
 * </code></pre>
 * </p>
 * <p/>
 * This class calculates solutions iteratively: method {@link #take()}
 * calculates and returns the next solution or null, if no
 * more solutions exist. So next solution will
 * be calculated only on the invocation of {@link #take()}.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class FrobeniusSolver implements OutputPortUnsafe<int[]> {
    private final OutputPortUnsafe<int[]> provider;

    /**
     * Constructs solver from the given system of equations (see class documentation for example).
     *
     * @param equations system of Frobenius equations
     * @throws IllegalArgumentException if {@code equations} have different lengths
     * @throws IllegalArgumentException if some coefficient is negative
     */
    public FrobeniusSolver(final int[]... equations) {
        if (equations.length == 0)
            throw new IllegalArgumentException();
        final int length = equations[0].length;
        if (length < 2)
            throw new IllegalArgumentException();

        int i, j;
        for (i = 1; i < equations.length; ++i)
            if (equations[i].length != length && !assertEq(equations[i]))
                throw new IllegalArgumentException();

        //processing initial solution: filling -1s.
        int[] initialSolution = new int[length - 1];
        int zeroCoefficientsCount = 0;
        OUT:
        for (i = 0; i < length - 1; ++i) {
            for (j = 0; j < equations.length; ++j)
                if (equations[j][i] != 0)
                    continue OUT;
            initialSolution[i] = -1;
            zeroCoefficientsCount++;
        }

        //processing initial remainders
        int[] initialRemainders = new int[equations.length];
        for (j = 0; j < equations.length; ++j)
            initialRemainders[j] = equations[j][length - 1];

        SolutionProvider dummy = new DummySolutionProvider(initialSolution, initialRemainders);

        int providersCount = length - 1 - zeroCoefficientsCount;
        SolutionProvider[] providers = new SolutionProvider[providersCount];
        int[] coefficients;
        int count = 0;

        for (i = 0; i < length - 1; ++i) {
            if (initialSolution[i] == -1)
                continue;
            //processing coefficients
            coefficients = new int[equations.length];
            for (j = 0; j < equations.length; ++j)
                coefficients[j] = equations[j][i];

            if (count == 0)
                if (providersCount == 1)
                    providers[count] = new FinalSolutionProvider(dummy, i, coefficients);
                else
                    providers[count] = new SingleSolutionProvider(dummy, i, coefficients);
            else if (count == providersCount - 1)
                providers[count] = new FinalSolutionProvider(providers[count - 1], i, coefficients);
            else
                providers[count] = new SingleSolutionProvider(providers[count - 1], i, coefficients);
            count++;
        }
        provider = new TotalSolutionProvider(providers);
        //redundant
        //dummy.tick()
    }

    /**
     * Calculates and returns the next solution or {@code null} if no more solutions exist.
     *
     * @return the next solution or {@code null} if no more solutions exist
     */
    @Override
    public int[] take() {
        return provider.take();
    }

    private boolean assertEq(int[] equation) {
        for (int i : equation)
            if (i < 0)
                return false;
        return true;
    }
}
