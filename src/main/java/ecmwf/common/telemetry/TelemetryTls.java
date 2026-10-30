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
package ecmwf.common.telemetry;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;

import ecmwf.common.technical.Cnf;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;

/** Exporter-local certificate verification opt-out; never changes JVM-wide HTTPS defaults. */
public final class TelemetryTls {
    private static final AtomicBoolean WARNED = new AtomicBoolean();
    private static final X509TrustManager UNVERIFIED = new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
        }
    };

    private TelemetryTls() {
    }

    public static <T> T configure(final T exporter, final ConfigProperties properties) {
        final var setting = Cnf.at("OpenTelemetry", "tlsVerify", "yes").trim();
        if ("yes".equalsIgnoreCase(setting) || "true".equalsIgnoreCase(setting)) {
            return exporter;
        }
        if (!"no".equalsIgnoreCase(setting) && !"false".equalsIgnoreCase(setting)) {
            throw new IllegalArgumentException("OpenTelemetry tlsVerify must be yes/true or no/false");
        }
        // Replacing the SSL context must not silently remove configured client authentication.
        for (final var signal : new String[] { "", "logs.", "traces.", "metrics." }) {
            if (properties.getString("otel.exporter.otlp." + signal + "client.key") != null
                    || properties.getString("otel.exporter.otlp." + signal + "client.certificate") != null) {
                throw new IllegalArgumentException(
                        "OpenTelemetry tlsVerify=no cannot be combined with client TLS credentials");
            }
        }
        try {
            final var ssl = SSLContext.getInstance("TLS");
            ssl.init(null, new TrustManager[] { UNVERIFIED }, null);
            final Object replacement;
            if (exporter instanceof OtlpHttpLogRecordExporter logs) {
                replacement = logs.toBuilder().setSslContext(ssl, UNVERIFIED).build();
                logs.shutdown();
            } else if (exporter instanceof OtlpHttpSpanExporter spans) {
                replacement = spans.toBuilder().setSslContext(ssl, UNVERIFIED).build();
                spans.shutdown();
            } else if (exporter instanceof OtlpHttpMetricExporter metrics) {
                replacement = metrics.toBuilder().setSslContext(ssl, UNVERIFIED).build();
                metrics.shutdown();
            } else if (exporter instanceof OtlpGrpcLogRecordExporter logs) {
                replacement = logs.toBuilder().setSslContext(ssl, UNVERIFIED).build();
                logs.shutdown();
            } else if (exporter instanceof OtlpGrpcSpanExporter spans) {
                replacement = spans.toBuilder().setSslContext(ssl, UNVERIFIED).build();
                spans.shutdown();
            } else if (exporter instanceof OtlpGrpcMetricExporter metrics) {
                replacement = metrics.toBuilder().setSslContext(ssl, UNVERIFIED).build();
                metrics.shutdown();
            } else {
                throw new IllegalArgumentException("OpenTelemetry tlsVerify=no requires an OTLP exporter");
            }
            if (WARNED.compareAndSet(false, true)) {
                LogManager.getLogger(TelemetryTls.class)
                        .warn("OpenTelemetry TLS certificate validation is DISABLED (tlsVerify=no); "
                                + "server impersonation can expose authorization tokens and telemetry. "
                                + "Hostname checks remain enabled. Use a trusted CA instead.");
            }
            @SuppressWarnings("unchecked")
            final var result = (T) replacement;
            return result;
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException("Cannot configure OpenTelemetry TLS", e);
        }
    }
}
