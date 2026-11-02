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

package ecmwf.ecpds.master.location;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.net.InetAddresses;

import ecmwf.ecpds.master.location.PhysicalLocationConfig.Machine;

/** Reconciles inventory and DNS off the scheduler and request threads, without scanning networks. */
public final class PhysicalLocationDiscovery implements AutoCloseable {
    private static final Logger LOG = LogManager.getLogger(PhysicalLocationDiscovery.class);

    @FunctionalInterface
    public interface Inventory {
        Map<String, String> hosts() throws Exception;
    }

    @FunctionalInterface
    interface Resolver {
        InetAddress[] resolve(String host) throws Exception;
    }

    private final PhysicalLocationConfig config;
    private final Inventory inventory;
    private final BiConsumer<Map<String, String>, Map<String, Machine>> publish;
    private final ThreadPoolExecutor worker;
    private volatile boolean busy;
    private volatile boolean closed;
    private volatile long nextDue;

    PhysicalLocationDiscovery(PhysicalLocationConfig config, Inventory inventory,
            BiConsumer<Map<String, String>, Map<String, Machine>> publish) {
        this.config = config;
        this.inventory = inventory;
        this.publish = publish;
        worker = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), runnable -> {
            final var thread = new Thread(runnable, "PhysicalLocationInventory");
            thread.setDaemon(true);
            return thread;
        });
    }

    void tick() {
        if (closed || busy || System.currentTimeMillis() < nextDue) {
            return;
        }
        busy = true;
        try {
            worker.execute(() -> {
                try {
                    reconcile(inventory.hosts(), InetAddress::getAllByName);
                } catch (Exception e) {
                    // Fail closed until inventory access is restored; retain persisted rows, not enrollment.
                    publish.accept(config.systems(), config.machines());
                    LOG.warn("Physical location inventory unavailable; automatic targets suspended");
                } finally {
                    nextDue = System.currentTimeMillis() + config.discovery().interval();
                    busy = false;
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException e) {
            busy = false;
        }
    }

    void reconcile(Map<String, String> hosts, Resolver resolver) {
        final Map<String, String> systems = new LinkedHashMap<>(config.systems());
        final Map<String, Machine> machines = new LinkedHashMap<>(config.machines());
        final Map<String, Machine> resolved = new HashMap<>();
        final var policy = config.discovery();
        boolean limited = false;
        int inspected = 0;
        for (final var entry : hosts.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            if (!(entry.getKey().equals("master") || entry.getKey().matches("(mover|monitor|database)\\..+"))
                    || entry.getKey().length() > 512 || entry.getKey().codePoints().anyMatch(Character::isISOControl)) {
                continue;
            }
            if (systems.containsKey(entry.getKey())) {
                continue;
            }
            if (systems.size() >= config.maxTargets() * 8 || inspected++ >= config.maxTargets() * 8) {
                limited = true;
                break;
            }
            final var host = entry.getValue();
            if (host == null || host.isBlank() || host.length() > 253
                    || !(InetAddresses.isInetAddress(host) || host.matches("[A-Za-z0-9][A-Za-z0-9._-]*"))) {
                continue;
            }
            if (!resolved.containsKey(host)) {
                Machine machine = null;
                try {
                    final var addresses = InetAddresses.isInetAddress(host)
                            ? new InetAddress[] { InetAddresses.forString(host) } : resolver.resolve(host);
                    // Reject mixed answers rather than silently sending credentials outside the policy.
                    if (addresses.length > 0 && Arrays.stream(addresses).allMatch(policy::permits)) {
                        final var address = Arrays.stream(addresses)
                                .min(Comparator.comparing(InetAddress::getHostAddress)).orElseThrow();
                        machine = PhysicalLocationConfig.machine(null, address, policy.port(), policy.profile());
                    }
                } catch (Exception e) {
                    // A failed/disallowed resolution removes the previous automatic mapping on publication.
                }
                resolved.put(host, machine);
            }
            final var machine = resolved.get(host);
            if (machine == null) {
                continue;
            }
            // Explicit mappings remain authoritative, including their profile and persisted identity.
            final var shared = machines.values().stream()
                    .filter(existing -> existing.targetKey().equals(machine.targetKey())).findFirst().orElse(machine);
            if (!machines.containsKey(shared.id()) && machines.size() >= config.maxTargets()) {
                limited = true;
                continue;
            }
            machines.putIfAbsent(shared.id(), shared);
            systems.put(entry.getKey(), shared.id());
        }
        if (limited) {
            LOG.warn("Physical location automatic inventory exceeds configured target/component limits");
        }
        if (!closed) {
            publish.accept(systems, machines);
        }
    }

    @Override
    public void close() {
        closed = true;
        worker.shutdownNow();
    }
}
