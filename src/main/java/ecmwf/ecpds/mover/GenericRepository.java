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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.Host;
import ecmwf.common.database.TransferMethod;

/**
 * The Interface GenericRepository.
 */
interface GenericRepository {
    /**
     * Gets the host.
     *
     * @param name
     *            the name
     *
     * @return the host
     */
    Host getHost(String name);

    /**
     * Gets the EC user.
     *
     * @param name
     *            the name
     *
     * @return the EC user
     */
    ECUser getECUser(String name);

    /**
     * Gets the ectrans module.
     *
     * @param name
     *            the name
     *
     * @return the ectrans module
     */
    ECtransModule getECtransModule(String name);

    /**
     * Gets the transfer method.
     *
     * @param name
     *            the name
     *
     * @return the transfer method
     */
    TransferMethod getTransferMethod(String name);

    /**
     * Gets the data input stream.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    InputStream getDataInputStream(Object ticket) throws IOException;

    /**
     * Gets the data output stream.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    OutputStream getDataOutputStream(Object ticket) throws IOException;

    /**
     * Gets the data input file.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data input file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    File getDataInputFile(Object ticket) throws IOException;

    /**
     * Gets the data output file.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data output file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    File getDataOutputFile(Object ticket) throws IOException;

    /**
     * Gets the original file name.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data output file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    String getOriginalFilename(Object ticket) throws IOException;
}
