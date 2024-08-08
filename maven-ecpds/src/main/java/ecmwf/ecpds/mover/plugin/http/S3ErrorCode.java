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

import javax.servlet.http.HttpServletResponse;

import com.google.common.base.CaseFormat;

/**
 * List of S3 error codes. Reference: http://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html
 */
enum S3ErrorCode {

    /** The access denied. */
    ACCESS_DENIED(HttpServletResponse.SC_FORBIDDEN, "Forbidden"),

    /** The bad digest. */
    BAD_DIGEST(HttpServletResponse.SC_BAD_REQUEST, "Bad Request"),

    /** The bucket already exists. */
    BUCKET_ALREADY_EXISTS(HttpServletResponse.SC_FORBIDDEN, """
            The requested bucket name is not available.\
             The bucket namespace is shared by all users of the system.\
             Please select a different name and try again."""),

    /** The bucket already owned by you. */
    BUCKET_ALREADY_OWNED_BY_YOU(HttpServletResponse.SC_CONFLICT,
            "Your previous request to create the named bucket" + " succeeded and you already own it."),

    /** The bucket not empty. */
    BUCKET_NOT_EMPTY(HttpServletResponse.SC_CONFLICT, "The bucket you tried to delete is not empty"),

    /** The entity too large. */
    ENTITY_TOO_LARGE(HttpServletResponse.SC_BAD_REQUEST,
            "Your proposed upload exceeds the maximum allowed object size."),

    /** The entity too small. */
    ENTITY_TOO_SMALL(HttpServletResponse.SC_BAD_REQUEST, """
            Your proposed upload is smaller than the minimum allowed object\
             size. Each part must be at least 5 MB in size, except the last\
             part."""),

    /** The invalid access key id. */
    INVALID_ACCESS_KEY_ID(HttpServletResponse.SC_FORBIDDEN, "Forbidden"),

    /** The invalid argument. */
    INVALID_ARGUMENT(HttpServletResponse.SC_BAD_REQUEST, "Bad Request"),

    /** The invalid bucket name. */
    INVALID_BUCKET_NAME(HttpServletResponse.SC_BAD_REQUEST, "The specified bucket is not valid."),

    /** The invalid cors origin. */
    INVALID_CORS_ORIGIN(HttpServletResponse.SC_BAD_REQUEST, "Insufficient information. Origin request header needed."),

    /** The invalid cors method. */
    INVALID_CORS_METHOD(HttpServletResponse.SC_BAD_REQUEST,
            "The specified Access-Control-Request-Method is not valid."),

    /** The invalid digest. */
    INVALID_DIGEST(HttpServletResponse.SC_BAD_REQUEST, "Bad Request"),

    /** The invalid location constraint. */
    INVALID_LOCATION_CONSTRAINT(HttpServletResponse.SC_BAD_REQUEST, """
            The specified location constraint is not valid. For\
             more information about Regions, see How to Select\
             a Region for Your Buckets."""),

    /** The invalid range. */
    INVALID_RANGE(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE, "The requested range is not satisfiable"),

    /** The invalid part. */
    INVALID_PART(HttpServletResponse.SC_BAD_REQUEST, """
            One or more of the specified parts could not be found.\
              The part may not have been uploaded, or the specified entity\
             tag may not match the part's entity tag."""),

    /** The invalid request. */
    INVALID_REQUEST(HttpServletResponse.SC_BAD_REQUEST, "Bad Request"),

    /** The malformed x m l. */
    MALFORMED_X_M_L(HttpServletResponse.SC_BAD_REQUEST,
            "The XML you provided was not well-formed or did not validate" + " against our published schema."),

    /** The max message length exceeded. */
    MAX_MESSAGE_LENGTH_EXCEEDED(HttpServletResponse.SC_BAD_REQUEST, "Your request was too big."),

    /** The method not allowed. */
    METHOD_NOT_ALLOWED(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method Not Allowed"),

    /** The missing content length. */
    MISSING_CONTENT_LENGTH(HttpServletResponse.SC_LENGTH_REQUIRED, "Length Required"),

    /** The no such bucket. */
    NO_SUCH_BUCKET(HttpServletResponse.SC_NOT_FOUND, "The specified bucket does not exist"),

    /** The no such key. */
    NO_SUCH_KEY(HttpServletResponse.SC_NOT_FOUND, "The specified key does not exist."),

    /** The no such policy. */
    NO_SUCH_POLICY(HttpServletResponse.SC_NOT_FOUND, "The specified bucket does not have a bucket policy."),

    /** The no such upload. */
    NO_SUCH_UPLOAD(HttpServletResponse.SC_NOT_FOUND, "Not Found"),

    /** The not implemented. */
    NOT_IMPLEMENTED(HttpServletResponse.SC_NOT_IMPLEMENTED,
            "A header you provided implies functionality that is not" + " implemented."),

    /** The precondition failed. */
    PRECONDITION_FAILED(HttpServletResponse.SC_PRECONDITION_FAILED,
            "At least one of the preconditions you specified did not hold."),

    /** The request time too skewed. */
    REQUEST_TIME_TOO_SKEWED(HttpServletResponse.SC_FORBIDDEN, "Forbidden"),

    /** The request timeout. */
    REQUEST_TIMEOUT(HttpServletResponse.SC_BAD_REQUEST, "Bad Request"),

    /** The signature does not match. */
    SIGNATURE_DOES_NOT_MATCH(HttpServletResponse.SC_FORBIDDEN, "Forbidden"),

    /** The x amz content s h a 256 mismatch. */
    X_AMZ_CONTENT_S_H_A_256_MISMATCH(HttpServletResponse.SC_BAD_REQUEST,
            "The provided 'x-amz-content-sha256' header does not match what" + " was computed.");

    /** The error code. */
    private final String errorCode;

    /** The http status code. */
    private final int httpStatusCode;

    /** The message. */
    private final String message;

    /**
     * Instantiates a new s 3 error code.
     *
     * @param httpStatusCode
     *            the http status code
     * @param message
     *            the message
     */
    S3ErrorCode(final int httpStatusCode, final String message) {
        this.errorCode = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name());
        this.httpStatusCode = httpStatusCode;
        this.message = requireNonNull(message);
    }

    /**
     * Gets the error code.
     *
     * @return the error code
     */
    String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the http status code.
     *
     * @return the http status code
     */
    int getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    String getMessage() {
        return message;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return getHttpStatusCode() + " " + getErrorCode() + " " + getMessage();
    }
}
