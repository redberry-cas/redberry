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

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.options.TransformationBuilder;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.math.BigInteger;
import java.util.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class DSLTransformation<T extends Transformation> {
    public final Class<T> clazz;

    public DSLTransformation(Class<T> clazz) {
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    public Transformation getAt(List args, Map map) throws Exception {
        return TransformationBuilder.createTransformation(clazz, toT(args), toT(map));
    }

    @SuppressWarnings("unchecked")
    public Transformation getAt(List args, List options) throws Exception {
        return TransformationBuilder.createTransformation(clazz, toT(args), toT(options));
    }

    @SuppressWarnings("unchecked")
    public Transformation getAt(final List options) throws Exception {
        if (options.get(options.size() - 1) instanceof Transformation) {
            return TransformationBuilder.createTransformation(clazz,
                    toT(options.subList(0, options.size() - 1)),
                    toT(new ArrayList() {{add(options.get(options.size() - 1));}}));
        } else if (options.get(options.size() - 1) instanceof List) {
            return TransformationBuilder.createTransformation(clazz,
                    toT(options.subList(0, options.size() - 1)),
                    toT((List) options.get(options.size() - 1)));
        } else if (options.get(options.size() - 1) instanceof Map) {
            return TransformationBuilder.createTransformation(clazz,
                    toT(options.subList(0, options.size() - 1)),
                    toT((Map) options.get(options.size() - 1)));
        } else
            return TransformationBuilder.createTransformation(clazz, toT(options));
    }

    @SuppressWarnings("unchecked")
    public Transformation getAt(Map map) throws Exception {
        return TransformationBuilder.createTransformation(clazz, Collections.emptyList(), toT(map));
    }

    @SuppressWarnings("unchecked")
    public Transformation getAt(Object o) throws Exception {
        return TransformationBuilder.createTransformation(clazz, Collections.singletonList(toT(o)));
    }

    @SuppressWarnings("unchecked")
    public Transformation getAt(String o) throws Exception {
        return TransformationBuilder.createTransformation(clazz, Collections.singletonList(toT(o)));
    }

    @SuppressWarnings("unchecked")
    private static List toT(List args) {
        List l = new ArrayList(args);
        ListIterator it = l.listIterator();
        while (it.hasNext())
            it.set(toT(it.next()));
        return l;
    }

    @SuppressWarnings("unchecked")
    private static Map toT(Map args) {
        Map m = new HashMap(args);
        Iterator<Map.Entry> it = m.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = it.next();
            e.setValue(toT(e.getValue()));
        }
        return m;
    }

    private static Object toT(Object o) {
        try {
            if (o instanceof String)
                return Tensors.parse((String) o);
            else if (o instanceof Closure)
                return DefaultGroovyMethods.asType(o, Transformation.class);
            else if (o instanceof Number) {
                if (o instanceof BigInteger)
                    return new Complex((BigInteger) o);
                else if (o instanceof Long || o instanceof Integer)
                    return new Complex(((Number) o).longValue());
                else if (o instanceof Float || o instanceof Double)
                    return new Complex(((Number) o).doubleValue());
                else
                    return new Complex(((Number) o).longValue());
            } else
                return o;
        } catch (Throwable e) {
            return o;
        }
    }
}
