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

package ecmwf.ecpds.master.plugin.http.controller.datafile.datafile;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Returns a DataTables-compatible JSON payload for the data file list, enabling
 * server-side pagination without loading all rows into the page HTML.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.datafile.DataFileHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetDataFileListJsonAction.
 *
 * Handles AJAX DataTables server-side requests for the data file list page. Returns JSON in the standard DataTables
 * server-side protocol format.
 */
public class GetDataFileListJsonAction extends PDSAction {

    /** Base path for data file detail pages. */
    private static final String DATAFILE_BASE_PATH = "/do/datafile/datafile";

    /** Shared Jackson mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Maps DataTables column index (0-based) to the DB sort column. SQL sort values: 0=DAF_ID, 1=DAF_TIME_BASE,
     * 2=DAF_SIZE, 3=DAF_TIME_STEP Columns: 0=Original, 1=Product Time, 2=Size, 3=TS
     */
    private static final int[] SORT_COLS = { 0, 1, 2, 3 };

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException {
        final var draw = parseSafeInt(request.getParameter("draw"), 1);
        final var metaDataName = Util.getValue(request, "metaDataName", "");
        final var metaDataValue = Util.getValue(request, "metaDataValue", "");
        final var rawSearch = Util.getValue(request, "datafileSearch", "");
        // Convert to SQL LIKE pattern: replace * → %, ? → _, wrap with % if no wildcards
        final var search = buildLikePattern(rawSearch);

        // Parse date, fall back to today if absent or unparseable
        final var iso = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = iso.parse(Util.getValue(request, "date", ""));
        } catch (final ParseException e) {
            date = new Date();
        }

        // Build DataBaseCursor from DataTables params
        var start = 0;
        var length = 25;
        try {
            start = Integer.parseInt(request.getParameter("start"));
        } catch (final Throwable _) {
        }
        try {
            length = Integer.parseInt(request.getParameter("length"));
            if (length < 1) {
                length = 25;
            }
        } catch (final Throwable _) {
        }
        var colIdx = parseSafeInt(request.getParameter("order[0][column]"), 1);
        if (colIdx < 0 || colIdx >= SORT_COLS.length) {
            colIdx = 1;
        }
        final var dbSortCol = SORT_COLS[colIdx];
        final var dir = request.getParameter("order[0][dir]");
        // DB order: "1" = ascending, "2" = descending
        final var dbOrder = "asc".equalsIgnoreCase(dir) ? "1" : "2";
        final var cursor = new DataBaseCursor(String.valueOf(dbSortCol), dbOrder, start, start + length);

        Collection<DataFile> datafiles;
        try {
            datafiles = DataFileHome.findByMetaDataAndDate(metaDataName, metaDataValue, search, date, cursor);
        } catch (final Exception e) {
            datafiles = new ArrayList<>(0);
        }

        final var recordsTotal = Util.getCollectionSizeFrom(datafiles);
        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", recordsTotal);
        root.put("recordsFiltered", recordsTotal);
        final var data = root.putArray("data");
        for (final DataFile df : datafiles) {
            final var row = data.addArray();
            row.add(buildOriginalHtml(df));
            final var pt = df.getProductTime();
            row.add(pt != null ? Format.formatTime(pt.getTime()) : "");
            row.add(Format.formatSize(df.getSize()));
            row.add(String.valueOf(df.getTimeStep()));
        }
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building data file list: " + e.getMessage());
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // HTML column builders
    // -------------------------------------------------------------------------

    private static String buildLikePattern(final String input) {
        if (input == null || input.isBlank()) {
            return ""; // no filter
        }
        // Escape SQL LIKE special chars in the raw input (except user wildcards)
        var s = input.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        // Convert user-friendly wildcards
        s = s.replace("*", "%").replace("?", "_");
        // If the user didn't use any wildcards, do a substring match
        if (!s.contains("%") && !s.contains("_")) {
            s = "%" + s + "%";
        }
        return s;
    }

    private static String buildOriginalHtml(final DataFile df) {
        final var id = escapeHtml(df.getId());
        final var original = escapeHtml(df.getOriginal());
        return "<a href=\"" + DATAFILE_BASE_PATH + "/" + id
                + "\" class=\"text-decoration-none font-monospace\" style=\"font-size:0.82rem\">" + original + "</a>";
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String escapeHtml(final String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
                "&#39;");
    }

    private static int parseSafeInt(final String s, final int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (final Throwable _) {
            return fallback;
        }
    }

    private static void writeError(final HttpServletResponse response, final int draw, final String message) {
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            final ObjectNode err = MAPPER.createObjectNode();
            err.put("draw", draw);
            err.put("recordsTotal", 0);
            err.put("recordsFiltered", 0);
            err.putArray("data");
            err.put("error", message);
            MAPPER.writeValue(response.getWriter(), err);
        } catch (final Exception ignored) {
        }
    }
}
