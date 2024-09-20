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

package ecmwf.ecpds.mover.plugin.http;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Date;

/**
 * The Class GetOptions.
 */
public class GetOptions {

    /** The if E tag matches. */
    private String _ifETagMatches;

    /** The if E tag doesnt match. */
    private String _ifETagDoesntMatch;

    /** The if modified since. */
    private Date _ifModifiedSince;

    /** The if unmodified since. */
    private Date _ifUnmodifiedSince;

    /** The range from. */
    private long _rangeFrom = 0; // By default from beginning. If <0 then it is a tail!

    /** The range to. */
    private long _rangeTo = -1; // If default value then this mean that no range is defined

    /**
     * If E tag matches.
     *
     * @param ifETagMatches
     *            the if E tag matches
     */
    public void ifETagMatches(final String ifETagMatches) {
        _ifETagMatches = ifETagMatches;
    }

    /**
     * If E tag doesnt match.
     *
     * @param ifETagDoesntMatch
     *            the if E tag doesnt match
     */
    public void ifETagDoesntMatch(final String ifETagDoesntMatch) {
        _ifETagDoesntMatch = ifETagDoesntMatch;
    }

    /**
     * If modified since.
     *
     * @param ifModifiedSince
     *            the if modified since
     */
    public void ifModifiedSince(final Date ifModifiedSince) {
        _ifModifiedSince = ifModifiedSince;
    }

    /**
     * If unmodified since.
     *
     * @param ifUnmodifiedSince
     *            the if unmodified since
     */
    public void ifUnmodifiedSince(final Date ifUnmodifiedSince) {
        _ifUnmodifiedSince = ifUnmodifiedSince;
    }

    /**
     * Tail.
     *
     * @param tail
     *            the tail
     */
    public void tail(final long tail) {
        _rangeFrom = tail * -1;
    }

    /**
     * Start at.
     *
     * @param startAt
     *            the start at
     */
    public void startAt(final long startAt) {
        _rangeFrom = startAt;
    }

    /**
     * Range.
     *
     * @param rangeFrom
     *            the range from
     * @param rangeTo
     *            the range to
     */
    public void range(final long rangeFrom, final long rangeTo) {
        _rangeFrom = rangeFrom;
        _rangeTo = rangeTo;
    }

    /**
     * Gets the if E tag matches.
     *
     * @return the if E tag matches
     */
    public String getIfETagMatches() {
        return _ifETagMatches;
    }

    /**
     * Gets the if E tag doesnt match.
     *
     * @return the if E tag doesnt match
     */
    public String getIfETagDoesntMatch() {
        return _ifETagDoesntMatch;
    }

    /**
     * Gets the if modified since.
     *
     * @return the if modified since
     */
    public Date getIfModifiedSince() {
        return _ifModifiedSince;
    }

    /**
     * Gets the if unmodified since.
     *
     * @return the if unmodified since
     */
    public Date getIfUnmodifiedSince() {
        return _ifUnmodifiedSince;
    }

    /**
     * Gets the range from.
     *
     * @return the range from
     */
    public long getRangeFrom() {
        return _rangeFrom;
    }

    /**
     * Gets the range to.
     *
     * @return the range to
     */
    public long getRangeTo() {
        return _rangeTo;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "GetOptions [_ifETagMatches=" + _ifETagMatches + ", _ifETagDoesntMatch=" + _ifETagDoesntMatch
                + ", _ifModifiedSince=" + _ifModifiedSince + ", _ifUnmodifiedSince=" + _ifUnmodifiedSince
                + ", _rangeFrom=" + _rangeFrom + ", _rangeTo=" + _rangeTo + "]";
    }
}
