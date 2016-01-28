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
import cc.redberry.core.context.ContextEvent;
import cc.redberry.core.context.ContextListener;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationToStringAble;
import cc.redberry.core.transformations.options.TransformationBuilder;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class DSLTransformationInst<T extends Transformation>
        extends DSLTransformation<T> implements TransformationToStringAble,
        ContextListener {
    protected volatile T instance = null;

    @SuppressWarnings("unchecked")
    public DSLTransformationInst(T instance) {
        super((Class<T>) instance.getClass());
        this.instance = instance;
        /* no need to register as listener since default instance provided */
    }

    @SuppressWarnings("unchecked")
    DSLTransformationInst(Class<T> clazz) {
        super(clazz);
        CC.current().registerListener(this);
    }

    private void ensureInstanceCreated() {
        if (instance == null)
            synchronized (this) {
                if (instance == null)
                    try {
                        instance = TransformationBuilder.createTransformationWithDefaultOptions(clazz);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
            }
    }

    @Override
    public void onEvent(ContextEvent event) {
        if (event == ContextEvent.RESET)
            instance = null;
    }

    @Override
    public Tensor transform(Tensor t) {
        ensureInstanceCreated();
        return instance.transform(t);
    }

    @Override
    public String toString() {
        return toString(CC.getDefaultOutputFormat());
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        ensureInstanceCreated();
        if (instance instanceof TransformationToStringAble)
            return ((TransformationToStringAble) instance).toString(outputFormat);
        else return instance.toString();
    }
}
