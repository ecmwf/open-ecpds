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

package ecmwf.ecpds.master.plugin.http.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.ModelSortItem;

/**
 * Base for all ModelBeanSearch implementations.
 *
 * Created: Mon Jul 8 17:25:44 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 0.1.0
 */

public class ModelSearchBase implements ModelSearch {

    private static Log log = LogFactory.getLog(ModelSearchBase.class);
    private String query = "";
    private String base = "";
    private int scope = ModelSearch.ONELEVEL_SCOPE;
    private int sizeLimit = Integer.MAX_VALUE;
    private int offset = 0;
    private ArrayList order;
    private boolean cacheable = true;
    private boolean storeSearchResults = false;
    private boolean partialMatches = false;

    public ModelSearchBase() {
        this.order = new ArrayList(1);
    }

    public ModelSearchBase(String query) {
        this();
        this.setQuery(query);
    }

    public ModelSearchBase(String query, int sizeLimit) {
        this(query);
        this.setSizeLimit(sizeLimit);
    }

    public ModelSearchBase(String query, int sizeLimit, int offset) {
        this(query, sizeLimit);
        this.setOffset(offset);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String v) {
        this.query = v;
    }

    public void addAndQuery(String v) {
        this.addQuery(v, "and");
    }

    public void addOrQuery(String v) {
        this.addQuery(v, "or");
    }

    private void addQuery(String v, String operator) {

        if (!"".equals(this.query))
            this.query = "(" + this.query + ") " + operator + " " + v;
        else
            this.query = v;
    }

    public String getBase() {
        return this.base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public int getScope() {
        return this.scope;
    }

    public int getSizeLimit() {
        return sizeLimit;
    }

    public void setSizeLimit(int v) {
        this.sizeLimit = v;
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int v) {
        this.offset = v;
    }

    public void addSortItem(ModelSortItem item) {
        this.order.add(item);
    }

    public void addSortItems(Collection c) {
        this.order.addAll(c);
    }

    public Collection getSortItems() {
        return this.order;
    }

    public void resetSortItems() {
        this.order = new ArrayList(1);
    }

    public boolean cacheable() {
        return this.cacheable;
    }

    public void setCacheable(boolean v) {
        this.cacheable = v;
    }

    public boolean storeSearchResults() {
        return this.storeSearchResults;
    }

    public void setStoreSearchResults(boolean v) {
        this.storeSearchResults = v;
    }

    public void setSortItemsFromStrings(Collection c) {
        if (c != null) {
            this.resetSortItems();
            Iterator i = c.iterator();
            while (i.hasNext())
                this.addSortItem(new ModelSortItem((String) i.next()));
        }
    }

    public boolean partialMatches() {
        return this.partialMatches;
    }

    public void setPartialMatches(boolean v) {
        this.partialMatches = v;
    }

    public ModelSearch getCopy() {
        try {
            return (ModelSearch) this.clone();
        } catch (CloneNotSupportedException e) {
            log.error("Could not clone object. Returned itself", e);
            return this;
        }

    }

    /**
     * The key, a value that should identify this ModelBeanSearch univocally, is calculated from the query, the
     * ordering, the limit, and the offset. This value will be used as key for the Cache system.
     */

    public String getKey() {
        return "C:(" + this.getClass().getName() + "), B:(" + this.getBase() + "), Q:(" + this.getQuery() + "), O:("
                + this.getSortItemsAsString() + "), S:(" + this.getScope() + "), N:(" + this.getSizeLimit() + "), F:("
                + this.getOffset() + ")";
    }

    // Private *********************************************************

    private String getSortItemsAsString() {
        StringBuffer tmp = new StringBuffer("");
        Iterator it = this.getSortItems().iterator();
        while (it.hasNext()) {
            ModelSortItem item = (ModelSortItem) it.next();
            tmp.append(item.getName());
            if (item.ascending())
                tmp.append("_A");
            else
                tmp.append("_D");
            tmp.append(";");
        }
        return tmp.toString();
    }

} // ModelBeanSearch
