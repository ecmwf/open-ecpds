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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;

import ecmwf.common.database.Alias;
import ecmwf.common.database.Association;
import ecmwf.common.database.Destination;
import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.Host;
import ecmwf.common.database.IncomingUser;
import ecmwf.common.database.TransferMethod;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStepStatus;

/**
 * The Class ECpdsApplication.
 */
public final class ECpdsApplication extends Application {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECpdsApplication.class);

    /**
     * {@inheritDoc}
     *
     * Gets the classes.
     */
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> s = new HashSet<>();
        s.add(ECpdsRESTV1.class);
        return s;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the singletons.
     */
    @SuppressWarnings("deprecation")
    @Override
    public Set<Object> getSingletons() {
        final Set<Object> s = new HashSet<>();
        final var jaxbProvider = new JacksonProvider();
        final var mapper = new ObjectMapper();
        mapper.getSerializationConfig().set(SerializationConfig.Feature.INDENT_OUTPUT, true);
        addMixInAnnotations(mapper, Destination.class, DestinationMixIn.class);
        addMixInAnnotations(mapper, IncomingUser.class, IncomingUserMixIn.class);
        addMixInAnnotations(mapper, Association.class, AssociationMixIn.class);
        addMixInAnnotations(mapper, Alias.class, AliasMixIn.class);
        addMixInAnnotations(mapper, Host.class, HostMixIn.class);
        addMixInAnnotations(mapper, ECUser.class, ECUserMixIn.class);
        addMixInAnnotations(mapper, TransferMethod.class, TransferMethodMixIn.class);
        addMixInAnnotations(mapper, ECtransModule.class, ECtransModuleMixIn.class);
        jaxbProvider.setMapper(mapper);
        s.add(jaxbProvider);
        return s;
    }

    /**
     * Adds the mix in annotations.
     *
     * @param mapper
     *            the mapper
     * @param target
     *            the target
     * @param mixinSource
     *            the mixin source
     */
    private void addMixInAnnotations(final ObjectMapper mapper, final Class<?> target, final Class<?> mixinSource) {
        mapper.getSerializationConfig().addMixInAnnotations(target, mixinSource);
        mapper.getDeserializationConfig().addMixInAnnotations(target, mixinSource);
    }

    /**
     * The Interface ECUserMixIn.
     */
    @JsonIgnoreProperties({ "collectionSize" })
    public interface ECUserMixIn {

        /**
         * Sets the gid.
         *
         * @param param
         *            the new gid
         */
        @JsonSetter("gid")
        void setGid(long param);

        /**
         * Sets the uid.
         *
         * @param param
         *            the new uid
         */
        @JsonSetter("uid")
        void setUid(long param);
    }

    /**
     * The Interface TransferMethodMixIn.
     */
    @JsonIgnoreProperties({ "collectionSize" })
    public interface TransferMethodMixIn {

        /**
         * Sets the active.
         *
         * @param param
         *            the new active
         */
        @JsonSetter("active")
        void setActive(boolean param);

        /**
         * Sets the resolve.
         *
         * @param param
         *            the new resolve
         */
        @JsonSetter("resolve")
        void setResolve(boolean param);

        /**
         * Sets the restrict.
         *
         * @param param
         *            the new restrict
         */
        @JsonSetter("restrict")
        void setRestrict(boolean param);
    }

    /**
     * The Interface ECtransModuleMixIn.
     */
    @JsonIgnoreProperties({ "collectionSize" })
    public interface ECtransModuleMixIn {

        /**
         * Sets the active.
         *
         * @param param
         *            the new active
         */
        @JsonSetter("active")
        void setActive(boolean param);
    }

    /**
     * The Interface IncomingUserMixIn.
     */
    @JsonIgnoreProperties({ "collectionSize", "connections", "password", "data", "active", "authorizedSSHKeys" })
    public interface IncomingUserMixIn {

        /**
         * Gets the iso.
         *
         * @return the iso
         */
        @JsonProperty("countryIso")
        String getIso();

        /**
         * Gets the comment.
         *
         * @return the comment
         */
        @JsonProperty("email")
        String getComment();

        /**
         * Gets the last login.
         *
         * @return the last login
         */
        @JsonProperty("lastLoginDate")
        java.sql.Timestamp getLastLogin();

        /**
         * Gets the last login host.
         *
         * @return the last login host
         */
        @JsonProperty("lastLoginInfo")
        String getLastLoginHost();

        /**
         * Sets the active.
         *
         * @param param
         *            the new active
         */
        @JsonSetter("active")
        void setActive(boolean param);

        /**
         * Sets the synchronized.
         *
         * @param param
         *            the new synchronized
         */
        @JsonSetter("synchronized")
        void setSynchronized(boolean param);

        /**
         * Sets the last login.
         *
         * @param param
         *            the new last login
         */
        @JsonSetter("lastLoginDate")
        void setLastLogin(java.sql.Timestamp param);
    }

    /**
     * The Interface DestinationMixIn.
     */
    @JsonIgnoreProperties({ "collectionSize", "ecuser", "hostForSource", "transferGroup", "schedulerValue", "country",
            "schedulerValueId", "userStatus", "statusCode" })
    public interface DestinationMixIn {

        /**
         * Sets the active.
         *
         * @param param
         *            the new active
         */
        @JsonSetter("active")
        void setActive(boolean param);

        /**
         * Sets the backup.
         *
         * @param param
         *            the new backup
         */
        @JsonSetter("backup")
        void setBackup(boolean param);

        /**
         * Sets the mail on end.
         *
         * @param param
         *            the new mail on end
         */
        @JsonSetter("mailOnEnd")
        void setMailOnEnd(boolean param);

        /**
         * Sets the mail on error.
         *
         * @param param
         *            the new mail on error
         */
        @JsonSetter("mailOnError")
        void setMailOnError(boolean param);

        /**
         * Sets the mail on start.
         *
         * @param param
         *            the new mail on start
         */
        @JsonSetter("mailOnStart")
        void setMailOnStart(boolean param);

        /**
         * Sets the monitor.
         *
         * @param param
         *            the new monitor
         */
        @JsonSetter("monitor")
        void setMonitor(boolean param);

        /**
         * Sets the group by date.
         *
         * @param param
         *            the new group by date
         */
        @JsonSetter("groupByDate")
        void setGroupByDate(boolean param);

        /**
         * Sets the stop if dirty.
         *
         * @param param
         *            the new stop if dirty
         */
        @JsonSetter("stopIfDirty")
        void setStopIfDirty(boolean param);

        /**
         * Sets the acquisition.
         *
         * @param param
         *            the new acquisition
         */
        @JsonSetter("acquisition")
        void setAcquisition(boolean param);

        /**
         * Sets the reset frequency.
         *
         * @param param
         *            the new reset frequency
         */
        @JsonSetter("resetFrequency")
        void setResetFrequency(long param);

        /**
         * Sets the max file size.
         *
         * @param param
         *            the new max file size
         */
        @JsonSetter("maxFileSize")
        void setMaxFileSize(long param);

        /**
         * Sets the transfer rate.
         *
         * @param param
         *            the new transfer rate
         */
        @JsonSetter("transferRate")
        void setTransferRate(long param);

        /**
         * Sets the if target exist.
         *
         * @param param
         *            the new if target exist
         */
        @JsonSetter("ifTargetExist")
        void setIfTargetExist(int param);

        /**
         * Sets the keep in spool.
         *
         * @param param
         *            the new keep in spool
         */
        @JsonSetter("keepInSpool")
        void setKeepInSpool(int param);

        /**
         * Sets the max connections.
         *
         * @param param
         *            the new max connections
         */
        @JsonSetter("maxConnections")
        void setMaxConnections(int param);

        /**
         * Sets the max pending.
         *
         * @param param
         *            the new max pending
         */
        @JsonSetter("maxPending")
        void setMaxPending(int param);

        /**
         * Sets the max requeue.
         *
         * @param param
         *            the new max requeue
         */
        @JsonSetter("maxRequeue")
        void setMaxRequeue(int param);

        /**
         * Sets the max start.
         *
         * @param param
         *            the new max start
         */
        @JsonSetter("maxStart")
        void setMaxStart(int param);

        /**
         * Sets the on host failure.
         *
         * @param param
         *            the new on host failure
         */
        @JsonSetter("onHostFailure")
        void setOnHostFailure(int param);

        /**
         * Sets the retry count.
         *
         * @param param
         *            the new retry count
         */
        @JsonSetter("retryCount")
        void setRetryCount(int param);

        /**
         * Sets the retry frequency.
         *
         * @param param
         *            the new retry frequency
         */
        @JsonSetter("retryFrequency")
        void setRetryFrequency(int param);

        /**
         * Sets the start frequency.
         *
         * @param param
         *            the new start frequency
         */
        @JsonSetter("startFrequency")
        void setStartFrequency(int param);

        /**
         * Sets the max inactivity.
         *
         * @param param
         *            the new max inactivity
         */
        @JsonSetter("maxInactivity")
        void setMaxInactivity(int param);

        /**
         * Sets the type.
         *
         * @param param
         *            the new type
         */
        @JsonSetter("type")
        void setType(int param);

        /**
         * Sets the scheduler value id.
         *
         * @param param
         *            the new scheduler value id
         */
        @JsonSetter("schedulerValueId")
        void setSchedulerValueId(int param);
    }

    /**
     * The Interface AssociationMixIn.
     */
    @JsonIgnoreProperties({ "collectionSize", "destination", "hostName", "destinationName" })
    public interface AssociationMixIn {

        /**
         * Sets the priority.
         *
         * @param param
         *            the new priority
         */
        @JsonSetter("priority")
        void setPriority(int param);
    }

    /**
     * The Interface AliasMixIn.
     */
    @JsonIgnoreProperties({ "collectionSize", "destination" })
    public interface AliasMixIn {

        /**
         * Gets the des name.
         *
         * @return the des name
         */
        @JsonProperty("aliasFrom")
        String getDesName();

        /**
         * Sets the des name.
         *
         * @param param
         *            the new des name
         */
        @JsonSetter("aliasFrom")
        void setDesName(String param);

        /**
         * Gets the destination name.
         *
         * @return the destination name
         */
        @JsonProperty("aliasTo")
        String getDestinationName();

        /**
         * Sets the destination name.
         *
         * @param param
         *            the new destination name
         */
        @JsonSetter("aliasTo")
        void setDestinationName(String param);
    }

    /**
     * The Interface HostMixIn.
     */
    @JsonIgnoreProperties({ "collectionSize", "ecuser", "transferMethod", "transferGroup", "hostStats", "hostLocation",
            "hostOutput", "useSourcePath", "name", "hostStatsId", "hostLocationId", "hostOutputId" })
    public interface HostMixIn {

        /**
         * Sets the active.
         *
         * @param param
         *            the new active
         */
        @JsonSetter("active")
        void setActive(boolean param);

        /**
         * Sets the check.
         *
         * @param param
         *            the new check
         */
        @JsonSetter("check")
        void setCheck(boolean param);

        /**
         * Sets the mail on error.
         *
         * @param param
         *            the new mail on error
         */
        @JsonSetter("mailOnError")
        void setMailOnError(boolean param);

        /**
         * Sets the mail on success.
         *
         * @param param
         *            the new mail on success
         */
        @JsonSetter("mailOnSuccess")
        void setMailOnSuccess(boolean param);

        /**
         * Sets the notify once.
         *
         * @param param
         *            the new notify once
         */
        @JsonSetter("notifyOnce")
        void setNotifyOnce(boolean param);

        /**
         * Sets the automatic location.
         *
         * @param param
         *            the new automatic location
         */
        @JsonSetter("automaticLocation")
        void setAutomaticLocation(boolean param);

        /**
         * Sets the use source path.
         *
         * @param param
         *            the new use source path
         */
        @JsonSetter("useSourcePath")
        void setUseSourcePath(boolean param);

        /**
         * Sets the check frequency.
         *
         * @param param
         *            the new check frequency
         */
        @JsonSetter("checkFrequency")
        void setCheckFrequency(long param);

        /**
         * Sets the acquisition frequency.
         *
         * @param param
         *            the new acquisition frequency
         */
        @JsonSetter("acquisitionFrequency")
        void setAcquisitionFrequency(long param);

        /**
         * Sets the max connections.
         *
         * @param param
         *            the new max connections
         */
        @JsonSetter("maxConnections")
        void setMaxConnections(int param);

        /**
         * Sets the retry count.
         *
         * @param param
         *            the new retry count
         */
        @JsonSetter("retryCount")
        void setRetryCount(int param);

        /**
         * Sets the retry frequency.
         *
         * @param param
         *            the new retry frequency
         */
        @JsonSetter("retryFrequency")
        void setRetryFrequency(int param);
    }

    /**
     * The Class DestinationForREST. This class is used to encapsulate the Destination class into a front end class to
     * show only the required fields for the destination/* endpoints.
     */
    protected static class DestinationForREST {

        /** The destination. */
        private final Destination destination;

        /**
         * Instantiates a new destination for REST.
         *
         * @param destination
         *            the destination
         */
        DestinationForREST(final Destination destination) {
            this.destination = destination;
        }

        /**
         * Gets the country iso.
         *
         * @return the country iso
         */
        public String getCountryIso() {
            return destination.getCountryIso();
        }

        /**
         * Gets the active.
         *
         * @return the active
         */
        public boolean getActive() {
            return destination.getActive();
        }

        /**
         * Gets the status code.
         *
         * @return the status code
         */
        public String getStatusCode() {
            return destination.getStatusCode();
        }

        /**
         * Gets the backup.
         *
         * @return the backup
         */
        public boolean getBackup() {
            return destination.getBackup();
        }

        /**
         * Gets the comment.
         *
         * @return the comment
         */
        public String getComment() {
            return destination.getComment();
        }

        /**
         * Gets the monitor.
         *
         * @return the monitor
         */
        public boolean getMonitor() {
            return destination.getMonitor();
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return destination.getName();
        }

        /**
         * Gets the acquisition.
         *
         * @return the acquisition
         */
        public boolean getAcquisition() {
            return destination.getAcquisition();
        }

        /**
         * Gets the user mail.
         *
         * @return the user mail
         */
        public String getUserMail() {
            return destination.getUserMail();
        }

        /**
         * Gets the filter name.
         *
         * @return the filter name
         */
        public String getFilterName() {
            return destination.getFilterName();
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        public int getType() {
            return destination.getType();
        }
    }

    /**
     * To destination for REST list. Convert Destination list into DestinationForREST list.
     *
     * @param list
     *            the list
     *
     * @return the list
     */
    protected static List<DestinationForREST> toDestinationForRESTList(final Collection<Destination> list) {
        final List<DestinationForREST> result = new ArrayList<>(list.size());
        final var i = list.iterator();
        while (i.hasNext()) {
            result.add(new DestinationForREST(i.next()));
        }
        return result;
    }

    /**
     * The Class ProductStepStatusForREST.
     *
     * This class is used to encapsulate the ProductStepStatus class into a front end class to show only the required
     * fields with the dates converted in to the number of seconds since ecpoch.
     */
    protected static class ProductStepStatusForREST {

        /** The pss. */
        private final ProductStepStatus pss;

        /**
         * Instantiates a new product step status for REST.
         *
         * @param pss
         *            the pss
         */
        ProductStepStatusForREST(final ProductStepStatus pss) {
            this.pss = pss;
        }

        /**
         * To seconds since epoch.
         *
         * @param date
         *            the date
         *
         * @return the long
         */
        private Long toSecondsSinceEpoch(final Date date) {
            return date != null ? date.getTime() / 1000L : null;
        }

        /**
         * Gets the product.
         *
         * @return the product
         */
        public String getProduct() {
            return pss.getProduct();
        }

        /**
         * Gets the time.
         *
         * @return the time
         */
        public String getTime() {
            return pss.getTime();
        }

        /**
         * Gets the buffer.
         *
         * @return the buffer
         */
        public long getBuffer() {
            return pss.getBuffer();
        }

        /**
         * Gets the step.
         *
         * @return the step
         */
        public long getStep() {
            return pss.getStep();
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        public String getType() {
            return pss.getType();
        }

        /**
         * Gets the arrival time.
         *
         * @return the arrival time
         */
        public Long getArrivalTime() {
            return toSecondsSinceEpoch(pss.getArrivalTime());
        }

        /**
         * Gets the status.
         *
         * @return the status
         */
        public String getStatus() {
            return pss.getGenerationStatusFormattedCode();
        }

        /**
         * Gets the minutes before schedule.
         *
         * @return the minutes before schedule
         */
        public long getMinutesBeforeSchedule() {
            return pss.getMinutesBeforeSchedule();
        }

        /**
         * Gets the scheduled time.
         *
         * @return the scheduled time
         */
        public Long getScheduledTime() {
            return toSecondsSinceEpoch(pss.getScheduledTime());
        }

        /**
         * Gets the last update time.
         *
         * @return the last update time
         */
        public Long getLastUpdate() {
            return toSecondsSinceEpoch(pss.getLastUpdate());
        }

        /**
         * Gets the product time.
         *
         * @return the product time
         */
        public Long getProductTime() {
            return toSecondsSinceEpoch(pss.getProductTime());
        }
    }

    /**
     * To product step status for REST list. Convert ProductStepStatus list into ProductStepStatusForREST list.
     *
     * @param list
     *            the list
     *
     * @return the list
     */
    protected static List<ProductStepStatusForREST> toProductStepStatusForRESTList(
            final Collection<ProductStepStatus> list) {
        final List<ProductStepStatusForREST> result = new ArrayList<>(list.size());
        final var i = list.iterator();
        while (i.hasNext()) {
            result.add(new ProductStepStatusForREST(i.next()));
        }
        return result;
    }

    /**
     * The Class JacksonProvider. Allow catching the parsing errors and send an appropriate exception to the container.
     */
    @javax.ws.rs.ext.Provider
    public static final class JacksonProvider extends JacksonJaxbJsonProvider {
        /**
         * Gets the throwable message.
         *
         * @param t
         *            the t
         *
         * @return the string
         */
        private static String getThrowableMessage(Throwable t) {
            String message = null;
            while (t != null && (message = t.getMessage()) == null && t.getCause() != null) {
                t = t.getCause();
            }
            return message == null ? "Server Error" : message;
        }

        /**
         * Read from.
         *
         * @param type
         *            the type
         * @param genericType
         *            the generic type
         * @param annotations
         *            the annotations
         * @param mediaType
         *            the media type
         * @param httpHeaders
         *            the http headers
         * @param entityStream
         *            the entity stream
         *
         * @return the object
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public Object readFrom(final Class<Object> type, final Type genericType, final Annotation[] annotations,
                final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders,
                final InputStream entityStream) throws IOException {
            try {
                return super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
            } catch (final UnrecognizedPropertyException e) {
                _log.debug("Unknown field", e);
                var message = e.getMessage();
                final var index = message.indexOf(" (Class ");
                if (index != -1) {
                    message = message.substring(0, index);
                }
                throw new WebApplicationException(Response.status(Status.PRECONDITION_FAILED).entity(message).build());
            } catch (final Throwable t) {
                _log.debug("Parsing error", t);
                throw new WebApplicationException(
                        Response.status(Status.PRECONDITION_FAILED).entity(getThrowableMessage(t)).build());
            }
        }
    }
}
