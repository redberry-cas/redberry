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
package cc.redberry.core.tensor;

import java.util.Arrays;

/**
 * This class represents the structure of product contraction.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class StructureOfContractions {
    public final static StructureOfContractions EMPTY_FULL_CONTRACTIONS_STRUCTURE = new StructureOfContractions(new long[0][]);
    public final long[][] contractions;
    public final int[] components;
    public final int componentCount;

    StructureOfContractions(final long[][] contractions) {
        this.contractions = contractions;
        components = new int[contractions.length];
        Arrays.fill(components, -1);
        int componentCounter = -1;
        for (int i = 0; i < contractions.length; i++) {
            if (components[i] == -1) {
                components[i] = ++componentCounter;
                fillComponents(components, componentCounter, i);
            }
        }
        this.componentCount = componentCounter + 1;
    }

    private void fillComponents(final int[] components, int component, int position) {
        for (long l : contractions[position]) {
            int to = toPosition(l);
            if (to != -1 && components[to] == -1) {
                components[to] = component;
                fillComponents(components, component, to);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < contractions.length; i++) {
            for (long l : contractions[i]) {
                sb.append(i).append("_").append(fromIPosition(l)).append(" -> ").append(toPosition(l)).append("_").append(toIDiffId(l));
                sb.append("\n");
            }

        }
        return sb.toString();
    }

    public static int toPosition(final long contraction) {
        return (int) (contraction >> 32);
    }

    public static short toIDiffId(final long contraction) {
        return (short) (0xFFFF & (contraction >> 16));
    }

    public static int fromIPosition(final long contraction) {
        return (int) (0xFFFF & contraction);
    }
}
