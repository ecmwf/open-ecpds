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

package ecmwf.ecpds.master.plugin.http.util.search.elements;

import java.util.Iterator;
import ecmwf.ecpds.master.plugin.http.model.AttributedBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;

/**
 * TerminalElement.java
 *
 *
 * Created: Fri May 17 15:52:23 2002
 *
 * @author <a href="mailto: "Daniel Varela Santoalla</a>
 *
 * @version
 */

public class TerminalElement extends BooleanContainerElement implements BooleanExpression {

    private String name;

    public TerminalElement() {

    }

    public TerminalElement(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String v) {
        this.name = v;
    }

    /**
     * Matches any bean that HAS and attribute whose name equals our attribute name.
     *
     * @param b
     *            The bean to match.
     *
     * @return If matches or not.
     */

    public boolean match(AttributedBean b) {

        String beanValue = null;

        try {
            beanValue = b.getAttribute(this.name);
            return true;
        } catch (ModelException e) {
            return false;
        }

    }

    public String dump() {

        String out = " TERM(";
        Iterator i = this.getElements().iterator();
        if (i.hasNext()) {
            BooleanExpression b = (BooleanExpression) i.next();
            out += b.dump();
            while (i.hasNext()) {
                out += "," + ((BooleanExpression) i.next()).dump();
            }
        } else {
            out += this.name;
        }
        out += ") ";
        return out;

    }

}// TerminalElement
