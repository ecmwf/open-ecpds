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

package ecmwf.ecpds.master.plugin.http.model.users;

import java.util.Collection;

import ecmwf.ecpds.master.plugin.http.model.ModelBean;

public interface ACL extends ModelBean {

    public static String READ = "Read";
    public static String WRITE = "Write";

    public Collection getPolicies();

    public int getLevel();

    public boolean getNeedsLogin();

    public String getType();

    /**
     * In the case of hierarchical access controls (our case now), the resource that defines the ACL may not be the one
     * we are querying, but one of its ancestors. This methods will return that one.
     *
     * @return The resource which "owns" this ACL
     */
    public Resource getAuthoritativeResource();

    // Setters, only should be allowed to "priviledged classes"
    public void setPolicies(String policies) throws IllegalAccessException;

    public void setLevel(int level) throws IllegalAccessException;;

    public void setNeedsLogin(boolean b) throws IllegalAccessException;;

}
