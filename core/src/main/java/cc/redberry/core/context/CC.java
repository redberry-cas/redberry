/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
import cc.redberry.core.tensor.SimpleTensor;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redberry current context. This class statically delegates common useful methods from
 * Redberry context of current session.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Context
 * @since 1.0
 */
public final class CC {

    private CC() {
    }

    private static final AtomicLong counter = new AtomicLong(0);

    /**
     * DEPRECATED legacy code
     */
    @Deprecated
    public static SimpleTensor generateNewSymbol() {
        //TODO DELETE
        return (SimpleTensor) current().getParseManager().parse("var" + counter.getAndIncrement());
    }

    /**
     * Returns the current context of Redberry session.
     *
     * @return the current context of Redberry session.
     */
    public static Context current() {
        return ContextManager.getCurrentContext();
    }

    /**
     * Returns the current context configuration
     *
     * @return the current context configuration
     */
    public static ContextConfiguration currentConfig() {
        return ContextManager.getCurrentContextConfiguration();
    }

    /**
     * Returns true if metric is defined for specified index type.
     *
     * @param type index type
     * @return true if metric is defined for specified index type
     */
    public static boolean isMetric(byte type) {
        return currentConfig().isMetric(type);
    }

    /**
     * Returns true if metric is defined for specified index type.
     *
     * @param type index type
     * @return true if metric is defined for specified index type
     */
    public static boolean isMetric(IndexType type) {
        return currentConfig().isMetric(type.getType());
    }

    /**
     * Returns {@code NameDescriptor} corresponding to the specified {@code int} nameId.
     *
     * @param name integer name of tensor
     * @return corresponding  {@code NameDescriptor}
     */
    public static VarDescriptor getVarDescriptor(int name) {
        return current().getVarDescriptor(name);
    }

    /**
     * Returns the name manager (namespace) of current session.
     *
     * @return the name manager (namespace) of current session.
     */
    public static NameManager getNameManager() {
        return current().getNameManager();
    }

    /**
     * Returns current default output format.
     *
     * @return current default output format
     */
    public static OutputFormat getDefaultOutputFormat() {
        return current().getDefaultOutputFormat();
    }

    /**
     * Returns all metric types.
     *
     * @return all metric types
     */
    public static Set<IndexType> getMetricTypes() {
        return currentConfig().metricTypes;
    }

    /**
     * Returns all matrix types.
     *
     * @return all matrix types
     */
    public static Set<IndexType> getMatrixTypes() {
        return currentConfig().getMatrixTypes();
    }

    /**
     * Sets the default output format.
     * <p/>
     * <p>After this step, all expressions will be printed according to the specified output format.</p>
     *
     * @param defaultOutputFormat output format
     */
    public static void setDefaultOutputFormat(OutputFormat defaultOutputFormat) {
        current().setDefaultOutputFormat(defaultOutputFormat);
    }

    /**
     * This method resets all tensor names in the namespace.
     * <p/>
     * <p>Any tensor created before this method call becomes invalid, and
     * must not be used! This method is mainly used in unit tests, so
     * avoid invocations of this method in general computations.</p>
     */
    public static void resetTensorNames() {
        current().resetTensorNames();
    }

    /**
     * Resets all definitions.
     */
    public static void reset() {
        current().reset();
    }

    /**
     * This method resets all tensor names in the namespace and sets a
     * specified seed to the {@link NameManager}. If this method is invoked
     * with constant seed before any interactions with Redberry, further
     * behaviour of Redberry will be fully deterministic from run to run
     * (order of summands and multipliers will be fixed, computation time
     * will be pretty constant, hash codes will be the same).
     * <p/>
     * <p>Any tensor created before this method call becomes invalid, and
     * must not be used! This method is mainly used in unit tests, so
     * avoid invocations of this method in general computations.</p>
     */
    public static void resetTensorNames(long seed) {
        current().resetTensorNames(seed);
    }


//    /**
//     * Generates a new symbol which never used before during current session.
//     *
//     * @return new symbol which never used before during current session
//     */
//    public static SimpleTensor generateNewSymbol() {
//        return current().generateNewSymbol();
//    }
//
//    /**
//     * Return output port which generates new symbol via {@link #generateNewSymbol()} at each {@code take()} invocation.
//     *
//     * @return output port which generates new symbol via {@link #generateNewSymbol()} at each {@code take()} invocation.
//     */
//    public static OutputPort<SimpleTensor> getParametersGenerator() {
//        return current().getDefaultParametersGenerator();
//    }

    /**
     * Returns random generator used by Redberry in current session.
     *
     * @return random generator used by Redberry in current session
     */
    public static RandomGenerator getRandomGenerator() {
        return current().getRandomGenerator();
    }

    /**
     * Allows to parse expressions with repeated indices of the same variance (like T_aa or T_a*T^a which results in T^a_a
     * and T^a*T_a respactively)
     *
     * @param b allow or not to parse repeated indices with same variance
     */
    public static void setParserAllowsSameVariance(boolean b) {
        current().setParserAllowsSameVariance(b);
    }

    /**
     * Returns whether repeated indices of the same variance are allowed to be parsed
     *
     * @return whether repeated indices of the same variance are allowed to be parsed
     */
    public static boolean getParserAllowsSameVariance() {
        return current().getParserAllowsSameVariance();
    }
}
