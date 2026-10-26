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

package ecmwf.ecpds.extension.ecaccess;

import java.io.IOException;
import java.rmi.Remote;

import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransDestination;
import ecmwf.common.database.MSUser;
import ecmwf.common.technical.ProxySocket;

/**
 * ECaccess gateway operations relayed by the Master for trusted Movers. Bulk data uses the existing ProxySocket
 * callback, not the Master RMI channel.
 */
public interface EcaccessGatewayAccess extends Remote {
    boolean available() throws IOException;

    void get(ECUser user, MSUser association, ECtransDestination destination, String cookie, String name,
            ProxySocket socket, long offset) throws IOException;

    void put(ECUser user, MSUser association, ECtransDestination destination, String cookie, String name,
            ProxySocket socket, long offset, long size) throws IOException;

    void del(ECUser user, MSUser association, ECtransDestination destination, String cookie, String name)
            throws IOException;

    void mkdir(ECUser user, MSUser association, ECtransDestination destination, String cookie, String name)
            throws IOException;

    void rmdir(ECUser user, MSUser association, ECtransDestination destination, String cookie, String name)
            throws IOException;

    long size(ECUser user, MSUser association, ECtransDestination destination, String cookie, String name)
            throws IOException;

    void move(ECUser user, MSUser association, ECtransDestination destination, String cookie, String source,
            String target) throws IOException;

    String[] list(ECUser user, MSUser association, ECtransDestination destination, String cookie, String directory)
            throws IOException;

    void remove(ECUser user, MSUser association, ECtransDestination destination, String cookie) throws IOException;

    void close() throws IOException;
}
