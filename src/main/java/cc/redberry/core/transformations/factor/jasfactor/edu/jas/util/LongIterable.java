/*
 * JAS: Java Algebra System.
 *
 * Copyright (c) 2000-2012:
 *    Heinz Kredel   <kredel@rz.uni-mannheim.de>
 *
 * This file is part of Java Algeba System (JAS).
 *
 * JAS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * JAS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAS. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * $Id$
 */

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.util;


import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Iterable for Long.
 *
 * @author Heinz Kredel
 */
public class LongIterable implements Iterable<Long> {


    private boolean nonNegative = true;


    private long upperBound = Long.MAX_VALUE;


    /**
     * Constructor.
     */
    public LongIterable() {
    }


    /**
     * Set the iteration algorithm to non-negative elements.
     */
    public void setNonNegativeIterator() {
        nonNegative = true;
    }


    /**
     * Get an iterator over Long.
     *
     * @return an iterator.
     */
    public Iterator<Long> iterator() {
        return new LongIterator(nonNegative, upperBound);
    }

}


/**
 * Long iterator.
 *
 * @author Heinz Kredel
 */
class LongIterator implements Iterator<Long> {


    /**
     * data structure.
     */
    long current;


    boolean empty;


    final boolean nonNegative;


    protected long upperBound;


    /**
     * Long iterator constructor.
     *
     * @param nn true for an iterator over non-negative longs, false for all
     *           elements iterator.
     * @param ub an upper bound for the entries.
     */
    public LongIterator(boolean nn, long ub) {
        current = 0L;
        empty = false;
        nonNegative = nn;
        upperBound = ub;
    }


    /**
     * Test for availability of a next long.
     *
     * @return true if the iteration has more Longs, else false.
     */
    public synchronized boolean hasNext() {
        return !empty;
    }


    /**
     * Get next Long.
     *
     * @return next Long.
     */
    public synchronized Long next() {
        if (empty) {
            throw new NoSuchElementException("invalid call of next()");
        }
        Long res = current;
        if (nonNegative) {
            current++;
        } else if (current > 0L) {
            current = -current;
        } else {
            current = -current;
            current++;
        }
        if (current > upperBound) {
            empty = true;
        }
        return res;
    }


    /**
     * Remove a tuple if allowed.
     */
    public void remove() {
        throw new UnsupportedOperationException("cannnot remove elements");
    }

}
