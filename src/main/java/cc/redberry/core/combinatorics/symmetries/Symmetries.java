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
package cc.redberry.core.combinatorics.symmetries;

import cc.redberry.core.combinatorics.InconsistentGeneratorsException;
import cc.redberry.core.combinatorics.PermutationsSpanIterator;
import cc.redberry.core.combinatorics.Symmetry;

import java.util.Iterator;
import java.util.List;

/**
 * <p>This interface is a representation of a set of symmetries, which takes into
 * account possible compositions of symmetries in this set. More formally, set
 * contains no element s, which can be expressed as s=s1*s2*s3*...*sN, where
 * {s1,s2,s3,...,sN} are elements of this set. So, this interface models the
 * mathematical <i>basis of subgroup of symmetric group</i>. It contains the identity
 * symmetry by default.
 * </p>
 * <p/>
 * <p>All symmetries, keeping by this set must be consistent. It means, that all
 * of them must have similar dimension (see {@link #dimension()});
 * next, if we denote symmetry as pair <i>(permutation, sign)</i>, there is no
 * any symmetry <i>(p,s)</i> and subset of symmetries {<i>(p1,s1)</i>,<i>(p2,s2)</i>,...,<<i>(pN,sN)</i>},
 * such that <i>p = p1*p2*p3*...*pN</i>, but <i>s != s1*s2*s3*...*sN</i>. Method
 * {@link #add(cc.redberry.core.combinatorics.Symmetry)} throws {@link InconsistentGeneratorsException}
 * if its argument is inconsistent with existing symmetries in set.
 * </p>
 * <p/>
 * <p>Iterator returned by this set iterates over all possible compositions
 * of symmetries from set, i.e. it works as {@link PermutationsSpanIterator}.
 * To extract only the basis symmetries, a special method {@link #getBasisSymmetries() }
 * introduced. This method returns an unmodifiable list of basis symmetries.</p>
 * <p/>
 * <p>Objects of this type can be created via static factory methods
 * in {@link SymmetriesFactory}.</p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see SymmetriesFactory
 * @see Symmetry
 * @see PermutationsSpanIterator
 * @see InconsistentGeneratorsException
 */
public interface Symmetries extends Iterable<Symmetry> {

    /**
     * Returns the dimension of the symmetries, which contained in this set.
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
     * Adds the specified symmetry to this set if it cannot be expressed as combination of
     * existing symmetries from this set.
     * <p>More detailed, adds if and only if there is no any subset of symmetries
     * {s1,s2,s3,...,sN} in this set, such that specified symmetry is equal to
     * the composition s1*s2*s2*...*sN.</p>
     * <p>If specified symmetry is not consistent with others from this set, in the
     * sense described in the class description, this method throws
     * {@link InconsistentGeneratorsException}.</p>
     *
     * @param symmetry {@code Symmetry} to be added to this set
     * @return {@code true} if symmetry was added (i.e. it cannot
     *         be expressed through existing symmetries in the set),
     *         and {@code false} otherwise
     * @throws InconsistentGeneratorsException
     *          if the specified symmetry is
     *          inconsistent with other symmetries from this set
     */
    boolean add(Symmetry symmetry)
            throws InconsistentGeneratorsException;

    /**
     * Adds specified symmetry to this set without any checks.
     *
     * @param symmetry symmetry
     * @return {@code true}
     */
    boolean addUnsafe(Symmetry symmetry);

    /**
     * This method returns an unmodifiable list of basis symmetries, which contained
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

    /**
     * Returns the iterator over the all possible symmetries,
     * which can be obtained by combination symmetries from this set. I.e. over
     * all elements in the corresponding subgroup of symmetric group.
     *
     * @return iterator over the all symmetries, which can be obtained by
     *         combination symmetries from this set
     */
    @Override
    Iterator<Symmetry> iterator();
}
