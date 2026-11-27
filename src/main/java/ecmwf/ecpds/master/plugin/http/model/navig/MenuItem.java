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
 * Created on Nov 13, 2003
 *
 * TODO: Documentation lacking.
 */
package ecmwf.ecpds.master.plugin.http.model.navig;

import java.util.Collection;

import ecmwf.ecpds.master.plugin.http.model.HierarchicalBean;
import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.OrderedBean;
import ecmwf.ecpds.master.plugin.http.model.users.Resource;

/**
 * @author sy8
 *
 *         TODO: Documentation lacking.
 */
public interface MenuItem extends ModelBean, OrderedBean, HierarchicalBean, Resource {

    public String HERE_KEY = "navig_here";
    public String TEXT_HOST_SEPARATOR = "$";

    /**
     * @return The text to be shown in the link
     */
    public String getText();

    public void setText(String text);

    /**
     * @return Some description of the content to which we are linking.
     */
    public String getDescription();

    public void setDescription(String description);

    /**
     * My Parent menu
     *
     * @return My parent
     *
     * @throws ModelException
     *             I <strong>bomb</string> if I'm the top...
     */
    public void setParent(MenuItem parent);

    /**
     * @return Some value which represents a relative importance of one menu against another.
     */
    public long getRelevance();

    /**
     * @return If the menu should be shown always open (while in current level), no matter if selected or not.
     */
    public boolean getAlwaysExtend();

    public void setAlwaysExtend(boolean b);

    /**
     * @return If this menu is only structural (ie, it might be linked as related, hold ACLs, etc, but will not show in
     *         the main menu).
     */
    public boolean getHideInMainMenu();

    public void setHideInMainMenu(boolean b);

    /**
     * @return Where this menu options goes to.
     */
    public String getDestinationUrl();

    /**
     * @return Does the item point to some external item.
     */
    public boolean isExternal();

    /**
     * @return If the item is an external item, but only defined in a RelatedMenuItem, not anybody else.
     */
    public boolean isRelatedExternal();

    /**
     * @return The order of this menu when displaying it
     */
    public int getOrder();

    public void setOrder(int order);

    /**
     * @return the Related menu that applies to this option (it may be located in a parent menu)
     */
    public Collection getRelated() throws MenuException;

    /**
     * @return The Related menu that is located just below this menu.
     */
    public Collection getLocalRelated() throws MenuException;

    public RelatedMenuItem getLocalRelated(String id) throws MenuException;

    /**
     * @return The number of parents of this menu.
     */
    public int getDepth();

}
