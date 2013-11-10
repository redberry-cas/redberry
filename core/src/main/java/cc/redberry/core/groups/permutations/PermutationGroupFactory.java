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
package cc.redberry.core.groups.permutations;

import java.util.Collection;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class PermutationGroupFactory {
    private PermutationGroupFactory() {
    }

    public static PermutationGroup createPermutationGroup(Collection<Permutation> generators) {
        return createPermutationGroup(generators.toArray(new Permutation[generators.size()]));
    }

    public static PermutationGroup createPermutationGroup(Permutation... generators) {
        if (generators.length == 0)
            throw new IllegalArgumentException("Now generators specified.");
        //todo Alt and Sym checks
        List<BSGSElement> BSGSList = BSGSAlgorithms.createBSGSList(generators);
        if (BSGSList.isEmpty())
            return new EmptyPermutationGroup(generators[0].length());
        return new PermutationGroupImpl(new BSGS(BSGSList));
    }
}
