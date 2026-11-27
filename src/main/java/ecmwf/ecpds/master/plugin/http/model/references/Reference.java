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

package ecmwf.ecpds.master.plugin.http.model.references;

import java.util.Collection;
import java.util.Date;

import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.FileModel;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.AttributedBean;
import ecmwf.ecpds.master.plugin.http.services.content.Content;

/**
 * Representation of a Library entry, whatever its type is.
 *
 * Created: Thu Apr 25 17:23:18 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 1.0
 */

public interface Reference extends ModelBean, AttributedBean, Content {

    public static final int SHORT_TITLE_MAX_LENGTH = 25;

    public EntryType getEntryType() throws ModelException;

    public void setEntryType(EntryType v);

    public Category getCategory() throws ModelException;

    public void setCategory(Category v);

    public boolean getBibliographyItem();

    public void setBibliographyItem(boolean value);

    public boolean getAnnounceable();

    public void setAnnounceable(boolean value);

    public FileModel getFile();

    public String getShortTitle();

    public void setFile(FileModel v);

    public Collection getFieldValues();

    public Collection getRequiredFieldValues() throws ModelException;

    public Collection getOptionalFieldValues() throws ModelException;

    public void setFieldValues(Collection v);

    public void addFieldValue(FieldValue v);

    public Date getCreationDate();

    public void setCreationDate(Date v);

    public Date getModificationDate();

    public void setModificationDate(Date v);

    public String getEditor();

    public void setEditor(String v);

    public String getUrl();

    public String getUrlFileName();

    public void setUrl(String v);

    public String getTypeId();

    public void setTypeId(String v);

    public String getCategoryId();

    public void setCategoryId(String v);

    public String getTypeName();

    // public void setTypeName(String v);
    public String getTypeIcon();

    public void setTypeIcon(String icon);

    public String getBibtexId();

    public void setBibtexId(String v);

    public void resetFieldValues();

    public FieldValue getFieldValueByName(String name) throws ReferenceException;

    public boolean hasFieldValue(String name);

    public Reference getPrev() throws ReferenceException;

    public Reference getNext() throws ReferenceException;

    public Reference getPrevOrSelf();

    public Reference getNextOrSelf();

    public boolean isBorrowed();

    public String getBorrowedByUid();

    public Date getBorrowedDate();

    public void setBorrowedByUid(String value);

    public void setBorrowedDate(Date d);

} // Reference
