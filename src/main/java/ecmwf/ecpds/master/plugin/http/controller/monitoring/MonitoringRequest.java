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
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

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
import ecmwf.ecpds.master.transfer.HostOption;
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
    private static final int PRODUCTS_TO_SHOW_HEADER_COUNT = Cnf.at("MonitorPlugin", "productsToShowHeaderCount", 80);

    /** The Constant PRODUCTS_TO_SHOW_PERIOD. */
    private static final int PRODUCTS_TO_SHOW_PERIOD = Cnf.at("MonitorPlugin", "productsToShowPeriod", 168);

    /** The Constant PRODUCTS_TO_SHOW_MONITORED_ONLY. */
    private static final boolean PRODUCTS_TO_SHOW_MONITORED_ONLY = Cnf.at("MonitorPlugin",
            "productsToShowMonitoredOnly", false);

    /** The Constant CONTACTS_FILE_NAME. */
    private static final String CONTACTS_FILE_NAME = Cnf.at("MonitorPlugin", "contactsFileName");

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

    /**
     * Distinct, sorted list of all known product names, regardless of the current product-name filter (used to populate
     * the "Products" picker panel so hidden products can still be re-enabled from it).
     */
    private final List<String> allProductNames;

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
        if (CONTACTS_FILE_NAME == null) {
            log.warn("No contacts file available");
        } else {
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
            if (CONTACTS_FILE_NAME != null
                    && System.currentTimeMillis() - lastContactListUpdate > 5 * Timer.ONE_MINUTE) {
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
            productWindowHeader = calculateProductWindow(mergeGroupedProducts(productStatuses), allDestinations,
                    PRODUCTS_TO_SHOW_HEADER_COUNT);
            allProductNames = calculateAllProductNames(productStatuses, allDestinations);
        } else {
            destinationProductStatuses = new HashMap<>();
            productStatuses = new HashMap<>();
            productWindow = new ArrayList<>();
            final var allProducts = ProductStatusHome.findFromMemory();
            productWindowHeader = calculateProductWindow(mergeGroupedProducts(allProducts), allDestinations,
                    PRODUCTS_TO_SHOW_HEADER_COUNT);
            allProductNames = calculateAllProductNames(allProducts, allDestinations);
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
     * Gets the distinct, sorted list of all known product names, regardless of the current product-name filter. Used to
     * populate the "Products" picker panel, so that products currently hidden by the filter can still be listed (and
     * re-enabled) from it.
     *
     * @return the all product names
     */
    public List<String> getAllProductNames() {
        return allProductNames;
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
     * Computes the distinct, sorted list of all known product names (ignoring the product-name filter), so that the
     * "Products" picker panel in the UI can list every product, including those currently hidden by the filter. Still
     * respects the "monitored destinations only" restriction, consistent with {@link #calculateProductWindow}.
     *
     * @param productStatuses
     *            the product statuses
     * @param destinations
     *            the list of monitored destinations
     *
     * @return the sorted list of distinct product names
     */
    private List<String> calculateAllProductNames(final Map<String, ProductStatus> productStatuses,
            final Collection<Destination> destinations) {
        final Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (final ProductStatus ps : productStatuses.values()) {
            if (!PRODUCTS_TO_SHOW_MONITORED_ONLY || isProductSentToAnyOfTheseDestinations(ps, destinations)) {
                names.add(ps.getProduct());
            }
        }
        return new ArrayList<>(names);
    }

    /**
     * For products configured (via the generic entry in Product Descriptions) to have all their cycles/times grouped
     * into a single monitoring page instead of one page per cycle/time, collapses all of that product's entries in the
     * given map into a single synthetic entry (see {@link #mergeProductStatuses}). The synthetic entry's "time" is left
     * empty, which is how the pill/monitoring page JSPs recognise a merged, "all cycles" entry and link to
     * {@code /do/monitoring/summary/PRODUCT} instead of {@code /do/monitoring/summary/PRODUCT/TIME}. Products not
     * configured to be grouped are left untouched. If the list of grouped products cannot be loaded (e.g. database
     * unavailable), the map is returned unchanged.
     *
     * @param source
     *            the product statuses, keyed by "product@time"
     *
     * @return a new map: ungrouped entries unchanged, plus one merged entry per grouped product
     */
    private Map<String, ProductStatus> mergeGroupedProducts(final Map<String, ProductStatus> source) {
        final var groupedProducts = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        try {
            for (final var m : ecmwf.ecpds.master.MasterManager.getDB().getProductMetadata()) {
                if (m.isGeneric() && m.isGroupTimes()) {
                    groupedProducts.add(m.getProduct());
                }
            }
        } catch (final Exception e) {
            log.warn("mergeGroupedProducts: failed to load grouped product names", e);
        }
        if (groupedProducts.isEmpty()) {
            return source;
        }
        final Map<String, ProductStatus> result = new HashMap<>();
        final Map<String, List<ProductStatus>> toMerge = new HashMap<>();
        for (final var ps : source.values()) {
            if (groupedProducts.contains(ps.getProduct())) {
                toMerge.computeIfAbsent(ps.getProduct(), k -> new ArrayList<>()).add(ps);
            } else {
                result.put(ps.getProduct() + "@" + ps.getTime(), ps);
            }
        }
        for (final var e : toMerge.entrySet()) {
            result.put(e.getKey() + "@", mergeProductStatuses(e.getKey(), e.getValue()));
        }
        return result;
    }

    /**
     * Merges several cycles/times of the same product into a single synthetic {@link ProductStatus}: the worst (highest
     * severity) status among its non-"none" cycles is used for the color/label (falling back to "none" only if every
     * cycle is "none"), the earliest scheduledTime is kept (so the merged pill sorts alongside its most urgent cycle,
     * consistent with {@link ProductStatusComparator}), and the most recent lastUpdate/productTime are kept. The number
     * of cycles being merged is stashed in the (otherwise unused, for this kind of entry) "buffer" field, so JSPs can
     * show it (e.g. {@code ${pro.buffer}}) without needing a new interface method.
     *
     * @param product
     *            the product name
     * @param cycles
     *            the individual per-time entries for this product (non-empty)
     *
     * @return the merged, synthetic entry, with an empty "time"
     */
    public static ProductStatus mergeProductStatuses(final String product, final List<ProductStatus> cycles) {
        final var merged = new ecmwf.ecpds.master.plugin.http.dao.monitoring.ProductStatusBean(product, "", 0, true);
        merged.setPresent(true);
        merged.setBuffer(cycles.size());
        ProductStatus worst = null;
        Date earliestScheduled = null;
        Date mostRecentUpdate = null;
        Date mostRecentProductTime = null;
        for (final var ps : cycles) {
            if (ps.getGenerationStatus() != ecmwf.ecpds.master.plugin.http.model.monitoring.GenerationMonitoringStatus.STATUS_NONE
                    && (worst == null || ps.getGenerationStatus() > worst.getGenerationStatus())) {
                worst = ps;
            }
            final var sched = ps.getScheduledTime();
            if (sched != null && (earliestScheduled == null || sched.before(earliestScheduled))) {
                earliestScheduled = sched;
            }
            final var upd = ps.getLastUpdate();
            if (upd != null && (mostRecentUpdate == null || upd.after(mostRecentUpdate))) {
                mostRecentUpdate = upd;
            }
            final var pt = ps.getProductTime();
            if (pt != null && (mostRecentProductTime == null || pt.after(mostRecentProductTime))) {
                mostRecentProductTime = pt;
            }
        }
        if (worst == null) {
            // Every cycle is STATUS_NONE.
            worst = cycles.get(0);
        }
        merged.setGenerationStatusCode(worst.getGenerationStatusCode());
        merged.setGenerationStatus(worst.getGenerationStatus());
        merged.setScheduledTime(earliestScheduled);
        merged.setLastUpdate(mostRecentUpdate);
        merged.setProductTime(mostRecentProductTime);
        return merged;
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
        // Grouped/merged "all cycles" entries (synthetic, time is empty) are never subject to the schedule-based
        // cutoff/trimming below: since they aggregate every cycle of a product, their (earliest) scheduledTime can
        // look much "older" than the individual entries of other, more frequent products, and would otherwise get
        // silently trimmed out of the visible window despite representing perfectly current data. They are always
        // kept (still subject to the monitored-destinations/application-name filters), and merged back in, in their
        // correct chronological position, at the end.
        final List<ProductStatus> pinned = new ArrayList<>();
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
            final var isGroupedEntry = isEmpty(ps.getTime());
            if (scheduledTime == null && !isGroupedEntry) {
                log.debug("Discarding product: " + name + " (no schedule time)");
                continue;
            }
            if (!isGroupedEntry && scheduledTime.after(limitRight) && window.size() >= productsToShowCount) {
                done = true;
                log.debug("Finished adding products. Discarding: " + name + ", Sched: " + scheduledTime
                        + ", Window size is already " + window.size());
            } else if (!PRODUCTS_TO_SHOW_MONITORED_ONLY || isProductSentToAnyOfTheseDestinations(ps, destinations)) {
                if (matchesApplicationFilter(application, name)) {
                    if (isGroupedEntry) {
                        log.debug("Pinning grouped product: " + name);
                        pinned.add(ps);
                    } else {
                        log.debug("Adding product: " + name);
                        window.add(ps);
                    }
                } else {
                    log.debug("Discarding product: " + name + " (application not " + application + ")");
                }
            } else {
                log.debug("Discarding product: " + name + " (destinations not monitored)");
            }
        }
        List<ProductStatus> result = window;
        if (window.size() > productsToShowCount) {
            final int realSize = window.size() - productsToShowCount;
            log.debug("Product window (sublist {} -> {}): {}", window.size(), realSize, window);
            result = window.subList(realSize, window.size());
        } else {
            log.debug("Product window: " + window);
        }
        if (!pinned.isEmpty()) {
            result = new ArrayList<>(result);
            result.addAll(pinned);
            Collections.sort(result, new ProductStatusComparator());
        }
        return result;
    }

    /**
     * Tests whether a product ({@code time-product}, e.g. {@code 06-GENFO}) should be shown, given the current
     * product-name filter. The filter is a comma-separated list of tokens, each of which may:
     * <ul>
     * <li>contain the {@code *}/{@code ?} wildcards (e.g. {@code *ERA5T*})</li>
     * <li>be an exact product name, matched against the trailing {@code -name} suffix (e.g. {@code ERA5T} matches
     * {@code 06-ERA5T})</li>
     * <li>be prefixed with {@code !} or {@code -} (or, for backward compatibility, with {@code no-}) to exclude rather
     * than include matching products</li>
     * </ul>
     * A product is shown when: it matches at least one include token (or there are no include tokens at all, i.e. only
     * exclude tokens or no filter), AND it does not match any exclude token. This allows combining an include pattern
     * with one or more exclusions, e.g. {@code *ERA5T*, !*TEST*}.
     *
     * @param application
     *            the product-name filter (comma-separated tokens, see above)
     * @param name
     *            the {@code time-product} name to test
     *
     * @return true if the product should be shown
     */
    private static boolean matchesApplicationFilter(final String application, final String name) {
        if (isEmpty(application)) {
            return true;
        }
        var hasIncludeToken = false;
        var matchedInclude = false;
        for (final var rawToken : application.split(",")) {
            var token = rawToken.trim();
            if (token.isEmpty()) {
                continue;
            }
            final boolean exclude;
            if (token.startsWith("!") || token.startsWith("-")) {
                exclude = true;
                token = token.substring(1).trim();
            } else if (token.toLowerCase().startsWith("no-")) {
                exclude = true;
                token = token.substring(3).trim();
            } else {
                exclude = false;
            }
            if (token.isEmpty()) {
                continue;
            }
            final var hasWildcard = token.contains("*") || token.contains("?");
            final var matches = hasWildcard ? matchesWildcard(token, name) : name.endsWith("-" + token);
            if (exclude) {
                if (matches) {
                    return false;
                }
            } else {
                hasIncludeToken = true;
                matchedInclude = matchedInclude || matches;
            }
        }
        return !hasIncludeToken || matchedInclude;
    }

    /**
     * Matches a product name against a wildcard pattern where {@code *} matches any sequence of characters and
     * {@code ?} matches exactly one character. Matching is case-insensitive.
     *
     * @param pattern
     *            the wildcard pattern
     * @param text
     *            the product name to test
     *
     * @return true if the text matches the pattern
     */
    private static boolean matchesWildcard(final String pattern, final String text) {
        final var sb = new StringBuilder("(?i)");
        for (int i = 0; i < pattern.length(); i++) {
            final char c = pattern.charAt(i);
            if (c == '*') {
                sb.append(".*");
            } else if (c == '?') {
                sb.append('.');
            } else {
                sb.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return text.matches(sb.toString());
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
        allStatus = new ArrayList<>();
        allStatus.add(new StringPair("ok", "Ok"));
        allStatus.add(new StringPair("warning", "Warning"));
        allNetworks = new ArrayList<>();
        for (var i = 0; i < HostOption.networkCode.length; i++) {
            allNetworks.add(new StringPair(HostOption.networkCode[i],
                    HostOption.networkName.length > i ? HostOption.networkName[i] : HostOption.networkCode[i]));
        }
        if (isEmpty(c)) {
            log.warn("No Destinations found in monitoring. Filters left uninitialized");
        } else {
            Destination first = null;
            try {
                first = c.iterator().next();
                allTypes = first.getAllTypes();
            } catch (final Exception e) {
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
