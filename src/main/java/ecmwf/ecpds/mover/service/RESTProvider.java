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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;

import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.Host;
import ecmwf.common.database.HostLocation;
import ecmwf.common.database.TransferMethod;

/**
 * The Class RESTProvider.
 */
final class RESTProvider {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RESTProvider.class);

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
        private static String _getThrowableMessage(Throwable t) {
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
                        Response.status(Status.PRECONDITION_FAILED).entity(_getThrowableMessage(t)).build());
            }
        }
    }

    /**
     * Gets the jackson provider.
     *
     * @return the jackson provider
     */
    public static JacksonProvider getJacksonProvider() {
        final var jaxbProvider = new JacksonProvider();
        final var mapper = new ObjectMapper();
        final var d = mapper.getDeserializationConfig();
        d.addMixInAnnotations(DataTransfer.class, DataTransferMixIn.class);
        d.addMixInAnnotations(DataFile.class, DataFileMixIn.class);
        d.addMixInAnnotations(ECUser.class, ECUserMixIn.class);
        d.addMixInAnnotations(HostLocation.class, HostLocationMixIn.class);
        d.addMixInAnnotations(Host.class, HostMixIn.class);
        d.addMixInAnnotations(TransferMethod.class, TransferMethodMixIn.class);
        d.addMixInAnnotations(ECtransModule.class, ECtransModuleMixIn.class);
        jaxbProvider.setMapper(mapper);
        return jaxbProvider;
    }

    /**
     * The Interface DataTransferMixIn.
     */
    @JsonIgnoreProperties({ "originalTransferServer", "transferServer", "destination", "backupHost", "monitoringValue",
            "collectionSize", "proxyName" })
    public interface DataTransferMixIn {
        /**
         * Sets the compressed.
         *
         * @param param
         *            the new compressed
         */
        void setCompressed(String param);

        /**
         * Sets the compressedOnTheFly.
         *
         * @param param
         *            the new compressedOnTheFly
         */
        void setCompressedOnTheFly(boolean param);

        /**
         * Sets the duration on close.
         *
         * @param param
         *            the new duration
         */
        void setDurationOnClose(long param);

        /**
         * Sets the statistics.
         *
         * @param param
         *            the new statistics
         */
        @JsonProperty("statistics")
        void setStatistics(String param);

        /**
         * Sets the data file id.
         *
         * @param param
         *            the new data file id
         */
        @JsonProperty("dataFileId")
        void setDataFileId(long param);

        /**
         * Sets the deleted.
         *
         * @param param
         *            the new deleted
         */
        @JsonProperty("deleted")
        void setDeleted(boolean param);

        /**
         * Sets the event.
         *
         * @param param
         *            the new event
         */
        @JsonProperty("event")
        void setEvent(boolean param);

        /**
         * Sets the replicated.
         *
         * @param param
         *            the new replicated
         */
        @JsonProperty("replicated")
        void setReplicated(boolean param);

        /**
         * Sets the asap.
         *
         * @param param
         *            the new asap
         */
        @JsonProperty("asap")
        void setAsap(boolean param);

        /**
         * Sets the ratio.
         *
         * @param param
         *            the new ratio
         */
        @JsonProperty("ratio")
        void setRatio(double param);

        /**
         * Sets the duration.
         *
         * @param param
         *            the new duration
         */
        @JsonProperty("duration")
        void setDuration(long param);

        /**
         * Sets the id.
         *
         * @param param
         *            the new id
         */
        @JsonProperty("id")
        void setId(long param);

        /**
         * Sets the priority.
         *
         * @param param
         *            the new priority
         */
        @JsonProperty("priority")
        void setPriority(int param);

        /**
         * Sets the requeue count.
         *
         * @param param
         *            the new requeue count
         */
        @JsonProperty("requeueCount")
        void setRequeueCount(int param);

        /**
         * Sets the replicate count.
         *
         * @param param
         *            the new replicate count
         */
        @JsonProperty("replicateCount")
        void setReplicateCount(int param);

        /**
         * Sets the requeue history.
         *
         * @param param
         *            the new requeue history
         */
        @JsonProperty("requeueHistory")
        void setRequeueHistory(int param);

        /**
         * Sets the sent.
         *
         * @param param
         *            the new sent
         */
        @JsonProperty("sent")
        void setSent(long param);

        /**
         * Sets the size.
         *
         * @param param
         *            the new size
         */
        @JsonProperty("size")
        void setSize(long param);

        /**
         * Sets the start count.
         *
         * @param param
         *            the new start count
         */
        @JsonProperty("startCount")
        void setStartCount(int param);

        /**
         * Sets the time step.
         *
         * @param param
         *            the new time step
         */
        @JsonProperty("timeStep")
        void setTimeStep(long param);

        /**
         * Sets the monitoring value id.
         *
         * @param param
         *            the new monitoring value id
         */
        @JsonProperty("monitoringValueId")
        void setMonitoringValueId(int param);
    }

    /**
     * The Interface DataFileMixIn.
     */
    @JsonIgnoreProperties({ "transferGroup", "monitoringValue", "collectionSize" })
    public interface DataFileMixIn {
        /**
         * Sets the filter size.
         *
         * @param param
         *            the new filter size
         */
        @JsonProperty("filterSize")
        void setFilterSize(long param);

        /**
         * Sets the deleted.
         *
         * @param param
         *            the new deleted
         */
        @JsonProperty("deleted")
        void setDeleted(boolean param);

        /**
         * Sets the standby.
         *
         * @param param
         *            the new standby
         */
        @JsonProperty("standby")
        void setStandby(boolean param);

        /**
         * Sets the index.
         *
         * @param param
         *            the new index
         */
        @JsonProperty("index")
        void setIndex(int param);

        /**
         * Sets the delete original.
         *
         * @param param
         *            the new delete original
         */
        @JsonProperty("deleteOriginal")
        void setDeleteOriginal(boolean param);

        /**
         * Sets the downloaded.
         *
         * @param param
         *            the new downloaded
         */
        @JsonProperty("downloaded")
        void setDownloaded(boolean param);

        /**
         * Sets the id.
         *
         * @param param
         *            the new id
         */
        @JsonProperty("id")
        void setId(long param);

        /**
         * Sets the removed.
         *
         * @param param
         *            the new removed
         */
        @JsonProperty("removed")
        void setRemoved(boolean param);

        /**
         * Sets the size.
         *
         * @param param
         *            the new size
         */
        @JsonProperty("size")
        void setSize(long param);

        /**
         * Sets the gets the duration.
         *
         * @param param
         *            the new gets the duration
         */
        @JsonProperty("getDuration")
        void setGetDuration(long param);

        /**
         * Sets the gets the complete duration.
         *
         * @param param
         *            the new gets the complete duration
         */
        @JsonProperty("getCompleteDuration")
        void setGetCompleteDuration(long param);

        /**
         * Sets the time step.
         *
         * @param param
         *            the new time step
         */
        @JsonProperty("timeStep")
        void setTimeStep(long param);

        /**
         * Sets the monitoring value id.
         *
         * @param param
         *            the new monitoring value id
         */
        @JsonProperty("monitoringValueId")
        void setMonitoringValueId(Integer param);

        /**
         * Sets the file instance.
         *
         * @param param
         *            the new file instance
         */
        @JsonProperty("fileInstance")
        void setFileInstance(Integer param);

        /**
         * Sets the file system.
         *
         * @param param
         *            the new file system
         */
        @JsonProperty("fileSystem")
        void setFileSystem(Integer param);
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
        @JsonProperty("gid")
        void setGid(long param);

        /**
         * Sets the uid.
         *
         * @param param
         *            the new uid
         */
        @JsonProperty("uid")
        void setUid(long param);
    }

    /**
     * The Interface HostMixIn.
     */
    @JsonIgnoreProperties({ "collectionSize" })
    public interface HostLocationMixIn {
        /**
         * Sets the ip.
         *
         * @param param
         *            the new ip
         */
        @JsonProperty("ip")
        void setIp(boolean param);
    }

    /**
     * The Interface HostMixIn.
     */
    @JsonIgnoreProperties({ "transferGroup", "hostStats", "hostOutput", "collectionSize" })
    public interface HostMixIn {
        /**
         * Sets the active.
         *
         * @param param
         *            the new active
         */
        @JsonProperty("active")
        void setActive(boolean param);

        /**
         * Sets the check.
         *
         * @param param
         *            the new check
         */
        @JsonProperty("check")
        void setCheck(boolean param);

        /**
         * Sets the check frequency.
         *
         * @param param
         *            the new check frequency
         */
        @JsonProperty("checkFrequency")
        void setCheckFrequency(long param);

        /**
         * Sets the acquisition frequency.
         *
         * @param param
         *            the new acquisition frequency
         */
        @JsonProperty("acquisitionFrequency")
        void setAcquisitionFrequency(long param);

        /**
         * Sets the connections.
         *
         * @param param
         *            the new connections
         */
        @JsonProperty("connections")
        void setConnections(int param);

        /**
         * Sets the duration.
         *
         * @param param
         *            the new duration
         */
        @JsonProperty("duration")
        void setDuration(long param);

        /**
         * Sets the mail on error.
         *
         * @param param
         *            the new mail on error
         */
        @JsonProperty("mailOnError")
        void setMailOnError(boolean param);

        /**
         * Sets the mail on success.
         *
         * @param param
         *            the new mail on success
         */
        @JsonProperty("mailOnSuccess")
        void setMailOnSuccess(boolean param);

        /**
         * Sets the max connections.
         *
         * @param param
         *            the new max connections
         */
        @JsonProperty("maxConnections")
        void setMaxConnections(int param);

        /**
         * Sets the notify once.
         *
         * @param param
         *            the new notify once
         */
        @JsonProperty("notifyOnce")
        void setNotifyOnce(boolean param);

        /**
         * Sets the retry count.
         *
         * @param param
         *            the new retry count
         */
        @JsonProperty("retryCount")
        void setRetryCount(int param);

        /**
         * Sets the retry frequency.
         *
         * @param param
         *            the new retry frequency
         */
        @JsonProperty("retryFrequency")
        void setRetryFrequency(int param);

        /**
         * Sets the sent.
         *
         * @param param
         *            the new sent
         */
        @JsonProperty("sent")
        void setSent(long param);

        /**
         * Sets the valid.
         *
         * @param param
         *            the new valid
         */
        @JsonProperty("valid")
        void setValid(boolean param);

        /**
         * Sets the automatic location.
         *
         * @param param
         *            the new automatic location
         */
        @JsonProperty("automaticLocation")
        void setAutomaticLocation(boolean param);
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
        @JsonProperty("active")
        void setActive(boolean param);

        /**
         * Sets the resolve.
         *
         * @param param
         *            the new resolve
         */
        @JsonProperty("resolve")
        void setResolve(boolean param);

        /**
         * Sets the restrict.
         *
         * @param param
         *            the new restrict
         */
        @JsonProperty("restrict")
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
        @JsonProperty("active")
        void setActive(boolean param);
    }
}
