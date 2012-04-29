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

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.math.random.BitsStreamGenerator;
import org.apache.commons.math.random.Well44497b;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class NameManager {
    private long seed;
    private final BitsStreamGenerator random;// = new Well44497b(); //TODO: what is the best bit provider at this point???
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final Map<Integer, NameDescriptor> fromId = new HashMap<>();
    private final Map<NameDescriptor.IndicesTypeStructureAndName, NameDescriptor> fromStructure = new HashMap<>();
    private final List<NewNameDescriptorListener> newNameDescriptorListeners = Collections.synchronizedList(new ArrayList<NewNameDescriptorListener>());
    private final Context context;

    public NameManager(Context context, Long seed) {
        this.context = context;
        if (seed == null) {
            random = new Well44497b();
            random.setSeed(this.seed = random.nextLong());
        } else
            random = new Well44497b(this.seed = seed.longValue());
    }

    public int mapNameDescriptor(NameDescriptor descriptor) {
        if (descriptor == null)
            throw new NullPointerException();
        boolean rLocked = true;
        readLock.lock();
        try {
            NameDescriptor knownND = fromStructure.get(descriptor.getKey());
            if (knownND == null) {
                readLock.unlock();
                rLocked = false;
                writeLock.lock();
                try {
                    knownND = fromStructure.get(descriptor.getKey());
                    if (knownND == null) { //Double check
                        int name = generateNewName();
                        descriptor.setId(name);
                        fromId.put(name, descriptor);
                        fromStructure.put(descriptor.getKey(), descriptor);
                        newDescriptor(descriptor); //TODO: synchronization conflicts (dead-locks) ?? [synchronized (context)]
                        return descriptor.getId();
                    }
                    readLock.lock();
                    rLocked = true;
                } finally {
                    writeLock.unlock();
                }
            }
            descriptor.setId(knownND.getId());
            return knownND.getId();
        } finally {
            if (rLocked)
                readLock.unlock();
        }
    }

    /**
     * See {@link Context#resetTensorNames()}.
     */
    public void reset() {
        writeLock.lock();
        try {
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
    public void reset(long seed) {
        writeLock.lock();
        try {
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

    private void newDescriptor(NameDescriptor nameDescriptor) {
        synchronized (context) {
            for (NewNameDescriptorListener listener : newNameDescriptorListeners)
                listener.newNameDescriptor(nameDescriptor);
        }
    }

    public void registerNewNameDescriptorListener(NewNameDescriptorListener listener) {
        newNameDescriptorListeners.add(listener);
    }

    public void removeNewNameDescriptorListener(NewNameDescriptorListener listener) {
        newNameDescriptorListeners.remove(listener);
    }

    public long getSeed() {
        return seed;
    }
}
