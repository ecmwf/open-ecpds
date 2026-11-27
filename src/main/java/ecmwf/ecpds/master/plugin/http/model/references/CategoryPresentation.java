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

/**
 * @author sy8
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

import java.util.Collection;
import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
//import ecmwf.ecpds.master.plugin.http.model.ModelBeanSortItem;

public interface CategoryPresentation extends ModelBean {

    public ModelSearch getDefaultSearch();

    public ModelSearch getDefaultSearch(String query);

    public String getName();

    public void setName(String v);

    public String getView();

    public void setView(String v);

    public String getAdditionalQuery();

    public void setAdditionalQuery(String v);

    public void setSortItems(Collection c);

    public void setDetailName(String v);

    public String getDetailName();

    public String getSubmenu();

    public void setSubmenu(String v);

    public Collection getFriendCategories();

    public void setFriendCategories(Collection c);

    public Collection getExpandedTreeOfFriendCategoriesByDate();

}
