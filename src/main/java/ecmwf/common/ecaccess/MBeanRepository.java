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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import ecmwf.common.text.Format;

/**
 * The Class MBeanRepository.
 *
 * @param <O>
 *            the generic type
 */
public abstract class MBeanRepository<O> extends MBeanScheduler {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MBeanRepository.class);

    /** The _objects. */
    private final Map<String, O> _objects = new ConcurrentHashMap<>();

    /** The _max authorised size. */
    private long _maxAuthorisedSize = 0;

    /** The _max recorded size. */
    private long _maxRecordedSize = 0;

    /** The _comparator. */
    private Comparator<O> _comparator = null;

    /** The _wakeup. */
    private boolean _wakeup = true;

    /**
     * Instantiates a new m bean repository.
     *
     * @param group
     *            the group
     * @param name
     *            the name
     */
    public MBeanRepository(final String group, final String name) {
        super(group, name);
        setDelay(30 * Timer.ONE_SECOND);
    }

    /**
     * Instantiates a new m bean repository.
     *
     * @param name
     *            the name
     */
    public MBeanRepository(final String name) {
        this("ECaccess:service", name);
    }

    /**
     * Clear.
     */
    public void clear() {
        _objects.clear();
    }

    /**
     * Gets the key.
     *
     * @param object
     *            the object
     *
     * @return the key
     */
    public String getKey(final O object) {
        return String.valueOf(object.hashCode());
    }

    /**
     * Gets the status.
     *
     * @param object
     *            the object
     *
     * @return the status
     */
    public String getStatus(final O object) {
        return object.toString();
    }

    /**
     * Sets the comparator.
     *
     * @param comparator
     *            the new comparator
     */
    public void setComparator(final Comparator<O> comparator) {
        _comparator = comparator;
    }

    /**
     * Sets the max authorised size.
     *
     * @param maxAuthorisedSize
     *            the max authorised size
     */
    public void setMaxAuthorisedSize(final int maxAuthorisedSize) {
        _maxAuthorisedSize = maxAuthorisedSize;
    }

    /**
     * Gets the comparator.
     *
     * @return the comparator
     */
    public Comparator<O> getComparator() {
        return _comparator;
    }

    /**
     * Sets the wakeup.
     *
     * @param wakeup
     *            the new wakeup
     */
    public void setWakeup(final boolean wakeup) {
        _wakeup = wakeup;
    }

    /**
     * Gets the wakeup.
     *
     * @return the wakeup
     */
    public boolean getWakeup() {
        return _wakeup;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public String getStatus() {
        final var status = new StringBuilder();
        for (final O object : getList()) {
            status.append(getKey(object)).append("=").append(getStatus(object).trim().replace(' ', '_')).append("\r\n");
        }
        return status.toString().trim();
    }

    /**
     * Gets the list.
     *
     * @return the list
     */
    public List<O> getList() {
        return getList(_comparator);
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize() {
        return _objects.size();
    }

    /**
     * Gets the list.
     *
     * @param comparator
     *            the comparator
     *
     * @return the list
     */
    public List<O> getList(final Comparator<O> comparator) {
        final List<O> list = new ArrayList<>(_objects.values());
        if (comparator != null) {
            Collections.sort(list, comparator);
        }
        return list;
    }

    /**
     * Sorts the list.
     *
     * @param list
     *            the list
     *
     * @return the list
     */
    public List<O> sort(final List<O> list) {
        if (_objects.size() > 0) {
            Collections.sort(list, _comparator);
        }
        return list;
    }

    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                "The MBeanRepository is used to manage a generic cache " + "of objects.",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("MaxRecordedSize", "long",
                                "MaxRecordedSize: maximum number of objects(s) seen in the queue.", true, false, false),
                        new MBeanAttributeInfo("MaxAuthorisedSize", "long",
                                "MaxAuthorisedSize: maximum number of objects(s) allowed in the queue.", true, false,
                                false),
                        new MBeanAttributeInfo("JammedTimeOut", "long",
                                "JammedTimeOut: timeout before to move to the jammed status.", true, false, false),
                        new MBeanAttributeInfo("QueueStatus", "java.lang.String",
                                "Queue: status of the object(s) in the queue.", true, false, false),
                        new MBeanAttributeInfo("QueueSize", "int", "QueueSize: number of objects in the queue.", true,
                                false, false) },
                new MBeanOperationInfo[] { new MBeanOperationInfo("remove",
                        "remove(key): remove the object from the queue.",
                        new MBeanParameterInfo[] {
                                new MBeanParameterInfo("key", "java.lang.String", "the key of the object to remove") },
                        "java.lang.Boolean", MBeanOperationInfo.ACTION) });
    }

    /**
     * Invoke.
     *
     * @param operationName
     *            the operation name
     * @param params
     *            the params
     * @param signature
     *            the signature
     *
     * @return the object
     *
     * @throws NoSuchMethodException
     *             the no such method exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws NoSuchMethodException, MBeanException {
        try {
            if ("remove".equals(operationName) && signature.length == 1) {
                final var object = removeKey(String.valueOf(params[0]));
                boolean result;
                if (result = object != null) {
                    _log.debug("Remove from the queue: {}", String.valueOf(object));
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
     * Gets the attribute.
     *
     * @param attributeName
     *            the attribute name
     *
     * @return the attribute
     *
     * @throws AttributeNotFoundException
     *             the attribute not found exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        try {
            if ("QueueSize".equals(attributeName)) {
                return _objects.size();
            }
            if ("JammedTimeOut".equals(attributeName)) {
                return getJammedTimeout();
            }
            if ("MaxRecordedSize".equals(attributeName)) {
                return _maxRecordedSize;
            }
            if ("MaxAuthorisedSize".equals(attributeName)) {
                return _maxAuthorisedSize;
            }
            if ("QueueStatus".equals(attributeName)) {
                return getStatus();
            }
        } catch (final Exception e) {
            _log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        return super.getAttribute(attributeName);
    }

    /**
     * Puts the object.
     *
     * @param object
     *            the object
     */
    public void put(final O object) {
        if (object != null) {
            var maxWait = 0L;
            while (_maxAuthorisedSize > 0 && _objects.size() >= _maxAuthorisedSize) {
                maxWait += 1000;
                waitFor(1000L);
            }
            _objects.put(getKey(object), object);
            final long size = _objects.size();
            if (size > _maxRecordedSize) {
                _maxRecordedSize = size;
            }
            if (_wakeup) {
                wakeup();
            }
            if (maxWait > 0) {
                _log.warn("Submission delayed by " + Format.formatDuration(maxWait));
            }
        }
    }

    /**
     * Removes the value.
     *
     * @param object
     *            the object
     *
     * @return the o
     */
    public O removeValue(final O object) {
        return removeKey(getKey(object));
    }

    /**
     * Removes the key.
     *
     * @param key
     *            the key
     *
     * @return the o
     */
    public O removeKey(final String key) {
        return _objects.remove(key);
    }

    /**
     * Contains value.
     *
     * @param object
     *            the object
     *
     * @return true, if successful
     */
    public boolean containsValue(final O object) {
        return containsKey(getKey(object));
    }

    /**
     * Contains key.
     *
     * @param key
     *            the key
     *
     * @return true, if successful
     */
    public boolean containsKey(final String key) {
        return _objects.containsKey(key);
    }

    /**
     * Gets the value.
     *
     * @param key
     *            the key
     *
     * @return the value
     */
    public O getValue(final String key) {
        return _objects.get(key);
    }
}
