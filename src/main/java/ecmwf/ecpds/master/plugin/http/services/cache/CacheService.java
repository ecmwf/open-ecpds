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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigException;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;

/**
 * <p>
 * Implements a service providing object caching. This cache is three level tree. The first level is the root
 * (implemented via the <code>Hashtable</code> handlers), the second level are the handlers themselves, object which
 * implement the <code>CacheHandler</code> interface. From every handler hang all the objects (of the same class) stored
 * into it. Every individual object is identified by a <code>String</code> key from a handler, or from a (className,
 * key) pair for the top of the tree.
 * </p>
 *
 * Created: Wed Apr 24 09:33:56 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla </a>
 *
 * @version 1.1
 */

public class CacheService {

    private static final String PARAM_TAG = ".param.";
    private static Log log = LogFactory.getLog(CacheService.class);
    private static CacheService instance = new CacheService();
    private HashMap handlers = new HashMap();

    public CacheService() {

        log.info("Initializing CacheService.");

        try {
            Properties props = ConfigService.getPropertySet("cache");

            // First round, load the handlers

            for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
                String name = (String) e.nextElement();
                String value = (String) props.get(name);
                if (name.indexOf(PARAM_TAG) < 0) {

                    String className = name;
                    String handlerName = value;

                    log.info("Loading handler " + handlerName + " for class " + className);
                    try {
                        CacheHandler handler = (CacheHandler) Class.forName(handlerName).newInstance();
                        handler.setName(className);
                        handlers.put(className, handler);
                    } catch (ClassNotFoundException ex) {
                        log.error("Class not found while loading CacheHandler '" + handlerName + "': ", ex);
                    } catch (InstantiationException ex) {
                        log.error("Instantiation exception while loading CacheHandler '" + handlerName + "': ", ex);
                    } catch (IllegalAccessException ex) {
                        log.error("Illegal access exception while loading CacheHandler '" + handlerName + "': ", ex);
                    }

                }
            }

            // Second round, set parameters

            for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
                String name = (String) e.nextElement();
                String value = (String) props.get(name);
                int pos = 0;
                if ((pos = name.indexOf(PARAM_TAG)) >= 0) {

                    try {
                        String className = name.substring(0, pos);
                        String propName = name.substring(pos + PARAM_TAG.length(), name.length());
                        String propValue = value;
                        CacheHandler handler = (CacheHandler) this.handlers.get(className);
                        handler.setProperty(propName, propValue);
                    } catch (Exception ex) {
                        log.error("Problem assigning property from name '" + name + "' value '" + value + "'", ex);
                    }
                }

            }

        } catch (ConfigException e) {
            log.error("Unable to configure CacheService: ", e);
        }

    }

    /**
     * Gets an <code>Object</code> from the cache. In principle it is not guaranteed that the object returned belongs to
     * the class requested, or that it IS a <code>Collection</code> of objects from that class. TODO: Get this to be
     * true. Implement checkings in "put" operation and maybe in "get".... See performance considerations.
     *
     * @param objectClass
     *            The class of the object trying to be retrieved. This value is going to be used to lookup a
     *            <code>CacheHandler</code> object, responsible for the final storage.
     * @param key
     *            The name of the object to be retrieved. It might be an Entity PK or a ModelBeanSearch signature (in
     *            this case returning a <code>Collection</code>).
     *
     * @return The Object if found, <code>null</code> else.
     *
     * @throws CacheException
     *             If a problem (like when there is no handler for the class) is found.
     */

    public static Object get(String objectClass, String key) throws CacheException {
        Object o = findInstance().getHandler(objectClass).get(key);

        if (log.isDebugEnabled())
            if (o == null)
                log.debug("CacheService: Miss for '" + objectClass + "' key '" + key + "'");
            else
                log.debug("CacheService: Hit for '" + objectClass + "' key '" + key + "'");

        return o;
    }

    /**
     * Stores an <code>Object</code> into the cache. Right now no checkings are made that the object belongs to the
     * class (or is a <code>Collection</code> of objects of that class.
     *
     * @param objectClass
     *            The class of the object. Used to lookup the handler.
     * @param key
     *            The key under the object will be stored.
     * @param o
     *            The object itself to store.
     *
     * @throws CacheException
     *             If a problem (like when there is no handler for the class) is found.
     *
     */

    public static void set(String objectClass, String key, Object o) throws CacheException {

        if (log.isDebugEnabled())
            log.debug("CacheService: Storing '" + key + "'");
        findInstance().getHandler(objectClass).set(key, o);

    }

    public static void remove(String objectClass, String key) throws CacheException {

        if (log.isDebugEnabled())
            log.debug("CacheService: Storing '" + key + "'");
        findInstance().getHandler(objectClass).remove(key);

    }

    /**
     * Invalidates (empties) a full handler, deleting all objects into it. (or is a <code>Collection</code> of objects
     * of that class.
     *
     * @param objectClass
     *            The class of the object. Used to lookup the handler.
     *
     * @throws CacheException
     *             If a problem (like when there is no handler for the class) is found.
     *
     */

    public static void invalidate(String objectClass, boolean applyCascadeInvalidate) throws CacheException {

        if (log.isDebugEnabled())
            log.debug("CacheService: Invalidating class '" + objectClass + "'. Cascade Invalidate set to "
                    + applyCascadeInvalidate);
        findInstance().getHandler(objectClass).invalidate(applyCascadeInvalidate);

    }

    public static void invalidate(String objectClass) throws CacheException {

        if (log.isDebugEnabled())
            log.debug("CacheService: Invalidating class '" + objectClass + "'");
        findInstance().getHandler(objectClass).invalidate();

    }

    public static String getInfo() {

        CacheService instance = findInstance();
        String out = "";
        Iterator i = instance.handlers.keySet().iterator();
        while (i.hasNext()) {
            try {
                String className = (String) i.next();
                out += className + ":\t" + instance.getHandler(className).getClass().getName() + " ("
                        + instance.getHandler(className).size() + ")\n";
            } catch (CacheException ex) {
                out += "\n" + ex.getMessage() + "\n";
            }
        }
        return out;
    }

    public static String getCacheContentsListing() {

        String out = "";
        Iterator i = findInstance().handlers.entrySet().iterator();
        while (i.hasNext()) {
            try {
                Entry e = (Entry) i.next();
                CacheHandler handler = (CacheHandler) e.getValue();
                out += "\n**************************************\n";
                out += "* Handler " + handler.getClass().getName() + " With name: " + handler.getName() + "\n\n";
                Map values = handler.getAsMap();
                Iterator j = values.entrySet().iterator();
                while (j.hasNext()) {
                    Entry f = (Entry) j.next();
                    out += f.getKey() + " => (";
                    if (f.getValue() != null)
                        out += f.getValue().getClass() + ")" + f.getValue() + "\n";
                    else
                        out += "null)\n";
                }

            } catch (Exception ex) {
                out += "\n" + ex.getMessage() + "\n";
            }
        }
        return out;
    }

    /**
     * Returns the proper <code>CacheHandler</code> for a given class name.
     *
     * @param className
     *            The name of the class for which we want to get a <code>CacheHandler</code>
     *
     * @return The handler
     *
     * @exception CacheException
     *                If an appropriate handler is not found.
     */

    public static CacheHandler getHandler(String className) throws CacheException {

        CacheHandler handler = (CacheHandler) (findInstance().handlers.get(className));
        if (handler == null)
            throw new CacheException("Couldn't find a CacheHandler for class '" + className
                    + "'. Please check file 'cache.properties'.");
        else
            return handler;

    }

    /**
     * Returns the proper <code>CacheHandler</code> for a given object.
     *
     * @param b
     *            The object to which we want to find a handler
     *
     * @return The handler
     *
     * @exception CacheException
     *                If an appropriate handler is not found.
     */

    // public CacheHandler getHandler(Object b) throws CacheException {
    // return this.getHandler(b.getClass().getName());
    // }
    /**
     * Store the individual beans of a <code>Collection</code> by their primary keys. With this enabled, a primary key
     * search will return a hit, after some other query which included this bean. This method will be called for
     * <code>DAOService.find(search)</code> operations whose search has the <code>storeSearchResults</code> attribute to
     * true;
     *
     * @param beanClass
     *            The class of the ModelBean included in the Collection
     * @param c
     *            The collection of the ModelBeans
     *
     * @throws CacheException
     *             If the handler is not found or any of the objects inside the collection is not an
     *             <code>ModelBean</code>
     *
     * @see <code>ModelBeanSearch</code>
     */

    public static void setModelBeanCollection(String beanClass, Collection c) throws CacheException {

        try {

            Iterator i = c.iterator();
            CacheHandler handler = findInstance().getHandler(beanClass);
            while (i.hasNext()) {
                ModelBean b = (ModelBean) i.next();
                if (log.isDebugEnabled())
                    log.debug("CacheService: Storing from Collection '" + b.getId() + "'.");
                handler.set(b.getId(), b);
            }

        } catch (ClassCastException e) {
            throw new CacheException("ClassCast exception: Expecting a ModelBean in a Collection: " + e.getMessage());
        }

    }

    /**
     * Returns a Collection which includes all the handlers available in this CacheService.
     *
     * @return A Collection with all the handlers.
     */

    public static Collection getHandlers() throws CacheException {
        return new ArrayList(findInstance().handlers.values());
    }

    public static Iterator getKeysIterator(String name) throws CacheException {
        return findInstance().getHandler(name).getKeysIterator();
    }

    public static Iterator getValuesIterator(String name) throws CacheException {
        return findInstance().getHandler(name).getValuesIterator();
    }

    private static CacheService findInstance() {
        return instance;
        // return (CacheService) DiscoverSingleton.find(CacheService.class, CacheService.class.getName());
    }

} // CacheService
