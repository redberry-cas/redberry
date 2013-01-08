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
package cc.redberry.core.context;

/**
 * Redberry current context. This class statically delegates common useful methods from
 * Redberry context of current session.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Context
 * @since 1.0
 */
public final class CC {

    private CC() {
    }

    /**
     * Returns the current context of Redberry session.
     *
     * @return the current context of Redberry session.
     */
    public static Context current() {
        return Context.get();
    }

    /**
     * Returns true if metric is defined for specified index type.
     *
     * @param type index type
     * @return true if metric is defined for specified index type
     */
    public static boolean isMetric(byte type) {
        return current().isMetric(type);
    }

    /**
     * Returns {@code NameDescriptor} corresponding to the specified {@code int} nameId.
     *
     * @param name integer name of tensor
     * @return corresponding  {@code NameDescriptor}
     */
    public static NameDescriptor getNameDescriptor(int name) {
        return current().getNameDescriptor(name);
    }

    /**
     * Returns the name manager manager (namespace) of current session.
     *
     * @return the name manager manager (namespace) of current session.
     */
    public static NameManager getNameManager() {
        return current().getNameManager();
    }

    /**
     * Returns index converter manager of current session.
     *
     * @return index converter manager of current session
     */
    public static IndexConverterManager getIndexConverterManager() {
        return current().getIndexConverterManager();
    }

    /**
     * Returns current default output format.
     *
     * @return current default output format
     */
    public static OutputFormat getDefaultOutputFormat() {
        return current().getDefaultOutputFormat();
    }

    /**
     * Sets the default output format. After this step, all expressions
     * will be printed according to the specified output format.
     *
     * @param defaultOutputFormat output format
     */
    public static void setDefaultOutputFormat(OutputFormat defaultOutputFormat) {
        current().setDefaultOutputFormat(defaultOutputFormat);
    }

    /**
     * This method resets all tensor names in the namespace.
     * Any tensor created before this method call becomes invalid, and
     * must not be used! This method is using mainly in unit tests, so
     * avoid using this method in your code.
     */
    public static void resetTensorNames() {
        current().resetTensorNames();
    }

    /**
     * This method resets all tensor names in the namespace and sets a
     * specified seed to the {@link NameManager}.
     * Any tensor created before this method call becomes invalid, and
     * must not be used! This method is using mainly in unit tests, so
     * avoid using this method in your code.
     */
    public static void resetTensorNames(long seed) {
        current().resetTensorNames(seed);
    }
}
