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

package ecmwf.ecpds.master.plugin.http.view.auth2.conditional;

/**
 * ElseTag.java
 * This class implements a "else" authorization tag. It will look up for a parent <code>IfTag</code>, and then ask
 * it for the compliancy of this request to its requirements. If it <b>DOESN'T</b> comply then the tag will
 * evaluate its content, which could include any other "if" tags. If complies, it will show nothing.
 *
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 0.9.0
 */

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class ElseTag extends BodyTagSupport {

    private static Log log = LogFactory.getLog(ElseTag.class);

    public ElseTag() {
    }

    /**
     * Hook method called by the taglib container at the start of the tag. We access to the parent tag to see if the
     * user is or not enabled to see content here, but, as we are in the "else" part, we reverse that result. So if the
     * user is NOT authorized, we will show this tag body (return EVAL_BODY_TAG), and if the user is authorized, we will
     * skip it (return SKIP_BODY).
     *
     * @return EVAL_BODY_TAG or SKIP_BODY, depending on parent.userEnabled() returning false or true.
     */

    public int doStartTag() {

        Tag parent = this.getParent();

        try {

            IfTag ifTag = (IfTag) parent;

            if (ifTag != null && !ifTag.userEnabled()) {
                return EVAL_BODY_BUFFERED;
            } else {
                return SKIP_BODY;
            }

        } catch (ClassCastException e) {
            log.error("Badly located ElseTag. It should be inside a <if> body: " + e.getMessage());
            return SKIP_BODY;
        }
    }

    /**
     * Hook method called by the taglib container at the end of the tag body, and after all of it has been evaluated. We
     * first write out the body content, and then call SKIP_BODY to avoid further body evaluations (as would be the
     * case, for example, of a loop tag).
     *
     * @return What to do after... SKIP_BODY
     */

    public int doAfterBody() throws JspTagException {

        try {

            bodyContent.writeOut(bodyContent.getEnclosingWriter());
            return SKIP_BODY;

        } catch (IOException ex) {
            throw new JspTagException(ex.toString());
        }

    }

}
