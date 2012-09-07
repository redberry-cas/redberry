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

import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.*;
import java.util.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class Together implements Transformation {

    @Override
    public Tensor transform(Tensor t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static Tensor together(Tensor t) {
        TensorLastIterator iterator = new TensorLastIterator(t);
        Tensor c;
        while ((c = iterator.next()) != null){
            if (!(c instanceof Sum))
                continue;
        }
        return t;
    }
    
    private static Tensor togetherSum(Tensor t){
        HashMap<Tensor, Tensor> denominators = new HashMap<>();
        
        return t;
    }
    
    private static class Denominator{
        final Tensor denominator;
        Tensor power;

        public Denominator(Tensor denominator, Tensor power) {
            this.denominator = denominator;
            this.power = power;
        }
        
    }
}
