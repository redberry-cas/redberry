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

package cc.redberry.core.combinatorics.symmetries;

import cc.redberry.core.combinatorics.InconsistentGeneratorsException;
import cc.redberry.core.combinatorics.PermutationsSpanIterator;
import cc.redberry.core.combinatorics.Symmetry;
import java.util.List;

/**
 * This interface is a representation of a set of symmetries, taking into
 * account possible compositions of symmetries in this set. More formally, set
 * contain no element s, which can be expressed as s=s1*s2*s3*...*sN, where
 * {s1,s2,s3,...,sN} are elements of this set. So, this interface models the
 * mathematical <i>basis of symmetries</i>. It also contains an identity
 * symmetry by default.
 *
 * <p>All the symmetries, keeping by this set must be consistent. First of all
 * it means, that all symmetries should have similar dimension (see
 * {@link Symmetry#dimension()}). Next, if we denote symmetry as pair
 * <i>(permutation, sign)</i>, there is no any symmetry <i>(p,s)</i> and subset
 * of symmetries {<i>(p1,s1)</i>,<i>(p2,s2)</i>,...,<<i>(pN,sN)</i>}, such that
 * <i>p = p1*p2*p3*...*pN</i>, but <i>s != s1*s2*s3*...*sN</i>. Method
 * {@link #add(cc.redberry.core.combinatorics.Symmetry)} throws
 * {@link InconsistentGeneratorsException} if its argument is inconsistent with
 * already added to this set symmetries.
 *
 * <p>Iterator returned by this set iterate over basis symmetries (which are
 * contains in this set) and all possible symmetries, which can be obtained by
 * composing the basis symmetries, i.e. it works as
 * {@link PermutationsSpanIterator}. To extract only the basis symmetries,
 * special method {@link #getBasisSymmetries() } introduced. This method returns
 * an unmodifiable list of basis symmetries.
 *
 * @see Symmetry
 * @see PermutationsSpanIterator
 * @see InconsistentGeneratorsException
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public interface Symmetries extends Iterable<Symmetry> {

    /**
     * Returns the dimension of the symmetries contained in this set. Other
     * words, returns {@link Symmetry#dimension()} (since all symmetries in set
     * have similar dimension).
     *
     * @return dimension of the symmetries contained in this set
     */
    int dimension();

    /**
     * Returns <tt>true</tt> if and only if this set contains only identity
     * symmetry and <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if and only if this set contains only identity
     *         symmetry and <tt>false</tt> otherwise
     */
    boolean isEmpty();

    /**
     * Adds the specified symmetry to this set if it is not already present.
     * More detailed, adds if and only if there is no any subset of symmetries
     * {s1,s2,s3,...,sN} in this set, such that specified symmetry is equal to
     * the composition s1*s2*s2*...*sN.
     *
     * If specified symmetry is not consistent with other set symmetries in the
     * sense described in the class description, this method throws
     * {@link InconsistentGeneratorsException}.
     *
     *
     * @param symmetry {@code Symmetry} to be added to this set
     *
     * @throws InconsistentGeneratorsException if the specified symmetry is
     *                                         inconsistent with other set
     *                                         symmetries in the sense described
     *                                         in the class description
     */
    boolean add(Symmetry symmetry)
            throws InconsistentGeneratorsException;

    boolean addUnsafe(Symmetry symmetry);

    /**
     * This method returns an unmodifiable list of basis symmetries, containing
     * in this set.
     *
     * @return unmodifiable list of basis symmetries
     */
    List<Symmetry> getBasisSymmetries();

    /**
     * Returns a deep clone of this set.
     *
     * @return a deep clone of this set
     */
    Symmetries clone();
}
