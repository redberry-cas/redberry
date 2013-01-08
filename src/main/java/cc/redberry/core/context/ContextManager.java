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

import cc.redberry.core.context.defaults.DefaultContextFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * This class implements context management logic.
 * It holds current thread-local context of Redberry session. It is possible
 * to set context explicitly using {@link #setCurrentContext(Context)} method.
 * Each thread is linked to its own context. All child threads created via {@code ExecutorService}
 * from {@link #getExecutorService()} have same context.
 *
 * @author Dmitriy Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class ContextManager {
    /**
     * Thread-local container for current context
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

    /**
     * Returns the current context of Redberry session.
     *
     * @return the current context of Redberry session.
     */
    public static Context getCurrentContext() {
        return threadLocalContainer.get().context;
    }

    /**
     * This method initializes and sets current session context by the default
     * value defined in {@link DefaultContextFactory}. After this step, all the
     * tensors that exist in the thread will be broken.
     *
     * @return created context
     */
    public static Context initializeNew() {
        Context context = DefaultContextFactory.INSTANCE.createContext();
        threadLocalContainer.get().context = context;
        return context;
    }

    /**
     * This method initializes and sets current session context from
     * the specified {@code context settings}. After this step, all the
     * tensors that exist in the thread will be broken.
     *
     * @return created context
     */
    public static Context initializeNew(ContextSettings contextSettings) {
        Context context = new Context(contextSettings);
        threadLocalContainer.get().context = context;
        return context;
    }

    /**
     * Sets current thread-local context to the specified one. After this step, all the
     * tensors that exist in the thread will be broken.
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

    private static class ContextContainer {
        volatile Context context = DefaultContextFactory.INSTANCE.createContext();
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
