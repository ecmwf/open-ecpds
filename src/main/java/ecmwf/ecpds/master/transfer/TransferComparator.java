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

package ecmwf.ecpds.master.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Comparator;

import ecmwf.common.database.DataTransfer;

/**
 * The Class TransferComparator.
 */
public final class TransferComparator implements Comparator<DataTransfer> {
    /** The _using priorities. */
    public final boolean _usingPriorities;

    /**
     * Instantiates a new transfer comparator. Allow specifying if the priorities should be used for the sorting.
     *
     * @param usingPriorities
     *            using priorities or not?
     */
    public TransferComparator(final boolean usingPriorities) {
        _usingPriorities = usingPriorities;
    }

    /**
     * Compare.
     *
     * @param transfer1
     *            the transfer 1
     * @param transfer2
     *            the transfer 2
     *
     * @return the int
     */
    @Override
    public int compare(final DataTransfer transfer1, final DataTransfer transfer2) {
        return _usingPriorities ? _compareDataTransfersUsingPriorities(transfer1, transfer2)
                : _compareDataTransfersWithoutPriorities(transfer1, transfer2);
    }

    /**
     * Compare data transfers using priorities.
     *
     * @param transfer1
     *            the transfer1
     * @param transfer2
     *            the transfer2
     *
     * @return the int
     */
    private static int _compareDataTransfersUsingPriorities(final DataTransfer transfer1,
            final DataTransfer transfer2) {
        final long priority1 = transfer1.getPriority();
        final long priority2 = transfer2.getPriority();
        if (priority1 < priority2) {
            return -1;
        }
        if (priority1 == priority2) {
            return _compareDataTransfersWithoutPriorities(transfer1, transfer2);
        } else {
            return 1;
        }
    }

    /**
     * _compare data transfers without using priorities.
     *
     * @param transfer1
     *            the transfer1
     * @param transfer2
     *            the transfer2
     *
     * @return the int
     */
    private static int _compareDataTransfersWithoutPriorities(final DataTransfer transfer1,
            final DataTransfer transfer2) {
        final var time1 = transfer1.getQueueTime().getTime();
        final var time2 = transfer2.getQueueTime().getTime();
        if (time1 < time2) {
            return -1;
        }
        if (time1 == time2) {
            return Long.compare(transfer1.getId(), transfer2.getId());
        } else {
            return 1;
        }
    }
}
