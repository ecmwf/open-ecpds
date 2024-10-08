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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * This class is used by the external commands running on ECPDS to gather
 * statistics. This replace the usage of curl or sendmail which are platform
 * dependent.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Scanner;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

/**
 * The Class SendMail.
 */
public class SendMail {
    /**
     * The main method. Takes the smtp server hostname as an argument and then reads from stdin to get the headers and
     * the body of the message. The body support HTML.
     *
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        try (final var sc = new Scanner(System.in)) {
            final var properties = System.getProperties();
            properties.setProperty("mail.smtp.host", args[0]);
            final var sb = new StringBuilder();
            final var message = new MimeMessage(Session.getDefaultInstance(properties));
            var contentType = "text/html; charset=us-ascii";
            var header = true;
            while (sc.hasNext()) {
                final var line = sc.nextLine();
                final int index;
                if (header && (index = line.indexOf(": ")) != -1) {
                    final var key = line.substring(0, index);
                    final var value = line.substring(index + 2);
                    if ("Content-Type".equalsIgnoreCase(key)) {
                        contentType = value;
                    } else {
                        message.addHeader(key, value);
                    }
                } else {
                    sb.append(line).append("\n");
                    header = false;
                }
            }
            message.setContent(sb.toString(), contentType);
            Transport.send(message);
        } catch (final MessagingException mex) {
            System.err.println("ERROR: " + mex.getMessage());
        }
    }
}
