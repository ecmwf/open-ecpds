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

package ecmwf.ecpds.master;

/**
 * ECMWF Product Data Store (OpenPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

public final class DestinationStep {
    /** The Constant DESTINATION_STEP_INIT. */
    public static final int DESTINATION_STEP_INIT = 0;

    /** The Constant DESTINATION_STEP_NO_PROVIDER. */
    public static final int DESTINATION_STEP_NO_PROVIDER = 1;

    /** The Constant DESTINATION_STEP_NO_TRANSFER. */
    public static final int DESTINATION_STEP_NO_TRANSFER = 2;

    /** The Constant DESTINATION_STEP_PROCESS_DELAY. */
    public static final int DESTINATION_STEP_PROCESS_DELAY = 3;

    /** The Constant DESTINATION_STEP_PROCESS_RETR. */
    public static final int DESTINATION_STEP_PROCESS_RETR = 4;

    /** The Constant DESTINATION_STEP_PROCESS_INTR. */
    public static final int DESTINATION_STEP_PROCESS_INTR = 5;

    /** The Constant DESTINATION_STEP_PROCESS_WAIT. */
    public static final int DESTINATION_STEP_PROCESS_WAIT = 6;

    /** The Constant DESTINATION_STEP_PROCESS_RUN. */
    public static final int DESTINATION_STEP_PROCESS_RUN = 7;

    /** The Constant _steps. */
    private static final String[] _steps = { "DESTINATION_STEP_INIT", "DESTINATION_STEP_NO_PROVIDER",
            "DESTINATION_STEP_NO_TRANSFER", "DESTINATION_STEP_PROCESS_DELAY", "DESTINATION_STEP_PROCESS_RETR",
            "DESTINATION_STEP_PROCESS_INTR", "DESTINATION_STEP_PROCESS_WAIT", "DESTINATION_STEP_PROCESS_RUN" };

    /**
     * Gets the step string.
     *
     * @param id
     *            the id
     *
     * @return the step string
     */
    public static String getStepString(final int id) {
        return id >= 0 && id <= 7 ? _steps[id] : "DESTINATION_STEP_UNDEFINED";
    }
}
