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

package ecmwf.common.ai;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 7.4.0
 * @since 2026-03-12
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a chat conversation between a user and the AI assistant. Maintains a limited message history to avoid
 * unbounded memory growth.
 */
public class ChatConversation implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_MAX_HISTORY_SIZE = 20;

    private final int maxHistorySize;
    private final List<Message> history = new ArrayList<>();

    /** Role of a message in the conversation. */
    public enum Role {
        USER, AI
    }

    /** Represents a single chat message. Immutable record. */
    public record Message(Role role, String content) implements Serializable {
        private static final long serialVersionUID = 1L;

        public boolean isUser() {
            return role == Role.USER;
        }
    }

    /** Default constructor with 20 messages max. */
    public ChatConversation() {
        this(DEFAULT_MAX_HISTORY_SIZE);
    }

    /** Constructor with custom max history size. */
    public ChatConversation(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    /** Add a user message. */
    public synchronized void addUserMessage(String message) {
        addMessage(new Message(Role.USER, message));
    }

    /** Add an AI message. */
    public synchronized void addAiMessage(String message) {
        addMessage(new Message(Role.AI, message));
    }

    /** Returns a copy of the conversation history. */
    public synchronized List<Message> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(history));
    }

    /** Returns the most recent user message, if any. */
    public synchronized Optional<Message> getLastUserMessage() {
        return history.stream().filter(Message::isUser).reduce((_, second) -> second);
    }

    /** Returns the most recent AI message, if any. */
    public synchronized Optional<Message> getLastAiMessage() {
        return history.stream().filter(m -> !m.isUser()).reduce((_, second) -> second);
    }

    /** Internal add with trimming. */
    private void addMessage(Message msg) {
        history.add(msg);
        if (history.size() > maxHistorySize) {
            history.subList(0, history.size() - maxHistorySize).clear();
        }
    }
}