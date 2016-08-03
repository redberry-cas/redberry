/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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
package cc.redberry.core.context2;

import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;

import java.util.List;

/**
 * @author Stanislav Poslavsky
 */
public interface NameFormatter {
    String getVarName(String baseName, List<String> aliases, SimpleIndices indices, OutputFormat format);

    NameFormatter DefaultName = new NameFormatter() {
        @Override
        public String getVarName(String baseName, List<String> aliases, SimpleIndices indices, OutputFormat format) {
            return baseName;
        }
    };

    class MetricOrKronecker implements NameFormatter {
        final String metric, kronecker;

        public MetricOrKronecker(String metric, String kronecker) {
            this.metric = metric;
            this.kronecker = kronecker;
        }

        @Override
        public String getVarName(String baseName, List<String> aliases, SimpleIndices indices, OutputFormat format) {
            boolean isMetric = IndicesUtils.haveEqualStates(indices.get(0), indices.get(1));
            return isMetric ? metric : kronecker;
        }
    }
}
