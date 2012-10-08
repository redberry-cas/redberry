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
package cc.redberry.core.number;

import java.io.Serializable;
import org.apache.commons.math3.Field;
import org.apache.commons.math3.FieldElement;

/**
 *
 * @author Stanislav Poslavsky
 */
public class ComplexField implements Field<Complex>, Serializable {
    private ComplexField() {
    }

    @Override
    public Complex getOne() {
        return Complex.ONE;
    }

    @Override
    public Complex getZero() {
        return Complex.ZERO;
    }

    @Override
    public Class<? extends FieldElement<Complex>> getRuntimeClass() {
        return Complex.class;
    }

    public static ComplexField getInstance() {
        return LazyHolder.INSANCE;
    }

    private static class LazyHolder {
        private static final ComplexField INSANCE = new ComplexField();
    }
}
