/*
 * ECaccess Project - ECauthTokenGenerator.java
 *
 * Class: ecmwf.ecbatch.eis.rmi.client.ECauthTokenGenerator
 * Using JDK: 1.8.0_60
 *
 * Copyright (c) 2000-2016 Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 */

package ecmwf.common.ecaccess;

/**
 * The Class ECauthTokenGenerator.
 *
 * @author <a href="mailto:syi@ecmwf.int">Laurent Gougeon</a>
 * @version 4.2.0
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ForkExec;

/**
 * The Class ECauthTokenGenerator.
 */
public final class ECauthTokenGenerator extends ECauthTokenManager {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECauthTokenGenerator.class);

    /** The _command. */
    private static String _command = Cnf.at("Command", "ecauth",
            Cnf.at("Server", "ecauth", Cnf.at("ECauthSession", "cmd")));

    /** The _instance. */
    private static ECauthTokenGenerator _instance = new ECauthTokenGenerator();

    /**
     * Gets the single instance of ECauthTokenGenerator.
     *
     * @return single instance of ECauthTokenGenerator
     */
    public static ECauthTokenGenerator getInstance() {
        return _instance;
    }

    /**
     * Request ECauth token.
     *
     * @param user
     *            the user
     *
     * @return the ecauth token
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public ECauthToken requestECauthToken(final String user) throws IOException {
        if (_command == null) {
            throw new IOException("Command ecauth not defined");
        }
        final var out = new ByteArrayOutputStream();
        final var exec = new ForkExec(new String[] { _command, user });
        try (exec) {
            exec.start();
            exec.writeInto(out);
        } catch (final IOException e) {
            _log.error("ECauth error (out=" + new String(out.toByteArray()) + ")", e);
            throw new IOException("ECauth error");
        }
        final var result = out.toByteArray();
        int code;
        try {
            code = exec.waitFor();
        } catch (final InterruptedException e) {
            code = -1;
        }
        if (code != 0 || !new String(result).startsWith("ECAUTH ")) {
            _log.error("ECauth error (code=" + code + ",output=" + new String(result) + ")");
            throw new IOException("ECauth error");
        }
        return new ECauthToken(result, ECauthToken.COMPLETED);
    }
}