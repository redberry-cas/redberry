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
 * Interface for passive objects consumer.<br/><br/> <b>Important:</b> read
 * contract for {@link put()} method.
 *
 * @author Bolotin Dmitriy (bolotin.dmitriy@gmail.com)
 * @param <T> type of objects consumed
 */
public interface InputPort<T> {
    /**
     * Method to put elements in this input port.<br/> <i>Contract:</i> Putting
     * {@code null} is allowed once and this indicates <b>closing</b> of this
     * input port. No invocation of this method allowed after <b>closing</b>.
     *
     * @param object
     * @throws InterruptedException
     */
    void put(T object) throws InterruptedException;
}
