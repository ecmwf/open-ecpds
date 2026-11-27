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
 * Created: Mon Jul 29 11:02:13 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 1.0
 */

public class ModelSortItem {

    public static final boolean ASCENDING = true;
    public static final boolean DESCENDING = false;

    private String name;
    private boolean ascending;

    public ModelSortItem(String name) {
        int pos;
        if ((pos = name.indexOf(" ")) > 0) {
            this.name = name.substring(0, pos);
            this.ascending = !"DESC".equals(name.substring(pos + 1, name.length()).trim().toUpperCase());
        } else {
            this.name = name.trim();
            this.ascending = ASCENDING;
        }
    }

    public ModelSortItem(String name, boolean ascending) {
        this.name = name;
        this.ascending = ascending;
    }

    public String getName() {
        return this.name;
    }

    public void setWay(boolean way) {
        this.ascending = way;
    }

    public boolean ascending() {
        return this.ascending;
    }

    public boolean descending() {
        return !this.ascending;
    }

} // ModelBeanOrderItem
