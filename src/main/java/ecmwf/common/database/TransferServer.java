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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Objects;

/**
 * The Class TransferServer.
 */
public class TransferServer extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 867943695671080402L;

    /** The trg name. */
    protected String TRG_NAME;

    /** The trs replicate. */
    protected boolean TRS_REPLICATE;

    /** The trs active. */
    protected boolean TRS_ACTIVE;

    /** The trs check. */
    protected boolean TRS_CHECK;

    /** The trs host. */
    protected String TRS_HOST;

    /** The trs last update. */
    protected long TRS_LAST_UPDATE;

    /** The trs max inactivity. */
    protected int TRS_MAX_INACTIVITY;

    /** The trs max transfers. */
    protected Integer TRS_MAX_TRANSFERS;

    /** The trs name. */
    protected String TRS_NAME;

    /** The trs port. */
    protected int TRS_PORT;

    /** The hos name for replication. */
    protected Integer HOS_NAME_FOR_REPLICATION;

    /** The transfer group. */
    protected TransferGroup transferGroup;

    /** The host for replication. */
    protected Host hostForReplication;

    /**
     * Instantiates a new transfer server.
     */
    public TransferServer() {
    }

    /**
     * Instantiates a new transfer server.
     *
     * @param name
     *            the name
     */
    public TransferServer(final String name) {
        setName(name);
    }

    /**
     * Gets the transfer group name.
     *
     * @return the transfer group name
     */
    public String getTransferGroupName() {
        return TRG_NAME;
    }

    /**
     * Sets the transfer group name.
     *
     * @param param
     *            the new transfer group name
     */
    public void setTransferGroupName(final String param) {
        TRG_NAME = param;
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public boolean getActive() {
        return TRS_ACTIVE;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final boolean param) {
        TRS_ACTIVE = param;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final String param) {
        TRS_ACTIVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the replicate.
     *
     * @return the replicate
     */
    public boolean getReplicate() {
        return TRS_REPLICATE;
    }

    /**
     * Sets the replicate.
     *
     * @param param
     *            the new replicate
     */
    public void setReplicate(final boolean param) {
        TRS_REPLICATE = param;
    }

    /**
     * Sets the replicate.
     *
     * @param param
     *            the new replicate
     */
    public void setReplicate(final String param) {
        TRS_REPLICATE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the check.
     *
     * @return the check
     */
    public boolean getCheck() {
        return TRS_CHECK;
    }

    /**
     * Sets the check.
     *
     * @param param
     *            the new check
     */
    public void setCheck(final boolean param) {
        TRS_CHECK = param;
    }

    /**
     * Sets the check.
     *
     * @param param
     *            the new check
     */
    public void setCheck(final String param) {
        TRS_CHECK = Boolean.parseBoolean(param);
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return TRS_HOST;
    }

    /**
     * Sets the host.
     *
     * @param param
     *            the new host
     */
    public void setHost(final String param) {
        TRS_HOST = param;
    }

    /**
     * Gets the last update.
     *
     * @return the last update
     */
    public long getLastUpdate() {
        return TRS_LAST_UPDATE;
    }

    /**
     * Sets the last update.
     *
     * @param param
     *            the new last update
     */
    public void setLastUpdate(final long param) {
        TRS_LAST_UPDATE = param;
    }

    /**
     * Sets the last update.
     *
     * @param param
     *            the new last update
     */
    public void setLastUpdate(final String param) {
        TRS_LAST_UPDATE = Long.parseLong(param);
    }

    /**
     * Gets the max inactivity.
     *
     * @return the max inactivity
     */
    public int getMaxInactivity() {
        return TRS_MAX_INACTIVITY;
    }

    /**
     * Sets the max inactivity.
     *
     * @param param
     *            the new max inactivity
     */
    public void setMaxInactivity(final int param) {
        TRS_MAX_INACTIVITY = param;
    }

    /**
     * Sets the max inactivity.
     *
     * @param param
     *            the new max inactivity
     */
    public void setMaxInactivity(final String param) {
        TRS_MAX_INACTIVITY = Integer.parseInt(param);
    }

    /**
     * Gets the max transfers.
     *
     * @return the max transfers
     */
    public Integer getMaxTransfers() {
        return TRS_MAX_TRANSFERS;
    }

    /**
     * Sets the max transfers.
     *
     * @param param
     *            the new max transfers
     */
    public void setMaxTransfers(final Integer param) {
        TRS_MAX_TRANSFERS = param;
    }

    /**
     * Sets the max transfers.
     *
     * @param param
     *            the new max transfers
     */
    public void setMaxTransfers(final int param) {
        TRS_MAX_TRANSFERS = param;
    }

    /**
     * Gets the max transfers.
     *
     * @param defaultValue
     *            the default value
     *
     * @return the max transfers
     */
    public int getMaxTransfers(final int defaultValue) {
        return TRS_MAX_TRANSFERS == null ? defaultValue : TRS_MAX_TRANSFERS;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return TRS_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        TRS_NAME = param;
    }

    /**
     * Gets the host for replication name.
     *
     * @return the host for replication name
     */
    public String getHostForReplicationName() {
        return integerToString(HOS_NAME_FOR_REPLICATION);
    }

    /**
     * Sets the host for replication name.
     *
     * @param param
     *            the new host for replication name
     */
    public void setHostForReplicationName(final String param) {
        HOS_NAME_FOR_REPLICATION = stringToInteger(param);
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    public int getPort() {
        return TRS_PORT;
    }

    /**
     * Sets the port.
     *
     * @param param
     *            the new port
     */
    public void setPort(final int param) {
        TRS_PORT = param;
    }

    /**
     * Sets the port.
     *
     * @param param
     *            the new port
     */
    public void setPort(final String param) {
        TRS_PORT = Integer.parseInt(param);
    }

    /**
     * Gets the transfer group.
     *
     * @return the transfer group
     */
    public TransferGroup getTransferGroup() {
        return transferGroup;
    }

    /**
     * Sets the transfer group.
     *
     * @param param
     *            the new transfer group
     */
    public void setTransferGroup(final TransferGroup param) {
        transferGroup = param;
    }

    /**
     * Gets the host for replication.
     *
     * @return the host for replication
     */
    public Host getHostForReplication() {
        return hostForReplication;
    }

    /**
     * Sets the host for replication.
     *
     * @param param
     *            the new host for replication
     */
    public void setHostForReplication(final Host param) {
        hostForReplication = param;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(TRS_NAME);
    }

    /**
     * Equals.
     *
     * @param obj
     *            the obj
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (TransferServer) obj;
        return Objects.equals(TRS_NAME, other.TRS_NAME);
    }
}
