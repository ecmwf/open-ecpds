/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package ecmwf.common.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Standalone regression checks using the core classes and lib/* classpath. */
public final class DeliveredNameCheck {
    private static void check(final boolean condition, final String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(final String[] args) throws Exception {
        final var transfer = new DataTransfer();
        transfer.setId(1);
        transfer.setDataFileId(2);
        transfer.setTarget("file.dat");
        check(transfer.getDeliveredName() == null, "New transfer must have no delivered name");
        for (final String name : new String[] { null, "", " ", "file.dat" }) {
            transfer.recordDeliveredName("old/name");
            transfer.recordDeliveredName(name);
            check(transfer.getDeliveredName() == null, "Missing/equal name retained stale value");
        }
        transfer.recordDeliveredName("FILE.dat");
        check("FILE.dat".equals(transfer.getDeliveredName()), "Comparison is not case sensitive");
        final var name = "folder/".repeat(100) + "<file&name>.dat";
        transfer.recordDeliveredName(name);
        check(name.equals(transfer.getDeliveredName()), "Long delivered name was truncated");
        check("file.dat".equals(transfer.getTarget()), "Requested target was changed");
        check(name.equals(((DataTransfer) transfer.clone()).getDeliveredName()), "Clone lost delivered name");

        final var bytes = new ByteArrayOutputStream();
        try (var output = new ObjectOutputStream(bytes)) {
            output.writeObject(transfer);
        }
        try (var input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            check(name.equals(((DataTransfer) input.readObject()).getDeliveredName()), "RMI serialization lost name");
        }
        final var mapper = new ObjectMapper();
        final var decoded = mapper.readValue(mapper.writeValueAsString(transfer), DataTransfer.class);
        check(name.equals(decoded.getDeliveredName()), "REST JSON lost delivered name");
        transfer.setDeliveredName(null);
        check(transfer.getDeliveredName() == null, "New-attempt reset failed");
        check(mapper.readValue(mapper.writeValueAsString(transfer), DataTransfer.class).getDeliveredName() == null,
                "REST JSON did not preserve reset");

        final var filter = new SQLParameterParser("target=*.dat delivered=\"folder/*O'Brien?.dat\" case=i",
                "target", "delivered");
        final var sql = filter.get(0, "DAT_TARGET") + filter.get("delivered", "DAT_DELIVERED_NAME");
        check(sql.contains("DAT_TARGET") && sql.contains("DAT_DELIVERED_NAME"), "Combined filter lost a field");
        check(sql.contains("%O\\'Brien_.dat") && sql.contains("COLLATE latin1_general_ci"),
                "Wildcard conversion, case handling or SQL escaping failed: " + sql);
        final var statusFilter = new SQLParameterParser("delivered=folder/*", "target", "method", "delivered");
        check(statusFilter.get("delivered", "DAT_DELIVERED_NAME").contains("folder/%"),
                "Named lookup depends on absent method");
        System.out.println("Delivered name checks passed");
    }
}
