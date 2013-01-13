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

import cc.redberry.core.indices.*;
import cc.redberry.core.parser.ParseManager;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.LongBackedBitArray;

/**
 * This class represents Redberry context. It stores all Redberry session data (in some sense it stores static data).
 *
 * <p>Management of current Redberry context is made through {@link ContextManager} class.
 * Context of Redberry is attached to the current thread, so that any thread created from the outside of Redebrry
 * will hold a unique instance of {@link Context} object. In such a way tensors created in one thread can not
 * be used in the other thread because they are in some sense "attached" to the initial thread. However, if thread is
 * created through the executor service which is obtained from {@link ContextManager#getExecutorService()}, it will
 * share the same context as initial thread (such threads could hold concurrent computations regarding single context,
 * the appropriate synchronization is assumed). In order to create a new session of Redberry with a particular context,
 * an instance of this class should be set as a current context via {@link ContextManager#setCurrentContext(Context)}.</p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see NameManager
 * @see IndexConverterManager
 * @see ParseManager
 * @see OutputFormat
 * @see ContextManager
 * @see ContextSettings
 * @since 1.0
 */
public final class Context {
    /**
     * NameManager has a sense of namespace
     */
    private final NameManager nameManager;
    /**
     * Defaults output format can be changed during the session
     */
    private OutputFormat defaultOutputFormat;

    private final IndexConverterManager converterManager;
    private final ParseManager parseManager;
    /**
     * Holds information about metric types.
     * This is a "map" from (byte) type to (bit) isMetric
     */
    private final LongBackedBitArray metricTypes = new LongBackedBitArray(128);

    /**
     * Creates context from the settings
     *
     * @param contextSettings settings
     * @see ContextSettings
     */
    public Context(ContextSettings contextSettings) {
        this.parseManager = new ParseManager(contextSettings.getParser());
        this.converterManager = contextSettings.getConverterManager();
        nameManager = new NameManager(contextSettings.getNameManagerSeed(), contextSettings.getKronecker(), contextSettings.getMetricName());

        defaultOutputFormat = contextSettings.getDefaultOutputFormat();

        for (IndexType type : contextSettings.getMetricTypes())
            metricTypes.set(type.getType());
    }

    /**
     * This method resets all tensor names in the namespace.
     *
     * <p>Any tensor created before this method call becomes invalid, and
     * must not be used! This method is mainly used in unit tests, so
     * avoid invocations of this method in general computations.</p>
     */
    public synchronized void resetTensorNames() {
        nameManager.reset();
    }

    /**
     * This method resets all tensor names in the namespace and sets a
     * specified seed to the {@link NameManager}. If this method is invoked
     * with constant seed before any interactions with Redberry, further
     * behaviour of Redberry will be fully deterministic from run to run
     * (order of summands and multipliers will be fixed, computation time
     * will be pretty constant, hash codes will be the same).
     *
     * <p>Any tensor created before this method call becomes invalid, and
     * must not be used! This method is mainly used in unit tests, so
     * avoid invocations of this method in general computations.</p>
     */
    public synchronized void resetTensorNames(long seed) {
        nameManager.reset(seed);
    }

    /**
     * Sets the default output format. After this step, all expressions
     * will be printed according to the specified output format.
     *
     * @param defaultOutputFormat output format
     */
    public void setDefaultOutputFormat(OutputFormat defaultOutputFormat) {
        this.defaultOutputFormat = defaultOutputFormat;
    }

    /**
     * Returns current default output format.
     *
     * @return current default output format
     */
    public OutputFormat getDefaultOutputFormat() {
        return defaultOutputFormat;
    }

    /**
     * Returns index converter manager of current session.
     *
     * @return index converter manager of current session
     */
    public IndexConverterManager getIndexConverterManager() {
        return converterManager;
    }

    /**
     * Returns the name manager (namespace) of current session.
     *
     * @return the name manager (namespace) of current session.
     */
    public NameManager getNameManager() {
        return nameManager;
    }

    /**
     * Returns {@code NameDescriptor} corresponding to the specified {@code int} nameId.
     *
     * @param nameId integer name of tensor
     * @return corresponding  {@code NameDescriptor}
     */
    public NameDescriptor getNameDescriptor(int nameId) {
        return nameManager.getNameDescriptor(nameId);
    }

    /**
     * Returns string representation of Kronecker delta name
     *
     * @return string representation of Kronecker delta name
     */
    public String getKroneckerName() {
        return nameManager.getKroneckerName();
    }

    /**
     * Returns string representation of metric tensor name
     *
     * @return string representation of metric tensor name
     */
    public String getMetricName() {
        return nameManager.getMetricName();
    }

    /**
     * Sets the default metric tensor name. After this step, metric tensor
     * will be printed with the specified string name.
     *
     * @param name string representation of metric tensor name
     */
    public void setMetricName(String name) {
        nameManager.setMetricName(name);
    }

    /**
     * Sets the default Kronecker tensor name. After this step, Kronecker tensor
     * will be printed with the specified string name.
     *
     * @param name string representation of Kronecker tensor name
     */
    public void setKroneckerName(String name) {
        nameManager.setKroneckerName(name);
    }

    /**
     * Returns {@code true} if specified tensor is a Kronecker tensor
     *
     * @param t tensor
     * @return {@code true} if specified tensor is a Kronecker tensor
     */
    public boolean isKronecker(SimpleTensor t) {
        return nameManager.isKroneckerOrMetric(t.getName())
                && !IndicesUtils.haveEqualStates(t.getIndices().get(0), t.getIndices().get(1));
    }

    /**
     * Returns {@code true} if specified tensor is a metric tensor
     *
     * @param t tensor
     * @return {@code true} if specified tensor is a metric tensor
     */
    public boolean isMetric(SimpleTensor t) {
        return nameManager.isKroneckerOrMetric(t.getName())
                && IndicesUtils.haveEqualStates(t.getIndices().get(0), t.getIndices().get(1));
    }

    /**
     * Returns {@code true} if specified tensor is a metric or a Kronecker tensor
     *
     * @param t tensor
     * @return {@code true} if specified tensor is a metric or a Kronecker tensor
     */
    public boolean isKroneckerOrMetric(SimpleTensor t) {
        return nameManager.isKroneckerOrMetric(t.getName());
    }

    /**
     * Returns parse manager of current session
     *
     * @return parse manager of current session
     */
    public ParseManager getParseManager() {
        return parseManager;
    }

    /**
     * Returns true if metric is defined for the specified index type.
     *
     * @param type index type
     * @return true if metric is defined for the specified index type
     */
    public boolean isMetric(byte type) {
        return metricTypes.get(type);
    }

    /**
     * Returns Kronecker tensor with specified upper and lower indices.
     *
     * @param index1 first index
     * @param index2 second index
     * @return Kronecker tensor with specified upper and lower indices
     * @throws IllegalArgumentException if indices have same states
     * @throws IllegalArgumentException if indices have different types
     */
    public SimpleTensor createKronecker(int index1, int index2) {
        byte type;
        if ((type = IndicesUtils.getType(index1)) != IndicesUtils.getType(index2) || IndicesUtils.getRawStateInt(index1) == IndicesUtils.getRawStateInt(index2))
            throw new IllegalArgumentException("This is not kronecker indices!");
        if (!isMetric(type) && IndicesUtils.getState(index2)) {
            int t = index1;
            index1 = index2;
            index2 = t;
        }
        SimpleIndices indices = IndicesFactory.createSimple(null, index1, index2);
        NameDescriptor nd = nameManager.mapNameDescriptor(nameManager.getKroneckerName(), new StructureOfIndices(indices));
        int name = nd.getId();
        return Tensors.simpleTensor(name, indices);
    }

    /**
     * Returns metric tensor with specified indices.
     *
     * @param index1 first index
     * @param index2 second index
     * @return metric tensor with specified indices
     * @throws IllegalArgumentException if indices have different states
     * @throws IllegalArgumentException if indices have different types
     * @throws IllegalArgumentException if indices have non metric types
     */
    public SimpleTensor createMetric(int index1, int index2) {
        byte type;
        if ((type = IndicesUtils.getType(index1)) != IndicesUtils.getType(index2)
                || !IndicesUtils.haveEqualStates(index1, index2)
                || !metricTypes.get(type))
            throw new IllegalArgumentException("Not metric indices.");
        SimpleIndices indices = IndicesFactory.createSimple(null, index1, index2);
        NameDescriptor nd = nameManager.mapNameDescriptor(nameManager.getMetricName(), new StructureOfIndices(indices));
        int name = nd.getId();
        return Tensors.simpleTensor(name, indices);
    }

    /**
     * Returns metric tensor if specified indices have same states and
     * Kronecker tensor if specified indices have different states.
     *
     * @param index1 first index
     * @param index2 second index
     * @return metric tensor if specified indices have same states and
     *         Kronecker tensor if specified indices have different states
     * @throws IllegalArgumentException if indices have different types
     * @throws IllegalArgumentException if indices have same states and non metric types
     */
    public SimpleTensor createMetricOrKronecker(int index1, int index2) {
        if (IndicesUtils.getRawStateInt(index1) == IndicesUtils.getRawStateInt(index2))
            return createMetric(index1, index2);
        return createKronecker(index1, index2);
    }

    /**
     * Returns the current context of Redberry session.
     *
     * @return the current context of Redberry session.
     */
    public static Context get() {
        return ContextManager.getCurrentContext();
    }
}
