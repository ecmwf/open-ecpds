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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Objects;

/**
 * The Class MetadataValue.
 */
public class MetadataValue extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8218425007673012498L;

    /** The daf id. */
    protected long DAF_ID;

    /** The mea name. */
    protected String MEA_NAME;

    /** The mev id. */
    protected long MEV_ID;

    /** The mev value. */
    protected String MEV_VALUE;

    /** The data file. */
    protected DataFile dataFile;

    /** The metadata attribute. */
    protected MetadataAttribute metadataAttribute;

    /**
     * Instantiates a new metadata value.
     */
    public MetadataValue() {
    }

    /**
     * Gets the data file id.
     *
     * @return the data file id
     */
    public long getDataFileId() {
        return DAF_ID;
    }

    /**
     * Sets the data file id.
     *
     * @param param
     *            the new data file id
     */
    public void setDataFileId(final long param) {
        DAF_ID = param;
    }

    /**
     * Sets the data file id.
     *
     * @param param
     *            the new data file id
     */
    public void setDataFileId(final String param) {
        DAF_ID = Long.parseLong(param);
    }

    /**
     * Gets the metadata attribute name.
     *
     * @return the metadata attribute name
     */
    public String getMetadataAttributeName() {
        return MEA_NAME;
    }

    /**
     * Sets the metadata attribute name.
     *
     * @param param
     *            the new metadata attribute name
     */
    public void setMetadataAttributeName(final String param) {
        MEA_NAME = param;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return MEV_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final long param) {
        MEV_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        MEV_ID = Long.parseLong(param);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return MEV_VALUE;
    }

    /**
     * Sets the value.
     *
     * @param param
     *            the new value
     */
    public void setValue(final String param) {
        MEV_VALUE = param;
    }

    /**
     * Gets the data file.
     *
     * @return the data file
     */
    public DataFile getDataFile() {
        return dataFile;
    }

    /**
     * Sets the data file.
     *
     * @param param
     *            the new data file
     */
    public void setDataFile(final DataFile param) {
        dataFile = param;
    }

    /**
     * Gets the metadata attribute.
     *
     * @return the metadata attribute
     */
    public MetadataAttribute getMetadataAttribute() {
        return metadataAttribute;
    }

    /**
     * Sets the metadata attribute.
     *
     * @param param
     *            the new metadata attribute
     */
    public void setMetadataAttribute(final MetadataAttribute param) {
        metadataAttribute = param;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(MEV_ID);
    }

    /**
     * Equals.
     *
     * @param obj
     *            the obj
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (MetadataValue) obj;
        return MEV_ID == other.MEV_ID;
    }
}
