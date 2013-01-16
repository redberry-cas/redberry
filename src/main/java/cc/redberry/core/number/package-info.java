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

/**
 * Implementation of numbers in Redberry. The implementation includes real numbers ({@link cc.redberry.core.number.Real}):
 * big rational ({@link cc.redberry.core.number.Rational}), floating point ({@link cc.redberry.core.number.Numeric}) and complex numbers ({@link cc.redberry.core.number.Complex}) over
 * the reals. The implementation of big rational numbers is based on the Apache Commons Math {@link org.apache.commons.math3.fraction.BigFraction}.
 */
package cc.redberry.core.number;