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
package cc.redberry.core.context;

import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesTypeStructure;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class NameManager {

    private long seed;
    private final RandomGenerator random;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final Map<Integer, NameDescriptor> fromId = new HashMap<>();
    private final Map<IndicesTypeStructureAndName, NameDescriptor> fromStructure = new HashMap<>();
    private final String[] kroneckerAndMetricNames = {"d", "g"};
    private final IntArrayList kroneckerAndMetricIds = new IntArrayList();

    NameManager(Long seed, String kronecker, String metric) {
        if (seed == null) {
            random = new Well44497b();
            random.setSeed(this.seed = random.nextLong());
        } else
            random = new Well44497b(this.seed = seed.longValue());
        kroneckerAndMetricNames[0] = kronecker;
        kroneckerAndMetricNames[1] = metric;
    }

    public boolean isKroneckerOrMetric(int name) {
        return ArraysUtils.binarySearch(kroneckerAndMetricIds, name) >= 0;
    }

    public String getKroneckerName() {
        return kroneckerAndMetricNames[0];
    }

    public String getMetricName() {
        return kroneckerAndMetricNames[1];
    }

    public void setKroneckerName(String name) {
        kroneckerAndMetricNames[0] = name;
        rebuild();
    }

    public void setMetricName(String name) {
        kroneckerAndMetricNames[1] = name;
        rebuild();
    }

    private void rebuild() {
        writeLock.lock();
        try {
            fromStructure.clear();
            for (NameDescriptor descriptor : fromId.values())
                for (IndicesTypeStructureAndName itsan : descriptor.getKeys())
                    fromStructure.put(itsan, descriptor);
        } finally {
            writeLock.unlock();
        }
    }

    private NameDescriptor createDescriptor(final String sname, final IndicesTypeStructure[] indicesTypeStructures, int id) {
        if (indicesTypeStructures.length != 1)
            return new NameDescriptorImpl(sname, indicesTypeStructures, id);
        final IndicesTypeStructure its = indicesTypeStructures[0];
        if (its.size() != 2)
            return new NameDescriptorImpl(sname, indicesTypeStructures, id);
        for (byte b = 0; b < IndexType.TYPES_COUNT; ++b)
            if (its.typeCount(b) == 2)
                if (sname.equals(kroneckerAndMetricNames[0]) || sname.equals(kroneckerAndMetricNames[1])) {
                    NameDescriptor descriptor = new NameDescriptorForMetricAndKronecker(kroneckerAndMetricNames, b, id);
                    descriptor.getSymmetries().add(b, false, 1, 0);
                    return descriptor;
                }
        return new NameDescriptorImpl(sname, indicesTypeStructures, id);
    }

    public NameDescriptor mapNameDescriptor(String sname, IndicesTypeStructure... indicesTypeStructures) {
        IndicesTypeStructureAndName key = new IndicesTypeStructureAndName(sname, indicesTypeStructures);
        boolean rLocked = true;
        readLock.lock();
        try {
            NameDescriptor knownND = fromStructure.get(key);
            if (knownND == null) {
                readLock.unlock();
                rLocked = false;
                writeLock.lock();
                try {
                    knownND = fromStructure.get(key);
                    if (knownND == null) { //Double check
                        int name = generateNewName();
                        NameDescriptor descriptor = createDescriptor(sname, indicesTypeStructures, name);
                        if (descriptor instanceof NameDescriptorForMetricAndKronecker) {
                            kroneckerAndMetricIds.add(name);
                            kroneckerAndMetricIds.sort();
                        }
                        fromId.put(name, descriptor);
                        for (IndicesTypeStructureAndName key1 : descriptor.getKeys())
                            fromStructure.put(key1, descriptor);
                        return descriptor;
                    }
                    readLock.lock();
                    rLocked = true;
                } finally {
                    writeLock.unlock();
                }
            }
            return knownND;
        } finally {
            if (rLocked)
                readLock.unlock();
        }
    }

    /**
     * See {@link Context#resetTensorNames()}.
     */
    void reset() {
        writeLock.lock();
        try {
            kroneckerAndMetricIds.clear();
            fromId.clear();
            fromStructure.clear();
            random.setSeed(this.seed = random.nextLong());
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * See {@link Context#resetTensorNames()}.
     */
    void reset(long seed) {
        writeLock.lock();
        try {
            kroneckerAndMetricIds.clear();
            fromId.clear();
            fromStructure.clear();
            random.setSeed(this.seed = seed);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <b>Important:</b> run only in write lock!
     */
    private int generateNewName() {
        int name;
        do
            name = random.nextInt();
        while (fromId.containsKey(name));
        return name;
    }

    public NameDescriptor getNameDescriptor(int nameId) {
        readLock.lock();
        try {
            return fromId.get(nameId);
        } finally {
            readLock.unlock();
        }
    }

    public boolean containtsNameId(int nameId) {
        if (nameId < 0)
            return false;
        writeLock.lock();
        try {
            return fromId.size() > nameId;
        } finally {
            writeLock.unlock();
        }
    }

    public long getSeed() {
        return seed;
    }
}
