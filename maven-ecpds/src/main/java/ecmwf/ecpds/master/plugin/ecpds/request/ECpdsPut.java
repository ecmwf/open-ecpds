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
import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;

import ecmwf.common.database.ECpdsBase;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.technical.RandomInputStream;
import ecmwf.ecpds.master.MasterServer;
import ecmwf.ecpds.master.plugin.ecpds.ECpdsClient;

/**
 * The Class ECpdsPut.
 */
public final class ECpdsPut extends ECpdsRequest {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2805183780943113691L;

    /** The Constant _master. */
    private static final MasterServer _master = StarterServer.getInstance(MasterServer.class);

    /** The Constant _dataBase. */
    private static final ECpdsBase _dataBase = _master.getECpdsBase();

    /** The at. */
    protected String _AT;

    /** The destination. */
    protected String _DESTINATION;

    /** The failedonly. */
    protected Boolean _FAILEDONLY;

    /** The force. */
    protected Boolean _FORCE;

    /** The from. */
    protected String _FROM;

    /** The groupby. */
    protected String _GROUPBY;

    /** The group. */
    protected String _GROUP;

    /** The caller. */
    protected String _CALLER;

    /** The hostforacquisition. */
    protected String _HOSTFORACQUISITION;

    /** The identity. */
    protected String _IDENTITY;

    /** The lifetime. */
    protected String _LIFETIME;

    /** The metadata. */
    protected String _METADATA;

    /** The noretrieval. */
    protected Boolean _NORETRIEVAL;

    /** The original. */
    protected String _ORIGINAL;

    /** The priority. */
    protected Integer _PRIORITY;

    /** The priority. */
    protected Integer _INDEX;

    /** The size. */
    protected Long _SIZE;

    /** The source. */
    protected String _SOURCE;

    /** The standby. */
    protected Boolean _STANDBY;

    /** The asap. */
    protected Boolean _ASAP;

    /** The event. */
    protected Boolean _EVENT;

    /** The target. */
    protected String _TARGET;

    /** The timefile. */
    protected String _TIMEFILE;

    /** The uniquename. */
    protected String _UNIQUENAME;

    /** The user. */
    protected String _USER;

    /** The version. */
    protected String _VERSION;

    /** The timestamp. */
    protected Long _TIMESTAMP;

    /** The type. */
    protected String _TYPE;

    /** The format. */
    protected String _FORMAT;

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
        if (_METADATA != null) {
            _METADATA = changeDate(_METADATA, _TIMESTAMP, ratio);
        }
        if (_AT != null) {
            _AT = changeDate(_FORMAT != null ? _FORMAT : "yyyyMMddHHmmss", _AT, _TIMESTAMP, ratio);
        }
        if (_TIMEFILE != null) {
            _TIMEFILE = changeTimefile(_TIMEFILE, _TIMESTAMP, ratio);
        }
        if (_dataBase.getDestinationObject(_DESTINATION) == null) {
            throw new IOException("Destination " + _DESTINATION + " not found");
        }
        if (_HOSTFORACQUISITION != null && _dataBase.getHostObject(_HOSTFORACQUISITION) == null) {
            throw new IOException("Host " + _HOSTFORACQUISITION + " not found");
        }
        _SIZE = _SIZE < 0 ? ThreadLocalRandom.current().nextLong(20971520) : _SIZE;
        ECpdsClient.process(this, new RandomInputStream(_SIZE), true);
    }

    /**
     * Process.
     *
     * @param in
     *            the in
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void process(final InputStream in) throws IOException {
        ECpdsClient.process(this, in, false);
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
     * Gets the format.
     *
     * @return the format
     */
    public String getFORMAT() {
        return _FORMAT;
    }

    /**
     * Sets the format.
     *
     * @param fORMAT
     *            the new format
     */
    public void setFORMAT(final String fORMAT) {
        _FORMAT = fORMAT;
    }

    /**
     * Instantiates a new ecpds put.
     */
    public ECpdsPut() {
        setTIMESTAMP(System.currentTimeMillis());
        setTYPE("PUT");
    }

    /**
     * Instantiates a new ecpds put.
     *
     * @param line
     *            the line
     *
     * @throws IllegalAccessException
     *             the illegal access exception
     */
    public ECpdsPut(final String line) throws IllegalAccessException {
        this();
        fromString(line);
    }

    /**
     * Gets the at.
     *
     * @return the at
     */
    public String getAT() {
        return _AT;
    }

    /**
     * Sets the at.
     *
     * @param aT
     *            the new at
     */
    public void setAT(final String aT) {
        _AT = aT;
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     */
    public String getDESTINATION() {
        return _DESTINATION;
    }

    /**
     * Sets the destination.
     *
     * @param dESTINATION
     *            the new destination
     */
    public void setDESTINATION(final String dESTINATION) {
        _DESTINATION = dESTINATION;
    }

    /**
     * Gets the failedonly.
     *
     * @return the failedonly
     */
    public Boolean getFAILEDONLY() {
        return _FAILEDONLY;
    }

    /**
     * Sets the failedonly.
     *
     * @param fAILEDONLY
     *            the new failedonly
     */
    public void setFAILEDONLY(final Boolean fAILEDONLY) {
        _FAILEDONLY = fAILEDONLY;
    }

    /**
     * Checks if is force.
     *
     * @return the boolean
     */
    public Boolean isFORCE() {
        return _FORCE;
    }

    /**
     * Sets the force.
     *
     * @param fORCE
     *            the new force
     */
    public void setFORCE(final Boolean fORCE) {
        _FORCE = fORCE;
    }

    /**
     * Gets the from.
     *
     * @return the from
     */
    public String getFROM() {
        return _FROM;
    }

    /**
     * Sets the from.
     *
     * @param fROM
     *            the new from
     */
    public void setFROM(final String fROM) {
        _FROM = fROM;
    }

    /**
     * Gets the groupby.
     *
     * @return the groupby
     */
    public String getGROUPBY() {
        return _GROUPBY;
    }

    /**
     * Sets the groupby.
     *
     * @param gROUPBY
     *            the new groupby
     */
    public void setGROUPBY(final String gROUPBY) {
        _GROUPBY = gROUPBY;
    }

    /**
     * Gets the group.
     *
     * @return the group
     */
    public String getGROUP() {
        return _GROUP;
    }

    /**
     * Sets the group.
     *
     * @param gROUP
     *            the new group
     */
    public void setGROUP(final String gROUP) {
        _GROUP = gROUP;
    }

    /**
     * Gets the caller.
     *
     * @return the caller
     */
    public String getCALLER() {
        return _CALLER;
    }

    /**
     * Sets the caller.
     *
     * @param cALLER
     *            the new caller
     */
    public void setCALLER(final String cALLER) {
        _CALLER = cALLER;
    }

    /**
     * Gets the hostforacquisition.
     *
     * @return the hostforacquisition
     */
    public String getHOSTFORACQUISITION() {
        return _HOSTFORACQUISITION;
    }

    /**
     * Sets the hostforacquisition.
     *
     * @param hOSTFORACQUISITION
     *            the new hostforacquisition
     */
    public void setHOSTFORACQUISITION(final String hOSTFORACQUISITION) {
        _HOSTFORACQUISITION = hOSTFORACQUISITION;
    }

    /**
     * Gets the identity.
     *
     * @return the identity
     */
    public String getIDENTITY() {
        return _IDENTITY;
    }

    /**
     * Sets the identity.
     *
     * @param iDENTITY
     *            the new identity
     */
    public void setIDENTITY(final String iDENTITY) {
        _IDENTITY = iDENTITY;
    }

    /**
     * Gets the lifetime.
     *
     * @return the lifetime
     */
    public String getLIFETIME() {
        return _LIFETIME;
    }

    /**
     * Sets the lifetime.
     *
     * @param lIFETIME
     *            the new lifetime
     */
    public void setLIFETIME(final String lIFETIME) {
        _LIFETIME = lIFETIME;
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

    /**
     * Checks if is noretrieval.
     *
     * @return the boolean
     */
    public Boolean isNORETRIEVAL() {
        return _NORETRIEVAL;
    }

    /**
     * Sets the noretrieval.
     *
     * @param nORETRIEVAL
     *            the new noretrieval
     */
    public void setNORETRIEVAL(final Boolean nORETRIEVAL) {
        _NORETRIEVAL = nORETRIEVAL;
    }

    /**
     * Gets the original.
     *
     * @return the original
     */
    public String getORIGINAL() {
        return _ORIGINAL;
    }

    /**
     * Sets the original.
     *
     * @param oRIGINAL
     *            the new original
     */
    public void setORIGINAL(final String oRIGINAL) {
        _ORIGINAL = oRIGINAL;
    }

    /**
     * Gets the priority.
     *
     * @return the priority
     */
    public Integer getPRIORITY() {
        return _PRIORITY;
    }

    /**
     * Sets the priority.
     *
     * @param pRIORITY
     *            the new priority
     */
    public void setPRIORITY(final Integer pRIORITY) {
        _PRIORITY = pRIORITY;
    }

    /**
     * Gets the index.
     *
     * @return the index
     */
    public Integer getINDEX() {
        return _INDEX;
    }

    /**
     * Sets the index.
     *
     * @param iNDEX
     *            the new index
     */
    public void setINDEX(final Integer iNDEX) {
        _INDEX = iNDEX;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public Long getSIZE() {
        return _SIZE;
    }

    /**
     * Sets the size.
     *
     * @param sIZE
     *            the new size
     */
    public void setSIZE(final Long sIZE) {
        _SIZE = sIZE;
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public String getSOURCE() {
        return _SOURCE;
    }

    /**
     * Sets the source.
     *
     * @param sOURCE
     *            the new source
     */
    public void setSOURCE(final String sOURCE) {
        _SOURCE = sOURCE;
    }

    /**
     * Checks if is standby.
     *
     * @return the boolean
     */
    public Boolean isSTANDBY() {
        return _STANDBY;
    }

    /**
     * Sets the standby.
     *
     * @param sTANDBY
     *            the new standby
     */
    public void setSTANDBY(final Boolean sTANDBY) {
        _STANDBY = sTANDBY;
    }

    /**
     * Checks if is asap.
     *
     * @return the boolean
     */
    public Boolean isASAP() {
        return _ASAP;
    }

    /**
     * Checks if is event.
     *
     * @return the boolean
     */
    public Boolean isEVENT() {
        return _EVENT;
    }

    /**
     * Sets the asap.
     *
     * @param aSAP
     *            the new asap
     */
    public void setASAP(final Boolean aSAP) {
        _ASAP = aSAP;
    }

    /**
     * Sets the event.
     *
     * @param eVENT
     *            the new event
     */
    public void setEVENT(final Boolean eVENT) {
        _EVENT = eVENT;
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public String getTARGET() {
        return _TARGET;
    }

    /**
     * Sets the target.
     *
     * @param tARGET
     *            the new target
     */
    public void setTARGET(final String tARGET) {
        _TARGET = tARGET;
    }

    /**
     * Gets the timefile.
     *
     * @return the timefile
     */
    public String getTIMEFILE() {
        return _TIMEFILE;
    }

    /**
     * Sets the timefile.
     *
     * @param tIMEFILE
     *            the new timefile
     */
    public void setTIMEFILE(final String tIMEFILE) {
        _TIMEFILE = tIMEFILE;
    }

    /**
     * Gets the uniquename.
     *
     * @return the uniquename
     */
    public String getUNIQUENAME() {
        return _UNIQUENAME;
    }

    /**
     * Sets the uniquename.
     *
     * @param uNIQUENAME
     *            the new uniquename
     */
    public void setUNIQUENAME(final String uNIQUENAME) {
        _UNIQUENAME = uNIQUENAME;
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
}
