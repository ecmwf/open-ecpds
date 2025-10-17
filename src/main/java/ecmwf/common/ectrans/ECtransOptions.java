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

package ecmwf.common.ectrans;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransConstants.BOOLEAN_NONE;
import static ecmwf.common.ectrans.ECtransConstants.BYTE_SIZE_NONE;
import static ecmwf.common.ectrans.ECtransConstants.DEFAULT;
import static ecmwf.common.ectrans.ECtransConstants.DURATION_NONE;
import static ecmwf.common.ectrans.ECtransConstants.INTEGER_NONE;
import static ecmwf.common.ectrans.ECtransConstants.STRING_NONE;
import static ecmwf.common.ectrans.ECtransConstants.TIME_RANGE_NONE;
import static ecmwf.common.technical.StreamPlugThread.DEFAULT_BUFF_SIZE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import ecmwf.common.technical.ByteSize;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.TimeRange;

/**
 * The Enum ECtransOptions.
 */
public enum ECtransOptions {

    /** The destination alias pattern. */
    DESTINATION_ALIAS_PATTERN("pattern", String.class, "recursive=no;delay=0;pattern=.*"),

    /** The destination incoming standby. */
    DESTINATION_INCOMING_STANDBY("standby", Boolean.class, false),

    /** The destination incoming priority. */
    DESTINATION_INCOMING_PRIORITY("priority", Integer.class, 99),

    /** The destination incoming event. */
    DESTINATION_INCOMING_EVENT("event", Boolean.class, false),

    /** The destination incoming date format. */
    DESTINATION_INCOMING_DATE_FORMAT("dateformat", String.class, "yyyyMMdd"),

    /** The destination incoming version. */
    DESTINATION_INCOMING_VERSION("version", String.class, STRING_NONE),

    /** The destination incoming tmp. */
    DESTINATION_INCOMING_TMP("tmp", String.class, STRING_NONE),

    /** The destination incoming delay. */
    DESTINATION_INCOMING_DELAY("delay", Duration.class, DURATION_NONE),

    /** The destination incoming lifetime. */
    DESTINATION_INCOMING_LIFETIME("lifetime", Duration.class, DURATION_NONE),

    /** The destination incoming fail on metadata parsing error. */
    DESTINATION_INCOMING_FAIL_ON_METADATA_PARSING_ERROR("failOnMetadataParsingError", Boolean.class, false),

    /** The destination incoming metadata. */
    DESTINATION_INCOMING_METADATA("metadata", String.class, STRING_NONE),

    /** The destination incoming root dir. */
    DESTINATION_INCOMING_ROOT_DIR("rootdir", String.class, STRING_NONE),

    /** The destination incoming sort. */
    DESTINATION_INCOMING_SORT("sort", String.class, STRING_NONE, Arrays.asList("size", "target", "time")),

    /** The destination incoming order. */
    DESTINATION_INCOMING_ORDER("order", String.class, STRING_NONE, Arrays.asList("asc", "desc")),

    /** The destination incoming max bytes per sec for input. */
    DESTINATION_INCOMING_MAX_BYTES_PER_SEC_FOR_INPUT("maxBytesPerSecForInput", ByteSize.class, BYTE_SIZE_NONE),

    /** The destination incoming max bytes per sec for output. */
    DESTINATION_INCOMING_MAX_BYTES_PER_SEC_FOR_OUTPUT("maxBytesPerSecForOutput", ByteSize.class, BYTE_SIZE_NONE),

    /** The destination mqtt publish. */
    DESTINATION_MQTT_PUBLISH("publish", Boolean.class, false),

    /** The destination mqtt topic. */
    DESTINATION_MQTT_TOPIC("topic", String.class, STRING_NONE),

    /** The destination mqtt qos. */
    DESTINATION_MQTT_QOS("qos", Integer.class, 1, Arrays.asList(0, 1, 2)),

    /** The destination mqtt expiry interval. */
    DESTINATION_MQTT_EXPIRY_INTERVAL("expiryInterval", Duration.class, DURATION_NONE),

    /** The destination mqtt content type. */
    DESTINATION_MQTT_CONTENT_TYPE("contentType", String.class, "text/plain"),

    /** The destination mqtt client id. */
    DESTINATION_MQTT_CLIENT_ID("clientId", String.class, STRING_NONE),

    /** The destination mqtt retain. */
    DESTINATION_MQTT_RETAIN("retain", Boolean.class, false),

    /** The destination mqtt payload. */
    DESTINATION_MQTT_PAYLOAD("payload", String.class, STRING_NONE),

    /** The destination scheduler active time range. */
    DESTINATION_SCHEDULER_ACTIVE_TIME_RANGE("activeTimeRange", TimeRange.class, TIME_RANGE_NONE),

    /** The destination scheduler date format. */
    DESTINATION_SCHEDULER_DATE_FORMAT("dateformat", String.class, "yyyyMMdd"),

    /** The destination scheduler standby. */
    DESTINATION_SCHEDULER_STANDBY("standby", String.class, "", Arrays.asList("yes", "no", "never")),

    /** The destination scheduler force stop. */
    DESTINATION_SCHEDULER_FORCE_STOP("forceStop", Boolean.class, true),

    /** The destination scheduler reset queue on change. */
    DESTINATION_SCHEDULER_RESET_QUEUE_ON_CHANGE("resetQueueOnChange", Boolean.class, true),

    /** The destination scheduler master to notify on done. */
    DESTINATION_SCHEDULER_MASTER_TO_NOTIFY_ON_DONE("masterToNotifyOnDone", String.class, STRING_NONE),

    /** The destination scheduler lifetime. */
    DESTINATION_SCHEDULER_LIFETIME("lifetime", Duration.class, DURATION_NONE),

    /** The destination scheduler delay. */
    DESTINATION_SCHEDULER_DELAY("delay", Duration.class, DURATION_NONE),

    /** The destination scheduler no retrieval. */
    DESTINATION_SCHEDULER_NO_RETRIEVAL("noRetrieval", Boolean.class, BOOLEAN_NONE),

    /** The destination scheduler asap. */
    DESTINATION_SCHEDULER_ASAP("asap", Boolean.class, BOOLEAN_NONE),

    /** The destination scheduler transfergroup. */
    DESTINATION_SCHEDULER_TRANSFERGROUP("transfergroup", String.class, STRING_NONE),

    /** The destination scheduler version. */
    DESTINATION_SCHEDULER_VERSION("version", String.class, STRING_NONE),

    /** The destination scheduler requeueon. */
    DESTINATION_SCHEDULER_REQUEUEON("requeueon", String.class, STRING_NONE),

    /** The destination scheduler force. */
    DESTINATION_SCHEDULER_FORCE("force", String.class, STRING_NONE),

    /** The destination scheduler requeuepattern. */
    DESTINATION_SCHEDULER_REQUEUEPATTERN("requeuepattern", String.class, ".*"),

    /** The destination scheduler requeueignore. */
    DESTINATION_SCHEDULER_REQUEUEIGNORE("requeueignore", String.class, STRING_NONE),

    /** The host ectrans connect time out. */
    HOST_ECTRANS_CONNECT_TIME_OUT("connectTimeOut", Duration.class, Duration.ofMinutes(1)),

    /** The host ectrans close time out. */
    HOST_ECTRANS_CLOSE_TIME_OUT("closeTimeOut", Duration.class, Duration.ofMinutes(1)),

    /** The host ectrans close asynchronous. */
    HOST_ECTRANS_CLOSE_ASYNCHRONOUS("closeAsynchronous", Boolean.class, true),

    /** The host ectrans retry count. */
    HOST_ECTRANS_RETRY_COUNT("retryCount", Integer.class, 1),

    /** The host ectrans retry frequency. */
    HOST_ECTRANS_RETRY_FREQUENCY("retryFrequency", Duration.class, Duration.ofSeconds(1)),

    /** The host ectrans debug. */
    HOST_ECTRANS_DEBUG("debug", Boolean.class, false),

    /** The host ectrans del time out. */
    HOST_ECTRANS_DEL_TIME_OUT("delTimeOut", Duration.class, Duration.ofSeconds(90)),

    /** The host ectrans get time out. */
    HOST_ECTRANS_GET_TIME_OUT("getTimeOut", Duration.class, DURATION_NONE),

    /** The host ectrans list time out. */
    HOST_ECTRANS_LIST_TIME_OUT("listTimeOut", Duration.class, Duration.ofSeconds(90)),

    /** The host ectrans mkdir time out. */
    HOST_ECTRANS_MKDIR_TIME_OUT("mkdirTimeOut", Duration.class, Duration.ofSeconds(90)),

    /** The host ectrans move time out. */
    HOST_ECTRANS_MOVE_TIME_OUT("moveTimeOut", Duration.class, Duration.ofSeconds(90)),

    /** The host ectrans usemget. */
    HOST_ECTRANS_USEMGET("usemget", Boolean.class, false),

    /** The host ectrans put time out. */
    HOST_ECTRANS_PUT_TIME_OUT("putTimeOut", Duration.class, DURATION_NONE),

    /** The host ectrans rmdir time out. */
    HOST_ECTRANS_RMDIR_TIME_OUT("rmdirTimeOut", Duration.class, Duration.ofSeconds(90)),

    /** The host ectrans size time out. */
    HOST_ECTRANS_SIZE_TIME_OUT("sizeTimeOut", Duration.class, Duration.ofSeconds(90)),

    /** The host ectrans get handler. */
    HOST_ECTRANS_GET_HANDLER("getHandler", Boolean.class, false),

    /** The host ectrans get handler cmd. */
    HOST_ECTRANS_GET_HANDLER_CMD("getHandlerCmd", String.class, STRING_NONE),

    /** The host ectrans get handler ack. */
    HOST_ECTRANS_GET_HANDLER_ACK("getHandlerAck", String.class, STRING_NONE),

    /** The host ectrans get handler exit code. */
    HOST_ECTRANS_GET_HANDLER_EXIT_CODE("getHandlerExitCode", Integer.class, 0),

    /** The host ectrans initial input filter. */
    HOST_ECTRANS_INITIAL_INPUT_FILTER("initialInputFilter", String.class, STRING_NONE),

    /** The host ectrans filter input stream. */
    HOST_ECTRANS_FILTER_INPUT_STREAM("filterInputStream", String.class, STRING_NONE),

    /** The host ectrans plug buff size. */
    HOST_ECTRANS_PLUG_BUFF_SIZE("plugBuffSize", ByteSize.class, ByteSize.of(DEFAULT_BUFF_SIZE)),

    /** The host ectrans plug do flush. */
    HOST_ECTRANS_PLUG_DO_FLUSH("plugDoFlush", Boolean.class, false),

    /** The host ectrans plug read fully. */
    HOST_ECTRANS_PLUG_READ_FULLY("plugReadFully", Boolean.class, false),

    /** The host ectrans initial input size. */
    HOST_ECTRANS_INITIAL_INPUT_SIZE("initialInputSize", Long.class, -1L),

    /** The host ectrans initial input md5. */
    HOST_ECTRANS_INITIAL_INPUT_MD5("initialInputMd5", String.class, STRING_NONE),

    /** The host ectrans create checksum. */
    HOST_ECTRANS_CREATE_CHECKSUM("createChecksum", Boolean.class, false),

    /** The host ectrans support filter. */
    HOST_ECTRANS_SUPPORT_FILTER("supportFilter", Boolean.class, false),

    /** The host ectrans put monitored input delta. */
    HOST_ECTRANS_PUT_MONITORED_INPUT_DELTA("putMonitoredInputDelta", Duration.class, DURATION_NONE),

    /** The host ectrans buff input size. */
    HOST_ECTRANS_BUFF_INPUT_SIZE("buffInputSize", ByteSize.class, BYTE_SIZE_NONE),

    /** The host ectrans buff output size. */
    HOST_ECTRANS_BUFF_OUTPUT_SIZE("buffOutputSize", ByteSize.class, BYTE_SIZE_NONE),

    /** The host ectrans put handler. */
    HOST_ECTRANS_PUT_HANDLER("putHandler", Boolean.class, false),

    /** The host ectrans put handler cmd. */
    HOST_ECTRANS_PUT_HANDLER_CMD("putHandlerCmd", String.class, STRING_NONE),

    /** The host ectrans put handler ack. */
    HOST_ECTRANS_PUT_HANDLER_ACK("putHandlerAck", String.class, STRING_NONE),

    /** The host ectrans put handler exit code. */
    HOST_ECTRANS_PUT_HANDLER_EXIT_CODE("putHandlerExitCode", Integer.class, 0),

    /** The host ectrans usednsname. */
    HOST_ECTRANS_USEDNSNAME("usednsname", Boolean.class, false),

    /** The host ectrans checkfiltersize. */
    HOST_ECTRANS_CHECKFILTERSIZE("checkfiltersize", Boolean.class, true),

    /** The host ectrans lastupdate. */
    HOST_ECTRANS_LASTUPDATE("lastupdate", Long.class, -1L),

    /** The host ectrans notify pre. */
    HOST_ECTRANS_NOTIFY_PRE("notifyPre", String.class, ""),

    /** The host ectrans notify auth. */
    HOST_ECTRANS_NOTIFY_AUTH("notifyAuth", String.class, ""),

    /** The host ectrans notify post. */
    HOST_ECTRANS_NOTIFY_POST("notifyPost", String.class, ""),

    /** The host ectrans notify publish. */
    HOST_ECTRANS_NOTIFY_PUBLISH("notifyPublish", String.class, ""),

    /** The host ectrans location. */
    HOST_ECTRANS_LOCATION("location", String.class, ""),

    /** The host ectrans host selector. */
    HOST_ECTRANS_HOST_SELECTOR("hostSelector", String.class, STRING_NONE),

    /** The host ectrans multiple input stream. */
    HOST_ECTRANS_MULTIPLE_INPUT_STREAM("multipleInputStream", String.class, STRING_NONE),

    /** The host ectrans stream timeout. */
    HOST_ECTRANS_STREAM_TIMEOUT("streamTimeout", Duration.class, Duration.ofMinutes(5)),

    /** The host ectrans filter minimum size. */
    HOST_ECTRANS_FILTER_MINIMUM_SIZE("filterMinimumSize", ByteSize.class, ByteSize.of(0)),

    /** The host ectrans filterpattern. */
    HOST_ECTRANS_FILTERPATTERN("filterpattern", String.class, ".*"),

    /** The host ectrans socket statistics. */
    HOST_ECTRANS_SOCKET_STATISTICS("socketStatistics", Boolean.class, true),

    /** The host ectrans tcp congestion control. */
    HOST_ECTRANS_TCP_CONGESTION_CONTROL("tcpCongestionControl", String.class, STRING_NONE),

    /** The host ectrans tcp max segment. */
    HOST_ECTRANS_TCP_MAX_SEGMENT("tcpMaxSegment", Integer.class, INTEGER_NONE),

    /** The host ectrans so max pacing rate. */
    HOST_ECTRANS_SO_MAX_PACING_RATE("soMaxPacingRate", ByteSize.class, BYTE_SIZE_NONE),

    /** The host ectrans tcp time stamp. */
    HOST_ECTRANS_TCP_TIME_STAMP("tcpTimeStamp", Boolean.class, BOOLEAN_NONE),

    /** The host ectrans tcp window clamp. */
    HOST_ECTRANS_TCP_WINDOW_CLAMP("tcpWindowClamp", Integer.class, INTEGER_NONE),

    /** The host ectrans tcp no delay. */
    HOST_ECTRANS_TCP_NO_DELAY("tcpNoDelay", Boolean.class, BOOLEAN_NONE),

    /** The host ectrans tcp keep alive. */
    HOST_ECTRANS_TCP_KEEP_ALIVE("tcpKeepAlive", Boolean.class, BOOLEAN_NONE),

    /** The host ectrans tcp keep alive time. */
    HOST_ECTRANS_TCP_KEEP_ALIVE_TIME("tcpKeepAliveTime", Integer.class, INTEGER_NONE),

    /** The host ectrans tcp keep alive interval. */
    HOST_ECTRANS_TCP_KEEP_ALIVE_INTERVAL("tcpKeepAliveInterval", Integer.class, INTEGER_NONE),

    /** The host ectrans tcp keep alive probes. */
    HOST_ECTRANS_TCP_KEEP_ALIVE_PROBES("tcpKeepAliveProbes", Integer.class, INTEGER_NONE),

    /** The host ectrans tcp linger enable. */
    HOST_ECTRANS_TCP_LINGER_ENABLE("tcpLingerEnable", Boolean.class, BOOLEAN_NONE),

    /** The host ectrans tcp linger time. */
    HOST_ECTRANS_TCP_LINGER_TIME("tcpLingerTime", Integer.class, INTEGER_NONE),

    /** The host ectrans tcp user timeout. */
    HOST_ECTRANS_TCP_USER_TIMEOUT("tcpUserTimeout", Integer.class, INTEGER_NONE),

    /** The host ectrans tcp quick ack. */
    HOST_ECTRANS_TCP_QUICK_ACK("tcpQuickAck", Boolean.class, BOOLEAN_NONE),

    /** The host acquisition list synchronous. */
    HOST_ACQUISITION_LIST_SYNCHRONOUS("listSynchronous", Boolean.class, true),

    /** The host acquisition list parallel. */
    HOST_ACQUISITION_LIST_PARALLEL("listParallel", Boolean.class, false),

    /** The host acquisition list max waiting. */
    HOST_ACQUISITION_LIST_MAX_WAITING("listMaxWaiting", Integer.class, 100),

    /** The host acquisition list max threads. */
    HOST_ACQUISITION_LIST_MAX_THREADS("listMaxThreads", Integer.class, 100),

    /** The host acquisition regex format. */
    HOST_ACQUISITION_REGEX_FORMAT("regexFormat", String.class, STRING_NONE),

    /** The host acquisition system key. */
    HOST_ACQUISITION_SYSTEM_KEY("systemKey", String.class, "UNIX",
            Arrays.asList("UNIX", "VMS", "WINDOWS", "OS/2", "OS/400", "AS/400", "MVS", "NETWARE", "MACOS PETER")),

    /** The host acquisition default date format. */
    HOST_ACQUISITION_DEFAULT_DATE_FORMAT("defaultDateFormat", String.class, STRING_NONE),

    /** The host acquisition recent date format. */
    HOST_ACQUISITION_RECENT_DATE_FORMAT("recentDateFormat", String.class, STRING_NONE),

    /** The host acquisition server language code. */
    HOST_ACQUISITION_SERVER_LANGUAGE_CODE("serverLanguageCode", String.class, "en"),

    /** The host acquisition short month names. */
    HOST_ACQUISITION_SHORT_MONTH_NAMES("shortMonthNames", String.class, STRING_NONE),

    /** The host acquisition server time zone id. */
    HOST_ACQUISITION_SERVER_TIME_ZONE_ID("serverTimeZoneId", String.class, STRING_NONE),

    /** The host acquisition fileage. */
    HOST_ACQUISITION_FILEAGE("fileage", String.class, STRING_NONE),

    /** The host acquisition filesize. */
    HOST_ACQUISITION_FILESIZE("filesize", String.class, STRING_NONE),

    /** The host acquisition dateformat. */
    HOST_ACQUISITION_DATEFORMAT("dateformat", String.class, "yyyyMMdd"),

    /** The host acquisition datedelta. */
    HOST_ACQUISITION_DATEDELTA("datedelta", Duration.class, Duration.ZERO),

    /** The host acquisition datepattern. */
    HOST_ACQUISITION_DATEPATTERN("datepattern", String.class, STRING_NONE),

    /** The host acquisition datesource. */
    HOST_ACQUISITION_DATESOURCE("datesource", String.class, STRING_NONE),

    /** The host acquisition use symlink. */
    HOST_ACQUISITION_USE_SYMLINK("useSymlink", Boolean.class, false),

    /** The host acquisition only valid time. */
    HOST_ACQUISITION_ONLY_VALID_TIME("onlyValidTime", Boolean.class, false),

    /** The host acquisition remove parameters. */
    HOST_ACQUISITION_REMOVE_PARAMETERS("removeParameters", Boolean.class, false),

    /** The host acquisition action. */
    HOST_ACQUISITION_ACTION("action", String.class, "queue", Arrays.asList("queue", "delete")),

    /** The host acquisition unique by target only. */
    HOST_ACQUISITION_UNIQUE_BY_TARGET_ONLY("uniqueByTargetOnly", Boolean.class, false),

    /** The host acquisition use target as unique name. */
    HOST_ACQUISITION_USE_TARGET_AS_UNIQUE_NAME("useTargetAsUniqueName", Boolean.class, false),

    /** The host acquisition unique by name and time. */
    HOST_ACQUISITION_UNIQUE_BY_NAME_AND_TIME("uniqueByNameAndTime", Boolean.class, false),

    /** The host acquisition standby. */
    HOST_ACQUISITION_STANDBY("standby", Boolean.class, false),

    /** The host acquisition requeueonupdate. */
    HOST_ACQUISITION_REQUEUEONUPDATE("requeueonupdate", Boolean.class, false),

    /** The host acquisition requeueonsamesize. */
    HOST_ACQUISITION_REQUEUEONSAMESIZE("requeueonsamesize", Boolean.class, false),

    /** The host acquisition requeueon. */
    HOST_ACQUISITION_REQUEUEON("requeueon", String.class, STRING_NONE),

    /** The host acquisition metadata. */
    HOST_ACQUISITION_METADATA("metadata", String.class, ""),

    /** The host acquisition target. */
    HOST_ACQUISITION_TARGET("target", String.class, "$target"),

    /** The host acquisition event. */
    HOST_ACQUISITION_EVENT("event", Boolean.class, false),

    /** The host acquisition lifetime. */
    HOST_ACQUISITION_LIFETIME("lifetime", Duration.class, Duration.ofDays(2)),

    /** The host acquisition groupby. */
    HOST_ACQUISITION_GROUPBY("groupby", String.class, STRING_NONE),

    /** The host acquisition noretrieval. */
    HOST_ACQUISITION_NORETRIEVAL("noretrieval", Boolean.class, false),

    /** The host acquisition deleteoriginal. */
    HOST_ACQUISITION_DELETEORIGINAL("deleteoriginal", Boolean.class, false),

    /** The host acquisition transfergroup. */
    HOST_ACQUISITION_TRANSFERGROUP("transferGroup", String.class, STRING_NONE),

    /** The host acquisition debug. */
    HOST_ACQUISITION_DEBUG("debug", Boolean.class, false),

    /** The host acquisition payload extension. */
    HOST_ACQUISITION_PAYLOAD_EXTENSION("payloadExtension", String.class, ".payload"),

    /** The host acquisition regex pattern. */
    HOST_ACQUISITION_REGEX_PATTERN("regexPattern", String.class, ""),

    /** The host acquisition wildcard filter. */
    HOST_ACQUISITION_WILDCARD_FILTER("wildcardFilter", String.class, ""),

    /** The host acquisition priority. */
    HOST_ACQUISITION_PRIORITY("priority", Integer.class, 99),

    /** The host acquisition maximum duration. */
    HOST_ACQUISITION_MAXIMUM_DURATION("maximumDuration", Duration.class, DURATION_NONE),

    /** The host acquisition interrupt slow. */
    HOST_ACQUISITION_INTERRUPT_SLOW("interruptSlow", Boolean.class, BOOLEAN_NONE),

    /** The host acquisition requeue on failure. */
    HOST_ACQUISITION_REQUEUE_ON_FAILURE("requeueOnFailure", Boolean.class, false),

    /** The host proxy http proxy url. */
    HOST_PROXY_HTTP_PROXY_URL("httpProxyUrl", String.class, STRING_NONE),

    /** The host proxy http mover url. */
    HOST_PROXY_HTTP_MOVER_URL("httpMoverUrl", String.class, STRING_NONE),

    /** The host proxy modulo. */
    HOST_PROXY_MODULO("modulo", Integer.class, 3),

    /** The host proxy timeout. */
    HOST_PROXY_TIMEOUT("timeout", Duration.class, Duration.ofSeconds(10)),

    /** The host proxy use destination filter. */
    HOST_PROXY_USE_DESTINATION_FILTER("useDestinationFilter", Boolean.class, false),

    /** The host retrieval interrupt slow. */
    HOST_RETRIEVAL_INTERRUPT_SLOW("interruptSlow", Boolean.class, false),

    /** The host retrieval maximum duration. */
    HOST_RETRIEVAL_MAXIMUM_DURATION("maximumDuration", Duration.class, Duration.ofMinutes(10)),

    /** The host retrieval minimum duration. */
    HOST_RETRIEVAL_MINIMUM_DURATION("minimumDuration", Duration.class, Duration.ofMinutes(1)),

    /** The host retrieval minimum rate. */
    HOST_RETRIEVAL_MINIMUM_RATE("minimumRate", ByteSize.class, ByteSize.of(2359296)),

    /** The host retrieval rate throttling. */
    HOST_RETRIEVAL_RATE_THROTTLING("rateThrottling", ByteSize.class, BYTE_SIZE_NONE),

    /** The host upload interrupt slow. */
    HOST_UPLOAD_INTERRUPT_SLOW("interruptSlow", Boolean.class, false),

    /** The host upload maximum duration. */
    HOST_UPLOAD_MAXIMUM_DURATION("maximumDuration", Duration.class, Duration.ofMinutes(10)),

    /** The host upload minimum duration. */
    HOST_UPLOAD_MINIMUM_DURATION("minimumDuration", Duration.class, Duration.ofMinutes(1)),

    /** The host upload minimum rate. */
    HOST_UPLOAD_MINIMUM_RATE("minimumRate", ByteSize.class, ByteSize.of(2359296)),

    /** The host upload rate throttling. */
    HOST_UPLOAD_RATE_THROTTLING("rateThrottling", ByteSize.class, BYTE_SIZE_NONE),

    /** The host ecpds mover list for source. */
    HOST_ECPDS_MOVER_LIST_FOR_SOURCE("moverListForSource", String.class, ""),

    /** The host ecpds mover list for backup. */
    HOST_ECPDS_MOVER_LIST_FOR_BACKUP("moverListForBackup", String.class, ""),

    /** The host ecpds mover list for processing. */
    HOST_ECPDS_MOVER_LIST_FOR_PROCESSING("moverListForProcessing", String.class, ""),

    /** The host test bytes per sec. */
    HOST_TEST_BYTES_PER_SEC("bytesPerSec", ByteSize.class, ByteSize.ofMB(10)),

    /** The host test delay. */
    HOST_TEST_DELAY("delay", Duration.class, Duration.ofMillis(500)),

    /** The host test errors frequency. */
    HOST_TEST_ERRORS_FREQUENCY("errorsFrequency", Integer.class, 1000),

    /** The host exec return code. */
    HOST_EXEC_RETURN_CODE("returnCode", Integer.class, 0),

    /** The host ftp port. */
    HOST_FTP_PORT("port", Integer.class, 21),

    /** The host ftp login. */
    HOST_FTP_LOGIN("login", String.class, STRING_NONE),

    /** The host ftp password. */
    HOST_FTP_PASSWORD("password", String.class, STRING_NONE),

    /** The host ftp usecleanpath. */
    HOST_FTP_USECLEANPATH("usecleanpath", Boolean.class, false),

    /** The host ftp usenlist. */
    HOST_FTP_USENLIST("usenlist", Boolean.class, false),

    /** The host ftp usetmp. */
    HOST_FTP_USETMP("usetmp", Boolean.class, true),

    /** The host ftp mkdirs. */
    HOST_FTP_MKDIRS("mkdirs", String.class, "yes", Arrays.asList("yes", "no", "remote")),

    /** The host ftp prefix. */
    HOST_FTP_PREFIX("prefix", String.class, ""),

    /** The host ftp suffix. */
    HOST_FTP_SUFFIX("suffix", String.class, ""),

    /** The host ftp md5 ext. */
    HOST_FTP_MD5_EXT("md5Ext", String.class, ".md5"),

    /** The host ftp post connect cmd. */
    HOST_FTP_POST_CONNECT_CMD("postConnectCmd", String.class, ""),

    /** The host ftp pre close cmd. */
    HOST_FTP_PRE_CLOSE_CMD("preCloseCmd", String.class, ""),

    /** The host ftp pre get cmd. */
    HOST_FTP_PRE_GET_CMD("preGetCmd", String.class, ""),

    /** The host ftp post get cmd. */
    HOST_FTP_POST_GET_CMD("postGetCmd", String.class, ""),

    /** The host ftp pre put cmd. */
    HOST_FTP_PRE_PUT_CMD("prePutCmd", String.class, ""),

    /** The host ftp post put cmd. */
    HOST_FTP_POST_PUT_CMD("postPutCmd", String.class, ""),

    /** The host ftp ignore mkdirs cmd errors. */
    HOST_FTP_IGNORE_MKDIRS_CMD_ERRORS("ignoreMkdirsCmdErrors", Boolean.class, false),

    /** The host ftp mkdirs cmd index. */
    HOST_FTP_MKDIRS_CMD_INDEX("mkdirsCmdIndex", Integer.class, 0),

    /** The host ftp post mkdirs cmd. */
    HOST_FTP_POST_MKDIRS_CMD("postMkdirsCmd", String.class, ""),

    /** The host ftp pre mkdirs cmd. */
    HOST_FTP_PRE_MKDIRS_CMD("preMkdirsCmd", String.class, ""),

    /** The host ftp keep alive. */
    HOST_FTP_KEEP_ALIVE("keepAlive", Duration.class, Duration.ZERO),

    /** The host ftp use noop. */
    HOST_FTP_USE_NOOP("useNoop", Duration.class, Duration.ZERO),

    /** The host ftp ignore check. */
    HOST_FTP_IGNORE_CHECK("ignoreCheck", Boolean.class, true),

    /** The host ftp ignore delete. */
    HOST_FTP_IGNORE_DELETE("ignoreDelete", Boolean.class, true),

    /** The host ftp use append. */
    HOST_FTP_USE_APPEND("useAppend", Boolean.class, false),

    /** The host ftp parallel streams. */
    HOST_FTP_PARALLEL_STREAMS("parallelStreams", Integer.class, 0),

    /** The host ftp delete on rename. */
    HOST_FTP_DELETE_ON_RENAME("deleteOnRename", Boolean.class, true),

    /** The host ftp retry after timeout on check. */
    HOST_FTP_RETRY_AFTER_TIMEOUT_ON_CHECK("retryAfterTimeoutOnCheck", Boolean.class, false),

    /** The host ftp keep control connection alive. */
    HOST_FTP_KEEP_CONTROL_CONNECTION_ALIVE("keepControlConnectionAlive", Boolean.class, false),

    /** The host ftp mksuffix. */
    HOST_FTP_MKSUFFIX("mksuffix", Boolean.class, false),

    /** The host ftp usesuffix. */
    HOST_FTP_USESUFFIX("usesuffix", Boolean.class, false),

    /** The host ftp cwd. */
    HOST_FTP_CWD("cwd", String.class, STRING_NONE),

    /** The host ftp nopassword. */
    HOST_FTP_NOPASSWORD("nopassword", Boolean.class, false),

    /** The host ftp passive. */
    HOST_FTP_PASSIVE("passive", String.class, "no", Arrays.asList("no", "yes", "shared")),

    /** The host ftp extended. */
    HOST_FTP_EXTENDED("extended", Boolean.class, false),

    /** The host ftp data alive. */
    HOST_FTP_DATA_ALIVE("dataAlive", Boolean.class, false),

    /** The host ftp low port. */
    HOST_FTP_LOW_PORT("lowPort", Boolean.class, false),

    /** The host ftp comm time out. */
    HOST_FTP_COMM_TIME_OUT("commTimeOut", Duration.class, Duration.ofMinutes(1)),

    /** The host ftp data time out. */
    HOST_FTP_DATA_TIME_OUT("dataTimeOut", Duration.class, Duration.ofMinutes(1)),

    /** The host ftp port time out. */
    HOST_FTP_PORT_TIME_OUT("portTimeOut", Duration.class, Duration.ofMinutes(1)),

    /** The host ftp listen address. */
    HOST_FTP_LISTEN_ADDRESS("listenAddress", String.class, STRING_NONE),

    /** The host ftp send buff size. */
    HOST_FTP_SEND_BUFF_SIZE("sendBuffSize", ByteSize.class, BYTE_SIZE_NONE),

    /** The host ftp receive buff size. */
    HOST_FTP_RECEIVE_BUFF_SIZE("receiveBuffSize", ByteSize.class, BYTE_SIZE_NONE),

    /** The host ftp set noop. */
    HOST_FTP_SET_NOOP("setNoop", String.class, STRING_NONE),

    /** The host ftp like. */
    HOST_FTP_LIKE("ftpLike", Boolean.class, true),

    /** The host ftp ftpuser. */
    HOST_FTP_FTPUSER("ftpuser", String.class, STRING_NONE),

    /** The host ftp ftpgroup. */
    HOST_FTP_FTPGROUP("ftpgroup", String.class, STRING_NONE),

    /** The host ftps port. */
    HOST_FTPS_PORT("port", Integer.class, 21),

    /** The host ftps login. */
    HOST_FTPS_LOGIN("login", String.class, ""),

    /** The host ftps password. */
    HOST_FTPS_PASSWORD("password", String.class, ""),

    /** The host ftps usecleanpath. */
    HOST_FTPS_USECLEANPATH("usecleanpath", Boolean.class, false),

    /** The host ftps usetmp. */
    HOST_FTPS_USETMP("usetmp", Boolean.class, true),

    /** The host ftps mkdirs. */
    HOST_FTPS_MKDIRS("mkdirs", Boolean.class, true),

    /** The host ftps prefix. */
    HOST_FTPS_PREFIX("prefix", String.class, ""),

    /** The host ftps suffix. */
    HOST_FTPS_SUFFIX("suffix", String.class, ""),

    /** The host ftps md5 ext. */
    HOST_FTPS_MD5_EXT("md5Ext", String.class, ".md5"),

    /** The host ftps post connect cmd. */
    HOST_FTPS_POST_CONNECT_CMD("postConnectCmd", String.class, ""),

    /** The host ftps pre close cmd. */
    HOST_FTPS_PRE_CLOSE_CMD("preCloseCmd", String.class, ""),

    /** The host ftps pre get cmd. */
    HOST_FTPS_PRE_GET_CMD("preGetCmd", String.class, ""),

    /** The host ftps post get cmd. */
    HOST_FTPS_POST_GET_CMD("postGetCmd", String.class, ""),

    /** The host ftps pre put cmd. */
    HOST_FTPS_PRE_PUT_CMD("prePutCmd", String.class, ""),

    /** The host ftps post put cmd. */
    HOST_FTPS_POST_PUT_CMD("postPutCmd", String.class, ""),

    /** The host ftps ignore mkdirs cmd errors. */
    HOST_FTPS_IGNORE_MKDIRS_CMD_ERRORS("ignoreMkdirsCmdErrors", Boolean.class, false),

    /** The host ftps mkdirs cmd index. */
    HOST_FTPS_MKDIRS_CMD_INDEX("mkdirsCmdIndex", Integer.class, 0),

    /** The host ftps post mkdirs cmd. */
    HOST_FTPS_POST_MKDIRS_CMD("postMkdirsCmd", String.class, ""),

    /** The host ftps pre mkdirs cmd. */
    HOST_FTPS_PRE_MKDIRS_CMD("preMkdirsCmd", String.class, ""),

    /** The host ftps keep alive. */
    HOST_FTPS_KEEP_ALIVE("keepAlive", Duration.class, Duration.ZERO),

    /** The host ftps use noop. */
    HOST_FTPS_USE_NOOP("useNoop", Duration.class, Duration.ZERO),

    /** The host ftps ignore check. */
    HOST_FTPS_IGNORE_CHECK("ignoreCheck", Boolean.class, true),

    /** The host ftps ignore delete. */
    HOST_FTPS_IGNORE_DELETE("ignoreDelete", Boolean.class, true),

    /** The host ftps use append. */
    HOST_FTPS_USE_APPEND("useAppend", Boolean.class, false),

    /** The host ftps delete on rename. */
    HOST_FTPS_DELETE_ON_RENAME("deleteOnRename", Boolean.class, true),

    /** The host ftps mksuffix. */
    HOST_FTPS_MKSUFFIX("mksuffix", Boolean.class, false),

    /** The host ftps usesuffix. */
    HOST_FTPS_USESUFFIX("usesuffix", Boolean.class, false),

    /** The host ftps cwd. */
    HOST_FTPS_CWD("cwd", String.class, STRING_NONE),

    /** The host ftps passive. */
    HOST_FTPS_PASSIVE("passive", Boolean.class, false),

    /** The host ftps connection type. */
    HOST_FTPS_CONNECTION_TYPE("connectionType", String.class, "FTP", Arrays.asList("FTP", "FTPS", "FTPES")),

    /** The host ftps close time out. */
    HOST_FTPS_CLOSE_TIME_OUT("closeTimeOut", Duration.class, Duration.ofMinutes(1)),

    /** The host ftps connection time out. */
    HOST_FTPS_CONNECTION_TIME_OUT("connectionTimeOut", Duration.class, Duration.ofMinutes(1)),

    /** The host ftps read time out. */
    HOST_FTPS_READ_TIME_OUT("readTimeOut", Duration.class, Duration.ofMinutes(1)),

    /** The host ftps listen address. */
    HOST_FTPS_LISTEN_ADDRESS("listenAddress", String.class, STRING_NONE),

    /** The host ftps send buff size. */
    HOST_FTPS_SEND_BUFF_SIZE("sendBuffSize", ByteSize.class, BYTE_SIZE_NONE),

    /** The host ftps receive buff size. */
    HOST_FTPS_RECEIVE_BUFF_SIZE("receiveBuffSize", ByteSize.class, BYTE_SIZE_NONE),

    /** The host ftps strict. */
    HOST_FTPS_STRICT("strict", Boolean.class, false),

    /** The host ftps protocol. */
    HOST_FTPS_PROTOCOL("protocol", String.class, "TLS"),

    /** The host sftp port. */
    HOST_SFTP_PORT("port", Integer.class, 22),

    /** The host sftp password. */
    HOST_SFTP_PASSWORD("password", String.class, STRING_NONE),

    /** The host sftp login. */
    HOST_SFTP_LOGIN("login", String.class, STRING_NONE),

    /** The host sftp allocate. */
    HOST_SFTP_ALLOCATE("allocate", String.class, STRING_NONE),

    /** The host sftp cwd. */
    HOST_SFTP_CWD("cwd", String.class, STRING_NONE),

    /** The host sftp properties. */
    HOST_SFTP_PROPERTIES("properties", String.class, STRING_NONE),

    /** The host sftp usecleanpath. */
    HOST_SFTP_USECLEANPATH("usecleanpath", Boolean.class, false),

    /** The host sftp usetmp. */
    HOST_SFTP_USETMP("usetmp", Boolean.class, true),

    /** The host sftp mkdirs. */
    HOST_SFTP_MKDIRS("mkdirs", Boolean.class, true),

    /** The host sftp prefix. */
    HOST_SFTP_PREFIX("prefix", String.class, ""),

    /** The host sftp suffix. */
    HOST_SFTP_SUFFIX("suffix", String.class, ""),

    /** The host sftp md5 ext. */
    HOST_SFTP_MD5_EXT("md5Ext", String.class, ".md5"),

    /** The host sftp chmod. */
    HOST_SFTP_CHMOD("chmod", String.class, STRING_NONE),

    /** The host sftp ignore check. */
    HOST_SFTP_IGNORE_CHECK("ignoreCheck", Boolean.class, false),

    /** The host sftp ignore mkdirs cmd errors. */
    HOST_SFTP_IGNORE_MKDIRS_CMD_ERRORS("ignoreMkdirsCmdErrors", Boolean.class, false),

    /** The host sftp mkdirs cmd index. */
    HOST_SFTP_MKDIRS_CMD_INDEX("mkdirsCmdIndex", Integer.class, 0),

    /** The host sftp post mkdirs cmd. */
    HOST_SFTP_POST_MKDIRS_CMD("postMkdirsCmd", String.class, ""),

    /** The host sftp pre mkdirs cmd. */
    HOST_SFTP_PRE_MKDIRS_CMD("preMkdirsCmd", String.class, ""),

    /** The host sftp exec code. */
    HOST_SFTP_EXEC_CODE("execCode", Integer.class, 0),

    /** The host sftp exec cmd. */
    HOST_SFTP_EXEC_CMD("execCmd", String.class, STRING_NONE),

    /** The host sftp client version. */
    HOST_SFTP_CLIENT_VERSION("clientVersion", String.class, DEFAULT),

    /** The host sftp kex. */
    HOST_SFTP_KEX("kex", String.class, DEFAULT),

    /** The host sftp server host key. */
    HOST_SFTP_SERVER_HOST_KEY("serverHostKey", String.class, DEFAULT),

    /** The host sftp cipher. */
    HOST_SFTP_CIPHER("cipher", String.class, "none"),

    /** The host sftp mac. */
    HOST_SFTP_MAC("mac", String.class, "none"),

    /** The host sftp compression. */
    HOST_SFTP_COMPRESSION("compression", String.class, "none"),

    /** The host sftp finger print. */
    HOST_SFTP_FINGER_PRINT("fingerPrint", String.class, STRING_NONE),

    /** The host sftp pass phrase. */
    HOST_SFTP_PASS_PHRASE("passPhrase", String.class, STRING_NONE),

    /** The host sftp private key file. */
    HOST_SFTP_PRIVATE_KEY_FILE("privateKeyFile", String.class, ""),

    /** The host sftp private key. */
    HOST_SFTP_PRIVATE_KEY("privateKey", String.class, ""),

    /** The host sftp listen address. */
    HOST_SFTP_LISTEN_ADDRESS("listenAddress", String.class, STRING_NONE),

    /** The host sftp session time out. */
    HOST_SFTP_SESSION_TIME_OUT("sessionTimeOut", Duration.class, Duration.ofMinutes(1)),

    /** The host sftp server alive interval. */
    HOST_SFTP_SERVER_ALIVE_INTERVAL("serverAliveInterval", Duration.class, DURATION_NONE),

    /** The host sftp server alive count max. */
    HOST_SFTP_SERVER_ALIVE_COUNT_MAX("serverAliveCountMax", Integer.class, INTEGER_NONE),

    /** The host sftp connect time out. */
    HOST_SFTP_CONNECT_TIME_OUT("connectTimeOut", Duration.class, Duration.ofSeconds(30)),

    /** The host sftp preferred authentications. */
    HOST_SFTP_PREFERRED_AUTHENTICATIONS("preferredAuthentications", String.class, DEFAULT),

    /** The host sftp options. */
    HOST_SFTP_OPTIONS("options", String.class, DEFAULT),

    /** The host sftp mksuffix. */
    HOST_SFTP_MKSUFFIX("mksuffix", Boolean.class, false),

    /** The host sftp use write flush. */
    HOST_SFTP_USE_WRITE_FLUSH("useWriteFlush", Boolean.class, false),

    /** The host sftp bulk request number. */
    HOST_SFTP_BULK_REQUEST_NUMBER("bulkRequestNumber", Integer.class, 64),

    /** The host sftp commit. */
    HOST_SFTP_COMMIT("commit", String.class, STRING_NONE),

    /** The host sftp list recursive. */
    HOST_SFTP_LIST_RECURSIVE("listRecursive", Boolean.class, true),

    /** The host sftp list max waiting. */
    HOST_SFTP_LIST_MAX_WAITING("listMaxWaiting", Integer.class, 100),

    /** The host sftp list max threads. */
    HOST_SFTP_LIST_MAX_THREADS("listMaxThreads", Integer.class, 10),

    /** The host sftp list max dirs. */
    HOST_SFTP_LIST_MAX_DIRS("listMaxDirs", Integer.class, 50000),

    /** The host s3 port. */
    HOST_S3_PORT("port", Integer.class, 80),

    /** The host s3 scheme. */
    HOST_S3_SCHEME("scheme", String.class, "http"),

    /** The host s3 bucket name. */
    HOST_S3_BUCKET_NAME("bucketName", String.class, STRING_NONE),

    /** The host s3 prefix. */
    HOST_S3_PREFIX("prefix", String.class, ""),

    /** The host s3 num upload threads. */
    HOST_S3_NUM_UPLOAD_THREADS("numUploadThreads", Integer.class, 2),

    /** The host s3 queue capacity. */
    HOST_S3_QUEUE_CAPACITY("queueCapacity", Integer.class, 2),

    /** The host s3 recursive level. */
    HOST_S3_RECURSIVE_LEVEL("recursiveLevel", Integer.class, 0),

    /** The host s3 part size. */
    HOST_S3_PART_SIZE("partSize", Integer.class, 10),

    /** The host s3 region. */
    HOST_S3_REGION("region", String.class, ""),

    /** The host s3 url. */
    HOST_S3_URL("url", String.class, STRING_NONE),

    /** The host s3 listen address. */
    HOST_S3_LISTEN_ADDRESS("listenAddress", String.class, STRING_NONE),

    /** The host s3 strict. */
    HOST_S3_STRICT("strict", Boolean.class, false),

    /** The host s3 ssl validation. */
    HOST_S3_SSL_VALIDATION("sslValidation", Boolean.class, false),

    /** The host s3 protocol. */
    HOST_S3_PROTOCOL("protocol", String.class, "TLS"),

    /** The host s3 acceleration. */
    HOST_S3_ACCELERATION("acceleration", Boolean.class, false),

    /** The host s3 dualstack. */
    HOST_S3_DUALSTACK("dualstack", Boolean.class, false),

    /** The host s3 force global bucket access. */
    HOST_S3_FORCE_GLOBAL_BUCKET_ACCESS("forceGlobalBucketAccess", Boolean.class, true),

    /** The host s3 disable chunked encoding. */
    HOST_S3_DISABLE_CHUNKED_ENCODING("disableChunkedEncoding", Boolean.class, false),

    /** The host s3 enable path style access. */
    HOST_S3_ENABLE_PATH_STYLE_ACCESS("enablePathStyleAccess", Boolean.class, false),

    /** The host s3 mk bucket. */
    HOST_S3_MK_BUCKET("mkBucket", Boolean.class, false),

    /** The host s3 role arn. */
    HOST_S3_ROLE_ARN("roleArn", String.class, STRING_NONE),

    /** The host s3 role session name. */
    HOST_S3_ROLE_SESSION_NAME("roleSessionName", String.class, STRING_NONE),

    /** The host s3 duration seconds. */
    HOST_S3_DURATION_SECONDS("durationSeconds", Integer.class, 3600),

    /** The host s3 external id. */
    HOST_S3_EXTERNAL_ID("externalId", String.class, STRING_NONE),

    /** The host s3 allow empty bucket name. */
    HOST_S3_ALLOW_EMPTY_BUCKET_NAME("allowEmptyBucketName", Boolean.class, false),

    /** The host s3 multipart size. */
    HOST_S3_MULTIPART_SIZE("multipartSize", ByteSize.class, ByteSize.of(Long.MAX_VALUE)),

    /** The host s3 use byte array input stream. */
    HOST_S3_USE_BYTE_ARRAY_INPUT_STREAM("useByteArrayInputStream", Boolean.class, false),

    /** The host s3 enable mark and reset. */
    HOST_S3_ENABLE_MARK_AND_RESET("enableMarkAndReset", Boolean.class, false),

    /** The host s3 singlepart size. */
    HOST_S3_SINGLEPART_SIZE("singlepartSize", Long.class, Long.MAX_VALUE),

    /** The host s3 ftpuser. */
    HOST_S3_FTPUSER("ftpuser", String.class, STRING_NONE),

    /** The host s3 ftpgroup. */
    HOST_S3_FTPGROUP("ftpgroup", String.class, STRING_NONE),

    /** The host gcs allow empty bucket name. */
    HOST_GCS_ALLOW_EMPTY_BUCKET_NAME("allowEmptyBucketName", Boolean.class, false),

    /** The host gcs bucket location. */
    HOST_GCS_BUCKET_LOCATION("bucketLocation", String.class, ""),

    /** The host gcs bucket name. */
    HOST_GCS_BUCKET_NAME("bucketName", String.class, STRING_NONE),

    /** The host gcs chunk size. */
    HOST_GCS_CHUNK_SIZE("chunkSize", ByteSize.class, BYTE_SIZE_NONE),

    /** The host client email */
    HOST_GCS_CLIENT_EMAIL("clientEmail", String.class, STRING_NONE),

    /** The host gcs client id */
    HOST_GCS_CLIENT_ID("clientId", String.class, STRING_NONE),

    /** The host gcs ftpgroup. */
    HOST_GCS_FTPGROUP("ftpgroup", String.class, STRING_NONE),

    /** The host gcs ftpuser. */
    HOST_GCS_FTPUSER("ftpuser", String.class, STRING_NONE),

    /** The host gcs mk bucket. */
    HOST_GCS_MK_BUCKET("mkBucket", Boolean.class, false),

    /** The host gcs port. */
    HOST_GCS_PORT("port", Integer.class, 443),

    /** The host gcs prefix. */
    HOST_GCS_PREFIX("prefix", String.class, ""),

    /** The host private key */
    HOST_GCS_PRIVATE_KEY("privateKey", String.class, STRING_NONE),

    /** The host private key id */
    HOST_GCS_PRIVATE_KEY_ID("privateKeyId", String.class, STRING_NONE),

    /** The host gcs project id */
    HOST_GCS_PROJECT_ID("projectId", String.class, STRING_NONE),

    /** The host gcs protocol. */
    HOST_GCS_PROTOCOL("protocol", String.class, "TLS"),

    /** The host gcs scheme. */
    HOST_GCS_SCHEME("scheme", String.class, "http"),

    /** The host gcs ssl validation. */
    HOST_GCS_SSL_VALIDATION("sslValidation", Boolean.class, false),

    /** The host gcs url. */
    HOST_GCS_URL("url", String.class, STRING_NONE),

    /** The host azure port. */
    HOST_AZURE_PORT("port", Integer.class, 443),

    /** The host azure scheme. */
    HOST_AZURE_SCHEME("scheme", String.class, "https", Arrays.asList("https", "http")),

    /** The host azure container name. */
    HOST_AZURE_CONTAINER_NAME("containerName", String.class, STRING_NONE),

    /** The host azure url. */
    HOST_AZURE_URL("url", String.class, STRING_NONE),

    /** The host azure sas url. */
    HOST_AZURE_SAS_URL("sasUrl", String.class, ""),

    /** The host azure sas subscription key. */
    HOST_AZURE_SAS_SUBSCRIPTION_KEY("sasSubscriptionKey", String.class, ""),

    /** The host azure user assigned client id. */
    HOST_AZURE_USER_ASSIGNED_CLIENT_ID("userAssignedClientId", String.class, ""),

    /** The host azure mk container. */
    HOST_AZURE_MK_CONTAINER("mkContainer", Boolean.class, false),

    /** The host azure use md5. */
    HOST_AZURE_USE_MD5("useMD5", Boolean.class, false),

    /** The host azure multipart size. */
    HOST_AZURE_MULTIPART_SIZE("multipartSize", ByteSize.class, ByteSize.of(268435456)),

    /** The host azure ignore delete. */
    HOST_AZURE_IGNORE_DELETE("ignoreDelete", Boolean.class, true),

    /** The host azure block size. */
    HOST_AZURE_BLOCK_SIZE("blockSize", ByteSize.class, ByteSize.ofKB(10)),

    /** The host azure num buffers. */
    HOST_AZURE_NUM_BUFFERS("numBuffers", Integer.class, 5),

    /** The host azure chunk size. */
    HOST_AZURE_CHUNK_SIZE("chunkSize", ByteSize.class, ByteSize.ofKB(0)),

    /** The host azure overwrite. */
    HOST_AZURE_OVERWRITE("overwrite", Boolean.class, true),

    /** The host azure ignore check. */
    HOST_AZURE_IGNORE_CHECK("ignoreCheck", Boolean.class, true),

    /** The host azure ftpuser. */
    HOST_AZURE_FTPUSER("ftpuser", String.class, STRING_NONE),

    /** The host azure ftpgroup. */
    HOST_AZURE_FTPGROUP("ftpgroup", String.class, STRING_NONE),

    /** The host http port. */
    HOST_HTTP_PORT("port", Integer.class, 80),

    /** The host http supported protocols. */
    HOST_HTTP_SUPPORTED_PROTOCOLS("supportedProtocols", String.class,
            Arrays.asList("TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3")),

    /** The host http filename attribute. */
    HOST_HTTP_FILENAME_ATTRIBUTE("filenameAttribute", String.class, "filename"),

    /** The host http upload end point. */
    HOST_HTTP_UPLOAD_END_POINT("uploadEndPoint", String.class, ""),

    /** The host http multipart mode. */
    HOST_HTTP_MULTIPART_MODE("multipartMode", String.class, "LEGACY", Arrays.asList("LEGACY", "STRICT", "EXTENDED")),

    /** The host http headers. */
    HOST_HTTP_HEADERS("headers", String.class, ""),

    /** The host http scheme. */
    HOST_HTTP_SCHEME("scheme", String.class, "http", Arrays.asList("https", "http")),

    /** The host http enable content compression. */
    HOST_HTTP_ENABLE_CONTENT_COMPRESSION("enableContentCompression", Boolean.class, true),

    /** The host http proxy. */
    HOST_HTTP_PROXY("proxy", String.class, STRING_NONE),

    /** The host http allow circular redirects. */
    HOST_HTTP_ALLOW_CIRCULAR_REDIRECTS("allowCircularRedirects", Boolean.class, false),

    /** The host http strict. */
    HOST_HTTP_STRICT("strict", Boolean.class, false),

    /** The host http ssl validation. */
    HOST_HTTP_SSL_VALIDATION("sslValidation", Boolean.class, false),

    /** The host http protocol. */
    HOST_HTTP_PROTOCOL("protocol", String.class, "TLS"),

    /** The host http list max threads. */
    HOST_HTTP_LIST_MAX_THREADS("listMaxThreads", Integer.class, 10),

    /** The host http authheader. */
    HOST_HTTP_AUTHHEADER("authheader", Boolean.class, true),

    /** The host http credentials. */
    HOST_HTTP_CREDENTIALS("credentials", Boolean.class, true),

    /** The host http authcache. */
    HOST_HTTP_AUTHCACHE("authcache", Boolean.class, false),

    /** The host http encode url. */
    HOST_HTTP_ENCODE_URL("encodeURL", Boolean.class, false),

    /** The host http has parameters. */
    HOST_HTTP_HAS_PARAMETERS("hasParameters", Boolean.class, false),

    /** The host http use post. */
    HOST_HTTP_USE_POST("usePost", Boolean.class, false),

    /** The host http use multipart. */
    HOST_HTTP_USE_MULTIPART("useMultipart", Boolean.class, false),

    /** The host http use head. */
    HOST_HTTP_USE_HEAD("useHead", Boolean.class, false),

    /** The host http is symlink. */
    HOST_HTTP_IS_SYMLINK("isSymlink", Boolean.class, BOOLEAN_NONE),

    /** The host http fail on empty symlink. */
    HOST_HTTP_FAIL_ON_EMPTY_SYMLINK("failOnEmptySymlink", Boolean.class, false),

    /** The host http ftp like. */
    HOST_HTTP_FTP_LIKE("ftpLike", Boolean.class, true),

    /** The host http ftpuser. */
    HOST_HTTP_FTPUSER("ftpuser", String.class, STRING_NONE),

    /** The host http ftpgroup. */
    HOST_HTTP_FTPGROUP("ftpgroup", String.class, STRING_NONE),

    /** The host http mqtt mode. */
    HOST_HTTP_MQTT_MODE("mqttMode", Boolean.class, false),

    /** The host http list max waiting. */
    HOST_HTTP_LIST_MAX_WAITING("listMaxWaiting", Integer.class, 100),

    /** The host http mqtt url. */
    HOST_HTTP_MQTT_URL("mqttUrl", String.class, STRING_NONE),

    /** The host http mqtt scheme. */
    HOST_HTTP_MQTT_SCHEME("mqttScheme", String.class, "ssl", Arrays.asList("ssl", "tcp")),

    /** The host http mqtt port. */
    HOST_HTTP_MQTT_PORT("mqttPort", Integer.class, 8883),

    /** The host http mqtt clean start. */
    HOST_HTTP_MQTT_CLEAN_START("mqttCleanStart", Boolean.class, false),

    /** The host http mqtt session expiry interval. */
    HOST_HTTP_MQTT_SESSION_EXPIRY_INTERVAL("mqttSessionExpiryInterval", Duration.class, Duration.ofDays(1)),

    /** The host http mqtt connection timeout. */
    HOST_HTTP_MQTT_CONNECTION_TIMEOUT("mqttConnectionTimeout", Duration.class, Duration.ofSeconds(30)),

    /** The host http mqtt keep alive interval. */
    HOST_HTTP_MQTT_KEEP_ALIVE_INTERVAL("mqttKeepAliveInterval", Duration.class, Duration.ofSeconds(60)),

    /** The host http mqtt max files. */
    HOST_HTTP_MQTT_MAX_FILES("mqttMaxFiles", Integer.class, 1000000),

    /** The host http mqtt persistence mode. */
    HOST_HTTP_MQTT_PERSISTENCE_MODE("mqttPersistenceMode", String.class, "file", Arrays.asList("file", "memory")),

    /** The host http mqtt subscriber id. */
    HOST_HTTP_MQTT_SUBSCRIBER_ID("mqttSubscriberId", String.class, STRING_NONE),

    /** The host http mqtt persistence. */
    HOST_HTTP_MQTT_PERSISTENCE("mqttPersistence", Boolean.class, false),

    /** The host http mqtt persistence directory. */
    HOST_HTTP_MQTT_PERSISTENCE_DIRECTORY("mqttPersistenceDirectory", String.class, STRING_NONE),

    /** The host http mqtt href. */
    HOST_HTTP_MQTT_HREF("mqttHref", String.class, STRING_NONE),

    /** The host http mqtt alternative name. */
    HOST_HTTP_MQTT_ALTERNATIVE_NAME("mqttAlternativeName", String.class, STRING_NONE),

    /** The host http mqtt size. */
    HOST_HTTP_MQTT_SIZE("mqttSize", ByteSize.class, BYTE_SIZE_NONE),

    /** The host http mqtt time. */
    HOST_HTTP_MQTT_TIME("mqttTime", Integer.class, -1),

    /** The host http mqtt body. */
    HOST_HTTP_MQTT_BODY("mqttBody", String.class, STRING_NONE),

    /** The host http mqtt add payload. */
    HOST_HTTP_MQTT_ADD_PAYLOAD("mqttAddPayload", Boolean.class, false),

    /** The host http mqtt qos. */
    HOST_HTTP_MQTT_QOS("mqttQos", Integer.class, 1),

    /** The host http mqtt await. */
    HOST_HTTP_MQTT_AWAIT("mqttAwait", Duration.class, Duration.ofSeconds(60)),

    /** The host http urldir. */
    HOST_HTTP_URLDIR("urldir", Boolean.class, BOOLEAN_NONE),

    /** The host http dodir. */
    HOST_HTTP_DODIR("dodir", Boolean.class, true),

    /** The host http max size. */
    HOST_HTTP_MAX_SIZE("maxSize", ByteSize.class, ByteSize.ofMB(10)),

    /** The host http select. */
    HOST_HTTP_SELECT("select", String.class, "a[href]"),

    /** The host http list max files. */
    HOST_HTTP_LIST_MAX_FILES("listMaxFiles", Integer.class, 500000),

    /** The host http attribute. */
    HOST_HTTP_ATTRIBUTE("attribute", String.class, ""),

    /** The host http alternative path. */
    HOST_HTTP_ALTERNATIVE_PATH("alternativePath", String.class, ""),

    /** The host http list recursive. */
    HOST_HTTP_LIST_RECURSIVE("listRecursive", Boolean.class, false),

    /** The host http list max dirs. */
    HOST_HTTP_LIST_MAX_DIRS("listMaxDirs", Integer.class, 50000),

    /** The host ecauth exec cmd. */
    HOST_ECAUTH_EXEC_CMD("execCmd", String.class, STRING_NONE),

    /** The host ecauth exec code. */
    HOST_ECAUTH_EXEC_CODE("execCode", Integer.class, 0),

    /** The host ecauth mkdirs cmd index. */
    HOST_ECAUTH_MKDIRS_CMD_INDEX("mkdirsCmdIndex", Integer.class, 0),

    /** The host ecauth pre mkdirs cmd. */
    HOST_ECAUTH_PRE_MKDIRS_CMD("preMkdirsCmd", String.class, STRING_NONE),

    /** The host ecauth post mkdirs cmd. */
    HOST_ECAUTH_POST_MKDIRS_CMD("postMkdirsCmd", String.class, STRING_NONE),

    /** The host ecauth keep alive. */
    HOST_ECAUTH_KEEP_ALIVE("keepAlive", Duration.class, Duration.ZERO),

    /** The host ecauth use noop. */
    HOST_ECAUTH_USE_NOOP("useNoop", Duration.class, Duration.ZERO),

    /** The host ecauth ignore mkdirs cmd errors. */
    HOST_ECAUTH_IGNORE_MKDIRS_CMD_ERRORS("ignoreMkdirsCmdErrors", Boolean.class, false),

    /** The host ecauth ignore check. */
    HOST_ECAUTH_IGNORE_CHECK("ignoreCheck", Boolean.class, true),

    /** The host ecauth usemget. */
    HOST_ECAUTH_USEMGET("usemget", Boolean.class, false),

    /** The host ecauth usetmp. */
    HOST_ECAUTH_USETMP("usetmp", Boolean.class, false),

    /** The host ecauth mkdirs. */
    HOST_ECAUTH_MKDIRS("mkdirs", Boolean.class, false),

    /** The host ecauth suffix. */
    HOST_ECAUTH_SUFFIX("suffix", String.class, ""),

    /** The host ecauth prefix. */
    HOST_ECAUTH_PREFIX("prefix", String.class, ""),

    /** The host ecauth mksuffix. */
    HOST_ECAUTH_MKSUFFIX("mksuffix", Boolean.class, false),

    /** The host ecauth user. */
    HOST_ECAUTH_USER("user", String.class, STRING_NONE),

    /** The host ecauth pass. */
    HOST_ECAUTH_PASS("pass", String.class, STRING_NONE),

    /** The host ecauth protocol. */
    HOST_ECAUTH_PROTOCOL("protocol", String.class, "ssh", Arrays.asList("ssh", "telnet")),

    /** The host ecauth resolve ip. */
    HOST_ECAUTH_RESOLVE_IP("resolveIP", Boolean.class, true),

    /** The host ecauth host list. */
    HOST_ECAUTH_HOST_LIST("hostList", String.class, ""),

    /** The host ecauth cwd. */
    HOST_ECAUTH_CWD("cwd", String.class, STRING_NONE),

    /** The host ecauth proxy list. */
    HOST_ECAUTH_PROXY_LIST("proxyList", String.class, ""),

    /** The host ecauth copy cmd. */
    HOST_ECAUTH_COPY_CMD("copyCmd", String.class, ""),

    /** The host ecauth chmod on copy. */
    HOST_ECAUTH_CHMOD_ON_COPY("chmodOnCopy", String.class, "640"),

    /** The host ecauth session timeout. */
    HOST_ECAUTH_SESSION_TIMEOUT("sessionTimeOut", Duration.class, DURATION_NONE),

    /** The host ecauth server alive interval. */
    HOST_ECAUTH_SERVER_ALIVE_INTERVAL("serverAliveInterval", Duration.class, DURATION_NONE),

    /** The host ecauth server alive count max. */
    HOST_ECAUTH_SERVER_ALIVE_COUNT_MAX("serverAliveCountMax", Integer.class, INTEGER_NONE),

    /** The host ecauth port. */
    HOST_ECAUTH_PORT("port", Integer.class, 22),

    /** The host ecauth private key file. */
    HOST_ECAUTH_PRIVATE_KEY_FILE("privateKeyFile", String.class, ""),

    /** The host ecauth private key. */
    HOST_ECAUTH_PRIVATE_KEY("privateKey", String.class, ""),

    /** The host ecauth finger print. */
    HOST_ECAUTH_FINGER_PRINT("fingerPrint", String.class, STRING_NONE),

    /** The host ecauth passphrase. */
    HOST_ECAUTH_PASSPHRASE("passPhrase", String.class, STRING_NONE),

    /** The host ecauth listen address. */
    HOST_ECAUTH_LISTEN_ADDRESS("listenAddress", String.class, STRING_NONE),

    /** The host ecauth connect time out. */
    HOST_ECAUTH_CONNECT_TIME_OUT("connectTimeOut", Duration.class, Duration.ofSeconds(30)),

    /** The host ecauth cipher. */
    HOST_ECAUTH_CIPHER("cipher", String.class, "none"),

    /** The host ecauth compression. */
    HOST_ECAUTH_COMPRESSION("compression", String.class, "none"),

    /** The host ecaccess lastused. */
    HOST_ECACCESS_LASTUSED("lastused", String.class, STRING_NONE),

    /** The host ecaccess loadbalancing. */
    HOST_ECACCESS_LOADBALANCING("loadbalancing", Boolean.class, false),

    /** The host ecaccess gateway. */
    HOST_ECACCESS_GATEWAY("gateway", String.class, "mover"),

    /** The host ecaccess destination. */
    HOST_ECACCESS_DESTINATION("destination", String.class, "genericFtp",
            Arrays.asList("genericFtp", "genericSftp", "genericExec", "genericFile")),

    /** The host master home dir. */
    HOST_MASTER_HOME_DIR("homeDir", String.class, STRING_NONE),

    /** The user portal anonymous. */
    USER_PORTAL_ANONYMOUS("anonymous", Boolean.class, false),

    /** The user portal geoblocling. */
    USER_PORTAL_GEOBLOCLING("geoblocking", String.class, STRING_NONE),

    /** The user portal use passcode. */
    USER_PORTAL_USE_PASSCODE("usePasscode", Boolean.class, false),

    /** The user portal max connections. */
    USER_PORTAL_MAX_CONNECTIONS("maxConnections", Integer.class, Cnf.at("Server", "maxIncomingConnections", 10)),

    /** The user portal update last login information. */
    USER_PORTAL_UPDATE_LAST_LOGIN_INFORMATION("updateLastLoginInformation", Boolean.class, true),

    /** The user portal tab. */
    USER_PORTAL_TAB("tab", String.class, STRING_NONE),

    /** The user portal title. */
    USER_PORTAL_TITLE("title", String.class, STRING_NONE),

    /** The user portal footer. */
    USER_PORTAL_FOOTER("footer", String.class, STRING_NONE),

    /** The user portal color. */
    USER_PORTAL_COLOR("color", String.class, STRING_NONE),

    /** The user portal warning. */
    USER_PORTAL_WARNING("warning", String.class, STRING_NONE),

    /** The user portal msg top. */
    USER_PORTAL_MSG_TOP("msgTop", String.class, STRING_NONE),

    /** The user portal msg down. */
    USER_PORTAL_MSG_DOWN("msgDown", String.class, STRING_NONE),

    /** The user portal trigger event. */
    USER_PORTAL_TRIGGER_EVENT("triggerEvent", Boolean.class, true),

    /** The user portal record history. */
    USER_PORTAL_RECORD_HISTORY("recordHistory", Boolean.class, true),

    /** The user portal record splunk. */
    USER_PORTAL_RECORD_SPLUNK("recordSplunk", Boolean.class, true),

    /** The user portal simple list. */
    USER_PORTAL_SIMPLE_LIST("simpleList", Boolean.class, false),

    /** The user portal trigger last range only. */
    USER_PORTAL_TRIGGER_LAST_RANGE_ONLY("triggerLastRangeOnly", Boolean.class, true),

    /** The user portal max ranges allowed. */
    USER_PORTAL_MAX_RANGES_ALLOWED("maxRangesAllowed", ByteSize.class, ByteSize.of(Long.MAX_VALUE)),

    /** The user portal destination. */
    USER_PORTAL_DESTINATION("destination", String.class, STRING_NONE),

    /** The user portal sort. */
    USER_PORTAL_SORT("sort", String.class, STRING_NONE, Arrays.asList("size", "target", "time")),

    /** The user portal order. */
    USER_PORTAL_ORDER("order", String.class, STRING_NONE, Arrays.asList("asc", "desc")),

    /** The user portal domain. */
    USER_PORTAL_DOMAIN("domain", String.class, STRING_NONE),

    /** The user portal welcome. */
    USER_PORTAL_WELCOME("welcome", String.class, STRING_NONE),

    /** The user portal mqtt permission. */
    USER_PORTAL_MQTT_PERMISSION("mqttPermission", String.class, STRING_NONE),

    /** The user portal delete path perm regex. */
    USER_PORTAL_DELETE_PATH_PERM_REGEX("deletePathPermRegex", String.class, STRING_NONE),

    /** The user portal dir path perm regex. */
    USER_PORTAL_DIR_PATH_PERM_REGEX("dirPathPermRegex", String.class, STRING_NONE),

    /** The user portal get path perm regex. */
    USER_PORTAL_GET_PATH_PERM_REGEX("getPathPermRegex", String.class, STRING_NONE),

    /** The user portal mkdir path perm regex. */
    USER_PORTAL_MKDIR_PATH_PERM_REGEX("mkdirPathPermRegex", String.class, STRING_NONE),

    /** The user portal rename path perm regex. */
    USER_PORTAL_RENAME_PATH_PERM_REGEX("renamePathPermRegex", String.class, STRING_NONE),

    /** The user portal put path perm regex. */
    USER_PORTAL_PUT_PATH_PERM_REGEX("putPathPermRegex", String.class, STRING_NONE),

    /** The user portal rmdir path perm regex. */
    USER_PORTAL_RMDIR_PATH_PERM_REGEX("rmdirPathPermRegex", String.class, STRING_NONE),

    /** The user portal size path perm regex. */
    USER_PORTAL_SIZE_PATH_PERM_REGEX("sizePathPermRegex", String.class, STRING_NONE),

    /** The user portal mtime path perm regex. */
    USER_PORTAL_MTIME_PATH_PERM_REGEX("mtimePathPermRegex", String.class, STRING_NONE),

    /** The user portal header registry. */
    USER_PORTAL_HEADER_REGISTRY("headerRegistry", String.class, STRING_NONE);

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECtransOptions.class);

    /** The option. */
    private final ECtransOption<?> option;

    /**
     * Instantiates a new ectrans options.
     *
     * @param <T>
     *            the generic type
     * @param name
     *            the name
     * @param clazz
     *            the clazz
     * @param defaultValue
     *            the default value
     */
    <T> ECtransOptions(final String name, final Class<T> clazz, final T defaultValue) {
        this(name, clazz, Arrays.asList(defaultValue), new ArrayList<>());
    }

    /**
     * Instantiates a new ectrans options.
     *
     * @param <T>
     *            the generic type
     * @param name
     *            the name
     * @param clazz
     *            the clazz
     * @param defaultValue
     *            the default value
     * @param choices
     *            the choices
     */
    <T> ECtransOptions(final String name, final Class<T> clazz, final T defaultValue, final List<T> choices) {
        this(name, clazz, Arrays.asList(defaultValue), choices);
    }

    /**
     * Instantiates a new ectrans options.
     *
     * @param <T>
     *            the generic type
     * @param name
     *            the name
     * @param clazz
     *            the clazz
     * @param defaultValues
     *            the default values
     */
    <T> ECtransOptions(final String name, final Class<T> clazz, final List<T> defaultValues) {
        this(name, clazz, defaultValues, new ArrayList<>());
    }

    /**
     * Instantiates a new ectrans options.
     *
     * @param <T>
     *            the generic type
     * @param name
     *            the name
     * @param clazz
     *            the clazz
     * @param defaultValues
     *            the default values
     * @param choices
     *            the choices
     */
    <T> ECtransOptions(final String name, final Class<T> clazz, final List<T> defaultValues, final List<T> choices) {
        final var words = toString().split("_");
        this.option = new ECtransOption<>(ECtransGroups.valueOf(words[0]), words[1].toLowerCase(), name.trim(), clazz,
                defaultValues, choices);
    }

    /**
     * Gets the default value. Tries to comply by doing a conversion if required (and possible).
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the clazz
     * @param object
     *            the object
     *
     * @return the default value
     */
    @SuppressWarnings("unchecked")
    private <T> T getDefaultValue(final Class<T> clazz, final Object object) {
        if (object == null) {
            return null; // Null object so no conversion
        }
        if (clazz.isInstance(object)) {
            return clazz.cast(object); // No conversion required
        } else {
            // We are required to make a conversion as the classes don't match!
            final var objectClassName = object.getClass().getName();
            if (!clazz.equals(String.class)) { // This is a normal toString()!
                _log.debug("Class mismatch requires conversion: {} ({} -> {})", option.getParameter(), objectClassName,
                        clazz.getName());
            }
            if (clazz.equals(Integer.class) && object instanceof final ByteSize value) {
                return (T) Integer.valueOf((int) value.size()); // ByteSize to Integer
            } else if (clazz.equals(Long.class) && object instanceof final ByteSize value) {
                return (T) Long.valueOf(value.size()); // ByteSize to Long
            } else if (clazz.equals(Double.class) && object instanceof final ByteSize value) {
                return (T) Double.valueOf(value.size()); // ByteSize to Double
            } else if (clazz.equals(Integer.class) && object instanceof final Long value) {
                return (T) Integer.valueOf(value.intValue()); // Long to Integer
            } else if (clazz.equals(Long.class) && object instanceof final Integer value) {
                return (T) Long.valueOf(value); // Integer to Long
            } else if (clazz.equals(Boolean.class) && object instanceof final String value) {
                return (T) Boolean.valueOf("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value));
            } else if (clazz.equals(Boolean.class) && object instanceof final Double value) {
                return (T) Boolean.valueOf(value.doubleValue() != 0); // Double Boolean
            } else if (clazz.equals(Boolean.class) && object instanceof final Long value) {
                return (T) Boolean.valueOf(value.longValue() != 0); // Long to Boolean
            } else if (clazz.equals(Boolean.class) && object instanceof final Integer value) {
                return (T) Boolean.valueOf(value.intValue() != 0); // Integer to Boolean
            } else if (clazz.equals(String.class) && object instanceof final Boolean value) {
                return (T) (Boolean.TRUE.equals(value) ? "yes" : "no"); // Boolean to String
            } else if (clazz.equals(ByteSize.class) && object instanceof final String value) {
                return (T) ("max-size".equals(value) ? ByteSize.MAX_VALUE : ByteSize.parse(value));
            } else if (clazz.equals(Double.class) && object instanceof final String value) {
                final Double maxValue = Double.MAX_VALUE;
                return (T) ("max-double".equals(value) ? maxValue : Double.valueOf(value));
            } else if (clazz.equals(Long.class) && object instanceof final String value) {
                final Long maxValue = Long.MAX_VALUE;
                return (T) ("max-long".equals(value) ? maxValue : Long.valueOf(value));
            } else if (clazz.equals(Integer.class) && object instanceof final String value) {
                final Integer maxValue = Integer.MAX_VALUE;
                return (T) ("max-integer".equals(value) ? maxValue : Integer.valueOf(value));
            } else if (clazz.equals(String.class) // All other objects available to String
                    && (object instanceof Period || object instanceof Duration || object instanceof TimeRange
                            || object instanceof ByteSize || object instanceof Integer || object instanceof Long
                            || object instanceof Double)) {
                return (T) object.toString();
            } else {
                throw new ClassCastException("Cannot convert " + clazz.getName() + " to " + objectClassName);
            }
        }
    }

    /**
     * Gets the default value.
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the clazz
     *
     * @return the default value
     */
    protected <T> T getDefaultValue(final Class<T> clazz) {
        return getDefaultValue(clazz, option.getDefaultValue());
    }

    /**
     * Gets the default values.
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the clazz
     *
     * @return the default values
     */
    protected <T> List<T> getDefaultValues(final Class<T> clazz) {
        return option.getDefaultValues().stream().map(object -> getDefaultValue(clazz, object)).toList();
    }

    /**
     * Gets the optional.
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the clazz
     *
     * @return the optional
     */
    private <T> Optional<T> getOptional(final Class<T> clazz) {
        return Optional.ofNullable(getDefaultValue(clazz));
    }

    /**
     * Gets the optional default byte size.
     *
     * @return the optional default byte size
     */
    public Optional<ByteSize> getOptionalDefaultByteSize() {
        return getOptional(ByteSize.class);
    }

    /**
     * Gets the default byte size.
     *
     * @return the default byte size
     */
    public ByteSize getDefaultByteSize() {
        return getDefaultValue(ByteSize.class);
    }

    /**
     * Gets the default byte size list.
     *
     * @return the default byte size list
     */
    public List<ByteSize> getDefaultByteSizeList() {
        return getDefaultValues(ByteSize.class);
    }

    /**
     * Gets the optional default duration.
     *
     * @return the optional default duration
     */
    public Optional<Duration> getOptionalDefaultDuration() {
        return getOptional(Duration.class);
    }

    /**
     * Gets the default duration.
     *
     * @return the default duration
     */
    public Duration getDefaultDuration() {
        return getDefaultValue(Duration.class);
    }

    /**
     * Gets the default duration list.
     *
     * @return the default duration list
     */
    public List<Duration> getDefaultDurationList() {
        return getDefaultValues(Duration.class);
    }

    /**
     * Gets the optional default period.
     *
     * @return the optional default period
     */
    public Optional<Period> getOptionalDefaultPeriod() {
        return getOptional(Period.class);
    }

    /**
     * Gets the default period.
     *
     * @return the default period
     */
    public Period getDefaultPeriod() {
        return getDefaultValue(Period.class);
    }

    /**
     * Gets the default period list.
     *
     * @return the default period list
     */
    public List<Period> getDefaultPeriodList() {
        return getDefaultValues(Period.class);
    }

    /**
     * Gets the optional default integer.
     *
     * @return the optional default integer
     */
    public Optional<Integer> getOptionalDefaultInteger() {
        return getOptional(Integer.class);
    }

    /**
     * Gets the default integer.
     *
     * @return the default integer
     */
    public Integer getDefaultInteger() {
        return getDefaultValue(Integer.class);
    }

    /**
     * Gets the default integer list.
     *
     * @return the default integer list
     */
    public List<Integer> getDefaultIntegerList() {
        return getDefaultValues(Integer.class);
    }

    /**
     * Gets the optional default long.
     *
     * @return the optional default long
     */
    public Optional<Long> getOptionalDefaultLong() {
        return getOptional(Long.class);
    }

    /**
     * Gets the default long.
     *
     * @return the default long
     */
    public Long getDefaultLong() {
        return getDefaultValue(Long.class);
    }

    /**
     * Gets the default long list.
     *
     * @return the default long list
     */
    public List<Long> getDefaultLongList() {
        return getDefaultValues(Long.class);
    }

    /**
     * Gets the optional default double.
     *
     * @return the optional default double
     */
    public Optional<Double> getOptionalDefaultDouble() {
        return getOptional(Double.class);
    }

    /**
     * Gets the default double.
     *
     * @return the default double
     */
    public Double getDefaultDouble() {
        return getDefaultValue(Double.class);
    }

    /**
     * Gets the default double list.
     *
     * @return the default double list
     */
    public List<Double> getDefaultDoubleList() {
        return getDefaultValues(Double.class);
    }

    /**
     * Gets the optional default boolean.
     *
     * @return the optional default boolean
     */
    public Optional<Boolean> getOptionalDefaultBoolean() {
        return getOptional(Boolean.class);
    }

    /**
     * Gets the default boolean.
     *
     * @return the default boolean
     */
    public boolean getDefaultBoolean() {
        return Boolean.TRUE.equals(getDefaultValue(Boolean.class));
    }

    /**
     * Gets the default boolean list.
     *
     * @return the default boolean list
     */
    public List<Boolean> getDefaultBooleanList() {
        return getDefaultValues(Boolean.class);
    }

    /**
     * Gets the optional default string.
     *
     * @return the optional default string
     */
    public Optional<String> getOptionalDefaultString() {
        return getOptional(String.class);
    }

    /**
     * Gets the default string.
     *
     * @return the default string
     */
    public String getDefaultString() {
        return getDefaultValue(String.class);
    }

    /**
     * Gets the default string list.
     *
     * @return the default string list
     */
    public List<String> getDefaultStringList() {
        return getDefaultValues(String.class);
    }

    /**
     * Gets the optional default time range.
     *
     * @return the optional default time range
     */
    public Optional<TimeRange> getOptionalDefaultTimeRange() {
        return getOptional(TimeRange.class);
    }

    /**
     * Gets the default time range.
     *
     * @return the default time range
     */
    public TimeRange getDefaultTimeRange() {
        return getDefaultValue(TimeRange.class);
    }

    /**
     * Gets the default time range list.
     *
     * @return the default time range list
     */
    public List<TimeRange> getDefaultTimeRangeList() {
        return getDefaultValues(TimeRange.class);
    }

    /**
     * Gets the parameter.
     *
     * @return the parameter
     */
    public String getParameter() {
        return option.getParameter();
    }

    /**
     * Gets the module.
     *
     * @return the module
     */
    public String getModule() {
        return option.getModule();
    }

    /**
     * Gets the full name.
     *
     * @return the full name
     */
    public String getFullName() {
        return getModule() + "." + getName();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return option.getName();
    }

    /**
     * Checks if is visible.
     *
     * @return true, if is visible
     */
    public boolean isVisible() {
        return option.isVisible();
    }

    /**
     * Gets the clazz.
     *
     * @return the clazz
     */
    public Class<?> getClazz() {
        return option.getClazz();
    }

    /**
     * Gets the all.
     *
     * @return the all
     */
    private static List<ECtransOptions> getAll() {
        return Arrays.stream(values()).sorted(Comparator.comparing(entry -> entry.option.getParameter())).toList();
    }

    /**
     * Gets the all.
     *
     * @param secured
     *            the secured
     *
     * @return the all
     */
    protected static List<ECtransOptions> getAll(final boolean secured) {
        return Arrays.stream(values()).filter(entry -> entry.option.isSecured() == secured)
                .sorted(Comparator.comparing(entry -> entry.option.getParameter())).toList();
    }

    /**
     * Gets the.
     *
     * @param group
     *            the group
     *
     * @return the list
     */
    public static List<ECtransOptions> get(final ECtransGroups group) {
        return Arrays.stream(values()).filter(entry -> entry.option.getGroup() == group)
                .sorted(Comparator.comparing(entry -> entry.option.getParameter())).toList();
    }

    /**
     * To string.
     *
     * @param group
     *            the group
     *
     * @return the string
     */
    public static String toString(final ECtransGroups group) {
        return toString(get(group));
    }

    /**
     * To string.
     *
     * @param options
     *            the options
     *
     * @return the string
     */
    private static String toString(final List<ECtransOptions> options) {
        return options.stream().filter(entry -> entry.option.isVisible())
                .map(entry -> toString(entry.option, entry.option.getParameter())).collect(Collectors.joining(",\n"));
    }

    /**
     * To string.
     *
     * @param option
     *            the option
     * @param names
     *            the names
     *
     * @return the string
     */
    public static String toString(final ECtransOptions option, final List<String> names) {
        return names.stream().map(name -> toString(option.option, option.getModule() + "." + name))
                .collect(Collectors.joining(",\n"));
    }

    /**
     * To string.
     *
     * @param option
     *            the option
     * @param parameter
     *            the parameter
     *
     * @return the string
     */
    private static String toString(final ECtransOption<?> option, final String parameter) {
        return "{ caption: '" + parameter + "', meta: '" + option.getComment(false) + "', type: '"
                + option.getClazz().getSimpleName() + "', choices: ["
                + option.getChoices().stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")) + "], tips: '"
                + option.getTips() + "' }";
    }

    /**
     * To types.
     *
     * @param options
     *            the options
     *
     * @return the string
     */
    public static String toTypes(final Collection<ECtransOptions> options) {
        return options.stream().map(entry -> entry.option.getClazz().getSimpleName()).distinct()
                .collect(Collectors.joining(","));
    }

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ALL);
        // Testing standardization of setup!
        final var setup = new ECtransSetup("incoming", """
                incoming.lifetime= "12d"
                incoming.maxBytesPerSecForInput = "1024,10MB"
                incoming.standby = "false"
                incoming.updateLastLoginInformation = "true"
                http.select = "a[href]"
                ectrans.delTimeOut = "90s"
                """);
        for (final ECtransOptions option : getAll()) {
            _log.info("\nProcessing {}", option.getFullName()); // e.g. "incoming.standby"
            setup.standardize(option, true);
        }
        _log.info("\n{}", setup.getData());
        // List all parameters to console!
        final var options = getAll();
        if (_log.isInfoEnabled()) {
            _log.info("{} option(s) selected\nUsed types: {}\n{}", options.size(), toTypes(options), toString(options));
        }
    }

    /**
     * The Class MarkdownGenerator is used from the building process to generate a markdown file with the ECtransOptions
     * API.
     */
    public static class MarkdownGenerator {

        /**
         * Generate the markdown string.
         *
         * @return the string
         */
        private static String toMarkdown() {
            final var result = new StringBuilder();
            try (final var input = ECtransOption.class.getResourceAsStream("ectrans-options.properties")) {
                final var properties = new Properties();
                properties.load(input);
                // Map to store the properties by group
                final Map<String, Map<String, String>> groupedProperties = new LinkedHashMap<>();
                String currentGroup = null;
                // Get the property names and sort them
                final var propertyNames = new ArrayList<>(properties.stringPropertyNames());
                Collections.sort(propertyNames);
                for (var name : propertyNames) {
                    if (!name.endsWith(".tips"))
                        // We only process the tips properties
                        continue;
                    var value = properties.getProperty(name);
                    if (value == null || value.isEmpty())
                        // Don't process properties with empty values.
                        continue;
                    name = name.substring(0, name.length() - 5);
                    final var groupName = getGroupName(name);
                    // Grouping by the prefix (before the first dot)
                    if (!groupName.equals(currentGroup)) {
                        currentGroup = groupName;
                        groupedProperties.put(currentGroup, new LinkedHashMap<>());
                    }
                    groupedProperties.get(currentGroup).put(name, value);
                }
                // Write the table of contents (TOC)
                result.append("# Properties Documentation\n\n");
                result.append("## Table of Contents\n\n");
                for (final var group : groupedProperties.keySet()) {
                    result.append("- [" + capitalize(group) + " Options](#" + group.toLowerCase() + "-options)\n");
                }
                result.append("\n");
                // Write the grouped properties
                for (final var groupEntry : groupedProperties.entrySet()) {
                    // Write group heading
                    final var groupName = groupEntry.getKey();
                    result.append("## " + capitalize(groupName) + " Options\n\n");
                    final var comment = properties.getProperty(groupName + ".comment");
                    if (comment != null && !comment.isBlank())
                        result.append(capitalize(comment) + "\n\n");
                    // Write each property in the group
                    for (final var property : groupEntry.getValue().entrySet()) {
                        result.append("### " + property.getKey() + "\n");
                        result.append(property.getValue() + "\n\n");
                    }
                }
            } catch (IOException e) {
                _log.error("An error occurred while generating markdown.", e);
            }
            return escapeHtmlTags(result.toString());
        }

        /**
         * Helper method to extract the group name (prefix before the first dot).
         *
         * @param propertyName
         *            the property name
         *
         * @return the group name
         */
        private static String getGroupName(String propertyName) {
            int index = propertyName.indexOf('.');
            if (index != -1) {
                return propertyName.substring(0, index);
            }
            // default group if no prefix
            return "general";
        }

        /**
         * Helper method to capitalize the group name for readability.
         *
         * @param str
         *            the str
         *
         * @return the string
         */
        private static String capitalize(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }

        /**
         * Escape html tags.
         *
         * @param input
         *            the input
         *
         * @return the string
         */
        private static String escapeHtmlTags(final String input) {
            if (input == null) {
                // Handle null input
                return null;
            }
            // Escape < and > characters
            String escaped = input.replace("<", "&lt;").replace(">", "&gt;");
            // Format inline HTML tags with backticks
            escaped = escaped.replaceAll("(?i)(`.*?`|\\<.*?\\>)", "`$0`");
            // Format multi-line HTML blocks with triple backticks
            // This regex finds multi-line HTML blocks that are not nested
            escaped = escaped.replaceAll("(?s)(\\<.*?\\>)", "\n```\n$0\n```\n");
            return escaped;
        }

        /**
         * Write to file.
         *
         * @param sb
         *            the sb
         * @param filename
         *            the filename
         */
        private static void writeToFile(final String sb, final String filename) {
            final var path = Paths.get(filename);
            try {
                Files.write(path, sb.getBytes());
            } catch (IOException e) {
                _log.error("An error occurred while writing to the file: {}", path, e);
            }
        }

        /**
         * The main method.
         *
         * @param args
         *            the arguments
         */
        public static void main(final String[] args) {
            Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ALL);
            if (args.length > 0) {
                writeToFile(toMarkdown(), args[0]);
            } else {
                _log.error("No filename provided.");
            }
        }
    }
}
