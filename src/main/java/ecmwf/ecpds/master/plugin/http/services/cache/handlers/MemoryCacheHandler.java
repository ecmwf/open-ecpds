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

package ecmwf.ecpds.master.plugin.http.services.cache.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.services.cache.CacheException;
import ecmwf.ecpds.master.plugin.http.services.cache.CacheHandler;
import ecmwf.ecpds.master.plugin.http.services.cache.CacheService;

/**
 * <p>
 * A cache handler that uses memory (and only memory) for storage.
 * </p>
 *
 * <p>
 * Created: Mon Sep 9 15:17:20 2002
 * </p>
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 1.0
 */

public class MemoryCacheHandler implements CacheHandler {

    private static Log log = LogFactory.getLog(MemoryCacheHandler.class);

    private Hashtable cache = null;
    private String name = null;
    private Collection cascadeInvalidate = null;

    public MemoryCacheHandler() {
        // FIXME: Use Collections.synchronizedMap(new HashMap) or Doug Lea's ConcurrentHashMap ???
        cache = new Hashtable();
    }

    public String getName() {
        return this.name;
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

    /**
     * We need to do the (key==null) trick to avoid NullPointers happening.
     */

    public Object get(String key) {
        if (key == null)
            return null;
        else
            return cache.get(key);
    }

    public void set(String key, Object o) {
        cache.put(key, o);
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public void invalidate() {
        invalidate(true);
    }

    public void invalidate(boolean applyCascadeInvalidate) {

        cache = new Hashtable();

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
        return cache.size();
    }

    /**
     * @return The keys of this handler.
     */

    public Iterator getKeysIterator() {
        // FIXME: We will have to create a new Collection by copying from the iterator in synchronized block.
        return new EnumerationIterator(this.cache.keys());
    }

    public Iterator getValuesIterator() {
        // FIXME: We will have to create a new Collection by copying from the iterator in synchronized block.
        return new EnumerationIterator(this.cache.elements());
    }

    public Map getAsMap() {
        return new HashMap(this.cache);
    }

} // MemoryCacheHandler
