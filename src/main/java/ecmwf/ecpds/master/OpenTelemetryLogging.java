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

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

/** Routes the Master's OpenTelemetry SDK diagnostics to its file logger rather than JUL's console. */
final class OpenTelemetryLogging {
    // Keep a strong reference: JUL otherwise permits namespace loggers to be garbage collected.
    private static final java.util.logging.Logger SDK = java.util.logging.Logger.getLogger("io.opentelemetry");
    private static boolean installed;

    private OpenTelemetryLogging() {
    }

    static synchronized void install() {
        if (installed) {
            return;
        }
        final var handler = new Handler() {
            private final org.apache.logging.log4j.Logger log = LogManager.getLogger("OpenTelemetrySdk");
            private final Formatter formatter = new Formatter() {
                @Override
                public String format(final LogRecord record) {
                    return formatMessage(record);
                }
            };

            @Override
            public void publish(final LogRecord record) {
                if (!isLoggable(record)) {
                    return;
                }
                final var value = record.getLevel().intValue();
                final var level = value >= java.util.logging.Level.SEVERE.intValue() ? Level.ERROR
                        : value >= java.util.logging.Level.WARNING.intValue() ? Level.WARN
                                : value >= java.util.logging.Level.INFO.intValue() ? Level.INFO
                                        : value >= java.util.logging.Level.FINE.intValue() ? Level.DEBUG : Level.TRACE;
                if (log.isEnabled(level)) {
                    log.log(level, "[" + record.getLoggerName() + "] " + formatter.formatMessage(record),
                            record.getThrown());
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        handler.setLevel(java.util.logging.Level.ALL);
        SDK.addHandler(handler);
        SDK.setLevel(java.util.logging.Level.ALL);
        SDK.setUseParentHandlers(false);
        installed = true;
    }
}
