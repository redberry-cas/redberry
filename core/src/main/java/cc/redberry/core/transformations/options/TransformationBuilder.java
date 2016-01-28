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
import gnu.trove.map.hash.TIntObjectHashMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class TransformationBuilder {
    private TransformationBuilder() {
    }

    /**
     * Creates options from {@code map<string,object>}
     *
     * @param clazz      options class
     * @param optionsMap map of options
     * @param <T>        type of Options class
     * @return object representing options
     * @throws Exception
     */
    public static <T> T
    buildOptionsFromMap(Class<T> clazz, Map<String, Object> optionsMap)
            throws Exception {
        T options = clazz.newInstance();
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Option.class)) {
                Option option = field.getAnnotation(Option.class);
                Object optionValue = optionsMap.get(option.name());
                if (optionValue != null)
                    field.set(options, optionValue);
                //else if (option.required() || (!field.getType().isPrimitive() && field.get(options) == null))
                //    throw new IllegalArgumentException("Value for " + option.name() + " option is not specified.");
            }
        }
        triggerOptionsCreated(options);
        return options;
    }

    /**
     * Creates options from {@code List<object>}
     *
     * @param clazz       options class
     * @param optionsList list of options
     * @param <T>         type of Options class
     * @return object representing options
     * @throws Exception
     */
    public static <T> T
    buildOptionsFromList(Class<T> clazz, List optionsList)
            throws Exception {
        T options = clazz.newInstance();
        Field[] fields = clazz.getFields();

        TIntObjectHashMap<Field> byIndex = new TIntObjectHashMap<>();
        for (Field field : fields)
            if (field.isAnnotationPresent(Option.class))
                byIndex.put(field.getAnnotation(Option.class).index(), field);
        for (int i = 0; i < optionsList.size(); i++)
            byIndex.get(i).set(options, optionsList.get(i));
        triggerOptionsCreated(options);
        return options;
    }

    private static void triggerOptionsCreated(Object o) {
        if (o instanceof IOptions)
            ((IOptions) o).triggerCreate();
    }

    /**
     * Creates transformation of specified clazz with specified list of required arguments and a map of
     * options.
     *
     * @param clazz     {@link Transformation} class
     * @param arguments list of required arguments of transformation
     * @param options   {@code map<string,object>} map of options
     * @param <T>       transformation type
     * @return an instance of the transformation
     * @throws Exception
     */
    public static <T extends Transformation> T
    createTransformation(Class<T> clazz, List<Object> arguments, Map<String, Object> options)
            throws Exception {
        return createTransformation0(clazz, arguments, options);
    }

    /**
     * Creates transformation of specified clazz with specified list of required arguments and a map of
     * options.
     *
     * @param clazz              {@link Transformation} class
     * @param argumentsOrOptions a list of options or a list of arguments
     * @param <T>                transformation type
     * @return an instance of the transformation
     * @throws Exception
     */
    public static <T extends Transformation> T
    createTransformation(Class<T> clazz, List<Object> argumentsOrOptions)
            throws Exception {
        if (hasRequiredArguments(clazz))
            return (T) createTransformation0(clazz, argumentsOrOptions, Collections.EMPTY_MAP);
        else
            return (T) createTransformation0(clazz, Collections.EMPTY_LIST, argumentsOrOptions);
    }

    /**
     * Creates default instance of transformation of specified clazz using default Options value.
     *
     * @param clazz {@link Transformation} class
     * @param <T>   transformation type
     * @return an instance of the transformation
     * @throws Exception
     */
    public static <T extends Transformation> T
    createTransformationWithDefaultOptions(Class<T> clazz)
            throws Exception {
        if (hasRequiredArguments(clazz))
            throw new RuntimeException("Required arguments are not specified.");
        else {
            CreatorData c = getCreatorData(clazz);
            return (T) c.creator.newInstance(c.optionsClass.newInstance());
        }
    }

    /**
     * Creates transformation of specified clazz with specified list of required arguments and a list of
     * options.
     *
     * @param clazz     {@link Transformation} class
     * @param arguments list of required arguments of transformation
     * @param options   {@code List<object>} list of options
     * @param <T>       transformation type
     * @return an instance of the transformation
     * @throws Exception
     */
    public static <T extends Transformation> T
    createTransformation(Class<T> clazz, List<Object> arguments, List options)
            throws Exception {
        return createTransformation0(clazz, arguments, options);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Transformation> T
    createTransformation0(Class<T> clazz, List<Object> arguments, Object options)
            throws Exception {
        CreatorData c = getCreatorData(clazz);

        if (c.optionsClass == null)
            if (c.creatorAnnotation.vararg())
                return (T) c.creator.newInstance(new Object[]{
                        arguments.toArray((Object[]) Array.newInstance(c.argTypes[0].getComponentType(), arguments.size()))});
            else
                return (T) c.creator.newInstance(arguments.toArray());

        Object opts = null;
        if (options instanceof Map)
            opts = buildOptionsFromMap(c.optionsClass, (Map) options);
        else if (options instanceof List)
            opts = buildOptionsFromList(c.optionsClass, (List) options);

        Object[] initargs;
        if (c.creatorAnnotation.vararg()) {
            initargs = new Object[]{arguments.toArray(
                    (Object[]) Array.newInstance(c.argTypes[0].getComponentType(), arguments.size())),
                    opts};
        } else {
            initargs = new Object[arguments.size() + 1];
            for (int i = 0; i < arguments.size(); i++)
                initargs[i] = arguments.get(i);
            initargs[arguments.size()] = opts;
        }
        return (T) c.creator.newInstance(initargs);
    }

    private static boolean hasRequiredArguments(Class<? extends Transformation> clazz) {
        Creator creatorAnnotation;
        for (Constructor<?> constructor : clazz.getConstructors()) {
            creatorAnnotation = constructor.getAnnotation(Creator.class);
            if (creatorAnnotation != null)
                return creatorAnnotation.hasArgs();
        }

        throw new RuntimeException("No creator.");
    }

    private static CreatorData getCreatorData(Class clazz) {
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
            throw new RuntimeException("No Creator.");

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
        return new CreatorData(creatorAnnotation, optionsClass, creator, argTypes);
    }

    private static final class CreatorData {
        final Creator creatorAnnotation;
        final Class optionsClass;
        final Constructor<?> creator;
        final Class<?>[] argTypes;

        CreatorData(Creator creatorAnnotation, Class optionsClass, Constructor<?> creator, Class<?>[] argTypes) {
            this.creatorAnnotation = creatorAnnotation;
            this.optionsClass = optionsClass;
            this.creator = creator;
            this.argTypes = argTypes;
        }
    }
}
