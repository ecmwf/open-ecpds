package ecmwf.ecpds.master.weather;

import java.io.Serializable;
import java.util.List;

/** Deliberately excludes raw diagnostics, transfer IDs, endpoint addresses and administrative errors. */
public record WeatherViewer(boolean enabled, long measuredAt, List<Path> paths) implements Serializable {

    public WeatherViewer {
        paths = List.copyOf(paths);
    }

    public record Path(String source, String hostId, String protocol, long successes, long failures, long cancellations,
            long unknown, long sockets, long lastObserved, Double rttMs, Double latitude, Double longitude,
            String probeMode, String probeOutcome, long probedAt, Double probeLatencyMs, String leg)
            implements Serializable {
        public Path(String source, String hostId, String protocol, long successes, long failures, long cancellations,
                long unknown, long sockets, long lastObserved, Double rttMs, Double latitude, Double longitude,
                String leg) {
            this(source, hostId, protocol, successes, failures, cancellations, unknown, sockets, lastObserved, rttMs,
                    latitude, longitude, null, null, 0, null, leg);
        }
    }
}
