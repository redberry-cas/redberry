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

package cc.redberry.concurrent;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides functionality of fixed size buffer between OutputPorts and their users.
 * 
 * It can serve several OutputPorts, merging their content to one port in a concurrent manner.<br/><br/>
 * 
 * This class creates N working threads. N is a number of OutputPorts connected to it.
 * 
 * @author Bolotin Dmitriy (bolotin.dmitriy@gmail.com)
 * @param <T> type of objects buffered
 */
public class ArrayBuffer<T> implements OutputPort<T> {
    private T[] buffer;
    private int currentWritePointer;
    private int currentReadPointer;
    private int elements;
    private int limit;
    private List<Thread> workers = new ArrayList<>();
    private boolean closed = false;
    private List<ArrayBufferInputPort> inputPorts = new ArrayList<>();

    public ArrayBuffer(int limit) {
        this.limit = limit;
        buffer = (T[]) new Object[limit];
        currentReadPointer = 0;
        currentWritePointer = 0;
        elements = 0;
    }

    private synchronized void closedTest() {
        for (ArrayBufferInputPort port : inputPorts)
            if (!port.closed)
                return;
        closed = true;
        notifyAll();
    }

    public synchronized void waitFree() throws InterruptedException {
        while (!closed)
            wait();
    }

    /**
     * Main method to put elements in array.
     * This method always executed through InputPort
     * 
     * @param element
     * @throws InterruptedException 
     */
    private synchronized void put(T element) throws InterruptedException {
        if (element == null)
            throw new NullPointerException();
        while (elements == limit)
            wait();
        buffer[currentWritePointer++] = element;
        if (currentWritePointer == limit)
            currentWritePointer = 0;
        elements++;
        notifyAll();
    }

    @Override
    public synchronized T take() throws InterruptedException {
        while (elements == 0 && !closed)
            wait();
        if (closed && elements == 0)
            return null;
        T ret = buffer[currentReadPointer++];
        if (currentReadPointer == limit)
            currentReadPointer = 0;
        elements--;
        notifyAll();
        return ret;
    }

    /**
     * Creates new input port to add elements to this buffer.
     * To close this buffer all input ports must be closed.
     * 
     * @return new InputPort for this buffer.
     */
    private ArrayBufferInputPort getInputPort() {
        ArrayBufferInputPort port = new ArrayBufferInputPort();
        inputPorts.add(port);
        return port;
    }

    /**
     * Each port will be processed in the single thread
     * So, no concurrent calls to port.take() will be done
     * 
     * @param port
     */
    public void redirectPort(OutputPort<T> port) {
        Thread w = new Thread(new O2IConnector(port, new ArrayBufferInputPort()));
        workers.add(w);
        w.start();
    }

    @Override
    public String toString() {
        return "B{" + elements + '/' + limit + '}';
    }

    private class ArrayBufferInputPort implements InputPort<T> {
        public boolean closed = false;

        public void put(T object) throws InterruptedException {
            if (object == null)
                if (!closed) {
                    closed = true;
                    closedTest();
                    return;
                } else
                    throw new RuntimeException("Port is already closed.");
            ArrayBuffer.this.put(object);
        }
    }
}
