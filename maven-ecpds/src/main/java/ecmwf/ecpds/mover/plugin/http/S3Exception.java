/*
 * Copyright 2014-2020 Andrew Gaul <andrew@gaul.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ecmwf.ecpds.mover.plugin.http;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * The Class S3Exception.
 */
@SuppressWarnings("serial")
public final class S3Exception extends Exception {

    /** The error. */
    private final S3ErrorCode error;

    /** The elements. */
    private final Map<String, String> elements;

    /**
     * Instantiates a new s 3 exception.
     *
     * @param error
     *            the error
     */
    S3Exception(final S3ErrorCode error) {
        this(error, error.getMessage(), (Throwable) null, ImmutableMap.<String, String> of());
    }

    /**
     * Instantiates a new s 3 exception.
     *
     * @param error
     *            the error
     * @param message
     *            the message
     */
    S3Exception(final S3ErrorCode error, final String message) {
        this(error, message, (Throwable) null, ImmutableMap.<String, String> of());
    }

    /**
     * Instantiates a new s 3 exception.
     *
     * @param error
     *            the error
     * @param cause
     *            the cause
     */
    S3Exception(final S3ErrorCode error, final Throwable cause) {
        this(error, error.getMessage(), cause, ImmutableMap.<String, String> of());
    }

    /**
     * Instantiates a new s 3 exception.
     *
     * @param error
     *            the error
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    S3Exception(final S3ErrorCode error, final String message, final Throwable cause) {
        this(error, message, cause, ImmutableMap.<String, String> of());
    }

    /**
     * Instantiates a new s 3 exception.
     *
     * @param error
     *            the error
     * @param message
     *            the message
     * @param cause
     *            the cause
     * @param elements
     *            the elements
     */
    S3Exception(final S3ErrorCode error, final String message, final Throwable cause,
            final Map<String, String> elements) {
        super(requireNonNull(message), cause);
        this.error = requireNonNull(error);
        this.elements = ImmutableMap.copyOf(elements);
    }

    /**
     * Gets the error.
     *
     * @return the error
     */
    S3ErrorCode getError() {
        return error;
    }

    /**
     * Gets the elements.
     *
     * @return the elements
     */
    Map<String, String> getElements() {
        return elements;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    @Override
    public String getMessage() {
        final var builder = new StringBuilder().append(super.getMessage());
        if (!elements.isEmpty()) {
            builder.append(" ").append(elements);
        }
        return builder.toString();
    }
}
