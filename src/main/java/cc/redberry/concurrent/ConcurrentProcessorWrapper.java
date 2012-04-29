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

import java.util.Arrays;
import java.util.List;

/**
 * This class helps process data from one OutputPort in N parallel threads.<br/>
 * It could be used either to access one Processor concurrently or N processors by corresponding threads.<br/>
 * See constructor descriptions.
 * 
 * @author Bolotin Dmitriy (bolotin.dmitriy@gmail.com)
 * @param <InputT> input type
 * @param <OutputT> output type
 */
public class ConcurrentProcessorWrapper<InputT, OutputT> implements OutputPort<OutputT> {
    private ArrayBuffer<OutputT> outputBuffer;
    private List<Port> ports;
    private OutputPort<InputT> input;

    /**
     * Creates ConcurrentProcessorWrapper working in one thread by processor mode.
     * 
     * @param input Port to take elements from.
     * @param factory Factory to create processors.
     * @param bufferSize Buffer size.
     * @param threads Number of parallel threads of processing. Also it is a number of processors to create.
     */
    public ConcurrentProcessorWrapper(OutputPort<InputT> input, ProcessorFactory<InputT, OutputT> factory, int bufferSize, int threads) {
        this(input, ProcessorFactory.Util.createArray(factory, threads), bufferSize);
    }

    /**
     * Creates ConcurrentProcessorWrapper working in one thread by processor mode.
     * 
     * @param input Port to take elements from.
     * @param processors Processors. Each processor will be accessed only by one thread.
     * @param bufferSize Buffer size
     */
    public ConcurrentProcessorWrapper(OutputPort<InputT> input, Processor<InputT, OutputT>[] processors, int bufferSize) {
        this.input = input;
        outputBuffer = new ArrayBuffer<OutputT>(bufferSize);
        Port[] ps = new ConcurrentProcessorWrapper.Port[processors.length];
        for (int i = 0; i < processors.length; ++i) {
            ps[i] = new Port(processors[i]);
            outputBuffer.redirectPort(ps[i]);
        }
        ports = Arrays.asList(ps);
    }

    /**
     * Creates ConcurrentProcessorWrapper working in concurrent processor access mode.
     * 
     * @param input Port to take elements from.
     * @param processor Processor. It will be accessed concurrently.
     * @param bufferSize Buffer size
     * @param threads Number of parallel threads of processing
     */
    public ConcurrentProcessorWrapper(OutputPort<InputT> input, Processor<InputT, OutputT> processor, int bufferSize, int threads) {
        if (threads > 1
                && !(processor instanceof ThreadSafe))
            throw new IllegalArgumentException("Processor not implements ThreadSafe interface.");
        this.input = input;
        outputBuffer = new ArrayBuffer<OutputT>(bufferSize);
        Port[] ps = new ConcurrentProcessorWrapper.Port[threads];
        for (int i = 0; i < threads; ++i) {
            ps[i] = new Port(processor);
            outputBuffer.redirectPort(ps[i]);
        }
        ports = Arrays.asList(ps);
    }

    public void join() throws InterruptedException {
        outputBuffer.waitFree();
    }

    public OutputT take() throws InterruptedException {
        return outputBuffer.take();
    }

    @Override
    public String toString() {
        return outputBuffer.toString();
    }

    private class Port implements OutputPort<OutputT> {
        private Processor<InputT, OutputT> processor;

        public Port(Processor<InputT, OutputT> processor) {
            this.processor = processor;
        }

        public OutputT take() throws InterruptedException {
            InputT i = input.take();
            if (i == null)
                return null;
            return processor.process(i);
        }
    }
}
