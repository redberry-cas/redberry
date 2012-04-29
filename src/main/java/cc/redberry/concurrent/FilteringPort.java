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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * OutputPort propagating only elements accepted by filter.<br/>
 * No additional threads created.
 * 
 * @author Bolotin Dmitriy (bolotin.dmitriy@gmail.com)
 * @param <T> 
 */
public class FilteringPort<T> implements OutputPort<T> {
    private OutputPort<T> port;
    private Filter<T> filter;
    private InputPort<T> discardedPort = null;
    private AtomicInteger accepted = new AtomicInteger(0);
    private AtomicInteger rejected = new AtomicInteger(0);

    public FilteringPort(OutputPort<T> port, Filter<T> filter) {
        if (port == null || filter == null)
            throw new NullPointerException();
        this.port = port;
        this.filter = filter;
    }

    public FilteringPort(OutputPort<T> port, Filter<T> filter, InputPort<T> discardedPort) {
        this(port, filter);
        if (port == null || filter == null)
            throw new NullPointerException();
        this.discardedPort = discardedPort;
    }

    @Override
    public T take() throws InterruptedException {
        T o;
        while ((o = port.take()) != null && !filter.accept(o)) {
            if (o != null && discardedPort != null)
                discardedPort.put(o);
            rejected.incrementAndGet();
        }
        if (o != null)
            accepted.incrementAndGet();
        return o;
    }

    public void attachDiscardPort(InputPort<T> discardedPort) {
        this.discardedPort = discardedPort;
    }

    public int getAccepted() {
        return accepted.get();
    }

    public int getRejected() {
        return rejected.get();
    }
}
