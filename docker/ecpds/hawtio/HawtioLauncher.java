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

/*
 * Minimal launcher for io.hawt.embedded.Main, bypassing the official project's JBang-based
 * "hawtio" CLI wrapper entirely. Main.main(String[]) ignores its args (fields are meant to
 * be set programmatically); this is the simplest path to that, with configuration read
 * directly from the environment variables this image's entrypoint sets.
 *
 * See docs/monitoring/jmx-export.md for why this exists instead of a published Hawtio image.
 */
import io.hawt.embedded.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HawtioLauncher {
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.setHost(env("HAWTIO_HOST", "0.0.0.0"));
        main.setPort(Integer.parseInt(env("HAWTIO_PORT", "8080")));
        main.setWar(env("HAWTIO_WAR", "/opt/hawtio/lib/hawtio-default-5.3.0.war"));

        // Comma-separated name=url pairs, e.g. "master=http://localhost:2062/jolokia,...".
        // Each becomes an entry in the Connect/Remote page's connection list - see
        // io.hawt.embedded.Main#call(), which converts this field into the
        // hawtio.connect.presetConnections system property the frontend reads on load.
        String presets = System.getenv("HAWTIO_PRESET_CONNECTIONS");
        if (presets != null && !presets.isBlank()) {
            Map<String, Optional<String>> connections = new HashMap<>();
            for (String entry : presets.split(",")) {
                String[] kv = entry.split("=", 2);
                connections.put(kv[0].trim(), kv.length > 1 ? Optional.of(kv[1].trim()) : Optional.empty());
            }
            main.setConnections(connections);
        }

        // call(), not run(): call() is what applies setConnections() to the system
        // property above (and is also the upstream-intended entry point - the "hawtio"
        // CLI itself invokes it via picocli's Callable mechanism, not run() directly).
        main.call();
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }
}
