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

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.tensor.iterator.TraverseState;
import cc.redberry.core.tensor.iterator.TreeTraverseIterator;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class Transformer implements Transformation {

    private final TraverseState state;
    private final Transformation[] transformations;
    private final TraverseGuide guide;

    public Transformer(TraverseState state, Transformation[] transformations, TraverseGuide guide) {
        this.state = state;
        this.transformations = transformations;
        this.guide = guide;
    }

    public Transformer(TraverseState state, Transformation[] transformations) {
        this.state = state;
        this.transformations = transformations;
        this.guide = TraverseGuide.ALL;
    }

    @Override
    public Tensor transform(Tensor t) {
        TreeTraverseIterator iterator = new TreeTraverseIterator(t, guide);
        TraverseState currentState;
        Tensor currentTensor, newTensor;
        while ((currentState = iterator.next()) != null) {
            if (currentState != state)
                continue;
            currentTensor = newTensor = iterator.current();

            for (Transformation transformation : transformations)
                newTensor = transformation.transform(newTensor);
            if (currentTensor != newTensor)
                iterator.set(newTensor);
        }
        return iterator.result();
    }
}
