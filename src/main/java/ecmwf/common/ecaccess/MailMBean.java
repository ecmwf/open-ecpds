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

package ecmwf.common.ecaccess;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;

/**
 * Manages sending and receiving emails through SMTP/IMAP/POP protocols.
 */
public class MailMBean extends MBeanRepository<MailMessage> {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MailMBean.class);

    /** The Constant DATA_ORDER. */
    private static final Comparator<Message> DATA_ORDER = (o1, o2) -> {
        try {
            return o1.getSentDate().compareTo(o2.getSentDate());
        } catch (final MessagingException _) {
            return 0;
        }
    };

    /** SMTP host (sending) */
    private String smtpHost;

    /** Store host (IMAP / POP) */
    private String storeHost;

    /** The mail user. */
    private String mailUser;

    /** The mail password. */
    private char[] mailPassword;

    /** The mail store type. */
    private String mailStoreType;

    /** The folder name. */
    private String folderName;

    /** The store. */
    private volatile Store store;

    /** The folder. */
    private volatile Folder folder;

    /** The session. */
    private Session session;

    /**
     * Constructs a MailMBean with the specified name.
     *
     * @param name
     *            the MBean name
     */
    public MailMBean(final String name) {
        super(name);
    }

    /**
     * Constructs a MailMBean with the specified group and name.
     *
     * @param group
     *            the MBean group
     * @param name
     *            the MBean name
     */
    public MailMBean(final String group, final String name) {
        super(group, name);
    }

    /**
     * Returns a string representation of the mail message status.
     *
     * @param message
     *            the mail message
     *
     * @return the status string
     */
    @Override
    public String getStatus(final MailMessage message) {
        return "[" + message.getTo() + "][" + message.getSubject() + "]";
    }

    /**
     * Returns the list of mail interfaces.
     *
     * @return array of mail interfaces
     */
    public MailInterface[] getMailInterfaces() {
        return new MailInterface[0]; // placeholder
    }

    /**
     * Sends an email without CC or attachment.
     *
     * @param from
     *            the sender
     * @param to
     *            the recipient(s)
     * @param subject
     *            the subject
     * @param content
     *            the message content
     */
    public void sendMail(final String from, final String to, final String subject, final String content) {
        sendMail(from, to, null, subject, content, null, null);
    }

    /**
     * Sends an email with optional CC and attachment.
     *
     * @param from
     *            the sender
     * @param to
     *            the recipient(s)
     * @param cc
     *            the CC recipient(s)
     * @param subject
     *            the subject
     * @param content
     *            the message content
     * @param attachmentName
     *            the attachment name
     * @param attachmentContent
     *            the attachment content
     */
    public void sendMail(final String from, final String to, final String cc, final String subject,
            final String content, final String attachmentName, final String attachmentContent) {
        if (to != null && !to.isBlank()) {
            _log.debug("Mail to {}: {}", to, subject);
            Arrays.stream(to.split("[,;]")).map(String::trim).filter(s -> !s.isEmpty()).forEach(recipient -> put(
                    new MailMessage(from, recipient, cc, subject, content, attachmentName, attachmentContent)));
        } else {
            _log.warn("Invalid address (mail not sent): to='{}' - subject='{}'", to, subject);
        }
    }

    /**
     * Initializes the mail subsystem by configuring and creating a JavaMail {@link Session} based on the application's
     * configuration.
     * <p>
     * This method sets up:
     * <ul>
     * <li><b>SMTP (sending)</b> — always enabled and configured using the "Mail.smtpHost", "Mail.port", "Mail.auth",
     * and "Mail.starttls" settings.</li>
     * <li><b>Mail store (receiving)</b> — optionally enabled when both "Mail.storeHost" and "Mail.storeType" are
     * provided. Supported store protocols include <code>imap</code>, <code>imaps</code>, <code>pop3</code>, and
     * <code>pop3s</code>.</li>
     * </ul>
     *
     * If no valid store configuration is present, the system operates in <b>send‑only mode</b>, and no IMAP/POP
     * connection will be attempted.
     * <p>
     * All SSL/TLS settings for both SMTP and the mail store are applied using the corresponding configuration keys. A
     * dedicated {@link javax.mail.Authenticator} is installed to supply credentials for both sending and receiving
     * actions.
     * <p>
     * This method is thread‑safe and will initialize the session only once.
     *
     * <h3>Configuration keys used:</h3>
     * <ul>
     * <li><b>Mail.smtpHost</b> – SMTP server hostname</li>
     * <li><b>Mail.port</b> – SMTP port</li>
     * <li><b>Mail.auth</b> – Enable SMTP authentication</li>
     * <li><b>Mail.starttls</b> – Enable SMTP STARTTLS</li>
     * <li><b>Mail.storeHost</b> – IMAP/POP server hostname (optional)</li>
     * <li><b>Mail.storeType</b> – One of: imap, imaps, pop3, pop3s (optional)</li>
     * <li><b>Mail.storePort</b> – Store port (optional, defaults per protocol)</li>
     * <li><b>Mail.user</b> – Username for both SMTP and store</li>
     * <li><b>Mail.password</b> – Password for authentication</li>
     * <li><b>Mail.folderName</b> – Folder to open when using a store (default: INBOX)</li>
     * <li><b>Mail.debug</b> – Enable JavaMail debug output</li>
     * </ul>
     *
     * <h3>Thread-safety</h3> The method is synchronized and will skip initialization if a session already exists,
     * ensuring that the mail system is configured exactly once.
     *
     * <h3>Logging</h3> A summary of the effective configuration is logged, with credentials safely masked.
     */
    @Override
    public synchronized void initialize() {
        if (session != null) {
            return; // already initialized
        }
        _log.info("Initializing Mail session");
        final var props = new Properties();
        // ------------------------------------------------------------------
        // 1. Load Configuration
        // ------------------------------------------------------------------
        final var defaultHost = Cnf.at("Mail", "host", "");
        smtpHost = Cnf.at("Mail", "smtpHost", defaultHost);
        storeHost = Cnf.at("Mail", "storeHost", ""); // empty = no store
        mailUser = Cnf.at("Mail", "user", "");
        mailPassword = Cnf.at("Mail", "password", "").toCharArray();
        folderName = Cnf.at("Mail", "folderName", "INBOX");
        // NOTE: NEW — storeType default = "" (send‑only)
        mailStoreType = Cnf.at("Mail", "storeType", "").toLowerCase().trim();
        final var debug = Cnf.at("Mail", "debug", false);
        // ------------------------------------------------------------------
        // 2. SMTP Configuration (Always Enabled)
        // ------------------------------------------------------------------
        final var smtpPort = Cnf.at("Mail", "port", 25);
        final var smtpTLS = Cnf.at("Mail", "starttls", true);
        final var smtpAuth = Cnf.at("Mail", "auth", true);
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.auth", String.valueOf(smtpAuth));
        props.put("mail.smtp.starttls.enable", String.valueOf(smtpTLS));
        props.put("mail.smtp.starttls.required", String.valueOf(smtpTLS));
        // ------------------------------------------------------------------
        // 3. Store Configuration (Optional)
        // ------------------------------------------------------------------
        final var storeConfigured = storeHost != null && !storeHost.isBlank() && mailStoreType != null
                && !mailStoreType.isBlank();
        if (storeConfigured) {
            props.put("mail.store.protocol", mailStoreType);
            props.put("mail." + mailStoreType + ".host", storeHost);
            // Determine store port
            var storePort = Cnf.at("Mail", "storePort", -1);
            if (storePort == -1) {
                storePort = switch (mailStoreType) {
                case "imaps" -> 993;
                case "imap" -> 143;
                case "pop3s" -> 995;
                case "pop3" -> 110;
                default -> 993; // safe default
                };
            }
            props.put("mail." + mailStoreType + ".port", String.valueOf(storePort));
            // TLS configuration for store
            final var tlsRequired = Cnf.at("Mail", "tls", true);
            if (mailStoreType.endsWith("s")) {
                // implicit SSL
                props.put("mail." + mailStoreType + ".ssl.enable", "true");
            } else {
                // explicit STARTTLS
                props.put("mail." + mailStoreType + ".starttls.enable", "true");
                props.put("mail." + mailStoreType + ".starttls.required", String.valueOf(tlsRequired));
            }
        } else {
            _log.info("Mail store disabled (send‑only mode)");
        }
        // ------------------------------------------------------------------
        // 4. Create Session
        // ------------------------------------------------------------------
        session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUser, new String(mailPassword));
            }
        });
        session.setDebug(debug);
        // ------------------------------------------------------------------
        // 5. Log Configuration Summary (Safe)
        // ------------------------------------------------------------------
        // mask all but last 2 chars
        final var userMasked = mailUser.isEmpty() ? "<empty>" : mailUser.replaceAll("(?=..).", "*");
        _log.info(
                "Mail configuration:\n" + "  SMTP: host={}, port={}, tls={}, auth={}\n"
                        + "  Store: type='{}', host='{}'{}\n" + "  Mail user: {}\n" + "  Debug: {}",
                smtpHost, smtpPort, smtpTLS, smtpAuth, storeConfigured ? mailStoreType : "<disabled>",
                storeConfigured ? storeHost : "", storeConfigured ? "" : "", userMasked, debug);
    }

    /**
     * Executes the next step in the MBean workflow: connect, send, receive.
     *
     * @return delay until next step
     */
    @Override
    public int nextStep() {
        try {
            connect();
            sendMessages();
            if (!mailStoreType.startsWith("smtp")) {
                getMessages();
            }
        } catch (final MessagingException e) {
            _log.error("Mail connection error", e);
            closesFolderAndStore();
        } catch (final Exception t) {
            _log.error("sendMessages/getMessages error", t);
            closesFolderAndStore();
        }
        return NEXT_STEP_DELAY;
    }

    /**
     * Closes the folder and store safely.
     */
    private synchronized void closesFolderAndStore() {
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(true);
            }
            if (store != null) {
                store.close();
            }
        } catch (final Exception e) {
            _log.debug("Error closing folder/store", e);
        } finally {
            folder = null;
            store = null;
        }
    }

    /**
     * Shuts down the MBean and optionally sends pending messages.
     *
     * @param graceful
     *            if true, send pending messages before shutdown
     */
    public void shutdown(final boolean graceful) {
        super.shutdown();
        if (graceful) {
            try {
                connect();
                sendMessages();
            } catch (final Exception e) {
                _log.warn("Shutdown mail send failed", e);
            }
        }
    }

    /**
     * Shutdown.
     */
    @Override
    public void shutdown() {
        shutdown(true);
    }

    /**
     * Sends a single message via SMTP.
     *
     * @param from
     *            the from
     * @param to
     *            the to
     * @param cc
     *            the cc
     * @param subject
     *            the subject
     * @param content
     *            the content
     * @param attachmentName
     *            the attachment name
     * @param attachmentContent
     *            the attachment content
     */
    private void sendMessage(final String from, final String to, final String cc, final String subject,
            final String content, final String attachmentName, final String attachmentContent) {
        if (to == null || to.isBlank()) {
            _log.warn("Invalid address: null or empty (mail not sent)");
            return;
        }
        try {
            _log.debug("Sending from={} to={} cc={} subject={} attachment={}", from, to, cc, subject, attachmentName);
            final var message = new MimeMessage(session);
            message.setFrom(new InternetAddress(
                    from != null && !from.isBlank() ? from : Cnf.at("Mail", "defaultFrom", mailUser)));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            if (cc != null && !cc.isBlank()) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
            }
            message.setSubject(subject);
            if (attachmentName != null && !attachmentName.isBlank()) {
                final var textPart = new MimeBodyPart();
                textPart.setText(content, "utf-8");
                final var filePart = new MimeBodyPart();
                filePart.setText(attachmentContent, "utf-8");
                filePart.setFileName(attachmentName);
                final Multipart multipart = new MimeMultipart("mixed"); // changed from "alternative"
                multipart.addBodyPart(textPart);
                multipart.addBodyPart(filePart);
                message.setContent(multipart);
            } else {
                message.setText(content);
            }
            message.saveChanges();
            Transport.send(message, message.getAllRecipients());
            _log.debug("Mail sent successfully to {}", to);
        } catch (final AddressException | SendFailedException e) {
            _log.warn("Invalid address: {} (mail not sent)", to, e);
        } catch (final MessagingException e) {
            _log.error("MessagingException while sending mail to {}: {}", to, e.getMessage(), e);
        } catch (final Exception e) {
            _log.error("Unexpected exception while sending mail to {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Orders messages by sent date.
     *
     * @param messages
     *            array of messages
     *
     * @return sorted array
     */
    private Message[] orderByDate(final Message[] messages) {
        Arrays.sort(messages, DATA_ORDER);
        return messages;
    }

    /**
     * Establishes a connection to the configured mail store (IMAP or POP) and opens the target folder for message
     * retrieval.
     * <p>
     * This method is invoked automatically by the mail-processing workflow and is responsible only for the receiving
     * side of the mail subsystem. SMTP sending is handled independently and is always available.
     *
     * <h3>Behavior</h3>
     * <ul>
     * <li>If a store connection is already established, the method returns immediately.</li>
     * <li>If no store host or store type is configured (send‑only mode), the method logs this and returns without
     * attempting any connection.</li>
     * <li>Otherwise, the method connects to the store using the protocol specified by {@code mailStoreType} (e.g.
     * {@code imap}, {@code imaps}, {@code pop3}, {@code pop3s}).</li>
     * <li>After a successful store login, the configured folder (default: {@code INBOX}) is opened in
     * {@link Folder#READ_WRITE} mode.</li>
     * </ul>
     *
     * <h3>Configuration Requirements</h3> The following properties must be set during {@link #initialize()} in order
     * for store connectivity to be enabled:
     * <ul>
     * <li>{@code Mail.storeHost} – hostname of the IMAP/POP server</li>
     * <li>{@code Mail.storeType} – store protocol (imap, imaps, pop3, pop3s)</li>
     * </ul>
     * If these properties are missing or empty, the system operates in <b>send‑only mode</b> and this method becomes a
     * no‑op.
     *
     * <h3>Exceptions</h3>
     * <ul>
     * <li>{@link NoSuchProviderException} if the configured store protocol is unsupported or misspelled.</li>
     * <li>{@link AuthenticationFailedException} if credentials are rejected by the mail store.</li>
     * <li>{@link MessagingException} for any other connection or folder‑access errors.</li>
     * </ul>
     *
     * <h3>Thread Safety</h3> The method is synchronized to ensure that the store and folder are opened at most once and
     * to prevent connection races.
     *
     * @throws MessagingException
     *             if the connection or folder opening fails
     */
    private synchronized void connect() throws MessagingException {
        // Already connected?
        if (store != null) {
            _log.debug("Mail store already connected");
            return;
        }
        // No store configured (send-only mode)?
        if (storeHost == null || storeHost.isBlank() || mailStoreType == null || mailStoreType.isBlank()) {
            _log.debug("Mail store not configured — operating in send‑only mode");
            return;
        }
        _log.debug("Connecting to mail store '{}', protocol='{}'", storeHost, mailStoreType);
        try {
            store = session.getStore(mailStoreType);
            store.connect(storeHost, mailUser, new String(mailPassword));
            folder = store.getFolder(folderName);
            folder.open(Folder.READ_WRITE);
            _log.info("Connected to mail store '{}' using {}", storeHost, mailStoreType);
        } catch (final NoSuchProviderException e) {
            _log.error("Invalid mail store provider: '{}'", mailStoreType, e);
            throw e;
        } catch (final AuthenticationFailedException e) {
            _log.error("Authentication failed connecting to mail store '{}': {}", storeHost, e.getMessage());
            throw e;
        } catch (final MessagingException e) {
            // Helpful error hint if user mis-configured the store port
            if (e.getMessage() != null && e.getMessage().contains("ESMTP")) {
                _log.error("Mail connection error: host '{}' looks like an SMTP server; "
                        + "check 'Mail.storeType' and 'Mail.storePort'", storeHost);
            }
            throw e;
        }
    }

    /**
     * Sends all pending messages.
     *
     * @throws Exception
     *             if sending fails
     */
    private void sendMessages() throws Exception {
        for (final MailMessage toSend : getList()) {
            sendMessage(toSend.getFrom(), toSend.getTo(), toSend.getCC(), toSend.getSubject(), toSend.getContent(),
                    toSend.getAttachmentName(), toSend.getAttachmentContent());
            removeValue(toSend);
        }
    }

    /**
     * Receives messages from the configured interfaces and processes them.
     *
     * @return the messages
     *
     * @throws MessagingException
     *             if message retrieval fails
     */
    private void getMessages() throws MessagingException {
        final var interfaces = getMailInterfaces();
        for (final MailInterface element : interfaces) {
            folder.expunge();
            final var messages = orderByDate(folder.search(element.getSearchTerm()));
            if (messages.length > 0) {
                _log.debug("Receiving {} mail(s)", messages.length);
                final var content = new StringBuilder();
                for (final Message notification : messages) {
                    if (notification.getFlags().contains(Flags.Flag.DELETED))
                        continue;
                    content.setLength(0);
                    var delete = false;
                    try (var bao = new ByteArrayOutputStream()) {
                        notification.getDataHandler().writeTo(bao);
                        content.append(bao.toString());
                        delete = element.received(notification, content.toString());
                    } catch (final Exception e) {
                        delete = true;
                        _log.warn("Message deleted: '{}'", content, e);
                    } finally {
                        if (delete && folder.isOpen()) {
                            notification.setFlag(Flags.Flag.DELETED, true);
                        }
                    }
                }
            }
        }
    }
}
