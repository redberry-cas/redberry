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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;


/**
 * Modular ring factory interface. Defines chinese remainder method and get
 * modul method.
 *
 * @author Heinz Kredel
 */

public interface ModularRingFactory<C extends RingElem<C> & Modular> extends RingFactory<C> {


    /**
     * Return the BigInteger modul for the factory.
     *
     * @return a BigInteger of this.modul.
     */
    public BigInteger getIntegerModul();


    /**
     * Chinese remainder algorithm. Assert c.modul >= a.modul and c.modul *
     * a.modul = this.modul.
     *
     * @param c  modular.
     * @param ci inverse of c.modul in ring of a.
     * @param a  other ModLong.
     * @return S, with S mod c.modul == c and S mod a.modul == a.
     */
    public C chineseRemainder(C c, C ci, C a);

}
