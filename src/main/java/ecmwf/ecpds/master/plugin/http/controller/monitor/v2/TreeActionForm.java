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

/*
 * Created on 04-Sep-2007
 *
 */
package ecmwf.ecpds.master.plugin.http.controller.monitor.v2;

import java.util.HashSet;
import java.util.Set;

import ecmwf.ecpds.master.plugin.http.controller.ECMWFActionForm;

public class TreeActionForm extends ECMWFActionForm {

    private Set open = new HashSet();
    private Set details = new HashSet();
    private Set config = new HashSet();

    public boolean isOpen(String id) {
        return this.open.contains(id);
    }

    public void setOpen(String id) {
        this.open.add(id);
    }

    public void setClose(String id) {
        this.open.remove(id);
    }

    public boolean getShowDetails(String id) {
        return this.details.contains(id);
    }

    public void setDetails(String id) {
        this.details.add(id);
    }

    public void setNoDetails(String id) {
        this.details.remove(id);
    }

    public boolean getShowConfig(String id) {
        return this.config.contains(id);
    }

    public void setConfig(String id) {
        this.config.add(id);
    }

    public void setNoConfig(String id) {
        this.config.remove(id);
    }

}
