/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
 * This package provides implementation of permutations and permutation groups.
 * <p><big><b>Permutations</b></big></p>
 * <p>
 * The interface {@link cc.redberry.core.groups.permutations.Permutation} describes a wide functionality of a single
 * permutation; it has several implementations ({@link cc.redberry.core.groups.permutations.PermutationOneLineByte},
 * {@link cc.redberry.core.groups.permutations.PermutationOneLineInt} etc.) and in order to create permutation in the
 * appropriate implementation (from the stand point of memory consumption) one should use static methods {@code createPermutation(...)} in
 * {@link cc.redberry.core.groups.permutations.Permutations} class.
 * <p><b>Symmetries and antisymmetries</b>
 * In many physical applications, permutations arise as symmetries of some mathematical structures. For example,
 * consider a function of 3 variables with the following property: f(x,y,z) == f(z,x,y). This symmetry can be put in
 * one-to-one correspondence with a permutation [2,0,1] acting on a set [x,y,z]. Generally, it is accepted to consider a more
 * general properties --- antisymmetries, which can additionally change the sigh of the function from the above example,
 * e.g. f(x,y,z) == -f(z,y,x). In order to take into account antisymmetries, each {@code Permutation} may have additional property ---
 * antisymmetry, which can have two values: +1 or -1. According to the sense of antisymmetry, this property simply
 * multiplies under the composition of permutations.
 * </p>
 * <p><big><b>Permutation groups</b></big></p>
 * <p>
 * Permutation groups are described in the {@link cc.redberry.core.groups.permutations.PermutationGroup} class. This
 * class provides a wide range of standard methods including membership testing, coset enumeration, searching for
 * centralizers, stabilizers, etc. The implementation is based on a <i>base and strong generating set (BSGS)</i>
 * (see <b>[Holt05]</b>). The description of BSGS data structure as well as algorithms for constructing, modifying and
 * manipulating with BSGS are placed in {@link cc.redberry.core.groups.permutations.AlgorithmsBase} class. Algorithms
 * for searching subgroups in permutation groups using backtracking are placed in
 * {@link cc.redberry.core.groups.permutations.AlgorithmsBacktrack} class.
 * </p>
 * <p>
 * <b><big>Literature:</big></b>
 * <br></br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp; <b>[Holt05]</b> Derek F. Holt, Bettina Eick, Eamonn A. O'Brien, <i>Handbook Of Computational Group Theory</i>, Chapman and Hall/CRC, 2005
 * </br>
 * </p>
 *
 * @see cc.redberry.core.groups.permutations.Permutation
 * @see cc.redberry.core.groups.permutations.PermutationOneLineInt
 * @see cc.redberry.core.groups.permutations.PermutationGroup
 * @see cc.redberry.core.groups.permutations.AlgorithmsBase
 * @see cc.redberry.core.groups.permutations.AlgorithmsBacktrack
 */
package cc.redberry.core.groups.permutations;