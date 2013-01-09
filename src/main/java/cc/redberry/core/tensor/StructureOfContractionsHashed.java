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
package cc.redberry.core.tensor;

import java.util.Arrays;

/**
 * Representation of hashed graph of product.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class StructureOfContractionsHashed {
    /**
     * Singleton for empty structure
     */
    public static final StructureOfContractionsHashed EMPTY_INSTANCE =
            new StructureOfContractionsHashed(new TensorContraction((short) -1, new long[0]));

    private final TensorContraction freeContraction;
    private final TensorContraction[] contractions;

    public StructureOfContractionsHashed(final TensorContraction freeContraction, final TensorContraction... contractions) {
        this.freeContraction = freeContraction;
        this.contractions = contractions;
    }

    public TensorContraction get(int i) {
        return contractions[i];
    }

    public TensorContraction getFreeContraction() {
        return freeContraction;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (hashCode() != obj.hashCode())
            return false;
        final StructureOfContractionsHashed other = (StructureOfContractionsHashed) obj;
        if (!freeContraction.equals(other.freeContraction))
            return false;
        return Arrays.equals(contractions, other.contractions);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.freeContraction.hashCode();
        hash = 67 * hash + Arrays.hashCode(this.contractions);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Free: ");
        builder.append(freeContraction);
        for (TensorContraction contraction : contractions)
            builder.append("\n").append(contraction);
        return builder.toString();
    }
}
