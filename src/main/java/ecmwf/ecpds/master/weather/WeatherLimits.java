package ecmwf.ecpds.master.weather;

import java.io.Serializable;
import java.util.Set;

import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.weather.WeatherObservation.Path;

/** Deployment limits are owned by the Master and included in administrator snapshots. */
public record WeatherLimits(int maxSources, int maxSelectedPaths, int maxCandidates, int maxProbePolicies,
        int maxHostChoices, int maxConfigurationBytes) implements Serializable {
    public WeatherLimits {
        if (maxSources < 1 || maxSelectedPaths < 1 || maxCandidates < maxSelectedPaths || maxProbePolicies < 1
                || maxHostChoices < 1 || maxCandidates == Integer.MAX_VALUE || maxConfigurationBytes < 1
                || maxConfigurationBytes > 16_777_215) {
            throw new IllegalArgumentException("Invalid NetworkWeather deployment limits");
        }
    }

    public static WeatherLimits configured() {
        return new WeatherLimits(setting("maxSources", 20), setting("maxSelectedPaths", 50),
                setting("maxCandidates", 500), setting("maxProbePolicies", 50), setting("maxHostChoices", 5000),
                setting("maxConfigurationBytes", 49152));
    }

    private static int setting(String name, int fallback) {
        try {
            return Integer.parseInt(Cnf.at("NetworkWeather", name, Integer.toString(fallback)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("NetworkWeather." + name + " must be a positive integer", e);
        }
    }

    public static int controlResponseBytes() {
        final int maximum = setting("maxControlResponseBytes", 65536);
        if (maximum < 1)
            throw new IllegalArgumentException("NetworkWeather.maxControlResponseBytes must be positive");
        return maximum;
    }

    public void validateSelection(Set<Path> paths) {
        if (paths.size() > maxSelectedPaths || paths.stream().map(Path::sourceId).distinct().count() > maxSources) {
            throw new IllegalArgumentException(
                    "At most " + maxSelectedPaths + " selected paths across " + maxSources + " sources are allowed");
        }
    }

    public void validatePolicy(WeatherAutomation.Configuration policy) {
        checkCount("Approved candidates", policy.approved().size(), maxCandidates);
        checkCount("Pinned paths", policy.pins().size(), maxSelectedPaths);
        checkCount("Source / transfer group candidates", policy.sourceGroups().size(), maxSources);
        checkCount("Probe policies", policy.probePolicies().size(), maxProbePolicies);
        checkCount("Distinct sources across Approved candidates and Source / transfer group candidates",
                java.util.stream.Stream
                        .concat(policy.approved().stream().map(Path::sourceId), policy.sourceGroups().keySet().stream())
                        .distinct().count(),
                maxSources);
    }

    private static void checkCount(String section, long count, int maximum) {
        if (count > maximum)
            throw new IllegalArgumentException(
                    section + ": " + count + " entries exceed the configured limit of " + maximum);
    }
}
