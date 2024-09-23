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

package ecmwf.common.ecaccess;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;

/**
 * The Class StorageRepository.
 *
 * @param <O>
 *            the generic type
 */
public abstract class StorageRepository<O> extends MBeanRepository<O> {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(StorageRepository.class);

    /** The _vector. */
    private final List<O> storageContent = Collections.synchronizedList(new ArrayList<>());

    /** The _threads. */
    private final List<StorageThread<O>> storageThreadsList;

    /** The _size. */
    private final int size;

    /**
     * Instantiates a new storage repository.
     *
     * @param name
     *            the name
     * @param storageThreadsCount
     *            the storage threads count
     * @param storageThreadsDelay
     *            the storage threads delay
     */
    public StorageRepository(final String name, final int storageThreadsCount, final long storageThreadsDelay) {
        super(name);
        size = storageThreadsCount;
        storageThreadsList = new ArrayList<>(storageThreadsCount);
        setDelay(storageThreadsDelay);
    }

    /**
     * Expired.
     *
     * @param object
     *            the object
     *
     * @return true, if successful
     */
    public boolean expired(final O object) {
        return true;
    }

    /**
     * Update.
     *
     * @param object
     *            the object
     *
     * @throws java.lang.Exception
     *             the exception
     */
    public abstract void update(O object) throws Exception;

    /**
     * {@inheritDoc}
     *
     * Initialize.
     */
    @Override
    public void initialize() {
        final var className = Format.getClassName(this);
        for (var i = 0; i < size; i++) {
            final var thread = new StorageThread<>(this);
            storageThreadsList.add(i, thread);
            thread.setThreadNameAndCookie(className, null, null, null);
            thread.execute();
        }
        _log.info("{} StorageThread(s) started for {}", storageThreadsList.size(), className);
    }

    /**
     * Gets the threads list size.
     *
     * @return the size
     */
    public int getThreadsSize() {
        return storageThreadsList.size();
    }

    /**
     * {@inheritDoc}
     *
     * Shutdown.
     */
    @Override
    public void shutdown() {
        super.shutdown();
        for (final StorageThread<O> thread : storageThreadsList) {
            if (thread != null) {
                thread.shutdown();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Next step.
     */
    @Override
    public int nextStep() {
        if (getSize() == 0) {
            if (!getWakeup()) {
                return NEXT_STEP_DELAY;
            }
            waitFor(Cnf.at("StorageRepository", "storageRepositoryDelay", Timer.ONE_HOUR));
        }
        var elementsAdded = false;
        for (final O object : getList()) {
            if (object == null) {
                continue;
            }
            if (expired(object)) {
                removeValue(object);
                if (storageThreadsList.isEmpty()) {
                    try {
                        update(object);
                    } catch (final Throwable t) {
                        _log.warn("update", t);
                        put(object);
                    }
                } else {
                    elementsAdded = true;
                    addElement(object);
                }
            }
        }
        if (elementsAdded) {
            for (final StorageThread<O> element : storageThreadsList) {
                element.wakeup();
            }
        }
        return NEXT_STEP_DELAY;
    }

    /**
     * Adds the element.
     *
     * @param object
     *            the object
     */
    private void addElement(final O object) {
        synchronized (storageContent) {
            if (!storageContent.contains(object)) {
                storageContent.add(object);
            }
        }
    }

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    boolean isEmpty() {
        return storageContent.isEmpty();
    }

    /**
     * Gets the next element.
     *
     * @return the next element
     */
    O getNextElement() {
        synchronized (storageContent) {
            return storageContent.isEmpty() ? null : storageContent.remove(0);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the MBean info.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        final var infoLength = storageThreadsList != null ? storageThreadsList.size() : 0;
        final var infos = new MBeanAttributeInfo[infoLength + 1];
        for (var i = 0; i < infoLength; i++) {
            infos[i] = new MBeanAttributeInfo("StorageThreadStatus_" + i, "java.lang.String",
                    "StorageThreadStatus_" + i + ": status of the storage thread.", true, false, false);
        }
        infos[infoLength] = new MBeanAttributeInfo("SharedSpoolSize", "int",
                "SharedSpoolSize: size of the shared spool between workers.", true, false, false);
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(), """
                The StorageRepository is used to manage a cache \
                of storage objects. A number of threads are \
                started to deal with the object storage when \
                it is expired.""", infos,
                new MBeanOperationInfo[] { new MBeanOperationInfo("update",
                        "update(key): remove/update the object from the queue.",
                        new MBeanParameterInfo[] {
                                new MBeanParameterInfo("key", "java.lang.String", "the key of the object to update") },
                        "java.lang.Boolean", MBeanOperationInfo.ACTION) });
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute.
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        try {
            if (attributeName.startsWith("StorageThreadStatus_")) {
                return storageThreadsList.get(Integer.parseInt(attributeName.substring(20))).getStatus();
            }
            if ("SharedSpoolSize".equals(attributeName)) {
                return storageContent.size();
            }
        } catch (final Exception e) {
            _log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        return super.getAttribute(attributeName);
    }

    /**
     * {@inheritDoc}
     *
     * Invoke.
     */
    @Override
    public Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws NoSuchMethodException, MBeanException {
        try {
            if ("update".equals(operationName) && signature.length == 1) {
                final var object = removeKey(String.valueOf(params[0]));
                final var result = object != null;
                if (result) {
                    _log.debug("Remove/update from the queue: {}", object);
                    update(object);
                }
                return result;
            }
        } catch (final Exception e) {
            _log.warn("Invoking the {} MBean method", operationName, e);
            throw new MBeanException(e);
        }
        return super.invoke(operationName, params, signature);
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        final var time = System.currentTimeMillis() - getStartDate().getTime();
        var updated = 0L;
        var duration = 0L;
        for (final StorageThread<O> thread : storageThreadsList) {
            updated += thread.getUpdated();
            duration += thread.getDuration();
        }
        return "threads=" + storageThreadsList.size() + ",duration=" + Format.formatDuration(time)
                + (updated > 0 ? ",speed=" + Format.formatDuration(duration / updated) + ",trend="
                        + Format.formatDuration(time / updated) : "");
    }
}
