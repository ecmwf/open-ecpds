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

package ecmwf.ecpds.master.plugin.http.model;

import ecmwf.ecpds.master.plugin.http.ECMWFException;

/**
 * ModelException.java
 *
 *
 * Created: Wed Apr 24 11:13:05 2002
 *
 * @author <a href="mailto: "Daniel Varela Santoalla</a>
 *
 * @version
 */

public class ModelException extends ECMWFException {

    public ModelException(String message, Throwable t) {
        super(message, t);
    }

    public ModelException(String message) {
        super(message);
    }

} // ModelBeanException
