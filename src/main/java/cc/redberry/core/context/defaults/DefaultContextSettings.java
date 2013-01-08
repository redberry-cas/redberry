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
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.IndexType;

/**
 * The default Redberry context settings. These default are: metric tensor name - "g",
 * Kronecker tensor name - "d", metric types - {@link IndexType#LatinLower}, {@link IndexType#LatinUpper},
 * {@link IndexType#GreekLower}, {@link IndexType#GreekUpper}. Random seed of {@link cc.redberry.core.context.NameManager}
 * is taken from the command line if {@code redberry.nmseed specified}, or generated randomly.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class DefaultContextSettings {
    private DefaultContextSettings() {
    }

    public static ContextSettings create() {
        ContextSettings defaultSettings = new ContextSettings(OutputFormat.Redberry, "d");
        defaultSettings.setMetricName("g");

        defaultSettings.addMetricIndexType(IndexType.LatinLower);
        defaultSettings.addMetricIndexType(IndexType.GreekLower);
        defaultSettings.addMetricIndexType(IndexType.LatinUpper);
        defaultSettings.addMetricIndexType(IndexType.GreekUpper);

        //Reading seed from property if exists
        if (System.getProperty("redberry.nmseed") != null)
            defaultSettings.setNameManagerSeed(Long.parseLong(System.getProperty("redberry.nmseed"), 10));

        return defaultSettings;
    }
}
