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

package ecmwf.common.mbean;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.text.Format;

/**
 * The Class MBeanManager.
 */
public final class MBeanManager implements DynamicMBean {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MBeanManager.class);

    /** The _service. */
    private final MBeanService _service;

    /** The _center. */
    private final MBeanCenter _center;

    /** The url. */
    private final String _URL;

    /**
     * Instantiates a new m bean manager.
     *
     * @param URL
     *            the url
     * @param service
     *            the service
     *
     * @throws InstanceNotFoundException
     *             the instance not found exception
     * @throws NotCompliantMBeanException
     *             the not compliant m bean exception
     * @throws InstanceAlreadyExistsException
     *             the instance already exists exception
     * @throws MBeanRegistrationException
     *             the MBean registration exception
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     */
    public MBeanManager(final String URL, final MBeanService service)
            throws InstanceNotFoundException, NotCompliantMBeanException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MalformedObjectNameException {
        _service = service;
        (_center = MBeanCenter.getMBeanCenter()).registerMBean(this, _URL = URL);
    }

    /**
     * Unregister.
     */
    public void unregister() {
        try {
            _center.unregisterMBean(_URL);
        } catch (final Exception e) {
            _log.debug(e);
        }
    }

    /**
     * Register.
     */
    public void register() {
        try {
            _center.registerMBean(this, _URL);
        } catch (final Exception e) {
            _log.debug(e);
        }
    }

    /**
     * Gets the attributes.
     *
     * @param attributeNames
     *            the attribute names
     *
     * @return the attributes
     */
    @Override
    public AttributeList getAttributes(final String[] attributeNames) {
        if (attributeNames == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("attributeNames[] cannot be null"),
                    "Cannot invoke a getter of " + _service.getClass().getName());
        }
        final var resultList = new AttributeList();
        if (attributeNames.length == 0) {
            return resultList;
        }
        for (final String attributeName : attributeNames) {
            try {
                final var value = getAttribute(attributeName);
                resultList.add(new Attribute(attributeName, value));
            } catch (final Exception e) {
                _log.debug(e);
            }
        }
        return resultList;
    }

    /**
     * Sets the attributes.
     *
     * @param attributes
     *            the attributes
     *
     * @return the attribute list
     */
    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        // Check attributes is not null to avoid NullPointerException later on
        if (attributes == null) {
            throw new RuntimeOperationsException(
                    new IllegalArgumentException("AttributeList attributes cannot be null"),
                    "Cannot invoke a setter of " + _service.getClass().getName());
        }
        final var resultList = new AttributeList();
        if (attributes.isEmpty()) {
            return resultList;
        }
        for (final var i = attributes.iterator(); i.hasNext();) {
            final var attr = (Attribute) i.next();
            try {
                setAttribute(attr);
                final var name = attr.getName();
                final var value = getAttribute(name);
                resultList.add(new Attribute(name, value));
            } catch (final Exception e) {
                _log.debug(e);
            }
        }
        return resultList;
    }

    /**
     * Sets the attribute.
     *
     * @param attribute
     *            the new attribute
     *
     * @throws AttributeNotFoundException
     *             the attribute not found exception
     * @throws InvalidAttributeValueException
     *             the invalid attribute value exception
     * @throws MBeanException
     *             the MBean exception
     * @throws ReflectionException
     *             the reflection exception
     */
    @Override
    public void setAttribute(final Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        if (attribute == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute cannot be null"),
                    "Cannot invoke a setter of " + _service.getClass().getName() + " with null attribute");
        }
        final var name = attribute.getName();
        final var value = attribute.getValue();
        if (name == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null"),
                    "Cannot invoke the setter of " + _service.getClass().getName() + " with null attribute name");
        }
        if (!_service.setAttribute(name, value)) {
            throw new AttributeNotFoundException(
                    "Attribute " + name + " not found in " + _service.getClass().getName());
        }
    }

    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return _service.getMBeanInfo();
    }

    /**
     * Adds the MBean info.
     *
     * @param mBeanInfo
     *            the MBean info
     * @param description
     *            the description
     * @param attributeInfo
     *            the attribute info
     * @param operationInfo
     *            the operation info
     *
     * @return the MBean info
     */
    public static MBeanInfo addMBeanInfo(final MBeanInfo mBeanInfo, final String description,
            MBeanAttributeInfo[] attributeInfo, MBeanOperationInfo[] operationInfo) {
        if (attributeInfo == null) {
            attributeInfo = new MBeanAttributeInfo[0];
        }
        if (operationInfo == null) {
            operationInfo = new MBeanOperationInfo[0];
        }
        final var info = mBeanInfo.getAttributes();
        final var newInfo = new MBeanAttributeInfo[info.length + attributeInfo.length];
        System.arraycopy(info, 0, newInfo, 0, info.length);
        for (var i = 0; i < attributeInfo.length; i++) {
            newInfo[info.length + i] = attributeInfo[i];
        }
        final var operation = mBeanInfo.getOperations();
        final var newOperation = new MBeanOperationInfo[operation.length + operationInfo.length];
        System.arraycopy(operation, 0, newOperation, 0, operation.length);
        for (var i = 0; i < operationInfo.length; i++) {
            newOperation[operation.length + i] = operationInfo[i];
        }
        return new MBeanInfo(mBeanInfo.getClassName(), description == null ? mBeanInfo.getDescription() : description,
                newInfo, mBeanInfo.getConstructors(), newOperation, mBeanInfo.getNotifications());
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
     * @throws MBeanException
     *             the MBean exception
     * @throws ReflectionException
     *             the reflection exception
     */
    @Override
    public Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws MBeanException, ReflectionException {
        if (operationName == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Operation name cannot be null"),
                    "Cannot invoke a null operation in " + _service.getClass().getName());
        }
        final var currentThread = Thread.currentThread();
        final var serviceLoader = _service.getClass().getClassLoader();
        final var currentLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(serviceLoader);
        try {
            return "<pre>" + Format.objectToString(_service.invoke(operationName, params, signature)) + "</pre>";
        } catch (final NoSuchMethodException e) {
            throw new ReflectionException(e,
                    "Cannot find the operation " + operationName + " in " + _service.getClass().getName());
        } catch (final MBeanException e) {
            throw e;
        } finally {
            currentThread.setContextClassLoader(currentLoader);
        }
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
     * @throws ReflectionException
     *             the reflection exception
     */
    @Override
    public Object getAttribute(final String attributeName)
            throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (attributeName == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null"),
                    "Cannot invoke a getter of " + _service.getClass().getName() + " with null attribute name");
        }
        final var currentThread = Thread.currentThread();
        final var serviceLoader = _service.getClass().getClassLoader();
        final var currentLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(serviceLoader);
        try {
            return _service.getAttribute(attributeName);
        } catch (final MBeanException e) {
            throw e;
        } finally {
            currentThread.setContextClassLoader(currentLoader);
        }
    }
}
