package ecmwf.ecpds.master.weather;

import java.io.Serializable;
import java.util.*;
import ecmwf.ecpds.master.weather.WeatherObservation.Path;

/** Typed, credential-free approved diagnostics; never an arbitrary command. */
public final class WeatherProbe {
    private WeatherProbe() {
    }

    public enum Mode {
        TCP_CONNECT, MTR_ICMP, MTR_TCP
    }

    public enum Outcome {
        CONNECTED, RESPONSE, NO_RESPONSE, DNS_FAILURE, TOOL_UNAVAILABLE, PRIVILEGE_DENIED, DEADLINE, MALFORMED,
        SOURCE_UNAVAILABLE, ERROR
    }

    public record Policy(Path path, int port, Mode mode, int intervalSeconds) implements Serializable {
        public Policy {
            Objects.requireNonNull(path);
            Objects.requireNonNull(mode);
            if (port < 1 || port > 65535 || intervalSeconds < (mode == Mode.TCP_CONNECT ? 60 : 300)
                    || intervalSeconds > 86400 || path.configuredTarget().length() > 253
                    || !path.configuredTarget().matches("[a-zA-Z0-9._:-]+")) {
                throw new IllegalArgumentException(
                        "Probe requires a configured hostname, explicit approved service port and bounded interval");
            }
        }
    }

    public record Job(UUID id, UUID masterEpoch, Policy policy, long issuedAt, long expiresAt) implements Serializable {
        public Job {
            Objects.requireNonNull(id);
            Objects.requireNonNull(masterEpoch);
            Objects.requireNonNull(policy);
            if (issuedAt <= 0 || expiresAt <= issuedAt || expiresAt - issuedAt > 60_000)
                throw new IllegalArgumentException("Invalid probe lease");
        }
    }

    public record Hop(int number, String address, Double lossPercent, Double rttMs, Integer sent, Integer received)
            implements Serializable {
        public Hop {
            if (number < 1 || number > 32 || address == null || address.length() > 64
                    || !address.matches("[0-9a-fA-F:.?]+")
                    || lossPercent != null && (!Double.isFinite(lossPercent) || lossPercent < 0 || lossPercent > 100)
                    || rttMs != null && (!Double.isFinite(rttMs) || rttMs < 0) || sent != null && (sent < 0 || sent > 3)
                    || received != null && (sent == null || received < 0 || received > sent))
                throw new IllegalArgumentException("Invalid bounded MTR hop");
        }
    }

    public record Result(UUID jobId, UUID sourceEpoch, String sourceId, Outcome outcome, long startedAt,
            long finishedAt, String endpoint, String addressFamily, Double latencyMs, List<Hop> hops)
            implements Serializable {
        public Result {
            Objects.requireNonNull(jobId);
            Objects.requireNonNull(sourceEpoch);
            Objects.requireNonNull(outcome);
            WeatherCapabilities.validateSource(sourceId);
            hops = List.copyOf(hops);
            if (startedAt <= 0 || finishedAt < startedAt || finishedAt - startedAt > 60_000 || hops.size() > 32
                    || endpoint == null || endpoint.length() > 64 || !endpoint.matches("[0-9a-fA-F:.]*")
                    || !Set.of("IPv4", "IPv6", "UNKNOWN").contains(addressFamily)
                    || latencyMs != null && (!Double.isFinite(latencyMs) || latencyMs < 0))
                throw new IllegalArgumentException("Invalid probe result");
        }
    }
}
