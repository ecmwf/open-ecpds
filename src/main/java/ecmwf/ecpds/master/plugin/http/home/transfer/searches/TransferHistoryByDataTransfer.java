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

package ecmwf.ecpds.master.plugin.http.home.transfer.searches;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.common.database.DataBaseCursor;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class TransferHistoryByDataTransfer.
 */
public class TransferHistoryByDataTransfer extends ModelSearchBase {

    /** The dt. */
    private final DataTransfer dt;

    /** The after schedule time. */
    private final boolean afterScheduleTime;

    /** The cursor. */
    private final DataBaseCursor cursor;

    /**
     * Instantiates a new transfer history by data transfer.
     *
     * @param dt
     *            the dt
     * @param afterScheduleTime
     *            the after schedule time
     * @param cursor
     *            the cursor
     */
    public TransferHistoryByDataTransfer(final DataTransfer dt, final boolean afterScheduleTime,
            final DataBaseCursor cursor) {
        this.dt = dt;
        this.afterScheduleTime = afterScheduleTime;
        this.cursor = cursor;
    }

    /**
     * Instantiates a new transfer history by data transfer.
     *
     * @param dt
     *            the dt
     */
    public TransferHistoryByDataTransfer(final DataTransfer dt) {
        this.dt = dt;
        this.afterScheduleTime = false;
        this.cursor = null;
    }

    /**
     * Gets the after schedule time.
     *
     * @return the after schedule time
     */
    public boolean getAfterScheduleTime() {
        return afterScheduleTime;
    }

    /**
     * Gets the data base cursor.
     *
     * @return the data base cursor
     */
    public DataBaseCursor getDataBaseCursor() {
        return cursor;
    }

    /**
     * Gets the data transfer.
     *
     * @return the data transfer
     */
    public DataTransfer getDataTransfer() {
        return dt;
    }
}
