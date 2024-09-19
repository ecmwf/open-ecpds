/*
 * ECaccess Project - EccmdException.java
 *
 * Class: ecmwf.ecbatch.eis.rmi.client.EccmdException
 * Using JDK: 1.8.0_60
 *
 * Copyright (c) 2000-2016 Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 */

package ecmwf.common.ecaccess;

import ecmwf.common.text.Format;

/**
 * The Class EccmdException.
 *
 * @author <a href="mailto:syi@ecmwf.int">Laurent Gougeon</a>
 *
 * @version 4.2.0
 */

public class EccmdException extends Exception {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1909068326708015315L;

    /** The _original message. */
    private final String _originalMessage;

    /**
     * Instantiates a new eccmd exception.
     *
     * @param me
     *            the me
     * @param throwable
     *            the throwable
     */
    public EccmdException(final Object me, final Throwable throwable) {
        super(Format.trimString(throwable.getMessage(), null));
        _originalMessage = throwable.getMessage();
    }

    /**
     * Instantiates a new eccmd exception.
     *
     * @param message
     *            the message
     */
    public EccmdException(final String message) {
        super(Format.trimString(message, "Internal error"));
        _originalMessage = message;
    }

    /**
     * Gets the original message.
     *
     * @return the original message
     */
    public String getOriginalMessage() {
        return _originalMessage;
    }
}
