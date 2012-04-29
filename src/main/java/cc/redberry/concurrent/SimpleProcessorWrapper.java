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
 * 
 * @author Bolotin Dmitriy (bolotin.dmitriy@gmail.com)
 * @param <InputT>
 * @param <OutputT> 
 */
public class SimpleProcessorWrapper<InputT, OutputT> implements OutputPort<OutputT> {
    private OutputPort<? extends InputT> inputPort;
    private Processor<? super InputT, ? extends OutputT> processor;

    public SimpleProcessorWrapper(OutputPort<? extends InputT> inputPort, Processor<? super InputT, ? extends OutputT> processor) {
        this.inputPort = inputPort;
        this.processor = processor;
    }

    @Override
    public OutputT take() throws InterruptedException {
        InputT input = inputPort.take();
        if (input == null)
            return null;
        return processor.process(input);
    }
}
