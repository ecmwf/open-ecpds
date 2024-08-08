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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Objects;

/**
 * The Class Activity.
 */
public class Activity extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4846968819809063552L;

    /** The act agent. */
    protected String ACT_AGENT;

    /** The act host. */
    protected String ACT_HOST;

    /** The act id. */
    protected long ACT_ID;

    /** The act plugin. */
    protected String ACT_PLUGIN;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The ecuser. */
    protected ECUser ecuser;

    /**
     * Instantiates a new activity.
     */
    public Activity() {
    }

    /**
     * Instantiates a new activity.
     *
     * @param id
     *            the id
     */
    public Activity(final long id) {
        setId(id);
    }

    /**
     * Instantiates a new activity.
     *
     * @param id
     *            the id
     */
    public Activity(final String id) {
        setId(id);
    }

    /**
     * Gets the agent.
     *
     * @return the agent
     */
    public String getAgent() {
        return ACT_AGENT;
    }

    /**
     * Sets the agent.
     *
     * @param param
     *            the new agent
     */
    public void setAgent(final String param) {
        ACT_AGENT = param;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return ACT_HOST;
    }

    /**
     * Sets the host.
     *
     * @param param
     *            the new host
     */
    public void setHost(final String param) {
        ACT_HOST = param;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return ACT_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final long param) {
        ACT_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        ACT_ID = Long.parseLong(param);
    }

    /**
     * Gets the plugin.
     *
     * @return the plugin
     */
    public String getPlugin() {
        return ACT_PLUGIN;
    }

    /**
     * Sets the plugin.
     *
     * @param param
     *            the new plugin
     */
    public void setPlugin(final String param) {
        ACT_PLUGIN = param;
    }

    /**
     * Gets the EC user name.
     *
     * @return the EC user name
     */
    public String getECUserName() {
        return ECU_NAME;
    }

    /**
     * Sets the EC user name.
     *
     * @param param
     *            the new EC user name
     */
    public void setECUserName(final String param) {
        ECU_NAME = param;
    }

    /**
     * Gets the EC user.
     *
     * @return the EC user
     */
    public ECUser getECUser() {
        return ecuser;
    }

    /**
     * Sets the EC user.
     *
     * @param param
     *            the new EC user
     */
    public void setECUser(final ECUser param) {
        ecuser = param;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(ACT_ID);
    }

    /**
     * Equals.
     *
     * @param obj
     *            the obj
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (Activity) obj;
        return ACT_ID == other.ACT_ID;
    }
}
