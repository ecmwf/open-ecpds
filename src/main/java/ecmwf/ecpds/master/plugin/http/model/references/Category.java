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

import ecmwf.ecpds.master.plugin.http.model.HierarchicalBean;
import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;

/**
 * This bean represents each of the different categories in which a ReferenceBean can fall. This is mainly useful for
 * doing limited data retrievals.
 *
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 0.1.0
 */

public interface Category extends ModelBean, HierarchicalBean {

    public static final int TOP_OF_HIERARCHY = Integer.MAX_VALUE;

    public String getName();

    public void setName(String v);

    public String getDescription();

    public void setDescription(String v);

    public String getItemName();

    public void setItemName(String v);

    public Date getEventDate();

    public void setEventDate(Date v);

    public String getFooter();

    public void setFooter(String v);

    public CategoryPresentation getDefaultPresentation() throws CategoryException;

    public CategoryPresentation getPresentation(String name) throws CategoryException;

    public Collection getPresentations();

    public Collection getPresentationIds();

    public void setPresentations(Collection v);

    public String getProfile();

    public void setProfile(String v);

    public String getParentId();

    public void setParentId(String v);

    public int getSortPriority();

    public void setSortPriority(int v);

    public Category getSafeParent(); // "Safe" version which returns null instead of exception... Easier to use from a
                                     // TAG in JSP....

    public Collection getBrowsableChildren() throws ModelException;

    public Collection getBrowsableSiblings() throws ModelException;

    public Collection getHierarchy();

    public boolean isBrowsable();

    public void setBrowsable(boolean v);

} // Category
