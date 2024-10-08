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

package ecmwf.common.callback;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;

/**
 * The Class RemoteEngineThread.
 */
public final class RemoteEngineThread extends ConfigurableLoopRunnable {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RemoteEngineThread.class);

    /** The Constant _remotes. */
    private static final Hashtable<String, Vector<RemoteManagement>> _remotes = new Hashtable<>();

    /** The _engine. */
    private static RemoteEngineThread _engine = null;

    /**
     * Instantiates a new remote engine thread.
     */
    private RemoteEngineThread() {
        setPause(2000);
    }

    /**
     * Removes the.
     *
     * @param group
     *            the group
     */
    public static void remove(final String group) {
        final var vector = _remotes.get(group);
        if (vector != null) {
            for (final RemoteManagement remote : vector) {
                remote.destroy();
            }
        }
    }

    /**
     * Removes all.
     */
    public static void removeAll() {
        final var remotes = _remotes.keys();
        while (remotes.hasMoreElements()) {
            remove(remotes.nextElement());
        }
    }

    /**
     * {@inheritDoc}
     *
     * Configurable loop run.
     */
    @Override
    public void configurableLoopRun() {
        try {
            final var keys = _remotes.keys();
            while (keys.hasMoreElements()) {
                final var key = keys.nextElement();
                final var vector = _remotes.get(key);
                final var remotes = vector.elements();
                while (remotes.hasMoreElements()) {
                    final var remote = remotes.nextElement();
                    if (remote != null && !remote.alive()) {
                        vector.remove(remote);
                        _log.debug("Removing remote object " + key);
                        try {
                            remote.destroy();
                        } catch (final Exception e) {
                            _log.warn("Destroying remote object " + key, e);
                        }
                        try {
                            UnicastRemoteObject.unexportObject(remote, true);
                        } catch (final Exception e) {
                            _log.warn("Unexporting remote object " + key, e);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            _log.warn("Running RemoteEngine thread", e);
        }
    }

    /**
     * Subscribe.
     *
     * @param group
     *            the group
     * @param remote
     *            the remote
     */
    public static void subscribe(final String group, final RemoteManagement[] remote) {
        for (final RemoteManagement remoteManagement : remote) {
            subscribe(group, remoteManagement);
        }
    }

    /**
     * Subscribe.
     *
     * @param group
     *            the group
     * @param remote
     *            the remote
     */
    public static synchronized void subscribe(final String group, final RemoteManagement remote) {
        if (_engine == null) {
            _engine = new RemoteEngineThread();
            _engine.setPriority(Thread.MIN_PRIORITY);
            _engine.execute();
        }
        final Vector<RemoteManagement> vector;
        if (!_remotes.containsKey(group)) {
            vector = new Vector<>();
            _remotes.put(group, vector);
        } else {
            vector = _remotes.get(group);
        }
        vector.add(remote);
    }
}
