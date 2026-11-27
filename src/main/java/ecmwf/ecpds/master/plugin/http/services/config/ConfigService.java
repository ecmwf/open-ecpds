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

package ecmwf.ecpds.master.plugin.http.services.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import ecmwf.ecpds.master.plugin.http.util.Util;

/**
 * ConfigService.java This is the common configuration service for the framework. It will load ALL the ".properties"
 * files in the "configDirectory" directory and put them into different sets of properties, for all the applications to
 * use. Every config file should be loaded from this mechanism.
 *
 * Created: Tue Apr 23 16:06:33 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla </a>
 *
 * @version 1.0
 */

public class ConfigService {

    private static ConfigService instance = null;
    private Hashtable propSet;
    private String configDirectory = "WEB-INF/config";
    private String basePath = null;

    /**
     * This method is called from the bootstrap servlet ConfigBootstrapServlet. It should be noted that different
     * "static" instances of ConfigService need to exist in every webapp, so the ConfigBootstrapServlet definition with
     * " <load-on-startup>1 </load-on-startup>" option should be present onj every web.xml file using this classes. The
     * lack of doing this will result on a NULL POINTER exception.
     *
     * @param base
     *            The directory where webapp is installed.
     * @param dir
     *            The relative subdirectory where we have all the configuration files.
     *
     * @throws ConfigException
     *             The dir supplied is bad or a problem happened when trying to open a file.
     *
     */

    public static synchronized void init(String base, String dir) throws ConfigException {

        instance = new ConfigService();
        ConfigService config = findInstance();
        config.basePath = base;
        config.configDirectory = base + dir;
        config.load();
        System.out.println(new Date() + ": ConfigService uses directory '" + config.configDirectory + "'.");

    }

    /**
     * Creates a new <code>ConfigService</code> instance. It will be called at the class loading time. Please note that
     * this is the only service that DOESN'T use the LogService, since it is a prerrequisite for LogService to work. The
     * log output will be shown in "stderr".
     *
     */
    public ConfigService() {
        propSet = new Hashtable();
    }

    private void load() throws ConfigException {

        try {

            for (Enumeration e = this.getConfigurationFiles(configDirectory).elements(); e.hasMoreElements();) {

                Properties props = new Properties();
                String file = (String) e.nextElement();
                props.load(new FileInputStream(configDirectory + "/" + file));
                propSet.put(file.substring(0, file.length() - ".properties".length()), props);
                // System.err.println(new Date()+": ConfigService: Loaded
                // "+file.substring(0,file.length()-".properties".length())+"
                // property set.");
            }
            System.err.println("ConfigService instance Thread.ContextClassLoader: "
                    + Thread.currentThread().getContextClassLoader() + ", ThreadGroup: "
                    + Thread.currentThread().getThreadGroup().getName() + ", ConfigService:" + this + " ("
                    + propSet.size() + " sets)");

        } catch (IOException e) {
            System.err.println("ConfigService: Error loading properties: " + e.getMessage());
        }

    }

    public static String getBasePath() {
        return findInstance().basePath;
    }

    /**
     * Returns a String value for a configuration parameter. It will take the first component of the parameter (till the
     * first point) as the "set" name, and will try to find the rest of the parameter inside that set.
     *
     * @param name
     *            The param name
     *
     * @return The <code>String</code> param value
     *
     * @exception ConfigException
     *                The parameter was not found.
     */
    public static String getParameter(String name) throws ConfigException {

        int dot = name.indexOf(".");
        if (dot < 0)
            throw new ConfigException(
                    "Parameter '" + name + "' has an invalid name. We expect a format like 'propertySet.paramName' ?");
        else
            return getParameter(name.substring(0, dot), name.substring(dot + 1, name.length()));

    }

    /**
     * Returns a String value for a configuration parameter in a given set.
     *
     * @param set
     *            The set name
     * @param name
     *            The param name
     *
     * @return The <code>String</code> param value
     *
     * @exception ConfigException
     *                The parameter was not found.
     */
    public static String getParameter(String set, String name) throws ConfigException {

        Properties props = getPropertySet(set);
        Object value = props.get(name);
        if (value == null) {
            throw new ConfigException("Parameter '" + name + "' not found in property set '" + set
                    + "'. Are you sure that it has been defined in the file '" + findInstance().configDirectory + "/"
                    + set + ".properties' ?");
        }
        return (String) value;

    }

    public static String getOptionalParameter(String set, String name, String defaultValue) {

        try {
            Properties props = getPropertySet(set);
            Object value = props.get(name);
            if (value == null) {
                return defaultValue;
            }
            return (String) value;
        } catch (ConfigException e) {
            return defaultValue;
        }

    }

    public static boolean getOptionalBooleanParameter(String set, String name, boolean defaultValue) {

        String value = getOptionalParameter(set, name, null);
        if (value == null)
            return defaultValue;
        else
            return value.toLowerCase().equals("yes") || value.toLowerCase().equals("true");

    }

    /**
     * Returns a <code>int</code> value for a configuration parameter.
     *
     * @param name
     *            The param name
     *
     * @return The <code>int</code> param value
     *
     * @exception ConfigException
     *                The parameter was not found or it doesn't hold a correct int value.
     */
    public static int getIntParameter(String set, String name) throws ConfigException {

        try {
            return Integer.parseInt(getParameter(set, name));
        } catch (NumberFormatException e) {
            throw new ConfigException("ConfigService.getIntParameter(): '" + getParameter(set, name)
                    + "' doesn't look very 'integer'. ;-)");
        }

    }

    /**
     * Returns a <code>long</code> value for a configuration parameter.
     *
     * @param name
     *            The param name
     *
     * @return The <code>long</code> param value
     *
     * @exception ConfigException
     *                The parameter was not found or it doesn't hold a correct long value.
     */
    public static long getLongParameter(String set, String name) throws ConfigException {

        try {
            return Long.parseLong(getParameter(set, name));
        } catch (NumberFormatException e) {
            throw new ConfigException("ConfigService.getLongParameter(): '" + getParameter(set, name)
                    + "' doesn't look very 'long'. ;-)");
        }

    }

    /**
     * Returns a <code>boolean</code> value for a configuration parameter.
     *
     * @param name
     *            The param name. "true" or "yes", case insensitive, will yield a true value.
     *
     * @return The <code>long</code> param value
     *
     * @exception ConfigException
     *                The parameter was not found.
     */
    public static boolean getBooleanParameter(String set, String name) throws ConfigException {
        String value = getParameter(set, name).toLowerCase();
        return "true".equals(value) || "yes".equals(value);
    }

    public static boolean getBooleanParameter(String set, String name, boolean defaultValue) {
        String value = getOptionalParameter(set, name, Boolean.toString(defaultValue)).toLowerCase();
        return "true".equals(value) || "yes".equals(value);
    }

    /**
     * Returns a <code>Collection</code> value for a configuration parameter. The parameter value is expected to be
     * comma-separated string. The Collection returned will have as many components as the comma-separated string.
     *
     * @param set
     *            The configuration set name.
     * @param name
     *            The param name.
     *
     * @return The <code>Collection</code> param value
     *
     * @exception ConfigException
     *                The parameter was not found.
     */

    public static Collection getCollectionParameter(String set, String name) throws ConfigException {

        String original = getParameter(set, name);
        return Util.listStringToCollection(original, ",");

    }

    /**
     * Returns a <code>Properties</code> object with the full set of parameters provenient from a config file.
     *
     * @param name
     *            The set name.
     *
     * @return The <code>Properties</code> object with the properties inside.
     *
     * @exception ConfigException
     *                The set doesn't exist.
     */
    public static Properties getPropertySet(String name) throws ConfigException {

        Properties props;
        ConfigService config = findInstance();
        Hashtable propSet = config.propSet;
        if (propSet == null || ((props = (Properties) propSet.get(name)) == null))
            throw new ConfigException("Properties set '" + name + "' not found. Expected a file called '"
                    + findInstance().configDirectory + "/" + name
                    + ".properties'. ConfigService may have not been correctly configured and initialized (for Thread ContextClassloader:"
                    + Thread.currentThread().getContextClassLoader() + ").");

        return props;

    }

    // ******************************************************************************

    private Vector getConfigurationFiles(String dir) throws ConfigException {

        Vector result = new Vector();
        File directory = new File(dir);
        String[] files = directory.list();
        if (files == null)
            throw new ConfigException("We could not read dir '" + dir + "' in order to get the configuration files");

        for (int i = 0; i < files.length; i++) {
            if (files[i].endsWith(".properties"))
                result.addElement(files[i]);
        }
        System.out.println(
                new Date() + ": Initializing ConfigService: " + result.size() + " files found in dir '" + dir + "'.");
        return result;

    }

    private static ConfigService findInstance() {
        // ConfigService config = (ConfigService) DiscoverSingleton.find(ConfigService.class,
        // ConfigService.class.getName());
        // return config;
        return instance;

    }

} // ConfigService
