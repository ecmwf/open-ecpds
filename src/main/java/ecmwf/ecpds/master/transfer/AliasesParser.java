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

package ecmwf.ecpds.master.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_ALIAS;

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.Destination;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.ecaccess.ECaccessServer;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.text.Format;

/**
 * The Class AliasesParser.
 */
public final class AliasesParser {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(AliasesParser.class);

    /** The Constant _dataBase. */
    private static final ECpdsBase dataBase = StarterServer.getInstance(ECaccessServer.class)
            .getDataBase(ECpdsBase.class);

    /** The _destinations. */
    private final HashMap<String, Destination> destinations = new HashMap<>();

    /** The _rules. */
    private final HashMap<String, AliasOptions> rules = new HashMap<>();

    /**
     * Instantiates a new aliases parser. Let's go recursively through the Aliases to build the options tree.
     *
     * @param destination
     *            the destination
     * @param target
     *            the target
     * @param lifetime
     *            the lifetime
     * @param delay
     *            the delay
     * @param priority
     *            the priority
     * @param asap
     *            the asap
     * @param event
     *            the event
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws DataBaseException
     *             the data base exception
     */
    public AliasesParser(final Destination destination, final String target, final String lifetime, final long delay,
            final int priority, final boolean asap, final boolean event) throws IOException, DataBaseException {
        parse(destinations, rules, destination, new ArrayList<>(), target, lifetime, delay, priority, asap, event);
    }

    /**
     * Get the list of Destinations where a DataTransfer should be created.
     *
     * @return the destinations
     */
    public Collection<Destination> getDestinations() {
        return destinations.values();
    }

    /**
     * Get the options for a specific Destination.
     *
     * @param destination
     *            the destination
     *
     * @return the alias options
     */
    public AliasOptions getAliasOptions(final String destination) {
        return rules.get(destination);
    }

    /**
     * The Class AliasOptions. Used to store the new values for the DataTransfer for each Destination.
     */
    public static final class AliasOptions {
        /** The _via. */
        private final List<String> via = new ArrayList<>();

        /** The _target. */
        private final String target;

        /** The _life time. */
        private final String lifeTime;

        /** The _delay. */
        private final long delay;

        /** The _priority. */
        private final int priority;

        /** The _asap. */
        private final boolean asap;

        /** The _event. */
        private final boolean event;

        /**
         * Instantiates a new alias options.
         *
         * @param via
         *            the via
         * @param target
         *            the target
         * @param lifeTime
         *            the life time
         * @param delay
         *            the delay
         * @param priority
         *            the priority
         * @param asap
         *            the asap
         * @param event
         *            the event
         */
        AliasOptions(final ArrayList<String> via, final String target, final String lifeTime, final long delay,
                final int priority, final boolean asap, final boolean event) {
            this.via.addAll(via);
            this.target = target;
            this.lifeTime = lifeTime;
            this.delay = delay;
            this.priority = priority;
            this.asap = asap;
            this.event = event;
        }

        /**
         * Gets the via.
         *
         * @return the via
         */
        public List<String> getVia() {
            return via;
        }

        /**
         * Gets the target.
         *
         * @return the target
         */
        public String getTarget() {
            return target;
        }

        /**
         * Gets the life time.
         *
         * @return the life time
         */
        public String getLifeTime() {
            return lifeTime;
        }

        /**
         * Gets the delay.
         *
         * @return the delay
         */
        public long getDelay() {
            return delay;
        }

        /**
         * Gets the priority.
         *
         * @return the priority
         */
        public int getPriority() {
            return priority;
        }

        /**
         * Gets the asap.
         *
         * @return the asap
         */
        public boolean getAsap() {
            return asap;
        }

        /**
         * Gets the event.
         *
         * @return the event
         */
        public boolean getEvent() {
            return event;
        }
    }

    /**
     * Go through each Alias and if not already in the queue then process it recursively if requested.
     *
     * @param destinations
     *            the destinations
     * @param rules
     *            the rules
     * @param destination
     *            the destination
     * @param via
     *            the via
     * @param target
     *            the target
     * @param lifeTime
     *            the life time
     * @param delay
     *            the delay
     * @param priority
     *            the priority
     * @param asap
     *            the asap
     * @param event
     *            the event
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws DataBaseException
     *             the data base exception
     */
    private static void parse(final HashMap<String, Destination> destinations,
            final HashMap<String, AliasOptions> rules, final Destination destination, final ArrayList<String> via,
            final String target, final String lifeTime, final long delay, final int priority, final boolean asap,
            final boolean event) throws IOException, DataBaseException {
        final var destinationName = destination.getName();
        _log.debug("Adding rule for {}", destinationName);
        destinations.put(destinationName, destination);
        rules.put(destinationName, new AliasOptions(via, target, lifeTime, delay, priority, asap, event));
        final var setup = DESTINATION_ALIAS.getECtransSetup(destination.getData());
        _log.debug("Recursive search on {} for {}", destinationName, target);
        for (final Destination alias : dataBase.getDestinations(destinationName)) {
            // Should we alias this Data Transfer to this Destination?
            final var aliasName = alias.getName();
            if (!destinations.containsKey(aliasName)) {
                final var targetFile = new File(target);
                final var options = setup.getOptions(aliasName, target, null);
                // Let's inject a few parameters!
                options.inject("$target", target);
                options.inject("$name", targetFile.getName());
                options.inject("$path", targetFile.getPath());
                options.inject("$parent", targetFile.getParent());
                options.inject("$destination", destinationName);
                options.inject("$alias", aliasName);
                final long date;
                final var delta = options.get("datedelta", "0");
                final var format = options.get("dateformat", "yyyyMMdd");
                // Do we have a source to parse the date from?
                final var source = options.get("datesource", null);
                if (isNotEmpty(source)) {
                    // Let's parse the date from the source according to the
                    // provided pattern!
                    final var pattern = options.get("datepattern", format);
                    final var simpleFormat = new SimpleDateFormat(pattern);
                    try {
                        date = simpleFormat.parse(source).getTime();
                    } catch (final Throwable t) {
                        throw new IOException("parsing date in " + source);
                    }
                } else {
                    // There is no source to parse the date from so let's use
                    // the current time!
                    date = System.currentTimeMillis();
                }
                // Let's now inject the date!
                options.inject("$date", Format.formatTime(format, date + Format.getDuration(delta)));
                if (options.matches("pattern", target, ".*") && !options.matches("ignore", target)) {
                    // We have a new level!
                    @SuppressWarnings("unchecked")
                    final var newVia = (ArrayList<String>) via.clone();
                    newVia.add(aliasName);
                    // Let's build the new parameters from the options!
                    final var newTarget = options.get("target", target);
                    if (!target.equals(newTarget)) {
                        _log.debug("Force a new target {} for Destination: {}", newTarget, aliasName);
                    }
                    final var newLifeTime = options.get("lifeTime", lifeTime);
                    final var newPriority = options.get("priority", priority);
                    final var newAsap = options.get("asap", asap);
                    final var newEvent = options.get("event", event);
                    var newDelay = options.getDuration("delay", Duration.ZERO).toMillis();
                    if (_log.isDebugEnabled() && newDelay > 0) {
                        _log.debug("Force delay increase by {} for Destination: {}", Format.formatDuration(newDelay),
                                aliasName);
                    } else {
                        newDelay = 0;
                    }
                    if (options.get("recursive", false)) {
                        // We continue to parse recursively!
                        parse(destinations, rules, alias, newVia, newTarget, newLifeTime, delay + newDelay, newPriority,
                                newAsap, newEvent);
                    } else {
                        // We stop there!
                        _log.debug("Adding rule for {}", aliasName);
                        destinations.put(aliasName, alias);
                        rules.put(aliasName, new AliasOptions(newVia, newTarget, newLifeTime, delay + newDelay,
                                newPriority, newAsap, newEvent));
                    }
                }
            }
        }
    }
}
