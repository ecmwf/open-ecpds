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
 * Every entry in the Transfer History.
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Date;

import ecmwf.ecpds.master.plugin.http.model.CollectionSizeBean;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;

/**
 * The Interface TransferHistory.
 */
public interface TransferHistory extends CollectionSizeBean, ModelBean {

    /**
     * Sets the user.
     *
     * @param user
     *            the new user
     */
    void setUser(User user);

    /**
     * Gets the user.
     *
     * @return the user
     */
    User getUser();

    /**
     * Gets the date.
     *
     * @return the date
     */
    Date getDate();

    /**
     * Gets the host.
     *
     * @return the host
     *
     * @throws TransferException
     *             the transfer exception
     */
    Host getHost() throws TransferException;

    /**
     * Gets the host nick name.
     *
     * @return the host nick name
     */
    String getHostNickName();

    /**
     * Gets the host name.
     *
     * @return the host name
     */
    String getHostName();

    /**
     * Gets the status.
     *
     * @return the status
     */
    String getStatus();

    /**
     * Gets the formatted status.
     *
     * @return the formatted status
     */
    String getFormattedStatus();

    /**
     * Gets the sent.
     *
     * @return the sent
     */
    long getSent();

    /**
     * Gets the formatted sent.
     *
     * @return the formatted sent
     */
    String getFormattedSent();

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    String getComment();

    /**
     * Gets the formatted comment.
     *
     * @return the formatted comment
     */
    String getFormattedComment();

    /**
     * Gets the error.
     *
     * @return the error
     */
    boolean getError();

    /**
     * Gets the data transfer id.
     *
     * @return the data transfer id
     */
    long getDataTransferId();

    /**
     * Gets the data transfer.
     *
     * @return the data transfer
     */
    DataTransfer getDataTransfer();
}
