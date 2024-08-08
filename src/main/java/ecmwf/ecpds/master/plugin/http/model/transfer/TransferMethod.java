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

package ecmwf.ecpds.master.plugin.http.model.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;

import ecmwf.web.model.ModelBean;

/**
 * The Interface TransferMethod.
 */
public interface TransferMethod extends ModelBean {

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the value.
     *
     * @return the value
     */
    String getValue();

    /**
     * Gets the restrict.
     *
     * @return the restrict
     */
    boolean getRestrict();

    /**
     * Gets the resolve.
     *
     * @return the resolve
     */
    boolean getResolve();

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    String getComment();

    /**
     * Gets the active.
     *
     * @return the active
     */
    boolean getActive();

    /**
     * Gets the ec trans module.
     *
     * @return the ec trans module
     *
     * @throws TransferException
     *             the transfer exception
     */
    EcTransModule getEcTransModule() throws TransferException;

    /**
     * Gets the ec trans module name.
     *
     * @return the ec trans module name
     */
    String getEcTransModuleName();

    /**
     * Gets the hosts.
     *
     * @return the hosts
     *
     * @throws TransferException
     *             the transfer exception
     */
    Collection<Host> getHosts() throws TransferException;

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    void setName(String name);

    /**
     * Sets the value.
     *
     * @param value
     *            the new value
     */
    void setValue(String value);

    /**
     * Sets the restrict.
     *
     * @param v
     *            the new restrict
     */
    void setRestrict(boolean v);

    /**
     * Sets the resolve.
     *
     * @param v
     *            the new resolve
     */
    void setResolve(boolean v);

    /**
     * Sets the comment.
     *
     * @param c
     *            the new comment
     */
    void setComment(String c);

    /**
     * Sets the active.
     *
     * @param a
     *            the new active
     */
    void setActive(boolean a);

    /**
     * Sets the ec trans module name.
     *
     * @param name
     *            the new ec trans module name
     */
    void setEcTransModuleName(String name);
}
