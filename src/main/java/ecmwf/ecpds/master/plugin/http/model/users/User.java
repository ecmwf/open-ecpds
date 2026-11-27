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

/**
 * This bean represents the Web User information. It is meant to be inserted
 * in the session whenever we can get the relevant data. ALL user operations
 * (get data, check permissions, etc) should be done through this object.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 1.0
 */

import java.security.Principal;
import java.util.Collection;
import java.util.Date;

import ecmwf.ecpds.master.plugin.http.model.ModelBean;

public interface User extends ModelBean, Comparable, Cloneable, Principal {

    public final static String SESSION_KEY = "ecmwf_user";

    // Authentication method valid values.
    public final static int ANONYMOUS = 1;
    public final static int CERTIFICATE = 2;
    public final static int DOMAIN = 3;
    public final static int DOMAIN_DATA = 4;
    public final static int IP_ADDRESS = 5;
    public final static int HTTP = 6;
    public final static int HTTPS = 7;

    public String getUid();
    // public void setUid(String v);

    public String getRemoteUser();

    public void setRemoteUser(String v);

    public String getCommonName();
    // public void setCommonName(String v);

    public String getFriendlyName();

    public Category getCategory() throws UserException;

    public Collection getPolicies() throws UserException;

    public Collection getCategories() throws UserException;

    public String getPassword();

    public String getAuthenticationMethod();

    public void setAuthenticationMethod(int authenticationMethod);

    public void setUserData(UserData data);

    public UserData getUserData();

    public boolean hasAccess(String path) throws UserException;

    public boolean hasAccess(String host, String path) throws UserException;

    public boolean hasAccess(Resource r) throws UserException;

    public boolean hasPolicy(String name) throws UserException;

    public boolean canRead(Resource r) throws UserException;

    public boolean canWrite(Resource r) throws UserException;

    public Object getCredentials();

    public void setCredentials(Object o);

    public Date getLastCredentialCheck();

    public void setLastCredentialCheck(Date d);

    public int compareTo(Object o);

    public User getDuplicate();
    // public Subject getSubject();

    /**
     * "Demote" the user to the lowest public user possible, that preconfigured one. This is intended to be used
     * directly by the authentication check tags, when they need to "invent" an user due to the imposibility of getting
     * it by any mean, but just as a fallback to avoid worse problems (users not accessing anything).
     */
    public void setAsFallbackPublic();

    public boolean isAnonymous();

    public String dump(String format);

}
