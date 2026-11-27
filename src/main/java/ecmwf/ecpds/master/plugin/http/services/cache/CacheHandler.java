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

package ecmwf.ecpds.master.plugin.http.services.cache;

/**
 * CacheHandler.java
 *
 * @author <a href="mailto:dvarela@emcwf.int">Daniel Varela Santoalla</a>
 * @version 0.1.0
 */

import java.util.Iterator;
import java.util.Map;

public interface CacheHandler {

    public String getName();

    public void setName(String name);

    public Object get(String key);

    public void set(String key, Object o);

    public void remove(String key);

    public void invalidate();

    public void invalidate(boolean applyCascadeInvalidate);

    public int size();

    public Iterator getKeysIterator();

    public Iterator getValuesIterator();

    public Map getAsMap();

    public void setProperty(String name, String value);

} // CacheHandler
