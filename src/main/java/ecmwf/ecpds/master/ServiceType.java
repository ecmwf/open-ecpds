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
 * @version 7.3.7
 * @since 2026-03-02
 */

/**
 * Represents the supported service types that can register with the MasterServer.
 *
 * <p>
 * This enumeration is used to avoid raw string comparisons when identifying services such as {@code DataMover} and
 * {@code DataProxy}. It provides helper methods to safely determine the service category and to convert external string
 * representations into strongly typed values.
 * </p>
 *
 * <p>
 * Any unknown or unsupported service string is mapped to {@link #OTHER}.
 * </p>
 */
public enum ServiceType {

    /**
     * Represents a DataMover service.
     */
    DATA_MOVER("DataMover"),

    /**
     * Represents a DataProxy service.
     */
    DATA_PROXY("DataProxy"),

    /**
     * Represents any unknown or unsupported service type.
     */
    OTHER("");

    /**
     * The external string representation of the service as received during registration.
     */
    private final String value;

    /**
     * Constructs a ServiceType with its corresponding external string representation.
     *
     * @param value
     *            the service name as provided by the client
     */
    ServiceType(final String value) {
        this.value = value;
    }

    /**
     * Converts a service name string into a {@code ServiceType}.
     *
     * <p>
     * If the provided string does not match any known service type, {@link #OTHER} is returned.
     * </p>
     *
     * @param service
     *            the service name (may be null)
     *
     * @return the corresponding {@code ServiceType}, or {@link #OTHER} if the service is unknown or null
     */
    public static ServiceType from(final String service) {
        if (service == null) {
            return OTHER;
        }
        for (final ServiceType type : values()) {
            if (type.value.equals(service)) {
                return type;
            }
        }
        return OTHER;
    }

    /**
     * Indicates whether this service type represents a DataMover.
     *
     * @return {@code true} if this is {@link #DATA_MOVER}, otherwise {@code false}
     */
    public boolean isMover() {
        return this == DATA_MOVER;
    }

    /**
     * Indicates whether this service type represents a DataProxy.
     *
     * @return {@code true} if this is {@link #DATA_PROXY}, otherwise {@code false}
     */
    public boolean isProxy() {
        return this == DATA_PROXY;
    }
}