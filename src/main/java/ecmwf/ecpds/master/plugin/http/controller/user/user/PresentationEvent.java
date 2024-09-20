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

package ecmwf.ecpds.master.plugin.http.controller.user.user;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.sql.Date;
import java.sql.Time;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.Activity;
import ecmwf.common.database.Event;
import ecmwf.ecpds.master.plugin.http.home.datafile.DataFileHome;
import ecmwf.ecpds.master.plugin.http.home.datafile.TransferGroupHome;
import ecmwf.ecpds.master.plugin.http.home.datafile.TransferServerHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferServer;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;

/**
 * The Class PresentationEvent.
 */
public class PresentationEvent {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(PresentationEvent.class);

    /** The ev. */
    private final Event ev;

    /** The comment. */
    private final String comment;

    /** The link id. */
    private String linkId;

    /** The type. */
    private String type = null;

    /** The related object. */
    private Object relatedObject = null;

    /** The name. */
    private String name = "";

    /** The file name. */
    private String fileName = "";

    /**
     * Instantiates a new presentation event.
     *
     * @param ev
     *            the ev
     */
    public PresentationEvent(final Event ev) {
        this.ev = ev;
        final var comment = this.ev.getComment();
        var pos = comment.indexOf(")");
        int pos2;
        if (pos == comment.length() - 1) {
            this.comment = "N/A";
        } else {
            if ((pos2 = comment.indexOf(":", pos)) >= 0) {
                pos = pos2;
            }
            this.comment = comment.substring(pos + 1);
        }
    }

    /**
     * Gets the action.
     *
     * @return the action
     */
    public String getAction() {
        return ev.getAction();
    }

    /**
     * Gets the activity.
     *
     * @return the activity
     */
    public Activity getActivity() {
        return ev.getActivity();
    }

    /**
     * Gets the activity id.
     *
     * @return the activity id
     */
    public long getActivityId() {
        return ev.getActivityId();
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public Date getDate() {
        return ev.getDate();
    }

    /**
     * Gets the error.
     *
     * @return the error
     */
    public boolean getError() {
        return ev.getError();
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return ev.getId();
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public Time getTime() {
        return ev.getTime();
    }

    /**
     * Gets the link id.
     *
     * @return the link id
     */
    public String getLinkId() {
        return linkId;
    }

    /**
     * Gets the related object.
     *
     * @return the related object
     */
    public Object getRelatedObject() {
        return this.relatedObject;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        if (this.type == null) {
            var pos = -1;
            int par;
            final var comment = this.ev.getComment();
            var type = "";
            try {
                if ((pos = comment.indexOf("DataTransfer(")) > 0 && (par = comment.indexOf(")", pos)) > 0) {
                    type = "datatransfer";
                    this.linkId = comment.substring(pos + type.length() + 1, par);
                    relatedObject = DataTransferHome.findByPrimaryKey(this.linkId);
                    final var t = (DataTransfer) relatedObject;
                    this.name = t.getId();
                    this.fileName = t.getTarget();
                } else if ((pos = comment.indexOf("DataFile(")) > 0 && (par = comment.indexOf(")", pos)) > 0) {
                    type = "datafile";
                    this.linkId = comment.substring(pos + type.length() + 1, par);
                    relatedObject = DataFileHome.findByPrimaryKey(this.linkId);
                    final var f = (DataFile) relatedObject;
                    this.name = f.getId();
                    this.fileName = f.getOriginal();
                } else if ((pos = comment.indexOf("Destination(")) > 0 && (par = comment.indexOf(")", pos)) > 0) {
                    type = "destination";
                    this.linkId = comment.substring(pos + type.length() + 1, par);
                    relatedObject = DestinationHome.findByPrimaryKey(this.linkId);
                    this.name = ((Destination) relatedObject).getName();
                    this.fileName = "N/A";
                } else if ((pos = comment.indexOf("Host(")) > 0 && (par = comment.indexOf(")", pos)) > 0) {
                    type = "host";
                    this.linkId = comment.substring(pos + type.length() + 1, par);
                    relatedObject = HostHome.findByPrimaryKey(this.linkId);
                    this.name = ((Host) relatedObject).getName();
                    this.fileName = "N/A";
                } else if ((pos = comment.indexOf("TransferServer(")) > 0 && (par = comment.indexOf(")", pos)) > 0) {
                    type = "transferserver";
                    this.linkId = comment.substring(pos + type.length() + 1, par);
                    relatedObject = TransferServerHome.findByPrimaryKey(this.linkId);
                    this.name = ((TransferServer) relatedObject).getName();
                    this.fileName = "N/A";
                } else if ((pos = comment.indexOf("TransferGroup(")) > 0 && (par = comment.indexOf(")", pos)) > 0) {
                    type = "transfergroup";
                    this.linkId = comment.substring(pos + type.length() + 1, par);
                    relatedObject = TransferGroupHome.findByPrimaryKey(this.linkId);
                    this.name = ((TransferGroup) relatedObject).getName();
                    this.fileName = "N/A";
                }
            } catch (final Throwable t) {
                log.warn("Could not get type for: " + comment, t);
                type = "(none)";
            }
            this.type = type;
        }
        return this.type;
    }
}
