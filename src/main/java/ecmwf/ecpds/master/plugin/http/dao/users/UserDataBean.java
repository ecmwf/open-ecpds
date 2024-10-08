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

package ecmwf.ecpds.master.plugin.http.dao.users;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Simplified implementation that just takes a String as data.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ecmwf.web.model.ModelException;
import ecmwf.web.model.users.UserData;

/**
 * The Class UserDataBean.
 */
public class UserDataBean implements UserData {

    /** The data. */
    private String data;

    /**
     * Instantiates a new user data bean.
     *
     * @param s
     *            the s
     */
    public UserDataBean(final String s) {
        this.data = s;
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return this.data;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute.
     */
    @Override
    public String getAttribute(final String name) throws ModelException {
        return this.data;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute.
     */
    @Override
    public String getAttribute(final String name, final String defaultValue) {
        return this.data;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the safe attribute.
     */
    @Override
    public String getSafeAttribute(final String name) {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute.
     */
    @Override
    public String getAttribute(final String name, final String defaultValue, final String prefix, final String suffix) {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the attribute.
     */
    @Override
    public void setAttribute(final String name, final String value) {
        this.data = value;
    }

    /**
     * {@inheritDoc}
     *
     * Dump.
     */
    @Override
    public String dump(final int mode) {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the id.
     */
    @Override
    public void setId(final String id) {
    }

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    public String getId() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the int id.
     */
    @Override
    public int getIntId() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is new.
     */
    @Override
    public boolean isNew() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute value.
     */
    @Override
    public Object getAttributeValue(final String name) throws ModelException {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attrs.
     */
    @Override
    public Map<String, String> getAttrs() {
        final Map<String, String> m = new HashMap<>(1);
        m.put("data", this.data);
        return m;
    }

    /**
     * {@inheritDoc}
     *
     * Save.
     */
    @Override
    public void save() throws ModelException {
    }

    /**
     * {@inheritDoc}
     *
     * Insert.
     */
    @Override
    public void insert() throws ModelException {
    }

    /**
     * {@inheritDoc}
     *
     * Delete.
     */
    @Override
    public void delete() throws ModelException {
    }

    /**
     * {@inheritDoc}
     *
     * Save.
     */
    @Override
    public void save(final Object context) throws ModelException {
    }

    /**
     * {@inheritDoc}
     *
     * Insert.
     */
    @Override
    public void insert(final Object context) throws ModelException {
    }

    /**
     * {@inheritDoc}
     *
     * Delete.
     */
    @Override
    public void delete(final Object context) throws ModelException {
    }

    /**
     * {@inheritDoc}
     *
     * Checks for attribute.
     */
    @Override
    public boolean hasAttribute(final String name) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute names.
     */
    @Override
    public Iterator<?> getAttributeNames() {
        return null;
    }
}
