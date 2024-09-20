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

package ecmwf.ecpds.master.plugin.http.controller.transfer.destination;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.dao.transfer.DataTransferLightBean;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.MetaData;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.web.util.bean.Pair;
import ecmwf.web.util.bean.Treble;

/**
 * The Class DetailActionDestinationCache.
 */
public class DetailActionDestinationCache {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DetailActionDestinationCache.class);

    /** The Constant ISO_FORMAT. */
    private static final String ISO_FORMAT = "yyyy-MM-dd";

    /** The Constant ALL. */
    protected static final String ALL = "All";

    /** The Constant NONE. */
    protected static final String NONE = "None";

    /** The daf. */
    private DetailActionForm daf = null;

    /** The destination. */
    private String destination = null;

    /** The dissemination stream. */
    private String disseminationStream = ALL;

    /** The status. */
    private String status = ALL;

    /** The data stream. */
    private String dataStream = ALL;

    /** The data time. */
    private String dataTime = ALL;

    /** The date. */
    private String date = new SimpleDateFormat(ISO_FORMAT).format(new Date());

    /** The new priority. */
    private String newPriority = "";

    /** The file name search. */
    private String fileNameSearch = "";

    /** The display tags params. */
    private Collection<Pair> displayTagsParams = null;

    /** The selected transfers. */
    private final HashMap<String, String> selectedTransfers = new HashMap<>(); // Transfers
                                                                               // first
                                                                               // selected
                                                                               // for
                                                                               // a
                                                                               // "mass
                                                                               // operation"
    /** The action transfers. */
    private final HashMap<String, String> actionTransfers = new HashMap<>(); // Transfers
    // that
    // "actually"
    // are
    // going
    // to be
    // used
    // for
    // the
    // operation.

    /**
     * Instantiates a new detail action destination cache.
     *
     * @param daf
     *            the daf
     */
    public DetailActionDestinationCache(final DetailActionForm daf) {
        this.daf = daf;
    }

    /**
     * Instantiates a new detail action destination cache.
     *
     * @param daf
     *            the daf
     * @param destination
     *            the destination
     */
    public DetailActionDestinationCache(final DetailActionForm daf, final String destination) {
        this.daf = daf;
        this.destination = destination;
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     */
    protected String getDestination() {
        return destination != null ? destination : daf.getId();
    }

    /**
     * Gets the action transfers.
     *
     * @return the action transfers
     */
    public Collection<DataTransfer> getActionTransfers() {
        final List<DataTransfer> l = new ArrayList<>(actionTransfers.size());
        for (final String key : actionTransfers.keySet()) {
            final var value = String.valueOf(actionTransfers.get(key));
            if ("on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
                try {
                    l.add(DataTransferHome.findByPrimaryKey(key));
                } catch (final TransferException e) {
                    log.warn("Problem adding Transfer with key '" + key + "' to list of action transfers", e);
                }
            }
        }
        return l;
    }

    /**
     * Gets the action transfer.
     *
     * @param name
     *            the name
     *
     * @return the action transfer
     */
    public String getActionTransfer(final String name) {
        final var action = actionTransfers.get(name);
        return action != null ? action : "off";
    }

    /**
     * The Class DataOptionsWithSizes.
     */
    protected class DataOptionsWithSizes {

        /** The data stream options with sizes. */
        final Collection<NameCountAndSizes> dataStreamOptionsWithSizes;

        /** The data time options with sizes. */
        final Collection<NameCountAndSizes> dataTimeOptionsWithSizes;

        /**
         * Instantiates a new data options with sizes.
         *
         * @param dataStreamOptionsWithSizes
         *            the data stream options with sizes
         * @param dataTimeOptionsWithSizes
         *            the data time options with sizes
         */
        DataOptionsWithSizes(final Collection<NameCountAndSizes> dataStreamOptionsWithSizes,
                final Collection<NameCountAndSizes> dataTimeOptionsWithSizes) {
            this.dataStreamOptionsWithSizes = dataStreamOptionsWithSizes;
            this.dataTimeOptionsWithSizes = dataTimeOptionsWithSizes;
        }
    }

    /**
     * Gets the data options with sizes.
     *
     * @return the data options with sizes
     */
    protected DataOptionsWithSizes getDataOptionsWithSizes() {
        final var c = getOptions(MetaData.DATA_STREAM_TAG, status);
        final List<NameCountAndSizes> dataStreamOptionsWithSizes = new ArrayList<>(c.size());
        final var dataTimeOptionsWithSizes = new HashMap<String, NameCountAndSizes>();
        for (final Pair p : c) {
            final var name = p.getName();
            final var value = p.getValue() != null ? p.getValue() : "";
            Object count = null;
            Object size = null;
            final var t = new StringTokenizer(value.toString(), ";");
            if (t.countTokens() == 2) {
                count = t.nextToken();
                size = t.nextToken();
            } else {
                count = value;
                size = "";
            }
            final var newValue = new NameCountAndSizes(name, name, count, name, count, size);
            dataStreamOptionsWithSizes.add(newValue);
            // Do we have a proper time defined (e.g. 00-GOPER)?
            final var currentName = name.toString();
            final var index = currentName.indexOf("-");
            if (index != -1) {
                _fillTable(dataTimeOptionsWithSizes, currentName.substring(0, index), count, size);
            } else // We can't parse it so it might be the 'All' entry?
            if (ALL.equals(currentName)) {
                _fillTable(dataTimeOptionsWithSizes, ALL, count, size);
            } else {
                // Don't know what it is so we put it in a None entry!
                _fillTable(dataTimeOptionsWithSizes, NONE, count, size);
            }
        }
        return new DataOptionsWithSizes(dataStreamOptionsWithSizes, dataTimeOptionsWithSizes.values());
    }

    /**
     * Fill table.
     *
     * @param dataTimeOptionsWithSizes
     *            the data time options with sizes
     * @param name
     *            the name
     * @param count
     *            the count
     * @param size
     *            the size
     */
    public static void _fillTable(final HashMap<String, NameCountAndSizes> dataTimeOptionsWithSizes, final String name,
            final Object count, final Object size) {
        try {
            var currentCount = Long.parseLong(count.toString());
            var currentSize = Long.parseLong(size.toString());
            final var entry = dataTimeOptionsWithSizes.get(name);
            if (entry != null) {
                // The entry already exists so we have to add-up the count and size with the
                // existing one!
                currentCount += Long.parseLong(entry.getValue().toString());
                currentSize += Long.parseLong(entry.getSize().toString());
            }
            dataTimeOptionsWithSizes.put(name,
                    new NameCountAndSizes(name, name, currentCount, name, currentCount, currentSize));
        } catch (final NumberFormatException e) {
            // Let's ignore this field!
            log.debug("Ignore data stream " + name + " (count=" + count + ",size=" + size + ")", e);
        }
    }

    /**
     * Gets the display tags params.
     *
     * @return the display tags params
     */
    public Collection<Pair> getDisplayTagsParams() {
        return displayTagsParams;
    }

    /**
     * Sets the display tags params.
     *
     * @param collection
     *            the new display tags params
     */
    public void setDisplayTagsParams(final Collection<Pair> collection) {
        displayTagsParams = collection;
    }

    /**
     * Gets the display tags params collection.
     *
     * @return the display tags params collection
     */
    public Collection<Pair> getDisplayTagsParamsCollection() {
        return displayTagsParams;
    }

    /**
     * Sets the display tags params collection.
     *
     * @param collection
     *            the new display tags params collection
     */
    public void setDisplayTagsParamsCollection(final Collection<Pair> collection) {
        displayTagsParams = collection;
    }

    /**
     * Gets the dissemination stream options with sizes.
     *
     * @return the dissemination stream options with sizes
     */
    protected Collection<NameCountAndSizes> getDisseminationStreamOptionsWithSizes() {
        final var c = getOptions(MetaData.DISSEMINATION_STREAM_TAG, status);
        final List<NameCountAndSizes> disseminationStreamOptionsWithSizes = new ArrayList<>(c.size());
        for (final Pair p : c) {
            final var name = p.getName();
            final var value = p.getValue() != null ? p.getValue() : "";
            Object count = null;
            Object size = null;
            final var t = new StringTokenizer(value.toString(), ";");
            if (t.countTokens() == 2) {
                count = t.nextToken();
                size = t.nextToken();
            } else {
                count = value;
                size = "";
            }
            disseminationStreamOptionsWithSizes.add(new NameCountAndSizes(name, name, count, name, count, size));
        }
        return disseminationStreamOptionsWithSizes;
    }

    /**
     * Gets the status options with sizes.
     *
     * @return the status options with sizes
     */
    protected Collection<NameCountAndSizes> getStatusOptionsWithSizes() {
        final var c = getOptions(MetaData.STATUS_TAG, "All");
        final var isMemberState = daf.isMemberState();
        final List<NameCountAndSizes> statusOptionsWithSizes = new ArrayList<>(c.size());
        for (final Pair p : c) {
            final var name = p.getName();
            final var value = p.getValue() != null ? p.getValue() : "";
            Object count = null;
            Object size = null;
            final var t = new StringTokenizer(value.toString(), ";");
            if (t.countTokens() == 2) {
                count = t.nextToken();
                size = t.nextToken();
            } else {
                count = value;
                size = "";
            }
            statusOptionsWithSizes.add(new NameCountAndSizes(name,
                    StatusFactory.getDataTransferStatusName(!isMemberState, name.toString()), count, name, count,
                    size));
        }
        return statusOptionsWithSizes;
    }

    /**
     * Gets the selected transfer.
     *
     * @param name
     *            the name
     *
     * @return the selected transfer
     */
    public String getSelectedTransfer(final String name) {
        final Object o = selectedTransfers.get(name);
        return o != null ? o.toString() : "off";
    }

    /**
     * Gets the selected transfers count.
     *
     * @return the selected transfers count
     */
    public int getSelectedTransfersCount() {
        var count = 0;
        for (final String key : selectedTransfers.keySet()) {
            final var value = selectedTransfers.get(key);
            if ("on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the selected transfers.
     *
     * @param from
     *            the from
     * @param to
     *            the to
     *
     * @return the selected transfers
     */
    public Collection<DataTransfer> getSelectedTransfers(final int from, final int to) {
        final List<DataTransfer> l = new ArrayList<>(to - from);
        final var treeSet = new TreeSet<String>(
                (s1, s2) -> Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2)));
        treeSet.addAll(selectedTransfers.keySet());
        final var i = treeSet.descendingIterator();
        var count = 0;
        while (count < to && i.hasNext()) {
            final var id = i.next();
            final var value = selectedTransfers.get(id);
            if ("on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
                if (count < from) {
                    count++;
                } else {
                    try {
                        l.add(DataTransferHome.findByPrimaryKey(id));
                        count++;
                    } catch (final TransferException e) {
                        log.warn("Problem getting Transfer with key '" + id + "' to list selected transfers", e);
                    }
                }
            }
        }
        return l;
    }

    /**
     * Gets the data transfer.
     *
     * @param id
     *            the id
     *
     * @return the data transfer
     */
    public DataTransfer getDataTransfer(final String id) {
        try {
            return DataTransferHome.findByPrimaryKey(id);
        } catch (final TransferException e) {
            log.warn("Problem getting Transfer with key '" + id + "' to list selected transfers", e);
            return null;
        }
    }

    /**
     * Delete from selection.
     *
     * @param dt
     *            the dt
     */
    public void deleteFromSelection(final DataTransfer dt) {
        selectedTransfers.remove(dt.getId());
        actionTransfers.remove(dt.getId());
    }

    /**
     * Adds the to selections.
     *
     * @param transfers
     *            the transfers
     */
    public void addToSelections(final Collection<DataTransferLightBean> transfers) {
        for (final DataTransferLightBean dt : transfers) {
            if (dt.getDestinationName().equals(getDestination())) {
                selectedTransfers.put(dt.getId(), "on");
                actionTransfers.put(dt.getId(), "on");
            } else {
                log.warn("When adding to selection for destination '" + getDestination() + "', got transfer '"
                        + dt.getId() + "', which belongs to destination '" + dt.getDestinationName() + "'. Not added.");
            }
        }
    }

    /**
     * Sets the action transfer.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    public void setActionTransfer(final String name, final String value) {
        actionTransfers.put(name, value);
    }

    /**
     * Sets the selected transfer.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    public void setSelectedTransfer(final String name, final String value) {
        selectedTransfers.put(name, value);
        setActionTransfer(name, value);
    }

    /**
     * Gets the data transfers.
     *
     * @param hasAccess
     *            the has access
     * @param cursor
     *            the cursor
     *
     * @return the data transfers
     *
     * @throws TransferException
     *             the transfer exception
     */
    public Collection<DataTransferLightBean> getDataTransfers(final boolean hasAccess, final DataBaseCursor cursor)
            throws TransferException {
        try {
            return DataTransferHome.findByFilter(getDestination(), disseminationStream, dataStream, dataTime, status,
                    hasAccess, fileNameSearch, ALL.equals(date) ? null : new SimpleDateFormat(ISO_FORMAT).parse(date),
                    cursor);
        } catch (final ParseException e) {
            throw new TransferException("Bad ISO Date (yyy-MM-dd)", e);
        }
    }

    /**
     * Gets the data transfers.
     *
     * @param hasAccess
     *            the has access
     *
     * @return the data transfers
     *
     * @throws TransferException
     *             the transfer exception
     */
    public Collection<DataTransferLightBean> getDataTransfers(final boolean hasAccess) throws TransferException {
        try {
            return DataTransferHome.findByFilter(getDestination(), disseminationStream, dataStream, dataTime, status,
                    hasAccess, fileNameSearch, ALL.equals(date) ? null : new SimpleDateFormat(ISO_FORMAT).parse(date));
        } catch (final ParseException e) {
            throw new TransferException("Bad ISO Date (yyy-MM-dd)", e);
        }
    }

    /**
     * Gets the data stream.
     *
     * @return the data stream
     */
    public String getDataStream() {
        return dataStream;
    }

    /**
     * Gets the data time.
     *
     * @return the data time
     */
    public String getDataTime() {
        return dataTime;
    }

    /**
     * Gets the dissemination stream.
     *
     * @return the dissemination stream
     */
    public String getDisseminationStream() {
        return disseminationStream;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the data stream.
     *
     * @param string
     *            the new data stream
     */
    public void setDataStream(final String string) {
        dataStream = string;
    }

    /**
     * Sets the data time.
     *
     * @param string
     *            the new data time
     */
    public void setDataTime(final String string) {
        dataTime = string;
    }

    /**
     * Sets the dissemination stream.
     *
     * @param string
     *            the new dissemination stream
     */
    public void setDisseminationStream(final String string) {
        disseminationStream = string;
    }

    /**
     * Sets the status.
     *
     * @param string
     *            the new status
     */
    public void setStatus(final String string) {
        status = string;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the date.
     *
     * @param string
     *            the new date
     */
    public void setDate(final String string) {
        date = string;
    }

    /**
     * Clean action transfers.
     */
    public void cleanActionTransfers() {
        actionTransfers.clear();
    }

    /**
     * Clean selected transfers.
     */
    public void cleanSelectedTransfers() {
        selectedTransfers.clear();
    }

    /**
     * Gets the file name search.
     *
     * @return the file name search
     */
    public String getFileNameSearch() {
        return fileNameSearch;
    }

    /**
     * Sets the file name search.
     *
     * @param fileNameSearch
     *            the new file name search
     */
    public void setFileNameSearch(final String fileNameSearch) {
        this.fileNameSearch = fileNameSearch;
    }

    /**
     * Gets the new priority.
     *
     * @return the new priority
     */
    public String getNewPriority() {
        return newPriority;
    }

    /**
     * Sets the new priority.
     *
     * @param newPriority
     *            the new new priority
     */
    public void setNewPriority(final String newPriority) {
        this.newPriority = newPriority;
    }

    /**
     * Gets the options.
     *
     * @param what
     *            the what
     * @param status
     *            the status
     *
     * @return the options
     */
    private Collection<Pair> getOptions(final String what, final String status) {
        try {
            if (daf.isMemberState()) {
                if (ALL.equals(date)) {
                    log.debug("getCountByFilterForNonPrivilegedUsers(" + what + "," + getDestination() + ","
                            + disseminationStream + "," + dataStream + "," + dataTime + "," + status + ","
                            + fileNameSearch + ")");
                    return DataTransferHome.getCountByFilterForNonPrivilegedUsers(what, getDestination(),
                            disseminationStream, dataStream, dataTime, status, fileNameSearch);
                } else {
                    log.debug("getCountByFilterForNonPrivilegedUsers(" + what + "," + getDestination() + ","
                            + disseminationStream + "," + dataStream + "," + dataTime + "," + status + ","
                            + fileNameSearch + "," + date + ")");
                    return DataTransferHome.getCountByFilterForNonPrivilegedUsers(what, getDestination(),
                            this.disseminationStream, dataStream, dataTime, status, fileNameSearch,
                            new SimpleDateFormat(ISO_FORMAT).parse(date));
                }
            }
            if (ALL.equals(this.date)) {
                log.debug("getCountByFilter(" + what + "," + getDestination() + "," + disseminationStream + ","
                        + dataStream + "," + dataTime + "," + status + "," + fileNameSearch + ")");
                return DataTransferHome.getCountByFilter(what, getDestination(), disseminationStream, dataStream,
                        dataTime, status, fileNameSearch);
            } else {
                log.debug("getCountByFilter(" + what + "," + getDestination() + "," + disseminationStream + ","
                        + dataStream + "," + dataTime + "," + status + "," + fileNameSearch + "," + date + ")");
                return DataTransferHome.getCountByFilter(what, this.getDestination(), disseminationStream, dataStream,
                        dataTime, status, fileNameSearch, new SimpleDateFormat(ISO_FORMAT).parse(date));
            }
        } catch (final TransferException e) {
            log.error("Error getting transfer count for " + what, e);
        } catch (final ParseException e) {
            log.error("Bad ISO Date (yyy-MM-dd): " + date, e);
        }
        return new ArrayList<>(0);
    }

    /**
     * The Class NameCountAndSizes.
     */
    public static final class NameCountAndSizes extends Treble {

        /** The name. */
        private final Object name;

        /** The value. */
        private final Object value;

        /** The size. */
        private final Object size;

        /**
         * Instantiates a new name count and sizes.
         *
         * @param first
         *            the first
         * @param second
         *            the second
         * @param third
         *            the third
         * @param name
         *            the name
         * @param value
         *            the value
         * @param size
         *            the size
         */
        public NameCountAndSizes(final Object first, final Object second, final Object third, final Object name,
                final Object value, final Object size) {
            super(first, second, third);
            this.name = name;
            this.value = value;
            this.size = size;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public Object getName() {
            return name;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        public Object getValue() {
            return value;
        }

        /**
         * Gets the size.
         *
         * @return the size
         */
        public Object getSize() {
            return size;
        }

        /**
         * Gets the formatted size.
         *
         * @return the formatted size
         */
        public String getFormattedSize() {
            return Format.formatSize(Long.parseLong(this.size.toString()));
        }
    }
}
