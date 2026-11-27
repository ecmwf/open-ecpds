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

package ecmwf.ecpds.master.plugin.http.view.general;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

/**
 * Looks up a message and sets a pageContext variable if it matches an optional value.
 *
 * @author Daniel Varela
 */
public class MessageTag extends TagSupport {

    protected String var = null;
    protected String name = null;
    private String ifMatches = null;

    public void setIfMatches(String ifMatches) {
        this.ifMatches = ifMatches;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public int doStartTag() throws JspException {
        String value = TagUtils.getResource(pageContext, this.name, "");
        if (this.ifMatches == null || this.ifMatches.equals(value)) {
            pageContext.setAttribute(this.var, value);
        }
        return SKIP_BODY;
    }
}
