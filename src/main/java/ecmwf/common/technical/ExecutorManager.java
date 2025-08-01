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
 * This class manages a pool of threads that execute tasks.
 *
 * It extends the Thread class, which means that it is itself a thread that can
 * be started and stopped, and it has its own run method that is executed when
 * the thread starts.
 *
 * The class uses an ExecutorService to manage the pool of threads. When a task
 * is submitted to the manager using the put or offer methods, the manager adds
 * the task to a queue of waiting tasks. As soon as a thread becomes available,
 * the manager dequeues the next task from the queue and submits it to the
 * executor service for execution.
 *
 * The manager keeps track of the number of threads currently running using an
 * AtomicInteger called threadCount, and it keeps track of the number of tasks
 * executed using an int called count.
 *
 * The manager runs a loop in its run method that continues until the manager is
 * stopped. The loop checks if there are any tasks in the waiting queue, and if
 * so, it dequeues the next task and submits it to the executor service. The
 * manager then increments the threadCount counter to indicate that a new thread
 * is running, and it removes the task from the waiting queue. If there are no
 * tasks in the waiting queue, the loop sleeps for 10 milliseconds before
 * checking again.
 *
 * When the manager is stopped, it stops accepting new tasks and waits for all
 * the currently running threads to complete before shutting down the executor
 * service. The stopAndJoin method can be used to stop the manager and wait for
 * all the threads to complete before returning. *
 *
 * @param <O> the generic type
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class ExecutorManager.
 *
 * @param <O>
 *            the generic type
 */
public final class ExecutorManager<O extends ExecutorRunnable> extends Thread {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ExecutorManager.class);

    /** The Constant ALLOW_VIRTUAL_THREAD. */
    private static final boolean ALLOW_VIRTUAL_THREAD = Cnf.at("ExecutorManager", "allowVirtualThread", false);

    /** The thread count. */
    private final AtomicInteger threadCount = new AtomicInteger(0);

    /** The started flag. */
    private final AtomicBoolean started = new AtomicBoolean(false);

    /** The continueRunning. */
    private final AtomicBoolean continueRunning = new AtomicBoolean(true);

    /** The count. */
    private final AtomicInteger totalRunCount = new AtomicInteger(0);

    /** The waiting list. */
    private final ArrayBlockingQueue<O> waitingList;

    /** The max running. */
    private final int maxRunning;

    /**
     * Instantiates a new executor manager.
     *
     * @param maxWaiting
     *            the max waiting
     * @param maxRunning
     *            the max running
     */
    public ExecutorManager(final int maxWaiting, final int maxRunning) {
        waitingList = new ArrayBlockingQueue<>(maxWaiting);
        this.maxRunning = maxRunning;
    }

    /**
     * Gets the thread count.
     *
     * @return the thread count
     */
    public AtomicInteger getThreadCount() {
        return threadCount;
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public int getCount() {
        return totalRunCount.get();
    }

    /**
     * Add a new Thread to the list. Inserts the Thread at the tail of this queue, waiting for space to become available
     * if the queue is full.
     *
     * @param entry
     *            the entry
     *
     * @throws InterruptedException
     *             the interrupted exception
     */
    public void put(final O entry) throws InterruptedException {
        waitingList.put(entry);
    }

    /**
     * Inserts the Thread at the tail of this queue if it is possible to do so immediately without exceeding the queue's
     * capacity, return false if this queue is full. If the Thread already exists in the queue then return true and do
     * nothing.
     *
     * @param entry
     *            the entry
     *
     * @return true, if successful
     */
    public boolean offer(final O entry) {
        return waitingList.contains(entry) || waitingList.offer(entry);
    }

    /**
     * Start the Thread if it is not yet started.
     */
    public void startIfNotStarted() {
        if (!isAlive() && started.compareAndSet(false, true)) {
            _log.debug("Requesting start");
            start();
        }
    }

    /**
     * Ends the loop.
     */
    public void stopRun() {
        _log.debug("Requesting stop");
        continueRunning.set(false);
    }

    /**
     * Run.
     */
    @Override
    public void run() {
        _log.debug("ExecutorManager started");
        try (final var executor = ThreadService.getCleaningThreadLocalExecutorService(maxRunning, ALLOW_VIRTUAL_THREAD,
                true)) {
            while (continueRunning.get() || !waitingList.isEmpty() || threadCount.get() > 0) {
                try {
                    final var thread = waitingList.poll(1, TimeUnit.SECONDS);
                    if (thread != null) {
                        executor.execute(thread);
                        threadCount.incrementAndGet();
                        totalRunCount.incrementAndGet();
                    }
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                    stopRun();
                    break;
                }
            }
        } finally {
            waitingList.clear();
            _log.debug("ExecutorManager completed (processed {} run(s))", totalRunCount.get());
        }
    }

    /**
     * Stop and join if it was previously started and is still alive.
     *
     * @throws InterruptedException
     *             the interrupted exception
     */
    public void stopAndJoin() throws InterruptedException {
        if (started.get() && isAlive()) {
            // We don't want to take more jobs!
            stopRun();
            // And now we wait for all the Threads to complete!
            join();
        }
    }
}
