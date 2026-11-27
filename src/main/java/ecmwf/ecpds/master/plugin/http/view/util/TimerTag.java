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

package ecmwf.ecpds.master.plugin.http.view.util;

import java.util.Date;

import javax.servlet.jsp.JspPage;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.util.bean.Treble;

/**
 * Stores a checkpoint in page scope and logs the elapsed time between checkpoints.
 *
 * @author Daniel Varela
 */
public class TimerTag extends TagSupport {

    private static Log log = LogFactory.getLog(TimerTag.class);
    private static final String KEY = "TIMER_KEY";
    private String name = "checkpoint";
    private int warnIfLongerThan = 5;

    public void setName(String name) {
        this.name = name;
    }

    public void setWarnIfLongerThan(String value) {
        this.warnIfLongerThan = Integer.parseInt(value);
    }

    @SuppressWarnings("removal")
    public int doStartTag() {
        Object o = pageContext.getAttribute(KEY);
        if (o != null && o instanceof Treble) {
            Treble checkpoint = (Treble) o;
            Integer serial = (Integer) checkpoint.getFirst();
            Integer nextSerial = new Integer(serial.intValue() + 1);
            String name = checkpoint.getSecond().toString();
            Date checkpointDate = (Date) checkpoint.getThird();
            Date current = new Date();
            double diff = (current.getTime() - checkpointDate.getTime()) / 1000.0;

            if (diff > warnIfLongerThan) {
                JspPage jspPage = (JspPage) pageContext.getAttribute(pageContext.PAGE);
                log.warn(jspPage + " took " + diff + " seconds, from checkpoint '" + name + "(" + serial + ")' to '"
                        + this.name + "(" + nextSerial + ")'. Performance must be reviewed.");
            } else if (log.isDebugEnabled()) {
                JspPage jspPage = (JspPage) pageContext.getAttribute(pageContext.PAGE);
                log.debug(jspPage + " took " + diff + " seconds, from checkpoint '" + name + "(" + serial + ")' to '"
                        + this.name + "(" + nextSerial + ")'.");
            }

            checkpoint.setFirst(nextSerial);
            checkpoint.setSecond(this.name);
            checkpoint.setThird(current);
        } else {
            pageContext.setAttribute(KEY, new Treble(new Integer(1), this.name, new Date()));
        }
        return SKIP_BODY;
    }
}
