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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;

import ecmwf.common.database.DataBaseObject;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.home.transfer.EcTransModuleHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.EcTransModule;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMethod;
import ecmwf.web.dao.ModelBeanBase;

/**
 * The Class TransferMethodBean.
 */
public class TransferMethodBean extends ModelBeanBase implements TransferMethod, OjbImplementedBean {

    /** The method. */
    private final ecmwf.common.database.TransferMethod method;

    /**
     * Instantiates a new transfer method bean.
     *
     * @param m
     *            the m
     */
    protected TransferMethodBean(final ecmwf.common.database.TransferMethod m) {
        this.method = m;
    }

    /**
     * Gets the ojb implementation.
     *
     * @return the ojb implementation
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return method;
    }

    /**
     * Gets the bean interface name.
     *
     * @return the bean interface name
     */
    @Override
    public String getBeanInterfaceName() {
        return TransferMethod.class.getName();
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
     * Gets the active.
     *
     * @return the active
     */
    @Override
    public boolean getActive() {
        return method.getActive();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return method.getName();
    }

    /**
     * Gets the resolve.
     *
     * @return the resolve
     */
    @Override
    public boolean getResolve() {
        return method.getResolve();
    }

    /**
     * Gets the restrict.
     *
     * @return the restrict
     */
    @Override
    public boolean getRestrict() {
        return method.getRestrict();
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Override
    public String getValue() {
        return method.getValue();
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    @Override
    public String getComment() {
        return method.getComment();
    }

    /**
     * Gets the ec trans module name.
     *
     * @return the ec trans module name
     */
    @Override
    public String getEcTransModuleName() {
        return method.getECtransModuleName();
    }

    /**
     * Gets the ec trans module.
     *
     * @return the ec trans module
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public EcTransModule getEcTransModule() throws TransferException {
        return EcTransModuleHome.findByPrimaryKey(getEcTransModuleName());
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    @Override
    public void setActive(final boolean param) {
        method.setActive(param);
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    @Override
    public void setComment(final String param) {
        method.setComment(param);
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    @Override
    public void setName(final String param) {
        method.setName(param);
    }

    /**
     * Sets the resolve.
     *
     * @param param
     *            the new resolve
     */
    @Override
    public void setResolve(final boolean param) {
        method.setResolve(param);
    }

    /**
     * Sets the restrict.
     *
     * @param param
     *            the new restrict
     */
    @Override
    public void setRestrict(final boolean param) {
        method.setRestrict(param);
    }

    /**
     * Sets the value.
     *
     * @param param
     *            the new value
     */
    @Override
    public void setValue(final String param) {
        method.setValue(param);
    }

    /**
     * Sets the ec trans module name.
     *
     * @param name
     *            the new ec trans module name
     */
    @Override
    public void setEcTransModuleName(final String name) {
        method.setECtransModuleName(name);
    }

    /**
     * Gets the hosts.
     *
     * @return the hosts
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<Host> getHosts() throws TransferException {
        return HostHome.findByTransferMethod(this);
    }
}
