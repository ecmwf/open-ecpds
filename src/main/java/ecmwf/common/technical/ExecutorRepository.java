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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * This class provides a mechanism for executing a set of actions in parallel on
 * a collection of objects of type O.
 *
 * The class has two constructors, which take in a list of exceptions, an atomic
 * long for keeping track of the number of objects processed, and the collection
 * of objects to be processed. The second constructor also takes in two integer
 * values for configuring the maximum number of actions that can be waiting or
 * running at any given time.
 *
 * The ExecutorRepository class is an abstract class and provides an abstract
 * method exec which takes in an object of type O and performs the action on it.
 *
 * The class also has a private inner class called ActionThread which extends
 * ExecutorRunnable and represents a thread that is responsible for executing a
 * single action on an object. When an ActionThread is created, it is passed an
 * instance of ExecutorManager and an object of type O. The ActionThread's
 * process method calls the exec method on the object, and if an exception is
 * thrown, it adds the exception to the list of exceptions maintained by the
 * ExecutorRepository class. Otherwise, it increments the count of processed
 * objects by 1.
 *
 * The ExecutorRepository class uses an instance of ExecutorManager to manage
 * the execution of actions on the collection of objects. It creates an
 * ExecutorManager<ActionThread> instance using the maximum waiting and running
 * values passed into the constructor and then adds an ActionThread for each
 * object in the collection. It then calls the stopAndJoin method of the
 * ExecutorManager instance to wait for all actions to complete. If an exception
 * is thrown during this process, it is added to the list of exceptions
 * maintained by the ExecutorRepository class.
 *
 * @param <O> the generic type
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class ExecutorRepository.
 *
 * @param <O>
 *            the generic type
 */
public abstract class ExecutorRepository<O> {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ExecutorRepository.class);

    /** The exceptions. */
    private final List<Exception> exceptions;

    /** The processedCount. */
    private final AtomicLong processedCount;

    /** The default configuration for the manager. */
    private static final int EXECUTOR_MAX_WAITING = 100;

    /** The Constant EXECUTOR_MAX_RUNNING. */
    private static final int EXECUTOR_MAX_RUNNING = 50;

    /**
     * Instantiates a new executor repository. Process the actions in parallel.
     *
     * @param exceptions
     *            the exceptions
     * @param processedCount
     *            the processed count
     * @param objects
     *            the objects
     */
    protected ExecutorRepository(final List<Exception> exceptions, final AtomicLong processedCount,
            final Collection<O> objects) {
        this(EXECUTOR_MAX_WAITING, EXECUTOR_MAX_RUNNING, exceptions, processedCount, objects);
    }

    /**
     * Instantiates a new executor repository. Process the actions in parallel.
     *
     * @param maxWaiting
     *            the max waiting
     * @param maxRunning
     *            the max running
     * @param exceptions
     *            the exceptions
     * @param processedCount
     *            the processed count
     * @param objects
     *            the objects
     */
    protected ExecutorRepository(final int maxWaiting, final int maxRunning, final List<Exception> exceptions,
            final AtomicLong processedCount, final Collection<O> objects) {
        this.exceptions = exceptions;
        this.processedCount = processedCount;
        if (objects != null && !objects.isEmpty()) {
            final var manager = new ExecutorManager<ActionThread>(maxWaiting, maxRunning, true);
            for (final O object : objects) {
                try {
                    manager.put(new ActionThread(manager, object));
                } catch (final InterruptedException e) {
                    _log.error("Action interrupted/lost", e);
                }
            }
            try {
                manager.stopAndJoin();
            } catch (final InterruptedException e) {
                _log.error("Action interrupted", e);
            }
        }
    }

    /**
     * Exec. Action to perform on the object.
     *
     * @param object
     *            the object
     *
     * @throws Exception
     *             the exception
     */
    public abstract void exec(final O object) throws Exception;

    /**
     * The Class ActionThread. Thread which is taking care of the execution of the action.
     */
    private class ActionThread extends ExecutorRunnable {

        /** The object. */
        final O object;

        /**
         * Instantiates a new action thread.
         *
         * @param manager
         *            the manager
         * @param object
         *            the object
         */
        ActionThread(final ExecutorManager<ActionThread> manager, final O object) {
            super(manager);
            this.object = object;
        }

        /**
         * Process.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void process() throws IOException {
            try {
                exec(object);
                processedCount.addAndGet(1);
            } catch (final Exception e) {
                exceptions.add(e);
            }
        }
    }
}