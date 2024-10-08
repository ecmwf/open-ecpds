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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.PooledDataSource;
import com.mchange.v2.c3p0.management.DynamicPooledDataSourceManagerMBean;

import ecmwf.common.mbean.MBeanCenter;
import ecmwf.common.mbean.MBeanService;

/**
 * The Class C3P0MBeanService.
 */
public class C3P0MBeanService implements MBeanService {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(C3P0MBeanService.class);

    /**
     * Global variable for DynamicPooledDataSourceManagerMBean that contains attributes/operations for C3P0 connection
     * pool.
     */
    private final DynamicPooledDataSourceManagerMBean mbean;

    /**
     * Constructor of C3P0MBeanService for C3P0 connection pool.
     *
     * @param name
     *            the name
     *
     * @throws java.lang.NullPointerException
     *             the null pointer exception
     * @throws java.lang.NullPointerException
     *             the exception
     */
    public C3P0MBeanService(final String name) throws NullPointerException, Exception {
        @SuppressWarnings("unchecked")
        final Set<PooledDataSource> pooledDataSources = C3P0Registry.getPooledDataSources();
        final var poolBackedDataSource = pooledDataSources.iterator().next();
        mbean = new DynamicPooledDataSourceManagerMBean(poolBackedDataSource, name,
                MBeanCenter.getMBeanCenter().getMBeanServer());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the MBean info.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return mbean.getMBeanInfo();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the attribute.
     */
    @Override
    public boolean setAttribute(final String name, final Object value)
            throws InvalidAttributeValueException, MBeanException {
        try {
            mbean.setAttribute(new Attribute(name, value));
        } catch (final Throwable e) {
            _log.error(e.getMessage() + "  " + e.getCause());

        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Invoke.
     */
    @Override
    public Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws MBeanException {
        try {
            mbean.invoke(operationName, params, signature);
        } catch (final ReflectionException e) {
            _log.error(e.getMessage() + "  " + e.getCause());
        }

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute.
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        Object object = null;
        try {
            object = mbean.getAttribute(attributeName);
        } catch (final ReflectionException e) {
            _log.error(e.getMessage() + "  " + e.getCause());
        }
        return object;
    }
}
