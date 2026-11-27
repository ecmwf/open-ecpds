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
 * Created on 30-ene-2005
 */
package ecmwf.ecpds.master.plugin.http.view.news;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;
import ecmwf.ecpds.master.plugin.http.services.content.Command;
import ecmwf.ecpds.master.plugin.http.services.content.CommandBase;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;

/**
 * @author daniel
 */
public class ImageCommand extends CommandBase implements Command {

    public String getOutput(Content container, Map params, String body, ContentProcessingOptions options)
            throws ContentProcessorException {
        String out = "";
        if (params.containsKey("name")) {
            if (params.containsKey("caption")) {
                out += "<table><tr><td>";
            }

            Content image = container.getLinkByName(params.get("name").toString());
            out += "<img src=\"" + image.getBasePathKey() + image.getAccessPath() + "\" "
                    + transformParametersIntoAttributes(params) + "/>";

            if (params.containsKey("caption")) {
                out += "</td></tr><tr><td>";
                Map context = new HashMap(1);
                context.put("image", image);
                out += TagUtils.parseMultiExpressionText(context, params.get("caption").toString());
                out += "</td></tr></table>";
            }
            return out;
        } else {
            return "['" + getName() + "' needs a 'name' parameter]";
        }
    }

    private String transformParametersIntoAttributes(Map params) {
        String out = "";
        Iterator i = params.entrySet().iterator();
        while (i.hasNext()) {
            Entry e = (Entry) i.next();
            if (Arrays.asList(new Object[] { "align", "height", "width", "title", "alt", "border" })
                    .contains(e.getKey()))
                out += e.getKey() + "=\"" + e.getValue() + "\" ";
        }
        return out;
    }
}
