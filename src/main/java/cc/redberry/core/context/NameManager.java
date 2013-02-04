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
package cc.redberry.core.context;

import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.StructureOfIndices;
import cc.redberry.core.parser.ParserException;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Object of this class represents a namespace of simple tensor and tensor fields in Redberry.
 * It is responsible for generation of unique name descriptors ({@link NameDescriptor}) and integer
 * identifiers for simple tensors and fields from raw data. These identifiers are the same for tensors
 * with the same mathematical nature. They are generated randomly in order to obtain the uniform distribution
 * through Redberry session. Each session of Redberry holds only one instance of this class, it can be obtained
 * through {@link CC#getNameManager()}.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class NameManager {

    private long seed;
    private final RandomGenerator random;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final Map<Integer, NameDescriptor> fromId = new HashMap<>();
    private final Map<NameAndStructureOfIndices, NameDescriptor> fromStructure = new HashMap<>();
    private final String[] kroneckerAndMetricNames = {"d", "g"};
    private final IntArrayList kroneckerAndMetricIds = new IntArrayList();

    NameManager(Long seed, String kronecker, String metric) {
        if (seed == null) {
            random = new Well44497b();
            random.setSeed(this.seed = random.nextLong());
        } else
            random = new Well44497b(this.seed = seed);
        kroneckerAndMetricNames[0] = kronecker;
        kroneckerAndMetricNames[1] = metric;
    }

    /**
     * Returns {@code true} if specified identifier is identifier of metric or Kronecker tensor
     *
     * @param name unique simple tensor identifier
     * @return {@code true} if specified identifier is identifier of metric or Kronecker tensor
     */
    public boolean isKroneckerOrMetric(int name) {
        return ArraysUtils.binarySearch(kroneckerAndMetricIds, name) >= 0;
    }

    /**
     * Returns string representation of Kronecker delta name
     *
     * @return string representation of Kronecker delta name
     */
    public String getKroneckerName() {
        return kroneckerAndMetricNames[0];
    }

    /**
     * Returns string representation of metric tensor name
     *
     * @return string representation of metric tensor name
     */
    public String getMetricName() {
        return kroneckerAndMetricNames[1];
    }

    /**
     * Sets the default Kronecker tensor name. After this step, Kronecker tensor
     * will be printed with the specified string name.
     *
     * @param name string representation of Kronecker tensor name
     */
    public void setKroneckerName(String name) {
        kroneckerAndMetricNames[0] = name;
        rebuild();
    }

    /**
     * Sets the default metric tensor name. After this step, metric tensor
     * will be printed with the specified string name.
     *
     * @param name string representation of metric tensor name
     */
    public void setMetricName(String name) {
        kroneckerAndMetricNames[1] = name;
        rebuild();
    }

    private void rebuild() {
        writeLock.lock();
        try {
            fromStructure.clear();
            for (NameDescriptor descriptor : fromId.values())
                for (NameAndStructureOfIndices itsan : descriptor.getKeys())
                    fromStructure.put(itsan, descriptor);
        } finally {
            writeLock.unlock();
        }
    }

    private NameDescriptor createDescriptor(final String sname, final StructureOfIndices[] structuresOfIndices, int id) {
        if (structuresOfIndices.length != 1)
            return new NameDescriptorImpl(sname, structuresOfIndices, id);
        final StructureOfIndices its = structuresOfIndices[0];
        if (its.size() != 2)
            return new NameDescriptorImpl(sname, structuresOfIndices, id);
        for (byte b = 0; b < IndexType.TYPES_COUNT; ++b)
            if (its.typeCount(b) == 2) {
                if (CC.isMetric(b)) {
                    if (sname.equals(kroneckerAndMetricNames[0]) || sname.equals(kroneckerAndMetricNames[1])) {
                        NameDescriptor descriptor = new NameDescriptorForMetricAndKronecker(kroneckerAndMetricNames, b, id);
                        descriptor.getSymmetries().add(b, false, 1, 0);
                        return descriptor;
                    }
                } else {
                    if (sname.equals(kroneckerAndMetricNames[1]))
                        throw new ParserException("Metric is not specified for non metric index type.");
                    if (sname.equals(kroneckerAndMetricNames[0])) {
                        if (its.getTypeData(b).states.get(0) != true || its.getTypeData(b).states.get(1) != false)
                            throw new ParserException("Illegal Kroneckers indices states.");
                        NameDescriptor descriptor = new NameDescriptorForMetricAndKronecker(kroneckerAndMetricNames, b, id);
                        return descriptor;
                    }
                }
            }
        return new NameDescriptorImpl(sname, structuresOfIndices, id);
    }

    /**
     * This method returns the existing name descriptor of simple tensor from the raw data if it contains in the
     * namespace or constructs and puts to namespace new instance of name descriptor otherwise.
     *
     * @param sname                string name of tensor
     * @param structureOfIndiceses structure of tensor indices (first element in array) and structure of indices
     *                             of arguments (in case of tensor field)
     * @return name descriptor corresponding to the specified information of tensor
     */
    public NameDescriptor mapNameDescriptor(String sname, StructureOfIndices... structureOfIndiceses) {
        NameAndStructureOfIndices key = new NameAndStructureOfIndices(sname, structureOfIndiceses);
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
                        NameDescriptor descriptor = createDescriptor(sname, structureOfIndiceses, name);
                        if (descriptor instanceof NameDescriptorForMetricAndKronecker) {
                            kroneckerAndMetricIds.add(name);
                            kroneckerAndMetricIds.sort();
                        }
                        fromId.put(name, descriptor);
                        for (NameAndStructureOfIndices key1 : descriptor.getKeys())
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

    /**
     * Returns the name descriptor of the specified unique identifier of simple tensor.
     *
     * @param nameId unique identifier of simple tensor
     * @return name descriptor of the specified unique identifier of simple tensor
     */
    public NameDescriptor getNameDescriptor(int nameId) {
        readLock.lock();
        try {
            return fromId.get(nameId);
        } finally {
            readLock.unlock();
        }
    }

    /*
    public boolean containsNameId(int nameId) {
        if (nameId < 0)
            return false;
        writeLock.lock();
        try {
            return fromId.size() > nameId;
        } finally {
            writeLock.unlock();
        }
    }*/

    /**
     * Returns the seed of the random generator used in name manager.
     *
     * @return seed of the random generator
     */
    public long getSeed() {
        return seed;
    }
}
