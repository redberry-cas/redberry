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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * This class implements context management logic.
 *
 * <p>It holds current thread-local context of Redberry session (see description for {@link Context} class).
 * It is possible to set context explicitly using {@link #setCurrentContext(Context)} method.
 * Each thread is linked to its own context. All child threads created via {@code ExecutorService}
 * from {@link #getExecutorService()} have same context.</p>
 *
 * @author Dmitriy Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class ContextManager {
    /**
     * Thread-local container for the current context
     */
    private final static ThreadLocal<ContextContainer> threadLocalContainer = new ThreadLocal<ContextContainer>() {
        @Override
        protected ContextContainer initialValue() {
            return new ContextContainer();
        }
    };
    /**
     * Thread-local {@code ExecutorService}
     */
    private static final ThreadLocal<ExecutorService> executorService = new ThreadLocal<ExecutorService>() {
        @Override
        protected ExecutorService initialValue() {
            return Executors.newCachedThreadPool(new CThreadFactory(threadLocalContainer.get()));
        }
    };

    private ContextManager() {
    }

    public static Context createDefaultContext() {
        return new Context(new ContextConfiguration());
    }

    /**
     * Returns the current context
     *
     * @return the current context
     */
    public static Context getCurrentContext() {
        return threadLocalContainer.get().get();
    }

    /**
     * Returns the current context configuration
     *
     * @return the current context configuration
     */
    public static ContextConfiguration getCurrentContextConfiguration() {
        return threadLocalContainer.get().contextConfiguration;
    }

    /**
     * This method initializes and sets current session context by the default
     * value defined in {@link #createDefaultContext()}. After this step, all
     * tensors that exist in the thread will be invalidated.
     */
    public static void initializeNew() {
        initializeNew(new ContextConfiguration());
    }

    /**
     * This method initializes and sets current session context from
     * the specified {@code context settings} ({@link ContextConfiguration}).
     * After invocation of this method, all the tensors that exist in
     * the current thread will be invalidated.
     */
    public static void initializeNew(ContextConfiguration contextConfiguration) {
        final ContextContainer cc = threadLocalContainer.get();
        cc.newConfiguration(contextConfiguration.clone());
    }

    /**
     * Sets current thread-local context to the specified one. After this step, all the
     * tensors that exist in the thread will be invalidated.
     *
     * @param context context
     */
    public static void setCurrentContext(Context context) {
        threadLocalContainer.get().context = context;
    }

    /**
     * Returns thread-local {@code ExecutorService} with fixed context. All threads linked
     * to this {@code ExecutorService} will have same context.
     *
     * @return thread-local {@code ExecutorService} with fixed context
     */
    public static ExecutorService getExecutorService() {
        return executorService.get();
    }

    private static final class ContextContainer {
        volatile ContextConfiguration contextConfiguration = new ContextConfiguration();
        private volatile Context context = null;

        void newConfiguration(ContextConfiguration contextConfiguration) {
            this.contextConfiguration = contextConfiguration;
            this.context = null;
        }

        Context get() {
            if (context == null) {
                synchronized (this) {
                    if (context == null)
                        context = new Context(contextConfiguration);
                }
            }
            return context;
        }
    }

    private static class CThreadFactory implements ThreadFactory {
        private final ContextContainer container;

        public CThreadFactory(ContextContainer container) {
            this.container = container;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(new RunnableWrapper(container, r));
        }
    }

    private static class RunnableWrapper implements Runnable {
        private final ContextContainer container;
        private final Runnable innerRunnable;

        public RunnableWrapper(ContextContainer container, Runnable innerRunnable) {
            this.container = container;
            this.innerRunnable = innerRunnable;
        }

        @Override
        public void run() {
            threadLocalContainer.set(container);
            innerRunnable.run();
        }
    }
}
