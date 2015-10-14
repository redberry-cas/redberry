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
package cc.redberry.core.transformations.options;

import cc.redberry.core.transformations.Transformation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TransformationBuilder {
    public static <T> T
    buildFromMap(Class<T> clazz, Map<String, Object> optionsMap)
            throws Exception {
        T options = clazz.newInstance();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Option.class)) {
                Option descr = field.getAnnotation(Option.class);
                Object value = optionsMap.get(descr.name());
                if (value != null)
                    field.set(options, value);
                else if (descr.required() || (!field.getType().isPrimitive() && field.get(options) == null))
                    throw new IllegalArgumentException("Value for " + descr.name() + " option is not specified.");
            }
        }
        return options;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Transformation> T
    createTransformation(Class<T> clazz, List<Object> arguments, Map<String, Object> options)
            throws Exception {
        Constructor<?> creator = null;
        Creator creatorAnnotation = null;
        for (Constructor<?> constructor : clazz.getConstructors()) {
            creatorAnnotation = constructor.getAnnotation(Creator.class);
            if (creatorAnnotation != null) {
                creator = constructor;
                break;
            }
        }

        if (creator == null)
            throw new RuntimeException("No constructor.");

        Class optionsClass = null;
        Annotation[][] argAnnotations = creator.getParameterAnnotations();
        Class<?>[] argTypes = creator.getParameterTypes();
        out:
        for (int i = 0; i < argAnnotations.length; i++) {
            Annotation[] cpa = argAnnotations[i];
            if (cpa.length > 0) {
                for (Annotation annotation : cpa) {
                    if (annotation.annotationType().equals(Options.class)) {
                        optionsClass = argTypes[i];
                        break out;
                    }
                }
            }
        }

        if (optionsClass != null) {
            Object opts = buildFromMap(optionsClass, options);
            Object[] initargs;
            if (creatorAnnotation.vararg()) {
                initargs = new Object[]{arguments.toArray((Object[]) Array.newInstance(argTypes[0].getComponentType(), 0)), opts};
            } else {
                initargs = new Object[arguments.size() + 1];
                for (int i = 0; i < arguments.size(); i++)
                    initargs[i] = arguments.get(i);
                initargs[arguments.size()] = opts;
            }
            return (T) creator.newInstance(initargs);
        } else {
            if (options != null)
                throw new IllegalArgumentException("Non null options.");
            return (T) creator.newInstance(arguments.toArray());
        }
    }
}
