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

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
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
    private String mailStoreType = "imap";

    /** The folder name. */
    private String folderName = "INBOX";

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
    public void initialize() {
        mailHost = Cnf.at("Mail", "host");
        mailUser = Cnf.at("Mail", "user");
        mailPassword = Cnf.at("Mail", "password");
        mailStoreType = Cnf.at("Mail", "storeType", mailStoreType);
        folderName = Cnf.at("Mail", "folderName", folderName);
        final var props = new Properties();
        if (mailUser != null && mailPassword != null) {
            props.put("mail.smtp.auth", "true");
            session = Session.getInstance(props, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailUser, mailPassword);
                }
            });
        } else {
            session = Session.getDefaultInstance(props, null);
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
            getMessages();
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
     *
     * @throws Exception
     *             if sending fails
     */
    private void sendMessage(final String from, final String to, final String cc, final String subject,
            final String content, final String attachmentName, final String attachmentContent) throws Exception {
        if (to == null || to.isBlank()) {
            _log.warn("Invalid address: null or empty (mail not sent)");
            return;
        }
        try {
            _log.debug("Sending from={} to={} cc={} subject={} attachment={}", from, to, cc, subject, attachmentName);
            final var message = new MimeMessage(session);
            message.setFrom(new InternetAddress(
                    from != null && !from.isBlank() ? from : Cnf.at("Mail", "defaultFrom", mailUser)));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            if (cc != null && !cc.isBlank()) {
                message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
            }
            message.setSubject(subject);
            if (attachmentName != null && !attachmentName.isBlank()) {
                final var textPart = new MimeBodyPart();
                textPart.setText(content, "utf-8");
                final var filePart = new MimeBodyPart();
                filePart.setText(attachmentContent, "utf-8");
                filePart.setFileName(attachmentName);
                final Multipart multipart = new MimeMultipart("alternative");
                multipart.addBodyPart(textPart);
                multipart.addBodyPart(filePart);
                message.setContent(multipart);
            } else {
                message.setText(content);
            }
            message.saveChanges();
            Transport.send(message);
        } catch (AddressException | SendFailedException e) {
            _log.warn("Invalid address: {} (mail not sent)", to, e);
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
    private synchronized void connect() throws MessagingException {
        if (store != null || mailHost == null || mailHost.isBlank()) {
            return;
        }
        store = session.getStore(mailStoreType);
        var host = mailHost;
        var port = -1; // default: use protocol default port
        // Check if mailHost includes a port, e.g. "imap.example.org:993"
        final var colonIndex = mailHost.lastIndexOf(':');
        if (colonIndex > 0 && colonIndex < mailHost.length() - 1) {
            final var portPart = mailHost.substring(colonIndex + 1);
            try {
                port = Integer.parseInt(portPart);
                host = mailHost.substring(0, colonIndex);
            } catch (final NumberFormatException _) {
                // not a valid port — keep host as-is, default port will be used
            }
        }
        // Connect using explicit port if provided
        if (port > 0) {
            store.connect(host, port, mailUser, mailPassword);
        } else {
            store.connect(host, mailUser, mailPassword);
        }
        folder = store.getFolder(folderName);
        folder.open(Folder.READ_WRITE);
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
