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

/*
 * Created on 25-Jan-2005
 *
 */
package ecmwf.ecpds.master.plugin.http.home.users;

import ecmwf.ecpds.master.plugin.http.dao.ModelSearchBase;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;

/**
 * <p>
 * Search that tries to get a validated user, after supplying uid, password, host, agent and comment.
 * </p>
 * <p>
 * This search but default is marked as NOT CACHEABLE
 * </p>
 *
 * @author sy8
 *
 *         TODO: Comment ME !!!
 */
public class UserByUidAndPass extends ModelSearchBase implements ModelSearch {

    private String uid = "";
    private String pass = "";
    private String host = "";
    private String agent = "";
    private String comment = "";

    public UserByUidAndPass(String uid, String pass, String host, String agent, String comment) {
        this.uid = uid;
        this.pass = pass;
        this.host = host;
        this.agent = agent;
        this.comment = comment;
        this.setCacheable(false);
    }

    /**
     * @return Returns the agent.
     */
    public String getAgent() {
        return agent;
    }

    /**
     * @return Returns the comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * @return Returns the host.
     */
    public String getHost() {
        return host;
    }

    /**
     * @return Returns the pass.
     */
    public String getPass() {
        return pass;
    }

    /**
     * @return Returns the uid.
     */
    public String getUid() {
        return uid;
    }
}
