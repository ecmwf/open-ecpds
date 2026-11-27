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
 * A person located in the organization tree and related to all other persons
 * in that tree.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 0.1.0
 *
 */

import java.util.List;

import ecmwf.ecpds.master.plugin.http.model.AttributedBean;
import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;

public interface OrgPerson extends ModelBean, AttributedBean {

    public static final String NAME = "cn";
    public static final String SURNAME = "sn";
    public static final String TITLE = "title";
    public static final String FUNCTION = "function";
    public static final String PHOTO = "photo";
    public static final String EMAIL = "mail";

    // TODO: Very bad thing, to have this hardcoded!!
    public static final int DIRECTOR = 6;
    public static final int HEAD_DEPARTMENT = 5;
    public static final int HEAD_DIVISION = 4;
    public static final int HEAD_SECTION = 3;
    public static final int HEAD_SUBSECTION = 2;
    public static final int I_WANT_MY_OWN_BOX = 1;

    public static final int PRIVATE = 0;

    public String getName() throws ModelException;

    public String getSurname() throws ModelException;

    public String getTitle() throws ModelException;

    public String getFunction() throws ModelException;

    public String getPhoto() throws ModelException;

    public String getEmail() throws ModelException;

    public String getParentId();

    public void setParentId(String v);

    public int getLevel();

    public void setLevel(int v);

    public String getOfficeMapUrl();

    public OrgUnit getUnit() throws OrgPersonException;

    public List getSubordinates();

    // Sibling operations, between the same branch of the tree.
    public List getSiblings() throws ModelException;

    public OrgPerson getNextSibling() throws ModelException;

    public OrgPerson getPriorSibling() throws ModelException;

    public OrgPerson getManager() throws ModelException;

    // The same operations, but now without limiting to the same branch.
    public List getTreeWideSiblings() throws OrgPersonException;

    public OrgPerson getNextTreeWideSibling() throws OrgPersonException;

    public OrgPerson getPriorTreeWideSibling() throws OrgPersonException;

} // OrgPerson
