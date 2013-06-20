/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
package cc.redberry.core.graph;

/**
 * Some basic types of graphs that are used to interpret matrices products.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1
 */
public enum GraphType {
    /**
     * The graph of form A^{i<sub>1</sub>}_{i<sub>2</sub>}*B^{i<sub>2</sub>}_{i<sub>3</sub>}*...*C^{i<sub>N</sub>}_{i<sub>1</sub>},
     * where {i<sub>j</sub>} denotes the whole set of tensor indices. Tensors of such structure have no free indices of considered
     * index type.
     */
    Cycle,
    /**
     * The graph of form A^{i<sub>1</sub>}_{i<sub>2</sub>}*B^{i<sub>2</sub>}_{i<sub>3</sub>}*...*C^{i<sub>N-1</sub>}_{i<sub>N</sub>},
     * where {i<sub>j</sub>} denotes the whole set of tensor indices.
     */
    Line,
    /**
     * Not cycle or line
     */
    Graph
}
