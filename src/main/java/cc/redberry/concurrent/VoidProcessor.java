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
 * Interface for processor without output.
 *
 * @author Bolotin Dmitriy (bolotin.dmitriy@gmail.com)
 * @param <InputT>
 */
public interface VoidProcessor<InputT> {
    void process(InputT input);

    public static class Utill {
        public static <T> void processAll(OutputPort<T> input, VoidProcessor<T> processor) {
            T object;
            try {
                while ((object = input.take()) != null)
                    processor.process(object);
            } catch (InterruptedException ex) {
            }
        }

        public static <T> void processInParalelAll(OutputPort<T> input, VoidProcessor<T> processor, int threadCount) {
            Thread[] threads = new Thread[threadCount];
            for (int i = 0; i < threadCount; ++i) {
                threads[i] = new Thread(new SimpleVoidProcessorThread<T>(input, processor));
                threads[i].start();
            }
            try {
                for (int i = 0; i < threadCount; ++i)
                    threads[i].join();
            } catch (InterruptedException ex) {
            }
        }

        public static <T> void processInParalelAll(OutputPort<T> input, VoidProcessorFactory<T> processorsFactory, int threadCount) {
            Thread[] threads = new Thread[threadCount];
            for (int i = 0; i < threadCount; ++i) {
                threads[i] = new Thread(new SimpleVoidProcessorThread<T>(input, processorsFactory.create()));
                threads[i].start();
            }
            try {
                for (int i = 0; i < threadCount; ++i)
                    threads[i].join();
            } catch (InterruptedException ex) {
            }
        }
    }
}
