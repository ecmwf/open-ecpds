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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

/**
 * Retrieves an internationalized message and creates a help link.
 *
 * @author Daniel Varela
 */
public class HelpTag extends TagSupport {

    private String key = "help.default";
    private String target = "help";
    private String iconKey = null;

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTarget() {
        return this.target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getIconKey() {
        return this.iconKey;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
    }

    public int doStartTag() throws JspException {
        String helpLink = TagUtils.getResource(pageContext, TagUtils.parseMultiExpressionText(pageContext, this.key),
                "#");
        String iconUrl = TagUtils.getResource(pageContext, this.iconKey != null ? this.iconKey : "icon.help", "#");
        try {
            pageContext.getOut().write("<a href=\"#\" onClick=\"javascript:w=window.open('" + helpLink + "','"
                    + this.target
                    + "','toolbar=no,status=no,scrollbars=yes,location=no,menubar=no,directories=no,width=800,height=600');w.focus()\"><img border=\"0\" alt=\"A Question Mark\" title=\"Get Help!\" src=\""
                    + iconUrl + "\"></a>");
        } catch (IOException e) {
            // Preserve the legacy tag's handling of output errors.
        }
        return SKIP_BODY;
    }
}
