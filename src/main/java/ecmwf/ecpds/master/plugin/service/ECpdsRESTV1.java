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

package ecmwf.ecpds.master.plugin.service;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DestinationBackup;
import ecmwf.common.text.BASE64Coder;
import ecmwf.common.version.Version;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStepStatusHome;
import ecmwf.ecpds.master.transfer.DestinationOption;

/**
 * The Class ECpdsRESTV1.
 */
@Path("v1")
public final class ECpdsRESTV1 {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECpdsRESTV1.class);

    // Version

    /**
     * Gets the version.
     *
     * @param request
     *            the request
     *
     * @return the version
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("version")
    public Response getVersion(@Context final HttpServletRequest request) {
        _log.debug("getVersion");
        try {
            final var message = RESTMessage.getSuccessMessage();
            message.put("version", Version.getFullVersion());
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("getVersion", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("getVersion", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Incoming user add.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param id
     *            the id
     * @param pass
     *            the pass
     * @param email
     *            the email
     * @param iso
     *            the iso
     *
     * @return the response
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("incoming/user/add")
    public Response incomingUserAdd(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @QueryParam("id") final String id,
            @QueryParam("pass") final String pass, @QueryParam("email") final String email,
            @QueryParam("iso") final String iso) {
        _log.debug("incomingUserAdd");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            _checkParameter("id", id);
            MasterManager.getDB().incomingUserAdd(userNameAndPassword, id, pass, email, iso);
            return RESTMessage.getSuccessMessage().getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("incomingUserAdd", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("incomingUserAdd", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Incoming user add2.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param id
     *            the id
     * @param email
     *            the email
     * @param iso
     *            the iso
     *
     * @return the response
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("incoming/user/add2")
    public Response incomingUserAdd2(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @QueryParam("id") final String id,
            @QueryParam("email") final String email, @QueryParam("iso") final String iso) {
        _log.debug("incomingUserAdd2");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            _checkParameter("id", id);
            _checkParameter("email", email);
            _checkParameter("iso", iso);
            final var message = RESTMessage.getSuccessMessage();
            message.put("pass", MasterManager.getDB().incomingUserAdd2(userNameAndPassword, id, email, iso));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("incomingUserAdd2", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("incomingUserAdd2", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Incoming user list.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param destination
     *            the destination
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("incoming/user/list")
    public Response incomingUserList(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @QueryParam("destination") final String destination) {
        _log.debug("incomingUserList");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            final var message = RESTMessage.getSuccessMessage();
            message.put("userList", MasterManager.getDB().incomingUserList(userNameAndPassword, destination));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("incomingUserList", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("incomingUserList", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * User del.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param id
     *            the id
     *
     * @return the response
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("incoming/user/del/{id}")
    public Response userDel(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @PathParam("id") final String id) {
        _log.debug("incomingUserDel");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            _checkParameter("id", id);
            MasterManager.getDB().incomingUserDel(userNameAndPassword, id);
            return RESTMessage.getSuccessMessage().getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("incomingUserDel", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("incomingUserDel", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Incoming category add.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param id
     *            the id
     * @param categories
     *            the categories
     *
     * @return the response
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("incoming/category/add")
    public Response incomingCategoryAdd(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @QueryParam("id") final String id,
            final List<String> categories) {
        _log.debug("incomingCategoryAdd");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            _checkParameter("id", id);
            _checkParameter("categories", categories);
            MasterManager.getDB().incomingCategoryAdd(userNameAndPassword, id, categories);
            return RESTMessage.getSuccessMessage().getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("incomingCategoryAdd", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("incomingCategoryAdd", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Incoming association add.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param id
     *            the id
     * @param destination
     *            the destination
     *
     * @return the response
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("incoming/association/add")
    public Response incomingAssociationAdd(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @QueryParam("id") final String id,
            @QueryParam("destination") final String destination) {
        _log.debug("incomingAssociationAdd");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            _checkParameter("id", id);
            _checkParameter("destination", destination);
            MasterManager.getDB().incomingAssociationAdd(userNameAndPassword, id, destination);
            return RESTMessage.getSuccessMessage().getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("incomingAssociationAdd", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("incomingAssociationAdd", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Incoming association del.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param id
     *            the id
     * @param destination
     *            the destination
     *
     * @return the response
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("incoming/association/del")
    public Response incomingAssociationDel(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @QueryParam("id") final String id,
            @QueryParam("destination") final String destination) {
        _log.debug("incomingAssociationDel");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            _checkParameter("id", id);
            _checkParameter("destination", destination);
            MasterManager.getDB().incomingAssociationDel(userNameAndPassword, id, destination);
            return RESTMessage.getSuccessMessage().getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("incomingAssociationDel", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("incomingAssociationDel", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Incoming association list.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param id
     *            the id
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("incoming/association/list")
    public Response incomingAssociationList(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @QueryParam("id") final String id) {
        _log.debug("incomingAssociationList");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            _checkParameter("id", id);
            final var message = RESTMessage.getSuccessMessage();
            message.put("associationList",
                    Arrays.asList(MasterManager.getDB().incomingAssociationList(userNameAndPassword, id)));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("incomingAssociationList", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("incomingAssociationList", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Destination list.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param iso
     *            the iso
     * @param id
     *            the id
     * @param type
     *            the type
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("destination/list")
    public Response destinationList(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @QueryParam("iso") final String iso,
            @QueryParam("id") final String id, @QueryParam("type") final Integer type) {
        _log.debug("destinationList");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            final var message = RESTMessage.getSuccessMessage();
            message.put("destinationList", ECpdsApplication.toDestinationForRESTList(
                    MasterManager.getDB().destinationList(userNameAndPassword, id, iso, type)));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("destinationList", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("destinationList", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Destination.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param name
     *            the name
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("destination/{name}")
    public Response destination(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @PathParam("name") final String name) {
        _log.debug("destination");
        try {
            _getUserNameAndPassword(authString, request);
            _checkParameter("name", name);
            final var message = RESTMessage.getSuccessMessage();
            message.put("destination",
                    new ECpdsApplication.DestinationForREST(MasterManager.getDB().getDestination(name, false)));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("destination", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("destination", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Destination list.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param iso
     *            the iso
     * @param id
     *            the id
     * @param type
     *            the type
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("destination/backup")
    public Response getDestinationBackup(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @QueryParam("iso") final String iso,
            @QueryParam("id") final String id, @QueryParam("type") final Integer type) {
        _log.debug("getDestinationBackup");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            final var message = RESTMessage.getSuccessMessage();
            message.put("backup", MasterManager.getDB().getDestinationBackup(userNameAndPassword, id, iso, type, null));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("getDestinationBackup", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("getDestinationBackup", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Destination.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param name
     *            the name
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("destination/backup/{name}")
    public Response getDestinationBackup(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @PathParam("name") final String name) {
        _log.debug("getDestinationBackup");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            _checkParameter("name", name);
            final var message = RESTMessage.getSuccessMessage();
            message.put("backup",
                    MasterManager.getDB().getDestinationBackup(userNameAndPassword, null, null, null, name));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("getDestinationBackup", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("getDestinationBackup", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * The Class PutDestinationBackupRequest.
     */
    static final class PutDestinationBackupRequest implements Serializable {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 4903532344315324986L;

        /** The backup. */
        public DestinationBackup backup;

        /** The success. */
        public String success;
    }

    /**
     * Put destination backup.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param copySharedHost
     *            the copy shared host
     * @param backupRequest
     *            the backup request
     *
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("destination/backup")
    public Response putDestinationBackup(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @QueryParam("copySharedHost") final Boolean copySharedHost,
            final PutDestinationBackupRequest backupRequest) {
        _log.debug("putDestinationBackup");
        try {
            final var message = RESTMessage.getSuccessMessage();
            final var copy = copySharedHost != null && copySharedHost;
            message.put("message", "Number of Destination(s) created: " + MasterManager.getDB()
                    .putDestinationBackup(_getUserNameAndPassword(authString, request), backupRequest.backup, copy));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("putDestinationBackup", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("putDestinationBackup", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Destination type list.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("destination/type/list")
    public Response destinationTypeList(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request) {
        _log.debug("destinationTypeList");
        try {
            _getUserNameAndPassword(authString, request);
            final var message = RESTMessage.getSuccessMessage();
            message.put("typeList", DestinationOption.getTypes(false));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("destinationTypeList", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("destinationTypeList", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Destination country list.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("destination/country/list")
    public Response destinationCountryList(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request) {
        _log.debug("destinationCountryList");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            final var message = RESTMessage.getSuccessMessage();
            message.put("countryList", MasterManager.getDB().destinationCountryList(userNameAndPassword));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("destinationCountryList", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("destinationCountryList", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Monitoring status list.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param product
     *            the product
     * @param time
     *            the time
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("monitoring/summary/{product}/{time}")
    public Response stepStatusList(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @PathParam("product") final String product,
            @PathParam("time") final String time) {
        _log.debug("stepStatusList");
        try {
            _getUserNameAndPassword(authString, request);
            _checkParameter("product", product);
            _checkParameter("time", time);
            final var message = RESTMessage.getSuccessMessage();
            message.put("stepStatusList",
                    ECpdsApplication.toProductStepStatusForRESTList(ProductStepStatusHome.findAll(product, time)));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("stepStatusList", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("stepStatusList", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Monitoring status list.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param product
     *            the product
     * @param time
     *            the time
     * @param step
     *            the step
     * @param type
     *            the type
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("monitoring/summary/{product}/{time}/{step}/{type}")
    public Response stepStatusHistoryList(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @PathParam("product") final String product,
            @PathParam("time") final String time, @PathParam("step") final String step,
            @PathParam("type") final String type) {
        _log.debug("stepStatusHistoryList");
        try {
            _getUserNameAndPassword(authString, request);
            _checkParameter("product", product);
            _checkParameter("time", time);
            _checkParameter("step", step);
            _checkParameter("type", type);
            var stepValue = 0L;
            try {
                stepValue = Long.parseLong(step);
            } catch (final NumberFormatException e) {
                throw _newException(Status.PRECONDITION_FAILED, "'step' has to be a number");
            }
            final var message = RESTMessage.getSuccessMessage();
            message.put("stepStatusHistoryList", ECpdsApplication.toProductStepStatusForRESTList(
                    ProductStepStatusHome.findHistory(product, time, stepValue, type, -1)));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("stepStatusHistoryList", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("stepStatusHistoryList", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Datafile put.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param destination
     *            the destination
     * @param metadata
     *            the metadata
     * @param source
     *            the source
     * @param uniquename
     *            the uniquename
     * @param target
     *            the target
     * @param priority
     *            the priority
     * @param lifetime
     *            the lifetime
     * @param at
     *            the at
     * @param standby
     *            the standby
     * @param force
     *            the force
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("datafile/put")
    public Response datafilePut(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @QueryParam("destination") final String destination,
            @QueryParam("metadata") final String metadata, @QueryParam("source") final String source,
            @QueryParam("uniquename") final String uniquename, @QueryParam("target") final String target,
            @QueryParam("priority") final Integer priority, @QueryParam("lifetime") final String lifetime,
            @QueryParam("at") final String at, @QueryParam("standby") final Boolean standby,
            @QueryParam("force") final Boolean force) {
        _log.debug("datafilePut");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            _checkParameter("destination", destination);
            _checkParameter("source", source);
            final var message = RESTMessage.getSuccessMessage();
            message.put("id", MasterManager.getDB().datafilePut(userNameAndPassword, request.getRemoteHost(),
                    destination, metadata, source, uniquename, target, priority, lifetime, at, standby, force));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("datafilePut", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("datafilePut", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Datafile size.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param id
     *            the id
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("datafile/size")
    public Response datafileSize(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @QueryParam("id") final Long id) {
        _log.debug("datafileSize");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            _checkParameter("id", id);
            final var message = RESTMessage.getSuccessMessage();
            message.put("size", MasterManager.getDB().datafileSize(userNameAndPassword, id));
            return message.getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("datafileSize", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("datafileSize", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    /**
     * Datafile del.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     * @param id
     *            the id
     *
     * @return the response
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("datafile/del")
    public Response datafileDel(@HeaderParam("authorization") final String authString,
            @Context final HttpServletRequest request, @QueryParam("id") final Long id) {
        _log.debug("datafileDel");
        try {
            final var userNameAndPassword = _getUserNameAndPassword(authString, request);
            _checkParameter("id", id);
            MasterManager.getDB().datafileDel(userNameAndPassword, id);
            return RESTMessage.getSuccessMessage().getResponse();
        } catch (final WebApplicationException w) {
            _log.warn("datafileDel", w);
            throw w;
        } catch (final Throwable t) {
            _log.warn("datafileDel", t);
            return RESTMessage.getErrorMessage(t).getResponse();
        }
    }

    // Utilities

    /**
     * Gets the user name.
     *
     * @param authString
     *            the auth string
     * @param request
     *            the request
     *
     * @return the string
     *
     * @throws WebApplicationException
     *             the web application exception
     */
    private static String _getUserNameAndPassword(final String authString, final HttpServletRequest request)
            throws WebApplicationException {
        if ("http".equalsIgnoreCase(request.getScheme())) {
            throw _newException(Status.FORBIDDEN, "Operation only available through a secure connection (https)");
        }
        String userNameAndPassword = null;
        if (authString != null && authString.toLowerCase().startsWith("basic ")) {
            // Header is in the format "Basic 5tyc0uiDat4". Let's decode the data back to
            // its original string!
            try {
                final var decodedAuth = new String(BASE64Coder.decode(authString.split("\\s+")[1]));
                final var credentials = decodedAuth.split(":");
                if (credentials.length == 2) {
                    userNameAndPassword = decodedAuth;
                }
            } catch (final Throwable t) {
                _log.warn("_getUserNameAndPassword", t);
            }
        }
        if (userNameAndPassword == null) {
            throw _newException(Status.UNAUTHORIZED, "Operation only available to registered users");
        }
        return userNameAndPassword;
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
    private static WebApplicationException _newException(final Response.Status status, final String message) {
        return new WebApplicationException(Response.status(status).entity(message).build());
    }

    /**
     * _check parameter.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    private static void _checkParameter(final String name, final String value) {
        if (value == null || value.trim().length() == 0) {
            throw _newException(Status.PRECONDITION_FAILED, "Missing parameter: " + name);
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
    private static void _checkParameter(final String name, final Object value) {
        if (value == null) {
            throw _newException(Status.PRECONDITION_FAILED, "Missing parameter: " + name);
        }
    }

    /**
     * The Class RESTMessage.
     */
    private static final class RESTMessage {
        /** The _message. */
        private final Map<String, Object> _message = new HashMap<>();

        /**
         * Gets the throwable message.
         *
         * @param t
         *            the t
         *
         * @return the string
         */
        private static String _getThrowableMessage(Throwable t) {
            String message = null;
            while (t != null && (message = t.getMessage()) == null && t.getCause() != null) {
                t = t.getCause();
            }
            return message == null ? "Server Error" : message;
        }

        /**
         * Gets the error message.
         *
         * @param t
         *            the t
         *
         * @return the error message
         */
        static RESTMessage getErrorMessage(final Throwable t) {
            return getErrorMessage(_getThrowableMessage(t));
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
            _message.put(key, value);
        }

        /**
         * Gets the response.
         *
         * @return the response
         */
        Response getResponse() {
            return Response.ok().entity(_message).build();
        }
    }
}
