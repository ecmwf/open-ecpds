/*
 * ECaccess Project - ECauthToken.java
 *
 * Class: ecmwf.ecbatch.eis.rmi.client.ECauthToken
 * Using JDK: 1.8.0_60
 *
 * Copyright (c) 2000-2016 Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 */
package ecmwf.common.ecaccess;

import java.io.Serializable;

/**
 * The Class ECauthToken.
 *
 * @author <a href="mailto:syi@ecmwf.int">Laurent Gougeon</a>
 *
 * @version 4.2.0
 */

public final class ECauthToken implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1948857166613608857L;

    /** The Constant COMPLETED. */
    public static final int COMPLETED = 0;

    /** The Constant PASSCODE_REQUIRED. */
    public static final int PASSCODE_REQUIRED = 1;

    /** The Constant PIN_REQUIRED. */
    public static final int PIN_REQUIRED = 2;

    /** The _token. */
    private final byte[] _token;

    /** The _status. */
    private final int _status;

    /** The _time. */
    private final long _time;

    /**
     * Instantiates a new ecauth token.
     *
     * @param token
     *            the token
     * @param status
     *            the status
     */
    public ECauthToken(final byte[] token, final int status) {
        _token = token;
        _status = status;
        _time = System.currentTimeMillis();
    }

    /**
     * Instantiates a new ecauth token.
     *
     * @param token
     *            the token
     * @param status
     *            the status
     */
    public ECauthToken(final String token, final int status) {
        this(token.getBytes(), status);
    }

    /**
     * Checks if is complete.
     *
     * @return true, if is complete
     */
    public boolean isComplete() {
        return _status == COMPLETED;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public int getStatus() {
        return _status;
    }

    /**
     * Gets the token.
     *
     * @return the token
     */
    public byte[] getToken() {
        return _token;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public long getTime() {
        return _time;
    }

    /**
     * Account is disabled.
     *
     * @return true, if successful
     */
    public boolean accountIsDisabled() {
        final var token = new String(_token);
        return token.indexOf("passwd/expired") != -1 || token.indexOf("passwd/disabled") != -1;
    }
}
