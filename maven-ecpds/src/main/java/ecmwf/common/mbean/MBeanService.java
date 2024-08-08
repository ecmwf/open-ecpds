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
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;

/**
 * The Interface MBeanService.
 */
public interface MBeanService {
    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    MBeanInfo getMBeanInfo();

    /**
     * Sets the attribute.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @return true, if successful
     *
     * @throws InvalidAttributeValueException
     *             the invalid attribute value exception
     * @throws MBeanException
     *             the MBean exception
     */
    boolean setAttribute(String name, Object value) throws InvalidAttributeValueException, MBeanException;

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
    Object getAttribute(String attributeName) throws AttributeNotFoundException, MBeanException;

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
    Object invoke(String operationName, Object[] params, String[] signature)
            throws NoSuchMethodException, MBeanException;
}
