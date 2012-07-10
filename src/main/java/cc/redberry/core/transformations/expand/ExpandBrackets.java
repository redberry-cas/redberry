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
package cc.redberry.core.transformations.expand;

import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.tensor.iterator.TraverseState;
import cc.redberry.core.tensor.iterator.TreeTraverseIterator;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.Indicator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpandBrackets implements Transformation {

    private final Indicator<Tensor> indicator;

    public ExpandBrackets(Indicator<Tensor> indicator) {
        this.indicator = indicator;
    }

    public ExpandBrackets() {
        this.indicator = Indicator.TRUE_INDICATOR;
    }

    @Override
    public Tensor transform(Tensor tensor) {
        return expandBrackets(tensor, indicator);
    }

    public static Tensor expandBrackets(Tensor tensor, Indicator<Tensor> indicator) {
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor, TraverseGuide.EXCEPT_FUNCTIONS_AND_FIELDS);
        TraverseState state;
        Tensor current;
        while ((state = iterator.next()) != null) {
            if (state != TraverseState.Entering)
                continue;
            current = iterator.current();
            if (!(current instanceof Product))
                continue;
            ArrayDeque<Sum> indexlessSums = new ArrayDeque<>();
            ArrayDeque<Sum> sums = new ArrayDeque<>();
            ArrayList<Tensor> nonSums = new ArrayList<>();

            int i;
            Tensor t;
            for (i = current.size() - 1; i >= 0; --i) {
                t = current.get(i);
                if (t instanceof Sum)
                    if (t.getIndices().size() == 0)
                        indexlessSums.push((Sum) t);
                    else
                        sums.push((Sum) t);
                else
                    nonSums.add(t);
            }
            
            if(sums.isEmpty() && indexlessSums.isEmpty())
                continue;
            
            Sum s1, s2;
            Tensor temp;
            while (sums.size() > 1) {
                s1 = sums.poll();
                s2 = sums.poll();
                temp = ExpandUtils.expandPairOfSums(s1, s2);
                if (temp instanceof Sum)
                    sums.add((Sum) temp);
                else
                    nonSums.add(temp);
            }
            while (indexlessSums.size() > 1) {
                s1 = indexlessSums.poll();
                s2 = indexlessSums.poll();
                temp = ExpandUtils.expandPairOfSums(s1, s2);
                if (temp instanceof Sum)
                    indexlessSums.add((Sum) temp);
                else
                    nonSums.add(temp);
            }
            if (sums.isEmpty()) {
                if (indexlessSums.isEmpty())
                   iterator.set(UnsafeTensors.unsafeMultiplyWithoutIndicesRenaming(nonSums.toArray(new Tensor[nonSums.size()])));
                Sum sum = indexlessSums.peek();
                Tensor[] newSum = new Tensor[sum.size()];
                for (i = sum.size() - 1; i >= 0; --i)
                    newSum[i] = multiply(nonSums, sum.get(i));
                iterator.set(UnsafeTensors.unsafeSum(newSum));
            } else {
            }
        }

        return iterator.result();
    }

    private static Tensor multiply(ArrayList<Tensor> list, Tensor tensor) {
        ProductBuilder builder = new ProductBuilder();
        for (Tensor t : list)
            builder.put(t);
        builder.put(tensor);
        return builder.build();
    }
}
