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

import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.indices.*;
import cc.redberry.core.parser.Parser;
import cc.redberry.core.tensor.*;
import java.util.Arrays;
import java.util.EnumSet;

public final class Context {
    private final NameManager nameManager;
    private ToStringMode defaultPrintMode;
    private final String kroneckerName;
    private final String metricName;
    private final int[] metricNames, kroneckerNames;
    private final EnumSet<IndexType> metricTypes;

    public Context(ContextSettings contextSettings) {
        nameManager = new NameManager(contextSettings.getNameManagerSeed());

        defaultPrintMode = contextSettings.getDefaultToStringMode();
        kroneckerName = contextSettings.getKronecker();
        metricTypes = contextSettings.getMetricTypes();
        metricName = contextSettings.getMetricName();

        metricNames = new int[contextSettings.getMetricTypes().size()];
        kroneckerNames = new int[IndexType.values().length];
        setMetricsAndKroneckersNames();
    }

    private void setMetricsAndKroneckersNames() {
        int i = -1;
        for (IndexType indexType : metricTypes) {
            NameDescriptor nd = new NameDescriptor(metricName,
                    new IndicesTypeStructure(new byte[]{indexType.getType()}, new int[]{2}));
            metricNames[++i] = nameManager.mapNameDescriptor(nd);
            nd.getSymmetries().add(indexType, false, new int[]{1, 0});
        }

        for (i = 0; i < kroneckerNames.length; ++i) {
            NameDescriptor nd = new NameDescriptor(metricName,
                    new IndicesTypeStructure(new byte[]{IndexType.values()[i].getType()}, new int[]{2}));
            kroneckerNames[i] = nameManager.mapNameDescriptor(nd);
            nd.getSymmetries().add(IndexType.values()[i], false, new int[]{1, 0});
        }
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
        setMetricsAndKroneckersNames();
    }

    public synchronized void resetTensorNames(long seed) {
        nameManager.reset(seed);
        setMetricsAndKroneckersNames();
    }

    public void setDefaultPrintMode(ToStringMode defaultPrintMode) {
        this.defaultPrintMode = defaultPrintMode;
    }

    public ToStringMode getDefaultPrintMode() {
        return defaultPrintMode;
    }

    public NameManager getNameManager() {
        return nameManager;
    }

    public NameDescriptor getNameDescriptor(int nameId) {
        return nameManager.getNameDescriptor(nameId);
    }

    public String getKroneckerName() {
        return kroneckerName;
    }

    public String getMetricName() {
        return metricName;
    }

    public boolean withMetric() {
        return metricName != null;
    }

    public boolean isKronecker(SimpleTensor t) {
        if (kroneckerNames == null)
            return false;
        // kroneckerNames naturally sorted
        return Arrays.binarySearch(kroneckerNames, t.getName()) >= 0;
    }

    public boolean isMetric(SimpleTensor t) {
        if (metricNames == null)
            return false;
        // metricNames naturally sorted
        synchronized (this) {
            return Arrays.binarySearch(metricNames, t.getName()) >= 0;
        }
    }

    public SimpleTensor createKronecker(int index1, int index2) {
        if (IndicesUtils.getType(index1) != IndicesUtils.getType(index2) || IndicesUtils.getRawStateInt(index1) == IndicesUtils.getRawStateInt(index2))
            throw new IllegalArgumentException("This is not kronecker indices!");
        SimpleIndices indices = IndicesFactory.createSimple(index1, index2);
        NameDescriptor nd = new NameDescriptor(kroneckerName, new IndicesTypeStructure(indices));
        int name = nameManager.mapNameDescriptor(nd);
        return new SimpleTensor(name, indices);
    }

    public SimpleTensor createMetric(int index1, int index2) {
        byte type;
        if ((type = IndicesUtils.getType(index1)) != IndicesUtils.getType(index2)
                || IndicesUtils.getRawStateInt(index1) != IndicesUtils.getRawStateInt(index2)
                || Arrays.binarySearch(metricTypes, type) < 0)
            throw new IllegalArgumentException("This is not metric indices!");
        SimpleIndices indices = IndicesFactory.createSimple(index1, index2);
        NameDescriptor nd = new NameDescriptor(metricName, new IndicesTypeStructure(indices));
        int name = nameManager.mapNameDescriptor(nd);
        return new SimpleTensor(name, indices);
    }

    public SimpleTensor createMetricOrKronecker(int index1, int index2) {
        if (IndicesUtils.getRawStateInt(index1) == IndicesUtils.getRawStateInt(index2))
            return createMetric(index1, index2);
        return createKronecker(index1, index2);
    }

    public Tensor parse(String expression) {
        return parser.parse(expression);
    }

    public SimpleTensor parseSimple(String expression) {
        Tensor t = parser.parse(expression);
        if (t instanceof SimpleTensor)
            return (SimpleTensor) t;
        throw new RuntimeException("Not simple tensor");
    }

    public SimpleTensor createSimpleTensor(String name, SimpleIndices indices) {
        NameDescriptor descriptor = new NameDescriptor(name, new IndicesTypeStructure(indices));
        int tensorName = nameManager.mapNameDescriptor(descriptor);
        //dumping symmetries
        SimpleIndices nIndices = IndicesFactory.createOfTensor(indices);
        //creating simple and binding nIndices and descriptor symmetries
        SimpleTensor t = new SimpleTensor(tensorName, nIndices);
        //adding additional symmetries
        nIndices.getSymmetries().addAllUnsafe(indices.getSymmetries());
        return t;
    }
}
