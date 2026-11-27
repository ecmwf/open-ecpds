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

package ecmwf.ecpds.master.plugin.http.model.references;

import ecmwf.ecpds.master.plugin.http.model.ModelBean;

/**
 * Represents the relation between a FieldBean and a ReferenceBean, characterized with a value. The meaning is: that
 * Field exists into that Reference with that value.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 1.0
 */

public interface FieldValue extends ModelBean {

    public Field getField();

    public void setField(Field v);

    public String getValue();

    public void setValue(String v);

    public boolean isRequired();

    public void setRequired(boolean v);

} // FieldValue
