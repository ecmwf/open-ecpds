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

/*
 * Created on Mar 29, 2004
 *
 */
package ecmwf.ecpds.master.plugin.http.model.navig;

import java.util.Collection;

import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.OrderedBean;

/**
 * @author sy8
 *
 *         TODO: Comment ME !!!
 */

public interface RelatedMenuItem extends ModelBean, OrderedBean {

    public String RELATED_KEY = "navig_related";

    Collection getMenuItems() throws MenuException;

    void setMenuItems(Collection c);

    void addMenuItem(MenuItem item);

    void deleteMenuItem(MenuItem item) throws MenuException;

    // void setParentMenu(MenuItem item);
    // MenuItem getParentMenu();
    String getText();

    void setText(String text);

    String getDescription();

    public boolean getSticky();

    public void setSticky(boolean b);

    public void setParentId(String id);

    public String getParentId();

    public long getOrder();

    public void setOrder(long l);

    public int getDepth();
    // public void raise();
    // public void lower();

}
