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
 * ECMWF Product Data Store (OpenPDS) Project.
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

final class MailMessage {
    /** The _from. */
    private final String _from;

    /** The _to. */
    private final String _to;

    /** The _bcc. */
    private final String _cc;

    /** The _subject. */
    private final String _subject;

    /** The _content. */
    private final String _content;

    /** The attachment name. */
    private final String _attachmentName;

    /** The attachment content. */
    private final String _attachmentContent;

    /**
     * Instantiates a new mail message.
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
     * @param attachmentcontent
     *            the attachmentcontent
     */
    MailMessage(final String from, final String to, final String cc, final String subject, final String content,
            final String attachmentName, final String attachmentcontent) {
        _from = from;
        _to = to;
        _cc = cc;
        _subject = subject;
        _content = content;
        _attachmentName = attachmentName;
        _attachmentContent = attachmentcontent;
    }

    /**
     * Gets the to sender.
     *
     * @return the to sender
     */
    String getFrom() {
        return _from;
    }

    /**
     * Gets the to recipient.
     *
     * @return the to recipient
     */
    String getTo() {
        return _to;
    }

    /**
     * Gets the cc recipient.
     *
     * @return the cc recipient
     */
    String getCC() {
        return _cc;
    }

    /**
     * Gets the subject.
     *
     * @return the subject
     */
    String getSubject() {
        return _subject;
    }

    /**
     * Gets the content.
     *
     * @return the content
     */
    String getContent() {
        return _content;
    }

    /**
     * Gets the attachment name.
     *
     * @return the attachment name
     */
    String getAttachmentName() {
        return _attachmentName;
    }

    /**
     * Gets the attachment content.
     *
     * @return attachment content
     */
    String getAttachmentContent() {
        return _attachmentContent;
    }
}