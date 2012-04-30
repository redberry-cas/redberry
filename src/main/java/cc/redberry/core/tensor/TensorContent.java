/*
 * org.redberry.concurrent: high-level Java concurrent library.
 * Copyright (c) 2010-2012.
 * Bolotin Dmitriy <bolotin.dmitriy@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 */
package cc.redberry.core.tensor;

/**
 * Many tensors have some child tensors. For sum and product it is summands and
 * factors respectively, for fraction it is numerator and denominator, etc...
 * This class represents abstract container for such child tensors. It is used
 * generalize some algorithms.<br/> It is only a root interface for tensor
 * containers. There are child interfaces for specific tensor types adding more
 * information about it's content.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public interface TensorContent {
    /**
     * Get tensor with specific index.
     *
     * @param position index of tensor
     * @return
     */
    Tensor get(int position);

    /**
     * Returns several tensors from this content.<br/><br/>
     * {@code assert result[i] == content.get(from + i) }
     *
     * @param from index of first tensor (inclusive)
     * @param to next index after last tensor (exclusive)
     * @return
     */
    Tensor[] getRange(int from, int to);

    //TODO comment
    Tensor[] getDataCopy();

    int size();

    /**
     * Empty tensor content singleton.
     */
    public static final TensorContent EMPTY = new TensorContent() {
        @Override
        public Tensor get(int position) {
            throw new IllegalStateException();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Tensor[] getRange(int from, int to) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Tensor[] getDataCopy() {
            return new Tensor[0];
        }
        
    };
}
