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

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.database.DataTransfer;
import ecmwf.common.plugin.PluginEvent;

/**
 * The Class DataTransferEvent.
 */
public final class DataTransferEvent extends PluginEvent<DataTransfer> {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 500275739755464953L;

    /** The Constant NAME. */
    public static final String NAME = "DataTransferEvent";

    /**
     * Instantiates a new data transfer event.
     *
     * @param target
     *            the target
     */
    public DataTransferEvent(final DataTransfer target) {
        super(NAME, target);
    }

    /**
     * Instantiates a new data transfer event.
     *
     * @param root
     *            the root
     * @param target
     *            the target
     */
    public DataTransferEvent(final String root, final DataTransfer target) {
        this(target);
        setTarget(root);
    }

    /**
     * Gets the data transfer.
     *
     * @return the data transfer
     */
    public DataTransfer getDataTransfer() {
        return getObject();
    }
}
