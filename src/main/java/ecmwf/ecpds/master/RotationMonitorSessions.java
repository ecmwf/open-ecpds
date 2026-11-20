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
 */

package ecmwf.ecpds.master;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import ecmwf.ecpds.extension.InteractiveLogins;

final class RotationMonitorSessions {
    private static final int CAPACITY = 4096;
    private static final Duration LIFETIME = Duration.ofHours(8);
    private final Map<String, Binding> issued = new HashMap<>();
    private final Clock clock;

    private record Binding(String user, String origin, Instant expires) {
    }

    RotationMonitorSessions(final Clock clock) {
        this.clock = clock;
    }

    synchronized String issue(final String user, final String origin) {
        if (user == null || user.isBlank() || origin == null || origin.isBlank()) {
            return null;
        }
        final var now = clock.instant();
        issued.values().removeIf(binding -> !binding.expires().isAfter(now));
        if (issued.size() >= CAPACITY) {
            return null;
        }
        final var token = InteractiveLogins.random();
        return issued.putIfAbsent(token, new Binding(user, origin, now.plus(LIFETIME))) == null ? token : null;
    }

    synchronized String verify(final ECpdsSession session, final String origin) throws MasterException {
        final var binding = session == null ? null : issued.get(session.rotationToken());
        if (binding == null || !binding.expires().isAfter(clock.instant()) || !binding.origin().equals(origin)
                || !binding.origin().equals(session.getRoot()) || session.getWebUser() == null
                || !binding.user().equals(session.getWebUser().getId())) {
            throw new MasterException("A fresh authenticated Monitor session is required for password rotation");
        }
        return binding.user();
    }

    synchronized void close(final ECpdsSession session) {
        if (session != null) {
            issued.remove(session.rotationToken());
        }
    }
}
