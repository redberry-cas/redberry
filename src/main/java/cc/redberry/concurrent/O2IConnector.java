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
 * @author dmitriybolotin
 */
public class O2IConnector<T> implements Runnable {
    public OutputPort<T> oPort;
    public InputPort iPort;

    public O2IConnector(OutputPort<T> output, InputPort<T> input) {
        this.oPort = output;
        this.iPort = input;
    }

    @Override
    public void run() {
        try {
            T element;
            while ((element = oPort.take()) != null)
                iPort.put(element);
        } catch (InterruptedException ex) {
        } catch (RuntimeException re) {
        } finally {
            try {
                iPort.put(null);
            } catch (InterruptedException ex) {
            }
        }
    }
}
