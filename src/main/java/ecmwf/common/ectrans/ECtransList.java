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

package ecmwf.common.ectrans;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.OutputStream;

import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;

/**
 * The Class ECtransList.
 */
public final class ECtransList extends ECtransAction {

    /** The types available for listing. */
    private enum TYPE {

        /** The string array. */
        STRING_ARRAY,
        /** The gzipped byte array. */
        GZIPPED_BYTE_ARRAY,
        /** The byte output. */
        BYTE_OUTPUT
    }

    /** The directory. */
    private final String directory;

    /** The pattern. */
    private final String pattern;

    /** The version. */
    private final TYPE type;

    /** The list as string array. */
    private String[] listAsStringArray = null;

    /** The list as byte array. */
    private byte[] listAsByteArray = null;

    /** The output stream. */
    private final OutputStream out;

    /**
     * Instantiates a new ectrans list.
     *
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     * @param out
     *            the out
     * @param type
     *            the type
     */
    private ECtransList(final String directory, final String pattern, final OutputStream out, final TYPE type) {
        this.directory = directory;
        this.pattern = pattern;
        this.out = out;
        this.type = type;
    }

    /**
     * Instantiates a new ectrans list.
     *
     * @param directory
     *            the directory
     */
    public ECtransList(final String directory) {
        this(directory, null, null, TYPE.STRING_ARRAY);
    }

    /**
     * Instantiates a new ectrans list.
     *
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     * @param asStringArray
     *            the asStringArray
     */
    public ECtransList(final String directory, final String pattern, final boolean asStringArray) {
        this(directory, pattern, null, asStringArray ? TYPE.STRING_ARRAY : TYPE.GZIPPED_BYTE_ARRAY);
    }

    /**
     * Instantiates a new ectrans list.
     *
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     * @param out
     *            the out
     */
    public ECtransList(final String directory, final String pattern, final OutputStream out) {
        this(directory, pattern, out, TYPE.BYTE_OUTPUT);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    protected String getName() {
        return "list";
    }

    /**
     * {@inheritDoc}
     *
     * Exec.
     */
    @Override
    protected void exec(final TransferModule module, final boolean interruptible) throws Exception {
        switch (type) {
        case STRING_ARRAY:
            listAsStringArray = module.listAsStringArray(directory, pattern);
            break;
        case GZIPPED_BYTE_ARRAY:
            listAsByteArray = module.listAsByteArray(directory, pattern);
            break;
        case BYTE_OUTPUT:
            // This is asynchronous so in case of error we need to close the output and
            // report about the error!
            try {
                module.list(directory, pattern, out);
            } catch (final Exception e) {
                out.write(("err:" + Format.getMessage(e)).getBytes());
                out.flush();
                throw e;
            } finally {
                StreamPlugThread.closeQuietly(out);
            }
            break;
        }
    }

    /**
     * Gets the list as a string array.
     *
     * @return the list
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     */
    public String[] getListAsStringArray() throws ECtransException {
        if (type != TYPE.STRING_ARRAY || listAsStringArray == null) {
            // The list was not set so either the exec failed or was not called!
            throw new ECtransException("List failed");
        }
        return listAsStringArray;
    }

    /**
     * Gets the list as a GZIPed byte array.
     *
     * @return the list
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     */
    public byte[] getListAsByteArray() throws ECtransException {
        if (type != TYPE.GZIPPED_BYTE_ARRAY || listAsByteArray == null) {
            // The list was not set so either the exec failed or was not called!
            throw new ECtransException("List failed");
        }
        return listAsByteArray;
    }
}
