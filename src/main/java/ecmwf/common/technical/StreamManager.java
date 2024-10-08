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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public interface StreamManager {
    /** The Constant NONE. */
    String NONE = "none";

    /** The Constant LZMA. */
    String LZMA = "lzma";

    /** The Constant ZIP. */
    String ZIP = "zip";

    /** The Constant GZIP. */
    String GZIP = "gzip";

    /** The Constant BZIP2a. */
    String BZIP2a = "bzip2a";

    /** The Constant BZIP2. */
    String LBZIP2 = "lbzip2";

    /** The Constant LZ4. */
    String LZ4 = "lz4";

    /** The Constant SNAPPY. */
    String SNAPPY = "snappy";

    /** The Constant names. */
    String[] names = { "none", "lzma (.lzma)", "zip (.zip)", "gzip (.gz)", "bzip2 (.bz2)", "bzip2 (.bz2)", "lz4 (.lz4)",
            "snappy (.sz)" };

    /** The Constant modes. */
    String[] modes = { "none", "lzma", "zip", "gzip", "lbzip2", "bzip2a", "lz4", "snappy" };
}
