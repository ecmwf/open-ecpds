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
 * HierarchicalBean.java
 */
package ecmwf.ecpds.master.plugin.http.model;

import java.util.Collection;
import java.util.List;

public interface HierarchicalBean {

    /**
     * This is also in ModelBean, but it will save some casts...
     */
    public String getId();

    public String getParentId();

    /**
     * Get the Bean's Old Man
     *
     * @throws ModelException
     *             If this is the top of the hierarchy.
     */
    public HierarchicalBean getParent() throws ModelException;

    /**
     * @return If the bean is the top one. A call to getParent here would throw an exception
     */
    public boolean getIsRoot();

    /**
     * More than 3 and it can claim benefits..
     *
     * @return A Collection of all the Beans to which this is parent
     */
    public Collection getChildren() throws ModelException;

    /**
     * Get all the (p/m)aternal line till Adan or Eve
     *
     * @return A Collection created by a recursive getParent, NOT including this
     */
    public Collection getAncestors() throws ModelException;

    /**
     * Get all nodes of this branch of the tree, without any specific order, INCLUDING this. We could say, the guy's
     * complete family. The CLAN.
     *
     * @return The node and all children, grandchildren, grandgrandchildren, grandgrand..... well, you get the idea.
     */
    public List getSubtree() throws ModelException;

    /**
     * Get all the siblings in the same branch or the tree. Many times it will be something like
     * getParent().getChildren(), but NOT including this.
     *
     * @return A Collection of siblings
     */
    public Collection getSiblings() throws ModelException;
}
