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
 * Represents a user feedback submission stored in the FEEDBACK table.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * The Class Feedback.
 */
public class Feedback extends DataBaseObject {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Auto-increment primary key. */
    protected Long FBK_ID;

    /** Web user login (null = anonymous). */
    protected String WEU_ID;

    /** Submission timestamp (millis since epoch). */
    protected BigDecimal FBK_TIME;

    /** Star rating 1–5. */
    protected Integer FBK_RATING;

    /** Free-text comment. */
    protected String FBK_COMMENT;

    /** How the user uses OpenECPDS (Evaluation / Production / Research / Other). */
    protected String FBK_USAGE;

    /** Most-used component (Dissemination / Acquisition / Portal / Monitoring). */
    protected String FBK_COMPONENT;

    /** Optional contact email for follow-up. */
    protected String FBK_CONTACT;

    /** The one thing that would make OpenECPDS better. */
    protected String FBK_ONE_THING;

    /** Would recommend? (null = no answer). */
    protected Boolean FBK_RECOMMEND;

    /** Permission to quote comments anonymously in presentations/docs. */
    protected boolean FBK_QUOTE_OK;

    /** Whether an admin has reviewed this feedback entry. */
    protected boolean FBK_REVIEWED;

    /** Instantiates a new Feedback. */
    public Feedback() {
    }

    public long getId() {
        return FBK_ID;
    }

    public void setId(final long id) {
        FBK_ID = id;
    }

    public String getWebUserId() {
        return WEU_ID;
    }

    public void setWebUserId(final String id) {
        WEU_ID = id;
    }

    public BigDecimal getTime() {
        return FBK_TIME;
    }

    public void setTime(final BigDecimal t) {
        FBK_TIME = t;
    }

    public int getRating() {
        return FBK_RATING != null ? FBK_RATING : 0;
    }

    public void setRating(final int r) {
        FBK_RATING = r;
    }

    public String getComment() {
        return FBK_COMMENT;
    }

    public void setComment(final String c) {
        FBK_COMMENT = c;
    }

    public String getUsage() {
        return FBK_USAGE;
    }

    public void setUsage(final String u) {
        FBK_USAGE = u;
    }

    public String getComponent() {
        return FBK_COMPONENT;
    }

    public void setComponent(final String c) {
        FBK_COMPONENT = c;
    }

    public String getContact() {
        return FBK_CONTACT;
    }

    public void setContact(final String c) {
        FBK_CONTACT = c;
    }

    public String getOneThing() {
        return FBK_ONE_THING;
    }

    public void setOneThing(final String s) {
        FBK_ONE_THING = s;
    }

    public Boolean getRecommend() {
        return FBK_RECOMMEND;
    }

    public void setRecommend(final Boolean r) {
        FBK_RECOMMEND = r;
    }

    public boolean isQuoteOk() {
        return FBK_QUOTE_OK;
    }

    public void setQuoteOk(final boolean q) {
        FBK_QUOTE_OK = q;
    }

    public boolean isReviewed() {
        return FBK_REVIEWED;
    }

    public void setReviewed(final boolean r) {
        FBK_REVIEWED = r;
    }

    /** Returns the submission time as a formatted string (yyyy-MM-dd HH:mm UTC). */
    public String getFormattedTime() {
        if (FBK_TIME == null)
            return "";
        final var sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date(FBK_TIME.longValue()));
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof final Feedback f && Objects.equals(FBK_ID, f.FBK_ID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(FBK_ID);
    }
}
