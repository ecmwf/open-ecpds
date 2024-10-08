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

package ecmwf.ecpds.master.plugin.http.dao.transfer;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.common.database.DataBaseObject;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.model.transfer.Alias;
import ecmwf.web.dao.ModelBeanBase;

/**
 * The Class AliasBean.
 */
public class AliasBean extends ModelBeanBase implements Alias, OjbImplementedBean {

    /** The alias. */
    private final ecmwf.common.database.Alias alias;

    /**
     * Instantiates a new alias bean.
     *
     * @param a
     *            the a
     */
    protected AliasBean(final ecmwf.common.database.Alias a) {
        this.alias = a;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ojb implementation.
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return this.alias;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return Alias.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    public String getId() {
        return getDesName() + getDestinationName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the des name.
     */
    @Override
    public String getDesName() {
        return this.alias.getDesName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination name.
     */
    @Override
    public String getDestinationName() {
        return this.alias.getDestinationName();
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final AliasBean aliasBean && equals(aliasBean);
    }

    /**
     * Equals.
     *
     * @param u
     *            the u
     *
     * @return true, if successful
     */
    public boolean equals(final AliasBean u) {
        return getId().equals(u.getId());
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + alias + " }";
    }
}
