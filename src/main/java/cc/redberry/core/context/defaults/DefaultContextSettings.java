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
package cc.redberry.core.context.defaults;

import cc.redberry.core.context.ContextSettings;
import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.indices.IndexType;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class DefaultContextSettings {

    public static ContextSettings create() {
        ContextSettings defaultSettings = new ContextSettings(ToStringMode.REDBERRY, "d");
        defaultSettings.setMetricName("g");

        defaultSettings.addMetricIndexType(IndexType.LatinLower);
        defaultSettings.addMetricIndexType(IndexType.GreekLower);
        defaultSettings.addMetricIndexType(IndexType.LatinUpper);
        //defaultSettings.addMetricIndexType(IndexType.GreekUpper);

        //Reading seed from property if exists
        if (System.getProperty("redberry.nmseed") != null)
            defaultSettings.setNameManagerSeed(Long.parseLong(System.getProperty("redberry.nmseed"), 10));

        return defaultSettings;
    }
}
