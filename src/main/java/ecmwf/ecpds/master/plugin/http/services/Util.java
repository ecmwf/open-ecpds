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
 * Created on 5 Jun 2008
 *
 */
package ecmwf.ecpds.master.plugin.http.services;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.ECMWFException;

public class Util {

    private static Log log = LogFactory.getLog(Util.class);

    public static Object loadAndConfigureObject(Properties set, String name) throws ECMWFException {

        Object o = loadObjectFromConfigSet(set, name);
        configureObject(o, set, name);

        return o;

    }

    public static Object loadObjectFromConfigSet(Properties set, String name) throws ECMWFException {

        String className = set.getProperty(name + ".class");
        if (className == null)
            throw new ECMWFException("Parameter '" + name + ".class' is missing in ConfigSet " + set);
        else
            try {
                return Class.forName(className).newInstance();
            } catch (ClassNotFoundException ex) {
                throw new ECMWFException("Class not found while loading  '" + className + "'", ex);
            } catch (InstantiationException ex) {
                throw new ECMWFException("Instantiation exception while loading '" + className + "'", ex);
            } catch (IllegalAccessException ex) {
                throw new ECMWFException("Illegal access exception while loading '" + className + "'", ex);
            }

    }

    public static void configureObject(Object object, Properties set, String name) {

        try {
            try {
                BeanUtils.setProperty(object, "name", name);
            } catch (Exception e) {
                log.warn("Object " + object + " doesn't have a 'name' property. Name not set.", e);
            }

            Iterator i = set.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                if (e.getKey().toString().startsWith(name + ".") && !(name + ".class").equals(e.getKey()))
                    BeanUtils.setProperty(object, e.getKey().toString().substring(name.length() + 1), e.getValue());
            }
        } catch (Exception e) {
            log.warn("Error configuring Object '" + object + "' from ConfigSet '" + set + "'", e);
        }

    }

}
