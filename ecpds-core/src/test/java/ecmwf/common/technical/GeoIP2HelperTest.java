package ecmwf.common.technical;

import java.nio.file.Files;
import java.util.List;

/** Local addresses must not open or query the GeoIP database. */
public final class GeoIP2HelperTest {
    public static void main(final String[] args) throws Exception {
        final var config = Files.createTempFile("ecpds-geoip-", ".properties");
        Files.writeString(config, "[Server]\ngeoip2DataFile=" + config + ".missing\n");
        System.setProperty("ecmwf.properties", config.toString());
        try {
            for (final var address : List.of("localhost", "127.0.0.1", "127.12.34.56", "0.0.0.0", "0.1.2.3", "10.1.2.3",
                    "172.16.0.1", "172.31.255.254", "192.168.1.1", "169.254.1.1", "100.64.0.1", "100.127.255.254",
                    "224.0.0.1", "::", "::1", "fe80::1", "fc00::1", "fd12::1", "ff02::1", "::ffff:127.0.0.1",
                    "::ffff:192.168.1.1")) {
                if (GeoIP2Helper.getCityResponse(address) != null) {
                    throw new AssertionError("Unexpected GeoIP result for " + address);
                }
            }
            for (final var address : List.of("8.8.8.8", "172.15.255.255", "172.32.0.1", "192.169.0.1", "100.63.255.255",
                    "100.128.0.1", "2606:4700:4700::1111")) {
                try {
                    GeoIP2Helper.getCityResponse(address);
                    throw new AssertionError("Public address must attempt database lookup: " + address);
                } catch (final java.io.IOException expected) {
                    // The deliberately absent database proves that public lookups remain enabled.
                }
            }
            System.out.println("PASS: local GeoIP lookups skipped; public address lookup preserved");
        } finally {
            Files.delete(config);
        }
    }
}
