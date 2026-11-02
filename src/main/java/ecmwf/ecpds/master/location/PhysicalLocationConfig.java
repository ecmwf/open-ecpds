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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.net.InetAddresses;

import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;

/** Startup-only allowlist; management addresses are literal IPs so collection never waits for DNS. */
public record PhysicalLocationConfig(boolean enabled, long interval, long staleAfter, long timeout, int retries,
        int concurrency, Map<String, String> systems, Map<String, Machine> machines) {

    public PhysicalLocationConfig {
        systems = Map.copyOf(systems);
        machines = Map.copyOf(machines);
    }

    public static final class Profile {
        final boolean v3;
        final String username;
        final String authProtocol;
        final String authPassword;
        final String privacyPassword;
        final String communityPassword;
        final String context;

        Profile(boolean v3, String username, String authProtocol, String authPassword, String privacyPassword,
                String communityPassword, String context) {
            this.v3 = v3;
            this.username = username;
            this.authProtocol = authProtocol;
            this.authPassword = authPassword;
            this.privacyPassword = privacyPassword;
            this.communityPassword = communityPassword;
            this.context = context;
        }

        @Override
        public String toString() {
            return "SNMP profile (credentials redacted)";
        }
    }

    public record Machine(String id, InetAddress address, int port, Profile profile, String targetKey) {
    }

    public static PhysicalLocationConfig disabled() {
        return new PhysicalLocationConfig(false, 1_800_000, 86_400_000, 2_000, 1, 4, Map.of(), Map.of());
    }

    public static PhysicalLocationConfig load() throws Exception {
        return load(section -> Cnf.at(section, Map.of()));
    }

    static PhysicalLocationConfig load(Function<String, Map<String, String>> sections) throws Exception {
        final var options = sections.apply("PhysicalLocation");
        final var enabled = options.getOrDefault("enabled", "no");
        if ("no".equalsIgnoreCase(enabled) || "false".equalsIgnoreCase(enabled)) {
            return disabled();
        }
        require("yes".equalsIgnoreCase(enabled) || "true".equalsIgnoreCase(enabled), "Invalid enabled flag");
        final long interval = duration(options, "interval", "30m", 60_000, 604_800_000);
        final long staleAfter = duration(options, "staleAfter", "24h", interval, 2_592_000_000L);
        final long timeout = duration(options, "timeout", "2s", 100, 10_000);
        final int retries = number(options, "retries", 1, 0, 2);
        final int concurrency = number(options, "concurrency", 4, 1, 16);
        final int maxTargets = number(options, "maxTargets", 256, 1, 1024);
        final var systems = sections.apply("PhysicalLocationSystems");
        require(systems.size() <= maxTargets * 8, "Too many system mappings");
        final Map<String, Machine> machines = new LinkedHashMap<>();
        final Map<String, Profile> profiles = new LinkedHashMap<>();
        for (final var entry : systems.entrySet()) {
            final var component = entry.getKey();
            require(component != null && component.length() <= 512
                    && (component.equals("master") || component.matches("(mover|monitor|database)\\..+"))
                    && component.codePoints().noneMatch(Character::isISOControl), "Invalid system mapping");
            final var id = entry.getValue();
            identifier(id);
            if (machines.containsKey(id)) {
                continue;
            }
            require(machines.size() < maxTargets, "Too many management targets");
            final var target = sections.apply("PhysicalLocationMachine." + id);
            final var address = InetAddresses.forString(target.getOrDefault("address", ""));
            require(!address.isAnyLocalAddress() && !address.isMulticastAddress() && !address.isLinkLocalAddress(),
                    "Management address must be an unicast IP without an interface scope");
            final int port = number(target, "port", 161, 1, 65535);
            final var profileName = target.getOrDefault("profile", "");
            identifier(profileName);
            var profile = profiles.get(profileName);
            if (profile == null) {
                profile = profile(sections.apply("PhysicalLocationProfile." + profileName));
                profiles.put(profileName, profile);
            }
            // Changing the management endpoint/context invalidates the old value; rotating credentials does not.
            final var identity = address.getHostAddress() + "\n" + port + "\n" + profile.context;
            final var key = HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8)));
            machines.put(id, new Machine(id, address, port, profile, key));
        }
        return new PhysicalLocationConfig(true, interval, staleAfter, timeout, retries, concurrency, systems, machines);
    }

    private static Profile profile(Map<String, String> options) throws Exception {
        final var version = options.getOrDefault("version", "v3");
        if ("v2c".equals(version)) {
            return new Profile(false, "", "", "", "", secret(options, "communityPasswordFile", 1), "");
        }
        require("v3".equals(version), "Only v3 and explicit v2c are supported");
        final var username = options.getOrDefault("username", "");
        require(!username.isBlank() && username.getBytes(StandardCharsets.UTF_8).length <= 32
                && username.codePoints().noneMatch(Character::isISOControl), "Invalid SNMPv3 username");
        final var auth = options.getOrDefault("authProtocol", "SHA256");
        require("SHA256".equals(auth) || "SHA512".equals(auth), "Only SHA256 and SHA512 authentication are supported");
        final var context = options.getOrDefault("context", "");
        require(context.getBytes(StandardCharsets.UTF_8).length <= 32
                && context.codePoints().noneMatch(Character::isISOControl), "Invalid SNMPv3 context");
        return new Profile(true, username, auth, secret(options, "authPasswordFile", 8),
                secret(options, "privacyPasswordFile", 8), "", context);
    }

    private static String secret(Map<String, String> options, String key, int minimum) throws Exception {
        final var path = Path.of(options.getOrDefault(key, ""));
        require(Files.isRegularFile(path) && Files.size(path) <= 4096, "Missing or oversized SNMP credential file");
        var value = Files.readString(path, StandardCharsets.UTF_8);
        if (value.endsWith("\n")) {
            value = value.substring(0, value.length() - 1);
            if (value.endsWith("\r")) {
                value = value.substring(0, value.length() - 1);
            }
        }
        require(value.getBytes(StandardCharsets.UTF_8).length >= minimum && !value.isBlank()
                && value.codePoints().noneMatch(Character::isISOControl), "Invalid SNMP credential file");
        return value;
    }

    private static long duration(Map<String, String> options, String key, String fallback, long min, long max) {
        final long value = Format.getDuration(options.getOrDefault(key, fallback));
        require(value >= min && value <= max, "Physical location duration outside supported limits");
        return value;
    }

    private static int number(Map<String, String> options, String key, int fallback, int min, int max) {
        final int value = Integer.parseInt(options.getOrDefault(key, Integer.toString(fallback)));
        require(value >= min && value <= max, "Physical location number outside supported limits");
        return value;
    }

    private static void identifier(String value) {
        require(value != null && value.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,127}"), "Invalid location identifier");
    }

    private static void require(boolean valid, String message) {
        if (!valid) {
            throw new IllegalArgumentException(message);
        }
    }
}
