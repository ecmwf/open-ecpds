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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * The action form used when listing a destination. Keeps the parameters which
 * say how to do the display.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.ecpds.master.plugin.http.controller.transfer.destination.DetailActionDestinationCache.DataOptionsWithSizes;
import ecmwf.ecpds.master.plugin.http.controller.transfer.destination.DetailActionDestinationCache.NameCountAndSizes;
import ecmwf.ecpds.master.plugin.http.dao.transfer.DataTransferLightBean;
import ecmwf.ecpds.master.plugin.http.home.transfer.StatusHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.util.bean.Pair;

/**
 * The Class DetailActionForm.
 */
public class DetailActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7704125567780098253L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DetailActionForm.class);

    /** The Constant MIN_REFRESH. */
    private static final int MIN_REFRESH = 10;

    /** The Constant ISO_FORMAT. */
    private static final String ISO_FORMAT = "yyyy-MM-dd";

    /** The Constant DISPLAY_FORMAT. */
    private static final String DISPLAY_FORMAT = "EEE'_'dd";

    /** The Constant DAYS_BACK. */
    private static final int DAYS_BACK = 5;

    /** The per destination cache. */
    private final HashMap<String, DetailActionDestinationCache> perDestinationCache = new HashMap<>();

    // Store search, selected, etc, settings per destination AND (of course) user
    // session.

    /** The current id. */
    private String currentId = null;

    /** The data transfers size. */
    private int dataTransfersSize = 0;

    /** The refresh period. */
    private int refreshPeriod = 0;

    /** The messages. */
    private final Collection<String> messages = new ArrayList<>();

    /** The message. */
    private String message = null;

    /** The is member state. */
    private boolean isMemberState = false;

    /**
     * Sets the id.
     *
     * @param id
     *            the new id
     */
    public void setId(final String id) {
        this.currentId = id;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return this.currentId;
    }

    /**
     * Gets the messages.
     *
     * @return the messages
     */
    public Collection<String> getMessages() {
        synchronized (messages) {
            final List<String> result = new ArrayList<>(messages);
            messages.clear();
            return result;
        }
    }

    /**
     * Sets the messages.
     *
     * @param c
     *            the new messages
     */
    protected void setMessages(final Collection<String> c) {
        synchronized (messages) {
            messages.clear();
            messages.addAll(c);
        }
    }

    /**
     * Sets the messages from exceptions.
     *
     * @param c
     *            the new messages from exceptions
     */
    protected void setMessagesFromExceptions(final Collection<Exception> c) {
        final List<String> result = new ArrayList<>(c.size());
        for (final Exception e : c) {
            result.add(e.getMessage());
        }
        setMessages(result);
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        final var s = message;
        message = null;
        return s;
    }

    /**
     * Sets the message.
     *
     * @param s
     *            the new message
     */
    protected void setMessage(final String s) {
        message = s;
    }

    /**
     * Checks if is member state.
     *
     * @return true, if is member state
     */
    public boolean isMemberState() {
        return this.isMemberState;
    }

    /**
     * Sets the checks if is member state.
     *
     * @param b
     *            the new checks if is member state
     */
    public void setIsMemberState(final boolean b) {
        this.isMemberState = b;
    }

    /**
     * Gets the data transfers size.
     *
     * @return the data transfers size
     */
    public int getDataTransfersSize() {
        return this.dataTransfersSize;
    }

    /**
     * Gets the refresh period.
     *
     * @return the refresh period
     */
    public String getRefreshPeriod() {
        return Integer.toString(this.refreshPeriod);
    }

    /**
     * Sets the refresh period.
     *
     * @param t
     *            the new refresh period
     */
    public void setRefreshPeriod(final String t) {
        try {
            final var p = Integer.parseInt(t);
            if (p > MIN_REFRESH || p == 0) {
                this.refreshPeriod = p;
            } else {
                this.refreshPeriod = MIN_REFRESH;
            }
        } catch (final NumberFormatException e) {
        }
    }

    /**
     * Gets the dissemination stream options with sizes.
     *
     * @return the dissemination stream options with sizes
     */
    public Collection<NameCountAndSizes> getDisseminationStreamOptionsWithSizes() {
        return getCache().getDisseminationStreamOptionsWithSizes();
    }

    /**
     * Gets the data options with sizes.
     *
     * @return the data options with sizes
     */
    public DataOptionsWithSizes getDataOptionsWithSizes() {
        return getCache().getDataOptionsWithSizes();
    }

    /**
     * Gets the date options.
     *
     * @return the date options
     */
    public Collection<Pair> getDateOptions() {
        final var N = DAYS_BACK + 1;
        final List<Pair> l = new ArrayList<>(N + 1);
        final var c = Calendar.getInstance();
        c.setTime(new Date());
        // Let's put the All button at the beginning!
        l.add(new Pair(DetailActionDestinationCache.ALL, DetailActionDestinationCache.ALL));
        l.add(new Pair(new SimpleDateFormat(ISO_FORMAT).format(c.getTime()),
                new SimpleDateFormat(DISPLAY_FORMAT).format(c.getTime())));
        for (var i = 0; i < N; i++) {
            c.add(Calendar.DATE, -1);
            l.add(new Pair(new SimpleDateFormat(ISO_FORMAT).format(c.getTime()),
                    new SimpleDateFormat(DISPLAY_FORMAT).format(c.getTime())));
        }
        return l;
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
        return getCache().getSelectedTransfer(name);
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
        getCache().setSelectedTransfer(name, value);
    }

    /**
     * Get the transfers pre-selected for a "group" operation. A second phase will be needed to actually "execute" the
     * operation, which will be done a second set "actionTransfers", which will be a subset of this.
     *
     * @return Collection of pre-selected DataTransfers.
     *
     * @see getActionTransfers
     */
    public int getSelectedTransfersCount() {
        return getCache().getSelectedTransfersCount();
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
        return getCache().getDataTransfer(id);
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
    public Collection<DataTransfer> getSelectedTransfers(final Integer from, final Integer to) {
        return getCache().getSelectedTransfers(from, to);
    }

    /**
     * Delete from selections.
     *
     * @param dt
     *            the dt
     */
    public void deleteFromSelections(final DataTransfer dt) {
        getCache().deleteFromSelection(dt);
    }

    /**
     * Adds the to selections.
     *
     * @param c
     *            the c
     */
    public void addToSelections(final Collection<DataTransferLightBean> c) {
        getCache().addToSelections(c);
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
        return getCache().getActionTransfer(name);
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
        getCache().setActionTransfer(name, value);
    }

    /**
     * Get the transfers to which we will apply "group" operations. We will be able to , ie, "requeue",
     * "increasePriority" or "decreasePriority" to all of them at the same time
     *
     * @return A Collection of DataTransfers which will be the subject for a "group" operation.
     */
    public Collection<DataTransfer> getActionTransfers() {
        return getCache().getActionTransfers();
    }

    /**
     * Gets the display tags param collection.
     *
     * @return the display tags param collection
     */
    public Collection<Pair> getDisplayTagsParamCollection() {
        return getCache().getDisplayTagsParamsCollection();
    }

    /**
     * Sets the display tags params.
     *
     * @param c
     *            the new display tags params
     */
    public void setDisplayTagsParams(final Collection<Pair> c) {
        getCache().setDisplayTagsParamsCollection(c);
    }

    /**
     * Gets the status options with sizes.
     *
     * @return the status options with sizes
     */
    public Collection<NameCountAndSizes> getStatusOptionsWithSizes() {
        return getCache().getStatusOptionsWithSizes();
    }

    /**
     * Gets the data stream.
     *
     * @return the data stream
     */
    public String getDataStream() {
        return getCache().getDataStream();
    }

    /**
     * Gets the data time.
     *
     * @return the data time
     */
    public String getDataTime() {
        return getCache().getDataTime();
    }

    /**
     * Gets the dissemination stream.
     *
     * @return the dissemination stream
     */
    public String getDisseminationStream() {
        return getCache().getDisseminationStream();
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public String getStatus() {
        return getCache().getStatus();
    }

    /**
     * Sets the data stream.
     *
     * @param string
     *            the new data stream
     */
    public void setDataStream(final String string) {
        getCache().setDataStream(string);
    }

    /**
     * Sets the data time.
     *
     * @param string
     *            the new data time
     */
    public void setDataTime(final String string) {
        getCache().setDataTime(string);
    }

    /**
     * Sets the dissemination stream.
     *
     * @param string
     *            the new dissemination stream
     */
    public void setDisseminationStream(final String string) {
        getCache().setDisseminationStream(string);
    }

    /**
     * Sets the status.
     *
     * @param string
     *            the new status
     */
    public void setStatus(final String string) {
        getCache().setStatus(string);
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public String getDate() {
        if (getCache() != null) {
            return getCache().getDate();
        }
        return new SimpleDateFormat(ISO_FORMAT).format(new Date());
    }

    /**
     * Sets the date.
     *
     * @param date
     *            the new date
     */
    public void setDate(final String date) {
        getCache().setDate(date);
    }

    /**
     * Gets the data transfer caption.
     *
     * @return the data transfer caption
     */
    public String getDataTransferCaption() {
        final var out = new StringBuilder();
        final var disseminationStream = getDisseminationStream();
        if (!"".equals(disseminationStream)) {
            out.append(disseminationStream);
        } else {
            out.append("All in destination");
        }
        if (getDataStream() != null) {
            out.append("/").append(getDataStream());
        }
        if (getDataTime() != null) {
            out.append("/").append(getDataTime());
        }
        try {
            if (getStatus() != null) {
                out.append("/").append(StatusHome.findByPrimaryKey(getStatus()).getName());
            }
        } catch (final Throwable t) {
        }
        if (getDate() != null) {
            out.append("/").append(getDate());
        }
        if (getFileNameSearch() != null) {
            out.append("/").append("".equals(getFileNameSearch()) ? "*" : getFileNameSearch());
        }
        return out.toString();
    }

    /**
     * Gets the data transfers.
     *
     * @param hasAccess
     *            the has access
     *
     * @return the data transfers
     */
    public Collection<DataTransferLightBean> getDataTransfers(final boolean hasAccess) {
        try {
            final var c = getCache().getDataTransfers(hasAccess);
            this.dataTransfersSize = c.size();
            return c;
        } catch (final TransferException e) {
            log.error("Problem getting Data Transfers", e);
            return new ArrayList<>(0);
        }
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
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    public Collection<DataTransferLightBean> getDataTransfers(final boolean hasAccess, final DataBaseCursor cursor)
            throws TransferException {
        final var c = getCache().getDataTransfers(hasAccess, cursor);
        this.dataTransfersSize = c.size();
        return c;
    }

    /**
     * Clean action transfers.
     */
    public void cleanActionTransfers() {
        getCache().cleanActionTransfers();
    }

    /**
     * Clean selected transfers.
     */
    public void cleanSelectedTransfers() {
        getCache().cleanSelectedTransfers();
    }

    /**
     * Sets the new priority.
     *
     * @param s
     *            the new new priority
     */
    public void setNewPriority(final String s) {
        getCache().setNewPriority(s);
    }

    /**
     * Gets the new priority.
     *
     * @return the new priority
     */
    public String getNewPriority() {
        return getCache().getNewPriority();
    }

    /**
     * Sets the file name search.
     *
     * @param s
     *            the new file name search
     */
    public void setFileNameSearch(final String s) {
        getCache().setFileNameSearch(s);
    }

    /**
     * Gets the file name search.
     *
     * @return the file name search
     */
    public String getFileNameSearch() {
        return getCache().getFileNameSearch();
    }

    /**
     * {@inheritDoc}
     *
     * Here we need to call setId() with the current Destination ID. This needs to be set in order to be able to select
     * the CACHE to which all setters are going to write. This will be called automatically BEFORE any setter, for EVERY
     * request, so this way we'll ensure that all the setters will find an adequate CACHE set.
     */
    @SuppressWarnings("null")
    @Override
    public void reset(final ActionMapping map, final HttpServletRequest req) {
        String id = null;
        final var parameter = map.getParameter();
        if (parameter != null) {
            final var tok = new StringTokenizer(parameter, ",");
            tok.nextToken(); // Skip basePath
            if (tok.hasMoreTokens()) {
                id = tok.nextToken();
                this.setId(id);
                log.debug("The ID is " + id + " (from map)");
            }
        }
        if (id == null) {
            final var pathInfo = req.getPathInfo();
            final var pos = pathInfo != null ? pathInfo.lastIndexOf("/") : -1;
            if (pos > 0 && pathInfo.length() > pos) {
                id = pathInfo.substring(pos + 1);
                log.debug("The ID is " + id + " (from pathInfo)");
            }
        }
        if (id == null) {
            log.debug("The ID is not defined (null)");
        }
    }

    /**
     * Gets the cache.
     *
     * @return the cache
     */
    private DetailActionDestinationCache getCache() {
        if (currentId == null) {
            return new DetailActionDestinationCache(this);
        }
        var cache = perDestinationCache.get(currentId);
        if (cache == null) {
            perDestinationCache.put(currentId, cache = new DetailActionDestinationCache(this, currentId));
        }
        return cache;
    }
}
