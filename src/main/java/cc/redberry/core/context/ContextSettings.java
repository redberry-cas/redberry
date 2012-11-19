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

import cc.redberry.core.indices.IndexType;
import cc.redberry.core.parser.Parser;

import java.util.EnumSet;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ContextSettings {
    //bindings

    private OutputFormat defaultOutputFormat;
    private String kronecker = "d";
    private String metricName = "g";
    //optional
    private EnumSet<IndexType> merticTypes = EnumSet.noneOf(IndexType.class);
    private Long nameManagerSeed;
    private IndexConverterManager converterManager = IndexConverterManager.DEFAULT;
    private Parser parser = Parser.DEFAULT;

    public ContextSettings(OutputFormat defaultOutputFormat, String kronecker) {
        this.defaultOutputFormat = defaultOutputFormat;
        this.kronecker = kronecker;
    }

    EnumSet<IndexType> getMetricTypes() {
        return merticTypes;
    }

    public void removeMetricIndexType(IndexType type) {
        merticTypes.remove(type);
    }

    public void addMetricIndexType(IndexType type) {
        merticTypes.add(type);
    }

    public OutputFormat getDefaultOutputFormat() {
        return defaultOutputFormat;
    }

    public void setDefaultOutputFormat(OutputFormat defaultOutputFormat) {
        if (defaultOutputFormat == null)
            throw new NullPointerException();
        this.defaultOutputFormat = defaultOutputFormat;
    }

    public String getKronecker() {
        return kronecker;
    }

    public void setKronecker(String kronecker) {
        if (kronecker == null)
            throw new NullPointerException();
        if (kronecker.isEmpty())
            throw new IllegalArgumentException();
        this.kronecker = kronecker;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        if (metricName == null)
            throw new NullPointerException();
        if (metricName.isEmpty())
            throw new IllegalArgumentException();
        this.metricName = metricName;
    }

    public Long getNameManagerSeed() {
        return nameManagerSeed;
    }

    public void setNameManagerSeed(Long nameManagerSeed) {
        this.nameManagerSeed = nameManagerSeed;
    }

    public IndexConverterManager getConverterManager() {
        return converterManager;
    }

    public void setConverterManager(IndexConverterManager converterManager) {
        this.converterManager = converterManager;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public Parser getParser() {
        return parser;
    }
}
