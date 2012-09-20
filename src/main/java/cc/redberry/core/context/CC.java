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
     *
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

    public static ToStringMode getDefaultPrintMode() {
        return current().getDefaultPrintMode();
    }

    public static void setDefaultToStringFormat(ToStringMode mode) {
        current().setDefaultToStringFormat(mode);
    }

    public static void resetTensorNames() {
        current().resetTensorNames();
    }

    public static void resetTensorNames(long seed) {
        current().resetTensorNames(seed);
    }
}
