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

package ecmwf.common.parser.format;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.parser.core.ParserConfig;
import ecmwf.common.technical.ByteSize;

/** Shared size and timestamp conversion for structured listings. */
public final class ListingMetadata {
    private static final Logger LOG = LogManager.getLogger(ListingMetadata.class);

    private final BigDecimal sizeMultiplier;

    public ListingMetadata(ParserConfig config) {
        String unit = config.getOrDefault("sizeUnit", "B");
        try {
            sizeMultiplier = BigDecimal.valueOf(ByteSize.unitMultiplier(unit));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported listing sizeUnit: " + unit, e);
        }
    }

    public long size(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }
        try {
            BigDecimal size = new BigDecimal(value.trim());
            if (size.signum() < 0) {
                throw new ArithmeticException("Negative size");
            }
            BigDecimal bytes = size.multiply(sizeMultiplier);
            if (bytes.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0) {
                throw new ArithmeticException("Size overflow");
            }
            return bytes.setScale(0, RoundingMode.HALF_UP).longValueExact();
        } catch (NumberFormatException | ArithmeticException e) {
            LOG.warn("Invalid listing size '{}'; leaving size unknown", value);
            return -1;
        }
    }

    public static long time(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }
        value = value.trim();
        try {
            return Instant.parse(value).toEpochMilli();
        } catch (DateTimeParseException | ArithmeticException e) {
            // Some catalogues provide a date without a time, or epoch milliseconds.
        }
        try {
            return LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        } catch (DateTimeParseException | ArithmeticException e) {
            // Try epoch milliseconds next.
        }
        try {
            long time = Long.parseLong(value);
            if (time >= 0) {
                return time;
            }
        } catch (NumberFormatException e) {
            // Report unsupported values below.
        }
        LOG.warn("Invalid listing timestamp '{}'; leaving timestamp unknown", value);
        return -1;
    }
}
