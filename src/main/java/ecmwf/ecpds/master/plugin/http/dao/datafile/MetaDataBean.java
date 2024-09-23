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

package ecmwf.ecpds.master.plugin.http.dao.datafile;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.common.database.DataBaseObject;
import ecmwf.common.database.MetadataAttribute;
import ecmwf.common.database.MetadataValue;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.home.datafile.DataFileHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.MetaData;
import ecmwf.web.dao.ModelBeanBase;

/**
 * The Class MetaDataBean.
 */
public class MetaDataBean extends ModelBeanBase implements MetaData, OjbImplementedBean {

    /** The attribute. */
    private final MetadataAttribute attribute;

    /** The value. */
    private final MetadataValue value;

    /**
     * Instantiates a new meta data bean.
     *
     * @param attribute
     *            the attribute
     */
    protected MetaDataBean(final MetadataAttribute attribute) {
        this(attribute, null);
    }

    /**
     * Instantiates a new meta data bean.
     *
     * @param value
     *            the value
     */
    protected MetaDataBean(final MetadataValue value) {
        this(null, value);
    }

    /**
     * Instantiates a new meta data bean.
     *
     * @param attribute
     *            the attribute
     * @param value
     *            the value
     */
    protected MetaDataBean(final MetadataAttribute attribute, final MetadataValue value) {
        this.attribute = attribute;
        this.value = value;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return MetaData.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ojb implementation.
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return attribute;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    public String getName() {
        if (attribute != null) {
            return attribute.getName();
        }
        if (value != null) {
            return value.getMetadataAttributeName();
        } else {
            return "N/A";
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the value.
     */
    @Override
    public String getValue() {
        if (value != null) {
            return value.getValue();
        }
        if (attribute != null) {
            return "";
        } else {
            return "N/A";
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data file.
     */
    @Override
    public DataFile getDataFile() throws DataFileException {
        try {
            return DataFileHome.findByPrimaryKey(Long.toString(value.getDataFileId()));
        } catch (final Exception e) {
            throw new DataFileException("Problem getting DataFile for MetaData ", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final MetaDataBean metadataBean && equals(metadataBean);
    }

    /**
     * Equals.
     *
     * @param u
     *            the u
     *
     * @return true, if successful
     */
    public boolean equals(final MetaDataBean u) {
        return getName().equals(u.getName());
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + attribute + "," + value + " }";
    }
}
