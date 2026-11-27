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

package ecmwf.ecpds.master.plugin.http.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsException;

/**
 * ModelUtils.java
 *
 * @author Daniel Varela Santoalla
 */
public class ModelUtils {
    private static Log log = LogFactory.getLog(ModelUtils.class);

    public static int parseInt(String number) throws ModelException {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            throw new ModelException("Bad number. ", e);
        }
    }

    public static Collection getFieldsFromBean(Object bean) {
        Collection fields = new Vector();
        try {
            Field[] fieldArr = bean.getClass().getDeclaredFields();
            for (int i = 0; i < fieldArr.length; i++)
                fields.add(fieldArr[i].getName());
        } catch (SecurityException e) {
            log.error("Security exception getting fields from class '" + bean.getClass().getName() + "':", e);
        }
        return fields;
    }

    public static Object getFieldValueFromBean(Object bean, String name) throws ModelException {
        try {
            return PropertyUtils.getProperty(bean, name);
        } catch (NoSuchMethodException e) {
            throw new ModelException(
                    "There is no attribute '" + name + "' in class '" + bean.getClass().getName() + "'", e);
        } catch (SecurityException e) {
            throw new ModelException(
                    "Security exception accessing '" + name + "' in class '" + bean.getClass().getName() + "'", e);
        } catch (IllegalArgumentException e) {
            throw new ModelException(
                    "Illegal argument accessing '" + name + "' in class '" + bean.getClass().getName() + "'", e);
        } catch (InvocationTargetException e) {
            throw new ModelException(
                    "Bean exception accessing '" + name + "' in class '" + bean.getClass().getName() + "'", e);
        } catch (IllegalAccessException e) {
            throw new ModelException(
                    "Access exception accessing '" + name + "' in class '" + bean.getClass().getName() + "'", e);
        }
    }

    public static Object getMethodValueFromBean(Object bean, String name, Object parameter) throws ModelException {
        try {
            return MethodUtils.invokeMethod(bean, name, parameter);
        } catch (NoSuchMethodException e) {
            throw new ModelException("There is no method '" + name + "' in class '" + bean.getClass().getName() + "'",
                    e);
        } catch (SecurityException e) {
            throw new ModelException(
                    "Security exception accessing '" + name + "' in class '" + bean.getClass().getName() + "'", e);
        } catch (IllegalArgumentException e) {
            throw new ModelException(
                    "Illegal argument accessing '" + name + "' in class '" + bean.getClass().getName() + "'", e);
        } catch (InvocationTargetException e) {
            throw new ModelException(
                    "Bean exception accessing '" + name + "' in class '" + bean.getClass().getName() + "'", e);
        } catch (IllegalAccessException e) {
            throw new ModelException(
                    "Access exception accessing '" + name + "' in class '" + bean.getClass().getName() + "'", e);
        }
    }

    public static void setValueIntoBean(Object bean, String name, Object value) throws ModelException {
        try {
            PropertyUtils.setProperty(bean, name, value);
        } catch (NoSuchMethodException e) {
            throw new ModelException("There is no method '" + name + "' in class '" + bean.getClass().getName() + "'",
                    e);
        } catch (SecurityException e) {
            throw new ModelException(
                    "Security exception accessing '" + name + "' in class '" + bean.getClass().getName() + "'", e);
        } catch (IllegalArgumentException e) {
            throw new ModelException(
                    "Illegal argument accessing '" + name + "' in class '" + bean.getClass().getName() + "'", e);
        } catch (InvocationTargetException e) {
            throw new ModelException(
                    "Bean exception accessing '" + name + "' in class '" + bean.getClass().getName() + "'", e);
        } catch (IllegalAccessException e) {
            throw new ModelException(
                    "Access exception accessing '" + name + "' in class '" + bean.getClass().getName() + "'", e);
        }
    }

    public static void populateBeanFromMap(Object bean, Map properties, boolean ignoreErrors) throws ModelException {
        Iterator i = properties.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            try {
                setValueIntoBean(bean, entry.getKey().toString(), entry.getValue());
            } catch (ModelException e) {
                if (!ignoreErrors)
                    throw e;
            }
        }
    }

    public static Object getFieldValueFromBean(Object bean, String name, Object defaultValue) {
        try {
            return getFieldValueFromBean(bean, name);
        } catch (ModelException e) {
            return defaultValue;
        }
    }

    public static Object getMethodValueFromBean(Object bean, String name, Object parameter, Object defaultValue) {
        try {
            return getMethodValueFromBean(bean, name, parameter);
        } catch (ModelException e) {
            return defaultValue;
        }
    }

    public static ModelBean getNextWithinCollection(ModelBean me, Collection findMeHere) throws ModelException {
        if (findMeHere == null || findMeHere.size() == 0) {
            log.warn("Requested NEXT within an empty list. Returning meself.");
            return me;
        }
        Iterator i = findMeHere.iterator();
        while (i.hasNext()) {
            ModelBean b = (ModelBean) i.next();
            if (b.getId().equals(me.getId()))
                if (i.hasNext())
                    return (ModelBean) i.next();
                else
                    return (ModelBean) findMeHere.iterator().next();
        }
        log.error("Requested NEXT within a search not containing current item: '" + me.getId() + "' not in '"
                + findMeHere + "'");
        throw new ModelException("Requested NEXT within a search not containing current item: '" + me.getId()
                + "' not in '" + findMeHere + "'");
    }

    public static ModelBean getPriorWithinCollection(ModelBean me, Collection findMeHere) throws ModelException {
        if (findMeHere == null || findMeHere.size() == 0) {
            log.warn("Requested PRIOR within an empty list. Returning meself.");
            return me;
        }
        Iterator i = findMeHere.iterator();
        ModelBean last = null;
        while (i.hasNext()) {
            ModelBean b = (ModelBean) i.next();
            if (b.getId().equals(me.getId()))
                if (last != null) {
                    return last;
                } else {
                    while (i.hasNext())
                        b = (ModelBean) i.next();
                    return b;
                }
            last = b;
        }
        log.error("Requested PRIOR within a search not containing current item: '" + me.getId() + "' not in '"
                + findMeHere + "'");
        throw new NewsException("Requested PRIOR within a search not containing current item: '" + me.getId()
                + "' not in '" + findMeHere + "'");
    }
}
