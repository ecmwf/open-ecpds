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
 * Created on 21-Jun-2004
 *
 */
package ecmwf.ecpds.master.plugin.http.services.cache.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.services.cache.CacheException;
import ecmwf.ecpds.master.plugin.http.services.cache.CacheHandler;
import ecmwf.ecpds.master.plugin.http.services.cache.CacheService;

/**
 * Cache handler for classes without a cache.
 *
 * @author sy8
 */

public class NullCacheHandler implements CacheHandler {

    private static Log log = LogFactory.getLog(NullCacheHandler.class);
    private String name = null;
    private Collection cascadeInvalidate = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProperty(String name, String value) {

        if ("cascadeInvalidate".equals(name)) {
            this.cascadeInvalidate = new ArrayList(1);
            StringTokenizer st = new StringTokenizer(value, ",");
            while (st.hasMoreTokens())
                this.cascadeInvalidate.add(st.nextToken());
        }

    }

    public Object get(String key) {
        return null;
    }

    public void set(String key, Object o) {
    }

    public void remove(String key) {
    }

    public void invalidate() {
        invalidate(true);
    }

    public void invalidate(boolean applyCascadeInvalidate) {

        // If we have to propagate the invalidations.
        if (applyCascadeInvalidate && this.cascadeInvalidate != null) {
            Iterator i = this.cascadeInvalidate.iterator();
            while (i.hasNext())
                try {
                    // Invalidate the dependent classes, but not their dependents
                    CacheService.invalidate(i.next().toString(), false);
                } catch (CacheException e) {
                    log.warn("Problem cascading cache invalidation", e);
                }
        }

    }

    public int size() {
        return 0;
    }

    public Iterator getKeysIterator() {
        return new ArrayList(0).iterator();
    }

    public Iterator getValuesIterator() {
        return new ArrayList(0).iterator();
    }

    public Map getAsMap() {
        return new HashMap(0);
    }

}
