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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import ecmwf.common.technical.ByteSize;
import ecmwf.common.technical.TimeRange;

/**
 * The Class ECtransConstants.
 */
public class ECtransConstants {

    /** The Constant DEFAULT. */
    public static final String DEFAULT = "default";

    /** The Constant NONE. */
    public static final String NONE = "none";

    /** The Constant STRING_NONE. */
    protected static final List<String> STRING_NONE = new ArrayList<>();

    /** The Constant BOOLEAN_NONE. */
    protected static final List<Boolean> BOOLEAN_NONE = new ArrayList<>();

    /** The Constant BYTE_SIZE_NONE. */
    protected static final List<ByteSize> BYTE_SIZE_NONE = new ArrayList<>();

    /** The Constant DURATION_NONE. */
    protected static final List<Duration> DURATION_NONE = new ArrayList<>();

    /** The Constant TIME_RANGE_NONE. */
    protected static final List<TimeRange> TIME_RANGE_NONE = new ArrayList<>();

    /** The Constant INTEGER_NONE. */
    protected static final List<Integer> INTEGER_NONE = new ArrayList<>();

    /** The Constant LONG_NONE. */
    protected static final List<Long> LONG_NONE = new ArrayList<>();

    /** The Constant DOUBLE_NONE. */
    protected static final List<Double> DOUBLE_NONE = new ArrayList<>();

    /**
     * Instantiates a new ectrans constants.
     */
    private ECtransConstants() {
    }
}
