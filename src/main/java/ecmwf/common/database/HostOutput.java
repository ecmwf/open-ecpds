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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The Class HostOutput.
 */
public class HostOutput extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6269434296900284206L;

    /** The hou id. */
    protected Integer HOU_ID;

    /** The acquisition time. */
    protected BigDecimal HOU_ACQUISITION_TIME;

    /** The output. */
    protected String HOU_OUTPUT;

    /**
     * Instantiates a new host.
     */
    public HostOutput() {
    }

    /**
     * Instantiates a new host output.
     *
     * @param id
     *            the id
     */
    public HostOutput(final int id) {
        setId(id);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return HOU_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final int param) {
        HOU_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        HOU_ID = Integer.parseInt(param);
    }

    /**
     * Gets the acquisition time.
     *
     * @return the acquisition time
     */
    public java.sql.Timestamp getAcquisitionTime() {
        return bigDecimalToTimestamp(HOU_ACQUISITION_TIME);
    }

    /**
     * Sets the acquisition time.
     *
     * @param param
     *            the new acquisition time
     */
    public void setAcquisitionTime(final java.sql.Timestamp param) {
        HOU_ACQUISITION_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the output.
     *
     * @return the latitude
     */
    public String getOutput() {
        return HOU_OUTPUT;
    }

    /**
     * Sets the output.
     *
     * @param output
     *            the new output
     */
    public void setOutput(final String output) {
        HOU_OUTPUT = output;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(HOU_ID);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (HostOutput) obj;
        return HOU_ID == other.HOU_ID;
    }
}
