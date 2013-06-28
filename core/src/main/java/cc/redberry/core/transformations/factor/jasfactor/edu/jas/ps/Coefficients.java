/*
 * JAS: Java Algebra System.
 *
 * Copyright (c) 2000-2013:
 *    Heinz Kredel   <kredel@rz.uni-mannheim.de>
 *
 * This file is part of Java Algebra System (JAS).
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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.ps;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;

import java.io.Serializable;
import java.util.HashMap;


/**
 * Abstract class for generating functions for coefficients of power series. Was
 * an interface, now this class handles the caching itself.
 *
 * @param <C> ring element type
 * @author Heinz Kredel
 */

public abstract class Coefficients<C extends RingElem<C>> implements Serializable {


    /**
     * Cache for already computed coefficients.
     */
    public final HashMap<Integer, C> coeffCache;


    /**
     * Public no arguments constructor.
     */
    public Coefficients() {
        this(new HashMap<Integer, C>());
    }


    /**
     * Public constructor with pre-filled cache.
     *
     * @param cache pre-filled coefficient cache.
     */
    public Coefficients(HashMap<Integer, C> cache) {
        coeffCache = cache;
    }


    /**
     * Get cached coefficient or generate coefficient.
     *
     * @param index of requested coefficient.
     * @return coefficient at index.
     */
    public C get(int index) {
        if (coeffCache == null) {
            return generate(index);
        }
        Integer i = index;
        C c = coeffCache.get(i);
        if (c != null) {
            return c;
        }
        c = generate(index);
        coeffCache.put(i, c);
        return c;
    }


    /**
     * Generate coefficient.
     *
     * @param index of requested coefficient.
     * @return coefficient at index.
     */
    protected abstract C generate(int index);

}
