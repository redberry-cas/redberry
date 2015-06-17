/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public interface ToString {
    /**
     * Returns a string representation of this object according to the specified
     * {@link cc.redberry.core.context.OutputFormat}.
     *
     * @param outputFormat output format
     * @return a string representation of this
     */
    String toString(OutputFormat outputFormat);

    /**
     * Returns a string representation of this object according to the default
     * {@link cc.redberry.core.context.OutputFormat} defined in
     * {@link cc.redberry.core.context.CC#getDefaultOutputFormat()}.
     *
     * @return a string representation of this
     */
    String toString();//todo replace with extension method
}
