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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
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

    /** The Constant USE_THREAD_SPOOL. */
    private static final boolean USE_THREAD_SPOOL = Cnf.at("Server", "useThreadSpool", true);

	/** The Constant USE_VIRTUAL_THREAD. */
	private static final boolean USE_VIRTUAL_THREAD = Cnf.at("Server", "useVirtualThread", true);

    /** The Constant configurablePool. */
    private static final ExecutorService configurablePool = ConfigurableThreadFactory.getExecutorService(false);

    /** The Constant interruptiblePool. */
    private static final ExecutorService interruptiblePool = ConfigurableThreadFactory.getExecutorService(true);

    /**
     * Instantiates a new thread service. Utility classes don't need public constructors!
     */
    private ThreadService() {
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
        if (current == null || current.length() == 0) {
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
        private long pause = 0;

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
                        } catch (final InterruptedException e) {
                        }
                    }
                    if (loop) {
                        configurableLoopRun();
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

        /** The futur. */
        private Future<?> futur = null;

        /** The thread. */
        private Thread thread = null;

        /** The _started from. */
        private String startedFrom = null;

        /** The name. */
        private String name = null;

        /** The cookie. */
        private String cookie = null;

        /** The priority. */
        private int priority = Thread.NORM_PRIORITY;

        /** The classLoader. */
        private ClassLoader classLoader = null;

        /** The inheritCookie. */
        private boolean inheritCookie = true;

        /** The forceCookie. */
        private boolean forceCookie = false;

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
         *            the interruptible rmi thread
         */
        public final void execute(final boolean interruptibleRMIThread) {
            if (started.compareAndSet(false, true)) {
                final var start1 = System.currentTimeMillis();
                interruptible = interruptibleRMIThread;
                startedFrom = Thread.currentThread().getName();
                if (USE_THREAD_SPOOL) {
                    startTime = System.currentTimeMillis();
                    futur = (interruptible ? interruptiblePool : configurablePool).submit(this);
                } else {
                    thread = new ConfigurableThreadFactory(interruptible).newThread(this);
                    startTime = System.currentTimeMillis();
                    thread.start();
                }
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
            if (futur != null) {
                _log.debug("{} canceled", className);
                return futur.cancel(true);
            }
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
            if (futur != null) {
                return futur.isCancelled();
            }
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
            if (futur != null) {
                return !futur.isCancelled() && !futur.isDone();
            }
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
         * @throws InterruptedException
         *             the interrupted exception
         * @throws ExecutionException
         *             the execution exception
         * @throws TimeoutException
         *             the timeout exception
         */
        public final void join() throws InterruptedException, ExecutionException, TimeoutException {
            join(Timer.ONE_WEEK);
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
            if (isAlive()) {
                if (futur != null) {
                    futur.get(timeout, TimeUnit.MILLISECONDS);
                    return true;
                }
                if (thread != null) {
                    thread.join(timeout);
                    return true;
                }
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
                if (cookieName.length() > 0) {
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
            return map.computeIfAbsent(key.toString(), k -> new AtomicLong(0))
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
            setThread(current);
            if (isNotEmpty(cookie)) {
                if (!forceCookie) {
                    setCookieIfNotAlreadySet(cookie);
                } else {
                    setCookie(cookie);
                }
            }
            if (startedFrom != null) {
				_log.debug("Thread started from {} in {}ms (interruptible={},virtual={})", startedFrom,
						System.currentTimeMillis() - startTime, interruptible, current.isVirtual());
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
                if (inheritCookie) {
                    ThreadContext.clearStack();
                }
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

        /** The group. */
        final ThreadGroup group;

        /** The threadNumber. */
        final AtomicLong threadNumber = new AtomicLong(1);

        /** The namePrefix. */
        final String namePrefix;

        /**
         * Gets the executor service.
         *
         * @param interruptibleRMIThread
         *            the interruptible rmi thread
         *
         * @return the executor service
         */
        static ExecutorService getExecutorService(final boolean interruptibleRMIThread) {
            try {
                return new ThreadPoolExecutorCleaningThreadLocals(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                        new SynchronousQueue<>(), new ConfigurableThreadFactory(interruptibleRMIThread),
                        new ThreadPoolExecutor.AbortPolicy(), new ThreadLocalChangeListener() {
                            @Override
                            public boolean isEnabled() {
                                return false;
                            }

                            @Override
                            public void changed(final Mode mode, final Thread thread, final ThreadLocal<?> threadLocal,
                                    final Object value) {
                            }
                        });
            } catch (final Throwable t) {
                _log.warn("Cannot use ThreadPoolExecutorCleaningThreadLocals, switch to newCachedThreadPool", t);
                return Executors.newCachedThreadPool(new ConfigurableThreadFactory(interruptibleRMIThread));
            }
        }

        /**
         * Instantiates a new configurable thread factory.
         *
         * @param interruptibleRMIThread
         *            the interruptible rmi thread
         */
        ConfigurableThreadFactory(final boolean interruptibleRMIThread) {
            group = Thread.currentThread().getThreadGroup();
            namePrefix = "Pool-" + poolNumber.getAndIncrement() + "-Thread-";
            this.interruptibleRMIThread = interruptibleRMIThread;
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
			final Thread t;
			if (interruptibleRMIThread) { // Need a redesign to allow using virtual threads!
				t = new InterruptibleRMIThread(group, runnable, name, 0);
			} else {
				t = USE_VIRTUAL_THREAD ? Thread.ofVirtual().name(name).unstarted(runnable)
						: Thread.ofPlatform().group(group).name(name).unstarted(runnable);
			}
			if (!USE_VIRTUAL_THREAD && t.isDaemon()) {
				_log.debug("Deactivate daemon flag for Thread: {}", name);
				t.setDaemon(false);
			}
			return t;
		}
    }
}
