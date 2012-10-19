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
 * This class implements context management logic.<br> There are two modes of
 * action: <ul> <li><b>Global</b> <i>[threadLocalMode=false]</i> - one global
 * context is used by all framework. It is useful in common cases (groovy
 * script, program solving one problem, ...). This mode is default.</li>
 * <li><b>ThreadLocal</b> <i>[threadLocalMode=true]</i> - each thread has it's
 * context. In this case you can set it explicitly using
 * <code>setCurrentContext</code> method. This mode is useful in cases where
 * several contexts needs (GUI, web application, ...). This mode is not
 * finished, so use it carefully, because interface could change in future
 * releases.</li> </ul>
 */
public final class ContextManager {
    private final static ThreadLocal<ContextContainer> threadLocalContainer = new ThreadLocal<ContextContainer>() {
        @Override
        protected ContextContainer initialValue() {
            return new ContextContainer();
        }
    };
    private static final ThreadLocal<ExecutorService> executorService = new ThreadLocal<ExecutorService>() {
        @Override
        protected ExecutorService initialValue() {
            return Executors.newCachedThreadPool(new CThreadFactory(threadLocalContainer.get()));
        }
    };

    private ContextManager() {
    }

    public static Context getCurrentContext() {
        return threadLocalContainer.get().context;
    }

    public static Context initializeNew() {
        Context context = DefaultContextFactory.INSTANCE.createContext();
        threadLocalContainer.get().context = context;
        return context;
    }

    public static Context initializeNew(ContextSettings contextSettings) {
        Context context = new Context(contextSettings);
        threadLocalContainer.get().context = context;
        return context;
    }

    public static void setCurrentContext(Context context) {
        threadLocalContainer.get().context = context;
    }

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
