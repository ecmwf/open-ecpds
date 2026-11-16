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

import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.Host;
import ecmwf.common.database.TransferMethod;

/**
 * The Class LocalRepository.
 */
final class LocalRepository extends MoverRepository {
    /** The _host. */
    private final Host _host;

    /**
     * Instantiates a new local repository.
     *
     * @param host
     *            the host
     */
    LocalRepository(final MoverServer _mover, final Host host) {
        super(_mover);

        _host = host;
    }

    /**
     * Gets the host.
     *
     * @param name
     *            the name
     *
     * @return the host
     */
    @Override
    public Host getHost(final String name) {
        return _host;
    }

    /**
     * Gets the EC user.
     *
     * @param name
     *            the name
     *
     * @return the EC user
     */
    @Override
    public ECUser getECUser(final String name) {
        return _host.getECUser();
    }

    /**
     * Gets the ectrans module.
     *
     * @param name
     *            the name
     *
     * @return the ectrans module
     */
    @Override
    public ECtransModule getECtransModule(final String name) {
        return _host.getTransferMethod().getECtransModule();
    }

    /**
     * Gets the transfer method.
     *
     * @param name
     *            the name
     *
     * @return the transfer method
     */
    @Override
    public TransferMethod getTransferMethod(final String name) {
        return _host.getTransferMethod();
    }
}
