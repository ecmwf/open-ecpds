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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;

import ecmwf.web.model.ModelBean;

/**
 * The Interface EcTransModule.
 */
public interface EcTransModule extends ModelBean {

    /**
     * Gets the active.
     *
     * @return the active
     */
    boolean getActive();

    /**
     * Gets the archive.
     *
     * @return the archive
     */
    String getArchive();

    /**
     * Gets the classe.
     *
     * @return the classe
     */
    String getClasse();

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Sets the active.
     *
     * @param b
     *            the new active
     */
    void setActive(boolean b);

    /**
     * Sets the archive.
     *
     * @param s
     *            the new archive
     */
    void setArchive(String s);

    /**
     * Sets the classe.
     *
     * @param c
     *            the new classe
     */
    void setClasse(String c);

    /**
     * Sets the name.
     *
     * @param n
     *            the new name
     */
    void setName(String n);

    /**
     * Gets the transfer methods.
     *
     * @return the transfer methods
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection getTransferMethods() throws TransferException;
}
