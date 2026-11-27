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

package ecmwf.ecpds.master.plugin.http.model;

/**
 * ModelBeanSearchBase.java
 *
 *
 * Created: Mon Jul  8 17:25:44 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 0.1.0
 */

import java.util.Collection;

public interface ModelSearch extends Cloneable {

    public static final int SUBTREE_SCOPE = 1;
    public static final int ONELEVEL_SCOPE = 2;
    public static final int OBJECT_SCOPE = 3;

    String getQuery();

    void setQuery(String query);

    void addAndQuery(String query);

    void addOrQuery(String query);

    String getBase();

    void setBase(String base);

    int getScope();

    void setScope(int scope);

    void setSizeLimit(int size);

    int getSizeLimit();

    void setOffset(int size);

    int getOffset();

    boolean partialMatches();

    void setPartialMatches(boolean v);

    void addSortItem(ModelSortItem item);

    void addSortItems(Collection c);

    void resetSortItems();

    Collection getSortItems();

    void setSortItemsFromStrings(Collection c);

    boolean cacheable();

    void setCacheable(boolean v);

    boolean storeSearchResults();

    void setStoreSearchResults(boolean v);

    String getKey();

    public ModelSearch getCopy();

} // ModelBeanSearch
