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
package ecmwf.ecpds.master.plugin.http.view.news;

import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.model.ModelUtils;
import ecmwf.ecpds.master.plugin.http.services.content.Command;
import ecmwf.ecpds.master.plugin.http.services.content.CommandBase;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;

/**
 * Locate a bean identified by "itemKey" in the environment and print its comments with CommentsTag. Translate all
 * parameters to calls to CommentTag setters.
 *
 * @author sy8
 */

public class ComponentsCommand extends CommandBase implements Command {

    private static Log log = LogFactory.getLog(ComponentsCommand.class);

    public String getOutput(Content container, Map parameters, String body, ContentProcessingOptions options)
            throws ContentProcessorException {

        try {

            Object o;
            if ((o = options.getExternalContext()) != null && (o instanceof PageContext)) {

                PageContext context = (PageContext) o;
                ComponentsTag tag = new ComponentsTag();

                ModelUtils.populateBeanFromMap(tag, parameters, true);
                return tag.getComponentsHTML(context, container);

            } else {
                return "[" + getName() + ": Needs a JSP PageContext to work]";
            }

        } catch (Exception e) {
            log.error("Problem executing command (params:" + parameters + ")", e);
            throw new ContentProcessorException("Problem executing command", e);
        }

    }

}