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
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;

import ecmwf.common.database.DataBaseObject;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.home.transfer.TransferMethodHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.EcTransModule;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMethod;
import ecmwf.web.dao.ModelBeanBase;

/**
 * The Class EcTransModuleBean.
 */
public class EcTransModuleBean extends ModelBeanBase implements EcTransModule, OjbImplementedBean {

    /** The module. */
    private final ecmwf.common.database.ECtransModule module;

    /**
     * Instantiates a new ec trans module bean.
     *
     * @param module
     *            the module
     */
    protected EcTransModuleBean(final ecmwf.common.database.ECtransModule module) {
        this.module = module;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return EcTransModule.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ojb implementation.
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return module;
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
        return module.getActive();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the archive.
     */
    @Override
    public String getArchive() {
        return module.getArchive();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the classe.
     */
    @Override
    public String getClasse() {
        return module.getClasse();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    public String getName() {
        return module.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the active.
     */
    @Override
    public void setActive(final boolean param) {
        module.setActive(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the archive.
     */
    @Override
    public void setArchive(final String param) {
        module.setArchive(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the classe.
     */
    @Override
    public void setClasse(final String param) {
        module.setClasse(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the name.
     */
    @Override
    public void setName(final String param) {
        module.setName(param);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer methods.
     */
    @Override
    public Collection<TransferMethod> getTransferMethods() throws TransferException {
        return TransferMethodHome.findByEcTransModule(this);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final EcTransModuleBean ecTransModuleBean && equals(ecTransModuleBean);
    }

    /**
     * Equals.
     *
     * @param d
     *            the d
     *
     * @return true, if successful
     */
    public boolean equals(final EcTransModuleBean d) {
        return getName().equals(d.getName());
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + module + " }";
    }
}
