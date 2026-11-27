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

package ecmwf.ecpds.master.plugin.http.util.search;

/**
 * AttributedBeanComparator.java
 *
 *
 * Created: Wed May 15 09:48:46 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version
 */

import java.util.Comparator;
import java.util.Vector;
import java.util.Enumeration;

import ecmwf.ecpds.master.plugin.http.dao.AttributedModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;

public class AttributedBeanComparator implements Comparator {

    private String attribute;
    private Vector attributeList = null;

    public AttributedBeanComparator(String attribute) {
        this.attribute = attribute;
    }

    public AttributedBeanComparator(Vector attributeList) {
        this.attributeList = attributeList;
    }

    /*
     * // Should implement this to directly get Sort options from ModelBeanSearch public AttributedBeanComparator
     * (Collection sortOptions) { this.attributeList = attributeList; }
     */

    public int compare(Object o1, Object o2) {

        // String val1 = "";
        // String val2 = "";

        if (this.attributeList == null) {
            return this.compareAttribute(o1, o2, this.attribute);
        } else {
            for (Enumeration e = this.attributeList.elements(); e.hasMoreElements();) {
                String attr = (String) e.nextElement();
                int result = this.compareAttribute(o1, o2, attr);
                if (result != 0)
                    return result;
            }
            return 0;
        }

    }

    /* * PRIVATE ************************************************************* */

    private int compareAttribute(Object o1, Object o2, String attribute) {

        AttributedModelBean b1 = (AttributedModelBean) o1;
        AttributedModelBean b2 = (AttributedModelBean) o2;

        String val1 = "";
        String val2 = "";

        try {
            val1 = (String) b1.getAttribute(attribute);
            val2 = (String) b2.getAttribute(attribute);
        } catch (ModelException e) {
            return 0;
        }

        return val1.compareTo(val2);

    }

} // AttributedBeanComparator
