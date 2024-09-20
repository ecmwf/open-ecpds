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

package ecmwf.common.telnet;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Imported/modified from external API: "TelnetProtocolHandler.java,v 2.14
 * 2001/10/07 20:17:43 marcus Exp $";
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;

/**
 * The Class TelnetDimension.
 */
public final class TelnetDimension implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5302457549648290622L;

    /**
     * Instantiates a new telnet dimension.
     *
     * @param width
     *            the width
     * @param height
     *            the height
     */
    TelnetDimension(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    /** The width. */
    public final int width;

    /** The height. */
    public final int height;
}
