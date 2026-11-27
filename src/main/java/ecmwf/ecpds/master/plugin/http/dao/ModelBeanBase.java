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

package ecmwf.ecpds.master.plugin.http.dao;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOService;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOException;

/**
 * ModelBeanBase.java
 *
 * <p>
 * A base class, implementing the interface ModelBean, that includes some features that are thought to be of interest
 * for all the ModelBean implementations. Many ModelBean's will extend this class instead of implementing the ModelBean
 * interface.
 * </p>
 * <p>
 * This common features are:
 * </p>
 * <ul>
 * <li>Management of a bean "id"</li>
 * <li>Implementation of save,insert,delete methods which use DAOService.</li>
 * </ul>
 *
 * <p>
 * Created: Wed Apr 24 10:03:24 2002
 * </p>
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 0.1.0
 */

public abstract class ModelBeanBase implements ModelBean {

    private static Log log = LogFactory.getLog(ModelBeanBase.class);
    private String id = null;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isNew() {
        return (this.id == null);
    }

    public abstract String getBeanInterfaceName();

    public int getIntId() {

        try {
            return Integer.parseInt(this.id);
        } catch (Exception e) {
            log.error("Trying to get a 'int' value from id=" + this.id);
            return -1; // FIXME: Hmmmm, not really happy about this...
        }
    }

    /**
     * Saves the state of this bean through the DAOService. This will probable map into an "update" SQL operation,
     * though that will depend on the DAOHandler defined for this class.
     *
     * @deprecated Use the version with context
     *
     * @throws ModelBeanException
     *             If the DAOService finds any problem.
     */

    @Deprecated
    public void save() throws ModelException {

        try {
            DAOService.save(this, null);
        } catch (DAOException e) {
            throw new ModelException("Error saving object " + this, e);
        }

    }

    public void save(Object context) throws ModelException {

        try {
            DAOService.save(this, context);
        } catch (DAOException e) {
            throw new ModelException("Error saving object " + this, e);
        }

    }

    /**
     * Creates a new instance of this bean in the persistent storage through the DAOService. This will probable map into
     * an "insert" SQL operation.
     *
     * @deprecated Use the version with context
     *
     * @throws ModelBeanException
     *             If the DAOService finds any problem.
     */

    @Deprecated
    public void insert() throws ModelException {

        try {
            DAOService.insert(this, null);
        } catch (DAOException e) {
            throw new ModelException("Error inserting object " + this, e);
        }

    }

    public void insert(Object context) throws ModelException {

        try {
            DAOService.insert(this, context);
        } catch (DAOException e) {
            throw new ModelException("Error inserting object " + this, e);
        }

    }

    /**
     * Deletes the instance of the bean from the persistent storage. This will probable map into an "delete" SQL
     * operation and can also have effect into "children" beans, depending on database constraints.
     *
     * @deprecated Use The version with context
     *
     * @throws ModelBeanException
     *             If the DAOService finds any problem.
     */

    @Deprecated
    public void delete() throws ModelException {

        try {
            DAOService.delete(this, null);
        } catch (DAOException e) {
            throw new ModelException("Error deleting object " + this, e);
        }

    }

    public void delete(Object context) throws ModelException {

        try {
            DAOService.delete(this, context);
        } catch (DAOException e) {
            throw new ModelException("Error deleting object " + this, e);
        }

    }

    /**
     * Gets the value of a named field
     *
     * @param name
     *            The name of the field
     *
     * @return The value
     */

    public Object getAttributeValue(String name) throws ModelException {

        // A little reflection.. Let's hope that this doesn't "reflect" on performance.
        try {
            Method getter = this.getClass()
                    .getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1, name.length()));
            return getter.invoke(this);
        } catch (NoSuchMethodException e) {
            throw new ModelException(
                    "There is no attribute '" + name + "' in class '" + this.getClass().getName() + "'.", e);
        } catch (SecurityException e) {
            throw new ModelException(
                    "Security exception accessing '" + name + "' in class '" + this.getClass().getName() + "'.", e);
        } catch (IllegalArgumentException e) {
            throw new ModelException(
                    "Illegal argument accessing '" + name + "' in class '" + this.getClass().getName() + "'.", e);
        } catch (InvocationTargetException e) {
            throw new ModelException(
                    "Bean exception accessing '" + name + "' in class '" + this.getClass().getName() + "'.", e);
        } catch (IllegalAccessException e) {
            throw new ModelException(
                    "Access exception accessing '" + name + "' in class '" + this.getClass().getName() + "'.", e);
        }

    }

} // ModelBeanBase
