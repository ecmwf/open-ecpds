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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * BooleanContainerElement.java
 *
 *
 * Created: Fri May 17 15:01:28 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 0.1.0
 */

public abstract class BooleanContainerElement {

    private ArrayList elements;

    public BooleanContainerElement() {
        this.elements = new ArrayList(1);
    }

    public void addElement(BooleanExpression element) {
        this.elements.add(element);
    }

    public ArrayList getElements() {
        return this.elements;
    }

    public void replaceElement(BooleanExpression out, BooleanExpression in) {
        int where = this.elements.indexOf(out);
        this.elements.set(where, in);
    }

    /**
     * @return "Leaf" elements in the expression tree.
     */

    public Collection getLeaves() {

        ArrayList leaves = new ArrayList();
        Iterator i = this.elements.iterator();
        while (i.hasNext()) {
            BooleanExpression e = (BooleanExpression) i.next();
            if (e.getElements().size() == 0)
                leaves.add(e);
            else
                leaves.addAll(e.getLeaves());
        }
        return leaves;

    }

    public String toString() {
        return this.dump();
    }

    public abstract String dump();

} // BooleanContainerElement
