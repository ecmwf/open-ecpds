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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
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
 * The Class MailMBean.
 */
public class MailMBean extends MBeanRepository<MailMessage> {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MailMBean.class);

    /** The Constant _dateOrder. */
    private static final Comparator<Message> _dateOrder = (o1, o2) -> {
        try {
            return o1.getSentDate().compareTo(o2.getSentDate());
        } catch (final MessagingException me) {
            return 0;
        }
    };

    /** The _host. */
    private String _host = null;

    /** The _username. */
    private String _username = null;

    /** The _password. */
    private String _password = null;

    /** The _storename. */
    private String _storename = "imap";

    /** The _foldername. */
    private String _foldername = "INBOX";

    /** The _store. */
    private Store _store = null;

    /** The _folder. */
    private Folder _folder = null;

    /** The _session. */
    private Session _session = null;

    /**
     * Instantiates a new mail m bean.
     *
     * @param name
     *            the name
     */
    public MailMBean(final String name) {
        super(name);
    }

    /**
     * Instantiates a new mail m bean.
     *
     * @param group
     *            the group
     * @param name
     *            the name
     */
    public MailMBean(final String group, final String name) {
        super(group, name);
    }

    /**
     * Gets the status.
     *
     * @param message
     *            the message
     *
     * @return the status
     */
    @Override
    public String getStatus(final MailMessage message) {
        return "[" + message.getTo() + "][" + message.getSubject() + "]";
    }

    /**
     * Gets the mail interfaces.
     *
     * @return the mail interfaces
     */
    public MailInterface[] getMailInterfaces() {
        return new MailInterface[0];
    }

    /**
     * Send mail.
     *
     * @param from
     *            the from
     * @param to
     *            the to
     * @param subject
     *            the subject
     * @param content
     *            the content
     */
    public void sendMail(final String from, final String to, final String subject, final String content) {
        sendMail(from, to, null, subject, content, null, null);
    }

    /**
     * Send mail.
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
    public void sendMail(final String from, final String to, final String cc, final String subject,
            final String content, final String attachmentName, final String attachmentContent) {
        if (to != null && to.length() > 0) {
            _log.debug("Mail to " + to + ": " + subject);
            final var tokenizer = new StringTokenizer(to, ";,");
            while (tokenizer.hasMoreElements()) {
                put(new MailMessage(from, tokenizer.nextToken(), cc, subject, content, attachmentName,
                        attachmentContent));
            }
        } else {
            _log.warn("Invalid address: null (mail not sent)");
        }
    }

    /**
     * Initialize.
     */
    @Override
    public void initialize() {
        final var props = new Properties();
        props.put("mail.smtp.host", Cnf.at("Mail", "smtp"));
        _session = Session.getDefaultInstance(props, null);
        _host = Cnf.at("Mail", "pop");
        _username = Cnf.at("Mail", "popUser");
        _password = Cnf.at("Mail", "popPassword");
        _storename = Cnf.at("Mail", "storeName", _storename);
        _foldername = Cnf.at("Mail", "folderName", _foldername);
    }

    /**
     * Next step.
     *
     * @return the int
     */
    @Override
    public int nextStep() {
        try {
            _connect();
            _sendMessages();
            _getMessages();
        } catch (final MessagingException e) {
            _log.error("Mail connection", e);
            _log.error("Mail connection", e.getNextException());
            _close();
        } catch (final Throwable t) {
            _log.error("sendMessages", t);
            _close();
        }
        return NEXT_STEP_DELAY;
    }

    /**
     * _close.
     */
    private synchronized void _close() {
        try {
            if (_folder != null && _folder.isOpen()) {
                _folder.close(true);
            }
            if (_store != null) {
                _store.close();
            }
        } catch (final Exception ignored) {
        } finally {
            _folder = null;
            _store = null;
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
     * Shutdown.
     *
     * @param graceful
     *            the graceful
     */
    public void shutdown(final boolean graceful) {
        super.shutdown();
        if (graceful) {
            try {
                _connect();
                _sendMessages();
            } catch (final Exception e) {
                _log.warn(e);
            }
        }
    }

    /**
     * _send message.
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
     *             the exception
     */
    private void _sendMessage(final String from, final String to, final String cc, final String subject,
            final String content, final String attachmentName, final String attachmentContent) throws Exception {
        if (to != null && to.length() > 0) {
            try {
                _log.debug("Sending from=" + from + " to=" + to + " bcc=" + cc + " subject=" + subject
                        + " attachmentName=" + attachmentName);
                final var message = new MimeMessage(_session);
                message.setFrom(new InternetAddress(from != null && from.length() > 0 ? from : Cnf.at("Mail", "from")));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                if (cc != null && cc.length() > 0) {
                    message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
                }
                message.setSubject(subject);
                if (attachmentName != null && attachmentName.length() > 0) {
                    _log.debug("Sending attachment");
                    final var filePart = new MimeBodyPart();
                    filePart.setText(attachmentContent, "utf-8");
                    filePart.setFileName(attachmentName);
                    final var textPart = new MimeBodyPart();
                    textPart.setText(content, "utf-8");
                    final Multipart multipart = new MimeMultipart("alternative");
                    multipart.addBodyPart(textPart);
                    multipart.addBodyPart(filePart);
                    message.setContent(multipart);
                } else {
                    message.setText(content);
                }
                message.saveChanges();
                Transport.send(message);
            } catch (final AddressException | SendFailedException e) {
                _log.warn("Invalid address: " + to + " (mail not sent)", e);
            } catch (final Exception e) {
                throw e;
            }
        } else {
            _log.warn("Invalid address: null (mail not sent)");
        }
    }

    /**
     * Orders by date.
     *
     * @param message
     *            the message
     *
     * @return the message[]
     */
    private Message[] _orderByDate(final Message[] message) {
        final List<Message> list = Arrays.asList(message);
        Collections.sort(list, _dateOrder);
        return list.toArray(new Message[list.size()]);
    }

    /**
     * _connect.
     *
     * @throws NoSuchProviderException
     *             the no such provider exception
     * @throws MessagingException
     *             the messaging exception
     */
    private synchronized void _connect() throws NoSuchProviderException, MessagingException {
        if (_store == null && _host != null && _host.length() > 0) {
            _store = _session.getStore(_storename);
            _store.connect(_host, _username, _password);
            _folder = _store.getFolder(_foldername);
            _folder.open(Folder.READ_WRITE);
        }
    }

    /**
     * Sends the messages.
     *
     * @throws Exception
     *             the exception
     */
    private void _sendMessages() throws Exception {
        for (final MailMessage toSend : getList()) {
            _sendMessage(toSend.getFrom(), toSend.getTo(), toSend.getCC(), toSend.getSubject(), toSend.getContent(),
                    toSend.getAttachmentName(), toSend.getAttachmentContent());
            removeValue(toSend);
        }
    }

    /**
     * Gets the messages.
     *
     * @throws MessagingException
     *             the messaging exception
     */
    private void _getMessages() throws MessagingException {
        final var interfaces = getMailInterfaces();
        for (final MailInterface element : interfaces) {
            _folder.expunge();
            final var message = _orderByDate(_folder.search(element.getSearchTerm()));
            if (message.length > 0) {
                _log.debug("Receiving " + message.length + " mail(s)");
                final var content = new StringBuilder();
                for (final Message notification : message) {
                    if (notification.getFlags().contains(Flags.Flag.DELETED)) {
                        continue;
                    }
                    content.setLength(0);
                    var delete = false;
                    try {
                        final var bao = new ByteArrayOutputStream();
                        notification.getDataHandler().writeTo(bao);
                        content.append(bao.toString());
                        bao.close();
                        delete = element.received(notification, content.toString());
                    } catch (final Exception e) {
                        delete = true;
                        _log.warn("Message deleted: '" + content.toString() + "'", e);
                    } finally {
                        if (delete && _folder.isOpen()) {
                            notification.setFlag(Flags.Flag.DELETED, true);
                        }
                    }
                }
            }
        }
    }
}