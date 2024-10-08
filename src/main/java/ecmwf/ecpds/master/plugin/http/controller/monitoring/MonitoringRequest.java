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

package ecmwf.ecpds.master.plugin.http.controller.monitoring;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Bean that provides an access point for monitoring data during a particular
 * request.
 *
 * syi: added support for synchronizing contact list with ActiveDirectory.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import static ecmwf.common.text.Util.isEmpty;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.timer.Timer;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.dao.monitoring.DestinationProductStatusResetterTask;
import ecmwf.ecpds.master.plugin.http.home.monitoring.DestinationProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.DestinationStatusHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationProductStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStatus;
import ecmwf.ecpds.master.plugin.http.model.transfer.ArrivalMonitoringParameters;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMonitoringParameters;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.web.ECMWFException;
import ecmwf.web.util.bean.StringPair;

/**
 * The Class MonitoringRequest.
 */
public class MonitoringRequest {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(MonitoringRequest.class);

    /** The Constant DESTINATIONS_PER_PAGE_OR_COLUMN. */
    protected static final int DESTINATIONS_PER_PAGE_OR_COLUMN = Cnf.at("MonitorPlugin", "destinationsPerPageOrColumn",
            46);

    /** The Constant PRODUCTS_TO_SHOW_COUNT. */
    private static final int PRODUCTS_TO_SHOW_COUNT = Cnf.at("MonitorPlugin", "productsToShowCount", 10);

    /** The Constant PRODUCTS_TO_SHOW_HEADER_COUNT. */
    private static final int PRODUCTS_TO_SHOW_HEADER_COUNT = Cnf.at("MonitorPlugin", "productsToShowHeaderCount", 45);

    /** The Constant PRODUCTS_TO_SHOW_PERIOD. */
    private static final int PRODUCTS_TO_SHOW_PERIOD = Cnf.at("MonitorPlugin", "productsToShowPeriod", 168);

    /** The Constant PRODUCTS_TO_SHOW_MONITORED_ONLY. */
    private static final boolean PRODUCTS_TO_SHOW_MONITORED_ONLY = Cnf.at("MonitorPlugin",
            "productsToShowMonitoredOnly", false);

    /** The Constant CONTACTS_FILE_NAME. */
    private static final String CONTACTS_FILE_NAME = Cnf.at("MonitorPlugin", "contactsFileName",
            Cnf.at("MonitorPlugin", "htdocs") + "/resources/maps/contacts.txt");

    /** The last contact list update. */
    private static long lastContactListUpdate = System.currentTimeMillis();

    /** The destination statuses. */
    private final Map<String, DestinationStatus> destinationStatuses;

    /** The destination product statuses. */
    private final Map<String, DestinationProductStatus> destinationProductStatuses;

    /** The product statuses. */
    private final Map<String, ProductStatus> productStatuses;

    /** The product window. */
    private final List<ProductStatus> productWindow;

    /** The product window header. */
    private final List<ProductStatus> productWindowHeader;

    /** The Constant contacts. */
    private static final Map<String, ContactList> contacts = new ConcurrentHashMap<>();

    /** The destinations. */
    private final List<Destination> destinations;

    /** The status. */
    private final Status status;

    /** The all types. */
    private List<StringPair> allTypes = null;

    /** The all networks. */
    private List<StringPair> allNetworks = null;

    /** The all status. */
    private List<StringPair> allStatus = null;

    /** The filter networks. */
    private List<String> filterNetworks = null;

    /** The filter types. */
    private List<String> filterTypes = null;

    /** The filter status. */
    private List<String> filterStatus = null;

    /** The page. */
    private String page = "";

    /** The application. */
    private String application = "";

    /** The filtered. */
    private String filtered = null;

    /** The filtered destinations size. */
    private int filteredDestinationsSize = 0;

    // Reading the list of email per Product from the configuration file
    static {
        final var contactsFile = new File(CONTACTS_FILE_NAME);
        if (contactsFile.exists() && contactsFile.canRead()) {
            try (final var br = new BufferedReader(new FileReader(contactsFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    final var tokenizer = new StringTokenizer(line, ",");
                    if (tokenizer.countTokens() > 2) {
                        final var product = tokenizer.nextToken();
                        final var lastupdate = Long.parseLong(tokenizer.nextToken());
                        final var emailList = tokenizer.nextToken("\n").substring(1);
                        final var contactList = new ContactList(emailList, lastupdate);
                        if (log.isDebugEnabled()) {
                            log.debug("Importing contacts for {}: {}", product, contactList.displayEmailList());
                        }
                        contacts.put(product, contactList);
                    }
                }
            } catch (final Throwable t) {
                log.warn("Importing contacts", t);
            }
        } else {
            log.warn("No contacts file available: {}", contactsFile.getAbsolutePath());
        }
    }

    /**
     * Refresh contacts list. Compute the contacts list and dump it to the configuration file!
     *
     * @param contactsPerDestinations
     *            the contacts per destinations
     */
    public static void refreshContactsList(final Map<String, String> contactsPerDestinations) {
        try {
            log.debug("Starting refresh");
            // Update the contacts list with the latest information!
            for (final DestinationProductStatus dps : DestinationProductStatusHome.findFromMemory().values()) {
                if (dps != null && dps.isPresent()) {
                    final var product = dps.getProduct() + "@" + dps.getTime();
                    final var found = contactsPerDestinations.get(dps.getDestinationName());
                    final HashMap<String, Long> emailList;
                    if (isNotEmpty(found)) {
                        synchronized (contacts) {
                            final var contactList = contacts.get(product);
                            if (contactList == null) {
                                // This list does not exists yet!
                                emailList = new HashMap<>();
                            } else {
                                emailList = contactList.emailList;
                            }
                            for (final String email : found.split(",")) {
                                emailList.put(email, System.currentTimeMillis());
                            }
                            if (contactList == null) {
                                // The entry does not exists so we create it!
                                contacts.put(product, new ContactList(emailList));
                            }
                        }
                        log.debug("Contacts for {}: {}", product, emailList);
                    }
                }
            }
            // Do we write it to the disk and synchronize it with ActiveDirectory?
            if (System.currentTimeMillis() - lastContactListUpdate > 5 * Timer.ONE_MINUTE) {
                var successful = true;
                log.debug("Exporting contact list to: {}", CONTACTS_FILE_NAME);
                try (final var pw = new PrintWriter(new FileWriter(new File(CONTACTS_FILE_NAME)))) {
                    synchronized (contacts) {
                        for (final String product : contacts.keySet()) {
                            final var contactList = contacts.get(product);
                            pw.println(product + "," + contactList.lastupdate + "," + contactList.displayEmailList());
                            pw.flush();
                        }
                    }
                } catch (final Throwable t) {
                    log.warn("Exporting contacts", t);
                    successful = false;
                }
                if (successful) {
                    log.debug("Synchronizing contact list with ActiveDirectory");
                    try {
                        ActiveDirectory.synchronize(CONTACTS_FILE_NAME);
                    } catch (final Throwable t) {
                        log.warn("Synchronizing contacts", t);
                    }
                }
                lastContactListUpdate = System.currentTimeMillis();
            }
        } catch (final Throwable t) {
            log.warn("Setting contacts list", t);
        }
        log.debug("{} contact(s) registered", contacts.size());
    }

    /**
     * Instantiates a new monitoring request.
     *
     * @param req
     *            the req
     * @param ses
     *            the ses
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     * @throws ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException
     *             the monitoring exception
     */
    public MonitoringRequest(final HttpServletRequest req, final MonitoringSessionActionForm ses)
            throws TransferException, MonitoringException {
        // Get request parameters
        if (req != null) {
            page = req.getParameter("page");
        }
        if (ses != null) {
            filterNetworks = ses.getNetworksList();
            filterTypes = ses.getTypesList();
            filterStatus = ses.getStatusList();
            application = ses.getApplication();
        }
        final var allDestinations = DestinationHome.findAllShowingInMonitor();
        setFilterOptions(allDestinations);
        destinations = new ArrayList<>(applyDestinationFilter(allDestinations));
        Collections.sort(destinations, new DestinationComparator("name", true));
        filteredDestinationsSize = destinations.size();
        if (isNotEmpty(page)) {
            final var pageI = Integer.parseInt(page);
            final var from = (pageI - 1) * DESTINATIONS_PER_PAGE_OR_COLUMN;
            final var to = pageI * DESTINATIONS_PER_PAGE_OR_COLUMN <= destinations.size()
                    ? pageI * DESTINATIONS_PER_PAGE_OR_COLUMN : destinations.size();
            destinations.retainAll(destinations.subList(from, to));
        }
        var acquisitionOnly = true;
        for (final Destination destination : destinations) {
            if (!DestinationOption.isAcquisition(destination.getType())) {
                acquisitionOnly = false;
                break;
            }
        }
        destinationStatuses = DestinationStatusHome.findFromMemory();
        if (!acquisitionOnly) {
            destinationProductStatuses = DestinationProductStatusHome.findFromMemory();
            productStatuses = ProductStatusHome.findFromMemory();
            productWindow = calculateProductWindow(productStatuses, allDestinations, PRODUCTS_TO_SHOW_COUNT);
            productWindowHeader = calculateProductWindow(productStatuses, allDestinations,
                    PRODUCTS_TO_SHOW_HEADER_COUNT);
        } else {
            destinationProductStatuses = new HashMap<>();
            productStatuses = new HashMap<>();
            productWindow = new ArrayList<>();
            productWindowHeader = calculateProductWindow(ProductStatusHome.findFromMemory(), allDestinations,
                    PRODUCTS_TO_SHOW_HEADER_COUNT);
        }
        status = new Status();
    }

    /**
     * The Class ContactList. Allow keeping track of the list of contacts!
     */
    static final class ContactList {

        /** The email list. */
        HashMap<String, Long> emailList;

        /** The lastupdate. */
        long lastupdate;

        /**
         * Instantiates a new contact list.
         *
         * @param emailList
         *            the email list
         */
        // The list is provided as is!
        ContactList(final HashMap<String, Long> emailList) {
            this.lastupdate = System.currentTimeMillis();
            this.emailList = emailList;
        }

        /**
         * Instantiates a new contact list.
         *
         * @param emailList
         *            the email list
         * @param lastupdate
         *            the lastupdate
         */
        // The list is in the format email=update!
        ContactList(final String emailList, final long lastupdate) {
            this.lastupdate = lastupdate;
            this.emailList = new HashMap<>();
            for (final String emailAndUpdate : emailList.split(",")) {
                final var indexOf = emailAndUpdate.indexOf("=");
                if (indexOf != -1) {
                    // We parse the email and last update value!
                    final var update = Long.parseLong(emailAndUpdate.substring(indexOf + 1));
                    final var email = emailAndUpdate.substring(0, indexOf);
                    if (isValid(email, update)) {
                        this.emailList.put(email, update);
                    }
                } else {
                    // We assume there is no last update value!
                    this.emailList.put(emailAndUpdate, System.currentTimeMillis());
                }
            }
        }

        /**
         * Checks if is valid.
         *
         * @param email
         *            the email
         * @param update
         *            the update
         *
         * @return true, if is valid
         */
        static boolean isValid(final String email, final long update) {
            final var result = System.currentTimeMillis() - update < 35 * Timer.ONE_DAY;
            // Entry is older than 35 days?
            if (!result) {
                log.info("Discarding entry: " + email + " (" + Format.formatTime(update) + ")");
            }
            return result;
        }

        /**
         * Display email list.
         *
         * @return the string
         */
        // Retrieve email list and remove old entries!
        public String displayEmailList() {
            final var sb = new StringBuilder();
            for (final String email : emailList.keySet().toArray(new String[0])) {
                final long update = emailList.get(email);
                if (isValid(email, update)) {
                    sb.append(sb.length() > 0 ? "," : "").append(email + "=" + update);
                } else {
                    this.emailList.remove(email);
                }
            }
            return sb.toString();
        }
    }

    /**
     * Simplified constructors that only creates a basic unparametrised "productWindow".
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     * @throws ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException
     *             the monitoring exception
     */
    public MonitoringRequest() throws TransferException, MonitoringException {
        this(null, null);
    }

    /**
     * Gets the destinations.
     *
     * @return the destinations
     */
    public Collection<Destination> getDestinations() {
        return destinations;
    }

    /**
     * Gets the type options.
     *
     * @return the type options
     */
    public Collection<StringPair> getTypeOptions() {
        return allTypes;
    }

    /**
     * Gets the network options.
     *
     * @return the network options
     */
    public Collection<StringPair> getNetworkOptions() {
        return allNetworks;
    }

    /**
     * Gets the status options.
     *
     * @return the status options
     */
    public Collection<StringPair> getStatusOptions() {
        return allStatus;
    }

    /**
     * Gets the product window.
     *
     * @return the product window
     */
    public Collection<ProductStatus> getProductWindow() {
        return productWindow;
    }

    /**
     * Gets the product window header.
     *
     * @return the product window header
     */
    public Collection<ProductStatus> getProductWindowHeader() {
        return productWindowHeader;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the contacts.
     *
     * @return the contacts
     */
    public Map<String, String> getContacts() {
        final Map<String, String> result = new HashMap<>();
        synchronized (contacts) {
            for (final String product : contacts.keySet()) {
                final var contactList = contacts.get(product);
                final var sb = new StringBuilder();
                for (final String contact : contactList.emailList.keySet()) {
                    sb.append(sb.length() > 0 ? "," : "").append(contact);
                }
                result.put(product, sb.toString());
            }
        }
        return result;
    }

    /**
     * Gets the page.
     *
     * @return the page
     */
    public String getPage() {
        return page;
    }

    /**
     * Sets the page.
     *
     * @param page
     *            the new page
     */
    public void setPage(final String page) {
        this.page = page;
    }

    /**
     * Gets the pages.
     *
     * @return the pages
     */
    public Collection<Integer> getPages() {
        final List<Integer> pages = new ArrayList<>();
        for (var i = 0; i <= filteredDestinationsSize / DESTINATIONS_PER_PAGE_OR_COLUMN; i++) {
            pages.add(i + 1);
        }
        return pages;
    }

    /**
     * Gets the filtered.
     *
     * @return the filtered
     */
    public String getFiltered() {
        return filtered;
    }

    /**
     * Apply destination filter.
     *
     * @param c
     *            the c
     *
     * @return the collection
     */
    private Collection<Destination> applyDestinationFilter(final Collection<Destination> c) {
        final var hasStatus = isNotEmpty(filterStatus);
        final var hasTypes = isNotEmpty(filterTypes);
        final var hasNetworks = isNotEmpty(filterNetworks);
        filtered = (hasStatus ? " status:" + filterStatus : "") + (hasTypes ? " types:" + filterTypes : "")
                + (hasNetworks ? " networks:" + filterNetworks : "");
        try {
            if (!hasStatus && !hasTypes && !hasNetworks) {
                // No filter defined!
                return c;
            }
            final List<Destination> filtered = new ArrayList<>();
            for (final Destination d : c) {
                if (hasStatus && filterStatus.contains(getStatusString(d))) {
                    continue;
                }
                final var primary = d.getMonitoringStatus().getPrimaryHost();
                if (hasNetworks && primary != null && filterNetworks.contains(primary.getNetworkCode())) {
                    continue;
                } else if (hasTypes && filterTypes.contains(String.valueOf(d.getType()))) {
                    continue;
                } else {
                    filtered.add(d);
                }
            }
            return filtered;
        } catch (final TransferException e) {
            log.error("Problem filtering destinations with filters (" + filtered.trim() + ")", e);
            return c;
        }
    }

    /**
     * Gets the status string.
     *
     * @param d
     *            the d
     *
     * @return the status string
     */
    private static final String getStatusString(final Destination d) {
        try {
            final var ds = d.getMonitoringStatus();
            if (ds.getBadDataTransfersSize() > 0) {
                log.warn("BadDataTransferSize=" + ds.getBadDataTransfersSize() + " for " + d.getName());
                return "warning";
            }
            final var bigSisterStatus = ds.getBigSisterStatus();
            if (bigSisterStatus == 1 || bigSisterStatus == 2) {
                log.debug("BigSisterStatus=" + ds.getBigSisterStatus() + " for " + d.getName());
                return "warning";
            }
            final var previousCheck = DestinationProductStatusResetterTask.getPreviousCheckDate();
            for (final ProductStatus ps : ProductStatusHome.findFromMemory().values()) {
                final var dps = DestinationProductStatusHome.find(d.getName(), ps.getProduct(), ps.getTime());
                if (dps != null && (dps
                        .getRealTimeArrivalStatus() > ArrivalMonitoringParameters.ARRIVAL_STATUS_OPERATORS_OK
                        || dps.getRealTimeTransferStatus() > TransferMonitoringParameters.TRANSFER_STATUS_OPERATORS_OK)) {
                    // If we received a DONE after the last time we run then we
                    // force it to Green. Otherwise we send a Warning!
                    if (dps.getWhenReceivedLastDone() != null && previousCheck.after(dps.getWhenReceivedLastDone())) {
                        log.warn("RealTimeArrivalStatus=" + dps.getRealTimeArrivalStatus() + " for " + d.getName()
                                + " (" + dps.getProduct() + "," + dps.getTime() + ")");
                        log.warn("RealTimeTransferStatus=" + dps.getRealTimeTransferStatus() + " for " + d.getName()
                                + " (" + dps.getProduct() + "," + dps.getTime() + ")");
                        return "warning";
                    }
                }
            }
            return "ok";
        } catch (final ECMWFException e) {
            log.error("Problem calculating warnings for destination " + d, e);
            return "ok";
        }
    }

    /**
     * Get the list of products to show. Conditions: a) Received by at least ONE currently monitored destination b)
     * Maximum number will be PRODUCTS_TO_SHOW c) If there are more than PRODUCTS_TO SHOW only take those that are
     * scheduled up to 4 hours later than now.
     *
     * @param productStatuses
     *            the product statuses
     * @param destinations
     *            The list of monitored destinations
     * @param productsToShowCount
     *            the products to show count
     *
     * @return the list
     */
    private List<ProductStatus> calculateProductWindow(final Map<String, ProductStatus> productStatuses,
            final Collection<Destination> destinations, final int productsToShowCount) {
        final List<ProductStatus> sortedProductStatus = new ArrayList<>(productStatuses.values());
        Collections.sort(sortedProductStatus, new ProductStatusComparator());
        final List<ProductStatus> window = new ArrayList<>();
        final var c = Calendar.getInstance();
        c.add(Calendar.HOUR, PRODUCTS_TO_SHOW_PERIOD);
        final var limitRight = c.getTime();
        var done = false;
        log.debug("Adding products scheduled till: " + Format.formatTime(limitRight.getTime()));
        final var i = sortedProductStatus.iterator();
        while (!done && i.hasNext()) {
            final var ps = i.next();
            final var scheduledTime = ps.getScheduledTime();
            final var name = ps.getTime() + "-" + ps.getProduct();
            if (scheduledTime == null) {
                log.debug("Discarding product: " + name + " (no schedule time)");
                continue;
            }
            if (scheduledTime.after(limitRight) && window.size() >= productsToShowCount) {
                done = true;
                log.debug("Finished adding products. Discarding: " + name + ", Sched: " + scheduledTime
                        + ", Window size is already " + window.size());
            } else if (!PRODUCTS_TO_SHOW_MONITORED_ONLY || isProductSentToAnyOfTheseDestinations(ps, destinations)) {
                if (isEmpty(application) || application.length() > 0 && (name.endsWith("-" + application)
                        || application.startsWith("no-") && !name.endsWith("-" + application.substring(3)))) {
                    log.debug("Adding product: " + name);
                    window.add(ps);
                } else {
                    log.debug("Discarding product: " + name + " (application not " + application + ")");
                }
            } else {
                log.debug("Discarding product: " + name + " (destinations not monitored)");
            }
        }
        if (window.size() > productsToShowCount) {
            log.debug("Product window (sublist): " + window);
            return window.subList(window.size() - productsToShowCount, window.size());
        }
        log.debug("Product window: " + window);
        return window;
    }

    /**
     * Gets the nearest to schedule index.
     *
     * @param productStatuses
     *            the product statuses
     *
     * @return The position within the where the product to sent next is.
     */
    public static int getNearestToScheduleIndex(final Collection<ProductStatus> productStatuses) {
        var index = 0;
        final var current = new Date();
        for (final ProductStatus s : productStatuses) {
            final var scheduledTime = s.getScheduledTime();
            if (scheduledTime != null && scheduledTime.after(current)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Gets the updated.
     *
     * @return the updated
     */
    public Date getUpdated() {
        return new Date();
    }

    /**
     * Gets the steps per column.
     *
     * @return the steps per column
     */
    public int getStepsPerColumn() {
        return DESTINATIONS_PER_PAGE_OR_COLUMN;
    }

    /**
     * Checks if is product sent to any of these destinations.
     *
     * @param ps
     *            A ProductStatus
     * @param destinations
     *            A Collection of destinations
     *
     * @return If the product is received by any of the destinations or true is no destinations is null
     */
    private boolean isProductSentToAnyOfTheseDestinations(final ProductStatus ps,
            final Collection<Destination> destinations) {
        if (destinations == null) {
            return true;
        }
        for (final Destination d : destinations) {
            final var key = d.getName() + "@" + ps.getProduct() + "@" + ps.getTime();
            final var dps = destinationProductStatuses.get(key);
            if (dps != null && dps.isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the filter options.
     *
     * @param c
     *            the new filter options
     */
    private void setFilterOptions(final Collection<Destination> c) {
        if (isEmpty(c)) {
            log.warn("No Destinations found in monitoring. Filters left uninitialized");
        } else {
            Destination first = null;
            try {
                first = c.iterator().next();
                final var hostForSource = first.getHostForSource();
                if (hostForSource != null) {
                    allNetworks = first.getHostForSource().getAllNetworks();
                    allTypes = first.getAllTypes();
                    allStatus = new ArrayList<>();
                    allStatus.add(new StringPair("ok", "Ok"));
                    allStatus.add(new StringPair("warning", "Warning"));
                }
            } catch (final TransferException e) {
                log.warn("Problem initializing filters from destination " + first, e);
            }
        }
    }

    /**
     * The Class Status.
     */
    private class Status implements Map<String, Object> {

        /**
         * Gets the.
         *
         * @param key
         *            the key
         *
         * @return the object
         */
        @Override
        public Object get(final Object key) {
            try {
                final var bits = key.toString().split("@");
                Object o = null;
                if (bits.length == 1) {
                    o = destinationStatuses.get(key);
                } else if (bits.length == 3) {
                    o = destinationProductStatuses.get(key);
                }
                return o;
            } catch (final Exception e) {
                log.error("Problem retrieving key " + key, e);
                return null;
            }
        }

        /**
         * Clear.
         */
        // The rest of the Map interface.
        @Override
        public void clear() {
        }

        /**
         * Contains key.
         *
         * @param key
         *            the key
         *
         * @return true, if successful
         */
        @Override
        public boolean containsKey(final Object key) {
            return false;
        }

        /**
         * Contains value.
         *
         * @param value
         *            the value
         *
         * @return true, if successful
         */
        @Override
        public boolean containsValue(final Object value) {
            return false;
        }

        /**
         * Entry set.
         *
         * @return the sets the
         */
        @Override
        public Set<Entry<String, Object>> entrySet() {
            return null;
        }

        /**
         * Checks if is empty.
         *
         * @return true, if is empty
         */
        @Override
        public boolean isEmpty() {
            return false;
        }

        /**
         * Key set.
         *
         * @return the sets the
         */
        @Override
        public Set<String> keySet() {
            return null;
        }

        /**
         * Put.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         *
         * @return the object
         */
        @Override
        public Object put(final String key, final Object value) {
            return null;
        }

        /**
         * Put all.
         *
         * @param m
         *            the m
         */
        @Override
        public void putAll(final Map<? extends String, ? extends Object> m) {
        }

        /**
         * Removes the.
         *
         * @param key
         *            the key
         *
         * @return the object
         */
        @Override
        public Object remove(final Object key) {
            return null;
        }

        /**
         * Size.
         *
         * @return the int
         */
        @Override
        public int size() {
            return 0;
        }

        /**
         * Values.
         *
         * @return the collection
         */
        @Override
        public Collection<Object> values() {
            return null;
        }
    }
}
