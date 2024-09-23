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
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
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
     * {@inheritDoc}
     *
     * Gets the ojb implementation.
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return method;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return TransferMethod.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    public String getId() {
        return getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the active.
     */
    @Override
    public boolean getActive() {
        return method.getActive();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    public String getName() {
        return method.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the resolve.
     */
    @Override
    public boolean getResolve() {
        return method.getResolve();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the restrict.
     */
    @Override
    public boolean getRestrict() {
        return method.getRestrict();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the value.
     */
    @Override
    public String getValue() {
        return method.getValue();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the comment.
     */
    @Override
    public String getComment() {
        return method.getComment();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ec trans module name.
     */
    @Override
    public String getEcTransModuleName() {
        return method.getECtransModuleName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ec trans module.
     */
    @Override
    public EcTransModule getEcTransModule() throws TransferException {
        return EcTransModuleHome.findByPrimaryKey(getEcTransModuleName());
    }

    /**
     * {@inheritDoc}
     *
     * Sets the active.
     */
    @Override
    public void setActive(final boolean param) {
        method.setActive(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the comment.
     */
    @Override
    public void setComment(final String param) {
        method.setComment(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the name.
     */
    @Override
    public void setName(final String param) {
        method.setName(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the resolve.
     */
    @Override
    public void setResolve(final boolean param) {
        method.setResolve(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the restrict.
     */
    @Override
    public void setRestrict(final boolean param) {
        method.setRestrict(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the value.
     */
    @Override
    public void setValue(final String param) {
        method.setValue(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the ec trans module name.
     */
    @Override
    public void setEcTransModuleName(final String name) {
        method.setECtransModuleName(name);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the hosts.
     */
    @Override
    public Collection<Host> getHosts() throws TransferException {
        return HostHome.findByTransferMethod(this);
    }
}
