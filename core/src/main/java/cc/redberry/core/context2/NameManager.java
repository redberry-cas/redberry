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
package cc.redberry.core.context2;

import cc.redberry.core.indices.StructureOfIndices;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.HashMap;

/**
 * @author Stanislav Poslavsky
 */
public final class NameManager {
    private static final int STARTUP_NAMESPACE_SIZE = 1024;

    final HashMap<NameAndStructureOfIndices, VarDescriptor> namesTable = new HashMap<>();
    final TIntObjectHashMap<VarDescriptor> idsTable = new TIntObjectHashMap<>(STARTUP_NAMESPACE_SIZE);
    final IdProvider idProvider;

    public NameManager(IdProvider idProvider) {
        this.idProvider = idProvider;
    }

    NameManager fork() {
        final NameManager nm = new NameManager(idProvider);
        nm.namesTable.putAll(this.namesTable);
        nm.idsTable.putAll(this.idsTable);

        return nm;
    }

    synchronized void reset() {
        namesTable.clear();
        idsTable.clear();
    }

    int size() {
        return namesTable.size();
    }

    synchronized void clear(int id) {
        final VarDescriptor nd = idsTable.remove(id);
        if (nd == null)
            return;
        namesTable.remove(new NameAndStructureOfIndices(nd.baseName, nd.varIndicesStructure));
        for (String alias : nd.aliases)
            namesTable.remove(new NameAndStructureOfIndices(alias, nd.varIndicesStructure));
    }

    synchronized void clear(int... ids) {
        for (int id : ids) {
            final VarDescriptor nd = idsTable.remove(id);
            if (nd == null)
                continue;
            namesTable.remove(new NameAndStructureOfIndices(nd.baseName, nd.varIndicesStructure));
            for (String alias : nd.aliases)
                namesTable.remove(new NameAndStructureOfIndices(alias, nd.varIndicesStructure));
        }
    }

    VarDescriptor resolveVarOrFunction(final String name,
                                       final StructureOfIndices structureOfIndices,
                                       final VarIndicesProvider indicesProvider,
                                       final boolean isFunction) {
        NameAndStructureOfIndices key = new NameAndStructureOfIndices(name, structureOfIndices);
        VarDescriptor nd = namesTable.get(key);
        if (nd == null) {
            synchronized (this) {
                nd = namesTable.get(key);
                if (nd == null) {
                    final int id = idProvider.id(key, idsTable);
                    nd = new VarDescriptor(id, name, structureOfIndices, indicesProvider, isFunction);

                    idsTable.put(id, nd);
                    namesTable.put(key, nd);
                }
            }
        }
        return nd;
    }

    synchronized void addNameAlias(final VarDescriptor nd, String alias) {
        nd.aliases.add(alias);
        namesTable.put(new NameAndStructureOfIndices(alias, nd.varIndicesStructure), nd);
    }

    VarDescriptor getVarDescriptor(int id) {
        return idsTable.get(id);
    }

//    private VarDescriptor resolveWithFixedIndices(final NameAndStructureOfIndices key, boolean isFunction) {
//        return resolveVarOrFunction(key, new String[0], new VarIndicesProvider.SymmetriesHolder(IndicesSymmetries.create(key.structureOfIndices)), isFunction);
//    }
//
//    public VarDescriptor resolveVar(final NameAndStructureOfIndices key) {
//        return resolveWithFixedIndices(key, false);
//    }
//
//    public VarDescriptor resolveFunction(final NameAndStructureOfIndices key) {
//        return resolveWithFixedIndices(key, true);
//    }
//
//    public VarDescriptor resolveVariadicFunction(final NameAndStructureOfIndices key,
//                                                 final VarIndicesProvider indicesProvider) {
//        return resolveVarOrFunction(key, new String[0], indicesProvider, true);
//    }

    interface IdProvider {
        int id(NameAndStructureOfIndices key, TIntObjectHashMap<VarDescriptor> generatedIds);
    }

    static IdProvider HashBasedIdProvider = new IdProvider() {
        @Override
        public int id(NameAndStructureOfIndices key, TIntObjectHashMap<VarDescriptor> generatedIds) {
            int id = key.hashCode();
            while (generatedIds.contains(id))
                ++id;
            return id;
        }
    };

    static IdProvider RandomIdProvider = new IdProvider() {
        @Override
        public int id(NameAndStructureOfIndices key, TIntObjectHashMap<VarDescriptor> generatedIds) {
            final RandomGenerator rnd = Context2.current().randomGenerator();
            int id;
            do
                id = rnd.nextInt();
            while (generatedIds.containsKey(id));
            return id;
        }
    };
}
