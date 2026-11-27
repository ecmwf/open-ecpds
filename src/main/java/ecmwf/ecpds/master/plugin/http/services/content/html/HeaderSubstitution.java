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

public class HeaderSubstitution implements Substitution {

    private static Log log = LogFactory.getLog(HeaderSubstitution.class);
    private int tocNumber = 0;

    protected HeaderSubstitution() {
    }

    public void appendSubstitution(StringBuffer appendBuffer, MatchResult match, int substitutionCount,
            PatternMatcherInput originalInput, PatternMatcher matcher, Pattern pattern) {

        String level = match.group(1);
        String body = match.group(2);
        String endLevel = match.group(3);

        if (endLevel != null && !level.equals(endLevel))
            log.warn("Level '" + level + "' header is ending with level'" + endLevel + "' header");

        appendBuffer.append("<h" + level + "><a name=\"" + HTMLContentProcessor.TOC_ANCHOR_PREFIX + (tocNumber++)
                + "\">" + body + "</h" + endLevel + ">");

    }

}
