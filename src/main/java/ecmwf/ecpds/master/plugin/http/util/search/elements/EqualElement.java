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

/**
 * AndElement.java
 *
 *
 * Created: Fri May 17 14:47:23 2002
 *
 * @author <a href="mailto: "Daniel Varela Santoalla</a>
 * @version
 */

import java.util.Iterator;
import ecmwf.ecpds.master.plugin.http.model.AttributedBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;

public class EqualElement extends BooleanContainerElement implements BooleanExpression {

    private static int EQUAL = -1;
    private static int NOT_EQUAL = 1;

    private String operator;
    private String name;
    private String value;
    private boolean hasLiteralValue = false;

    public EqualElement() {

    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        this.name = v;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String v) {
        this.value = v;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String v) {
        this.operator = v;
    }

    public void setHasLiteralValue(boolean v) {
        this.hasLiteralValue = v;
    }

    public boolean hasLiteralValue() {
        return this.hasLiteralValue;
    }

    public boolean match(AttributedBean bean) {

        Iterator i = this.getElements().iterator();

        if (!i.hasNext()) {

            String beanValue = null;

            try {
                beanValue = bean.getAttribute(this.name);
            } catch (ModelException e) {
                return false;
            }

            if ("=".equals(this.operator)) {
                if (beanValue.toLowerCase().indexOf(this.value.toLowerCase()) >= 0)
                    return true;
                else
                    return false;

            } else if ("==".equals(this.operator)) {
                if (beanValue.equals(this.value))
                    return true;
                else
                    return false;

            }

        } else {

            while (i.hasNext()) {
                BooleanExpression b = (BooleanExpression) i.next();
                if (!b.match(bean))
                    return false;
            }

        }
        return true;

    }

    public String dump() {

        String quote = this.hasLiteralValue ? "\"" : "";

        String out = " EQ(";
        Iterator i = this.getElements().iterator();
        if (i.hasNext()) {
            BooleanExpression b = (BooleanExpression) i.next();
            out += b.dump();
            while (i.hasNext()) {
                out += this.operator + ((BooleanExpression) i.next()).dump();
            }
        } else {
            out += this.name + this.operator + quote + this.value + quote;
        }
        out += ") ";
        return out;

    }

} // AndElement
