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

package ecmwf.ecpds.mover.plugin.http;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Closeable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.NativeAuthenticationProvider;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.ecpds.mover.MoverServer;

/**
 * The Class BlobStoreLocator.
 */
public class BlobStoreLocator {

    /** The Constant logger. */
    private static final Logger logger = LogManager.getLogger(BlobStoreLocator.class);

    /** The Constant _mover. */
    private static final MoverServer _mover = StarterServer.getInstance(MoverServer.class);

    /**
     * Locate blob store.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param identity
     *            the identity
     * @param container
     *            the container
     * @param blob
     *            the blob
     *
     * @return the map. entry
     */
    Map.Entry<String, BlobStore> locateBlobStore(final HttpServletRequest request, final HttpServletResponse response,
            final String identity, final String container, final String blob) {
        logger.debug("identity: " + identity + ", container: " + container + ", blob: " + blob);
        final String incomingUserHash;
        try {
            incomingUserHash = _mover.getMasterInterface().getIncomingUserHash(identity);
        } catch (final Throwable t) {
            logger.warn("getUserSession", t);
            return null;
        }

        return incomingUserHash != null ? new Map.Entry<>() {

            BlobStore blobStore;

            @Override
            public String getKey() {
                return identity;
            }

            @Override
            public synchronized BlobStore getValue() {
                if (blobStore != null) {
                    // Already created
                    return blobStore;
                }
                // First call (should be called as late as possible after all the other
                // verifications are completed)
                final var remoteAddr = request.getRemoteAddr();
                try {
                    return blobStore = new BlobStore(remoteAddr, NativeAuthenticationProvider.getInstance()
                            .getUserSession(remoteAddr, identity, incomingUserHash, "s3", (Closeable) () -> response
                                    .sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Interrupted by server")));
                } catch (final Throwable t) {
                    logger.warn("getUserSession", t);
                    return null;
                }
            }

            @Override
            public BlobStore setValue(final BlobStore value) {
                // Should never be called
                return blobStore = value;
            }
        } : null;
    }
}
