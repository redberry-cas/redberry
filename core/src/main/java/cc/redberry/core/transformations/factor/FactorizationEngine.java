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
package cc.redberry.core.transformations.factor;

import cc.redberry.core.tensor.Tensor;

/**
 * Abstract factorization engine.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public interface FactorizationEngine {

    /**
     * Factors a multivariate symbolic polynomial.
     *
     * @param tensor symbolic (without any indexes) polynomial or rational expression
     * @return factorization
     * @throws RuntimeException if specified tensor is not symbolic (without any indexes) expression
     */
    Tensor factor(Tensor tensor);
}
