/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 * This package is based on the work done by Keiron Liddle, Aftex Software
 * <keiron@aftexsw.com> to whom the Ant project is very grateful for his
 * great code.
 */

package ecmwf.common.compression.bzip2a;

/**
 * An input stream that decompresses from the BZip2 format (without the file
 * header chars) to be read as any other stream.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 */

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class CBZip2InputStream.
 */
public final class CBZip2InputStream extends InputStream implements BZip2Constants {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(CBZip2InputStream.class);

    /**
     * Cadvise.
     */
    private static void cadvise() {
        _log.error("CRC Error");
    }

    /**
     * Compressed stream EOF.
     */
    private static void compressedStreamEOF() {
        cadvise();
    }

    /**
     * Make maps.
     */
    private void makeMaps() {
        int i;
        nInUse = 0;
        for (i = 0; i < 256; i++) {
            if (inUse[i]) {
                seqToUnseq[nInUse] = (char) i;
                unseqToSeq[i] = (char) nInUse;
                nInUse++;
            }
        }
    }

    /** Index of the last char in the block, so the block size == last + 1.. */
    private int last;

    /** Index in zptr[] of original string after sorting. */
    private int origPtr;

    /**
     * The block size 100 k. Always in the range 0 .. 9. The current block size is 100000 * this number.
     */
    private int blockSize100k;

    /** The block randomised. */
    private boolean blockRandomised;

    /** The bs buff. */
    private int bsBuff;

    /** The bs live. */
    private int bsLive;

    /** The m crc. */
    private final CRC mCrc = new CRC();

    /** The in use. */
    private final boolean[] inUse = new boolean[256];

    /** The n in use. */
    private int nInUse;

    /** The seq to unseq. */
    private final char[] seqToUnseq = new char[256];

    /** The unseq to seq. */
    private final char[] unseqToSeq = new char[256];

    /** The selector. */
    private final char[] selector = new char[MAX_SELECTORS];

    /** The selector mtf. */
    private final char[] selectorMtf = new char[MAX_SELECTORS];

    /** The tt. */
    private int[] tt;

    /** The ll 8. */
    private char[] ll8;

    /**
     * The unzftab. Freq table collected to save a pass over the data during decompression.
     */
    private final int[] unzftab = new int[256];

    /** The limit. */
    private final int[][] limit = new int[N_GROUPS][MAX_ALPHA_SIZE];

    /** The base. */
    private final int[][] base = new int[N_GROUPS][MAX_ALPHA_SIZE];

    /** The perm. */
    private final int[][] perm = new int[N_GROUPS][MAX_ALPHA_SIZE];

    /** The min lens. */
    private final int[] minLens = new int[N_GROUPS];

    /** The bs stream. */
    private InputStream bsStream;

    /** The stream end. */
    private boolean streamEnd = false;

    /** The current char. */
    private int currentChar = -1;

    /** The Constant START_BLOCK_STATE. */
    private static final int START_BLOCK_STATE = 1;

    /** The Constant RAND_PART_A_STATE. */
    private static final int RAND_PART_A_STATE = 2;

    /** The Constant RAND_PART_B_STATE. */
    private static final int RAND_PART_B_STATE = 3;

    /** The Constant RAND_PART_C_STATE. */
    private static final int RAND_PART_C_STATE = 4;

    /** The Constant NO_RAND_PART_A_STATE. */
    private static final int NO_RAND_PART_A_STATE = 5;

    /** The Constant NO_RAND_PART_B_STATE. */
    private static final int NO_RAND_PART_B_STATE = 6;

    /** The Constant NO_RAND_PART_C_STATE. */
    private static final int NO_RAND_PART_C_STATE = 7;

    /** The current state. */
    private int currentState = START_BLOCK_STATE;

    /** The stored block CRC. */
    private int storedBlockCRC;

    /** The computed combined CRC. */
    private int computedBlockCRC, computedCombinedCRC;

    /** The ch 2. */
    private int i2, count, chPrev, ch2;

    /** The t pos. */
    private int i, tPos;

    /** The r N to go. */
    private int rNToGo = 0;

    /** The r T pos. */
    private int rTPos = 0;

    /** The j 2. */
    private int j2;

    /** The z. */
    private char z;

    /**
     * Instantiates a new CB zip 2 input stream.
     *
     * @param zStream
     *            the z stream
     */
    public CBZip2InputStream(final InputStream zStream) {
        ll8 = null;
        tt = null;
        bsSetStream(zStream);
        initialize();
        initBlock();
        setupBlock();
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read() {
        if (streamEnd) {
            return -1;
        }
        final var retChar = currentChar;
        switch (currentState) {
        case START_BLOCK_STATE:
            break;
        case RAND_PART_A_STATE:
            break;
        case RAND_PART_B_STATE:
            setupRandPartB();
            break;
        case RAND_PART_C_STATE:
            setupRandPartC();
            break;
        case NO_RAND_PART_A_STATE:
            break;
        case NO_RAND_PART_B_STATE:
            setupNoRandPartB();
            break;
        case NO_RAND_PART_C_STATE:
            setupNoRandPartC();
            break;
        default:
            break;
        }
        return retChar;
    }

    /**
     * Initialize.
     */
    private void initialize() {
        final char magic1, magic2, magic3, magic4;
        magic1 = bsGetUChar();
        magic2 = bsGetUChar();
        magic3 = bsGetUChar();
        magic4 = bsGetUChar();
        if (magic1 != 'B' || magic2 != 'Z' || magic3 != 'h' || magic4 < '1' || magic4 > '9') {
            bsFinishedWithStream();
            streamEnd = true;
            return;
        }

        setDecompressStructureSizes(magic4 - '0');
        computedCombinedCRC = 0;
    }

    /**
     * Inits the block.
     */
    private void initBlock() {
        final char magic1, magic2, magic3, magic4;
        final char magic5, magic6;
        magic1 = bsGetUChar();
        magic2 = bsGetUChar();
        magic3 = bsGetUChar();
        magic4 = bsGetUChar();
        magic5 = bsGetUChar();
        magic6 = bsGetUChar();
        if (magic1 == 0x17 && magic2 == 0x72 && magic3 == 0x45 && magic4 == 0x38 && magic5 == 0x50 && magic6 == 0x90) {
            complete();
            return;
        }

        if (magic1 != 0x31 || magic2 != 0x41 || magic3 != 0x59 || magic4 != 0x26 || magic5 != 0x53 || magic6 != 0x59) {
            badBlockHeader();
            streamEnd = true;
            return;
        }

        storedBlockCRC = bsGetInt32();

        if (bsR(1) == 1) {
            blockRandomised = true;
        } else {
            blockRandomised = false;
        }

        // currBlockNo++;
        getAndMoveToFrontDecode();

        mCrc.initialiseCRC();
        currentState = START_BLOCK_STATE;
    }

    /**
     * End block.
     */
    private void endBlock() {
        computedBlockCRC = mCrc.getFinalCRC();
        /* A bad CRC is considered a fatal error. */
        if (storedBlockCRC != computedBlockCRC) {
            crcError();
        }

        computedCombinedCRC = computedCombinedCRC << 1 | computedCombinedCRC >>> 31;
        computedCombinedCRC ^= computedBlockCRC;
    }

    /**
     * Complete.
     */
    private void complete() {
        var storedCombinedCRC = bsGetInt32();
        if (storedCombinedCRC != computedCombinedCRC) {
            crcError();
        }

        bsFinishedWithStream();
        streamEnd = true;
    }

    /**
     * Block overrun.
     */
    private static void blockOverrun() {
        cadvise();
    }

    /**
     * Bad block header.
     */
    private static void badBlockHeader() {
        cadvise();
    }

    /**
     * Crc error.
     */
    private static void crcError() {
        cadvise();
    }

    /**
     * Bs finished with stream.
     */
    private void bsFinishedWithStream() {
        try {
            if ((this.bsStream != null) && (this.bsStream != System.in)) {
                this.bsStream.close();
                this.bsStream = null;
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

    /**
     * Bs set stream.
     *
     * @param f
     *            the f
     */
    private void bsSetStream(final InputStream f) {
        bsStream = f;
        bsLive = 0;
        bsBuff = 0;
    }

    /**
     * Bs R.
     *
     * @param n
     *            the n
     *
     * @return the int
     */
    private int bsR(final int n) {
        final int v;
        while (bsLive < n) {
            final int zzi;
            var res = 0;
            char thech = 0;
            try {
                res = bsStream.read();
            } catch (final IOException e) {
                compressedStreamEOF();
            }
            if (res == -1) {
                compressedStreamEOF();
            }
            thech = (char) res;
            zzi = thech;
            bsBuff = bsBuff << 8 | zzi & 0xff;
            bsLive += 8;
        }

        v = bsBuff >> bsLive - n & (1 << n) - 1;
        bsLive -= n;
        return v;
    }

    /**
     * Bs get U char.
     *
     * @return the char
     */
    private char bsGetUChar() {
        return (char) bsR(8);
    }

    /**
     * Bs getint.
     *
     * @return the int
     */
    private int bsGetint() {
        var u = 0;
        u = u << 8 | bsR(8);
        u = u << 8 | bsR(8);
        u = u << 8 | bsR(8);
        return u << 8 | bsR(8);
    }

    /**
     * Bs get int VS.
     *
     * @param numBits
     *            the num bits
     *
     * @return the int
     */
    private int bsGetIntVS(final int numBits) {
        return bsR(numBits);
    }

    /**
     * Bs get int 32.
     *
     * @return the int
     */
    private int bsGetInt32() {
        return bsGetint();
    }

    /**
     * Hb create decode tables.
     *
     * @param limit
     *            the limit
     * @param base
     *            the base
     * @param perm
     *            the perm
     * @param length
     *            the length
     * @param minLen
     *            the min len
     * @param maxLen
     *            the max len
     * @param alphaSize
     *            the alpha size
     */
    private void hbCreateDecodeTables(final int[] limit, final int[] base, final int[] perm, final char[] length,
            final int minLen, final int maxLen, final int alphaSize) {
        int pp, i, j, vec;

        pp = 0;
        for (i = minLen; i <= maxLen; i++) {
            for (j = 0; j < alphaSize; j++) {
                if (length[j] == i) {
                    perm[pp] = j;
                    pp++;
                }
            }
        }

        for (i = 0; i < MAX_CODE_LEN; i++) {
            base[i] = 0;
        }
        for (i = 0; i < alphaSize; i++) {
            base[length[i] + 1]++;
        }

        for (i = 1; i < MAX_CODE_LEN; i++) {
            base[i] += base[i - 1];
        }

        for (i = 0; i < MAX_CODE_LEN; i++) {
            limit[i] = 0;
        }
        vec = 0;

        for (i = minLen; i <= maxLen; i++) {
            vec += base[i + 1] - base[i];
            limit[i] = vec - 1;
            vec <<= 1;
        }
        for (i = minLen + 1; i <= maxLen; i++) {
            base[i] = (limit[i - 1] + 1 << 1) - base[i];
        }
    }

    /**
     * Recv decoding tables.
     */
    private void recvDecodingTables() {
        final char len[][] = new char[N_GROUPS][MAX_ALPHA_SIZE];
        int i, j, t, nGroups, nSelectors, alphaSize;
        int minLen, maxLen;
        final var inUse16 = new boolean[16];

        /* Receive the mapping table */
        for (i = 0; i < 16; i++) {
            if (bsR(1) == 1) {
                inUse16[i] = true;
            } else {
                inUse16[i] = false;
            }
        }

        for (i = 0; i < 256; i++) {
            inUse[i] = false;
        }

        for (i = 0; i < 16; i++) {
            if (inUse16[i]) {
                for (j = 0; j < 16; j++) {
                    if (bsR(1) == 1) {
                        inUse[i * 16 + j] = true;
                    }
                }
            }
        }

        makeMaps();
        alphaSize = nInUse + 2;

        /* Now the selectors */
        nGroups = bsR(3);
        nSelectors = bsR(15);
        for (i = 0; i < nSelectors; i++) {
            j = 0;
            while (bsR(1) == 1) {
                j++;
            }
            selectorMtf[i] = (char) j;
        }

        /* Undo the MTF values for the selectors. */
        {
            final var pos = new char[N_GROUPS];
            char tmp, v;
            for (v = 0; v < nGroups; v++) {
                pos[v] = v;
            }

            for (i = 0; i < nSelectors; i++) {
                v = selectorMtf[i];
                tmp = pos[v];
                while (v > 0) {
                    pos[v] = pos[v - 1];
                    v--;
                }
                pos[0] = tmp;
                selector[i] = tmp;
            }
        }

        /* Now the coding tables */
        for (t = 0; t < nGroups; t++) {
            var curr = bsR(5);
            for (i = 0; i < alphaSize; i++) {
                while (bsR(1) == 1) {
                    if (bsR(1) == 0) {
                        curr++;
                    } else {
                        curr--;
                    }
                }
                len[t][i] = (char) curr;
            }
        }

        /* Create the Huffman decoding tables */
        for (t = 0; t < nGroups; t++) {
            minLen = 32;
            maxLen = 0;
            for (i = 0; i < alphaSize; i++) {
                if (len[t][i] > maxLen) {
                    maxLen = len[t][i];
                }
                if (len[t][i] < minLen) {
                    minLen = len[t][i];
                }
            }
            hbCreateDecodeTables(limit[t], base[t], perm[t], len[t], minLen, maxLen, alphaSize);
            minLens[t] = minLen;
        }
    }

    /**
     * Gets the and move to front decode.
     *
     * @return the and move to front decode
     */
    private void getAndMoveToFrontDecode() {
        final var yy = new char[256];
        int i, j, nextSym, limitLast;
        int EOB, groupNo, groupPos;

        limitLast = baseBlockSize * blockSize100k;
        origPtr = bsGetIntVS(24);

        recvDecodingTables();
        EOB = nInUse + 1;
        groupNo = -1;
        groupPos = 0;

        /*
         * Setting up the unzftab entries here is not strictly necessary, but it does save having to do it later in a
         * separate pass, and so saves a block's worth of cache misses.
         */
        for (i = 0; i <= 255; i++) {
            unzftab[i] = 0;
        }

        for (i = 0; i <= 255; i++) {
            yy[i] = (char) i;
        }

        last = -1;

        {
            int zt, zn, zvec, zj;
            if (groupPos == 0) {
                groupNo++;
                groupPos = G_SIZE;
            }
            groupPos--;
            zt = selector[groupNo];
            zn = minLens[zt];
            zvec = bsR(zn);
            while (zvec > limit[zt][zn]) {
                zn++;
                {
                    {
                        while (bsLive < 1) {
                            final int zzi;
                            var res = 0;
                            char thech = 0;
                            try {
                                res = bsStream.read();
                            } catch (final IOException e) {
                                compressedStreamEOF();
                            }
                            if (res == -1) {
                                compressedStreamEOF();
                            }
                            thech = (char) res;
                            zzi = thech;
                            bsBuff = bsBuff << 8 | zzi & 0xff;
                            bsLive += 8;
                        }
                    }
                    zj = bsBuff >> bsLive - 1 & 1;
                    bsLive--;
                }
                zvec = zvec << 1 | zj;
            }
            nextSym = perm[zt][zvec - base[zt][zn]];
        }

        while (true) {

            if (nextSym == EOB) {
                break;
            }

            if ((nextSym != RUNA) && (nextSym != RUNB)) {
                char tmp;
                last++;
                if (last >= limitLast) {
                    blockOverrun();
                }

                tmp = yy[nextSym - 1];
                unzftab[seqToUnseq[tmp]]++;
                ll8[last] = seqToUnseq[tmp];

                /*
                 * This loop is hammered during decompression, hence the unrolling.
                 *
                 * for (j = nextSym-1; j > 0; j--) yy[j] = yy[j-1];
                 */

                j = nextSym - 1;
                for (; j > 3; j -= 4) {
                    yy[j] = yy[j - 1];
                    yy[j - 1] = yy[j - 2];
                    yy[j - 2] = yy[j - 3];
                    yy[j - 3] = yy[j - 4];
                }
                for (; j > 0; j--) {
                    yy[j] = yy[j - 1];
                }

                yy[0] = tmp;
                {
                    int zt, zn, zvec, zj;
                    if (groupPos == 0) {
                        groupNo++;
                        groupPos = G_SIZE;
                    }
                    groupPos--;
                    zt = selector[groupNo];
                    zn = minLens[zt];
                    zvec = bsR(zn);
                    while (zvec > limit[zt][zn]) {
                        zn++;
                        {
                            {
                                while (bsLive < 1) {
                                    final int zzi;
                                    char thech = 0;
                                    try {
                                        thech = (char) bsStream.read();
                                    } catch (final IOException e) {
                                        compressedStreamEOF();
                                    }
                                    zzi = thech;
                                    bsBuff = bsBuff << 8 | zzi & 0xff;
                                    bsLive += 8;
                                }
                            }
                            zj = bsBuff >> bsLive - 1 & 1;
                            bsLive--;
                        }
                        zvec = zvec << 1 | zj;
                    }
                    nextSym = perm[zt][zvec - base[zt][zn]];
                }
                continue;
            }
            final char ch;
            var s = -1;
            var N = 1;
            do {
                if (nextSym == RUNA) {
                    s = s + (0 + 1) * N;
                } else if (nextSym == RUNB) {
                    s = s + (1 + 1) * N;
                }
                N = N * 2;
                {
                    int zt, zn, zvec, zj;
                    if (groupPos == 0) {
                        groupNo++;
                        groupPos = G_SIZE;
                    }
                    groupPos--;
                    zt = selector[groupNo];
                    zn = minLens[zt];
                    zvec = bsR(zn);
                    while (zvec > limit[zt][zn]) {
                        zn++;
                        {
                            {
                                while (bsLive < 1) {
                                    final int zzi;
                                    var res = 0;
                                    char thech = 0;
                                    try {
                                        res = bsStream.read();
                                    } catch (final IOException e) {
                                        compressedStreamEOF();
                                    }
                                    if (res == -1) {
                                        compressedStreamEOF();
                                    }
                                    thech = (char) res;
                                    zzi = thech;
                                    bsBuff = bsBuff << 8 | zzi & 0xff;
                                    bsLive += 8;
                                }
                            }
                            zj = bsBuff >> bsLive - 1 & 1;
                            bsLive--;
                        }
                        zvec = zvec << 1 | zj;
                    }
                    nextSym = perm[zt][zvec - base[zt][zn]];
                }
            } while (nextSym == RUNA || nextSym == RUNB);

            s++;
            ch = seqToUnseq[yy[0]];
            unzftab[ch] += s;

            while (s > 0) {
                last++;
                ll8[last] = ch;
                s--;
            }

            if (last >= limitLast) {
                blockOverrun();
            }
        }
    }

    /**
     * Setup block.
     */
    private void setupBlock() {
        var cftab = new int[257];
        char ch;

        cftab[0] = 0;
        for (i = 1; i <= 256; i++) {
            cftab[i] = unzftab[i - 1];
        }
        for (i = 1; i <= 256; i++) {
            cftab[i] += cftab[i - 1];
        }

        for (i = 0; i <= last; i++) {
            ch = ll8[i];
            tt[cftab[ch]] = i;
            cftab[ch]++;
        }
        cftab = null;

        tPos = tt[origPtr];

        count = 0;
        i2 = 0;
        ch2 = 256; /* not a char and not EOF */

        if (blockRandomised) {
            rNToGo = 0;
            rTPos = 0;
            setupRandPartA();
        } else {
            setupNoRandPartA();
        }
    }

    /**
     * Setup rand part A.
     */
    private void setupRandPartA() {
        if (i2 <= last) {
            chPrev = ch2;
            ch2 = ll8[tPos];
            tPos = tt[tPos];
            if (rNToGo == 0) {
                rNToGo = rNums[rTPos];
                rTPos++;
                if (rTPos == 512) {
                    rTPos = 0;
                }
            }
            rNToGo--;
            ch2 ^= rNToGo == 1 ? 1 : 0;
            i2++;

            currentChar = ch2;
            currentState = RAND_PART_B_STATE;
            mCrc.updateCRC(ch2);
        } else {
            endBlock();
            initBlock();
            setupBlock();
        }
    }

    /**
     * Setup no rand part A.
     */
    private void setupNoRandPartA() {
        if (i2 <= last) {
            chPrev = ch2;
            ch2 = ll8[tPos];
            tPos = tt[tPos];
            i2++;

            currentChar = ch2;
            currentState = NO_RAND_PART_B_STATE;
            mCrc.updateCRC(ch2);
        } else {
            endBlock();
            initBlock();
            setupBlock();
        }
    }

    /**
     * Setup rand part B.
     */
    private void setupRandPartB() {
        if (ch2 != chPrev) {
            currentState = RAND_PART_A_STATE;
            count = 1;
            setupRandPartA();
        } else {
            count++;
            if (count >= 4) {
                z = ll8[tPos];
                tPos = tt[tPos];
                if (rNToGo == 0) {
                    rNToGo = rNums[rTPos];
                    rTPos++;
                    if (rTPos == 512) {
                        rTPos = 0;
                    }
                }
                rNToGo--;
                z ^= rNToGo == 1 ? 1 : 0;
                j2 = 0;
                currentState = RAND_PART_C_STATE;
                setupRandPartC();
            } else {
                currentState = RAND_PART_A_STATE;
                setupRandPartA();
            }
        }
    }

    /**
     * Setup rand part C.
     */
    private void setupRandPartC() {
        if (j2 < z) {
            currentChar = ch2;
            mCrc.updateCRC(ch2);
            j2++;
        } else {
            currentState = RAND_PART_A_STATE;
            i2++;
            count = 0;
            setupRandPartA();
        }
    }

    /**
     * Setup no rand part B.
     */
    private void setupNoRandPartB() {
        if (ch2 != chPrev) {
            currentState = NO_RAND_PART_A_STATE;
            count = 1;
            setupNoRandPartA();
        } else {
            count++;
            if (count >= 4) {
                z = ll8[tPos];
                tPos = tt[tPos];
                currentState = NO_RAND_PART_C_STATE;
                j2 = 0;
                setupNoRandPartC();
            } else {
                currentState = NO_RAND_PART_A_STATE;
                setupNoRandPartA();
            }
        }
    }

    /**
     * Setup no rand part C.
     */
    private void setupNoRandPartC() {
        if (j2 < z) {
            currentChar = ch2;
            mCrc.updateCRC(ch2);
            j2++;
        } else {
            currentState = NO_RAND_PART_A_STATE;
            i2++;
            count = 0;
            setupNoRandPartA();
        }
    }

    /**
     * Sets the decompress structure sizes.
     *
     * @param newSize100k
     *            the new decompress structure sizes
     */
    private void setDecompressStructureSizes(final int newSize100k) {
        if (!(0 <= newSize100k && newSize100k <= 9 && 0 <= blockSize100k && blockSize100k <= 9)) {
            // throw new IOException("Invalid block size");
        }

        blockSize100k = newSize100k;

        if (newSize100k == 0) {
            return;
        }

        final var n = baseBlockSize * newSize100k;
        ll8 = new char[n];
        tt = new int[n];
    }
}
