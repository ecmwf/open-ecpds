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

/**
 * Created on Oct 15, 2003
 *
 */
package ecmwf.ecpds.master.plugin.http;

/**
 * @author sy8
 */

public class ECMWFRecoverableException extends ECMWFException {

    private String dataKey;
    private Object dataObject;
    private String returnLabel;
    private String returnLink;

    public ECMWFRecoverableException(String message, Throwable t, String dataKey, Object dataObject, String returnLabel,
            String returnLink) {
        super(message, t);
        this.dataKey = dataKey;
        this.dataObject = dataObject;
        this.returnLabel = returnLabel;
        this.returnLink = returnLink;
    }

    public String getDataKey() {
        return dataKey;
    }

    public Object getDataObject() {
        return dataObject;
    }

    public String getReturnLabel() {
        return returnLabel;
    }

    public String getReturnLink() {
        return returnLink;
    }

}
