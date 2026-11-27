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
 * Created on 18-Jul-2005
 *
 */
package ecmwf.ecpds.master.plugin.http.model.monitor;

import java.util.Collection;
import java.util.Map;

import ecmwf.ecpds.master.plugin.http.model.HierarchicalBean;
import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.OrderedBean;

/**
 * @author sy8
 *
 *         TODO: Comment ME !!!
 */
public interface MonitoredSystem extends ModelBean, HierarchicalBean, OrderedBean {

    public String getName();

    public String getBigSisterCategory();

    public Collection getMonitorClasses();

    public MonitorClass getMonitorClass(String name) throws MonitorException;

    public Map getConfigParameters();

    /**
     * Return dependences including ancestors
     */
    public Collection getFullDependences() throws ModelException;

    /**
     * Return just non-hierarchical dependencies
     */
    public Collection getDependences();

}
