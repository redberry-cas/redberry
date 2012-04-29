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
package cc.redberry.core.context.defaults;

import cc.redberry.core.context.Context;
import cc.redberry.core.context.ContextFactory;
import cc.redberry.core.context.ContextSettings;

public class DefaultContextFactory implements ContextFactory {
    public static final DefaultContextFactory INSTANCE = new DefaultContextFactory();
   
    private DefaultContextFactory() {
    }

    @Override
    public Context createContext() {
        //Creating context defaults
        Context context = new Context(ContextSettings.createDefault());
        /*
         * ************************** DEFAULT INDEXCONVERTER TYPES *****************
         * 
         * Default class provides int <---> symbol convertation.
         * Default types id are
         * 
         *          Symbol                                      Type Code          Name String
         * 
         * Latin down case letters a....z                           0              Coordinate
         * 
         * Latin upper case letters A...Z                           1              Group SU(N)
         * 
         * Greek LaTeX down case letters  \alpha....\beta           2              Local Lorentz
         * 
         * Greek LaTeX upper case letters \Alpha....\Beta           3              Special Group
         */

        return context;
    }
}
