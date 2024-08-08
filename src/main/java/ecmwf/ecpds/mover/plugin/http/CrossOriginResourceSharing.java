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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * The Class CrossOriginResourceSharing.
 */
public final class CrossOriginResourceSharing {

    /** The Constant logger. */
    private static final Logger logger = LogManager.getLogger(CrossOriginResourceSharing.class);

    /** The Constant SUPPORTED_METHODS. */
    protected static final Collection<String> SUPPORTED_METHODS = ImmutableList.of("GET", "HEAD", "PUT", "POST");

    /** The Constant HEADER_VALUE_SEPARATOR. */
    private static final String HEADER_VALUE_SEPARATOR = ", ";

    /** The Constant ALLOW_ANY_ORIGIN. */
    private static final String ALLOW_ANY_ORIGIN = "*";

    /** The Constant ALLOW_ANY_HEADER. */
    private static final String ALLOW_ANY_HEADER = "*";

    /** The allowed methods raw. */
    private final String allowedMethodsRaw;

    /** The allowed headers raw. */
    private final String allowedHeadersRaw;

    /** The any origin allowed. */
    private final boolean anyOriginAllowed;

    /** The allowed origins. */
    private final Set<Pattern> allowedOrigins;

    /** The allowed methods. */
    private final Set<String> allowedMethods;

    /** The allowed headers. */
    private final Set<String> allowedHeaders;

    /**
     * Instantiates a new cross origin resource sharing.
     */
    public CrossOriginResourceSharing() {
        // CORS Allow all
        this(Lists.newArrayList(ALLOW_ANY_ORIGIN), SUPPORTED_METHODS, Lists.newArrayList(ALLOW_ANY_HEADER));
    }

    /**
     * Instantiates a new cross origin resource sharing.
     *
     * @param allowedOrigins
     *            the allowed origins
     * @param allowedMethods
     *            the allowed methods
     * @param allowedHeaders
     *            the allowed headers
     */
    public CrossOriginResourceSharing(final Collection<String> allowedOrigins, final Collection<String> allowedMethods,
            final Collection<String> allowedHeaders) {
        final Set<Pattern> allowedPattern = new HashSet<>();
        var anyOriginAllowed = false;

        if (allowedOrigins != null) {
            if (allowedOrigins.contains(ALLOW_ANY_ORIGIN)) {
                anyOriginAllowed = true;
            } else {
                for (final String origin : allowedOrigins) {
                    allowedPattern.add(Pattern.compile(origin, Pattern.CASE_INSENSITIVE));
                }
            }
        }
        this.anyOriginAllowed = anyOriginAllowed;
        this.allowedOrigins = ImmutableSet.copyOf(allowedPattern);

        if (allowedMethods == null) {
            this.allowedMethods = ImmutableSet.of();
        } else {
            this.allowedMethods = ImmutableSet.copyOf(allowedMethods);
        }
        this.allowedMethodsRaw = Joiner.on(HEADER_VALUE_SEPARATOR).join(this.allowedMethods);

        if (allowedHeaders == null) {
            this.allowedHeaders = ImmutableSet.of();
        } else {
            this.allowedHeaders = ImmutableSet.copyOf(allowedHeaders);
        }
        this.allowedHeadersRaw = Joiner.on(HEADER_VALUE_SEPARATOR).join(this.allowedHeaders);

        logger.info("CORS allowed origins: {}", allowedOrigins);
        logger.info("CORS allowed methods: {}", allowedMethods);
        logger.info("CORS allowed headers: {}", allowedHeaders);
    }

    /**
     * Gets the allowed methods.
     *
     * @return the allowed methods
     */
    public String getAllowedMethods() {
        return this.allowedMethodsRaw;
    }

    /**
     * Gets the allowed origin.
     *
     * @param origin
     *            the origin
     *
     * @return the allowed origin
     */
    public String getAllowedOrigin(final String origin) {
        if (this.anyOriginAllowed) {
            return ALLOW_ANY_ORIGIN;
        }
        return origin;
    }

    /**
     * Checks if is origin allowed.
     *
     * @param origin
     *            the origin
     *
     * @return true, if is origin allowed
     */
    public boolean isOriginAllowed(final String origin) {
        if (!Strings.isNullOrEmpty(origin)) {
            if (this.anyOriginAllowed) {
                logger.debug("CORS origin allowed: {}", origin);
                return true;
            }
            for (final Pattern pattern : this.allowedOrigins) {
                final var matcher = pattern.matcher(origin);
                if (matcher.matches()) {
                    logger.debug("CORS origin allowed: {}", origin);
                    return true;
                }
            }
        }
        logger.debug("CORS origin not allowed: {}", origin);
        return false;
    }

    /**
     * Checks if is method allowed.
     *
     * @param method
     *            the method
     *
     * @return true, if is method allowed
     */
    public boolean isMethodAllowed(final String method) {
        if (!Strings.isNullOrEmpty(method) && this.allowedMethods.contains(method)) {
            logger.debug("CORS method allowed: {}", method);
            return true;
        }
        logger.debug("CORS method not allowed: {}", method);
        return false;
    }

    /**
     * Checks if is every header allowed.
     *
     * @param headers
     *            the headers
     *
     * @return true, if is every header allowed
     */
    public boolean isEveryHeaderAllowed(final String headers) {
        var result = false;

        if (!Strings.isNullOrEmpty(headers)) {
            if (ALLOW_ANY_HEADER.equals(this.allowedHeadersRaw)) {
                result = true;
            } else {
                for (final String header : Splitter.on(HEADER_VALUE_SEPARATOR).split(headers)) {
                    result = this.allowedHeaders.contains(header);
                    if (!result) {
                        // First not matching header breaks
                        break;
                    }
                }
            }
        }

        if (result) {
            logger.debug("CORS headers allowed: {}", headers);
        } else {
            logger.debug("CORS headers not allowed: {}", headers);
        }

        return result;
    }

    /**
     * Equals.
     *
     * @param object
     *            the object
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof final CrossOriginResourceSharing that) {
            return this.allowedOrigins.equals(that.allowedOrigins)
                    && this.allowedMethodsRaw.equals(that.allowedMethodsRaw)
                    && this.allowedHeadersRaw.equals(that.allowedHeadersRaw);
        }
        return false;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.allowedOrigins, this.allowedMethodsRaw, this.allowedHeadersRaw);
    }
}
