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
 *
 */
package ecmwf.ecpds.master.plugin.http.view.references;

import java.io.StringWriter;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import ecmwf.ecpds.master.plugin.http.model.ModelUtils;
import ecmwf.ecpds.master.plugin.http.services.content.Command;
import ecmwf.ecpds.master.plugin.http.services.content.CommandBase;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;

/**
 * Content Command wrapper on ReferenceBeanSearchTag, it can insert item information into any processed content.
 *
 * TODO: Make this generic, since the Command wrapper for a Tag is always the same
 *
 * @author sy8
 */
public class ReferenceBeanSearchCommand extends CommandBase implements Command {

    public String getOutput(Content container, Map parameters, String body, ContentProcessingOptions options)
            throws ContentProcessorException {

        try {

            Object o;
            if ((o = options.getExternalContext()) != null && (o instanceof PageContext)) {

                PageContext context = (PageContext) o;

                ReferenceBeanSearchTag tag = new ReferenceBeanSearchTag();
                ModelUtils.populateBeanFromMap(tag, parameters, true);

                StringWriter out = new StringWriter();
                tag.print(out, context);
                return out.toString();

            } else {
                return "[" + getName() + ": Needs a JSP PageContext to work]";
            }

        } catch (Exception e) {
            throw new ContentProcessorException("Problem executing commmand", e);
        }

    }

}