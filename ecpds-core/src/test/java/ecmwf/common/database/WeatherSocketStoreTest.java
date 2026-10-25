package ecmwf.common.database;

import java.sql.DriverManager;
import java.sql.SQLException;

/** Run against a fresh development schema, including lower_case_table_names=0 and 1. */
public final class WeatherSocketStoreTest {
    public static void main(final String[] args) throws Exception {
        try (final var connection = DriverManager.getConnection(args[0], args[1],
                System.getenv("ECPDS_TEST_DATABASE_PASSWORD"))) {
            WeatherSocketStore.checkSchema(connection);
            WeatherSocketStore.checkConfigurationCapacity(connection, 65536);
            final var catalog = connection.getCatalog();
            try {
                connection.setCatalog("information_schema");
                try {
                    WeatherSocketStore.checkSchema(connection);
                    throw new AssertionError("Missing Network Weather tables must fail the capability check");
                } catch (final SQLException expected) {
                    if (!expected.getMessage().contains("schema is missing")) {
                        throw expected;
                    }
                }
            } finally {
                connection.setCatalog(catalog);
            }
            System.out.println("PASS: fresh schema accepted; missing schema rejected");
        }
    }
}
