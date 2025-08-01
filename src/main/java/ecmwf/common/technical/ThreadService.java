/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In applying the License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.ThreadContext.ContextStack;
import org.apache.sling.commons.threads.impl.ThreadLocalChangeListener;
import org.apache.sling.commons.threads.impl.ThreadPoolExecutorCleaningThreadLocals;

import ecmwf.common.rmi.interruptible.InterruptibleRMIThread;
import ecmwf.common.text.Format;

/**
 * The Class ThreadService.
 */
public final class ThreadService {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ThreadService.class);

    /** The Constant ALLOW_VIRTUAL_THREAD. */
    private static final boolean ALLOW_VIRTUAL_THREAD = Cnf.at("ThreadService", "allowVirtualThread", false);

    /** The Constant DEBUG_THREAD_LOCAL. */
    private static final boolean DEBUG_THREAD_LOCAL = Cnf.at("ThreadService", "debugThreadLocal", false);

    /**
     * Instantiates a new thread service. Utility classes don't need public constructors!
     */
    private ThreadService() {
    }

    /**
     * The Class CloseableExecutorService.
     */
    private final static class CloseableExecutorService extends AbstractExecutorService implements AutoCloseable {

        /** The delegate. */
        private final ExecutorService delegate;

        /** The timeout. */
        private final long timeout;

        /** The unit. */
        private final TimeUnit unit;

        /**
         * Instantiates a new closeable executor service.
         *
         * @param delegate
         *            the delegate
         * @param timeout
         *            the timeout
         * @param unit
         *            the unit
         */
        public CloseableExecutorService(final ExecutorService delegate, final long timeout, final TimeUnit unit) {
            this.delegate = delegate;
            this.timeout = timeout;
            this.unit = unit;
        }

        /**
         * Shutdown.
         */
        @Override
        public void shutdown() {
            delegate.shutdown();
        }

        /**
         * Shutdown now.
         *
         * @return the list
         */
        @Override
        public List<Runnable> shutdownNow() {
            return delegate.shutdownNow();
        }

        /**
         * Checks if is shutdown.
         *
         * @return true, if is shutdown
         */
        @Override
        public boolean isShutdown() {
            return delegate.isShutdown();
        }

        /**
         * Checks if is terminated.
         *
         * @return true, if is terminated
         */
        @Override
        public boolean isTerminated() {
            return delegate.isTerminated();
        }

        /**
         * Await termination.
         *
         * @param timeout
         *            the timeout
         * @param unit
         *            the unit
         *
         * @return true, if successful
         *
         * @throws InterruptedException
         *             the interrupted exception
         */
        @Override
        public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }

        /**
         * Execute.
         *
         * @param command
         *            the command
         */
        @Override
        public void execute(final Runnable command) {
            delegate.execute(command);
        }

        /**
         * Close.
         */
        @Override
        public void close() {
            delegate.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait for existing tasks to terminate
                if (!delegate.awaitTermination(timeout, unit)) {
                    delegate.shutdownNow(); // Cancel currently executing tasks
                    // Wait again for tasks to respond to being cancelled
                    if (!delegate.awaitTermination(timeout, unit)) {
                        _log.warn("Executor did not terminate");
                    }
                }
            } catch (final InterruptedException ie) {
                delegate.shutdownNow();
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        }
    }

    /**
     * Gets the cleaning thread local executor service.
     *
     * @param nThreads
     *            the n threads
     * @param useVirtualThreads
     *            the use virtual threads
     * @param daemonThreads
     *            the daemon threads
     *
     * @return the executor service
     */
    public static ExecutorService getCleaningThreadLocalExecutorService(final int nThreads,
            final boolean useVirtualThreads, final boolean daemonThreads) {
        return new CloseableExecutorService(ConfigurableThreadFactory.getExecutorService(nThreads, nThreads,
                new LinkedBlockingQueue<>(), false, useVirtualThreads && ALLOW_VIRTUAL_THREAD, daemonThreads), 5,
                TimeUnit.SECONDS);
    }

    /**
     * Gets the single cleaning thread local executor service.
     *
     * @param useVirtualThreads
     *            the use virtual threads
     * @param daemonThreads
     *            the daemon threads
     *
     * @return the executor service
     */
    public static ExecutorService getSingleCleaningThreadLocalExecutorService(final boolean useVirtualThreads,
            final boolean daemonThreads) {
        return getCleaningThreadLocalExecutorService(1, useVirtualThreads, daemonThreads);
    }

    /**
     * Sets the cookie if not already set.
     *
     * @param name
     *            the name
     *
     * @return true, if successful
     */
    public static boolean setCookieIfNotAlreadySet(final String name) {
        final var current = ThreadContext.peek();
        if (current == null || current.isEmpty()) {
            setCookie(name);
            return true;
        }
        return false;
    }

    /**
     * Sets the cookie.
     *
     * @param name
     *            the new cookie
     */
    public static void setCookie(final String name) {
        ThreadContext.pop();
        ThreadContext.push(name);
    }

    /**
     * Removes the cookie.
     */
    public static void removeCookie() {
        ThreadContext.clearAll();
    }

    /**
     * Gets the cookie.
     *
     * @return the cookie
     */
    public static String getCookie() {
        return ThreadContext.peek();
    }

    /**
     * The Class ConfigurableLoopRunnable.
     */
    public abstract static class ConfigurableLoopRunnable extends ConfigurableRunnable {
        /** The loop. */
        private boolean loop = true;

        /** The pause. */
        private volatile long pause = 0;

        /**
         * Gets the loop.
         *
         * @return the loop
         */
        public final boolean getLoop() {
            return isAlive() && loop;
        }

        /**
         * Sets the pause.
         *
         * @param pause
         *            the new pause
         */
        public final void setPause(final long pause) {
            this.pause = pause;
        }

        /**
         * Sets the loop.
         *
         * @param loop
         *            the new loop
         */
        public final void setLoop(final boolean loop) {
            this.loop = loop;
        }

        /**
         * Shutdown.
         */
        public final void shutdown() {
            shutdown(0);
        }

        /**
         * Shutdown.
         *
         * @param timeout
         *            the timeout
         */
        public final void shutdown(final long timeout) {
            loop = false;
            if (timeout > 0) {
                try {
                    join(timeout);
                } catch (final InterruptedException _) {
                    Thread.currentThread().interrupt();
                } catch (final Exception e) {
                    _log.warn("shutdown", e);
                }
            }
            if (isAlive()) {
                interrupt();
            }
        }

        /**
         * Configurable run.
         */
        @Override
        public final void configurableRun() {
            try {
                configurableLoopStart();
                while (loop) {
                    if (pause > 0) {
                        try {
                            if (!Thread.interrupted()) {
                                Thread.sleep(pause);
                            }
                        } catch (final InterruptedException _) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    if (loop) {
                        final var currentThread = Thread.currentThread();
                        final var originalCL = currentThread.getContextClassLoader();
                        try {
                            configurableLoopRun();
                        } finally {
                            currentThread.setContextClassLoader(originalCL);
                            ThreadContext.clearAll();
                        }
                    }
                }
            } catch (final Throwable t) {
                _log.warn("configurableRun", t);
            } finally {
                try {
                    configurableLoopEnd();
                } catch (final Throwable t) {
                    _log.warn("configurableRun", t);
                }
            }
        }

        /**
         * Configurable loop start.
         *
         * @throws Exception
         *             the exception
         */
        public void configurableLoopStart() throws Exception {
        }

        /**
         * Configurable loop end.
         *
         * @throws Exception
         *             the exception
         */
        public void configurableLoopEnd() throws Exception {
        }

        /**
         * Configurable loop run.
         *
         * @throws Exception
         *             the exception
         */
        public abstract void configurableLoopRun() throws Exception;
    }

    /**
     * The Class ConfigurableRunnable.
     */
    public abstract static class ConfigurableRunnable implements Runnable {
        /** The Constant connectionNumbers. */
        private static final Map<String, AtomicLong> connectionNumbers = new ConcurrentHashMap<>();

        /** The Constant threadNumbers. */
        private static final Map<String, AtomicLong> threadNumbers = new ConcurrentHashMap<>();

        /** The started. */
        private final AtomicBoolean started = new AtomicBoolean(false);

        /** The thread sync. */
        private final Object threadSync = new Object();

        /** The name sync. */
        private final Object nameSync = new Object();

        /** The stack. */
        private final ContextStack stack = ThreadContext.cloneStack();

        /** The thread. */
        private volatile Thread thread = null;

        /** The name. */
        private volatile String name = null;

        /** The _started from. */
        private String startedFrom = null;

        /** The cookie. */
        private volatile String cookie = null;

        /** The priority. */
        private int priority = Thread.NORM_PRIORITY;

        /** The classLoader. */
        private volatile ClassLoader classLoader = null;

        /** The inheritCookie. */
        private volatile boolean inheritCookie = true;

        /** The forceCookie. */
        private volatile boolean forceCookie = false;

        /** The interruptible. */
        private boolean interruptible = false;

        /** The startTime. */
        private long startTime;

        /**
         * Check if the Thread has been started. Started does not mean that it is still running.
         *
         * @return true, if is started
         */
        public final boolean isStarted() {
            return started.get();
        }

        /**
         * Execute.
         */
        public final void execute() {
            execute(false);
        }

        /**
         * Execute.
         *
         * @param interruptibleRMIThread
         *            the interruptible RMI thread
         */
        public final void execute(final boolean interruptibleRMIThread) {
            execute(interruptibleRMIThread, ALLOW_VIRTUAL_THREAD);
        }

        /**
         * Execute.
         *
         * @param interruptibleRMIThread
         *            the interruptible rmi thread
         * @param useVirtualThreads
         *            the use virtual threads
         */
        public final void execute(final boolean interruptibleRMIThread, final boolean useVirtualThreads) {
            if (started.compareAndSet(false, true)) {
                final var start1 = System.currentTimeMillis();
                interruptible = interruptibleRMIThread;
                startedFrom = Thread.currentThread().getName();
                thread = new ConfigurableThreadFactory(interruptible, useVirtualThreads, false).newThread(this);
                startTime = System.currentTimeMillis();
                thread.start();
                _log.debug("Execute started: elapsed1={}ms,elapsedTotal={}ms", startTime - start1,
                        System.currentTimeMillis() - start1);
            } else {
                _log.debug("Already started");
            }
        }

        /**
         * Interrupt.
         *
         * @return true, if successful
         */
        public final boolean interrupt() {
            final var className = Format.getClassName(this);
            synchronized (threadSync) {
                if (thread != null) {
                    _log.debug("{} interrupted", className);
                    thread.interrupt();
                    return true;
                } else {
                    return false;
                }
            }
        }

        /**
         * Interrupted.
         *
         * @return true, if successful
         */
        public final boolean interrupted() {
            return Thread.interrupted();
        }

        /**
         * Sets the thread.
         *
         * @param newThread
         *            the new thread
         */
        private void setThread(final Thread newThread) {
            synchronized (threadSync) {
                if ((thread = newThread) != null) {
                    newThread.setPriority(priority);
                    if (classLoader != null) {
                        newThread.setContextClassLoader(classLoader);
                    }
                    if (name == null) {
                        setThreadNameAndCookie(null, null, null, null);
                    } else {
                        newThread.setName(name);
                    }
                }
            }
        }

        /**
         * Sets the context class loader.
         *
         * @param classLoader
         *            the new context class loader
         */
        public final void setContextClassLoader(final ClassLoader classLoader) {
            synchronized (threadSync) {
                if (thread != null) {
                    thread.setContextClassLoader(classLoader);
                }
            }
            this.classLoader = classLoader;
        }

        /**
         * Gets the context class loader.
         *
         * @return the context class loader
         */
        public final ClassLoader getContextClassLoader() {
            synchronized (threadSync) {
                if (thread != null) {
                    return thread.getContextClassLoader();
                }
            }
            return classLoader;
        }

        /**
         * Sets the priority.
         *
         * @param newPriority
         *            the new priority
         */
        public final void setPriority(final int newPriority) {
            synchronized (threadSync) {
                if (thread != null) {
                    thread.setPriority(newPriority);
                }
            }
            priority = newPriority;
        }

        /**
         * Gets the priority.
         *
         * @return the priority
         */
        public final int getPriority() {
            synchronized (threadSync) {
                if (thread != null) {
                    return thread.getPriority();
                }
            }
            return priority;
        }

        /**
         * Checks if is alive.
         *
         * @return true, if is alive
         */
        public final boolean isAlive() {
            synchronized (threadSync) {
                if (thread != null) {
                    return thread.isAlive();
                } else {
                    return false;
                }
            }
        }

        /**
         * Join.
         *
         * @return true, if successful
         *
         * @throws InterruptedException
         *             the interrupted exception
         * @throws ExecutionException
         *             the execution exception
         * @throws TimeoutException
         *             the timeout exception
         */
        public final boolean join() throws InterruptedException, ExecutionException, TimeoutException {
            return join(Timer.ONE_WEEK);
        }

        /**
         * Join.
         *
         * @param timeout
         *            the timeout
         *
         * @return true if we had to wait for the thread
         *
         * @throws InterruptedException
         *             the interrupted exception
         * @throws ExecutionException
         *             the execution exception
         * @throws TimeoutException
         *             the timeout exception
         */
        public final boolean join(final long timeout)
                throws InterruptedException, ExecutionException, TimeoutException {
            if (isAlive() && thread != null) {
                thread.join(timeout);
                return true;
            }
            return false;
        }

        /**
         * Sets the thread name and cookie.
         *
         * @param info
         *            the info
         * @param user
         *            the user
         * @param context
         *            the context
         * @param address
         *            the address
         */
        public final void setThreadNameAndCookie(final String info, final String user, final String context,
                final String address) {
            final var threadName = new StringBuilder(Format.getClassName(getClass()))
                    .append(isNotEmpty(info) ? "(" + info + ")" : "");
            synchronized (nameSync) { // This method could be used concurrently!
                if (isNotEmpty(address)) {
                    final var threadKey = getValue(connectionNumbers, threadName);
                    threadName.append(" Connection(").append(threadKey).append(")-").append(address);
                } else {
                    final var threadKey = getValue(threadNumbers, threadName);
                    threadName.append("-").append(threadKey);
                }
                name = threadName.toString();
                synchronized (threadSync) { // The thread might not be created yet!
                    if (thread != null) {
                        thread.setName(name);
                    }
                }
                final var cookieName = (Format.trimString(user, "") + " " + Format.trimString(context, "")).trim();
                if (!cookieName.isEmpty()) {
                    cookie = cookieName;
                }
            }
        }

        /**
         * Gets the value.
         *
         * @param map
         *            the map
         * @param key
         *            the key
         *
         * @return the value
         */
        private static long getValue(final Map<String, AtomicLong> map, final StringBuilder key) {
            return map.computeIfAbsent(key.toString(), _ -> new AtomicLong(0))
                    .updateAndGet(c -> c == Long.MAX_VALUE ? 1 : c + 1);
        }

        /**
         * Gets the thread name.
         *
         * @return the thread name
         */
        public final String getThreadName() {
            synchronized (threadSync) {
                if (thread != null) {
                    return thread.getName();
                }
            }
            return name;
        }

        /**
         * Sets the inherit cookie.
         *
         * @param newInheritCookie
         *            the new inherit cookie
         */
        public final void setInheritCookie(final boolean newInheritCookie) {
            inheritCookie = newInheritCookie;
        }

        /**
         * Sets the force cookie.
         *
         * @param forceCookie
         *            the new force cookie
         */
        public final void setForceCookie(final boolean forceCookie) {
            this.forceCookie = forceCookie;
        }

        /**
         * Run.
         */
        @Override
        public final void run() {
            if (inheritCookie) {
                ThreadContext.setStack(stack);
            }
            final var current = Thread.currentThread();
            final var originalName = current.getName();
            final var originalClassLoader = current.getContextClassLoader();
            setThread(current);
            if (isNotEmpty(cookie)) {
                if (!forceCookie) {
                    setCookieIfNotAlreadySet(cookie);
                } else {
                    setCookie(cookie);
                }
            }
            if (startedFrom != null) {
                _log.debug("Thread started from {} in {}ms (interruptible={},virtual={},daemon={})", startedFrom,
                        System.currentTimeMillis() - startTime, interruptible, current.isVirtual(), current.isDaemon());
            } else {
                _log.debug("Thread NOT started with ConfigurableRunnable.execute()");
            }
            final var startRun = System.currentTimeMillis();
            try {
                configurableRun();
            } finally {
                _log.debug("Thread completed after {}ms", System.currentTimeMillis() - startRun);
                setThread(null);
                current.setName(originalName);
                removeCookie();
                // Cleanup thread-locals, MDC, and reset context class loader to avoid leaks
                ThreadContext.clearAll();
                current.setContextClassLoader(originalClassLoader); // Restore to avoid leaks
            }
        }

        /**
         * Configurable run.
         */
        public abstract void configurableRun();
    }

    /**
     * A factory for creating ConfigurableThread objects.
     */
    private static class ConfigurableThreadFactory implements ThreadFactory {
        /** The Constant poolNumber. */
        static final AtomicLong poolNumber = new AtomicLong(1);

        /** The interruptibleRMIThread. */
        final boolean interruptibleRMIThread;

        /** The useVirtualThreads. */
        final boolean useVirtualThreads;

        /** The group. */
        final ThreadGroup group;

        /** The threadNumber. */
        final AtomicLong threadNumber = new AtomicLong(1);

        /** The namePrefix. */
        final String namePrefix;

        /** The daemon flag. */
        final boolean daemon;

        /**
         * Gets the executor service.
         *
         * @param corePoolSize
         *            the core pool size
         * @param maximumPoolSize
         *            the maximum pool size
         * @param workQueue
         *            the work queue
         * @param interruptibleRMIThread
         *            the interruptible rmi thread
         * @param useVirtualThreads
         *            the use virtual threads
         * @param daemonThreads
         *            the daemon threads
         *
         * @return the executor service
         */
        static ExecutorService getExecutorService(final int corePoolSize, final int maximumPoolSize,
                final BlockingQueue<Runnable> workQueue, final boolean interruptibleRMIThread,
                final boolean useVirtualThreads, final boolean daemonThreads) {
            try {
                return new ThreadPoolExecutorCleaningThreadLocals(corePoolSize, maximumPoolSize, 60L, TimeUnit.SECONDS,
                        workQueue,
                        new ConfigurableThreadFactory(interruptibleRMIThread, useVirtualThreads, daemonThreads),
                        new ThreadPoolExecutor.AbortPolicy(), new ThreadLocalChangeListener() {
                            @Override
                            public boolean isEnabled() {
                                return true;
                            }

                            @Override
                            public void changed(final Mode mode, final Thread thread, final ThreadLocal<?> threadLocal,
                                    final Object value) {
                                if (DEBUG_THREAD_LOCAL && _log.isDebugEnabled()) {
                                    final var discouraged = mode == Mode.ADDED
                                            && threadLocal.getClass().getName().contains("InheritableThreadLocal");
                                    _log.debug("ThreadLocal {}: {} value: {}{}", mode.name(), threadLocal, value,
                                            discouraged ? " (InheritableThreadLocal is discouraged)" : "");
                                }
                            }
                        });
            } catch (final Throwable t) {
                _log.warn("Cannot use ThreadPoolExecutorCleaningThreadLocals, switch to ThreadPoolExecutor", t);
                return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60L, TimeUnit.SECONDS, workQueue,
                        new ConfigurableThreadFactory(interruptibleRMIThread, useVirtualThreads, daemonThreads));
            }
        }

        /**
         * Instantiates a new configurable thread factory.
         *
         * @param interruptibleRMIThread
         *            the interruptible rmi thread
         * @param useVirtualThreads
         *            the use virtual threads
         * @param daemon
         *            the daemon
         */
        ConfigurableThreadFactory(final boolean interruptibleRMIThread, final boolean useVirtualThreads,
                final boolean daemon) {
            group = Thread.currentThread().getThreadGroup();
            namePrefix = "Pool-" + poolNumber.getAndIncrement() + "-Thread-";
            this.interruptibleRMIThread = interruptibleRMIThread;
            this.useVirtualThreads = useVirtualThreads;
            this.daemon = daemon;
        }

        /**
         * New thread.
         *
         * @param runnable
         *            the runnable
         *
         * @return the thread
         */
        @Override
        public Thread newThread(final Runnable runnable) {
            final var name = namePrefix + threadNumber.getAndIncrement();
            Thread t;
            if (interruptibleRMIThread) {
                t = new InterruptibleRMIThread(group, runnable, name, 0);
            } else {
                t = useVirtualThreads && ALLOW_VIRTUAL_THREAD ? Thread.ofVirtual().name(name).unstarted(runnable)
                        : Thread.ofPlatform().group(group).name(name).unstarted(runnable);
            }
            if (!t.isVirtual())
                t.setDaemon(daemon);
            return t;
        }
    }
}
