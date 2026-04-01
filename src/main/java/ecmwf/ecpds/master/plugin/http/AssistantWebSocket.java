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

package ecmwf.ecpds.master.plugin.http;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 7.4.0
 * @since 2026-03-12
 */

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.ai.AssistantService;
import ecmwf.common.ai.ChatConversation;

/**
 * WebSocket endpoint for streaming AI assistant interactions to clients.
 * <p>
 * Supports bidirectional communication between the client and {@link AssistantService}. Each WebSocket maintains a
 * conversation per HTTP session, streams AI-generated tokens in real time, and supports cancellation and heartbeat
 * pings.
 * </p>
 * <p>
 * Features:
 * <ul>
 * <li>Streaming tokens from AI assistant to client</li>
 * <li>Maintains per-session chat history</li>
 * <li>Heartbeat pings to prevent WebSocket timeouts</li>
 * <li>Context-aware AI queries using JSON context</li>
 * <li>Graceful cancellation on disconnect or error</li>
 * <li>Token batching to reduce message overhead</li>
 * </ul>
 * </p>
 */
@WebSocket
public class AssistantWebSocket {

    private static final Logger LOG = LogManager.getLogger(AssistantWebSocket.class);

    /** Thread pool for heartbeat ping and token flushing tasks. */
    private static final ScheduledThreadPoolExecutor HEARTBEAT_POOL = new ScheduledThreadPoolExecutor(1, r -> {
        final var t = new Thread(r, "ai-heartbeat");
        t.setDaemon(true);
        return t;
    });

    static {
        HEARTBEAT_POOL.setRemoveOnCancelPolicy(true);
    }

    /** Executor service for AI request processing. */
    private static final ExecutorService AI_EXECUTOR = Executors.newCachedThreadPool(r -> {
        final var t = new Thread(r, "ai-worker");
        t.setDaemon(true);
        return t;
    });

    /** Shared AI assistant service instance. */
    private static final AssistantService aiService = new AssistantService();

    /** JSON parser for incoming/outgoing WebSocket messages. */
    private static final ObjectMapper JSON = new ObjectMapper();

    /** HTTP session to maintain per-user conversation state. */
    private final HttpSession httpSession;

    /** Jetty WebSocket session for this client. */
    private Session session;

    /** Scheduled task for periodic WebSocket ping. */
    private ScheduledFuture<?> wsPingTask;

    /** Scheduled task for flushing token buffer at intervals. */
    private ScheduledFuture<?> flushTask;

    /** Future representing the current AI request task. */
    private CompletableFuture<Void> currentTask;

    /** Flag to indicate cancellation of the current AI task. */
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    /** Buffer for streaming AI tokens before flushing to client. */
    private final StringBuilder tokenBuffer = new StringBuilder();

    /** Lock object for thread-safe access to token buffer. */
    private final Object bufferLock = new Object();

    /** Flush interval in milliseconds for sending buffered tokens to client. */
    private static final long TOKEN_FLUSH_INTERVAL_MS = 50;

    /** Maximum number of characters in token buffer before forced flush. */
    private static final int TOKEN_BATCH_SIZE = 512;

    /**
     * Constructs a WebSocket instance bound to the given HTTP session.
     *
     * @param httpSession
     *            the HTTP session for maintaining conversation state
     */
    public AssistantWebSocket(final HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    // ----------------------------------------------------------------------
    // WebSocket lifecycle
    // ----------------------------------------------------------------------

    /**
     * Called when the WebSocket is connected.
     * <p>
     * Schedules periodic token flush and heartbeat ping tasks, and sets idle timeout.
     *
     * @param session
     *            the connected WebSocket session
     */
    @OnWebSocketConnect
    public void onConnect(final Session session) {
        this.session = session;
        session.setIdleTimeout(Duration.ofMinutes(2));
        // schedule token flush at regular interval
        flushTask = HEARTBEAT_POOL.scheduleAtFixedRate(this::flushTokenBuffer, TOKEN_FLUSH_INTERVAL_MS,
                TOKEN_FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
        // schedule WS ping every 20s
        wsPingTask = HEARTBEAT_POOL.scheduleAtFixedRate(() -> {
            if (session != null && session.isOpen()) {
                try {
                    session.getRemote().sendPing(ByteBuffer.wrap(new byte[] { 1, 2, 3, 4 }));
                } catch (final Exception e) {
                    LOG.debug("WS ping failed: {}", e.toString());
                }
            }
        }, 20, 20, TimeUnit.SECONDS);
    }

    /**
     * Called when a message is received from the client.
     * <p>
     * Expected JSON format:
     *
     * <pre>
     * {
     *   "question": "user question",
     *   "context": { ... optional AI context ... }
     * }
     * </pre>
     *
     * Supports cancellation messages:
     *
     * <pre>
     * {
     *   "type": "cancel"
     * }
     * </pre>
     *
     * @param message
     *            the raw JSON message from the client
     */
    @OnWebSocketMessage
    public void onMessage(final String message) {
        try {
            final var node = JSON.readTree(message);
            // ---- Handle cancel request ----
            if (node.has("type") && "cancel".equals(node.get("type").asText())) {
                cancelled.set(true);
                if (currentTask != null)
                    currentTask.cancel(true);
                // Optionally clear token buffer and notify client
                synchronized (bufferLock) {
                    tokenBuffer.setLength(0);
                }
                if (session != null && session.isOpen()) {
                    final var cancelNode = JSON.createObjectNode();
                    cancelNode.put("type", "done"); // signal client the response is stopped
                    session.getRemote().sendString(JSON.writeValueAsString(cancelNode));
                }
                return; // done processing this cancel message
            }
            // ---- Existing processing for normal question ----
            if (!node.has("question")) {
                sendError("Missing 'question' field in JSON message.");
                return;
            }
            cancelled.set(false); // reset cancel flag for new question
            final var conversation = getConversation();
            final var question = node.get("question").asText();
            final var tmpContext = node.has("context") ? node.get("context") : null;
            conversation.addUserMessage(question);
            final ScheduledFuture<?> heartbeat = HEARTBEAT_POOL.scheduleAtFixedRate(this::sendPing, 5, 5,
                    TimeUnit.SECONDS);
            currentTask = CompletableFuture.runAsync(() -> {
                try {
                    aiService.askStreaming(conversation.getHistory(), question, tmpContext, this::bufferToken,
                            cancelled);
                    if (!cancelled.get()) {
                        String finalResponse;
                        synchronized (bufferLock) {
                            finalResponse = tokenBuffer.toString();
                            tokenBuffer.setLength(0);
                        }
                        sendEnd();
                        conversation.addAiMessage(finalResponse);
                    }
                } catch (final RuntimeException e) {
                    if (!"Cancelled".equals(e.getMessage()))
                        sendError("Internal server error");
                } catch (final Exception e) {
                    sendError("Internal server error");
                } finally {
                    heartbeat.cancel(true);
                }
            }, AI_EXECUTOR);
        } catch (final Exception e) {
            LOG.warn("Invalid JSON message: {}", message, e);
            sendError("Invalid message format");
        }
    }

    /**
     * Called when the WebSocket is closed.
     *
     * @param statusCode
     *            the close status code
     * @param reason
     *            the reason for closure
     */
    @OnWebSocketClose
    public void onClose(final int statusCode, final String reason) {
        cancelled.set(true);
        if (currentTask != null)
            currentTask.cancel(true);
        if (flushTask != null)
            flushTask.cancel(true);
        if (wsPingTask != null)
            wsPingTask.cancel(true);
        LOG.debug("Closed: {} - {}", statusCode, reason != null ? reason : "none");
    }

    /**
     * Called on WebSocket error.
     *
     * @param error
     *            the thrown error
     */
    @OnWebSocketError
    public void onError(final Throwable error) {
        cancelled.set(true);
        if (currentTask != null)
            currentTask.cancel(true);
        if (flushTask != null)
            flushTask.cancel(true);
        if (wsPingTask != null)
            wsPingTask.cancel(true);
        LOG.warn("WebSocket error", error);
    }

    // ----------------------------------------------------------------------
    // Token buffering & flush (structured JSON)
    // ----------------------------------------------------------------------

    /**
     * Buffers a token received from the AI model for batched delivery to the client.
     * <p>
     * If the accumulated tokens exceed {@link #TOKEN_BATCH_SIZE}, the buffer is immediately flushed to the client via
     * {@link #flushTokenBuffer()}.
     *
     * @param token
     *            the AI-generated token to buffer
     *
     *            Example:
     *
     *            <pre>
     *            // Suppose the AI model generates these tokens sequentially:
     *            bufferToken("The quick ");
     *            bufferToken("brown fox ");
     *            bufferToken("jumps over");
     *            // If the buffer exceeds TOKEN_BATCH_SIZE, flushTokenBuffer() is
     *            // triggered
     *            </pre>
     */
    private void bufferToken(final String token) {
        synchronized (bufferLock) {
            tokenBuffer.append(token);
            if (tokenBuffer.length() >= TOKEN_BATCH_SIZE) {
                flushTokenBuffer();
            }
        }
    }

    /**
     * Flushes the buffered tokens to the WebSocket client as a single JSON message.
     * <p>
     * Each flush sends a JSON object with type "token" and the accumulated text. The buffer is cleared after sending.
     *
     * Example payload sent to client:
     *
     * <pre>
     * {
     *   "type": "token",
     *   "text": "The quick brown fox jumps over"
     * }
     * </pre>
     *
     * If the session is closed or there are no tokens, this method returns immediately.
     */
    private void flushTokenBuffer() {
        if (session == null || !session.isOpen())
            return;
        final String toSend;
        synchronized (bufferLock) {
            if (tokenBuffer.isEmpty())
                return;
            toSend = tokenBuffer.toString();
            tokenBuffer.setLength(0);
        }
        try {
            // Wrap tokens in structured JSON
            final var node = JSON.createObjectNode();
            node.put("type", "token");
            node.put("text", toSend);
            session.getRemote().sendString(JSON.writeValueAsString(node));
        } catch (final Exception e) {
            LOG.warn("Failed to flush tokens", e);
        }
    }

    /**
     * Sends an end-of-response marker to the client.
     * <p>
     * Signals that the AI assistant has completed its response. Frontend can use this to finalize UI updates, enable
     * input, or show completion indicators.
     *
     * Example payload:
     *
     * <pre>
     * {
     *   "type": "done"
     * }
     * </pre>
     */
    private void sendEnd() {
        if (session == null || !session.isOpen())
            return;
        try {
            final var node = JSON.createObjectNode();
            node.put("type", "done");
            session.getRemote().sendString(JSON.writeValueAsString(node));
        } catch (final Exception e) {
            LOG.warn("END failed", e);
        }
    }

    /**
     * Sends an error message to the client in structured JSON format.
     * <p>
     * Use this to notify the frontend of issues during processing, e.g., invalid JSON, AI failure, or unexpected
     * exceptions.
     *
     * @param msg
     *            the error message string
     *
     *            Example payload:
     *
     *            <pre>
     * {
     *   "type": "error",
     *   "message": "Invalid message format"
     * }
     *            </pre>
     */
    private void sendError(final String msg) {
        if (session == null || !session.isOpen())
            return;
        try {
            final var node = JSON.createObjectNode();
            node.put("type", "error");
            node.put("message", msg);
            session.getRemote().sendString(JSON.writeValueAsString(node));
        } catch (final Exception e) {
            LOG.warn("Error message failed", e);
        }
    }

    /**
     * Sends a ping to the client to keep the WebSocket alive.
     * <p>
     * The frontend may ignore this or use it to show connection health indicators.
     *
     * Example payload:
     *
     * <pre>
     * {
     *   "type": "ping"
     * }
     * </pre>
     */
    private void sendPing() {
        if (session == null || !session.isOpen())
            return;
        try {
            final var node = JSON.createObjectNode();
            node.put("type", "ping");
            session.getRemote().sendString(JSON.writeValueAsString(node));
        } catch (final Exception e) {
            LOG.warn("PING failed", e);
        }
    }

    /**
     * Retrieves the chat conversation for this HTTP session.
     * <p>
     * Creates a new {@link ChatConversation} if none exists.
     *
     * @return the current chat conversation
     */
    private ChatConversation getConversation() {
        final var obj = httpSession.getAttribute("AI_CONVERSATION");
        if (obj instanceof final ChatConversation conv)
            return conv;
        final var conv = new ChatConversation();
        httpSession.setAttribute("AI_CONVERSATION", conv);
        return conv;
    }
}