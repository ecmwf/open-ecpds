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

package ecmwf.ecpds.master.plugin.http.dao.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;

import ecmwf.common.database.DataBaseObject;
import ecmwf.ecpds.master.plugin.http.model.transfer.ChangeLog;
import ecmwf.web.dao.ModelBeanBase;

/**
 * The Class ChangeLogBean.
 */
public class ChangeLogBean extends ModelBeanBase implements ChangeLog {

    /** The Constant HTML_MULTIPLE_LINES. */
    private static final String HTML_MULTIPLE_LINES = "<i><font color='grey'>(across-multiple-lines)</font></i>";

    /** The Constant PARAMETER_MODIFIED. */
    private static final String PARAMETER_MODIFIED = "<i><font color='grey'>(modified)</font></i>";

    /** The Constant translationMap. */
    private static final Map<String, String> translationMap = new HashMap<>();

    static {
        // Destination fields
        translationMap.put("DES_UPDATE", ""); // ignored
        translationMap.put("DES_USER_STATUS", ""); // ignored
        translationMap.put("COU_ISO", "Country");
        translationMap.put("DES_ACTIVE", "Enabled");
        translationMap.put("DES_BACKUP", "Backup");
        translationMap.put("DES_COMMENT", "Comment");
        translationMap.put("DES_IF_TARGET_EXIST", "If Target Exist");
        translationMap.put("DES_KEEP_IN_SPOOL", "Keep In Spool");
        translationMap.put("DES_MAIL_ON_END", "Mail On End");
        translationMap.put("DES_MAIL_ON_ERROR", "Mail On Error");
        translationMap.put("DES_MAIL_ON_START", "Mail On Start");
        translationMap.put("DES_MAX_CONNECTIONS", "Maximum Connections");
        translationMap.put("DES_MAX_PENDING", "Maximum Pending");
        translationMap.put("DES_MAX_FILE_SIZE", "Maximum File Size");
        translationMap.put("DES_MAX_REQUEUE", "Maximum Requeue");
        translationMap.put("DES_MAX_START", "Maximum Start");
        translationMap.put("DES_MONITOR", "Monitored");
        translationMap.put("DES_NAME", "Name");
        translationMap.put("DES_DATA", "Options");
        translationMap.put("DES_ON_HOST_FAILURE", "On Host Failure");
        translationMap.put("DES_RESET_FREQUENCY", "Reset Frequency");
        translationMap.put("DES_RETRY_COUNT", "Retry Count");
        translationMap.put("DES_RETRY_FREQUENCY", "Retry Frequency");
        translationMap.put("DES_START_FREQUENCY", "Start Frequency");
        translationMap.put("DES_STOP_IF_DIRTY", "Stop If Dirty");
        translationMap.put("DES_ACQUISITION", "Acquisition");
        translationMap.put("DES_MAX_INACTIVITY", "Maximum Inactivity");
        translationMap.put("DES_TRANSFER_RATE", "Transfer Rate");
        translationMap.put("DES_FILTER_NAME", "Filter Name");
        translationMap.put("DES_USER_MAIL", "User Mail");
        translationMap.put("DES_TYPE", "Type");
        translationMap.put("DES_GROUPBY_DATE", "Group By Date");
        translationMap.put("DES_DATE_FORMAT", "Date Format");
        translationMap.put("ECU_NAME", "User Name");
        translationMap.put("TRG_NAME", "Transfer Group Name");
        translationMap.put("SCV_ID", "Schedule Identifier");
        translationMap.put("STA_CODE", "Status");
        translationMap.put("HOS_NAME_FOR_SOURCE", "Host For Source");
        // Host fields
        translationMap.put("HOS_ACTIVE", "Enabled");
        translationMap.put("HOS_CHECK", "Check");
        translationMap.put("HOS_TYPE", "Type");
        translationMap.put("HOS_CHECK_FILENAME", "Check Filename");
        translationMap.put("HOS_CHECK_FREQUENCY", "Check Frequency");
        translationMap.put("HOS_ACQUISITION_FREQUENCY", "Acquisition Frequency");
        translationMap.put("HOS_COMMENT", "Comment");
        translationMap.put("HOS_DATA", "Options");
        translationMap.put("HOS_DIR", "Directory");
        translationMap.put("HOS_HOST", "Hostname");
        translationMap.put("HOS_LOGIN", "Login");
        translationMap.put("HOS_MAIL_ON_ERROR", "Mail On Error");
        translationMap.put("HOS_MAIL_ON_SUCCESS", "Mail On Success");
        translationMap.put("HOS_MAX_CONNECTIONS", "Maximum Number of Connections");
        translationMap.put("HOS_NAME", "Id");
        translationMap.put("HOS_NETWORK_CODE", "Network Code");
        translationMap.put("HOS_NETWORK_NAME", "Network Name");
        translationMap.put("HOS_NICKNAME", "Nickname");
        translationMap.put("HOS_NOTIFY_ONCE", "Notify Once");
        translationMap.put("HOS_PASSWD", "Password(*)"); // Don't show value
        translationMap.put("HOS_RETRY_COUNT", "Retry Count");
        translationMap.put("HOS_RETRY_FREQUENCY", "Retry Frequency");
        translationMap.put("HOS_USER_MAIL", "User Mail");
        translationMap.put("TME_NAME", "Transfer Module");
        translationMap.put("HLO_ID", "Location Identifier");
        translationMap.put("HOU_ID", "Output Identifier");
        translationMap.put("HST_ID", "Stats Identifier");
        translationMap.put("HOS_FILTER_NAME", "Filter Name");
        translationMap.put("HOS_AUTOMATIC_LOCATION", "Automatic Location");
    }

    /**
     * Translate.
     *
     * @param key
     *            the key
     *
     * @return the string
     */
    private static String translate(final String key) {
        return translationMap.getOrDefault(key, "");
    }

    /** The change log. */
    private final ecmwf.common.database.ChangeLog changeLog;

    /** The current object. */
    private final String currentObject;

    /**
     * Gets the bean interface name.
     *
     * @return the bean interface name
     */
    @Override
    public String getBeanInterfaceName() {
        return ChangeLog.class.getName();
    }

    /**
     * Instantiates a new change log bean.
     *
     * @param currentObject
     *            the current object
     * @param changeLog
     *            the change log
     */
    protected ChangeLogBean(final String currentObject, final ecmwf.common.database.ChangeLog changeLog) {
        this.currentObject = currentObject;
        this.changeLog = changeLog;
    }

    /**
     * Gets the change log id.
     *
     * @return the change log id
     */
    @Override
    public long getChangeLogId() {
        return changeLog.getId();
    }

    /**
     * Gets the key name.
     *
     * @return the key name
     */
    @Override
    public String getKeyName() {
        return changeLog.getKeyName();
    }

    /**
     * Gets the key value.
     *
     * @return the key value
     */
    @Override
    public String getKeyValue() {
        return changeLog.getKeyValue();
    }

    /**
     * Gets the web user id.
     *
     * @return the web user id
     */
    @Override
    public String getWebUserId() {
        return changeLog.getWebUserId();
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    @Override
    public Date getDate() {
        return changeLog.getTime();
    }

    /**
     * Gets the old object.
     *
     * @return the old object
     */
    @Override
    public String getOldObject() {
        return changeLog.getOldObject();
    }

    /**
     * Gets the new object.
     *
     * @return the new object
     */
    @Override
    public String getNewObject() {
        return changeLog.getNewObject();
    }

    /**
     * Gets the differences.
     *
     * @return the differences
     */
    public String getDifferences() {
        return process(getDifferences(getOldObject(), getNewObject()));
    }

    /**
     * Gets the differences from current.
     *
     * @return the differences from current
     */
    public String getDifferencesFromCurrent() {
        return process(getDifferences(getNewObject(), currentObject));
    }

    /**
     * Gets the differences.
     *
     * @param from
     *            the from
     * @param to
     *            the to
     *
     * @return the differences
     */
    private static String getDifferences(final String from, final String to) {
        final var result = new StringBuilder();
        for (final DiffRow row : DiffRowGenerator.create().showInlineDiffs(true).mergeOriginalRevised(true)
                .inlineDiffByWord(true).oldTag((tag, f) -> f ? "<font color='red'><s>" : "</s></font>")
                .newTag((tag, f) -> f ? "<font color='green'>" : "</font>").build()
                .generateDiffRows(Arrays.asList(from.split("\n")), Arrays.asList(to.split("\n")))) {
            if (!result.isEmpty()) {
                result.append("\n");
            }
            result.append(row.getOldLine());
        }
        return result.toString()
                .replace(DataBaseObject.TAG_ACROSS_MULTIPLE_LINES,
                        HTML_MULTIPLE_LINES + "<font style='background-color:lightyellow;'>")
                .replace(DataBaseObject.TAG_END_OF_LINES, "");
    }

    /**
     * Process.
     *
     * @param differences
     *            the differences
     *
     * @return the string
     */
    private static String process(final String differences) {
        final var result = new StringBuilder();
        try (var scanner = new Scanner(differences)) {
            String paramName = null;
            final var paramValue = new StringBuilder();
            while (scanner.hasNextLine()) {
                final var line = scanner.nextLine();
                final var index = line.indexOf("]");
                if (line.startsWith("[") && index > 1) {
                    // If line starts with '[', it's a parameter name
                    if (paramName != null) {
                        addParameter(result, paramName, paramValue.toString());
                    }
                    paramName = line.substring(1, index);
                    paramValue.setLength(0); // Clear StringBuilder for new value
                    // If line contains a value, add it to the result immediately
                    final var valueStartIndex = index + 1;
                    if (valueStartIndex < line.length()) {
                        addParameter(result, paramName, line.substring(valueStartIndex));
                        paramName = null; // Reset paramName to handle multi-line values
                    }
                } else if (paramName != null) {
                    // If not, it's part of the parameter value
                    paramValue.append(line).append("\n");
                }
            }
            // Add the last parameter
            if (paramName != null) {
                addParameter(result, paramName, paramValue.toString());
            }
        }
        return result.toString().trim();
    }

    /**
     * Adds the parameter. Helper method to add parameter only if some changes have been detected.
     *
     * @param result
     *            the result
     * @param paramName
     *            the param name
     * @param paramValue
     *            the param value
     */
    private static void addParameter(final StringBuilder result, final String paramName, final String paramValue) {
        final var displayName = translate(paramName);
        final var invisible = displayName.endsWith("(*)");
        if (!displayName.isEmpty() && (paramValue.indexOf("<font color='red'><s>") != -1
                || paramValue.indexOf("<font color='green'>") != -1) && paramValue.indexOf("</font>") != -1) {
            result.append("<b>").append(invisible ? displayName.substring(0, displayName.length() - 3) : displayName)
                    .append(":</b> ").append(invisible ? PARAMETER_MODIFIED : paramValue.trim()).append("</font>\n");
        }
    }
}