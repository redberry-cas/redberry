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
package cc.redberry.core.context;

import cc.redberry.core.indices.*;
import cc.redberry.core.parser.ParseManager;
import cc.redberry.core.parser.Parser;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensors;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Stanislav Poslavsky
 */
public final class Context {
    /**
     * Configuration
     */
    private final ContextConfiguration contextConfiguration;
    /**
     * Name manager instance
     */
    private final NameManager nameManager;
    /**
     * Parser manager
     */
    private final ParseManager parseManager;
    /**
     * Context listeners
     */
    private final Set<ContextListener> listeners = Collections.newSetFromMap(new ConcurrentHashMap<ContextListener, Boolean>());

    /**
     * Random generator instance
     */
    private final RandomGenerator randomGenerator;
    /**
     * Random seed
     */
    private long randomSeed;

    /**
     * VarDescriptors for metric tensors
     */
    private final EnumMap<IndexType, VarDescriptor> metricDescriptors;
    /**
     * Ids (names) of different metric tensors (for fast access)
     */
    private final int[] metricIds;

    Context(ContextConfiguration cc) {
        this.contextConfiguration = cc.clone();
        this.parseManager = new ParseManager(Parser.DEFAULT);
        this.nameManager = new NameManager(
                contextConfiguration.idProvider == null
                        ? contextConfiguration.idAlgorithm.create(contextConfiguration)
                        : contextConfiguration.idProvider);
        this.randomGenerator = contextConfiguration.randomGenerator;
        this.randomGenerator.setSeed(this.randomSeed = randomGenerator.nextLong());
        this.metricDescriptors = new EnumMap<>(IndexType.class);
        this.metricIds = new int[contextConfiguration.metricTypes.size()];
        updateHashes();
    }

    private void updateHashes() {
        int i = 0;
        for (IndexType mt : contextConfiguration.metricTypes) {
            final StructureOfIndices st = StructureOfIndices.create(mt, 2);
            final VarDescriptor nd = nameManager.resolve(
                    contextConfiguration.metricName, st);
            nd.setNameFormatter(new NameFormatter.MetricOrKronecker(
                    contextConfiguration.metricName,
                    contextConfiguration.kroneckerName
            ));
            nd.getSymmetries().setSymmetric();
            nameManager.addNameAlias(nd, contextConfiguration.kroneckerName);
            metricDescriptors.put(mt, nd);
            metricIds[i++] = nd.id;
        }
        Arrays.sort(this.metricIds);

        //default for derivative
        nameManager.resolve("DArg", StructureOfIndices.getEmpty(), VarIndicesProvider.DerivativeArg);
        nameManager.resolve("D", StructureOfIndices.getEmpty(), VarIndicesProvider.JoinAll);
    }

    public RandomGenerator randomGenerator() {
        return randomGenerator;
    }

    /**
     * This method resets all tensor names in the namespace.
     * <p/>
     * <p>Any tensor created before this method call becomes invalid, and
     * must not be used! This method is mainly used in unit tests, so
     * avoid invocations of this method in general computations.</p>
     */
    @Deprecated
    public synchronized void resetTensorNames() {
        resetTensorNames(randomGenerator.nextLong());
    }

    /**
     * Resets all definitions.
     */
    public synchronized void reset() {
        parseManager.reset();
        resetTensorNames();
    }

    /**
     * This method resets all tensor names in the namespace and sets a
     * specified seed to the {@link cc.redberry.core.context.NameManager}. If this method is invoked
     * with constant seed before any interactions with Redberry, further
     * behaviour of Redberry will be fully deterministic from run to run
     * (order of summands and multipliers will be fixed, computation time
     * will be pretty constant, hash codes will be the same).
     * <p/>
     * <p>Any tensor created before this method call becomes invalid, and
     * must not be used! This method is mainly used in unit tests, so
     * avoid invocations of this method in general computations.</p>
     */
    @Deprecated
    public synchronized void resetTensorNames(long seed) {
        nameManager.reset();
        resetRandom(seed);
        updateHashes();
        resetEvent();
    }

    public synchronized void resetRandom(long seed) {
        randomGenerator.setSeed(randomSeed = seed);
    }

    /**
     * Returns random generator used by Redberry in current session.
     *
     * @return random generator used by Redberry in current session
     */
    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    public long getSeed() {
        return randomSeed;
    }

    /**
     * Sets the default output format. After this step, all expressions
     * will be printed according to the specified output format.
     *
     * @param defaultOutputFormat output format
     */
    public void setDefaultOutputFormat(OutputFormat defaultOutputFormat) {
        contextConfiguration.defaultOutputFormat = defaultOutputFormat;
    }

    /**
     * Returns current default output format.
     *
     * @return current default output format
     */
    public OutputFormat getDefaultOutputFormat() {
        return contextConfiguration.defaultOutputFormat;
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
    public VarDescriptor getVarDescriptor(int nameId) {
        return nameManager.getVarDescriptor(nameId);
    }

    /**
     * Returns string representation of Kronecker delta name
     *
     * @return string representation of Kronecker delta name
     */
    public String getKroneckerName() {
        return contextConfiguration.kroneckerName;
    }

    /**
     * Returns string representation of metric tensor name
     *
     * @return string representation of metric tensor name
     */
    public String getMetricName() {
        return contextConfiguration.metricName;
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
     * Allows to parse expressions with repeated indices of the same variance (like T_aa or T_a*T^a which results in T^a_a
     * and T^a*T_a respactively)
     *
     * @param b allow or not to parse repeated indices with same variance
     */
    public void setParserAllowsSameVariance(boolean b) {
        parseManager.getParser().setAllowSameVariance(b);
    }

    /**
     * Returns whether repeated indices of the same variance are allowed to be parsed
     *
     * @return whether repeated indices of the same variance are allowed to be parsed
     */
    public boolean getParserAllowsSameVariance() {
        return parseManager.getParser().isAllowSameVariance();
    }

    /**
     * Register new context listener
     *
     * @param listener listener
     */
    public void registerListener(ContextListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregister listener
     *
     * @param listener listener
     */
    public void removeListener(ContextListener listener) {
        listeners.remove(listener);
    }

    /**
     * New context event
     *
     * @param event event
     */
    public void newEvent(ContextEvent event) {
        synchronized (this) {
            for (ContextListener listener : listeners)
                listener.onEvent(event);
        }
    }

    private void resetEvent() {
        newEvent(ContextEvent.RESET);
    }

    /**
     * Returns the current context of Redberry session.
     *
     * @return the current context of Redberry session.
     */
    public static Context get() {
        return ContextManager.getCurrentContext();
    }

    /**
     * Returns {@code true} if specified tensor is a Kronecker tensor
     *
     * @param t tensor
     * @return {@code true} if specified tensor is a Kronecker tensor
     */
    public boolean isKronecker(SimpleTensor t) {
        return isKroneckerOrMetric(t.getName())
                && !IndicesUtils.haveEqualStates(t.getIndices().get(0), t.getIndices().get(1));
    }

    /**
     * Returns {@code true} if specified tensor is a metric tensor
     *
     * @param t tensor
     * @return {@code true} if specified tensor is a metric tensor
     */
    public boolean isMetric(SimpleTensor t) {
        return isKroneckerOrMetric(t.getName())
                && IndicesUtils.haveEqualStates(t.getIndices().get(0), t.getIndices().get(1));
    }

    /**
     * Returns {@code true} if specified {@code id} corresponds to metric or Kronecker tensor
     *
     * @param t id
     * @return {@code true} if specified {@code id} corresponds to metric or Kronecker tensor
     */
    public boolean isKroneckerOrMetric(int t) {
        return Arrays.binarySearch(metricIds, t) >= 0;
    }


    /**
     * Returns {@code true} if specified tensor is a metric or a Kronecker tensor
     *
     * @param t tensor
     * @return {@code true} if specified tensor is a metric or a Kronecker tensor
     */
    public boolean isKroneckerOrMetric(SimpleTensor t) {
        return isKroneckerOrMetric(t.getName());
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
        if (!contextConfiguration.isMetric(type) && IndicesUtils.getState(index2)) {
            int t = index1;
            index1 = index2;
            index2 = t;
        }
        SimpleIndices indices = IndicesFactory.createSimple(null, index1, index2);
        return Tensors.simpleTensor(metricDescriptors.get(IndexType.getType(type)).id, indices);
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
                || !contextConfiguration.isMetric(type))
            throw new IllegalArgumentException("Not metric indices.");
        SimpleIndices indices = IndicesFactory.createSimple(null, index1, index2);
        return Tensors.simpleTensor(metricDescriptors.get(IndexType.getType(type)).id, indices);
    }

    /**
     * Returns metric tensor if specified indices have same states and
     * Kronecker tensor if specified indices have different states.
     *
     * @param index1 first index
     * @param index2 second index
     * @return metric tensor if specified indices have same states and
     * Kronecker tensor if specified indices have different states
     * @throws IllegalArgumentException if indices have different types
     * @throws IllegalArgumentException if indices have same states and non metric types
     */
    public SimpleTensor createMetricOrKronecker(int index1, int index2) {
        if (IndicesUtils.getRawStateInt(index1) == IndicesUtils.getRawStateInt(index2))
            return createMetric(index1, index2);
        return createKronecker(index1, index2);
    }
}
