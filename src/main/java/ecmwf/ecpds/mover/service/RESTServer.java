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

package ecmwf.ecpds.mover.service;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.PortalTraffic;
import ecmwf.common.database.ExistingStorageDirectory;
import ecmwf.common.database.Host;
import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.ecaccess.FileListElement;
import ecmwf.common.ecaccess.NativeAuthenticationProvider;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ecaccess.UserSession;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.MonitoredInputStream;
import ecmwf.common.technical.ProxyEvent;
import ecmwf.common.technical.ProxySocket;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.BASE64Coder;
import ecmwf.common.text.Format;
import ecmwf.common.version.Version;
import ecmwf.ecpds.mover.MoverProvider;
import ecmwf.ecpds.mover.MoverServer;
import ecmwf.ecpds.mover.service.RESTClient.MonitorRequest;
import ecmwf.ecpds.mover.service.RESTClient.PutRequest;
import ecmwf.ecpds.mover.service.RESTClient.UpdateDataRequest;

/**
 * The Class RESTServer.
 */
@Path("")
public final class RESTServer {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RESTServer.class);

    /** The Constant mover. */
    private static final MoverServer mover = StarterServer.getInstance(MoverServer.class);

    /** The Constant HOME_FILE. */
    private static final String HOME_FILE = Cnf.at("HttpPlugin", "homeFile");

    /** The Constant PORTAL_FILE. */
    private static final String PORTAL_FILE = Cnf.at("HttpPlugin", "portalFile");

    /** The Constant REGISTER_FILE. */
    private static final String REGISTER_FILE = Cnf.at("HttpPlugin", "registerFile");

    /** The Constant LOGIN_FILE. */
    private static final String LOGIN_FILE = Cnf.at("HttpPlugin", "loginFile");

    /** The Constant homeContent. */
    private static final StringBuilder homeContent = new StringBuilder();

    /** The Constant portalContent. */
    private static final StringBuilder portalContent = new StringBuilder();

    /** The Constant registerContent. */
    private static final StringBuilder registerContent = new StringBuilder();

    /** The Constant loginContent. */
    private static final StringBuilder loginContent = new StringBuilder();

    /** The Constant MULTIPART_BOUNDARY. */
    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

    /** The Constant CONTENT_TYPE. */
    private static final String CONTENT_TYPE = "Content-Type";

    /** The Constant CONTENT_LENGTH. */
    private static final String CONTENT_LENGTH = "Content-Length";

    /** The Constant CONTENT_DISPOSITION. */
    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    /** The Constant CONTENT_RANGE. */
    private static final String CONTENT_RANGE = "Content-Range";

    /** The Constant LAST_MODIFIED. */
    private static final String LAST_MODIFIED = "Last-Modified";

    /** The Constant ACCEPT_RANGES. */
    private static final String ACCEPT_RANGES = "Accept-Ranges";

    /** The Constant ACCEPT. */
    private static final String ACCEPT = "Accept";

    /** The Constant RANGE. */
    private static final String RANGE = "Range";

    /** The Constant IF_MODIFIED_SINCE. */
    private static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    /** The Constant IF_NONE_MATCH. */
    private static final String IF_NONE_MATCH = "If-None-Match";

    /** The Constant IF_UNMODIFIED_SINCE. */
    private static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

    /** The Constant IF_RANGE. */
    private static final String IF_RANGE = "If-Range";

    /** The Constant CACHE_CONTROL. */
    private static final String CACHE_CONTROL = "Cache-Control";

    /** The Constant NO_CACHE. */
    private static final String NO_CACHE = "no-cache";

    /** The Constant ETAG. */
    private static final String ETAG = "ETag";

    /** The Constant RESERVED_HEADERS. */
    private static final Set<String> RESERVED_HEADERS = Set.of(ACCEPT_RANGES.toLowerCase(),
            CONTENT_DISPOSITION.toLowerCase(), ETAG.toLowerCase(), LAST_MODIFIED.toLowerCase());

    /**
     * Get version.
     *
     * @param ui
     *            the ui
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mover/getVersion")
    public Response version(@Context final UriInfo ui) {
        _log.debug("REST received request: getVersion");
        checkIsControlChannel(ui);
        try {
            final var message = RESTMessage.getSuccessMessage();
            message.put("version", Version.getFullVersion());
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("getVersion - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("getVersion", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Del.
     *
     * @param ui
     *            the ui
     * @param dataFile
     *            the data file
     *
     * @return the response
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mover/del")
    public Response del(@Context final UriInfo ui, final DataFile dataFile) {
        _log.debug("REST received request: del({})", dataFile);
        checkIsControlChannel(ui);
        try {
            checkParameter("dataFile", dataFile);
            return mover.del(dataFile) ? RESTMessage.getSuccessMessage().getResponse()
                    : RESTMessage.getErrorMessage("DataFile not deleted").getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("del - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("del", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Close.
     *
     * @param ui
     *            the ui
     * @param dataTransfer
     *            the data transfer
     *
     * @return the response
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mover/close")
    public Response close(@Context final UriInfo ui, final DataTransfer dataTransfer) {
        _log.debug("REST received request: close({})", dataTransfer);
        checkIsControlChannel(ui);
        try {
            checkParameter("dataTransfer", dataTransfer);
            return mover.closeDataTransfer(dataTransfer) ? RESTMessage.getSuccessMessage().getResponse()
                    : RESTMessage.getErrorMessage("DataTransfer not closed").getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("close - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("close", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Purge.
     *
     * @param ui
     *            the ui
     * @param directories
     *            the directories
     *
     * @return the response
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mover/purge")
    public Response purge(@Context final UriInfo ui, final List<ExistingStorageDirectory> directories) {
        _log.debug("REST received request: purge({})",
                directories != null ? directories.size() + " directorie(s)" : "no-directory");
        checkIsControlChannel(ui);
        checkParameter("directories", directories);
        try {
            mover.purge(new ArrayList<>(directories));
            return RESTMessage.getSuccessMessage().getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("purge - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("purge", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Puts the.
     *
     * @param ui
     *            the ui
     * @param request
     *            the request
     *
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mover/put")
    public Response put(@Context final UriInfo ui, final PutRequest request) {
        _log.debug("REST received request: put({})", request);
        checkIsControlChannel(ui);
        checkParameter("transfer", request.transfer);
        checkParameter("fileName", request.fileName);
        checkParameter("localPosn", request.localPosn);
        checkParameter("remotePosn", request.remotePosn);
        try {
            mover.put(new Host[0], request.transfer, request.fileName, request.localPosn, request.remotePosn);
            final var message = RESTMessage.getSuccessMessage();
            message.put("root", mover.getRoot());
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("put - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("put", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Gets the host report.
     *
     * @param ui
     *            the ui
     * @param host
     *            the host
     *
     * @return the host report
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mover/getHostReport")
    public Response getHostReport(@Context final UriInfo ui, final Host host) {
        _log.debug("REST received request: getHostReport({})", host);
        checkIsControlChannel(ui);
        checkParameter("host", host);
        try {
            final var message = RESTMessage.getSuccessMessage();
            message.put("report", mover.getReport(host));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("getHostReport - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("getHostReport", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Gets the mover report.
     *
     * @param ui
     *            the ui
     *
     * @return the mover report
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mover/getMoverReport")
    public Response getMoverReport(@Context final UriInfo ui) {
        _log.debug("REST received request: getMoverReport()");
        checkIsControlChannel(ui);
        try {
            final var message = RESTMessage.getSuccessMessage();
            message.put("report", mover.getReport());
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("getMoverReport - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("getMoverReport", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Gets the ecauth token.
     *
     * @param ui
     *            the ui
     * @param user
     *            the user
     *
     * @return the ecauth token
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("master/getECauthToken")
    public Response getECauthToken(@Context final UriInfo ui, @QueryParam("user") final String user) {
        _log.debug("REST received request: getECauthToken({})", user);
        checkIsControlChannel(ui);
        checkParameter("user", user);
        try {
            final var message = RESTMessage.getSuccessMessage();
            message.put("ecauthToken", mover.getECauthToken(user));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("getECauthToken - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("getECauthToken", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Checks if is valid data file.
     *
     * @param ui
     *            the ui
     * @param dataFileId
     *            the data file id
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("master/isValidDataFile")
    public Response isValidDataFile(@Context final UriInfo ui, @QueryParam("dataFileId") final Long dataFileId) {
        _log.debug("REST received request: isValidDataFile({})", dataFileId);
        checkIsControlChannel(ui);
        checkParameter("dataFileId", dataFileId);
        try {
            final var message = RESTMessage.getSuccessMessage();
            message.put("isValid", mover.getMasterInterface().isValidDataFile(true, dataFileId));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("isValidDataFile - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("isValidDataFile", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Proxy host is alive.
     *
     * @param ui
     *            the ui
     * @param name
     *            the name
     *
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("master/proxyHostIsAlive")
    public Response proxyHostIsAlive(@Context final UriInfo ui, final String name) {
        _log.debug("REST received request: proxyHostIsAlive({})", name);
        checkIsControlChannel(ui);
        checkParameter("name", name);
        try {
            final var message = RESTMessage.getSuccessMessage();
            message.put("restartTime", mover.getMasterProxy().proxyHostIsAlive(name));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("proxyHostIsAlive - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("proxyHostIsAlive", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Update data.
     *
     * @param ui
     *            the ui
     * @param request
     *            the request
     *
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("master/updateDataRequest")
    public Response updateDataRequest(@Context final UriInfo ui, final UpdateDataRequest request) {
        _log.debug("REST received request: updateDataRequest({})", request);
        checkIsControlChannel(ui);
        checkParameter("hostId", request.hostId);
        checkParameter("data", request.data);
        try {
            mover.getMasterProxy().updateData(request.hostId, request.data);
            return RESTMessage.getSuccessMessage().getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("updateDataRequest - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("updateDataRequest", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Update data.
     *
     * @param ui
     *            the ui
     * @param host
     *            the host
     *
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("master/updateData")
    public Response updateData(@Context final UriInfo ui, final Host host) {
        _log.debug("REST received request: updateData({})", host);
        checkIsControlChannel(ui);
        checkParameter("host", host);
        try {
            mover.getMasterProxy().updateData(host);
            return RESTMessage.getSuccessMessage().getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("updateData - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("updateData", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Update location.
     *
     * @param ui
     *            the ui
     * @param host
     *            the host
     *
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("master/updateLocation")
    public Response updateLocation(@Context final UriInfo ui, final Host host) {
        _log.debug("REST received request: updateLocation({})", host);
        checkIsControlChannel(ui);
        checkParameter("host", host);
        try {
            mover.getMasterProxy().updateLocation(host);
            return RESTMessage.getSuccessMessage().getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("updateLocation - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("updateLocation", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Update data transfers.
     *
     * @param ui
     *            the ui
     * @param transfers
     *            the transfers
     *
     * @return the response
     */
    @SuppressWarnings("null")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("master/updateDataTransfers")
    public Response updateDataTransfers(@Context final UriInfo ui, final List<DataTransfer> transfers) {
        _log.debug("REST received request: updateDataTransfers({})",
                transfers != null ? transfers.size() + " transfer(s)" : "no-transfer");
        checkIsControlChannel(ui);
        checkParameter("transfers", transfers);
        try {
            mover.getMasterProxy().updateDataTransfers(transfers.toArray(new DataTransfer[0]));
            return RESTMessage.getSuccessMessage().getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("updateDataTransfers - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("updateDataTransfers", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Sends the message.
     *
     * @param ui
     *            the ui
     * @param request
     *            the request
     *
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("master/sendMessage")
    public Response sendMessage(@Context final UriInfo ui, final MonitorRequest request) {
        _log.debug("REST received request: sendMessage({})", request);
        checkIsControlChannel(ui);
        checkParameter("name", request.name);
        checkParameter("service", request.service);
        checkParameter("status", request.status);
        checkParameter("message", request.message);
        try {
            mover.getMasterProxy().sendMessage(request.name, request.service, request.status, request.message);
            return RESTMessage.getSuccessMessage().getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("sendMessage - {}", describe(w));
            throw w;
        } catch (final Throwable t) {
            _log.warn("sendMessage", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    // End-users methods

    /**
     * File get (if directory then list otherwise download).
     *
     * @param ui
     *            the ui
     * @param user
     *            the user
     * @param range
     *            the range
     * @param request
     *            the request
     * @param response
     *            the response
     *
     * @return the input stream
     */
    @HEAD
    @Path("home/{user}")
    public Response homeHead(@Context final UriInfo ui, @PathParam("user") final String user,
            @HeaderParam("range") final String range, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response) {
        return fileHead(ui, getBasicAuth(user + ":" + user), range, request, response, "");
    }

    /**
     * File get (if directory then list otherwise download).
     *
     * @param ui
     *            the ui
     * @param user
     *            the user
     * @param range
     *            the range
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filename
     *            the filename
     *
     * @return the input stream
     */
    @HEAD
    @Path("home/{user}/{filename: .*}")
    public Response homeHead(@Context final UriInfo ui, @PathParam("user") final String user,
            @HeaderParam("range") final String range, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response, @PathParam("filename") final String filename) {
        return fileHead(ui, getBasicAuth(user + ":" + user), range, request, response, filename);
    }

    /**
     * CORS preflight for home directory listing.
     *
     * @param user
     *            the user
     * @param request
     *            the request
     * @param response
     *            the response
     *
     * @return the response
     */
    @OPTIONS
    @Path("home/{user}")
    public Response homeOptions(@PathParam("user") final String user, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response) {
        _log.debug("REST received request: homeOptions({})", user);
        final var session = getUserSession(getBasicAuth(user + ":" + user), request, response);
        try {
            final var builder = Response.noContent();
            addCorsHeaders(session, request, builder);
            builder.header("Access-Control-Max-Age", "86400");
            return builder.build();
        } finally {
            if (session != null) {
                session.close(true);
            }
        }
    }

    /**
     * CORS preflight for home file download.
     *
     * @param user
     *            the user
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filename
     *            the filename
     *
     * @return the response
     */
    @OPTIONS
    @Path("home/{user}/{filename: .*}")
    public Response homeOptions(@PathParam("user") final String user, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response, @PathParam("filename") final String filename) {
        _log.debug("REST received request: homeOptions({}/{})", user, filename);
        final var session = getUserSession(getBasicAuth(user + ":" + user), request, response);
        try {
            final var builder = Response.noContent();
            addCorsHeaders(session, request, builder);
            builder.header("Access-Control-Max-Age", "86400");
            return builder.build();
        } finally {
            if (session != null) {
                session.close(true);
            }
        }
    }

    /**
     * File get (if directory then list otherwise download).
     *
     * @param ui
     *            the ui
     * @param authString
     *            the auth string
     * @param range
     *            the range
     * @param request
     *            the request
     * @param response
     *            the response
     *
     * @return the input stream
     */
    @HEAD
    @Path("file")
    public Response fileHead(@Context final UriInfo ui, @HeaderParam("authorization") final String authString,
            @HeaderParam("range") final String range, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response) {
        return fileHead(ui, authString, range, request, response, "");
    }

    /**
     * File get (if directory then list otherwise download).
     *
     * @param ui
     *            the ui
     * @param authString
     *            the auth string
     * @param range
     *            the range
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filename
     *            the filename
     *
     * @return the input stream
     */
    @HEAD
    @Path("file/{filename: .*}")
    public Response fileHead(@Context final UriInfo ui, @HeaderParam("authorization") final String authString,
            @HeaderParam("range") final String range, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response, @PathParam("filename") final String filename) {
        _log.debug("REST received request: fileHead({})", filename);
        checkIsControlChannel(ui);
        final var session = getUserSession(authString, request, response);
        try {
            final var path = getFilename(session, filename);
            // Check if this is a list or get request?
            final var list = session.getFileListElement(path);
            if (!list.isDirectory()) {
                return dataFileHead(ui, authString, range, request, response, filename);
            }
            // Let's treat it as a list request!
            final var elements = session.getFileList(path);
            final var biggerIndexes = getBiggerIndexes(elements);
            final var builder = Response.ok();
            builder.header(CONTENT_TYPE, MediaType.TEXT_HTML);
            builder.lastModified(new Date(biggerIndexes[3]));
            builder.header(CACHE_CONTROL, NO_CACHE);
            return builder.build();
        } catch (final WebApplicationException w) {
            _log.warn("fileHead - {}", describe(w));
            throw w;
        } catch (final FileNotFoundException e) {
            _log.warn("fileHead", e);
            throw newException(e, 404, "Not Found: " + e.getMessage());
        } catch (final EccmdException e) {
            _log.warn("fileHead", e);
            final var message = e.getMessage();
            if (message.contains("File not found") || message.contains("Destination not found")) {
                throw newException(e, 404, "Not Found: " + message);
            }
            throw newException(e, 500, message);
        } catch (final Throwable t) {
            _log.warn("fileHead", t);
            throw newException(t, 500, "Internal server error");
        } finally {
            if (session != null) {
                session.close(true);
            }
        }
    }

    /**
     * File get (if directory then list otherwise download).
     *
     * @param ui
     *            the ui
     * @param user
     *            the user
     * @param range
     *            the range
     * @param request
     *            the request
     * @param response
     *            the response
     *
     * @return the input stream
     */
    @GET
    @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN,
            MediaType.APPLICATION_JSON, "text/csv" })
    @Path("home/{user}")
    public Response homeGet(@Context final UriInfo ui, @PathParam("user") final String user,
            @HeaderParam("range") final String range, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response, @Context final HttpHeaders headers) {
        return fileGet(ui, getBasicAuth(user + ":" + user), range, request, response, headers, "");
    }

    /**
     * File get (if directory then list otherwise download).
     *
     * @param ui
     *            the ui
     * @param user
     *            the user
     * @param range
     *            the range
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filename
     *            the filename
     *
     * @return the input stream
     */
    @GET
    @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN,
            MediaType.APPLICATION_JSON, "text/csv" })
    @Path("home/{user}/{filename: .*}")
    public Response homeGet(@Context final UriInfo ui, @PathParam("user") final String user,
            @HeaderParam("range") final String range, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response, @Context final HttpHeaders headers,
            @PathParam("filename") final String filename) {
        return fileGet(ui, getBasicAuth(user + ":" + user), range, request, response, headers, filename);
    }

    /**
     * File get (if directory then list otherwise download).
     *
     * @param ui
     *            the ui
     * @param authString
     *            the auth string
     * @param range
     *            the range
     * @param request
     *            the request
     * @param response
     *            the response
     *
     * @return the input stream
     */
    @GET
    @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
    @Path("file")
    public Response fileGet(@Context final UriInfo ui, @HeaderParam("authorization") final String authString,
            @HeaderParam("range") final String range, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response, @Context final HttpHeaders headers) {
        return fileGet(ui, authString, range, request, response, headers, "");
    }

    /**
     * File get (if directory then list otherwise download).
     *
     * @param ui
     *            the ui
     * @param authString
     *            the auth string
     * @param range
     *            the range
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filename
     *            the filename
     *
     * @return the input stream
     */
    @GET
    @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
    @Path("file/{filename: .*}")
    public Response fileGet(@Context final UriInfo ui, @HeaderParam("authorization") final String authString,
            @HeaderParam("range") final String range, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response, @Context final HttpHeaders headers,
            @PathParam("filename") final String filename) {
        _log.debug("REST received request: fileGet({})", filename);
        checkIsControlChannel(ui);
        final var session = getUserSession(authString, request, response);
        // When file-serving is delegated, serveDataFile takes full ownership of the
        // session
        // (closes on failure, transfers to streamer on success) — do not close here.
        var sessionTransferred = false;
        try {
            final var path = getFilename(session, filename);
            // Check if this is a list or get request?
            final var list = session.getFileListElement(path);
            if (!list.isDirectory()) {
                sessionTransferred = true;
                return serveDataFile(session, request, filename);
            }
            // Let's treat it as a list request!
            final var setup = session.getECtransSetup();
            if (setup != null && !setup.getBoolean(ECtransOptions.USER_PORTAL_SIMPLE_LIST)) {
                // Send the full html page (or negotiated format)
                return dataListGet(ui, authString, request, response, headers, filename);
            }
            // Only send a simple text list
            final var builder = Response.ok();
            builder.header(CONTENT_TYPE, MediaType.TEXT_PLAIN);
            builder.header(CACHE_CONTROL, NO_CACHE);
            final var elements = session.getFileList(path);
            final StreamingOutput output = os -> {
                for (final var element : elements) {
                    os.write((element.getName() + (element.isDirectory() ? "/" : "") + "\n")
                            .getBytes(StandardCharsets.UTF_8));
                }
            };
            return builder.entity(output).lastModified(new Date(getBiggerIndexes(elements)[3])).build();
        } catch (final WebApplicationException w) {
            _log.warn("fileGet - {}", describe(w));
            throw w;
        } catch (final FileNotFoundException e) {
            _log.warn("fileGet", e);
            throw newException(e, 404, "Not Found: " + e.getMessage());
        } catch (final EccmdException e) {
            _log.warn("fileGet", e);
            final var message = e.getMessage();
            if (message.contains("File not found") || message.contains("Destination not found")) {
                throw newException(e, 404, "Not Found: " + message);
            }
            throw newException(e, 500, message);
        } catch (final Throwable t) {
            _log.warn("fileGet", t);
            throw newException(t, 500, "Internal server error");
        } finally {
            if (!sessionTransferred && session != null) {
                session.close(true);
            }
        }
    }

    /**
     * Data file options (CORS preflight).
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filename
     *            the filename
     *
     * @return the response
     */
    @OPTIONS
    @Path("data/file/{filename: .*}")
    public Response dataFileOptions(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @Context final HttpServletResponse response,
            @PathParam("filename") final String filename) {
        _log.debug("REST received request: dataFileOptions({})", filename);
        final var session = getUserSession(authString, request, response);
        try {
            final var builder = Response.noContent();
            addCorsHeaders(session, request, builder);
            builder.header("Access-Control-Max-Age", "86400");
            return builder.build();
        } finally {
            if (session != null) {
                session.close(true);
            }
        }
    }

    /**
     * Data file head.
     *
     * @param ui
     *            the ui
     * @param authString
     *            the auth string
     * @param range
     *            the range
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filename
     *            the filename
     *
     * @return the input stream
     */
    @HEAD
    @Path("data/file/{filename: .*}")
    public Response dataFileHead(@Context final UriInfo ui, @HeaderParam("authorization") final String authString,
            @HeaderParam("range") final String range, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response, @PathParam("filename") final String filename) {
        _log.debug("REST received request: dataFileHead({})", filename);
        checkIsControlChannel(ui);
        final var session = getUserSession(authString, request, response);
        try {
            checkParameter("filename", filename);
            final var builder = Response.ok();
            final var mediaRequest = processGet(session, request, builder, filename);
            addCorsHeaders(session, request, builder);
            return builder.header(CONTENT_LENGTH, mediaRequest.size).build();
        } catch (final WebApplicationException w) {
            _log.warn("dataFileHead - {}", describe(w));
            throw w;
        } catch (final FileNotFoundException e) {
            _log.warn("dataFileHead", e);
            throw newException(e, 404, "Not Found: " + e.getMessage());
        } catch (final EccmdException e) {
            _log.warn("dataFileHead", e);
            final var message = e.getMessage();
            if (message.contains("File not found") || message.contains("Destination not found")) {
                throw newException(e, 404, "Not Found: " + message);
            }
            throw newException(e, 500, message);
        } catch (final Throwable t) {
            _log.warn("dataFileHead", t);
            throw newException(t, 500, "Internal server error");
        } finally {
            if (session != null) {
                session.close(true);
            }
        }
    }

    /**
     * Data file delete.
     *
     * @param ui
     *            the ui
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filename
     *            the filename
     *
     * @return the response
     */
    @DELETE
    @Path("data/file/{filename: .*}")
    public Response dataFileDelete(@Context final UriInfo ui, @HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @Context final HttpServletResponse response,
            @PathParam("filename") final String filename) {
        _log.debug("REST received request: dataFileDelete({})", filename);
        checkIsControlChannel(ui);
        final var session = getUserSession(authString, request, response);
        try {
            checkParameter("filename", filename);
            session.deleteFile(processGet(session, request, null, filename).name, true);
            return Response.ok().build();
        } catch (final WebApplicationException w) {
            _log.warn("dataFileDelete - {}", describe(w));
            throw w;
        } catch (final FileNotFoundException e) {
            _log.warn("dataFileDelete", e);
            throw newException(e, 404, "Not Found: " + e.getMessage());
        } catch (final EccmdException e) {
            _log.warn("dataFileDelete", e);
            final var message = e.getMessage();
            if (message.contains("File not found") || message.contains("Destination not found")) {
                throw newException(e, 404, "Not Found: " + message);
            }
            throw newException(e, 500, message);
        } catch (final Throwable t) {
            _log.warn("dataFileDelete", t);
            throw newException(t, 500, "Internal server error");
        } finally {
            if (session != null) {
                session.close(true);
            }
        }
    }

    /**
     * Data file get.
     *
     * @param ui
     *            the ui
     * @param authString
     *            the auth string
     * @param range
     *            the range
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filename
     *            the filename
     *
     * @return the input stream
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("data/file/{filename: .*}")
    public Response dataFileGet(@Context final UriInfo ui, @HeaderParam("authorization") final String authString,
            @HeaderParam("range") final String range, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response, @PathParam("filename") final String filename) {
        _log.debug("REST received request: dataFileGet({})", filename);
        checkIsControlChannel(ui);
        final var session = getUserSession(authString, request, response);
        // serveDataFile owns the session lifecycle: closes on failure, transfers to
        // streamer on success.
        return serveDataFile(session, request, filename);
    }

    /**
     * Data file post.
     *
     * @param ui
     *            the ui
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filename
     *            the filename
     * @param in
     *            the input stream
     *
     * @return the status message
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Path("data/file/{filename: .*}")
    public Response dataFilePost(@Context final UriInfo ui, @HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @Context final HttpServletResponse response,
            @PathParam("filename") final String filename, final InputStream in) {
        _log.debug("REST received request: dataFilePost({})", filename);
        checkIsControlChannel(ui);
        final var session = getUserSession(authString, request, response);
        StreamPlugThread plug = null;
        OutputStream out = null;
        ProxySocket proxy = null;
        try {
            checkParameter("filename", filename);
            final var name = getFilename(session, filename);
            proxy = session.getProxySocketOutput(name, 0, 640);
            plug = new StreamPlugThread(in, out = proxy.getDataOutputStream());
            plug.configurableRun();
            final var message = plug.getMessage();
            plug.close();
            out.close();
            proxy.close();
            session.check(proxy);
            if (message != null) {
                throw newException(new Exception(message), 500, message);
            }
            final var element = session.getFileListElement(name);
            final var etag = getETag(element.getComment());
            return Response.ok("Upload complete (id: " + element.getComment() + ", ETag: " + etag + ")")
                    .type(MediaType.TEXT_PLAIN).header("ETag", etag).build();
        } catch (final WebApplicationException w) {
            _log.warn("dataFilePost - {}", describe(w));
            throw w;
        } catch (final FileNotFoundException e) {
            _log.warn("dataFilePost", e);
            throw newException(e, 404, "Not Found:" + e.getMessage());
        } catch (final EccmdException e) {
            _log.warn("dataFilePost", e);
            final var message = e.getMessage();
            if (message.contains("File not found") || message.contains("Destination not found")) {
                throw newException(e, 404, "Not Found: " + message);
            }
            throw newException(e, 500, message);
        } catch (final Throwable t) {
            _log.warn("dataFilePost", t);
            throw newException(t, 500, Format.getMessage(t));
        } finally {
            StreamPlugThread.closeQuietly(plug);
            StreamPlugThread.closeQuietly(out);
            StreamPlugThread.closeQuietly(proxy);
            if (session != null) {
                session.close(true);
            }
        }
    }

    /**
     * Data list get.
     *
     * @param ui
     *            the ui
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filename
     *            the filename
     *
     * @return the listing
     */
    @GET
    @Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, "text/csv" })
    @Path("data/list/{filename: .*}")
    public Response dataListGet(@Context final UriInfo ui, @HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @Context final HttpServletResponse response,
            @Context final HttpHeaders headers, @PathParam("filename") final String filename) {
        _log.debug("REST received request: dataListGet({})", filename);
        checkIsControlChannel(ui);
        final var session = getUserSession(authString, request, response);
        try {
            final var path = getFilename(session, filename);
            // Check if the file or directory exists?
            final var list = session.getFileListElement(path);
            if (!list.isDirectory() && list.getSize() == null) {
                throw new FileNotFoundException("No such file or directory");
            }
            final var elements = session.getFileList(path);
            final var biggerIndexes = getBiggerIndexes(elements);
            final var lastModified = new Date(biggerIndexes[3]);
            // Determine the response format from the Accept header (HTML is the default).
            final var fmt = resolveListFormat(headers.getAcceptableMediaTypes());
            if ("json".equals(fmt)) {
                return buildJsonListing(elements, lastModified);
            }
            if ("text".equals(fmt)) {
                return buildTextListing(elements, lastModified);
            }
            if ("csv".equals(fmt)) {
                return buildCsvListing(elements, lastModified);
            }
            // HTML (default)
            // Load the appropriate template!
            final var setup = session.getECtransSetup();
            final var anonymous = "open-access".equals(session.getPortalService());
            final var sb = new StringBuilder().append(anonymous ? getTemplateContent(homeContent, HOME_FILE)
                    : getTemplateContent(portalContent, PORTAL_FILE));
            // Fill the parameters in the template
            final var title = System.getProperty("mover.title", "Data Store for Acquisition & Dissemination");
            final var tab = System.getProperty("mover.tab", title);
            final var footer = System.getProperty("mover.footer",
                    "Powered by <a href=\"https://github.com/ecmwf/open-ecpds\" target=\"_blank\">OpenECPDS</a>");
            final var color = System.getProperty("mover.color", "#000000");
            final var warning = System.getProperty("mover.warning", "");
            final var msgTop = System.getProperty("mover.msgTop", "");
            final var msgDown = System.getProperty("mover.msgDown", "");
            Format.replaceAll(sb, "${tab}", setup != null ? setup.get(ECtransOptions.USER_PORTAL_TAB, tab) : tab);
            Format.replaceAll(sb, "${title}",
                    setup != null ? setup.get(ECtransOptions.USER_PORTAL_TITLE, title) : title);
            Format.replaceAll(sb, "${footer}",
                    setup != null ? setup.get(ECtransOptions.USER_PORTAL_FOOTER, footer) : footer);
            Format.replaceAll(sb, "${color}",
                    setup != null ? setup.get(ECtransOptions.USER_PORTAL_COLOR, color) : color);
            Format.replaceAll(sb, "${warning}",
                    setup != null ? setup.get(ECtransOptions.USER_PORTAL_WARNING, warning) : warning);
            Format.replaceAll(sb, "${msgTop}",
                    setup != null ? setup.get(ECtransOptions.USER_PORTAL_MSG_TOP, msgTop) : msgTop);
            Format.replaceAll(sb, "${msgDown}",
                    setup != null ? setup.get(ECtransOptions.USER_PORTAL_MSG_DOWN, msgDown) : msgDown);
            final var accessGuide = setup == null || setup.getBoolean(ECtransOptions.USER_PORTAL_ACCESS_GUIDE);
            Format.replaceAll(sb, "${accessGuide}", String.valueOf(accessGuide));
            final var loginButton = setup == null || setup.getBoolean(ECtransOptions.USER_PORTAL_LOGIN_BUTTON);
            Format.replaceAll(sb, "${loginButtonHidden}", loginButton ? "" : "d-none");
            final var registrationEnabled = "self-service".equals(session.getPortalService());
            Format.replaceAll(sb, "${registerLinkHidden}", registrationEnabled ? "" : "d-none");
            final var trafficStats = setup == null || setup.getBoolean(ECtransOptions.USER_PORTAL_TRAFFIC_STATS);
            Format.replaceAll(sb, "${trafficStats}", String.valueOf(trafficStats));
            final var userId = session.getUser();
            Format.replaceAll(sb, "${userid}", userId);
            if (trafficStats) {
                try {
                    final List<PortalTraffic> traffic = mover.getMasterInterface().getPortalTraffic(userId, 24);
                    final var tsb = new StringBuilder("[");
                    var tFirst = true;
                    for (final var pt : traffic) {
                        if (!tFirst)
                            tsb.append(",");
                        tFirst = false;
                        tsb.append("{\"time\":\"").append(pt.getTime()).append("\"").append(",\"connections\":")
                                .append(pt.getConnections()).append(",\"bytesIn\":").append(pt.getBytesIn())
                                .append(",\"bytesOut\":").append(pt.getBytesOut()).append(",\"durationIn\":")
                                .append(pt.getDurationIn()).append(",\"durationOut\":").append(pt.getDurationOut())
                                .append("}");
                    }
                    tsb.append("]");
                    Format.replaceAll(sb, "${trafficData}", tsb.toString());
                } catch (final Throwable t) {
                    _log.warn("getPortalTraffic inline", t);
                    Format.replaceAll(sb, "${trafficData}", "[]");
                }
            } else {
                Format.replaceAll(sb, "${trafficData}", "[]");
            }
            Format.replaceAll(sb, "${s3CanWrite}", String.valueOf(session.hasPermission("put")));
            Format.replaceAll(sb, "${s3CanDelete}", String.valueOf(session.hasPermission("delete")));
            final var destination = setup != null
                    ? setup.getOptionalString(ECtransOptions.USER_PORTAL_DESTINATION).orElse("") : "";
            Format.replaceAll(sb, "${destination}", destination);
            final var maxConnections = setup != null ? setup.getInteger(ECtransOptions.USER_PORTAL_MAX_CONNECTIONS)
                    : -1;
            Format.replaceAll(sb, "${maxconnections}", maxConnections > 0 ? String.valueOf(maxConnections) : "");
            final var maxConnectionsSchedule = setup != null
                    ? setup.getString(ECtransOptions.USER_PORTAL_MAX_CONNECTIONS_SCHEDULE) : "";
            Format.replaceAll(sb, "${maxConnectionsSchedule}",
                    maxConnectionsSchedule != null ? maxConnectionsSchedule : "");
            if (maxConnections > 0) {
                try {
                    final var currentConnections = mover.getMasterInterface().getIncomingConnectionCount(userId);
                    Format.replaceAll(sb, "${currentconnections}",
                            currentConnections >= 0 ? String.valueOf(currentConnections) : "");
                } catch (final Throwable t) {
                    _log.warn("getIncomingConnectionCount", t);
                    Format.replaceAll(sb, "${currentconnections}", "");
                }
            } else {
                Format.replaceAll(sb, "${currentconnections}", "");
            }
            // Upload byte quota
            final var maxUploadBytes = setup != null
                    ? setup.getByteSize(ECtransOptions.USER_PORTAL_MAX_UPLOAD_BYTES).size() : 0L;
            final var uploadPeriodMs = setup != null ? setup
                    .getOptionalDuration(ECtransOptions.USER_PORTAL_UPLOAD_PERIOD).map(Duration::toMillis).orElse(0L)
                    : 0L;
            if (maxUploadBytes > 0 && uploadPeriodMs > 0) {
                Format.replaceAll(sb, "${maxUploadBytes}", String.valueOf(maxUploadBytes));
                Format.replaceAll(sb, "${uploadPeriodMinutes}", String.valueOf(uploadPeriodMs / 60_000L));
                try {
                    final var used = mover.getMasterInterface().getPortalBytesUsed(userId, true, uploadPeriodMs);
                    Format.replaceAll(sb, "${currentUploadBytes}", String.valueOf(used));
                } catch (final Throwable t) {
                    _log.warn("getPortalBytesUsed(upload)", t);
                    Format.replaceAll(sb, "${currentUploadBytes}", "");
                }
            } else {
                Format.replaceAll(sb, "${maxUploadBytes}", "");
                Format.replaceAll(sb, "${uploadPeriodMinutes}", "");
                Format.replaceAll(sb, "${currentUploadBytes}", "");
            }
            // Download byte quota
            final var maxDownloadBytes = setup != null
                    ? setup.getByteSize(ECtransOptions.USER_PORTAL_MAX_DOWNLOAD_BYTES).size() : 0L;
            final var downloadPeriodMs = setup != null ? setup
                    .getOptionalDuration(ECtransOptions.USER_PORTAL_DOWNLOAD_PERIOD).map(Duration::toMillis).orElse(0L)
                    : 0L;
            if (maxDownloadBytes > 0 && downloadPeriodMs > 0) {
                Format.replaceAll(sb, "${maxDownloadBytes}", String.valueOf(maxDownloadBytes));
                Format.replaceAll(sb, "${downloadPeriodMinutes}", String.valueOf(downloadPeriodMs / 60_000L));
                try {
                    final var used = mover.getMasterInterface().getPortalBytesUsed(userId, false, downloadPeriodMs);
                    Format.replaceAll(sb, "${currentDownloadBytes}", String.valueOf(used));
                } catch (final Throwable t) {
                    _log.warn("getPortalBytesUsed(download)", t);
                    Format.replaceAll(sb, "${currentDownloadBytes}", "");
                }
            } else {
                Format.replaceAll(sb, "${maxDownloadBytes}", "");
                Format.replaceAll(sb, "${downloadPeriodMinutes}", "");
                Format.replaceAll(sb, "${currentDownloadBytes}", "");
            }
            final String uriList;
            final String uriGet;
            if (request.getAttribute("original-target") instanceof final String originalTarget) {
                uriList = originalTarget;
                uriGet = originalTarget;
            } else {
                uriList = "/ecpds/" + (anonymous ? "home/" + userId : "data/list");
                uriGet = "/ecpds/" + (anonymous ? "home/" + userId : "data/file");
            }
            setTemplateParameters(sb, uriList, filename, biggerIndexes);
            final var listing = new StringBuilder();
            for (final FileListElement element : elements) {
                addListElement(listing, uriList, uriGet, filename, element);
            }
            Format.replaceAll(sb, "${listing}", listing.toString());
            final var builder = Response.ok(sb.toString(), MediaType.TEXT_HTML);
            builder.lastModified(lastModified);
            builder.header(CACHE_CONTROL, NO_CACHE);
            return builder.build();
        } catch (final WebApplicationException w) {
            _log.warn("dataListGet - {}", describe(w));
            throw w;
        } catch (final FileNotFoundException e) {
            _log.debug("dataListGet", e);
            throw newException(e, 404, "Not Found: " + e.getMessage());
        } catch (final EccmdException e) {
            _log.debug("dataListGet", e);
            final var message = e.getMessage();
            if (message.contains("File not found") || message.contains("Destination not found")) {
                throw newException(e, 404, "Not Found: " + message);
            }
            throw newException(new Exception(message), 500, message);
        } catch (final Throwable t) {
            _log.debug("dataListGet", t);
            throw newException(t, 500, Format.getMessage(t));
        } finally {
            if (session != null) {
                session.close(true);
            }
        }
    }

    /**
     * Data list head.
     *
     * @param ui
     *            the ui
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filename
     *            the filename
     */
    @HEAD
    @Path("data/list/{filename: .*}")
    public void dataListHead(@Context final UriInfo ui, @HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @Context final HttpServletResponse response,
            @PathParam("filename") final String filename) {
        _log.debug("REST received request: dataListHead({})", filename);
        checkIsControlChannel(ui);
        final var session = getUserSession(authString, request, response);
        try {
            final var path = getFilename(session, filename);
            // Check if the file or directory exists?
            final var list = session.getFileListElement(path);
            if (!list.isDirectory() && list.getSize() == null) {
                throw new FileNotFoundException("No such file or directory");
            }
            response.addDateHeader(LAST_MODIFIED, getBiggerIndexes(session.getFileList(path))[3]);
        } catch (final WebApplicationException w) {
            _log.warn("dataListHead - {}", describe(w));
            throw w;
        } catch (final FileNotFoundException e) {
            _log.warn("dataListHead", e);
            throw newException(e, 404, "Not Found: " + e.getMessage());
        } catch (final EccmdException e) {
            _log.warn("dataListHead", e);
            final var message = e.getMessage();
            if (message.contains("File not found") || message.contains("Destination not found")) {
                throw newException(e, 404, "Not Found: " + message);
            }
            throw newException(e, 500, message);
        } catch (final Throwable t) {
            _log.warn("dataListHead", t);
            throw newException(t, 500, Format.getMessage(t));
        } finally {
            if (session != null) {
                session.close(true);
            }
        }
    }

    /**
     * Logout endpoint — clears the portal session cookie and evicts the token from the cache, then flushes any
     * browser-cached Basic Auth credentials and redirects to the given {@code next} path (default: {@code file}).
     *
     * Registration page — serves the self-service account request form (no authentication required).
     *
     * @param request
     *            the HTTP request
     * @param response
     *            the HTTP response
     *
     * @return the registration HTML page
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("register")
    public Response registerGet(@Context final HttpServletRequest request,
            @Context final HttpServletResponse response) {
        _log.debug("REST received request: registerGet");
        try {
            final var sb = new StringBuilder().append(getTemplateContent(registerContent, REGISTER_FILE));
            final var title = System.getProperty("mover.title", "Data Store for Acquisition & Dissemination");
            final var tab = System.getProperty("mover.tab", title);
            final var footer = System.getProperty("mover.footer",
                    "Powered by <a href=\"https://github.com/ecmwf/open-ecpds\" target=\"_blank\">OpenECPDS</a>");
            final var color = System.getProperty("mover.color", "#000000");
            Format.replaceAll(sb, "${tab}", tab);
            Format.replaceAll(sb, "${title}", title);
            Format.replaceAll(sb, "${footer}", footer);
            Format.replaceAll(sb, "${color}", color);
            Format.replaceAll(sb, "${version}", Version.getVersion());
            Format.replaceAll(sb, "${build}", Version.getBuild());
            return Response.ok(sb.toString(), MediaType.TEXT_HTML).build();
        } catch (final Exception e) {
            _log.warn("registerGet", e);
            return Response.serverError().entity("Registration page unavailable").build();
        }
    }

    /**
     * Registration submit — creates a pending account and sends a verification email. Accepts JSON:
     * {@code {"id":"...","name":"...","email":"...","iso":"..."}}. Returns 200 with {@code {"status":"pending"}} on
     * success, or 400/500 with {@code {"message":"..."}} on failure. No authentication required.
     *
     * @param request
     *            the HTTP request
     * @param response
     *            the HTTP response
     * @param body
     *            the JSON request body
     *
     * @return JSON response
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("register")
    public Response registerPost(@Context final HttpServletRequest request, @Context final HttpServletResponse response,
            final String body) {
        _log.debug("REST received request: registerPost");
        try {
            final var mapper = new ObjectMapper();
            final var json = mapper.readValue(body, Map.class);
            // 'user' is the IncomingUser ID (e.g., "test") that this subscriber is
            // registering for
            final var user = trim(json.get("user"));
            final var name = trim(json.get("name"));
            final var email = trim(json.get("email"));
            final var iso = trim(json.get("iso"));
            if (user.isEmpty() || name.isEmpty() || email.isEmpty() || iso.isEmpty()) {
                return jsonError(400, "All fields are required");
            }
            // Create PortalSubscriber + get verification token via master RMI.
            // selfRegisterUser(id, name, email, iso) where id = the IncomingUser to
            // subscribe to.
            final String token;
            try {
                token = mover.getMasterInterface().selfRegisterUser(user, name, email, iso);
            } catch (final ecmwf.common.database.DataBaseException e) {
                return jsonError(400, e.getMessage());
            }
            // Resolve per-user registration settings from ECtrans options (with global
            // fallback for admin email)
            var registrationAdminEmail = Cnf.at("DataPortal", "registrationAdminEmail", "");
            var registrationAutoApprove = false;
            var registrationEmailExtraVerify = "";
            try {
                final var profile = mover.getMasterInterface().getIncomingProfileNoAuth(user);
                if (profile != null) {
                    final var setup = profile.getECtransSetup();
                    if (setup != null) {
                        final var perUserEmail = setup.getString(ECtransOptions.USER_PORTAL_REGISTRATION_ADMIN_EMAIL);
                        if (!perUserEmail.isEmpty()) {
                            registrationAdminEmail = perUserEmail;
                        }
                        registrationAutoApprove = setup
                                .getBoolean(ECtransOptions.USER_PORTAL_REGISTRATION_AUTO_APPROVE);
                        registrationEmailExtraVerify = setup
                                .getString(ECtransOptions.USER_PORTAL_REGISTRATION_EMAIL_EXTRA_VERIFY);
                    }
                }
            } catch (final Exception ignored) {
                // fall back to global Cnf settings
            }
            final var emailExtraHtml = (registrationEmailExtraVerify == null || registrationEmailExtraVerify.isEmpty())
                    ? "" : "<hr>" + registrationEmailExtraVerify;
            // Derive the verify URL from the exact URL the client used to reach this
            // endpoint.
            // Replacing /register at the end gives the correct external URL even behind a
            // reverse proxy (e.g. https://portal.example.com/ecpds/verify?token=...).
            final var verifyUrl = request.getRequestURL().toString().replaceFirst("/register$", "/verify") + "?token="
                    + token;
            // Send verification email to the registrant
            final var subject = "Verify your data portal registration";
            final var emailBody = "<p>Hello " + escapeHtml(name) + ",</p>"
                    + "<p>Thank you for registering for access to the data portal. "
                    + "Please click the link below to verify your email address and complete your registration:</p>"
                    + "<p><a href=\"" + verifyUrl + "\">" + verifyUrl + "</a></p>"
                    + "<p>Once verified, you will be able to log in to the data portal using:</p>"
                    + "<ul><li><strong>Username:</strong> " + escapeHtml(user) + "</li>"
                    + "<li><strong>Email:</strong> " + escapeHtml(email) + "</li></ul>"
                    + "<p>Your personal password will be sent in a follow-up email after your access is confirmed.</p>"
                    + "<p>This link expires in 24 hours. If you did not make this request, please ignore this email.</p>"
                    + emailExtraHtml;
            try {
                mover.getMasterInterface().sendNotificationEmail(email, subject, emailBody);
            } catch (final Exception e) {
                _log.warn("Failed to send verification email to {}", email, e);
            }
            // Notify admin
            if (!registrationAdminEmail.isEmpty()) {
                final var adminSubject = "New registration request for data user '" + user + "'";
                final var adminBody = "<p>A new subscriber registration has been submitted for data user <strong>"
                        + escapeHtml(user) + "</strong>:</p>" + "<ul><li><strong>Name:</strong> " + escapeHtml(name)
                        + "</li>" + "<li><strong>Email:</strong> " + escapeHtml(email) + "</li>"
                        + "<li><strong>Country:</strong> " + escapeHtml(iso) + "</li></ul>"
                        + "<p>The subscriber has been sent a verification email. Once verified, the account will be "
                        + (registrationAutoApprove ? "activated automatically."
                                : "pending your approval in the admin interface.")
                        + "</p>";
                try {
                    mover.getMasterInterface().sendNotificationEmail(registrationAdminEmail, adminSubject, adminBody);
                } catch (final Exception e) {
                    _log.warn("Failed to send admin notification email", e);
                }
            }
            return Response.ok("{\"status\":\"pending\"}", MediaType.APPLICATION_JSON).build();
        } catch (final Exception e) {
            _log.warn("registerPost", e);
            return jsonError(500, "Internal error. Please try again later.");
        }
    }

    /**
     * Email verification endpoint — called from the link in the verification email. If valid: auto-approves or marks
     * email as verified, then redirects to the register page with a {@code ?state=} query parameter indicating the
     * outcome.
     *
     * @param token
     *            the verification token from the email link
     * @param request
     *            the HTTP request
     * @param response
     *            the HTTP response
     *
     * @return redirect to the register page
     */
    @GET
    @Path("verify")
    public Response verifyGet(@QueryParam("token") final String token, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response) {
        _log.debug("REST received request: verifyGet");
        // Derive the register page redirect URL from the exact URL the client used to
        // reach /verify — replacing /verify with /register preserves the external base.
        final var registerBase = request.getRequestURL().toString().replaceFirst("/verify$", "/register") + "?state=";
        if (token == null || token.isBlank()) {
            return Response.seeOther(java.net.URI.create(registerBase + "invalid")).build();
        }
        try {
            final var result = mover.getMasterInterface().verifyRegistrationToken(token, false);
            if (result == null || result.equals("invalid")) {
                return Response.seeOther(java.net.URI.create(registerBase + "invalid")).build();
            }
            if (result.startsWith("activated:")) {
                // result = "activated:email:password:inuId" — auto-approved subscriber
                final var parts = result.split(":", 4);
                final var email = parts.length > 1 ? parts[1] : "";
                final var password = parts.length > 2 ? parts[2] : "";
                final var inuId = parts.length > 3 ? parts[3] : "";
                // Resolve per-user admin email (with global fallback)
                final var adminEmail = getRegistrationAdminEmail(inuId);
                final var extraHtml = getRegistrationEmailExtra(inuId);
                final var emailExtraHtml = (extraHtml == null || extraHtml.isEmpty()) ? "" : "<hr>" + extraHtml;
                // Send welcome email with credentials directly to the subscriber
                if (!email.isEmpty()) {
                    final var credSubject = "Your data portal access is ready";
                    final var credBody = "<p>Your registration has been confirmed!</p>"
                            + "<p>You can now log in to the data portal using:</p>"
                            + "<ul><li><strong>Username:</strong> " + escapeHtml(inuId) + "</li>"
                            + "<li><strong>Password:</strong> <code>" + escapeHtml(password) + "</code></li></ul>"
                            + "<p>Please keep your password secure and confidential. Store your credentials safely, as they will be required for future access to your account.</p>"
                            + emailExtraHtml;
                    try {
                        mover.getMasterInterface().sendNotificationEmail(email, credSubject, credBody);
                    } catch (final Exception e) {
                        _log.warn("Failed to send credentials email to {}", email, e);
                    }
                }
                // Notify admin too
                if (!adminEmail.isEmpty() && !email.isEmpty()) {
                    try {
                        mover.getMasterInterface().sendNotificationEmail(adminEmail,
                                "Subscriber activated for data user '" + inuId + "'",
                                "<p>Subscriber <strong>" + escapeHtml(email)
                                        + "</strong> has been auto-activated for data user <strong>" + escapeHtml(inuId)
                                        + "</strong>.</p>");
                    } catch (final Exception e) {
                        _log.warn("Failed to send admin activation notification", e);
                    }
                }
                return Response.seeOther(java.net.URI.create(registerBase + "activated")).build();
            }
            if (result.startsWith("verified:")) {
                // result = "verified:email:inuId" — email verified but pending admin approval
                final var parts = result.split(":", 3);
                final var email = parts.length > 1 ? parts[1] : "";
                final var inuId = parts.length > 2 ? parts[2] : "";
                // Resolve per-user admin email (with global fallback)
                final var adminEmail = getRegistrationAdminEmail(inuId);
                // Notify admin to approve
                if (!adminEmail.isEmpty() && !email.isEmpty()) {
                    final var adminBody = "<p>Subscriber <strong>" + escapeHtml(email)
                            + "</strong> has verified their email address and is requesting access to data user <strong>"
                            + escapeHtml(inuId) + "</strong>.</p>"
                            + "<p>Please log in to the admin interface to approve or reject this request.</p>";
                    try {
                        mover.getMasterInterface().sendNotificationEmail(adminEmail,
                                "Registration pending approval for data user '" + inuId + "'", adminBody);
                    } catch (final Exception e) {
                        _log.warn("Failed to send verification notification", e);
                    }
                }
                return Response.seeOther(java.net.URI.create(registerBase + "verified")).build();
            }
            return Response.seeOther(java.net.URI.create(registerBase + "invalid")).build();
        } catch (final Exception e) {
            _log.warn("verifyGet", e);
            return Response.seeOther(java.net.URI.create(registerBase + "invalid")).build();
        }
    }

    /** Helper: safely trim an object from JSON to a String. */
    private static String trim(final Object o) {
        return o == null ? "" : o.toString().trim();
    }

    /**
     * Helper: resolve the registration admin email for a given IncomingUser login. Uses the per-user ECtrans option
     * {@code portal.registrationAdminEmail} if set, otherwise falls back to the global
     * {@code DataPortal.registrationAdminEmail} in ecmwf.properties.
     */
    private String getRegistrationAdminEmail(final String inuId) {
        if (inuId != null && !inuId.isBlank()) {
            try {
                final var profile = mover.getMasterInterface().getIncomingProfileNoAuth(inuId);
                if (profile != null) {
                    final var setup = profile.getECtransSetup();
                    if (setup != null) {
                        final var email = setup.getString(ECtransOptions.USER_PORTAL_REGISTRATION_ADMIN_EMAIL);
                        if (!email.isEmpty()) {
                            return email;
                        }
                    }
                }
            } catch (final Exception ignored) {
                // fall through to global config
            }
        }
        return Cnf.at("DataPortal", "registrationAdminEmail", "");
    }

    /**
     * Helper: resolve the optional extra email text for the credentials/access-ready email for a given IncomingUser
     * login via the per-user ECtrans option {@code portal.registrationEmailExtraAccess}. Returns an empty string if not
     * set.
     */
    private String getRegistrationEmailExtra(final String inuId) {
        if (inuId != null && !inuId.isBlank()) {
            try {
                final var profile = mover.getMasterInterface().getIncomingProfileNoAuth(inuId);
                if (profile != null) {
                    final var setup = profile.getECtransSetup();
                    if (setup != null) {
                        return setup.getString(ECtransOptions.USER_PORTAL_REGISTRATION_EMAIL_EXTRA_ACCESS);
                    }
                }
            } catch (final Exception ignored) {
                // fall through
            }
        }
        return "";
    }

    /** Build a JSON error response. */
    private static Response jsonError(final int status, final String message) {
        final var body = "{\"message\":\"" + message.replace("\"", "\\\"") + "\"}";
        return Response.status(status).entity(body).type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("login")
    @Produces(MediaType.TEXT_HTML)
    public Response loginPage(@Context final HttpServletRequest request) {
        // Already logged in? Go directly to the portal.
        final var cookies = request.getCookies();
        if (cookies != null) {
            for (final var cookie : cookies) {
                if ("portal_session".equals(cookie.getName())) {
                    final var user = MoverProvider.getUserForPortalSession(cookie.getValue());
                    if (user != null) {
                        return Response.seeOther(URI.create("/data/list/")).build();
                    }
                }
            }
        }
        try {
            final var sb = new StringBuilder().append(getTemplateContent(loginContent, LOGIN_FILE));
            final var title = System.getProperty("mover.title", "Data Store for Acquisition & Dissemination");
            final var tab = System.getProperty("mover.tab", title);
            final var footer = System.getProperty("mover.footer",
                    "Powered by <a href=\"https://github.com/ecmwf/open-ecpds\" target=\"_blank\">OpenECPDS</a>");
            final var color = System.getProperty("mover.color", "#000000");
            Format.replaceAll(sb, "${tab}", tab);
            Format.replaceAll(sb, "${title}", title);
            Format.replaceAll(sb, "${footer}", footer);
            Format.replaceAll(sb, "${color}", color);
            Format.replaceAll(sb, "${version}", Version.getVersion());
            Format.replaceAll(sb, "${build}", Version.getBuild());
            Format.replaceAll(sb, "${message}", "");
            return Response.ok(sb.toString(), MediaType.TEXT_HTML).build();
        } catch (final Exception e) {
            _log.warn("registerGet", e);
            return Response.serverError().entity("Registration page unavailable").build();
        }
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("username") final String username, @FormParam("password") final String password,
            @Context final HttpServletRequest request, @Context final HttpServletResponse response) {
        try {
            final var session = NativeAuthenticationProvider.getInstance().getUserSession(request.getRemoteAddr(),
                    username, password, "https", (Closeable) () -> {
                        try {
                            response.sendError(-1);
                        } catch (IOException ignored) {
                        }
                    });
            // Do not create sessions for anonymous/open access users.
            final var setup = session.getECtransSetup();
            if (setup == null || !"open-access".equals(session.getPortalService())) {
                setPortalSessionCookie(response, session.getToken());
            }
            session.close(true);
            return Response.seeOther(URI.create("/data/list/")).build();
        } catch (final Throwable t) {
            _log.debug("Browser login failed for {}", username);
            try {
                final var sb = new StringBuilder().append(getTemplateContent(loginContent, LOGIN_FILE));
                final var title = System.getProperty("mover.title", "Data Store for Acquisition & Dissemination");
                final var tab = System.getProperty("mover.tab", title);
                final var footer = System.getProperty("mover.footer",
                        "Powered by <a href=\"https://github.com/ecmwf/open-ecpds\" target=\"_blank\">OpenECPDS</a>");
                final var color = System.getProperty("mover.color", "#000000");
                Format.replaceAll(sb, "${tab}", tab);
                Format.replaceAll(sb, "${title}", title);
                Format.replaceAll(sb, "${footer}", footer);
                Format.replaceAll(sb, "${color}", color);
                Format.replaceAll(sb, "${version}", Version.getVersion());
                Format.replaceAll(sb, "${build}", Version.getBuild());
                Format.replaceAll(sb, "${message}", "Login failed");
                return Response.status(Response.Status.UNAUTHORIZED).entity(sb.toString()).type(MediaType.TEXT_HTML)
                        .build();
            } catch (final Exception e) {
                _log.warn("login", e);
                return Response.serverError().entity("Login page unavailable").build();
            }
        }
    }

    @GET
    @Path("logout")
    public Response logout(@Context final HttpServletRequest request, @Context final HttpServletResponse response) {

        final var cookies = request.getCookies();

        if (cookies != null) {
            for (final var cookie : cookies) {
                if ("portal_session".equals(cookie.getName())) {
                    MoverProvider.invalidatePortalSession(cookie.getValue());
                    break;
                }
            }
        }

        final var expired = new Cookie("portal_session", "");
        expired.setHttpOnly(true);
        expired.setSecure(true);
        expired.setPath("/");
        expired.setMaxAge(0);

        response.addCookie(expired);

        return Response.seeOther(URI.create("/login")).build();
    }

    /**
     * The Class MediaRequest.
     */
    private static final class MediaRequest {

        /** The remote addr. */
        final String remoteAddr;

        /** The content type. */
        final String contentType;

        /** The name. */
        final String name;

        /** The size. */
        final long size;

        /**
         * Instantiates a new media request.
         *
         * @param remoteAddress
         *            the remote address
         * @param contentType
         *            the content type
         * @param name
         *            the name
         * @param size
         *            the size
         */
        MediaRequest(final String remoteAddress, final String contentType, final String name, final long size) {
            this.remoteAddr = remoteAddress;
            this.contentType = contentType;
            this.name = name;
            this.size = size;
        }
    }

    // Utilities for the REST services

    /**
     * Process get.
     *
     * @param session
     *            the session
     * @param request
     *            the request
     * @param builder
     *            the builder
     * @param filename
     *            the filename
     *
     * @return the media request
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static MediaRequest processGet(final UserSession session, final HttpServletRequest request,
            final ResponseBuilder builder, final String filename) throws EccmdException, IOException {
        final var name = getFilename(session, filename);
        final var element = session.getFileListElement(name);
        if (element.isDirectory() || element.getSize() == null || element.getComment() == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN)
                    .entity(filename + ": Not a regular file").build());
        }
        // Are we required to check the ETag?
        final var ifNoneMatch = request.getHeader(IF_NONE_MATCH);
        final var etag = getETag(element.getComment());
        // Are we required to check against the last modified time?
        final var lastModified = element.getTime();
        final var ifModifiedSince = request.getDateHeader(IF_MODIFIED_SINCE);
        final var ifUnmodifiedSince = request.getDateHeader(IF_UNMODIFIED_SINCE);
        final var etagMatches = ifNoneMatch != null && !ifNoneMatch.isEmpty() && ifNoneMatch.equals(etag);
        final var notModifiedSince = ifNoneMatch == null && ifModifiedSince != -1
                && lastModified < ifModifiedSince + 1000;
        final var modifiedAfterUnmodifiedSince = ifNoneMatch == null && ifUnmodifiedSince != -1
                && lastModified >= ifUnmodifiedSince + 1000;
        if (etagMatches || notModifiedSince || modifiedAfterUnmodifiedSince) {
            throw new WebApplicationException(Response.notModified().header(ETAG, etag).build());
        }
        final var fileName = new File(element.getName()).getName();
        // Get content type by file name and set content disposition.
        var contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
        var disposition = "inline";
        final var setup = session.getECtransSetup();
        if (setup != null && builder != null) {
            for (final var entry : setup.getOptions(ECtransOptions.USER_PORTAL_HEADER_REGISTRY, filename, null)
                    .getProperties().entrySet()) {
                final var key = entry.getKey().toString();
                final var value = entry.getValue().toString();
                if (key.equalsIgnoreCase(CONTENT_TYPE)) {
                    contentType = value;
                } else {
                    if (!RESERVED_HEADERS.contains(key.toLowerCase())) {
                        builder.header(key, entry.getValue());
                    }
                }
            }
        }
        // If content type is unknown, then set the default value.
        // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
        // To add new content types, add new mime-mapping entry in web.xml.
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        } else if (!contentType.startsWith("image")) {
            // Except for images, determine content disposition. If content type is
            // supported by the browser, then set to inline, else attachment which will pop
            // a 'save as' dialogue.
            final var accept = request.getHeader(ACCEPT);
            disposition = accept != null && accepts(accept, contentType) ? "inline" : "attachment";
        }
        if (builder != null) {
            builder.header(ACCEPT_RANGES, "bytes");
            builder.header(CONTENT_TYPE, contentType);
            builder.header(CONTENT_DISPOSITION, disposition + ";filename=\"" + fileName + "\"");
            builder.header(ETAG, etag);
            builder.lastModified(new Date(lastModified));
        }
        return new MediaRequest(request.getRemoteAddr(), contentType, name, Long.parseLong(element.getSize()));
    }

    /**
     * Gets the e tag.
     *
     * @param dataTransferId
     *            the data transfer id
     *
     * @return the e tag
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String getETag(final String dataTransferId) throws IOException {
        try {
            return mover.getMasterInterface().getETag(Long.parseLong(dataTransferId));
        } catch (final Throwable t) {
            _log.warn("Cannot process ETag", t);
            throw new IOException("Cannot process ETag");
        }
    }

    /**
     * Gets the template content.
     *
     * @param stringBuilder
     *            the string builder
     * @param templateName
     *            the template name
     *
     * @return the template content
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static synchronized StringBuilder getTemplateContent(final StringBuilder stringBuilder,
            final String templateName) throws IOException {
        if (stringBuilder.isEmpty()) {
            _log.debug("Loading template: {}", templateName);
            final var fis = new FileInputStream(templateName);
            final var dis = new BufferedReader(new InputStreamReader(fis));
            String line;
            try {
                while ((line = dis.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
            } catch (final IOException e) {
                _log.warn(e);
            } finally {
                dis.close();
            }
        }
        return stringBuilder;
    }

    /**
     * Gets the bigger indexes.
     *
     * @param elements
     *            the elements
     *
     * @return the bigger indexes
     */
    private static long[] getBiggerIndexes(final FileListElement[] elements) {
        var biggerName = 10;
        var biggerSize = 10;
        var biggerID = 10;
        var biggerTime = 0L;
        for (final FileListElement element : elements) {
            // Let's find out which filename and size are bigger for the listing!
            final var nameLength = element.getName().length();
            if (nameLength > biggerName) {
                biggerName = nameLength;
            }
            final var currentTime = element.getTime();
            if (currentTime > biggerTime) {
                biggerTime = currentTime;
            }
            if (!element.isDirectory()) {
                final var sizeLength = element.getSize().trim().length();
                if (sizeLength > biggerSize) {
                    biggerSize = sizeLength;
                }
                final var idLength = element.getComment().trim().length();
                if (idLength > biggerID) {
                    biggerID = idLength;
                }
            }
        }
        return new long[] { biggerName + 4, biggerSize + 4, biggerID + 4,
                biggerTime == 0 ? System.currentTimeMillis() : biggerTime };
    }

    /**
     * Sets the template parameters.
     *
     * Resolve the ${title}, ${header}, ${parent}, ${version} and ${build} in the template!
     *
     * @param builder
     *            the builder
     * @param uriList
     *            the uri list
     * @param filename
     *            the filename
     * @param biggerIndexes
     *            the bigger indexes
     *
     * @throws FileNotFoundException
     *             the file not found exception
     */
    private static void setTemplateParameters(final StringBuilder builder, final String uriList, final String filename,
            final long[] biggerIndexes) throws FileNotFoundException {
        Format.replaceAll(builder, "${path}", "");
        Format.replaceAll(builder, "${base}", Format.normalizePath(uriList) + "/");
        final var header = new StringBuilder();
        header.append(Format.formatString("Name", (int) biggerIndexes[0], ' ', true));
        final var fmt = new Formatter(header);
        fmt.format(" %-16s", "Scheduled date");
        fmt.close();
        header.append(Format.formatString("Size", (int) biggerIndexes[1], ' ', false));
        header.append(Format.formatString("Id", (int) biggerIndexes[2], ' ', false));
        Format.replaceAll(builder, "${header}", header.toString());
        if (filename.length() > 0) {
            final var parent = new File(filename).getParent();
            final var url = Format.normalizePath(uriList + (parent != null ? "/" + parent : "")) + "/";
            Format.replaceAll(builder, "${parent}", "<a href=\"" + url + "\">Parent Directory</a>");
        } else {
            Format.replaceAll(builder, "${parent}", "");
        }
        Format.replaceAll(builder, "${version}", Version.getVersion());
        Format.replaceAll(builder, "${build}", Version.getBuild());
        Format.replaceAll(builder, "${ftpPort}", Cnf.at("FtpPlugin", "port", ""));
        Format.replaceAll(builder, "${s3Path}", Cnf.at("HttpPlugin", "s3ServicePath", "/s3"));
        Format.replaceAll(builder, "${sftpPort}", Cnf.at("Ssh2Plugin", "port", ""));
        Format.replaceAll(builder, "${httpsPublicBaseUrl}", Cnf.at("DataPortal", "httpsPublicBaseUrl", ""));
        Format.replaceAll(builder, "${s3PublicEndpointUrl}", Cnf.at("DataPortal", "s3PublicEndpointUrl", ""));
        Format.replaceAll(builder, "${ftpPublicHost}", Cnf.at("DataPortal", "ftpPublicHost", ""));
        Format.replaceAll(builder, "${ftpPublicPort}", Cnf.at("DataPortal", "ftpPublicPort", ""));
        Format.replaceAll(builder, "${sftpPublicHost}", Cnf.at("DataPortal", "sftpPublicHost", ""));
        Format.replaceAll(builder, "${sftpPublicPort}", Cnf.at("DataPortal", "sftpPublicPort", ""));
        Format.replaceAll(builder, "${s3Enabled}", Cnf.at("DataPortal", "s3Enabled", true));
        Format.replaceAll(builder, "${ftpEnabled}", Cnf.at("DataPortal", "ftpEnabled", true));
        Format.replaceAll(builder, "${sftpEnabled}", Cnf.at("DataPortal", "sftpEnabled", true));
    }

    /**
     * Adds the list element.
     *
     * @param builder
     *            the builder
     * @param uriList
     *            the uri list
     * @param uriGet
     *            the uri get
     * @param path
     *            the path
     * @param element
     *            the element
     *
     * @throws FileNotFoundException
     *             the file not found exception
     */
    private static void addListElement(final StringBuilder builder, final String uriList, final String uriGet,
            final String path, final FileListElement element) throws FileNotFoundException {
        final var name = element.getName();
        final var isDir = element.isDirectory();
        final var srvc = isDir ? uriList : uriGet;
        final var dir = isDir ? "/" : "";
        final var url = Format.normalizePath(srvc + "/" + path + "/" + name) + dir;
        final String sizeRaw = isDir ? null : element.getSize().trim();
        final String sizeDisplay = isDir ? "-" : Format.formatSize(Long.parseLong(sizeRaw));
        final String sizeOrder = isDir ? "-1" : sizeRaw;
        final var icon = isDir ? "<i class=\"bi bi-folder-fill text-warning me-1\" style=\"font-size:0.85rem;\"></i>"
                : "<i class=\"bi bi-file-earmark text-secondary me-1\" style=\"font-size:0.85rem;\"></i>";
        builder.append("<tr>").append("<td><a href=\"").append(url).append("\">").append(icon).append(escapeHtml(name))
                .append(dir).append("</a></td>").append("<td>")
                .append(Format.formatTime("dd-MM-yyyy HH:mm", element.getTime())).append("</td>")
                .append("<td data-order=\"").append(sizeOrder).append("\">").append(sizeDisplay).append("</td>")
                .append("<td>").append(isDir ? "-" : escapeHtml(element.getComment().trim())).append("</td>")
                .append("</tr>\n");
    }

    /**
     * Resolves the desired listing format from the JAX-RS Accept media types. Returns "json", "text", "csv", or "html"
     * (the default when no specific format is requested or Accept is *\/*).
     *
     * @param acceptTypes
     *            the acceptable media types from the request
     *
     * @return the format token
     */
    private static String resolveListFormat(final List<MediaType> acceptTypes) {
        for (final MediaType mt : acceptTypes) {
            // Wildcards (*/* or text/*) default to HTML for backwards compatibility —
            // isCompatible() would otherwise match JSON/text for */* too.
            if (mt.isWildcardType() || mt.isWildcardSubtype()) {
                return "html";
            }
            if (mt.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                return "json";
            }
            if (mt.isCompatible(new MediaType("text", "csv"))) {
                return "csv";
            }
            if (mt.isCompatible(MediaType.TEXT_PLAIN_TYPE)) {
                return "text";
            }
            if (mt.isCompatible(MediaType.TEXT_HTML_TYPE)) {
                return "html";
            }
        }
        return "html";
    }

    /**
     * Builds a JSON listing response for the given file elements.
     *
     * @param elements
     *            the file list elements
     * @param lastModified
     *            the last-modified date for the response header
     *
     * @return the response
     *
     * @throws Exception
     *             on serialization error
     */
    private static Response buildJsonListing(final FileListElement[] elements, final Date lastModified)
            throws Exception {
        final var items = new ArrayList<Map<String, Object>>(elements.length);
        for (final FileListElement e : elements) {
            final var item = new LinkedHashMap<String, Object>();
            item.put("name", e.getName());
            item.put("size", e.isDirectory() ? null : Long.parseLong(e.getSize().trim()));
            item.put("time", Instant.ofEpochMilli(e.getTime()).toString());
            item.put("directory", e.isDirectory());
            items.add(item);
        }
        return Response.ok(new ObjectMapper().writeValueAsString(items), MediaType.APPLICATION_JSON)
                .lastModified(lastModified).header(CACHE_CONTROL, NO_CACHE).build();
    }

    /**
     * Builds a plain-text listing response: one entry per line, directories have a trailing '/'.
     *
     * @param elements
     *            the file list elements
     * @param lastModified
     *            the last-modified date for the response header
     *
     * @return the response
     */
    private static Response buildTextListing(final FileListElement[] elements, final Date lastModified) {
        final var sb = new StringBuilder();
        for (final FileListElement e : elements) {
            sb.append(e.getName());
            if (e.isDirectory()) {
                sb.append('/');
            }
            sb.append('\n');
        }
        return Response.ok(sb.toString(), MediaType.TEXT_PLAIN).lastModified(lastModified)
                .header(CACHE_CONTROL, NO_CACHE).build();
    }

    /**
     * Builds a CSV listing response with columns: name, size, time, directory.
     *
     * @param elements
     *            the file list elements
     * @param lastModified
     *            the last-modified date for the response header
     *
     * @return the response
     */
    private static Response buildCsvListing(final FileListElement[] elements, final Date lastModified) {
        final var sb = new StringBuilder("name,size,time,directory\n");
        for (final FileListElement e : elements) {
            sb.append(quoteCsv(e.getName())).append(',');
            sb.append(e.isDirectory() ? "" : e.getSize().trim()).append(',');
            sb.append(Instant.ofEpochMilli(e.getTime())).append(',');
            sb.append(e.isDirectory()).append('\n');
        }
        return Response.ok(sb.toString(), "text/csv; charset=UTF-8").lastModified(lastModified)
                .header(CACHE_CONTROL, NO_CACHE).build();
    }

    /**
     * Quotes a value for CSV output per RFC 4180: wraps in double-quotes if the value contains a comma, double-quote,
     * or newline, escaping any embedded double-quotes by doubling them.
     *
     * @param value
     *            the value to quote
     *
     * @return the CSV-safe value
     */
    private static String quoteCsv(final String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Escapes HTML special characters in a string to prevent markup injection.
     *
     * @param text
     *            the text to escape
     *
     * @return the escaped text
     */
    private static String escapeHtml(final String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /**
     * Gets the filename.
     *
     * @param session
     *            the session
     * @param filename
     *            the filename
     *
     * @return the filename
     *
     * @throws FileNotFoundException
     *             the file not found exception
     */
    private static String getFilename(final UserSession session, final String filename) throws FileNotFoundException {
        return "[" + session.getUser() + "]DATA:" + Format.normalizePath(filename);
    }

    /**
     * Serves a data file download using an already-authenticated session. This is the shared core of
     * {@link #dataFileGet} and the file branch of {@link #fileGet}, avoiding double session creation.
     * <p>
     * Session lifecycle: on failure the session is closed here; on success it is transferred to the
     * {@link SingleStreamer}/{@link MultiStreamer} which closes it after streaming completes.
     *
     * @param session
     *            the authenticated user session
     * @param request
     *            the HTTP servlet request
     * @param filename
     *            the file path (relative, will be resolved via {@link #getFilename})
     *
     * @return the JAX-RS streaming response
     */
    private Response serveDataFile(final UserSession session, final HttpServletRequest request, final String filename) {
        var success = false;
        try {
            checkParameter("filename", filename);
            final var builder = Response.ok();
            final var mediaRequest = processGet(session, request, builder, filename);
            final var fullRange = new Range(0, mediaRequest.size - 1, mediaRequest.size);
            // Is it a bytes range request?
            final var rangeHeader = request.getHeader(RANGE);
            final StreamingOutput streamer;
            if (rangeHeader == null) {
                // Return full file
                builder.header(CONTENT_LENGTH, fullRange.length);
                streamer = new SingleStreamer(session, mediaRequest, 0, -1);
            } else {
                // Found a Range request, returning HTTP 206 Partial Content
                builder.status(206);
                final var ranges = getRanges(rangeHeader, fullRange, filename, mediaRequest.size, request);
                if (ranges.isEmpty() || ranges.get(0) == fullRange) {
                    // Return full file
                    builder.header(CONTENT_RANGE,
                            "bytes " + fullRange.start + "-" + fullRange.end + "/" + fullRange.total);
                    builder.header(CONTENT_LENGTH, fullRange.length);
                    streamer = new SingleStreamer(session, mediaRequest, 0, -1);
                } else if (ranges.size() == 1) {
                    // Only one range requested
                    final var singleRange = ranges.get(0);
                    builder.header(CONTENT_RANGE,
                            "bytes " + singleRange.start + "-" + singleRange.end + "/" + singleRange.total);
                    builder.header(CONTENT_LENGTH, singleRange.length);
                    streamer = new SingleStreamer(session, mediaRequest, singleRange.start, singleRange.length);
                } else {
                    // Multiple ranges requested
                    final var rangeSize = ranges.size();
                    final var setup = session.getECtransSetup();
                    if (setup != null
                            && rangeSize > setup.getByteSize(ECtransOptions.USER_PORTAL_MAX_RANGES_ALLOWED).size()) {
                        throw newException(429, "Too Many Requests: Max ranges allowed exceeded (" + rangeSize + ")");
                    }
                    builder.header(CONTENT_TYPE, "multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
                    streamer = new MultiStreamer(session, mediaRequest, ranges);
                }
            }
            success = true;
            addCorsHeaders(session, request, builder);
            return builder.entity(streamer).build();
        } catch (final WebApplicationException w) {
            _log.warn("serveDataFile - {}", describe(w));
            throw w;
        } catch (final FileNotFoundException e) {
            _log.warn("serveDataFile", e);
            throw newException(e, 404, "Not Found: " + e.getMessage());
        } catch (final EccmdException e) {
            _log.warn("serveDataFile", e);
            final var message = e.getMessage();
            if (message.contains("File not found") || message.contains("Destination not found")) {
                throw newException(e, 404, "Not Found: " + message);
            }
            throw newException(e, 500, message);
        } catch (final Throwable t) {
            _log.warn("serveDataFile", t);
            throw newException(t, 500, Format.getMessage(t));
        } finally {
            if (!success && session != null) {
                session.close(true);
            }
        }
    }

    /**
     * Adds CORS response headers derived from the user's {@code portal.corsAllowOrigin} ECtrans option. Does nothing if
     * the request has no {@code Origin} header or the option is not set for the user.
     *
     * @param session
     *            the user session (may be null)
     * @param request
     *            the HTTP servlet request
     * @param builder
     *            the JAX-RS response builder to add headers to
     */
    private static void addCorsHeaders(final UserSession session, final HttpServletRequest request,
            final ResponseBuilder builder) {
        if (session == null || request.getHeader("Origin") == null) {
            return;
        }
        try {
            final var setup = session.getECtransSetup();
            if (setup == null) {
                return;
            }
            final var corsAllowOrigin = setup.getString(ECtransOptions.USER_PORTAL_CORS_ALLOW_ORIGIN);
            if (!corsAllowOrigin.isEmpty()) {
                builder.header("Access-Control-Allow-Origin", corsAllowOrigin)
                        .header("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS")
                        .header("Access-Control-Allow-Headers", "Range, Content-Type, Authorization")
                        .header("Access-Control-Expose-Headers",
                                "Content-Range, Content-Length, Accept-Ranges, ETag, Last-Modified");
            }
        } catch (final Exception e) {
            _log.debug("CORS: could not read corsAllowOrigin for user: {}", e.getMessage());
        }
    }

    private UserSession getPortalSession(final HttpServletRequest request, final HttpServletResponse response) {
        final var cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (final var cookie : cookies) {
            if (!"portal_session".equals(cookie.getName())) {
                continue;
            }
            final var token = cookie.getValue();
            final var cachedUser = MoverProvider.getUserForPortalSession(token);
            if (cachedUser == null) {
                break;
            }
            try {
                final var session = NativeAuthenticationProvider.getInstance().getUserSession(request.getRemoteAddr(),
                        cachedUser, MoverProvider.PORTAL_SESSION_PREFIX + token, "https", (Closeable) () -> {
                            try {
                                response.sendError(-1);
                            } catch (IOException ignored) {
                            }
                        });
                // Refresh the cookie only for non-anonymous users.
                final var setup = session.getECtransSetup();
                if (setup == null || !"open-access".equals(session.getPortalService())) {
                    setPortalSessionCookie(response, session.getToken());
                }
                return session;
            } catch (final Throwable t) {
                _log.debug("Portal session cookie invalid", t);
                break;
            }
        }
        return null;
    }

    /**
     * Authenticates the user using HTTP Basic Authentication.
     *
     * @param authString
     *            the Authorization header
     * @param request
     *            the HTTP request
     * @param response
     *            the HTTP response
     *
     * @return the authenticated user session
     */
    private UserSession authenticateBasic(final String authString, final HttpServletRequest request,
            final HttpServletResponse response) {
        final String[] credentials = decodeBasicCredentials(authString);
        try {
            final var session = NativeAuthenticationProvider.getInstance().getUserSession(request.getRemoteAddr(),
                    credentials[0], credentials[1], "https", (Closeable) () -> response.sendError(-1));
            // Successful authentication.
            // Issue/refresh the portal session cookie so browser requests do not
            // need to re-authenticate (especially important for TOTP users).
            // Skip anonymous users ("open-access") as they do not require a
            // server-side portal session.
            if (!"open-access".equals(session.getPortalService())) {
                setPortalSessionCookie(response, session.getToken());
            }
            // Log request headers if enabled.
            if (Cnf.at("HttpPlugin", "logHeadersAndUri", false)) {
                final var headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    final var key = headerNames.nextElement();
                    _log.debug("Request header: {} = [{}]", key, request.getHeader(key));
                }
            }
            return session;
        } catch (final Throwable t) {
            final var message = t.getMessage();
            // "Login failed" is a normal authentication failure.
            if (message != null && message.contains("Login failed")) {
                _log.debug("authenticateBasic: login failed for request from {}", request.getRemoteAddr());
            } else {
                _log.warn("authenticateBasic", t);
            }
            // Special error mappings.
            if (message != null) {
                if (message.contains("Maximum number of connections exceeded")) {
                    throw new WebApplicationException(
                            Response.status(429).type(MediaType.TEXT_PLAIN).entity("Too Many Requests").build());
                }
                if (message.contains("Upload quota exceeded") || message.contains("Download quota exceeded")) {

                    throw new WebApplicationException(Response.status(429).type(MediaType.TEXT_PLAIN)
                            .entity("Too Many Requests: " + message).build());
                }
                if (message.contains(" not allowed for ")) {
                    throw new WebApplicationException(
                            Response.status(403).type(MediaType.TEXT_PLAIN).entity("Forbidden").build());
                }
            }
            // Authentication failed.
            // Check whether this is a self-service user and redirect to the
            // registration page instead of returning 401.
            try {
                final var attemptedUser = credentials[0];
                final var profile = mover.getMasterInterface().getIncomingProfileNoAuth(attemptedUser);
                if (profile != null && "self-service".equals(profile.getIncomingUser().getPortalService())) {
                    final var registerUrl = request.getRequestURL().toString().replaceFirst("/ecpds/.*",
                            "/ecpds/register") + "?user=" + attemptedUser;
                    throw new WebApplicationException(Response.status(302).header("Location", registerUrl).build());
                }
            } catch (final WebApplicationException wae) {
                throw wae;
            } catch (final Exception ignored) {
                // Lookup failed — fall through to standard 401.
            }
            throw unauthorized();
        }
    }

    /**
     * Gets the user session.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param response
     *            the response
     *
     * @return the user session
     */
    private UserSession getUserSession(final String authString, final HttpServletRequest request,
            final HttpServletResponse response) {
        final var session = getPortalSession(request, response);
        if (session != null) {
            return session;
        }
        return authenticateBasic(authString, request, response);
    }

    private UserSession getPortalUserSession(final HttpServletRequest request, final HttpServletResponse response) {
        final var session = getPortalSession(request, response);
        if (session != null) {
            return session;
        }
        throw unauthorized();
    }

    private WebApplicationException unauthorized() {
        return new WebApplicationException(Response.status(401).type(MediaType.TEXT_PLAIN).entity("Unauthorized")
                .header("WWW-Authenticate", "Basic realm=\"Data User Credentials\"").build());
    }

    /**
     * Decodes an HTTP Basic Authorization header.
     *
     * @param authString
     *            the Authorization header
     *
     * @return the decoded username/password pair
     *
     * @throws WebApplicationException
     *             if the header is missing or malformed
     */
    private String[] decodeBasicCredentials(final String authString) {
        if (authString == null || !authString.toLowerCase().startsWith("basic ")) {
            throw unauthorized();
        }
        try {
            final var encoded = authString.split("\\s+", 2)[1];
            final var decoded = new String(BASE64Coder.decode(encoded), StandardCharsets.UTF_8);
            final var credentials = decoded.split(":", 2);
            if (credentials.length != 2) {
                throw unauthorized();
            }
            return credentials;
        } catch (final IllegalArgumentException e) {
            throw unauthorized();
        }
    }

    /**
     * Set (or refresh) the portal session cookie on the HTTP response.
     *
     * @param response
     *            the HTTP response
     * @param token
     *            the session token
     */
    private static void setPortalSessionCookie(final HttpServletResponse response, final String token) {
        final var maxAge = (int) (MoverProvider._portalSessionTtlMs / 1000);
        response.addHeader("Set-Cookie", "portal_session=" + token + "; Path=/" + "; Max-Age=" + maxAge + "; HttpOnly"
                + "; Secure" + "; SameSite=Lax");
    }

    /**
     * Get the basic authentication.
     *
     * @param authString
     *            the credentials for user portal
     *
     * @return the string as returned by the client
     */
    private static String getBasicAuth(final String authString) {
        return new StringBuilder("Basic ").append(BASE64Coder.encode(authString.getBytes())).toString();
    }

    /**
     * _new exception.
     *
     * @param status
     *            the status
     * @param message
     *            the message
     *
     * @return the web application exception
     */
    private static WebApplicationException newException(final int status, final String message) {
        return newException(new Exception(status + ": " + message), status, message);
    }

    /**
     * _new exception.
     *
     * @param t
     *            the t
     * @param status
     *            the status
     * @param message
     *            the message
     *
     * @return the web application exception
     */
    private static WebApplicationException newException(final Throwable t, final int status, final String message) {
        return new WebApplicationException(t,
                Response.status(status).type(MediaType.TEXT_PLAIN).entity(message).build());
    }

    /**
     * Check if the control channel is secured. Should NOT be the http protocol as it is insecure.
     *
     * @param ui
     *            the ui
     */
    private static void checkIsControlChannel(final UriInfo ui) {
        if (Cnf.at("HttpPlugin", "checkControlChannelIsSecure", false)
                && "http".equalsIgnoreCase(ui.getBaseUri().getScheme())) {
            throw newException(403, "Forbidden: SSL required");
        }
        // Log URI if asked to do so?
        if (Cnf.at("HttpPlugin", "logHeadersAndUri", false)) {
            _log.debug("Request URI: {}", ui.getRequestUri());
        }
    }

    /**
     * _check parameter.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    private static void checkParameter(final String name, final String value) {
        if (value == null || value.trim().isEmpty()) {
            throw newException(400, "Bad Request: Missing parameter '" + name + "'");
        }
    }

    /**
     * _check parameter.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    private static void checkParameter(final String name, final Object value) {
        if (value == null) {
            throw newException(400, "Bad Request: Missing parameter '" + name + "'");
        }
    }

    /**
     * The Class RESTMessage.
     */
    private static final class RESTMessage {
        /** The _message. */
        private final Map<String, Object> message = new HashMap<>();

        /**
         * Gets the error message.
         *
         * @param t
         *            the t
         *
         * @return the error message
         */
        static RESTMessage getErrorMessage(final Throwable t) {
            return getErrorMessage(Format.getMessage(t));
        }

        /**
         * Gets the error message.
         *
         * @param error
         *            the error
         *
         * @return the error message
         */
        static RESTMessage getErrorMessage(final String error) {
            final var message = new RESTMessage();
            message.put("success", "no");
            message.put("error", error);
            return message;
        }

        /**
         * Gets the success message.
         *
         * @return the success message
         */
        static RESTMessage getSuccessMessage() {
            final var message = new RESTMessage();
            message.put("success", "yes");
            return message;
        }

        /**
         * Puts the.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         */
        void put(final String key, final Object value) {
            message.put(key, value);
        }

        /**
         * Gets the response.
         *
         * @return the response
         */
        Response getResponse() {
            return Response.ok().entity(message).build();
        }
    }

    /**
     * Gets the ranges.
     *
     * @param rangeHeader
     *            the range header
     * @param full
     *            the full
     * @param fileName
     *            the file name
     * @param fileSize
     *            the file size
     * @param request
     *            the request
     *
     * @return the ranges
     */
    private static List<Range> getRanges(final String rangeHeader, final Range full, final String fileName,
            final long fileSize, final HttpServletRequest request) {
        // The full Range represents the complete file.
        final List<Range> ranges = new ArrayList<>();
        // Remove all spaces/tab from the range header
        final var range = rangeHeader.replaceAll("\\s", "").toLowerCase();
        // Range header should match format "bytes=n-n,n-n,n-n..."
        if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
            throw newException(416, "Range Not Satisfiable: Bad range format (" + rangeHeader + ")");
        }
        final var ifRange = request.getHeader(IF_RANGE);
        if (ifRange != null && !ifRange.equals(fileName)) {
            try {
                final var ifRangeTime = request.getDateHeader(IF_RANGE); // Throws IAE if invalid.
                if (ifRangeTime != -1) {
                    ranges.add(full);
                }
            } catch (final IllegalArgumentException ignore) {
                ranges.add(full);
            }
        }
        // If any valid If-Range header, then process each part of byte range
        if (ranges.isEmpty()) {
            for (final String part : range.substring(6).split(",")) {
                // Assuming a file with length of 100, the following examples returns bytes at:
                // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                var start = Range.sublong(part, 0, part.indexOf("-"));
                var end = Range.sublong(part, part.indexOf("-") + 1, part.length());
                if (start == -1) {
                    start = fileSize - end;
                    end = fileSize - 1;
                } else if (end == -1 || end > fileSize - 1) {
                    end = fileSize - 1;
                }
                // Check if Range is syntactically valid. If not, then return 416.
                if (start > end) {
                    throw newException(416, "Range Not Satisfiable: Syntactically invalid (" + part + ")");
                }
                // Add range.
                ranges.add(new Range(start, end, fileSize));
            }
        }
        return ranges;
    }

    /**
     * The Class SingleStreamer.
     */
    private static final class SingleStreamer implements StreamingOutput {

        /** The session. */
        private final UserSession session;

        /** The media request. */
        private final MediaRequest mediaRequest;

        /** The posn. */
        private final long posn;

        /** The length. */
        private final long length;

        /**
         * Instantiates a new single streamer.
         *
         * @param session
         *            the session
         * @param mediaRequest
         *            the media request
         * @param posn
         *            the posn
         * @param length
         *            the length
         */
        public SingleStreamer(final UserSession session, final MediaRequest mediaRequest, final long posn,
                final long length) {
            this.session = session;
            this.mediaRequest = mediaRequest;
            this.posn = posn;
            this.length = length;
        }

        /**
         * Write.
         *
         * @param out
         *            the out
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws WebApplicationException
         *             the web application exception
         */
        @Override
        public void write(final OutputStream out) throws IOException, WebApplicationException {
            try {
                transferRange(out, session, mediaRequest, posn, length, -1, -1);
            } finally {
                session.close(true);
            }
        }
    }

    /**
     * The Class MultiStreamer.
     */
    private static final class MultiStreamer implements StreamingOutput {

        /** The session. */
        private final UserSession session;

        /** The media request. */
        private final MediaRequest mediaRequest;

        /** The ranges. */
        private final List<Range> ranges;

        /**
         * Instantiates a new multi streamer.
         *
         * @param session
         *            the session
         * @param mediaRequest
         *            the media request
         * @param ranges
         *            the ranges
         */
        public MultiStreamer(final UserSession session, final MediaRequest mediaRequest, final List<Range> ranges) {
            this.session = session;
            this.mediaRequest = mediaRequest;
            this.ranges = ranges;
        }

        /**
         * Write.
         *
         * @param out
         *            the out
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws WebApplicationException
         *             the web application exception
         */
        @Override
        public void write(final OutputStream out) throws IOException, WebApplicationException {
            try {
                final var setup = session.getECtransSetup();
                final boolean triggerEvent;
                final boolean triggerLastRangeOnly;
                if (setup != null) {
                    triggerEvent = setup.getBoolean(ECtransOptions.USER_PORTAL_TRIGGER_EVENT);
                    triggerLastRangeOnly = setup.getBoolean(ECtransOptions.USER_PORTAL_TRIGGER_LAST_RANGE_ONLY);
                } else {
                    triggerEvent = true;
                    triggerLastRangeOnly = true;
                }
                final var startTime = System.currentTimeMillis();
                final var rangesCount = ranges.size();
                var fullLength = 0L;
                // Copy multipart range
                for (var i = 0; i < rangesCount; i++) {
                    final var range = ranges.get(i);
                    fullLength += range.length;
                    // Print the header!
                    println(out, "");
                    println(out, "--" + MULTIPART_BOUNDARY);
                    println(out, "Content-Type: " + mediaRequest.contentType);
                    println(out, "Content-Range: bytes " + range.start + "-" + range.end + "/" + range.total);
                    println(out, "");
                    // And drop the content
                    transferRange(out, session, mediaRequest, range.start, range.length, startTime,
                            triggerEvent ? triggerLastRangeOnly ? i == rangesCount - 1 ? fullLength : 0 : -1 : 0);
                }
                // End with multipart boundary.
                println(out, "");
                println(out, "--" + MULTIPART_BOUNDARY + "--");
            } finally {
                session.close(true);
            }
        }

        /**
         * Println.
         *
         * @param out
         *            the out
         * @param string
         *            the string
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private static void println(final OutputStream out, final String string) throws IOException {
            out.write((string + "\r\n").getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * The Class Range.
     */
    private static final class Range {

        /** The start. */
        final long start;

        /** The end. */
        final long end;

        /** The length. */
        final long length;

        /** The total. */
        final long total;

        /**
         * Construct a byte range.
         *
         * @param start
         *            Start of the byte range.
         * @param end
         *            End of the byte range.
         * @param total
         *            Total length of the byte source.
         */
        Range(final long start, final long end, final long total) {
            this.start = start;
            this.end = end;
            this.length = end - start + 1;
            this.total = total;
        }

        /**
         * Sublong.
         *
         * @param value
         *            the value
         * @param beginIndex
         *            the begin index
         * @param endIndex
         *            the end index
         *
         * @return the long
         */
        static long sublong(final String value, final int beginIndex, final int endIndex) {
            final var substring = value.substring(beginIndex, endIndex);
            return substring.length() > 0 ? Long.parseLong(substring) : -1;
        }
    }

    /**
     * Transfer range.
     *
     * If fullLength is -1 then we trigger an event. If fullLength is 0 we do nothing and if fullLength is > 0 then we
     * record an event with the start time and full length specified.
     *
     * @param out
     *            the out
     * @param session
     *            the session
     * @param mediaRequest
     *            the media request
     * @param posn
     *            the posn
     * @param length
     *            the length
     * @param startTime
     *            the start time
     * @param fullLength
     *            the full length
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws WebApplicationException
     *             the web application exception
     */
    private static void transferRange(final OutputStream out, final UserSession session,
            final MediaRequest mediaRequest, final long posn, final long length, final long startTime,
            final long fullLength) throws IOException, WebApplicationException {
        try {
            final var proxy = session.getProxySocketInput(mediaRequest.name, posn, length);
            final InputStream in = new MonitoredInputStream(proxy.getDataInputStream()) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        // Populating with the transfer rate informations if required!
                        if (fullLength != 0) {
                            final var event = new ProxyEvent(proxy);
                            event.setProtocol("https");
                            event.setLocalHost(mover.getRoot());
                            event.setRemoteHost(mediaRequest.remoteAddr);
                            event.setUserType(ProxyEvent.UserType.DATA_USER);
                            event.setUserName(session.getUser());
                            if (fullLength > 0) {
                                // This is the last range of the list!
                                event.setDuration(System.currentTimeMillis() - startTime);
                                event.setStartTime(startTime);
                                event.setSent(fullLength);
                            } else {
                                // Only one range sent!
                                event.setDuration(getDuration());
                                event.setStartTime(getStartTime());
                                event.setSent(getByteSent());
                            }
                        }
                        try {
                            session.check(proxy);
                        } catch (final EccmdException e) {
                            throw newException(e, 500, e.getMessage());
                        }
                    }
                }
            };
            StreamPlugThread.copy(out, in, StreamPlugThread.DEFAULT_BUFF_SIZE);
            out.flush();
            in.close();
        } catch (final EccmdException e) {
            throw newException(e, 500, e.getMessage());
        }
    }

    /**
     * Builds a human-readable description of a {@link WebApplicationException}, including the HTTP status code,
     * headers, exception message, and root cause.
     * <p>
     * This method is useful for logging, since the default {@code toString()} of {@link WebApplicationException}
     * typically only shows the class name without any of the associated HTTP response details.
     *
     * @param ex
     *            the {@link WebApplicationException} to describe, may be {@code null}
     *
     * @return a string with status, headers, entity type/value, message, and cause; or
     *         {@code "null WebApplicationException"} if {@code ex} is null.
     */
    private static String describe(final WebApplicationException ex) {
        if (ex == null) {
            return "null WebApplicationException";
        }
        final var r = ex.getResponse();
        if (r == null) {
            return "WebApplicationException with no Response: " + ex.toString();
        }
        final var sb = new StringBuilder();
        sb.append("WebApplicationException: status=").append(r.getStatus());
        // Headers (metadata)
        final var headers = r.getMetadata();
        if (headers != null && !headers.isEmpty()) {
            final var hdrs = headers.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(", "));
            sb.append(", headers={").append(hdrs).append("}");
        }
        // Exception message if any
        if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            sb.append(", message=").append(ex.getMessage());
        }
        // Root cause if any
        if (ex.getCause() != null) {
            sb.append(", cause=").append(ex.getCause().toString());
        }
        return sb.toString();
    }

    /**
     * Accepts.
     *
     * @param acceptHeader
     *            the accept header
     * @param toAccept
     *            the to accept
     *
     * @return true, if successful
     */
    private static boolean accepts(final String acceptHeader, final String toAccept) {
        final var acceptValues = acceptHeader.split("\\s*(,|;)\\s*");
        Arrays.sort(acceptValues);
        return Arrays.binarySearch(acceptValues, toAccept) > -1
                || Arrays.binarySearch(acceptValues, toAccept.replaceAll("/.*$", "/*")) > -1
                || Arrays.binarySearch(acceptValues, "*/*") > -1;
    }
}