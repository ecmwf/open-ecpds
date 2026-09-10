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

package ecmwf.ecpds.master.plugin.http.controller.monitoring;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Holds the built-in default text and the encoding logic for the two configurable product-status notification messages
 * ("Products Delay" and "Products Resumed") shown as pre-filled Outlook email bodies on the product monitoring page
 * ({@code product.jsp}). The actual, possibly customized, text is stored in the SYS_CONFIG database table (group
 * "ProductStatus") and edited from the Monitor UI (Admin Tasks &rarr; Product Status Messages), instead of being
 * hardcoded in the JSP page.
 *
 * <p>
 * Both messages support the {@code {{PRODUCT}}} and {@code {{CYCLE}}} placeholders, substituted at render time with the
 * product name and cycle/time currently being viewed (e.g. "GENFO" and "06" for
 * {@code /do/monitoring/summary/GENFO/06}).
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2026-09-10
 */
public final class ProductStatusMessages {

    /** SYS_CONFIG parameter name for the "Products Delay" message. */
    public static final String DELAY_MESSAGE_NAME = "productDelayMessage";

    /** SYS_CONFIG parameter name for the "Products Resumed" message. */
    public static final String RESUMED_MESSAGE_NAME = "productResumedMessage";

    /** Placeholder replaced with the product name (e.g. "GENFO") currently being viewed. */
    public static final String PRODUCT_PLACEHOLDER = "{{PRODUCT}}";

    /** Placeholder replaced with the cycle/time (e.g. "06") currently being viewed. */
    public static final String CYCLE_PLACEHOLDER = "{{CYCLE}}";

    /** Built-in default text for the "Products Delay" message (used until an administrator customizes it). */
    public static final String DEFAULT_DELAY_MESSAGE = """
            Dear colleagues,

            << Due to if known and not commercially or infrastructure sensitive, please give some information of the reason for the delay, the or otherwise The >> dissemination of ECMWF {{PRODUCT}} products for the {{CYCLE}}Z cycle will be delayed <<estimate time if possible>>.

            Our teams and partners are actively working to restore services.

            (Preferable to give a time stamp if and when appropriate, eg The next update will be at xx:xx UTC) / As soon as we have further details, we will inform you by email <<or other channel if email/system vulnerable?>>.

            For more up-to-date information, you may please refer to ECMWF service status page at http://www.ecmwf.int/en/service-status.

            Our sincere apologies for the inconvenience caused by this delay.

            Kind regards

            ECMWF Duty Manager""";

    /** Built-in default text for the "Products Resumed" message (used until an administrator customizes it). */
    public static final String DEFAULT_RESUMED_MESSAGE = """
            Dear colleagues,
            I am pleased to inform you that the issues we encountered earlier
            within the operational production for the {{PRODUCT}} {{CYCLE}}Z cycle have been resolved and the dissemination of products has started.
            Our sincere apologies for the inconvenience caused by this delay.
            Kind regards
            ECMWF Duty Manager""";

    private ProductStatusMessages() {
        // Hiding constructor!
    }

    /**
     * Replaces the {@code {{PRODUCT}}} and {@code {{CYCLE}}} placeholders, if present, with the actual product name and
     * cycle/time currently being viewed (e.g. "GENFO" and "06" for {@code /do/monitoring/summary/GENFO/06}).
     *
     * @param text
     *            the plain-text message, possibly containing placeholders
     * @param product
     *            the product name to substitute for {@link #PRODUCT_PLACEHOLDER}
     * @param cycle
     *            the cycle/time to substitute for {@link #CYCLE_PLACEHOLDER}
     *
     * @return the message with placeholders replaced
     */
    public static String substitutePlaceholders(final String text, final String product, final String cycle) {
        if (text == null) {
            return null;
        }
        return text.replace(PRODUCT_PLACEHOLDER, product != null ? product : "").replace(CYCLE_PLACEHOLDER,
                cycle != null ? cycle : "");
    }

    /**
     * Encodes a plain-text message for embedding as the {@code body} parameter of the Outlook Web deeplink URL used to
     * pre-fill the "Products Delay"/"Products Resumed" emails. Only the characters that are unsafe or meaningful in
     * that URL are percent-encoded (comma, angle brackets, double quote, colon, slash, and newlines); everything else
     * (including spaces) is left as-is, matching the encoding historically used for these two messages.
     *
     * @param text
     *            the plain-text message
     *
     * @return the encoded message, ready to be embedded in the deeplink URL
     */
    public static String encodeForEmailBody(final String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n").replace(",", "%2C").replace("<", "%3C").replace(">", "%3E")
                .replace("\"", "%22").replace(":", "%3A").replace("/", "%2F").replace("\n", "%0A");
    }
}
