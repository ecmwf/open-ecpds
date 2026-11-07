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

package ecmwf.common.ectrans.notification;

import java.io.IOException;
import java.util.regex.Pattern;

/** Backwards-compatible listing envelope; structured attribution is present only for selected profiles. */
public record AcquisitionListing(String line, String body, String message, AcquisitionNotification notification) {

    private static final Pattern SUFFIX = Pattern.compile("^(.*)\\s\\[(.+)]$");

    public static String format(final String line, final String body, final String message,
            final AcquisitionNotification notification) throws IOException {
        // Base64 cannot contain "~"; distinguish an empty inline body from no body for profiles.
        final var content = notification != null && body != null && body.isEmpty() ? "~" : body == null ? "" : body;
        final var payload = message == null ? "" : message;
        if (notification == null && content.isEmpty() && payload.isEmpty())
            return line;
        return line + " [" + content + ";" + payload + (notification == null ? "" : ";n1:" + notification.encode())
                + "]";
    }

    public static AcquisitionListing parse(final String line) throws IOException {
        final var matcher = SUFFIX.matcher(line);
        if (!matcher.matches())
            return new AcquisitionListing(line, null, null, null);
        final var content = matcher.group(2);
        final var fields = content.split(";", 3);
        if (fields.length == 3) {
            if (!fields[2].startsWith("n1:"))
                throw new IOException("Unsupported acquisition notification envelope");
            final var notification = AcquisitionNotification.decode(fields[2].substring(3));
            return new AcquisitionListing(matcher.group(1),
                    "~".equals(fields[0]) ? "" : fields[0].isEmpty() ? null : fields[0], fields[1], notification);
        }
        return new AcquisitionListing(matcher.group(1), fields.length == 2 && !fields[0].isEmpty() ? fields[0] : null,
                fields.length == 2 ? fields[1] : content, null);
    }
}
