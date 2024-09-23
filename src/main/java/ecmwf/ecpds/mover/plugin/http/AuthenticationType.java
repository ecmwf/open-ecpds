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

import com.google.common.base.CaseFormat;

/**
 * The Enum AuthenticationType.
 *
 * @author root
 */
public enum AuthenticationType {

    /** The aws v2. */
    AWS_V2,
    /** The aws v4. */
    AWS_V4,
    /** The aws v2 or v4. */
    AWS_V2_OR_V4;

    /**
     * From string.
     *
     * @param string
     *            the string
     *
     * @return the authentication type
     */
    static AuthenticationType fromString(final String string) {
        return AuthenticationType.valueOf(CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, string));
    }
}
