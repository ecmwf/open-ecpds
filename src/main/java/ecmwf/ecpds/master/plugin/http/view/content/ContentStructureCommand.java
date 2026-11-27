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
 * Created on 17-Dec-2004
 */
package ecmwf.ecpds.master.plugin.http.view.content;

import java.util.Map;

import ecmwf.ecpds.master.plugin.http.services.content.Command;
import ecmwf.ecpds.master.plugin.http.services.content.CommandBase;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorService;

/**
 * Print the structure of the Content where this command is embedded.
 *
 * @author sy8
 */
public class ContentStructureCommand extends CommandBase implements Command {

    private String defaultProperty = "body";
    private int defaultStartLevel = 1;

    public String getOutput(Content container, Map parameters, String body, ContentProcessingOptions options)
            throws ContentProcessorException {
        String property = parameters.containsKey("property") ? parameters.get("property").toString()
                : this.defaultProperty;
        int startLevel = parameters.containsKey("startLevel")
                ? Integer.parseInt(parameters.get("startLevel").toString()) : this.defaultStartLevel;
        return ContentProcessorService.getStructure(container, property, startLevel, options).getStringRepresentation();
    }
}
