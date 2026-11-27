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

package ecmwf.ecpds.master.plugin.http.view.auth2;

/**
 * Implements a user information tag. It takes its info from the WebUserBean instance stored in the session.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 0.9.0
 */

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import ecmwf.ecpds.master.plugin.http.model.users.User;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
//import ecmwf.ecpds.master.plugin.http.services.log.LogService;

public class UserInfo extends TagSupport {

    private static Log log = LogFactory.getLog(UserInfo.class);
    private String property = "cn";
    private String def = "unknown";

    public UserInfo() {
        // RealmService.log(DEBUG,"UserInfo instantiated !!!");
    }

    /**
     * Hook method called by the taglib container when a "property" attribute is found, for this class to read it.
     *
     * @param property
     *            The user property we will show.
     */

    public void setProperty(String property) {

        this.property = property;

    }

    /**
     * Hook method called by the taglib container when a "default" attribute is found, for this class to read it.
     *
     * @param default
     *            The default value, in case the user or the property is not found.
     */

    public void setDefault(String def) {

        this.def = def;

    }

    /**
     * Hook method called by the taglib container at the start of the tag. We respond EVAL_BODY_TAG, meaning that we
     * want the body of the tag to be evaluated (the "then" and "else" tags probably are there).
     *
     * @return What to do after... EVAL_BODY_TAG
     */

    public int doStartTag() {

        try {

            User b = (User) pageContext.getSession().getAttribute(User.SESSION_KEY);
            if (b == null) {
                pageContext.getOut().print(this.def);
            } else {
                try {
                    Object value = b.getAttributeValue(this.property);
                    if (value == null)
                        pageContext.getOut().print(this.def);
                    else
                        pageContext.getOut().print(value.toString());
                } catch (ModelException e) {
                    log.error("Error getting attribute from User: ", e);
                    pageContext.getOut().print(this.def);
                }
            }

        } catch (IOException ex) {

            log.error("IOError writing User information to screen: ", ex);
            // throw new JspTagException("IOError writing User information to screen: "+ex.getMessage());
        }

        return SKIP_BODY;

    }

    /**
     * Hook method called by the taglib container at the end of the tag body, and after all of it has been evaluated. We
     * first write out the body content, and then call SKIP_BODY to avoid further body evaluations (as would be the
     * case, for example, of a loop tag).
     *
     * @return What to do after... SKIP_BODY
     */

    public int doAfterBody() throws JspTagException {

        return SKIP_BODY;

    }

}
