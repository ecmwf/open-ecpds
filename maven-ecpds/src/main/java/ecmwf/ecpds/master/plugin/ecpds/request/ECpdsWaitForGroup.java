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
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;

import ecmwf.ecpds.master.plugin.ecpds.ECpdsClient;

/**
 * The Class ECpdsWaitForGroup.
 */
public final class ECpdsWaitForGroup extends ECpdsRequest {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2447202184275863972L;

    /** The user. */
    protected String _USER;

    /** The version. */
    protected String _VERSION;

    /** The waitforgroup. */
    protected String _WAITFORGROUP;

    /** The timestamp. */
    protected Long _TIMESTAMP;

    /** The type. */
    protected String _TYPE;

    /**
     * Process.
     *
     * @param ratio
     *            the ratio
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void process(final long ratio) throws IOException {
        ECpdsClient.process(this);
    }

    /**
     * Gets the timestamp.
     *
     * @return the timestamp
     */
    @Override
    public long getTIMESTAMP() {
        return _TIMESTAMP;
    }

    /**
     * Sets the timestamp.
     *
     * @param tIMESTAMP
     *            the new timestamp
     */
    @Override
    public void setTIMESTAMP(final long tIMESTAMP) {
        _TIMESTAMP = tIMESTAMP;
    }

    /**
     * Gets the type.
     *
     * @return the type
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
     * Instantiates a new ecpds wait for group.
     */
    public ECpdsWaitForGroup() {
        setTIMESTAMP(System.currentTimeMillis());
        setTYPE("WAITFORGROUP");
    }

    /**
     * Instantiates a new ecpds wait for group.
     *
     * @param line
     *            the line
     *
     * @throws IllegalAccessException
     *             the illegal access exception
     */
    public ECpdsWaitForGroup(final String line) throws IllegalAccessException {
        this();
        fromString(line);
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
     * Gets the waitforgroup.
     *
     * @return the waitforgroup
     */
    public String getWAITFORGROUP() {
        return _WAITFORGROUP;
    }

    /**
     * Sets the waitforgroup.
     *
     * @param wAITFORGROUP
     *            the new waitforgroup
     */
    public void setWAITFORGROUP(final String wAITFORGROUP) {
        _WAITFORGROUP = wAITFORGROUP;
    }
}
