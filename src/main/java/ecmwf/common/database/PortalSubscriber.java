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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Objects;

/**
 * The Class PortalSubscriber.
 */
public class PortalSubscriber extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 654835549637748632L;

    /** The portal subscriber id. */
    protected Long PSB_ID;

    /** The target incoming user id. */
    protected String PSB_INU_ID;

    /** The subscriber email. */
    protected String PSB_EMAIL;

    /** The subscriber name. */
    protected String PSB_NAME;

    /** The subscriber ISO country code. */
    protected String PSB_ISO;

    /** The subscriber password. */
    protected String PSB_PASSWORD;

    /** The subscriber active flag. */
    protected boolean PSB_ACTIVE;

    /** The verification token. */
    protected String PSB_VERIFY_TOKEN;

    /** The creation time in epoch milliseconds. */
    protected Long PSB_CREATED_TIME;

    /**
     * Instantiates a new portal subscriber.
     */
    public PortalSubscriber() {
    }

    public Long getPsbId() {
        return PSB_ID;
    }

    public void setPsbId(final Long psbId) {
        PSB_ID = psbId;
    }

    public String getPsbInuId() {
        return PSB_INU_ID;
    }

    public void setPsbInuId(final String psbInuId) {
        PSB_INU_ID = psbInuId;
    }

    public String getPsbEmail() {
        return PSB_EMAIL;
    }

    public void setPsbEmail(final String psbEmail) {
        PSB_EMAIL = psbEmail;
    }

    public String getPsbName() {
        return PSB_NAME;
    }

    public void setPsbName(final String psbName) {
        PSB_NAME = psbName;
    }

    public String getPsbIso() {
        return PSB_ISO;
    }

    public void setPsbIso(final String psbIso) {
        PSB_ISO = psbIso;
    }

    public String getPsbPassword() {
        return PSB_PASSWORD;
    }

    public void setPsbPassword(final String psbPassword) {
        PSB_PASSWORD = psbPassword;
    }

    public boolean getPsbActive() {
        return PSB_ACTIVE;
    }

    public void setPsbActive(final boolean psbActive) {
        PSB_ACTIVE = psbActive;
    }

    public String getPsbVerifyToken() {
        return PSB_VERIFY_TOKEN;
    }

    public void setPsbVerifyToken(final String psbVerifyToken) {
        PSB_VERIFY_TOKEN = psbVerifyToken;
    }

    public Long getPsbCreatedTime() {
        return PSB_CREATED_TIME;
    }

    public void setPsbCreatedTime(final Long psbCreatedTime) {
        PSB_CREATED_TIME = psbCreatedTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(PSB_ID);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (PortalSubscriber) obj;
        return Objects.equals(PSB_ID, other.PSB_ID);
    }
}
