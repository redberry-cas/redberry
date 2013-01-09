/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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

import cc.redberry.core.indices.IndexType;
import cc.redberry.core.parser.Parser;

import java.util.EnumSet;

/**
 * A simple container of context-sensitive raw Redberry information (like string name of metrics etc.),
 * which then used in the constructor of {@link Context} class.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Context
 * @since 1.0
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

    /**
     * Creates context settings with specified default output format and Kronecker delta name.
     *
     * @param defaultOutputFormat output format
     * @param kronecker           string name of Kronecker tensor
     */
    public ContextSettings(OutputFormat defaultOutputFormat, String kronecker) {
        this.defaultOutputFormat = defaultOutputFormat;
        this.kronecker = kronecker;
    }

    EnumSet<IndexType> getMetricTypes() {
        return merticTypes;
    }

    /**
     * Set specified index type to be non metric.
     *
     * @param type index type
     */
    public void removeMetricIndexType(IndexType type) {
        merticTypes.remove(type);
    }

    /**
     * Set specified index type to be a metric type.
     *
     * @param type index type
     */
    public void addMetricIndexType(IndexType type) {
        merticTypes.add(type);
    }

    /**
     * Returns the default output format
     *
     * @return the default output format
     */
    public OutputFormat getDefaultOutputFormat() {
        return defaultOutputFormat;
    }

    /**
     * Sets the default output format.
     *
     * @param defaultOutputFormat output format
     */
    public void setDefaultOutputFormat(OutputFormat defaultOutputFormat) {
        if (defaultOutputFormat == null)
            throw new NullPointerException();
        this.defaultOutputFormat = defaultOutputFormat;
    }

    /**
     * Returns string representation of Kronecker tensor name.
     *
     * @return string representation of Kronecker tensor name
     */
    public String getKronecker() {
        return kronecker;
    }

    /**
     * Sets string representation of Kronecker tensor name.
     *
     * @param kronecker string name of Kronecker tensor
     * @return string representation of Kronecker tensor name
     */
    public void setKronecker(String kronecker) {
        if (kronecker == null)
            throw new NullPointerException();
        if (kronecker.isEmpty())
            throw new IllegalArgumentException();
        this.kronecker = kronecker;
    }

    /**
     * Returns string representation of metric tensor name.
     *
     * @return string representation of metric tensor name
     */
    public String getMetricName() {
        return metricName;
    }

    /**
     * Sets string representation of metric tensor name.
     */
    public void setMetricName(String metricName) {
        if (metricName == null)
            throw new NullPointerException();
        if (metricName.isEmpty())
            throw new IllegalArgumentException();
        this.metricName = metricName;
    }

    /**
     * Returns seed of name manager
     *
     * @return seed of name manager
     */
    public Long getNameManagerSeed() {
        return nameManagerSeed;
    }

    /**
     * Sets seed of name manager
     */
    public void setNameManagerSeed(Long nameManagerSeed) {
        this.nameManagerSeed = nameManagerSeed;
    }

    /**
     * Returns index converter manager
     *
     * @return index converter manager
     */
    public IndexConverterManager getConverterManager() {
        return converterManager;
    }

    /**
     * Sets index converter manager
     */
    public void setConverterManager(IndexConverterManager converterManager) {
        this.converterManager = converterManager;
    }

    /**
     * Sets parser
     */
    public void setParser(Parser parser) {
        this.parser = parser;
    }

    /**
     * Returns parser
     *
     * @return parser
     */
    public Parser getParser() {
        return parser;
    }
}
