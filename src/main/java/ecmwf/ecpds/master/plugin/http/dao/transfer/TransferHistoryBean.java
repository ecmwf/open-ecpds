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

import java.util.Date;

import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferHistory;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.web.dao.ModelBeanBase;
import ecmwf.web.model.users.User;

/**
 * The Class TransferHistoryBean.
 */
public class TransferHistoryBean extends ModelBeanBase implements TransferHistory {

    /** The item. */
    private final ecmwf.common.database.TransferHistory item;

    /** The user. */
    private User user;

    /**
     * {@inheritDoc}
     *
     * Sets the user.
     */
    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the user.
     */
    @Override
    public User getUser() {
        return user;
    }

    /**
     * Instantiates a new transfer history bean.
     *
     * @param user
     *            the user
     * @param i
     *            the i
     */
    protected TransferHistoryBean(final User user, final ecmwf.common.database.TransferHistory i) {
        this.user = user;
        this.item = i;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the collection size.
     */
    @Override
    public int getCollectionSize() {
        return item.getCollectionSize();
    }

    /**
     * Instantiates a new transfer history bean.
     *
     * @param i
     *            the i
     */
    protected TransferHistoryBean(final ecmwf.common.database.TransferHistory i) {
        this.item = i;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return TransferHistory.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    public String getId() {
        return Long.toString(item.getId());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the comment.
     */
    @Override
    public String getComment() {
        return item.getComment();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted comment.
     */
    @Override
    public String getFormattedComment() {
        return Util.getFormatted(user, item.getComment());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfer.
     */
    @Override
    public DataTransfer getDataTransfer() {
        return new DataTransferBaseBean(item.getDataTransfer());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfer id.
     */
    @Override
    public long getDataTransferId() {
        return item.getDataTransferId();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the date.
     */
    @Override
    public Date getDate() {
        return item.getTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the sent.
     */
    @Override
    public long getSent() {
        return item.getSent();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted sent.
     */
    @Override
    public String getFormattedSent() {
        return Format.formatSize(this.getSent());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the status.
     */
    @Override
    public String getStatus() {
        return item.getStatusCode();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted status.
     */
    @Override
    public String getFormattedStatus() {
        return StatusFactory.getDataTransferStatusName(false, item.getStatusCode());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the error.
     */
    @Override
    public boolean getError() {
        return item.getError();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host.
     */
    @Override
    public Host getHost() throws TransferException {
        final var host = item.getHost();
        if (host == null) {
            throw new TransferException("No host assigned yet for transfer " + getId());
        }
        return new HostBean(user, host);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host nick name.
     */
    @Override
    public String getHostNickName() {
        try {
            return getHost().getNickName();
        } catch (final TransferException e) {
            return "";
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host name.
     */
    @Override
    public String getHostName() {
        return item.getHostName();
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final TransferHistoryBean transferHistoryBean && equals(transferHistoryBean);
    }

    /**
     * Equals.
     *
     * @param u
     *            the u
     *
     * @return true, if successful
     */
    public boolean equals(final TransferHistoryBean u) {
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
        return getClass().getName() + " { " + item + " }";
    }
}
