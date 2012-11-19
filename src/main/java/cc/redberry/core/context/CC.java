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
 * the Free Software Foundation, either version 2 of the License, or
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
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class CC {

    private CC() {
    }

    public static Context current() {
        return Context.get();
    }

    /**
     * Returns true if metric is defined for specified index type in current
     * context.
     *
     * @param type index type
     * @return true if metric is defined for specified index type in current
     *         context
     */
    public static boolean isMetric(byte type) {
        return current().isMetric(type);
    }

    public static NameDescriptor getNameDescriptor(int name) {
        return current().getNameDescriptor(name);
    }

    public static NameManager getNameManager() {
        return current().getNameManager();
    }

    public static IndexConverterManager getIndexConverterManager() {
        return current().getIndexConverterManager();
    }

    public static OutputFormat getDefaultOutputFormat() {
        return current().getDefaultOutputFormat();
    }

    public static void setDefaultOutputFormat(OutputFormat mode) {
        current().setDefaultOutputFormat(mode);
    }

    public static void resetTensorNames() {
        current().resetTensorNames();
    }

    public static void resetTensorNames(long seed) {
        current().resetTensorNames(seed);
    }
}
