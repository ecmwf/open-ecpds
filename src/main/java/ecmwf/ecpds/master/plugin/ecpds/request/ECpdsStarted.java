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

package ecmwf.ecpds.master.plugin.ecpds.request;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;

import ecmwf.ecpds.master.plugin.ecpds.ECpdsClient;

/**
 * The Class ECpdsStarted.
 */
public final class ECpdsStarted extends ECpdsRequest {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 9189108007837808876L;

    /** The version. */
    protected String _VERSION;

    /** The user. */
    protected String _USER;

    /** The metadata. */
    protected String _METADATA;

    /** The timestamp. */
    protected Long _TIMESTAMP;

    /** The type. */
    protected String _TYPE;

    /**
     * {@inheritDoc}
     *
     * Process.
     */
    @Override
    public void process(final long ratio) throws IOException {
        _METADATA = changeDate(_METADATA, _TIMESTAMP, ratio);
        ECpdsClient.process(this);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the timestamp.
     */
    @Override
    public long getTIMESTAMP() {
        return _TIMESTAMP;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the timestamp.
     */
    @Override
    public void setTIMESTAMP(final long tIMESTAMP) {
        _TIMESTAMP = tIMESTAMP;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the type.
     */
    @Override
    public String getTYPE() {
        return _TYPE;
    }

    /**
     * Sets the type.
     *
     * @param tYPE
     *            the new type
     */
    protected void setTYPE(final String tYPE) {
        _TYPE = tYPE;
    }

    /**
     * Instantiates a new ecpds started.
     */
    public ECpdsStarted() {
        setTIMESTAMP(System.currentTimeMillis());
        setTYPE("STARTED");
    }

    /**
     * Instantiates a new ecpds started.
     *
     * @param line
     *            the line
     *
     * @throws java.lang.IllegalAccessException
     *             the illegal access exception
     */
    public ECpdsStarted(final String line) throws IllegalAccessException {
        this();
        fromString(line);
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVERSION() {
        return _VERSION;
    }

    /**
     * Sets the version.
     *
     * @param vERSION
     *            the new version
     */
    public void setVERSION(final String vERSION) {
        _VERSION = vERSION;
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    public String getUSER() {
        return _USER;
    }

    /**
     * Sets the user.
     *
     * @param uSER
     *            the new user
     */
    public void setUSER(final String uSER) {
        _USER = uSER;
    }

    /**
     * Gets the metadata.
     *
     * @return the metadata
     */
    public String getMETADATA() {
        return _METADATA;
    }

    /**
     * Sets the metadata.
     *
     * @param mETADATA
     *            the new metadata
     */
    public void setMETADATA(final String mETADATA) {
        _METADATA = mETADATA;
    }
}
