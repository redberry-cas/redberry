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
package cc.redberry.core.transformations;

import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Collect implements Transformation{

    public static interface CollectMatcher {

        boolean matches(Tensor t);
    }

    public static final class CollectMatcherImpl implements CollectMatcher {

        private final SimpleTensor[] patterns;

        public CollectMatcherImpl(SimpleTensor... patterns) {
            this.patterns = patterns;
        }

        @Override
        public boolean matches(Tensor t) {
            if (t instanceof SimpleTensor) {
                int name = ((SimpleTensor) t).getName();
                for (SimpleTensor p : patterns)
                    if (p.getName() == name)
                        return true;
            }
            return false;
        }
    }
    
    private final CollectMatcher matcher;

    public Collect(CollectMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public Tensor transform(Tensor t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    public static Tensor collect(Tensor tensor, CollectMatcher matcher){
        
        
        
        return tensor;
    }
}
