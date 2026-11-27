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

package ecmwf.ecpds.master.plugin.http.services.persistence;

import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.ECMWFException;
import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.services.Util;
import ecmwf.ecpds.master.plugin.http.services.cache.CacheException;
import ecmwf.ecpds.master.plugin.http.services.cache.CacheService;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;

/**
 * The central point for all the persistence engine requests. It contains different <code>DAOHandler</code> classes for
 * the different <code>ModelBean</code> classes. It should not be called directly from user code with the exception of
 * Home interfaces.
 *
 * Created: Wed Apr 24 09:33:56 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 1.0
 */

public class DAOService {

    private static Log log = LogFactory.getLog(DAOService.class);
    private static final int MAX_CACHEABLE_RESULT_SIZE = 1200;
    private static DAOService instance = new DAOService();
    private Hashtable handlers;
    private static int warnIfLongerThan = 5;

    /**
     * Initializes the service. Executed when the class is loaded. It loads all the handlers.
     */

    public DAOService() {

        handlers = new Hashtable();
        log.info("Initializing DAOService.");

        /*
         * try { Properties props = ConfigService.getPropertySet("dao"); for (Enumeration e = props.propertyNames();
         * e.hasMoreElements();) { String className = (String) e.nextElement(); String handlerName = (String)
         * props.get(className); log.info("Loading handler " + handlerName + " for class ." + className); try {
         * DAOHandler handler = (DAOHandler) Class.forName(handlerName).newInstance(); handlers.put(className, handler);
         * } catch (ClassNotFoundException ex) { log.error("Class not found while loading DAOHandler '" + handlerName +
         * "': ", ex); } catch (InstantiationException ex) {
         * log.error("Instantiation exception while loading DAOHandler '" + handlerName + "': ", ex); } catch
         * (IllegalAccessException ex) { log.error("Illegal access exception while loading DAOHandler '" + handlerName +
         * "': ", ex); }
         *
         * }
         *
         * } catch (ConfigException e) { log.error("Unable to configure DAOService: ", e); }
         */

    }

    public static ModelBean create(String beanClass) throws DAOException {
        return findInstance().getHandler(beanClass).create();
    }

    public static ModelBean findByPrimaryKey(String beanClass, String key) throws DAOException {
        return findByPrimaryKey(beanClass, key, false);
    }

    public static ModelBean findByPrimaryKey(String beanClass, String key, boolean cacheable) throws DAOException {

        ModelBean result = null;
        try {
            if (cacheable) {
                result = (ModelBean) CacheService.get(beanClass, key);
                if (result == null) {
                    result = findInstance().getHandler(beanClass).findByPrimaryKey(key);
                    CacheService.set(beanClass, key, result); // Save data in the cache for further use.
                }
            } else {
                result = findInstance().getHandler(beanClass).findByPrimaryKey(key);
            }
        } catch (CacheException e) {
            if (log.isDebugEnabled())
                log.debug("DAOService: CacheException: ", e);
            result = findInstance().getHandler(beanClass).findByPrimaryKey(key);
        } catch (ClassCastException e) {
            log.error("ClassCastException. Expecting a ModelBean instance: ", e);
            result = findInstance().getHandler(beanClass).findByPrimaryKey(key);
        }

        return result;

    }

    /**
     *
     *
     * @param beanClass
     *            The class of the bean we try to get. This is used to choose a <code>DAOHandler</code>.
     * @param search
     *            The search to be executed.
     *
     * @return A <code>Collection</code> of the objects of the requested type which comply with the search.
     *
     * @throws DAOException
     *             Exception passed from <code>DAOHandler</code> classes when anything goes wrong in the retrieval.
     */

    public static Collection find(String beanClass, ModelSearch search) throws DAOException {

        Collection results;
        Date begin = new Date();

        // If allowed, first try in the cache.., if not allowed, or data not available, go to the DAOHandler.
        try {
            if (search.cacheable()) {
                Object cacheResults = CacheService.get(beanClass, search.getKey());
                if (cacheResults == null || !(cacheResults instanceof Collection)) {
                    results = findInstance().getHandler(beanClass).find(search);
                    if (results.size() < MAX_CACHEABLE_RESULT_SIZE) {
                        CacheService.set(beanClass, search.getKey(), results); // Save the whole in the cache for
                                                                               // further use.
                        if (search.storeSearchResults()) { // Do we want to store the individual objects also ?
                            CacheService.setModelBeanCollection(beanClass, results);
                        }
                    } else {
                        log.warn("The search '" + search.getKey() + "' returned more than " + MAX_CACHEABLE_RESULT_SIZE
                                + " items (" + results.size() + "). It won't be cached.");
                    }
                } else {
                    results = (Collection) cacheResults;
                }
            } else {
                results = findInstance().getHandler(beanClass).find(search);
            }
        } catch (CacheException e) {
            if (log.isDebugEnabled())
                log.debug("CacheException: ", e);
            results = findInstance().getHandler(beanClass).find(search);
        } catch (ClassCastException e) {
            log.error("ClassCastException. Expecting a java.util.Collection: ", e);
            results = findInstance().getHandler(beanClass).find(search);
        }

        Date end = new Date();
        double diff = (end.getTime() - begin.getTime()) / 1000.0;
        if (diff > warnIfLongerThan) {
            log.warn(search.getKey() + " got " + results.size() + " elements in " + diff
                    + " secs. Performance must be reviewed.");
        } else if (log.isDebugEnabled())
            log.debug(search.getKey() + " got " + results.size() + " elements in " + diff + " secs.");

        return results;

    }

    public static void save(ModelBean bean, Object context) throws DAOException {

        String interfaceName = bean.getBeanInterfaceName();
        try {
            CacheService.invalidate(interfaceName);
        } catch (CacheException e) {
            log.warn("CacheException: ", e);
        }
        findInstance().getHandler(interfaceName).save(bean, context);

    }

    public static void insert(ModelBean bean, Object context) throws DAOException {

        String interfaceName = bean.getBeanInterfaceName();
        try {
            CacheService.invalidate(interfaceName);
        } catch (CacheException e) {
            log.warn("CacheException: ", e);
        }
        findInstance().getHandler(interfaceName).insert(bean, context);

    }

    public static void delete(ModelBean bean, Object context) throws DAOException {

        String interfaceName = bean.getBeanInterfaceName();
        try {
            CacheService.invalidate(interfaceName);
        } catch (CacheException e) {
            log.warn("CacheException: ", e);
        }
        findInstance().getHandler(interfaceName).delete(bean, context);

    }
    // Private ***************************************************************************************

    /*
     * private DAOHandler getHandler(String className) throws DAOException {
     *
     * DAOHandler handler = (DAOHandler) handlers.get(className); if (handler == null) throw new
     * DAOException("Couldn't find a DAOHandler for class '" + className + "'. Please check file 'dao.properties'.");
     * else return handler;
     *
     * }
     */

    private DAOHandler getHandler(String name) throws DAOException {

        DAOService instance = findInstance();
        DAOHandler handler = (DAOHandler) instance.handlers.get(name);
        Object o = null;
        if (handler == null) {
            synchronized (instance.handlers) {
                handler = (DAOHandler) instance.handlers.get(name);
                // Maybe somebody else initialized it while we were blocked.... Check again!!
                if (handler == null) {
                    try {
                        o = Util.loadAndConfigureObject(ConfigService.getPropertySet("dao"), name);
                        handler = (DAOHandler) o;
                        instance.handlers.put(name, handler);
                        if (log.isInfoEnabled())
                            log.info(
                                    "Loaded DAOHandler for '" + name + "' implemented by '" + handler.getClass() + "'");
                        return handler;
                    } catch (ECMWFException e) {
                        throw new DAOException("Can't load a DAOHandler for class '" + name + "'", e);
                    } catch (ClassCastException e) {
                        throw new DAOException("Object with class '" + o.getClass() + "' is not a '" + DAOHandler.class
                                + "' instance as expected", e);
                    }
                } else
                    return handler;
            }
        } else
            return handler;

    }

    /*
     * private DAOHandler getHandler(ModelBean b) throws DAOException { return
     * this.getHandler(b.getBeanInterfaceName()); }
     */

    private static DAOService findInstance() {
        // return (DAOService)DiscoverSingleton.find(DAOService.class,DAOService.class.getName());
        return instance;
    }

} // DAOService
