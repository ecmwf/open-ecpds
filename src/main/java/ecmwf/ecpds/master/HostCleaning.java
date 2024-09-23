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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ACQUISITION;
import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ECACCESS;
import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_PROXY;
import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_RETRIEVAL;
import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_UPLOAD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECACCESS_DESTINATION;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.Host;
import ecmwf.common.database.TransferMethod;
import ecmwf.common.ectrans.ECtransGroups;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.ecpds.master.transfer.HostOption;

/**
 * The Class HostCleaning.
 */
final class HostCleaning {

    /**
     * Instantiates a new host cleaning. Utility class should not have public constructors.
     */
    private HostCleaning() {
    }

    /**
     * Clean all Hosts.
     *
     * @param base
     *            the base
     *
     * @return the int
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public static int cleanHosts(final ECpdsBase base) throws DataBaseException {
        var processed = 0;
        for (final Host host : base.getHostArray()) {
            cleanHost(base, host);
            processed++;
        }
        return processed;
    }

    /**
     * Clean the Hosts by type.
     *
     * @param base
     *            the base
     * @param type
     *            the type
     *
     * @return the int
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public static int cleanHosts(final ECpdsBase base, final String type) throws DataBaseException {
        var processed = 0;
        for (final Host host : base.getHostArray()) {
            final var hostType = host.getType();
            if (type.equals(hostType)) {
                cleanHost(base, host);
                processed++;
            }
        }
        return processed;
    }

    /**
     * Clean the Host.
     *
     * @param base
     *            the base
     * @param host
     *            the host
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public static void cleanHost(final ECpdsBase base, final Host host) throws DataBaseException {
        final var hostType = host.getType();
        final var ectransModuleName = host.getTransferMethod().getECtransModuleName();
        final var setup = new ECtransSetup(ectransModuleName, host.getData());
        final String ecaccessModuleName;
        // If this is the ecaccess module then we have to get the transfer
        // module to use on the remote Gateway!
        if (ectransModuleName.equals(HOST_ECACCESS.getName())) {
            final var transferMethodName = setup.getString(HOST_ECACCESS_DESTINATION);
            final var transferMethod = base.getTransferMethodObject(transferMethodName);
            if (transferMethod == null) {
                if ("genericExec".equals(transferMethodName)) {
                    // This module does not exist on ECPDS, only on ECaccess!
                    ecaccessModuleName = "exec";
                } else {
                    // Not found!
                    ecaccessModuleName = null;
                }
            } else {
                // We found it!
                ecaccessModuleName = transferMethod.getECtransModuleName();
            }
        } else {
            ecaccessModuleName = null;
        }
        for (final TransferMethod method : base.getTransferMethodArray()) {
            final var thisModuleName = method.getECtransModuleName();
            if (!thisModuleName.equals(ectransModuleName) && !thisModuleName.equals(ecaccessModuleName)) {
                // Not used so let's remove it!
                setup.removeAll(thisModuleName);
            }
        }
        if (!HostOption.PROXY.equals(hostType)) {
            // Not used so let's remove it!
            setup.removeAll(HOST_PROXY);
        }
        if (!HostOption.ACQUISITION.equals(hostType)) {
            // Not used so let's remove it!
            setup.removeAll(HOST_ACQUISITION);
        }
        if (!HostOption.ACQUISITION.equals(hostType) && !HostOption.SOURCE.equals(hostType)) {
            // Not used so let's remove it!
            setup.removeAll(HOST_RETRIEVAL);
        }
        if (!HostOption.DISSEMINATION.equals(hostType) && !HostOption.PROXY.equals(hostType)) {
            // Not used so let's remove it!
            setup.removeAll(HOST_UPLOAD);
        }
        // Normalize the representation of the data and remove option with same value as
        // default value!
        ECtransOptions.get(ECtransGroups.HOST).stream().forEach(option -> setup.standardize(option, true));
        // Save the modified Host!
        host.setData(setup.getData());
        base.update(host);
    }
}
