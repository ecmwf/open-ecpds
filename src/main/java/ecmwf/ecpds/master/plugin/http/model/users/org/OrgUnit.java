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

package ecmwf.ecpds.master.plugin.http.model.users.org;

/**
 * @author sy8
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

import java.util.Collection;

import ecmwf.ecpds.master.plugin.http.model.AttributedBean;
import ecmwf.ecpds.master.plugin.http.model.HierarchicalBean;
import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.OrderedBean;

public interface OrgUnit extends ModelBean, AttributedBean, HierarchicalBean, OrderedBean {

    public static final int DIRECTOR = 10;
    public static final int DEPARTMENT = 9;
    public static final int DIVISION = 8;
    public static final int SECTION = 7;
    public static final int GROUP = 6;

    public static final int SPECIAL = 5;

    public static final int PRIVATE = 0;

    public static final int CONTRACTOR = -5;
    public static final int CONTRACTOR_TOP = -10;

    public int getLevel();

    public boolean getIsRoot(String config);

    public OrgPerson getManager() throws OrgPersonException;

    public Collection getMembers() throws OrgPersonException;

    public Collection getAssociatedMembers() throws OrgPersonException;

    public OrgUnit getNextTreeWideSibling() throws ModelException;

    public OrgUnit getPriorTreeWideSibling() throws ModelException;

}
