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

package ecmwf.ecpds.master.plugin.http.services.content;

import java.util.ArrayList;
import java.util.Collection;

import ecmwf.ecpds.master.plugin.http.services.content.Command;

/**
 * Base implementation for content commands.
 *
 * @author sy8
 */
public abstract class CommandBase implements Command {
    private String name = null;
    private String description = null;

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setName(String s) {
        this.name = s;
    }

    public void setDescription(String s) {
        this.description = s;
    }

    public Collection getParameters() {
        return new ArrayList(0);
    }
}
