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
 * Created on 29-Aug-2007
 *
 */
package ecmwf.ecpds.master.plugin.http.model.monitor;

import java.util.Date;

import ecmwf.ecpds.master.plugin.http.model.ModelBean;

public interface MonitoredParameter extends ModelBean {

    public static final Integer STORED = new Integer(0); // Parameter will be written into the database. This is the
                                                         // default expected behaviour
    public static final Integer CONFIG = new Integer(1); // The parameter will be kept in the Task status, but not into
                                                         // the db.

    public String getMonitoredSystemId();

    public String getMonitorClassName();

    public String getName();

    public Object getValue();

    public Date getDate();

    public void setMonitoredSystemId(String v);

    public void setMonitorClassName(String v);

    public void setName(String v);

    public void setValue(Object o);

    public void setDate(Date d);

}
