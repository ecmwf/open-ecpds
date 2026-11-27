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

import java.util.Iterator;
import java.util.Map;

/**
 * AttributedBean.java A model bean that has a list of variable atributes.
 *
 * Created: Thu Apr 25 17:23:18 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 0.1.0
 */

public interface AttributedBean {

    public String getAttribute(String name) throws ModelException;

    public String getAttribute(String name, String defaultValue);

    public String getSafeAttribute(String name);

    public String getAttribute(String name, String defaultValue, String prefix, String suffix);

    public void setAttribute(String name, String value);

    public Iterator getAttributeNames();

    public String dump(int mode);

    public Map getAttrs();

    public boolean hasAttribute(String name);

}// AttributedBean
