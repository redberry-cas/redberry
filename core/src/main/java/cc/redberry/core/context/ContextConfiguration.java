/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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
package cc.redberry.core.context;

import cc.redberry.core.indices.IndexType;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import java.util.EnumSet;

import static cc.redberry.core.indices.IndexType.*;

/**
 * @author Stanislav Poslavsky
 */
public final class ContextConfiguration implements Cloneable {
    public String metricName = "g";

    public String kroneckerName = "d";

    public EnumSet<IndexType> metricTypes = EnumSet.of(LatinLower, LatinUpper, GreekLower, GreekUpper);

    public OutputFormat defaultOutputFormat = OutputFormat.Redberry;

    public RandomGenerator randomGenerator = new Well19937c(System.currentTimeMillis() + System.identityHashCode(this));

    public IdAlgorithm idAlgorithm = IdAlgorithm.Random;

    public NameManager.IdProvider idProvider = null;

    @Override
    public ContextConfiguration clone() {
        try {
            return (ContextConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    private interface Factory {
        NameManager.IdProvider create(ContextConfiguration cc);
    }

    public enum IdAlgorithm {
        Random(new Factory() {
            @Override
            public NameManager.IdProvider create(ContextConfiguration cc) {
                return new NameManager.RandomIdProvider(cc.randomGenerator);
            }
        }), Fixed(new Factory() {
            @Override
            public NameManager.IdProvider create(ContextConfiguration cc) {
                return NameManager.HashBasedIdProvider;
            }
        });

        private final Factory factory;

        IdAlgorithm(Factory factory) {
            this.factory = factory;
        }

        public NameManager.IdProvider create(ContextConfiguration cc) {
            return factory.create(cc);
        }
    }
}
