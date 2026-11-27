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
 * Represents each one of the individually addressable resources in the
 * web site.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 1.0
 */

import java.util.Collection;

import ecmwf.ecpds.master.plugin.http.model.ModelBean;

public interface Resource extends ModelBean {

    public static String HOST_PATH_SEPARATOR = "@";
    public static String ORIGINAL_URL_REQUEST_KEY = "original_url_request_key";
    // public static String WEB_SERVICE_CATEGORY_PREFIX = "web/";

    public String getHost();

    public void setHost(String v) throws IllegalAccessException;

    public String getPath();

    public void setPath(String v) throws IllegalAccessException;

    public ACL getReadACL() throws UserException;

    public void setReadACL(ACL acl) throws IllegalAccessException;

    public ACL getWriteACL() throws UserException;

    // This is still needed for compatibility
    public Collection getCategories() throws UserException;

}
