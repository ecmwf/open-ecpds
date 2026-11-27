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
 * Created on Oct 14, 2003
 *
 * TODO: Documentation lacking.
 */
package ecmwf.ecpds.master.plugin.http.services.users;

import ecmwf.ecpds.master.plugin.http.ECMWFException;

/**
 * @author sy8
 *
 *         TODO: Documentation lacking.
 */
public class UsersServiceException extends ECMWFException {

    public UsersServiceException(String message) {
        super(message);
    }

    public UsersServiceException(String message, Throwable e) {
        super(message, e);
    }

}
