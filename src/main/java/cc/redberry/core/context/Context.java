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

import cc.redberry.core.indices.*;
import cc.redberry.core.parser.ParseManager;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.LongBackedBitArray;

public final class Context {

    private final NameManager nameManager;
    private ToStringMode defaultPrintMode;
    private final IndexConverterManager converterManager;
    private final ParseManager parseManager;
    private final LongBackedBitArray metricTypes = new LongBackedBitArray(128);

    public Context(ContextSettings contextSettings) {
        this.parseManager = new ParseManager(contextSettings.getParser());
        this.converterManager = contextSettings.getConverterManager();
        nameManager = new NameManager(contextSettings.getNameManagerSeed(), contextSettings.getKronecker(), contextSettings.getMetricName());

        defaultPrintMode = contextSettings.getDefaultToStringMode();

        for (IndexType type : contextSettings.getMetricTypes())
            metricTypes.set(type.getType());
    }

    /**
     * This method resets all tensor names.
     * <p/>
     * <br/><b>Any tensor created before this method call becomes invalid, and
     * must not be used!</b> <br/><br/>Mainly this method used in unit tests, so
     * avoid using this method in your code.
     */
    public synchronized void resetTensorNames() {
        nameManager.reset();
    }

    public synchronized void resetTensorNames(long seed) {
        nameManager.reset(seed);
    }

    public void setDefaultToStringFormat(ToStringMode defaultPrintMode) {
        this.defaultPrintMode = defaultPrintMode;
    }

    public ToStringMode getDefaultToStringFormat() {
        return defaultPrintMode;
    }

    public IndexConverterManager getIndexConverterManager() {
        return converterManager;
    }

    public NameManager getNameManager() {
        return nameManager;
    }

    public NameDescriptor getNameDescriptor(int nameId) {
        return nameManager.getNameDescriptor(nameId);
    }

    public String getKroneckerName() {
        return nameManager.getKroneckerName();
    }

    public String getMetricName() {
        return nameManager.getMetricName();
    }

    public void setMetricName(String name) {
        nameManager.setMetricName(name);
    }

    public void setKroneckerName(String name) {
        nameManager.setKroneckerName(name);
    }

    public boolean isKronecker(SimpleTensor t) {
        return nameManager.isKroneckerOrMetric(t.getName())
                && !IndicesUtils.haveEqualStates(t.getIndices().get(0), t.getIndices().get(1));
    }

    public boolean isMetric(SimpleTensor t) {
        return nameManager.isKroneckerOrMetric(t.getName())
                && IndicesUtils.haveEqualStates(t.getIndices().get(0), t.getIndices().get(1));
    }

    public boolean isKroneckerOrMetric(SimpleTensor s) {
        return nameManager.isKroneckerOrMetric(s.getName());
    }

    public ParseManager getParseManager() {
        return parseManager;
    }

    /**
     * Returns true if metric is defined for specified index type.
     *
     * @param type index type
     *
     * @return true if metric is defined for specified index type
     */
    public boolean isMetric(byte type) {
        return metricTypes.get(type);
    }

    public SimpleTensor createKronecker(int index1, int index2) {
        if (IndicesUtils.getType(index1) != IndicesUtils.getType(index2) || IndicesUtils.getRawStateInt(index1) == IndicesUtils.getRawStateInt(index2))
            throw new IllegalArgumentException("This is not kronecker indices!");
        SimpleIndices indices = IndicesFactory.createSimple(null, index1, index2);
        NameDescriptor nd = nameManager.mapNameDescriptor(nameManager.getKroneckerName(), new IndicesTypeStructure(indices));
        int name = nd.getId();
        return Tensors.simpleTensor(name, indices);
    }

    public SimpleTensor createMetric(int index1, int index2) {
        byte type;
        if ((type = IndicesUtils.getType(index1)) != IndicesUtils.getType(index2)
                || !IndicesUtils.haveEqualStates(index1, index2)
                || !metricTypes.get(type))
            throw new IllegalArgumentException("Not metric indices.");
        SimpleIndices indices = IndicesFactory.createSimple(null, index1, index2);
        NameDescriptor nd = nameManager.mapNameDescriptor(nameManager.getMetricName(), new IndicesTypeStructure(indices));
        int name = nd.getId();
        return Tensors.simpleTensor(name, indices);
    }

    public SimpleTensor createMetricOrKronecker(int index1, int index2) {
        if (IndicesUtils.getRawStateInt(index1) == IndicesUtils.getRawStateInt(index2))
            return createMetric(index1, index2);
        return createKronecker(index1, index2);
    }

    public static Context get() {
        return ContextManager.getCurrentContext();
    }
}
