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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ecmwf.ecpds.master.plugin.http.model.AttributedBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;

/**
 * AttributedModelBean.java A model bean that has a list of variable atributes.
 *
 * Created: Thu Apr 25 17:23:18 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 0.1.0
 */

public class AttributedModelBean extends ModelBeanBase implements AttributedBean {

    public static final int HTML_DUMP = 0;
    public static final int TXT_DUMP = 1;

    private HashMap attributes;

    public AttributedModelBean() {
        attributes = new HashMap();
    }

    public String getBeanInterfaceName() {
        return AttributedModelBean.class.getName();
    }

    /**
     * Get the value of attribute.
     *
     * @return value of attribute.
     *
     * @throws ModelBeanException
     *             If an attribute with this name doesn't exist.
     */

    public String getAttribute(String name) throws ModelException {
        String value = (String) attributes.get(name);
        if (value == null)
            throw new ModelException("This AttributedModelBean doesn't have an attribute called: " + name);
        else
            return value;
    }

    /**
     * Get the value of attribute but instead fo dumping an exception, show a default value if it doesn't exist.
     *
     * @param name
     *            The name of the attribute
     * @param defaultValue
     *            The default value to show
     *
     * @return value of attribute.
     */

    public String getAttribute(String name, String defaultValue) {
        String value = (String) attributes.get(name);
        if (value == null)
            return defaultValue;
        else
            return value;
    }

    /**
     * The same as <code>getAttribute(name,defaultValue)</code> but easier to use as a bean attribute.
     *
     * @param name
     *            The name of the attribute
     *
     * @return Name of attribute
     */

    public String getSafeAttribute(String name) {
        return this.getAttribute(name, "");
    }

    /**
     * Get the value of attribute but instead fo dumping an exception, show a default value if it doesn't exist. Only if
     * it exists, show it with a prefix and a suffix. This could be useful for showing comma separated lists, etc, where
     * you only show the comma if the element exists.
     *
     * @param name
     *            The name of the attribute
     * @param defaultValue
     *            The default value to show
     * @param prefix
     *            Prefix to show before the field value.
     * @param suffix
     *            Suffix to show after the field value.
     *
     * @return value of attribute, with prefix and suffix appended if the value is not the default.
     */

    public String getAttribute(String name, String defaultValue, String prefix, String suffix) {
        String value = (String) attributes.get(name);
        if (value == null)
            return defaultValue;
        else
            return prefix + value + suffix;
    }

    /**
     * Set the value of attribute.
     *
     * @param name
     *            Name of the attribute.
     * @param value
     *            Value to assign to attribute.
     */

    public void setAttribute(String name, String value) {
        this.attributes.put(name, value);
    }

    /**
     * Get the list of the attribute names
     *
     * @return The Iterator of the attribute names.
     */

    public Iterator getAttributeNames() {
        return this.attributes.keySet().iterator();
    }

    /**
     * Gets a representation of the object, HTML or plaintext formatted.
     *
     * @param mode
     *            The type of the dump.
     *
     * @return The String representation.
     */

    public String dump(int mode) {

        if (mode == HTML_DUMP)
            return this.dumpHTML();
        else
            return this.dumpTXT();
    }

    public Map getAttrs() {
        return this.attributes;
    }

    public boolean hasAttribute(String name) {
        return this.attributes.containsKey(name);
    }

    private String dumpHTML() {

        String out = "<table>";
        Iterator i = this.getAttributeNames();
        while (i.hasNext()) {
            String name = (String) i.next();
            out += "<tr><th>" + name + "</th><td>" + this.getAttribute(name, "--") + "</td></tr>\n";
        }
        out += "</table>";
        return out;

    }

    private String dumpTXT() {

        String out = "";
        Iterator i = this.getAttributeNames();
        while (i.hasNext()) {
            String name = (String) i.next();
            out += "* " + name + " = " + this.getAttribute(name, "--") + "\n";
        }
        return out;

    }

} // AttributedBean
