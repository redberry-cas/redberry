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
package cc.redberry.core.solver;

import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.SimpleTensor;

/**
 * This is a simple holder for system of symbolic equations.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1.5
 */
public class ReducedSystem {
    final Expression[] equations;
    final SimpleTensor[] unknownCoefficients;
    final Expression[] generalSolutions;

    public ReducedSystem(Expression[] equations, SimpleTensor[] unknownCoefficients, Expression[] generalSolutions) {
        this.equations = equations;
        this.unknownCoefficients = unknownCoefficients;
        this.generalSolutions = generalSolutions;
    }

    /**
     * Returns an underlying system of equations.
     *
     * @return underlying system of equations
     */
    public Expression[] getEquations() {
        return equations.clone();
    }


    /**
     * Returns an unknown variables.
     *
     * @return an unknown variables
     */
    public SimpleTensor[] getUnknownCoefficients() {
        return unknownCoefficients.clone();
    }


    /**
     * Returns the general solution of the tensorial system.
     *
     * @return the general solution of the tensorial system
     */
    public Expression[] getGeneralSolutions() {
        return generalSolutions.clone();
    }
}
