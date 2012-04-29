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

/**
 * T-branch like element putting each taken from OutputPort object into InputPort and propagating it forward by it's take method.
 * 
 * @author Bolotin Dmitriy (bolotin.dmitriy@gmail.com)
 */
public class TBranchOutputPort<T> implements OutputPort<T> {
    private final InputPort<? super T> inputPort;
    private final OutputPort<? extends T> outputPort;

    public TBranchOutputPort(InputPort<? super T> inputPort, OutputPort<? extends T> outputPort) {
        this.inputPort = inputPort;
        this.outputPort = outputPort;
    }

    @Override
    public T take() throws InterruptedException {
        T object = outputPort.take();
        inputPort.put(object);
        return object;
    }

    public static <T> TBranchOutputPort<T> wrap(InputPort<? super T> inputPort, OutputPort<? extends T> outputPort) {
        return new TBranchOutputPort<T>(inputPort, outputPort);
    }
}
