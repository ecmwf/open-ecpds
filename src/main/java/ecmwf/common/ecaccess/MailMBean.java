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

    /** The mail host. */
    private String mailHost;

    /** The mail user. */
    private String mailUser;

    /** The mail password. */
    private String mailPassword;

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
            _log.warn("Invalid address: null or empty (mail not sent)");
        }
    }

    /**
     * Initializes the mail session and POP/IMAP configuration.
     */
    @Override
    public synchronized void initialize() {
        if (session == null) {
            _log.info("Initializing Mail session");
            final var props = new Properties();
            // --- Common settings ---
            mailHost = Cnf.at("Mail", "host", "");
            mailUser = Cnf.at("Mail", "user", "");
            mailPassword = Cnf.at("Mail", "password", "");
            mailStoreType = Cnf.at("Mail", "storeType", "smtp").toLowerCase();
            folderName = Cnf.at("Mail", "folderName", "INBOX");
            // --- Determine default ports ---
            final var defaultSmtpPort = 25;
            final var defaultImapPort = 143;
            final var defaultImapsPort = 993;
            final var defaultPopPort = 110;
            final var defaultPopSslPort = 995;
            // --- SMTP properties (always safe to define) ---
            var smtpPort = Cnf.at("Mail", "port", "");
            if (smtpPort.isBlank()) {
                smtpPort = "smtp".equals(mailStoreType) ? String.valueOf(defaultSmtpPort) : "25";
            }
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.host", mailHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.starttls.enable", Cnf.at("Mail", "starttls", "true"));
            props.put("mail.smtp.auth", Cnf.at("Mail", "auth", "true"));
            // Optional: IMAP/POP properties for receiving mail
            if (!mailStoreType.startsWith("smtp")) {
                props.put("mail.store.protocol", mailStoreType);
                props.put("mail." + mailStoreType + ".host", mailHost);
                // Determine default store port if not provided
                var storePort = Cnf.at("Mail", "storePort", "");
                if (storePort.isBlank()) {
                    if (mailStoreType.startsWith("imap")) {
                        storePort = mailStoreType.equals("imaps") ? String.valueOf(defaultImapsPort)
                                : String.valueOf(defaultImapPort);
                    } else if (mailStoreType.startsWith("pop")) {
                        storePort = mailStoreType.equals("pops") ? String.valueOf(defaultPopSslPort)
                                : String.valueOf(defaultPopPort);
                    } else {
                        storePort = "993"; // fallback safe default
                    }
                }
                props.put("mail." + mailStoreType + ".port", storePort);
                props.put("mail." + mailStoreType + ".ssl.enable", Cnf.at("Mail", "ssl", "true"));
            }
            // Enable debug if configured
            final var debug = Cnf.at("Mail", "debug", false);
            // Create authenticated session
            session = Session.getInstance(props, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailUser, mailPassword);
                }
            });
            session.setDebug(debug);
            _log.info("Mail session initialized (protocol=" + mailStoreType + ", debug=" + debug + ")");
        }
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
     * Connects to the mail store and folder if not already connected.
     *
     * @throws MessagingException
     *             if connection fails
     */
    /**
     * Connects to the configured mail store (IMAP/POP) if applicable.
     * <p>
     * If the configuration indicates an SMTP-only setup (e.g. host name contains "smtp" or the protocol is "smtp"),
     * this method will simply log that no store connection is required and return immediately.
     *
     * @throws MessagingException
     *             if the connection to the mail store fails
     */
    private synchronized void connect() throws MessagingException {
        if (store != null) {
            _log.debug("Mail store already connected");
            return;
        }
        if (mailHost == null || mailHost.isEmpty()) {
            _log.debug("No mailHost configured — skipping mail store connection");
            return;
        }
        // Determine the store type (imap, imaps, pop3, pop3s, or smtp)
        // Detect SMTP-only configurations and skip connecting
        if (mailStoreType.startsWith("smtp")) {
            _log.debug("Detected SMTP-only configuration for host '{}'; skipping store connection", mailHost);
            return;
        }
        _log.debug("Connecting to mail store '{}', protocol='{}'", mailHost, mailStoreType);
        try {
            store = session.getStore(mailStoreType);
            store.connect(mailHost, mailUser, mailPassword);
            folder = store.getFolder(folderName); // <- folderName is usually "INBOX"
            folder.open(Folder.READ_WRITE);
            _log.info("Connected to mail store '{}' using {}", mailHost, mailStoreType);
        } catch (final NoSuchProviderException e) {
            _log.error("Invalid mail store provider: '{}'", mailStoreType, e);
            throw e;
        } catch (final AuthenticationFailedException e) {
            _log.error("Authentication failed connecting to mail store '{}': {}", mailHost, e.getMessage());
            throw e;
        } catch (final MessagingException e) {
            // Common mistake: connecting IMAP to an SMTP port
            if (e.getMessage() != null && e.getMessage().contains("ESMTP")) {
                _log.error("Mail connection error: host '{}' appears to be an SMTP server; "
                        + "use SMTP-only mode or correct mail.store.protocol", mailHost);
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
