/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
package cc.redberry.groovy;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationToStringAble;
import cc.redberry.core.transformations.options.TransformationBuilder;

import java.util.Collections;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class DSLTransformationInst<T extends Transformation>
        extends DSLTransformation<T> implements TransformationToStringAble {
    private final T instance;

    @SuppressWarnings("unchecked")
    public DSLTransformationInst(T instance) {
        super((Class<T>) instance.getClass());
        this.instance = instance;
    }

    @SuppressWarnings("unchecked")
    public DSLTransformationInst(Class<T> clazz) throws Exception {
        super(clazz);
        this.instance = null;
    }

    @Override
    public Tensor transform(Tensor t) {
        if (instance == null)
            try {
                return TransformationBuilder.createTransformation(clazz, Collections.emptyList()).transform(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        else
            return instance.transform(t);
    }

    @Override
    public String toString() {
        return toString(CC.getDefaultOutputFormat());
    }

    private T dummyInstance = null;

    @Override
    public String toString(OutputFormat outputFormat) {
        if (dummyInstance == null) {
            if (this.instance != null)
                dummyInstance = this.instance;
            else
                try {
                    dummyInstance = TransformationBuilder.createTransformation(clazz, Collections.emptyList());
                } catch (Exception e) {
                    return super.toString();
                }
        }
        if (dummyInstance instanceof TransformationToStringAble)
            return ((TransformationToStringAble) dummyInstance).toString(outputFormat);
        else
            return dummyInstance.toString();
    }
}
