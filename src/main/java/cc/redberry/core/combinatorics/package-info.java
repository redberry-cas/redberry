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
/**
 * <p>This package provides a number of useful combinatorial algorithms. It includes two main types of classes:
 * classes aimed on the enumeration of a particular kinds of combinations, and classes providing facilities to
 * operate with single permutations and symmetric groups.</p>
 *
 * <p><b><i>Enumerating combinations.</i></b> All of the classes, which implements the algorithms of combinations enumeration
 * are strictly follows a common pattern of output port, which is specified in
 * {@link cc.redberry.core.combinatorics.IntCombinatorialPort}. The calculation of the next combination occurs
 * strictly on the invocation of method {@link cc.redberry.core.combinatorics.IntCombinatorialPort#take()} and the
 * returned array is always the same reference. Some of these classes are also implements {@link java.util.Iterator} and {@link Iterable} interfaces for convenience.
 * <table>
 *     <tr>
 *         <td><b>List of enumeration algorithms:</b></td>
 *         <td></td>
 *     </tr>
 *     <tr>
 *         <td>{@link IntPermutationsGenerator}</td>
 *         <td>Enumerates all permutations of dimension N ( N! permutations). </td>
 *     </tr>
 *     <tr>
 *         <td>{@link IntCombinationsGenerator}</td>
 *         <td>Enumerates all combinations of K elements chosen as N ( N!/(K!(N-K)!) combinations). </td>
 *     </tr>
 *     <tr>
 *         <td>{@link IntCombinationPermutationGenerator}</td>
 *         <td>Enumerates all combinations with permutations of K elements chosen as N ( N!/(N-K)! combinations). </td>
 *     </tr>
 *     <tr>
 *         <td>{@link IntDistinctTuplesPort}</td>
 *         <td>Enumerates all distinct N-tuples, which can be chosen from {@code N} sets of integers. </td>
 *     </tr>
 *     <tr>
 *         <td>{@link IntTuplesPort}</td>
 *         <td>Enumerates all N-tuples, which can be chosen from {@code N} sets of integers of the form
 *               <i>array</i><sub>i</sub> = [0, 1, 2, ..., K<sub>i</sub>]. </td>
 *     </tr>
 *     <tr>
 *         <td>{@link IntPriorityPermutationsGenerator}</td>
 *         <td>Enumerates all permutations of dimension N ( N! permutations) and allows to affect on the
 *              enumeration order.</td>
 *     </tr>
 *     <tr>
 *         <td>{@link IntPermutationsSpanGenerator}</td>
 *         <td>Enumerates all permutations from the subgroup of a symmetric group, which is defined by a generating set.</td>
 *     </tr>
 * </table>
 * </p>
 *
 * <p><b><i>Permutations.</i></b> Permutations are represented in the one-line notation and implemented in
 * {@link Permutation}. Class {@link Symmetry} implements a permutational symmetry. Class
 * {@link PermutationsSpanIterator} allows to enumerate all permutations (or symmetries) from a given group, defined by
 * a generating set.
 * </p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
package cc.redberry.core.combinatorics;
