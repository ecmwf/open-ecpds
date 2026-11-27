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
package ecmwf.ecpds.master.plugin.http.view.users;

import java.io.StringWriter;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.home.users.org.OrgPersonHome;
import ecmwf.ecpds.master.plugin.http.model.ModelUtils;
import ecmwf.ecpds.master.plugin.http.model.users.org.OrgPerson;
import ecmwf.ecpds.master.plugin.http.model.users.org.OrgUnit;
import ecmwf.ecpds.master.plugin.http.services.content.Command;
import ecmwf.ecpds.master.plugin.http.services.content.CommandBase;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;

/**
 * Content Command wrapper on the OrganigramTag, it can insert organigram information into any processed content.
 *
 * @author sy8
 */
public class OrganigramCommand extends CommandBase implements Command {

    private static Log log = LogFactory.getLog(OrganigramCommand.class);

    public String getOutput(Content container, Map parameters, String body, ContentProcessingOptions options)
            throws ContentProcessorException {

        try {

            Object o;
            if ((o = options.getExternalContext()) != null && (o instanceof PageContext)) {

                PageContext context = (PageContext) o;

                OrgPerson person = null;
                Object uid = parameters.get("uid");
                if (uid != null)
                    person = OrgPersonHome.findByUid(uid.toString());
                else
                    return "[" + getName() + ": Needs an 'uid' parameter]";
                OrgUnit unit = person.getUnit();

                OrganigramUnitsTag tag = new OrganigramUnitsTag();
                ModelUtils.populateBeanFromMap(tag, parameters, true);

                if (!parameters.containsKey("showArrows")) {
                    tag.setShowArrows("false");
                }

                StringWriter out = new StringWriter();
                tag.print(out, context, unit, unit, 0);
                return out.toString();

            } else {
                return "[" + getName() + ": Needs a JSP PageContext to work]";
            }

        } catch (Exception e) {
            log.error("Problem executing command", e);
            return "[" + getName() + ": Error]";
        }

    }

}