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

/**
 * The Class AwsHttpHeaders.
 */
final class AwsHttpHeaders {

    /** The Constant ACL. */
    static final String ACL = "x-amz-acl";

    /** The Constant CONTENT_SHA256. */
    static final String CONTENT_SHA256 = "x-amz-content-sha256";

    /** The Constant COPY_SOURCE. */
    static final String COPY_SOURCE = "x-amz-copy-source";

    /** The Constant COPY_SOURCE_IF_MATCH. */
    static final String COPY_SOURCE_IF_MATCH = "x-amz-copy-source-if-match";

    /** The Constant COPY_SOURCE_IF_MODIFIED_SINCE. */
    static final String COPY_SOURCE_IF_MODIFIED_SINCE = "x-amz-copy-source-if-modified-since";

    /** The Constant COPY_SOURCE_IF_NONE_MATCH. */
    static final String COPY_SOURCE_IF_NONE_MATCH = "x-amz-copy-source-if-none-match";

    /** The Constant COPY_SOURCE_IF_UNMODIFIED_SINCE. */
    static final String COPY_SOURCE_IF_UNMODIFIED_SINCE = "x-amz-copy-source-if-unmodified-since";

    /** The Constant COPY_SOURCE_RANGE. */
    static final String COPY_SOURCE_RANGE = "x-amz-copy-source-range";

    /** The Constant DATE. */
    static final String DATE = "x-amz-date";

    /** The Constant DECODED_CONTENT_LENGTH. */
    static final String DECODED_CONTENT_LENGTH = "x-amz-decoded-content-length";

    /** The Constant METADATA_DIRECTIVE. */
    static final String METADATA_DIRECTIVE = "x-amz-metadata-directive";

    /** The Constant REQUEST_ID. */
    static final String REQUEST_ID = "x-amz-request-id";

    /** The Constant STORAGE_CLASS. */
    static final String STORAGE_CLASS = "x-amz-storage-class";

    /**
     * Instantiates a new aws http headers.
     */
    private AwsHttpHeaders() {
        throw new AssertionError("intentionally unimplemented");
    }
}
