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

package ecmwf.ecpds.master.plugin.http.dao.ecuser;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.common.database.DataBaseObject;
import ecmwf.common.database.ECUser;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUser;
import ecmwf.web.dao.ModelBeanBase;

/**
 * The Class EcUserBean.
 */
public class EcUserBean extends ModelBeanBase implements EcUser, OjbImplementedBean {

    /** The user. */
    private final ecmwf.common.database.ECUser user;

    /**
     * Instantiates a new ec user bean.
     *
     * @param u
     *            the u
     */
    protected EcUserBean(final ECUser u) {
        this.user = u;
    }

    /**
     * Gets the bean interface name.
     *
     * @return the bean interface name
     */
    @Override
    public String getBeanInterfaceName() {
        return EcUser.class.getName();
    }

    /**
     * Gets the ojb implementation.
     *
     * @return the ojb implementation
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return user;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public String getId() {
        return getName();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return user.getName();
    }

    /**
     * Gets the dir.
     *
     * @return the dir
     */
    @Override
    public String getDir() {
        return user.getDir();
    }

    /**
     * Gets the gid.
     *
     * @return the gid
     */
    @Override
    public long getGid() {
        return user.getGid();
    }

    /**
     * Gets the shell.
     *
     * @return the shell
     */
    @Override
    public String getShell() {
        return user.getShell();
    }

    /**
     * Gets the uid.
     *
     * @return the uid
     */
    @Override
    public long getUid() {
        return user.getUid();
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    @Override
    public String getComment() {
        return user.getComment();
    }

    /**
     * Equals.
     *
     * @param o
     *            the o
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final EcUserBean ecUserBean && equals(ecUserBean);
    }

    /**
     * Equals.
     *
     * @param u
     *            the u
     *
     * @return true, if successful
     */
    public boolean equals(final EcUserBean u) {
        return getName().equals(u.getName());
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + user + " }";
    }
}
