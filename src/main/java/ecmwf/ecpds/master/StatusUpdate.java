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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;

import ecmwf.common.database.DataBaseException;

/**
 * The Interface StatusUpdate.
 */
public interface StatusUpdate {

    /** The in progress. */
    String IN_PROGRESS = "In progress ...";

    /**
     * Info to display in the host output.
     *
     * @param messages
     *            the messages
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    void info(String... messages) throws IOException, DataBaseException;

    /**
     * Warn to display in the host output.
     *
     * @param messages
     *            the messages
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    void warn(String... messages) throws IOException, DataBaseException;
}
