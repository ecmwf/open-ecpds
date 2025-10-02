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
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
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

    /** The Constant homeContent. */
    private static final StringBuilder homeContent = new StringBuilder();

    /** The Constant portalContent. */
    private static final StringBuilder portalContent = new StringBuilder();

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
     * Data portal head.
     *
     * @param ui
     *            the ui
     * @param request
     *            the request
     * @param response
     *            the response
     * @param filename
     *            the filename
     */
    @HEAD
    @Path("portal/{filename: .*}")
    public void dataPortalHead(@Context final UriInfo ui, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response, @PathParam("filename") final String filename) {
        dataFileHead(ui, getBasicAuth("portal:portal"), null, request, response, filename);
    }

    /**
     * Data portal get.
     *
     * @param ui
     *            the ui
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
    @Path("portal/{filename: .*}")
    public Response dataPortalGet(@Context final UriInfo ui, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response, @PathParam("filename") final String filename) {
        return dataFileGet(ui, getBasicAuth("portal:portal"), null, request, response, filename);
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
    @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
    @Path("home/{user}")
    public Response homeGet(@Context final UriInfo ui, @PathParam("user") final String user,
            @HeaderParam("range") final String range, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response) {
        return fileGet(ui, getBasicAuth(user + ":" + user), range, request, response, "");
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
    @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
    @Path("home/{user}/{filename: .*}")
    public Response homeGet(@Context final UriInfo ui, @PathParam("user") final String user,
            @HeaderParam("range") final String range, @Context final HttpServletRequest request,
            @Context final HttpServletResponse response, @PathParam("filename") final String filename) {
        return fileGet(ui, getBasicAuth(user + ":" + user), range, request, response, filename);
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
            @Context final HttpServletResponse response) {
        return fileGet(ui, authString, range, request, response, "");
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
            @Context final HttpServletResponse response, @PathParam("filename") final String filename) {
        _log.debug("REST received request: fileGet({})", filename);
        checkIsControlChannel(ui);
        final var session = getUserSession(authString, request, response);
        try {
            final var path = getFilename(session, filename);
            // Check if this is a list or get request?
            final var list = session.getFileListElement(path);
            if (!list.isDirectory()) {
                return dataFileGet(ui, authString, range, request, response, filename);
            }
            // Let's treat it as a list request!
            final var setup = session.getECtransSetup();
            if (setup != null && !setup.getBoolean(ECtransOptions.USER_PORTAL_SIMPLE_LIST)) {
                // Send the full html page
                return dataListGet(ui, authString, request, response, filename);
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
            return builder.entity(streamer).build();
        } catch (final WebApplicationException w) {
            _log.warn("dataFileGet - {}", describe(w));
            throw w;
        } catch (final FileNotFoundException e) {
            _log.warn("dataFileGet", e);
            throw newException(e, 404, "Not Found: " + e.getMessage());
        } catch (final EccmdException e) {
            _log.warn("dataFileGet", e);
            final var message = e.getMessage();
            if (message.contains("File not found") || message.contains("Destination not found")) {
                throw newException(e, 404, "Not Found: " + message);
            }
            throw newException(e, 500, message);
        } catch (final Throwable t) {
            _log.warn("dataFileGet", t);
            throw newException(t, 500, Format.getMessage(t));
        } finally {
            if (!success && session != null) {
                session.close(true);
            }
        }
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
    @Produces(MediaType.TEXT_HTML)
    @Path("data/list/{filename: .*}")
    public Response dataListGet(@Context final UriInfo ui, @HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @Context final HttpServletResponse response,
            @PathParam("filename") final String filename) {
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
            // Load the appropriate template!
            final var setup = session.getECtransSetup();
            final var anonymous = setup != null && setup.getBoolean(ECtransOptions.USER_PORTAL_ANONYMOUS);
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
            final var userId = session.getUser();
            Format.replaceAll(sb, "${userid}", userId);
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
                addListElement(listing, uriList, uriGet, filename, element, biggerIndexes);
            }
            final var length = listing.length();
            if (length > 0) {
                listing.deleteCharAt(length - 1);
            } else {
                listing.append("empty");
            }
            Format.replaceAll(sb, "${listing}", listing.toString());
            final var builder = Response.ok(sb.toString(), MediaType.TEXT_HTML);
            builder.lastModified(new Date(biggerIndexes[3]));
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
        final var path = new StringBuilder();
        // Let's build the list of links!
        final var currentPath = new StringBuilder();
        for (final String directory : "./".concat(filename).split("/")) {
            path.append(
                    "/<a href=\"" + uriList + "/" + (path.isEmpty() ? currentPath : currentPath.append(directory + "/"))
                            + "\">" + (path.isEmpty() ? "home" : directory) + "</a>");
        }
        Format.replaceAll(builder, "${path}", Format.normalizePath(path.toString()) + "/ directory");
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
     * @param biggerIndexes
     *            the bigger indexes
     *
     * @throws FileNotFoundException
     *             the file not found exception
     */
    private static void addListElement(final StringBuilder builder, final String uriList, final String uriGet,
            final String path, final FileListElement element, final long[] biggerIndexes) throws FileNotFoundException {
        final var name = element.getName();
        final String srvc;
        final String dir;
        if (element.isDirectory()) {
            // This is a directory so we forward the user to the list service!
            srvc = uriList;
            dir = "/";
        } else {
            // This is a file so we forward the user to the get service!
            srvc = uriGet;
            dir = "";
        }
        final var url = Format.normalizePath(srvc + "/" + path + "/" + name) + dir;
        builder.append(Format.formatString("<a href=\"" + url + "\">" + name + dir + "</a>",
                (int) biggerIndexes[0] + 15 + url.length(), ' ', true));
        final var fmt = new Formatter(builder);
        fmt.format(" %-16s", Format.formatTime("dd-MM-yyyy HH:mm", element.getTime()));
        fmt.close();
        final var isDir = element.isDirectory();
        builder.append(Format.formatString(isDir ? "-" : element.getSize().trim(), (int) biggerIndexes[1], ' ', false))
                .append(Format.formatString(isDir ? "-" : element.getComment().trim(), (int) biggerIndexes[2], ' ',
                        false))
                .append("\n");
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
        if (authString != null && authString.toLowerCase().startsWith("basic ")) {
            // Header is in the format "Basic 5tyc0uiDat4". Let's decode the data back to
            // its original string!
            try {
                final var decodedAuth = new String(BASE64Coder.decode(authString.split("\\s+")[1]));
                final var credentials = decodedAuth.split(":");
                if (credentials.length == 2) {
                    final var provider = NativeAuthenticationProvider.getInstance();
                    final var session = provider.getUserSession(request.getRemoteAddr(), credentials[0], credentials[1],
                            "https", (Closeable) () -> response.sendError(-1));
                    // Log headers if asked to do so?
                    if (Cnf.at("HttpPlugin", "logHeadersAndUri", false)) {
                        final var headerNames = request.getHeaderNames();
                        while (headerNames.hasMoreElements()) {
                            final var key = headerNames.nextElement();
                            _log.debug("Request header: {} = [{}]", key, request.getHeader(key));
                        }
                    }
                    return session;
                }
            } catch (final Throwable t) {
                _log.warn("getUserSession", t);
                final var message = t.getMessage();
                if (message != null) {
                    if (message.contains("Maximum number of connections exceeded")) {
                        throw new WebApplicationException(
                                Response.status(429).type(MediaType.TEXT_PLAIN).entity("Too Many Requests").build());
                    }
                    if (message.contains(" not allowed for ")) {
                        throw new WebApplicationException(
                                Response.status(403).type(MediaType.TEXT_PLAIN).entity("Forbidden").build());
                    }
                }
            }
        }
        // For whatever reason we were not able to authenticate the user!
        throw new WebApplicationException(Response.status(401).type(MediaType.TEXT_PLAIN).entity("Unauthorized")
                .header("WWW-Authenticate", "Basic realm=\"Data User Credentials\"").build());
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