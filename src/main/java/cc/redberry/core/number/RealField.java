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

import org.apache.commons.math3.Field;
import org.apache.commons.math3.FieldElement;

import java.io.Serializable;

/**
 *
 * @author Stanislav Poslavsky
 */
public class RealField implements Field<Real>, Serializable {
    private RealField() {
    }

    @Override
    public Real getOne() {
        return Rational.ONE;
    }

    @Override
    public Real getZero() {
        return Rational.ZERO;
    }

    @Override
    public Class<? extends FieldElement<Real>> getRuntimeClass() {
        return null;
    }

    /**
     * Get the unique instance.
     *
     * @return the unique instance
     */
    public static RealField getInstance() {
        return RealField.LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        /**
         * Cached field instance.
         */
        private static final RealField INSTANCE = new RealField();
    }
   
    /**
     * Handle deserialization of the singleton.
     *
     * @return the singleton instance
     */
    private Object readResolve() {
        // return the singleton instance
        return RealField.LazyHolder.INSTANCE;
    }
}
