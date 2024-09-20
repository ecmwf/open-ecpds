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

import java.io.IOException;

/**
 * The Class TelnetClientHandler.
 */
public abstract class TelnetClientHandler {
    /** The one. */
    private static byte[] one = new byte[1];

    /** The Constant STATE_DATA. */
    private static final byte STATE_DATA = 0;

    /** The Constant STATE_IAC. */
    private static final byte STATE_IAC = 1;

    /** The Constant STATE_IACSB. */
    private static final byte STATE_IACSB = 2;

    /** The Constant STATE_IACWILL. */
    private static final byte STATE_IACWILL = 3;

    /** The Constant STATE_IACDO. */
    private static final byte STATE_IACDO = 4;

    /** The Constant STATE_IACWONT. */
    private static final byte STATE_IACWONT = 5;

    /** The Constant STATE_IACDONT. */
    private static final byte STATE_IACDONT = 6;

    /** The Constant STATE_IACSBIAC. */
    private static final byte STATE_IACSBIAC = 7;

    /** The Constant STATE_IACSBDATA. */
    private static final byte STATE_IACSBDATA = 8;

    /** The Constant STATE_IACSBDATAIAC. */
    private static final byte STATE_IACSBDATAIAC = 9;

    /** The Constant IAC. */
    private static final byte IAC = (byte) 255;

    /** The Constant EOR. */
    private static final byte EOR = (byte) 239;

    /** The Constant WILL. */
    private static final byte WILL = (byte) 251;

    /** The Constant WONT. */
    private static final byte WONT = (byte) 252;

    /** The Constant DO. */
    private static final byte DO = (byte) 253;

    /** The Constant DONT. */
    private static final byte DONT = (byte) 254;

    /** The Constant SB. */
    private static final byte SB = (byte) 250;

    /** The Constant SE. */
    private static final byte SE = (byte) 240;

    /** The Constant TELOPT_BINARY. */
    public static final byte TELOPT_BINARY = (byte) 0;

    /** The Constant TELOPT_ECHO. */
    public static final byte TELOPT_ECHO = (byte) 1;

    /** The Constant TELOPT_SGA. */
    public static final byte TELOPT_SGA = (byte) 3;

    /** The Constant TELOPT_EOR. */
    public static final byte TELOPT_EOR = (byte) 25;

    /** The Constant TELOPT_NAWS. */
    public static final byte TELOPT_NAWS = (byte) 31;

    /** The Constant TELOPT_TTYPE. */
    public static final byte TELOPT_TTYPE = (byte) 24;

    /** The Constant IACSB. */
    private static final byte[] IACSB = { IAC, SB };

    /** The Constant IACSE. */
    private static final byte[] IACSE = { IAC, SE };

    /** The Constant TELQUAL_IS. */
    private static final byte TELQUAL_IS = (byte) 0;

    /** The Constant TELQUAL_SEND. */
    private static final byte TELQUAL_SEND = (byte) 1;

    /** The Constant ENVQUAL_USERVAR. */
    private static final byte ENVQUAL_USERVAR = (byte) 3;

    /** The Constant ENVQUAL_VALUE. */
    private static final byte ENVQUAL_VALUE = (byte) 1;

    /** The Constant TELOPT_ENVIRON. */
    private static final byte TELOPT_ENVIRON = (byte) 36;

    /** The Constant TELOPT_XDISPLOC. */
    private static final byte TELOPT_XDISPLOC = (byte) 35;

    /** The tempbuf. */
    private byte[] tempbuf = {};

    /** The crlf. */
    private byte[] crlf = new byte[2];

    /** The cr. */
    private byte[] cr = new byte[2];

    // ===================================================================
    // the actual negotiation handling for the telnet protocol follows:
    // ===================================================================

    /** The neg_state. */
    private byte neg_state = 0;

    /** The current_sb. */
    private byte current_sb;

    /** The received dx. */
    private byte[] receivedDX;

    /** The received wx. */
    private byte[] receivedWX;

    /** The sent dx. */
    private byte[] sentDX;

    /** The sent wx. */
    private byte[] sentWX;

    /**
     * Instantiates a new telnet client handler.
     */
    public TelnetClientHandler() {
        reset();
        crlf[0] = 13;
        crlf[1] = 10;
        cr[0] = 13;
        cr[1] = 0;
    }

    /**
     * Gets the terminal type.
     *
     * @return the terminal type
     */
    protected abstract String getTerminalType();

    /**
     * Gets the window size.
     *
     * @return the window size
     */
    protected abstract TelnetDimension getWindowSize();

    /**
     * Sets the local echo.
     *
     * @param echo
     *            the new local echo
     */
    protected abstract void setLocalEcho(boolean echo);

    /**
     * Notify end of record.
     */
    protected abstract void notifyEndOfRecord();

    /**
     * Write.
     *
     * @param b
     *            the b
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected abstract void write(byte[] b) throws IOException;

    /**
     * Write.
     *
     * @param b
     *            the b
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void write(final byte b) throws IOException {
        one[0] = b;
        write(one);
    }

    /**
     * Reset.
     */
    public void reset() {
        neg_state = 0;
        receivedDX = new byte[256];
        sentDX = new byte[256];
        receivedWX = new byte[256];
        sentWX = new byte[256];
    }

    /**
     * Sends the telnet control.
     *
     * @param code
     *            the code
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void sendTelnetControl(final byte code) throws IOException {
        final var b = new byte[2];
        b[0] = IAC;
        b[1] = code;
        write(b);
    }

    /**
     * Handle_sb.
     *
     * @param type
     *            the type
     * @param sbdata
     *            the sbdata
     * @param sbcount
     *            the sbcount
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void handle_sb(final byte type, final byte[] sbdata, final int sbcount) throws IOException {
        switch (type) {
        case TELOPT_TTYPE:
            if (sbcount > 0 && sbdata[0] == TELQUAL_SEND) {
                write(IACSB);
                write(TELOPT_TTYPE);
                write(TELQUAL_IS);
                var ttype = getTerminalType();
                if (ttype == null) {
                    ttype = "dumb";
                }
                write(ttype.getBytes());
                write(IACSE);
            }
        }
    }

    /**
     * Startup.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void startup() throws IOException {
    }

    /**
     * Transpose.
     *
     * @param buf
     *            the buf
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void transpose(final byte[] buf) throws IOException {
        int i;
        byte[] nbuf;
        byte[] xbuf;
        var nbufptr = 0;
        nbuf = new byte[buf.length * 2];
        for (i = 0; i < buf.length; i++) {
            switch (buf[i]) {
            // Escape IAC twice in stream ... to be telnet protocol compliant
            // this is there in binary and non-binary mode.
            case IAC:
                nbuf[nbufptr++] = IAC;
                nbuf[nbufptr++] = IAC;
                break;
            // We need to heed RFC 854. LF (\n) is 10, CR (\r) is 13
            // we assume that the Terminal sends \n for lf+cr and \r for just cr
            // linefeed+carriage return is CR LF */
            case 10: // \n
                if (receivedDX[TELOPT_BINARY + 128] != DO) {
                    while (nbuf.length - nbufptr < crlf.length) {
                        xbuf = new byte[nbuf.length * 2];
                        System.arraycopy(nbuf, 0, xbuf, 0, nbufptr);
                        nbuf = xbuf;
                    }
                    for (final byte element : crlf) {
                        nbuf[nbufptr++] = element;
                    }
                    break;
                }
                // copy verbatim in binary mode.
                nbuf[nbufptr++] = buf[i];
                break;
            // carriage return is CR NUL */
            case 13: // \r
                if (receivedDX[TELOPT_BINARY + 128] != DO) {
                    while (nbuf.length - nbufptr < cr.length) {
                        xbuf = new byte[nbuf.length * 2];
                        System.arraycopy(nbuf, 0, xbuf, 0, nbufptr);
                        nbuf = xbuf;
                    }
                    for (final byte element : cr) {
                        nbuf[nbufptr++] = element;
                    }
                } else {
                    // copy verbatim in binary mode.
                    nbuf[nbufptr++] = buf[i];
                }
                break;
            // all other characters are just copied
            default:
                nbuf[nbufptr++] = buf[i];
                break;
            }
        }
        xbuf = new byte[nbufptr];
        System.arraycopy(nbuf, 0, xbuf, 0, nbufptr);
        write(xbuf);
    }

    /**
     * Sets the crlf.
     *
     * @param xcrlf
     *            the new crlf
     */
    public void setCRLF(final String xcrlf) {
        crlf = xcrlf.getBytes();
    }

    /**
     * Sets the cr.
     *
     * @param xcr
     *            the new cr
     */
    public void setCR(final String xcr) {
        cr = xcr.getBytes();
    }

    /**
     * Negotiate.
     *
     * @param nbuf
     *            the nbuf
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public int negotiate(final byte[] nbuf) throws IOException {
        final var sbbuf = new byte[tempbuf.length];
        final var count = tempbuf.length;
        final var buf = tempbuf;
        final var sendbuf = new byte[3];
        byte b;
        byte reply;
        var sbcount = 0;
        var boffset = 0;
        var noffset = 0;
        var dobreak = false;
        if (count == 0) { // buffer is empty.
            return -1;
        }
        while (!dobreak && boffset < count && noffset < nbuf.length) {
            b = buf[boffset++];
            // of course, byte is a signed entity (-128 -> 127)
            // but apparently the SGI Netscape 3.0 doesn't seem
            // to care and provides happily values up to 255
            if (b >= 128) {
                b = (byte) (b - 256);
            }
            switch (neg_state) {
            case STATE_DATA:
                if (b == IAC) {
                    neg_state = STATE_IAC;
                    dobreak = true; // leave the loop so we can sync.
                } else {
                    nbuf[noffset++] = b;
                }
                break;
            case STATE_IAC:
                switch (b) {
                case IAC:
                    neg_state = STATE_DATA;
                    nbuf[noffset++] = IAC;
                    break;
                case WILL:
                    neg_state = STATE_IACWILL;
                    break;
                case WONT:
                    neg_state = STATE_IACWONT;
                    break;
                case DONT:
                    neg_state = STATE_IACDONT;
                    break;
                case DO:
                    neg_state = STATE_IACDO;
                    break;
                case EOR:
                    notifyEndOfRecord();
                    dobreak = true; // leave the loop so we can sync.
                    neg_state = STATE_DATA;
                    break;
                case SB:
                    neg_state = STATE_IACSB;
                    sbcount = 0;
                    break;
                default:
                    neg_state = STATE_DATA;
                    break;
                }
                break;
            case STATE_IACWILL:
                switch (b) {
                case TELOPT_ECHO:
                    reply = DO;
                    setLocalEcho(false);
                    break;
                case TELOPT_SGA:
                    reply = DO;
                    break;
                case TELOPT_EOR:
                    reply = DO;
                    break;
                case TELOPT_BINARY:
                    reply = DO;
                    break;
                default:
                    reply = DONT;
                    break;
                }
                if (reply != sentDX[b + 128] || WILL != receivedWX[b + 128]) {
                    sendbuf[0] = IAC;
                    sendbuf[1] = reply;
                    sendbuf[2] = b;
                    write(sendbuf);
                    sentDX[b + 128] = reply;
                    receivedWX[b + 128] = WILL;
                }
                neg_state = STATE_DATA;
                break;
            case STATE_IACWONT:
                reply = switch (b) {
                case TELOPT_ECHO -> {
                    setLocalEcho(true);
                    yield DONT;
                }
                case TELOPT_SGA -> DONT;
                case TELOPT_EOR -> DONT;
                case TELOPT_BINARY -> DONT;
                default -> DONT;
                };
                if (reply != sentDX[b + 128] || WONT != receivedWX[b + 128]) {
                    sendbuf[0] = IAC;
                    sendbuf[1] = reply;
                    sendbuf[2] = b;
                    write(sendbuf);
                    sentDX[b + 128] = reply;
                    receivedWX[b + 128] = WILL;
                }
                neg_state = STATE_DATA;
                break;
            case STATE_IACDO:
                switch (b) {
                case TELOPT_ECHO:
                    reply = WILL;
                    setLocalEcho(true);
                    break;
                case TELOPT_SGA:
                    reply = WILL;
                    break;
                case TELOPT_TTYPE:
                    reply = WILL;
                    break;
                case TELOPT_BINARY:
                    reply = WILL;
                    break;
                case TELOPT_NAWS:
                    final var size = getWindowSize();
                    receivedDX[b] = DO;
                    if (size == null) {
                        // this shouldn't happen
                        write(IAC);
                        write(WONT);
                        write(TELOPT_NAWS);
                        reply = WONT;
                        sentWX[b] = WONT;
                        break;
                    }
                    reply = WILL;
                    sentWX[b] = WILL;
                    sendbuf[0] = IAC;
                    sendbuf[1] = WILL;
                    sendbuf[2] = TELOPT_NAWS;
                    write(sendbuf);
                    write(IAC);
                    write(SB);
                    write(TELOPT_NAWS);
                    write((byte) (size.width >> 8));
                    write((byte) (size.width & 0xff));
                    write((byte) (size.height >> 8));
                    write((byte) (size.height & 0xff));
                    write(IAC);
                    write(SE);
                    break;
                default:
                    reply = WONT;
                    break;
                }
                if (reply != sentWX[128 + b] || DO != receivedDX[128 + b]) {
                    sendbuf[0] = IAC;
                    sendbuf[1] = reply;
                    sendbuf[2] = b;
                    write(sendbuf);
                    sentWX[b + 128] = reply;
                    receivedDX[b + 128] = DO;
                }
                neg_state = STATE_DATA;
                break;
            case STATE_IACDONT:
                switch (b) {
                case TELOPT_ECHO:
                    reply = WONT;
                    setLocalEcho(false);
                    break;
                case TELOPT_SGA:
                    reply = WONT;
                    break;
                case TELOPT_NAWS:
                    reply = WONT;
                    break;
                case TELOPT_BINARY:
                    reply = WONT;
                    break;
                default:
                    reply = WONT;
                    break;
                }
                if (reply != sentWX[b + 128] || DONT != receivedDX[b + 128]) {
                    write(IAC);
                    write(reply);
                    write(b);
                    sentWX[b + 128] = reply;
                    receivedDX[b + 128] = DONT;
                }
                neg_state = STATE_DATA;
                break;
            case STATE_IACSBIAC:
                if (b == IAC) {
                    sbcount = 0;
                    current_sb = b;
                    neg_state = STATE_IACSBDATA;
                } else {
                    // System.err.println("(bad) "+b+" ");
                    neg_state = STATE_DATA;
                }
                break;
            case STATE_IACSB:
                neg_state = switch (b) {
                case IAC -> STATE_IACSBIAC;
                default -> {
                    current_sb = b;
                    sbcount = 0;
                    yield STATE_IACSBDATA;
                }
                };
                break;
            case STATE_IACSBDATA:
                switch (b) {
                case IAC:
                    neg_state = STATE_IACSBDATAIAC;
                    break;
                default:
                    sbbuf[sbcount++] = b;
                    break;
                }
                break;
            case STATE_IACSBDATAIAC:
                switch (b) {
                case IAC:
                    neg_state = STATE_IACSBDATA;
                    sbbuf[sbcount++] = IAC;
                    break;
                case SE:
                    handle_sb(current_sb, sbbuf, sbcount);
                    current_sb = 0;
                    neg_state = STATE_DATA;
                    break;
                case SB:
                    handle_sb(current_sb, sbbuf, sbcount);
                    neg_state = STATE_IACSB;
                    break;
                default:
                    neg_state = STATE_DATA;
                    break;
                }
                break;
            default:
                neg_state = STATE_DATA;
                break;
            }
        }
        // shrink tempbuf to new processed size.
        final var xb = new byte[count - boffset];
        System.arraycopy(tempbuf, boffset, xb, 0, count - boffset);
        tempbuf = xb;
        return noffset;
    }

    /**
     * Inputfeed.
     *
     * @param b
     *            the b
     * @param len
     *            the len
     */
    public void inputfeed(final byte[] b, final int len) {
        final var xb = new byte[tempbuf.length + len];
        System.arraycopy(tempbuf, 0, xb, 0, tempbuf.length);
        System.arraycopy(b, 0, xb, tempbuf.length, len);
        tempbuf = xb;
    }

    /**
     * Sends the display location.
     *
     * @param display
     *            the display
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void sendDisplayLocation(final String display) throws IOException {
        final var size = display.length();
        final var b = new byte[6 + size];
        b[0] = IAC;
        b[1] = SB;
        b[2] = TELOPT_XDISPLOC;
        b[3] = TELQUAL_IS;
        System.arraycopy(display.getBytes(), 0, b, 4, size);
        b[b.length - 2] = IAC;
        b[b.length - 1] = SE;
        write(b);
    }

    /**
     * Sends the env.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void sendEnv(final String name, final String value) throws IOException {
        final var sizen = name.length();
        final var sizev = value.length();
        final var b = new byte[8 + sizev + sizen];
        b[0] = IAC;
        b[1] = SB;
        b[2] = TELOPT_ENVIRON;
        b[3] = TELQUAL_IS;
        b[4] = ENVQUAL_USERVAR;
        System.arraycopy(name.getBytes(), 0, b, 5, sizen);
        b[5 + sizen] = ENVQUAL_VALUE;
        System.arraycopy(value.getBytes(), 0, b, 6 + sizen, sizev);
        b[6 + sizen + sizev] = IAC;
        b[7 + sizen + sizev] = SE;
        write(b);
    }

    /**
     * Sends the option.
     *
     * @param option
     *            the option
     * @param state
     *            the state
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void sendOption(final char option, final boolean state) throws IOException {
        final var b = new byte[3];
        b[0] = IAC;
        b[1] = state ? WILL : WONT;
        b[2] = String.valueOf(option).getBytes()[0];
        write(b);
    }

    /**
     * Sub negotiation.
     *
     * @param option
     *            the option
     * @param params
     *            the params
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void subNegotiation(final char option, final char[] params) throws IOException {
        final var length = params.length;
        final var b = new byte[5 + length];
        b[0] = IAC;
        b[1] = SB;
        b[2] = String.valueOf(option).getBytes()[0];
        for (var i = 0; i < length; i++) {
            b[3 + i] = String.valueOf(params[i]).getBytes()[0];
        }
        b[3 + length] = IAC;
        b[4 + length] = SE;
        write(b);
    }

    /**
     * Sends the option request.
     *
     * @param option
     *            the option
     * @param state
     *            the state
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void sendOptionRequest(final byte option, final boolean state) throws IOException {
        final var b = new byte[3];
        b[0] = IAC;
        b[1] = state ? DO : DONT;
        b[2] = option;
        write(b);
    }
}
