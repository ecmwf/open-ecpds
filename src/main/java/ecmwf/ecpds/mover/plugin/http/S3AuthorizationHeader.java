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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

/**
 * The Class S3AuthorizationHeader.
 */
final class S3AuthorizationHeader {

    /** The Constant DIGEST_MAP. */
    private static final ImmutableMap<String, String> DIGEST_MAP = ImmutableMap.<String, String> builder()
            .put("SHA256", "SHA-256").put("SHA1", "SHA-1").put("MD5", "MD5").build();

    /** The Constant SIGNATURE_FIELD. */
    private static final String SIGNATURE_FIELD = "Signature=";

    /** The Constant CREDENTIAL_FIELD. */
    private static final String CREDENTIAL_FIELD = "Credential=";

    /** The authentication type. */
    final AuthenticationType authenticationType;

    /** The hmac algorithm. */
    final String hmacAlgorithm;

    /** The hash algorithm. */
    final String hashAlgorithm;

    /** The region. */
    final String region;

    /** The date. */
    final String date;

    /** The service. */
    final String service;

    /** The identity. */
    final String identity;

    /** The signature. */
    final String signature;

    /**
     * Instantiates a new s 3 authorization header.
     *
     * @param header
     *            the header
     */
    S3AuthorizationHeader(final String header) {
        if (header.startsWith("AWS ")) {
            authenticationType = AuthenticationType.AWS_V2;
            hmacAlgorithm = null;
            hashAlgorithm = null;
            region = null;
            date = null;
            service = null;
            final var fields = Splitter.on(' ').splitToList(header);
            if (fields.size() != 2) {
                throw new IllegalArgumentException("Invalid header");
            }
            final var identityTuple = Splitter.on(':').splitToList(fields.get(1));
            if (identityTuple.size() != 2) {
                throw new IllegalArgumentException("Invalid header");
            }
            identity = identityTuple.get(0);
            signature = identityTuple.get(1);
        } else if (header.startsWith("AWS4-HMAC")) {
            authenticationType = AuthenticationType.AWS_V4;
            signature = extractSignature(header);

            final var credentialIndex = header.indexOf(CREDENTIAL_FIELD);
            if (credentialIndex < 0) {
                throw new IllegalArgumentException("Invalid header");
            }
            final var credentialEnd = header.indexOf(',', credentialIndex);
            if (credentialEnd < 0) {
                throw new IllegalArgumentException("Invalid header");
            }
            final var credential = header.substring(credentialIndex + CREDENTIAL_FIELD.length(), credentialEnd);
            final var fields = Splitter.on('/').splitToList(credential);
            if (fields.size() != 5) {
                throw new IllegalArgumentException("Invalid Credential: " + credential);
            }
            identity = fields.get(0);
            date = fields.get(1);
            region = fields.get(2);
            service = fields.get(3);
            final var awsSignatureVersion = header.substring(0, header.indexOf(' '));
            hashAlgorithm = DIGEST_MAP.get(Splitter.on('-').splitToList(awsSignatureVersion).get(2));
            hmacAlgorithm = "Hmac" + Splitter.on('-').splitToList(awsSignatureVersion).get(2);
        } else {
            throw new IllegalArgumentException("Invalid header");
        }
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return "Identity: " + identity + "; Signature: " + signature + "; HMAC algorithm: " + hmacAlgorithm
                + "; Hash algorithm: " + hashAlgorithm + "; region: " + region + "; date: " + date + "; service "
                + service;
    }

    /**
     * Extract signature.
     *
     * @param header
     *            the header
     *
     * @return the string
     */
    private static String extractSignature(final String header) {
        var signatureIndex = header.indexOf(SIGNATURE_FIELD);
        if (signatureIndex < 0) {
            throw new IllegalArgumentException("Invalid signature");
        }
        signatureIndex += SIGNATURE_FIELD.length();
        final var signatureEnd = header.indexOf(',', signatureIndex);
        if (signatureEnd < 0) {
            return header.substring(signatureIndex);
        }
        return header.substring(signatureIndex, signatureEnd);
    }
}
