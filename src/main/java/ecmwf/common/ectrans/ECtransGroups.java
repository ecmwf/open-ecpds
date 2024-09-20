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

package ecmwf.common.ectrans;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import ecmwf.common.technical.Pair;

/**
 * The Enum ECtransGroups.
 */
public enum ECtransGroups {

    /** The host. */
    HOST(Module.HOST_ECACCESS, Module.HOST_ACQUISITION, Module.HOST_ECAUTH, Module.HOST_ECPDS, Module.HOST_ECTRANS,
            Module.HOST_FTP, Module.HOST_MASTER, Module.HOST_PROXY, Module.HOST_RETRIEVAL, Module.HOST_UPLOAD,
            Module.HOST_SFTP, Module.HOST_S3, Module.HOST_GCS, Module.HOST_AZURE),

    /** The destination. */
    DESTINATION(Module.DESTINATION_ALIAS, Module.DESTINATION_ECTRANS, Module.DESTINATION_INCOMING,
            Module.DESTINATION_SCHEDULER, Module.DESTINATION_MQTT),

    /** The user. */
    USER(Module.USER_PORTAL);

    /** The modules. */
    private final List<Module> modules = new ArrayList<>();

    /**
     * Instantiates a new ectrans groups.
     *
     * @param modules
     *            the modules
     */
    ECtransGroups(final Module... modules) {
        this.modules.addAll(Arrays.asList(modules));
    }

    /**
     * Gets the modules.
     *
     * @return the modules
     */
    public List<Module> getModules() {
        return modules.stream().sorted(Comparator.comparing(Object::toString)).toList();
    }

    /**
     * The Enum Module.
     */
    public enum Module {

        /** The user portal. */
        USER_PORTAL,
        /** The host proxy. */
        HOST_PROXY,
        /** The host ecauth. */
        HOST_ECAUTH,
        /** The host ecaccess. */
        HOST_ECACCESS,
        /** The host ecpds. */
        HOST_ECPDS,
        /** The host retrieval. */
        HOST_RETRIEVAL,
        /** The host upload. */
        HOST_UPLOAD,
        /** The host acquisition. */
        HOST_ACQUISITION,

        /** The host ectrans. */
        HOST_ECTRANS,
        /** The host master. */
        HOST_MASTER,
        /** The host ftp. */
        HOST_FTP,
        /** The host sftp. */
        HOST_SFTP,
        /** The host s3. */
        HOST_S3,
        /** The host gcs. */
        HOST_GCS,
        /** The host azure. */
        HOST_AZURE,
        /** The destination alias. */
        DESTINATION_ALIAS,

        /** The destination ectrans. */
        DESTINATION_ECTRANS,
        /** The destination mqtt. */
        DESTINATION_MQTT,
        /** The destination incoming. */
        DESTINATION_INCOMING,
        /** The destination scheduler. */
        DESTINATION_SCHEDULER;

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return toString().split("_")[1].toLowerCase();
        }

        /**
         * Gets the ectrans setup.
         *
         * @param data
         *            the data
         * @param parameters
         *            the parameters
         *
         * @return the ectrans setup
         */
        public ECtransSetup getECtransSetup(final String data, final Pair<?>... parameters) {
            return new ECtransSetup(getName(), data, parameters);
        }
    }
}
