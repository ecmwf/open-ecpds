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
 * Created on 20-Nov-2006
 *
 */
package ecmwf.ecpds.master.plugin.http.services.content.html;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Substitution;

import ecmwf.ecpds.master.plugin.http.services.content.CommandService;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;

public class CommandSubstitution implements Substitution {

    private static Log log = LogFactory.getLog(CommandSubstitution.class);
    private Content container = null;
    private ContentProcessingOptions options = null;

    protected CommandSubstitution(Content container, ContentProcessingOptions options) {
        this.container = container;
        this.options = options;
    }

    public void appendSubstitution(StringBuffer appendBuffer, MatchResult match, int substitutionCount,
            PatternMatcherInput originalInput, PatternMatcher matcher, Pattern pattern) {

        String command = match.group(1);
        String params = match.group(2);
        String body = match.group(3);
        String endCommand = match.group(4);

        if (endCommand != null && !command.equals(endCommand))
            log.warn("Command that starts with '" + command + "' is ending with '" + endCommand + "'");

        try {
            appendBuffer.append(CommandService.executeCommand(this.container, command + " " + params,
                    (body != null ? body : ""), this.options, true));
        } catch (ContentProcessorException e) {
            log.error("Error processing command '" + command + "' with params '" + params + "' and body '" + body + "'",
                    e);
            appendBuffer.append("[ERROR]");
        }

    }

}
