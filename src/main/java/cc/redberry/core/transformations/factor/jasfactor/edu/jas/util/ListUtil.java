/*
 * JAS: Java Algebra System.
 *
 * Copyright (c) 2000-2012:
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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.util;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Element;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.UnaryFunctor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * List utilities. For example map functor on list elements.
 *
 * @author Heinz Kredel
 */

public class ListUtil {


    // private static boolean debug = false;


    /**
     * Map a unary function to the list.
     *
     * @param f evaluation functor.
     * @return new list elements f(list(i)).
     */
    public static <C extends Element<C>, D extends Element<D>> List<D> map(List<C> list, UnaryFunctor<C, D> f) {
        if (list == null) {
            return null;
        }
        List<D> nl;
        if (list instanceof ArrayList) {
            nl = new ArrayList<>(list.size());
        } else if (list instanceof LinkedList) {
            nl = new LinkedList<>();
        } else {
            throw new RuntimeException("list type not implemented");
        }
        for (C c : list) {
            D n = f.eval(c);
            nl.add(n);
        }
        return nl;
    }

}
