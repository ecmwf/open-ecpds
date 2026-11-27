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

package ecmwf.ecpds.master.plugin.http.util.bean;

import java.io.Serializable;

/**
 * Represents a pair name,value. Useful when supplying labels with values to forms.
 *
 * @author sy8
 */
public class StringPair implements Serializable {
    private String name;
    private String value;

    public StringPair() {
        this.name = "";
        this.value = "";
    }

    public StringPair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public String getFirst() {
        return this.name;
    }

    public String getSecond() {
        return this.value;
    }

    public String toString() {
        return "(" + this.getFirst() + "," + this.getSecond() + ")";
    }
}
