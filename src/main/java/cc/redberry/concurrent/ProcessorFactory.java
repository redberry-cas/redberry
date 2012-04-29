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
 * Processor Factory.
 * 
 * @see Processor
 * @author Bolotin Dmitriy (bolotin.dmitriy@gmail.com)
 */
public interface ProcessorFactory<InputT, OutputT> {
    Processor<InputT, OutputT> create();

    public static class Util {
        public static <InputT, OutputT> Processor<InputT, OutputT>[] createArray(ProcessorFactory<InputT, OutputT> factory, int size) {
            Processor<InputT, OutputT>[] result = new Processor[size];
            for (int i = 0; i < size; ++i)
                result[i] = factory.create();
            return result;
        }
    }
}
