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

package ecmwf.ecpds.master;

import static ecmwf.common.text.Util.isNotEmpty;
import java.util.ArrayList;
import java.util.List;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.ecaccess.StorageRepository;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.ecpds.master.transfer.TransferComparator;

/**
 * The Class TransferRepository.
 */
final class TransferRepository extends StorageRepository<DataTransfer> {
    private final MasterServer _master;

    /**
     * Instantiates a new transfer repository.
     *
     * @param name
     *            the name
     */
    TransferRepository(final MasterServer _master, final String name) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(name, Cnf.at("StorageRepository", "transferSize", 5), Cnf.at("StorageRepository", "transferDelay", 250));
        setComparator(new TransferComparator(false));
    }

    /**
     * Gets the key.
     *
     * @param transfer
     *            the transfer
     *
     * @return the key
     */
    @Override
    public String getKey(final DataTransfer transfer) {
        return Format.formatLong(transfer.getId(), 10, true);
    }

    /**
     * Gets the data transfer.
     *
     * @param id
     *            the id
     *
     * @return the data transfer
     */
    DataTransfer getDataTransfer(final long id) {
        return getValue(Format.formatLong(id, 10, true));
    }

    /**
     * Gets the status.
     *
     * @param transfer
     *            the transfer
     *
     * @return the status
     */
    @Override
    public String getStatus(final DataTransfer transfer) {
        final var fileSize = transfer.getDataFile().getSize();
        final var duration = transfer.getDuration();
        final var byteSent = transfer.getSent();
        final var moverName = transfer.getMoverName();
        return transfer.getStatusCode() + "("
                + (byteSent >= fileSize ? Format.formatRate(fileSize, duration)
                        : Format.formatPercentage(fileSize, byteSent) + " of " + Format.formatSize(fileSize))
                + " for " + transfer.getDestinationName() + (isNotEmpty(moverName) ? " on " + moverName : "") + "/"
                + transfer.getHostName() + ")";
    }

    /**
     * Gets the status.
     *
     * @param destinationName
     *            the Destination name
     *
     * @return the status
     */
    public String getStatus(final String destinationName) {
        final var result = new StringBuilder();
        for (final DataTransfer transfer : getList()) {
            if (transfer.getDestinationName().equals(destinationName)) {
                result.append(result.isEmpty() ? "" : " ").append(getStatus(transfer).replace(' ', '_'));
            }
        }
        return result.toString();
    }

    /**
     * Gets the data transfers.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the data transfers
     */
    public DataTransfer[] getDataTransfers(final String destinationName) {
        final List<DataTransfer> array = new ArrayList<>();
        for (final DataTransfer transfer : getList()) {
            if (destinationName.equals(transfer.getDestinationName())) {
                array.add(transfer);
            }
        }
        return array.toArray(new DataTransfer[array.size()]);
    }

    /**
     * Expired.
     *
     * @param transfer
     *            the transfer
     *
     * @return true, if successful
     */
    @Override
    public boolean expired(final DataTransfer transfer) {
        final var code = transfer.getStatusCode();
        return transfer.getDeleted() || !StatusFactory.EXEC.equals(code) && !StatusFactory.WAIT.equals(code);
    }

    @Override
    protected boolean removeIfExpired(final DataTransfer transfer) {
        return removeCurrentIfExpired(transfer);
    }

    /**
     * Update.
     *
     * @param transfer
     *            the transfer
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public void update(final DataTransfer transfer) throws Exception {
        final var size = transfer.getDataFile().getSize();
        final var filterSize = transfer.getDataFile().getFilterSize();
        final var sent = transfer.getSent();
        final var code = transfer.getStatusCode();
        final var comment = transfer.getComment();
        if (StatusFactory.INIT.equals(code) || StatusFactory.SCHE.equals(code) || StatusFactory.FETC.equals(code)) {
            return;
        }
        MasterServer._log.debug("Update DataTransfer-{}={} ({})", transfer.getId(), code,
                StatusFactory.DONE.equals(code) ? Format.formatRate(sent, transfer.getDuration())
                        : isNotEmpty(comment) ? comment : "no-comment");
        if (size != sent && filterSize != sent && StatusFactory.DONE.equals(code)) {
            MasterServer._log.warn("Incompatible sent/size for DataTransfer-{} (sent={},size={},filterSize={})",
                    transfer.getId(), sent, size, filterSize);
        }
        final var event = new DataTransferEvent(transfer);
        event.setSource("MasterServer.TransferRepository.update");
        _master.getDataBase().update(transfer);
        _master.handle(event);
        if (!transfer.getDeleted() && !StatusFactory.RETR.equals(code)) {
            _master.addTransferHistory(transfer);
        }
    }
}
