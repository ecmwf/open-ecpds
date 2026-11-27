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
 * Created on 30-ene-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ecmwf.ecpds.master.plugin.http.services.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import ecmwf.ecpds.master.plugin.http.ECMWFException;
import ecmwf.ecpds.master.plugin.http.services.Util;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigException;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;

/**
 * @author daniel
 *
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class CommandService {

    private static Log log = LogFactory.getLog(CommandService.class);
    private static CommandService instance = new CommandService();
    private HashMap commands = new HashMap();
    private Collection availableCommands;
    private static final String COMMAND_PREF = "command.";
    private static final String COMMAND_CLASS = ".class";

    private Pattern parameterPattern = null;

    public CommandService() {
        // initializeCommands();
        PatternCompiler compiler = new Perl5Compiler();
        try {
            this.parameterPattern = compiler.compile(ConfigService.getParameter("content", "parameterMatch.regexp"),
                    Perl5Compiler.READ_ONLY_MASK);
            this.availableCommands = ConfigService.getCollectionParameter("content", "commands.available");
        } catch (MalformedPatternException e) {
            log.error("Problem with parameter matching regexp", e);
        } catch (ConfigException e) {
            log.error("Problem configuring parameter matching regexp", e);
        }

    }

    /*
     * public static Command getCommand(String name) throws ContentProcessorException {
     *
     * Command command = (Command) findInstance().commands.get(name); if (command == null) throw new
     * ContentProcessorException("Can't find a Command named '" + name + "'. Please check file 'content.properties'.");
     * else { return command; }
     *
     * }
     */

    public static Collection getAllCommands() throws ContentProcessorException {

        CommandService instance = findInstance();
        Iterator i = instance.availableCommands.iterator();
        ArrayList c = new ArrayList();
        while (i.hasNext()) {
            c.add(instance.getCommand(i.next().toString()));
        }
        return c;

    }

    public static String executeCommand(Content container, String fullCommand, String commandBody,
            ContentProcessingOptions options, boolean safeMode) throws ContentProcessorException {

        int pos = fullCommand.indexOf(" ");
        String command = (pos > 0) ? fullCommand.substring(0, pos) : fullCommand;
        HashMap params = getParametersFromFullCommand(fullCommand);

        return executeCommand(container, command, params, commandBody, options, safeMode);

    }

    public static String executeCommand(Content container, String command, HashMap params, String commandBody,
            ContentProcessingOptions options, boolean safeMode) throws ContentProcessorException {
        try {
            return getCommand(command).getOutput(container, params, commandBody, options);
        } catch (ContentProcessorException e) {
            if (safeMode)
                return "[" + command + params + ": " + Arrays.asList(e.getMessages()) + "]";
            else
                throw e;
        }
    }

    /**
     * Return one parameter. This method calculates the full parameter list every time, so if it going to be used more
     * than once, replace it by getParameters();
     *
     * @see getParameters(String)
     *
     * @param commandText
     *            The full text of the command
     * @param name
     *            The name of the parameters.
     *
     * @throws ContentProcessorException
     *             If the parameter doesn't exist.
     */

    public static String getParameter(String commandText, String name) throws ContentProcessorException {
        HashMap parameters = getParameters(getParameterString(commandText));
        String value = parameters.get(name).toString();
        if (value == null)
            throw new ContentProcessorException("[" + commandText + ": Needs a '" + name + "' parameter]");
        else
            return value;

    }

    /**
     * @return The part of the command which is not the name. From the name onwards.
     */

    public static String getParameterString(String commandText) {

        int pos = 0;
        if (commandText != null && (pos = commandText.indexOf(" ")) > 0)
            return commandText.substring(pos);
        else
            return "";

    }

    /**
     * Returns a HashMap with all the attributes. MultipleAttributes will be a Collection. Attributes without value will
     * have "true" as value.
     *
     * @param parameterString
     *            The part of the command which is not its name
     *
     * @return The HashMap with all the attributes.
     */

    public static HashMap getParameters(String parameterString) {

        HashMap parameters = new HashMap(3);

        PatternMatcher matcher = new Perl5Matcher();
        PatternMatcherInput input = new PatternMatcherInput(parameterString);

        MatchResult result = null;
        CommandService instance = findInstance();
        while (matcher.contains(input, instance.parameterPattern)) {
            result = matcher.getMatch();
            String name = result.group(1);
            String value = result.group(2);

            if (log.isDebugEnabled())
                log.debug("Detected parameter: " + name + ": " + value);

            if (name != null && value != null) {
                name = name.trim();
                value = value.trim();
                if (value.startsWith("'"))
                    value = value.replace('\'', ' ');
                else if (value.startsWith("\""))
                    value = value.replace('\"', ' ');
                value = value.trim();
                parameters.put(name, value);
            }
        }

        return parameters;
    }

    public static HashMap getParametersFromFullCommand(String fullCommand) {
        return getParameters(getParameterString(fullCommand));
    }

    /*
     * private void initializeCommands() {
     *
     * try {
     *
     * this.commands = new HashMap(); Properties props = ConfigService.getPropertySet("content");
     *
     * Collection names = ConfigService.getCollectionParameter("content", "commands.available"); Iterator c =
     * names.iterator(); while (c.hasNext()) {
     *
     * String commandName = c.next().toString(); try {
     *
     * log.info("Loading command '" + commandName + "'."); Command command = null; this.commands.put(commandName,
     * command = (Command) this.loadHandler(commandName, props.getProperty(COMMAND_PREF + commandName + COMMAND_CLASS),
     * Command.class));
     *
     * command.setOption("name", commandName); // Now lets get the options Iterator i = props.keySet().iterator(); while
     * (i.hasNext()) { String entryName = i.next().toString(); if (entryName.startsWith(COMMAND_PREF + commandName)) {
     * String propertyName = entryName.substring((COMMAND_PREF + commandName).length() + 1, entryName.length()); String
     * propertyValue = props.getProperty(entryName); command.setOption(propertyName, propertyValue);
     *
     * } } } catch (ContentProcessorException e) { log.error("Problem loading command '" + commandName + "'"); }
     *
     * }
     *
     * } catch (ConfigException e) { log.error("Unable to configure CommandService: ", e); }
     *
     * }
     *
     */
    /*
     * private Object loadHandler(String contentType, String className, Class expectedType) throws
     * ContentProcessorException {
     *
     * log.info("Loading class '" + className + "' with key '" + contentType + "'"); try {
     *
     * Object o = Class.forName(className).newInstance(); if (expectedType.isAssignableFrom(o.getClass())) return o;
     * else throw new ContentProcessorException("Loaded a class of type '" + o.getClass().getName() +
     * "' while expected '" + expectedType.getName() + "'");
     *
     * } catch (ClassNotFoundException ex) { String message = "Class not found while loading Processor '" + contentType
     * + "'"; log.error(message, ex); throw new ContentProcessorException(message, ex);
     *
     * } catch (InstantiationException ex) { String message = "Instantiation exception while loading Processor '" +
     * contentType + "'"; log.error(message, ex); throw new ContentProcessorException(message, ex);
     *
     * } catch (IllegalAccessException ex) { String message = "Illegal access exception while loading Processor '" +
     * contentType + "'"; log.error(message, ex); throw new ContentProcessorException(message, ex); }
     *
     * }
     *
     */
    public static Command getCommand(String name) throws ContentProcessorException {

        CommandService instance = findInstance();
        Map commands = instance.commands;
        try {
            Command command = (Command) commands.get(name);
            if (command == null) {
                if (instance.availableCommands.contains(name)) {
                    command = (Command) Util.loadAndConfigureObject(ConfigService.getPropertySet("content"),
                            COMMAND_PREF + name);
                    synchronized (command) {
                        commands.put(name, command);
                    }
                } else {
                    throw new ContentProcessorException("Command '" + name + "' is not allowed in this setup");
                }
            }
            return command;
        } catch (ECMWFException e) {
            throw new ContentProcessorException(
                    "Can't find a Command with name '" + name + "'. Please check file 'content.properties'.", e);
        }

    }

    private static CommandService findInstance() {
        // return (CommandService)DiscoverSingleton.find(CommandService.class,CommandService.class.getName());
        return instance;
    }

}