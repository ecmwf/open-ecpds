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

package ecmwf.ecpds.master.plugin.http.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ecmwf.ecpds.master.plugin.http.ECMWFException;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessor;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorService;

/**
 * Extends the struts ActionForm providing utility methods to:
 * <ul>
 * <li>Get and set "path parameters" from and into paths.</li>
 * <li>Check field validity.</li>
 * <li>Handle errors</li>
 * </ul>
 *
 * Created: Fri Jun 7 13:47:24 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 1.0
 */

public class ECMWFActionForm extends ActionForm {

    private static Log log = LogFactory.getLog(ECMWFActionForm.class);
    // Recognized types
    public final static String STRING = "string";
    public final static String NUMBER = "number";
    public final static String NUMBER_OTHER = "number/other";
    public final static String ISODATE = "isodate";
    public final static String DATE = "date";
    public final static String IDENTIFIER = "identifier";

    public static final String DATE_FORMAT_SHORT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_MEDIUM = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_LONG = "yyyy-MM-dd HH:mm:ss";
    public static final String[] DATE_FORMATS = { DATE_FORMAT_LONG, DATE_FORMAT_MEDIUM, DATE_FORMAT_SHORT };

    public static final String ALLOWED_IN_IDENTIFIER = "abcdefghijklmnopqrstuvwxyz_-.0987654321";

    private ActionErrors errors = null;

    /**
     * Path parameters are those path elements which show after the "mapping path", the path which is sufficient to make
     * the mapping to this Action.
     *
     * @param mapping
     *            The struts mapping which led to this Action.
     * @param request
     *            The servlet request
     *
     * @return A Vector of all the "path parameters"
     */

    public static ArrayList getPathParameters(ActionMapping mapping, HttpServletRequest request) {

        ArrayList requestPathParameters = (ArrayList) request.getAttribute(ECMWFAction.PATH_PARAMETERS_KEY);
        return (requestPathParameters != null) ? requestPathParameters : (new ArrayList());

    }

    /**
     * Return a specific path parameter.
     *
     * @param mapping
     *            The struts mapping which led to this Action
     * @param request
     *            The servlet request
     * @param position
     *            The integer position (starting at 0) of the path parameter to retrieve.
     *
     * @return The string value of parameter
     *
     * @throws ECMWFActionFormException
     *             If there are no enough parameters.
     */

    public static String getPathParameter(ActionMapping mapping, HttpServletRequest request, int position)
            throws ECMWFActionFormException {

        ArrayList v = getPathParameters(mapping, request);
        if (v.size() < position + 1)
            throw new ECMWFActionFormException("There isn't a path parameter in position " + position + ": " + v);
        else
            return (String) v.get(position);

    }

    /**
     * Takes an action forward and, if it ends with "*", substitutes the "*" by a parameter. It is used when the
     * ActionForward depends both on the struts-config.xml file and on some application parameter.
     *
     * @param forward
     *            The original ActionForward
     * @param parameter
     *            What to put instead of the asterisk.
     *
     * @return A new ActionForward with the new path.
     */

    public static ActionForward adaptActionForward(ActionForward forward, String parameter)
            throws ECMWFActionFormException {

        int pos;
        String path = forward.getPath();
        if ((pos = path.indexOf("*")) > 0) {
            path = path.substring(0, pos) + parameter + path.substring(pos + 1);
            return new ActionForward(path, forward.getRedirect());
        } else {
            return forward;
        }

    }

    /**
     * Takes an action forward and, if it ends with "*", substitutes the "*" by a parameter. It is used when the
     * ActionForward depends both on the struts-config.xml file and on some application parameter. This version of the
     * method allows sthe usage of two parameters.
     *
     * @deprecated So far it is not needed. Could be used in the future.
     *
     * @param forward
     *            The original ActionForward
     * @param parameter1
     *            What to put instead of the first asterisk.
     * @param parameter2
     *            What to put instead of the second asterisk.
     *
     * @return A new ActionForward with the new path.
     */

    @Deprecated
    public static ActionForward adaptActionForward(ActionForward forward, String parameter1, String parameter2)
            throws ECMWFActionFormException {

        String path = forward.getPath();
        int pos1, pos2;
        if (((pos1 = path.indexOf("*")) < 0) || ((pos2 = path.indexOf("*", pos1 + 1)) < 0))
            return forward;

        path = path.substring(0, pos1) + parameter1 + path.substring(pos1 + 1, pos2) + parameter2
                + path.substring(pos2 + 1, path.length());
        return new ActionForward(path, forward.getRedirect());

    }

    /**
     * Creates an ActionMessages object from a key and a message.
     *
     * @param key
     *            The key used to lookup the error message in ECMWFResources.properties
     * @param message
     *            The additional message
     *
     * @return The ActionMessages object.
     */

    public static ActionMessages newErrors(String key, String message) {

        ActionMessages errors = new ActionMessages();
        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(key, message));
        return errors;

    }

    public static ActionMessages newErrors(String key, String message1, String message2) {

        ActionMessages errors = new ActionMessages();
        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(key, message1, message2));
        return errors;

    }

    public static ActionMessages newErrors(String key, String message1, String message2, String message3) {

        ActionMessages errors = new ActionMessages();
        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(key, message1, message2, message3));
        return errors;

    }

    /**
     * Creates an ActionMessages object from a key and a ModelException. The resulting ActionMessages is going to
     * contain as many ActionError elements as the length chain of nested exceptions stored in the ModelException
     * (getParent method).
     *
     * @param key
     *            The key used to lookup the error message in ECMWFResources.properties
     * @param e
     *            The ModelException whose message chain is going to be taken.
     *
     * @return The ActionMessages object.
     */

    public static ActionMessages newErrors(String key, ECMWFException e) {

        ActionMessages errors = new ActionMessages();
        String[] messages = e.getMessages();
        for (int i = 0; i < messages.length; i++) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(key, messages[i]));
        }
        return errors;

    }

    /**
     * Every ECMWFActionForm keeps its own list of errors, resulting from from validation failures. This method gets
     * them.
     *
     * @return The list of form validation failures.
     */

    public ActionErrors getErrors() {
        return this.errors;
    }

    /**
     * Resets the error list, coming from validation failures. If the ECMWFActionForm is stored in the session, the list
     * need to be cleaned between form invocations or the errors will just aggregate, preventing any submission after
     * the first error.
     */

    public void cleanErrors() {
        this.errors = null;
    }

    /**
     * Adds an error, using key and message, to the internal error list. For resources with one parameter.
     *
     * @param key
     *            A key to looked up in ECMWFResources.properties.
     * @param message
     *            A message to be inserted in the string retrieved from resources.
     */

    public void addError(String key, String message) {
        (this.errors == null ? (this.errors = new ActionErrors()) : this.errors).add(ActionMessages.GLOBAL_MESSAGE,
                new ActionMessage(key, message));
    }

    /**
     * Add and error, using key, message and value, to the internal error list. For resources with two parameters
     *
     * @param key
     *            A key to looked up in ECMWFResources.properties.
     * @param message
     *            A message to be inserted in the string retrieved from resources
     * @param value
     *            A message to be inserted in the string retrieved from resources
     */

    public void addError(String key, String message, String value) {
        (this.errors == null ? (this.errors = new ActionErrors()) : this.errors).add(ActionMessages.GLOBAL_MESSAGE,
                new ActionMessage(key, message, value));
    }

    /**
     * Add and error, using key, message, value and fieldName, to the internal error list. For resources with three
     * parameters
     *
     * @param key
     *            A key to looked up in ECMWFResources.properties.
     * @param message
     *            A message to be inserted in the string retrieved from resources
     * @param value
     *            A message to be inserted in the string retrieved from resources
     * @param fieldName
     *            A message to be inserted in the string retrieved from resources
     */

    public void addError(String key, String message, String value, String fieldName) {
        (this.errors == null ? (this.errors = new ActionErrors()) : this.errors).add(ActionMessages.GLOBAL_MESSAGE,
                new ActionMessage(key, message, value, fieldName));
    }

    /* ** Parameter checking methods which DO MODIFY the error state ************************************************ */

    /**
     * Check if a value is NOT NULL, adding an error to the internal list with some message if not.
     *
     * @param value
     *            The value to check
     * @param message
     *            The message to add to the errors list if the test fail.
     *
     * @return If the validation succeed.
     */

    public boolean nonNull(String value, String message) {

        if (value == null) {
            this.addError("errors.nullParameter", message, value);
            return false;
        }
        return true;

    }

    /**
     * Check if a value is NOT BLANK, adding an error to the internal list with some message if not.
     *
     * @param value
     *            The value to check
     * @param message
     *            The message to add to the errors list if the test fails.
     *
     * @return If the validation succeed.
     */

    public boolean nonBlank(String value, String message) {

        if (value == null || value.trim().equals("")) {
            this.addError("errors.blankParameter", message);
            return false;
        }
        return true;

    }

    /**
     * Check if some Content is VALID, using a ContentProcessorService content processor, the one configured to validate
     * its content type, adding an error to the internal list with some message if not.
     *
     * @param content
     *            The Content object to check
     * @param attribute
     *            The name of the content object attribute which contains the actual content
     * @param contentType
     *            The content type of the supplied content
     * @param message
     *            The message to add to the errors list if the test fails.
     *
     * @return If the validation succeed.
     *
     * @see ContentProcessorService
     * @see ContentProcessor
     */

    public boolean validateContent(Content content, String attribute, String contentType, String message) {

        log.debug("ECMWFActionForm.validateContent: Checking '" + message + "' for type '" + contentType + "'.");
        try {
            ContentProcessorService.validate(content, attribute, ContentProcessingOptions.getDefault());
        } catch (ContentProcessorException e) {
            this.addError("errors.badContent", message, contentType, e.getFullMessage());
            return false;
        }
        return true;

    }

    /**
     * Check if some String is VALID, using a ContentProcessorService content processor, the one configured to validate
     * its content type, adding an error to the internal list with some message if not.
     *
     * @param text
     *            Literal content
     * @param contentType
     *            The content type of the supplied content
     * @param message
     *            The message to add to the errors list if the test fails.
     *
     * @return If the validation succeed.
     *
     * @see ContentProcessorService
     * @see ContentProcessor
     */

    public boolean validateContent(String text, String contentType, String message) {

        log.debug("ECMWFActionForm.validateContent: Checking '" + message + "' for type '" + contentType + "'.");
        Content content = ContentProcessorService.createContent(text, contentType);
        try {
            ContentProcessorService.validate(content, ContentProcessingOptions.getDefault());
        } catch (ContentProcessorException e) {
            this.addError("errors.badContent", message, contentType, e.getFullMessage());
            return false;
        }
        return true;

    }

    /**
     * Check if a value is VALID, according to some type, adding an error to the internal list with some message if not.
     *
     * @param type
     *            The type expected for the value.
     * @param value
     *            The value to check
     * @param message
     *            The message to add to the errors list if the test fails.
     *
     * @return If the validation succeed.
     */

    public boolean checkType(String type, String value, String fieldName) {

        // log.debug("Testing value '"+value+"' for type '"+type+"'...");

        try {
            if (ECMWFActionForm.STRING.equals(type) || ECMWFActionForm.NUMBER_OTHER.equals(type))
                return ECMWFActionForm.isString(value);
            else if (ECMWFActionForm.NUMBER.equals(type))
                return ECMWFActionForm.isNumber(value);
            else if (ECMWFActionForm.ISODATE.equals(type))
                return ECMWFActionForm.isIsoDate(value);
            else if (ECMWFActionForm.DATE.equals(type))
                return ECMWFActionForm.isDate(value);
            else if (ECMWFActionForm.IDENTIFIER.equals(type))
                return ECMWFActionForm.isIdentifier(value);

            else
                log.warn("ECMWFActionForm: Don't know how to test type '" + type + "'...");

            return true;

        } catch (ECMWFActionFormException e) {
            // The Exception message is a key to the real message...
            this.addError(e.getMessage(), fieldName, value, type);
            return false;
        }

    }

    /**
     * This method converts a value into a Date, increasing/creating the errors object if anything fails.
     *
     * @param value
     *            The string literal
     * @param format
     *            The string format
     *
     * @return The Date object.
     */

    public Date convertToDate(String value, String format) {

        try {
            if (value == null)
                throw new ParseException("", 0);
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date d = sdf.parse(value);
            // log.warn("The original value is '"+value+"'... and the date "+d.toString());
            return d;
        } catch (ParseException e) {
            this.addError("errors.badDate", e.getMessage(), value);
            return new Date();
        }

    }

    /**
     * This method converts a value into a boolean, increasing/creating the errors object if anything fails.
     *
     * @param value
     *            The string literal
     *
     * @return The boolean value.
     */

    public boolean convertToBoolean(String value) {

        if (value != null) {
            String v = value.toLowerCase();
            return "on".equals(v) || "true".equals(v) || "yes".equals(v);
        } else {
            this.addError("errors.badDate", value, "boolean");
            return false;
        }

    }

    /**
     * This method converts a value into a Date, increasing/creating the errors object if everything fails. First it
     * will try to parse the formatDateTime format. If it fails, the second formatDate will ve tried. If EVERYTHING
     * fails an error will be issued.
     *
     * @param value
     *            The string literal
     * @param formatDateTime
     *            A format which includes a the date and the time.
     * @param formatDate
     *            A format only including the date. The time supplied will be the current one (now).
     *
     * @return The Date object.
     */

    public Date convertToDate(String value, String[] formats) {

        try {

            if (value == null)
                throw new ECMWFActionFormException("Can't parse a null value as a date.");

            for (int i = 0; i < formats.length; i++) {
                try {
                    return new SimpleDateFormat(formats[i]).parse(value);
                } catch (ParseException e) {
                }

            }
            throw new ECMWFActionFormException("Can't parse value '" + value + "' with any of the formats: " + formats);

        } catch (ECMWFActionFormException e) {

            this.addError("errors.badDate", e.getFullMessage(), value);
            return new Date();

        }

    }

    /**
     * This method converts a Date into a string, increasing/creating the errors object if anything fails.
     *
     * @param value
     *            The Date object
     *
     * @return The string literal.
     */

    public String convertToString(Date value, String format) {

        if (value != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(value);
        } else
            return "";

    }

    /**
     * This method converts a String into an int, increasing/creating the errors object if anything fails.
     *
     * @param value
     *            The string literal
     *
     * @return The int
     */

    public int convertToInt(String value) {

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            this.addError("errors.badNumber", e.getMessage(), value);
            return 0;
        }

    }

    /**
     * This method converts a String into a long, increasing/creating the errors object if anything fails.
     *
     * @param value
     *            The string literal
     *
     * @return The long
     */

    public long convertToLong(String value) {

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            this.addError("errors.badNumber", e.getMessage(), value);
            return 0;
        }

    }

    /*
     * *** Type and size checking static methods that DO NOT MODIFY the error state (throw ECMWFActionFormException
     * instead) ******
     */

    public static boolean checkSize(String value, int size, String message) {

        return false;

    }

    public static boolean isNumber(String value, String message) {

        return false;

    }

    /**
     * Checks if the value is an ISO DATE, throwing an exception if not. It doesnt't modify the internal error list.
     *
     * @param value
     *            The value to check.
     *
     * @return true.
     *
     * @throws ECMWFActionFormException
     *             If the value is not an ISO DATE.
     */

    public static boolean isIsoDate(String value) throws ECMWFActionFormException {

        try {
            if (value == null)
                throw new ParseException("", 0);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
            sdf.parse(value);
            return true;
        } catch (ParseException e) {
            throw new ECMWFActionFormException("errors.badIsoDate");
        }

    }

    /**
     * Checks if the value is a DATE, throwing an exception if not. It doesnt't modify the internal error list.
     *
     * @param value
     *            The value to check.
     *
     * @return true.
     *
     * @throws ECMWFActionFormException
     *             If the value is not a DATE.
     */

    public static boolean isDate(String value) throws ECMWFActionFormException {

        try {
            if (value == null)
                throw new ParseException("", 0);
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.parse(value);
            return true;
        } catch (ParseException e) {
            throw new ECMWFActionFormException("errors.badDate");
        }

    }

    /**
     * Checks if the value is a NUMBER, throwing an exception if not. It doesnt't modify the internal error list.
     *
     * @param value
     *            The value to check.
     *
     * @return true.
     *
     * @throws ECMWFActionFormException
     *             If the value is not a NUMBER.
     */

    public static boolean isNumber(String value) throws ECMWFActionFormException {

        try {
            if (value == null)
                throw new NumberFormatException();
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            throw new ECMWFActionFormException("errors.badNumber");
        }

    }

    public static boolean isIdentifier(String value) throws ECMWFActionFormException {

        if (value == null)
            throw new ECMWFActionFormException("Null identifier can not be checked");

        String v = value.toLowerCase();
        for (int i = 0; i < v.length(); i++)
            if (ALLOWED_IN_IDENTIFIER.indexOf(v.charAt(i)) < 0)
                throw new ECMWFActionFormException("errors.badIdentifier");

        return true;

    }

    public static boolean isString(String value) {
        return true;
    }

} // ECMWFActionForm
