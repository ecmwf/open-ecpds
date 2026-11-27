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

package ecmwf.ecpds.master.plugin.http.home.monitor;

/**
 * Home interface for Category.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 1.0
 */

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import ecmwf.ecpds.master.plugin.http.home.ModelHomeBase;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.monitor.MonitorException;
import ecmwf.ecpds.master.plugin.http.model.monitor.MonitoredParameter;
import ecmwf.ecpds.master.plugin.http.model.monitor.MonitoredSystem;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOService;

public class MonitoredParameterHome extends ModelHomeBase {

    private static final String INTERFACE = MonitoredParameter.class.getName();

    public static MonitoredParameter create() throws MonitorException {

        try {
            return (MonitoredParameter) DAOService.create(INTERFACE);
        } catch (DAOException e) {
            throw new MonitorException("Error creating object", e);
        }

    }

    public static MonitoredSystem findByPrimaryKey(String key) throws MonitorException {

        try {
            return (MonitoredSystem) DAOService.findByPrimaryKey(INTERFACE, key, true);
        } catch (DAOException e) {
            throw new MonitorException("Error retrieving object set: ", e);
        }

    }

    public static Collection findBySystemClassAndDate(String systemId, String className, Date from, Date to)
            throws MonitorException {
        ModelSearch search = getDefaultSearch("system='" + systemId + "' and monitor='" + className + "' and (date>='"
                + from + "' and date=<'" + to + "')");
        return find(search);
    }

    public static Collection findBySystemClassNameAndDate(String systemId, String className, String name, Date from,
            Date to) throws MonitorException {
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        ModelSearch search = getDefaultSearch("system='" + systemId + "' and monitor='" + className + "' and name='"
                + name + "' and date >= '" + iso.format(from) + "' and date <= '" + iso.format(to) + "'");
        return find(search);
    }

    public static Collection findBySystemAndClass(String systemId, String className) throws MonitorException {
        ModelSearch search = getDefaultSearch("system='" + systemId + "' and monitor='" + className + "'");
        return find(search);
    }

    public static Collection findBySystemClassAndName(String systemId, String className, String name)
            throws MonitorException {
        ModelSearch search = getDefaultSearch(
                "system='" + systemId + "' and monitor='" + className + "' and name='" + name + "'");
        return find(search);
    }

    public static Collection findLastBySystemAndClass(String systemId, String className, int size)
            throws MonitorException {
        ModelSearch search = getDefaultSearch("system='" + systemId + "' and monitor='" + className + "'");
        search.setSizeLimit(size);
        return find(search);
    }

    public static Collection find(ModelSearch search) throws MonitorException {

        try {
            return DAOService.find(INTERFACE, search);
        } catch (DAOException e) {
            throw new MonitorException("Error retrieving object set", e);
        }

    }

} // MonitoredParameterHome
