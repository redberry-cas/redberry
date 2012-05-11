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

import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.utils.ArrayIterator;

import java.util.Iterator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorField extends SimpleTensor {
    private Tensor[] args;
    private SimpleIndices[] argIndices;

    public TensorField(int name, SimpleIndices indices, Tensor[] args) {
        super(name, indices);
        this.args = args;
        argIndices = new SimpleIndices[args.length];
        int i = 0;
        for (Tensor t : args)
            argIndices[i++] = IndicesFactory.createSimple(null,t.getIndices().getFreeIndices());
    }

    public TensorField(int name, SimpleIndices indices, Tensor[] args, SimpleIndices[] argIndices) {
        super(name, indices);
        this.args = args;
        this.argIndices = argIndices;
    }

    public SimpleIndices[] getArgIndices() {
        return argIndices;
    }

    @Override
    public Iterator<Tensor> iterator() {
        return new ArrayIterator<Tensor>(args);
    }



    @Override
    public TensorContent getContent() {
        return new TensorContentImpl(args);
    }

//    @Override
//    public TensorField clone() {
//        Tensor[] _args = new Tensor[args.length];
//        SimpleIndices[] _argIndices = new SimpleIndices[args.length];
//        for (int i = 0; i < args.length; ++i) {
//            _args[i] = args[i].clone();
//            _argIndices[i] = argIndices[i].clone();
//        }
//        return new TensorField(name, indices.clone(), true, _argIndices, _args);
//    }
//
//    @Override
//    public String toString(ToStringMode mode) {
//        StringBuilder sb = new StringBuilder();
//        sb.append('[');
//        for (Tensor t : args) {
//            sb.append(t.toString(mode));
//            sb.append(',');
//        }
//        sb.deleteCharAt(sb.length() - 1);
//        sb.append(']');
//        return super.toString(mode) + sb.toString();
//    }
//
//    public static Indicator<TensorIterator> FieldIteratorIndicator(final Indicator<TensorField> fieldIndicator) {
//        if (fieldIndicator == null)
//            return fieldIteratorIndicator;
//        return new AbstractTensorIteratorIndicator() {
//            @Override
//            public boolean _is(TensorIterator object) {
//                return (object instanceof FieldIterator) && fieldIndicator.is(((FieldIterator) object).field());
//            }
//        };
//    }
//
//    public static final Indicator<TensorIterator> fieldIteratorIndicator = new AbstractTensorIteratorIndicator() {
//        @Override
//        public boolean _is(TensorIterator iterator) {
//            return iterator instanceof FieldIterator;
//        }
//    };
//
//    public static Indicator<TensorIterator> FieldIteratorOnArgumentNoIndicator(final int indexOfArgument) {
//        return new AbstractTensorIteratorIndicator() {
//            @Override
//            public boolean _is(TensorIterator iterator) {
//                return (iterator instanceof FieldIterator) && ((FieldIterator) iterator).index == indexOfArgument;
//            }
//        };
//    }
//
//    protected class FieldIterator extends AbstractTensorIterator {
//        int index = -1;
//        int size = args.length;
//
//        @Override
//        public void set(Tensor t) {
//            t.parent = TensorField.this;
//            args[index] = t;
//        }
//
//        @Override
//        public boolean hasNext() {
//            return index < size - 1;
//        }
//
//        @Override
//        public Tensor next() {
//            return args[++index];
//        }
//
//        @Override
//        public void remove() {
//            throw new UnsupportedOperationException();
//        }
//
//        public TensorField field() {
//            return TensorField.this;
//        }
//    }
}
