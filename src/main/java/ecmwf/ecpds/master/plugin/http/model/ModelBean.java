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
 * ModelBean.java
 *
 *
 * Created: Wed Apr 24 09:56:11 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 1.0
 */

public interface ModelBean {

    public void setId(String id); // TODO: This MUST NOT be here.

    public String getId();

    public int getIntId(); // TODO: Should this be here ?????????? Probably not.

    public boolean isNew();

    /**
     * Get (through reflection) the value of a bean's attribute.
     *
     * @param name
     *            Name of the attribute
     *
     * @return Value of the attribute
     *
     * @throws ModelException
     *             If the attribute doesn't exist or is not readable.
     */
    public Object getAttributeValue(String name) throws ModelException;

    String getBeanInterfaceName();

    void save() throws ModelException;

    void insert() throws ModelException;

    void delete() throws ModelException;

    void save(Object context) throws ModelException;

    void insert(Object context) throws ModelException;

    void delete(Object context) throws ModelException;

} // ModelBean
