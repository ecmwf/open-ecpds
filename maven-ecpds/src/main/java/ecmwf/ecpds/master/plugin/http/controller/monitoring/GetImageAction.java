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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Status;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/**
 * The Class GetImageAction.
 */
public class GetImageAction extends PDSAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(GetImageAction.class);

    /** The Constant SCHEDULED. */
    private static final int SCHEDULED = 0;

    /** The Constant SCHEDULED_10. */
    private static final int SCHEDULED_10 = 1;

    /** The Constant SCHEDULED_25. */
    private static final int SCHEDULED_25 = 2;

    /** The Constant SCHEDULED_40. */
    private static final int SCHEDULED_40 = 3;

    /** The Constant EARLIEST. */
    private static final int EARLIEST = 4;

    /** The Constant LATEST. */
    private static final int LATEST = 5;

    /** The Constant TARGET. */
    private static final int TARGET = 6;

    /** The Constant PREDICTED. */
    private static final int PREDICTED = 7;

    /** The Constant ACTUAL. */
    private static final int ACTUAL = 8;

    /** The Constant NFIELDS. */
    private static final int NFIELDS = ACTUAL + 1;

    /** The date format. */
    public static String DATE_FORMAT = "dd/MM/yyyy HH:mm";

    /** The Constant TWO_DIGITS. */
    public static final DecimalFormat TWO_DIGITS = new DecimalFormat("00");

    /** The Constant colours. */
    public static final Color[] colours = { new Color(0x444444), new Color(0x777777), new Color(0xaaaaaa),
            new Color(0xdddddd), Color.PINK, Color.RED, Color.GREEN, Color.ORANGE, Color.BLUE };

    /** The Constant valueNames. */
    public static final String[] valueNames = { "S", "S1", "S2", "S4", "E", "L", "T", "P", "A" };

    /** The Constant valueDescriptions. */
    public static final String[] valueDescriptions = { "Scheduled", "Scheduled-10m", "Scheduled-25m", "Scheduled-40m",
            "Earliest", "Latest", "Target", "Predicted", "Actual" };

    /** The Constant EXTRA_SPACE_RIGHT. */
    private static final int EXTRA_SPACE_RIGHT = 30;

    /**
     * Safe authorized perform.
     *
     * @param mapping
     *            the mapping
     * @param form
     *            the form
     * @param request
     *            the request
     * @param response
     *            the response
     * @param user
     *            the user
     *
     * @return the action forward
     *
     * @throws ECMWFException
     *             the ECMWF exception
     * @throws ClassCastException
     *             the class cast exception
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var param = ECMWFActionForm.getPathParameter(mapping, request, 0);
        if ("timeline".equals(param)
                && request.getAttribute("datatransfers") instanceof final Collection<?> datatransfers
                && request.getAttribute("step") instanceof final Integer step
                && request.getAttribute("stepWidth") instanceof final Integer stepWidth) {
            request.setAttribute("image", getTimelineImage(datatransfers, step, stepWidth));
        } else if (request.getAttribute("datatransfers") instanceof final Collection<?> datatransfers) {
            request.setAttribute("image",
                    getImage(datatransfers, request.getAttribute("product").toString(), "transfer".equals(param)));
        }
        request.setAttribute("contentType", "image/png");
        return mapping.findForward("binary_dump");
    }

    /**
     * Gets the image.
     *
     * @param dataTransfers
     *            the data transfers
     * @param tag
     *            the tag
     * @param transfer
     *            the transfer
     *
     * @return the image
     *
     * @throws TransferException
     *             the transfer exception
     */
    private static final BufferedImage getImage(final Collection<?> dataTransfers, final String tag,
            final boolean transfer) throws TransferException {
        final var N = dataTransfers.size();
        final var values = new long[N][NFIELDS];
        final var timeSteps = new String[N];
        final var fileNames = new String[N];
        final var spacePerTransfer = 17;
        final var marginLeft = 40;
        final var marginTop = 40;
        final var width = 600;
        final var height = N * spacePerTransfer + marginTop;
        // Fill data array;
        var maxValue = dataTransfers.size() > 0 ? Long.MIN_VALUE : new Date().getTime();
        var minValue = dataTransfers.size() > 0 ? Long.MAX_VALUE : new Date().getTime();
        var i = 0;
        for (Object dataTransfer : dataTransfers) {
            if (dataTransfer instanceof final DataTransfer data) {
                timeSteps[i] = Long.toString(data.getDataFile().getTimeStep());
                final var datafile = data.getDataFile();
                fileNames[i] = datafile.getMetaTime() + "-" + datafile.getMetaStream() + "-" + datafile.getMetaType();
                if (transfer) {
                    fillTransferValues(values[i], data);
                } else {
                    fillArrivalValues(values[i], data, data.getDataFile());
                }
                // Check if any of the values is the MAX or MIN
                for (var j = 0; j < NFIELDS; j++) {
                    if (values[i][j] < minValue && values[i][j] > 0) {
                        minValue = values[i][j];
                    }
                    if (values[i][j] > maxValue) {
                        maxValue = values[i][j];
                    }
                }
                i++;
            }
        }
        // The scale will start/end half an hour before/after the
        // earliest/latest time.
        // long timeBase = minValue - 30 * 60 * 1000;
        // long timeWidth = (maxValue + 30 * 60 * 1000) - timeBase;
        // 2009-12-16: Not anymore....
        final var timeBase = minValue;
        final var timeWidth = maxValue - timeBase;
        // Prepare image, with background
        final var l = createBasicImageAndGraphics(width, height);
        final var image = (BufferedImage) l.get(0);
        final var g = (Graphics2D) l.get(1);
        drawTimeScales(g, width, height, timeBase, timeWidth, marginLeft, marginTop);
        // Calculate X positions for the values
        for (i = 0; i < N; i++) {
            for (var j = 0; j < NFIELDS; j++) {
                values[i][j] = calculateXFromTime(values[i][j], timeBase, timeWidth, width, marginLeft);
            }
        }
        // And now draw the darn thing
        var y = marginTop;
        if (N > 0) {
            for (var j = 0; j < NFIELDS; j++) {
                if (!transfer && j != SCHEDULED_10 && j != SCHEDULED_25 && j != SCHEDULED_40) {
                    g.setColor(Color.BLACK);
                }
                g.drawBytes(valueNames[j].getBytes(), 0, valueNames[j].length(), (int) values[0][j], y);
            }
        }
        for (i = 0; i < N; i++) {
            // TS caption
            g.setColor(Color.BLACK);
            g.setFont(new Font("Monospace", Font.PLAIN, 10));
            g.drawBytes(timeSteps[i].getBytes(), 0, timeSteps[i].length(), 10, y);
            g.setColor(new Color(0xccce9b));
            g.drawBytes(fileNames[i].getBytes(), 0, fileNames[i].length(), marginLeft + 1, y - 1);
            // Horizontal line
            g.setColor(new Color(0xeeeede));
            g.drawLine(marginLeft, y, width + EXTRA_SPACE_RIGHT, y);
            y += spacePerTransfer;
        }
        // And now draw the vertical lines and the Earliest/Latest
        y = marginTop;
        for (i = 1; i < N; i++) {
            for (var j = 0; j < NFIELDS; j++) {
                if (values[i][j] > 0 && values[i - 1][j] > 0) {
                    g.setColor(colours[j]);
                    g.drawLine((int) values[i - 1][j], y, (int) values[i][j], y + spacePerTransfer);
                }
            }
            // Earliest/Latest
            g.setColor(Color.BLACK);
            g.drawLine((int) values[i - 1][EARLIEST], y, (int) values[i - 1][LATEST], y);
            g.drawLine((int) values[i - 1][EARLIEST], y - 2, (int) values[i - 1][EARLIEST], y + 2);
            g.drawLine((int) values[i - 1][LATEST], y - 2, (int) values[i - 1][LATEST], y + 2);
            y += spacePerTransfer;
        }
        if (N > 6) {
            drawArrivalTransfersConventionsBox(g, 485, 50, transfer);
        }
        return image;
    }

    /**
     * Gets the timeline image.
     *
     * @param dataTransfers
     *            the data transfers
     * @param step
     *            the step
     * @param stepSize
     *            the step size
     *
     * @return the timeline image
     *
     * @throws TransferException
     *             the transfer exception
     */
    private static final BufferedImage getTimelineImage(final Collection<?> dataTransfers, final int step,
            final int stepSize) throws TransferException {
        final var size = dataTransfers.size();
        final var limA = (step - 1) * stepSize;
        var limB = step * stepSize;
        if (limB >= size) {
            limB = size;
        }
        final List<?> dataTransferSlice = new ArrayList<>(dataTransfers).subList(limA, limB);
        final var N = dataTransferSlice.size();
        final var values = new long[N][2];
        final var spacePerTransfer = 17;
        final var marginLeft = 40;
        final var marginTop = 40;
        final var width = 600;
        final var height = N * spacePerTransfer + marginTop;
        var maxValue = !dataTransferSlice.isEmpty() ? Long.MIN_VALUE : new Date().getTime();
        var minValue = !dataTransferSlice.isEmpty() ? Long.MAX_VALUE : new Date().getTime();
        // Prepare data and calculate ranges
        var i = 0;
        for (Object element : dataTransferSlice) {
            if (element instanceof final DataTransfer data) {
                // Cope with the different statii for the transfer.
                // Some dates might not be set, depending on it.
                if (data.getStartTime() == null) {
                    values[i][0] = data.getScheduledTime().getTime();
                    values[i][1] = data.getScheduledTime().getTime();
                } else if (data.getFinishTime() == null) {
                    values[i][0] = data.getStartTime().getTime();
                    values[i][1] = new Date().getTime();
                } else {
                    values[i][0] = data.getStartTime().getTime();
                    values[i][1] = data.getFinishTime().getTime();
                }
                if (values[i][0] < minValue) {
                    minValue = values[i][0];
                }
                if (values[i][1] > maxValue) {
                    maxValue = values[i][1];
                }
                i++;
            }
        }
        final var timeBase = minValue;
        final var timeWidth = maxValue - minValue;
        // Prepare image, with background
        final var l = createBasicImageAndGraphics(width, height);
        final var image = (BufferedImage) l.get(0);
        final var g = (Graphics2D) l.get(1);
        // Draw lines
        drawTimeScales(g, width, height, timeBase, timeWidth, marginLeft, marginTop);
        // Draw data
        var y = marginTop;
        i = 0;
        for (Object element : dataTransferSlice) {
            if (element instanceof final DataTransfer data) {
                final var datafile = data.getDataFile();
                // TS caption
                g.setColor(Color.BLACK);
                g.setFont(new Font("Monospace", Font.PLAIN, 10));
                g.drawBytes(Long.toString(datafile.getTimeStep()).getBytes(), 0,
                        Long.toString(datafile.getTimeStep()).length(), 10, y);
                g.setColor(new Color(0xccce9b));
                // g.drawBytes(data.getTarget().getBytes(), 0,
                // data.getTarget().length(), marginLeft + 1, y - 1);

                // String text = ((data.getHostName() != null) ? data.getHostName()
                // : "Unassigned") + ":" + data.getId();
                final var text = datafile.getMetaTime() + "-" + datafile.getMetaStream() + "-" + datafile.getMetaType();
                g.drawBytes(text.getBytes(), 0, text.length(), marginLeft + 1, y - 1);
                // Horizontal line
                g.setColor(new Color(0xeeeede));
                g.drawLine(marginLeft, y, width + EXTRA_SPACE_RIGHT, y);
                // Rectangle representing transfer.
                // Color depends on the status
                g.setColor(getStatusColor(data.getStatusCode()));
                final var x1 = calculateXFromTime(values[i][0], timeBase, timeWidth, width, marginLeft);
                final var x2 = calculateXFromTime(values[i][1], timeBase, timeWidth, width, marginLeft);
                // If transfer is "In progress" draw a pointy rectangle
                if (Status.EXEC.equals(data.getStatusCode())) {
                    g.drawLine(x1, y - 10, x2 - 5, y - 10);
                    g.drawLine(x1, y, x2 - 5, y);
                    g.drawLine(x1, y, x1, y - 10);
                    g.drawLine(x2 - 5, y - 10, x2, y - 5);
                    g.drawLine(x2 - 5, y, x2, y - 5);
                    final var progress = data.getProgress() + " %";
                    g.drawBytes(progress.getBytes(), 0, progress.length(), x1 + 3, y - 1);
                } else {
                    g.drawRect(x1, y - 10, x2 - x1, 10);
                }
                // Write the time taken by the transfer
                final var timeDiff = values[i][1] - values[i][0];
                if (timeDiff > 0) {
                    final var time = formatTime(timeDiff);
                    g.drawBytes(time.getBytes(), 0, time.length(), x2 + 3, y - 1);
                } else {
                    g.drawBytes(data.getStatusCode().getBytes(), 0, data.getStatusCode().length(), x2 + 3, y - 1);
                }
                // Next one!
                y += spacePerTransfer;
                i++;
            }
        }
        return image;
    }

    /**
     * Fill arrival values.
     *
     * @param fields
     *            the fields
     * @param data
     *            the data
     * @param file
     *            the file
     */
    private static final void fillArrivalValues(final long[] fields, final DataTransfer data, final DataFile file) {
        try {
            fields[EARLIEST] = data.getArrivalEarliestTime() != null ? data.getArrivalEarliestTime().getTime() : -1;
            fields[LATEST] = data.getArrivalLatestTime() != null ? data.getArrivalLatestTime().getTime() : -1;
            fields[TARGET] = data.getArrivalTargetTime() != null ? data.getArrivalTargetTime().getTime() : -1;
            fields[ACTUAL] = file.getArrivedTime() != null ? file.getArrivedTime().getTime() : -1;
            fields[PREDICTED] = data.getArrivalPredictedTime() != null ? data.getArrivalPredictedTime().getTime() : -1;
            fields[SCHEDULED] = data.getScheduledTime() != null ? data.getScheduledTime().getTime() : -1;
            fields[SCHEDULED_10] = data.getScheduledTimeMinusMinutes(10) != null
                    ? data.getScheduledTimeMinusMinutes(10).getTime() : -1;
            fields[SCHEDULED_25] = data.getScheduledTimeMinusMinutes(25) != null
                    ? data.getScheduledTimeMinusMinutes(25).getTime() : -1;
            fields[SCHEDULED_40] = data.getScheduledTimeMinusMinutes(40) != null
                    ? data.getScheduledTimeMinusMinutes(40).getTime() : -1;
        } catch (final Exception e) {
            log.error("Got an exception filling arrival data for DataTransfer '" + data.getTarget() + "'", e);
        }
    }

    /**
     * Fill transfer values.
     *
     * @param fields
     *            the fields
     * @param data
     *            the data
     */
    private static final void fillTransferValues(final long[] fields, final DataTransfer data) {
        try {
            fields[EARLIEST] = data.getTransferEarliestTime() != null ? data.getTransferEarliestTime().getTime() : -1;
            fields[LATEST] = data.getTransferLatestTime() != null ? data.getTransferLatestTime().getTime() : -1;
            fields[TARGET] = data.getTransferTargetTime() != null ? data.getTransferTargetTime().getTime() : -1;
            fields[ACTUAL] = data.getFinishTime() != null ? data.getFinishTime().getTime() : -1;
            fields[PREDICTED] = data.getTransferPredictedTime() != null ? data.getTransferPredictedTime().getTime()
                    : -1;
            fields[SCHEDULED] = data.getScheduledTime() != null ? data.getScheduledTime().getTime() : -1;
            fields[SCHEDULED_10] = data.getScheduledTime() != null ? data.getScheduledTime().getTime() : -1;
            fields[SCHEDULED_25] = data.getScheduledTime() != null ? data.getScheduledTime().getTime() : -1;
            fields[SCHEDULED_40] = data.getScheduledTime() != null ? data.getScheduledTime().getTime() : -1;
        } catch (final Exception e) {
            log.error("Got an exception filling transfer data for DataTransfer '" + data.getTarget() + "'", e);
        }
    }

    /**
     * Calculate X from time.
     *
     * @param date
     *            the date
     * @param timeBase
     *            the time base
     * @param timeWidth
     *            the time width
     * @param width
     *            the width
     * @param marginLeft
     *            the margin left
     *
     * @return the int
     */
    private static final int calculateXFromTime(final long date, final long timeBase, final long timeWidth,
            final int width, final int marginLeft) {
        final var time = date - timeBase;

        // long last = timeBase + timeWidth;
        // if (date>last)
        // log.debug("Date out of range!!!!!: "+new Date(date));
        // if (x>width)
        // log.debug("Too wide!!!!!: "+x);
        return (int) (time * (width - marginLeft) / timeWidth) + marginLeft;
    }

    /**
     * Format time.
     *
     * @param msecs
     *            the msecs
     *
     * @return the string
     */
    private static final String formatTime(final long msecs) {
        if (msecs < 1000) {
            return msecs + " ms.";
        }
        if (msecs < 60000) {
            return TWO_DIGITS.format((int) (msecs / 1000)) + " s.";
        } else if (msecs < 3600000) {
            return TWO_DIGITS.format((int) (msecs / 60000)) + ":" + TWO_DIGITS.format((int) (msecs % 60000 / 1000))
                    + " s.";
        } else if (msecs < 86400000) {
            return TWO_DIGITS.format((int) (msecs / 3600000)) + ":" + TWO_DIGITS.format((int) (msecs % 3600000 / 60000))
                    + " m.";
        } else {
            return (int) (msecs / 86400000) + " days " + (int) (msecs % 86400000 / 3600000) + " hours.";
        }
    }

    /**
     * Creates the basic image and graphics.
     *
     * @param width
     *            the width
     * @param height
     *            the height
     *
     * @return the list
     */
    private static final List<Object> createBasicImageAndGraphics(final int width, final int height) {
        final List<Object> l = new ArrayList<>(2);
        final var image = new BufferedImage(width + EXTRA_SPACE_RIGHT, height, BufferedImage.TYPE_3BYTE_BGR);
        final var g = image.createGraphics();
        final Map<Key, Object> options = new HashMap<>();
        options.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHints(options);
        g.setBackground(new Color(0xfffff2));
        g.clearRect(0, 0, width + EXTRA_SPACE_RIGHT, height);
        g.setColor(new Color(0xeeeede));
        l.add(image);
        l.add(g);
        return l;
    }

    /**
     * Draw time scales.
     *
     * @param g
     *            the g
     * @param width
     *            the width
     * @param height
     *            the height
     * @param timeBase
     *            the time base
     * @param timeWidth
     *            the time width
     * @param marginLeft
     *            the margin left
     * @param marginTop
     *            the margin top
     */
    private static final void drawTimeScales(final Graphics2D g, final int width, final int height, final long timeBase,
            final long timeWidth, final int marginLeft, final int marginTop) {
        final var startTime = new Date(timeBase);
        final var endTime = new Date(timeBase + timeWidth);
        g.drawLine(marginLeft, marginTop, marginLeft, height);
        g.drawLine(marginLeft, 9, width, 9);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospace", Font.BOLD, 9));
        final var timeBaseStr = "<-" + new SimpleDateFormat(DATE_FORMAT).format(startTime);
        // g.drawBytes(timeBaseStr.getBytes(), 0, timeBaseStr.length(),
        // marginLeft, 8);
        g.drawBytes(timeBaseStr.getBytes(), 0, timeBaseStr.length(), 1, 8);
        final var timeEndStr = new SimpleDateFormat(DATE_FORMAT).format(endTime) + "->";
        g.drawBytes(timeEndStr.getBytes(), 0, timeEndStr.length(), width - 100, 8);
        // We'll try to draw ~10 vertical lines at "round" time figures (1s, 5s,
        // 10s, 1m, 1h, etc).
        final var periodMSecs = timeWidth / 20;
        final int periodField;
        final int periodJump;
        final var cal = Calendar.getInstance();
        cal.setTime(startTime);
        if (periodMSecs < 1000) {
            periodField = Calendar.SECOND;
            periodJump = 1;
            cal.set(Calendar.MILLISECOND, 0);
        } else if (periodMSecs < 5000) {
            periodField = Calendar.SECOND;
            periodJump = 5;
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
        } else if (periodMSecs < 10000) {
            periodField = Calendar.SECOND;
            periodJump = 10;
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
        } else if (periodMSecs < 30000) {
            periodField = Calendar.SECOND;
            periodJump = 30;
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
        } else if (periodMSecs < 60000) {
            periodField = Calendar.MINUTE;
            periodJump = 1;
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
        } else if (periodMSecs < 3600000) {
            periodField = Calendar.HOUR_OF_DAY;
            periodJump = 1;
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
        } else {
            periodField = Calendar.DAY_OF_MONTH;
            periodJump = 1;
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);
        }
        g.setColor(new Color(0xccce9b));
        while (cal.getTime().before(endTime)) {
            cal.add(periodField, periodJump);
            final var xTime = cal.getTime();
            if (xTime.after(startTime)) {
                final var x = calculateXFromTime(xTime.getTime(), timeBase, timeWidth, width, marginLeft);
                g.drawLine(x, 9, x, height);
                final var caption = Integer.toString(cal.get(periodField));
                g.drawBytes(caption.getBytes(), 0, caption.getBytes().length, x + 2, 17);
            }
        }
    }

    /**
     * Gets the status color.
     *
     * @param status
     *            the status
     *
     * @return the status color
     */
    private static final Color getStatusColor(final String status) {
        if (Status.DONE.equals(status)) {
            return new Color(0x007700);
        }
        if (Status.EXEC.equals(status)) {
            return Color.BLUE;
        } else if (Status.STOP.equals(status)) {
            return Color.RED;
        } else if (Status.RETR.equals(status)) {
            return Color.ORANGE;
        } else {
            return Color.GRAY;
        }
    }

    /**
     * Draw arrival transfers conventions box.
     *
     * @param g
     *            the g
     * @param X
     *            the x
     * @param Y
     *            the y
     * @param transfer
     *            the transfer
     */
    private static final void drawArrivalTransfersConventionsBox(final Graphics2D g, final int X, final int Y,
            final boolean transfer) {
        final var step = 14;
        var x = X;
        var y = Y + 14;
        g.setColor(new Color(0xccce9b));
        // g.drawRect(X-3, Y-3, 112, (colours.length*20)+16);
        g.drawRect(X - 2, Y - 2, 110, (transfer ? colours.length - 3 : colours.length) * step + 14);
        g.setColor(Color.WHITE);
        g.fillRect(X, Y, 106, (transfer ? colours.length - 3 : colours.length) * step + 10);
        for (var i = 0; i < colours.length; i++) {
            if (!transfer || i != SCHEDULED_10 && i != SCHEDULED_25 && i != SCHEDULED_40) {
                g.setColor(Color.BLACK);
                x = X + 5;
                g.setColor(colours[i]);
                final var text = valueNames[i] + ": " + valueDescriptions[i];
                g.drawBytes(text.getBytes(), 0, text.length(), x, y);
                y += step;
            }
        }
    }
}
