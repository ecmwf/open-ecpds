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

package ecmwf.ecpds.mover;

import static ecmwf.common.text.Util.isNotEmpty;

import java.util.ArrayList;
import java.util.List;

import javax.management.timer.Timer;

import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.Host;
import ecmwf.common.database.TransferMethod;
import ecmwf.common.ecaccess.MBeanRepository;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.ecpds.master.transfer.TransferComparator;

/**
 * The Class TransferRepository.
 */
final class TransferRepository extends MBeanRepository<DataTransfer> {
    private final MoverServer _mover;

    /** The _list. */
    private final List<DataTransfer> _list = new ArrayList<>();

    /**
     * Instantiates a new transfer repository.
     *
     * @param name
     *            the name
     */
    TransferRepository(final MoverServer _mover, final String name) {
        this._mover = _mover;

        super(name);
        setComparator(new TransferComparator(false));
        setDelay(Cnf.durationAt("Scheduler", "transferRepository", 5 * Timer.ONE_SECOND));
        setJammedTimeout(Cnf.durationAt("Scheduler", "transferRepositoryJammedTimeout", 5 * Timer.ONE_MINUTE));
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
    public DataTransfer getDataTransfer(final long id) {
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
        final var comment = byteSent >= fileSize ? Format.formatRate(fileSize, duration)
                : Format.formatPercentage(fileSize, byteSent) + " of " + Format.formatSize(fileSize);
        return transfer.getStatusCode() + "(" + comment + " on " + transfer.getDestinationName() + "/"
                + transfer.getHostName() + ") ";
    }

    /**
     * Clear.
     */
    @Override
    public void clear() {
        super.clear();
        _list.clear();
    }

    /**
     * Next step.
     *
     * @return the int
     */
    @Override
    public int nextStep() {
        if (_mover.masterManager != null && !_mover.masterManager.isConnected()) {
            return NEXT_STEP_DELAY;
        }
        for (final DataTransfer transfer : getList()) {
            if (!_list.contains(transfer)) {
                _list.add(transfer);
            }
        }
        final var size = _list.size();
        if (size > 0) {
            final var array = _list.toArray(new DataTransfer[size]);
            synchronized (_mover.transferRepository) {
                try {
                    _mover.getMasterProxy().updateDataTransfers(array);
                    _list.clear();
                } catch (final Exception e) {
                    MoverServer._log.warn("updateDataTransfers", e);
                    return NEXT_STEP_DELAY;
                }
                for (final DataTransfer transfer : array) {
                    delDataTransfer(false, transfer.getId());
                }
            }
        }
        return NEXT_STEP_DELAY;
    }

    /**
     * Del data transfer.
     *
     * @param force
     *            the force
     * @param id
     *            the id
     *
     * @return true, if successful
     */
    public boolean delDataTransfer(final boolean force, final long id) {
        var result = true;
        try (final var mutex = _mover.dataTransferMutexProvider.getMutex(id)) {
            synchronized (mutex.lock()) {
                final DataTransfer transfer;
                if ((transfer = getDataTransfer(id)) != null) {
                    final var statusCode = transfer.getStatusCode();
                    if (StatusFactory.DONE.equals(statusCode) || StatusFactory.RETR.equals(statusCode)) {
                        MoverServer._log.info("Remove transfer {} from data mover ({})", id, statusCode);
                        removeValue(transfer);
                    } else if (force) {
                        MoverServer._log.warn("Force removal of transfer {} from data mover ({})", id, statusCode);
                        removeValue(transfer);
                    } else {
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets the host.
     *
     * @param name
     *            the name
     *
     * @return the host
     */
    public Host getHost(final String name) {
        if (isNotEmpty(name)) {
            for (final DataTransfer transfer : getList()) {
                if (name.equals(transfer.getHostName())) {
                    return transfer.getHost();
                }
            }
        }
        return null;
    }

    /**
     * Gets the EC user.
     *
     * @param name
     *            the name
     *
     * @return the ECuser
     */
    public ECUser getECUser(final String name) {
        if (isNotEmpty(name)) {
            for (final DataTransfer transfer : getList()) {
                final var host = transfer.getHost();
                if (name.equals(host.getECUserName())) {
                    return host.getECUser();
                }
            }
        }
        return null;
    }

    /**
     * Gets the ectrans module.
     *
     * @param name
     *            the name
     *
     * @return the ectrans module
     */
    public ECtransModule getECtransModule(final String name) {
        if (isNotEmpty(name)) {
            for (final DataTransfer transfer : getList()) {
                final var method = transfer.getHost().getTransferMethod();
                if (name.equals(method.getECtransModuleName())) {
                    return method.getECtransModule();
                }
            }
        }
        return null;
    }

    /**
     * Gets the transfer method.
     *
     * @param name
     *            the name
     *
     * @return the transfer method
     */
    public TransferMethod getTransferMethod(final String name) {
        if (isNotEmpty(name)) {
            for (final DataTransfer transfer : getList()) {
                final var host = transfer.getHost();
                if (name.equals(host.getTransferMethodName())) {
                    return host.getTransferMethod();
                }
            }
        }
        return null;
    }
}
